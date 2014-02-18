/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.governance.gadgets.impactanalysis.services;

import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.gadgets.impactanalysis.beans.ImpactBean;
import org.wso2.carbon.governance.gadgets.impactanalysis.services.util.BeanUtils;
import org.wso2.carbon.registry.common.services.RegistryAbstractAdmin;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

public class ImpactAnalysisAdminService extends RegistryAbstractAdmin {

	public ImpactBean getImpactAnalysisBean() throws GovernanceException {
		Registry registry = getGovernanceRegistry();
		ServiceManager manager;
		try {
	 	    manager = new ServiceManager(registry);
		} catch (RegistryException e) {	
		    throw new GovernanceException("ServiceManager initialization failed", e);
		}
		return BeanUtils.populateImpactBean(manager);
	}
}
