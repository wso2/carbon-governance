/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.governance.common.internal;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.governance.common.GovernanceConfiguration;
import org.wso2.carbon.governance.common.GovernanceConfigurationService;
import org.wso2.carbon.governance.common.utils.GovernanceUtils;

public class GovernanceConfigurationServiceActivator implements BundleActivator {

    private final Log log = LogFactory.getLog(GovernanceConfigurationServiceActivator.class);

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        try {
            GovernanceConfiguration configuration = GovernanceUtils.getGovernanceConfiguration();
            GovernanceConfigurationServiceImpl service = new GovernanceConfigurationServiceImpl();
            service.setGovernanceConfigurationService(configuration);
            bundleContext.registerService(GovernanceConfigurationService.class, service, null);
            if(log.isDebugEnabled()){
                log.debug("GovernanceConfigurationServiceActivator " + configuration);
            }
        } catch (Throwable e) {
            log.error("Cannot start Governance common bundle", e);
            // do not throw exceptions;
        }

    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {

    }
}
