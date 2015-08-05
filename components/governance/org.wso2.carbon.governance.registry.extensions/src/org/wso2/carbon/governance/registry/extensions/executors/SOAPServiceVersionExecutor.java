/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.wso2.carbon.governance.registry.extensions.executors;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.api.util.GovernanceConstants;
import org.wso2.carbon.governance.registry.extensions.aspects.utils.LifecycleConstants;
import org.wso2.carbon.governance.registry.extensions.aspects.utils.StatCollection;
import org.wso2.carbon.governance.registry.extensions.aspects.utils.StatWriter;
import org.wso2.carbon.governance.registry.extensions.executors.utils.Utils;
import org.wso2.carbon.governance.registry.extensions.interfaces.Execution;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.session.CurrentSession;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.extensions.utils.CommonConstants;
import org.wso2.carbon.registry.extensions.utils.CommonUtil;

import javax.xml.stream.XMLStreamException;
import java.util.*;

import static org.wso2.carbon.governance.registry.extensions.aspects.utils.Utils.getHistoryInfoElement;
import static org.wso2.carbon.governance.registry.extensions.executors.utils.ExecutorConstants.*;
import static org.wso2.carbon.governance.registry.extensions.executors.utils.Utils.*;
import static org.wso2.carbon.registry.extensions.utils.CommonUtil.setServiceVersion;

public class SOAPServiceVersionExecutor implements Execution {
    private static final Log log = LogFactory.getLog(SOAPServiceVersionExecutor.class);

    //    from the old code
    private String serviceMediaType = "application/vnd.wso2-soap-service+xml";

    //    To track whether we need to move comments,tags,ratings and all the associations.
    private boolean copyComments = false;
    private boolean copyTags = false;
    private boolean copyRatings = false;
    private boolean copyAllAssociations = false;
    private boolean copyDependencies = true;
    private boolean override = false;

    private static final String ASSOCIATION = "association";
    private static final String LIFECYCLE_ASPECT_NAME= "registry.LC.name";
    private boolean isAuditEnabled = true;

    private Map parameterMap = new HashMap();

    public void init(Map parameterMap) {
        //To change body of implemented methods use File | Settings | File Templates.
        this.parameterMap = parameterMap;

        if (parameterMap.get(SERVICE_MEDIA_TYPE_KEY) != null) {
            serviceMediaType = parameterMap.get(SERVICE_MEDIA_TYPE_KEY).toString();
        }
        if (parameterMap.get(COPY_COMMENTS) != null) {
            copyComments = Boolean.parseBoolean((String) parameterMap.get(COPY_COMMENTS));
        }
        if (parameterMap.get(COPY_TAGS) != null) {
            copyTags = Boolean.parseBoolean((String) parameterMap.get(COPY_TAGS));
        }
        if (parameterMap.get(COPY_RATINGS) != null) {
            copyRatings = Boolean.parseBoolean((String) parameterMap.get(COPY_RATINGS));
        }
        if (parameterMap.get(COPY_ASSOCIATIONS) != null) {
            copyAllAssociations = Boolean.parseBoolean((String) parameterMap.get(COPY_ASSOCIATIONS));
        }
        if (parameterMap.get(COPY_DEPENDENCIES) != null) {
            copyDependencies = Boolean.parseBoolean((String) parameterMap.get(COPY_DEPENDENCIES));
        }
        if (parameterMap.get(OVERRIDE) != null) {
            override = Boolean.parseBoolean((String) parameterMap.get(OVERRIDE));
        }

    }

