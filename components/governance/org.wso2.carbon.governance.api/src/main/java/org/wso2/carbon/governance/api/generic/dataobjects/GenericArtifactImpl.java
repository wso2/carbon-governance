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
package org.wso2.carbon.governance.api.generic.dataobjects;

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifactImpl;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.utils.RegistryUtils;

import javax.xml.namespace.QName;
import java.lang.String;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a generic governance artifact.
 */
public class GenericArtifactImpl extends GovernanceArtifactImpl implements GenericArtifact {

    private QName qName;
    private String mediaType;
    private byte[] content;

    /**
     * Copy constructor used for cloning.
     *
     * @param artifact the object to be copied.
     */
    protected GenericArtifactImpl(GovernanceArtifact artifact, String mediaType) {
        super((GovernanceArtifactImpl)artifact);
        this.qName = artifact.getQName();
        setArtifactPath(((GovernanceArtifactImpl) artifact).getArtifactPath());
        setLcName(((GovernanceArtifactImpl) artifact).getLcName());
        setLcState(((GovernanceArtifactImpl) artifact).getLcState());
        this.mediaType = mediaType;
    }

    protected GenericArtifactImpl(GovernanceArtifact artifact, String mediaType, List<String> uniqueAttributes) {
        super((GovernanceArtifactImpl)artifact, uniqueAttributes);
        this.qName = artifact.getQName();
        setArtifactPath(((GovernanceArtifactImpl) artifact).getArtifactPath());
        setLcName(((GovernanceArtifactImpl) artifact).getLcName());
        setLcState(((GovernanceArtifactImpl) artifact).getLcState());
        this.mediaType = mediaType;
    }

    /**
     * Constructor accepting resource identifier and the qualified name.
     *
     * @param id    the resource identifier.
     * @param qName the qualified name.
     */
    public GenericArtifactImpl(String id, QName qName, String mediaType) {
        super(id);
        this.qName = qName;
        this.mediaType = mediaType;
    }

    public GenericArtifactImpl(String id, QName qName, String mediaType, List<String> uniqueAttributes) {
        this(id, qName, mediaType);
        setUniqueAttributes(uniqueAttributes);
    }


    public GenericArtifactImpl(QName qName, String mediaType) {
        super(UUID.randomUUID().toString());
        this.qName = qName;
        this.mediaType = mediaType;
    }

    public GenericArtifactImpl(QName qName, String mediaType, List<String> uniqueAttributes) {
        this(qName, mediaType);
        setUniqueAttributes(uniqueAttributes);
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    /**
     * Method to load the details into this artifact.
     *
     * @throws GovernanceException if the operation failed.
     */
    public void loadDetails() throws GovernanceException {
        checkRegistryResourceAssociation();
        Registry registry = getAssociatedRegistry();
        String path = getPath();
        String id = getId();
        Resource resource;
        try {
            resource = registry.get(path);
            this.content = (byte[]) resource.getContent();
            this.mediaType = resource.getMediaType();
        } catch (RegistryException e) {
            throw new GovernanceException("Error in getting the qualified name for the artifact. " +
                    "artifact id: " + id + ", " + "path: " + path + ".", e);
        }
        // get the target namespace.
        String fileName = RegistryUtils.getResourceName(path);
        this.qName = new QName(null, fileName);

        // and then iterate all the properties and add.
        Properties properties = resource.getProperties();
        if (properties != null) {
            Set keySet = properties.keySet();
            if (keySet != null) {
                for (Object keyObj : keySet) {
                    String key = (String) keyObj;
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

    /**
     * Constructor accepting resource identifier and the artifact content.
     *
     * @param id                         the resource identifier.
     * @param artifactContentElement     an XML element containing the content.
     * @param artifactNameAttribute      the attribute that specifies the name of the artifact.
     * @param artifactNamespaceAttribute the attribute that specifies the namespace of the artifact.
     * @param artifactElementNamespace   the attribute that specifies the artifact element's
     *                                   namespace.
     *
     * @throws GovernanceException if the construction fails.
     */
    public GenericArtifactImpl(String id, OMElement artifactContentElement,
                               String artifactNameAttribute,
                               String artifactNamespaceAttribute,
                               String artifactElementNamespace,
                               String mediaType) throws GovernanceException {
        super(id, artifactContentElement);
        String name = GovernanceUtils.getAttributeValue(artifactContentElement,
                artifactNameAttribute, artifactElementNamespace);
        String namespace = (artifactNamespaceAttribute != null) ?
                GovernanceUtils.getAttributeValue(artifactContentElement,
                        artifactNamespaceAttribute, artifactElementNamespace) : null;
        if (name != null && !name.equals("")) {
            this.qName = new QName(namespace, name);
        }
        this.mediaType = mediaType;
    }

    /**
     * Constructor accepting resource identifier and the artifact content.
     *
     * @param id                         the resource identifier.
     * @param registry                   the registry instance to fetch artifact content.
     *
     * @throws GovernanceException if the construction fails.
     */
    public GenericArtifactImpl(String id, Registry registry) throws GovernanceException {
        super(id);
        associateRegistry(registry);
        loadDetails();
    }

    public QName getQName() {
        return qName;
    }

    public String getMediaType() {
        return mediaType;
    }

    /**
     * Method to set the qualified name of this artifact.
     *
     * @param qName the qualified name.
     *
     * @throws GovernanceException if the operation failed.
     */
    public void setQName(QName qName) {
        // the path will be synced with the qualified name
        this.qName = qName;
    }

}
