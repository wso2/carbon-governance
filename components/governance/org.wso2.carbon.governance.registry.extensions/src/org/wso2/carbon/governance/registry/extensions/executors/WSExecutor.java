/*
 *  Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.governance.registry.extensions.executors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaxen.JaxenException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceArtifactConfiguration;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.registry.extensions.executors.utils.ExecutorConstants;
import org.wso2.carbon.governance.registry.extensions.interfaces.Execution;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;

/**
 * Web Service executor allows Web Services to be invoked during lifecycle state transitions
 */
public class WSExecutor implements Execution {

    private static final Log log = LogFactory.getLog(WSExecutor.class);
    private boolean isWSSuccessful;
    private boolean isAsynchronous = false;
    private String wsPayload;
    private String EPR;
    private String saveResponseAs = "attribute";
    private String responseName;
    private String responseDestinationPath;
    private String responseXpath;
    private String responseNamespace;
    private String responseNamespacePrefix;
    private Map parameterMap = new HashMap();

    @Override
    public void init(Map parameterMap) {
        this.parameterMap = parameterMap;
        isWSSuccessful = false;
        if (parameterMap.get(ExecutorConstants.WS_PAYLOAD) != null) {
            wsPayload = parameterMap.get(ExecutorConstants.WS_PAYLOAD)
                    .toString();
        }
        if (parameterMap.get(ExecutorConstants.WS_EPR) != null) {
            EPR = parameterMap.get(ExecutorConstants.WS_EPR).toString();
        }
        if (parameterMap.get(ExecutorConstants.WS_ASYNC) != null) {
            isAsynchronous = Boolean.parseBoolean(parameterMap.get(
                    ExecutorConstants.WS_ASYNC).toString());
        }
        if (parameterMap.get(ExecutorConstants.WS_RESPONSE_DESTINATION) != null) {
            responseDestinationPath = parameterMap.get(
                    ExecutorConstants.WS_RESPONSE_DESTINATION).toString();
        }
        if (parameterMap.get(ExecutorConstants.WS_RESPONSE_SAVE_TYPE) != null) {
            saveResponseAs = parameterMap.get(
                    ExecutorConstants.WS_RESPONSE_SAVE_TYPE).toString();
        }
        if (parameterMap.get(ExecutorConstants.WS_RESPONSE_SAVE_NAME) != null) {
            responseName = parameterMap.get(
                    ExecutorConstants.WS_RESPONSE_SAVE_NAME).toString();
        }
        if (parameterMap.get(ExecutorConstants.WS_RESPONSE_XPATH) != null) {
            responseXpath = parameterMap.get(
                    ExecutorConstants.WS_RESPONSE_XPATH).toString();
        }
        if (parameterMap.get(ExecutorConstants.WS_RESPONSE_NAMESPACE) != null) {
            responseNamespace = parameterMap.get(
                    ExecutorConstants.WS_RESPONSE_NAMESPACE).toString();
        }
        if (parameterMap.get(ExecutorConstants.WS_RESPONSE_NAMESPACE_PREFIX) != null) {
            responseNamespacePrefix = parameterMap.get(
                    ExecutorConstants.WS_RESPONSE_NAMESPACE_PREFIX).toString();
        }
    }

    @Override
    public boolean execute(RequestContext context, String currentState,
                           String targetState) {
        if (EPR == null || wsPayload == null) {
            log.error("The parameters 'epr' and 'payload' must be specified");
            return false;
        }
        if (!isAsynchronous) {
            AXIOMXPath xpathExpression;
            try {
                if (responseXpath != null && responseNamespacePrefix != null && responseNamespace != null) {
                    xpathExpression = new AXIOMXPath(responseXpath);
                    xpathExpression.addNamespace(responseNamespacePrefix, responseNamespace);
                } else {
                    xpathExpression = null;
                }
                synchronousExecuteWS(getEPR(context), getParameterizedPayload(context), context, xpathExpression);
            } catch (JaxenException e) {
                log.error(e);
                return false;
            }
        } else {
            asynchronousExecuteWS(getEPR(context),
                    getParameterizedPayload(context));
        }
        return isWSSuccessful;
    }

    /**
     * Returns the parameterized end point reference
     *
     * @param context - the request context
     * @return the parameterized end point reference
     */
    private String getEPR(RequestContext context) {

        // Getting the necessary values from the request context
        String finalEPR = "";
        finalEPR = GovernanceUtils.parameterizeString(context, EPR);
        return finalEPR;
    }

    /**
     * Returns the parameterized payload
     *
     * @param context - the request context
     * @return the parameterized payload
     */
    private OMElement getParameterizedPayload(RequestContext context) {

        OMElement finalPayload = null;
        OMElement currentPayload;
        try {
            currentPayload = getPayload();
            String payloadString = currentPayload.toString();
            String newPayloadString = "";
            newPayloadString = GovernanceUtils.parameterizeString(context,
                    payloadString);
            finalPayload = AXIOMUtil.stringToOM(newPayloadString);
			// to validate XML
		    finalPayload.toString();
        } catch (XMLStreamException e) {
            log.error("Invalid XML payload found", e);
        }
        return finalPayload;
    }

