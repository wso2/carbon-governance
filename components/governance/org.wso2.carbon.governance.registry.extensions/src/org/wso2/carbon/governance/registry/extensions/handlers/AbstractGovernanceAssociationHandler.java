/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.registry.extensions.internal.GovernanceRegistryExtensionsDataHolder;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.session.UserRegistry;

/**
 * This is the convenient class to extends to listen add and remove association events, unlike Registry level handlers
 * this class provides source and target as GenericArtifacts and operate on governance level.
 *
 * @since 4.5.2
 *
 */
public abstract class AbstractGovernanceAssociationHandler extends Handler {

    public static final String SYSTEM_GOVERNANCE_ROOT_PATH = "/_system/governance";

    @Override
    public final void addAssociation(RequestContext requestContext) throws RegistryException {
        super.addAssociation(requestContext);
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        UserRegistry registry = GovernanceRegistryExtensionsDataHolder.getInstance().getRegistryService()
                .getGovernanceUserRegistry(tenantDomain);
        GenericArtifact source = (GenericArtifact) GovernanceUtils.retrieveGovernanceArtifactByPath(registry,
                                                                   getUserRegistryPath(requestContext.getSourcePath()));

        GenericArtifact target = (GenericArtifact) GovernanceUtils.retrieveGovernanceArtifactByPath(registry,
                                                                   getUserRegistryPath(requestContext.getTargetPath()));
        onAddAssociation(requestContext.getAssociationType(), source, target);
    }

    @Override
    public final void removeAssociation(RequestContext requestContext) throws RegistryException {
        super.removeAssociation(requestContext);
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        UserRegistry registry = GovernanceRegistryExtensionsDataHolder.getInstance().getRegistryService()
                .getGovernanceUserRegistry(tenantDomain);
        GenericArtifact source = (GenericArtifact) GovernanceUtils.retrieveGovernanceArtifactByPath(registry,
                                                                   getUserRegistryPath(requestContext.getSourcePath()));

        GenericArtifact target = (GenericArtifact) GovernanceUtils.retrieveGovernanceArtifactByPath(registry,
                                                                   getUserRegistryPath(requestContext.getTargetPath()));
        onRemoveAssociation(requestContext.getAssociationType(), source, target);
    }

    /**
     * This method get executed during the association creation but before commit to the repository.
     * @param association given association type.
     * @param source  source artifact as GenericArtifact
     * @param target  target artifact as GenericArtifact
     * @throws GovernanceException
     */
    public abstract void onAddAssociation(String association, GenericArtifact source,
                                          GenericArtifact target) throws GovernanceException;

    /**
     * This method get executed during the association removal but before commit to the repository.
     * @param association given association type.
     * @param source  source artifact as GenericArtifact
     * @param target  target artifact as GenericArtifact
     * @throws GovernanceException
     */
    public abstract void onRemoveAssociation(String association, GenericArtifact source,
                                             GenericArtifact target) throws GovernanceException;

    private String getUserRegistryPath(String sourcePath) {
        if (sourcePath.startsWith(SYSTEM_GOVERNANCE_ROOT_PATH)) {
            return sourcePath.replace(SYSTEM_GOVERNANCE_ROOT_PATH, "");
        }
        return sourcePath;
    }
}
