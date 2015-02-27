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
package org.wso2.carbon.governance.lcm.tasks.dao;

import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.lcm.tasks.LCNotification;
import org.wso2.carbon.registry.core.Registry;

import java.util.ArrayList;

/**
 * This class contains data access objects for checkpoint
 */
public interface LifecycleNotificationDAO {

    /**
     * Checkpoint notification scheduler table name.
     */
    public static final String TABLE_NAME = "REG_CHECKPOINT_SCHEDULER";

    /**
     * Registry path id column name.
     */
    public static final String REG_PATH = "REG_PATH";

    /**
     * Lifecycle name column name.
     */
    public static final String REG_LC_NAME = "REG_LC_NAME";

    /**
     * Lifecycle checkpoint id column name.
     */
    public static final String REG_LC_CHECKPOINT_ID = "REG_LC_CHECKPOINT_ID";

    /**
     * Lifecycle state checkpoint notification date.
     */
    public static final String REG_LC_NOTIFICATION_DATE = "REG_LC_NOTIFICATION_DATE";

    /**
     * REG_UUID column name.
     */
    public static final String REG_UUID = "REG_UUID";

    /**
     * Registry tenant id column name.
     */
    public static final String REG_TENANT_ID = "REG_TENANT_ID";

    /**
     * This holds the interface method to get checkpoint notification schedulers.
     *
     * @param registry              core registry.
     * @return                      Array list of checkpoint notification data object.
     * @throws GovernanceException  Throws when a GovernanceException occurs while getting valid notifications.
     */
    public ArrayList<LCNotification> getValidNotifications(Registry registry) throws GovernanceException;

    /**
     * This holds the interface method to add checkpoint notification schedulers.
     *
     * @param registry              core registry.
     * @param schedulerBean         checkpoint notification scheduler bean.
     * @return                      boolean true is scheduler added successfully.
     */
    public boolean addScheduler(Registry registry, LCNotification schedulerBean)
            throws GovernanceException;
}
