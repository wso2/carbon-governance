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

package org.wso2.carbon.governance.api.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.common.AttributeSearchService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.indexing.service.TermsSearchService;


/**
 * The Governance API Declarative Service Component.
 *
 * @scr.component name="org.wso2.carbon.governance.api" immediate="true"
 * @scr.reference name="registryService.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 * @scr.reference name="registry.search.component"
 * interface="org.wso2.carbon.registry.common.AttributeSearchService"
 * cardinality="1..1" policy="dynamic" bind="setAttributeSearchService" unbind="unsetAttributeSearchService"
 * @scr.reference name="registry.term.component"
 * interface="org.wso2.carbon.registry.indexing.service.TermsSearchService"
 * cardinality="1..1" policy="dynamic" bind="setTermsSearchService" unbind="unsetTermsSearchService"
 */
@SuppressWarnings({"JavaDoc", "unused"})
public class GovernanceAPIServiceComponent {

    private static final Log log = LogFactory.getLog(GovernanceAPIServiceComponent.class);

    /**
     * Activates the Governance API bundle.
     *
     * @param context the OSGi component context.
     */
    protected void activate(ComponentContext context) {
        try {
            log.debug("Governance API bundle is activated ");
        } catch (Exception e) {
            log.debug("****** Failed to activate Governance API bundle *******");
        }
    }

    /**
     * Deactivates the Governance API bundle.
     *
     * @param context the OSGi component context.
     */
    protected void deactivate(ComponentContext context) {
        log.debug("Governance API bundle is deactivated ");
    }

    /**
     * Method to set the registry service used. This will be used to access the registry instance.
     * This method is called when the OSGi Registry Service is available.
     *
     * @param registryService the registry service.
     */
    protected void setRegistryService(RegistryService registryService) {
        log.debug("Setting the Registry Service");
        GovernanceUtils.setRegistryService(registryService);
    }

    /**
     * This method is called when the current registry service becomes un-available.
     *
     * @param registryService the current registry service instance, to be used for any
     *                        cleaning-up.
     */
    protected void unsetRegistryService(RegistryService registryService) {
        log.debug("Un-setting the Registry Service");
        GovernanceUtils.setRegistryService(null);
    }

    protected void  setAttributeSearchService(AttributeSearchService searchService){
        if (log.isDebugEnabled()) {
            log.debug("Setting ContentBasedSearchService");
        }
        GovernanceUtils.setAttributeSearchService(searchService);

    }

    protected void  unsetAttributeSearchService(AttributeSearchService searchService){
        if (log.isDebugEnabled()) {
            log.debug("Unsetting ContentBasedSearchService");
        }
        GovernanceUtils.setAttributeSearchService(null);

    }

    protected void setTermsSearchService(TermsSearchService searchService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting TermSearchService");
        }
        GovernanceUtils.setTermsSearchService(searchService);
    }

    protected void  unsetTermsSearchService(TermsSearchService searchService){
        if (log.isDebugEnabled()) {
            log.debug("Unsetting TermSearchService");
        }
        GovernanceUtils.setTermsSearchService(null);

    }
}
