/*
 * Copyright (c) 2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.governance.list.util.filter;


import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.list.util.CommonUtil;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;

public class FilterGenericArtifact extends FilterStrategy {


    public FilterGenericArtifact(String criteria, Registry governanceRegistry, String artifactKey) {
        super(criteria, governanceRegistry, artifactKey);
    }

    @Override
    public GovernanceArtifact[] getArtifacts() throws RegistryException {
        UserRegistry userRegistry =
                CommonUtil.getRegistryService().getRegistry(
                        CarbonConstants.REGISTRY_SYSTEM_USERNAME);
        GovernanceUtils.loadGovernanceArtifacts(userRegistry);
        GenericArtifactManager artifactManager =
                new GenericArtifactManager(getGovernanceRegistry(), getArtifactKey());
        return artifactManager.getAllGenericArtifacts();
    }
}
