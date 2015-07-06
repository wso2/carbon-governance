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

package org.wso2.carbon.governance.lcm.util;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaxen.JaxenException;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifactImpl;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.lcm.beans.BoundaryBean;
import org.wso2.carbon.governance.lcm.beans.CheckpointBean;
import org.wso2.carbon.governance.lcm.beans.DurationBean;
import org.wso2.carbon.governance.registry.extensions.aspects.utils.LifecycleConstants;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * This class holds the utility methods related to lifecycle stateDuration.
 */
public class LifecycleStateDurationUtils {

    /**
     * Log variable used to log.
     */
    private static final Log log = LogFactory.getLog(LifecycleStateDurationUtils.class);

    /**
     * This method is used to get CurrentLifecycleStateDuration from registry history.
     *
     * @param registryPathToResource    registry path to the resource.
     * @param lifecycleName             name of the lifecycle.
     * @param registry                  root registry object.
     * @return                          duration of the lifecycle state since last state update.
     * @throws GovernanceException      Throws when when:
     *                                  <ul>
     *                                      <li>If an error occurs when generating the history file.</li>
     *                                      <li>If xPath reading fails in history file.</li>
     *                                  </ul>
     */
    public static DurationBean getCurrentLifecycleStateDuration(String registryPathToResource, String lifecycleName,
            Registry registry) throws GovernanceException {
        if (StringUtils.isEmpty(registryPathToResource) || StringUtils.isEmpty(lifecycleName) || registry == null) {
            throw new IllegalArgumentException("Invalid arguments supplied as registryPathToResource: '" +
                    registryPathToResource + "', lifecycleName: " + lifecycleName + " and registry.");
        }
        DurationBean durationBean = null;
            String historyResourcePath = LifecycleConstants.LOG_DEFAULT_PATH + registryPathToResource
                    .replaceAll("/", "_");
            UserRegistry userRegistry = (UserRegistry) registry;
            try {
                if(log.isDebugEnabled()){
                    log.debug("Resource history file accessed in: " + historyResourcePath);
                }
                if (userRegistry.resourceExists(historyResourcePath)) {
                    String textContent = RegistryUtils.decodeBytes((byte[]) userRegistry.get(historyResourcePath)
                            .getContent());
                    boolean historyFileEmpty = StringUtils.isEmpty(textContent);
                    if(log.isDebugEnabled()){
                        log.debug("Resource history file empty = " + historyFileEmpty);
                    }
                    if (!historyFileEmpty) {
                        // Reading the history file and generating a document element
                        OMElement omElement = getHistoryElement(textContent, lifecycleName, historyResourcePath);
                        String lastStateChangedTime = omElement.getAttribute(new QName(LifecycleConstants
                                .HISTORY_ITEM_TIME_STAMP)).getAttributeValue();
                        // Return checkpoint object.
                        durationBean = getCheckpointByDuration(lifecycleName, calculateTimeDifference(getCurrentTime(),
                                lastStateChangedTime), registryPathToResource, registry);
                    }
                }
            } catch (RegistryException e) {
                throw new GovernanceException(
                        "Error while checking resource exists for: '" + registryPathToResource + "'", e);
            }
        return durationBean;
    }

    /**
     * This method used to calculate time difference of two timestamps.
     *
     * @param timeStampOne              latest timestamp.
     * @param timeStampTwo              earlier timestamp.
     * @return timeDurationTimestamp    timestamp difference from current time to current lifecycle last state changed
     *                                  time.
     */
    public static long calculateTimeDifference(String timeStampOne, String timeStampTwo) {
        if (StringUtils.isEmpty(timeStampOne) && StringUtils.isEmpty(timeStampTwo)) {
            throw new IllegalArgumentException("Invalid arguments supplied as timestamp one: '" + timeStampOne + "' or"
                    + " " + "timestamp two: '" + timeStampTwo + "' is not set");
        }
        return Timestamp.valueOf(timeStampOne).getTime() - Timestamp.valueOf(timeStampTwo).getTime();
    }

