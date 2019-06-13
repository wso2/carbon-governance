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
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * The Generic UI Declarative Service Component.
 */
@SuppressWarnings({ "unused", "JavaDoc" })
@Component(
         name = "org.wso2.carbon.governance.generic.ui", 
         immediate = true)
public class GovernanceGenericUIServiceComponent {

    private static Log log = LogFactory.getLog(GovernanceGenericUIServiceComponent.class);

    private ServiceRegistration serviceRegistration;

    @Activate
    protected void activate(ComponentContext context) {
        UIAuthenticationExtender authenticationExtender = new UIAuthenticationExtender() {

            public void onSuccessAdminLogin(HttpServletRequest request, String s, String s1, String s2) {
                GenericUtil.buildMenuItems(request, s, s1, s2);
            }
        };
        serviceRegistration = context.getBundleContext().registerService(UIAuthenticationExtender.class.getName(), authenticationExtender, null);
        log.debug("******* Governance List UI bundle is activated ******* ");
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {
        if (serviceRegistration != null) {
            serviceRegistration.unregister();
            serviceRegistration = null;
        }
        log.debug("Governance Generic UI bundle is deactivated ");
    }

    @Reference(
             name = "ui.authenticator", 
             service = org.wso2.carbon.ui.CarbonUIAuthenticator.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetCarbonUIAuthenticator")
    protected void setCarbonUIAuthenticator(CarbonUIAuthenticator uiAuthenticator) {
    }

    protected void unsetCarbonUIAuthenticator(CarbonUIAuthenticator uiAuthenticator) {
    }
}

