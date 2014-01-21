package org.wso2.carbon.governance.platform.extensions.mediation;

/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

import java.util.ArrayList;
import java.util.List;

public class SequenceBean implements ArtifactBean{

    private String name;
    private String onErrorSequence;
    private String trace;
    private List<String> mediators = new ArrayList<String>();
    private List<String> endpointList = new ArrayList<String>();
    private List<String> dependentSequenceList = new ArrayList<String>();

    public List<String> getDependentSequenceList() {
        return dependentSequenceList;
    }

    public void setDependentSequenceList(List<String> dependentSequenceList) {
        this.dependentSequenceList = dependentSequenceList;
    }

    public void setEndpointList(List<String> endpointList) {
        this.endpointList = endpointList;
    }

    public List<String> getEndpointList() {
        return endpointList;
    }

    public String getName() {
        return name;
    }

    public String getOnErrorSequence() {
        return onErrorSequence;
    }

    public String getTrace() {
        return trace;
    }

    public List<String> getMediators() {
        return mediators;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOnErrorSequence(String onErrorSequence) {
        this.onErrorSequence = onErrorSequence;
    }

    public void setTrace(String trace) {
        this.trace = trace;
    }

    public void setMediators(List<String> mediators) {
        this.mediators = mediators;
    }
}
