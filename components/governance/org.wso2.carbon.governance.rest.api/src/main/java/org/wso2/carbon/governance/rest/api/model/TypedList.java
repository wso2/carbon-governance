/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TypedList<T> {

    private Class<T> type;
    private Map<String, List<T>> artifacts = new HashMap();

    public TypedList(Class<T> type, Map<String, List<T>> artifacts) {
        this.type = type;
        this.artifacts = artifacts;
    }

    public TypedList(Class<T> type) {
        this.type = type;
    }

    public TypedList(Class<T> genericArtifactClass, String assetType, List<T> artifactList) {
        this.type = type;
        this.artifacts.put(assetType, artifactList);
    }

    public Class<T> getType() {
        return type;
    }

    public Map<String, List<T>> getArtifacts() {
        return artifacts;
    }

    public void addArtifacts(String shortName, List<T> artifacts) {
        getArtifacts().put(shortName, artifacts);
    }

    public boolean hasData() {
        return artifacts.size() > 0 ? true : false;

    }

    @Override
    public String toString() {
        return "TypedList{" +
               "type=" + type +
               ", artifacts=" + artifacts +
               '}';
    }
}
