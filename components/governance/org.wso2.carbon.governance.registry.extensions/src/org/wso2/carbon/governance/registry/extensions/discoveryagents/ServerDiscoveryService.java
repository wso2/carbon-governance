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
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.util.List;
import java.util.Map;

public class ServerDiscoveryService extends DiscoveryAgentExecutorSupport {


    public Map<String, List<DetachedGenericArtifact>> discoverArtifacts(GenericArtifact server)
            throws DiscoveryAgentException {
        DiscoveryAgentExecutor discoveryAgentExecutor = DiscoveryAgentExecutor.getInstance();
        return discoveryAgentExecutor.executeDiscoveryAgent(server);
    }

    //TODO - change method name   persistArtifacts()
    public Map<String, List<String>> save(Map<String, List<DetachedGenericArtifact>> discovredArtifacts,
                                          GenericArtifact server, String existArtifactStrategy,
                                          String orphanArtifactStrategy) throws
                                                                         DiscoveryAgentException {

        setExistArtifactStrategy(ExistArtifactStrategy.valueOf(existArtifactStrategy.toUpperCase()));
        setOrphanArtifactStrategy(OrphanArtifactStrategy.valueOf(orphanArtifactStrategy));
        try {
            Registry govRegistry = getGovRegistry();
            String originProperty = getOriginProperty(server);
            String seqNo = getSequnceNo();
            Map<String, List<String>> feedback = persistDiscoveredArtifacts(govRegistry, discovredArtifacts, server,
                                                                            seqNo, originProperty);
            handleOrphanArtifacts(govRegistry, discovredArtifacts, seqNo, originProperty);
            return feedback;
        } catch (RegistryException e) {
            throw new DiscoveryAgentException("Exception occurred while accessing registry", e);
        }
    }

}
