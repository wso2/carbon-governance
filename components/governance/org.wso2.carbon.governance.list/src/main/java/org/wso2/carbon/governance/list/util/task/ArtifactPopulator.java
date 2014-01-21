///*
// *  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
// *
// *  WSO2 Inc. licenses this file to you under the Apache License,
// *  Version 2.0 (the "License"); you may not use this file except
// *  in compliance with the License.
// *  You may obtain a copy of the License at
// *
// *  http://www.apache.org/licenses/LICENSE-2.0
// *
// *  Unless required by applicable law or agreed to in writing,
// *  software distributed under the License is distributed on an
// *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// *  KIND, either express or implied.  See the License for the
// *  specific language governing permissions and limitations
// *  under the License.
// *
// */
//package org.wso2.carbon.governance.list.util.task;
//
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//import org.wso2.carbon.governance.api.cache.ArtifactCache;
//import org.wso2.carbon.governance.api.cache.ArtifactCacheManager;
//import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
//import org.wso2.carbon.governance.list.util.filter.FilterStrategy;
//import org.wso2.carbon.registry.core.exceptions.RegistryException;
//import org.wso2.carbon.registry.core.session.UserRegistry;
//
///**
// * Populates the relevant tenant artifact cache with the artifacts filtered out according to the
// * filter strategy provided. For example, this can be used to populate artifacts such as WSDLs,
// * Schemas, Services and Policies, etc.
// */
//public class ArtifactPopulator {
//
//    private static final String REGISTRY_LC_NAME = "registry.LC.name";
//    private static final Log log = LogFactory.getLog(ArtifactPopulator.class);
//    private static ArtifactPopulator thisInstance = new ArtifactPopulator();
//
//    public static synchronized ArtifactPopulator getArtifactPopulator() {
//        return thisInstance;
//    }
//
//    private ArtifactPopulator() {}
//
//    /**
//     * Populates the relevant tenant artifact cache with the filtered artifacts.
//     *
//     * @param filterStrategy        Filtering strategy to be evaluated.
//     * @throws RegistryException    If any unusual state appears while carrying out registry
//     *                              related invocations
//     */
//    public void populateTenantArtifactCache(FilterStrategy filterStrategy, UserRegistry userRegistry) throws
//            RegistryException {
//        GovernanceArtifact[] artifacts = filterStrategy.getArtifacts();
//        try {
//            ArtifactCache cache = ArtifactCacheManager.getCacheManager().getTenantArtifactCache(userRegistry.getTenantId());
//            for (GovernanceArtifact artifact : artifacts) {
//                GovernanceArtifact tmp = cache.getArtifact(artifact.getPath());
//                if(tmp == null){
//                    ArtifactCacheManager.getCacheManager().getTenantArtifactCache(userRegistry.getTenantId()).addArtifact(
//                            artifact.getPath(), artifact);
//                }
//            }
//        } catch (RegistryException e) {
//            log.error("Unable to get the resource from the registry", e);
//        }
//    }
//
//
//}
