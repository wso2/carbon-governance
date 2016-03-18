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

package org.wso2.carbon.governance.registry.extensions.executors.apistore;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.registry.extensions.interfaces.Execution;
import org.wso2.carbon.governance.registry.extensions.internal.GovernanceRegistryExtensionsComponent;
import org.wso2.carbon.governance.registry.extensions.utils.Constants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.internal.RegistryCoreServiceComponent;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.extensions.utils.CommonUtil;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;

import javax.xml.stream.XMLStreamException;
import java.util.*;

import static org.wso2.carbon.governance.registry.extensions.executors.utils.ExecutorConstants.*;

/**
 * This executor used to publish a service to API store as a API.
 */
public class RestServiceToAPIExecutor implements Execution {

    Log log = LogFactory.getLog(RestServiceToAPIExecutor.class);

    private String apimEndpoint = null;
    private String apimUsername = null;
    private String apimPassword = null;
    private String apimEnv = null;
    private String defaultTier = "Unlimited";
    private String apiThrottlingTier = "Unlimited,Unlimited,Unlimited,Unlimited,Unlimited";

    private Map parameterMap = new HashMap();

    /**
     * This method is called when the execution class is initialized.
     * All the execution classes are initialized only once.
     *
     * @param parameterMap Static parameter map given by the user.
     *                     These are the parameters that have been given in the
     *                     lifecycle configuration as the parameters of the executor.
     */
    public void init(Map parameterMap) {

        SecretResolver secretResolver = SecretResolverFactory.create((OMElement) null, false);
        // Retrieves the secured password as follows
        secretResolver.init(GovernanceRegistryExtensionsComponent.getSecretCallbackHandlerService()
                                                                 .getSecretCallbackHandler());
        if (secretResolver != null && secretResolver.isInitialized()) {
            apimUsername = secretResolver.resolve("apim.username");
        }
        if (secretResolver != null && secretResolver.isInitialized()) {
            apimPassword = secretResolver.resolve("apim.password");
        }

        this.parameterMap = parameterMap;
        if (parameterMap.get(APIM_ENDPOINT) != null) {
            apimEndpoint = parameterMap.get(APIM_ENDPOINT).toString();
        }
        if (parameterMap.get(APIM_USERNAME) != null) {
            apimUsername = parameterMap.get(APIM_USERNAME).toString();
        }
        if (parameterMap.get(APIM_PASSWORD) != null) {
            apimPassword = parameterMap.get(APIM_PASSWORD).toString();
        }
        if (parameterMap.get(DEFAULT_TIER) != null) {
            defaultTier = parameterMap.get(DEFAULT_TIER).toString();
        }
        if (parameterMap.get(THROTTLING_TIER) != null) {
            apiThrottlingTier = parameterMap.get(THROTTLING_TIER).toString();
        }
        if (parameterMap.get(Constants.APIM_ENV) != null) {
            apimEnv = parameterMap.get(Constants.APIM_ENV).toString();
        }
    }

    /**
     * @param context      The request context that was generated from the registry core.
     *                     The request context contains the resource, resource path and other
     *                     variables generated during the initial call.
     * @param currentState The current lifecycle state.
     * @param targetState  The target lifecycle state.
     * @return Returns whether the execution was successful or not.
     */
    public boolean execute(RequestContext context, String currentState, String targetState) {

        Resource resource = context.getResource();
        try {
            String artifactString = RegistryUtils.decodeBytes((byte[]) resource.getContent());
            String user = CarbonContext.getThreadLocalCarbonContext().getUsername();
            OMElement xmlContent = AXIOMUtil.stringToOM(artifactString);
            String serviceName = CommonUtil.getServiceName(xmlContent);
            GenericArtifactManager manager = new GenericArtifactManager(
                    RegistryCoreServiceComponent.getRegistryService().getGovernanceUserRegistry(user, CarbonContext
                            .getThreadLocalCarbonContext().getTenantId()), "restservice");

            GenericArtifact api = manager.getGenericArtifact(context.getResource().getUUID());

            publishDataToAPIM(api, serviceName);

        } catch (RegistryException e) {
            log.error("Failed to publish service to API store ", e);
            return false;
        } catch (XMLStreamException e) {
            log.error("Failed to convert service to xml content");
            return false;
        }
        return true;
    }

