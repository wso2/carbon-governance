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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
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
import org.wso2.carbon.governance.registry.extensions.executors.utils.Utils;
import org.wso2.carbon.governance.registry.extensions.interfaces.Execution;
import org.wso2.carbon.governance.registry.extensions.internal.GovernanceRegistryExtensionsComponent;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.internal.RegistryCoreServiceComponent;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class is an implementation of the interface {@link org.wso2.carbon.governance.registry.extensions.interfaces.Execution}
 * This class consists methods that will delete an API from API Manager.
 *
 * This class gets initiated when a REST Service lifecycle is added to a REST Service. In initialization following
 * static configuration parameters should be defined in the lifecycle.
 *  Eg:- <execution forEvent="Demote" class="org.wso2.carbon.governance.registry.extensions.executors.APIDeleteExecutor">
 *           <parameter name="apim.endpoint" value="http://localhost:9763/"/>
 *           <parameter name="apim.username" value="admin"/>
 *           <parameter name="apim.password" value="admin"/>
 *       </execution>
 *
 * If there are no subscriptions for the API in API Manager, execute method will remove the API from the API Manager and
 * move the governance artifact to the production state.
 *
 * @see org.wso2.carbon.governance.registry.extensions.interfaces.Execution
 */
public class APIDeleteExecutor implements Execution {
	private static final Log log = LogFactory.getLog(APIDeleteExecutor.class);

	private String apimEndpoint = null;
	private String apimUsername = null;
	private String apimPassword = null;

	/**
	 * This method is called when the execution class is initialized.
	 * All the execution classes are initialized only once.
	 *
	 * @param parameterMap the parameters that have been given in the
	 *                     lifecycle configuration as the parameters of the executor.
	 */
	@Override
	public void init(Map parameterMap) {
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
	 * @param context      the request context that was generated from the registry core.
	 * @param currentState the current lifecycle state.
	 * @param targetState  the target lifecycle state.
	 * @return             Returns whether the execution was successful or not.
	 */
	@Override
	public boolean execute(RequestContext context, String currentState, String targetState) {
		boolean deleted = false;

		String user = CarbonContext.getThreadLocalCarbonContext().getUsername();
		try {
			GenericArtifactManager manager = new GenericArtifactManager(
					RegistryCoreServiceComponent.getRegistryService().getGovernanceUserRegistry(user, CarbonContext
							.getThreadLocalCarbonContext().getTenantId()), ExecutorConstants.REST_SERVICE_KEY);

			GenericArtifact api = manager.getGenericArtifact(context.getResource().getUUID());
			deleted = deleteFromAPIManager(api);

		} catch (GovernanceException e) {
			log.error("Failed to read the REST API artifact from the registry. ", e);
		} catch (RegistryException e) {
			log.error(ExecutorConstants.API_DEMOTE_FAIL, e);
		}
		return deleted;
	}

	/**
	 * Deletes API from the API Manager
	 *
	 * @param api API Generic artifact.
	 * @return    True if successfully deleted.
	 */
	private boolean deleteFromAPIManager(GenericArtifact api) throws RegistryException {
		if (apimEndpoint == null || apimUsername == null || apimPassword == null) {
			throw new RuntimeException(ExecutorConstants.APIM_LOGIN_UNDEFINED);
		}

		CookieStore cookieStore = new BasicCookieStore();
		HttpContext httpContext = new BasicHttpContext();
		httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

		Utils.authenticateAPIM(httpContext, apimEndpoint, apimUsername, apimPassword);
		String removeEndpoint = apimEndpoint + ExecutorConstants.APIM_REMOVE_URL;

		try {
			// create a post request to addAPI.
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(removeEndpoint);

			// Request parameters and other properties.
			List<NameValuePair> params = new ArrayList<>();
			params.add(new BasicNameValuePair(ExecutorConstants.API_ACTION,
			                                  ExecutorConstants.API_REMOVE_ACTION));
			params.add(new BasicNameValuePair(ExecutorConstants.API_NAME,
			                                  api.getAttribute(ExecutorConstants.SERVICE_NAME)));
			params.add(new BasicNameValuePair(ExecutorConstants.API_PROVIDER, apimUsername));
			params.add(new BasicNameValuePair(ExecutorConstants.API_VERSION,
			                                  api.getAttribute(ExecutorConstants.SERVICE_VERSION)));

			httppost.setEntity(new UrlEncodedFormEntity(params, ExecutorConstants.DEFAULT_CHAR_ENCODING));

			HttpResponse response = httpclient.execute(httppost, httpContext);

			if (response.getStatusLine().getStatusCode() != 200) {
				throw new RuntimeException(
						"Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
			}

		} catch (ClientProtocolException e) {
			throw new RegistryException(ExecutorConstants.APIM_POST_REQ_FAIL, e);
		} catch (UnsupportedEncodingException e) {
			throw new RegistryException(ExecutorConstants.ENCODING_FAIL, e);
		} catch (IOException e) {
			throw new RegistryException(ExecutorConstants.APIM_POST_REQ_FAIL, e);
		}

		return true;
	}
}
