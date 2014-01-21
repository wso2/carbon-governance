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
package org.wso2.carbon.governance.generic.ui.utils;

import org.apache.axiom.om.*;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.common.ui.WSDLConstants;
import org.wso2.carbon.registry.common.ui.utils.UIUtil;
import org.wso2.carbon.registry.common.ui.utils.TreeNode;
import org.wso2.carbon.registry.common.ui.utils.TreeNodeBuilderUtil;


import javax.xml.namespace.QName;
import java.util.List;

public class WSDLTreeNodeBuilder {


    private OMElement wsdlElement;
    private String wsdlPath;
    private String actualWsdlPath;

    public WSDLTreeNodeBuilder(String wsdlPath, String wsdlContent) throws Exception {
        wsdlElement = UIUtil.buildOMElement(wsdlContent);
        this.wsdlPath = wsdlPath;
    }

    public TreeNode buildTree() throws Exception {
        // prepare the root tree node.
        String wsdlName = RegistryUtils.getResourceName(wsdlPath);
        TreeNode root = new TreeNode(TreeNodeBuilderUtil.generateKeyName(WSDLConstants.WSDL, wsdlName));
        // adding the documentation
        OMElement wsdlDocElement = TreeNodeBuilderUtil.evaluateXPathToElement(WSDLConstants.WSDL_DOCUMENTATION_EXPR,
                wsdlElement);
        if (wsdlDocElement != null) {
            OMNode wsdlDocChild = wsdlDocElement.getFirstOMChild();
            if (wsdlDocChild instanceof OMText) {
                String documentationText = ((OMText)wsdlDocChild).getText();
                if (documentationText != null) {
                    documentationText = documentationText.replace('\n', ' ').trim();
                    root.addChild(new TreeNode(WSDLConstants.WSDL_DOCUMENTATION, documentationText));
                }
            }
        }

        // adding the version
        String wsdlVersion = getWSDLVersion(wsdlElement);
        root.addChild(new TreeNode(WSDLConstants.WSDL_VERSION, wsdlVersion));

        if (wsdlVersion.equals(WSDLConstants.WSDL_VERSION_VALUE_11)) {
            buildWSDL11Services(root);

            // adding the schema imports
            List<String> wsdlImports = TreeNodeBuilderUtil.evaluateXPathToValues(WSDLConstants.WSDL_IMPORTS_EXPR, wsdlElement);
            if (wsdlImports.size() != 0) {
                TreeNode wsdlImportNode = new TreeNode(WSDLConstants.WSDL_IMPORTS);
                root.addChild(wsdlImportNode);
                for (String wsdlImport: wsdlImports) {
                    String wsdImportAbsolutePath;
                    if (actualWsdlPath != null) {
                        wsdImportAbsolutePath = TreeNodeBuilderUtil.calculateAbsolutePath(actualWsdlPath, wsdlImport);
                    } else {
                        wsdImportAbsolutePath = TreeNodeBuilderUtil.calculateAbsolutePath(wsdlPath, wsdlImport);
                    }
                    String wsdlImportEntry = "<a href='" + WSDLConstants.RESOURCE_JSP_PAGE + "?" +
                            WSDLConstants.PATH_REQ_PARAMETER + "=" +
                            wsdImportAbsolutePath + "'>" + wsdlImport + "</a>";
                    wsdlImportNode.addChild(wsdlImportEntry);
                }
            }
        } else {
            String msg = "Unknown WSDL Version.";
            throw new Exception(msg);
        }

        List<String> schemaImports = TreeNodeBuilderUtil.evaluateXPathToValues(WSDLConstants.SCHEMA_IMPORTS_EXPR, wsdlElement);
        if (schemaImports.size() != 0) {
            TreeNode schemaImportsNode = new TreeNode(WSDLConstants.SCHEMA_IMPORTS);
            root.addChild(schemaImportsNode);
            for (String schemaImport: schemaImports) {
                if (schemaImport.indexOf(";version:") > 0) {
                    schemaImport = schemaImport.substring(0, schemaImport.lastIndexOf(";version:"));
                }
                String schemaImportAbsolutePath;
                if (actualWsdlPath != null) {
                    schemaImportAbsolutePath = TreeNodeBuilderUtil.calculateAbsolutePath(actualWsdlPath, schemaImport);
                } else {
                    schemaImportAbsolutePath = TreeNodeBuilderUtil.calculateAbsolutePath(wsdlPath, schemaImport);
                }
                String schemaImportEntry = "<a href='" + WSDLConstants.RESOURCE_JSP_PAGE + "?" +
                        WSDLConstants.PATH_REQ_PARAMETER + "=" +
                        schemaImportAbsolutePath + "'>" + schemaImport + "</a>";
                schemaImportsNode.addChild(schemaImportEntry);
            }
        }


        return root;
    }

