/*
 *  Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.discovery.proxy;

import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.discovery.util.ConfigHolder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
         name = "org.wso2.carbon.discovery.proxy", 
         immediate = true)
public class DiscoveryProxyComponent {

    private static final Log log = LogFactory.getLog(DiscoveryProxyComponent.class);

    private ConfigurationContext cfgCtx;

    @Activate
    protected void activate(ComponentContext context) {
        if (cfgCtx != null) {
            AxisConfiguration axisConfig = cfgCtx.getAxisConfiguration();
            DiscoveryProxyObserver observer = new DiscoveryProxyObserver();
            observer.init(axisConfig);
            axisConfig.addObservers(observer);
        } else {
            log.warn("ConfigurationContext is not available. Unable to register the " + "DiscoveryProxyObserver.");
        }
    }

    @Reference(
             name = "configuration.context.service", 
             service = org.wso2.carbon.utils.ConfigurationContextService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetConfigurationContextService")
    protected void setConfigurationContextService(ConfigurationContextService cfgCtxService) {
        if (log.isDebugEnabled()) {
            log.debug("ConfigurationContextService bound to the discovery proxy component");
        }
        cfgCtx = cfgCtxService.getServerConfigContext();
    }

    protected void unsetConfigurationContextService(ConfigurationContextService cfgCtxService) {
        if (log.isDebugEnabled()) {
            log.debug("ConfigurationContextService unbound from the discovery proxy component");
        }
        cfgCtx = null;
    }
}

