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
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.lcm.tasks.events.LifecycleNotificationEvent;
import org.wso2.carbon.governance.lcm.util.CommonUtil;
import org.wso2.carbon.governance.lcm.util.LifecycleStateDurationUtils;
import org.wso2.carbon.governance.registry.extensions.aspects.utils.LifecycleConstants;
import org.wso2.carbon.registry.common.AttributeSearchService;
import org.wso2.carbon.registry.common.ResourceData;
import org.wso2.carbon.registry.common.eventing.NotificationService;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.eventing.services.EventingServiceImpl;
import org.wso2.carbon.registry.indexing.IndexingConstants;

import javax.xml.namespace.QName;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * This Util class holds methods to send checkpoint notifications and add schedulers.
 */
public class LCNotificationScheduler {

    private static final Log log = LogFactory.getLog(LCNotificationScheduler.class);

    // Duration of a day 1 * 24 * 60 * 60 * 1000.
    private final long durationOfADay = 86400000;

    // Used to as Qname for checkpoint id.
    private final String checkpointId = "id";

    // Date separator.
    private final String dateSeparator = "-";

    // Date length.
    private final int dateLength = 10;

    // Date format used in MySQL queries.
    private final String dateFormat = "yyyy-M-d";

    // Lifecycle property name to which is used to store.
    private final String lcCheckPointPropertyName = "registry.LCCheckpoints";

    // Super admin username.
    private final String superAdminUsername = "admin";

    // Super tenant domain.
    private final String superTenantDomain = "carbon.super";

    // Super tenant tenant Id.
    private final int superTenantId = -1234;

    // Equals constant used for solar advance search.
    private final String equals = "eq";

    // Not applicable constant used for solar advance search.
    private final String notApplicable = "na";

    /**
     * This method is used to send notifications for checkpoints in lifecycle. This method is called using the
     * scheduler task CheckpointNotificationSchedulerTask.
     */
    public void run() {
        try {
            ArrayList<LCNotification> notifications = getValidNotifications();

            // Send notifications if exists.
            if (notifications.size() > 0) {
                // Parse this for a another method.
                for (LCNotification schedulerBean : notifications) {
                    // Creating a Checkpoint notification event.
                    String notificationMessage = getNotificationMessage(schedulerBean);
                    LifecycleNotificationEvent<String> notificationEvent
                            = new LifecycleNotificationEvent<>(notificationMessage);
                    notificationEvent.setTenantId(superTenantId);
                    notificationEvent.setResourcePath(schedulerBean.getRegPath());
                    // Sending notification using notification service.
                    NotificationService notificationService = new EventingServiceImpl();
                    try {
                        notificationService.notify(notificationEvent);
                        if (log.isDebugEnabled()) {
                            log.debug("Notification '" + notificationMessage + "' sent to notification service.");
                        }
                        // Exception is caught because notificationService.notify method throws the exception from Exception
                        // class. This will be refactored after fixing this JIRA: https://wso2.org/jira/browse/REGISTRY-2433
                    } catch (Exception e) {
                        log.error("Error getting registry while getting scheduler objects to send notifications", e);
                    }
                }
            }
        } catch (RegistryException e) {
            log.error("Error while getting valid notifications on " + getCurrentDate(), e);
        }
    }

