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
package org.wso2.carbon.governance.platform.extensions.client;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.mediation.configadmin.stub.ConfigServiceAdminStub;

import java.rmi.RemoteException;


public class ConfigManagementClient {

    private static final Log log = LogFactory.getLog(ConfigManagementClient.class);

    private ConfigServiceAdminStub stub;

    public ConfigManagementClient(String cookie,
                                  String backendServerURL,
                                  ConfigurationContext configCtx) throws AxisFault {

        String serviceURL = backendServerURL + "ConfigServiceAdmin";
        stub = new ConfigServiceAdminStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
		option.setTimeOutInMilliSeconds(15 * 60 * 1000);
        option.setProperty(HTTPConstants.SO_TIMEOUT, 15 * 60 * 1000);
        option.setProperty(HTTPConstants.CONNECTION_TIMEOUT,15 * 60 * 1000);
        option.setManageSession(true);
        option.setProperty(HTTPConstants.COOKIE_STRING, cookie);

    }

    public String getSynapseConfig() throws RemoteException {
     return  stub.getConfiguration();

    }




}
