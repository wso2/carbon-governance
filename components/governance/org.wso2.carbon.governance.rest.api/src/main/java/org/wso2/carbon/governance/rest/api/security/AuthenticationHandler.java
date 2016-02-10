/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.governance.rest.api.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.jaxrs.ext.RequestHandler;
import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.message.Message;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.rest.api.RestApiBasicAuthenticationException;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AuthenticationHandler implements RequestHandler {

    public static final String WWW_AUTHENTICATE = "WWW-Authenticate";
    public static final String AUTHORIZATION_HEADER_NAME = "Authorization";
    protected Log log = LogFactory.getLog(AuthenticationHandler.class);

    private final static String AUTH_TYPE_BASIC = "Basic";
    private final static String AUTH_TYPE_BASIC_REALM_VALUE = " Realm=\"WSO2-Registry\"";
    private final static String AUTH_TYPE_OAuth = "Bearer";
    private final static String METHOD_GET = "GET";


    /**
     * Implementation of RequestHandler.handleRequest method.
     * This method retrieves userName and password from Basic auth header,
     * and tries to authenticate against carbon user store
     * <p/>
     * Upon successful authentication allows process to proceed to retrieve requested REST resource
     * Upon invalid credentials returns a HTTP 401 UNAUTHORIZED response to client
     * Upon receiving a userStoreExceptions or IdentityException returns HTTP 500 internal server error to client
     *
     * @param message
     * @param classResourceInfo
     * @return Response
     */
    public Response handleRequest(Message message, ClassResourceInfo classResourceInfo) {
        AuthorizationPolicy policy = message.get(AuthorizationPolicy.class);

        if (policy != null && AUTH_TYPE_BASIC.equals(policy.getAuthorizationType())) {
            return handleBasicAuth(policy, message);
        } else if (policy != null) {
            return handleOAuth(message);
        } else {
            return handleAnonymousAcess(message);
        }
    }

    private Response handleAnonymousAcess(Message message) {
        String method = (String) message.get(Message.HTTP_REQUEST_METHOD);
        if (method != null && METHOD_GET.equals(method)) {
            String tenantDomain = getTenantDomain(null, message);
            PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            carbonContext.setUsername(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME);
            carbonContext.setTenantId(getTenantId(tenantDomain));
            carbonContext.setTenantDomain(tenantDomain);
            return null;
        }
        return authenticationFail(AUTH_TYPE_BASIC + AUTH_TYPE_BASIC_REALM_VALUE);
    }

    protected Response handleBasicAuth(AuthorizationPolicy policy, Message message) {
        String username = policy.getUserName();
        String password = policy.getPassword();
        String tenantDomain = getTenantDomain(username , message);
        try {
            if (authenticate(username, password, tenantDomain)) {
                return null;
            }
        } catch (RestApiBasicAuthenticationException e) {
            log.error("Could not authenticate user : " + username + "against carbon userStore", e);
        }
        return authenticationFail();
    }

    protected Response handleOAuth(Message message) {
        ArrayList<String> headers = ((Map<String, ArrayList>) message.get(Message.PROTOCOL_HEADERS)).get(AUTHORIZATION_HEADER_NAME);
        if (headers != null) {
            String authHeader = headers.get(0);
            if (authHeader.startsWith(AUTH_TYPE_OAuth)) {
                return authenticationFail(AUTH_TYPE_OAuth);
            }
        }
        return authenticationFail(AUTH_TYPE_BASIC + AUTH_TYPE_BASIC_REALM_VALUE);
    }

    /**
     * Checks whether a given userName:password combination authenticates correctly against carbon userStore
     * Upon successful authentication returns true, false otherwise
     *
     * @param userName
     * @param password
     * @return
     * @throws RestApiBasicAuthenticationException wraps and throws exceptions occur when trying to authenticate
     *                                             the user
     */
    private boolean authenticate(String userName, String password, String tenantDomain) throws RestApiBasicAuthenticationException {
        String tenantAwareUserName = MultitenantUtils.getTenantAwareUsername(userName);
        String userNameWithTenantDomain = tenantAwareUserName + "@" + tenantDomain;

        RealmService realmService = RegistryContext.getBaseInstance().getRealmService();
        int tenantId = getTenantId(tenantDomain);

        // tenantId == -1, means an invalid tenant.
        if (tenantId == -1) {
            if (log.isDebugEnabled()) {
                log.debug("Basic authentication request with an invalid tenant : " + userNameWithTenantDomain);
            }
            return false;
        }

        UserStoreManager userStoreManager = null;
        boolean authStatus = false;

        try {
            userStoreManager = realmService.getTenantUserRealm(tenantId).getUserStoreManager();
            authStatus = userStoreManager.authenticate(tenantAwareUserName, password);
        } catch (UserStoreException e) {
            throw new RestApiBasicAuthenticationException(
                    "User store exception thrown while authenticating user : " + userNameWithTenantDomain, e);
        }

        if (log.isDebugEnabled()) {
            log.debug("Basic authentication request completed. " +
                      "Username : " + userNameWithTenantDomain +
                      ", Authentication State : " + authStatus);
        }

        if (authStatus) {
            /* Upon successful authentication existing thread local carbon context
             * is updated to mimic the authenticated user */

            PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            carbonContext.setUsername(userName);
            carbonContext.setTenantId(tenantId);
            carbonContext.setTenantDomain(tenantDomain);
        }
        return authStatus;

    }

    private String getTenantDomain(String username, Message message) {
        String tenantDomain = null;
        // 1. Read "tenant" query parameter from the request URI.
        String query = (String) message.get(Message.QUERY_STRING);
        if (query != null && !query.isEmpty()) {
            int index = query.indexOf("tenant=");
            if (index > -1) {
                query = query.substring(index + 7);
                index = query.indexOf(",");
                if (index > 0) {
                    tenantDomain = query.substring(0, index);
                } else {
                    tenantDomain = query;
                }
            }
        }

        // 2. If tenantDomain not found, read "X_TENANT" HTTP header.
        if (tenantDomain == null || tenantDomain.isEmpty()) {
            Map<String, List<String>> headers = (Map<String, List<String>>) message.get(Message.PROTOCOL_HEADERS);
            if (headers != null && !headers.isEmpty()) {
                List<String> headerValues = headers.get("X_TENANT");
                if (headerValues != null && !headerValues.isEmpty()) {
                    tenantDomain = headerValues.get(0);
                }
            }
        }

        // 3. If tenantDomain not found, use "username" to resolve the tenant.
        if ((tenantDomain == null || tenantDomain.isEmpty()) && username != null) {
            tenantDomain = MultitenantUtils.getTenantDomain(username);
        }

        // 4. If tenantDomain still not found use supper tenant as default option.
        if (tenantDomain == null) {
            tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }

        return tenantDomain;
    }

    private int getTenantId(String tenantDomain) {
        RealmService realmService = RegistryContext.getBaseInstance().getRealmService();
        TenantManager mgr = realmService.getTenantManager();
        try {
            return mgr.getTenantId(tenantDomain);
        } catch (UserStoreException e) {
            log.error("Identity exception thrown while getting tenantID for : " + tenantDomain, e);
        }
        return 0;
    }

    private Response authenticationFail() {
        return authenticationFail(AUTH_TYPE_BASIC + AUTH_TYPE_BASIC_REALM_VALUE);
    }

    private Response authenticationFail(String authType) {
        //authentication failed, request the authetication, add the realm name if needed to the value of WWW-Authenticate
        return Response.status(401).header(WWW_AUTHENTICATE, authType).build();
    }


}