    /**
     * This method provides AXIOMPath respective to the given Xpath.
     *
     * @param XpathString           Xpath.
     * @return                      AXIOMPath.
     * @throws GovernanceException  Throws when an exception occurs while getting value for the xPath.
     */
    private static AXIOMXPath getAxiomPath(String XpathString) throws GovernanceException {
        try {
            return new AXIOMXPath(XpathString);
        } catch (JaxenException e) {
            throw new GovernanceException("Error while getting value relevant to Xpath: " + XpathString, e);
        }
    }

    /**
     * This method returns a duration bean which includes data related to the current lifecycle state.
     *
     * @param lifecycleName             lifecycle name.
     * @param duration                  duration of the current lifecycle state.
     * @param registryPathToResource    registry path to the resource.
     * @param registry                  root registry.
     * @return                          durationBean of the checkpoint.
     * @throws GovernanceException      Throws when GovernanceException's are thrown from getCurrentLifecycleState and
     *                                  getLifecycleConfigurationElement methods.
     */
    private static DurationBean getCheckpointByDuration(String lifecycleName, long duration,
            String registryPathToResource, Registry registry) throws GovernanceException {
        DurationBean durationBean = new DurationBean();
        // Get current lifecycle state
        String lifecycleCurrentState = getCurrentLifecycleState(registry, registryPathToResource, lifecycleName);
        if (StringUtils.isNotEmpty(lifecycleCurrentState)) {
            // Get lifecycle configurations OM element
            OMElement lifecycleConfigurationElement = getLifecycleConfigurationElement(lifecycleName);
            String xpathString = LifecycleConstants.XPATH_STATE_WITH_ID + lifecycleCurrentState + "']"
                    + LifecycleConstants.XPATH_CHECKPOINT;
            List checkpoints = CommonUtil.evaluateXpath(lifecycleConfigurationElement, xpathString, null);

            CheckpointBean checkpointBean = new CheckpointBean();

            if (!checkpoints.isEmpty()) {
                for (Object checkpoint : checkpoints) {
                    OMElement checkpointElement = (OMElement) checkpoint;
                    OMElement boundary = checkpointElement.getFirstChildWithName(new QName
                            (LifecycleConstants.LIFECYCLE_CONFIGURATION_NAMESPACE_URI,
                                    LifecycleConstants.LIFECYCLE_CHECKPOINT_BOUNDARY));
                    String minTimestamp = boundary.getAttribute(new QName(LifecycleConstants.LIFECYCLE_LOWER_BOUNDARY))
                            .getAttributeValue();
                    String maxTimestamp = boundary.getAttribute(new QName(LifecycleConstants.LIFECYCLE_UPPER_BOUNDARY))
                            .getAttributeValue();

                    if (isDurationBetweenTimestamps(duration, minTimestamp, maxTimestamp)) {
                        // Get colour code
                        String durationColour = checkpointElement.getAttribute(new QName(
                                LifecycleConstants.LIFECYCLE_DURATION_COLOUR))
                                .getAttributeValue();
                        // Get checkpoint name
                        String checkpointName = checkpointElement.getAttribute(new QName(
                                LifecycleConstants.LIFECYCLE_CHECKPOINT_NAME))
                                .getAttributeValue();
                        // Set checkpoint boundaries
                        BoundaryBean checkpointBoundaries = new BoundaryBean();
                        checkpointBoundaries.setMin(minTimestamp);
                        checkpointBoundaries.setMax(maxTimestamp);
                        // Set checkpointBean
                        checkpointBean.setCheckpointBoundaries(checkpointBoundaries);
                        checkpointBean.setDurationColour(durationColour.toLowerCase());
                        checkpointBean.setName(checkpointName);
                    }
                }
            }
            // Setting the duration
            durationBean.setDuration(duration);
            // Setting checkpointBean
            durationBean.setCheckpoint(checkpointBean);
        }
        return durationBean;
    }

