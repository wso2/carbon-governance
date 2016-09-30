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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssetState {
    String state = null;
    String lifecycleName;
    List<String> actions = new ArrayList<>();

    public AssetState() {
    }

    public AssetState(String state, String lifecycleName) {
        this.state = state;
        this.lifecycleName = lifecycleName;
    }

    public String getState() {
        return state;
    }

    public void addActions(String action) {
        actions.add(action);
    }

    public List<String> getActions() {
        return actions;
    }

    public String getLc() {
        return lifecycleName;
    }

    public void setLc(String lifecycle) {
        this.lifecycleName = lifecycle;
    }

    @Override
    public String toString() {
        return "AssetState{" +
               "state='" + state + '\'' +
               ", states=" + actions +
               '}';
    }
}
