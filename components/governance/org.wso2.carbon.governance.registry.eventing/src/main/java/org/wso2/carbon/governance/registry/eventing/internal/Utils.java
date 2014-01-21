/*
 *  Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.governance.registry.eventing.internal;

import javax.servlet.http.HttpServletRequest;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.common.eventing.NotificationService;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Utils {
    private static final Log log = LogFactory.getLog(Utils.class);

    private static RegistryService registryService;

    private static NotificationService registryNotificationService;

    private static String defaultNotificationServiceURL;

    private static String remoteTopicHeaderName;

    private static String remoteTopicHeaderNS;

    private static String remoteSubscriptionStoreContext;

    public static void setRegistryService(RegistryService service) {
        registryService = service;
    }

    public static RegistryService getRegistryService() {
        return registryService;
    }

    public static String getDefaultNotificationServiceURL() {
        return defaultNotificationServiceURL;
    }

    public static void setDefaultNotificationServiceURL(String defaultNotificationServiceURL) {
        Utils.defaultNotificationServiceURL = defaultNotificationServiceURL;
    }

    public static String getRemoteTopicHeaderName() {
        return remoteTopicHeaderName;
    }

    public static void setRemoteTopicHeaderName(String remoteTopicHeaderName) {
        Utils.remoteTopicHeaderName = remoteTopicHeaderName;
    }

    public static String getRemoteTopicHeaderNS() {
        return remoteTopicHeaderNS;
    }

    public static void setRemoteTopicHeaderNS(String remoteTopicHeaderNS) {
        Utils.remoteTopicHeaderNS = remoteTopicHeaderNS;
    }

    public static String getRemoteSubscriptionStoreContext() {
        return remoteSubscriptionStoreContext;
    }

    public static void setRemoteSubscriptionStoreContext(String remoteSubscriptionStoreContext) {
        Utils.remoteSubscriptionStoreContext = remoteSubscriptionStoreContext;
    }

    public static NotificationService getRegistryNotificationService() {
        return registryNotificationService;
    }

    public static void setRegistryNotificationService(NotificationService registryNotificationService) {
        Utils.registryNotificationService = registryNotificationService;
    }
}
