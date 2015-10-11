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
package org.wso2.carbon.governance.api.generic;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.api.common.GovernanceArtifactManager;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.dataobjects.DetachedGenericArtifactImpl;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifactImpl;
import org.wso2.carbon.governance.api.util.GovernanceArtifactConfiguration;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.common.TermData;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Manager class for a generic governance artifact.
 */
@SuppressWarnings("unused")
public class GenericArtifactManager {

    private String artifactNameAttribute;
    private String artifactNamespaceAttribute;
    private String artifactElementNamespace;
    private GovernanceArtifactManager manager;
    private String mediaType;

    private static final Log log = LogFactory.getLog(GenericArtifactManager.class);

    /**
     * Constructor accepting an instance of the registry, and also details on the type of manager.
     *
     * @param registry                   the instance of the registry.
     * @param mediaType                  the media type of resources being saved or fetched.
     * @param artifactNameAttribute      the attribute that specifies the name of the artifact.
     * @param artifactNamespaceAttribute the attribute that specifies the namespace of the artifact.
     * @param artifactElementRoot        the attribute that specifies the root artifact element.
     * @param artifactElementNamespace   the attribute that specifies the artifact element's
     *                                   namespace.
     * @param pathExpression             the expression that can be used to compute where to store
     *                                   the artifact.
     * @param relationshipDefinitions    the relationship definitions for the types of associations
     *                                   that will be created when the artifact gets updated.
     */
    public GenericArtifactManager(Registry registry, String mediaType,
                                     String artifactNameAttribute,
                                     String artifactNamespaceAttribute, String artifactElementRoot,
                                     String artifactElementNamespace, String pathExpression,
                                     Association[] relationshipDefinitions) {
        manager = new GovernanceArtifactManager(registry, mediaType, artifactNameAttribute,
                artifactNamespaceAttribute, artifactElementRoot, artifactElementNamespace,
                pathExpression, relationshipDefinitions);
        this.artifactNameAttribute = artifactNameAttribute;
        this.artifactNamespaceAttribute = artifactNamespaceAttribute;
        this.artifactElementNamespace = artifactElementNamespace;
        this.mediaType = mediaType;
    }

    /**
     * Constructor accepting an instance of the registry, and key identifying the type of manager.
     *
     * @param registry              the instance of the registry.
     * @param key                   the key short name of the artifact type.
     * @throws RegistryException    Thrown when rxt configuration is not in registry.
     */
    public GenericArtifactManager(Registry registry, String key) throws RegistryException {
        try {
            GovernanceArtifactConfiguration configuration =
                    GovernanceUtils.findGovernanceArtifactConfiguration(key, registry);
            if (configuration != null) {
                artifactNameAttribute = configuration.getArtifactNameAttribute();
                artifactNamespaceAttribute = configuration.getArtifactNamespaceAttribute();
                artifactElementNamespace = configuration.getArtifactElementNamespace();
                manager = new GovernanceArtifactManager(registry, configuration.getMediaType(),
                        artifactNameAttribute, artifactNamespaceAttribute,
                        configuration.getArtifactElementRoot(), artifactElementNamespace,
                        configuration.getPathExpression(), configuration.getLifecycle(),
                        configuration.getValidationAttributes(), configuration.getRelationshipDefinitions());
                mediaType = configuration.getMediaType();
            } else {
                String message = "Artifact type '" + key
                        + "' is not in registry or unable to find relevant configuration.";
                log.error(message);
                throw new GovernanceException(message);
            }
        } catch (RegistryException e) {
            String message = "Unable to obtain governance artifact configuration for rxt: " + key;
            log.error(message, e);
            throw new GovernanceException(message, e);
        }
    }

    /**
     * Creates a new artifact from the given qualified name.
     *
     * @param qName the qualified name of this artifact.
     *
     * @return the artifact added.
     * @throws GovernanceException if the operation failed.
     */
    public GenericArtifact newGovernanceArtifact(QName qName) throws GovernanceException {
        GenericArtifactImpl genericArtifact =
                new GenericArtifactImpl(manager.newGovernanceArtifact(), mediaType) {};
        genericArtifact.setQName(qName);
        return genericArtifact;
    }

    /**
     * Creates a new artifact from the given qualified name.
     *
     * @param qName the qualified name of this artifact.
     *
     * @return the artifact added.
     * @throws GovernanceException if the operation failed.
     */
    public GenericArtifact newGovernanceArtifact(QName qName, byte[] content) throws GovernanceException {
        GenericArtifact genericArtifact = newGovernanceArtifact(qName);
        genericArtifact.setContent(content);
        return genericArtifact;
    }

