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

package org.wso2.carbon.governance.custom.lifecycles.checklist.util;

import org.wso2.carbon.governance.custom.lifecycles.checklist.beans.LifecycleBean;
import org.wso2.carbon.governance.registry.extensions.aspects.utils.LifecycleConstants;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.exceptions.ResourceNotFoundException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.common.utils.UserUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.utils.UUIDGenerator;
import org.wso2.carbon.user.core.UserRealm;

import java.util.*;

import javax.cache.Cache;
import org.wso2.carbon.registry.core.config.DataBaseConfiguration;
import org.wso2.carbon.registry.api.GhostResource;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.core.caching.RegistryCacheKey;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.config.Mount;
import org.wso2.carbon.registry.core.config.RemoteConfiguration;

public class LifecycleBeanPopulator {

    private static final Log log = LogFactory.getLog(LifecycleBeanPopulator.class);

    private static Map<String, Boolean> lifecycleAspects = new HashMap<String, Boolean>();

    public static LifecycleBean getLifecycleBean(String path, UserRegistry registry,
                                                 Registry systemRegistry) throws Exception {

        LifecycleBean lifecycleBean;
        ResourcePath resourcePath = new ResourcePath(path);
        UserRealm userRealm;
        boolean isTransactionStarted = false;
        try {
            Resource resource = registry.get(path);
            if (resource != null) {
                lifecycleBean = new LifecycleBean();
                lifecycleBean.setMediaType(resource.getMediaType());

                if (resource.getProperty("registry.link") != null &&
                        resource.getProperty("registry.mount") == null) {
                    lifecycleBean.setLink(true);
                    return lifecycleBean;
                }
                if (resource.getProperty("registry.mount") != null) {
                    lifecycleBean.setMounted(true);
                }
                List<String> aspects = resource.getAspects();

                if (aspects != null) {
                    LifecycleActions[] actions = new LifecycleActions[aspects.size()];

                    for (int i = 0; i < actions.length; i++) {
                        String aspect = aspects.get(i);

                        String[] aspectActions = registry.getAspectActions(resourcePath.getPath(), aspect);
                        if (aspectActions == null) continue;

                        LifecycleActions lifecycleActionsEntry = new LifecycleActions();
                        lifecycleActionsEntry.setLifecycle(aspect);
                        lifecycleActionsEntry.setActions(aspectActions);
                        actions[i] = lifecycleActionsEntry;
                    }

                    lifecycleBean.setAvailableActions(actions);
                }

                String[] aspectsToAdd = registry.getAvailableAspects();

                List<String> lifecycleAspectsToAdd = new LinkedList<String>();
                if (aspectsToAdd != null) {
                    String tempResourcePath = "/governance/lcm/" + UUIDGenerator.generateUUID();
                    for (String aspectToAdd : aspectsToAdd) {
                        if (systemRegistry.getRegistryContext().isReadOnly()) {
                            lifecycleAspectsToAdd.add(aspectToAdd);
                            continue;
                        }
                        Boolean isLifecycleAspect = lifecycleAspects.get(aspectToAdd);
                        if (isLifecycleAspect == null) {
                            if (!isTransactionStarted) {
                                systemRegistry.beginTransaction();
                                isTransactionStarted = true;
                            }
                            systemRegistry.put(tempResourcePath, systemRegistry.newResource());
                            systemRegistry.associateAspect(tempResourcePath, aspectToAdd);
                            Resource r  = systemRegistry.get(tempResourcePath);
                            Properties props = r.getProperties();
                            Set keys  = props.keySet();
                            for (Object key : keys) {
                                String propKey = (String) key;
                                if (propKey.startsWith("registry.lifecycle.")
                                        || propKey.startsWith("registry.custom_lifecycle.checklist.")) {
                                    isLifecycleAspect = Boolean.TRUE;
                                    break;
                                }
                            }
                            if (isLifecycleAspect == null) {
                                isLifecycleAspect = Boolean.FALSE;
                            }
                            lifecycleAspects.put(aspectToAdd, isLifecycleAspect);
                        }
                        if (isLifecycleAspect) {
                            lifecycleAspectsToAdd.add(aspectToAdd);
                        }
                    }
                    if (isTransactionStarted) {
                        systemRegistry.delete(tempResourcePath);
                        systemRegistry.rollbackTransaction();
                        isTransactionStarted = false;
                    }
                }
                lifecycleBean.setAspectsToAdd(lifecycleAspectsToAdd.toArray(
                        new String[lifecycleAspectsToAdd.size()]));

                // TODO - Following line that calls removeCache is to fix the issue related to life-cycle view not getting updated in the UI. The issue
                // happens mainly in the mount setup and also in non H2 databases, due to getLifeCyclebean method been called before the invokeAspect returns, making the
                // following registry.get() below the removeCache to get the non updated resource and updating the cache with old resource.
                // Further this issue doesn't seems like a caching issue. The issue to be traced is, why that this getLifecycleBean method getcalled before
                //  the invokeAspect, only in mount and non H2 DB setups. When that's found, remove the following line
                removeCache(registry, path);

                resource = registry.get(path);
                Properties props = resource.getProperties();
                List<Property> propList = new ArrayList<Property>();
                List<Property> voteList = new ArrayList<Property>();
                Iterator iKeys = props.keySet().iterator();
                while (iKeys.hasNext()) {
                    String propKey = (String) iKeys.next();

                    if (propKey.startsWith("registry.lifecycle.")
//                            || propKey.equals(Aspect.AVAILABLE_ASPECTS)
                            || propKey.startsWith("registry.custom_lifecycle.checklist.") || propKey.equals("registry.LC.name")){
//                            || propKey.startsWith("registry.custom_lifecycle.js.")) {
                        Property property = new Property();
                        property.setKey(propKey);
                        List<String> propValues = (List<String>) props.get(propKey);
                        property.setValues(propValues.toArray(new String[propValues.size()]));
                        propList.add(property);
                    } else if (propKey.startsWith("registry.custom_lifecycle.votes.option.")) {
                    	Property property = new Property();
                        property.setKey(propKey);
                        List<String> propValues = (List<String>) props.get(propKey);
                        if (propKey.endsWith(".vote")) {                        	
                            boolean userVoted = false;
                            for (String propValue : propValues) {
    							if (propValue.startsWith("users:")) {
    								String users = propValue.replace("users:", "");
    			                	String[] votedUsers = users.split(",");
    			                	userVoted = Arrays.asList(votedUsers).contains(registry.getUserName()); 
    							}
    						} 
                            propValues.add("uservote:"+userVoted); 
                        }                        
                    	
                        property.setValues(propValues.toArray(new String[propValues.size()]));
                        voteList.add(property);
                    }
                }
               
                lifecycleBean.setLifecycleProperties(propList.toArray(new Property[propList.size()]));
                lifecycleBean.setLifecycleApproval(voteList.toArray(new Property[voteList.size()]));

                lifecycleBean.setPathWithVersion(resourcePath.getPathWithVersion());
                lifecycleBean.setVersionView(!resourcePath.isCurrentVersion());
                lifecycleBean.setPutAllowed(
                        UserUtil.isPutAllowed(registry.getUserName(), resourcePath.getPath(), registry));
                lifecycleBean.setLoggedIn(!RegistryConstants.ANONYMOUS_USER.equals(registry.getUserName()));
                lifecycleBean.setShowAddDelete(true);

//                This is to add the roles of the current user. We are using this to enable disable actions in UI
                userRealm = registry.getUserRealm();
                String[] rolesList = userRealm.getUserStoreManager().getRoleListOfUser(registry.getUserName());
                lifecycleBean.setRolesOfUser(rolesList);

                resource.discard();
            }
            else {
                lifecycleBean = null;
            }
        }
        catch (ResourceNotFoundException rnfe) {
            lifecycleBean = null;  
        }
        catch (RegistryException e) {
            String msg = "Failed to get life cycle information of resource " +
                    resourcePath + ". " + e.getMessage();
            log.error(msg, e);
            throw new Exception(msg, e);
        } finally {
            if (isTransactionStarted) {
                systemRegistry.rollbackTransaction();
            }
        }

        return lifecycleBean;
    }

