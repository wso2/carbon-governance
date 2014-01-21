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
package org.wso2.carbon.governance.api.wsdls.dataobjects;

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.endpoints.dataobjects.Endpoint;
import org.wso2.carbon.governance.api.endpoints.dataobjects.EndpointImpl;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.schema.dataobjects.Schema;
import org.wso2.carbon.governance.api.schema.dataobjects.SchemaImpl;


public interface Wsdl extends GovernanceArtifact {
    /**
     * Method to obtain the WSDL element of this WSDL artifact.
     *
     * @return the WSDL element.
     */
    OMElement getWsdlElement();

    /**
     * Method to set the WSDL element of this WSDL artifact.
     *
     * @param wsdlElement the WSDL element.
     */
    @SuppressWarnings("unused")
    void setWsdlElement(OMElement wsdlElement);

    /**
     * Attach a schema artifact to a WSDL artifact. Both the artifacts should be saved, before
     * calling this method.
     *
     * @param schema the schema to attach.
     *
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException if the operation failed.
     */
    @SuppressWarnings("unused")
    void attachSchema(Schema schema) throws GovernanceException;

    /**
     * Detach a schema artifact from a WSDL artifact. Both the artifacts should be saved, before
     * calling this method.
     *
     * @param schemaId the identifier of the schema to detach.
     *
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException if the operation failed.
     */
    @SuppressWarnings("unused")
    void detachSchema(String schemaId) throws GovernanceException;

    /**
     * Method to retrieve all schemas attached to this WSDL artifact.
     *
     * @return all schemas attached to this WSDL artifact.
     *
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException if the operation failed.
     */
    Schema[] getAttachedSchemas() throws GovernanceException;

    /**
     * Attach an endpoint artifact to a WSDL artifact. Both the artifacts should be saved, before
     * calling this method.
     *
     * @param endpoint the endpoint to attach.
     *
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException if the operation failed.
     */
    @SuppressWarnings("unused")
    void attachEndpoint(Endpoint endpoint) throws GovernanceException;

    /**
     * Detach an endpoint artifact from a WSDL artifact. Both the artifacts should be saved, before
     * calling this method.
     *
     * @param endpointId the identifier of the endpoint to detach.
     *
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException if the operation failed.
     */
    @SuppressWarnings("unused")
    void detachEndpoint(String endpointId) throws GovernanceException;

    /**
     * Method to retrieve all endpoints attached to this WSDL artifact.
     *
     * @return all endpoints attached to this WSDL artifact.
     *
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException if the operation failed.
     */
    Endpoint[] getAttachedEndpoints() throws GovernanceException;
}