    private void buildWSDL11Services(TreeNode root) throws Exception {
        boolean servicesFound = false;
        // start with services.
        List<OMElement> serviceElements = TreeNodeBuilderUtil.evaluateXPathToElements(WSDLConstants.SERVICE_EXPR, wsdlElement);
        for (OMElement serviceElement: serviceElements) {
            String serviceName = serviceElement.getAttributeValue(new QName(WSDLConstants.SERVICE_NAME_ATTRIBUTE));
            String serviceNamespace = TreeNodeBuilderUtil.evaluateXPathToValue(WSDLConstants.TARGET_NAMESPACE, serviceElement);
            TreeNode serviceNode = new TreeNode(TreeNodeBuilderUtil.generateKeyName(WSDLConstants.WSDL_SERVICE,
                    TreeNodeBuilderUtil.convertQNameToString(serviceName, serviceNamespace)));
            root.addChild(serviceNode);

            // now build the ports for each service nodes.
            if (buildPorts(serviceNode, serviceElement)) {
                // until there are valid ports, we don't mark the service is found.
                servicesFound = true;
            }
        }
        if (!servicesFound) {
            // we will iterate through the bindings and fill the data to the wsdl root node
            boolean bindingsFound = false;
            List<OMElement> bindingElements = TreeNodeBuilderUtil.evaluateXPathToElements(WSDLConstants.BINDING_EXPR, wsdlElement);
            for (OMElement bindingElement: bindingElements) {
                bindingsFound = true;
                String bindingName = bindingElement.getAttributeValue(new QName(WSDLConstants.BINDING_NAME_ATTRIBUTE));
                String bindingNamespace = TreeNodeBuilderUtil.evaluateXPathToValue(WSDLConstants.TARGET_NAMESPACE, bindingElement);
                TreeNode bindingNode = new TreeNode(TreeNodeBuilderUtil.generateKeyName(WSDLConstants.BINDING,
                        TreeNodeBuilderUtil.convertQNameToString(bindingName, bindingNamespace)));
                root.addChild(bindingNode);
                fillBinding(bindingNode, bindingElement);
            }

            if (!bindingsFound) {
                // ok hope there are port types, if not we just give up.
                List<OMElement> portTypeElements = TreeNodeBuilderUtil.evaluateXPathToElements(WSDLConstants.PORT_TYPE_EXPR, wsdlElement);
                for (OMElement portTypeElement: portTypeElements) {
                    String portTypeName = portTypeElement.getAttributeValue(new QName(WSDLConstants.POR_TYPE_NAME_ATTRIBUTE));
                    String portTypeNamespace = TreeNodeBuilderUtil.evaluateXPathToValue(WSDLConstants.TARGET_NAMESPACE, portTypeElement);
                    TreeNode portTypeNode = new TreeNode(TreeNodeBuilderUtil.generateKeyName(WSDLConstants.PORT_TYPE,
                            TreeNodeBuilderUtil.convertQNameToString(portTypeName, portTypeNamespace)));
                    root.addChild(portTypeNode);
                    fillPortType(portTypeNode, portTypeElement);
                }
            }
        }
    }

