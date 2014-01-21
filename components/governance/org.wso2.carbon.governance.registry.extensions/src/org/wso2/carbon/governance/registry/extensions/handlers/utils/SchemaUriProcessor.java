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

import com.ibm.wsdl.extensions.schema.SchemaImportImpl;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaExternal;
import org.apache.ws.commons.schema.XmlSchemaObjectCollection;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.registry.extensions.internal.GovernanceRegistryExtensionsComponent;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.session.CurrentSession;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.extensions.handlers.utils.SchemaInfo;
import org.wso2.carbon.registry.extensions.handlers.utils.WSDLUtils;
import org.wso2.carbon.registry.extensions.utils.CommonConstants;
import org.wso2.carbon.registry.extensions.utils.CommonUtil;
import org.wso2.carbon.registry.extensions.utils.WSDLUtil;
import org.wso2.carbon.registry.extensions.utils.WSDLValidationInfo;
import org.wso2.carbon.utils.CarbonUtils;
import org.xml.sax.InputSource;

import javax.wsdl.Types;
import javax.wsdl.extensions.schema.Schema;
import javax.xml.namespace.QName;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class SchemaUriProcessor {
    private Registry registry;
    private Registry systemRegistry;
    private Registry governanceUserRegistry;
    private List<String> processedSchemas;
    private List<String> visitedSchemas;
    private Map<String, SchemaInfo> schemas;
    private List<Association> associations;
    private String baseURI = null;
    private WSDLValidationInfo validationInfo;
    private static final String SCHEMA_VALIDATION_MESSAGE = "Schema Validation Message ";
    private static final String SCHEMA_STATUS = "Schema Validation";
    private boolean useOriginalSchema;

    private String resourceName = "";

    private static final Log log = LogFactory.getLog(SchemaUriProcessor.class);

    private int i;

    public SchemaUriProcessor(RequestContext requestContext, WSDLValidationInfo validationInfo) {
        this.registry = requestContext.getRegistry();
        try {
            this.systemRegistry = CommonUtil.getUnchrootedSystemRegistry(requestContext);
            int tenantId = CurrentSession.getTenantId();
            String userName = CurrentSession.getUser();
            this.governanceUserRegistry = GovernanceRegistryExtensionsComponent.getRegistryService().getGovernanceUserRegistry(userName, tenantId);
        } catch (RegistryException ignore) {
            this.systemRegistry = null;
        }
        i = 0;
        schemas = new LinkedHashMap<String, SchemaInfo> ();
        processedSchemas = new ArrayList<String>();
        visitedSchemas = new ArrayList<String>();
        associations = new ArrayList<Association>();
        this.validationInfo = validationInfo;
    }

    public SchemaUriProcessor(RequestContext requestContext, WSDLValidationInfo validationInfo, boolean useOriginalSchema) {
        this(requestContext,validationInfo);
        this.useOriginalSchema = useOriginalSchema;
    }

    /* Save the schema, schema imports, associations in the registry. Intended to used by the XSD Media-type handler
       only. 
     */
    public String importSchemaToRegistry(RequestContext requestContext,
                                         String resourcePath,
                                         String commonLocation,
                                         String sourceURL) throws RegistryException {
        resourceName = resourcePath.substring(resourcePath.lastIndexOf(RegistryConstants.PATH_SEPARATOR) + 1);

        XmlSchemaCollection xmlSchemaCollection = new XmlSchemaCollection();
        xmlSchemaCollection.setBaseUri(sourceURL);
        baseURI = sourceURL;
        InputSource inputSource = new InputSource(sourceURL);

        try {
            // Here we assue schema is correct. Schema validation is beyond our scope, so we don't
            // bother with a ValidationEventHandler.
            XmlSchema xmlSchema = xmlSchemaCollection.read(inputSource, null);
            evaluateSchemasRecursively(xmlSchema, null, false, true);
        } catch (RuntimeException re) {
            String msg = "Could not read the XML Schema Definition file. ";
            if (re.getCause() instanceof org.apache.ws.commons.schema.XmlSchemaException) {
                msg += re.getCause().getMessage();
                log.error(msg, re);
                throw new RegistryException(msg);
            }
            throw new RegistryException(msg, re);
        }
        updateSchemaPaths(commonLocation);

        updateSchemaInternalsAndAssociations();

        Resource metaResource = requestContext.getResource();
        String path = saveSchemaToRegistry(resourcePath, metaResource); // should depend on the central location / relative location flag
        persistAssociations(path);
        return path;
    }


    public void evaluateSchemas(
            Types types,
            String wsdlDocumentBaseURI,
            ArrayList<String> dependencies) throws RegistryException {
        baseURI = wsdlDocumentBaseURI;
        /* evaluating schemas found under wsdl:types tag in a wsdl */
        if (types != null) {
            List extensibleElements = types.getExtensibilityElements();
            Schema schema;
            Object extensionObject;
            XmlSchema xmlSchema;
            XmlSchemaCollection xmlSchemaCollection;
            wsdlDocumentBaseURI = wsdlDocumentBaseURI.substring(0, wsdlDocumentBaseURI.lastIndexOf("/") + 1);
            for (Object extensibleElement : extensibleElements) {
                extensionObject = extensibleElement;
                if (extensionObject instanceof Schema) {
                    schema = (Schema)extensionObject;
                    if (schema.getImports().size() > 0) {
                        SchemaImportImpl schemaImport =
                                (SchemaImportImpl) ((Vector) schema.getImports().values()
                                        .toArray()[0]).firstElement();
                        if (schemaImport.getReferencedSchema() != null) {
                            // already added imported xsd
                            CommonUtil.addImportedArtifact(
                                    new File(schemaImport.getReferencedSchema().getDocumentBaseURI()).toString());
                        }
                    }
                    xmlSchemaCollection = new XmlSchemaCollection();
                    /* setting base URI in the collection to load relative schemas */
                    xmlSchemaCollection.setBaseUri(wsdlDocumentBaseURI);
                    xmlSchema = xmlSchemaCollection.read(schema.getElement());
                    evaluateSchemasRecursively(xmlSchema, dependencies, true, false);
                }
            }
        }
    }

    private void evaluateSchemasRecursively(
            XmlSchema xmlSchema,
            ArrayList<String> dependencies,
            boolean isWSDLInlineSchema, boolean isMasterSchema) throws RegistryException {
        // first process the imports and includes
        XmlSchemaObjectCollection includes = xmlSchema.getIncludes();
        SchemaInfo schemaInfo = new SchemaInfo();
        schemaInfo.setMasterSchema(isMasterSchema);
        // set this as an visited schema to stop infinite traversal
        visitedSchemas.add(xmlSchema.getSourceURI());
        if (includes != null) {
            Object externalComponent;
            XmlSchemaExternal xmlSchemaExternal;
            XmlSchema innerSchema;
            for (Iterator iter = includes.getIterator(); iter.hasNext();) {
                externalComponent = iter.next();
                if (externalComponent instanceof XmlSchemaExternal) {
                    xmlSchemaExternal = (XmlSchemaExternal)externalComponent;
                    innerSchema = xmlSchemaExternal.getSchema();
                    if (innerSchema != null) {
                        String sourceURI = innerSchema.getSourceURI();
                        if (isWSDLInlineSchema) {
                            dependencies.add(sourceURI);
                        }
                        else {
                            schemaInfo.getSchemaDependencies().add(sourceURI);
                        }

                        if (!visitedSchemas.contains(sourceURI)) {
                            evaluateSchemasRecursively(
                                    innerSchema,
                                    null,   /* passing null is safe since we are passing isWSDLSchema = false */
                                    false, false); /* ignore inline schema and proceed with included ones */
                        }
                    }
                }
            }
        }

        if (!isWSDLInlineSchema) {
            // after processing includes and imports save the xml schema
            String sourceURI = xmlSchema.getSourceURI();
            String fileNameToSave;
            if (isMasterSchema) {
                fileNameToSave = extractResourceFromURL(resourceName, ".xsd");
            } else {
                fileNameToSave = extractResourceFromURL(sourceURI.substring(sourceURI.lastIndexOf(RegistryConstants.PATH_SEPARATOR) + 1), ".xsd");
            }

            fileNameToSave = fileNameToSave.replace("?xsd=", ".");
            String originalName = fileNameToSave;
            while (processedSchemas.contains(fileNameToSave)) {
                fileNameToSave = fileNameToSave.substring(0, fileNameToSave.indexOf(".")) + ++i + ".xsd";
            }

            // If this was the master schema, and we already have a schema by that name, simply swap
            // proposed names. But in the process, validate whether the schema that already exists
            // on the list is not a master schema, and thereby avoid any recursion.
            if (schemaInfo.isMasterSchema() && !originalName.equals(fileNameToSave)) {
                for (SchemaInfo schema : schemas.values()) {
                    if (schema.getProposedResourceName().equals(originalName)) {
                        if (!schema.isMasterSchema()) {
                            schema.setProposedResourceName(fileNameToSave);
                            fileNameToSave = originalName;
                        }
                        break;
                    }
                }
            }
            // add this entry to the processed schema map
            processedSchemas.add(fileNameToSave);
            //schemaInfo.setProposedRegistryURL(fileNameToSave);
            schemaInfo.setProposedResourceName(fileNameToSave);
            schemaInfo.setSchema((xmlSchema));
            schemaInfo.setOriginalURL(sourceURI);
            schemas.put(getAbsoluteSchemaURL(sourceURI), schemaInfo);
        }
    }

    public String getSchemaRegistryPath(String parentRegistryPath, String sourceURL) throws RegistryException {
        SchemaInfo schemaInfo = schemas.get(getAbsoluteSchemaURL(sourceURL));
        if (schemaInfo != null) {
            //return WSDLUtil.getLocationPrefix(parentRegistryPath) + schemaInfo.getProposedRegistryURL();
            return WSDLUtil.computeRelativePathWithVersion(parentRegistryPath, schemaInfo.getProposedRegistryURL(), systemRegistry);
        }
        return null;
    }

    public String getSchemaAssociationPath(String sourceURL) {
        SchemaInfo schemaInfo = schemas.get(sourceURL);
        if (schemaInfo != null) {
            String proposedRegistryURL = schemaInfo.getProposedRegistryURL();
            return proposedRegistryURL.replaceAll("\\.\\./", "");
        }
        return null;
    }

    /**
     * For each schema found in schemas, change corresponding schemaInfo's ProposedRegistryURL based on
     * commonSchemaLocation and mangled targetNamespace
     * @param commonSchemaLocation the location to store schemas
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException if the operation failed.
     */
    private void updateSchemaPaths(String commonSchemaLocation) throws RegistryException {
        /* i.e. ROOT/commonSchemaLocation */
        if (!systemRegistry.resourceExists(commonSchemaLocation)) {
            systemRegistry.put(commonSchemaLocation, systemRegistry.newCollection());
        }
        for (SchemaInfo schemaInfo: schemas.values()) {
            XmlSchema schema = schemaInfo.getSchema();
            String targetNamespace = schema.getTargetNamespace();
            if ((targetNamespace == null) || ("".equals(targetNamespace))) {
                targetNamespace = "unqualified";
            }
            String schemaLocation = commonSchemaLocation + schemaInfo.getProposedResourceName();
            schemaInfo.setProposedRegistryURL(schemaLocation);
        }
    }
    private void updateSchemaPaths(String commonSchemaLocation,String version,List dependencies) throws RegistryException {
        /* i.e. ROOT/commonSchemaLocation */
        if (!systemRegistry.resourceExists(commonSchemaLocation)) {
            systemRegistry.put(commonSchemaLocation, systemRegistry.newCollection());
        }
        outerLoop:
        for (SchemaInfo schemaInfo: schemas.values()) {
            XmlSchema schema = schemaInfo.getSchema();
            String targetNamespace = schema.getTargetNamespace();
            if ((targetNamespace == null) || ("".equals(targetNamespace))) {
                targetNamespace = "unqualified";
            }

            String schemaLocation = (commonSchemaLocation +
                    CommonUtil.derivePathFragmentFromNamespace(targetNamespace)).replace("//", "/");
            String regex =  schemaLocation + "[\\d].[\\d].[\\d]" + RegistryConstants.PATH_SEPARATOR
                    + schemaInfo.getProposedResourceName();

            for (Object dependency : dependencies) {
                String path = dependency.toString();
                if(path.matches(regex)){
                    schemaLocation = path;
                    schemaInfo.setProposedRegistryURL(schemaLocation);
                    continue outerLoop;
                }
            }
            schemaLocation = schemaLocation + version + RegistryConstants.PATH_SEPARATOR
                    + schemaInfo.getProposedResourceName();
            schemaInfo.setProposedRegistryURL(schemaLocation);



        }
    }

    /**
     * Update the schema's internal import location according to the new registry URLs.
     * Furthermore, fill the associations arraylist according to the detectored associations.
     *
     * @throws RegistryException if the operation fails
     */
    private void updateSchemaInternalsAndAssociations() throws RegistryException {
        for (SchemaInfo schemaInfo: schemas.values()) {
            XmlSchema schema = schemaInfo.getSchema();
            XmlSchemaObjectCollection includes = schema.getIncludes();
            if (includes != null) {
                for (Iterator iter = includes.getIterator(); iter.hasNext();) {
                    Object externalComponent = iter.next();
                    if (externalComponent instanceof XmlSchemaExternal) {
                        XmlSchemaExternal xmlSchemaExternal = (XmlSchemaExternal)externalComponent;
                        XmlSchema schema1 = xmlSchemaExternal.getSchema();
                        if (schema1 != null) {
                            String sourceURI = getAbsoluteSchemaURL(schema1.getSourceURI());
                            if (schemas.containsKey(sourceURI)) {
                                SchemaInfo info = schemas.get(sourceURI);
                                String relativeSchemaPath =
                                        WSDLUtil.computeRelativePathWithVersion(schemaInfo.getProposedRegistryURL(),
                                                info.getProposedRegistryURL(), registry);
                                xmlSchemaExternal.setSchemaLocation(relativeSchemaPath);
                            }
                        }
                    }
                }
            }

            // creating associations
            for(String associatedTo : schemaInfo.getSchemaDependencies()) {
                SchemaInfo schemaInfoAssociated = schemas.get(associatedTo);
                if (schemaInfoAssociated != null) {
                    associations.add(new Association(schemaInfo.getProposedRegistryURL(),
                            schemaInfoAssociated.getProposedRegistryURL(),
                            CommonConstants.DEPENDS));
                    associations.add(new Association(schemaInfoAssociated.getProposedRegistryURL(),
                            schemaInfo.getProposedRegistryURL(),
                            CommonConstants.USED_BY));
                }
            }
        }
    }

    public String saveSchemasToRegistry(String commonSchemaLocation,
                                        Resource metaResource)
            throws RegistryException {
        updateSchemaPaths(commonSchemaLocation);
        updateSchemaInternalsAndAssociations();
        String path = saveSchemaToRegistry(null, metaResource);
        persistAssociations(path);
        return path;
    }

    public String saveSchemasToRegistry(String commonSchemaLocation,
                                        Resource metaResource,String version,List dependencies)
            throws RegistryException {
        updateSchemaPaths(commonSchemaLocation,version,dependencies);
        updateSchemaInternalsAndAssociations();
        String path = saveSchemaToRegistry(null, metaResource);
        persistAssociations(path);
        return path;
    }

    @SuppressWarnings("unchecked")
    private String saveSchemaToRegistry(String resourcePath,
                                        Resource metaResource)
            throws RegistryException {
        String path = resourcePath;
        for (SchemaInfo schemaInfo: schemas.values()) {
            XmlSchema schema = schemaInfo.getSchema();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            schema.write(byteArrayOutputStream);
            byte[] xsdContent = byteArrayOutputStream.toByteArray();

            String schemaPath = schemaInfo.getProposedRegistryURL();
            Resource xsdResource;
            if (metaResource != null && registry.resourceExists(schemaPath)) {
                xsdResource = registry.get(schemaPath);
            } else {
                xsdResource = new ResourceImpl();
                if (metaResource != null) {
                    xsdResource.setUUID(metaResource.getUUID());
                    Properties properties = metaResource.getProperties();
                    if (properties != null) {
                        List<String> linkProperties = Arrays.asList(
                                RegistryConstants.REGISTRY_LINK,
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
                                xsdResource.setProperty(key, (List<String>) e.getValue());
                            }
                        }
                    }
                }
            }
            xsdResource.setMediaType("application/x-xsd+xml");

            if (this.useOriginalSchema) {
                try {
                    xsdResource.setContent(CarbonUtils.getBytesFromFile(
                            new File(new URI(schemaInfo.getOriginalURL()))));
                } catch (CarbonException e) {
                    String errMsg = "Trying to store original schema in registry failed while " +
                            "generating the content from original schema.";
                    log.error(errMsg, e);
                    throw new RegistryException(errMsg, e);
                } catch (URISyntaxException e) {
                    String errMsg = "Trying to store original schema in registry failed due to error " +
                            "occurred in file url:" + schemaInfo.getOriginalURL();
                    log.error(errMsg, e);
                    throw new RegistryException(errMsg, e);
                }
            } else {
                xsdResource.setContent(xsdContent);
            }

            if(metaResource != null){
                xsdResource.setDescription(metaResource.getDescription());
            }
            String targetNamespace = schema.getTargetNamespace();
            xsdResource.setProperty("targetNamespace", targetNamespace);


            if (schemaInfo.isMasterSchema() && validationInfo != null) {

                ArrayList<String> messages = validationInfo.getValidationMessages();
                if (messages.size() > 0) {
                    xsdResource.setProperty(SCHEMA_STATUS, WSDLUtils.INVALID);
                } else {
                    xsdResource.setProperty(SCHEMA_STATUS, WSDLUtils.VALID);
                }
                int i = 1;
                for (String message : messages) {
                    if (message == null) {
                        continue;
                    }
                    if (message.length() > 1000) {
                        message = message.substring(0, 997) + "...";
                    }
                    xsdResource.setProperty(SCHEMA_VALIDATION_MESSAGE + i, message);
                    i++;
                }
            }
            saveToRepositorySafely(schemaInfo.getOriginalURL(), schemaPath,
                    xsdResource, schemaInfo.isMasterSchema());

            if (schemaInfo.isMasterSchema()) {
                path = schemaPath;
            }
        }
        return path;
    }

    /**
     * Save associations to the registry if they do not exist.
     * Execution time could be improved if registry provides a better way to check existing associations.
     *
     * @param schemaPath path of the schema
     *
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException if the operation fails
     */
    private void persistAssociations(String schemaPath) throws RegistryException {
        // until registry provides a functionality to check existing associations, this method will consume a LOT of time
        for (Association association: associations) {
            boolean isAssociationExist = false;
            Association[] existingAssociations = registry.getAllAssociations(association.getSourcePath());
            if (existingAssociations != null) {
                for (Association currentAssociation: existingAssociations) {
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
        // this code was added to fix CARBON-11188
        if( schemaPath!= null && associations.isEmpty()) {
            Association[] dependencies = registry.getAssociations(schemaPath, CommonConstants.DEPENDS);
            for(Association dependency : dependencies) {
                if(dependency.getSourcePath().equals(schemaPath)) {
                    registry.removeAssociation(dependency.getSourcePath(),
                            dependency.getDestinationPath(), CommonConstants.DEPENDS);
                    registry.removeAssociation(dependency.getDestinationPath(),
                            dependency.getSourcePath(), CommonConstants.USED_BY);
                }
            }
        }
    }

    /**
     * Saves the resource iff the resource is not already existing in the repository
     *
     * @param url url of the resource
     * @param path: resource path
     * @param resource: resource object
     * @param isMasterSchema Whether master schema or not
     *
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException if the operation fails
     */
    private void saveToRepositorySafely(String url, String path,
                                        Resource resource, boolean isMasterSchema) throws RegistryException {

        String schemaId = resource.getUUID();

        if (schemaId == null) {
            // generate a service id
            schemaId = UUID.randomUUID().toString();
            resource.setUUID(schemaId);
        }
//        if (systemRegistry != null) {
//            CommonUtil.addGovernanceArtifactEntryWithAbsoluteValues(systemRegistry, schemaId, path);
//        }

        if (registry.resourceExists(path)) {
            log.debug("A Resource already exists at given location. Overwriting resource content.");
        }

        addSchemaToRegistry(path, url, resource, registry, isMasterSchema);

//        if (!(resource instanceof Collection) &&
//           ((ResourceImpl) resource).isVersionableChange()) {
//            registry.createVersion(path);
//        }
        String relativeArtifactPath = RegistryUtils.getRelativePath(registry.getRegistryContext(), path);
        // adn then get the relative path to the GOVERNANCE_BASE_PATH
        relativeArtifactPath = RegistryUtils.getRelativePathToOriginal(relativeArtifactPath,
                RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH);
        ((ResourceImpl)resource).setPath(relativeArtifactPath);
    }

    /**
     * Method that gets called instructing a schema to be added the registry.
     *
     * @param path     the path to add the resource to.
     * @param url      the path from which the resource was imported from.
     * @param resource the resource to be added.
     * @param registry the registry instance to use.
     * @param isMasterSchema Whether master schema or not
     *
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException if the operation failed.
     */
    protected void addSchemaToRegistry(String path, String url,
                                       Resource resource, Registry registry, boolean isMasterSchema) throws RegistryException {
        String source = getSource(path);
        GenericArtifactManager genericArtifactManager = new GenericArtifactManager(governanceUserRegistry, "uri");

        if(!registry.resourceExists(path)){
            GenericArtifact xsd = genericArtifactManager.newGovernanceArtifact(new QName(source));
            if(isMasterSchema){
                xsd.setId(resource.getUUID());
            }
            xsd.setAttribute("overview_name", source);
            xsd.setAttribute("overview_uri", url);
            xsd.setAttribute("overview_type", HandlerConstants.XSD);
            genericArtifactManager.addGenericArtifact(xsd);
        } else {
            Resource existResource = registry.get(path);
            GenericArtifact artifact = genericArtifactManager.getGenericArtifact(existResource.getUUID());
            if(!artifact.getAttribute("overview_uri").equals(url)){
                throw new RegistryException("Different schema URI already exists in " + path + ".");
            }
        }
    }

    private String extractResourceFromURL(String wsdlURL, String suffix) {
        String resourceName = wsdlURL;
        if (wsdlURL.indexOf("?") > 0) {
            resourceName = wsdlURL.substring(0, wsdlURL.indexOf("?")) + suffix;
        } else if (wsdlURL.indexOf(".") > 0) {
            resourceName = wsdlURL.substring(0, wsdlURL.lastIndexOf(".")) + suffix;
        } else if (!wsdlURL.endsWith(suffix)) {
            resourceName = wsdlURL + suffix;
        }
        return resourceName;
    }

    private String getAbsoluteSchemaURL(String schemaLocation) throws RegistryException {
        if (schemaLocation != null && baseURI != null) {
            try {
                URI uri = new URI(baseURI);
                URI absoluteURI = uri.resolve(schemaLocation);
                return absoluteURI.toString();
            } catch (URISyntaxException e) {
                throw new RegistryException(e.getMessage(), e);
            }
        }
        return schemaLocation;
    }

    public static String getSource(String uri){
        return uri.split("/")[uri.split("/").length -1];
    }

}
