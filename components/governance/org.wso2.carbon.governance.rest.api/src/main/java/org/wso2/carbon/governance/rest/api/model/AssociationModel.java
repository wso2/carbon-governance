/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.governance.rest.api.model;

public class AssociationModel {

    private String associationPath;
    private String associationType;
    private String selfLink;
    private String associationArtifactType;

    public void setAssociationPath(String associationPath) {
        this.associationPath = associationPath;
    }

    public String getAssociationPath() {
        return associationPath;
    }

    public void setAssociationType(String associationType) {
        this.associationType = associationType;
    }

    public String getAssociationType() {
        return associationType;
    }

    public String getSelfLink() {
        return selfLink;
    }

    public void setSelfLink(String selfLink) {
        this.selfLink = selfLink;
    }


    public String getAssociationArtifactType() {
        return associationArtifactType;
    }

    public void setAssociationArtifactType(String artifactType) {
        this.associationArtifactType = artifactType;
    }
}
