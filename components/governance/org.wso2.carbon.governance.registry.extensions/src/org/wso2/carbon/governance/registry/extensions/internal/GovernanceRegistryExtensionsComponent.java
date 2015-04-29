/*
*  Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.governance.registry.extensions.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.governance.registry.extensions.listeners.RxtLoader;
import org.wso2.carbon.governance.registry.extensions.utils.CommonUtil;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.extensions.services.RXTStoragePathService;
import org.wso2.carbon.securevault.SecretCallbackHandlerService;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;

/**
 * @scr.component name="org.wso2.governance.registry.extensions.services" immediate="true"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic"  bind="setRegistryService" unbind="unsetRegistryService"
 * @scr.reference name="secret.callback.handler.service"
 * interface="org.wso2.carbon.securevault.SecretCallbackHandlerService" cardinality="1..1"  policy="dynamic"
 * bind="setSecretCallbackHandlerService" unbind="unsetSecretCallbackHandlerService"
 * @scr.reference name="extensions.service"
 * interface="org.wso2.carbon.registry.extensions.services.RXTStoragePathService" cardinality="1..1"
 * policy="dynamic" bind="setRxtStoragePathService" unbind="unsetRxtStoragePathService"
 */

public class GovernanceRegistryExtensionsComponent {

    private static final Log log = LogFactory.getLog(GovernanceRegistryExtensionsComponent.class);
    private static RegistryService registryService = null;
    private static SecretCallbackHandlerService secretCallbackHandlerService = null;

    protected void activate(ComponentContext componentContext) {
        BundleContext bundleCtx = componentContext.getBundleContext();
        RxtLoader rxtLoader = new RxtLoader();
        ServiceRegistration tenantMgtListenerSR = bundleCtx.registerService(
                Axis2ConfigurationContextObserver.class.getName(), rxtLoader, null);
        if (tenantMgtListenerSR != null) {
            log.debug("Identity Provider Management - RXTLoader registered");
        } else {
            log.error("Identity Provider Management - RXTLoader could not be registered");
        }
        if(log.isDebugEnabled()){
            log.debug("GovernanceRegistryExtensionsComponent activated");
        }
    }

    protected void setRegistryService(RegistryService registryService) {
        if(registryService!=null && log.isDebugEnabled()){
          log.debug("Registry service initialized");
        }
        this.registryService = registryService;
    }

    protected void unsetRegistryService(RegistryService registryService) {
        this.registryService = null;
    }

    public static RegistryService getRegistryService() throws RegistryException {
        return registryService;
    }

    protected void setSecretCallbackHandlerService(SecretCallbackHandlerService secretCallbackHandlerService){
            if (log.isDebugEnabled()) {
                log.debug("Setting SecretCallbackHandlerService");
            }
           this.secretCallbackHandlerService = secretCallbackHandlerService;
    }

    protected void unsetSecretCallbackHandlerService(SecretCallbackHandlerService secretCallbackHandlerService) {
        this.secretCallbackHandlerService = null;
    }

    public static SecretCallbackHandlerService getSecretCallbackHandlerService(){
        return secretCallbackHandlerService;
    }

    protected void setRxtStoragePathService(RXTStoragePathService rxtStoragePathService) {
        CommonUtil.setRxtStoragePathService(rxtStoragePathService);
    }

    protected void unsetRxtStoragePathService(RXTStoragePathService rxtSPService) {
        CommonUtil.setRxtStoragePathService(null);
    }
}
