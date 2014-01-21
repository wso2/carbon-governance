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
package org.wso2.carbon.governance.api.schema.dataobjects;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifactImpl;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.utils.RegistryUtils;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * This represents a schema artifact stored on the Registry. Schema artifacts are created as a
 * result of importing or uploading a schema or WSDL, or when a service which has an attached WSDL
 * is created.
 */
public class SchemaImpl extends GovernanceArtifactImpl implements Schema {

    private static final Log log = LogFactory.getLog(SchemaImpl.class);

    private static final String SCHEMA_TARGET_NAMESPACE_ATTRIBUTE = "targetNamespace";
    private QName qname;
    private OMElement schemaElement;
    private String url;

    /**
     * Constructor accepting resource path, identifier and a registry instance.
     * This constructor should be used only when the wsdl already saved in the registry.
     *
     * @param path     the resource path.
     * @param id       the resource identifier.
     * @param registry the registry instance.
     *
     * @throws GovernanceException if the construction fails.
     */
    public SchemaImpl(String id, Registry registry) throws GovernanceException {
        super(id);
        associateRegistry(registry);
        loadSchemaDetails();
    }

    /**
     * Constructor accepting resource identifier and the schema URL.
     *
     * @param id  the resource identifier.
     * @param url the schema URL.
     */
    public SchemaImpl(String id, String url) {
        super(id);
        this.url = url;
    }

    public QName getQName() {
        return qname;
    }

    /**
     * Method to obtain the schema element of this schema artifact.
     *
     * @return the schema element.
     */
    @Override
    public OMElement getSchemaElement() {
        return schemaElement;
    }

    /**
     * Method to set the schema element of this schema artifact.
     *
     * @param schemaElement the schema element.
     */
    @Override
    @SuppressWarnings("unused")
    public void setSchemaElement(OMElement schemaElement) {
        this.schemaElement = schemaElement;
    }

    /**
     * Method to obtain the schema URL.
     *
     * @return the schema URL.
     */
    public String getUrl() {
        return url;
    }

    /**
     * Method to load the schema details into this artifact.
     *
     * @throws GovernanceException if the operation failed.
     */
    public void loadSchemaDetails() throws GovernanceException {
        checkRegistryResourceAssociation();
        Registry registry = getAssociatedRegistry();
        String path = getPath();
        String id = getId();
        Resource resource;
        try {
            resource = registry.get(path);
            byte[] content = (byte[]) resource.getContent();
            schemaElement = GovernanceUtils.buildOMElement(content);

        } catch (RegistryException e) {
            String msg =
                    "Error in getting the content for the artifact. artifact id: " + id + ", " +
                            "path: " + path + ".";
            log.error(msg);
            throw new GovernanceException(msg, e);
        }
        // get the target namespace.
        String fileName = RegistryUtils.getResourceName(path);
        String namespaceURI =
                schemaElement.getAttributeValue(new QName(SCHEMA_TARGET_NAMESPACE_ATTRIBUTE));
        qname = new QName(namespaceURI, fileName);

        // and then iterate all the properties and add.
        Properties properties = resource.getProperties();
        if (properties != null) {
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
                }
            }
        }
    }
}
