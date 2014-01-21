/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.governance.lcm.ui.clients;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceStub;
import org.wso2.carbon.registry.common.utils.CommonUtil;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.ui.CarbonUIUtil;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class LifeCycleManagementServiceClient {

    private static final Log log = LogFactory.getLog(LifeCycleManagementServiceClient.class);

    private LifeCycleManagementServiceStub stub;

    private String epr;

    public LifeCycleManagementServiceClient(String cookie, ServletConfig config, HttpSession session)
            throws RegistryException {

        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config.
                getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        epr = backendServerURL + "LifeCycleManagementService";

        try {
            stub = new LifeCycleManagementServiceStub(configContext, epr);

            ServiceClient client = stub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

        } catch (AxisFault axisFault) {
            String msg = "Failed to initiate life cycle service client. " + axisFault.getMessage();
            log.error(msg, axisFault);
            throw new RegistryException(msg, axisFault);
        }
    }

    public String[] getLifeCycleList(HttpServletRequest request) throws Exception  {
        String[] output;
        try {
            output = stub.getLifecycleList();
        } catch (Exception e) {
            if (e instanceof RegistryException) {
                return new String[0];
            } else {
                throw e;
            }
        }
        return output;
    }

    public String getLifeCycleConfiguration(HttpServletRequest request) throws Exception  {
        String lifecycleName = request.getParameter("lifecycleName");
        String output,version;
        try {
            output = stub.getLifecycleConfiguration(lifecycleName);
            version = stub.getLifecycleConfigurationVersion(lifecycleName);
            request.getSession().setAttribute("resourceVersion",version);

        } catch (Exception e) {
            if (e instanceof RegistryException) {
                return "";
            } else {
                throw e;
            }
        }
        return output;
    }

    public boolean newLifeCycle(HttpServletRequest request) throws Exception {
        String payload = request.getParameter("payload");
        boolean output = false;
        try {
            if(stub.parseConfiguration(payload))
                output = stub.createLifecycle(payload);
        } catch (Exception e) {
            if (e instanceof RegistryException) {
                return false;
            } else {
                throw e;
            }
        }
        return output;
    }

    public boolean updateLifeCycle(HttpServletRequest request) throws Exception {
        String lifecycleName = request.getParameter("lifecycleName");
        String updateOverride =  request.getParameter("updateOverride");
        if (lifecycleName == null) {
            lifecycleName = "";
        }
        String payload = request.getParameter("payload");
        boolean output;
        try {

            if (!stub.parseConfiguration(payload)) {
                return false;
            }
            String latestVersion = stub.getLifecycleConfigurationVersion(lifecycleName);

            if(updateOverride.equals("true") || CommonUtil.isLatestVersion((String) request.getSession().getAttribute("resourceVersion"),latestVersion)){
                output = stub.updateLifecycle(lifecycleName, payload);
            }
            else{
                throw new Exception("Lifecycle resource has already modified ");
            }

        } catch (Exception e) {
            if (e instanceof RegistryException) {
                return false;

            } else {
                log.error(e);
                throw e;
            }
        }
        return output;
    }

    public boolean deleteLifeCycle(HttpServletRequest request) throws Exception  {
        String lifecycleName = request.getParameter("lifecycleName");
        boolean output;
        try {
            output = stub.deleteLifecycle(lifecycleName);
        } catch (Exception e) {
            if (e instanceof RegistryException) {
                return false;
            } else {
                throw e;
            }
        }
        return output;
    }

     public boolean isLifeCycleNameInUse(HttpServletRequest request) throws Exception  {
        String lifeCycleName = request.getParameter("lifecycleName");
        return isLifeCycleNameInUse(lifeCycleName);
    }

    public boolean isLifeCycleNameInUse(String lifeCycleName) throws Exception  {
        boolean output;
        try {
            output = stub.isLifecycleNameInUse(lifeCycleName);
        } catch (Exception e) {
            if (e instanceof RegistryException) {
                return false;
            } else {
                throw e;
            }
        }
        return output;
    }

	}