    /**
     * Creates a new artifact from the given content.
     *
     * @param content the artifact content.
     *
     * @return the artifact added.
     * @throws GovernanceException if the operation failed.
     */
    public GenericArtifact newGovernanceArtifact(OMElement content)
            throws GovernanceException {
        GenericArtifactImpl genericArtifact =
                new GenericArtifactImpl(manager.newGovernanceArtifact(content), mediaType) {};
        String name = GovernanceUtils.getAttributeValue(content,
                artifactNameAttribute, artifactElementNamespace);
        String namespace = (artifactNamespaceAttribute != null) ?
                GovernanceUtils.getAttributeValue(content,
                        artifactNamespaceAttribute, artifactElementNamespace) : null;
        if (name != null && !name.equals("")) {
            genericArtifact.setQName(new QName(namespace, name));
            //This is to fix the REGISTRY-1603.
//        } else {
//            throw new GovernanceException("Unable to compute QName from given XML payload, " +
//                    "please ensure that the content passed in matches the configuration.");
        }
        return genericArtifact;
    }


    /**
     * Creates a new artifact from the given string content.
     *
     * @param omContent the artifact content in string
     *
     * @return the artifact added.
     * @throws GovernanceException if the operation failed.
     */
    public GenericArtifact newGovernanceArtifact(String omContent) throws GovernanceException {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.IS_COALESCING, true);

