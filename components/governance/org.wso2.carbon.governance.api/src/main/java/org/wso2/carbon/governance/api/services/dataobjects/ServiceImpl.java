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
package org.wso2.carbon.governance.api.services.dataobjects;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifactImpl;
import org.wso2.carbon.governance.api.endpoints.dataobjects.Endpoint;
import org.wso2.carbon.governance.api.endpoints.dataobjects.EndpointImpl;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.policies.dataobjects.Policy;
import org.wso2.carbon.governance.api.policies.dataobjects.PolicyImpl;
import org.wso2.carbon.governance.api.schema.dataobjects.Schema;
import org.wso2.carbon.governance.api.schema.dataobjects.SchemaImpl;
import org.wso2.carbon.governance.api.util.GovernanceConstants;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.governance.api.wsdls.dataobjects.WsdlImpl;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.extensions.utils.CommonUtil;

import javax.xml.namespace.QName;
import java.util.*;


/**
 * This represents a service artifact stored on the Registry. Service artifacts are created as a
 * result of adding a new service or uploading or importing a WSDL file into the registry.
 */
public class ServiceImpl extends GovernanceArtifactImpl implements Service {

    private static final Log log = LogFactory.getLog(ServiceImpl.class);
    private static final String ACTIVATE_PROPERTY_FLAG_NAME = "active";
    private QName qName;

    /**
     * Copy constructor used for cloning.
     *
     * @param service the object to be copied.
     */
    protected ServiceImpl(GovernanceArtifact service) {
        super((GovernanceArtifactImpl)service);
        this.qName = service.getQName();
        setLcName(((GovernanceArtifactImpl) service).getLcName());
        setLcState(((GovernanceArtifactImpl) service).getLcState());
        setArtifactPath(((GovernanceArtifactImpl) service).getArtifactPath());
    }

    /**
     * Constructor accepting resource identifier and the qualified name.
     *
     * @param id    the resource identifier.
     * @param qName the qualified name.
     */
    public ServiceImpl(String id, QName qName) {
        super(id);
        this.qName = qName;
    }

    /**
     * Constructor accepting resource identifier and the service content.
     *
     * @param id                    the resource identifier.
     * @param serviceContentElement an XML element containing the service content.
     *
     * @throws GovernanceException if the construction fails.
     */
    public ServiceImpl(String id, OMElement serviceContentElement) throws GovernanceException {
        super(id, serviceContentElement);

        String serviceName = CommonUtil.getServiceName(serviceContentElement);
        String serviceNamespace = CommonUtil.getServiceNamespace(serviceContentElement);
        if (serviceName != null && !serviceName.equals("")) {
            this.qName = new QName(serviceNamespace, serviceName);
        }
    }

    public QName getQName() {
        return qName;
    }

    /**
     * Method to set the qualified name of this service artifact.
     *
     * @param qName the qualified name.
     *
     * @throws GovernanceException if the operation failed.
     */
    public void setQName(QName qName) throws GovernanceException {
        // the path will be synced with the qualified name
        this.qName = qName;
    }

    /**
     * Method to activate this service.
     * @deprecated since active functionality is no longer used.
     *
     * @throws GovernanceException if the operation failed.
     */
    public void activate() throws GovernanceException {
        checkRegistryResourceAssociation();
        try {
            Registry registry = getAssociatedRegistry();
            String path = getPath();
            Resource r = registry.get(path);
            r.setProperty(ACTIVATE_PROPERTY_FLAG_NAME, Boolean.toString(true));
            registry.put(path, r);
        } catch (RegistryException e) {
            String msg = "Error in activating the service: id:" + getId() +
                    ", path: " + getPath() + ".";
            log.error(msg, e);
            throw new GovernanceException(msg, e);
        }
    }

    /**
     * Method to deactivate this service.
     * @deprecated since active functionality is no longer used.
     *
     * @throws GovernanceException if the operation failed.
     */
    public void deactivate() throws GovernanceException {
        checkRegistryResourceAssociation();
        try {
            Registry registry = getAssociatedRegistry();
            String path = getPath();
            Resource r = registry.get(path);
            r.setProperty(ACTIVATE_PROPERTY_FLAG_NAME, Boolean.toString(false));
            registry.put(path, r);
        } catch (RegistryException e) {
            String msg = "Error in activating the service: id:" + getId() +
                    ", path: " + getPath() + ".";
            log.error(msg, e);
            throw new GovernanceException(msg, e);
        }
    }

    /**
     * Method to set/unset the active status of this service.
     *
     * @param isActive whether the service is active.
     * @deprecated since active functionality is no longer used.
     *
     * @throws GovernanceException if the operation failed.
     */
    //TODO remove this in the next release
    @Deprecated
    public void setActive(boolean isActive) throws GovernanceException {
        checkRegistryResourceAssociation();
        try {
            Registry registry = getAssociatedRegistry();
            String path = getPath();
            Resource r = registry.get(path);
            r.setProperty(ACTIVATE_PROPERTY_FLAG_NAME, Boolean.toString(isActive));
            registry.put(path, r);
        } catch (RegistryException e) {
            String msg = "Error in activating the service: id:" + getId() +
                    ", path: " + getPath() + ".";
            log.error(msg, e);
            throw new GovernanceException(msg, e);
        }
    }

