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

package org.wso2.carbon.governance.registry.extensions.discoveryagents;

import org.wso2.carbon.governance.api.generic.dataobjects.DetachedGenericArtifact;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;

import java.util.List;
import java.util.Map;

/**
 * DiscoveryAgent is a callback interface, each server (instance of Server RXT) can define any number of implementations
 * of this interface and possible to registered through governance.xml file.  Generally DiscoveryAgent communicate with
 * remote server though it's remote API and find available metadata assets in each server, finally return as set of
 * GenericArtifact lists. Caller can persist or render these list for further processing.
 *
 * @since 5.0.0  TODO
 */
public interface DiscoveryAgent {

    /**
     * Initialization method of DiscoveryAgent, this method will be called right after the DiscoveryAgent creation. Also
     * this method executed only one time during the DiscoveryAgent lifetime.
     *
     * @param properties Set of initialization properties defined in governance.xml file for this DiscoveryAgent
     */
    public void init(Map properties);

    /**
     * Finalizer method of DiscoveryAgent, this method executed only one time during the DiscoveryAgent lifetime.
     *
     * @param properties Set of properties defined in governance.xml file for this DiscoveryAgent
     */
    public void close(Map properties);

    /**
     * This callback method get executed whenever someone trigger "Discover" option from UI or trigger through a
     * scheduler job. One Server (instance of Server RXT) can have number of DiscoveryAgent registered through
     * governance.xml file
     *
     * @param server GenericArtifact - metadata about remote server
     * @return set of GenericArtifact lists (e.g -
     */
    public Map<String, List<DetachedGenericArtifact>> discoverArtifacts(GenericArtifact server)
            throws DiscoveryAgentException;

}
