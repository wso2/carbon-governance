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
package org.wso2.carbon.governance.generic.beans;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

import java.util.Date;
import java.text.Format;
import java.text.SimpleDateFormat;

@SuppressWarnings({"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
public class ArtifactBean {

    private String[] valuesA;
    private String[] valuesB;
    private String path;
    private boolean canDelete;
    private String LCName;
    private String LCState;

    private Date createdDate ;
    private Date lastUpdatedDate ;
    private String createdBy ;
    private String lastUpdatedBy ;

    public String[] getValuesA() {
        return valuesA;
    }

    public void setValuesA(String[] valuesA) {
        this.valuesA = valuesA;
    }

    public String[] getValuesB() {
        return valuesB;
    }

    public void setValuesB(String[] valuesB) {
        this.valuesB = valuesB;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean getCanDelete() {
        return canDelete;
    }

    public void setCanDelete(boolean canDelete) {
        this.canDelete = canDelete;
    }

    public String getLCName() {
        return LCName;
    }

    public void setLCName(String LCName) {
        this.LCName = LCName;
    }

    public String getLCState() {
        return LCState;
    }

    public void setLCState(String LCState) {
        this.LCState = LCState;
    }

    public void setCreatedDate(Date date) {
        this.createdDate = date ;
    }

    public String getCreatedDate() {
        Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String createdDateStr = formatter.format(createdDate);
        return createdDateStr;
    }

    public void setLastUpdatedDate(Date date) {
        this.lastUpdatedDate = date ;
    }

    public String getLastUpdatedDate() {
        Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String lastUpdatedDateStr = formatter.format(lastUpdatedDate);
        return lastUpdatedDateStr;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy ;
    }

    public String getCreatedBy() {
        return createdBy ;
    }

    public void setLastUpdatedBy(String lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy ;
    }

    public String getLastUpdatedBy() {
        return lastUpdatedBy ;
    }
}
