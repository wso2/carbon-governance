/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.governance.api.endpoints.dataobjects;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifactImpl;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.util.GovernanceConstants;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.extensions.handlers.utils.EndpointUtils;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * This represents an endpoint artifact stored on the Registry. Endpoint artifacts are created as a
 * result of importing or uploading a WSDL, or when a service is created..
 */
public class EndpointImpl extends GovernanceArtifactImpl implements Endpoint {

    private static final Log log = LogFactory.getLog(EndpointImpl.class);
    private String url;
    private String name;

    /**
     * Constructor accepting resource path, identifier and a registry instance.
     * This constructor should be used only when the endpoint already saved in the registry.
     *
     * @param id       the resource identifier.
     * @param registry the registry instance.
     * @throws GovernanceException if the construction fails.
     */
    public EndpointImpl(String id, Registry registry) throws GovernanceException {
        super(id);
        associateRegistry(registry);
        loadEndpointDetails();
    }

    /**
     * Constructor accepting resource identifier and the endpoint URL.
     *
     * @param id  the resource identifier.
     * @param url the endpoint URL.
     */
    public EndpointImpl(String url, String id) {
        super(id);
        this.url = url;
    }

    /**
     * Method to obtain the endpoint URL.
     *
     * @return the endpoint URL.
     */
    @Override
    public String getUrl() {
        return url;
    }

    public QName getQName() {
        return new QName(name);
    }

    @Override
    public void setQName(QName qName) throws GovernanceException {
        this.name = qName.getLocalPart();
    }

    /**
     * Method to load the endpoint details into this artifact.
     *
     * @throws GovernanceException if the operation failed.
     */
    public void loadEndpointDetails() throws GovernanceException {
        checkRegistryResourceAssociation();
        Registry registry = getAssociatedRegistry();
        String path = getPath();
        String id = getId();
        Resource resource;
        String endpointContent;
        try {
            resource = registry.get(path);
            Object contentObj = resource.getContent();
            if (contentObj instanceof String) {
                endpointContent = (String) contentObj;
            } else {
                endpointContent = new String((byte[]) contentObj);
            }

            url = EndpointUtils.deriveEndpointFromContent(endpointContent);
            name = EndpointUtils.deriveNameFromContent(endpointContent);
        } catch (RegistryException e) {
            String msg =
                    "Error in getting the content for the artifact. artifact id: " + id + ", " +
                    "path: " + path + ".";
            log.error(msg, e);
            throw new GovernanceException(msg, e);
        }
        OMElement contentElement = buildOMElement(endpointContent);

        serializeToAttributes(contentElement, null);

        /*// and then iterate all the properties and add.
        Properties properties = resource.getProperties();
        if (properties != null && properties.size() > 0) {
            Set keySet = properties.keySet();
            if (keySet != null) {
                for (Object keyObj : keySet) {
                    String key = (String) keyObj;
                    //                    if (key.equals(GovernanceConstants.ARTIFACT_ID_PROP_KEY)) {
                    // it is not a property.
                    //                        continue;
                    //                    }
                    List values = (List) properties.get(key);
                    if (values != null) {
                        for (Object valueObj : values) {
                            String value = (String) valueObj;
                            addAttribute(key, value);
                        }
                    }
                    // Workaround for endpoint resource properties are not capturing.
                    if (!keySet.contains(GovernanceConstants.NAME_ATTRIBUTE)) {
                        try {
                            addAttribute(GovernanceConstants.NAME_ATTRIBUTE,
                                         EndpointUtils.deriveNameFromContent(endpointContent));
                        } catch (RegistryException e) {
                            String msg =
                                    "Error while deriving the attributes for the artifact. artifact id: " + id + ", " +
                                    "path: " + path + ".";
                            log.error(msg, e);
                            throw new GovernanceException(msg, e);
                        }
                    }
                    if (!keySet.contains(GovernanceConstants.VERSION_ATTRIBUTE)) {
                        try {
                            addAttribute(GovernanceConstants.VERSION_ATTRIBUTE,
                                         EndpointUtils.deriveVersionFromContent(endpointContent));
                        } catch (RegistryException e) {
                            String msg =
                                    "Error while deriving the attributes for the artifact. artifact id: " + id + ", " +
                                    "path: " + path + ".";
                            log.error(msg, e);
                            throw new GovernanceException(msg, e);
                        }
                    }
                }
            }
        } else {
            try {
                // Workaround for endpoint resource properties are not capturing.
                addAttribute(GovernanceConstants.NAME_ATTRIBUTE, EndpointUtils.deriveNameFromContent(endpointContent));
                addAttribute(GovernanceConstants.VERSION_ATTRIBUTE,
                             EndpointUtils.deriveVersionFromContent(endpointContent));
            } catch (RegistryException e) {
                String msg = "Error while deriving the attributes for the artifact. artifact id: " + id + ", " +
                             "path: " + path + ".";
                log.error(msg, e);
                throw new GovernanceException(msg, e);
            }
        }*/
    }

    public OMElement buildOMElement(String content) throws GovernanceException {
        XMLStreamReader parser;
        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            factory.setProperty(XMLInputFactory.IS_COALESCING, new Boolean(true));
            parser = factory.createXMLStreamReader(new StringReader(content));
        } catch (XMLStreamException e) {
            String msg = "Error in initializing the parser to build the OMElement.";
            log.error(msg, e);
            throw new GovernanceException(msg, e);
        }

        //create the builder
        StAXOMBuilder builder = new StAXOMBuilder(parser);
        //get the root element (in this case the envelope)

        return builder.getDocumentElement();
    }

    // Method to serialize attributes.
    private void serializeToAttributes(OMElement contentElement, String parentAttributeName)
            throws GovernanceException {
        Iterator childIt = contentElement.getChildren();
        if (childIt.hasNext()) {
            while (childIt.hasNext()) {
                Object childObj = childIt.next();
                if (childObj instanceof OMElement) {
                    OMElement childElement = (OMElement) childObj;
                    String elementName = childElement.getLocalName();
                    String attributeName =
                            (parentAttributeName == null ? "" : parentAttributeName + "_") +
                            elementName;
                    serializeToAttributes(childElement, attributeName);
                } else if (childObj instanceof OMText) {
                    OMText childText = (OMText) childObj;
                    if (childText.getNextOMSibling() == null &&
                        childText.getPreviousOMSibling() == null) {
                        // if it is only child, we consider it is a value.
                        String textValue = childText.getText();
                        addAttribute(parentAttributeName, textValue);
                    }
                }
            }
        } else {
            if (!contentElement.getChildElements().hasNext()) {
                addAttribute(parentAttributeName, null);
            }
        }

    }
}