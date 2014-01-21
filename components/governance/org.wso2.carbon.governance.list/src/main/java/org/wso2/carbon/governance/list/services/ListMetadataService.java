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
package org.wso2.carbon.governance.list.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.policies.dataobjects.Policy;
import org.wso2.carbon.governance.api.policies.dataobjects.PolicyImpl;
import org.wso2.carbon.governance.api.schema.dataobjects.Schema;
import org.wso2.carbon.governance.api.schema.dataobjects.SchemaImpl;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.governance.api.wsdls.dataobjects.WsdlImpl;
import org.wso2.carbon.governance.list.beans.PolicyBean;
import org.wso2.carbon.governance.list.beans.SchemaBean;
import org.wso2.carbon.governance.list.beans.ServiceBean;
import org.wso2.carbon.governance.list.beans.WSDLBean;
import org.wso2.carbon.governance.list.util.ListServiceUtil;
import org.wso2.carbon.governance.list.util.filter.FilterPolicy;
import org.wso2.carbon.governance.list.util.filter.FilterSchema;
import org.wso2.carbon.governance.list.util.filter.FilterWSDL;
import org.wso2.carbon.registry.admin.api.governance.IListMetadataService;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.extensions.utils.CommonConstants;
import org.wso2.carbon.user.core.UserStoreException;

import java.util.HashMap;
import java.util.Map;

