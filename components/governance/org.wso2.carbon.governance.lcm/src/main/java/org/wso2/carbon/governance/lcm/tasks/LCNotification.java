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

/**
 * This bean class holds data object mapping for scheduler objects.
 */
public class LCNotification {

    /**
     * Registry path of the resource.
     */
    private String regPath;

    /**
     * Lifecycle name.
     */
    private String lcName;

    /**
     * Lifecycle checkpoint id.
     */
    private String lcCheckpointId;

    /**
     * Notification date.
     */
    private String notificationDate;

    /**
     * This method is used to get registry path.
     *
     * @return          registry path.
     */
    public String getRegPath() {
        return regPath;
    }

    /**
     * This method is used to set registry path.
     *
     * @param regPath   path of the resource.
     */
    public void setRegPath(String regPath) {
        this.regPath = regPath;
    }

    /**
     * This method is used to get lifecycle checkpoint Id.
     *
     * @return          checkpoint Id.
     */
    public String getLcCheckpointId() {
        return lcCheckpointId;
    }

    /**
     * This method is used to set lifecycle checkpoint id.
     *
     * @param lcCheckpointId    checkpoint id.
     */
    public void setLcCheckpointId(String lcCheckpointId) {
        this.lcCheckpointId = lcCheckpointId;
    }

    /**
     * This method is used to get lifecycle name.
     *
     * @return          lifecycle name.
     */
    public String getLcName() {
        return lcName;
    }

    /**
     * This method is used to set lifecycle name.
     *
     * @param lcName    lifecycle name.
     */
    public void setLcName(String lcName) {
        this.lcName = lcName;
    }

    /**
     * This method is used to get notification date.
     *
     * @return          notification date in yyyy-mm-dd format.
     */
    public String getNotificationDate() {
        return notificationDate;
    }

    /**
     * This method is used to set notification date.
     *
     * @param notificationDate  notification date in yyyy-mm-dd format.
     */
    public void setNotificationDate(String notificationDate) {
        this.notificationDate = notificationDate;
    }
}
