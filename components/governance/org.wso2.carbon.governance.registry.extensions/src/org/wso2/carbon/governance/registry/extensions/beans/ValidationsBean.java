package org.wso2.carbon.governance.registry.extensions.beans;

import java.util.Map;

public class ValidationsBean {
    private String className;
    private Map<String,String> paramNameValues;
    private String eventName;

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Map<String, String> getParamNameValues() {
        return paramNameValues;
    }

    public void setParamNameValues(Map<String, String> paramNameValues) {
        this.paramNameValues = paramNameValues;
    }
}