    /**
     * Method to obtain whether this service is active or not.
     * @deprecated since active functionality is no longer used.
     *
     * @return true if this service is active, and false if not.
     * @throws GovernanceException if the operation failed.
     */
    public boolean isActive() throws GovernanceException {
        checkRegistryResourceAssociation();
        try {
            Registry registry = getAssociatedRegistry();
            String path = getPath();
            Resource r = registry.get(path);
            // if the inactive flag is not set explicitly it is active.
            return !Boolean.toString(false).equals(r.getProperty(ACTIVATE_PROPERTY_FLAG_NAME));
        } catch (RegistryException e) {
            String msg = "Error in checking the activeness of the service: id:" + getId() +
                    ", path: " + getPath() + ".";
            log.error(msg, e);
            throw new GovernanceException(msg, e);
        }
    }

    /**
     * Attach a policy artifact to a service artifact. Both the artifacts should be saved, before
     * calling this method.
     *
     * @param policy the policy to attach.
     *
     * @throws GovernanceException if the operation failed.
     */
    @Override
    public void attachPolicy(Policy policy) throws GovernanceException {
        attach(policy);
    }

    /**
     * Detach a policy artifact from a service artifact. Both the artifacts should be saved, before
     * calling this method.
     *
     * @param policyId the identifier of the policy to detach.
     *
     * @throws GovernanceException if the operation failed.
     */
    @Override
    @SuppressWarnings("unused")
    public void detachPolicy(String policyId) throws GovernanceException {
        detach(policyId);
    }

    /**
     * Method to retrieve all policies attached to this service artifact.
     *
     * @return all policies attached to this service artifact.
     * @throws GovernanceException if the operation failed.
     */
    @Override
    public Policy[] getAttachedPolicies() throws GovernanceException {
        checkRegistryResourceAssociation();
        Registry registry = getAssociatedRegistry();
        String path = getPath();
        List<Policy> policies = new ArrayList<Policy>();
        try {
            Association[] associations =
                    registry.getAssociations(path, GovernanceConstants.DEPENDS);
            for (Association association : associations) {
                String destinationPath = association.getDestinationPath();
                GovernanceArtifact governanceArtifact =
                        GovernanceUtils.retrieveGovernanceArtifactByPath(registry, destinationPath);
                if (governanceArtifact instanceof PolicyImpl) {
                    policies.add((Policy) governanceArtifact);
                }
            }
        } catch (RegistryException e) {
            String msg =
                    "Error in getting attached policies from the artifact at path: " + path + ".";
            log.error(msg, e);
            throw new GovernanceException(msg, e);
        }
        return policies.toArray(new Policy[policies.size()]);
    }

    /**
     * Attach a schema artifact to a service artifact. Both the artifacts should be saved, before
     * calling this method.
     *
     * @param schema the schema to attach.
     *
     * @throws GovernanceException if the operation failed.
     */
    @Override
    @SuppressWarnings("unused")
    public void attachSchema(Schema schema) throws GovernanceException {
        attach(schema);
    }

    /**
     * Detach a schema artifact from a service artifact. Both the artifacts should be saved, before
     * calling this method.
     *
     * @param schemaId the identifier of the schema to detach.
     *
     * @throws GovernanceException if the operation failed.
     */
    @Override
    @SuppressWarnings("unused")
    public void detachSchema(String schemaId) throws GovernanceException {
        detach(schemaId);
    }

    /**
     * Method to retrieve all schemas attached to this service artifact.
     *
     * @return all schemas attached to this service artifact.
     * @throws GovernanceException if the operation failed.
     */
    @Override
    @SuppressWarnings("unused")
    public Schema[] getAttachedSchemas() throws GovernanceException {
        checkRegistryResourceAssociation();
        Registry registry = getAssociatedRegistry();
        String path = getPath();
        List<Schema> schemas = new ArrayList<Schema>();
        try {
            Association[] associations =
                    registry.getAssociations(path, GovernanceConstants.DEPENDS);
            for (Association association : associations) {
                String destinationPath = association.getDestinationPath();
                GovernanceArtifact governanceArtifact =
                        GovernanceUtils.retrieveGovernanceArtifactByPath(registry, destinationPath);
                if (governanceArtifact instanceof SchemaImpl) {
                    schemas.add((Schema) governanceArtifact);
                }
            }
        } catch (RegistryException e) {
            String msg =
                    "Error in getting attached schemas from the artifact at path: " + path + ".";
            log.error(msg, e);
            throw new GovernanceException(msg, e);
        }
        return schemas.toArray(new Schema[schemas.size()]);
    }

