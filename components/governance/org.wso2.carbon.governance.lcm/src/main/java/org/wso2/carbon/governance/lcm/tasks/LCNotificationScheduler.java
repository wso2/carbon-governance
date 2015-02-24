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

import org.apache.axiom.om.OMElement;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.lcm.tasks.dao.LifecycleNotificationDAO;
import org.wso2.carbon.governance.lcm.tasks.data.JDBCLifecycleNotificationDAOImpl;
import org.wso2.carbon.governance.lcm.tasks.events.LifecycleNotificationEvent;
import org.wso2.carbon.governance.lcm.util.LifecycleStateDurationUtils;
import org.wso2.carbon.governance.registry.extensions.aspects.utils.LifecycleConstants;
import org.wso2.carbon.registry.common.eventing.NotificationService;
import org.wso2.carbon.registry.common.services.RegistryAbstractAdmin;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.eventing.services.EventingServiceImpl;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * THis Util class holds methods to send checkpoint notifications and add schedulers.
 */
public class LCNotificationScheduler extends RegistryAbstractAdmin {

    private static final Log log = LogFactory.getLog(LCNotificationScheduler.class);

    // Duration of a day 1 * 24 * 60 * 60 * 1000.
    private final long durationOfADay = 86400000;

    // Used to as Qname for checkpoint id.
    private final String checkpointId = "id";

    // Date separator
    private final String dateSeparator = "-";

    // Date length
    private final int dateLength = 10;

    /**
     * THis method is used to send notifications for checkpoints in lifecycle. This method is called using the
     * scheduler task CheckpointNotificationSchedulerTask.
     *
     * @throws GovernanceException
     */
    public void run() {

        LifecycleNotificationDAO lifecycleNotificationDAO = new JDBCLifecycleNotificationDAOImpl();

        ArrayList<LCNotification> notifications;
        try {
            notifications = lifecycleNotificationDAO.getValidNotifications(getRootRegistry());
        } catch (GovernanceException e) {
            log.error("Error while getting notifications. Hold notification sending job.", e);
            /*
             Throwing the exception is no need since this is a scheduler task and scheduler tasks uses
             org.wso2carbon.ntask.core.Task interface which doesn't throw exceptions from execute() method signature.
             */
            return;
        }

        //Parse this for a another method
        for (LCNotification schedulerBean : notifications) {
            // Creating a Checkpoint notification event.
            StringBuilder stringBuilder = new StringBuilder("Resource / Service lifecycle: ").append(schedulerBean
                    .getLcName()).append(" is reaching ").append("checkpoint '").append(schedulerBean
                    .getLcCheckpointId()).append("' tomorrow");
            LifecycleNotificationEvent<String> notificationEvent = new LifecycleNotificationEvent<String>
                    (stringBuilder.toString());
            notificationEvent.setTenantId(schedulerBean.getTenantId());
            notificationEvent.setResourcePath(schedulerBean.getRegPath());
            // Sending notification using notification service.
            NotificationService notificationService = new EventingServiceImpl();
            try {
                notificationService.notify(notificationEvent);
                if(log.isDebugEnabled()){
                    log.debug("Notification " + stringBuilder + "sent to notification service.");
                }
                //TODO refactor org.wso2.carbon.registry.common.eventing.NotificationService.
            } catch (Exception e) {
                log.error("Error while getting scheduler objects to send notifications", e);
            }
        }
    }

    /**
     * This method is used to add checkpoint notification schedulers to the database. These schedulers are used to
     * send notifications when they reaches the checkpoints defined in the attached lifecycle.
     *
     * @param resource  resource which the lifecycle is attached.
     * @param lifecycleName lifecycle name.
     * @param tenantId  tenant Id.
     * @param lifecycleState    new lifecycle state after the state changed.
     * @throws GovernanceException
     */
    public void addScheduler(ResourceImpl resource, String lifecycleName, int tenantId, String lifecycleState)
            throws GovernanceException {
        if (resource != null && StringUtils.isNotEmpty(lifecycleName) && StringUtils.isNotEmpty(lifecycleState)) {

            List checkpointsList = LifecycleStateDurationUtils
                    .getDurationBeans(lifecycleName, lifecycleState);
            if (checkpointsList != null) {
                // Iterate through the checkpoint objects.
                for (Object checkpoint : checkpointsList) {
                    OMElement checkpointElement = (OMElement) checkpoint;
                    OMElement boundary = checkpointElement.getFirstChildWithName(new QName
                            (LifecycleConstants.LIFECYCLE_CONFIGURATION_NAMESPACE_URI, LifecycleConstants
                                    .LIFECYCLE_CHECKPOINT_BOUNDARY));
                    String minTimestamp = boundary
                            .getAttribute(new QName(LifecycleConstants.LIFECYCLE_LOWER_BOUNDARY)).getAttributeValue();
                    String maxTimestamp = boundary
                            .getAttribute(new QName(LifecycleConstants.LIFECYCLE_UPPER_BOUNDARY)).getAttributeValue();

                    long durationDifference = getDurationDifference(minTimestamp, maxTimestamp);

                    // Set scheduler properties.
                    LCNotification schedulerBean = new LCNotification();
                    schedulerBean.setRegPath(resource.getPath());
                    schedulerBean.setLcName(lifecycleName);
                    schedulerBean.setUUID(resource.getUUID());
                    schedulerBean.setTenantId(tenantId);
                    schedulerBean.setLcCheckpointId(checkpointElement.getAttribute(new QName(checkpointId))
                            .getAttributeValue());
                    schedulerBean.setNotificationDate(getNotificationDate(durationDifference));

                    LifecycleNotificationDAO scheduler = new JDBCLifecycleNotificationDAOImpl();
                    try {
                        scheduler.addScheduler(getRootRegistry(), schedulerBean);
                    } catch (GovernanceException e) {
                        log.error("Error while adding scheduler belongs to " + lifecycleName + "'s state '"
                                + lifecycleState + "'to database", e);
                    }
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.error("Invalid arguments supplied as lifecycle name: " + lifecycleName + ", lifecycle state: "
                            + lifecycleState);
                }
            }
        }
    }

    /**
     * This method is used get duration differences using two durations in format dd:hh:mm:ss.
     *
     * @param timeStampOne  lower duration.
     * @param timeStampTwo  higher duration.
     * @return long    duration difference in timestamp format.
     */
    private long getDurationDifference(String timeStampOne, String timeStampTwo) {
        return LifecycleStateDurationUtils.getMillySecondsByDuration(timeStampTwo) - LifecycleStateDurationUtils
                .getMillySecondsByDuration(timeStampOne);
    }

    /**
     * This method is used to calculate notification date of the scheduler.
     *
     * @param durationDifference    duration difference of the lifecycle checkpoint.
     * @return                      notification date in the format of yyyy-MM-dd.
     */
    private String getNotificationDate(long durationDifference){
        // Getting current time
        long currentTimeInMillySeconds = Calendar.getInstance().getTimeInMillis();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(durationDifference + currentTimeInMillySeconds - durationOfADay);
        int mYear = calendar.get(Calendar.YEAR);
        int mMonth = calendar.get(Calendar.MONTH);
        int mDay = calendar.get(Calendar.DAY_OF_MONTH);

        StringBuilder dateBuilder = new StringBuilder(dateLength);
        dateBuilder.append(mYear);
        dateBuilder.append(dateSeparator);
        // +1 is because mMonth is the completed months.
        dateBuilder.append(mMonth + 1);
        dateBuilder.append(dateSeparator);
        dateBuilder.append(mDay);
        return dateBuilder.toString();
    }
}
