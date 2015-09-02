package com.netuitive.remote.plugin.jdbc;


import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.netuitive.common.remote.RemoteProperty;
import com.netuitive.remote.exception.PluginInitializationFailedException;
import com.netuitive.remote.plugin.AbstractRemotePlugin;
import com.netuitive.remote.plugin.RemotePlugin;


public class JdbcPlugin extends AbstractRemotePlugin implements RemotePlugin {

    private static final Log log = LogFactory.getLog(JdbcPlugin.class);
    
    /**
     * default serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    @Override
    public void init(RemoteProperty rp) throws PluginInitializationFailedException {
        
        log.info("Initializing JDBC plugin..");
        
        /**
         * process queries.xml
         */
        String xmlConfigFile = rp.getProperties().getProperty("queries");
        if(xmlConfigFile==null) 
            throw new PluginInitializationFailedException (" queries file cannot be null! ");
        
        validate(xmlConfigFile);
        
        XMLConfigurationHelper xmlConfigurationHelper = new XMLConfigurationHelper();
        xmlConfigurationHelper.processQueries(rp.getName(), xmlConfigFile);
        
        /**
         * wire up producer
         */
        JdbcProducer jdbcProducer = new JdbcProducer();
        this.setProducer(jdbcProducer);
        jdbcProducer.setName(rp.getName());
        jdbcProducer.setCycleTime(Integer.valueOf(rp.getProperties().getProperty("cycleTime")));
        jdbcProducer.setQueue(this.getQueue());
        jdbcProducer.setJdbcDataSourceManager(JdbcDataSourceManager.getInstance());
        jdbcProducer.setRp(rp);
        jdbcProducer.setXMLConfigurationHelper(xmlConfigurationHelper);
        
       
       /**
        * wire up datastrategy
        */
       JdbcDataStrategy jdbcDataStrategy = new JdbcDataStrategy();
       jdbcDataStrategy.setName(rp.getName());
               
       /**
        * finish off producer
        */
       this.getProducer().setDataStrategy(jdbcDataStrategy);
               
       this.setRootNode(rp);
       
       log.info("JDBC plugin created");
   }

   private void validate(String xmlConfigFile) throws PluginInitializationFailedException {
       File xmlFile = new File(xmlConfigFile);
       
       if(!xmlFile.exists() || !xmlFile.canRead())
           throw new PluginInitializationFailedException (xmlConfigFile + " does not exists or cannot be read!");
    }
    
 
}