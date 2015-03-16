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
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.registry.extensions.executors.utils.ExecutorConstants;
import org.wso2.carbon.governance.registry.extensions.interfaces.Execution;
import org.wso2.carbon.governance.registry.extensions.internal.GovernanceRegistryExtensionsComponent;
import org.wso2.carbon.registry.common.CommonConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.internal.RegistryCoreServiceComponent;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.extensions.utils.CommonUtil;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused") public class APIPublishExecutor implements Execution {

	private static final Log log = LogFactory.getLog(APIPublishExecutor.class);

	public static final String REST_SERVICE_KEY = "restservice";
	private static final String URI_TEMPLATE = "uritemplate";
	private static final String URL_PATTERN = "urlPattern";
	private static final String HTTP_VERB = "httpVerb";
	private static final String AUTH_TYPE = "authType";
	private static final String OPERATION = "operation";
	private static final String PATH = "path";
	private static final String API_PROVIDER = "API_PROVIDER";
	private static final String PUBLISH_URL = "publisher/site/blocks/item-add/ajax/add.jag";

	private String apimEndpoint = null;
	private String apimUsername = null;
	private String apimPassword = null;
	private String defaultTier = "Unlimited";
	private String apiThrottlingTier = "Unlimited,Unlimited,Unlimited,Unlimited,Unlimited";

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
			apimUsername = secretResolver.resolve("apim.username");
			apimPassword = secretResolver.resolve("apim.password");
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
	 * @param context      The request context that was generated from the registry core.
	 *                     The request context contains the resource, resource path and other
	 *                     variables generated during the initial call.
	 * @param currentState The current lifecycle state.
	 * @param targetState  The target lifecycle state.
	 * @return Returns whether the execution was successful or not.
	 */
	@Override public boolean execute(RequestContext context, String currentState,
	                                 String targetState) {
		Resource resource = context.getResource();

		try {
			String artifactString = RegistryUtils.decodeBytes((byte[]) resource.getContent());
			String user = CarbonContext.getThreadLocalCarbonContext().getUsername();
			OMElement xmlContent = AXIOMUtil.stringToOM(artifactString);
			String serviceName = CommonUtil.getServiceName(xmlContent);
			GenericArtifactManager manager = new GenericArtifactManager(
					RegistryCoreServiceComponent.getRegistryService()
					                            .getGovernanceUserRegistry(user, CarbonContext
							                            .getThreadLocalCarbonContext()
							                            .getTenantId()), REST_SERVICE_KEY);

			GenericArtifact api = manager.getGenericArtifact(context.getResource().getUUID());
			publishData(api, serviceName, xmlContent);

		} catch (RegistryException e) {
			log.error("Failed to publish service to API store ", e);
			return false;
		} catch (XMLStreamException e) {
			log.error("Failed to convert service to xml content");
			return false;
		} catch (Exception e) {
			log.error("Unexpected error occurred when publishing data to API Manager.");
			return false;
		}
		return true;
	}

	/**
	 * Publish the data to API Manager
	 *
	 * @param api         API registry artifact.
	 * @param serviceName Name of the REST service.
	 * @param xmlContent  Url Pattern value iterator.
	 */
	private void publishData(GenericArtifact api, String serviceName, OMElement xmlContent)
			throws RegistryException {

		if (apimEndpoint == null || apimUsername == null || apimPassword == null) {
			throw new RuntimeException("APIManager login credentials are not defined");
		}

		CookieStore cookieStore = new BasicCookieStore();
		HttpContext httpContext = new BasicHttpContext();
		httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

		authenticate(httpContext);
		String publishEndpoint = apimEndpoint + PUBLISH_URL;

		try {
			// create a post request to addAPI.
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(publishEndpoint);

			// Request parameters and other properties.
			List<NameValuePair> params = getRequestParameters(api, serviceName, xmlContent);

			if (api.getAttribute("overview_endpointURL") != null &&
			    api.getAttribute("overview_endpointURL").isEmpty()) {
				log.warn("Service Endpoint is empty.");
			}

			httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

			HttpResponse response = httpclient.execute(httppost, httpContext);

			if (response.getStatusLine().getStatusCode() != 200) {
				throw new RuntimeException(
						"Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
			}

		} catch (Exception e) {
			log.error("Error in publishing the data to API Manager", e);
		}
	}

	/**
	 * Creates request parameter list to publish.
	 *
	 * @param api         API artifact.
	 * @param serviceName Service name.
	 * @param xmlContent  API artifact content.
	 * @return Request parameter list.
	 * @throws GovernanceException
	 */
	private List<NameValuePair> getRequestParameters(GenericArtifact api, String serviceName,
	                                                 OMElement xmlContent)
			throws GovernanceException {
		List<NameValuePair> params = new ArrayList<NameValuePair>();

		//Adding request parameters

		//API endpoint URL.
		params.add(new BasicNameValuePair(ExecutorConstants.API_ENDPOINT,
		                                  api.getAttribute(ExecutorConstants.API_ENDPOINT_URL)));
		//API add action
		params.add(new BasicNameValuePair(ExecutorConstants.API_ACTION,
		                                  ExecutorConstants.API_ADD_ACTION));
		//API Name
		params.add(new BasicNameValuePair(ExecutorConstants.API_NAME, serviceName));
		//API Context
		params.add(new BasicNameValuePair(ExecutorConstants.API_CONTEXT, serviceName));
		//API version
		params.add(new BasicNameValuePair(ExecutorConstants.API_VERSION,
		                                  api.getAttribute(ExecutorConstants.SERVICE_VERSION)));
		//API Provider
		params.add(new BasicNameValuePair(API_PROVIDER, CarbonContext.getThreadLocalCarbonContext()
		                                                             .getUsername()));
		//API Tier availability
		params.add(new BasicNameValuePair(ExecutorConstants.API_TIER, defaultTier));

		Iterator resources = xmlContent.getChildrenWithLocalName(URI_TEMPLATE);

		if (!resources.hasNext()) {
			log.warn("Resources list is empty.");
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

			params.add(new BasicNameValuePair("uriTemplate-" + resourceCount, urlPatternText));
			params.add(new BasicNameValuePair("resourceMethod-" + resourceCount,
			                                  httpVerbText.toUpperCase()));
			params.add(new BasicNameValuePair("resourceMethodAuthType-" + resourceCount,
			                                  authTypeText));
			params.add(new BasicNameValuePair("resourceMethodThrottlingTier-" + resourceCount,
			                                  apiThrottlingTier));
			++resourceCount;

		}

		if (resourceCount > 0) {
			params.add(new BasicNameValuePair("resourceCount", Integer.toString(resourceCount)));
		}
		//API Visibility
		params.add(new BasicNameValuePair(ExecutorConstants.API_VISIBLITY,
		                                  ExecutorConstants.DEFAULT_VISIBILITY));

		String endpointConfigJson = "{\"production_endpoints\":{\"url\":\"" +
		                            api.getAttribute("overview_endpointURL") +
		                            "\",\"config\":null},\"endpoint_type\":\"http\"}";
		//End point configuration
		params.add(new BasicNameValuePair("endpoint_config", endpointConfigJson));

		return params;
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
				throw new RuntimeException(
						" Authentication with API Manager failed: HTTP error code : " +
						response.getStatusLine().getStatusCode());
			}

		} catch (Exception e) {
			log.error("Authentication failed", e);
		}
	}
}
