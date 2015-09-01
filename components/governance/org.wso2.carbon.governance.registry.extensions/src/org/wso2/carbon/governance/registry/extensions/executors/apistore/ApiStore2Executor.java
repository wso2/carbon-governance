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
import org.wso2.carbon.governance.api.generic.GenericArtifactFilter;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.registry.extensions.executors.utils.ExecutorConstants;
import org.wso2.carbon.governance.registry.extensions.interfaces.Execution;
import org.wso2.carbon.governance.registry.extensions.internal.GovernanceRegistryExtensionsComponent;
import org.wso2.carbon.governance.registry.extensions.internal.GovernanceRegistryExtensionsDataHolder;
import org.wso2.carbon.governance.registry.extensions.utils.APIUtils;
import org.wso2.carbon.governance.registry.extensions.utils.Constants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;
import org.wso2.carbon.registry.core.Registry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.governance.registry.extensions.executors.utils.ExecutorConstants.*;

/**
 * This class will be used for publishing services as apis to APIM 2.0 through publisher apis.
 * This will be invoked in promote transitions.
 * sample LC config
 * <execution forEvent="Publish" class="org.wso2.carbon.governance.registry.extensions.executors.apistore.ApiStore2Executor">
 * <parameter name="apim.username" value="admin"/>
 * <parameter name="apim.password" value="admin"/>
 * <parameter name="apim.endpoint" value="http://localhost:9763/"/>
 * <parameter name="apim.env" value="Production"/>
 * <parameter name="apim.publisher" value="local"/>
 * </execution>
 */
public class ApiStore2Executor implements Execution {

	private static final Log LOG = LogFactory.getLog(ApiStore2Executor.class);

	private String soapServiceMediaType = "application/vnd.wso2-soap-service+xml";
	private String apimEndpoint = null;
	private String apimUsername = null;
	private String apimPassword = null;
	private String defaultTier = "Unlimited";
	private String apimEnv = null;
	private String apimPublisher = null;  //local or external
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
		if (parameterMap.get(APIM_PUBLISHER) != null) {
			apimPublisher = parameterMap.get(APIM_PUBLISHER).toString();
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
			String artifactRelativePath = artifactAbsolutePath.substring("/_system/governance".length());
			Resource apiArtifact = registry.get(artifactRelativePath);
			GovernanceUtils.loadGovernanceArtifacts((UserRegistry)registry,
			GovernanceUtils.findGovernanceArtifactConfigurations(registry));
			GenericArtifactManager	artifactManager;
			if (resource.getMediaType().equals(soapServiceMediaType)){
				artifactManager = new GenericArtifactManager(registry, "soapservice");
			} else {
				artifactManager = new GenericArtifactManager(registry, "restservice");
			}
			String artifactId = apiArtifact.getUUID();
			artifact = artifactManager.getGenericArtifact(artifactId);
			apiName = artifact.getAttribute("overview_name");

		} catch (RegistryException e) {
			LOG.error("Failed to convert service to xml content", e);
			return false;
		} catch (Exception e) {
			LOG.error("Exception ocurred while getting rest service artifact", e);
			return false;
		}
		try {
			publishDataToAPIM(resource, apiName);

		} catch (GovernanceException e) {
			LOG.error("Exception occurred while publishing to APIM", e);
			return false;
		} catch (Exception e) {
			LOG.error("Exception occurred while publishing to APIM", e);
			return false;
		}

		if (apimPublisher != null && apimPublisher.equalsIgnoreCase("local")) {
			try {
				createAssociation();
			} catch (RegistryException e) {
				LOG.error("Exception occurred while creating association between rest service and api", e);
				return false;
			}
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
		String sessionCookie = APIUtils.authenticateAPIM_2(httpContext, apimEndpoint, apimUsername, apimPassword);
		String addAPIendpoint = apimEndpoint + Constants.APIM_2_0_0_ENDPOINT;

		try {
			// create a post request to addAPI.
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(addAPIendpoint);
			httppost.setHeader("Cookie", "JSESSIONID="+ sessionCookie);
			// Request parameters and other properties.
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			addParameters(params, resource, serviceName);
			LOG.info(new UrlEncodedFormEntity(params, Constants.UTF_8_ENCODE));
			APIUtils.callAPIMToPublishAPI2(httpclient, httppost, params,httpContext);
		} catch (Exception e) {
			LOG.error("Exception occurred while publishing to APIM", e);
			throw new GovernanceException(e.getMessage(), e);
		}
		return valid;
	}

