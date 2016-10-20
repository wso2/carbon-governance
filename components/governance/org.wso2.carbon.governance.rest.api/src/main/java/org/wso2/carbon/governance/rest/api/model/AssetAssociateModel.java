/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.governance.rest.api.model;

public class AssetAssociateModel {

    private String destAssetType;
    private String destAssetID;
    private String sourceAssocType;
    private String destAssocType;

    public String getDestAssetType() {
        return destAssetType;
    }

    public void setDestAssetType(String destAssetType) {
        this.destAssetType = destAssetType;
    }

    public String getDestAssetID() {
        return destAssetID;
    }

    public void setDestAssetID(String destAssetID) {
        this.destAssetID = destAssetID;
    }

    public String getSourceAssocType() {
        return sourceAssocType;
    }

    public void setSourceAssocType(String sourceAssocType) {
        this.sourceAssocType = sourceAssocType;
    }

    public String getDestAssocType() {
        return destAssocType;
    }

    public void setDestAssocType(String destAssocType) {
        this.destAssocType = destAssocType;
    }
}
