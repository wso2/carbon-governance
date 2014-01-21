package org.wso2.carbon.governance.registry.extensions.aspects.utils;

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.registry.core.Registry;

import java.util.HashMap;
import java.util.Map;

public class StatCollection {

    private Registry registry;
    private String resourcePath;
    private String originalPath;
    private String userName;
    private String state;
    private String action;
    private String actionType;
    private Map<String,OMElement> validations;
    private Map<String,OMElement> executors;
    private String actionValue;
    private String targetState;
    private long timeMillis;

    public String getTargetState() {
        return targetState;
    }

    public void setTargetState(String targetState) {
        this.targetState = targetState;
    }

    public String getOriginalPath() {
        return originalPath;
    }

    public void setOriginalPath(String originalPath) {
        this.originalPath = originalPath;
    }

    public String getActionValue() {
        return actionValue;
    }

    public void setActionValue(String comment) {
        this.actionValue = comment;
    }

    public void addValidations(String validationName,OMElement info){
        if(validations == null){
            validations = new HashMap<String, OMElement> ();
        }
        validations.put(validationName,info);
    }

    public void addExecutors(String executorName,OMElement info){
        if(executors == null){
            executors =  new HashMap<String, OMElement>();
        }
        executors.put(executorName,info);
    }
    public Map<String,OMElement> getValidations() {
        return validations;
    }

    public void setValidations(Map<String,OMElement> validations) {
        this.validations = validations;
    }

    public Map<String,OMElement> getExecutors() {
        return executors;
    }
    public void setExecutors(Map<String,OMElement> executors) {
        this.executors = executors;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Registry getRegistry() {
        return registry;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public long getTimeMillis() {
        return timeMillis;
    }

    public void setTimeMillis(long timeMillis) {
        this.timeMillis = timeMillis;
    }
}