    /**
     * This method is used to add checkpoint notification schedulers to the database. These schedulers are used to
     * send notifications when they reaches the checkpoints defined in the attached lifecycle.
     *
     * @param resource              resource which the lifecycle is attached.
     * @param lifecycleName         lifecycle name.
     * @param lifecycleState        new lifecycle state after the state changed.
     * @param isInvokeAspect        is this add scheduler method is called when invoking a lifecycle.
     * @throws GovernanceException  Throws when an error occurred while adding a lifecycle notification scheduler to
     *                              the database.
     */
    public void addScheduler(ResourceImpl resource, String lifecycleName, String lifecycleState, boolean isInvokeAspect)
            throws GovernanceException {
        if (resource != null && StringUtils.isNotEmpty(lifecycleName) && StringUtils.isNotEmpty(lifecycleState)) {

            List checkpointsList = LifecycleStateDurationUtils.getDurationBeans(lifecycleName, lifecycleState);
            if (checkpointsList != null) {
                // Iterate through the checkpoint objects.
                for (Object checkpoint : checkpointsList) {
                    OMElement checkpointElement = (OMElement) checkpoint;
                    OMElement boundary = checkpointElement.getFirstChildWithName(
                            new QName(LifecycleConstants.LIFECYCLE_CONFIGURATION_NAMESPACE_URI,
                                    LifecycleConstants.LIFECYCLE_CHECKPOINT_BOUNDARY));
                    String minTimestamp = boundary
                            .getAttribute(new QName(LifecycleConstants.LIFECYCLE_LOWER_BOUNDARY)).getAttributeValue();
                    String maxTimestamp = boundary
                            .getAttribute(new QName(LifecycleConstants.LIFECYCLE_UPPER_BOUNDARY)).getAttributeValue();

                    long durationDifference = getDurationDifference(minTimestamp, maxTimestamp);

                    // Set scheduler property.
                    String propertyValue = lifecycleName + "." + checkpointElement.getAttribute(new QName(checkpointId))
                            .getAttributeValue() + "." + getNotificationDate(durationDifference);
                    resource.addProperty(lcCheckPointPropertyName, propertyValue);
                    // If the lifecycle is invoke registry.put is need to add the property because when invoke is
                    // triggered registry put method is not called.
                    if (isInvokeAspect) {
                        try {
                            CommonUtil.getRootSystemRegistry().put(resource.getPath(), resource);
                        } catch (RegistryException e) {
                            throw new GovernanceException(
                                    "Error while saving resource when lifecycle is invoked after adding property '"
                                            + propertyValue + "' to " + resource.getPath(), e);
                        }
                    }
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Invalid arguments supplied as lifecycle name: " + lifecycleName + ", lifecycle state: "
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
     * @return long         duration difference in timestamp format.
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
    private String getNotificationDate(long durationDifference) {
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

    /**
     * This method is used to create notification message.
     *
     * @param schedulerBean     scheduler bean need to create the notification message.
     * @return                  notification message.
     */
    private String getNotificationMessage(LCNotification schedulerBean) {

        String[] pathParams = schedulerBean.getRegPath().split("/");
        // Lifecycle cannot be applied to registry path "/". Hence pathParams.length is always greater than or equals
        // to one. Hence (pathParams.length - 1) cannot be a minus value.
        String resourceName = pathParams[pathParams.length - 1];

        StringBuilder stringBuilder = new StringBuilder("Resource '").append(resourceName).append("'s lifecycle '")
                .append(schedulerBean.getLcName()).append("' is reaching lifecycle checkpoint '")
                .append(schedulerBean.getLcCheckpointId()).append("' on ").append(schedulerBean.getNotificationDate())
                .append(".");
        return stringBuilder.toString();
    }

    /**
     * This method used to get current date in yyyy-M-d format.
     *
     * @return current date in yyyy-M-d format.
     */
    private String getCurrentDate() {
        DateFormat dateFormat = new SimpleDateFormat(this.dateFormat);
        //get current date time with Calendar()
        Calendar cal = Calendar.getInstance();
        return dateFormat.format(cal.getTime());
    }

    /**
     * This method is used to get valid lifecycle checkpoints to be send.
     *
     * @return                      valid lifecycle checkpoint notifications.
     * @throws RegistryException    Throws if an error occurred while getting registry or when getting lifecycle
     *                              checkpoint notification properties.
     */
    private ArrayList<LCNotification> getValidNotifications() throws RegistryException {

        ArrayList<LCNotification> notifications = new ArrayList<>();
        AttributeSearchService attributeSearchService = CommonUtil.getAttributeSearchService();
        Map<String, String> searchAttributes = new HashMap<>();
        searchAttributes.put(IndexingConstants.FIELD_PROPERTY_NAME, lcCheckPointPropertyName);
        searchAttributes.put(IndexingConstants.FIELD_RIGHT_PROPERTY_VAL, "%" + getCurrentDate()
                .replaceAll("-", "\\\\-"));
        searchAttributes.put(IndexingConstants.FIELD_RIGHT_OP, equals);
        searchAttributes.put(IndexingConstants.FIELD_LEFT_OP, notApplicable);

        ResourceData[] resourceDataList = null;
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(superTenantDomain);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(superAdminUsername);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(superTenantId);

            resourceDataList = attributeSearchService.search(CommonUtil.getRootSystemRegistry(), searchAttributes);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }

        if (resourceDataList != null) {
            for (ResourceData resourceData : resourceDataList) {
                LCNotification lcNotification = new LCNotification();
                ArrayList propValues = getLcNotificationProperties(resourceData);
                if (propValues.size() > 0) {
                    for (Object propValueObject : propValues) {
                        String propValue = (String) propValueObject;
                        String[] lcCheckpointValues = propValue.split("\\.");
                        lcNotification.setLcName(lcCheckpointValues[0]);
                        lcNotification.setLcCheckpointId(lcCheckpointValues[1]);
                        lcNotification.setNotificationDate(lcCheckpointValues[2]);
                        lcNotification.setRegPath(resourceData.getResourcePath());
                    }
                }
                notifications.add(lcNotification);
            }
        }
        return notifications;
    }

    /**
     * This method is used to get lifecycle notification properties.
     *
     * @param resourceData          search resource data.
     * @return                      lifecycle notification properties.
     * @throws RegistryException    Throws if an error occurred when getting the resource from registry.
     */
    private ArrayList getLcNotificationProperties(ResourceData resourceData) throws RegistryException {
        Properties propertyNameValues = CommonUtil.getRootSystemRegistry().get(resourceData.getResourcePath())
                .getProperties();
        Iterator propIterator = propertyNameValues.entrySet().iterator();
        ArrayList propValues = new ArrayList();
        while (propIterator.hasNext()) {
            Map.Entry entry = (Map.Entry) propIterator.next();
            String propertyName = (String) entry.getKey();
            if (lcCheckPointPropertyName.equals(propertyName)) {
                propValues = (ArrayList) entry.getValue();
            }
        }
        return propValues;
    }
}
