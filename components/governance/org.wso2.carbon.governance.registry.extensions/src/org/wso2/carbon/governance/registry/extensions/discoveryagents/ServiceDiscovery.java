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

import com.google.gson.Gson;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.DetachedGenericArtifact;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.registry.extensions.internal.GovernanceRegistryExtensionsDataHolder;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.util.List;
import java.util.Map;

public class ServiceDiscovery {

    public static final String SERVER_RXT_OVERVIEW_NAME = "overview_name";
    private static final String SERVER_RXT_OVERVIEW_VERSION = "overview_version";

    /**
     * This methos is used to discover artifacts for a particular server.
     *
     * @param server    server GenericArtifact.
     * @return          Discovered artifacts.
     * @throws DiscoveryAgentException
     */
    public Map<String, List<DetachedGenericArtifact>> discoverArtifacts(GenericArtifact server)
            throws DiscoveryAgentException {
        try {
            DiscoveryAgentExecutor discoveryAgentExecutor = DiscoveryAgentExecutor.getInstance();
            return discoveryAgentExecutor.executeDiscoveryAgent(server);
        } catch (DiscoveryAgentException e) {
            return null;
        }

    }

    /**
     * This method is used to save artifacts.
     *
     * @param map       artifacts map.
     * @param server    server GenericArtifact.
     * @return
     */
    public Map<String, List<String>> save(Map<String, List<DetachedGenericArtifact>> map, GenericArtifact server) {
        try {

            for (Map.Entry<String, List<DetachedGenericArtifact>> entry : map.entrySet()) {
                for (Object artifact : entry.getValue()) {
                    Gson gson = new Gson();
                    String json = gson.toJson(artifact);

                    //                    Gson gson2 = new Gson();
                    //                    gson2.fromJson(json, GovernanceArtifact.class);
                    //TODO: Add artifacs to a map.
                }
            }

            persistDiscoveredArtifacts(getGovRegistry(), map);
        } catch (RegistryException e) {
            //TODO
        }
        return null;
    }

    private Registry getGovRegistry() throws RegistryException {
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(CarbonConstants.REGISTRY_SYSTEM_USERNAME);
        return GovernanceRegistryExtensionsDataHolder.getInstance().getRegistryService().getGovernanceSystemRegistry();
    }

    private void persistDiscoveredArtifacts(Registry registry, Map<String, List<DetachedGenericArtifact>> newArtifacts)
            throws RegistryException {
        for (Map.Entry<String, List<DetachedGenericArtifact>> artifactEntry : newArtifacts.entrySet()) {
            String shortName = artifactEntry.getKey();
            List<DetachedGenericArtifact> artifacts = artifactEntry.getValue();
            GenericArtifactManager artifactManager = getGenericArtifactManager(registry, shortName);
            for (DetachedGenericArtifact artifact : artifacts) {
                persistNewArtifact(artifactManager, artifact);
            }
        }

    }

    private GenericArtifactManager getGenericArtifactManager(Registry registry, String mediaType)
            throws RegistryException {
        return new GenericArtifactManager(registry, mediaType);
    }

    private void persistNewArtifact(GenericArtifactManager artifactManager, DetachedGenericArtifact artifact)
            throws GovernanceException {

        addNewGenericArtifact(artifactManager, artifact);
    }

    private GenericArtifact addNewGenericArtifact(GenericArtifactManager artifactManager,
            DetachedGenericArtifact artifact)
            throws GovernanceException {
        GenericArtifact newArtifact = artifact.makeRegistryAware(artifactManager);
        artifactManager.addGenericArtifact(newArtifact);
        return newArtifact;
    }
}
