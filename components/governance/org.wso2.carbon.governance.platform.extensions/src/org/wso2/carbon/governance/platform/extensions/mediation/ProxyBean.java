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

import org.apache.commons.collections.list.SetUniqueList;

import java.util.ArrayList;
import java.util.List;

public class ProxyBean implements ArtifactBean{

    private String name = null;
    private String transports = null;
    private String startOnLoad = null;
    private String trace  = null;
    private String pinnedServers = null;
    private String serviceGroup = null;

    private String inSequence = null;
    private String outSequence = null;
    private String faultSequence = null;
    private String publishWSDL = null;
    private String endPoint = null;
    private List<String> policies = new ArrayList<String>();
    private List<String> parameters = new ArrayList<String>();
    private List<String> sequenceList = new ArrayList<String>();
    private List<String> endpointList = new ArrayList<String>();
    private String enableAddressing = null;
    private String enableRM = null;
    private String enableSecurity = null;

    public void setEndpointList(List<String> endpointList) {
        this.endpointList = endpointList;
    }

    public List<String> getEndpointList() {
        return endpointList;
    }

    public List<String> getSequenceList() {
        return sequenceList;
    }

    public void setSequenceList(List<String> sequenceList) {
        this.sequenceList = sequenceList;
    }

    public String getName() {

        return name;
    }

    public String getTransports() {
        return transports;
    }

    public String getStartOnLoad() {
        return startOnLoad;
    }

    public String getTrace() {
        return trace;
    }

    public String getPinnedServers() {
        return pinnedServers;
    }

    public String getServiceGroup() {
        return serviceGroup;
    }

    public String getInSequence() {
        return inSequence;
    }

    public String getOutSequence() {
        return outSequence;
    }

    public String getFaultSequence() {
        return faultSequence;
    }

    public String getPublishWSDL() {
        return publishWSDL;
    }

    public String getEndPoint() {
        return endPoint;
    }

    public List<String> getPolicies() {
        return policies;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public String getEnableAddressing() {
        return enableAddressing;
    }

    public String getEnableSecurity() {
        return enableSecurity;
    }

    public String getEnableRM() {
        return enableRM;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTransports(String transports) {
        this.transports = transports;
    }

    public void setStartOnLoad(String startOnLoad) {
        this.startOnLoad = startOnLoad;
    }

    public void setTrace(String trace) {
        this.trace = trace;
    }

    public void setPinnedServers(String pinnedServers) {
        this.pinnedServers = pinnedServers;
    }

    public void setServiceGroup(String serviceGroup) {
        this.serviceGroup = serviceGroup;
    }

    public void setInSequence(String inSequence) {
        this.inSequence = inSequence;
    }

    public void setOutSequence(String outSequence) {
        this.outSequence = outSequence;
    }

    public void setFaultSequence(String faultSequence) {
        this.faultSequence = faultSequence;
    }

    public void setPublishWSDL(String publishWSDL) {
        this.publishWSDL = publishWSDL;
    }

    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    public void setPolicies(List<String> policies) {
        this.policies = policies;
    }

    public void setParameters(List<String> parameters) {
        this.parameters = parameters;
    }

    public void setEnableAddressing(String enableAddressing) {
        this.enableAddressing = enableAddressing;
    }

    public void setEnableSecurity(String enableSecurity) {
        this.enableSecurity = enableSecurity;
    }

    public void setEnableRM(String enableRM) {
        this.enableRM = enableRM;
    }
}
