/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.governance.list.internal;

import org.apache.axis2.AxisFault;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.governance.api.util.GovernanceArtifactConfiguration;
import org.wso2.carbon.governance.api.util.GovernanceConstants;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.list.util.CommonUtil;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.HandlerManager;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.jdbc.handlers.filters.MediaTypeMatcher;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.extensions.services.RXTStoragePathService;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.component.xml.config.ManagementPermission;

import java.util.List;

/**
 * @scr.component name="org.wso2.carbon.governance.list"
 * immediate="true"
 * @scr.reference name="configuration.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="1..1"
 * policy="dynamic" bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService" cardinality="1..1"
 * policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 * @scr.reference name="extensions.service"
 * interface="org.wso2.carbon.registry.extensions.services.RXTStoragePathService" cardinality="1..1"
 * policy="dynamic" bind="setRxtStoragePathService" unbind="unsetRxtStoragePathService"
 */
public class GovernanceMgtUIListMetadataServiceComponent {

    private static Log log = LogFactory.getLog(GovernanceMgtUIListMetadataServiceComponent.class);
    private ServiceRegistration serviceRegistration;

    protected void activate(ComponentContext context) {
        final RegistryService registryService = CommonUtil.getRegistryService();
        try {
            Registry registry =
                    registryService.getGovernanceUserRegistry(CarbonConstants.REGISTRY_SYSTEM_USERNAME);
            CommonUtil.configureGovernanceArtifacts(registry,
                    CommonUtil.getConfigurationContext().getAxisConfiguration());
            HandlerManager handlerManager = registry.getRegistryContext().getHandlerManager();
            if (handlerManager != null) {
                handlerManager.addHandler(null,
                        new MediaTypeMatcher(
                                GovernanceConstants.GOVERNANCE_ARTIFACT_CONFIGURATION_MEDIA_TYPE),
                        new Handler() {
                            public void put(RequestContext requestContext)
                                    throws RegistryException {
                                if (!org.wso2.carbon.registry.extensions.utils.CommonUtil
                                        .isUpdateLockAvailable()) {
                                    return;
                                }
                                org.wso2.carbon.registry.extensions.utils.CommonUtil
                                        .acquireUpdateLock();
                                try {
                                    String rxtContent;
                                    Object rxtObj = requestContext.getResource().getContent();

                                    if (rxtObj instanceof byte[]) {
                                        rxtContent = RegistryUtils.decodeBytes(
                                                (byte[]) requestContext.getResource().getContent());
                                    } else if (rxtObj instanceof String) {
                                        rxtContent = rxtObj.toString();
                                    } else {
                                        throw new RegistryException("Invalid RXT");
                                    }
                                    if (!CommonUtil.validateXMLConfigOnSchema(rxtContent,
                                            "rxt-ui-config")) {
                                        throw new RegistryException("Violation of RXT definition in" +
                                                " configuration file, follow the schema correctly..!!");
                                    }
	                                GovernanceArtifactConfiguration artifactConfiguration =
			                                GovernanceUtils.getGovernanceArtifactConfiguration(rxtContent);
	                                CommonUtil.addStoragePath(artifactConfiguration.getMediaType(),artifactConfiguration.getPathExpression());

                                    Registry userRegistry = requestContext.getRegistry();
                                    userRegistry.put(
                                            requestContext.getResourcePath().getPath(),
                                            requestContext.getResource());
                                    Registry systemRegistry = requestContext.getSystemRegistry();
                                    CommonUtil.configureGovernanceArtifacts(systemRegistry,
                                            CommonUtil.getConfigurationContext().getAxisConfiguration());
                                    requestContext.setProcessingComplete(true);
                                } finally {
                                    org.wso2.carbon.registry.extensions.utils.CommonUtil
                                            .releaseUpdateLock();
                                }
                            }

                            public void delete(RequestContext requestContext) throws RegistryException {
                                Resource resource = requestContext.getResource();
                                Object content = resource.getContent();
                                String elementString;
                                if (content instanceof String) {
                                    elementString = (String) content;
                                } else {
                                    elementString = RegistryUtils.decodeBytes((byte[]) content);
                                }
                                GovernanceArtifactConfiguration artifactConfiguration =
                                        GovernanceUtils.getGovernanceArtifactConfiguration(elementString);
	                            CommonUtil.removeStoragePath(artifactConfiguration.getMediaType());
                                String needToDelete = artifactConfiguration.getKey();

                                UserRegistry systemRegistry =
                                        registryService.getRegistry(CarbonConstants.REGISTRY_SYSTEM_USERNAME);
                                if (systemRegistry.resourceExists(GovernanceConstants.ARTIFACT_CONTENT_PATH + needToDelete)) {
                                    systemRegistry.delete(GovernanceConstants.ARTIFACT_CONTENT_PATH + needToDelete);
                                }
                                List<GovernanceArtifactConfiguration> configurations =
                                        GovernanceUtils.findGovernanceArtifactConfigurations(systemRegistry);
                                GovernanceUtils.loadGovernanceArtifacts(systemRegistry, configurations);
                                for (GovernanceArtifactConfiguration configuration : configurations) {
                                    for (ManagementPermission uiPermission : configuration.getUIPermissions()) {
                                        String resourceId = RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                                                uiPermission.getResourceId();
                                        if (systemRegistry.resourceExists(resourceId) && needToDelete.equals(configuration.getKey())) {
                                            systemRegistry.delete(resourceId);
                                        }
                                    }
                                }

                                unDeployCRUDService(artifactConfiguration,
                                        CommonUtil.getConfigurationContext().getAxisConfiguration());
                            }
                        });
                handlerManager.addHandler(null,
                        new MediaTypeMatcher() {
                            public boolean handlePut(RequestContext requestContext)
                                    throws RegistryException {
                                Resource resource = requestContext.getResource();
                                if (resource == null) {
                                    return false;
                                }
                                String mType = resource.getMediaType();
                                return mType != null && (invert != (mType.matches(
                                        "application/vnd\\.[a-zA-Z0-9.-]+\\+xml") & !mType.matches(
                                        "application/vnd.wso2-service\\+xml")));
                            }

                            @Override
                            public boolean handleCreateLink(RequestContext requestContext) throws RegistryException {
                                String targetPath = requestContext.getTargetPath();
                                if (!requestContext.getRegistry().resourceExists(targetPath)) {
                                    return false;
                                }
                                Resource targetResource = requestContext.getRegistry().get(targetPath);
                                String mType = targetResource.getMediaType();

                                return mType != null && (invert != (mType.matches(
                                        "application/vnd\\.[a-zA-Z0-9.-]+\\+xml") & !mType.matches(
                                        "application/vnd.wso2-service\\+xml")));
                            }
                        },
                        new Handler() {
                            @Override
                            public void createLink(RequestContext requestContext) throws RegistryException {
                                String symlinkPath = requestContext.getResourcePath().getPath();

                                if (!symlinkPath.startsWith(RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH)) {
                                    throw new RegistryException("symlink creation is not allowed for artifact "
                                            + requestContext.getTargetPath());
                                }
                            }
                        });
            }
        } catch (RegistryException e) {
            log.error("Unable to load governance artifacts.", e);
        }
        log.debug("******* Governance List Metadata bundle is activated ******* ");
    }

    private void unDeployCRUDService(GovernanceArtifactConfiguration configuration, AxisConfiguration axisConfig) {
        String singularLabel = configuration.getSingularLabel();

        try {
            if (axisConfig.getService(singularLabel) != null) {
                axisConfig.removeService(singularLabel);
            }
        } catch (AxisFault axisFault) {
            log.error(axisFault);
        }
    }

    protected void setRegistryService(RegistryService registryService) {
        CommonUtil.setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        CommonUtil.setRegistryService(null);
    }

    protected void setConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        log.debug("The Configuration Context Service was set");
        if (configurationContextService != null) {
            CommonUtil.setConfigurationContext(configurationContextService.getServerConfigContext());
        }
    }
    protected void unsetConfigurationContextService(ConfigurationContextService configurationContextService) {
        CommonUtil.setConfigurationContext(null);
    }

	protected void setRxtStoragePathService(RXTStoragePathService rxtStoragePathService) {
		CommonUtil.setRxtStoragePathService(rxtStoragePathService);
	}

	protected void unsetRxtStoragePathService(RXTStoragePathService rxtStoragePathService) {
		CommonUtil.setRxtStoragePathService(null);
	}

}
