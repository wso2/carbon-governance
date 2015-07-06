/*
 * Copyright (c) 2010-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.governance.registry.extensions.aspects.utils;

public class LifecycleConstants {

    /**
     * Defines the prefix of the property name that is used to add checklist items
     */
    public static final String REGISTRY_CUSTOM_LIFECYCLE_CHECKLIST_OPTION =
            "registry.custom_lifecycle.checklist.option.";

    /**
     * Defines the prefix of the property name that is used to add script elements
     */
    public static final String REGISTRY_CUSTOM_LIFECYCLE_CHECKLIST_JS_SCRIPT_CONSOLE =
            "registry.custom_lifecycle.checklist.js.script.console.";

    /**
     * Defines the prefix of the property name that is used to add the transition UIs
     */
    public static final String REGISTRY_CUSTOM_LIFECYCLE_CHECKLIST_TRANSITION_UI =
            "registry.custom_lifecycle.checklist.transition.ui.";

    /**
     * Defines the execution elements
     */
    public static final String EXECUTION = "execution";

    /**
     * Defines the validation elements
     */
    public static final String VALIDATION = "validation";

    /**
     * Defines the attribute name of the audit object
     */
    public static final String STAT_COLLECTION = "statCollection";

    /**
     * Defines the base path of the history resources. All the history resources will be kept in this location.
     */
    public static final String REGISTRY_LIFECYCLE_HISTORY_ORIGINAL_PATH = "registry.lifecycle_history.originalPath";

    /**
     * Defines the prefix of the state property
     */
    public static final String REGISTRY_LIFECYCLE = "registry.lifecycle.";

    /**
     * Defines "forEvent" attribute name
     */
    public static final String FOR_EVENT = "forEvent";

    /**
     * Defines the suffix of the property name that is used to add checklist items
     */
    public static final String ITEM = ".item";

    /**
     * Defines the suffix of the property name that is used to add checklist items permissions
     */
    public static final String ITEM_PERMISSION = ".item.permission";

    /**
     * Defines the "itemClick" action
     */
    public static final String ITEM_CLICK = "itemClick";

    /**
     * Defines the transition name
     */
    public static final String TRANSITION = "transition";

    /**
     * Defines type attribute
     */
    public static final String TYPE = "type";

    /**
     * Defines name attribute
     */
    public static final String NAME = "name";

    /**
     * Defines the attribute name that is used to enable auditing
     */
    public static final String AUDIT = "audit";

    /**
     * Defines the custom validations message key
     */
    public static final String VALIDATIONS_MESSAGE_KEY = "validationsMessage";

    /**
     * Defines the custom validations message key
     */
    public static final String EXECUTOR_MESSAGE_KEY = "executorMessage";
    
    /**
     * Defines the prefix of the property name that is used to add checklist items
     */
    public static final String REGISTRY_CUSTOM_LIFECYCLE_VOTES_OPTION =
            "registry.custom_lifecycle.votes.option.";
    
    /**
     * Defines the suffix of the property name that is used to add vote checklist items
     */
    public static final String VOTE = ".vote";
    
    /**
     * Defines the suffix of the property name that is used to add vote checklist items permissions
     */
    public static final String VOTE_PERMISSION = ".vote.permission";
    
    /**
     * Defines the prefix of the property name that is used to add user specific vote checklist items
     */
    public static final String REGISTRY_CUSTOM_LIFECYCLE_USER_VOTE =
            "registry.custom_lifecycle.user.vote";

    /**
     * Lifecycle history items timestamp.
     */
    public static final String STATE = ".state";

    /**
     * Registry path for lifecycle history stored location.
     */
    public static final String LOG_DEFAULT_PATH = "/_system/governance/repository/components/org.wso2.carbon"
            + ".governance/lifecycles/history/";

    /**
     * XPATH expression for extracting lifecycle history items targetState.
     */
    public static final String HISTORY_ITEM_TARGET_STATE_XPATH = "//item[@targetState]";

    /**
     * XPATH expression for extracting lifecycle history items timestamp.
     */
    public static final String HISTORY_ITEM_TIME_STAMP_XPATH = "//item[@timestamp]";

    /**
     * XPATH expression for extracting lifecycle history items timestamp.
     */
    public static final String HISTORY_ITEM_LIFECYCLE_NAME_PARAMETER = "@aspect";

    /**
     * Lifecycle history items timestamp.
     */
    public static final String HISTORY_ITEM_TIME_STAMP = "timestamp";

    /**
     * Lifecycle history items timestamp format.
     */
    public static final String HISTORY_ITEM_TIME_STAMP_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

    /**
     * XPath for state with id condition.
     */
    public static final String XPATH_STATE_WITH_ID = "/aspect/configuration/lifecycle/*[name()='scxml']/*[name()" +
            "='state'][@id='";

    /**
     * XPath for checkpoint.
     */
    public static final String XPATH_CHECKPOINT = "/*[name()='checkpoints']/*[name()='checkpoint']";

    /**
     * lifecycle configuration namespace URI.
     */
    public static final String LIFECYCLE_CONFIGURATION_NAMESPACE_URI = "http://www.w3.org/2005/07/scxml";

    /**
     * lifecycle checkpoint boundary QName.
     */
    public static final String LIFECYCLE_CHECKPOINT_BOUNDARY = "boundary";

    /**
     * lifecycle checkpoint boundary lower boundary QName.
     */
    public static final String LIFECYCLE_LOWER_BOUNDARY = "min";

    /**
     * lifecycle checkpoint boundary upper boundary QName.
     */
    public static final String LIFECYCLE_UPPER_BOUNDARY = "max";

    /**
     * lifecycle checkpoint duration colour QName.
     */
    public static final String LIFECYCLE_DURATION_COLOUR = "durationColour";

    /**
     * lifecycle checkpoint name QName.
     */
    public static final String LIFECYCLE_CHECKPOINT_NAME = "id";
}
