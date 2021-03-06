package org.openengsb.experiments.provider.model;

public class TestModelObject {
    private String key;
    private Object value;
    private Class<?> type;
    
    public TestModelObject() {
    }
    
    public TestModelObject(String key, Object value, Class<?> type) {
        this.key = key;
        this.value = value;
        this.type = type;
    }
    
    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;        
    }
    public Object getValue() {
        return value;
    }
    public void setValue(Object value) {
        this.value = value;
    }

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    
}