    /**
     * Invokes the web service in a synchronous manner and stores the result in
     * the registry
     *
     * @param endPointReferance - the end point reference of the web service
     * @param payload           - the payload needed to invoke the web service
     * @param context           - the request context
     * @param xpathExpression   - the xpath expression to extract the value from the response
     */
    private void synchronousExecuteWS(final String endPointReferance,
                                      final OMElement payload, RequestContext context,
                                      AXIOMXPath xpathExpression) {
        final ArrayList<OMElement> arrayList = new ArrayList<OMElement>();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Create a service client
                    ServiceClient client = new ServiceClient();

                    // Set the endpoint address
                    client.getOptions().setTo(
                            new EndpointReference(endPointReferance));

                    // Make the request and get the response
                    arrayList.add(0, client.sendReceive(payload));
                } catch (AxisFault e) {
                    log.error(e.getMessage(), e);
                }
            }
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
        // check if the response needs to be saved
        if (responseName == null) {
            isWSSuccessful = true;
            log.warn("The reponse was not saved since the parameter save.name was not defined");
            return;
        }
        // save the response received into the registry
        try {

            if (arrayList.isEmpty()) {
                isWSSuccessful = false;
                log.error("Service invocation was not successful");
                return;
            }
            OMElement response = arrayList.get(0);
            OMElement value = response;
            if (xpathExpression != null) {
                value = (OMElement) xpathExpression.selectSingleNode(response);
            }

            Registry registry = context.getSystemRegistry();
            GenericArtifact genericArtifact = null;
            String resourcePath = context.getResourcePath().getPath();
            String finalValue;
            String destinationPath;

            if (responseDestinationPath != null) {
                destinationPath = GovernanceUtils.parameterizeString(context,
                        responseDestinationPath);
            } else {
                destinationPath = resourcePath;
            }

            if (value.getFirstElement() != null) {
                finalValue = value.getFirstElement().toString();
            } else {
                finalValue = value.getText();
            }

            if (saveResponseAs.equals("attribute")) {
                try {
                    genericArtifact = (GenericArtifact) GovernanceUtils
                            .retrieveGovernanceArtifactByPath(
                                    context.getSystemRegistry(),
                                    destinationPath);
                    if (genericArtifact != null) {

                        GovernanceArtifactConfiguration governanceArtifactConfiguration = GovernanceUtils
                                .findGovernanceArtifactConfigurationByMediaType(
                                        genericArtifact.getMediaType(),
                                        registry);
                        GenericArtifactManager genericArtifactManager = new GenericArtifactManager(
                                registry,
                                governanceArtifactConfiguration.getKey());

                        if (genericArtifact.getAttribute(responseName) != null) {
                            genericArtifact.setAttribute(responseName,
                                    finalValue);
                        } else {
                            genericArtifact.addAttribute(responseName,
                                    finalValue);
                        }

                        genericArtifactManager
                                .updateGenericArtifact(genericArtifact);
                        if (destinationPath.equals(context.getResourcePath()
                                .getPath())) {
                            context.setResource(registry.get(destinationPath));
                        }
                        isWSSuccessful = true;

                    } else {
                        log.error("Unable to locate registry governance artifact for the given destination path");
                    }
                } catch (RegistryException e) {
                    log.error(e.getMessage(), e);
                }
            } else {
                Resource resource = registry.get(destinationPath);
                if (resource != null) {
                    if (resource.getProperty(responseName) != null) {
                        resource.setProperty(responseName, finalValue);
                    } else {
                        resource.addProperty(responseName, finalValue);
                    }

                    registry.put(destinationPath, resource);
                    if (destinationPath.equals(context.getResourcePath()
                            .getPath())) {
                        context.setResource(resource);
                    }
                    isWSSuccessful = true;

                } else {
                    log.error("Unable to locate registry resource for the given destination path");
                }
            }
        } catch (RegistryException e) {
            log.error(e.getMessage(), e);
        } catch (JaxenException e1) {
            log.error(e1.getMessage(), e1);
        }
    }

    /**
     * This method will execute the web service in asynchronous way
     *
     * @param endPointReferance - the end point reference of the web service
     * @param payload           - the payload needed to invoke the web service
     */
    private void asynchronousExecuteWS(final String endPointReferance,
                                       final OMElement payload) {

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Create a service client
                    ServiceClient client = new ServiceClient();

                    // Set the endpoint address
                    client.getOptions().setTo(
                            new EndpointReference(endPointReferance));

                    // Make the request
                    client.sendRobust(payload);
                    isWSSuccessful = true;
                } catch (AxisFault e) {
                    log.error(e.getMessage(), e);
                }
            }
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Extracts the payload
     *
     * @return the payload
     * @throws XMLStreamException
     */
    private OMElement getPayload() throws XMLStreamException {

        OMElement wsPayloadOM = AXIOMUtil.stringToOM(wsPayload);
		// to validate XML
		wsPayloadOM.toString();
        OMElement payload = wsPayloadOM;
        if (wsPayloadOM.getLocalName().equals("Envelope")) {
            Iterator<OMElement> childs = wsPayloadOM.getChildElements();
            while (childs.hasNext()) {
                OMElement child = childs.next();
                if (child.getLocalName().equals("Body")) {
                    payload = child.getFirstElement();
                } else {
                    payload = null;
                }
            }
            if (payload == null) {
                log.error("The SOAP body could not be found within the SOAP envelope");
            }
        } else {
            payload = wsPayloadOM;
        }
        return payload;
    }

}