    private boolean buildPorts(TreeNode serviceNode, OMElement serviceElement) throws Exception {
        // check the ports
        List<OMElement> portElements = TreeNodeBuilderUtil.evaluateXPathToElements(WSDLConstants.PORT_EXPR, serviceElement);
        boolean portTypesFound = false;
        for (OMElement portElement: portElements) {
            String portName = portElement.getAttributeValue(new QName(WSDLConstants.PORT_NAME_ATTRIBUTE));
            String portNamespace = TreeNodeBuilderUtil.evaluateXPathToValue(WSDLConstants.TARGET_NAMESPACE, portElement);
            TreeNode portNode = new TreeNode(TreeNodeBuilderUtil.generateKeyName(WSDLConstants.SERVICE_PORT,
                    TreeNodeBuilderUtil.convertQNameToString(portName, portNamespace)));
            serviceNode.addChild(portNode);

            // finding the bindings for the port.
            String bindingName = portElement.getAttributeValue(new QName(WSDLConstants.BINDING_ATTRIBUTE));
            if (bindingName == null) {
                continue;
            }

            String bindingNamespace = TreeNodeBuilderUtil.getNamespaceURI(bindingName, portElement);
            String bindingLocalName = TreeNodeBuilderUtil.getLocalName(bindingName);

            // so we cruising for the binding with that name.
            String bindingExpression = "/wsdl:definitions/wsdl:binding[@name='" + bindingLocalName +
                    "' and ancestor::*[@targetNamespace='" + bindingNamespace + "']]";
            // get the first bindingElement.
            OMElement bindingElement = TreeNodeBuilderUtil.evaluateXPathToElement(bindingExpression, wsdlElement);

            if (bindingElement != null) {
                // until there is a bound binding elements, we doesn't mark the existence of port type
                portTypesFound = true;
                // now check for the soap binding
                OMElement bindingProtocolElement = TreeNodeBuilderUtil.evaluateXPathToElement(WSDLConstants.SOAP_BINDING_EXPR, bindingElement);

                if (bindingProtocolElement != null) {
                    // so it is soap 1.1
                    String endpoint = TreeNodeBuilderUtil.evaluateXPathToValue(WSDLConstants.SOAP_11_ENDPOINT_EXPR, portElement);
                    portNode.addChild(new TreeNode(WSDLConstants.SOAP_11_ENDPOINT, endpoint));
                    portNode.addChild(new TreeNode(WSDLConstants.SOAP_VERSION, WSDLConstants.SOAP_11));
                } else {
                    bindingProtocolElement = TreeNodeBuilderUtil.evaluateXPathToElement(WSDLConstants.SOAP12_BINDING_EXPR, bindingElement);
                    if (bindingProtocolElement != null) {
                        // it is soap 1.2
                        String endpoint = TreeNodeBuilderUtil.evaluateXPathToValue(WSDLConstants.SOAP_12_ENDPOINT_EXPR, portElement);
                        portNode.addChild(new TreeNode(WSDLConstants.SOAP_12_ENDPOINT, endpoint));
                        portNode.addChild(new TreeNode(WSDLConstants.SOAP_VERSION, WSDLConstants.SOAP_12));
                    }
                }

                if (bindingProtocolElement != null) {
                    // We found this is SOAP (either 1.1. or 1.2), now we will get the transport and style info
                    String style = TreeNodeBuilderUtil.evaluateXPathToValue(WSDLConstants.BINDING_STYLE_EXPR, bindingProtocolElement);
                    portNode.addChild(new TreeNode(WSDLConstants.BINDING_STYLE, style));

                    String transport = bindingProtocolElement.getAttributeValue(new QName(WSDLConstants.BINDING_TRANSPORT_ATTRIBUTE));
                    if (transport.equals(WSDLConstants.HTTP_TRANSPORT_ATTRIBUTE_VALUE)) {
                        portNode.addChild(new TreeNode(WSDLConstants.BINDING_TRANSPORT,
                                WSDLConstants.HTTP_TRANSPORT_VALUE));
                    } else {
                        portNode.addChild(new TreeNode(WSDLConstants.BINDING_TRANSPORT,
                                WSDLConstants.NON_HTTP_TRANSPORT_VALUE));
                    }

                }

                if (bindingProtocolElement == null) {
                    // expecting this to be http
                    bindingProtocolElement = TreeNodeBuilderUtil.evaluateXPathToElement(WSDLConstants.HTTP_BINDING_EXPR, bindingElement);
                    if (bindingProtocolElement != null) {
                        String endpoint = TreeNodeBuilderUtil.evaluateXPathToValue(WSDLConstants.HTTP_ENDPOINT_EXPR, portElement);
                        portNode.addChild(new TreeNode(WSDLConstants.HTTP_ENDPOINT, endpoint));
                        portNode.addChild(new TreeNode(WSDLConstants.HTTP_BINDING, WSDLConstants.HTTP_BINDING_TRUE));

                        String verb = bindingProtocolElement.getAttributeValue(new QName(WSDLConstants.BINDING_VERB_ATTRIBUTE));
                        portNode.addChild(new TreeNode(WSDLConstants.BINDING_VERB, verb));
                    }
                }
                // Here we don't create a special node for bindingNode, so bindingNode = portNode
                fillBinding(portNode, bindingElement);
            } else {
                // even if the binding element is not present, we probably can find the endpoint from the 'address'
                // element
                String endpoint = TreeNodeBuilderUtil.evaluateXPathToValue(WSDLConstants.SOAP_11_ENDPOINT_EXPR, portElement);
                if (endpoint != null) {
                    portNode.addChild(new TreeNode(WSDLConstants.SOAP_11_ENDPOINT, endpoint));
                    portNode.addChild(new TreeNode(WSDLConstants.SOAP_VERSION, WSDLConstants.SOAP_11));
                } else {
                    endpoint = TreeNodeBuilderUtil.evaluateXPathToValue(WSDLConstants.SOAP_12_ENDPOINT_EXPR, portElement);
                    if (endpoint != null) {
                        portNode.addChild(new TreeNode(WSDLConstants.SOAP_12_ENDPOINT, endpoint));
                        portNode.addChild(new TreeNode(WSDLConstants.SOAP_VERSION, WSDLConstants.SOAP_12));
                    }
                }
                if (endpoint == null) {
                    // then we can assume it is http
                    endpoint = TreeNodeBuilderUtil.evaluateXPathToValue(WSDLConstants.HTTP_ENDPOINT_EXPR, portElement);
                    if (endpoint != null) {
                        portNode.addChild(new TreeNode(WSDLConstants.HTTP_ENDPOINT, endpoint));
                        portNode.addChild(new TreeNode(WSDLConstants.HTTP_BINDING, WSDLConstants.HTTP_BINDING_TRUE));
                    }
                }
            }
            portNode.addChild(new TreeNode(WSDLConstants.BINDING, TreeNodeBuilderUtil.convertQNameToString(bindingLocalName,
                    bindingNamespace)));
        }
        return portTypesFound;
    }

