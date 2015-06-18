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

import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.registry.extensions.handlers.utils.HandlerConstants;
import org.wso2.carbon.governance.registry.extensions.internal.GovernanceRegistryExtensionsComponent;
import org.wso2.carbon.governance.registry.extensions.internal.GovernanceRegistryExtensionsDataHolder;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.session.CurrentSession;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.extensions.utils.CommonUtil;

import javax.xml.namespace.QName;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.UUID;

public class PolicyUriHandler{
    private static final Log log = LogFactory.getLog(PolicyUriHandler.class);
    private Registry governanceUserRegistry;
    private Registry registry;

    public void importResource(RequestContext requestContext, String sourceURL) throws RegistryException {
        if (!CommonUtil.isUpdateLockAvailable()) {
            return;
        }
        CommonUtil.acquireUpdateLock();
        log.debug("Processing Policy URI started");

        registry = requestContext.getRegistry();
        int tenantId = CurrentSession.getTenantId();
        String userName = CurrentSession.getUser();
        this.governanceUserRegistry = GovernanceRegistryExtensionsDataHolder.getInstance().getRegistryService()
                .getGovernanceUserRegistry(userName, tenantId);

        try {

            InputStream inputStream;
            try {
                if (sourceURL != null && sourceURL.toLowerCase().startsWith("file:")) {
                    String msg = "The source URL must not be file in the server's local file system.";
                    throw new RegistryException(msg);
                }
                inputStream = new URL(sourceURL).openStream();
            } catch (IOException e) {
                throw new RegistryException("The URL " + sourceURL + " is incorrect.", e);
            }
            addPolicyToRegistry(requestContext, inputStream, sourceURL);
            log.debug("Processing Policy URI finished");
        } finally {
            CommonUtil.releaseUpdateLock();
        }
    }

    private void addPolicyToRegistry(RequestContext requestContext, InputStream inputStream, String sourceURL) throws RegistryException {
        Resource policyResource;
        if (requestContext.getResource() == null) {
            policyResource = new ResourceImpl();
            policyResource.setMediaType("application/policy+xml");
        } else {
            policyResource = requestContext.getResource();
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int nextChar;
        try {
            while ((nextChar = inputStream.read()) != -1) {
                outputStream.write(nextChar);
            }
            outputStream.flush();
        } catch (IOException e) {
            throw new RegistryException("Exception occured while reading policy content.", e);
        }
        policyResource.setContent(outputStream.toByteArray());

        try {
            AXIOMUtil.stringToOM(RegistryUtils.decodeBytes(outputStream.toByteArray()));
        } catch (Exception e) {
            throw new RegistryException("The given policy file does not contain valid XML.");
        }

        String resourcePath = requestContext.getResourcePath().getPath();
        String policyFileName = resourcePath.substring(resourcePath.lastIndexOf(RegistryConstants.PATH_SEPARATOR) + 1);
        Registry systemRegistry = CommonUtil.getUnchrootedSystemRegistry(requestContext);
        String commonLocation = getChrootedLocation(requestContext.getRegistryContext());
        if (!systemRegistry.resourceExists(commonLocation)) {
            systemRegistry.put(commonLocation, systemRegistry.newCollection());
        }
        String policyPath = commonLocation + extractResourceFromURL(policyFileName, ".xml");


        String policyId = policyResource.getUUID();
        if (policyId == null) {
            // generate a service id
            policyId = UUID.randomUUID().toString();
            policyResource.setUUID(policyId);
        }

        String relativeArtifactPath = RegistryUtils.getRelativePath(registry.getRegistryContext(), policyPath);
        // adn then get the relative path to the GOVERNANCE_BASE_PATH
        relativeArtifactPath = RegistryUtils.getRelativePathToOriginal(relativeArtifactPath,
                RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH);
        addPolicyToRegistry(policyPath, sourceURL, policyResource);
        ((ResourceImpl)policyResource).setPath(relativeArtifactPath);

        requestContext.setProcessingComplete(true);
    }

    /**
     * Method that gets called instructing a policy to be added the registry.
     *
     * @param path     the path to add the resource to.
     * @param url      the path from which the resource was imported from.
     * @param resource the resource to be added.
     *
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException if the operation failed.
     */
    protected void addPolicyToRegistry(String path, String url, Resource resource) throws RegistryException {
        String source = getSource(path);
        GenericArtifactManager genericArtifactManager = new GenericArtifactManager(governanceUserRegistry, "uri");
        GenericArtifact policy = genericArtifactManager.newGovernanceArtifact(new QName(source));
        policy.setId(resource.getUUID());
        policy.setAttribute("overview_name", source);
        policy.setAttribute("overview_uri", url);
        policy.setAttribute("overview_type", HandlerConstants.POLICY);
        genericArtifactManager.addGenericArtifact(policy);
    }

    private String extractResourceFromURL(String policyURL, String suffix) {
        String resourceName = policyURL;
        if (policyURL.lastIndexOf("?") > 0) {
            resourceName = policyURL.substring(0, policyURL.indexOf("?")) + suffix;
        } else if (policyURL.indexOf(".") > 0) {
            resourceName = policyURL.substring(0, policyURL.lastIndexOf(".")) + suffix;
        } else if (!policyURL.endsWith(suffix)) {
            resourceName = policyURL + suffix;
        }
        return resourceName;
    }

    private String getChrootedLocation(RegistryContext registryContext) {
        return RegistryUtils.getAbsolutePath(registryContext,
                RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + HandlerConstants.POLICY_LOCATION);
    }

    public static String getSource(String uri){
        return uri.split("/")[uri.split("/").length -1];
    }
}
