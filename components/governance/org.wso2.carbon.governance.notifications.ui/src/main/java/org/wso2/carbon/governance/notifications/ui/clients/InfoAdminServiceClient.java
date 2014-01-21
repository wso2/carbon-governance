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

package org.wso2.carbon.governance.notifications.ui.clients;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.governance.notifications.stub.InfoAdminServiceStub;
import org.wso2.carbon.governance.notifications.stub.beans.xsd.EventTypeBean;
import org.wso2.carbon.governance.notifications.stub.beans.xsd.SubscriptionBean;
import org.wso2.carbon.governance.notifications.stub.services.utils.xsd.EventType;
import org.wso2.carbon.governance.notifications.stub.services.utils.xsd.SubscriptionInstance;
import org.wso2.carbon.governance.notifications.ui.Utils;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.ui.CarbonUIUtil;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class InfoAdminServiceClient {

    private static final Log log = LogFactory.getLog(InfoAdminServiceClient.class);

    private InfoAdminServiceStub stub;
    private String epr;

    public InfoAdminServiceClient(String cookie, ServletConfig config, HttpSession session)
            throws RegistryException {

        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config.
                getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        epr = backendServerURL + "InfoAdminService";

        try {
            stub = new InfoAdminServiceStub(configContext, epr);

            ServiceClient client = stub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

        } catch (AxisFault axisFault) {
            String msg = "Failed to initiate comment service client. " + axisFault.getMessage();
            log.error(msg, axisFault);
            throw new RegistryException(msg, axisFault);
        }
    }

    public EventTypeBean getEventTypes(HttpServletRequest request) throws Exception {
        String path = (String) Utils.getParameter(request, "path");
        if (path == null) {
            path ="-R/";
        }
        EventTypeBean bean = null;
        try {
            bean = stub.getEventTypes(path, null);
            if (bean.getEventTypes() == null) {
                bean.setEventTypes(new EventType[0]);
            }
        } catch (Exception e) {
            String msg = "Failed to get Event Types. " +
                    e.getMessage();
            log.error(msg, e);
            throw new Exception(msg);
        }
        return bean;
    }

    public SubscriptionBean getSubscriptions(HttpServletRequest request) throws Exception {
        String path = (String) Utils.getParameter(request, "path");
        if (path == null) {
            path ="-R/";
        }
        SubscriptionBean bean = null;
        try {
            bean = stub.getSubscriptions(path, null);
            if (bean.getSubscriptionInstances() == null) {
                bean.setSubscriptionInstances(new SubscriptionInstance[0]);
            }
        } catch (Exception e) {
            String msg = "Failed to get subscriptions. " +
                    e.getMessage();
            log.error(msg, e);
            throw new Exception(msg);
        }
        return bean;
    }

    public SubscriptionBean subscribe(HttpServletRequest request) throws Exception {
        String path = (String) Utils.getParameter(request, "path");
        String endpoint = (String) Utils.getParameter(request, "endpoint");
        String eventName = (String) Utils.getParameter(request, "eventName");
        String topicDelimiter= (String) Utils.getParameter(request, "delimiter");

        if (topicDelimiter.equals("#") || topicDelimiter.equals("*")) {
            if (path.endsWith("/")) {
                path = path + topicDelimiter;
            } else {
                path = path + "/" + topicDelimiter;
            }
        }
        SubscriptionBean bean = null;
        try {
            bean = stub.subscribe(path, endpoint, eventName, null);
            if (bean.getSubscriptionInstances() == null) {
                bean.setSubscriptionInstances(new SubscriptionInstance[0]);
            }
        } catch (Exception e) {
            String msg = "Failed to subscribe. " +
                    e.getMessage();
            log.error(msg, e);
            throw new Exception(msg);
        }
        return bean;
    }

    public SubscriptionBean subscribeREST(HttpServletRequest request) throws Exception {
        String path = (String) Utils.getParameter(request, "path");
        String endpoint = (String) Utils.getParameter(request, "endpoint");
        String eventName = (String) Utils.getParameter(request, "eventName");
        SubscriptionBean bean = null;
        try {
            bean = stub.subscribeREST(path, endpoint, eventName, null);
            if (bean.getSubscriptionInstances() == null) {
                bean.setSubscriptionInstances(new SubscriptionInstance[0]);
            }
        } catch (Exception e) {
            String msg = "Failed to subscribe. " +
                    e.getMessage();
            log.error(msg, e);
            throw new Exception(msg);
        }
        return bean;
    }

    public boolean unsubscribe(HttpServletRequest request) throws Exception {
        String path = (String) Utils.getParameter(request, "path");
        String id = (String) Utils.getParameter(request, "id");
        boolean result = false;
        try {
            result = stub.unsubscribe(path, id, null);
        } catch (Exception e) {
            String msg = "Failed to unsubscribe. " +
                    e.getMessage();
            log.error(msg, e);
            throw new Exception(msg);
        }
        return result;
    }

    public boolean isResource(HttpServletRequest request) throws Exception {
        String path = (String) Utils.getParameter(request, "path");
        boolean result = true;
        try {
            result = stub.isResource(path, null);
        } catch (Exception e) {
            String msg = "Failed to get resource type. " +
                    e.getMessage();
            log.error(msg, e);
            throw new Exception(msg);
        }
        return result;
    }

    public String getRemoteURL(String path) throws Exception {
        String result = null;
        try {
            result = stub.getRemoteURL(path, null);
        } catch (Exception e) {
            String msg = "Failed to get remote URL. " +
                    e.getMessage();
            log.error(msg, e);
            throw new Exception(msg);
        }
        return result;
    }
}
