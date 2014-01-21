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
import org.uddi.api_v3.AuthToken;
import org.wso2.carbon.governance.registry.extensions.handlers.utils.WsdlUriProcessor;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourcePath;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.extensions.beans.BusinessServiceInfo;
import org.wso2.carbon.registry.extensions.handlers.utils.UDDIPublisher;
import org.wso2.carbon.registry.extensions.utils.CommonConstants;
import org.wso2.carbon.registry.extensions.utils.CommonUtil;
import org.wso2.carbon.registry.uddi.utils.UDDIUtil;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class WsdlUriHandler {
    private static final Log log = LogFactory.getLog(WsdlUriHandler.class);

    public void importResource(RequestContext requestContext, String sourceURL) throws RegistryException {
        if (!CommonUtil.isUpdateLockAvailable()) {
            return;
        }
        CommonUtil.acquireUpdateLock();
        WsdlUriProcessor wsdlUriProcessor;
        log.debug("Processing WSDL URI started");
        try {
            Resource metadata = requestContext.getResource();
            if (requestContext.getSourceURL() != null &&
                    requestContext.getSourceURL().toLowerCase().startsWith("file:")) {
                String msg = "The source URL must not be file in the server's local file system.";
                throw new RegistryException(msg);
            }
            try {
                wsdlUriProcessor = buildWsdlUriProcessor(requestContext);
                String wsdlPath =
                        processWSDLImport(requestContext, wsdlUriProcessor, metadata, sourceURL);
                ResourcePath resourcePath = requestContext.getResourcePath();
                String path = null;
                if (resourcePath != null) {
                    path = resourcePath.getPath();
                }
                onPutCompleted(path,
                        Collections.singletonMap(sourceURL, wsdlPath),
                        Collections.<String>emptyList(), requestContext);
                requestContext.setActualPath(wsdlPath);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                throw new RegistryException(e.getMessage(), e);
            }

            log.debug("Processing WSDL URI finished");
            requestContext.setProcessingComplete(true);

            if (wsdlUriProcessor != null && CommonConstants.ENABLE.equals(System.getProperty(CommonConstants.UDDI_SYSTEM_PROPERTY))) {
                AuthToken authToken = UDDIUtil.getPublisherAuthToken();
                if(authToken == null){
                    return;
                }
                BusinessServiceInfo businessServiceInfo = new BusinessServiceInfo();
                businessServiceInfo.setServiceWSDLInfo(wsdlUriProcessor.getMasterWSDLInfo());
                UDDIPublisher publisher = new UDDIPublisher();
                publisher.publishBusinessService(authToken, businessServiceInfo);
            }
        } finally {
            CommonUtil.releaseUpdateLock();
        }
    }

    /**
     * Method that runs the WSDL import/upload procedure.
     *
     * @param requestContext the request context for the import/put operation
     * @param metadata the resource metadata
     * @param sourceURL the URL from which the WSDL is imported
     * @param wsdlUriProcessor the WSDL Processor instance, used for upload and validation
     *
     * @return the path at which the WSDL was uploaded to
     *
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException if the operation failed.
     */
    protected String processWSDLImport(RequestContext requestContext, WsdlUriProcessor wsdlUriProcessor,
                                       Resource metadata, String sourceURL)
            throws RegistryException {
        return wsdlUriProcessor.addWSDLToRegistry(requestContext, sourceURL, metadata, false);
    }

    /**
     * Method that will executed after the put operation has been done.
     *
     * @param path the path of the resource.
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

    /**
     * Method to customize the WSDL Processor.
     * @param requestContext the request context for the import/put operation
     * @return the WSDL Processor instance.
     */
    @SuppressWarnings("unused")
    protected WsdlUriProcessor buildWsdlUriProcessor(RequestContext requestContext) {
        return new WsdlUriProcessor(requestContext);
    }
}