    /**
     * Update the APIM DB for the published API.
     *
     * @param api
     * @param serviceName
     */
    private void publishDataToAPIM(GenericArtifact api, String serviceName) throws GovernanceException {

        if (apimEndpoint == null || apimUsername == null || apimPassword == null) {
            String msg = "APIManager endpoint URL or credentials are not defined";
            log.error(msg);
            throw new RuntimeException(msg + "API Publish might fail");
        }

        CookieStore cookieStore = new BasicCookieStore();
        HttpContext httpContext = new BasicHttpContext();
        httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

        authenticateAPIM(httpContext);
        String addAPIendpoint = apimEndpoint + "publisher/site/blocks/item-add/ajax/add.jag";

        try {
            // create a post request to addAPI.
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(addAPIendpoint);

            // Request parameters and other properties.
            List<NameValuePair> params = new ArrayList<NameValuePair>();

            for (String key : api.getAttributeKeys()) {
                if (log.isDebugEnabled()) {
                    log.error(key + "  :  " + api.getAttribute(key));
                }
            }

            if (api.getAttribute("overview_endpointURL") != null &&
                api.getAttribute("overview_endpointURL").isEmpty()) {
                String msg =
                        "Service Endpoint is a must attribute to create an API definition at the APIStore.Publishing at gateway might fail";
                log.warn(msg);
            }

            //params.add(new BasicNameValuePair(API_ENDPOINT, api.getAttribute("overview_endpointURL")));
            params.add(new BasicNameValuePair(API_ACTION, API_ADD_ACTION));
            params.add(new BasicNameValuePair(API_NAME, serviceName));
            params.add(new BasicNameValuePair(API_CONTEXT, serviceName));
            params.add(new BasicNameValuePair(API_VERSION, api.getAttribute(SERVICE_VERSION)));
            params.add(new BasicNameValuePair("API_PROVIDER", CarbonContext.getThreadLocalCarbonContext()
                                                                           .getUsername()));
            params.add(new BasicNameValuePair(API_TIER, defaultTier));
            params.add(new BasicNameValuePair(API_URI_PATTERN, DEFAULT_URI_PATTERN));
            params.add(new BasicNameValuePair(API_URI_HTTP_METHOD, DEFAULT_HTTP_VERB));
            params.add(new BasicNameValuePair(API_URI_AUTH_TYPE, DEFAULT_AUTH_TYPE));
            params.add(new BasicNameValuePair(API_VISIBLITY, DEFAULT_VISIBILITY));
            params.add(new BasicNameValuePair(API_THROTTLING_TIER, apiThrottlingTier));

            String[] endPoints=api.getAttributes(Constants.ENDPOINTS_ENTRY);
            if (endPoints != null && endPoints.length > 0) {
                List<String> endPointsList= Arrays.asList(endPoints);
                if (endPointsList.size() > 0) {
                    String endpointConfigJson = "{\"production_endpoints\":{\"url\":\"" +
                            getEnvironmentUrl(endPointsList) +
                            "\",\"config\":null},\"endpoint_type\":\"http\"}";
                    params.add(new BasicNameValuePair(Constants.ENDPOINT_CONFIG, endpointConfigJson));
                }else {
                    String msg = "Endpoint is a must attribute to create an API definition at the APIStore";
                    throw new GovernanceException(msg);
                }
            } else {
                String msg = "Endpoint is a must attribute to create an API definition at the APIStore";
                throw new GovernanceException(msg);
            }


            httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

            HttpResponse response = httpclient.execute(httppost, httpContext);

            if (response.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " +
                                           response.getStatusLine().getStatusCode());
            }

        } catch (Exception e) {
            log.error("Error in updating APIM", e);
            throw new GovernanceException("Error in updating APIM", e);

        }
        // after publishing update the lifecycle status
        //updateStatus(service, serviceName, httpContext);
    }

    /**
     * Authenticate to APIM
     *
     * @param httpContext
     */
    private void authenticateAPIM(HttpContext httpContext) throws GovernanceException {
        String loginEP = apimEndpoint + "publisher/site/blocks/user/login/ajax/login.jag";
        try {
            // create a post request to addAPI.
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(loginEP);
            // Request parameters and other properties.
            List<NameValuePair> params = new ArrayList<NameValuePair>(3);

            params.add(new BasicNameValuePair(API_ACTION, API_LOGIN_ACTION));
            params.add(new BasicNameValuePair(API_USERNAME, apimUsername));
            params.add(new BasicNameValuePair(API_PASSWORD, apimPassword));
            httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

            HttpResponse response = httpclient.execute(httppost, httpContext);
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException(" Authentication with APIM failed: HTTP error code : " +
                                           response.getStatusLine().getStatusCode());
            }

        } catch (Exception e) {
            log.error("Authentication with APIM fails", e);
            throw new GovernanceException("Authentication with APIM fails", e);
        }
    }

    /**
     * Update the lifecycle status of the published API
     *
     * @param service
     * @param serviceName
     * @param httpContext
     */
    private void updateStatus(Service service, String serviceName, HttpContext httpContext) throws GovernanceException {
        String lifeCycleEP =
                apimEndpoint +
                "publisher/site/blocks/life-cycles/ajax/life-cycles.jag";
        try {
            // create a post request to addAPI.
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(lifeCycleEP);
            // Request parameters and other properties.
            List<NameValuePair> params = new ArrayList<NameValuePair>(6);
            params.add(new BasicNameValuePair(API_ACTION, API_UPDATESTATUS_ACTION));
            params.add(new BasicNameValuePair(API_NAME, serviceName));
            params.add(new BasicNameValuePair(API_VERSION, service.getAttribute(SERVICE_VERSION)));
            params.add(new BasicNameValuePair(API_PROVIDER, CarbonContext.getThreadLocalCarbonContext()
                                                                         .getUsername()));
            params.add(new BasicNameValuePair(API_STATUS, API_PUBLISHED_STATUS));
            params.add(new BasicNameValuePair(API_PUBLISH_GATEWAY_ACTION, "false"));

            httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

            HttpResponse response = httpclient.execute(httppost, httpContext);
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("PublishedAPI status update failed: HTTP error code : " +
                                           response.getStatusLine().getStatusCode());
            }

        } catch (Exception e) {
            log.error("PublishedAPI status update failed", e);
            throw new GovernanceException("PublishedAPI status update failed", e);
        }
    }

    private String getEnvironmentUrl(List<String> endpointsEntry) throws GovernanceException{
        for (String att : endpointsEntry) {
            if (att.substring(0, att.indexOf(Constants.COLUNM_SEPERATOR)).equalsIgnoreCase(apimEnv)) {
                return att.substring(att.indexOf(Constants.COLUNM_SEPERATOR) + 1);
            }
        }
        throw new GovernanceException("Related url is not available");
    }
}