        try {
            XMLStreamReader reader = factory.createXMLStreamReader(new StringReader(omContent));
            GenericArtifact artifact = this.newGovernanceArtifact(new StAXOMBuilder(reader).getDocumentElement());
            artifact.setContent(omContent.getBytes());

            return artifact;
        } catch (XMLStreamException e) {
            String message = "Error in creating the content from the parameters.";
            log.error(message, e);
            throw new GovernanceException(message, e);
        }
    }

    /**
     * Adds the given artifact to the registry. Please do not use this method to update an
     * existing artifact use the update method instead. If this method is used to update an existing
     * artifact, all existing properties (such as lifecycle details) will be removed from the
     * existing artifact.
     *
     * @param artifact the artifact.
     *
     * @throws GovernanceException if the operation failed.
     */
    public void addGenericArtifact(GenericArtifact artifact) throws GovernanceException {
         manager.addGovernanceArtifact(artifact);
    }

    /**
     * Updates the given artifact on the registry.
     *
     * @param artifact the artifact.
     *
     * @throws GovernanceException if the operation failed.
     */
    public void updateGenericArtifact(GenericArtifact artifact) throws GovernanceException {
        manager.updateGovernanceArtifact(artifact);
    }

    /**
     * Fetches the given artifact on the registry.
     *
     * @param artifactId the identifier of the artifact.
     *
     * @return the artifact.
     * @throws GovernanceException if the operation failed.
     */
    public GenericArtifact getGenericArtifact(String artifactId) throws GovernanceException {
        GovernanceArtifact governanceArtifact = manager.getGovernanceArtifact(artifactId);
        if (governanceArtifact == null) {
            return null;
        }
        if (governanceArtifact instanceof GenericArtifactImpl &&
                ((GenericArtifactImpl)governanceArtifact).getMediaType() != null &&
                !mediaType.equals(((GenericArtifactImpl)governanceArtifact).getMediaType())) {
            // A wrong artifact manager has been used to retrieve the artifact. Fix for REGISTRY-2064.
            return null;
        }
        return new GenericArtifactImpl(governanceArtifact, mediaType) {};
    }

    /**
     * Removes the given artifact from the registry.
     *
     * @param artifactId the identifier of the artifact.
     *
     * @throws GovernanceException if the operation failed.
     */
    public void removeGenericArtifact(String artifactId) throws GovernanceException {
        manager.removeGovernanceArtifact(artifactId);
    }

    /**
     * Finds all artifacts matching the given filter criteria.
     *
     * @param criteria the filter criteria to be matched.
     *
     * @return the artifacts that match.
     * @throws GovernanceException if the operation failed.
     */
    public GenericArtifact[] findGenericArtifacts(Map<String, List<String>> criteria)
            throws GovernanceException {
        return getGenericArtifacts(manager.findGovernanceArtifacts(criteria));
    }

    /**
     * Find all possible terms and its count for the given facet field and query criteria
     * @param criteria the filter criteria to be matched
     * @param facetField field used for faceting
     * @param authRequired authorization required flag
     * @return term results
     * @throws GovernanceException
     */
    public TermData[] getTermData(Map<String, List<String>> criteria, String facetField, boolean authRequired) throws GovernanceException {
        return manager.getTermData(criteria, facetField, authRequired);
    }

    /**
     * Finds and returns GenericArtifact instances matching the search query
     *
     * @param query The query string that needs to be searched for
     * @return The GenericArtifact list that matching the query
     * @throws GovernanceException if the operation failed
     */
    public GenericArtifact[] findGovernanceArtifacts(String query) throws GovernanceException {
        return getGenericArtifacts(manager.findGovernanceArtifacts(query));
    }

    /**
     * Finds all artifacts matching the given filter criteria.
     *
     * @param criteria the filter criteria to be matched.
     *
     * @return the artifacts that match.
     * @throws GovernanceException if the operation failed.
     */
    public GenericArtifact[] findGenericArtifacts(GenericArtifactFilter criteria)
            throws GovernanceException {
        List<GenericArtifact> artifacts = new ArrayList<GenericArtifact>();
        for (GenericArtifact artifact : getAllGenericArtifacts()) {
            if (artifact != null) {
                if (criteria.matches(artifact)) {
                    artifacts.add(artifact);
                }
            }
        }
        return artifacts.toArray(new GenericArtifact[artifacts.size()]);
    }

    /**
     * Finds all artifacts on the registry.
     *
     * @return all artifacts on the registry.
     * @throws GovernanceException if the operation failed.
     */
    public GenericArtifact[] getAllGenericArtifacts() throws GovernanceException {
        return getGenericArtifacts(manager.getAllGovernanceArtifacts());
    }

    /**
     * Retrieve all the generic artifacts which associated with the given lifecycle
     *
     * @param lcName Name of the lifecycle
     *
     * @return GenericArtifact array
     * @throws GovernanceException
     */
    public GenericArtifact[] getAllGenericArtifactsByLifecycle(String lcName) throws GovernanceException {
        return getGenericArtifacts(manager.getAllGovernanceArtifactsByLifecycle(lcName));
    }

    /**
     * Retrieve all the generic artifacts which associated with the given lifecycle in the given lifecycle state
     *
     * @param lcName  Name of the lifecycle
     * @param lcState Name of the current lifecycle state
     *
     * @return GenericArtifact array
     * @throws GovernanceException
     */
    public GenericArtifact[] getAllGenericArtifactsByLifecycleStatus(String lcName, String lcState) throws GovernanceException {
        return getGenericArtifacts(manager.getAllGovernanceArtifactsByLIfecycleStatus(lcName, lcState));
    }

    // Method to obtain artifacts from governance artifacts.
    private GenericArtifact[] getGenericArtifacts(GovernanceArtifact[] governanceArtifacts) {
        List<GenericArtifact> artifacts =
                new ArrayList<GenericArtifact>(governanceArtifacts.length);
        for (GovernanceArtifact governanceArtifact : governanceArtifacts) {
            if(governanceArtifact != null) {
                artifacts.add(new GenericArtifactImpl(governanceArtifact, mediaType) {});
            }
        }
        return artifacts.toArray(new GenericArtifact[artifacts.size()]);
    }

    /**
     * Finds all identifiers of the artifacts on the registry.
     *
     * @return an array of identifiers of the artifacts.
     * @throws GovernanceException if the operation failed.
     */
    public String[] getAllGenericArtifactIds() throws GovernanceException {
        return manager.getAllGovernanceArtifactIds();
    }


    public static GenericArtifact newDetachedGovernanceArtifact(QName artifactName,String mediaType ){
        return new DetachedGenericArtifactImpl(artifactName ,mediaType);
    }

    /**
     * Check whether GovernanceArtifact is exists in the Registry without loading whole artifact into memory.
     * This method only work for Configurable Governance Artifacts and doe not work for Content Artifacts such
     * as WSDL, WADL, Swagger, XMLSchema etc.
     *
     * @param artifact GovernanceArtifact to check it's existence.
     * @return true or false
     * @throws GovernanceException if the operation failed.
     */
    public boolean isExists(GovernanceArtifact artifact) throws GovernanceException {
        return manager.isExists(artifact);
    }

    public void removeGenericArtifact(GenericArtifact artifact) throws GovernanceException {
        manager.removeGenericArtifact(artifact);
    }

}
