/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.governance.registry.extensions.listeners;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.registry.extensions.utils.CommonUtil;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.internal.RegistryCoreServiceComponent;
import org.wso2.carbon.registry.core.service.RegistryService;

/**
 * This class loads the Association configuration from the file system
 */
public class AssociationConfigLoader {
    private Log log = LogFactory.getLog(AssociationConfigLoader.class);

    public void createdConfigurationContext(ConfigurationContext configContext) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        RegistryService service = RegistryCoreServiceComponent.getRegistryService();
        if (log.isDebugEnabled()) {
            log.debug("Loading association configuration to the registry for tenant " + tenantId);
        }
        try {
            CommonUtil.addAssociationConfig(service.getGovernanceSystemRegistry(tenantId), tenantId);
            if (log.isDebugEnabled()) {
                log.debug("Successfully loaded association configuration the registry for tenant " + tenantId);
            }
        } catch (RegistryException e) {
            log.error("Failed to add association configuration files to registry", e);
        }
    }
}
