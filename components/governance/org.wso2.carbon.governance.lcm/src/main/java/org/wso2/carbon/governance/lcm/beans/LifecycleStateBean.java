/*
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.governance.lcm.beans;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

@SuppressWarnings({"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
public class LifecycleStateBean {

    private String stateName;
    private ChecklistBean checklist;
    private String location;
    private JSBean js;
    private PermissionBean[] permissions;
    private String extensionsXml;
    
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    public ChecklistBean getChecklist() {
        return checklist;
    }

    public void setChecklist(ChecklistBean checklist) {
        this.checklist = checklist;
    }

    public LifecycleStateBean() {
        this.stateName = null;
    }

    public JSBean getJs() {
        return js;
    }

    public void setJs(JSBean js) {
        this.js = js;
    }

    public PermissionBean[] getPermissions() {
        return permissions;
    }

    public void setPermissions(PermissionBean[] permissions) {
        this.permissions = permissions;
    }

    public String getExtensionsXml() {
        return extensionsXml;
    }

    public void setExtensionsXml(String extensionsXml) {
        this.extensionsXml = extensionsXml;
    }
}