    /**
     * Attach a WSDL artifact to a service artifact. Both the artifacts should be saved, before
     * calling this method.
     *
     * @param wsdl the WSDL to attach.
     *
     * @throws GovernanceException if the operation failed.
     */
    @Override
    public void attachWSDL(Wsdl wsdl) throws GovernanceException {
        attach(wsdl);
        addAttribute(GovernanceConstants.SERVICE_WSDL_ATTRIBUTE,
                RegistryUtils.getAbsolutePathToOriginal(wsdl.getPath(),
                        RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH));
    }

    /**
     * Detach a WSDL artifact from a service artifact. Both the artifacts should be saved, before
     * calling this method.
     *
     * @param wsdlId the identifier of the WSDL to detach.
     *
     * @throws GovernanceException if the operation failed.
     */
    @Override
    @SuppressWarnings("unused")
    public void detachWSDL(String wsdlId) throws GovernanceException {
        detach(wsdlId);
    }

    /**
     * Method to retrieve all WSDLs attached to this service artifact.
     *
     * @return all WSDLs attached to this service artifact.
     * @throws GovernanceException if the operation failed.
     */
    @Override
    public Wsdl[] getAttachedWsdls() throws GovernanceException {
        checkRegistryResourceAssociation();
        Registry registry = getAssociatedRegistry();
        String path = getPath();
        List<Wsdl> wsdls = new ArrayList<Wsdl>();
        try {
            Association[] associations =
                    registry.getAssociations(path, GovernanceConstants.DEPENDS);
            for (Association association : associations) {
                String destinationPath = association.getDestinationPath();
                GovernanceArtifact governanceArtifact =
                        GovernanceUtils.retrieveGovernanceArtifactByPath(registry, destinationPath);
                if (governanceArtifact instanceof WsdlImpl) {
                    wsdls.add((Wsdl) governanceArtifact);
                }
            }
        } catch (RegistryException e) {
            String msg = "Error in getting attached wsdls from the artifact at path: " + path + ".";
            log.error(msg, e);
            throw new GovernanceException(msg, e);
        }
        return wsdls.toArray(new Wsdl[wsdls.size()]);
    }

    // currently only the valid services are possible to add
    //public void validate() throws GovernanceException {
    // TODO: add the service validation code..
    //}

    /**
     * Attach an endpoint artifact to a service artifact. Both the artifacts should be saved, before
     * calling this method.
     *
     * @param endpoint the endpoint to attach.
     *
     * @throws GovernanceException if the operation failed.
     */
    @Override
    public void attachEndpoint(Endpoint endpoint) throws GovernanceException {
        // add an endpoint and attache to the service..
        attach(endpoint);
    }

    /**
     * Detach an endpoint artifact from a service artifact. Both the artifacts should be saved,
     * before calling this method.
     *
     * @param endpointId the identifier of the endpoint to detach.
     *
     * @throws GovernanceException if the operation failed.
     */
    @Override
    public void detachEndpoint(String endpointId) throws GovernanceException {
        // detach the endpoint and delete the endpoint.
        detach(endpointId);
    }

    /**
     * Method to retrieve all endpoints attached to this service artifact.
     *
     * @return all endpoints attached to this service artifact.
     * @throws GovernanceException if the operation failed.
     */
    @Override
    public Endpoint[] getAttachedEndpoints() throws GovernanceException {
        checkRegistryResourceAssociation();
        Registry registry = getAssociatedRegistry();
        String path = getPath();
        List<Endpoint> endpoints = new ArrayList<Endpoint>();
        try {
            Association[] associations =
                    registry.getAssociations(path, GovernanceConstants.DEPENDS);
            for (Association association : associations) {
                String destinationPath = association.getDestinationPath();
                GovernanceArtifact governanceArtifact =
                        GovernanceUtils.retrieveGovernanceArtifactByPath(registry, destinationPath);
                if (governanceArtifact instanceof EndpointImpl) {
                    endpoints.add((Endpoint) governanceArtifact);
                }
            }
        } catch (RegistryException e) {
            String msg =
                    "Error in getting attached endpoints from the artifact at path: " + path + ".";
            log.error(msg, e);
            throw new GovernanceException(msg, e);
        }
        return endpoints.toArray(new Endpoint[endpoints.size()]);
    }

/*
    public PeopleArtifact[] getOwners() throws GovernanceException {
        return GovernanceUtils.extractPeopleFromAttribute(getAssociatedRegistry(), this,
                GovernanceConstants.SERVICE_OWNERS_ATTRIBUTE);
    }
*/

/*
    public PeopleArtifact[] getConsumers() throws GovernanceException {
        return GovernanceUtils.extractPeopleFromAttribute(getAssociatedRegistry(), this,
                GovernanceConstants.SERVICE_CONSUMERS_ATTRIBUTE);
    }
*/
}
