/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.governance.api.util;

import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.extensions.utils.CommonConstants;

/**
 * Constants used by the governance API.
 */
public class GovernanceConstants {

    /**
     * Association type for dependencies.
     */
    public static final String DEPENDS = CommonConstants.DEPENDS;

    /**
     * Association type of usages.
     */
    public static final String USED_BY = CommonConstants.USED_BY;

    /**
     * Association type for people owning an artifact.
     */
    public static final String OWNS = CommonConstants.OWNS;

    /**
     * Association type for people owning an artifact.
     */
    public static final String OWNED_BY = CommonConstants.OWNED_BY;

    /**
     * Association type for people consuming a service
     */
    public static final String CONSUMES = CommonConstants.CONSUMES;

    /**
     * Association type for services consumed by people
     */
    public static final String CONSUMED_BY = CommonConstants.CONSUMED_BY;

    /**
     * Association type for a sub-group of another group
     */
    public static final String IS_PART_OF = "isPartOf"; //TODO: Move to CommonConstants??

    /**
     * Association type for a group having a sub-group
     */
    public static final String SUB_GROUP = "subGroup";

    /**
     * The id of the cache that will be used sync RXT configurations
     */
    public static final String RXT_CONFIG_CACHE_ID = "RXT_CONFIG_CACHE";
    /**
     *
     */
    public static final String SERVICE_ARTIFACT_KEY = "service";


    /**
     * This is the character used in configured governance artifact (service, people, sla, etc..)
     * xml content to separate an entry name and a value
     */
    public static final String ENTRY_VALUE_SEPARATOR = ":";

    /**
     * The index path of the governance artifacts.
     */
    public static final String GOVERNANCE_ARTIFACT_INDEX_PATH =
            CommonConstants.GOVERNANCE_ARTIFACT_INDEX_PATH;

    /**
     * The key of the artifact id property.
     */
//    public static final String ARTIFACT_ID_PROP_KEY =
//            CommonConstants.ARTIFACT_ID_PROP_KEY;

    ////////////////////////////////////////////////////////
    // Media types
    ////////////////////////////////////////////////////////

    /**
     * Media type of a artifact configuration file.
     */
    public static final String GOVERNANCE_ARTIFACT_CONFIGURATION_MEDIA_TYPE =
            "application/vnd.wso2.registry-ext-type+xml";

    /**
     * Media type of a policy artifact.
     */
    public static final String POLICY_XML_MEDIA_TYPE = "application/policy+xml";

    /**
     * Media type of a WSDL artifact.
     */
    public static final String WSDL_MEDIA_TYPE = "application/wsdl+xml";

    /**
     * Media type of a process artifact.
     */
    public static final String PROCESS_MEDIA_TYPE = "application/vnd.wso2-process+xml";

    /**
     * Media type of a sla artifact.
     */
    public static final String SLA_MEDIA_TYPE = "application/vnd.wso2-sla+xml";

    /**
     * Media type of a service artifact.
     */
    public static final String SERVICE_MEDIA_TYPE = RegistryConstants.SERVICE_MEDIA_TYPE;

    /**
     * Version of a  service artifact.
     */
    public static final String SERVICE_VERSION_ATTRIBUTE = "overview_version";

    /**
     * Media type of a consumer artifact
     */
    public static final String PEOPLE_MEDIA_TYPE = "application/vnd.wso2-people+xml";

    /**
     * Media type of a endpoint artifact.
     */
    public static final String ENDPOINT_MEDIA_TYPE = CommonConstants.ENDPOINT_MEDIA_TYPE;

    /**
     * Media type of a schema artifact.
     */
    public static final String SCHEMA_MEDIA_TYPE = "application/x-xsd+xml";

    ////////////////////////////////////////////////////////
    // Constants related to services
    ////////////////////////////////////////////////////////

    /**
     * The root element of the content of the service artifact.
     */
    public static final String SERVICE_ELEMENT_ROOT = CommonConstants.SERVICE_ELEMENT_ROOT;
    /**
     * The name attribute of the service artifact.
     */
    public static final String SERVICE_NAME_ATTRIBUTE = CommonConstants.SERVICE_NAME_ATTRIBUTE;

    /**
     * The common namespace used by the service artifacts.
     */
    public static final String SERVICE_ELEMENT_NAMESPACE =
            CommonConstants.SERVICE_ELEMENT_NAMESPACE;

    /**
     * The namespace attribute of the service artifact. This is the namespace of the particular
     * service.
     */
    public static final String SERVICE_NAMESPACE_ATTRIBUTE =
            CommonConstants.SERVICE_NAMESPACE_ATTRIBUTE;

