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

package org.wso2.carbon.governance.sramp.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.HttpService;
import org.wso2.carbon.governance.sramp.SRAMPServlet;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * OSGi Service Component that Registers the S-RAMP servlet implementation of Carbon.
 *
 * @scr.component name="org.wso2.carbon.registry.servlet" immediate="true"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService" cardinality="1..1"
 * policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 * @scr.reference name="configuration.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="1..1"
 * policy="dynamic" bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 * @scr.reference name="http.service" interface="org.osgi.service.http.HttpService"
 * cardinality="1..1" policy="dynamic"  bind="setHttpService" unbind="unsetHttpService"
 */
@SuppressWarnings({"unused", "JavaDoc"})
public class SRAMPServiceComponent {

    private static Log log = LogFactory.getLog(SRAMPServiceComponent.class);

    private RegistryService registryService = null;
    private ConfigurationContextService configurationContextService = null;
    private HttpService httpService = null;

    /**
     * Activates the Governance S-Ramp bundle.
     *
     * @param context the OSGi component context.
     */
    protected void activate(ComponentContext context) {
        try {
            registerServlet(context.getBundleContext());
            log.debug("******* Governance S-Ramp bundle is activated ******* ");
        } catch (Throwable e) {
            log.error("******* Failed to activate Governance S-Ramp bundle ******* ", e);
        }
    }

    /**
     * Deactivates the Governance S-Ramp bundle.
     *
     * @param context the OSGi component context.
     */
    protected void deactivate(ComponentContext context) {
        httpService.unregister("/s-ramp");
        log.debug("******* Governance S-Ramp bundle is deactivated ******* ");
    }

    private void registerServlet(BundleContext bundleContext) throws Exception {
        httpService.registerServlet("/s-ramp",
                new SRAMPServlet(configurationContextService, registryService), null,
                httpService.createDefaultHttpContext());
    }

    protected void setRegistryService(RegistryService registryService) {
        this.registryService = registryService;
    }

    protected void unsetRegistryService(RegistryService registryService) {
        this.registryService = null;
    }

    protected void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    protected void unsetHttpService(HttpService httpService) {
        this.httpService = null;
    }

    protected void setConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        this.configurationContextService = configurationContextService;
    }

    protected void unsetConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        this.configurationContextService = null;
    }
}
