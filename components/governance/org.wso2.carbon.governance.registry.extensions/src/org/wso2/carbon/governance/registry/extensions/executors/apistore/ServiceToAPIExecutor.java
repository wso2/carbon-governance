/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.wso2.carbon.governance.registry.extensions.executors.utils.ExecutorConstants;
import org.wso2.carbon.governance.registry.extensions.interfaces.Execution;
import org.wso2.carbon.governance.registry.extensions.internal.GovernanceRegistryExtensionsComponent;
import org.wso2.carbon.governance.registry.extensions.internal.GovernanceRegistryExtensionsDataHolder;
import org.wso2.carbon.governance.registry.extensions.utils.APIUtils;
import org.wso2.carbon.governance.registry.extensions.utils.Constants;
import org.wso2.carbon.governance.registry.extensions.utils.ResponseAPIM;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;
import org.wso2.carbon.registry.core.Registry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.governance.registry.extensions.executors.utils.ExecutorConstants.*;

/**
 * This class will be used for publishing services as apis to APIM through publisher apis.
 * This will be invoked in promote transitions.
 */
public class ServiceToAPIExecutor implements Execution {

    private static final Log LOG = LogFactory.getLog(ServiceToAPIExecutor.class);

    private String soapServiceMediaType = "application/vnd.wso2-soap-service+xml";
    private String apimEndpoint = null;
    private String apimUsername = null;
    private String apimPassword = null;
    private String defaultTier = "Unlimited";
    private String apimEnv = null;
    private String apiThrottlingTier = "Unlimited,Unlimited,Unlimited,Unlimited,Unlimited";
    private Registry registry;
    private GenericArtifact artifact;

    /**
     * This method is called when the execution class is initialized.
     * All the execution classes are initialized only once.
     *
     * @param parameterMap Static parameter map given by the user.
     *                     These are the parameters that have been given in the
     *                     lifecycle configuration as the parameters of the executor.
     */
    @Override
    public void init(Map parameterMap) {
        SecretResolver secretResolver = SecretResolverFactory.create((OMElement) null, false);
        // Retrieves the secured password as follows
        secretResolver.init(GovernanceRegistryExtensionsComponent.getSecretCallbackHandlerService()
                .getSecretCallbackHandler());
        if (secretResolver.isInitialized()) {
            apimUsername = secretResolver.resolve(APIM_USERNAME);
            apimPassword = secretResolver.resolve(APIM_PASSWORD);
        }
        if (parameterMap.get(APIM_ENDPOINT) != null) {
            apimEndpoint = parameterMap.get(APIM_ENDPOINT).toString();
        }
        if (parameterMap.get(APIM_USERNAME) != null) {
            apimUsername = parameterMap.get(APIM_USERNAME).toString();
        }
        if (parameterMap.get(APIM_PASSWORD) != null) {
            apimPassword = parameterMap.get(APIM_PASSWORD).toString();
        }
        if (parameterMap.get(Constants.APIM_ENV) != null) {
            apimEnv = parameterMap.get(Constants.APIM_ENV).toString();
        }
        if (parameterMap.get(DEFAULT_TIER) != null) {
            defaultTier = parameterMap.get(DEFAULT_TIER).toString();
        }
        if (parameterMap.get(THROTTLING_TIER) != null) {
            apiThrottlingTier = parameterMap.get(THROTTLING_TIER).toString();
        }

    }

    /**
     * @param context      The request context that was generated from the registry core.
     *                     The request context contains the resource, resource PATH and other
     *                     variables generated during the initial call.
     * @param currentState The current lifecycle state.
     * @param targetState  The target lifecycle state.
     * @return Returns whether the execution was successful or not.
     */
    public boolean execute(RequestContext context, String currentState, String targetState) {
        List<String> publishedEnvList = new ArrayList<String>();
        List<String> failedEnvList = new ArrayList<String>();

        Resource resource = context.getResource();

        String apiName;
        try {
            String user = CarbonContext.getThreadLocalCarbonContext().getUsername();
            registry= GovernanceRegistryExtensionsDataHolder.getInstance().getRegistryService().getGovernanceUserRegistry(user,CarbonContext.getThreadLocalCarbonContext().getTenantId());
            String artifactAbsolutePath = resource.getPath();
            String artifactRelativePath=artifactAbsolutePath.substring("/_system/governance".length());
            Resource apiArtifact = registry.get(artifactRelativePath);
            GenericArtifactManager artifactManager;
            if (resource.getMediaType().equals(soapServiceMediaType)){
                artifactManager = new GenericArtifactManager(registry, "soapservice");
            } else {
                artifactManager = new GenericArtifactManager(registry, "service");
            }
            String artifactId = apiArtifact.getUUID();
            artifact = artifactManager.getGenericArtifact(artifactId);
            apiName = artifact.getAttribute("overview_name");


        } catch (RegistryException e) {
            LOG.error("Failed to convert service to xml content", e);
            return false;
        } catch (Exception e) {
            LOG.error("Failed to convert service to xml content", e);
            return false;
        }
        try {
            publishDataToAPIM(resource, apiName);

        } catch (GovernanceException e) {
            LOG.error("Failed to convert service to xml content", e);
            return false;
        } catch (Exception e) {
            LOG.error("Failed to convert service to xml content", e);
            return false;
        }
        return true;
    }

