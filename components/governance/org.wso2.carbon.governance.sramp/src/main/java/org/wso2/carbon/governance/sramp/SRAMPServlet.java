/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.governance.sramp;

import org.apache.abdera.Abdera;
import org.apache.abdera.factory.Factory;
import org.apache.abdera.i18n.iri.IRI;
import org.apache.abdera.model.*;
import org.apache.abdera.model.Collection;
import org.apache.abdera.parser.stax.FOMEntry;
import org.apache.abdera.parser.stax.FOMFeed;
import org.apache.abdera.parser.stax.FOMService;
import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaxen.JaxenException;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.services.ServiceFilter;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.governance.sramp.exceptions.SRAMPServletException;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.core.utils.UUIDGenerator;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Servlet Exposing an REST/Atom S-RAMP API to the Registry, Repository, and Governance Framework
 * of the Carbon Platform. More information about S-RAMP can be found at
 * <a href="http://s-ramp.org/">s-ramp.org</a>.
 */
public class SRAMPServlet extends HttpServlet {

    private static final long serialVersionUID = 5371668294418554489L;

    ////////////////////////////////////////////////////////
    // Base Media Types
    ////////////////////////////////////////////////////////

    private static final String ATOM_MEDIA_TYPE = "application/atom+xml";
    private static final String XML_MEDIA_TYPE = "text/xml";
    private static final String APPLICATION_ATOMSVC_XML = "application/atomsvc+xml";

    ////////////////////////////////////////////////////////
    // Derived Media Types
    ////////////////////////////////////////////////////////

    private static final String ATOM_FEED_MEDIA_TYPE = ATOM_MEDIA_TYPE + "; type=feed";
    private static final String ATOM_ENTRY_MEDIA_TYPE = ATOM_MEDIA_TYPE + "; type=entry";
    private static final String UTF8_XML_MEDIA_TYPE = XML_MEDIA_TYPE + "; charset=UTF-8";

    ////////////////////////////////////////////////////////
    // HTTP Headers
    ////////////////////////////////////////////////////////

    private static final String LAST_MODIFIED_HEADER = "Last-Modified";
    private static final String E_TAG_HEADER = "ETag";
    private static final String CONTENT_DISPOSITION_HEADER = "Content-Disposition";

    ////////////////////////////////////////////////////////
    // S-RAMP SOA Model Artifact Classifications
    ////////////////////////////////////////////////////////

    private static final String SOA_MODEL_SERVICE_INTERFACE_CLASSIFICATION =
            "http://wso2.org/soaModel#ServiceInterface";
    private static final String SOA_MODEL_SERVICE_CLASSIFICATION =
            "http://wso2.org/soaModel#Service";

    ////////////////////////////////////////////////////////
    // S-RAMP Artifact Attributes
    ////////////////////////////////////////////////////////

    private static final String CONTENT_ENCODING_ATTRIBUTE = "contentEncoding";
    private static final String UUID_ATTRIBUTE = "uuid";
    private static final String VERSION_ATTRIBUTE = "version";
    private static final String NAME_ATTRIBUTE = "name";
    private static final String CREATED_BY_ATTRIBUTE = "createdBy";
    private static final String CREATED_TIMESTAMP_ATTRIBUTE = "createdTimestamp";
    private static final String LAST_MODIFIED_TIMESTAMP_ATTRIBUTE = "lastModifiedTimestamp";
    private static final String LAST_MODIFIED_BY_ATTRIBUTE = "lastModifiedBy";
    private static final String DESCRIPTION_ATTRIBUTE = "description";
    private static final String CONTENT_SIZE_ATTRIBUTE = "contentSize";

    ////////////////////////////////////////////////////////
    // REST Path Components
    ////////////////////////////////////////////////////////

    private static final String CONTENT_PATH_COMPONENT = "/content";
    private static final String WSDL_WSDL_DOCUMENT_PATH_COMPONENT = "/wsdl/WsdlDocument/";
    private static final String SOA_SERVICE_PATH_COMPONENT = "/soa/Service/";
    private static final String SOA_SERVICE_INTERFACE_PATH_COMPONENT = "/soa/ServiceInterface/";
    private static final String SERVICE_DOCUMENT_PATH_COMPONENT = "/servicedocument";

    ////////////////////////////////////////////////////////
    // Other Constants
    ////////////////////////////////////////////////////////

    private static final String ATTACHMENT_FILENAME = "attachment; filename=";
    private static final String URN_UUID = "urn:uuid:";
    private static final String UTF_8 = "UTF-8";
    private static final String DEFAULT_ARTIFACT_VERSION = "1.0";
    private static final String S_RAMP_SERVLET_CONTEXT = "/s-ramp";
    private static final QName HREF_Q_NAME =
            new QName("http://www.w3.org/1999/xlink", "href", "xlin");

    private static Log log = LogFactory.getLog(SRAMPServlet.class);

    private String servletURL;

    private transient Abdera abdera;
    private transient UserRegistry governanceSystemRegistry;