    public boolean execute(RequestContext requestContext, String currentState, String targetState) {
//        To keep track of the registry transaction state
        boolean transactionStatus = false;
        OMElement historyOperation = null;
        List<String> otherDependencyList = new ArrayList<String>();
//        for logging purposes
        try {
            historyOperation = AXIOMUtil.stringToOM("<operation></operation>");
        } catch (XMLStreamException e) {
            log.error(e);
        }

//        getting the necessary values from the request context
        Resource resource = requestContext.getResource();
        Registry registry = requestContext.getRegistry();
        String resourcePath = requestContext.getResourcePath().getPath();

        Map<String, String> currentParameterMap = new HashMap<String, String>();
        Map<String, String> newPathMappings;

//        Returning true since this executor is not compatible with collections
        if (resource instanceof Collection) {
            return true;
        } else if (resource.getMediaType() == null || "".equals(resource.getMediaType().trim())) {
            log.warn("The media-type of the resource '" + resourcePath
                    + "' is undefined. Hence exiting the service version executor.");
            return true;
        } else if (!resource.getMediaType().equals(serviceMediaType)) {
//            We have a generic copy executor to copy any resource type.
//            This executor is written for services.
//            If a resource other than a service comes here, then we simply return true
//            since we can not handle it using this executor.
            return true;
        }

//        Getting the target environment and the current environment from the parameter map.

        String targetEnvironment = RegistryUtils.getAbsolutePath(registry.getRegistryContext(),
                                  (String) parameterMap.get(TARGET_ENVIRONMENT));
        String currentEnvironment = RegistryUtils.getAbsolutePath(registry.getRegistryContext(),
                                  (String) parameterMap.get(CURRENT_ENVIRONMENT));
        if ((targetEnvironment == null || currentEnvironment == null) || (currentEnvironment.isEmpty()
                || targetEnvironment.isEmpty())) {
            log.warn("Current environment and the Target environment has not been defined to the state");
//             Here we are returning true because the executor has been configured incorrectly
//             We do NOT consider that as a execution failure
//             Hence returning true here
            return true;
        }

//        Here we are populating the parameter map that was given from the UI
        if (!populateParameterMap(requestContext, currentParameterMap)) {
            log.error("Failed to populate the parameter map");
            return false;
        }

        try {
//            Starting a registry transaction
            registry.beginTransaction();

            Resource newResource = registry.newResource();
//            This loop is there to reformat the paths with the new versions.
            newPathMappings = getNewPathMappings(targetEnvironment, currentEnvironment, currentParameterMap, otherDependencyList);
//            Once the paths are updated with the new versions we do through the service resource and update the
//            content of the service resource with the new service version, wsdl path.
            if (!CommonUtil.isUpdateLockAvailable()) {
                return false;
            }
            CommonUtil.acquireUpdateLock();
            try {
//                Iterating through the list of dependencies
                for (Map.Entry<String, String> currentParameterMapEntry : currentParameterMap.entrySet()) {
                    if (registry.resourceExists(currentParameterMapEntry.getKey())) {
                        String newTempResourcePath;
                        Resource tempResource = registry.get(currentParameterMapEntry.getKey());

                        if (!(tempResource instanceof Collection) && tempResource.getMediaType() != null) {
                            updateNewPathMappings(tempResource.getMediaType(), currentEnvironment, targetEnvironment,
                                    newPathMappings, currentParameterMapEntry.getKey(), currentParameterMapEntry.getValue());
                        }

                        StringBuilder resourceContent = new StringBuilder(getResourceContent(tempResource));

//                        Update resource content to reflect new paths
                        for (Map.Entry<String, String> newPathMappingsEntry : newPathMappings.entrySet()) {
                            if (resourceContent != null && !ENDPOINT_MEDIA_TYPE.equals(tempResource.getMediaType())) {
                                int index;
                                if ((index = resourceContent.indexOf(newPathMappingsEntry.getKey())) > -1) {
                                    resourceContent.replace(index, index + newPathMappingsEntry.getKey().length()
                                            , newPathMappingsEntry.getValue());
                                } else if (SCHEMA_MEDIA_TYPE.equals(tempResource.getMediaType())) {
                                    updateSchemaRelativePaths(targetEnvironment, currentEnvironment,
                                            resourceContent, newPathMappingsEntry);
                                } else if (WSDL_MEDIA_TYPE.equals(tempResource.getMediaType())) {
                                    updateWSDLRelativePaths(targetEnvironment, currentEnvironment,
                                            resourceContent, newPathMappingsEntry);
                                }
                            }
                        }
                        tempResource.setContent(resourceContent.toString());
                        newTempResourcePath = newPathMappings.get(tempResource.getPath());

//                        Checking whether this resource is a service resource
//                        If so, then we handle it in a different way
                        if ((tempResource.getMediaType() != null)
                                && (tempResource.getMediaType().equals(serviceMediaType))) {
                            newResource = tempResource;
                            OMElement serviceElement = getServiceOMElement(newResource);
                            OMFactory fac = OMAbstractFactory.getOMFactory();
//                            Adding required fields at the top of the xml which will help to easily read in service side
                            Iterator it = serviceElement.getChildrenWithLocalName("newServicePath");
                            if (it.hasNext()) {
                                OMElement next = (OMElement) it.next();
                                next.setText(newTempResourcePath);
                            } else {
                                OMElement operation = fac.createOMElement("newServicePath",
                                        serviceElement.getNamespace(), serviceElement);
                                operation.setText(newTempResourcePath);
                            }
                            setServiceVersion(serviceElement, currentParameterMap.get(tempResource.getPath()));
//                            This is here to override the default path
                            serviceElement.build();
                            resourceContent = new StringBuilder(serviceElement.toString());
                            newResource.setContent(resourceContent.toString());
                            addNewId(registry, newResource, newTempResourcePath);
                            continue;
                        }
                        addNewId(registry, tempResource, newTempResourcePath);

//                        We add all the resources other than the original one here
                        if (!tempResource.getPath().equals(resourcePath)) {
//                            adding logs
                            historyOperation.addChild(getHistoryInfoElement(newTempResourcePath + " created"));
                            registry.put(newTempResourcePath, tempResource);

                           // copyCommunityFeatures(requestContext, registry, resourcePath, newPathMappings, historyOperation);
                            copyComments(registry,newTempResourcePath,currentParameterMapEntry.getKey(),historyOperation);
                            copyRatings(requestContext.getSystemRegistry(),newTempResourcePath,currentParameterMapEntry.getKey(),historyOperation);
                            copyAllAssociations(registry,newTempResourcePath,currentParameterMapEntry.getKey(),historyOperation);
                        }
                    }
                }
//                We check whether there is a resource with the same name,namespace and version in this environment
//                if so, we make it return false based on override flag.
                if (registry.resourceExists(newPathMappings.get(resourcePath)) & !override) {
//                    This means that we should not do this operation and we should fail this
                    String message = "A resource exists with the given version";
                    requestContext.setProperty(LifecycleConstants.EXECUTOR_MESSAGE_KEY, message);
                    throw new RegistryException(message);
                }

//                This is to handle the original resource and put it to the new path
                registry.put(newPathMappings.get(resourcePath), newResource);
                historyOperation.addChild(getHistoryInfoElement(newPathMappings.get(resourcePath) + " created"));

                // Initializing statCollection object
                StatCollection statCollection = new StatCollection();
                // Set action type="association"
                statCollection.setActionType(ASSOCIATION);
                statCollection.setAction("");
                statCollection.setRegistry(registry.getRegistryContext().getEmbeddedRegistryService()
                        .getSystemRegistry(CurrentSession.getTenantId()));
                statCollection.setTimeMillis(System.currentTimeMillis());
                statCollection.setState(currentState);
                statCollection.setResourcePath(newPathMappings.get(resourcePath));
                statCollection.setUserName(CurrentSession.getUser());
                statCollection.setOriginalPath(newPathMappings.get(resourcePath));
                statCollection.setTargetState(targetState);
                statCollection.setAspectName(resource.getProperty(LIFECYCLE_ASPECT_NAME));
                // Writing the logs to the registry as history
                if (isAuditEnabled) {
                    StatWriter.writeHistory(statCollection);
                }

            } finally {
                CommonUtil.releaseUpdateLock();
            }
//            Associating the new resource with the LC
            String aspectName = resource.getProperty(REGISTRY_LC_NAME);
            registry.associateAspect(newPathMappings.get(resourcePath)
                    , aspectName);

            makeDependencies(requestContext, currentParameterMap, newPathMappings);
            makeOtherDependencies(requestContext, newPathMappings, otherDependencyList);


//           Here we are coping the comments,tags,rating and associations of the original resource
            copyCommunityFeatures(requestContext, registry, resourcePath, newPathMappings, historyOperation);
            addSubscriptionAvailableProperty(newResource);

            requestContext.setResource(newResource);
            requestContext.setOldResource(resource);
            requestContext.setResourcePath(new ResourcePath(newPathMappings.get(resourcePath)));

//           adding logs
            StatCollection statCollection = (StatCollection) requestContext.getProperty(LifecycleConstants.STAT_COLLECTION);

//            keeping the old path due to logging purposes
            newResource.setProperty(LifecycleConstants.REGISTRY_LIFECYCLE_HISTORY_ORIGINAL_PATH + aspectName,
                    statCollection.getOriginalPath());
            statCollection.addExecutors(this.getClass().getName(), historyOperation);

            transactionStatus = true;
        } catch (RegistryException e) {
            log.error("Failed to perform registry operation", e);
            return false;
        } finally {
            try {
                if (transactionStatus) {
                    registry.commitTransaction();
                } else {
                    registry.rollbackTransaction();
                }
            } catch (RegistryException e) {
                log.error("Unable to finish the transaction", e);
            }
        }
        return true;
    }