public class ListMetadataService extends AbstractAdmin implements
        IListMetadataService<ServiceBean, WSDLBean, PolicyBean, SchemaBean> {

    private static final Log log = LogFactory.getLog(ListMetadataService.class);
    private static final String REGISTRY_WSDL_TARGET_NAMESPACE = "registry.wsdl.TargetNamespace";
    private static final String REGISTRY_SCHEMA_TARGET_NAMESPACE = "targetNamespace";

    private static Map<String,String> namespaceMap;


    public ServiceBean listservices(String criteria) throws RegistryException {
        UserRegistry registry = (UserRegistry) getGovernanceUserRegistry();
        return ListServiceUtil.fillServiceBean(registry,criteria);
    }

    private String[] getLCInfo(Resource resource) {
        String[] LCInfo = new String[2];
        String lifecycleState;
        if(resource.getProperties()!=null){
            if (resource.getProperty("registry.LC.name") != null) {
                LCInfo[0] =resource.getProperty("registry.LC.name");
            }

            if(LCInfo[0]!=null){
                lifecycleState = "registry.lifecycle." + LCInfo[0] + ".state";
                if (resource.getProperty("registry.lifecycle.ServiceLifeCycle.state") != null) {
                    LCInfo[1] = resource.getProperty("registry.lifecycle.ServiceLifeCycle.state");
                }
            }
        }
        return LCInfo;
    }

    public WSDLBean listwsdls()throws RegistryException{
        RegistryUtils.recordStatistics();
        UserRegistry registry = (UserRegistry)getGovernanceUserRegistry();
        GovernanceArtifact[] artifacts = new GovernanceArtifact[0];
        try {
            artifacts = (new FilterWSDL(null, registry,null)).getArtifacts();
        } catch (RegistryException e) {
            log.error("An error occurred while obtaining the list of WSDLs.", e);
        }

       return getWSDLBeanFromPaths(registry, artifacts);
    }

    public WSDLBean listWsdlsByName(String wsdlName)throws RegistryException{
        RegistryUtils.recordStatistics();
        UserRegistry registry = (UserRegistry)getGovernanceUserRegistry();
        GovernanceArtifact[] artifacts = new GovernanceArtifact[0];
        try {
            artifacts = (new FilterWSDL(wsdlName, registry,null)).getArtifacts();
        } catch (Exception e) {
            log.error("An error occurred while obtaining the list of WSDLs.", e);
        }
       return getWSDLBeanFromPaths(registry, artifacts);
    }

    public PolicyBean listpolicies()throws RegistryException{
        RegistryUtils.recordStatistics();
        UserRegistry registry = (UserRegistry)getGovernanceUserRegistry();
        GovernanceArtifact[] artifacts = new GovernanceArtifact[0];
        try {
            artifacts = (new FilterPolicy(null, registry,null)).getArtifacts();
        } catch (RegistryException e) {
            log.error("An error occurred while obtaining the list of policies.", e);
        }
        return getPolicyBeanFromPaths(registry, artifacts);
    }

    public PolicyBean listPoliciesByNames(String policyName) throws Exception {
        RegistryUtils.recordStatistics();
        UserRegistry registry = (UserRegistry)getGovernanceUserRegistry();
        GovernanceArtifact[] artifacts = new GovernanceArtifact[0];
        try {
            artifacts = (new FilterPolicy(policyName, registry,null)).getArtifacts();
        } catch (RegistryException e) {
            log.error("An error occurred while obtaining the list of policies.", e);
        }
        return getPolicyBeanFromPaths(registry, artifacts);
    }

    public SchemaBean listschema()throws RegistryException{
        RegistryUtils.recordStatistics();
        UserRegistry registry = (UserRegistry)getGovernanceUserRegistry();
        GovernanceArtifact[] artifacts = new GovernanceArtifact[0];
        try {
            artifacts = (new FilterSchema(null, registry,null)).getArtifacts();
        } catch (RegistryException e) {
            log.error("An error occurred while obtaining the list of schemas.", e);
        }

      return getSchemaBeanFromPaths(registry, artifacts);

    }

    public SchemaBean listSchemaByName(String schemaName)throws Exception{
        RegistryUtils.recordStatistics();
        UserRegistry registry = (UserRegistry)getGovernanceUserRegistry();
        GovernanceArtifact[] artifacts = new GovernanceArtifact[0];
        try {
            artifacts = (new FilterSchema(schemaName, registry,null)).getArtifacts();
        } catch (RegistryException e) {
            log.error("An error occurred while obtaining the list of schemas.", e);
        }

       return getSchemaBeanFromPaths(registry, artifacts);
    }

    public PolicyBean getPolicyBeanFromPaths(UserRegistry registry,
                                             GovernanceArtifact[] artifacts) throws RegistryException {
        PolicyBean bean = new PolicyBean();
        String[] path = new String[artifacts.length];
        String[] name = new String[artifacts.length];
        boolean[] canDelete = new boolean[artifacts.length];
        String[] LCName = new String[artifacts.length];
        String[] LCState = new String[artifacts.length];

        for(int i = 0; i < artifacts.length; i++){
            bean.increment();
            Policy policy = (Policy) artifacts[i];
            path[i] = ((PolicyImpl)policy).getArtifactPath();
            name[i] = policy.getQName().getLocalPart();
            if (registry.getUserRealm() != null && registry.getUserName() != null) {
                try {
                    canDelete[i] =
                            registry.getUserRealm().getAuthorizationManager().isUserAuthorized(
                                    registry.getUserName(),
                                    RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + path[i],
                                    ActionConstants.DELETE);
                } catch (UserStoreException e) {
                    canDelete[i] = false;
                }
            } else {
                canDelete[i] = false;
            }
            LCName[i] = ((PolicyImpl)policy).getLcName();
            LCState[i] = ((PolicyImpl)policy).getLcState();
        }
        bean.setName(name);
        bean.setPath(path);
        bean.setCanDelete(canDelete);
        bean.setLCName(LCName);
        bean.setLCState(LCState);
        return bean;

    }

    private SchemaBean getSchemaBeanFromPaths(UserRegistry registry,
                                              GovernanceArtifact[] artifacts) throws RegistryException {
        SchemaBean bean = new SchemaBean();
        String[] path = new String[artifacts.length];
        String[] name = new String[artifacts.length];
        String[] namespace = new String[artifacts.length];
        boolean[] canDelete = new boolean[artifacts.length];
        String[] LCName = new String[artifacts.length];
        String[] LCState = new String[artifacts.length];

        for(int i = 0; i < path.length; i++){
            bean.increment();
            Schema schema = (Schema) artifacts[i];
            path[i] = ((SchemaImpl)schema).getArtifactPath();
            name[i] = schema.getQName().getLocalPart();

            String[] pathSegments = path[i].split("/" +
                    CommonConstants.SERVICE_VERSION_REGEX.substring(1, +
                    CommonConstants.SERVICE_VERSION_REGEX.length() - 1));

            if (namespaceMap == null) {
                namespaceMap = new HashMap<String, String>();
            }

            if(pathSegments[0].endsWith(name[i])){
                pathSegments[0] = pathSegments[0].substring(0,pathSegments[0].lastIndexOf("/"));
            }

            if (namespaceMap.containsKey(pathSegments[0] + registry.getTenantId())) {
                namespace[i] = namespaceMap.get(pathSegments[0] + registry.getTenantId());
            } else {
                namespace[i] = schema.getQName().getNamespaceURI();
                namespaceMap.put(pathSegments[0] + registry.getTenantId(), namespace[i]);
            }
            if (registry.getUserRealm() != null && registry.getUserName() != null) {
                try {
                    canDelete[i] =
                            registry.getUserRealm().getAuthorizationManager().isUserAuthorized(
                                    registry.getUserName(),
                                    RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + path[i],
                                    ActionConstants.DELETE);
                } catch (UserStoreException e) {
                    canDelete[i] = false;
                }
            } else {
                canDelete[i] = false;
            }
            LCName[i] = ((SchemaImpl)schema).getLcName();
            LCState[i] = ((SchemaImpl)schema).getLcName();
        }
        bean.setName(name);
        bean.setNamespace(namespace);
        bean.setPath(path);
        bean.setCanDelete(canDelete);
        bean.setLCName(LCName);
        bean.setLCState(LCState);
        return bean;
    }

    private WSDLBean getWSDLBeanFromPaths(UserRegistry registry,
                                          GovernanceArtifact[] artifacts) throws RegistryException {
        WSDLBean bean = new WSDLBean();

        String[] path = new String[artifacts.length];
        String[] name = new String[artifacts.length];
        String[] namespaces = new String[artifacts.length];
        boolean[] canDelete = new boolean[artifacts.length];
        String[] LCName = new String[artifacts.length];
        String[] LCState = new String[artifacts.length];

        for(int i = 0; i < artifacts.length; i++){
            bean.increment();
            Wsdl wsdl = (Wsdl) artifacts[i];
            path[i] = ((WsdlImpl)wsdl).getArtifactPath();
            name[i] = wsdl.getQName().getLocalPart();
            String[] pathSegments = path[i].split("/" +
                    CommonConstants.SERVICE_VERSION_REGEX.substring(1, +
                    CommonConstants.SERVICE_VERSION_REGEX.length() - 1));

            if(namespaceMap == null){
                namespaceMap = new HashMap<String, String>();
            }

            if(pathSegments[0].endsWith(name[i])){
                pathSegments[0] = pathSegments[0].substring(0,pathSegments[0].lastIndexOf("/"));
            }

            if (namespaceMap.containsKey(pathSegments[0] + registry.getTenantId())) {
                namespaces[i] = namespaceMap.get(pathSegments[0] + registry.getTenantId());
            } else {
                namespaces[i] = wsdl.getQName().getNamespaceURI();
                namespaceMap.put(pathSegments[0] + registry.getTenantId(), namespaces[i]);
            }
            LCName[i] = ((WsdlImpl)wsdl).getLcName();
            LCState[i] = ((WsdlImpl)wsdl).getLcState();
            if (registry.getUserRealm() != null && registry.getUserName() != null) {
                try {
                    canDelete[i] =
                            registry.getUserRealm().getAuthorizationManager().isUserAuthorized(
                                    registry.getUserName(),
                                    RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + path[i],
                                    ActionConstants.DELETE);
                } catch (UserStoreException e) {
                    canDelete[i] = false;
                }
            } else {
                canDelete[i] = false;
            }
        }
        bean.setName(name);
        bean.setNamespace(namespaces);
        bean.setPath(path);
        bean.setCanDelete(canDelete);
        bean.setLCName(LCName);
        bean.setLCState(LCState);
        return bean;
    }


    public String[] getAllLifeCycleState(String LCName) {
        return new String[0];
    }




}
