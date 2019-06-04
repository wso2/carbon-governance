/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.governance.registry.extensions.executors.apistore;

import com.google.gson.Gson;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
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
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.registry.extensions.executors.utils.ExecutorConstants;
import org.wso2.carbon.governance.registry.extensions.interfaces.Execution;
import org.wso2.carbon.governance.registry.extensions.internal.GovernanceRegistryExtensionsComponent;
import org.wso2.carbon.governance.registry.extensions.utils.Constants;
import org.wso2.carbon.governance.registry.extensions.utils.ResponseAPIM;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.internal.RegistryCoreServiceComponent;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.extensions.utils.CommonUtil;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;
import org.wso2.securevault.commons.MiscellaneousUtil;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.governance.registry.extensions.executors.utils.ExecutorConstants.APIM_ENDPOINT;
import static org.wso2.carbon.governance.registry.extensions.executors.utils.ExecutorConstants.APIM_PASSWORD;
import static org.wso2.carbon.governance.registry.extensions.executors.utils.ExecutorConstants.APIM_USERNAME;
import static org.wso2.carbon.governance.registry.extensions.executors.utils.ExecutorConstants.API_ACTION;
import static org.wso2.carbon.governance.registry.extensions.executors.utils.ExecutorConstants.API_LOGIN_ACTION;
import static org.wso2.carbon.governance.registry.extensions.executors.utils.ExecutorConstants.API_NAME;
import static org.wso2.carbon.governance.registry.extensions.executors.utils.ExecutorConstants.API_PASSWORD;
import static org.wso2.carbon.governance.registry.extensions.executors.utils.ExecutorConstants.API_PROVIDER;
import static org.wso2.carbon.governance.registry.extensions.executors.utils.ExecutorConstants.API_USERNAME;
import static org.wso2.carbon.governance.registry.extensions.executors.utils.ExecutorConstants.API_VERSION;


public class APIDeleteExecutor implements Execution {

    private static final Log log = LogFactory.getLog(APIDeleteExecutor.class);

    private String apimEndpoint = null;
    private String apimUsername = null;
    private String apimPassword = null;


    /**
     * This method is called when the execution class is initialized. All the execution classes are initialized only
     * once.
     *
     * @param parameterMap Static parameter map given by the user. These are the parameters that have been given in the
     *                     lifecycle configuration as the parameters of the executor.
     *                     <p>
     *                     Eg:- <execution forEvent="Promote" class="org.wso2.carbon.governance.registry.extensions
     *                     .executors.ServiceVersionExecutor"> <parameter name="currentEnvironment"
     *                     value="/_system/governance/trunk/"/> <parameter name="targetEnvironment"
     *                     value="/_system/governance/branches/testing/"/> <parameter name="service.mediatype"
     *                     value="application/vnd.wso2-service+xml"/> </execution>
     *                     <p>
     *                     The parameters defined here are passed to the executor using this method.
     */
    @Override
    public void init(Map parameterMap) {
        SecretResolver secretResolver = SecretResolverFactory.create((OMElement) null, false);
        // Retrieves the secured password as follows
        secretResolver.init(GovernanceRegistryExtensionsComponent.getSecretCallbackHandlerService()
                                    .getSecretCallbackHandler());
        if (parameterMap.get(APIM_ENDPOINT) != null) {
            apimEndpoint = parameterMap.get(APIM_ENDPOINT).toString();
        }

        if (parameterMap.get(ExecutorConstants.APIM_USERNAME) != null) {
            apimUsername = parameterMap.get(ExecutorConstants.APIM_USERNAME).toString();
            if (secretResolver.isInitialized()) {
                if (secretResolver.isTokenProtected(ExecutorConstants.APIM_USERNAME)) {
                    apimUsername = secretResolver.resolve(ExecutorConstants.APIM_USERNAME);
                } else {
                    apimUsername = MiscellaneousUtil.resolve(apimUsername, secretResolver);
                }
            }
        }
        if (parameterMap.get(ExecutorConstants.APIM_PASSWORD) != null) {
            apimPassword = parameterMap.get(ExecutorConstants.APIM_PASSWORD).toString();
            if (secretResolver.isInitialized()) {
                if (secretResolver.isTokenProtected(ExecutorConstants.APIM_PASSWORD)) {
                    apimPassword = secretResolver.resolve(ExecutorConstants.APIM_PASSWORD);
                } else {
                    apimPassword = MiscellaneousUtil.resolve(apimPassword, secretResolver);
                }

            }
        }
    }

