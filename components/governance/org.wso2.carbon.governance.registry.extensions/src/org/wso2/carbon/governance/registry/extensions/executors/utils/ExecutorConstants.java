/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.wso2.carbon.governance.registry.extensions.executors.utils;

import org.wso2.carbon.governance.api.util.GovernanceConstants;

public class ExecutorConstants {

    /**
     * Defines the name of the property where we keep the lifecycle name
     */
    public static final String REGISTRY_LC_NAME = "registry.LC.name";

    /**
     * Defines the target environment key
     */
    public static final String TARGET_ENVIRONMENT = "targetEnvironment";

    /**
     * Defines the current state key
     */
    public static final String CURRENT_ENVIRONMENT = "currentEnvironment";

    /**
     * Defines the service media type key
     */
    public static final String SERVICE_MEDIA_TYPE_KEY = "service.mediatype";

    /**
     * defines the endpoint media type
     */
    public static final String WSDL_MEDIA_TYPE = GovernanceConstants.WSDL_MEDIA_TYPE;

    /**
     * defines the endpoint media type
     */
    public static final String SCHEMA_MEDIA_TYPE = GovernanceConstants.SCHEMA_MEDIA_TYPE;

    /**
     * defines the endpoint media type
     */
    public static final String ENDPOINT_MEDIA_TYPE = GovernanceConstants.ENDPOINT_MEDIA_TYPE;

    /**
     * defines the policy media type
     */
    public static final String POLICY_MEDIA_TYPE = GovernanceConstants.POLICY_XML_MEDIA_TYPE;


    /**
     * Defines the copy comments key
     */
    public static final String COPY_COMMENTS = "copyComments";

    /**
     * Defines the copy tags key
     */
    public static final String COPY_TAGS = "copyTags";

    /**
     * Defines the copy ratings key
     */
    public static final String COPY_RATINGS = "copyRatings";

    /**
     * Defines the copy  dependencies key
     */
    public static final String COPY_DEPENDENCIES = "copyDependencies";

    /**
     * Defines the copy all associations key
     */
    public static final String COPY_ASSOCIATIONS = "copyAssociations";

    /**
     * Defines the override key. This only works for services
     */
    public static final String OVERRIDE = "override";

    /**
     * Defines the resource name key
     */
    public static final String RESOURCE_NAME = "{@resourceName}";

    /**
     * Defines the resource path key
     */
    public static final String RESOURCE_PATH = "{@resourcePath}";

    /**
     * Defines the resource version key
     */
    public static final String RESOURCE_VERSION = "{@version}";
    
    /**
     * Defines the xpath expression that is used to find import elements
     */
    public static final String IMPORT_XPATH_STRING = "//x:import";

    /**
     * Defines the xpath expression that is used to find the embedded schema
     */
    public static final String XSD_XPATH_STRING = "//x:schema";
    
    /**
     * Defines the APIM endpoint(used to publish the service as API)
     */
    public static final String APIM_ENDPOINT = "apim.endpoint";
    
    /**
     * Defines the APIM endpoint(used to publish the service as API)
     */
    public static final String APIM_USERNAME = "apim.username";
    
    /**
     * Defines the APIM endpoint(used to publish the service as API)
     */
    public static final String APIM_PASSWORD = "apim.password";

    /**
     * Defines the APIM publisher local or external to Governance Registry
     */
    public static final String APIM_PUBLISHER = "apim.publisher";
    
    /**
     * Defines the body of the web service request
     */
    public static final String WS_PAYLOAD = "payload";
    
    /**
     * Defines the End Point Reference of the web service
     */
    public static final String WS_EPR = "epr";
      
   /**
     * Defines the xpath used to extract the value from the response
     */
    public static final String WS_RESPONSE_XPATH = "response.xpath";
    
    /**
     * Defines the name of the parameter that defines whether the web service is to be called
     * synchronous or asynchronous 
     */
    public static final String WS_ASYNC = "async";
    
    /**
     * Defines the name of the parameter that defines where the response should be stored
     */
    public static final String WS_RESPONSE_DESTINATION= "response.destination";
    
    /**
     * Defines the name of the parameter that defines whether the response is to be stored as
     * an attribute or property
     */
    public static final String WS_RESPONSE_SAVE_TYPE= "save.type";
    
    /**
     * Defines the name of the parameter that defines name of the attribute or property
     */
    public static final String WS_RESPONSE_SAVE_NAME= "save.name";
    
    /**
     * Defines the name of the parameter that defines name of the response namespace
     */
    public static final String WS_RESPONSE_NAMESPACE= "response.namespace";
    
    /**
     * Defines the name of the parameter that defines name of the response namespace prefix
     */
    public static final String WS_RESPONSE_NAMESPACE_PREFIX= "response.namespace.prefix";

