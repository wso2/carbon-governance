/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.mashup.javascript.hostobjects.registry;

public class MashupConstants {

    /**
     * AxisService is injected to the Rhino engine as a thread local variable
     * with the key set to this value. Values from the AxisService is needed by some host
     * objects at the deployment time as well as in the run time.. In the run
     * time we would have obtained the AxisService through the injected
     * MessageContext. But we simply don't have a MessageContext in
     * the deployment time.
     */
    public static final String AXIS2_SERVICE = "axisService";

    /**
     * ConfigurationContext is injected to the Rhino engine as a thread local variable
     * with the key set to this value. Reason for doing this is same as the injecting the AxisService.
     */
    public static final String AXIS2_CONFIGURATION_CONTEXT = "axisConfigurationContext";

    public static final String RESOURCES_FOLDER = "ResourcesFolder";

    public static final String FORWARD_SLASH = "/";

    public static final String SEPARATOR_CHAR = "/";

    public static final String FILE_HOST_OBJECT_NAME = "org.wso2.carbon.mashup.javascript.hostobjects.file.FileHostObject";

    public static final String RAMPART = "rampart";

    public static final String ADDRESSING = "addressing";

    public static final String USERNAME = "username";

    public static final String PASSWORD = "password";

    public static final String EMAIL_CONFIG = "EmailConfig";

    public static final String EMAIL_USERNAME = EMAIL_CONFIG + "." + USERNAME;

    public static final String EMAIL_PASSWORD = EMAIL_CONFIG + "." + PASSWORD;

    public static final String HOST = EMAIL_CONFIG + "." + "host";

    public static final String PORT = EMAIL_CONFIG + "." + "port";

    public static final String FROM = EMAIL_CONFIG + "." + "from";

    public static final String IM_CONFIG = "IMConfig";

    public static final String JS_SERVICE = "js_service";

    public static final String SERVICE_JS = "ServiceJS";

    public static final String ACTIVE_SCOPE = "active_scope";

    public static final String JS_SERVICE_REPO = "jsservices";

    public static final String JS_SERVICE_EXTENSION = ".js";

    public static final String JS_SCHEDULED_FUNCTION_MAP = "js_sheduled_function_map";

    public static final String LOAD_JSSCRIPTS = "loadJSScripts";

    public static final String CONTENT_TYPE_QUERY_PARAM = "content-type";

    public static final String JS_FUNCTION_NAME = "jsFunctionName";

    public static final String ANNOTATED = "annotated";

    public static final String AXIS2_MESSAGECONTEXT = "messageContext";

    public static final String MASHUP_AUTHOR = "mashupAuthor";

    public static final String SERVICE_JS_STREAM = "serviceJsStream";
}

