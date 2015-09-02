package com.netuitive.remote.plugin.jdbc;

import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.netuitive.common.remote.RemoteProperty;
import com.netuitive.remote.producers.AbstractProducer;

public class JdbcProducer extends AbstractProducer{

    private static final Log log = LogFactory.getLog(JdbcProducer.class);

    private JdbcDataSourceManager jdbcDataSourceManager;

    private RemoteProperty rp;
    
    protected XMLConfigurationHelper xmlConfigurationHelper;

    @Override
    protected TimerTask getTimerTask() {
        return new JdbcProducerTimerTask(this);
    }

    public void setXMLConfigurationHelper(XMLConfigurationHelper xmlConfigurationHelper) {
        this.xmlConfigurationHelper = xmlConfigurationHelper;
    }
       
    @Override
    public synchronized void init() throws Exception {
        xmlConfigurationHelper.processQueries(this.getName(), this.getRp().getProperties().getProperty("queries"));
        super.init();
    }
    
    @Override
    public synchronized void shutdown() throws IllegalStateException {
        /**
         * shutdown datasource
         */
        this.getJdbcDataSourceManager().destroyDataSource(this.getName());
        super.shutdown();
    }

    protected JdbcDataSourceManager getJdbcDataSourceManager() {
        return this.jdbcDataSourceManager;
    }

    protected void setJdbcDataSourceManager(
            JdbcDataSourceManager jdbcDataSourceManager) {
        this.jdbcDataSourceManager = jdbcDataSourceManager;
    }

    public RemoteProperty getRp() {
        return this.rp;
    }

    public void setRp(RemoteProperty rp) {
        this.rp = rp;
    }

}