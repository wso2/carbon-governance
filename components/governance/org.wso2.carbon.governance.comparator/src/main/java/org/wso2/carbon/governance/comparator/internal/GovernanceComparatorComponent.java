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
package org.wso2.carbon.governance.comparator.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.governance.common.GovernanceConfigurationService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
         name = "org.wso2.carbon.governance.comparator.services", 
         immediate = true)
public class GovernanceComparatorComponent {

    private static final Log log = LogFactory.getLog(GovernanceComparatorComponent.class);

    private GovernanceComparatorDataHolder dataHolder = GovernanceComparatorDataHolder.getInstance();

    @Activate
    protected void activate(ComponentContext componentContext) {
    }

    @Reference(
             name = "governanceconfiguration.service", 
             service = org.wso2.carbon.governance.common.GovernanceConfigurationService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetGovernanceConfiguration")
    protected void setGovernanceConfigurationService(GovernanceConfigurationService govConfigService) {
        dataHolder.setGovernanceConfiguration(govConfigService.getGovernanceConfiguration());
    }

    protected void unsetGovernanceConfiguration(GovernanceConfigurationService govConfigService) {
        dataHolder.setGovernanceConfiguration(null);
    }
}

