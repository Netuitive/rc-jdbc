package com.netuitive.remote.plugin.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.TimerTask;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.netuitive.common.lang.TimePeriod;

public class JdbcProducerTimerTask extends TimerTask {

    private enum Type {
        RELATIONSHIP, COLLECTION, ATTRIBUTE
    }

    private static final Log log = LogFactory.getLog(JdbcProducerTimerTask.class);

    private JdbcProducer p = null;

    JdbcProducerTimerTask(JdbcProducer p) {

        this.p = p;
    }

    @Override
    public void run() {

        Connection conn = null;

        if (log.isDebugEnabled()) {
            log.debug("starting jdbc connection run..");
        }
        /**
         * timing
         */
        this.p.startMeasurement();

        try {
            conn = this.p.getJdbcDataSourceManager().getConnection(this.p.getRp(), this.p.xmlConfigurationHelper);

            Map<String, String> queries = this.p.xmlConfigurationHelper.getCollectionQueries(this.p.getName());
            for (String name : queries.keySet()) {
                try {
                    runQuery(Type.COLLECTION, this.getTimePeriod(), conn, queries.get(name), name);
                } catch (SQLException sqe) {
                    log.error(sqe);
                }
            }

            Map<String, String> relationshipQueries = this.p.xmlConfigurationHelper.getRelationshipQueries(this.p.getName());
            log.debug("relationship queries:" + relationshipQueries);
            for (String name : relationshipQueries.keySet()) {
                try {
                    log.debug("relationship query:" + name + " " + relationshipQueries);
                    runQuery(Type.RELATIONSHIP, this.getTimePeriod(), conn, relationshipQueries.get(name), name);
                } catch (SQLException sqe) {
                    log.error(sqe);
                }
            }

            Map<String, String> attributeQueries = this.p.xmlConfigurationHelper.getAttributeQueries(this.p.getName());
            log.debug("attribute queries:" + attributeQueries);
            for (String name : attributeQueries.keySet()) {
                try {
                    log.debug("attribute query:" + name + " " + attributeQueries);
                    runQuery(Type.ATTRIBUTE, this.getTimePeriod(), conn, attributeQueries.get(name), name);
                } catch (SQLException sqe) {
                    log.error(sqe);
                }
            }

        } catch (IllegalArgumentException e) {
            log.error(e);
        } catch (SQLException e) {
            log.error(e);
        } finally {
            DbUtils.closeQuietly(conn);
            /**
             * timing
             */
            this.p.stopMeasurement();
            log.debug("ended jdbc connection run..");

        }
    }

    private TimePeriod getTimePeriod() {

        int cycleTime = Integer.valueOf(this.p.getRp().getProperties().getProperty("cycleTime")).intValue();

        TimeZone tz = TimeZone.getTimeZone(this.p.getRp().getProperties().getProperty("timezone"));
        Calendar c = Calendar.getInstance(tz, Locale.US);

        /**
         * allows you to roll the cycleTime data window back some number of seconds
         */
        int cycleTimeOffset = Integer.valueOf(this.p.xmlConfigurationHelper.getCycleTimeOffset());
        if (cycleTimeOffset > 0) {
            /** offset the calendar by the offSet time **/
            c.add(Calendar.SECOND, -(cycleTimeOffset));
        }

        /** set endTime = now() or now()-cycleTimeOffset using calendar above **/
        Date endTime = new Date(c.getTime().getTime() + tz.getOffset(c.getTime().getTime()));

        /** set startTime = endTime-cycleTime **/
        c.add(Calendar.SECOND, -(cycleTime));
        Date startTime = new Date(c.getTime().getTime() + tz.getOffset(c.getTime().getTime()));

        if (log.isDebugEnabled())
            log.debug("JDBC getTimePeriod: Start=" + startTime.toString() + " End=" + endTime.toString());

        return new TimePeriod(startTime, endTime);

    }

    @SuppressWarnings("boxing")
    private void runQuery(Type type, TimePeriod timePeriod, Connection conn, String query, String name)
        throws SQLException {

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            if (log.isDebugEnabled()) {
                log.debug("Collection query [name=" + name + ", query= " + query + "]");
            }

            ps = conn.prepareStatement(query);

            setParams(ps, timePeriod);

            if (log.isDebugEnabled()) {
                log.debug("Collecting [name=" + name + ", query= " + ps.toString() + "]");
            }

            // set query timeout
            int queryTimeout = Integer.valueOf(this.p.getRp().getProperties().getProperty("queryTimeout"));
            ps.setQueryTimeout(queryTimeout);

            rs = ps.executeQuery();

            processResultSet(type, rs, timePeriod, name);

        } finally {
            DbUtils.closeQuietly(rs);
            DbUtils.closeQuietly(ps);
        }
    }

    private void processResultSet(Type type, ResultSet rs, TimePeriod timePeriod, String name) {

        int rowsProcessed = 0;
        JdbcPayload jp = new JdbcPayload(name);

        try {

            while (rs.next()) {
                rowsProcessed++;
                switch (type) {
                case COLLECTION:
                    String matchKey = rs.getString(1);
                    Double sampleValue = rs.getDouble(2);

                    jp.add(matchKey, sampleValue);
                    break;
                case ATTRIBUTE:
                    matchKey = rs.getString(1);
                    String value = rs.getString(2);

                    jp.addAttributes(matchKey, value);
                    break;
                case RELATIONSHIP:
                    String relationship = rs.getString(1);

                    jp.addRelationship(relationship);
                    break;
                }
            }
            if (log.isDebugEnabled()) {
                log.debug(this.p.getName() + " processed " + rowsProcessed);
            }

        } catch (SQLException e) {
            log.error(e);
        } finally {
            /**
             * send it off to be processed
             */
            this.p.add(jp);
        }

    }

    protected void setParams(PreparedStatement ps, TimePeriod timePeriod) throws SQLException {

        Timestamp startTime = new Timestamp(timePeriod.getStartTime().getTime());
        Timestamp endTime = new Timestamp(timePeriod.getEndTime().getTime());
        int parameterCount = ps.getParameterMetaData().getParameterCount();
        for (int i = 1; i <= parameterCount; i++) {
            ps.setTimestamp(i, i % 2 == 1
                    ? startTime
                    : endTime);
        }

        if (log.isDebugEnabled())
            log.debug("JDBCPlugin: startTime=" + startTime.toString() + " endTime=" + endTime.toString());
    }
}