    /**
     * Update the APIM DB for the published API.
     *
     * @param resource
     * @param serviceName
     */
    private boolean publishDataToAPIM(Resource resource, String serviceName) throws GovernanceException {

        boolean valid = true;

        if (apimEndpoint == null || apimUsername == null || apimPassword == null || apimEnv == null) {
            throw new GovernanceException(ExecutorConstants.APIM_LOGIN_UNDEFINED);
        }

        CookieStore cookieStore = new BasicCookieStore();
        HttpContext httpContext = new BasicHttpContext();
        httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
      //  APIUtils apiUtils = new APIUtils();
        APIUtils.authenticateAPIM(httpContext, apimEndpoint, apimUsername, apimPassword);
        String addAPIendpoint = apimEndpoint + Constants.APIM_ENDPOINT;

        try {
            // create a post request to addAPI.
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(addAPIendpoint);

            // Request parameters and other properties.
            List<NameValuePair> params = new ArrayList<NameValuePair>();

            String[] endPoints=artifact.getAttributes(Constants.ENDPOINTS_ENTRY);
            List<String> endPointsList= Arrays.asList(endPoints);
            if (endPointsList==null){
                String msg = "Service Endpoint is a must attribute to create an API definition at the APIStore";
                throw new GovernanceException(msg);
            }

            addParameters(params, resource, serviceName);
            LOG.info(new UrlEncodedFormEntity(params, Constants.UTF_8_ENCODE));
            ResponseAPIM responseAPIM = APIUtils.callAPIMToPublishAPI(httpclient,httppost,params,httpContext);

            if (responseAPIM.getError().equalsIgnoreCase(Constants.TRUE_CONSTANT)) {
                LOG.error(responseAPIM.getMessage());
                throw new GovernanceException("Error occurred while adding the api to API Manager.");
            }

        } catch (Exception e) {
            throw new GovernanceException(e.getMessage(), e);
        }
        return valid;
    }

    private void addParameters(List<NameValuePair> params, Resource resource, String serviceName) throws GovernanceException{

        String[] endPoints=artifact.getAttributes(Constants.ENDPOINTS_ENTRY);
        List<String> endPointsList=Arrays.asList(endPoints);

        params.add(new BasicNameValuePair(API_ENDPOINT,getEnvironmentUrl(endPointsList)));
        params.add(new BasicNameValuePair(API_ACTION, API_ADD_ACTION));
        params.add(new BasicNameValuePair(API_NAME, serviceName));
        params.add(new BasicNameValuePair(API_CONTEXT, serviceName));
        params.add(new BasicNameValuePair(API_VERSION, artifact.getAttribute(SERVICE_VERSION)));
//        params.add(new BasicNameValuePair(API_PROVIDER, resource.getProperty(Constants.OVERVIEW_BUSINESS_UNIT)));
        params.add(new BasicNameValuePair(API_TIER, defaultTier));
        params.add(new BasicNameValuePair(API_URI_PATTERN, DEFAULT_URI_PATTERN));
        params.add(new BasicNameValuePair(API_URI_HTTP_METHOD, DEFAULT_HTTP_VERB));
        params.add(new BasicNameValuePair(API_URI_AUTH_TYPE, DEFAULT_AUTH_TYPE));
        params.add(new BasicNameValuePair(API_VISIBLITY, DEFAULT_VISIBILITY));
        params.add(new BasicNameValuePair(API_THROTTLING_TIER, apiThrottlingTier));

        params.add(new BasicNameValuePair(Constants.HTTP_CHECKED, Constants.HTTP_CONSTANT));
        params.add(new BasicNameValuePair(Constants.HTTPS_CHECKED, Constants.HTTPS_CONSTANT));
        params.add(new BasicNameValuePair(Constants.RESPONSE_CACHE, Constants.DISABLED));
        params.add(new BasicNameValuePair(Constants.ENDPOINT_TYPE, Constants.NONSECURED));

        params.add(new BasicNameValuePair(Constants.TAG, artifact.getAttribute(Constants.OVERVIEW_BUSINESS_UNIT)));

        if (endPointsList.size() > 0) {
            String endpointConfigJson = "{\"production_endpoints\":{\"url\":\"" +
                    getEnvironmentUrl(endPointsList) +
                    "\",\"config\":null},\"endpoint_type\":\"http\"}";
            params.add(new BasicNameValuePair(Constants.ENDPOINT_CONFIG, endpointConfigJson));
        }

        params.add(new BasicNameValuePair(Constants.SUBSCRIPTIONS, Constants.CURRENT_TENANT));

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
