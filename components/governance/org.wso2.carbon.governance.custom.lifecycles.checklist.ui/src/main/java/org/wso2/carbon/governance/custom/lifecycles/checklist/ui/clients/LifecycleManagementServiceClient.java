/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.governance.custom.lifecycles.checklist.ui.clients;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.custom.lifecycles.checklist.ui.Beans.CurrentStateDurationBean;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceGovernanceExceptionException;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceStub;
import org.wso2.carbon.governance.lcm.stub.beans.xsd.DurationBean;

import java.rmi.RemoteException;
import java.util.concurrent.TimeUnit;

/**
 * This class contains the client to call lifecycle management service and get data.
 */
public class LifecycleManagementServiceClient {

    /**
     *  log object to use when logging is required in this class.
     */
    private static final Log log = LogFactory.getLog(LifecycleManagementServiceClient.class);

    /**
     * Lifecycle management service name.
     */
    private final String serviceName = "LifeCycleManagementService";

    /**
     * Duration seconds format.
     * This will produce a two digit number for seconds followed with character 's'.
     * Example 08s.
     */
    private final String durationSecondsFormat = "%02ds";

    /**
     * Duration minutes and seconds format.
     * This will provide a two digit number for minutes followed by character 'm' and a two digit number for seconds
     * followed with character 's'.
     * Example: 01m:23s.
     */
    private final String durationMinutesSecondsFormat = "%02dm:%02ds";

    /**
     * Duration hours, minutes and seconds format. This will provide a two digit number for hours followed by
     * character 'h', a two digit number for minutes followed with character 'm' and a two digit number for seconds
     * followed with character 's'.
     * Example 07h:12m:09s.
     */
    private final String durationHoursMinutesSecondsFormat = "%02dh:%02dm:%02ds";

    /**
     * Duration days, hours, minutes and seconds format.
     * This will produce a number for days followed with character 'd', a two digit number for hours followed by
     * character 'h', a two digit number for minutes followed with character 'm' and a two digit number for seconds
     * followed with character 's'.
     */
    private final String durationDaysHoursMinutesSecondsFormat = "%dd:%02dh:%02dm:%02ds";

    /**
     * Lifecycle current state time not available message
     */
    public final String timeNotAvailableMessage = "Time duration not available";

    /**
     * Lifecycle current state duration default colour
     */
    public final String currentStateDurationDefaultColour = "black";

    /**
     * Stub object of lifecycle management service.
     * This is used to call to LifeCycleManagementService service operations.
     */
    private LifeCycleManagementServiceStub stub;

    /**
     * Constructor to initialize lifecycle management service client.
     *
     * @param cookie            session cookie
     * @param backendServerURL  backend service URL
     * @param configContext     Configuration context.
     */
    public LifecycleManagementServiceClient(
            String cookie, String backendServerURL, ConfigurationContext configContext) {
        // End point reference to the service.
        String endPointReference = backendServerURL + serviceName;
        try {
            stub = new LifeCycleManagementServiceStub(configContext, endPointReference);
            ServiceClient client = stub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        } catch (AxisFault axisFault) {
            log.error("Failed to initiate lifecycle management service client. " + axisFault.getMessage(), axisFault);
        }
    }

    /**
     * This client side method to get current lifecycle state duration from lifecycle management service.
     * @param resourcePath  registry path to the resource.
     * @param lifecycleName lifecycle name associated to the resource. In multiple lifecycle scenario this service is
     *                      called once at a time.
     * @return              lifecycle current lifecycle state duration.
     */
    public CurrentStateDurationBean getLifecycleCurrentStateDuration(String resourcePath, String lifecycleName) {
        CurrentStateDurationBean currentStateDurationBean = null;
        if (!StringUtils.isEmpty(resourcePath) && !StringUtils.isEmpty(lifecycleName)) {
            try {
                DurationBean durationBean = stub.getLifecycleCurrentStateDuration(resourcePath, lifecycleName);
                if(durationBean != null) {
                    currentStateDurationBean = new CurrentStateDurationBean();
                    // Checkpoint includes its name, colour to be displayed and boundaries
                    currentStateDurationBean.setCheckpointBean(durationBean.getCheckpoint());
                    // Format time duration to dd:hh:mm:ss
                    currentStateDurationBean.setDuration(formatTimeDuration(durationBean.getDuration()));
                }
            } catch (RemoteException e) {
                log.error(serviceName + "'s operation, getLifecycleCurrentStateDuration error or its not "
                        + "unavailable", e);
            } catch (LifeCycleManagementServiceGovernanceExceptionException e) {
                log.error("Error in service: " + serviceName + " while getting lifecycle current state "
                        + "duration", e);
            }
        } else {
            log.error("Lifecycle directory path: '" + resourcePath + "' is or lifecycle name: '" + lifecycleName
                    + "' is not set");
        }
        return currentStateDurationBean;
    }

    /**
     * This method is used to format a timestamp to 'dd:hh:mm:ss'.
     *
     * @param duration  timestamp duration.
     * @return          formatted time duration to 'dd:hh:mm:ss'.
     */
    private String formatTimeDuration(long duration) {
        String timeDuration;
        long days = TimeUnit.MILLISECONDS.toDays(duration);
        long hours = TimeUnit.MILLISECONDS.toHours(duration) - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS
                .toDays(duration));
        long minutes = TimeUnit.MILLISECONDS.toMinutes(duration) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS
                .toHours(duration));
        long seconds = TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS
                .toMinutes(duration));
        // Setting the duration to a readable format.
        if (days == 0 && hours == 0 && minutes == 0) {
            timeDuration = String.format(durationSecondsFormat, seconds);
        } else if (days == 0 && hours == 0) {
            timeDuration = String.format(durationMinutesSecondsFormat, minutes, seconds);
        } else if (days == 0) {
            timeDuration = String.format(durationHoursMinutesSecondsFormat, hours, minutes, seconds);
        } else {
            timeDuration = String.format(durationDaysHoursMinutesSecondsFormat, days, hours, minutes, seconds);
        }
        return timeDuration;
    }
}