    private void copyCommunityFeatures(RequestContext requestContext, Registry registry, String resourcePath,
                                       Map<String, String> newPathMappings, OMElement historyOperation) throws RegistryException {
        copyComments(registry, newPathMappings.get(resourcePath), resourcePath, historyOperation);
        copyTags(registry, newPathMappings.get(resourcePath), resourcePath, historyOperation);
        copyRatings(requestContext.getSystemRegistry(), newPathMappings.get(resourcePath), resourcePath, historyOperation);
//       We avoid copying dependencies here because they are added to the new resources
        copyAllAssociations(registry, newPathMappings.get(resourcePath), resourcePath, historyOperation);
    }

    private void addSubscriptionAvailableProperty(Resource newResource) throws RegistryException {
        newResource.setProperty(GovernanceConstants.REGISTRY_IS_ENVIRONMENT_CHANGE, "true");

    }

    private void copyAllAssociations(Registry registry, String newPath, String path, OMElement historyOperation) throws RegistryException {
        if (copyAllAssociations) {
            Utils.copyAssociations(registry, newPath, path);
            historyOperation.addChild(getHistoryInfoElement("All associations copied"));
        }
    }

    private void copyRatings(Registry registry, String newPath, String path, OMElement historyOperation) throws RegistryException {
        if (copyRatings) {
            Utils.copyRatings(registry, newPath, path);
            historyOperation.addChild(getHistoryInfoElement("Average rating copied"));
        }
    }