    /**
     * This method used to check whether a duration is between a specific boundary.
     *
     * @param duration  lifecycle current state duration timestamp.
     * @param minTime   boundary lower value.
     * @param maxTime   boundary upper value.
     * @return          true when duration is between the boundary.
     */
    private static boolean isDurationBetweenTimestamps(long duration, String minTime, String maxTime) {
        boolean result = false;
        // Current duration in milly seconds
        long durationInMillySeconds = TimeUnit.MILLISECONDS.convert(duration, TimeUnit.MILLISECONDS);
        // Get checkpoint boundary values in milly seconds
        long minBoundaryInMillySeconds = getMillySecondsByDuration(minTime);
        // Get checkpoint boundary values in milly seconds
        long maxBoundaryInMillySeconds = getMillySecondsByDuration(maxTime);
        // Check the duration is between the boundaries
        if (minBoundaryInMillySeconds < durationInMillySeconds && durationInMillySeconds < maxBoundaryInMillySeconds) {
            result = true;
        }
        return result;
    }

    /**
     * This method is used to get duration in milly seconds by passing the duration as a String.
     * @param duration  duration as a String.
     * @return          duration in milly seconds.
     */
    public static long getMillySecondsByDuration(String duration) {
        if (StringUtils.isEmpty(duration)) {
            throw new IllegalArgumentException("Invalid arguments supplied as duration: " + duration);
        }

        String formattedDuration = duration.replaceAll("d", "").replaceAll("h", "").replaceAll("m", "")
                .replaceAll("s", "");

        String[] tokens = formattedDuration.split(":");
        long secondsToMillySeconds = Long.parseLong(tokens[3]) * 1000;
        long minutesToMillySeconds = Long.parseLong(tokens[2]) * 60 * 1000;
        long hoursToMillySeconds = Long.parseLong(tokens[1]) * 60 * 60 * 1000;
        long daysToMillySeconds = Long.parseLong(tokens[0]) * 24 * 60 * 60 * 1000;
        return daysToMillySeconds + secondsToMillySeconds + minutesToMillySeconds + hoursToMillySeconds;
    }

    /**
     * This method is used to get current lifecycle state.
     *
     * @param registry                  core registry.
     * @param registryPathToResource    registry path to the resource.
     * @param lifecycleName             lifecycle name.
     * @return                          lifecycle current state.
     * @throws GovernanceException
     */
    private static String getCurrentLifecycleState(Registry registry, String registryPathToResource,
            String lifecycleName) throws GovernanceException {
        String currentLifecycleState = null;
        try {
            if (registry.resourceExists(registryPathToResource)) {
                currentLifecycleState = registry.get(registryPathToResource).getProperty(LifecycleConstants
                        .REGISTRY_LIFECYCLE + lifecycleName + LifecycleConstants.STATE);
                if (currentLifecycleState != null) {
                    return currentLifecycleState;
                } else {
                    // Set lifecycle state for default lifecycle
                    String lifecycleStateProperty = registry.get(registryPathToResource).getProperty(
                            LifecycleConstants.REGISTRY_CUSTOM_LIFECYCLE_CHECKLIST_OPTION + lifecycleName + ".0"
                                    + LifecycleConstants.ITEM);
                    if (lifecycleStateProperty != null && StringUtils.isNotEmpty(lifecycleStateProperty)) {
                        String[] lifecycleStateProperties = lifecycleStateProperty.split(":");
                        currentLifecycleState = lifecycleStateProperties[1];
                    } else {
                        GovernanceArtifact governanceArtifact;
                        try {
                            governanceArtifact = GovernanceArtifactImpl
                                    .create(registry, registry.get(registryPathToResource).getUUID());
                        } catch (RegistryException e) {
                            throw new GovernanceException("Error while creating generic artifact to resource" +
                                    registryPathToResource, e);
                        }
                        try {
                            return governanceArtifact.getLifecycleState(lifecycleName);
                        } catch (GovernanceException e) {
                            throw new GovernanceException("Error while current lifecycle state of lifecycle"
                                    + lifecycleName, e);
                        }
                    }
                }
            }
        } catch (RegistryException e) {
            throw new GovernanceException("Error while getting " + registryPathToResource + " from registry.", e);
        }
        if(log.isDebugEnabled()){
            log.debug("Lifecycle " + lifecycleName + "current state" + currentLifecycleState);
        }
        return currentLifecycleState;
    }

