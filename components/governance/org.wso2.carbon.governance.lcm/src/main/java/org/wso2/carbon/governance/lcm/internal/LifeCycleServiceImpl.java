/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.governance.lcm.internal;

import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.util.GovernanceConstants;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.lcm.beans.DurationBean;
import org.wso2.carbon.governance.lcm.beans.LCStateBean;
import org.wso2.carbon.governance.lcm.beans.LifeCycleActionsBean;
import org.wso2.carbon.governance.lcm.beans.LifeCycleApprovalBean;
import org.wso2.carbon.governance.lcm.beans.LifeCycleCheckListItemBean;
import org.wso2.carbon.governance.lcm.beans.LifeCycleInputBean;
import org.wso2.carbon.governance.lcm.exception.LifeCycleException;
import org.wso2.carbon.governance.lcm.services.LifeCycleService;
import org.wso2.carbon.governance.lcm.util.CommonUtil;
import org.wso2.carbon.governance.lcm.util.LifecycleStateDurationUtils;
import org.wso2.carbon.registry.api.GhostResource;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.caching.RegistryCacheKey;
import org.wso2.carbon.registry.core.config.DataBaseConfiguration;
import org.wso2.carbon.registry.core.config.Mount;
import org.wso2.carbon.registry.core.config.RemoteConfiguration;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.cache.Cache;

/**
 * API implementation of the LifeCycleService(Used to fetch lifecycle information) .
 */
public class LifeCycleServiceImpl implements LifeCycleService {

    public static final String REGISTRY_CUSTOM_LIFECYCLE_INPUTS = "registry.custom_lifecycle.inputs.";

    @Override
    public boolean createLifecycle(String lifecycleConfiguration) throws LifeCycleException {
        throw new UnsupportedOperationException("Not implemented, yet");
    }

    @Override
    public boolean updateLifecycle(String lifCycleName, String lifecycleConfiguration)
            throws LifeCycleException {
        throw new UnsupportedOperationException("Not implemented, yet");
    }

    @Override
    public boolean deleteLifecycle(String lifCycleName) throws LifeCycleException {
        throw new UnsupportedOperationException("Not implemented, yet");
    }

    @Override
    public boolean isLifecycleNameInUse(String name) throws LifeCycleException {
        throw new UnsupportedOperationException("Not implemented, yet");
    }

    @Override
    public boolean validateLifeCycleConfiguration(String lifecycleConfiguration) throws LifeCycleException {
        throw new UnsupportedOperationException("Not implemented, yet");
    }

    @Override
    public DurationBean getLifecycleCurrentStateDuration(String artifactId, String lifecycleName)
            throws LifeCycleException {
        try {
            Registry registry = CommonUtil.getRootSystemRegistry(getTenantId());
            String path = GovernanceUtils.getDirectArtifactPath(registry, artifactId);
            if (path != null) {
                return LifecycleStateDurationUtils.getCurrentLifecycleStateDuration(path, lifecycleName,
                                                                                    registry);
            } else {
                throw new LifeCycleException("Unable to find the artifact " + artifactId);
            }

        } catch (RegistryException e) {
            throw new LifeCycleException(e);
        }

    }

    @Override
    public LCStateBean getLifeCycleStateBean(String artifactId, String artifactLC)
            throws LifeCycleException {
        try {
            UserRegistry registry = (UserRegistry) getGovernanceUserRegistry();
            UserRealm userRealm = registry.getUserRealm();
            String[] roleNames = userRealm.getUserStoreManager().getRoleListOfUser(registry.getUserName());
            String path = GovernanceUtils.getArtifactPath(registry, artifactId);
            if (path != null) {
                removeCache(registry, path);
                Resource resource = registry.get(path);
                LCStateBean lifeCycleStateBean = getCheckListItems(resource, artifactLC, roleNames, registry);

                GovernanceArtifact governanceArtifact =
                        GovernanceUtils.retrieveGovernanceArtifactById(registry, artifactId);

                Map<String, String> currentStateDurationData = governanceArtifact
                        .getCurrentStateDuration(artifactId, artifactLC);

                if (currentStateDurationData != null && !currentStateDurationData.isEmpty()) {
                    lifeCycleStateBean
                            .setLifeCycleCurrentStateDuration(currentStateDurationData.get("currentStateDuration"));
                    lifeCycleStateBean
                            .setLifeCycleCurrentStateDurationColour(currentStateDurationData.get("durationColour"));

                }

                return lifeCycleStateBean;
            } else {
                throw new LifeCycleException("Unable to find the artifact " + artifactId);
            }
        } catch (UserStoreException | RegistryException e) {
            throw new LifeCycleException(e);
        }

    }

