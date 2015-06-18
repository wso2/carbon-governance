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

package org.wso2.carbon.governance.registry.extensions.internal;

import org.wso2.carbon.governance.common.GovernanceConfiguration;
import org.wso2.carbon.registry.core.service.RegistryService;

public class GovernanceRegistryExtensionsDataHolder {

    private static GovernanceRegistryExtensionsDataHolder instance = new GovernanceRegistryExtensionsDataHolder();

    private GovernanceConfiguration governanceConfiguration;
    private RegistryService registryService;

    private GovernanceRegistryExtensionsDataHolder() {

    }

    public static GovernanceRegistryExtensionsDataHolder getInstance() {
        return instance;
    }


    public GovernanceConfiguration getGovernanceConfiguration() {
        return governanceConfiguration;
    }

    public void setGovernanceConfiguration(GovernanceConfiguration governanceConfiguration) {
        this.governanceConfiguration = governanceConfiguration;
    }

    public RegistryService getRegistryService() {
        return registryService;
    }

    public void setRegistryService(RegistryService registryService) {
        this.registryService = registryService;
    }
}
