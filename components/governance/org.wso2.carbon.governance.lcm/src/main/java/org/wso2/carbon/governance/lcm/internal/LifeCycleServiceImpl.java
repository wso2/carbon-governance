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
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.lcm.beans.*;
import org.wso2.carbon.governance.lcm.exception.LifeCycleException;
import org.wso2.carbon.governance.lcm.services.LifeCycleService;
import org.wso2.carbon.governance.lcm.util.CommonUtil;
import org.wso2.carbon.governance.lcm.util.LifecycleStateDurationUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * API implementation of the LifeCycleService(Used to fetch lifecycle information) .
 */
public class LifeCycleServiceImpl implements LifeCycleService {
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
            String path = GovernanceUtils.getDirectArtifactPath(registry,artifactId);
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
    public LifeCycleStateBean getLifeCycleStateBean(String artifactId, String artifactLC)
            throws LifeCycleException {
        try {
            UserRegistry registry = (UserRegistry) getGovernanceUserRegistry();
            UserRealm userRealm = registry.getUserRealm();
            String[] roleNames = userRealm.getUserStoreManager().getRoleListOfUser(registry.getUserName());
            String path = GovernanceUtils.getArtifactPath(registry, artifactId);
            if (path != null) {
                Resource resource = registry.get(path);
                return getCheckListItems(resource, artifactLC, roleNames, registry);
            } else {
                throw new LifeCycleException("Unable to find the artifact " + artifactId);
            }
        } catch (UserStoreException | RegistryException e) {
            throw new LifeCycleException(e);
        }

    }

    @Override
    public List<LifeCycleStateBean> getLifeCycleStateBeans(String artifactId)
            throws LifeCycleException {
        try {
            List<LifeCycleStateBean> lifeCycleStateBeans = new ArrayList<>();
            UserRegistry registry = (UserRegistry) getGovernanceUserRegistry();
            UserRealm userRealm = registry.getUserRealm();
            String[] rolesList = userRealm.getUserStoreManager().getRoleListOfUser(registry.getUserName());
            String path = GovernanceUtils.getArtifactPath(registry, artifactId);
            if (path != null) {
                Resource resource = registry.get(path);
                List<String> aspects = resource.getAspects();
                if (aspects != null) {
                    for (String aspect : aspects) {
                        LifeCycleStateBean lifeCycleStateBean =
                                getCheckListItems(resource, aspect, rolesList, registry);
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

    private LifeCycleStateBean getCheckListItems(Resource artifactResource, String artifactLC, String[] roleNames,
                                                 UserRegistry registry)
            throws RegistryException {

        String artifactLCState = artifactResource.getProperty("registry.lifecycle." + artifactLC + ".state");

        LifeCycleStateBean lifeCycleStateBean = new LifeCycleStateBean();

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

        Set propertyKeys = lifecycleProps.keySet();

        for (Object propertyObj : propertyKeys) {
            String propertyKey = (String) propertyObj;
            String checkListPrefix = "registry.custom_lifecycle.checklist.";
            String permissionSuffix = ".item.permission";

            String votePrefix = "registry.custom_lifecycle.votes.";
            String votePermissionSuffix = ".vote.permission";
            if (propertyKey.startsWith(checkListPrefix) && propertyKey.endsWith(permissionSuffix) &&
                propertyKey.contains(artifactLC)) {
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
                propertyKey.contains(artifactLC)) {
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

        for (Object propertyObj : propertyKeys) {
            String propertyKey = (String) propertyObj;

            String checkListPrefix = "registry.custom_lifecycle.checklist.";
            String checkListSuffix = ".item";

            String prefixVote = "registry.custom_lifecycle.votes.";
            String suffixVote = ".vote";

            if (propertyKey.startsWith(checkListPrefix) && propertyKey.endsWith(checkListSuffix) &&
                propertyKey.contains(artifactLC)) {
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
                       propertyKey.contains(artifactLC)) {
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

        return lifeCycleStateBean;
    }
}
