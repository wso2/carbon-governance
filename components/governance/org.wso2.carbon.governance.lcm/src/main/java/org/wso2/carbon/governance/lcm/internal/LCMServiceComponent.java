/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.governance.lcm.internal;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.common.AttributeSearchService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.governance.lcm.util.CommonUtil;
import org.wso2.carbon.governance.lcm.listener.LifecycleLoader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.indexing.service.ContentSearchService;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

/**
 * @scr.component name="org.wso2.carbon.governance.lcm"
 * immediate="true"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService" cardinality="1..1"
 * policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 * @scr.reference name="registry.attribute.indexing"
 * interface="org.wso2.carbon.registry.common.AttributeSearchService" cardinality="1..1"
 * policy="dynamic" bind="setAttributeSearchService" unbind="unsetAttributeSearchService"
 */
public class LCMServiceComponent {

    private static Log log = LogFactory.getLog(LCMServiceComponent.class);

    protected void activate(ComponentContext context) {
        BundleContext bundleContext = context.getBundleContext();
        LifecycleLoader lifecycleLoader = new LifecycleLoader();
        ServiceRegistration tenantMgtListenerSR = bundleContext.registerService(
                Axis2ConfigurationContextObserver.class.getName(), lifecycleLoader, null);
        if (tenantMgtListenerSR != null) {
            if(log.isDebugEnabled()) {
                log.debug("Governance Life Cycle Management - LifecycleLoader registered");
            }
        } else {
            log.error("Governance Life Cycle Management - LifecycleLoader could not be registered");
        }
        if (log.isDebugEnabled()) {
            log.debug("Governance Life Cycle Management Service bundle is activated");
        }
    }

    protected void deactivate(ComponentContext context) {
        if(log.isDebugEnabled()) {
            log.debug("Governance Life Cycle Management Service bundle is deactivated");
        }
    }

    protected void setRegistryService(RegistryService registryService) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        carbonContext.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        carbonContext.setTenantId(MultitenantConstants.SUPER_TENANT_ID);

        CommonUtil.setRegistryService(registryService);
        // Generate LCM search query if it doesn't exist.
        try {
            CommonUtil.addDefaultLifecyclesIfNotAvailable(registryService.getConfigSystemRegistry(),
                    registryService.getRegistry(CarbonConstants.REGISTRY_SYSTEM_USERNAME));
        } catch (Exception e) {
            log.error("An error occurred while setting up Governance Life Cycle Management", e);
        }
    }

    protected void unsetRegistryService(RegistryService registryService) {
        CommonUtil.setRegistryService(null);
    }

    /**
     * Method used to set attribute search service.
     *
     * @param attributeSearchService    attribute search service.
     */
    protected void setAttributeSearchService(AttributeSearchService attributeSearchService) {
        CommonUtil.setAttributeSearchService(attributeSearchService);
    }

    /**
     * Method used to unset attribute search service.
     *
     * @param attributeSearchService    attribute search service.
     */
    protected void unsetAttributeSearchService(AttributeSearchService attributeSearchService) {
        CommonUtil.setAttributeSearchService(null);
    }
}