    private void copyTags(Registry registry, String newPath, String path, OMElement historyOperation) throws RegistryException {
        if (copyTags) {
            Utils.copyTags(registry, newPath, path);
            historyOperation.addChild(getHistoryInfoElement("Tags copied"));
        }
    }

    private void copyComments(Registry registry, String newPath, String path, OMElement historyOperation) throws RegistryException {
        if (copyComments) {
            Utils.copyComments(registry, newPath, path);
            historyOperation.addChild(getHistoryInfoElement("Comments copied"));
        }
    }

    private void updateNewPathMappings(String mediaType, String currentExpression, String targetExpression,
                                       Map<String, String> newPathMappingsMap, String resourcePath, String version) throws RegistryException {
        boolean hasValue = false;
        if (parameterMap.containsKey(mediaType + ":" + CURRENT_ENVIRONMENT)) {
            hasValue = true;
            currentExpression = (String) parameterMap.get(mediaType + ":" + CURRENT_ENVIRONMENT);
        }
        if (parameterMap.containsKey(mediaType + ":" + TARGET_ENVIRONMENT)) {
            hasValue = true;
            targetExpression = (String) parameterMap.get(mediaType + ":" + TARGET_ENVIRONMENT);
        }
        if (hasValue) {
            String path = reformatPath(resourcePath, currentExpression, targetExpression, version);
            newPathMappingsMap.put(resourcePath, path);
        }
    }

    private void updateSchemaRelativePaths(String targetEnvironment, String currentEnvironment, StringBuilder resourceContent,
                                           Map.Entry<String, String> newPathMappingsEntry) {
        try {
            OMElement contentElement = AXIOMUtil.stringToOM(resourceContent.toString());
            updateRelativePath(targetEnvironment, currentEnvironment, contentElement, newPathMappingsEntry);
            resourceContent.replace(0, resourceContent.length(), contentElement.toString());
        } catch (XMLStreamException e) {
            log.error(e);
        }
    }