    /**
     * This method will be called when the invoke() method of the default lifecycle implementation is called. Execution
     * logic should reside in this method since the default lifecycle implementation will determine the execution output
     * by looking at the output of this method.
     *
     * @param context      The request context that was generated from the registry core for the invoke() call. The
     *                     request context contains the resource, resource path and other variables generated during the
     *                     initial call.
     * @param currentState The current lifecycle state.
     * @param targetState  The target lifecycle state.
     * @return Returns whether the execution was successful or not.
     */
    @Override
    public boolean execute(RequestContext context, String currentState, String targetState) {
        Resource resource = context.getResource();
        boolean valid;
        try {
            String user = CarbonContext.getThreadLocalCarbonContext().getUsername();
            String artifactString = RegistryUtils.decodeBytes((byte[]) resource.getContent());
            OMElement xmlContent = AXIOMUtil.stringToOM(artifactString);
            String serviceName = CommonUtil.getServiceName(xmlContent);
            GenericArtifactManager manager;
            Registry registry = RegistryCoreServiceComponent.getRegistryService().getGovernanceUserRegistry(user,
                                                             CarbonContext.getThreadLocalCarbonContext().getTenantId());
            if (resource.getMediaType().equals(ExecutorConstants.SOAP_MEDIA_TYPE)) {
                manager = new GenericArtifactManager(registry, "soapservice");
            } else {
                manager = new GenericArtifactManager(registry, "restservice");
            }
            GenericArtifact genericArtifact = manager.getGenericArtifact(context.getResource().getUUID());
            valid = deleteAPIFromAPIM(genericArtifact, serviceName);

        } catch (Exception e) {
            log.error(ExecutorConstants.FAILED_TO_DELETE_MESSAGE, e);
            valid = false;
        }
        return valid;
    }

    private boolean deleteAPIFromAPIM(GenericArtifact genericArtifact, String serviceName) throws GovernanceException {
        if (apimEndpoint == null || apimUsername == null || apimPassword == null) {
            String msg = ExecutorConstants.APIM_LOGIN_UNDEFINED;
            throw new RuntimeException(msg + "API delete might fail");
        }
        CookieStore cookieStore = new BasicCookieStore();
        HttpContext httpContext = new BasicHttpContext();
        httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
        // APIUtils apiUtils = new APIUtils();
        authenticateAPIM(httpContext);
        String addAPIendpoint = apimEndpoint + ExecutorConstants.APIM_REMOVE_URL;

        // create a post request to addAPI.
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(addAPIendpoint);

        // Request parameters and other properties.
        List<NameValuePair> params = new ArrayList<>();

        params.add(new BasicNameValuePair(API_ACTION, "removeAPI"));
        params.add(new BasicNameValuePair(API_NAME, serviceName));
        params.add(new BasicNameValuePair(API_PROVIDER, apimUsername));
        params.add(new BasicNameValuePair(API_VERSION, genericArtifact.getAttribute("overview_version")));

        ResponseAPIM responseAPIM = callAPIMToPublishAPI(httpclient, httppost, params, httpContext);

        if (responseAPIM.getError().equalsIgnoreCase(Constants.TRUE_CONSTANT)) {
            throw new RuntimeException("Error occured while deleting the api from API Manager. " +
                                               responseAPIM.getMessage());
        }

        return true;
    }

    /**
     * Authenticate to APIM
     *
     * @param httpContext HttpContext
     */
    private void authenticateAPIM(HttpContext httpContext) throws GovernanceException {
        String loginEP = apimEndpoint + "publisher/site/blocks/user/login/ajax/login.jag";
        try {
            // create a post request to addAPI.
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(loginEP);
            // Request parameters and other properties.
            List<NameValuePair> params = new ArrayList<>(3);

            params.add(new BasicNameValuePair(API_ACTION, API_LOGIN_ACTION));
            params.add(new BasicNameValuePair(API_USERNAME, apimUsername));
            params.add(new BasicNameValuePair(API_PASSWORD, apimPassword));
            httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

            HttpResponse response = httpclient.execute(httppost, httpContext);
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new GovernanceException(" Authentication with APIM failed: HTTP error code : " +
                                                      response.getStatusLine().getStatusCode());
            }

        } catch (IOException e) {
            throw new GovernanceException("Authentication with APIM fails", e);
        }
    }

    /**
     * This method will publish api to APIM.
     *
     * @param httpclient  HttpClient
     * @param httppost    HttpPost
     * @param params      List of NameValuePair
     * @param httpContext HttpContext
     * @return ResponseAPIM
     */
    public static ResponseAPIM callAPIMToPublishAPI(HttpClient httpclient, HttpPost httppost,
                                                    List<NameValuePair> params,
                                                    HttpContext httpContext) throws GovernanceException {
        try {
            httppost.setEntity(new UrlEncodedFormEntity(params, Constants.UTF_8_ENCODE));
            HttpResponse response = httpclient.execute(httppost, httpContext);
            if (response.getStatusLine().getStatusCode() != Constants.SUCCESS_RESPONSE_CODE) {
                // 200 is the successful response status code
                throw new RuntimeException("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
            }
            HttpEntity entity = response.getEntity();
            String responseString = EntityUtils.toString(entity, Constants.UTF_8_ENCODE);
            Gson gson = new Gson();
            return gson.fromJson(responseString, ResponseAPIM.class);

        } catch (java.net.SocketTimeoutException e) {
            throw new GovernanceException("Connection timed out, Please check the network availability", e);
        } catch (UnsupportedEncodingException e) {
            throw new GovernanceException("Unsupported encode exception.", e);
        } catch (IOException e) {
            throw new GovernanceException("IO Exception occurred.", e);
        } catch (Exception e) {
            throw new GovernanceException(e.getMessage(), e);
        }
    }
}