    @Override
    public List<LCStateBean> getLifeCycleStateBeans(String artifactId)
            throws LifeCycleException {
        try {
            List<LCStateBean> lifeCycleStateBeans = new ArrayList<>();
            UserRegistry registry = (UserRegistry) getGovernanceUserRegistry();
            UserRealm userRealm = registry.getUserRealm();
            String[] rolesList = userRealm.getUserStoreManager().getRoleListOfUser(registry.getUserName());
            String path = GovernanceUtils.getArtifactPath(registry, artifactId);
            if (path != null) {
                Resource resource = registry.get(path);
                List<String> aspects = resource.getAspects();
                if (aspects != null) {
                    for (String aspect : aspects) {
                        LCStateBean lifeCycleStateBean =
                                getCheckListItems(resource, aspect, rolesList, registry);

                        GovernanceArtifact governanceArtifact =
                                GovernanceUtils.retrieveGovernanceArtifactByPath(registry, resource.getPath());
                        lifeCycleStateBean.setLifeCycleCurrentStateDuration(governanceArtifact.getCurrentStateDuration
                                (resource.getPath(), aspect).get("currentStateDuration"));
                        lifeCycleStateBean.setLifeCycleCurrentStateDurationColour(
                                governanceArtifact.getCurrentStateDuration(resource.getPath(), aspect)
                                        .get("durationColour"));

                        lifeCycleStateBeans.add(lifeCycleStateBean);
                    }
                }
            } else {
                throw new LifeCycleException("Unable to find the artifact " + artifactId);
            }
            return lifeCycleStateBeans;
        } catch (UserStoreException | RegistryException e) {
            throw new LifeCycleException(e);
        }
    }

    private Registry getGovernanceUserRegistry() {
        return (Registry) PrivilegedCarbonContext.getThreadLocalCarbonContext().
                getRegistry(RegistryType.USER_GOVERNANCE);
    }

    private int getTenantId() {
        return PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
    }