    private OMElement updateRelativePath(String targetEnvironment, String currentEnvironment, OMElement contentElement,
                                         Map.Entry<String, String> newPathMappingsEntry) throws XMLStreamException {
        List importNodes = evaluateXpath(contentElement, IMPORT_XPATH_STRING);
        for (Object node : importNodes) {
            OMElement nodeElement = (OMElement) node;
            updateRelativePathContent(targetEnvironment, currentEnvironment, newPathMappingsEntry, nodeElement);
        }
        return contentElement;
    }

    private void updateWSDLRelativePaths(String targetEnvironment, String currentEnvironment, StringBuilder resourceContent,
                                         Map.Entry<String, String> newPathMappingsEntry) {
        try {
            OMElement contentElement = AXIOMUtil.stringToOM(resourceContent.toString());
            updateRelativePath(targetEnvironment, currentEnvironment, contentElement, newPathMappingsEntry);
            List SchemaNodes = evaluateXpath(contentElement, XSD_XPATH_STRING);

            for (Object schemaNode : SchemaNodes) {
                OMElement schema = (OMElement) schemaNode;
                updateRelativePath(targetEnvironment, currentEnvironment, schema, newPathMappingsEntry);
            }
            resourceContent.replace(0, resourceContent.length(), contentElement.toString());
        } catch (XMLStreamException e) {
            log.error(e);
        }
    }

    private void updateRelativePathContent(String targetEnvironment, String currentEnvironment,
                                           Map.Entry<String, String> newPathMappingsEntry,
                                           OMElement nodeElement) {

        Iterator it = nodeElement.getAllAttributes();

        while (it.hasNext()) {
            OMAttribute next = (OMAttribute) it.next();
            if (next.getLocalName().equals("location") || next.getLocalName().equals("schemaLocation")) {
                String relativePath = next.getAttributeValue();
                String originalRelativePath = getOriginalRelativePath(currentEnvironment, newPathMappingsEntry);
                String newRelativePath = null;
                if (relativePath.equals(originalRelativePath)) {
                    newRelativePath = getNewRelativePath(targetEnvironment, newPathMappingsEntry, null);
                }else if(relativePath.endsWith(originalRelativePath)){
                    String prefix = relativePath.replace(originalRelativePath,"");
                    newRelativePath = prefix + getNewRelativePath(targetEnvironment,newPathMappingsEntry,null);
                }else {
                    boolean contains = false;
                    String[] relativePathSegments = relativePath.split(RegistryConstants.PATH_SEPARATOR);
                    String[] originalSegments = originalRelativePath.split(RegistryConstants.PATH_SEPARATOR);

                    String temp = originalRelativePath;

                    for (int i = 0; i < originalSegments.length; i++) {
                        temp = temp.substring(temp.indexOf(RegistryConstants.PATH_SEPARATOR) + 1);
                        if(relativePath.endsWith(temp)){
                            contains = true;
                            break;
                        }
                    }

                    if (contains) {
                        List<String> unwantedSegments = new ArrayList<String>();

                        for (String segment : originalSegments) {
                            if (!relativePath.contains(RegistryConstants.PATH_SEPARATOR + segment
                                    + RegistryConstants.PATH_SEPARATOR) & !relativePath.endsWith(segment)) {
                                unwantedSegments.add(segment);
                            }
                        }
                        newRelativePath =
                                getNewRelativePath(targetEnvironment, newPathMappingsEntry, unwantedSegments);

                        if(originalSegments.length > relativePathSegments.length){
                            for(int i =0;i< originalSegments.length - relativePathSegments.length ; i++){
                                newRelativePath = newRelativePath.substring(
                                        newRelativePath.indexOf(RegistryConstants.PATH_SEPARATOR) +1);
                            }
                        }
                    }
                }
                if (newRelativePath != null) {
                    next.setAttributeValue(newRelativePath);
                }
            }
        }

    }

    private String getNewRelativePath(String targetEnvironment, Map.Entry<String, String> newPathMappingsEntry,
                                      List<String> unwantedSegments) {
        StringBuilder targetBuffer = new StringBuilder();
        String targetPrefix = targetEnvironment.substring(0, targetEnvironment.indexOf(RESOURCE_PATH));
        String replacementValue = newPathMappingsEntry.getValue().replace(targetPrefix, "");

        targetPrefix = targetPrefix.substring(RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH.length() + 1);
        int targetPrefixPathSegments = targetPrefix.split(RegistryConstants.PATH_SEPARATOR).length;
        for (int i = 1; i < targetPrefixPathSegments; i++) {
            targetBuffer.append(".." + RegistryConstants.PATH_SEPARATOR);
        }
        if (unwantedSegments != null) {
            for (String unwantedSegment : unwantedSegments) {
                replacementValue = replacementValue.replaceFirst(unwantedSegment, "..");
            }
        }

        return targetBuffer.toString() + replacementValue;
    }

