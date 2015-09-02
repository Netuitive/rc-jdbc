package com.netuitive.remote.plugin.jdbc;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import com.netuitive.common.remote.RemoteProperty;

public class JdbcDataSourceManager {

    private static final Log log = LogFactory.getLog(JdbcDataSourceManager.class);

    private Map<String, BasicDataSource> dataSourceCache;

    private static JdbcDataSourceManager instance;
    
    private JdbcDataSourceManager() {
        super();
        this.dataSourceCache = new HashMap<String, BasicDataSource>();
    }
    
    /**
     * 
     * @return
     */
    public static JdbcDataSourceManager getInstance() {
        if(instance == null) {
            instance = new JdbcDataSourceManager();
        }
        return instance;
    }

    /**
     * Retreive connection 
     * @param rootNode remote property
     * @return connection to database
     * @throws IllegalArgumentException
     * @throws SQLException
     */
    public Connection getConnection(RemoteProperty rp, XMLConfigurationHelper xmlConfigurationHelper) throws IllegalArgumentException, SQLException {
        if(!rp.isRoot()) 
            throw new IllegalArgumentException ( " RemoteProperty must be root node! ");
        
        String instanceName = rp.getName();
        if(this.getDataSource(instanceName) == null) 
            createBasicDataSource(rp, xmlConfigurationHelper);
        
        return this.getDataSource(instanceName).getConnection();
    }
    
    /**
     * create basic data source and cache it
     * @param rp
     */
    private void createBasicDataSource(RemoteProperty rp, XMLConfigurationHelper xmlConfigurationHelper) {
        BasicDataSource bds = new BasicDataSource();
        bds.setDriverClassName(xmlConfigurationHelper.getJdbcDriver());
        bds.setUrl(buildURL(rp, xmlConfigurationHelper));
        bds.setUsername(rp.getProperties().getProperty("username"));
        bds.setPassword(rp.getProperties().getProperty("password"));
        bds.setValidationQuery(xmlConfigurationHelper.getvalidationQuery());
        
        /**
         * add to cache
         */
        this.dataSourceCache.put(rp.getName(), bds);
    }
    
    private String buildURL(RemoteProperty rp, XMLConfigurationHelper xmlConfigurationHelper) {
        String returnURL = null;
        
        VelocityContext context = new VelocityContext();
        context.put("hostname", rp.getProperties().getProperty("hostname"));
        context.put("port", rp.getProperties().getProperty("port"));
        context.put("database", rp.getProperties().getProperty("database"));

        StringWriter writer = new StringWriter();
        try {
            Velocity.evaluate(context, writer, "TemplateName", xmlConfigurationHelper.getUrl());
            returnURL = writer.toString();
        } catch (ParseErrorException e) {
            log.error("Error parsing database URL: " + e.toString());
        } catch (MethodInvocationException e) {
            log.error("Method error parsing database URL: " + e.toString());
        } catch (ResourceNotFoundException e) {
            log.error("Unable to parse database URL: " + e.toString());
        } catch (IOException e) {
            log.error("IO error parsing database URL: " + e.toString());
        } 
        return returnURL;
        
    }
    
    /**
     * this should be called by producer's shutdown method
     * @param pluginInstance
     */
    public void destroyDataSource(String pluginInstance) {
        BasicDataSource bds = this.dataSourceCache.get(pluginInstance);
        try {
            if(bds!=null) 
                bds.close();
        } catch (SQLException e) {
            // do nothing, we're out of here
        }
        this.dataSourceCache.remove(pluginInstance);        
    }

    /**
     * retrieve plugin specific datasource
     * @param pluginInstance
     * @return
     */
    private BasicDataSource getDataSource(String pluginInstance) {
        return this.dataSourceCache.get(pluginInstance);
    }
    
}