    /**
     * This method is used to get a list of duration beans.
     *
     * @param lifecycleName         lifecycle name.
     * @param lifecycleState        lifecycle state.
     * @return                      list of duration bean.
     * @throws GovernanceException
     */
    public static List getDurationBeans(String lifecycleName, String lifecycleState)
            throws GovernanceException {
        if (StringUtils.isEmpty(lifecycleName) || StringUtils.isEmpty(lifecycleState)) {
            throw new IllegalArgumentException("Invalid supplied for lifecycle name: " + lifecycleName + "state: "
                    + lifecycleState);
        }
        List durationBeans = null;
        OMElement lifecycleConfigurationElement = getLifecycleConfigurationElement(lifecycleName);
        if (lifecycleConfigurationElement != null) {
            String xpathString = LifecycleConstants.XPATH_STATE_WITH_ID + lifecycleState + "']"
                    + LifecycleConstants.XPATH_CHECKPOINT;
            durationBeans = CommonUtil.evaluateXpath(lifecycleConfigurationElement, xpathString, null);
        }
        return durationBeans;
    }

    /**
     * This method is used to get lifecycle configuration.
     *
     * @param lifecycleName lifecycle name.
     * @return OMElement   lifecycle configuration.
     * @throws GovernanceException
     */
    private static OMElement getLifecycleConfigurationElement(String lifecycleName)
            throws GovernanceException {
        // Get system registry
        Registry systemRegistry = (Registry) PrivilegedCarbonContext.getThreadLocalCarbonContext().getRegistry
                (RegistryType.SYSTEM_CONFIGURATION);
        String lifecycleConfiguration;
        try {
            lifecycleConfiguration = CommonUtil.getLifecycleConfiguration(lifecycleName, systemRegistry);
        } catch (RegistryException e) {
            throw new GovernanceException("Registry error while getting lifecycle configurations of '" +
                    lifecycleName + "' lifecycle.", e);
        } catch (XMLStreamException e) {
            throw new GovernanceException("XML stream error while getting lifecycle configurations of '" +
                    lifecycleName + "' lifecycle.", e);
        }

        try {
            if (lifecycleConfiguration != null && StringUtils.isNotEmpty(lifecycleConfiguration)) {
                return CommonUtil.buildOMElement(lifecycleConfiguration);
            } else {
                throw new GovernanceException("Can not get lifecycle configurations of '" + lifecycleName + "' "
                        + "lifecycle.");
            }
        } catch (RegistryException e) {
            throw new GovernanceException("Error while building OM element to lifecycle configurations of '" +
                    lifecycleName + "' lifecycle.", e);
        }
    }

    /**
     * This method is used to current time
     *
     * @return String  current time in  yyyy-MM-dd HH:mm:ss.SSS format.
     */
    public static String getCurrentTime() {
        Date currentTimeStamp = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat(LifecycleConstants.HISTORY_ITEM_TIME_STAMP_FORMAT);
        return dateFormat.format(currentTimeStamp);
    }

