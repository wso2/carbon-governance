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
package org.wso2.carbon.governance.api.common.dataobjects;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMText;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.api.common.util.ApproveItemBean;
import org.wso2.carbon.governance.api.common.util.CheckListItemBean;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.util.GovernanceConstants;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Governance Artifact abstract class, This is overwritten by Endpoint, Policy, Schema, Service,
 * WSDL, People classes. This keeps common methods shared by all the governance artifacts
 */
@SuppressWarnings("unused")
public abstract class GovernanceArtifactImpl implements GovernanceArtifact {

    private static final Log log = LogFactory.getLog(GovernanceArtifactImpl.class);

    private String id;
    private String path;
    private Registry registry; // associated registry

    private String lcName;
    private String lcState;
    private String artifactPath;
    private List<String> uniqueAttributes;


    public List<String> getUniqueAttributes() {
        return uniqueAttributes;
    }

    public void setUniqueAttributes(List<String> uniqueAttributes) {
        this.uniqueAttributes = uniqueAttributes;
    }

    public String getLcName() {
        return lcName;
    }

    public void setLcName(String lcName) {
        this.lcName = lcName;
    }

    public String getLcState() {
        return lcState;
    }

    public void setLcState(String lcState) {
        this.lcState = lcState;
    }

    public String getArtifactPath() {
        return artifactPath;
    }

    public void setArtifactPath(String artifactPath) {
        this.artifactPath = artifactPath;
    }

    /**
     * Map of attributes associated with this governance artifact.
     */
    protected Map<String, List<String>> attributes = new HashMap<String, List<String>>();

    /**
     * Map of properties associated with this governance artifact.
     */
    protected Map<String, List<String>> properties = new HashMap<String, List<String>>();

    /**
     * Construct a governance artifact object from the path and the id.
     *
     * @param id the id
     */
    public GovernanceArtifactImpl(String id) {
        this.id = id;
    }

    /**
     * Construct a governance artifact. The default constructor.
     */
    public GovernanceArtifactImpl() {
        // the default constructor
    }

    /**
     * Copy constructor used for cloning.
     *
     * @param artifact the object to be copied.
     */
    protected GovernanceArtifactImpl(GovernanceArtifactImpl artifact) {
        if (artifact != null) {
            this.attributes = artifact.attributes;
            this.properties = artifact.properties;
            this.lcName = artifact.lcName;
            this.lcState = artifact.lcState;
            this.uniqueAttributes = artifact.uniqueAttributes;
//            if (artifact.checkListItemBeans != null) {
//                this.checkListItemBeans = Arrays.copyOf(artifact.checkListItemBeans, artifact.checkListItemBeans.length);
//            }
//            if (artifact.approveItemBeans != null) {
//                this.approveItemBeans = Arrays.copyOf(artifact.approveItemBeans, artifact.approveItemBeans.length);
//            }
            this.artifactPath = artifact.artifactPath;
            try {
                associateRegistry(artifact.getAssociatedRegistry());
            } catch (GovernanceException ignored) {
            }
            setId(artifact.getId());
        }
    }

    protected GovernanceArtifactImpl(GovernanceArtifactImpl artifact,  List<String> uniqueAttributes) {
       this(artifact);
       setUniqueAttributes(uniqueAttributes);
    }

    /**
     * Constructor accepting resource identifier and the XML content.
     *
     * @param id             the resource identifier.
     * @param contentElement an XML element containing the content.
     * @throws GovernanceException if the construction fails.
     */
    public GovernanceArtifactImpl(String id, OMElement contentElement) throws GovernanceException {
        this(id);
        serializeToAttributes(contentElement, null);
    }