    private LCStateBean getCheckListItems(Resource artifactResource, String artifactLC, String[] roleNames,
                                                 UserRegistry registry)
            throws RegistryException {

        String artifactLCState = artifactResource.getProperty("registry.lifecycle." + artifactLC + ".state");

        LCStateBean lifeCycleStateBean = new LCStateBean();
        if (artifactLCState == null) {
            return lifeCycleStateBean;
        }
        lifeCycleStateBean.setLifeCycleName(artifactLC);
        lifeCycleStateBean.setLifeCycleState(artifactLCState);

        List<LifeCycleCheckListItemBean> checkListItemList = new ArrayList<>();
        List<LifeCycleApprovalBean> lifeCycleApprovalBeanList = new ArrayList<>();

        String[] aspectActions = registry.getAspectActions(artifactResource.getPath(), artifactLC);
        if (aspectActions != null && aspectActions.length > 0) {
            LifeCycleActionsBean lifecycleActionsEntry = new LifeCycleActionsBean();
            lifecycleActionsEntry.setLifecycle(artifactLC);
            lifecycleActionsEntry.setActions(aspectActions);
            lifeCycleStateBean.setLifeCycleActionsBean(lifecycleActionsEntry);
        }

        Properties lifecycleProps = artifactResource.getProperties();

        List<String> permissionList = new ArrayList();
        List<String> approvePermissionList = new ArrayList();


        Map<String, List<LifeCycleInputBean>> lifeCycleInputBeanMap = new HashMap();

        Set propertyKeys = lifecycleProps.keySet();

        for (Object propertyObj : propertyKeys) {
            String propertyKey = (String) propertyObj;
            String checkListPrefix = "registry.custom_lifecycle.checklist.";
            String permissionSuffix = ".item.permission";

            String votePrefix = "registry.custom_lifecycle.votes.";
            String votePermissionSuffix = ".vote.permission";
            if (propertyKey.startsWith(checkListPrefix) && propertyKey.endsWith(permissionSuffix) &&
                    propertyKey.contains(GovernanceConstants.LIFECYCLE_PROPERTY_SEPERATOR + artifactLC
                    + GovernanceConstants.LIFECYCLE_PROPERTY_SEPERATOR)) {
                for (String role : roleNames) {
                    List<String> propValues = (List<String>) lifecycleProps.get(propertyKey);
                    for (String propValue : propValues) {
                        String key = propertyKey.replace(checkListPrefix, "").replace(permissionSuffix, "");
                        if (propValue.equals(role)) {
                            permissionList.add(key);
                        } else if (propValue.startsWith(checkListPrefix) && propValue.endsWith(permissionSuffix)) {
                            permissionList.add(key);
                        }
                    }
                }
            }
            if (propertyKey.startsWith(votePrefix) && propertyKey.endsWith(votePermissionSuffix) &&
                    propertyKey.contains(GovernanceConstants.LIFECYCLE_PROPERTY_SEPERATOR + artifactLC
                    + GovernanceConstants.LIFECYCLE_PROPERTY_SEPERATOR)) {
                for (String role : roleNames) {
                    List<String> propValues = (List<String>) lifecycleProps.get(propertyKey);
                    for (String propValue : propValues) {
                        String key = propertyKey.replace(votePrefix, "").replace(votePermissionSuffix, "");
                        if (propValue.equals(role)) {
                            approvePermissionList.add(key);
                        } else if (propValue.startsWith(votePrefix) && propValue.endsWith(votePermissionSuffix)) {
                            approvePermissionList.add(key);
                        }
                    }
                }
            }
        }
        String lifecyleInputs = REGISTRY_CUSTOM_LIFECYCLE_INPUTS;
        for (Object key : lifecycleProps.keySet()) {
            if(key.toString().startsWith(lifecyleInputs + artifactLC + "." + artifactLCState)){
                List<String> propValues = (List<String>) lifecycleProps.get(key);
                LifeCycleInputBean lifeCycleInputBean = new LifeCycleInputBean();
                lifeCycleInputBean.setName(propValues.get(1));
                lifeCycleInputBean.setRequired(Boolean.getBoolean(propValues.get(2)));
                lifeCycleInputBean.setLabel(propValues.get(3));
                lifeCycleInputBean.setPlaceHolder(propValues.get(4));
                lifeCycleInputBean.setTooltip(propValues.get(5));
                lifeCycleInputBean.setRegex(propValues.get(6));
                List<LifeCycleInputBean> lifeCycleInputBeanList = lifeCycleInputBeanMap.get(propValues.get(0));
                if (lifeCycleInputBeanList == null){
                    lifeCycleInputBeanList  =  new ArrayList<LifeCycleInputBean>();
                }
                lifeCycleInputBeanList.add(lifeCycleInputBean);
                lifeCycleInputBeanMap.put(propValues.get(0),lifeCycleInputBeanList);
            }
        }

        for (Object propertyObj : propertyKeys) {
            String propertyKey = (String) propertyObj;

            String checkListPrefix = "registry.custom_lifecycle.checklist.";
            String checkListSuffix = ".item";

            String prefixVote = "registry.custom_lifecycle.votes.";
            String suffixVote = ".vote";

            if (propertyKey.startsWith(checkListPrefix) && propertyKey.endsWith(checkListSuffix) &&
                    propertyKey.contains(GovernanceConstants.LIFECYCLE_PROPERTY_SEPERATOR + artifactLC
                    + GovernanceConstants.LIFECYCLE_PROPERTY_SEPERATOR)) {
                List<String> propValues = (List<String>) lifecycleProps.get(propertyKey);
                LifeCycleCheckListItemBean checkListItem = new LifeCycleCheckListItemBean();
                if (propValues != null && propValues.size() > 2) {
                    for (String param : propValues) {
                        if ((param.startsWith("status:"))) {
                            checkListItem.setStatus(param.substring(7));
                        } else if ((param.startsWith("name:"))) {
                            checkListItem.setName(param.substring(5));
                        } else if ((param.startsWith("value:"))) {
                            checkListItem.setValue(Boolean.parseBoolean(param.substring(6)));
                        } else if ((param.startsWith("order:"))) {
                            checkListItem.setOrder(Integer.parseInt(param.substring(6)));
                        }
                    }
                }
                String key = propertyKey.replace(checkListPrefix, "").replace(checkListSuffix, "");
                if (permissionList.contains(key)) {
                    checkListItem.setVisible("true");
                }

                checkListItemList.add(checkListItem);
            } else if (propertyKey.startsWith(prefixVote) && propertyKey.endsWith(suffixVote) &&
                    propertyKey.contains(GovernanceConstants.LIFECYCLE_PROPERTY_SEPERATOR + artifactLC
                            + GovernanceConstants.LIFECYCLE_PROPERTY_SEPERATOR)) {
                List<String> propValues = (List<String>) lifecycleProps.get(propertyKey);
                LifeCycleApprovalBean approveItem = new LifeCycleApprovalBean();
                approveItem.setVisible("false");
                if (propValues != null && propValues.size() > 2) {
                    for (String param : propValues) {
                        if ((param.startsWith("name:"))) {
                            approveItem.setName(param.substring(5));
                        }
                        if ((param.startsWith("uservote:"))) {
                            approveItem.setValue(Boolean.getBoolean(param.substring(9)));
                        }
                        if ((param.startsWith("votes:"))) {
                            approveItem.setRequiredVote(Integer.parseInt(param.substring(6)));
                        }
                        if ((param.startsWith("current:"))) {
                            approveItem.setCurrentVote(Integer.parseInt(param.substring(8)));
                        }
                        if ((param.startsWith("order:"))) {
                            approveItem.setOrder(Integer.parseInt(param.substring(6)));
                        }
                    }
                }
                String key = propertyKey.replace(prefixVote, "").replace(suffixVote, "");

                if (approvePermissionList.contains(key)) {
                    approveItem.setVisible("true");
                }
                lifeCycleApprovalBeanList.add(approveItem);
            }

        }
        lifeCycleStateBean.setLifeCycleApprovalBeanList(lifeCycleApprovalBeanList);
        lifeCycleStateBean.setLifeCycleCheckListItemBeans(checkListItemList);
        lifeCycleStateBean.setLifeCycleInputBeanMap(lifeCycleInputBeanMap);

        return lifeCycleStateBean;
    }

