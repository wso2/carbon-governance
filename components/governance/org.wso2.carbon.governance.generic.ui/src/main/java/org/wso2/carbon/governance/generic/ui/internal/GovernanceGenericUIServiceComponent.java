/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.governance.generic.ui.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.governance.generic.ui.utils.GenericUtil;
import org.wso2.carbon.ui.CarbonUIAuthenticator;
import org.wso2.carbon.ui.UIAuthenticationExtender;

import javax.servlet.http.HttpServletRequest;

/**
 * The Generic UI Declarative Service Component.
 *
 * @scr.component name="org.wso2.carbon.governance.generic.ui"
 * immediate="true"
 * @scr.reference name="ui.authenticator"
 * interface="org.wso2.carbon.ui.CarbonUIAuthenticator" cardinality="1..1"
 * policy="dynamic" bind="setCarbonUIAuthenticator" unbind="unsetCarbonUIAuthenticator"
 */
@SuppressWarnings({"unused", "JavaDoc"})
public class GovernanceGenericUIServiceComponent {

    private static Log log = LogFactory.getLog(GovernanceGenericUIServiceComponent.class);
    private ServiceRegistration serviceRegistration;

    protected void activate(ComponentContext context) {
        UIAuthenticationExtender authenticationExtender = new UIAuthenticationExtender() {
            public void onSuccessAdminLogin(HttpServletRequest request, String s, String s1,
                                            String s2) {
                GenericUtil.buildMenuItems(request, s, s1, s2);
            }
        };
        serviceRegistration = context.getBundleContext().registerService(
                UIAuthenticationExtender.class.getName(), authenticationExtender, null);
        log.debug("******* Governance List UI bundle is activated ******* ");
    }

    protected void deactivate(ComponentContext context) {
        if (serviceRegistration != null) {
            serviceRegistration.unregister();
            serviceRegistration = null;
        }
        log.debug("Governance Generic UI bundle is deactivated ");
    }

    protected void setCarbonUIAuthenticator(CarbonUIAuthenticator uiAuthenticator) {
    }

    protected void unsetCarbonUIAuthenticator(CarbonUIAuthenticator uiAuthenticator) {
    }
}
