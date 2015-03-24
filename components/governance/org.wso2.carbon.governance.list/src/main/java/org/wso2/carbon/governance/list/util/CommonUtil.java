/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.governance.list.util;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.jaxen.JaxenException;
import org.jaxen.SimpleNamespaceContext;
import org.wso2.carbon.governance.api.util.GovernanceArtifactConfiguration;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.list.operations.*;
import org.wso2.carbon.governance.list.operations.util.OperationsConstants;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.extensions.services.RXTStoragePathService;
import org.wso2.carbon.registry.extensions.services.RXTStoragePathServiceImpl;
import org.wso2.carbon.registry.extensions.utils.CommonConstants;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.component.xml.config.ManagementPermission;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommonUtil {

    private static final Log log = LogFactory.getLog(CommonUtil.class);

    private static RegistryService registryService;
    private static ConfigurationContext configurationContext;
    private static RXTStoragePathService rxtStoragePathService;

    public static void setRegistryService(RegistryService service) {
        registryService = service;
    }

    public static RegistryService getRegistryService() {
        return registryService;
    }

    public static ConfigurationContext getConfigurationContext() {
        return configurationContext;
    }

    public static void setConfigurationContext(ConfigurationContext configurationContext) {
        CommonUtil.configurationContext = configurationContext;
    }

    public static RXTStoragePathService getRxtStoragePathService() {
        return rxtStoragePathService;
    }

    public static void setRxtStoragePathService(RXTStoragePathService rxtStoragePathService) {
        CommonUtil.rxtStoragePathService = rxtStoragePathService;
    }

	public static String getServiceName(Resource resource) throws RegistryException {
        String serviceInfo = convertContentToString(resource);
        try {
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(serviceInfo));
            StAXOMBuilder builder = new StAXOMBuilder(reader);
            OMElement serviceInfoElement = builder.getDocumentElement();
            return getNameFromContent(serviceInfoElement);
        } catch (Exception e) {
            String msg = "Error in getting the service name. service path: " + resource.getPath() + ".";
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
    }

    public static String getServiceNamespace(Resource resource) throws RegistryException {
        String serviceInfo = convertContentToString(resource);
        try {
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(serviceInfo));
            StAXOMBuilder builder = new StAXOMBuilder(reader);
            OMElement serviceInfoElement = builder.getDocumentElement();
            return getNamespaceFromContent(serviceInfoElement);
        } catch (Exception e) {
            String msg = "Error in getting the service namespace. service path: " + resource.getPath() + ".";
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
    }

    public static String getLifeCycleName(Resource resource) {
        String lifeCycleName = "";
        if (resource.getProperties() != null) {
            if (resource.getProperty("registry.LC.name") != null) {
                lifeCycleName = resource.getProperty("registry.LC.name");
            }
        }
        return lifeCycleName;
    }

    public static String getLifeCycleState(Resource resource) {
        String lifeCycleState = "";
        if (resource.getProperties() != null) {
            if (!getLifeCycleName(resource).equals("")) {
                String LCStatePropertyName = "registry.lifecycle." + getLifeCycleName(resource) + ".state";
                if (resource.getProperty(LCStatePropertyName) != null) {
                    lifeCycleState = resource.getProperty(LCStatePropertyName);
                }
            }

        }
        return lifeCycleState;
    }

    public static String getResourceName(String path) {
        String[] temp = path.split("/");
        return temp[temp.length - 1];
    }

/*
     public static String getSchemaNamespace(String path,String defaultPrefix) {
        return getNamespace(path,"schemas",defaultPrefix);
     }
*/

/*
    public static String getWsdlNamespace(String path,String defaultPrefix) {
        return getNamespace(path,"wsdls",defaultPrefix);
    }
*/

/*
    private static String getNamespace(String path, String metadataType, String defaultPrefix) {
        String namespace = "";

        if (path.startsWith(defaultPrefix)) {
            namespace = path.substring(path.indexOf(metadataType) + metadataType.length() + 1, path.lastIndexOf("/"));
        } else {
            String tempPath = path.substring(0, path.lastIndexOf("/"));
            namespace = path.substring(path.indexOf(metadataType) + metadataType.length() + 1, tempPath.lastIndexOf("/"));
        }

        return namespace.replaceAll("/", ".");
    }
*/

    public static String getNamespaceFromContent(OMElement head) {
        OMElement overview = head.getFirstChildWithName(new
                QName("Overview"));
        if (overview != null) {
            return overview.getFirstChildWithName(new QName("Namespace")).getText();
        }
        return head.getFirstChildWithName(new
                QName(CommonConstants.SERVICE_ELEMENT_NAMESPACE, "overview")).
                getFirstChildWithName(new QName(CommonConstants.SERVICE_ELEMENT_NAMESPACE,
                        "namespace")).getText();
    }

    public static String getNameFromContent(OMElement head) {
        OMElement overview = head.getFirstChildWithName(new
                QName("Overview"));
        if (overview != null) {
            return overview.getFirstChildWithName(new QName("Name")).getText();
        }
        return head.getFirstChildWithName(new
                QName(CommonConstants.SERVICE_ELEMENT_NAMESPACE, "overview")).
                getFirstChildWithName(new QName(CommonConstants.SERVICE_ELEMENT_NAMESPACE,
                        "name")).getText();
    }

    public static OMElement buildServiceOMElement(Resource resource) throws RegistryException {
        String serviceInfo = convertContentToString(resource);
        try {
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(serviceInfo));
            StAXOMBuilder builder = new StAXOMBuilder(reader);
            return builder.getDocumentElement();
        } catch (Exception e) {
            log.error("Unable to build service OMElement", e);
        }
        return null;
    }

    private static String convertContentToString(Resource resource) throws RegistryException {
        if (resource.getContent() instanceof String) {
            return RegistryUtils.decodeBytes(((String) resource.getContent()).getBytes());
        } else if (resource.getContent() instanceof byte[]) {
            return RegistryUtils.decodeBytes((byte[]) resource.getContent());
        }
        return RegistryUtils.decodeBytes("".getBytes());
    }

    public static String getVersionFromContent(OMElement content) {
        try {
            AXIOMXPath xPath = new AXIOMXPath("//pre:version");
            SimpleNamespaceContext context = new SimpleNamespaceContext();
            context.addNamespace("pre", content.getNamespace().getNamespaceURI());
            xPath.setNamespaceContext(context);

            List versionElements = xPath.selectNodes(content);

            if (versionElements != null) {
                for (Object versionElement : versionElements) {
                    OMElement version = (OMElement) versionElement;
                    if (((OMElement) version.getParent()).getLocalName().equals("overview")) {
                        return version.getText();
                    }
                }
            }

        } catch (JaxenException e) {
            log.error("Unable to get the version of the service", e);
        }
        return "";
    }

    public static boolean validateXMLConfigOnSchema(String xml, String schema) throws RegistryException {
        String serviceConfPath = "";
        if ("rxt-ui-config".equalsIgnoreCase(schema)) {
            serviceConfPath = CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator +
                    "resources" + File.separator + "rxt.xsd";
        } else if ("lifecycle-config".equalsIgnoreCase(schema)) {
            serviceConfPath = CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator + "resources"
                    + File.separator + "lifecycle-config.xsd";
        }
        return validateRXTContent(xml, serviceConfPath);
    }

    private static boolean validateRXTContent(String rxtContent, String xsdPath) throws RegistryException {
        try {
            OMElement rxt = getRXTContentOMElement(rxtContent);
            AXIOMXPath xpath = new AXIOMXPath("//artifactType");
            OMElement c1 = (OMElement) xpath.selectSingleNode(rxt);
            InputStream is = new ByteArrayInputStream(c1.toString().getBytes());
            Source xmlFile = new StreamSource(is);
            SchemaFactory schemaFactory = SchemaFactory
                    .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(new File(xsdPath));
            Validator validator = schema.newValidator();
            validator.validate(xmlFile);
        } catch (Exception e) {
            log.error("RXT validation fails due to: " + e.getMessage());
            return false;
        }
        return true;
    }


    public static OMElement getRXTContentOMElement(String xml) throws RegistryException {

        XMLStreamReader parser;
        try {
            parser = XMLInputFactory.newInstance().createXMLStreamReader(new ByteArrayInputStream(xml.getBytes("utf-8")));
            StAXOMBuilder builder = new StAXOMBuilder(parser);
            return builder.getDocumentElement();
        } catch (Exception e) {
            throw new RegistryException(e.getMessage());
        }
    }

    public static void configureGovernanceArtifacts(Registry systemRegistry, AxisConfiguration axisConfig)
            throws RegistryException {
        List<GovernanceArtifactConfiguration> configurations =
                GovernanceUtils.findGovernanceArtifactConfigurations(systemRegistry);
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) systemRegistry, configurations);
        Registry governanceSystemRegistry = GovernanceUtils.getGovernanceSystemRegistry(systemRegistry);

        for (GovernanceArtifactConfiguration configuration : configurations) {
            for (ManagementPermission uiPermission : configuration.getUIPermissions()) {
                String resourceId = uiPermission.getResourceId();
                if (systemRegistry.resourceExists(resourceId)) {
                    continue;
                }
                Collection collection = systemRegistry.newCollection();
                collection.setProperty("name", uiPermission.getDisplayName());
                systemRegistry.put(resourceId, collection);
            }
            RXTMessageReceiver receiver = new RXTMessageReceiver();

            if (axisConfig != null) {
                try {

                    String singularLabel = configuration.getSingularLabel().replaceAll("\\s", "");
                    String key = configuration.getKey();
                    String mediaType = configuration.getMediaType();

                    //                    We avoid creation of a axis service if there is a service with the same name
                    if (axisConfig.getService(singularLabel) != null) {
                        continue;
                    }
                    AxisService service = new AxisService(singularLabel);

                    Parameter param1 = new Parameter("AuthorizationAction", "/permission/admin/login");
                    param1.setLocked(true);
                    service.addParameter(param1);

                    Parameter param2 = new Parameter("adminService", "true");
                    param2.setLocked(true);
                    service.addParameter(param2);

                    Parameter param3 = new Parameter("hiddenService", "true");
                    param3.setLocked(true);
                    service.addParameter(param3);

                    Parameter param4 = new Parameter("enableMTOM", "true");
                    param4.setLocked(true);
                    service.addParameter(param4);

                    XmlSchemaCollection schemaCol = new XmlSchemaCollection();
                    List<XmlSchema> schemaList = new ArrayList<XmlSchema>();

                    AbstractOperation create = new CreateOperation(new QName(OperationsConstants.ADD + singularLabel),
                            governanceSystemRegistry, mediaType,
                            OperationsConstants.NAMESPACE_PART1 +
                                    OperationsConstants.ADD + "." + key + OperationsConstants.NAMESPACE_PART2).
                            init(key, receiver);

                    Parameter authorizationActionCreate = new Parameter("AuthorizationAction",
                            "/permission/admin/manage/resources/govern/" + key + "/add");
                    authorizationActionCreate.setLocked(true);
                    create.addParameter(authorizationActionCreate);

                    service.addOperation(create);
                    schemaList.addAll(Arrays.asList(create.getSchemas(schemaCol)));

                    AbstractOperation read = new ReadOperation(new QName(OperationsConstants.GET + singularLabel),
                            governanceSystemRegistry, mediaType,
                            OperationsConstants.NAMESPACE_PART1 +
                                    OperationsConstants.GET + "." + key + OperationsConstants.NAMESPACE_PART2).
                            init(key, receiver);

                    Parameter authorizationActionRead = new Parameter("AuthorizationAction",
                            "/permission/admin/manage/resources/govern/" + key + "/list");
                    authorizationActionRead.setLocked(true);
                    read.addParameter(authorizationActionRead);

                    service.addOperation(read);
                    schemaList.addAll(Arrays.asList(read.getSchemas(schemaCol)));

                    AbstractOperation update = new UpdateOperation(new QName(OperationsConstants.UPDATE + singularLabel),
                            governanceSystemRegistry, mediaType,
                            OperationsConstants.NAMESPACE_PART1 +
                                    OperationsConstants.UPDATE + "." + key + OperationsConstants.NAMESPACE_PART2).
                            init(key, receiver);

                    Parameter authorizationActionUpdate = new Parameter("AuthorizationAction",
                            "/permission/admin/manage/resources/govern/" + key + "/add");
                    authorizationActionUpdate.setLocked(true);
                    update.addParameter(authorizationActionUpdate);

                    service.addOperation(update);
                    schemaList.addAll(Arrays.asList(update.getSchemas(schemaCol)));

                    AbstractOperation delete = new DeleteOperation(new QName(OperationsConstants.DELETE + singularLabel),
                            governanceSystemRegistry, mediaType,
                            OperationsConstants.NAMESPACE_PART1 +
                                    OperationsConstants.DELETE + "." + key + OperationsConstants.NAMESPACE_PART2).
                            init(key, receiver);

                    Parameter authorizationActionDelete = new Parameter("AuthorizationAction",
                            "/permission/admin/manage/resources/govern/" + key + "/add");
                    authorizationActionDelete.setLocked(true);
                    delete.addParameter(authorizationActionDelete);

                    service.addOperation(delete);
                    schemaList.addAll(Arrays.asList(delete.getSchemas(schemaCol)));

                    AbstractOperation getAllArtifactIds = new GetAllArtifactIDsOperation(
                            new QName(OperationsConstants.GET + singularLabel + OperationsConstants.ARTIFACT_IDS),
                            governanceSystemRegistry, mediaType,
                            OperationsConstants.NAMESPACE_PART1 +
                                    OperationsConstants.GET + "." + key + "." +
                                    OperationsConstants.ARTIFACT_IDS.toLowerCase() + OperationsConstants.NAMESPACE_PART2).
                            init(key, receiver);

                    Parameter authorizationActionGetArtifactIDs = new Parameter("AuthorizationAction",
                            "/permission/admin/manage/resources/govern/" + key + "/list");
                    authorizationActionGetArtifactIDs.setLocked(true);
                    getAllArtifactIds.addParameter(authorizationActionGetArtifactIDs);

                    service.addOperation(getAllArtifactIds);
                    schemaList.addAll(Arrays.asList(getAllArtifactIds.getSchemas(schemaCol)));

                    AbstractOperation getDependencies = new GetDependenciesOperation(
                            new QName(OperationsConstants.GET + singularLabel + OperationsConstants.DEPENDENCIES),
                            governanceSystemRegistry, mediaType,
                            OperationsConstants.NAMESPACE_PART1 +
                                    OperationsConstants.GET + "." + key + "." +
                                    OperationsConstants.DEPENDENCIES.toLowerCase() + OperationsConstants.NAMESPACE_PART2).
                            init(key, receiver);

                    Parameter authorizationActionGetDependencies = new Parameter("AuthorizationAction",
                            "/permission/admin/manage/resources/govern/" + key + "/list");
                    authorizationActionGetDependencies.setLocked(true);
                    getDependencies.addParameter(authorizationActionGetDependencies);

                    service.addOperation(getDependencies);
                    schemaList.addAll(Arrays.asList(getDependencies.getSchemas(schemaCol)));

                    List<String> transports = new ArrayList<String>();
                    transports.add(Constants.TRANSPORT_HTTPS);
                    service.setExposedTransports(transports);
                    axisConfig.addService(service);

                    XmlSchema schema = schemaCol.read(new StreamSource(
                            new ByteArrayInputStream(OperationsConstants.REGISTRY_EXCEPTION1_XSD.getBytes())), null);
                    schemaList.add(schema);

                    schema = schemaCol.read(new StreamSource(
                            new ByteArrayInputStream(OperationsConstants.GOVERNANCE_EXCEPTION_XSD.getBytes())), null);
                    schemaList.add(schema);

                    schema = schemaCol.read(new StreamSource(
                            new ByteArrayInputStream(OperationsConstants.REGISTRY_EXCEPTION2_XSD.getBytes())), null);
                    schemaList.add(schema);

                    service.addSchema(schemaList);

                } catch (AxisFault axisFault) {
                    String msg = "Error occured while adding services";
                    log.error(msg, axisFault);
                }
            }
        }
    }

    public static void addStoragePath(String mediaType, String storagePath) {
        RXTStoragePathServiceImpl service = (RXTStoragePathServiceImpl) getRxtStoragePathService();
        service.addStoragePath(mediaType, storagePath);
    }

    public static void removeStoragePath(String mediaType) {
        RXTStoragePathServiceImpl service = (RXTStoragePathServiceImpl) getRxtStoragePathService();
        service.removeStoragePath(mediaType);
    }
}