    private static void removeCache(Registry registry, String path) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        Cache<RegistryCacheKey, GhostResource> cache = RegistryUtils.getResourceCache(RegistryConstants.REGISTRY_CACHE_BACKED_ID);
        RegistryCacheKey cacheKey = null ;

        if(registry.getRegistryContext().getRemoteInstances().size() > 0) {
            for (Mount mount : registry.getRegistryContext().getMounts()) {
                for(RemoteConfiguration configuration : registry.getRegistryContext().getRemoteInstances()) {
                    if (path.startsWith(mount.getPath())) {
                        DataBaseConfiguration dataBaseConfiguration = registry.getRegistryContext().getDBConfig(configuration.getDbConfig());
                        String connectionId = (dataBaseConfiguration.getUserName() != null
                                ? dataBaseConfiguration.getUserName().split("@")[0]:dataBaseConfiguration.getUserName()) + "@" + dataBaseConfiguration.getDbUrl();
                        cacheKey = RegistryUtils.buildRegistryCacheKey(connectionId, tenantId, path);

                        if (cacheKey != null && cache.containsKey(cacheKey)) {
                            cache.remove(cacheKey);
                        }
                    }
                }
            }
        } else {
            DataBaseConfiguration dataBaseConfiguration = registry.getRegistryContext().getDefaultDataBaseConfiguration();
            String connectionId = (dataBaseConfiguration.getUserName() != null
                    ? dataBaseConfiguration.getUserName().split("@")[0]:dataBaseConfiguration.getUserName()) + "@" + dataBaseConfiguration.getDbUrl();
            cacheKey = RegistryUtils.buildRegistryCacheKey(connectionId, tenantId, path);

            if (cacheKey != null && cache.containsKey(cacheKey)) {
                cache.remove(cacheKey);
            }
        }
    }
}
