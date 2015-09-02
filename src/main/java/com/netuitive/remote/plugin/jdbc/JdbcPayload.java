package com.netuitive.remote.plugin.jdbc;

import java.util.ArrayList;
import java.util.List;

import com.netuitive.remote.payload.AbstractPayload;

public class JdbcPayload extends AbstractPayload {

    List<KeyInfo> keys = new ArrayList<KeyInfo>();
    List<AttributeInfo> attributeKeys = new ArrayList<AttributeInfo>();
    List<String> relationshipKeys = new ArrayList<String>();
    
    String name;
    
    public JdbcPayload(String name) {
        super();
        this.name=name;
    }
    
    public void add(String matchKey, Double sampleValue) {
        this.keys.add(new KeyInfo(matchKey, sampleValue));
    }
    
    public void addRelationship(String relationship) {
        this.relationshipKeys.add(relationship);
    }
    
    public List<String> getRelationships() {
        return this.relationshipKeys;
    }
    
    public void addAttributes(String matchKey, String sampleValue) {
        this.attributeKeys.add(new AttributeInfo(matchKey, sampleValue));
    }
    
    public List<AttributeInfo> getAttributes() {
        return this.attributeKeys;
    }
    
    public List<KeyInfo> getKeyInfos() {
        return this.keys;
    }
    
    class KeyInfo {
        String matchKey;
        Double sampleValue;
        
        public KeyInfo(String matchKey, Double sampleValue) {
            this.matchKey = matchKey;
            this.sampleValue = sampleValue;
        }
        
        public String getMatchKey() {
            return this.matchKey;
        }
        public void setMatchKey(String matchKey) {
            this.matchKey = matchKey;
        }
        public Double getSampleValue() {
            return this.sampleValue;
        }
        public void setSampleValue(Double sampleValue) {
            this.sampleValue = sampleValue;
        }
    }

    class AttributeInfo {
        String matchKey;
        String sampleValue;
        
        public AttributeInfo(String matchKey, String sampleValue) {
            this.matchKey = matchKey;
            this.sampleValue = sampleValue;
        }
        
        public String getMatchKey() {
            return this.matchKey;
        }
        public void setMatchKey(String matchKey) {
            this.matchKey = matchKey;
        }
        public String getSampleValue() {
            return this.sampleValue;
        }
        public void setSampleValue(String sampleValue) {
            this.sampleValue = sampleValue;
        }
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }
}