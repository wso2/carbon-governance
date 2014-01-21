/*
 * Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.governance.registry.extensions.handlers.utils;

import com.ibm.wsdl.util.xml.DOM2Writer;
import com.ibm.wsdl.xml.WSDLReaderImpl;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.registry.extensions.internal.GovernanceRegistryExtensionsComponent;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.session.CurrentSession;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.extensions.handlers.utils.EndpointUtils;
import org.wso2.carbon.registry.extensions.handlers.utils.ExWSDLReaderImpl;
import org.wso2.carbon.registry.extensions.handlers.utils.WSDLInfo;
import org.wso2.carbon.registry.extensions.handlers.utils.WSDLUtils;
import org.wso2.carbon.registry.extensions.utils.CommonConstants;
import org.wso2.carbon.registry.extensions.utils.CommonUtil;
import org.wso2.carbon.registry.extensions.utils.WSDLUtil;
import org.wso2.carbon.registry.extensions.utils.WSDLValidationInfo;
import org.xml.sax.InputSource;

import javax.wsdl.*;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.wsdl.xml.WSDLWriter;
import javax.xml.namespace.QName;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.*;

public class WsdlUriProcessor {
    private Registry registry;
    private Registry systemRegistry;
    private Registry governanceUserRegistry;
    private Definition originalDefinition;
    private List<Association> associations;
    private SchemaUriProcessor schemaUriProcessor;
    private int i;

    private String resourceName;

    public static final String IMPORT_TAG = "import";
    public static final String INCLUDE_TAG = "include";
    private static final String SAMPLE_BASE_URL = "http://this.wsdl.needs/a/valid/url/to/proceed.wsdl";

    private List<String> visitedWSDLs;
    private List<String> processedWSDLs;
    private Map<String, WSDLInfo> wsdls;

    private WSDLValidationInfo wsdlValidationInfo = null;
    private WSDLValidationInfo wsiValidationInfo = null;

    private boolean hasWSDLImports = false;

    private static Log log = LogFactory.getLog(WsdlUriProcessor.class);

    public WsdlUriProcessor(RequestContext requestContext) {
        this.registry = requestContext.getRegistry();
        try {
            this.systemRegistry = CommonUtil.getUnchrootedSystemRegistry(requestContext);
            if (!systemRegistry.resourceExists(
                    getChrootedSchemaLocation(requestContext.getRegistryContext()))) {
                systemRegistry.put(getChrootedSchemaLocation(requestContext.getRegistryContext()),
                        systemRegistry.newCollection());
            }
            if (!systemRegistry.resourceExists(
                    getChrootedWSDLLocation(requestContext.getRegistryContext()))) {
                systemRegistry.put(getChrootedWSDLLocation(requestContext.getRegistryContext()),
                        systemRegistry.newCollection());
            }
            int tenantId = CurrentSession.getTenantId();
            String userName = CurrentSession.getUser();
            this.governanceUserRegistry = GovernanceRegistryExtensionsComponent.getRegistryService().getGovernanceUserRegistry(userName, tenantId);

        } catch (RegistryException ignore) {
            this.systemRegistry = null;
        }
        i = 0;
        associations = new ArrayList<Association>();
        visitedWSDLs = new ArrayList<String>();
        processedWSDLs = new ArrayList<String>();
        schemaUriProcessor = buildSchemaProcessor(requestContext, null);
        wsdls = new LinkedHashMap<String, WSDLInfo>();
        resourceName = "";
    }

    public static String getChrootedWSDLLocation(RegistryContext registryContext) {
        return RegistryUtils.getAbsolutePath(registryContext,
                RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + HandlerConstants.WSDL_LOCATION);
    }


    private String getChrootedSchemaLocation(RegistryContext registryContext) {
        return RegistryUtils.getAbsolutePath(registryContext,
                RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + HandlerConstants.XSD_LOCATION);
    }

    private String getChrootedPolicyLocation(RegistryContext registryContext) {
        return RegistryUtils.getAbsolutePath(registryContext,
                RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + HandlerConstants.POLICY_LOCATION);
    }

    private String getChrootedServiceLocation(Registry registry, RegistryContext registryContext) {
        return  RegistryUtils.getAbsolutePath(registryContext,
                registry.getRegistryContext().getServicePath());  // service path contains the base
    }

    public String addWSDLToRegistry(
            RequestContext context,
            String wsdlURL,
            Resource metadata, boolean isPut)
            throws RegistryException {
        boolean evaluateExports = true;
        boolean isDefaultEnvironment =true;
        String currentWsdlLocation = null;
        String currentSchemaLocation = null;
        String currentEndpointLocation = null;
        String currentEnvironment;
        String masterVersion= null;

        List<String> dependeinciesList = new ArrayList<String>();


        String resourcePath = context.getResourcePath().getPath();
        resourceName = resourcePath.substring(resourcePath.lastIndexOf(RegistryConstants.PATH_SEPARATOR) + 1);
        RegistryContext registryContext = context.getRegistryContext();
        // 3rd parameter is false, for importing WSDLs.
        evaluateWSDLsToDefinitions(wsdlURL, context, evaluateExports, false, isPut);
        String wsdlPath;
        for (WSDLInfo wsdlInfo : wsdls.values()) {
            Definition wsdlDefinition = wsdlInfo.getWSDLDefinition();
            if (wsdlDefinition != null) {
                Types types = wsdlDefinition.getTypes();
                schemaUriProcessor.evaluateSchemas(types,
                        wsdlDefinition.getDocumentBaseURI(),
                        wsdlInfo.getSchemaDependencies());
                String wsdlName = wsdlInfo.getProposedRegistryURL();
                int index = wsdlName.lastIndexOf("/");
                String wsdlResourceName = wsdlName.substring(index +1);
                wsdlPath = getChrootedWSDLLocation(registryContext) + wsdlResourceName;
                if(!resourcePath.contains(wsdlResourceName)){
                    wsdlInfo.setProposedRegistryURL(wsdlPath);
                    continue;
                }
                if (!resourcePath.equals(RegistryConstants.PATH_SEPARATOR + wsdlName)
                        & !resourcePath.contains(HandlerConstants.WSDL_LOCATION) & (registry.resourceExists(resourcePath))) {
                    if(currentWsdlLocation == null){
                        currentEnvironment = resourcePath.substring(0,resourcePath.indexOf(CommonUtil.
                                derivePathFragmentFromNamespace(wsdlDefinition.getTargetNamespace()).replace("//", "/")));
                        String[] pathFragments = HandlerConstants.WSDL_LOCATION.split(RegistryConstants.PATH_SEPARATOR);
                        String wsdlLocation = HandlerConstants.WSDL_LOCATION;
                        String schemaLocation = HandlerConstants.XSD_LOCATION;
                        String policyLocation = HandlerConstants.POLICY_LOCATION;
                        String endpointLocation = EndpointUtils.getEndpointLocation();
                        for (String pathFragment : pathFragments) {
                            wsdlLocation = wsdlLocation.replace(pathFragment,"");
                            if(wsdlLocation.startsWith(RegistryConstants.PATH_SEPARATOR)){
                                wsdlLocation = wsdlLocation.replaceFirst(RegistryConstants.PATH_SEPARATOR,"");
                            }
                            schemaLocation = schemaLocation.replace(pathFragment,"");
                            if(schemaLocation.startsWith(RegistryConstants.PATH_SEPARATOR)){
                                schemaLocation = schemaLocation.replaceFirst(RegistryConstants.PATH_SEPARATOR,"");
                            }
                            policyLocation = policyLocation.replace(pathFragment,"");
                            if(policyLocation.startsWith(RegistryConstants.PATH_SEPARATOR)){
                                policyLocation = policyLocation.replaceFirst(RegistryConstants.PATH_SEPARATOR,"");
                            }
                            endpointLocation = endpointLocation.replace(pathFragment,"");
                            if(endpointLocation.startsWith(RegistryConstants.PATH_SEPARATOR)){
                                endpointLocation = endpointLocation.replaceFirst(RegistryConstants.PATH_SEPARATOR,"");
                            }
                            currentEnvironment = currentEnvironment.replace(pathFragment,"");
                        }
                        if(wsdlLocation.endsWith(RegistryConstants.PATH_SEPARATOR)){
                            wsdlLocation = wsdlLocation.substring(0, wsdlLocation.length() - 1);
                        }
                        if(schemaLocation.endsWith(RegistryConstants.PATH_SEPARATOR)){
                            schemaLocation = schemaLocation.substring(0, schemaLocation.length() - 1);
                        }
                        if(policyLocation.startsWith(RegistryConstants.PATH_SEPARATOR)){
                            policyLocation = policyLocation.substring(0, policyLocation.length() - 1);
                        }
                        if(endpointLocation.startsWith(RegistryConstants.PATH_SEPARATOR)){
                            endpointLocation = endpointLocation.substring(0, endpointLocation.length() - 1);
                        }
                        currentWsdlLocation = currentEnvironment + wsdlLocation;
                        currentSchemaLocation = currentEnvironment + schemaLocation;
                        currentEndpointLocation = currentEnvironment + endpointLocation;

                    }
                    if(masterVersion == null){
                        String namespaceSegment = CommonUtil.
                                derivePathFragmentFromNamespace(wsdlDefinition.getTargetNamespace()).replace("//", "/");
                        String suffix = resourcePath.substring(resourcePath.indexOf(namespaceSegment)
                                + namespaceSegment.length());
                        masterVersion = suffix.substring(0,suffix.indexOf(RegistryConstants.PATH_SEPARATOR));
                    }
                    wsdlPath = resourcePath;
                    isDefaultEnvironment = false;

                    Association[] associations = registry.getAssociations(wsdlPath, CommonConstants.DEPENDS);
                    for (Association association : associations) {
                        if(association.getSourcePath().equals(wsdlPath)){
                            dependeinciesList.add(association.getDestinationPath());
                        }
                    }
                }
                wsdlInfo.setProposedRegistryURL(wsdlPath);
            }

        }

        String masterWSDLPath;
        if (!isDefaultEnvironment) {
            schemaUriProcessor.saveSchemasToRegistry(currentSchemaLocation,
                    null,masterVersion,dependeinciesList);
            updateWSDLSchemaLocations();
            masterWSDLPath = saveWSDLsToRepositoryNew(metadata,currentEndpointLocation
                    ,dependeinciesList,masterVersion);// 3rd parameter is false, for importing WSDLs.

            addPolicyImports(context);

            saveAssociations();
        } else {
            schemaUriProcessor.saveSchemasToRegistry(getChrootedSchemaLocation(registryContext),
                    null);
            updateWSDLSchemaLocations();

            masterWSDLPath = saveWSDLsToRepositoryNew(metadata);

            addPolicyImports(context);

            saveAssociations();
            List<OMElement> serviceContentBeans = createServiceContent(masterWSDLPath, metadata);
            for (OMElement serviceContentBean : serviceContentBeans) {
                addService(serviceContentBean, context);

            }
        }
        return masterWSDLPath;
    }

    private void addPolicyImports(RequestContext context) throws RegistryException {
        /* storing policyReferences in to Registry if available in the WSDL */
        for (WSDLInfo wsdlInfo : wsdls.values()) {
            if(wsdlInfo.isExistPolicyReferences()){
                Iterator iter = wsdlInfo.getPolicyDependencies().iterator();
                while(iter.hasNext()){
                    String policyURL = (String)iter.next();
                    boolean lockAlreadyAcquired = !CommonUtil.isUpdateLockAvailable();
                    CommonUtil.releaseUpdateLock();
                    try{
                        String path = policyURL.substring(policyURL.lastIndexOf(RegistryConstants.PATH_SEPARATOR) + 1);
                        if(policyURL.lastIndexOf(RegistryConstants.PATH_SEPARATOR) > 0){
                            String source = getSource(policyURL);

                            String policyPath = getChrootedPolicyLocation(context.getRegistryContext()) + path;
                            GenericArtifactManager genericArtifactManager = new GenericArtifactManager(governanceUserRegistry, "uri");

                            if(!registry.resourceExists(policyPath)){
                                GenericArtifact policy = genericArtifactManager.newGovernanceArtifact(new QName(source));
                                policy.setAttribute("overview_name", source);
                                policy.setAttribute("overview_uri", policyURL);
                                policy.setAttribute("overview_type", HandlerConstants.POLICY);
                                genericArtifactManager.addGenericArtifact(policy);
                            } else {
                                Resource resource = registry.get(policyPath);
                                GenericArtifact artifact = genericArtifactManager.getGenericArtifact(resource.getUUID());
                                if(!artifact.getAttribute("overview_uri").equals(policyURL)){
                                    throw new RegistryException("Different policy URI already exists in " + policyPath + ".");
                                }
                            }
                            registry.addAssociation(policyPath,
                                    wsdlInfo.getProposedRegistryURL(), CommonConstants.USED_BY);
                            registry.addAssociation(wsdlInfo.getProposedRegistryURL(),
                                    policyPath, CommonConstants.DEPENDS);
                        }
                    }finally {
                        if (lockAlreadyAcquired) {
                            CommonUtil.acquireUpdateLock();
                        }
                    }
                }
            }
        }
    }

    /**
     * Get Master WSDL
     * @return  WSDLinfo object of the Master WSDL
     */
    public WSDLInfo getMasterWSDLInfo() {
        for (WSDLInfo wsdlInfo : wsdls.values()) {
            if (wsdlInfo.isMasterWSDL()) {
                return wsdlInfo;
            }
        }
        return null;
    }

    /**
     * Save associations to the registry if they do not exist.
     * Execution time could be improved if registry provides a better way to check existing associations.
     *
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException Thrown in case a association cannot be saved
     */
    private void saveAssociations() throws RegistryException {
        // until registry provides a functionality to check existing associations, this method will consume a LOT of time
        for (Association association : associations) {
            boolean isAssociationExist = false;
            Association[] existingAssociations = registry.getAllAssociations(association.getSourcePath());
            if (existingAssociations != null) {
                for (Association currentAssociation : existingAssociations) {
                    if (currentAssociation.getDestinationPath().equals(association.getDestinationPath()) &&
                            currentAssociation.getAssociationType().equals(association.getAssociationType())) {
                        isAssociationExist = true;
                        break;
                    }
                }
            }
            if (!isAssociationExist) {
                registry.addAssociation(association.getSourcePath(),
                        association.getDestinationPath(),
                        association.getAssociationType());
            }
        }
    }

    /**
     * Extract an appropriate name for the resource from the given URL
     *
     * @param wsdlURL, the URL
     * @param suffix,  the suffix introduced
     * @return resource name
     */
    private String extractResourceFromURL(String wsdlURL, String suffix) {
        String resourceName = wsdlURL;
        if (wsdlURL.indexOf("?") > 0) {
            resourceName = wsdlURL.substring(0, wsdlURL.indexOf("?")) + suffix;
        } else if (wsdlURL.indexOf(".") > 0) {
            resourceName = wsdlURL.substring(0, wsdlURL.lastIndexOf(".")) + suffix;
        } else if (!wsdlURL.endsWith(".wsdl")) {
            resourceName = wsdlURL + suffix;
        }
        return resourceName;
    }

    private void evaluateWSDLsToDefinitions(String wsdlLocation,
                                            RequestContext context,
                                            boolean evaluateImports,
                                            boolean isServiceImport,
                                            boolean isPut)
            throws RegistryException {
        WSDLReader wsdlReader;
        Definition wsdlDefinition = null;
        originalDefinition = null;

        try {
            wsdlReader = new ExWSDLReaderImpl(
                    (WSDLReaderImpl) WSDLFactory.newInstance().newWSDLReader());
        } catch (WSDLException e) {
            String msg = "Could not initiate the wsdl reader. Caused by: " + e.getMessage();
            throw new RegistryException(msg);
        }

        wsdlReader.setFeature("javax.wsdl.importDocuments", true);
        wsdlReader.setFeature("javax.wsdl.verbose", log.isDebugEnabled());

        try {
            if (isPut) {
                ByteArrayInputStream byteArrayInputStream =
                        new ByteArrayInputStream((byte[]) context.getResource().getContent());
                InputSource inputSource = new InputSource(byteArrayInputStream);
                wsdlDefinition = wsdlReader.readWSDL(null, inputSource);
            } else {
                wsdlDefinition = wsdlReader.readWSDL(wsdlLocation);
            }
            originalDefinition = wsdlDefinition;
        } catch (WSDLException e) {
            String msg = "Could not evaluate WSDL Definition.";
            if (e.getCause() instanceof ConnectException ||
                    e.getCause() instanceof UnknownHostException) {
                msg += " Unable to resolve imported document at '" + wsdlLocation +
                        "'. Connection refused.";
                log.error(msg, e);
            } else if (e.getCause() instanceof IOException) {
                msg += " This WSDL file or one of its imports was not found.";
                log.error(msg, e);
            }
        }

        if (!isServiceImport) {
            processedWSDLs.add(resourceName);
        }

        Map map = null;
        if (wsdlDefinition != null) {
            map = wsdlDefinition.getImports();
        }
        // We perform validation only if there are no wsdl imports

        if (map != null && map.size() == 0) {
            log.trace("Starting WSDL Validation");
            wsdlValidationInfo = WSDLUtils.validateWSDL(context);
            log.trace("Ending WSDL Validation");
            log.trace("Starting WSI Validation");
            wsiValidationInfo = WSDLUtils.validateWSI(context);
            log.trace("Ending WSI Validation");

        } else {
            hasWSDLImports = true;
        }

        if(wsdlDefinition == null) {
            log.trace("Invalid WSDL definition found.");
            throw new RegistryException("Invalid WSDL definition found.");
        }
        evaluateWSDLsToDefinitionsRecursively(wsdlDefinition, evaluateImports, isServiceImport, null, true);
    }

    private void evaluateWSDLsToDefinitionsRecursively(Definition wsdlDefinition,
                                                       boolean evaluateImports,
                                                       boolean isServiceImport, WSDLInfo parent, boolean masterWSDL)
            throws RegistryException {
        WSDLInfo wsdlInfo = new WSDLInfo();
        wsdlInfo.setMasterWSDL(masterWSDL);
        wsdlInfo.setParent(parent);
        if (evaluateImports) {
            Iterator iter = wsdlDefinition.getImports().values().iterator();
            Vector values;
            Import wsdlImport;
            visitedWSDLs.add(wsdlDefinition.getDocumentBaseURI());
            CommonUtil.addImportedArtifact(new File(wsdlDefinition.getDocumentBaseURI()).toString());
            for (; iter.hasNext();) {
                values = (Vector) iter.next();
                for (Object value : values) {
                    wsdlImport = (Import) value;
                    Definition innerDefinition = wsdlImport.getDefinition();
                    if (innerDefinition == null) {
                        continue;
                    }
                    if (innerDefinition.getTargetNamespace() == null) {
                        // if this import was a schema, WSDL4J will not extract the target
                        // namespace.
                        continue;
                    }
                    wsdlInfo.getWSDLDependencies().add(innerDefinition.getDocumentBaseURI());
                    if (!visitedWSDLs.contains(innerDefinition.getDocumentBaseURI())) {
                        evaluateWSDLsToDefinitionsRecursively(
                                innerDefinition,
                                evaluateImports,
                                isServiceImport, wsdlInfo, false);
                    }
                }
            }
        }

        Iterator iter = wsdlDefinition.getBindings().values().iterator();
        while(iter.hasNext()){
            Binding binding = (Binding)iter.next();
            if(binding.getBindingPolicyReference() != null){
                wsdlInfo.setExistPolicyReferences(true);
                wsdlInfo.getPolicyDependencies().add(binding.getBindingPolicyReference().getURI());
            }
        }



        String baseURI = wsdlDefinition.getDocumentBaseURI();
        String fileNameToSave;
        if (baseURI != null) {
            String wsdlFileName = baseURI.substring(baseURI.lastIndexOf(RegistryConstants.PATH_SEPARATOR) + 1);
            if ((baseURI.equals(originalDefinition.getDocumentBaseURI()))
                    && (!isServiceImport)) {
                fileNameToSave = extractResourceFromURL(resourceName, ".wsdl");
            }
            else {
                fileNameToSave = extractResourceFromURL(wsdlFileName, ".wsdl");
                while (processedWSDLs.contains(fileNameToSave)) {
                    fileNameToSave = extractResourceFromURL(wsdlFileName, (++i) + ".wsdl");
                }
            }
            wsdlInfo.setOriginalURL(baseURI);
        }
        else {
            // This is taken from the file system. So, no base URI is available for the wsdl.
            fileNameToSave = extractResourceFromURL(resourceName, ".wsdl");
            wsdlInfo.setOriginalURL(SAMPLE_BASE_URL);
            wsdlDefinition.setDocumentBaseURI(SAMPLE_BASE_URL);
        }

        wsdlInfo.setWSDLDefinition(wsdlDefinition);
        wsdlInfo.setProposedRegistryURL(fileNameToSave);
        wsdls.put(baseURI, wsdlInfo);
        processedWSDLs.add(fileNameToSave);
    }

    /**
     * Change all schema path locations to registry locations in each WSDL definitions
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException Thrown in case the schema
     * locations cannot be updated
     */
    private void updateWSDLSchemaLocations() throws RegistryException {
        updateWSDLocations();
        for (WSDLInfo wsdlInfo : wsdls.values()) {
            Definition definition = wsdlInfo.getWSDLDefinition();
            Types types = definition.getTypes();
            if (types != null) {
                List extensibleElements = types.getExtensibilityElements();
                Schema schemaExtension;
                Object extensionObject;
                for (Object extensibleElement : extensibleElements) {
                    extensionObject = extensibleElement;
                    if (extensionObject instanceof Schema) {
                        // first get the schema object
                        schemaExtension = (Schema) extensionObject;
                        NodeList nodeList = schemaExtension.getElement().getChildNodes();
                        String tagName;
                        for (int i = 0; i < nodeList.getLength(); i++) {
                            tagName = nodeList.item(i).getLocalName();
                            if (IMPORT_TAG.equals(tagName) || INCLUDE_TAG.equals(tagName)) {
                                NamedNodeMap nodeMap = nodeList.item(i).getAttributes();
                                Node attribute;
                                String attributeValue;
                                for (int j = 0; j < nodeMap.getLength(); j++) {
                                    attribute = nodeMap.item(j);
                                    if (attribute.getNodeName().equals("schemaLocation")) {
                                        attributeValue = attribute.getNodeValue();
                                        String schemaPath = schemaUriProcessor.getSchemaRegistryPath(wsdlInfo.getProposedRegistryURL(), attributeValue);
                                        if (schemaPath != null) {
                                            attribute.setNodeValue(schemaPath);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void updateWSDLocations() {
        for (WSDLInfo wsdlInfo : wsdls.values()) {
            Definition definition = wsdlInfo.getWSDLDefinition();
            ArrayList<String> wsdlDependencies = wsdlInfo.getWSDLDependencies();
            Vector[] importVector = (Vector[])definition.getImports().values().toArray(new Vector[definition.getImports().values().size()]);
            int count = 0;
            for (String wsdlDependancy : wsdlDependencies) {
                Vector values = importVector[count];
                WSDLInfo dependantWSDLInfo = wsdls.get(wsdlDependancy);
                dependantWSDLInfo.getProposedRegistryURL();
                for (Object value : values) {
                    Import importedWSDL = (Import) value;
                    String relativeSchemaPath =  WSDLUtil.computeRelativePathWithVersion(wsdlInfo.getProposedRegistryURL(),
                            dependantWSDLInfo.getProposedRegistryURL(), registry);
                    importedWSDL.setLocationURI(relativeSchemaPath);
                }
                count++;
            }
        }
    }

    private void identifyAssociationsNew(WSDLInfo wsdlInfo) {
        String wsdlPath = wsdlInfo.getProposedRegistryURL();
        for (String association : wsdlInfo.getSchemaDependencies()) {
            String associatedTo = schemaUriProcessor.getSchemaAssociationPath(association);
            if (associatedTo != null) {
                associations.add(new Association(wsdlPath,
                        associatedTo,
                        CommonConstants.DEPENDS));
                associations.add(new Association(associatedTo,
                        wsdlPath,
                        CommonConstants.USED_BY));
            }
        }
        for (String association : wsdlInfo.getWSDLDependencies()) {
            WSDLInfo info = wsdls.get(association);
            if (info != null) {
                String associatedTo = info.getProposedRegistryURL();
                if (associatedTo != null) {
                    associations.add(new Association(wsdlPath,
                            associatedTo,
                            CommonConstants.DEPENDS));
                    associations.add(new Association(associatedTo,
                            wsdlPath,
                            CommonConstants.USED_BY));
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private String saveWSDLsToRepositoryNew(Resource metaDataResource)
            throws RegistryException {
        String masterWSDLPath = null;
        try {
            for (WSDLInfo wsdlInfo : wsdls.values()) {
                Definition wsdlDefinition = wsdlInfo.getWSDLDefinition();
                WSDLWriter wsdlWriter = WSDLFactory.newInstance().newWSDLWriter();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                wsdlWriter.writeWSDL(wsdlDefinition, byteArrayOutputStream);
                byte[] wsdlResourceContent = byteArrayOutputStream.toByteArray();

                // create a resource this wsdlResourceContent and put it to the registry with the name
                // importedResourceName (in some path)
                String wsdlPath = wsdlInfo.getProposedRegistryURL();
                Resource wsdlResource;
                if (metaDataResource != null && registry.resourceExists(wsdlPath)) {
                    wsdlResource = registry.get(wsdlPath);
                } else {
                    wsdlResource = new ResourceImpl();
                    wsdlResource.setUUID(metaDataResource.getUUID());
                    if (metaDataResource != null) {
                        Properties properties = metaDataResource.getProperties();
                        if (properties != null) {
                            List<String> linkProperties =
                                    Arrays.asList(RegistryConstants.REGISTRY_LINK,
                                            RegistryConstants.REGISTRY_USER,
                                            RegistryConstants.REGISTRY_MOUNT,
                                            RegistryConstants.REGISTRY_AUTHOR,
                                            RegistryConstants.REGISTRY_MOUNT_POINT,
                                            RegistryConstants.REGISTRY_TARGET_POINT,
                                            RegistryConstants.REGISTRY_ACTUAL_PATH,
                                            RegistryConstants.REGISTRY_REAL_PATH);
                            for (Map.Entry<Object, Object> e : properties.entrySet()) {
                                String key = (String) e.getKey();
                                if (!linkProperties.contains(key)) {
                                    wsdlResource.setProperty(key, (List<String>) e.getValue());
                                }
                            }
                        }
                    }
                }

                // getting the parameters
                masterWSDLPath = addProperties(masterWSDLPath, wsdlInfo, wsdlDefinition, wsdlResourceContent, wsdlPath, wsdlResource);
                if (metaDataResource != null) {
                    wsdlResource.setDescription(metaDataResource.getDescription());
                }
                saveResource(wsdlInfo.getOriginalURL(), wsdlPath, wsdlResource, wsdlInfo.isMasterWSDL());
                if (systemRegistry != null) {
                    org.wso2.carbon.registry.extensions.handlers.utils.EndpointUtils.saveEndpointsFromWSDL(wsdlPath, wsdlResource, registry,
                            systemRegistry);
                }

                identifyAssociationsNew(wsdlInfo);
            }
        }
        catch (WSDLException e) {
            throw new RegistryException("Invalid WSDL file");
        }
        return masterWSDLPath;
    }

    private String addProperties(String masterWSDLPath, WSDLInfo wsdlInfo, Definition wsdlDefinition,
                                 byte[] wsdlResourceContent, String wsdlPath, Resource wsdlResource)
            throws RegistryException {
        if (wsdlDefinition.getQName() != null) {
            String name = wsdlDefinition.getQName().getLocalPart();
            if (name != null) {
                wsdlResource.addProperty("registry.wsdl.Name", name);
            }
        }
        if (wsdlDefinition.getDocumentationElement() != null) {
            String document = wsdlDefinition.getDocumentationElement().getTextContent();
            if (document != null) {
                wsdlResource.addProperty("registry.wsdl.documentation", document);
            }
        }

        String targetNamespace = wsdlDefinition.getTargetNamespace();
        wsdlResource.addProperty("registry.wsdl.TargetNamespace", targetNamespace);
        wsdlResource.setMediaType(RegistryConstants.WSDL_MEDIA_TYPE);
        wsdlResource.setContent(wsdlResourceContent);

        if (wsdlInfo.isMasterWSDL()) {
            masterWSDLPath = wsdlPath;
            log.trace("Setting WSDL Validation properties");
            if (wsdlValidationInfo != null) {
                wsdlResource.setProperty(WSDLUtils.WSDL_STATUS, wsdlValidationInfo.getStatus());
                ArrayList<String> validationMessages = wsdlValidationInfo.getValidationMessages();
                if (validationMessages.size() > 0) {
                    int i = 1;
                    for (String message : validationMessages) {
                        if (message == null) {
                            continue;
                        }
                        if (message.length() > 1000) {
                            message = message.substring(0, 997) + "...";
                        }
                        wsdlResource.setProperty(WSDLUtils.WSDL_VALIDATION_MESSAGE + i,
                                message);
                        i++;
                    }

                }
            } else if (hasWSDLImports) {
                wsdlResource.setProperty(WSDLUtils.WSDL_STATUS,
                        "Validation is not supported for WSDLs containing WSDL imports.");
            }
            log.trace("Finished setting WSDL Validation properties");
            log.trace("Setting WSI Validation properties");
            if (wsiValidationInfo != null) {
                wsdlResource.setProperty(WSDLUtils.WSI_STATUS,
                        wsiValidationInfo.getStatus());
                ArrayList<String> validationMessages =
                        wsiValidationInfo.getValidationMessages();
                if (validationMessages.size() > 0) {
                    int i = 1;
                    for (String message : validationMessages) {
                        if (message == null) {
                            continue;
                        }
                        if (message.length() > 1000) {
                            message = message.substring(0, 997) + "...";
                        }
                        wsdlResource.setProperty(WSDLUtils.WSI_VALIDATION_MESSAGE + i,
                                message);
                        i++;
                    }

                }
            } else if (hasWSDLImports) {
                wsdlResource.setProperty(WSDLUtils.WSI_STATUS,
                        "Validation is not supported for WSDLs containing WSDL imports.");
            }
            log.trace("Finished setting WSI Validation properties");
        }
        return masterWSDLPath;
    }

    @SuppressWarnings("unchecked")
    private String saveWSDLsToRepositoryNew(Resource metaDataResource
            ,String endpointEnvironment,List<String> dependenciesList,String version)
            throws RegistryException {
        String masterWSDLPath = null;
        try {
            for (WSDLInfo wsdlInfo : wsdls.values()) {
                Definition wsdlDefinition = wsdlInfo.getWSDLDefinition();
                WSDLWriter wsdlWriter = WSDLFactory.newInstance().newWSDLWriter();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                wsdlWriter.writeWSDL(wsdlDefinition, byteArrayOutputStream);
                byte[] wsdlResourceContent = byteArrayOutputStream.toByteArray();

                // create a resource this wsdlResourceContent and put it to the registry with the name
                // importedResourceName (in some path)
                String wsdlPath = wsdlInfo.getProposedRegistryURL();
                Resource wsdlResource;
                if (metaDataResource != null && registry.resourceExists(wsdlPath)) {
                    wsdlResource = registry.get(wsdlPath);
                } else {
                    wsdlResource = new ResourceImpl();
                    if (metaDataResource != null) {
                        Properties properties = metaDataResource.getProperties();
                        if (properties != null) {
                            List<String> linkProperties =
                                    Arrays.asList(RegistryConstants.REGISTRY_LINK,
                                            RegistryConstants.REGISTRY_USER,
                                            RegistryConstants.REGISTRY_MOUNT,
                                            RegistryConstants.REGISTRY_AUTHOR,
                                            RegistryConstants.REGISTRY_MOUNT_POINT,
                                            RegistryConstants.REGISTRY_TARGET_POINT,
                                            RegistryConstants.REGISTRY_ACTUAL_PATH,
                                            RegistryConstants.REGISTRY_REAL_PATH);
                            for (Map.Entry<Object, Object> e : properties.entrySet()) {
                                String key = (String) e.getKey();
                                if (!linkProperties.contains(key)) {
                                    wsdlResource.setProperty(key, (List<String>) e.getValue());
                                }
                            }
                        }
                    }
                }

                if(registry.resourceExists(wsdlPath)){
                    // we are copying all the properties, rather than using the exisint pointer
                    Resource oldWsdlResource = registry.get(wsdlPath);
                    Properties properties = oldWsdlResource.getProperties();
                    for (Map.Entry<Object, Object> e : properties.entrySet()) {
                        if (e.getValue() instanceof String) {
                            wsdlResource.setProperty((String) e.getKey(), (String) e.getValue());
                        } else {
                            wsdlResource.setProperty((String) e.getKey(),
                                    (List<String>) e.getValue());
                        }
                    }
                }
                // getting the parameters
                masterWSDLPath = addProperties(masterWSDLPath, wsdlInfo, wsdlDefinition, wsdlResourceContent, wsdlPath, wsdlResource);
                if (metaDataResource != null) {
                    wsdlResource.setDescription(metaDataResource.getDescription());
                }
                saveResource(wsdlInfo.getOriginalURL(), wsdlPath, wsdlResource, wsdlInfo.isMasterWSDL());
                if (systemRegistry != null) {
                    EndpointUtils.saveEndpointsFromWSDL(wsdlPath, wsdlResource, registry,
                            systemRegistry, endpointEnvironment, dependenciesList, version);
                }

                identifyAssociationsNew(wsdlInfo);
            }
        }
        catch (WSDLException e) {
            throw new RegistryException("Invalid WSDL file");
        }
        return masterWSDLPath;
    }

    private List<OMElement> createServiceContent(String wsdlURL, Resource metadata)
            throws RegistryException {
        List<OMElement> serviceContentomelements = new ArrayList<OMElement>();
        if (originalDefinition != null) {
            Map servicesMap = originalDefinition.getServices();
            for (Object serviceObject : servicesMap.values()) {
                Service service = (Service) serviceObject;
                QName qname = service.getQName();
                OMFactory fac = OMAbstractFactory.getOMFactory();
                OMNamespace namespace = fac.createOMNamespace(CommonConstants.SERVICE_ELEMENT_NAMESPACE, "");

                OMElement data = fac.createOMElement(CommonConstants.SERVICE_ELEMENT_ROOT, namespace);
                OMElement wsdlurl = fac.createOMElement("wsdlURL", namespace);
                OMElement overview = fac.createOMElement("overview", namespace);
                OMElement interfaceelement = fac.createOMElement("interface", namespace);
                OMElement name = fac.createOMElement("name", namespace);
                name.setText(qname.getLocalPart());
                wsdlurl.setText(RegistryUtils.getRelativePath(registry.getRegistryContext(), wsdlURL));
                OMElement namespaceElement = fac.createOMElement("namespace", namespace);
                OMElement descriptionelement = fac.createOMElement("description", namespace);

                namespaceElement.setText(qname.getNamespaceURI());
                String description = metadata.getDescription();
                if (description == null) {
                    Element documentationElement = originalDefinition.getDocumentationElement();
                    if ((documentationElement != null) && (documentationElement.getFirstChild() != null)) {
                        Node firstChild = documentationElement.getFirstChild();
                        description = DOM2Writer.nodeToString(firstChild);
                    }
                }
                descriptionelement.setText(description);

                overview.addChild(name);
                overview.addChild(namespaceElement);
                overview.addChild(descriptionelement);
                interfaceelement.addChild(wsdlurl);
                data.addChild(overview);
                data.addChild(interfaceelement);
                serviceContentomelements.add(data);
            }

        }
        return serviceContentomelements;
    }


    private void saveResource(String url, String path, Resource resource,
                              boolean isMasterWSDL)
            throws RegistryException {
        log.trace("Started saving resource");

        String source = getSource(path);
        GenericArtifactManager genericArtifactManager = new GenericArtifactManager(governanceUserRegistry, "uri");
        GenericArtifact wsdl = genericArtifactManager.newGovernanceArtifact(new QName(source));
        if (isMasterWSDL){
            wsdl.setId(resource.getUUID());
        }
        wsdl.setAttribute("overview_name", source);
        wsdl.setAttribute("overview_uri", url);
        wsdl.setAttribute("overview_type", HandlerConstants.WSDL);
        genericArtifactManager.addGenericArtifact(wsdl);

        log.trace("Finished saving resource");
    }

    /**
     * Method to customize the Schema Processor.
     * @param requestContext the request context for the import/put operation.
     * @param validationInfo the WSDL validation information.
     * @return the Schema Processor instance.
     */
    @SuppressWarnings("unused")
    protected SchemaUriProcessor buildSchemaProcessor(RequestContext requestContext,
                                                      WSDLValidationInfo validationInfo) {
        return new SchemaUriProcessor(requestContext, validationInfo);
    }

    /**
     * Method to customize the Schema Processor.
     * @param requestContext the request context for the import/put operation.
     * @param validationInfo the WSDL validation information.
     * @param useOriginalSchema whether the schema to be original
     * @return the Schema Processor instance.
     */
    @SuppressWarnings("unused")
    protected SchemaUriProcessor buildSchemaProcessor(RequestContext requestContext,
                                                      WSDLValidationInfo validationInfo, boolean useOriginalSchema) {
        return new SchemaUriProcessor(requestContext, validationInfo, useOriginalSchema);
    }

    private void addService(OMElement service, RequestContext context)throws RegistryException {
        Resource resource = registry.newResource();
        String tempNamespace = CommonUtil.derivePathFragmentFromNamespace(
                CommonUtil.getServiceNamespace(service));
        String path = getChrootedServiceLocation(registry, context.getRegistryContext()) + tempNamespace +
                CommonUtil.getServiceName(service);
        String artifactId = UUID.randomUUID().toString();
        resource.setUUID(artifactId);
        String content = service.toString();
        resource.setContent(RegistryUtils.encodeString(content));
        resource.setMediaType(RegistryConstants.SERVICE_MEDIA_TYPE);
        // when saving the resource we are expecting to call the service media type handler, so
        // we intentionally release the lock here.
        boolean lockAlreadyAcquired = !CommonUtil.isUpdateLockAvailable();
        CommonUtil.releaseUpdateLock();
        try {
            registry.put(path, resource);
        } finally {
            if (lockAlreadyAcquired) {
                CommonUtil.acquireUpdateLock();
            }
        }
        registry.addAssociation(path, RegistryUtils.getAbsolutePath(registry.getRegistryContext(),
                CommonUtil.getDefinitionURL(service)), CommonConstants.DEPENDS);
        registry.addAssociation(RegistryUtils.getAbsolutePath(registry.getRegistryContext(),
                CommonUtil.getDefinitionURL(service)),path, CommonConstants.USED_BY);
    }

    public static String getSource(String uri){
        return uri.split("/")[uri.split("/").length -1];
    }

}