    private void fillBinding(TreeNode bindingNode, OMElement bindingElement) throws Exception {
        // we will follow the port types
        String typeName = bindingElement.getAttributeValue(new QName(WSDLConstants.BINDING_PORT_TYPE));

        if (typeName != null) {
            String typeNamespace = TreeNodeBuilderUtil.getNamespaceURI(typeName, bindingElement);
            String typeLocalName = TreeNodeBuilderUtil.getLocalName(typeName);

            TreeNode typeNode = new TreeNode(TreeNodeBuilderUtil.generateKeyName(WSDLConstants.PORT_TYPE, TreeNodeBuilderUtil.convertQNameToString(typeLocalName,
                    typeNamespace)));
            bindingNode.addChild(typeNode);

            // so we cursing for the binding with that name.
            String typeExpression = "/wsdl:definitions/wsdl:portType[@name='" + typeLocalName +
                    "' and ancestor::*[@targetNamespace='" + typeNamespace + "']]";

            OMElement typeElement = TreeNodeBuilderUtil.evaluateXPathToElement(typeExpression, wsdlElement);
            if (typeElement != null) {
                fillPortType(typeNode, typeElement);
            }
        }
    }

    private void fillPortType(TreeNode typeNode, OMElement typeElement) throws Exception {
        TreeNode operationsNode = new TreeNode(WSDLConstants.OPERATIONS);
        typeNode.addChild(operationsNode);
        List<String> operations = TreeNodeBuilderUtil.evaluateXPathToValues(WSDLConstants.PORT_TYPE_OPERATIONS_EXPR, typeElement);
        for (String operation: operations) {
            operationsNode.addChild(operation);
        }
    }

    private static String getWSDLVersion(OMElement wsdlElement) throws Exception {
        QName wsdl11RootQName = new QName("http://schemas.xmlsoap.org/wsdl/", "definitions");
        QName wsdl2RootQName = new QName("http://www.w3.org/ns/wsdl", "description");
        String wsdlVersion;
        if (wsdl11RootQName.equals(wsdlElement.getQName())) {
            wsdlVersion = WSDLConstants.WSDL_VERSION_VALUE_11;
        } else if (wsdl2RootQName.equals(wsdlElement.getQName())) {
            wsdlVersion = WSDLConstants.WSDL_VERSION_VALUE_20;
        } else {
            wsdlVersion = WSDLConstants.WSDL_VERSION_VALUE_UNKNOWN;
        }
        return wsdlVersion;
    }

    public void setActualWsdlPath(String actualWsdlPath) {
        this.actualWsdlPath = actualWsdlPath;
    }
}