    public GovernanceArtifactImpl(String id, OMElement contentElement, List<String> uniqueAttributes) throws GovernanceException {
        this(id, contentElement);
        setUniqueAttributes(uniqueAttributes);
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
        	if(!contentElement.getChildElements().hasNext()){
        		addAttribute(parentAttributeName, null);
        	}
        }
        
    }

    public static GovernanceArtifactImpl create(final Registry registry, final String artifactId)
            throws GovernanceException {
        return new GovernanceArtifactImpl(artifactId) {
            {
                associateRegistry(registry);
            }

            public QName getQName() {
                return null;
            }
        };
    }

    public static GovernanceArtifactImpl create(final Registry registry, final String artifactId,
                                                final OMElement content) throws GovernanceException {
        return new GovernanceArtifactImpl(artifactId, content) {
            {
                associateRegistry(registry);
            }

            public QName getQName() {
                return null;
            }
        };
    }

    public static GovernanceArtifactImpl create(final Registry registry, final String artifactId,
                                                final List<String> uniqueAttributes)
                                                throws GovernanceException {
        GovernanceArtifactImpl artifact = create(registry, artifactId);
        artifact.setUniqueAttributes(uniqueAttributes);
        return artifact;
    }

    public static GovernanceArtifactImpl create(final Registry registry, final String artifactId,
                                                final OMElement content, List<String> uniqueAttributes)
                                                throws GovernanceException {
        GovernanceArtifactImpl artifact = create(registry, artifactId, content);
        artifact.setUniqueAttributes(uniqueAttributes);
        return artifact;
    }

    /**
     * Returns the id of the artifact
     *
     * @return the id
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     * Set the id
     *
     * @param id the id
     */
    @Override
    public void setId(String id) {
        this.id = id;
    }


    /**
     * Returns the path of the artifact, need to save the artifact before
     * getting the path.
     *
     * @return here we return the path of the artifact.
     * @throws GovernanceException if an error occurred.
     */
    @Override
    public String getPath() throws GovernanceException {
        if (path == null) {
            path = GovernanceUtils.getArtifactPath(registry, id);
        }
        return path;
    }

    public String getMediaType() {
        if(path == null) {
            try {
                path = getPath();
            } catch (GovernanceException ex) {
                log.error("An error occurred while obtaining the path of artifact " + ex);
                return null;
            }
        }
        if(path != null) {
            try {
                if (registry.resourceExists(path)) {
                    return registry.get(path).getMediaType();
                }
            } catch(RegistryException ex) {
                log.error("An error occurred obtaining the media type of the artifact " + ex);
            }
        }
        return null;
    }

    /**
     * Returns the name of the lifecycle associated with this artifact.
     *
     * @return the name of the lifecycle associated with this artifact.
     * @throws GovernanceException if an error occurred.
     */
    @Override
    public String getLifecycleName() throws GovernanceException {
        String path = getPath();
        if (path != null) {
            try {
                if (!registry.resourceExists(path)) {
                    String msg =
                            "The artifact is not added to the registry. Please add the artifact " +
                                    "before reading lifecycle information.";
                    log.error(msg);
                    throw new GovernanceException(msg);
                }
                return registry.get(path).getProperty("registry.LC.name");
            } catch (RegistryException e) {
                String msg = "Error in obtaining lifecycle name for the artifact. id: " + id +
                        ", path: " + path + ".";
                log.error(msg, e);
                throw new GovernanceException(msg, e);
            }
        }
        return null;
    }

    /**
     * Returns the name of the lifecycle associated with this artifact.
     *
     * @return the name of the lifecycle associated with this artifact.
     * @throws GovernanceException if an error occurred.
     */
    @Override
    public String[] getLifecycleNames() throws GovernanceException {
        String path = getPath();
        if (path != null) {
            try {
                if (!registry.resourceExists(path)) {
                    String msg =
                            "The artifact is not added to the registry. Please add the artifact " +
                                    "before reading lifecycle information.";
                    log.error(msg);
                    throw new GovernanceException(msg);
                }

                List<String> lifeCycleNames = new ArrayList<String>();
                Resource resource = registry.get(path);
                for (Object object : resource.getProperties().keySet()) {
                    String property = (String) object;
                    if (property.startsWith("registry.LC.name.")) {
                        lifeCycleNames.add(resource.getProperty(property));
                    }
                }

                return lifeCycleNames.toArray(new String[lifeCycleNames.size()]);
            } catch (RegistryException e) {
                String msg = "Error in obtaining lifecycle names for the artifact. id: " + id +
                        ", path: " + path + ".";
                log.error(msg, e);
                throw new GovernanceException(msg, e);
            }
        }
        return null;
    }

    /**
     * Associates the named lifecycle with the artifact
     *
     * @param name the name of the lifecycle to be associated with this artifact.
     * @throws GovernanceException if an error occurred.
     */
    @Override
    public void attachLifecycle(String name) throws GovernanceException {
        try {
            String path = getPath();
            if(name != null && path != null) {
                registry.associateAspect(path, name);

                Resource resource = registry.get(path);
                if(resource.getAspects().size() == 1) {
                    // Since this is the first life-cycle we make it default
                    resource.setProperty("registry.LC.name", name);
                    registry.put(path, resource);
                }
            }
        } catch (RegistryException e) {
            String msg = "Error in associating lifecycle for the artifact. id: " + id +
                    ", path: " + path + ".";
            log.error(msg, e);
            throw new GovernanceException(msg, e);
        }
    }


    /**
     * De-associate lifecycle associated with the artifact
     *
     * @throws GovernanceException if an error occurred.
     */
    public void detachLifecycle(String lifecycleName) throws GovernanceException {
        try {
            GovernanceUtils.removeAspect(path, lifecycleName, registry);
        } catch (RegistryException e) {
            String msg = "Error in de-associating lifecycle for the artifact. id: " + id +
                    ", path: " + path + ".";
            log.error(msg, e);
            throw new GovernanceException(msg, e);
        }
    }

    /**
     * Returns the state of the lifecycle associated with this artifact.
     *
     * @return the state of the lifecycle associated with this artifact.
     * @throws GovernanceException if an error occurred.
     */
    @Override
    public String getLifecycleState() throws GovernanceException {
        String path = getPath();
        if (path != null) {
            try {
                if (!registry.resourceExists(path)) {
                    String msg =
                            "The artifact is not added to the registry. Please add the artifact " +
                                    "before reading lifecycle information.";
                    log.error(msg);
                    throw new GovernanceException(msg);
                }
                Resource resource = registry.get(path);
                for (Object object : resource.getProperties().keySet()) {
                    String property = (String) object;
                    if (property.startsWith("registry.lifecycle.") && property.endsWith(".state") && getLifecycleName() != null && property.contains((getLifecycleName()))) {
                        lcState = resource.getProperty(property);
                        return lcState;
                    }
                }
            } catch (RegistryException e) {
                String msg = "Error in obtaining lifecycle state for the artifact. id: " + id +
                        ", path: " + path + ".";
                log.error(msg, e);
                throw new GovernanceException(msg, e);
            }
        }
        return null;
    }

    /**
     * Returns the state of the lifecycle associated with this artifact.
     *
     * @return the state of the lifecycle associated with this artifact.
     * @throws GovernanceException if an error occurred.
     */
    @Override
    public String getLifecycleState(String lifeCycleName) throws GovernanceException {
        String path = getPath();
        if (path != null) {
            try {
                if (!registry.resourceExists(path)) {
                    String msg =
                            "The artifact is not added to the registry. Please add the artifact " +
                                    "before reading lifecycle information.";
                    log.error(msg);
                    throw new GovernanceException(msg);
                }
                Resource resource = registry.get(path);
                for (Object object : resource.getProperties().keySet()) {
                    String property = (String) object;
                    if (property.startsWith("registry.lifecycle.") && property.endsWith(".state") && property.contains(lifeCycleName)) {
                        return resource.getProperty(property);
                    }
                }
            } catch (RegistryException e) {
                String msg = "Error in obtaining lifecycle state for the artifact. id: " + id +
                        ", path: " + path + ".";
                log.error(msg, e);
                throw new GovernanceException(msg, e);
            }
        }
        return null;
    }

    /**
     * update the path after moving the resource.
     *
     * @throws GovernanceException if an error occurred.
     */
    public void updatePath() throws GovernanceException {
        path = GovernanceUtils.getArtifactPath(registry, id);
    }

    /**
     * update the path after moving the resource.
     *
     * @param artifactId id of the artifact
     * @throws GovernanceException if an error occurred.
     */
    public void updatePath(String artifactId) throws GovernanceException {
        path = GovernanceUtils.getArtifactPath(registry, artifactId);
    }

    /**
     * Create a version of the artifact.
     *
     * @throws GovernanceException throws if the operation failed.
     */
    public void createVersion() throws GovernanceException {
        checkRegistryResourceAssociation();
        try {
            if (!registry.resourceExists(path)) {
                String msg =
                        "The artifact is not added to the registry. Please add the artifact " +
                                "before creating versions.";
                log.error(msg);
                throw new GovernanceException(msg);
            }
            registry.createVersion(path);
        } catch (RegistryException e) {
            String msg = "Error in creating a version for the artifact. id: " + id +
                    ", path: " + path + ".";
            log.error(msg, e);
            throw new GovernanceException(msg, e);
        }
    }

    /**
     * Associate a registry, this is mostly used by the artifact manager when creating the
     * artifact.
     *
     * @param registry the registry.
     * @throws GovernanceException throws if the operation failed.
     */
    public void associateRegistry(Registry registry) throws GovernanceException {
        this.registry = registry;
    }

    /**
     * Adding an attribute to the artifact. The artifact should be saved to get effect the change.
     * In the case of a single-valued attribute, this method will set or replace the existing
     * attribute with the provided value. In the case of a multi-valued attribute, this method will
     * append the provided value to the existing list.
     *
     * @param key   the key.
     * @param value the value.
     * @throws GovernanceException throws if the operation failed.
     */
    @Override
    public void addAttribute(String key, String value) throws GovernanceException {
        boolean isAttribute = key.contains(".");
        if (!isAttribute) {
            List<String> values = attributes.get(key);
            if (values == null) {
                values = new ArrayList<String>();
                attributes.put(key, values);
            }
            values.add(value);
        } else {
            List<String> values = properties.get(key);
            if (values == null) {
                values = new ArrayList<String>();
                properties.put(key, values);
            }
            values.add(value);
        }
    }

    /**
     * Set/Update an attribute with multiple values. The artifact should be saved to get effect the
     * change.
     *
     * @param key       the key
     * @param newValues the value
     * @throws GovernanceException throws if the operation failed.
     */
    @Override
    public void setAttributes(String key, String[] newValues) throws GovernanceException {
        boolean isAttribute = key.contains(".");
        if (!isAttribute) {
            List<String> values = new ArrayList<String>();
            values.addAll(Arrays.asList(newValues));
            attributes.put(key, values);
        } else {
            List<String> values = properties.get(key);
            if (values == null) {
                values = new ArrayList<String>();
                properties.put(key, values);
            }
            values.addAll(Arrays.asList(newValues));
        }

    }

    /**
     * Set/Update an attribute with a single value. The artifact should be saved to get effect the
     * change. This method will replace the existing attribute with the provided value. In the case
     * of a multi-valued attribute this will remove all existing values. If you want to append the
     * provided value to a list values of a multi-valued attribute, use the addAttribute method
     * instead.
     *
     * @param key      the key
     * @param newValue the value
     * @throws GovernanceException throws if the operation failed.
     */
    @Override
    public void setAttribute(String key, String newValue) throws GovernanceException {
        boolean isAttribute = key.contains(".");
        if (!isAttribute) {
            List<String> values = new ArrayList<String>();
            values.add(newValue);
            attributes.put(key, values);
        } else {
            List<String> values = properties.get(key);
            if (values == null) {
                values = new ArrayList<String>();
                properties.put(key, values);
            }
            values.add(newValue);
        }

    }

    /**
     * Returns the attribute of a given key.
     *
     * @param key the key
     * @return the value of the attribute, if there are more than one attribute for the key this
     *         returns the first value.
     * @throws GovernanceException throws if the operation failed.
     */
    @Override
    public String getAttribute(String key) throws GovernanceException {
        boolean isAttribute = key.contains(".");
        if (!isAttribute) {
            List<String> values = attributes.get(key);
            if (values == null || values.size() == 0) {
                return null;
            }
            return values.get(0);
        } else {
            List<String> values = properties.get(key);
            if (values == null || values.size() == 0) {
                return null;
            }
            return values.get(0);
        }

    }

    /**
     * Returns the available attribute keys
     *
     * @return an array of attribute keys.
     * @throws GovernanceException throws if the operation failed.
     */
    @Override
    public String[] getAttributeKeys() throws GovernanceException {
        Set<String> attributeKeys = attributes.keySet();
        return attributeKeys.toArray(new String[attributeKeys.size()]);
    }

    /**
     * Returns the attribute values for a key.
     *
     * @param key the key.
     * @return attribute values for the key.
     * @throws GovernanceException throws if the operation failed.
     */
    @Override
    public String[] getAttributes(String key) throws GovernanceException {
        boolean isAttribute = key.contains(".");
        if (!isAttribute) {
            List<String> values = attributes.get(key);
            if (values == null) {
                return null; //TODO: This should return String[0]
            }
            return values.toArray(new String[values.size()]);
        } else {
            List<String> values = properties.get(key);
            if (values == null) {
                return null; //TODO: This should return String[0]
            }
            return values.toArray(new String[values.size()]);
        }
    }

    /**
     * Remove attribute with the given key. The artifact should be saved to get effect the change.
     *
     * @param key the key
     * @throws GovernanceException throws if the operation failed.
     */
    @Override
    public void removeAttribute(String key) throws GovernanceException {
        boolean isAttribute = key.contains(".");
        if (!isAttribute) {
            attributes.remove(key);
        } else {
            properties.remove(key);
        }

    }

    /**
     * Get dependencies of an artifacts. The artifacts should be saved, before calling this method.
     *
     * @return an array of dependencies of this artifact.
     * @throws GovernanceException throws if the operation failed.
     */
    @Override
    public GovernanceArtifact[] getDependencies() throws GovernanceException {
        checkRegistryResourceAssociation();
        // uses the path from the getter to make sure the used overloaded method
        String path = getPath();
        List<GovernanceArtifact> governanceArtifacts = new ArrayList<GovernanceArtifact>();
        try {
            Association[] associations =
                    registry.getAssociations(path, GovernanceConstants.DEPENDS);
            for (Association association : associations) {
                String destinationPath = association.getDestinationPath();
                if (!destinationPath.equals(path)) {
                    GovernanceArtifact governanceArtifact =
                            GovernanceUtils.retrieveGovernanceArtifactByPath(registry, destinationPath);
                    governanceArtifacts.add(governanceArtifact);
                }
            }
        } catch (RegistryException e) {
            String msg = "Error in getting dependencies from the artifact. id: " + id +
                    ", path: " + path + ".";
            log.error(msg, e);
            throw new GovernanceException(msg, e);
        }
        return governanceArtifacts.toArray(new GovernanceArtifact[governanceArtifacts.size()]);
    }

    /**
     * Get dependents of an artifact. The artifacts should be saved, before calling this method.
     *
     * @return an array of artifacts that is dependent on this artifact.
     * @throws GovernanceException throws if the operation failed.
     */
    @Override
    public GovernanceArtifact[] getDependents() throws GovernanceException {
        checkRegistryResourceAssociation();
        // uses the path from the getter to make sure the used overloaded method
        String path = getPath();
        List<GovernanceArtifact> governanceArtifacts = new ArrayList<GovernanceArtifact>();
        try {
            Association[] associations =
                    registry.getAssociations(path, GovernanceConstants.USED_BY);
            for (Association association : associations) {
                String destinationPath = association.getDestinationPath();
                if (!destinationPath.equals(path)) {
                    GovernanceArtifact governanceArtifact =
                            GovernanceUtils.retrieveGovernanceArtifactByPath(registry, destinationPath);
                    governanceArtifacts.add(governanceArtifact);
                }
            }
        } catch (RegistryException e) {
            String msg = "Error in getting dependents from the artifact. id: " + id +
                    ", path: " + path + ".";
            log.error(msg, e);
            throw new GovernanceException(msg, e);
        }
        return governanceArtifacts.toArray(new GovernanceArtifact[governanceArtifacts.size()]);
    }

    /**
     * Get all lifecycle actions for the current state of the lifecycle
     *
     * @param lifeCycleName lifecycle name of which actions are needed
     * @return Action set which can be invoked
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException
     *          throws if the operation failed.
     */
    public String[] getAllLifecycleActions(String lifeCycleName) throws GovernanceException {
    	String path = getPath();
        try {
            return registry.getAspectActions(path, lifeCycleName);
        } catch (RegistryException e) {
            String lifecycleState = getLifecycleState(lifeCycleName);
            String msg = "Error while retrieving the lifecycle actions " +
                    "for lifecycle: " + lifeCycleName + " in lifecycle state: " + lifecycleState;
            throw new GovernanceException(msg, e);
        }
    }

    /**
     * Invoke lifecycle action
     *
     * @param action lifecycle action tobe invoked
     * @param aspectName aspect name of which the action need to be invoked
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException
     *          throws if the operation failed.
     */
    public void invokeAction(String action, String aspectName) throws GovernanceException {
        invokeAction(action, new HashMap<String, String>(), aspectName);
    }

    /**
     * Invoke lifecycle action
     *
     * @param action     lifecycle action tobe invoked
     * @param parameters extra parameters needed when promoting
     * @param aspectName aspect name of which the action need to be invoked
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException
     *          throws if the operation failed.
     */
    public void invokeAction(String action, Map<String, String> parameters, String aspectName) throws GovernanceException {
        Resource artifactResource = getArtifactResource();
        CheckListItemBean[] checkListItemBeans = GovernanceUtils.getAllCheckListItemBeans(artifactResource, this, aspectName);
        try {
            if (checkListItemBeans != null) {
                for (CheckListItemBean checkListItemBean : checkListItemBeans) {
                    parameters.put(checkListItemBean.getOrder() + ".item", checkListItemBean.getValue().toString());
                }
            }
            registry.invokeAspect(getArtifactPath(), aspectName, action, parameters);
        } catch (RegistryException e) {
            String msg = "Invoking lifecycle action \"" + action + "\" failed";
            log.error(msg, e);
            throw new GovernanceException(msg, e);
        }
    }

    /**
     * Retrieve name set of the checklist items
     *
     * @param aspectName lifecycle name of which action to be invoked
     * @return Checklist item name set
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException
     *          throws if the operation failed.
     */
    public String[] getAllCheckListItemNames(String aspectName) throws GovernanceException {
        Resource artifactResource = getArtifactResource();
        CheckListItemBean[] checkListItemBeans = GovernanceUtils.getAllCheckListItemBeans(artifactResource, this, aspectName);
        if (checkListItemBeans == null) {
            return null;
        }
        String[] checkListItemNames = new String[checkListItemBeans.length];
        for (CheckListItemBean checkListItemBean : checkListItemBeans) {
            checkListItemNames[checkListItemBean.getOrder()] = checkListItemBean.getName();
        }
        return checkListItemNames;
    }

    /**
     * Check the checklist item
     *
     * @param aspectName lifecycle name of which action to be invoked
     * @param order order of the checklist item need to checked
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException
     *          throws if the operation failed.
     */
    public void checkLCItem(int order, String aspectName) throws GovernanceException {
        Resource artifactResource = getArtifactResource();
        CheckListItemBean[] checkListItemBeans = GovernanceUtils.getAllCheckListItemBeans(artifactResource, this, aspectName);
        if (checkListItemBeans == null || order < 0 || order >= checkListItemBeans.length) {
            throw new GovernanceException("Invalid check list item.");
        } else if (checkListItemBeans[order].getValue()) {
            throw new GovernanceException("lifecycle checklist item \"" +
                    checkListItemBeans[order].getName() + "\" already checked");
        }
        try {
            setCheckListItemValue(order, true, checkListItemBeans, aspectName);
        } catch (RegistryException e) {
            String msg = "Checking LC item failed for check list item " + checkListItemBeans[order].getName();
            log.error(msg, e);
            throw new GovernanceException(msg, e);
        }
    }

    /**
     * Check whether the given ordered lifecycle checklist item is checked or not
     *
     * @param order order of the checklist item need to unchecked
     * @param aspectName lifecycle name of which action to be invoked
     * @return whether the given ordered lifecycle checklist item is checked or not
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException
     *          throws if the operation failed.
     */
    public boolean isLCItemChecked(int order, String aspectName) throws GovernanceException {
        Resource artifactResource = getArtifactResource();
        CheckListItemBean[] checkListItemBeans = GovernanceUtils.getAllCheckListItemBeans(artifactResource, this, aspectName);
        if (checkListItemBeans == null || order < 0 || order >= checkListItemBeans.length) {
            throw new GovernanceException("Invalid check list item.");
        }
        return checkListItemBeans[order].getValue();

    }

    /**
     * Un-check the checklist item
     *
     * @param order order of the checklist item need to unchecked
     * @param aspectName lifecycle name of which action to be invoked
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException
     *          throws if the operation failed.
     */
    public void uncheckLCItem(int order, String aspectName) throws GovernanceException {
        Resource artifactResource = getArtifactResource();
        CheckListItemBean[] checkListItemBeans = GovernanceUtils.getAllCheckListItemBeans(artifactResource, this, aspectName);
        if (checkListItemBeans == null || order < 0 || order >= checkListItemBeans.length) {
            throw new GovernanceException("Invalid check list item.");
        } else if (!checkListItemBeans[order].getValue()) {
            throw new GovernanceException("lifecycle checklist item \"" +
                    checkListItemBeans[order].getName() + "\" not checked");
        }
        try {
            setCheckListItemValue(order, false, checkListItemBeans, aspectName);
        } catch (RegistryException e) {
            String msg = "Unchecking LC item failed for check list item: " + checkListItemBeans[order].getName();
            log.error(msg, e);
            throw new GovernanceException(msg, e);
        }
    }

    /**
     * Set the checklist item value
     *
     * @param order order of the checklist item
     * @param value value of the checklist item
     * @param aspectName aspect name
     * @throws RegistryException throws if the operation failed.
     */
    private void setCheckListItemValue(int order, boolean value,
                                       CheckListItemBean[] checkListItemBeans, String aspectName) throws RegistryException {
        checkListItemBeans[order].setValue(value);
        Map<String, String> parameters = new HashMap<String, String>();
        for (CheckListItemBean checkListItemBean : checkListItemBeans) {
            parameters.put(checkListItemBean.getOrder() + ".item", checkListItemBean.getValue().toString());
        }
        registry.invokeAspect(getArtifactPath(), aspectName, "itemClick", parameters);
    }

    /**
     * Retrieve action set which need votes
     *
     * @return Action set which can vote
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException
     *          throws if the operation failed.
     */
    public String[] getAllVotingItems() throws GovernanceException {
        Resource artifactResource = getArtifactResource();
        ApproveItemBean[] approveItemBeans = GovernanceUtils.
                getAllApproveItemBeans(((UserRegistry) registry).getUserName(), artifactResource, this);
        if (approveItemBeans == null) {
            throw new GovernanceException("No voting event found for the lifecycle: " + getLcName() +
                    " in lifecycle state: " + getLcState() + " of the artifact " + getQName().getLocalPart());
        }
        String[] votingItems = new String[approveItemBeans.length];
        for (ApproveItemBean approveItemBean : approveItemBeans) {
            votingItems[approveItemBean.getOrder()] = approveItemBean.getName();
        }
        return votingItems;
    }

    /**
     * Vote for an action
     *
     * @param order order of the action which need to be voted
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException
     *          throws if the operation failed.
     */
    public void vote(int order) throws GovernanceException {
        Resource artifactResource = getArtifactResource();
        ApproveItemBean[] approveItemBeans = GovernanceUtils.
                getAllApproveItemBeans(((UserRegistry) registry).getUserName(), artifactResource, this);
        if (approveItemBeans == null || order < 0 || order >= approveItemBeans.length) {
            throw new GovernanceException("Invalid voting action selected");
        } else if (approveItemBeans[order].getValue()) {
            throw new GovernanceException("Already voted for the action " + approveItemBeans[order].getName());
        }
        try {
            setVotingItemValue(order, true, approveItemBeans);
        } catch (RegistryException e) {
            String msg = "Voting failed for action " + approveItemBeans[order].getName();
            log.error(msg, e);
            throw new GovernanceException(msg, e);
        }
    }

    /**
     * Check whether the current user voted for given order event
     *
     * @param order order of the action which need to be voted
     * @return whether the current user voted for the given order event
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException
     *          throws if the operation failed.
     */
    public boolean isVoted(int order) throws GovernanceException {
        Resource artifactResource = getArtifactResource();
        ApproveItemBean[] approveItemBeans = GovernanceUtils.
                getAllApproveItemBeans(((UserRegistry) registry).getUserName(), artifactResource, this);
        if (approveItemBeans == null || order < 0 || order >= approveItemBeans.length) {
            throw new GovernanceException("Invalid voting action selected");
        }
        return approveItemBeans[order].getValue();
    }

    /**
     * Unvote for an action
     *
     * @param order order of the action which need to be unvoted
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException
     *          throws if the operation failed.
     */
    public void unvote(int order) throws GovernanceException {
        Resource artifactResource = getArtifactResource();
        ApproveItemBean[] approveItemBeans = GovernanceUtils.
                getAllApproveItemBeans(((UserRegistry) registry).getUserName(), artifactResource, this);
        if (approveItemBeans == null || order < 0 || order >= approveItemBeans.length) {
            throw new GovernanceException("Invalid voting action selected");
        } else if (!approveItemBeans[order].getValue()) {
            throw new GovernanceException("Not voted for the action \""
                    + approveItemBeans[order].getName() + "\"");
        }
        try {
            setVotingItemValue(order, false, approveItemBeans);
        } catch (RegistryException e) {
            String msg = "Unvoting failed for action \"" + approveItemBeans[order].getName() + "\"";
            log.error(msg, e);
            throw new GovernanceException(msg, e);
        }
    }

    /**
     * Set the approval value
     *
     * @param order order of the approve event
     * @param value value of the approve
     * @throws RegistryException throws if the operation failed.
     */
    private void setVotingItemValue(int order, boolean value,
                                    ApproveItemBean[] approveItemBeans) throws RegistryException {
        approveItemBeans[order].setValue(value);
        Map<String, String> parameters = new HashMap<String, String>();
        for (ApproveItemBean approveItemBean : approveItemBeans) {
            parameters.put(approveItemBean.getOrder() + ".vote", approveItemBean.getValue().toString());
        }
        registry.invokeAspect(getArtifactPath(), getLcName(), "voteClick", parameters);
    }

    /**
     * Attach the current artifact to an another artifact. Both the artifacts should be saved,
     * before calling this method. This method will two generic artifact types. There are specific
     * methods
     *
     * @param attachedToArtifact the artifact the current artifact is attached to
     * @throws GovernanceException throws if the operation failed.
     */
    public void attach(GovernanceArtifact attachedToArtifact) throws GovernanceException {
        checkRegistryResourceAssociation();
        // uses the path from the getter to make sure the used overloaded method
        String path = getPath();
        String attachedToArtifactPath = attachedToArtifact.getPath();
        if (attachedToArtifactPath == null) {
            String msg = "'Attached to artifact' is not associated with a registry path.";
            log.error(msg);
            throw new GovernanceException(msg);
        }
        try {
            registry.addAssociation(attachedToArtifactPath, path, GovernanceConstants.USED_BY);
            registry.addAssociation(path, attachedToArtifactPath, GovernanceConstants.DEPENDS);
        } catch (RegistryException e) {
            String msg = "Error in attaching the artifact. source id: " + id + ", path: " + path +
                    ", target id: " + attachedToArtifact.getId() + ", path:" +
                    attachedToArtifactPath +
                    ", attachment type: " + attachedToArtifact.getClass().getName() + ".";
            log.error(msg, e);
            throw new GovernanceException(msg, e);
        }
    }


    /**
     * Detach the current artifact from the provided artifact. Both the artifacts should be saved,
     * before calling this method.
     *
     * @param artifactId the artifact id of the attached artifact
     * @throws GovernanceException throws if the operation failed.
     */
    public void detach(String artifactId) throws GovernanceException {
        checkRegistryResourceAssociation();
        // uses the path from the getter to make sure the used overloaded method
        String path = getPath();
        String artifactPath = GovernanceUtils.getArtifactPath(registry, artifactId);
        if (artifactPath == null) {
            String msg = "Attached to artifact is not associated with a registry path.";
            log.error(msg);
            throw new GovernanceException(msg);
        }
        try {
            registry.removeAssociation(path, artifactPath, GovernanceConstants.DEPENDS);
            registry.removeAssociation(artifactPath, path, GovernanceConstants.USED_BY);
        } catch (RegistryException e) {
            String msg = "Error in detaching the artifact. source id: " + id + ", path: " + path +
                    ", target id: " + artifactId +
                    ", target path:" + artifactPath + ".";
            log.error(msg, e);
            throw new GovernanceException(msg, e);
        }
    }

    /**
     * Validate the resource is associated with a registry
     *
     * @throws GovernanceException if the resource is not associated with a registry.
     */
    protected void checkRegistryResourceAssociation() throws GovernanceException {
        // uses the path from the getter to make sure the used overloaded method
        String path = getPath();
        if (registry == null) {
            String msg = "A registry is not associated with the artifact.";
            log.error(msg);
            throw new GovernanceException(msg);
        }
        if (path == null) {
            String msg = "A path is not associated with the artifact.";
            log.error(msg);
            throw new GovernanceException(msg);
        }
        if (id == null) {
            String msg = "An id is not associated with the artifact.";
            log.error(msg);
            throw new GovernanceException(msg);
        }
    }

    /**
     * Returns the associated registry to the artifact.
     *
     * @return the associated registry
     */
    protected Registry getAssociatedRegistry() {
        return registry;
    }

    /**
     * Get the resource related to this artifact
     *
     * @return resource related to this artifact
     * @throws GovernanceException if there is no resource related to the artifact in the registry
     */
    private Resource getArtifactResource() throws GovernanceException {
        Resource artifactResource;
        try {
            return registry.get(artifactPath);
        } catch (RegistryException e) {
            String msg = "Artifact resource \"" + getQName().getLocalPart() + "\" not found in the registry";
            throw new GovernanceException();
        }
    }

    /**
     * Returns the available attribute keys
     *
     * @return an array of attribute keys.
     * @throws GovernanceException throws if the operation failed.
     */
    @Override
    public String[] getPropertyKeys() throws GovernanceException {
        Set<String> attributeKeys = properties.keySet();
        return attributeKeys.toArray(new String[attributeKeys.size()]);
    }

    @Override
    public boolean equals(Object artifact) {
        GovernanceArtifact governanceArtifact ;
        if(!(artifact instanceof GovernanceArtifact)) {
            return false;
        } else {
            governanceArtifact = (GovernanceArtifact) artifact;
        }

        return governanceArtifact.getId().equals(this.getId());
    }

    @Override
    public void attach(String artifactId) throws GovernanceException {
        throw new UnsupportedOperationException("Not yet Implemented");
    }

    @Override
    public void addAssociation(String associationType, GovernanceArtifact attachedToArtifact)
            throws GovernanceException {
        checkRegistryResourceAssociation();
        // uses the path from the getter to make sure the used overloaded method
        String path = getPath();
        String attachedToArtifactPath = attachedToArtifact.getPath();
        if (attachedToArtifactPath == null) {
            String msg = "'Attached to artifact' is not associated with a registry path.";
            log.error(msg);
            throw new GovernanceException(msg);
        }
        try {
            registry.addAssociation(path, attachedToArtifactPath, associationType);
        } catch (RegistryException e) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Error in attaching the artifact. source id: ")
                    .append(id)
                    .append(", path: ")
                    .append(path)
                    .append(", target id: ")
                    .append(attachedToArtifact.getId())
                    .append(", path:")
                    .append(attachedToArtifactPath)
                    .append(", attachment type: ")
                    .append(attachedToArtifact.getClass().getName());
            throw new GovernanceException(stringBuilder.toString(), e);
        }    }

    @Override
    public void addAssociation(String associationType, String artifactId) throws GovernanceException {
        throw new UnsupportedOperationException("Not yet Implemented");
    }

    @Override
    public void addBidirectionalAssociation(String forwardType, String backwardType,
                                            GovernanceArtifact attachedToArtifact) throws GovernanceException {
        checkRegistryResourceAssociation();
        // uses the path from the getter to make sure the used overloaded method
        String path = getPath();
        String attachedToArtifactPath = attachedToArtifact.getPath();
        if (attachedToArtifactPath == null) {
            String msg = "'Attached to artifact' is not associated with a registry path.";
            log.error(msg);
            throw new GovernanceException(msg);
        }
        try {
            registry.addAssociation(path, attachedToArtifactPath, forwardType);
            registry.addAssociation(attachedToArtifactPath, path, backwardType);
        } catch (RegistryException e) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Error in attaching the artifact. source id: ")
                    .append(id)
                    .append(", path: ")
                    .append(path)
                    .append(", target id: ")
                    .append(attachedToArtifact.getId())
                    .append(", path:")
                    .append(attachedToArtifactPath)
                    .append(", attachment type: ")
                    .append(attachedToArtifact.getClass().getName());
            throw new GovernanceException(stringBuilder.toString(), e);
        }
    }

    @Override
    public void removeAssociation(String associationType, String artifactId) throws GovernanceException {
        throw new UnsupportedOperationException("Not yet Implemented");
    }

    @Override
    public void removeAssociation(String artifactId) throws GovernanceException {
        throw new UnsupportedOperationException("Not yet Implemented");
    }

    @Override
    public Map<String, List<GovernanceArtifact>> getAssociations() throws GovernanceException {
        throw new UnsupportedOperationException("Not yet Implemented");
    }

    @Override
    public Map<String, List<String>> getAssociatedArtifactIds() throws GovernanceException {
        throw new UnsupportedOperationException("Not yet Implemented");
    }

    @Override
    public boolean isRegistryAwareArtifact() {
        try {
            checkRegistryResourceAssociation();
            return true;
        } catch (GovernanceException e) {
            return false;
        }
    }

    @Override
    public void addTag(String tag) throws GovernanceException {
        throw new UnsupportedOperationException("Not yet Implemented");
    }

    @Override
    public void addTags(List<String> tags) throws GovernanceException {
        throw new UnsupportedOperationException("Not yet Implemented");
    }

    @Override
    public List<String> listTags() throws GovernanceException {
        throw new UnsupportedOperationException("Not yet Implemented");
    }

    @Override
    public void removeTag(String tag) throws GovernanceException {
        throw new UnsupportedOperationException("Not yet Implemented");
    }

    @Override
    public void removeTags(List<String> tags) throws GovernanceException {
        throw new UnsupportedOperationException("Not yet Implemented");
    }

    @Override
    public String toString() {
        return "GovernanceArtifactImpl{" +
               "attributes=" + attributes +
               ", id='" + id + '\'' +
               '}';
    }

    public boolean uniqueTo(GovernanceArtifact artifact) {
        if (artifact != null) {
            List<String> uAttributes = getUniqueAttributes(artifact);
            if (this == artifact || this.equals(artifact)) {
                return true;
            } else if (uAttributes != null && uAttributes.size() > 0) {
                try {
                    for (String attributeName : uAttributes) {
                        if (this.getAttribute(attributeName) != null && artifact.getAttribute(attributeName) !=
                                                                        null && !this.getAttribute(attributeName).equals
                                (artifact.getAttribute(attributeName))) {
                            return false;
                        }
                    }
                    //all unique attributes are same
                    return true;
                } catch (GovernanceException e) {
                    log.error(e);
                }
            }
        }
        return false;
    }

    private List<String> getUniqueAttributes(GovernanceArtifact artifact) {
        if(this.uniqueAttributes != null){
            return uniqueAttributes;
        } else if(artifact instanceof GovernanceArtifactImpl){
            return ((GovernanceArtifactImpl)artifact).getUniqueAttributes();
        }
        return null;
    }

    public boolean compareTo(GovernanceArtifact artifact) {
        if (artifact != null) {
            if (this == artifact || this.equals(artifact)) {
                return true;
            } else {
                try {
                    for (String key : this.getAttributeKeys()) {
                        if (!this.getAttribute(key).equals(artifact.getAttribute(key))) {
                            return false;
                        }
                    }
                    //all unique attributes are same
                    return true;
                } catch (GovernanceException e) {
                    log.error(e);
                }
            }
        }
        return false;
    }
}
