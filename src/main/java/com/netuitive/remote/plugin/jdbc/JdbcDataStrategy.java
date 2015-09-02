package com.netuitive.remote.plugin.jdbc;

import java.sql.Timestamp;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.netuitive.remote.datastrategies.AbstractDataStrategy;
import com.netuitive.remote.plugin.jdbc.JdbcPayload.AttributeInfo;
import com.netuitive.remote.plugin.jdbc.JdbcPayload.KeyInfo;

public class JdbcDataStrategy extends AbstractDataStrategy<JdbcPayload> {

    private static final Log log = LogFactory.getLog(JdbcDataStrategy.class);

    Pattern pattern = Pattern.compile("([^!]+)!(.+)");

    public Pattern getPattern() {

        return this.pattern;
    }

    public void setPattern(Pattern pattern) {

        this.pattern = pattern;
    }

    public void parseData(JdbcPayload jdbcPayload) throws IllegalArgumentException {

        for (KeyInfo ki : jdbcPayload.getKeyInfos()) {
            if (ki.getMatchKey() != null && ki.getMatchKey().length() > 0) {
                this.process(
                    this.getName(),
                    ki.getMatchKey(),
                    pattern,
                    new Timestamp(new Date().getTime()),
                    ki.getSampleValue());
            }

        }

        log.debug("Relationships: " + jdbcPayload.getRelationships());

        for (String rel : jdbcPayload.getRelationships()) {
            log.debug("Relationship: " + rel);

            if (rel != null && rel.length() > 0) {
                this.processRelationship(this.getName(), rel);

            }
        }

        log.debug("Attributes: " + jdbcPayload.getAttributes());

        for (AttributeInfo att : jdbcPayload.getAttributes()) {
            log.debug("Attribute: " + att);

            if (att != null && att.getMatchKey().length() > 0) {
                Matcher m = pattern.matcher(att.getMatchKey());
                if (m.find()) {
                    this.processAttribute(
                        this.getName(),
                        m.group(1),
                        m.group(2),
                        att.getSampleValue());
                }
            }
        }
    }
}
