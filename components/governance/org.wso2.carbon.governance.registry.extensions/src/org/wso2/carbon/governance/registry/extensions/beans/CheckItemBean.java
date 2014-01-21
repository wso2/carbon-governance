package org.wso2.carbon.governance.registry.extensions.beans;

import java.util.ArrayList;
import java.util.List;

public class CheckItemBean {
    private List<PermissionsBean> permissionsBeans;
    private String name;
    private List<CustomCodeBean> validationBeans;
    private List<String> events;

    public CheckItemBean() {
        this.permissionsBeans = new ArrayList<PermissionsBean>();
        this.name="";
        this.validationBeans = new ArrayList<CustomCodeBean>();
        this.events = new ArrayList<String>();
    }

    public List<String> getEvents() {
        return events;
    }

    public void setEvents(List<String> events) {
        this.events = events;
    }

    public List<CustomCodeBean> getValidationBeans() {
        return validationBeans;
    }

    public void setValidationBeans(List<CustomCodeBean> validationBeans) {
        this.validationBeans = validationBeans;
    }

    public List<PermissionsBean> getPermissionsBeans() {
        return permissionsBeans;
    }

    public void setPermissionsBeans(List<PermissionsBean> permissionsBeans) {
        this.permissionsBeans = permissionsBeans;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
