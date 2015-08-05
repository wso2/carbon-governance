/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

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