    /**
     * The WSDL attribute of the service artifact.
     */
    public static final String SERVICE_WSDL_ATTRIBUTE = CommonConstants.SERVICE_WSDL_ATTRIBUTE;

    /**
     * The owner attribute of the service artifact.
     */
    public static final String SERVICE_OWNERS_ATTRIBUTE = CommonConstants.SERVICE_OWNERS_ATTRIBUTE;

    /**
     * The owner attribute of the service artifact.
     */
    public static final String SERVICE_CONSUMERS_ATTRIBUTE = CommonConstants.SERVICE_CONSUMERS_ATTRIBUTE;
    //TODO: Remove these constants from CommonConstants
    /**
     * The BPEL attribute of the process artifact.
     */
    public static final String PROCESS_BPEL_ATTRIBUTE = CommonConstants.PROCESS_BPEL_ATTRIBUTE;

    ////////////////////////////////////////////////////////
    // Constants related to people
    ////////////////////////////////////////////////////////

    /**
     * The root element of the content of a People artifact.
     */
    public static final String PEOPLE_ELEMENT_ROOT = CommonConstants.PEOPLE_ELEMENT_ROOT;

    /**
     * The group attribute of a people artifact.
     */
    public static final String PEOPLE_GROUP_ATTRIBUTE = "overview_group";

    /**
     * The type attribute of a people artifact.
     */
    public static final String PEOPLE_TYPE_ATTRIBUTE = "overview_type";

    /**
     * The affiliations attribute of a people artifact. (applicable to project groups and persons)
     */
    public static final String AFFILIATIONS_ATTRIBUTE = "affiliations_entry";

    /**
     * The group attribute value for person types.
     */
    public static final String PEOPLE_GROUP_ATTRIBUTE_VALUE_PERSON = "person";
    /**
     * The group attribute value for organization types.
     */
    public static final String PEOPLE_GROUP_ATTRIBUTE_VALUE_ORGANIZATION = "organization";

    /**
     * The group attribute value for department types.
     */
    public static final String PEOPLE_GROUP_ATTRIBUTE_VALUE_DEPARTMENT = "department";

    /**
     * The group attribute value for project group types.
     */
    public static final String PEOPLE_GROUP_ATTRIBUTE_VALUE_PROJECT_GROUP = "project_group";

    /**
     * The person's type attribute for a consumer type person.
     */
    public static final String PEOPLE_TYPE_ATTRIBUTE_VALUE_CONSUMER = "consumer";

/**
     * The person's type attribute for a internal type person.
     */
    public static final String PEOPLE_TYPE_ATTRIBUTE_VALUE_INTERNAL = "internal";


/**
     * The person's type attribute for a provider type person.
     */
    public static final String PEOPLE_TYPE_ATTRIBUTE_VALUE_PROVIDER = "provider";
    /*
     This the path which save the content of configurations.
     */
    public static final String ARTIFACT_CONTENT_PATH="/_system/config/repository/components/org.wso2.carbon.governance/configuration/";

   /*
     This is the path which save the rxt config files.
     */
   public static final String RXT_CONFIGS_PATH = RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
           RegistryConstants.GOVERNANCE_COMPONENT_PATH + RegistryConstants.PATH_SEPARATOR + "types";

    /**
     * Defines the environment change of the resource in order to be used in creating notifications
     */
    public static final String REGISTRY_IS_ENVIRONMENT_CHANGE = "registry.is.environment.change.property";
     //Default service name
    public static final String DEFAULT_SERVICE_NAME="C0E6D4A8-C446-4f01-99DB-70E212685A40";
    // Default service namespace
    public static final String DEFAULT_NAMESPACE=".*";

    public static final String PRE_FETCH_TASK = "PRE_FETCH_TASK";

    public static final String PRE_FETCH_TASK_CLASS = "org.wso2.carbon.governance.list.util.task.PreFetchTask";

    public static final String TASK_CLASS = "taskClass";

    public static final String ARTIFACT_TYPE = "artifactType";

    public static final class ArtifactTypes {

        private ArtifactTypes() {
            throw new AssertionError();
        }

        public static String SERVICE = "SERVICE";
        public static String WSDL = "WSDL";
        public static String POLICY = "POLICY";
        public static String SCHEMA = "SCHEMA";
        public static String GENERIC = "GENERIC";
    }

    /**
     * The name attribute.
     */
    public static final String NAME_ATTRIBUTE = "overview_name";

    /**
     * The version attribute.
     */
    public static final String VERSION_ATTRIBUTE = "overview_version";

}
