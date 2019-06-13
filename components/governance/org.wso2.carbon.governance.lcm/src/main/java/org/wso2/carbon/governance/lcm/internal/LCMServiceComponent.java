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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.lcm.listener.LifecycleLoader;
import org.wso2.carbon.governance.lcm.services.LifeCycleService;
import org.wso2.carbon.governance.lcm.util.CommonUtil;
import org.wso2.carbon.registry.common.AttributeSearchService;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import javax.xml.stream.XMLStreamException;
import java.io.FileNotFoundException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
         name = "org.wso2.carbon.governance.lcm", 
         immediate = true)
public class LCMServiceComponent {

    private static Log log = LogFactory.getLog(LCMServiceComponent.class);

    @Activate
    protected void activate(ComponentContext context) {
        BundleContext bundleContext = context.getBundleContext();
        // Generate LCM search query if it doesn't exist.
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(-1234, true);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(CarbonConstants.REGISTRY_SYSTEM_USERNAME);
            RegistryService registryService = LifeCycleServiceHolder.getInstance().getRegistryService();
            CommonUtil.addDefaultLifecyclesIfNotAvailable(registryService.getConfigSystemRegistry(), registryService.getRegistry(CarbonConstants.REGISTRY_SYSTEM_USERNAME));
        } catch (XMLStreamException | FileNotFoundException | RegistryException e) {
            log.error("An error occurred while setting up Governance Life Cycle Management", e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        LifecycleLoader lifecycleLoader = new LifecycleLoader();
        ServiceRegistration tenantMgtListenerSR = bundleContext.registerService(Axis2ConfigurationContextObserver.class.getName(), lifecycleLoader, null);
        bundleContext.registerService(LifeCycleService.class.getName(), new LifeCycleServiceImpl(), null);
        if (tenantMgtListenerSR != null) {
            if (log.isDebugEnabled()) {
                log.debug("Governance Life Cycle Management - LifecycleLoader registered");
            }
        } else {
            log.error("Governance Life Cycle Management - LifecycleLoader could not be registered");
        }
        if (log.isDebugEnabled()) {
            log.debug("Governance Life Cycle Management Service bundle is activated");
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {
        if (log.isDebugEnabled()) {
            log.debug("Governance Life Cycle Management Service bundle is deactivated");
        }
    }

    @Reference(
             name = "registry.service", 
             service = org.wso2.carbon.registry.core.service.RegistryService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetRegistryService")
    protected void setRegistryService(RegistryService registryService) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        carbonContext.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        carbonContext.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
        LifeCycleServiceHolder.getInstance().setRegistryService(registryService);
        // Generate LCM search query if it doesn't exist.
        try {
            CommonUtil.addDefaultLifecyclesIfNotAvailable(registryService.getConfigSystemRegistry(), registryService.getRegistry(CarbonConstants.REGISTRY_SYSTEM_USERNAME));
        } catch (Exception e) {
            log.error("An error occurred while setting up Governance Life Cycle Management", e);
        }
    }

    protected void unsetRegistryService(RegistryService registryService) {
        LifeCycleServiceHolder.getInstance().setRegistryService(null);
    }

    /**
     * Method used to set attribute search service.
     *
     * @param attributeSearchService    attribute search service.
     */
    @Reference(
             name = "registry.attribute.indexing", 
             service = org.wso2.carbon.registry.common.AttributeSearchService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetAttributeSearchService")
    protected void setAttributeSearchService(AttributeSearchService attributeSearchService) {
        LifeCycleServiceHolder.getInstance().setAttributeSearchService(attributeSearchService);
    }

    /**
     * Method used to unset attribute search service.
     *
     * @param attributeSearchService    attribute search service.
     */
    protected void unsetAttributeSearchService(AttributeSearchService attributeSearchService) {
        LifeCycleServiceHolder.getInstance().setAttributeSearchService(null);
    }
}

