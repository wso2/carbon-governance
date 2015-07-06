/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.common.eventing.NotificationService;

public class EventDataHolder {

    private static EventDataHolder holder = new EventDataHolder();

    private EventDataHolder(){
    }

    public static EventDataHolder getInstance(){
         return holder;
    }

    private RegistryService registryService;

    private NotificationService registryNotificationService;

    private String defaultNotificationServiceURL;

    private String remoteTopicHeaderName;

    private String remoteTopicHeaderNS;

    private String remoteSubscriptionStoreContext;

    public void setRegistryService(RegistryService service) {
        registryService = service;
    }

    public RegistryService getRegistryService() {
        return registryService;
    }

    public String getDefaultNotificationServiceURL() {
        return defaultNotificationServiceURL;
    }

    public void setDefaultNotificationServiceURL(String defaultNotificationServiceURL) {
        this.defaultNotificationServiceURL = defaultNotificationServiceURL;
    }

    public String getRemoteTopicHeaderName() {
        return remoteTopicHeaderName;
    }

    public void setRemoteTopicHeaderName(String remoteTopicHeaderName) {
        this.remoteTopicHeaderName = remoteTopicHeaderName;
    }

    public String getRemoteTopicHeaderNS() {
        return remoteTopicHeaderNS;
    }

    public void setRemoteTopicHeaderNS(String remoteTopicHeaderNS) {
        this.remoteTopicHeaderNS = remoteTopicHeaderNS;
    }

    public String getRemoteSubscriptionStoreContext() {
        return remoteSubscriptionStoreContext;
    }

    public void setRemoteSubscriptionStoreContext(String remoteSubscriptionStoreContext) {
        this.remoteSubscriptionStoreContext = remoteSubscriptionStoreContext;
    }

    public NotificationService getRegistryNotificationService() {
        return registryNotificationService;
    }

    public void setRegistryNotificationService(NotificationService registryNotificationService) {
        this.registryNotificationService = registryNotificationService;
    }
}