    /**
     * This method used to get history file document element.
     *
     * @param textContent           text content of the history file.
     * @return                      OM element created to history file.
     * @throws GovernanceException  Throws when:
     *                              <ul>
     *                                  <li>If a XMLStreamException occurs when creating the OM element.
     *                                  <li>If an IOException occurs while closing the input stream created to
     *                                  create OM element.</li>
     *                              </ul>
     */
    private static OMElement getHistoryFileDocumentElement(String textContent) throws GovernanceException {
        ByteArrayInputStream byteArrayInputStream = null;
        try {
            byteArrayInputStream = new ByteArrayInputStream(textContent.getBytes());
            return new StAXOMBuilder(byteArrayInputStream).getDocumentElement();
        } catch (XMLStreamException e) {
            throw new GovernanceException("Error while creating history file document element from " + textContent, e);
        } finally {
            if (byteArrayInputStream != null) {
                try {
                    byteArrayInputStream.close();
                } catch (IOException e) {
                    throw new GovernanceException("Error while closing byte array input for " + textContent, e);
                }
            }
        }
    }

    /**
     * This method is used to get history item node list while reading history file.
     *
     * @param targetStateXpath      xPath to the history item node list.
     * @param documentElement       history file document element.
     * @param historyResourcePath   history file resource path.
     * @return                      list of history items nodes.
     * @throws GovernanceException  Throws when an error occurs to when getting history items while evaluating the
     *                              xPath.
     */
    private static List getTransitionNodeList(AXIOMXPath targetStateXpath, OMElement documentElement, String
            historyResourcePath) throws GovernanceException{
        try {
            return  targetStateXpath.selectNodes(documentElement);
        } catch (JaxenException e) {
            throw new GovernanceException("Error while selecting nodes relevant to Xpath: "
                    + LifecycleConstants.HISTORY_ITEM_TARGET_STATE_XPATH + " from document element "
                    + "generated from " + historyResourcePath, e);
        }
    }

    /**
     * This method is used to get history item om element.
     *
     * @param textContent           history file text content.
     * @param lifecycleName         lifecycle name.
     * @param historyResourcePath   history file registry path.
     * @return                      history item OM element.
     * @throws GovernanceException  Throws when GovernanceException's thrown from methods getAxiomPath and
     *                              getTransitionNodeList.
     */
    private static OMElement getHistoryElement(String textContent, String lifecycleName, String historyResourcePath)
            throws GovernanceException {
        // Reading the history file and generating a document element
        OMElement documentElement = getHistoryFileDocumentElement(textContent);
        // Selecting 'item' nodes with an attribute 'targetState'
        String XpathString = LifecycleConstants.HISTORY_ITEM_TARGET_STATE_XPATH + "["
                + LifecycleConstants.HISTORY_ITEM_LIFECYCLE_NAME_PARAMETER + "='" + lifecycleName + "']";
        AXIOMXPath targetStateXpath = getAxiomPath(XpathString);
        // Selecting the nodes from document element by target state Xpath
        List transitionNodesList = getTransitionNodeList(targetStateXpath, documentElement, historyResourcePath);
        /*
         Getting the latest updated note to an OM element.
         If loop runs when there is a lifecycle state has changed from initial state, and else loop
         runs in initial lifecycle state when there is no lifecycle state change.
         */
        OMElement omElement;
        if (transitionNodesList != null && !transitionNodesList.isEmpty()) {
            // First node (index 0) is selected because the latest updated history was stored in it.
            omElement = (OMElement) transitionNodesList.get(0);
        } else {
            // Changing the Xpath to timestamp
            XpathString = LifecycleConstants.HISTORY_ITEM_TIME_STAMP_XPATH + "["
                    + LifecycleConstants.HISTORY_ITEM_LIFECYCLE_NAME_PARAMETER + "='" + lifecycleName + "']";
            targetStateXpath = getAxiomPath(XpathString);
            // Selecting timestamp from item nodes by timestamp Xpath
            transitionNodesList = getTransitionNodeList(targetStateXpath, documentElement, textContent);
            /*
             At this stage transitionNodesList doesn't get null values because it always has a history
             entry as item with timestamp.
             Getting the latest lifecycle history item node.
             */
            omElement = (OMElement) transitionNodesList.get(transitionNodesList.size() - 1);
        }
        return omElement;
    }
}
