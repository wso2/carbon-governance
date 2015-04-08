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
import org.wso2.carbon.registry.common.CommonConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.internal.RegistryCoreServiceComponent;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class is an implementation of the interface {@link org.wso2.carbon.governance.registry.extensions.interfaces.Execution}
 * This class consists methods that will publish an API to API Manager.
 *
 * This class gets initiated when a REST Service lifecycle is added to a REST Service. In initialization following
 * static configuration parameters should be defined in the lifecycle.
 *  Eg:- <execution forEvent="Publish" class="org.wso2.carbon.governance.registry.extensions.executors.APIPublishExecutor">
 *           <parameter name="apim.endpoint" value="http://localhost:9763/"/>
 *           <parameter name="apim.username" value="admin"/>
 *           <parameter name="apim.password" value="admin"/>
 *           <parameter name="default.tier" value="Unlimited"/>
 *           <parameter name="throttlingTier" value="Unlimited,Unlimited,Unlimited,Unlimited,Unlimited"/>
 *       </execution>
 *
 * Once the REST Service is is set to publish to API Manager, the execute method will get called. The execute method
 * contains the logic to publish an API to API Manager through a http POST request to API Publisher. The method will
 * return true if the API published successfully and false if fails to publish the API.
 *
 * @see org.wso2.carbon.governance.registry.extensions.interfaces.Execution
 */
public class APIPublishExecutor implements Execution {

	private static final Log log = LogFactory.getLog(APIPublishExecutor.class);

	private static final String URI_TEMPLATE = "uritemplate";
	private static final String URL_PATTERN = "urlPattern";
	private static final String HTTP_VERB = "httpVerb";
	private static final String AUTH_TYPE = "authType";
	private static final String API_PROVIDER = "API_PROVIDER";

	private String apimEndpoint = null;
	private String apimUsername = null;
	private String apimPassword = null;
	private String defaultTier = "Unlimited";
	private String apiThrottlingTier = "Unlimited,Unlimited,Unlimited,Unlimited,Unlimited";

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
		if (parameterMap.get(ExecutorConstants.DEFAULT_TIER) != null) {
			defaultTier = parameterMap.get(ExecutorConstants.DEFAULT_TIER).toString();
		}
		if (parameterMap.get(ExecutorConstants.THROTTLING_TIER) != null) {
			apiThrottlingTier = parameterMap.get(ExecutorConstants.THROTTLING_TIER).toString();
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
		Resource resource = context.getResource();

		try {
			String artifactString = RegistryUtils.decodeBytes((byte[]) resource.getContent());
			String user = CarbonContext.getThreadLocalCarbonContext().getUsername();
			OMElement xmlContent = AXIOMUtil.stringToOM(artifactString);
			GenericArtifactManager manager = new GenericArtifactManager(
					RegistryCoreServiceComponent.getRegistryService().getGovernanceUserRegistry(user, CarbonContext
							.getThreadLocalCarbonContext().getTenantId()), ExecutorConstants.REST_SERVICE_KEY);

			GenericArtifact api = manager.getGenericArtifact(context.getResource().getUUID());
			publishData(api, xmlContent);

		} catch (RegistryException e) {
			log.error("Failed to publish service to API store ", e);
			return false;
		} catch (XMLStreamException e) {
			log.error("Failed to convert service to xml content", e);
			return false;
		}
		return true;
	}

