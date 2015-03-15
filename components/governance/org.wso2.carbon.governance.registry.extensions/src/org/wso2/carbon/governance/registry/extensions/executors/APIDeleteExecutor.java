/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.governance.registry.extensions.executors;

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
import org.wso2.carbon.governance.registry.extensions.executors.utils.ExecutorConstants;
import org.wso2.carbon.governance.registry.extensions.interfaces.Execution;
import org.wso2.carbon.governance.registry.extensions.internal.GovernanceRegistryExtensionsComponent;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.extensions.utils.CommonUtil;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused") public class APIDeleteExecutor implements Execution {
	private static final Log log = LogFactory.getLog(APIDeleteExecutor.class);
	public static final String REMOVE_URL = "publisher/site/blocks/item-add/ajax/remove.jag";

	private String apimEndpoint = null;
	private String apimUsername = null;
	private String apimPassword = null;
	private String apimEnv = null;

	/**
	 * This method is called when the execution class is initialized.
	 * All the execution classes are initialized only once.
	 *
	 * @param parameterMap Static parameter map given by the user.
	 *                     These are the parameters that have been given in the
	 *                     lifecycle configuration as the parameters of the executor.
	 */
	@Override public void init(Map parameterMap) {
		SecretResolver secretResolver = SecretResolverFactory.create((OMElement) null, false);
		// Retrieves the secured password as follows
		secretResolver.init(GovernanceRegistryExtensionsComponent.getSecretCallbackHandlerService()
		                                                         .getSecretCallbackHandler());
		if (secretResolver.isInitialized()) {
			apimUsername = secretResolver.resolve(ExecutorConstants.APIM_USERNAME);
			apimPassword = secretResolver.resolve(ExecutorConstants.APIM_PASSWORD);
		}
		if (parameterMap.get(ExecutorConstants.APIM_ENDPOINT) != null) {
			apimEndpoint = parameterMap.get(ExecutorConstants.APIM_ENDPOINT).toString();
		}
		if (parameterMap.get(ExecutorConstants.APIM_USERNAME) != null) {
			apimUsername = parameterMap.get(ExecutorConstants.APIM_USERNAME).toString();
		}
		if (parameterMap.get(ExecutorConstants.APIM_PASSWORD) != null) {
			apimPassword = parameterMap.get(ExecutorConstants.APIM_PASSWORD).toString();
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
	@Override public boolean execute(RequestContext context, String currentState,
	                                 String targetState) {
		Resource resource = context.getResource();
		boolean deleted;
		try {
			String artifactString = RegistryUtils.decodeBytes((byte[]) resource.getContent());
			OMElement xmlContent = AXIOMUtil.stringToOM(artifactString);
			String serviceName = CommonUtil.getServiceName(xmlContent);

			deleted = deleteFromAPIManager(resource, serviceName);

		} catch (Exception e) {
			log.error(e.getMessage());
			deleted = false;
		}
		return deleted;
	}

	/**
	 * Deletes API from the API Manager
	 *
	 * @param resource    API resource.
	 * @param serviceName API Name.
	 * @return True if successfully deleted.
	 */
	private boolean deleteFromAPIManager(Resource resource, String serviceName) {
		if (apimEndpoint == null || apimUsername == null || apimPassword == null) {
			throw new RuntimeException("APIManager login credentials are not defined");
		}

		CookieStore cookieStore = new BasicCookieStore();
		HttpContext httpContext = new BasicHttpContext();
		httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

		authenticate(httpContext);
		String removeEndpoint = apimEndpoint + REMOVE_URL;

		try {
			// create a post request to addAPI.
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(removeEndpoint);

			// Request parameters and other properties.
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair(ExecutorConstants.API_ACTION,
			                                  ExecutorConstants.API_REMOVE_ACTION));
			params.add(new BasicNameValuePair(ExecutorConstants.API_NAME, serviceName));
			params.add(new BasicNameValuePair(ExecutorConstants.API_PROVIDER, apimUsername));
			params.add(new BasicNameValuePair(ExecutorConstants.API_VERSION, resource.getProperty(
					RegistryConstants.VERSION_PARAMETER_NAME)));

			httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

			HttpResponse response = httpclient.execute(httppost, httpContext);

			if (response.getStatusLine().getStatusCode() != 200) {
				throw new RuntimeException(
						"Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
			}

		} catch (Exception e) {
			log.error("Error in removing the API from API Manager", e);
			return false;
		}

		return true;
	}

	/**
	 * Authenticate to API Manager
	 *
	 * @param httpContext HTTP context.
	 */
	private void authenticate(HttpContext httpContext) {
		String loginEP = apimEndpoint + "publisher/site/blocks/user/login/ajax/login.jag";
		try {
			// create a post request to addAPI.
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(loginEP);
			// Request parameters and other properties.
			List<NameValuePair> params = new ArrayList<NameValuePair>(3);

			params.add(new BasicNameValuePair(ExecutorConstants.API_ACTION,
			                                  ExecutorConstants.API_LOGIN_ACTION));
			params.add(new BasicNameValuePair(ExecutorConstants.API_USERNAME, apimUsername));
			params.add(new BasicNameValuePair(ExecutorConstants.API_PASSWORD, apimPassword));
			httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

			HttpResponse response = httpclient.execute(httppost, httpContext);
			if (response.getStatusLine().getStatusCode() != 200) {
				throw new RuntimeException(" Authentication with APIM failed: HTTP error code : " +
				                           response.getStatusLine().getStatusCode());
			}

		} catch (Exception e) {
			log.error("Authentication failed", e);
		}
	}
}