    /**
     * Defines the default tier for api subscription
     */
    public static final String DEFAULT_TIER = "default.tier";

	/**
	 * Defines the API Manager login url to send login request.
	 */
	public static final String APIM_LOGIN_URL = "publisher/site/blocks/user/login/ajax/login.jag";
	/**
	 * Defines the API Manager remove API to send remove API request.
	 */
	public static final String APIM_REMOVE_URL = "publisher/site/blocks/item-add/ajax/remove.jag";
	/**
	 * Defines the API Manager publish API to send publish API request.
	 */
	public static final String APIM_PUBLISH_URL = "publisher/site/blocks/item-add/ajax/add.jag";


	//URI Template default settings
    public static final String DEFAULT_URI_PATTERN = "/*";
    public static final String DEFAULT_HTTP_VERB = "POST";
    public static final String DEFAULT_AUTH_TYPE = "Any";

    //default visibility setting
    public static final String DEFAULT_VISIBILITY = "public";

    // Those constance are used in API artifact.
    public static final String API_NAME = "name";
    public static final String API_VERSION = "version";
    public static final String API_CONTEXT = "context";
    public static final String API_ENDPOINT = "endpoint";
    public static final String API_WSDL = "wsdl";
    public static final String API_PROVIDER = "provider";
    public static final String API_TIER = "tiersCollection";
    public static final String API_STATUS = "status";
    public static final String API_PUBLISHED_STATUS = "CREATED";
    public static final String API_THROTTLING_TIER="resourceMethodThrottlingTier-0";

    public static final String API_URI_PATTERN ="uriTemplate-0";
    public static final String API_URI_HTTP_METHOD ="resourceMethod-0";
    public static final String API_URI_AUTH_TYPE ="resourceMethodAuthType-0";
	public static final String API_RESOURCE_COUNT = "resourceCount";
	public static final String API_ENDPOINT_CONFIG = "endpoint_config";
    public static final String API_ACTION = "action";
    public static final String API_VISIBLITY ="visibility";
    public static final String API_ADD_ACTION = "addAPI";
	public static final String API_REMOVE_ACTION = "removeAPI";
    public static final String API_LOGIN_ACTION = "login";
    public static final String API_UPDATESTATUS_ACTION = "updateStatus";
    public static final String API_PUBLISH_GATEWAY_ACTION = "publishToGateway";

    public static final String API_USERNAME = "username";
    public static final String API_PASSWORD = "password";

	public static final String REST_SERVICE_KEY = "restservice";
	public static final String SERVICE_NAME = "overview_name";
	public static final String SERVICE_VERSION = "overview_version";
	public static final String SERVICE_ENDPOINT_URL = "overview_endpointURL";
    public static final String THROTTLING_TIER= "throttlingTier";
	public static final String DEFAULT_CHAR_ENCODING = "UTF-8";


	//common messages
	public static final String APIM_POST_REQ_FAIL = "Failed to send the http POST request to API Manager. ";
	public static final String API_DEMOTE_FAIL = "Failed to delete API from the API manager. ";
	public static final String EMPTY_ENDPOINT = "Service Endpoint is empty.";
	public static final String APIM_LOGIN_UNDEFINED = "APIManager login credentials are not defined";
	public static final String ENCODING_FAIL = "Failed when encoding the parameter list. ";

    //API Manager 2.0.0 constants
    public static final String DESIGN_API_ACTION = "design";
    public static final String API_OVERVIEW_NAME = "overview_name";
    public static final String API_OVERVIEW_PROVIDR = "overview_provider";
    public static final String API_OVERVIEW_VERSION = "overview_version";
    public static final String API_OVERVIEW_CONTEXT = "overview_context";
    public static final String API_VISIBILITY = "visibility";
    public static final String API_ROLES = "roles";
    public static final String API_OVERVIEW_THUMBNAIL = "overview_thumbnail";
    public static final String API_OVERVIEW_DESCRIPTION = "overview_description";
    public static final String API_OVERVIEW_TAGS = "overview_tags";
    public static final String API_SWAGGER = "swagger";
    public static final String ACTION = "action";
    public static final String DEFAULT_SWAGGER_DOC = "{\"paths\":{\"/*\":{\"put\":{\"responses\":{\"200\":{}}},"
                                                     + "\"post\":{\"responses\":{\"200\":{}}},"
                                                     + "\"get\":{\"responses\":{\"200\":{}}},"
                                                     + "\"delete\":{\"responses\":{\"200\":{}}},"
                                                     + "\"head\":{\"responses\":{\"200\":{}}}}},\"swagger\":\"2.0\","
                                                     + "\"info\":{\"title\":\"\",\"version\":\"\"}}\n";

}