    /**
     * This method clears the cache for particular resource at given path.
     * @param registry
     * @param path
     */
    private static void removeCache(Registry registry, String path) {
        String updatedPath = RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + path;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        Cache<RegistryCacheKey, GhostResource> cache = RegistryUtils.
                getResourceCache(RegistryConstants.REGISTRY_CACHE_BACKED_ID);
        RegistryCacheKey cacheKey = null;

        if (registry.getRegistryContext().getRemoteInstances().size() > 0) {
            for (Mount mount : registry.getRegistryContext().getMounts()) {
                for (RemoteConfiguration configuration : registry.getRegistryContext().getRemoteInstances()) {
                    if (updatedPath.startsWith(mount.getPath())) {
                        DataBaseConfiguration dataBaseConfiguration = registry.getRegistryContext().
                                getDBConfig(configuration.getDbConfig());
                        String connectionId = (dataBaseConfiguration.getUserName() != null
                                               ? dataBaseConfiguration.getUserName().
                                split("@")[0] : dataBaseConfiguration.getUserName()) + "@" + dataBaseConfiguration.getDbUrl();
                        cacheKey = RegistryUtils.buildRegistryCacheKey(connectionId, tenantId, updatedPath);

                        if (cacheKey != null && cache.containsKey(cacheKey)) {
                            cache.remove(cacheKey);
                        }
                    }
                }
            }
        } else {
            DataBaseConfiguration dataBaseConfiguration = registry.getRegistryContext().getDefaultDataBaseConfiguration();
            String connectionId = (dataBaseConfiguration.getUserName() != null
                                   ? dataBaseConfiguration.getUserName().
                    split("@")[0] : dataBaseConfiguration.getUserName()) + "@" + dataBaseConfiguration.getDbUrl();
            cacheKey = RegistryUtils.buildRegistryCacheKey(connectionId, tenantId, updatedPath);

            if (cacheKey != null && cache.containsKey(cacheKey)) {
                cache.remove(cacheKey);
            }
        }
    }
}
