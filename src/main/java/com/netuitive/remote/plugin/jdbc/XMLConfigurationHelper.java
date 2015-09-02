package com.netuitive.remote.plugin.jdbc;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class XMLConfigurationHelper {

    private static final Log log = LogFactory.getLog(XMLConfigurationHelper.class);

    private Map<String, Map<String, String>> collectionQueries = new TreeMap<String, Map<String, String>>();
    private Map<String, Map<String, String>> relationshipQueries = new TreeMap<String, Map<String, String>>();
    private Map<String, Map<String, String>> attributeQueries = new TreeMap<String, Map<String, String>>();

    private String jdbcDriver = null;

    private String validationQuery = null;

    private String url = null;

    private int cycleTimeOffset = 0;

    public void processQueries(String instanceName, String xmlFile) {

        XMLConfiguration config = null;
        /**
         * clear'em out
         */
        if (collectionQueries.get(instanceName) != null) {
            collectionQueries.get(instanceName).clear();
        }
        if (relationshipQueries.get(instanceName) != null) {
            relationshipQueries.get(instanceName).clear();
        }

        if (jdbcDriver != null) {
            jdbcDriver = null;
        }

        if (validationQuery != null) {
            validationQuery = null;
        }

        if (url != null) {
            url = null;
        }

        try {
            /**
             * use strange ethiopic unicode character so that it never delimits the properties
             * 
             * This is a workaround becuase config.setDelimiterParsingDisabled(true) doesn't disable parsing
             */
            AbstractConfiguration.setDefaultListDelimiter('\u12BF');
            config = new XMLConfiguration(xmlFile);
            config.setDelimiterParsingDisabled(true);
            Map<String, String> instanceCollectionQueryMap = new TreeMap<String, String>();
            Map<String, String> instanceRelationshipQueryMap = new TreeMap<String, String>();
            Map<String, String> instanceAttributeQueryMap = new TreeMap<String, String>();

            @SuppressWarnings("unchecked")
            List<HierarchicalConfiguration> params = config.configurationsAt("configuration");
            for (HierarchicalConfiguration param : params) {
                this.jdbcDriver = param.getString("driver");
                this.validationQuery = param.getString("validationquery");
                this.url = param.getString("url");
                this.cycleTimeOffset = param.getInt("cycletimeoffset");
                if (log.isDebugEnabled()) {
                    log.debug("jdbcDriver:" + param.getString("driver"));
                    log.debug("validationQuery:" + param.getString("validationquery"));
                    log.debug("url:" + param.getString("url"));
                    log.debug("cycleTimeOffset:" + param.getInt("cycletimeoffset"));
                }
            }

            @SuppressWarnings("unchecked")
            List<HierarchicalConfiguration> queries = config.configurationsAt("queries.query");
            for (HierarchicalConfiguration sub : queries) {
                String name = sub.getString("name");
                String type = sub.getString("type");
                String sql = sub.getString("sql");

                if (type.equalsIgnoreCase("collection")) {
                    instanceCollectionQueryMap.put(name, sql);
                } else if (type.equalsIgnoreCase("relationship")) {
                    instanceRelationshipQueryMap.put(name, sql);
                } else if (type.equalsIgnoreCase("attribute")) {
                    instanceAttributeQueryMap.put(name, sql);
                }

            }

            collectionQueries.put(instanceName, instanceCollectionQueryMap);
            relationshipQueries.put(instanceName, instanceRelationshipQueryMap);
            attributeQueries.put(instanceName, instanceAttributeQueryMap);

        } catch (ConfigurationException e) {
            log.error(e);
        }

    }

    public Map<String, String> getCollectionQueries(String instanceName) {

        return collectionQueries.get(instanceName);
    }

    public Map<String, String> getRelationshipQueries(String instanceName) {

        return relationshipQueries.get(instanceName);
    }

    public Map<String, String> getAttributeQueries(String instanceName) {

        return attributeQueries.get(instanceName);
    }

    public String getJdbcDriver() {

        return this.jdbcDriver;
    }

    public String getvalidationQuery() {

        return this.validationQuery;
    }

    public String getUrl() {

        return this.url;
    }

    public int getCycleTimeOffset() {

        return this.cycleTimeOffset;
    }

}
