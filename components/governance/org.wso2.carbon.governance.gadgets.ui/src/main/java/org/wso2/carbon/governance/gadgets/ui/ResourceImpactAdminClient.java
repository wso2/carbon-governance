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
package org.wso2.carbon.governance.gadgets.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.governance.gadgets.stub.ResourceImpactAdminServiceStub;
import org.wso2.carbon.governance.gadgets.stub.beans.xsd.AssociationBean;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class ResourceImpactAdminClient {

	private static final Log log = LogFactory.getLog(ResourceImpactAdminClient.class);

    ResourceImpactAdminServiceStub stub;

    public ResourceImpactAdminClient(ServletConfig config, HttpSession session,
			HttpServletRequest request) throws AxisFault {
		String backendServerURL = CarbonUIUtil.getServerURL(config
				.getServletContext(), session);
		ConfigurationContext configContext = (ConfigurationContext) config
				.getServletContext().getAttribute(
						CarbonConstants.CONFIGURATION_CONTEXT);
		String cookie = (String) session
				.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

        String serviceURL = backendServerURL + "ResourceImpactAdminService";


        stub = new ResourceImpactAdminServiceStub(configContext, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

    }

    public AssociationBean[] getResourceAssociations(String path, boolean reverse){

        AssociationBean[] associationBeans = new AssociationBean[0];
        try {
            associationBeans = stub.getAssociations(path, reverse);
        } catch (Exception e) {
            String msg = "Failed to get associations for " + path;
            log.error(msg, e);
        }
        return associationBeans;
    }
}