    /**
     * Default Constructor of the S-RAMP servlet.
     *
     * @param configurationContextService instance of the configuration context OSGi service.
     * @param registryService instance of the Registry OSGi service.
     *
     * @throws RegistryException if unable to obtain a registry instance.
     */
    public SRAMPServlet(ConfigurationContextService configurationContextService,
                        RegistryService registryService) throws RegistryException {
        abdera = new Abdera();
        governanceSystemRegistry = registryService.getGovernanceSystemRegistry();
        String serverURL =
                CarbonUtils.getServerURL(CarbonUtils.getServerConfiguration(),
                        configurationContextService.getServerConfigContext());
        servletURL =
                serverURL.substring(0, serverURL.lastIndexOf("/services")) + S_RAMP_SERVLET_CONTEXT;
    }

    ////////////////////////////////////////////////////////
    // Implementation of HTTP Method handlers
    ////////////////////////////////////////////////////////

    /**
     * {@inheritDoc}
     */
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String query = getXPathForQuery(req);
        if (query != null) {
            processQueryRequest(req, resp, query);
        } else {
            processGETRequest(req, resp);
        }
    }

    ////////////////////////////////////////////////////////
    // Processing of REST GET requests
    ////////////////////////////////////////////////////////

    protected void processGETRequest(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo.startsWith(WSDL_WSDL_DOCUMENT_PATH_COMPONENT)) {
            processGETWSDLDocumentRequest(req, resp,
                    pathInfo.substring(WSDL_WSDL_DOCUMENT_PATH_COMPONENT.length()));
        } else if (pathInfo.startsWith(SOA_SERVICE_PATH_COMPONENT)) {
            processGETServiceRequest(req, resp,
                    pathInfo.substring(SOA_SERVICE_PATH_COMPONENT.length()));
        } else if (pathInfo.startsWith(SOA_SERVICE_INTERFACE_PATH_COMPONENT)) {
            processGETServiceInterfaceRequest(req, resp,
                    pathInfo.substring(SOA_SERVICE_INTERFACE_PATH_COMPONENT.length()));
        } else if (pathInfo.startsWith(SERVICE_DOCUMENT_PATH_COMPONENT)) {
            processGETServiceDocumentRequest(req, resp);
        }
    }

    private void processGETWSDLDocumentRequest(HttpServletRequest req, HttpServletResponse resp,
                                               String pathInfo)
            throws ServletException, IOException {
        String[] pathComponents = pathInfo.split("/");
        if (pathComponents.length == 2 && pathComponents[1].equals(
                CONTENT_PATH_COMPONENT.substring(1))) {
            processGETWSDLContentRequest(resp, pathComponents[0]);
        } else if (pathComponents.length == 1) {
            processGETWSDLArtifactRequest(req, resp, pathComponents[0]);
        }
    }

    private void processGETWSDLArtifactRequest(HttpServletRequest req, HttpServletResponse resp,
                                               String pathComponent)
            throws ServletException, IOException {
        Entry entry = abdera.newEntry();
        Element artifact = newSRAMPArtifact();
        Element wsdlDocument = newExtensionElement(artifact, "WsdlDocument");

        wsdlDocument.setAttributeValue(CONTENT_ENCODING_ATTRIBUTE, UTF_8);
        WsdlManager manager = new WsdlManager(governanceSystemRegistry);
        try {
            Wsdl wsdl = manager.getWsdl(pathComponent);
            String wsdlId = wsdl.getId();
            entry.setId(URN_UUID + wsdlId);
            wsdlDocument.setAttributeValue(UUID_ATTRIBUTE, wsdlId);
            wsdlDocument.setAttributeValue(VERSION_ATTRIBUTE, DEFAULT_ARTIFACT_VERSION);
            String path = wsdl.getPath();
            String resourceName = RegistryUtils.getResourceName(path);
            entry.setTitle(resourceName);
            wsdlDocument.setAttributeValue(NAME_ATTRIBUTE, resourceName);
            Resource resource = getResource(path);
            if (resource != null) {
                fillArtifactEntryDetailsFromResource(wsdlDocument, entry, resource);
                fillArtifactEntryDescriptionFromResource(wsdlDocument, entry, resource);
                fillPropertiesFromResource(wsdlDocument, resource);
                fillArtifactEntryContentDetailsFromResource(wsdlDocument, resource);
            }
            String serviceURL = getServletURL(req) + WSDL_WSDL_DOCUMENT_PATH_COMPONENT + wsdlId;
            entry.setContent(new IRI(serviceURL + CONTENT_PATH_COMPONENT), XML_MEDIA_TYPE);
            addAtomLinksForGETRequest(entry, serviceURL);
            entry.addExtension(artifact);
        } catch (GovernanceException e) {
            String message = "Unable to locate WSDL";
            log.error(message, e);
            throw new SRAMPServletException(message, e);
        }
        resp.setContentType(ATOM_ENTRY_MEDIA_TYPE);
        resp.setStatus(200);
        serializeOutput(resp, (FOMEntry) entry);
    }

    private void processGETWSDLContentRequest(HttpServletResponse resp, String pathComponent)
            throws ServletException, IOException {
        WsdlManager manager = new WsdlManager(governanceSystemRegistry);
        try {
            Wsdl wsdl = manager.getWsdl(pathComponent);
            if (wsdl == null || wsdl.getWsdlElement() == null) {
                String message = "Unable to retrieve WSDL content for the given id: " +
                        pathComponent;
                log.error(message);
                throw new SRAMPServletException(message);
            }
            Resource resource;
            String path = wsdl.getPath();
            try {
                resource = governanceSystemRegistry.get(path);
            } catch (Exception e) {
                resource = null;
            }
            if (resource != null) {
                String errorMessage = "Unable to read content of the WSDL element";
                try {
                    resp.setDateHeader(LAST_MODIFIED_HEADER, resource.getLastModified().getTime());
                    Object temp = resource.getContent();
                    String content;
                    if (temp instanceof String) {
                        content = (String) temp;
                    } else if (temp instanceof byte[]) {
                        content = RegistryUtils.decodeBytes((byte[]) temp);
                    } else {
                        log.error(errorMessage);
                        throw new SRAMPServletException(errorMessage);
                    }
                    resp.setHeader(E_TAG_HEADER, DigestUtils.md5Hex(content));
                } catch (RegistryException e) {
                    log.error(errorMessage, e);
                    throw new SRAMPServletException(errorMessage, e);
                }
                resp.setHeader(CONTENT_DISPOSITION_HEADER, ATTACHMENT_FILENAME +
                        RegistryUtils.getResourceName(path));
            }
            resp.setContentType(UTF8_XML_MEDIA_TYPE);
            resp.setStatus(200);
            OMElement wsdlElement = wsdl.getWsdlElement();
            OMDocument document = wsdlElement.getOMFactory().createOMDocument();
            document.addChild(wsdlElement);
            document.build();
            try {
                document.serialize(resp.getOutputStream());
            } catch (XMLStreamException e) {
                String message = "Unable to serialize WSDL content";
                log.error(message, e);
                throw new SRAMPServletException(message, e);
            }
        } catch (GovernanceException e) {
            String message = "Unable to locate WSDL";
            log.error(message, e);
            throw new SRAMPServletException(message, e);
        }
    }

    private void processGETServiceRequest(HttpServletRequest req, HttpServletResponse resp,
                                          String pathComponent)
            throws ServletException, IOException {
        Entry entry = abdera.newEntry();
        Element artifact = newSRAMPArtifact();
        Element serviceElement = newExtensionElement(artifact, "Service");

        serviceElement.setAttributeValue(CONTENT_ENCODING_ATTRIBUTE, UTF_8);
        try {
            ServiceManager manager = new ServiceManager(governanceSystemRegistry);
            Service service = manager.getService(pathComponent);
            String serviceId = service.getId();
            entry.setId(URN_UUID + serviceId);
            serviceElement.setAttributeValue(UUID_ATTRIBUTE, serviceId);
            serviceElement.setAttributeValue(VERSION_ATTRIBUTE, DEFAULT_ARTIFACT_VERSION);
            String path = service.getPath();
            String serviceName = service.getQName().getLocalPart();
            entry.setTitle(serviceName);
            serviceElement.setAttributeValue(NAME_ATTRIBUTE, serviceName);
            Resource resource = getResource(path);
            if (resource != null) {
                fillArtifactEntryDetailsFromResource(serviceElement, entry, resource);
                fillArtifactEntryDescriptionFromResource(serviceElement, entry, resource);
                fillPropertiesFromResource(serviceElement, resource);
            }
            String serviceURL = getServletURL(req) + SOA_SERVICE_PATH_COMPONENT + serviceId;
            entry.setContent("");
            addAtomLinksForGETRequest(entry, serviceURL);
            newExtensionElement(serviceElement, "classifiedBy").setText(
                    SOA_MODEL_SERVICE_CLASSIFICATION);
            Wsdl[] attachedWsdls = service.getAttachedWsdls();
            if (attachedWsdls.length > 0) {
                StringBuffer buffer = new StringBuffer(serviceId);
                for (Wsdl wsdl : attachedWsdls) {
                    buffer.append(wsdl.getId());
                }
                String serviceInterfaceId = uuidForString(buffer.toString());
                Element relationship = newExtensionElement(serviceElement, "relationship");
                newExtensionElement(relationship, "relationshipType").setText("hasInterface");
                Element relationshipTarget = newExtensionElement(relationship,
                        "relationshipTarget");
                relationshipTarget.setText(serviceInterfaceId);
                relationshipTarget.setAttributeValue(HREF_Q_NAME,
                        getServletURL(req) + SOA_SERVICE_INTERFACE_PATH_COMPONENT +
                                serviceInterfaceId);
            }
            entry.addExtension(artifact);
        } catch (RegistryException e) {
            String message = "Unable to locate Service";
            log.error(message, e);
            throw new SRAMPServletException(message, e);
        }
        resp.setContentType(ATOM_ENTRY_MEDIA_TYPE);
        resp.setStatus(200);
        serializeOutput(resp, (FOMEntry) entry);
    }

    private void processGETServiceInterfaceRequest(HttpServletRequest req,
                                                   HttpServletResponse resp, String pathComponent)
            throws ServletException, IOException {
        Entry entry = abdera.newEntry();
        Element artifact = newSRAMPArtifact();
        Element serviceInterface = newExtensionElement(artifact, "ServiceInterface");

        serviceInterface.setAttributeValue(CONTENT_ENCODING_ATTRIBUTE, UTF_8);
        try {
            ServiceManager manager = new ServiceManager(governanceSystemRegistry);
            Service[] services = findServiceForInterfaceId(pathComponent, manager);
            if (services.length != 1) {
                String message = "Unable to locate Service for Given Interface";
                log.error(message);
                throw new SRAMPServletException(message);
            }
            Service service = services[0];
            entry.setId(URN_UUID + pathComponent);
            serviceInterface.setAttributeValue(UUID_ATTRIBUTE, pathComponent);
            serviceInterface.setAttributeValue(VERSION_ATTRIBUTE, DEFAULT_ARTIFACT_VERSION);
            String path = service.getPath();
            String serviceName = service.getQName().getLocalPart();
            String serviceInterfaceName = serviceName + " SI";
            entry.setTitle(serviceInterfaceName);
            serviceInterface.setAttributeValue(NAME_ATTRIBUTE, serviceInterfaceName);
            Resource resource = getResource(path);
            if (resource != null) {
                fillArtifactEntryDetailsFromResource(serviceInterface, entry, resource);
            }
            String summary = serviceName + " service interface";
            serviceInterface.setAttributeValue(DESCRIPTION_ATTRIBUTE, summary);
            entry.setSummary(summary);
            entry.setContent("");
            addAtomLinksForGETRequest(entry,
                    getServletURL(req) + SOA_SERVICE_INTERFACE_PATH_COMPONENT +
                            pathComponent);
            newExtensionElement(serviceInterface, "classifiedBy").setText(
                    SOA_MODEL_SERVICE_INTERFACE_CLASSIFICATION);
            Wsdl[] attachedWsdls = service.getAttachedWsdls();
            if (attachedWsdls.length > 0) {
                for (Wsdl wsdl : attachedWsdls) {
                    Element relationship = newExtensionElement(serviceInterface, "relationship");
                    newExtensionElement(relationship, "relationshipType").setText("documentation");
                    Element relationshipTarget = newExtensionElement(relationship,
                            "relationshipTarget");
                    String wsdlId = wsdl.getId();
                    relationshipTarget.setText(wsdlId);
                    relationshipTarget.setAttributeValue(HREF_Q_NAME,
                            getServletURL(req) + WSDL_WSDL_DOCUMENT_PATH_COMPONENT + wsdlId);
                }
            }
            entry.addExtension(artifact);
        } catch (RegistryException e) {
            String message = "Unable to locate Service Interface";
            log.error(message, e);
            throw new SRAMPServletException(message, e);
        }
        resp.setContentType(ATOM_ENTRY_MEDIA_TYPE);
        resp.setStatus(200);
        serializeOutput(resp, (FOMEntry) entry);
    }

    private void processGETServiceDocumentRequest(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        org.apache.abdera.model.Service service = abdera.newService();
        Factory factory = abdera.getFactory();
        String servletURL = getServletURL(req);

        service.addWorkspace(buildCoreModel(factory, servletURL));
        service.addWorkspace(buildSOAPWSDLModel(factory, servletURL));
        service.addWorkspace(buildQueryModel(factory, servletURL));
        service.addWorkspace(buildXSDModel(factory, servletURL));
        service.addWorkspace(buildSOAModel(factory, servletURL));
        service.addWorkspace(buildPolicyModel(factory, servletURL));
        service.addWorkspace(buildWSDLModel(factory, servletURL));
        service.addWorkspace(buildServiceImplementation(factory, servletURL));

        resp.setContentType(APPLICATION_ATOMSVC_XML);
        resp.setStatus(200);
        serializeOutput(resp, (FOMService) service);
    }

    ////////////////////////////////////////////////////////
    // Utility Methods for building Atom Service Document
    ////////////////////////////////////////////////////////

    private Collection buildParentCollection(Factory factory, String servletURL,
                                             Collection[] children,
                                             String href, String title) {
        Collection parent = factory.newCollection().setHref(servletURL + href);
        parent.setTitle(title);
        List<Category> categories = new LinkedList<Category>();
        for (Collection child : children) {
            categories.add(child.getCategories().get(0).getCategoriesWithScheme().get(0));
        }
        parent.setAcceptsNothing().addCategories(categories, true, null);
        return parent;
    }

    private Collection buildChildCollection(Factory factory, String servletURL, String href,
                                            String title, String accept, String label,
                                            String term) {
        Collection collection = factory.newCollection().setHref(servletURL + href);
        collection.setTitle(title);
        if (accept != null) {
            collection.setAccept(accept);
        } else {
            collection.setAcceptsNothing();
        }
        collection.addCategories(factory.newCategories().setFixed(true).addCategory(
                factory.newCategory().setScheme("urn:x-s-ramp:2010:type").setTerm(term).setLabel(
                        label)));
        return collection;
    }

    private Workspace buildCoreModel(Factory factory, String servletURL) {
        Workspace coreModel = factory.newWorkspace();
        coreModel.setTitle("Core Model");
        Collection[] children =
                {
                        buildChildCollection(factory, servletURL, "/core/XmlDocument",
                                "XML Documents",
                                "application/xml", "XML Document", "XmlDocument"),
                        buildChildCollection(factory, servletURL, "/core/document",
                                "Documents",
                                "application/octet-stream", "Document", "Document")
                };
        for (Collection child : children) {
            coreModel.addCollection(child);
        }
        coreModel.addCollection(
                buildParentCollection(factory, servletURL, children, "/core",
                        "Core Model Objects"));
        return coreModel;
    }

    private Workspace buildSOAPWSDLModel(Factory factory, String servletURL) {
        Workspace soapWsdlModel = factory.newWorkspace();
        soapWsdlModel.setTitle("SOAP WSDL Model");
        Collection[] children =
                {
                        buildChildCollection(factory, servletURL, "/soapWsdl/SoapBinding",
                                "SOAP Bindings", null, "SOAP Binding", "SoapBinding"),
                        buildChildCollection(factory, servletURL, "/soapWsdl/SoapAddress",
                                "SOAP Addresses", null, "SOAP Address", "SoapAddress")
                };
        for (Collection child : children) {
            soapWsdlModel.addCollection(child);
        }
        soapWsdlModel.addCollection(
                buildParentCollection(factory, servletURL, children, "/soapWsdl",
                        "SOAP WSDL Model Objects"));
        return soapWsdlModel;
    }

    private Workspace buildServiceImplementation(Factory factory, String servletURL) {
        Workspace serviceImplementation = factory.newWorkspace();
        serviceImplementation.setTitle("Service Implementation");
        Collection[] children =
                {
                        buildChildCollection(factory, servletURL,
                                "/serviceImplementation/ServiceOperation", "Service Operations",
                                "application/atom+xml; type=entry", "Service Operation",
                                "ServiceOperation"),
                        buildChildCollection(factory, servletURL,
                                "/serviceImplementation/Organization",
                                "Organizations", "application/atom+xml; type=entry", "Organization",
                                "Organization"),
                        buildChildCollection(factory, servletURL,
                                "/serviceImplementation/ServiceInstance",
                                "Service Instances", "application/atom+xml; type=entry",
                                "Service Instance", "ServiceInstance"),
                        buildChildCollection(factory, servletURL,
                                "/serviceImplementation/ServiceEndpoint",
                                "Service Endpoints", "application/atom+xml; type=entry",
                                "Service Endpoint", "ServiceEndpoint")
                };
        for (Collection child : children) {
            serviceImplementation.addCollection(child);
        }
        serviceImplementation.addCollection(
                buildParentCollection(factory, servletURL, children, "/serviceImplementation",
                        "Service Implementation Objects"));
        return serviceImplementation;
    }

    private Workspace buildXSDModel(Factory factory, String servletURL) {
        Workspace xsdModel = factory.newWorkspace();
        xsdModel.setTitle("XSD Model");
        Collection[] children =
                {
                        buildChildCollection(factory, servletURL,
                                "/xsd/XsdType", "XSD Types", null, "XSD Type", "XsdType"),
                        buildChildCollection(factory, servletURL,
                                "/xsd/ElementDeclaration", "Element Declarations", null,
                                "Element Declaration", "ElementDeclaration"),
                        buildChildCollection(factory, servletURL,
                                "/xsd/AttributeDeclaration", "Attribute Declarations", null,
                                "Attribute Declaration", "AttributeDeclaration"),
                        buildChildCollection(factory, servletURL,
                                "/xsd/ComplexTypeDeclaration", "Complex Type Declarations",
                                null, "Complex Type Declaration", "ComplexTypeDeclaration"),
                        buildChildCollection(factory, servletURL,
                                "/xsd/SimpleTypeDeclaration", "Simple Type Declarations",
                                null, "Simple Type Declaration", "SimpleTypeDeclaration"),
                        buildChildCollection(factory, servletURL,
                                "/xsd/XsdDocument", "XSD Documents", "application/xml",
                                "XSD Document", "XsdDocument")
                };
        for (Collection child : children) {
            xsdModel.addCollection(child);
        }
        xsdModel.addCollection(
                buildParentCollection(factory, servletURL, children, "/xsd",
                        "XSD Model Objects"));
        return xsdModel;
    }

    private Workspace buildSOAModel(Factory factory, String servletURL) {
        Workspace soaModel = factory.newWorkspace();
        soaModel.setTitle("SOA Model");
        String[] types = {"Service Contract", "Orchestration Process", "Choreography Process",
                "Service Interface", "Collaboration Process", "Process", "Actor", "Collaboration",
                "Composition", "Element", "Event", "Orchestration", "Policy Subject", "Effect",
                "Information Type", "Task", "System", "Service", "Policy", "Choreography"};
        List<Collection> children = new LinkedList<Collection>();
        for (String type1 : types) {
            String type2 = type1.replace(" ", "");
            Collection child = buildChildCollection(factory, servletURL, "/soa/" + type2, type1,
                    "application/atom+xml; type=entry", type2, type1);
            soaModel.addCollection(child);
            children.add(child);
        }
        soaModel.addCollection(
                buildParentCollection(factory, servletURL,
                        children.toArray(new Collection[children.size()]), "/soa",
                        "SOA Model Objects"));

        return soaModel;
    }

    private Workspace buildWSDLModel(Factory factory, String servletURL) {
        Workspace wsdlModel = factory.newWorkspace();
        wsdlModel.setTitle("WSDL Model");
        Collection[] children =
                {
                        buildChildCollection(factory, servletURL,
                                "/wsdl/BindingOperationOutput", "Binding Operation Outputs", null,
                                "Binding Operation Output", "BindingOperationOutput"),
                        buildChildCollection(factory, servletURL,
                                "/wsdl/BindingOperationInput", "Binding Operation Inputs", null,
                                "Binding Operation Input", "BindingOperationInput"),
                        buildChildCollection(factory, servletURL,
                                "/wsdl/BindingOperationFault", "Binding Operation Faults", null,
                                "Binding Operation Fault", "BindingOperationFault"),
                        buildChildCollection(factory, servletURL,
                                "/wsdl/BindingOperation", "Binding Operations", null,
                                "Binding Operation", "BindingOperation"),
                        buildChildCollection(factory, servletURL,
                                "/wsdl/Binding", "Bindings", null,
                                "Binding", "Binding"),
                        buildChildCollection(factory, servletURL,
                                "/wsdl/Operation", "Operations", null,
                                "Operation", "Operation"),
                        buildChildCollection(factory, servletURL,
                                "/wsdl/OperationOutput", "Operation Outputs", null,
                                "Operation Output", "OperationOutput"),
                        buildChildCollection(factory, servletURL,
                                "/wsdl/OperationInput", "Operation Inputs", null,
                                "Operation Input", "OperationInput"),
                        buildChildCollection(factory, servletURL,
                                "/wsdl/Part", "Parts", null,
                                "Part", "Part"),
                        buildChildCollection(factory, servletURL,
                                "/wsdl/Message", "Messages", null,
                                "Message", "Message"),
                        buildChildCollection(factory, servletURL,
                                "/wsdl/Port", "Ports", null,
                                "Port", "Port"),
                        buildChildCollection(factory, servletURL,
                                "/wsdl/Fault", "Faults", null,
                                "Fault", "Fault"),
                        buildChildCollection(factory, servletURL,
                                "/wsdl/PortType", "Port Types", null,
                                "Port Type", "PortType"),
                        buildChildCollection(factory, servletURL,
                                "/wsdl/WsdlService", "WSDL Services",
                                "application/atom+xml; type=entry",
                                "WSDL Service", "WsdlService"),
                        buildChildCollection(factory, servletURL,
                                "/wsdl/WsdlExtension", "WSDL Extensions", null,
                                "WSDL Extension", "WsdlExtension"),
                        buildChildCollection(factory, servletURL,
                                "/wsdl/WsdlDocument", "WSDL Documents", "application/xml",
                                "WSDL Document", "WsdlDocument")
                };
        for (Collection child : children) {
            wsdlModel.addCollection(child);
        }
        wsdlModel.addCollection(
                buildParentCollection(factory, servletURL, children, "/wsdl",
                        "WSDL Model Objects"));
        return wsdlModel;
    }

    private Workspace buildPolicyModel(Factory factory, String servletURL) {
        Workspace policyModel = factory.newWorkspace();
        policyModel.setTitle("Policy Model");
        Collection[] children =
                {
                        buildChildCollection(factory, servletURL,
                                "/policy/PolicyExpression", "Policy Expressions", null,
                                "Policy Expressions", "PolicyExpression"),
                        buildChildCollection(factory, servletURL,
                                "/policy/PolicyAttachment", "Policy Attachments", null,
                                "Policy Attachment", "PolicyAttachment"),
                        buildChildCollection(factory, servletURL,
                                "/policy/PolicyDocument", "Policy Documents", "application/xml",
                                "Policy Document", "PolicyDocument")
                };
        for (Collection child : children) {
            policyModel.addCollection(child);
        }
        policyModel.addCollection(
                buildParentCollection(factory, servletURL, children, "/policy",
                        "Policy Model Objects"));
        return policyModel;
    }

    private Workspace buildQueryModel(Factory factory, String servletURL) {
        Workspace queryModel = factory.newWorkspace();
        queryModel.setTitle("Query Model");

        queryModel.addCollection(
                buildChildCollection(factory, servletURL, "/query", "Query Model Objects",
                        "application/atom+xml; type=entry", "Query", "query"));
        return queryModel;
    }

    ////////////////////////////////////////////////////////
    // Utility Methods for processing of REST GET requests
    ////////////////////////////////////////////////////////

    private void fillArtifactEntryContentDetailsFromResource(Element extensionElement,
                                                             Resource resource)
            throws ServletException {
        String errorMessage = "Unable to read content of the artifact";
        try {
            Object temp = resource.getContent();
            byte[] content;
            if (temp instanceof String) {
                content = RegistryUtils.encodeString((String) temp);
            } else if (temp instanceof byte[]) {
                content = (byte[]) temp;
            } else {
                log.error(errorMessage);
                throw new SRAMPServletException(errorMessage);
            }
            extensionElement.setAttributeValue(CONTENT_SIZE_ATTRIBUTE,
                    Integer.toString(content.length));
        } catch (RegistryException e) {
            log.error(errorMessage, e);
            throw new SRAMPServletException(errorMessage, e);
        }
    }

    private void fillArtifactEntryDetailsFromResource(Element extensionElement, Entry entry,
                                                      Resource resource) {
        Date lastModified = resource.getLastModified();
        entry.setUpdated(lastModified);
        extensionElement.setAttributeValue(CREATED_BY_ATTRIBUTE, resource.getAuthorUserName());
        extensionElement.setAttributeValue(CREATED_TIMESTAMP_ATTRIBUTE,
                formatDate(resource.getCreatedTime()));
        extensionElement.setAttributeValue(LAST_MODIFIED_TIMESTAMP_ATTRIBUTE,
                formatDate(lastModified));
        extensionElement.setAttributeValue(LAST_MODIFIED_BY_ATTRIBUTE,
                resource.getLastUpdaterUserName());
        entry.addAuthor(resource.getAuthorUserName());
    }

    private void fillArtifactEntryDescriptionFromResource(Element extensionElement,
                                                          Entry entry, Resource resource) {
        String description = resource.getDescription();
        if (description == null) {
            description = "";
        }
        entry.setSummary(description);
        extensionElement.setAttributeValue(DESCRIPTION_ATTRIBUTE, description);
    }


    private void fillPropertiesFromResource(Element artifactElement, Resource resource) {
        Properties properties = resource.getProperties();
        if (properties != null) {
            Set keySet = properties.keySet();
            if (keySet != null) {
                for (Object keyObj : keySet) {
                    String key = (String) keyObj;
//                    if (key.equals(GovernanceConstants.ARTIFACT_ID_PROP_KEY) ||
//                            RegistryUtils.isHiddenProperty(key)) {
                        // it is not a property.
//                        continue;
//                    }
                    List values = (List) properties.get(key);
                    if (values != null) {
                        for (Object valueObj : values) {
                            String value = (String) valueObj;
                            Element property = newExtensionElement(artifactElement, "property");
                            newExtensionElement(property, "propertyName").setText(key);
                            newExtensionElement(property, "propertyValue").setText(value);
                        }
                    }
                }
            }
        }
    }

    private Element newExtensionElement(Element parent, String name) {
        return abdera.getFactory().newElement(new QName("http://s-ramp.org/xmlns/2010/s-ramp",
                name, "s-ramp"), parent);
    }

    private Element newSRAMPArtifact() {
        return newExtensionElement(null, "artifact");
    }

    ////////////////////////////////////////////////////////
    // Processing of query requests
    ////////////////////////////////////////////////////////

    protected void processQueryRequest(HttpServletRequest req, HttpServletResponse resp,
                                       String query)
            throws ServletException, IOException {
        Feed feed = abdera.newFeed();
        addFeedDetailsForQuery(feed);
        if (query.indexOf("//Service") >= 0) {
            processQueryServiceRequest(req, feed, query);
            resp.setContentType(ATOM_MEDIA_TYPE);
            resp.setStatus(200);
            serializeOutput(resp, (FOMFeed) feed);
        }
    }

    private void processQueryServiceRequest(HttpServletRequest req, Feed feed, String query)
            throws ServletException {
        Service[] services = getServicesListForQuery(query);

        for (Service service : services) {
            Entry entry = feed.addEntry();
            entry.setId(URN_UUID + service.getId());
            entry.setTitle(service.getQName().getLocalPart());
            try {
                Resource resource = getResource(service.getPath());
                if (resource != null) {
                    entry.setUpdated(resource.getLastModified());
                    entry.setSummary(resource.getDescription());
                    entry.addAuthor(resource.getAuthorUserName());
                }
            } catch (GovernanceException e) {
                log.warn("An error occurred while processing service details", e);
            }
            entry.setContent("");

            String serviceURL = getServletURL(req) + SOA_SERVICE_PATH_COMPONENT + service.getId();
            addAtomLinksForGETRequest(entry, serviceURL);
        }
    }

    ////////////////////////////////////////////////////////
    // Utility Methods for processing of query requests
    ////////////////////////////////////////////////////////

    private Service[] getServicesListForQuery(String query) throws ServletException {
        final String name;
        if (query.indexOf("@name") > 0) {
            // request for service by name.
            name = query.substring(query.indexOf("'") + 1, query.lastIndexOf("'"));
        } else {
            name = null;
        }

        Service[] services;
        try {
            ServiceManager manager = new ServiceManager(governanceSystemRegistry);
            if (name == null) {
                services = manager.getAllServices();
            } else {
                services = manager.findServices(new ServiceFilter() {
                    public boolean matches(Service service) throws GovernanceException {
                        return name.equals(service.getQName().getLocalPart());
                    }
                });
            }
        } catch (RegistryException e) {
            String message = "Unable to locate services";
            log.warn(message, e);
            return new Service[0];
        }
        return services;
    }

    private String getXPathForQuery(HttpServletRequest req) throws ServletException {
        String query = req.getParameter("query");
        if (query != null) {
            try {
                new AXIOMXPath(query);
            } catch (JaxenException e) {
                String message = "Invalid XPath query: " + query;
                log.error(message, e);
                throw new SRAMPServletException(message, e);
            }
        }
        return query;
    }

    private void addFeedDetailsForQuery(Feed feed) {
        String uuid = UUIDGenerator.generateUUID();
        feed.setId(uuid);
        feed.setTitle("Query Response");
        feed.setUpdated(new Date());
        feed.addLink(uuid, "self");
    }

    ////////////////////////////////////////////////////////
    // Common Utility Methods for processing of GET requests
    ////////////////////////////////////////////////////////

    private void addAtomLinksForGETRequest(Entry entry, String serviceURL) {
        entry.addLink(serviceURL, "self", ATOM_ENTRY_MEDIA_TYPE, null, null, -1);
        entry.addLink(serviceURL, "edit", ATOM_ENTRY_MEDIA_TYPE, null, null, -1);
        entry.addLink(serviceURL + "/properties", "urn:x-s-ramp:2010:properties",
                ATOM_FEED_MEDIA_TYPE, null, null, -1);
        entry.addLink(serviceURL + "/relationships", "urn:x-s-ramp:2010:relationships",
                ATOM_FEED_MEDIA_TYPE, null, null, -1);
        entry.addLink(serviceURL + "/relationshipTypes", "urn:x-s-ramp:2010:relationshipTypes",
                ATOM_FEED_MEDIA_TYPE, null, null, -1);
        entry.addLink(serviceURL + "/classifications", "urn:x-s-ramp:2010:classifications",
                ATOM_FEED_MEDIA_TYPE, null, null, -1);
    }

    ////////////////////////////////////////////////////////
    // Generic Utility Methods for processing requests
    ////////////////////////////////////////////////////////

    private Service[] findServiceForInterfaceId(final String uuid, ServiceManager manager)
            throws GovernanceException {
        return manager.findServices(new ServiceFilter() {
            public boolean matches(Service service) throws GovernanceException {
                Wsdl[] attachedWsdls = service.getAttachedWsdls();
                if (attachedWsdls.length > 0) {
                    StringBuffer buffer = new StringBuffer(service.getId());
                    for (Wsdl wsdl : attachedWsdls) {
                        buffer.append(wsdl.getId());
                    }
                    return uuid.equals(uuidForString(buffer.toString()));
                }
                return false;
            }
        });
    }

    private String uuidForString(String string) {
        String md5 = DigestUtils.md5Hex(string);
        return md5.substring(0, 8) + "-" + md5.substring(8, 12) + "-" + md5.substring(12, 16) +
                "-" + md5.substring(16, 20) + "-" + md5.substring(20, 32);
    }

    private Resource getResource(String path) {
        Resource resource;
        try {
            resource = governanceSystemRegistry.get(path);
        } catch (Exception e) {
            resource = null;
        }
        return resource;
    }

    private String getServletURL(HttpServletRequest req) {
        try {
            StringBuffer requestURL = req.getRequestURL();
            return requestURL.substring(0, requestURL.indexOf(S_RAMP_SERVLET_CONTEXT) +
                    S_RAMP_SERVLET_CONTEXT.length());
        } catch (Exception ignore) {
            // if there are any issues in obtaining the servlet URL, simply return the default HTTPS
            // URL.
            return servletURL;
        }
    }

    private String formatDate(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        return formatter.format(date);
    }

    private void serializeOutput(HttpServletResponse resp, OMElement element)
            throws IOException, ServletException {
        OMDocument document = element.getOMFactory().createOMDocument();
        document.addChild(element);
        document.build();
        try {
            document.serialize(resp.getOutputStream());
        } catch (XMLStreamException e) {
            String message = "Unable to serialize Atom Feed";
            log.error(message, e);
            throw new SRAMPServletException(message, e);
        }
    }
}