    private String getOriginalRelativePath(String currentEnvironment, Map.Entry<String, String> newPathMappingsEntry) {
        String prefix = currentEnvironment.substring(0, currentEnvironment.indexOf(RESOURCE_PATH));
        String pathSuffix = (newPathMappingsEntry.getKey()).replace(prefix, "");
        StringBuilder sourceBuffer = new StringBuilder();

        prefix = prefix.substring(RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH.length() + 1);
        int prefixPathSegments = prefix.split(RegistryConstants.PATH_SEPARATOR).length;
        for (int i = 1; i < prefixPathSegments; i++) {
            sourceBuffer.append(".." + RegistryConstants.PATH_SEPARATOR);
        }
        return sourceBuffer.toString() + pathSuffix;
    }

    private Map<String, String> getNewPathMappings(String targetEnvironment, String currentEnvironment
            , Map<String, String> currentParameterMap, List<String> otherDependencyList) throws RegistryException {

        Map<String, String> newPathMappingsMap = new HashMap<String, String>();

        for (Map.Entry<String, String> keyValueSet : currentParameterMap.entrySet()) {
            String path = reformatPath(keyValueSet.getKey(), currentEnvironment, targetEnvironment,
                    keyValueSet.getValue());
//                This condition is there to check whether we need to move the resources
//                The executor will not execute beyond this point, to all the resources that are not under the given environment prefix
            if (path.equals(keyValueSet.getKey())) {
                log.info("Resource " + path + " is not in the given environment");
                otherDependencyList.add(path);
                continue;
            }
            newPathMappingsMap.put(keyValueSet.getKey(), path);
        }

        for (String otherDependency : otherDependencyList) {
            currentParameterMap.remove(otherDependency);
        }
        return newPathMappingsMap;
    }

    private boolean populateParameterMap(RequestContext requestContext, Map<String, String> currentParameterMap) {
        Set parameterMapKeySet = (Set) requestContext.getProperty("parameterNames");
        if (parameterMapKeySet == null) {
            if (serviceMediaType.equals(requestContext.getResource().getMediaType())) {
                if (getServiceOMElement(requestContext.getResource()) != null) {
                    currentParameterMap.put(requestContext.getResource().getPath(),
                            org.wso2.carbon.registry.common.utils.CommonUtil.getServiceVersion(
                                    getServiceOMElement(requestContext.getResource())));
                    return true;
                }
            }
            return false;
        }
        for (Object entry : parameterMapKeySet) {
            String key = (String) entry;
            if (!key.equals("preserveOriginal") && !key.endsWith(".item")) {
                currentParameterMap.put(key, (String) requestContext.getProperty(key));
            }
        }
        if (currentParameterMap.isEmpty()) {
            if (serviceMediaType.equals(requestContext.getResource().getMediaType())) {
                if (getServiceOMElement(requestContext.getResource()) != null) {
                    currentParameterMap.put(requestContext.getResource().getPath(),
                            org.wso2.carbon.registry.common.utils.CommonUtil.getServiceVersion(
                                    getServiceOMElement(requestContext.getResource())));

//                    add if any dependencies are available for this resource under the version of the service
                    if (copyDependencies) {
                        try {
                            Association[] associations =
                                    requestContext.getRegistry().getAllAssociations(requestContext.getResource().getPath());
                            if (associations != null && associations.length != 0) {
                                for (Association association : associations) {
                                    if (association.getAssociationType().equals(CommonConstants.DEPENDS)) {
                                        if (requestContext.getResource().getPath().equals(association.getSourcePath())) {
                                            currentParameterMap.put(association.getDestinationPath(),
                                                    org.wso2.carbon.registry.common.utils.CommonUtil.getServiceVersion(
                                                            getServiceOMElement(requestContext.getResource())));
                                        }
                                    }
                                }
                            }

                        } catch (RegistryException e) {
                            log.error(e);
                        }
                    }
                }
            }
        }
        return true;
    }