	private void addParameters(List<NameValuePair> params, Resource resource, String serviceName) throws GovernanceException{

		String[] endPoints=artifact.getAttributes(Constants.ENDPOINTS_ENTRY);
		String[] urlPatterns = artifact.getAttributes(ExecutorConstants.API_URL_PATTERNS);
		String[] httpVerbs = artifact.getAttributes(ExecutorConstants.API_URL_HTTPVERB);
		String[] urlAuthType = artifact.getAttributes(ExecutorConstants.API_URL_AUTHTYPE);

		//Parameters for create API
		params.add(new BasicNameValuePair(ExecutorConstants.API_OVERVIEW_NAME, serviceName));
		params.add(new BasicNameValuePair(ExecutorConstants.API_ACTION, "design"));
		params.add(new BasicNameValuePair(ExecutorConstants.API_OVERVIEW_DESCRIPTION, ""));
		params.add(new BasicNameValuePair(ExecutorConstants.API_OVERVIEW_THUMBNAIL, ""));
		params.add(new BasicNameValuePair(ExecutorConstants.API_ROLES, ""));
		params.add(new BasicNameValuePair(ExecutorConstants.API_OVERVIEW_CONTEXT, serviceName));
		params.add(new BasicNameValuePair(ExecutorConstants.API_OVERVIEW_VERSION, artifact.getAttribute(SERVICE_VERSION)));
		params.add(new BasicNameValuePair(ExecutorConstants.API_OVERVIEW_TAGS, artifact.getAttribute(Constants.OVERVIEW_BUSINESS_UNIT)));
		params.add(new BasicNameValuePair(ExecutorConstants.API_VISIBLITY, DEFAULT_VISIBILITY));
		params.add(new BasicNameValuePair(ExecutorConstants.API_OVERVIEW_PROVIDR, CarbonContext.getThreadLocalCarbonContext()
				.getUsername()));
		if (urlPatterns != null && urlPatterns.length > 0) {
			StringBuffer sb = new StringBuffer();
			sb.append("{\"paths\":{");
			for (int i = 0; i < urlPatterns.length; i++) {
				sb.append("\"");
				sb.append(urlPatterns[i]);
				sb.append("\":{\"");
				sb.append(httpVerbs[i]);
				sb.append("\":{\"responses\":{\"200\":{}}}");
				sb.append("}");
				if (urlPatterns.length > 1 && i != urlPatterns.length -1) {
					sb.append(",");
				}
			}
			sb.append("},");
			sb.append("\"swagger\":\"2.0\",");
			sb.append("\"info\":{\"title\":\"\",\"version\":\"\"}}\n");
			params.add(new BasicNameValuePair(ExecutorConstants.API_SWAGGER, sb.toString()));
		} else {
			params.add(new BasicNameValuePair(ExecutorConstants.API_SWAGGER, ExecutorConstants.DEFAULT_SWAGGER_DOC));
		}
		String endpointConfigJson = "";
		if (endPoints != null && endPoints.length > 0) {
			for (int i = 0; i < endPoints.length; i++) {
				if (endPoints[i] != null) {
					if (endPoints[i] != null && endPoints[i].startsWith(ExecutorConstants.DEFAULT_ENDPOINT_ENV)) {
						String url = endPoints[i].split(":")[1];
						endpointConfigJson = "{\"production_endpoints\":{\"url\":\"" +
								url + "\",\"config\":null},\"endpoint_type\":\"http\"}";
						break;
					}
				}
			}
		}
		if (!endpointConfigJson.equals("")) {
			params.add(new BasicNameValuePair(ExecutorConstants.API_ENDPOINT_CONFIG, endpointConfigJson));
		} else {
			params.add(new BasicNameValuePair(ExecutorConstants.API_ENDPOINT_CONFIG, null));
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

	/**
	 * Create an association between rest service and api.
	 *
	 */
	private void createAssociation() throws GovernanceException, RegistryException{
		GenericArtifactManager artifactManager = new GenericArtifactManager(registry, "api");
		GenericArtifact[] artifacts = artifactManager.findGenericArtifacts(
				new GenericArtifactFilter() {
					public boolean matches(GenericArtifact apiArtifact) throws GovernanceException {
						String attributeVal = apiArtifact.getAttribute("overview_name");
						return (attributeVal != null && attributeVal.equals(artifact.getAttribute("overview_name")));
					}
				});
		if (artifacts != null && artifacts.length > 0) {
			GenericArtifact api = artifacts[0];
			api.addAssociation("createdBy", artifact);
			artifact.addAssociation("promotedTo", api);
		}
	}
}
