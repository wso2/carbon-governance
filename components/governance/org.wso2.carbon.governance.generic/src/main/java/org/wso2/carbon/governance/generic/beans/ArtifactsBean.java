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

@SuppressWarnings({"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
public class ArtifactsBean {

    private String[] names;
    private String[] types;
    private String[] keys;
    private ArtifactBean[] artifacts;

    public String[] getNames() {
        return names;
    }

    public String[] getTypes() {
        return types;
    }

    public void setNames(String[] names) {
        this.names = names;
    }

    public void setTypes(String[] types) {
        this.types = types;
    }

    public ArtifactBean[] getArtifacts() {
        return artifacts;
    }

    public void setArtifacts(ArtifactBean[] artifacts) {
        this.artifacts = artifacts;
    }

    public String[] getKeys() {
        return keys;
    }

    public void setKeys(String[] keys) {
        this.keys = keys;
    }
}
