package org.wso2.carbon.governance.registry.extensions.beans;

public class CustomCodeBean {
    private Object classObeject;
    private String eventName;

    public Object getClassObeject() {
        return classObeject;
    }
    public void setClassObeject(Object classObeject) {
        this.classObeject = classObeject;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }
}
