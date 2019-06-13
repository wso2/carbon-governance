/*
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.governance.dashboardpopulator.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.governance.dashboardpopulator.DashboardPopulatorContext;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
         name = "org.wso2.carbon.governance.dashboardpopulator", 
         immediate = true)
public class DashboardPopulatorServiceComponent {

    private static final Log log = LogFactory.getLog(DashboardPopulatorServiceComponent.class);

    @Activate
    protected void activate(ComponentContext context) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        carbonContext.setTenantDomain(org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        carbonContext.setTenantId(org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_ID);
        DashboardPopulatorAxis2ConfigurationContextObserver observer = new DashboardPopulatorAxis2ConfigurationContextObserver();
        context.getBundleContext().registerService(Axis2ConfigurationContextObserver.class.getName(), observer, null);
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {
        log.debug("Dashboard Populator for Governance bundle is deactivated ");
    }

    @Reference(
             name = "registry.service", 
             service = org.wso2.carbon.registry.core.service.RegistryService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetRegistryService")
    protected void setRegistryService(RegistryService registryService) {
        log.debug("Set the Registry Service");
        DashboardPopulatorContext.setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        log.debug("Unset the Registry Service");
        DashboardPopulatorContext.setRegistryService(null);
    }
}

