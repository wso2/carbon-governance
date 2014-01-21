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

import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.endpoints.dataobjects.Endpoint;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.policies.dataobjects.Policy;
import org.wso2.carbon.governance.api.schema.dataobjects.Schema;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;


public interface Service extends GovernanceArtifact {
    /**
     * Attach a policy artifact to a service artifact. Both the artifacts should be saved, before
     * calling this method.
     *
     * @param policy the policy to attach.
     *
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException if the operation failed.
     */
    void attachPolicy(Policy policy) throws GovernanceException;

    /**
     * Detach a policy artifact from a service artifact. Both the artifacts should be saved, before
     * calling this method.
     *
     * @param policyId the identifier of the policy to detach.
     *
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException if the operation failed.
     */
    @SuppressWarnings("unused")
    void detachPolicy(String policyId) throws GovernanceException;

    /**
     * Method to retrieve all policies attached to this service artifact.
     *
     * @return all policies attached to this service artifact.
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException if the operation failed.
     */
    Policy[] getAttachedPolicies() throws GovernanceException;

    /**
     * Attach a schema artifact to a service artifact. Both the artifacts should be saved, before
     * calling this method.
     *
     * @param schema the schema to attach.
     *
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException if the operation failed.
     */
    @SuppressWarnings("unused")
    void attachSchema(Schema schema) throws GovernanceException;

    /**
     * Detach a schema artifact from a service artifact. Both the artifacts should be saved, before
     * calling this method.
     *
     * @param schemaId the identifier of the schema to detach.
     *
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException if the operation failed.
     */
    @SuppressWarnings("unused")
    void detachSchema(String schemaId) throws GovernanceException;

    /**
     * Method to retrieve all schemas attached to this service artifact.
     *
     * @return all schemas attached to this service artifact.
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException if the operation failed.
     */
    @SuppressWarnings("unused")
    Schema[] getAttachedSchemas() throws GovernanceException;

    /**
     * Attach a WSDL artifact to a service artifact. Both the artifacts should be saved, before
     * calling this method.
     *
     * @param wsdl the WSDL to attach.
     *
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException if the operation failed.
     */
    void attachWSDL(Wsdl wsdl) throws GovernanceException;

    /**
     * Detach a WSDL artifact from a service artifact. Both the artifacts should be saved, before
     * calling this method.
     *
     * @param wsdlId the identifier of the WSDL to detach.
     *
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException if the operation failed.
     */
    @SuppressWarnings("unused")
    void detachWSDL(String wsdlId) throws GovernanceException;

    /**
     * Method to retrieve all WSDLs attached to this service artifact.
     *
     * @return all WSDLs attached to this service artifact.
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException if the operation failed.
     */
    Wsdl[] getAttachedWsdls() throws GovernanceException;

    /**
     * Attach an endpoint artifact to a service artifact. Both the artifacts should be saved, before
     * calling this method.
     *
     * @param endpoint the endpoint to attach.
     *
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException if the operation failed.
     */
    void attachEndpoint(Endpoint endpoint) throws GovernanceException;

    /**
     * Detach an endpoint artifact from a service artifact. Both the artifacts should be saved,
     * before calling this method.
     *
     * @param endpointId the identifier of the endpoint to detach.
     *
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException if the operation failed.
     */
    void detachEndpoint(String endpointId) throws GovernanceException;

    /**
     * Method to retrieve all endpoints attached to this service artifact.
     *
     * @return all endpoints attached to this service artifact.
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException if the operation failed.
     */
    Endpoint[] getAttachedEndpoints() throws GovernanceException;

    /**
     * Method to activate this service.
     * @deprecated since active functionality is no longer used.
     *
     * @throws GovernanceException if the operation failed.
     */
    void activate() throws GovernanceException;

    /**
     * Method to deactivate this service.
     * @deprecated since active functionality is no longer used.
     *
     * @throws GovernanceException if the operation failed.
     */
    void deactivate() throws GovernanceException;

    /**
     * Method to obtain whether this service is active or not.
     * @deprecated since active functionality is no longer used.
     *
     * @return true if this service is active, and false if not.
     * @throws GovernanceException if the operation failed.
     */
    boolean isActive() throws GovernanceException;
}
