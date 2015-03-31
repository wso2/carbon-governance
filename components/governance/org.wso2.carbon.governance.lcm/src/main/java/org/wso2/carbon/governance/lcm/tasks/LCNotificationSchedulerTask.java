/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.governance.lcm.tasks;

import org.wso2.carbon.ntask.core.Task;

import java.util.Map;

/**
 * This class includes the methods related to sending notifications when a lifecycle reaches its checkpoints.
 */
public class LCNotificationSchedulerTask implements Task {

    /**
     * Method set properties related to schedule task checkpoint notification scheduler execution.
     *
     * @param map Property map
     */
    public void setProperties(Map<String, String> map) {
    }

    /**
     * Method to to init in checkpoint notification scheduler.
     */
    public void init() {
    }

    /**
     * Method to execute when the schedule task lifecycle notification scheduler invokes.
     */
    public void execute() {
        LCNotificationScheduler notificationScheduler = new LCNotificationScheduler();
        notificationScheduler.run();
    }
}
