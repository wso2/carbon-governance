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

package org.wso2.carbon.governance.registry.extensions.handlers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.wso2.carbon.governance.registry.extensions.handlers.utils.HandlerConstants;
import org.wso2.carbon.governance.registry.extensions.handlers.utils.SchemaUriProcessor;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.extensions.handlers.utils.SchemaValidator;
import org.wso2.carbon.registry.extensions.utils.CommonUtil;
import org.wso2.carbon.registry.extensions.utils.WSDLValidationInfo;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SchemaUriHandler {
    private static final Log log = LogFactory.getLog(SchemaUriHandler.class);

    public void importResource(RequestContext requestContext, String sourceURL) throws RegistryException {
        if (!CommonUtil.isUpdateLockAvailable()) {
            return;
        }
        CommonUtil.acquireUpdateLock();
        log.debug("Processing Schema URI started");
        try {
            String resourcePath = requestContext.getResourcePath().getCompletePath();

            WSDLValidationInfo validationInfo;
            try {
                validationInfo =
                        SchemaValidator.validate(new XMLInputSource(null, sourceURL, null));
            } catch (Exception e) {
                throw new RegistryException("Exception occured while validating the schema.", e);
            }

            String savedName = processSchemaImport(requestContext, resourcePath, validationInfo, sourceURL);

            onPutCompleted(resourcePath,
                    Collections.singletonMap(sourceURL, savedName),
                    Collections.<String>emptyList(), requestContext);

            requestContext.setActualPath(savedName);
            log.debug("Processing Schema URI finished");
            requestContext.setProcessingComplete(true);
        } finally {
            CommonUtil.releaseUpdateLock();
        }
    }

    /**
     * Method to customize the Schema Processor.
     *
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
     * Method that runs the schema import procedure.
     *
     * @param requestContext the request context for the import operation
     * @param resourcePath   the path of the resource
     * @param validationInfo the validation information
     * @param sourceURL url of the source
     * @return the path at which the schema was uploaded to
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException if the operation failed.
     */
    protected String processSchemaImport(RequestContext requestContext, String resourcePath,
                                         WSDLValidationInfo validationInfo, String sourceURL) throws RegistryException {
        SchemaUriProcessor schemaProcessor =
                buildSchemaProcessor(requestContext, validationInfo);

        return schemaProcessor
                .importSchemaToRegistry(requestContext, resourcePath,
                        getChrootedLocation(requestContext.getRegistryContext()), sourceURL);
    }

    /**
     * Method that will executed after the put operation has been done.
     *
     * @param path           the path of the resource.
     * @param addedResources the resources that have been added to the registry.
     * @param otherResources the resources that have not been added to the registry.
     * @param requestContext the request context for the put operation.
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException if the operation failed.
     */
    @SuppressWarnings("unused")
    protected void onPutCompleted(String path, Map<String, String> addedResources,
                                  List<String> otherResources, RequestContext requestContext)
            throws RegistryException {
    }

    private String getChrootedLocation(RegistryContext registryContext) {
        return RegistryUtils.getAbsolutePath(registryContext,
                RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + HandlerConstants.XSD_LOCATION);
    }

}
