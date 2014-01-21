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
//import org.wso2.carbon.CarbonConstants;
//import org.wso2.carbon.governance.api.util.GovernanceArtifactConfiguration;
//import org.wso2.carbon.governance.api.util.GovernanceConstants;
//import org.wso2.carbon.governance.api.util.GovernanceUtils;
//import org.wso2.carbon.governance.list.util.CommonUtil;
//import org.wso2.carbon.governance.list.util.filter.FilterFactory;
//import org.wso2.carbon.governance.list.util.filter.FilterStrategy;
//import org.wso2.carbon.ntask.core.Task;
//import org.wso2.carbon.registry.core.Registry;
//import org.wso2.carbon.registry.core.exceptions.RegistryException;
//import org.wso2.carbon.registry.core.session.UserRegistry;
//
//import java.util.List;
//import java.util.Map;
//
//public class PreFetchTask implements Task {
//
//    private Registry governanceRegistry;
//
//    private String artifactType;
//
//    private static final Log log = LogFactory.getLog(PreFetchTask.class);
//
//    @Override
//    public void setProperties(Map<String, String> properties) {
//        this.artifactType = properties.get(GovernanceConstants.ARTIFACT_TYPE);
//    }
//
//    @Override
//    public void init() {
//        try {
//            UserRegistry userRegistry =
//                    CommonUtil.getRegistryService().getRegistry(
//                            CarbonConstants.REGISTRY_SYSTEM_USERNAME);
//            this.governanceRegistry =
//                    GovernanceUtils.getGovernanceSystemRegistry(userRegistry);
//        } catch (RegistryException e) {
//            log.error("Error occurred while retrieving user registry", e);
//        }
//    }
//
//    @Override
//    public void execute() {
//        try {
//            UserRegistry userRegistry =
//                    CommonUtil.getRegistryService().getRegistry(
//                            CarbonConstants.REGISTRY_SYSTEM_USERNAME);
//            if (artifactType.equals(FilterFactory.FilterTypes.GENERIC.toString())) {
//                List<GovernanceArtifactConfiguration> configurationList =
//                        GovernanceUtils.findGovernanceArtifactConfigurations(governanceRegistry);
//                GovernanceArtifactConfiguration[] configurations =
//                        configurationList.toArray(new GovernanceArtifactConfiguration[configurationList.size()]);
//                for (GovernanceArtifactConfiguration configuration : configurations) {
//
//                    FilterStrategy filterStrategy =
//                            FilterFactory.createFilter(artifactType, null, governanceRegistry, configuration.getKey());
//                    ArtifactPopulator.getArtifactPopulator().populateTenantArtifactCache(filterStrategy, userRegistry);
//                }
//
//            }
//        } catch (Exception e) {
//            log.error("Error occurred while populating the cache", e);
//        }
//    }
//
//}