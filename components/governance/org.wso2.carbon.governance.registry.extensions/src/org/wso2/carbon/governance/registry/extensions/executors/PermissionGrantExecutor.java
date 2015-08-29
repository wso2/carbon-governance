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

package org.wso2.carbon.governance.registry.extensions.executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.registry.extensions.interfaces.Execution;
import org.wso2.carbon.governance.registry.extensions.internal.GovernanceRegistryExtensionsDataHolder;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PermissionGrantExecutor implements Execution {

    private static final Log log = LogFactory.getLog(PermissionGrantExecutor.class);

    private UserRealm userRealm;
    private int tenantId;
    private List<RolePermission> list = new ArrayList<RolePermission>();
    private static final String READ = "read";
    private static final String WRITE = "write";
    private static final String DELETE = "delete";
    private static final String ADD = "add";

    @Override
    public void init(Map parameterMap) {
        obtainTenantId();
        obtainUserRealm();
        populateValues(parameterMap);
    }

    @Override
    public boolean execute(RequestContext context, String currentState, String targetState) {
        String resourcePath = context.getResourcePath().getPath();
        boolean isErrorOccurred = false;
        for (RolePermission role : list) {
            for (String rule : role.getPermission()) {
                String action = null;
                if (rule.equalsIgnoreCase(READ)) {
                    action = ActionConstants.GET;
                } else if (rule.equalsIgnoreCase(WRITE)) {
                    action = ActionConstants.PUT;
                } else if (rule.equalsIgnoreCase(DELETE)) {
                    action = ActionConstants.DELETE;
                } else {
                    break;
                }
                try {
                    executePermission(role.getAction(), userRealm, role.getRole(), resourcePath, action);
                } catch (UserStoreException e) {
                    isErrorOccurred = true;
                }
            }
        }
        return !isErrorOccurred;
    }

    /*
    The method obtains the tenant id from a string tenant id
     */
    private void obtainTenantId() {
        String stringTenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        try {
            this.tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        } catch (Exception e) {
            String errorMessage = "Failed to obtain Tenant id";
            log.error(errorMessage, e);
        }
    }

    /*
    The method is used to obtain the User Realm from the RealmContext
     */
    private void obtainUserRealm() {
        try {
            this.userRealm = GovernanceRegistryExtensionsDataHolder.getInstance().getRegistryService()
                                                                   .getUserRealm(this.tenantId);
        } catch (RegistryException e) {
            String errorMessage = "Failed to load User Realm Manager.";
            log.error(errorMessage, e);
        }
    }

    private void executePermission(int opType, UserRealm user, String role, String target, String rule)
            throws UserStoreException {
        switch (opType) {
            case 1:
                addPermission(user, role, target, rule);
                break;
            case 2:
                removePermission(user, role, target, rule);
                break;
            default:
                if (log.isDebugEnabled()) {
                    log.debug("Ignoring " + opType);
                }
                break;
        }
    }

    private void addPermission(UserRealm user, String role, String target, String rule) throws UserStoreException {

        //Do nothing if either the role,target or rule is empty
        if ((role == null) || (target == null) || (rule == null)) {
            return;
        }
        user.getAuthorizationManager().authorizeRole(role, target, rule);
        if (log.isDebugEnabled()) {
            log.debug("Permission " + rule + " ADDED to role: " + role + " for " + target);
        }

    }

    private void removePermission(UserRealm user, String role, String target, String rule) throws UserStoreException {

        if ((role == null) || (target == null) || (rule == null)) {
            return;
        }
        user.getAuthorizationManager().denyRole(role, target, rule);
        if (log.isDebugEnabled()) {
            log.debug("Permission: " + rule + " REMOVED from role: " + role + " for " + target);
        }

    }

    private void populateValues(Map parameterMap) {
        Iterator<String> mapKeyIterator = parameterMap.keySet().iterator();
        String key;
        String value;

        //Go through all keys in the map object
        while (mapKeyIterator.hasNext()) {
            key = mapKeyIterator.next();
            value = (String) parameterMap.get(key);
            String[] values = key.split(":");
            String[] permissions = value.split(",");
            RolePermission role = new RolePermission();
            if (values[1].equalsIgnoreCase(ADD)) {
                role.setAction(1);
            } else {
                role.setAction(2);
            }
            role.setPermission(permissions);
            role.setRole(values[0]);
            list.add(role);
        }
    }

    private class RolePermission {
        private String role;
        private int action;
        private String[] permission;

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public int getAction() {
            return action;
        }

        public void setAction(int action) {
            this.action = action;
        }

        public String[] getPermission() {
            return permission;
        }

        public void setPermission(String[] permission) {
            this.permission = permission;
        }
    }
}