	/**
	 * Publish the data to API Manager
	 *
	 * @param api        API registry artifact.
	 * @param xmlContent url Pattern value iterator.
	 */
	private void publishData(GenericArtifact api, OMElement xmlContent) throws RegistryException {

		if (apimEndpoint == null || apimUsername == null || apimPassword == null) {
			throw new RuntimeException(ExecutorConstants.APIM_LOGIN_UNDEFINED);
		}

		CookieStore cookieStore = new BasicCookieStore();
		HttpContext httpContext = new BasicHttpContext();
		httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

		Utils.authenticateAPIM(httpContext, apimEndpoint, apimUsername, apimPassword);
		String publishEndpoint = apimEndpoint + ExecutorConstants.APIM_PUBLISH_URL;

		// create a post request to addAPI.
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(publishEndpoint);

		// Request parameters and other properties.
		List<NameValuePair> params = getRequestParameters(api, xmlContent);

		if (api.getAttribute(ExecutorConstants.SERVICE_ENDPOINT_URL) != null &&
		    api.getAttribute(ExecutorConstants.SERVICE_ENDPOINT_URL).isEmpty()) {
			log.warn(ExecutorConstants.EMPTY_ENDPOINT);
		}

		try {
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
	}

	/**
	 * Creates request parameter list to publish.
	 *
	 * @param api                   API artifact.
	 * @param xmlContent            API artifact content.
	 * @return                      request parameter list.
	 * @throws GovernanceException  If fails to get required attributes from the API artifact.
	 */
	private List<NameValuePair> getRequestParameters(GenericArtifact api, OMElement xmlContent)
			throws GovernanceException {
		List<NameValuePair> params = new ArrayList<>();

		String serviceName = api.getAttribute(ExecutorConstants.SERVICE_NAME);
		String serviceVersion = api.getAttribute(ExecutorConstants.SERVICE_VERSION);
		//Adding request parameters
		//API endpoint URL.
		params.add(new BasicNameValuePair(ExecutorConstants.API_ENDPOINT, api.getAttribute(
				ExecutorConstants.SERVICE_ENDPOINT_URL)));
		//API add action
		params.add(new BasicNameValuePair(ExecutorConstants.API_ACTION,
		                                  ExecutorConstants.API_ADD_ACTION));
		//API Name
		params.add(new BasicNameValuePair(ExecutorConstants.API_NAME, serviceName));
		//API Context
		params.add(new BasicNameValuePair(ExecutorConstants.API_CONTEXT, serviceName));
		//API version
		params.add(new BasicNameValuePair(ExecutorConstants.API_VERSION, serviceVersion));
		//API Provider
		params.add(new BasicNameValuePair(API_PROVIDER, CarbonContext.getThreadLocalCarbonContext()
		                                                             .getUsername()));
		//API Tier availability
		params.add(new BasicNameValuePair(ExecutorConstants.API_TIER, defaultTier));

		Iterator resources = xmlContent.getChildrenWithLocalName(URI_TEMPLATE);

		if (!resources.hasNext()) {
			log.warn("Resources list is empty. Publishing to API Manager might fail.");
		}
		//Adding resources
		int resourceCount = 0;
		while (resources.hasNext()) {
			OMElement resource = (OMElement) resources.next();

			OMElement urlPattern = resource.getFirstChildWithName(
					new QName(CommonConstants.SERVICE_ELEMENT_NAMESPACE, URL_PATTERN));
			OMElement httpVerb = resource.getFirstChildWithName(
					new QName(CommonConstants.SERVICE_ELEMENT_NAMESPACE, HTTP_VERB));
			OMElement authType = resource.getFirstChildWithName(
					new QName(CommonConstants.SERVICE_ELEMENT_NAMESPACE, AUTH_TYPE));

			String urlPatternText = urlPattern.getText();
			String httpVerbText = httpVerb.getText();
			String authTypeText = authType.getText();

			if (urlPatternText == null || httpVerbText == null) {
				continue;
			}

			authTypeText =
					authTypeText.isEmpty() ? ExecutorConstants.DEFAULT_AUTH_TYPE : authTypeText;

			params.add(new BasicNameValuePair(ExecutorConstants.API_URI_PATTERN + resourceCount, urlPatternText));
			params.add(new BasicNameValuePair(ExecutorConstants.API_URI_HTTP_METHOD + resourceCount,
			                                  httpVerbText.toUpperCase()));
			params.add(new BasicNameValuePair(ExecutorConstants.API_URI_AUTH_TYPE + resourceCount,
			                                  authTypeText));
			params.add(new BasicNameValuePair(ExecutorConstants.API_THROTTLING_TIER + resourceCount,
			                                  apiThrottlingTier));
			++resourceCount;

		}

		if (resourceCount > 0) {
			params.add(new BasicNameValuePair(ExecutorConstants.API_RESOURCE_COUNT, Integer.toString(resourceCount)));
		}
		//API Visibility
		params.add(new BasicNameValuePair(ExecutorConstants.API_VISIBLITY,
		                                  ExecutorConstants.DEFAULT_VISIBILITY));

		String endpointConfigJson = "{\"production_endpoints\":{\"url\":\"" +
		                            api.getAttribute(ExecutorConstants.SERVICE_ENDPOINT_URL) +
		                            "\",\"config\":null},\"endpoint_type\":\"http\"}";
		//End point configuration
		params.add(new BasicNameValuePair(ExecutorConstants.API_ENDPOINT_CONFIG, endpointConfigJson));

		return params;
	}
}
