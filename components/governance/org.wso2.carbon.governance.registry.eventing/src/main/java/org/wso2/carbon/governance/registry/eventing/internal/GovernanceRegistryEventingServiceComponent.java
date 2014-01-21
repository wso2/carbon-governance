/*
 *  Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.governance.registry.eventing.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axis2.engine.ListenerManager;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.jdbc.handlers.filters.URLMatcher;
import org.wso2.carbon.registry.core.jdbc.handlers.HandlerLifecycleManager;
import org.wso2.carbon.registry.app.RemoteRegistryService;
import org.wso2.carbon.registry.common.eventing.NotificationService;
import org.wso2.carbon.governance.registry.eventing.handlers.GovernanceEventingHandler;

/**
 * @scr.component name="org.wso2.carbon.governance.registry.eventing" immediate="true"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService" cardinality="1..1"
 * policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 * @scr.reference name="listener.manager.service"
 * interface="org.apache.axis2.engine.ListenerManager" cardinality="0..1" policy="dynamic"
 * bind="setListenerManager" unbind="unsetListenerManager"
 * @scr.reference name="registry.notification.service"
 * interface="org.wso2.carbon.registry.common.eventing.NotificationService" cardinality="1..1"
 * policy="dynamic" bind="setRegistryNotificationService" unbind="unsetRegistryNotificationService"
 */
public class GovernanceRegistryEventingServiceComponent {

    private static Log log = LogFactory.getLog(GovernanceRegistryEventingServiceComponent.class);

    private Registry registry = null;

    private ListenerManager listenerManager = null;

    private boolean initialized = false;

    protected void activate(ComponentContext context) {
        log.debug("Governance Eventing bundle is activated ");
    }

    protected void deactivate(ComponentContext context) {
        log.debug("Governance Eventing bundle is deactivated ");
    }

    protected void setRegistryService(RegistryService registryService) {
        Utils.setRegistryService(registryService);
        initailize();
    }

    protected void unsetRegistryService(RegistryService registryService) {
        Utils.setRegistryService(null);
    }

    protected void setRegistryNotificationService(NotificationService notificationService) {
        Utils.setRegistryNotificationService(notificationService);
        initailize();
    }

    protected void unsetRegistryNotificationService(NotificationService notificationService) {
        Utils.setRegistryNotificationService(null);
    }

    protected void setListenerManager(ListenerManager listenerManager) {
        this.listenerManager = listenerManager;
        initailize();
    }

    protected void unsetListenerManager(ListenerManager listenerManager) {
        this.listenerManager = null;
    }

    private void initailize() {
        if (Boolean.toString(Boolean.TRUE).equals(System.getProperty("disable.event.handlers"))) {
            initialized = true;
            log.debug("Default Eventing Handlers have been disabled. Events will not be " +
                    "generated unless a custom handler has been configured.");
            return;
        }
        RegistryService registryService = Utils.getRegistryService();
        if (!initialized &&
                listenerManager != null && registryService != null &&
                Utils.getRegistryNotificationService() != null) {
            if (registryService instanceof RemoteRegistryService) {
                initialized = true;
                log.warn("Eventing is not available on Remote Registry");
                return;
            }
            initialized = true;
            try {
                // We can't get Registry from Utils, as the MessageContext is not available at
                // activation time.
                Registry systemRegistry = registryService.getConfigSystemRegistry();
                if (registry != null && registry == systemRegistry) {
                    // Handler has already been set.
                    return;
                }
                registry = systemRegistry;
                if (registry == null ||
                        registry.getRegistryContext() == null ||
                        registry.getRegistryContext().getHandlerManager() == null) {
                    String msg = "Error Initializing Governance Eventing Handler";
                    log.error(msg);
                } else {
                    URLMatcher filter = new URLMatcher();
                    filter.setPutPattern(".*");
                    filter.setInvokeAspectPattern(".*");
                    GovernanceEventingHandler handler = new GovernanceEventingHandler();
                    registry.getRegistryContext().getHandlerManager().addHandler(null, filter,
                            handler, HandlerLifecycleManager.DEFAULT_REPORTING_HANDLER_PHASE);
                    handler.init(registry.getEventingServiceURL(null));
                    log.debug("Successfully Initialized the Governance Eventing Handler");
                }
            } catch (Exception e) {
                String msg = "Error Initializing Eventing on Registry";
                log.error(msg, e);
                throw new RuntimeException(msg, e);
            }
        }
    }
}