    /*
    * This method returns the target path. The target path is calculated from the given expression
    * When calculating the target path, we split the current path using the given current expression and then map the
    * path segments to the corresponding ones in the target path expression
    * */
    private String reformatPath(String path, String currentExpression, String targetExpression, String newResourceVersion) throws RegistryException {
        TreeMap<Integer, String> indexMap = new TreeMap<Integer, String>();

        String returnPath = targetExpression;
        String prefix;

        if (currentExpression.equals(targetExpression)) {
            return path;
        }
        indexMap.put(currentExpression.indexOf(RESOURCE_NAME), RESOURCE_NAME);
        indexMap.put(currentExpression.indexOf(RESOURCE_PATH), RESOURCE_PATH);
        indexMap.put(currentExpression.indexOf(RESOURCE_VERSION), RESOURCE_VERSION);

        String tempExpression = currentExpression;

        while (indexMap.lastKey() < tempExpression.lastIndexOf(RegistryConstants.PATH_SEPARATOR)) {
            tempExpression = tempExpression.substring(0, tempExpression.lastIndexOf(RegistryConstants.PATH_SEPARATOR));
            path = path.substring(0, path.lastIndexOf(RegistryConstants.PATH_SEPARATOR));
        }

        prefix = currentExpression.substring(0, currentExpression.indexOf(indexMap.get(indexMap.higherKey(-1))));

        if (!path.startsWith(prefix)) {
            return path;
        }
        path = path.replace(prefix, "");

        while (true) {
            if (indexMap.firstKey() < 0) {
                indexMap.pollFirstEntry();
            } else {
                break;
            }
        }

        while (true) {
            if (indexMap.size() == 0) {
                break;
            }
            Map.Entry lastEntry = indexMap.pollLastEntry();
            if (lastEntry.getValue().equals(RESOURCE_PATH)) {
                String pathValue = path;

                for (int i = 0; i < indexMap.size(); i++) {
//                    pathValue = formatPath(pathValue.substring(path.indexOf(RegistryConstants.PATH_SEPARATOR)));
                    pathValue = formatPath(pathValue.substring(pathValue.indexOf(RegistryConstants.PATH_SEPARATOR)));
                }

                if (!pathValue.equals("")) {
                    returnPath = returnPath.replace(RESOURCE_PATH, formatPath(pathValue));
                    path = path.replace(pathValue, "");
                } else {
                    returnPath = returnPath.replace("/" + lastEntry.getValue(), "");
                }

                continue;
            }
            if (lastEntry.getValue().equals(RESOURCE_VERSION)) {
                returnPath = returnPath.replace(RESOURCE_VERSION, newResourceVersion);
                if (path.contains("/")) {
                    path = path.substring(0, path.lastIndexOf(RegistryConstants.PATH_SEPARATOR));
                } else {
                    path = "";
                }
                continue;
            }

            String tempPath;
            if (path.contains("/")) {
                tempPath = path.substring(path.lastIndexOf(RegistryConstants.PATH_SEPARATOR) + 1);
            } else {
                tempPath = path;
            }
            if (!tempPath.equals("")) {
                returnPath = returnPath.replace((String) lastEntry.getValue(), formatPath(tempPath));
                if (path.contains("/")) {
                    path = path.substring(0, path.lastIndexOf(RegistryConstants.PATH_SEPARATOR));
                } else {
                    path = "";
                }
            } else {
                returnPath = returnPath.replace("/" + lastEntry.getValue(), "");
                if (path.contains("/")) {
                    path = path.substring(0, path.lastIndexOf(RegistryConstants.PATH_SEPARATOR));
                }
            }

        }

//        Adding the version validation here.
        if(!newResourceVersion.matches("^\\d+[.]\\d+[.]\\d+(-[a-zA-Z0-9]+)?$")){
            String message = "Invalid version found for " +
                    RegistryUtils.getResourceName(path);
            log.error(message);
            throw new RegistryException(message);
        }
        if (returnPath.contains(RESOURCE_VERSION)) {
            return returnPath.replace(RESOURCE_VERSION, newResourceVersion);
        }
        return returnPath;
    }
}