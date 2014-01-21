/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.governance.list.beans;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

@SuppressWarnings({"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
public class WSDLBean {

    private String[] name;
    private String[] namespace;
    private String[] path;
    private boolean[] canDelete;
    private String[] LCName;
    private String[] LCState;
    private int size=0;

    public void setName(String[] name) {
        this.name = name;
    }

    public void setPath(String[] path) {
        this.path = path;
    }

    public String[] getName() {

        return name;
    }

    public String[] getNamespace() {
        return namespace;
    }

    public void setNamespace(String[] namespace) {
        this.namespace = namespace;
    }
    public String[] getPath() {
        return path;
    }
    public void increment(){
        size++;
    }

    public int getSize() {
        return size;
    }

    public boolean[] getCanDelete() {
        return canDelete;
    }

    public void setCanDelete(boolean[] canDelete) {
        this.canDelete = canDelete;
    }

    public String[] getLCName() {
        return LCName;
    }

    public void setLCName(String[] LCName) {
        this.LCName = LCName;
    }

    public String[] getLCState() {
        return LCState;
    }

    public void setLCState(String[] LCState) {
        this.LCState = LCState;
    }
}
