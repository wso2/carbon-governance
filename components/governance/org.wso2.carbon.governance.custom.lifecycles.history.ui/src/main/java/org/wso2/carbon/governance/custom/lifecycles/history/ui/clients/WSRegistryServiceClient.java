/*
 * Copyright (c) WSO2 Inc. (http://www.wso2.com) All Rights Reserved.
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

package org.wso2.carbon.governance.custom.lifecycles.history.ui.clients;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.stub.WSRegistryServiceStub;
import org.wso2.carbon.ui.CarbonUIUtil;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpSession;

//import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
//import org.wso2.carbon.core.common.AuthenticationException;

public class WSRegistryServiceClient{
	private static final Log log = LogFactory.getLog(org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient.class);
	private WSRegistryServiceStub stub;
	private String cookie;
	private String epr;


    public WSRegistryServiceClient(String cookie, ServletConfig config, HttpSession session)
            throws RegistryException {

        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config.
                getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        epr = backendServerURL + "WSRegistryService";
        try{

            stub = new WSRegistryServiceStub(configContext, epr);
            ServiceClient client = stub._getServiceClient();
            Options options = client.getOptions();

            options.setManageSession(true);
            options.setProperty(HTTPConstants.COOKIE_STRING, cookie);
            stub._getServiceClient().getOptions().setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
            //Increase the time out when sending large attachments
            stub._getServiceClient().getOptions().setTimeOutInMilliSeconds(1000000);

        } catch (AxisFault axisFault) {
            String msg = "Failed to initiate WSRegistry Service client. " + axisFault.getMessage();
            log.error(msg, axisFault);
            throw new RegistryException(msg, axisFault);
        }
    }

	public  boolean resourceExists(String path) throws RegistryException {
		try {
			return stub.resourceExists(path);
		} catch (Exception e) {
			String msg = "Failed to perform resourceExists operation.";
			log.error(msg, e);
			throw new RegistryException(msg, e);
		}
	}

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    public void setStub(WSRegistryServiceStub stub) {
        this.stub = stub;
    }

    public WSRegistryServiceStub getStub() {
        return stub;
    }

    public void setEpr(String epr) {
        this.epr = epr;
    }
//
//    public boolean authenticate(ConfigurationContext ctx, String serverURL, String username, String password) throws AxisFault, AuthenticationException {
//        String serviceEPR = serverURL + "AuthenticationAdmin";
//
//        AuthenticationAdminStub stub = new AuthenticationAdminStub(ctx, serviceEPR);
//        ServiceClient client = stub._getServiceClient();
//        Options options = client.getOptions();
//        options.setManageSession(true);
//        try {
//            boolean result = stub.login(username, password, new URL(serviceEPR).getHost());
//            if (result){
//                setCookie((String) stub._getServiceClient().getServiceContext().
//                        getProperty(HTTPConstants.COOKIE_STRING));
//            }
//            return result;
//        } catch (Exception e) {
//            String msg = "Error occurred while logging in";
//            throw new AuthenticationException(msg, e);
//        }
//    }
}
