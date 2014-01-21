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

import org.wso2.carbon.governance.notifications.stub.services.utils.xsd.SubscriptionInstance;

public class SubscriptionsUIUtils {

    public static SubscriptionInstance[] getPaginatedSubscriptions(int start, int pageLength, SubscriptionInstance[] subscriptions) {
                int availableLength = 0;
        if (subscriptions != null && subscriptions.length > 0) {
            availableLength = subscriptions.length - start;
        }
        if (availableLength < pageLength) {
            pageLength = availableLength;
        }

        SubscriptionInstance[] resultSubscriptions = new SubscriptionInstance[pageLength];
        System.arraycopy(subscriptions, start, resultSubscriptions, 0, pageLength);
        return resultSubscriptions;

    }
}
