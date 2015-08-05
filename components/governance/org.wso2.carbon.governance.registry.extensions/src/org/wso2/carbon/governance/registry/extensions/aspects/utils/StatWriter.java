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

package org.wso2.carbon.governance.registry.extensions.aspects.utils;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.utils.RegistryUtils;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import java.io.StringReader;
import java.sql.Timestamp;
import java.util.Map;

public class StatWriter {

    private static final Log log = LogFactory.getLog(StatWriter.class);
    private static final String LOG_DEFAULT_PATH = "/_system/governance/repository/components/org.wso2.carbon.governance/lifecycles/history";
    private static final String REGISTRY_LIFECYCLE_HISTORY_ORDER = "registry.lifecycle_history.order";
    private static OMFactory factory = OMAbstractFactory.getOMFactory();

    public static void writeHistory(StatCollection currentCollection) {

        try {
            Registry systemRegistry = currentCollection.getRegistry();
            String resourcePath = currentCollection.getResourcePath();
            Resource statResource;

            String statResourcePath;
            if (currentCollection.getOriginalPath().equals(resourcePath)) {
                statResourcePath = getStatResourcePath(resourcePath);
            }else{
                statResourcePath = getStatResourcePath(currentCollection.getOriginalPath());
            }

            if (systemRegistry.resourceExists(statResourcePath)) {
                statResource = systemRegistry.get(statResourcePath);
            } else {
                statResource = systemRegistry.newResource();
                statResource.setMediaType("application/xml");
            }

            statResource.setContent(buildOMContent(statResource, currentCollection));
            systemRegistry.put(statResourcePath, statResource);
        } catch (Exception e) {
            log.error("Failed to add lifecycle history", e);
        }
    }

    private static String getStatResourcePath(String resourcePath) {
        return LOG_DEFAULT_PATH + RegistryConstants.PATH_SEPARATOR
                + resourcePath.replace(RegistryConstants.PATH_SEPARATOR,"_");
    }

    private static String buildOMContent(Resource resource, StatCollection currentCollection) throws Exception {
        String content = null;

        String property = resource.getProperty(REGISTRY_LIFECYCLE_HISTORY_ORDER);
        int newOrder = 0;
        if (property != null) {
            newOrder = Integer.parseInt(property) + 1;
        }

        resource.setProperty(REGISTRY_LIFECYCLE_HISTORY_ORDER, "" + newOrder);

        if (resource.getContent() != null) {
            if (resource.getContent() instanceof String) {
                content = (String) resource.getContent();
            } else if (resource.getContent() instanceof byte[]) {
                content = RegistryUtils.decodeBytes((byte[]) resource.getContent());
            }
        }

        if (content == null) {
            content = buildInitialOMElement().toString();
        }

        return addNewContentElement(content, currentCollection, newOrder);
    }

    private static String addNewContentElement(String currentContent, StatCollection currentCollection, int order) throws Exception {
        OMElement currentOmElement;

        javax.xml.stream.XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(
                new StringReader(currentContent));
        StAXOMBuilder builder = new StAXOMBuilder(reader);

        currentOmElement = builder.getDocumentElement();

//        Adding the parent element
        OMElement itemChildElement = factory.createOMElement("item", currentOmElement.getNamespace(), currentOmElement);

//        Adding attributes
//        Adding the order attribute
        itemChildElement.addAttribute("order", "" + order, null);

//        Adding the user attribute
        itemChildElement.addAttribute("user", currentCollection.getUserName(), null);

        // Adding the aspect name
        if(currentCollection.getAspectName() != null) {
            itemChildElement.addAttribute("aspect", currentCollection.getAspectName(), null);
        }

//        Adding the state attribute
        itemChildElement.addAttribute("state", currentCollection.getState(), null);

//        Adding the target state if it is not null
        if (currentCollection.getTargetState() != null) {
            itemChildElement.addAttribute("targetState",currentCollection.getTargetState(),null);
        }

//        Adding the originalPath attribute
        itemChildElement.addAttribute("originalPath", currentCollection.getResourcePath(), null);

//        Adding the timestamp of the invocation
        itemChildElement.addAttribute("timestamp", (new Timestamp(currentCollection.getTimeMillis())).toString(), null);

//        Adding the action element
        OMElement actionElement = factory.createOMElement("action", itemChildElement.getNamespace(), itemChildElement);

//        Adding the action type attribute
        actionElement.addAttribute("type", currentCollection.getActionType(), null);

//        Adding the action name
        actionElement.addAttribute("name", currentCollection.getAction(), null);

        if (currentCollection.getValidations() != null) {
//          Adding the sub elements under action element
            OMElement actionValidationsElement = factory.createOMElement("validations", actionElement.getNamespace(), actionElement);

            Map<String,OMElement> validations = currentCollection.getValidations();

            for (Map.Entry<String, OMElement> validation : validations.entrySet()) {
                OMElement validationElement = factory.createOMElement("validation"
                        , actionValidationsElement.getNamespace(), actionValidationsElement);
                validationElement.addAttribute("name",validation.getKey(),null);

                if (validation.getValue() != null) {
                    OMElement validationsInfoElement = factory.createOMElement("operations"
                            ,validationElement.getNamespace(),validationElement);
                    validationsInfoElement.addChild(validation.getValue());
                }
            }
        }
        if (currentCollection.getExecutors() != null) {
            OMElement actionExecutors = factory.createOMElement("executors", actionElement.getNamespace(), actionElement);

            Map<String,OMElement> executors = currentCollection.getExecutors();

            for (Map.Entry<String, OMElement> executor : executors.entrySet()) {
                OMElement executorElement = factory.createOMElement("executor"
                        , actionExecutors.getNamespace(), actionExecutors);
                executorElement.addAttribute("name", executor.getKey(), null);

                if (executor.getValue() != null) {
                    OMElement executorInfoElement =factory.createOMElement("operations"
                            ,executorElement.getNamespace(),executorElement);
                    executorInfoElement.addChild(executor.getValue());
                }
            }
        }
        if(currentCollection.getActionValue() != null){
    //        adding comments
            OMElement actionValueElement = factory.createOMElement("value", actionElement.getNamespace(), actionElement);
            actionValueElement.setText(currentCollection.getActionValue());
        }
        if(log.isDebugEnabled())  {
            log.debug(currentOmElement.getFirstElement().toString());
        }

        return currentOmElement.toString();
    }

    private static OMElement buildInitialOMElement() {
        OMElement initialOMElement;
        initialOMElement = factory.createOMElement(new QName("lifecycleHistory"));
        return initialOMElement;
    }
}
