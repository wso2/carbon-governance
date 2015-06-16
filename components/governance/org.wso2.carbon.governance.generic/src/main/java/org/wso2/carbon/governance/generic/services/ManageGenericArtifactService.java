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
package org.wso2.carbon.governance.generic.services;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifactImpl;
import org.wso2.carbon.governance.api.util.GovernanceArtifactConfiguration;
import org.wso2.carbon.governance.api.util.GovernanceConstants;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.generic.beans.ArtifactBean;
import org.wso2.carbon.governance.generic.beans.ArtifactsBean;
import org.wso2.carbon.governance.generic.beans.ContentArtifactsBean;
import org.wso2.carbon.governance.generic.beans.StoragePathBean;
import org.wso2.carbon.governance.generic.util.GenericArtifactUtil;
import org.wso2.carbon.governance.generic.util.Util;
import org.wso2.carbon.governance.registry.extensions.utils.CommonUtil;
import org.wso2.carbon.registry.admin.api.governance.IManageGenericArtifactService;
import org.wso2.carbon.registry.common.CommonConstants;
import org.wso2.carbon.registry.common.services.RegistryAbstractAdmin;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.indexing.IndexingConstants;
import org.wso2.carbon.user.core.UserStoreException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.*;

@SuppressWarnings({"unused", "NonJaxWsWebServices", "ValidExternallyBoundObject"})
public class ManageGenericArtifactService extends RegistryAbstractAdmin implements IManageGenericArtifactService {
    private static final Log log = LogFactory.getLog(ManageGenericArtifactService.class);
    private static final String GOVERNANCE_ARTIFACT_CONFIGURATION_PATH =
            RegistryConstants.GOVERNANCE_COMPONENT_PATH + "/configuration/";

    public String addArtifact(String key, String info, String lifecycleAttribute) throws
            RegistryException {
        RegistryUtils.recordStatistics(key, info, lifecycleAttribute);
        Registry registry = getGovernanceUserRegistry();
        if (RegistryUtils.isRegistryReadOnly(registry.getRegistryContext())) {
            return null;
        }
        try {
        	XMLInputFactory factory = XMLInputFactory.newInstance();
        	factory.setProperty(XMLInputFactory.IS_COALESCING, true);
            XMLStreamReader reader = factory.createXMLStreamReader(new StringReader(info));

            GenericArtifactManager manager = new GenericArtifactManager(registry, key);
            GenericArtifact artifact = manager.newGovernanceArtifact(new StAXOMBuilder(reader).getDocumentElement());
            
            // want to save original content, so set content here
            artifact.setContent(info.getBytes());
            artifact.setAttribute("resource.source", "AdminConsole");
            manager.addGenericArtifact(artifact);
            if (lifecycleAttribute != null) {
                String lifecycle = artifact.getAttribute(lifecycleAttribute);
                if (lifecycle != null) {
                    artifact.attachLifecycle(lifecycle);
                }
            }
            return RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + artifact.getPath();
        } catch (Exception e) {
            String msg = "Unable to add artifact. ";
            if (e instanceof RegistryException) {
                throw (RegistryException) e;
            } else if (e instanceof OMException) {
                msg += "Unexpected character found in input-field name.";
                log.error(msg, e);
                throw new RegistryException(msg, e);
            }
            throw new RegistryException(
                    msg + (e.getCause() instanceof SQLException ? "" : e.getCause().getMessage()),
                    e);
        }
    }

    public StoragePathBean getStoragePath(String key) {
        StoragePathBean bean = new StoragePathBean();
        try {
            GovernanceArtifactConfiguration configuration =
                    GovernanceUtils.findGovernanceArtifactConfiguration(key, getRootRegistry());
            bean.setStoragePath(configuration.getPathExpression());
            OMElement contentDefinition = configuration.getContentDefinition();
            Iterator fields = contentDefinition.getChildrenWithName(new QName("field"));
            List<String> names = new LinkedList<String>();
            List<String> labels = new LinkedList<String>();
            while (fields.hasNext()) {
                OMElement fieldElement = (OMElement) fields.next();
                OMElement nameElement = fieldElement.getFirstChildWithName(new QName("name"));
                String name = nameElement.getText();
                names.add(name);
                String label = nameElement.getAttributeValue(new QName("label"));
                labels.add(label != null ? label : name);
                bean.increment();
            }
            if (bean.getSize() > 0) {
                bean.setNames(names.toArray(new String[names.size()]));
                bean.setLabels(labels.toArray(new String[labels.size()]));
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("An error occurred while obtaining the storage path details.", e);
        }
        return bean;
    }

    private boolean nameMatches(GovernanceArtifact artifact, String criteria)
            throws GovernanceException {
        String name = getName(artifact);
        return name != null && name.contains(criteria);
    }


    private boolean lcMatches(GovernanceArtifact artifact, String LCName, String LCState, String LCInOut, String LCStateInOut)
            throws GovernanceException {
        String name = artifact.getLifecycleName();
        String state = artifact.getLifecycleState();
        if(LCName.equalsIgnoreCase("")){
            return true;
        }
        if(!LCState.equalsIgnoreCase("")){
            if(LCInOut.equalsIgnoreCase("in") && LCStateInOut.equalsIgnoreCase("in")){
                if(name != null
                   && state != null && LCState.equalsIgnoreCase(state)
                   && LCName.equalsIgnoreCase(name)){
                    return true;
                }else{
                    return false;
                }
            } else if(LCInOut.equalsIgnoreCase("in") && !LCStateInOut.equalsIgnoreCase("in")){
                if(name != null
                   && state != null && !LCState.equalsIgnoreCase(state)
                   && LCName.equalsIgnoreCase(name)){
                    return true;
                }else{
                    return false;
                }
            } else if(!LCInOut.equalsIgnoreCase("in") && LCStateInOut.equalsIgnoreCase("in")){
                if(name != null
                   && state != null && LCState.equalsIgnoreCase(state)
                   && !LCName.equalsIgnoreCase(name)){
                    return true;
                }else{
                    return false;
                }
            } else {
                if(name != null
                   && state != null && !LCState.equalsIgnoreCase(state)
                   && !LCName.equalsIgnoreCase(name)){
                    return true;
                }else{
                    return false;
                }
            }

        }else{
            if(LCInOut.equalsIgnoreCase("in")){
                if(name != null
                   && LCName.equalsIgnoreCase(name)){
                    return true;
                }else{
                    return false;
                }
            }else{
                if(!LCName.equalsIgnoreCase(name)){
                    return true;
                }else{
                    return false;
                }
            }
        }
    }

    private String getName(GovernanceArtifact artifact) {
        String local = artifact.getQName().getLocalPart();
        if (local != null && !"".equals(local)) {
            if (local.contains("\\.")) {
                return local.substring(0, local.lastIndexOf("\\."));
            } else {
                return local;
            }
        }
        return local;
    }

    public ContentArtifactsBean listContentArtifacts(String mediaType)throws RegistryException{
        return listContentArtifactsByName(mediaType, null);
    }

    public ContentArtifactsBean listContentArtifactsByLC(String mediaType, String LCName, String LCState,
                                                         String LCInOut, String LCStateInOut)throws RegistryException{
        RegistryUtils.recordStatistics();
        //Construct Solr input
        if ("Any".equals(LCName)) {
            LCName = "*";
        }
        ContentArtifactsBean bean = new ContentArtifactsBean();
        UserRegistry registry = (UserRegistry)getGovernanceUserRegistry();
        GovernanceArtifactConfiguration configuration =
                GovernanceUtils.findGovernanceArtifactConfigurationByMediaType(mediaType, getRootRegistry());

        GenericArtifactManager manager = new GenericArtifactManager(registry,
                configuration.getMediaType(), configuration.getArtifactNameAttribute(),
                configuration.getArtifactNamespaceAttribute(),
                configuration.getArtifactElementRoot(),
                configuration.getArtifactElementNamespace(),
                configuration.getPathExpression(),
                configuration.getRelationshipDefinitions());
        Map<String, List<String>> fields = new HashMap<String, List<String>>();
        if (LCName.length() > 0) {
            if (LCState.length() > 0) {
                fields.put(LCStateInOut.equalsIgnoreCase("in") ? IndexingConstants.FIELD_LC_STATE : "-" +
                        IndexingConstants.FIELD_LC_STATE, Arrays.asList(LCState));
            }
            fields.put(LCInOut.equalsIgnoreCase("in") ? IndexingConstants.FIELD_LC_NAME : "-" +
                    IndexingConstants.FIELD_LC_NAME, Arrays.asList(LCName));
        }
        GenericArtifact[] artifacts = manager.findGenericArtifacts(fields);
        if (artifacts != null) {
            String[] names = new String[artifacts.length];
            String[] namespaces = new String[artifacts.length];
            boolean[] canDelete = new boolean[artifacts.length];
            String[] lifecycleName = new String[artifacts.length];
            String[] lifecycleState = new String[artifacts.length];
            String[] paths = new String[artifacts.length];
            for (GenericArtifact artifact : artifacts) {
                int i = bean.getSize();
                paths[i] = artifact.getPath();
                names[i] = artifact.getQName().getLocalPart();
                namespaces[i] = artifact.getQName().getNamespaceURI();
                lifecycleName[i] = artifact.getLifecycleName();
                lifecycleState[i] = artifact.getLifecycleState();
                if (registry.getUserRealm() != null && registry.getUserName() != null) {
                    try {
                        canDelete[i] =
                                registry.getUserRealm().getAuthorizationManager().isUserAuthorized(
                                        registry.getUserName(),
                                        RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + paths[i],
                                        ActionConstants.DELETE);
                    } catch (UserStoreException ignored) {
                    }
                }
                bean.increment();
            }
            bean.setName(names);
            bean.setNamespace(namespaces);
            bean.setPath(paths);
            bean.setCanDelete(canDelete);
            bean.setLCName(lifecycleName);
            bean.setLCState(lifecycleState);
        }
         return bean;
    }

    public ContentArtifactsBean listContentArtifactsByName(String mediaType, String criteria)
            throws RegistryException{
        RegistryUtils.recordStatistics();
        ContentArtifactsBean bean = new ContentArtifactsBean();
        UserRegistry registry = (UserRegistry)getGovernanceUserRegistry();
        GovernanceArtifactConfiguration configuration =
                GovernanceUtils.findGovernanceArtifactConfigurationByMediaType(mediaType, getRootRegistry());
        //Configuration will be null for uploading gar/zip file, because media type is application/vnd.wso2.governance-archive
        if (configuration == null) {
            return null;
        }
        GenericArtifactManager manager = new GenericArtifactManager(registry,
                configuration.getMediaType(), configuration.getArtifactNameAttribute(),
                configuration.getArtifactNamespaceAttribute(),
                configuration.getArtifactElementRoot(),
                configuration.getArtifactElementNamespace(),
                configuration.getPathExpression(),
                configuration.getRelationshipDefinitions());
        GenericArtifact[] artifacts;
        if (criteria == null) {
            artifacts = manager.findGenericArtifacts(Collections.<String, List<String>>emptyMap());
        } else {
            artifacts = manager.findGenericArtifacts(Collections.<String, List<String>>singletonMap(
                    configuration.getArtifactNameAttribute(), Arrays.asList(criteria)));
        }

        if (artifacts != null) {
            String[] names = new String[artifacts.length];
            String[] namespaces = new String[artifacts.length];
            boolean[] canDelete = new boolean[artifacts.length];
            String[] lifecycleName = new String[artifacts.length];
            String[] lifecycleState = new String[artifacts.length];
            String[] paths = new String[artifacts.length];
            for (GenericArtifact artifact : artifacts) {
                int i = bean.getSize();
                paths[i] = artifact.getPath();
                names[i] = artifact.getQName().getLocalPart();
                namespaces[i] = artifact.getQName().getNamespaceURI();
                lifecycleName[i] = artifact.getLifecycleName();
                lifecycleState[i] = artifact.getLifecycleState();
                if (registry.getUserRealm() != null && registry.getUserName() != null) {
                    try {
                        canDelete[i] =
                                registry.getUserRealm().getAuthorizationManager().isUserAuthorized(
                                        registry.getUserName(),
                                        RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + paths[i],
                                        ActionConstants.DELETE);
                    } catch (UserStoreException ignored) {
                    }
                }
                bean.increment();
            }
            bean.setName(names);
            bean.setNamespace(namespaces);
            bean.setPath(paths);
            bean.setCanDelete(canDelete);
            bean.setLCName(lifecycleName);
            bean.setLCState(lifecycleState);
        }
        return bean;
    }

    private Map<String, List<String>> getFieldsList(GovernanceArtifact referenceArtifact,
                                              GovernanceArtifactConfiguration configuration) throws GovernanceException{
        if (referenceArtifact == null) {
            return Collections.<String, List<String>>emptyMap();
        }
        Map<String, List<String>> output = new HashMap<String, List<String>>();
        String[] keys = referenceArtifact.getAttributeKeys();
        boolean defaultNameMatched = false;
        boolean defaultNamespaceMatched = false;

        for (String key : keys) {
            if ("operation".equals(key)) {
                // this is a special case
                continue;
            }
            if (key.toLowerCase().contains("count")) {
                // we ignore the count.
                continue;
            }
            String[] referenceValues = referenceArtifact.getAttributes(key);
            if (referenceValues == null) {
                continue;
            }
            else {
                if(!defaultNameMatched &&
                        key.equals(configuration.getArtifactNameAttribute()) &&
                        GovernanceConstants.DEFAULT_SERVICE_NAME.
                                equalsIgnoreCase(referenceArtifact.getAttribute(
                                        configuration.getArtifactNameAttribute()))) {
                    defaultNameMatched = true;
                    continue;
                }

                if(!defaultNamespaceMatched &&
                        key.equals(configuration.getArtifactNamespaceAttribute()) &&
                        GovernanceConstants.DEFAULT_NAMESPACE.
                                equals(referenceArtifact.getAttribute(
                                        configuration.getArtifactNamespaceAttribute()))){
                    defaultNamespaceMatched = true;
                    continue;
                }
            }
            output.put(key, Arrays.asList(referenceValues));
        }
        return output;
    }

    private GovernanceArtifactConfiguration loadAndFindGovernanceArtifactConfiguration(
            String key, Registry registry) throws RegistryException {
        List<GovernanceArtifactConfiguration> governanceArtifactConfigurations =
                GovernanceUtils.findGovernanceArtifactConfigurations(registry);
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) getGovernanceUserRegistry(),
                governanceArtifactConfigurations);
        for (GovernanceArtifactConfiguration configuration : governanceArtifactConfigurations) {
            if (key.equals(configuration.getKey())) {
                return configuration;
            }
        }
        return null;
    }

    public ArtifactsBean listArtifacts(String key, String criteria) {
        RegistryUtils.recordStatistics(key, criteria);
        UserRegistry governanceRegistry = (UserRegistry) getGovernanceUserRegistry();
        ArtifactsBean bean = new ArtifactsBean();
        try {
            final GovernanceArtifactConfiguration configuration =
                    loadAndFindGovernanceArtifactConfiguration(key, getRootRegistry());
            GenericArtifactManager manager = new GenericArtifactManager(governanceRegistry,
                    configuration.getMediaType(), configuration.getArtifactNameAttribute(),
                    configuration.getArtifactNamespaceAttribute(),
                    configuration.getArtifactElementRoot(),
                    configuration.getArtifactElementNamespace(),
                    configuration.getPathExpression(),
                    configuration.getRelationshipDefinitions());
            final GenericArtifact referenceArtifact;
            if (criteria != null) {
            	XMLInputFactory factory = XMLInputFactory.newInstance();
            	factory.setProperty(XMLInputFactory.IS_COALESCING, true);
                XMLStreamReader reader = factory.createXMLStreamReader(
                        new StringReader(criteria));
                referenceArtifact = manager.newGovernanceArtifact(
                        new StAXOMBuilder(reader).getDocumentElement());
            } else {
                referenceArtifact = null;
            }

            bean.setNames(configuration.getNamesOnListUI());
            bean.setTypes(configuration.getTypesOnListUI());
            bean.setKeys(configuration.getKeysOnListUI());
            String[] expressions = configuration.getExpressionsOnListUI();
            String[] keys = configuration.getKeysOnListUI();
            List<GovernanceArtifact> artifacts = new LinkedList<GovernanceArtifact>();
            artifacts.addAll(Arrays.asList(manager.findGenericArtifacts(getFieldsList(referenceArtifact, configuration))));
            List<ArtifactBean> artifactBeans = new LinkedList<ArtifactBean>();
            for (GovernanceArtifact artifact : artifacts) {
                int kk=0;
                ArtifactBean artifactBean = new ArtifactBean();
                List<String> paths = new ArrayList<String>();
                List<String> values = new ArrayList<String>();
                String path =
                        RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                                ((GenericArtifactImpl) artifact).getArtifactPath();
                artifactBean.setPath(path);
                for(int i=0;i<expressions.length;i++){
                    if (expressions[i] != null) {
                        if (expressions[i].contains("@{storagePath}") &&
                                ((GenericArtifactImpl) artifact).getArtifactPath() != null) {
                            paths.add(RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                                    GovernanceUtils
                                            .getPathFromPathExpression(expressions[i], artifact,
                                                    ((GenericArtifactImpl) artifact).getArtifactPath()));
                        } else {
                            if("link".equals(bean.getTypes()[i])){
                                paths.add(GovernanceUtils
                                        .getPathFromPathExpression(expressions[i], artifact,
                                                configuration.getPathExpression()));
                            } else {
                                paths.add(RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                                        GovernanceUtils
                                                .getPathFromPathExpression(expressions[i], artifact,
                                                        configuration.getPathExpression()));
                            }
                        }
                    } else {
                        paths.add("");
                    }
                }
                artifactBean.setValuesB(paths.toArray(new String[paths.size()]));
                for (String keyForValue : keys) {
                    if (keyForValue != null) {
                        values.add(artifact.getAttribute(keyForValue));
                    } else {
                        values.add("");
                    }
                }
                artifactBean.setValuesA(values.toArray(new String[values.size()]));
                artifactBean.setCanDelete(
                        governanceRegistry.getUserRealm().getAuthorizationManager()
                                .isUserAuthorized(governanceRegistry.getUserName(),
                                        path, ActionConstants.DELETE));
                artifactBean.setLCName(((GenericArtifactImpl) artifact).getLcName());
                artifactBean.setLCState(((GenericArtifactImpl) artifact).getLcState());

                artifactBean.setCreatedDate(governanceRegistry.get(((GenericArtifactImpl) artifact).getArtifactPath()).getCreatedTime());
                artifactBean.setLastUpdatedDate(governanceRegistry.get(((GenericArtifactImpl) artifact).getArtifactPath()).getLastModified());
                artifactBean.setCreatedBy(governanceRegistry.get(((GenericArtifactImpl) artifact).getArtifactPath()).getAuthorUserName());
                artifactBean.setLastUpdatedBy(governanceRegistry.get(((GenericArtifactImpl) artifact).getArtifactPath()).getLastUpdaterUserName());

                artifactBeans.add(artifactBean);
            }
            bean.setArtifacts(artifactBeans.toArray(new ArtifactBean[artifactBeans.size()]));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("An error occurred while obtaining the list of artifacts.", e);
        }
        return bean;
    }


    public ArtifactsBean listArtifactsByLC(String key, String LCName, String LCState, String LCInOut,
                                           String LCStateInOut) {
        RegistryUtils.recordStatistics(key);
        //Construct Solr input
        if ("Any".equals(LCName)) {
            LCName = "*";
        }
        UserRegistry governanceRegistry = (UserRegistry) getGovernanceUserRegistry();
        ArtifactsBean bean = new ArtifactsBean();
        try {
            GovernanceArtifactConfiguration configuration =
                    loadAndFindGovernanceArtifactConfiguration(key, getRootRegistry());

            GenericArtifactManager manager = new GenericArtifactManager(governanceRegistry,
                                                                        configuration.getMediaType(), configuration.getArtifactNameAttribute(),
                                                                        configuration.getArtifactNamespaceAttribute(),
                                                                        configuration.getArtifactElementRoot(),
                                                                        configuration.getArtifactElementNamespace(),
                                                                        configuration.getPathExpression(),
                                                                        configuration.getRelationshipDefinitions());

            Map<String, List<String>> fields = new HashMap<String, List<String>>();
            if (LCName.length() > 0) {
                if (LCState.length() > 0) {
                        fields.put(LCStateInOut.equalsIgnoreCase("in") ? IndexingConstants.FIELD_LC_STATE : "-" +
                                IndexingConstants.FIELD_LC_STATE, Arrays.asList(LCState.toLowerCase()));
                    }
                    fields.put(LCInOut.equalsIgnoreCase("in") ? IndexingConstants.FIELD_LC_NAME : "-" +
                            IndexingConstants.FIELD_LC_NAME, Arrays.asList(LCName.toLowerCase()));
            }
            GenericArtifact[] artifacts = manager.findGenericArtifacts(fields);

            bean.setNames(configuration.getNamesOnListUI());
            bean.setTypes(configuration.getTypesOnListUI());
            bean.setKeys(configuration.getKeysOnListUI());
            String[] expressions = configuration.getExpressionsOnListUI();
            String[] keys = configuration.getKeysOnListUI();

            if (artifacts != null) {
                List<ArtifactBean> artifactBeans = new LinkedList<ArtifactBean>();
                for (GenericArtifact artifact : artifacts) {
                    ArtifactBean artifactBean = new ArtifactBean();
                    List<String> paths = new ArrayList<String>();
                    List<String> values = new ArrayList<String>();
                    String path =
                            RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + artifact.getPath();
                    artifactBean.setPath(path);
                    for (String expression : expressions) {
                        if (expression != null) {
                            if (expression.contains("@{storagePath}") && artifact.getPath() != null) {
                                paths.add(RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                                          GovernanceUtils
                                                  .getPathFromPathExpression(expression, artifact,
                                                                             artifact.getPath()));
                            } else {
                                paths.add(RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                                          GovernanceUtils
                                                  .getPathFromPathExpression(expression, artifact,
                                                                             configuration.getPathExpression()));
                            }
                        } else {
                            paths.add("");
                        }
                    }
                    artifactBean.setValuesB(paths.toArray(new String[paths.size()]));
                    for (String keyForValue : keys) {
                        if (keyForValue != null) {
                            values.add(artifact.getAttribute(keyForValue));
                        } else {
                            values.add("");
                        }
                    }
                    artifactBean.setValuesA(values.toArray(new String[values.size()]));
                    artifactBean.setCanDelete(
                            governanceRegistry.getUserRealm().getAuthorizationManager()
                                    .isUserAuthorized(governanceRegistry.getUserName(),
                                                      path, ActionConstants.DELETE));
                    artifactBean.setLCName(artifact.getLifecycleName());
                    artifactBean.setLCState(artifact.getLifecycleState());

                    artifactBean.setCreatedDate(governanceRegistry.get(((GenericArtifactImpl) artifact).getArtifactPath()).getCreatedTime());
                    artifactBean.setLastUpdatedDate(governanceRegistry.get(((GenericArtifactImpl) artifact).getArtifactPath()).getLastModified());
                    artifactBean.setCreatedBy(governanceRegistry.get(((GenericArtifactImpl) artifact).getArtifactPath()).getAuthorUserName());
                    artifactBean.setLastUpdatedBy(governanceRegistry.get(((GenericArtifactImpl) artifact).getArtifactPath()).getLastUpdaterUserName());

                    artifactBeans.add(artifactBean);
                }
                bean.setArtifacts(artifactBeans.toArray(new ArtifactBean[artifactBeans.size()]));

            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("An error occurred while obtaining the list of artifacts.", e);
        }
        return bean;
    }
    public String editArtifact(String path, String key, String info, String lifecycleAttribute)
            throws RegistryException {
        RegistryUtils.recordStatistics(path, key, info, lifecycleAttribute);
        Registry registry = getGovernanceUserRegistry();
        if (RegistryUtils.isRegistryReadOnly(registry.getRegistryContext())) {
            return null;
        }
        try {
        	XMLInputFactory factory = XMLInputFactory.newInstance();
        	factory.setProperty(XMLInputFactory.IS_COALESCING, true);
            XMLStreamReader reader = factory.createXMLStreamReader(new StringReader(info));

            GovernanceArtifactConfiguration configuration =
                    loadAndFindGovernanceArtifactConfiguration(key, getRootRegistry());

            GenericArtifactManager manager = new GenericArtifactManager(registry, key);
            GenericArtifact artifact = manager.newGovernanceArtifact(
                    new StAXOMBuilder(reader).getDocumentElement());
            String currentPath;
            if (path != null && path.length() > 0) {
                currentPath = path.substring(
                        RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH.length());
            } else {
                currentPath = GovernanceUtils.getPathFromPathExpression(
                        configuration.getPathExpression(), artifact);
            }
            if (registry.resourceExists(currentPath)) {
                GovernanceArtifact oldArtifact = GovernanceUtils
                        .retrieveGovernanceArtifactByPath(registry, currentPath);
                if (!(oldArtifact instanceof GovernanceArtifact)) {
                    String msg = "The updated path is occupied by a non-generic artifact. path: " +
                            currentPath + ".";
                    log.error(msg);
                    throw new Exception(msg);
                }
                artifact.setId(oldArtifact.getId());
                
                // want to save original content 
                artifact.setContent(info.getBytes());
                
                manager.updateGenericArtifact(artifact);
            } else {
                manager.addGenericArtifact(artifact);
            }
            if (lifecycleAttribute != null && !lifecycleAttribute.equals("null")) {
                String lifecycle = artifact.getAttribute(lifecycleAttribute);
                artifact.attachLifecycle(lifecycle);
            }
            return RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + artifact.getPath();
        } catch (Exception e) {
            String msg = "Unable to edit artifact. ";
            if (e instanceof RegistryException) {
                throw (RegistryException) e;
            } else if (e instanceof OMException) {
                msg += "Unexpected character found in input-field name.";
                log.error(msg, e);
                throw new RegistryException(msg, e);
            }
            throw new RegistryException(msg + (e.getCause() instanceof SQLException ? "" :
                    e.getCause().getMessage()), e);
        }
    }

    public String getArtifactContent(String path) throws RegistryException {
        Registry registry = getGovernanceUserRegistry();
        // resource path is created to make sure the version page doesn't give null values
        if (!registry.resourceExists(new ResourcePath(path).getPath())) {
            return null;
        }
        return RegistryUtils.decodeBytes((byte[]) registry.get(path).getContent());
    }

    public String getArtifactUIConfiguration(String key) throws RegistryException {
        try {
            Registry registry = getConfigSystemRegistry();
            return RegistryUtils.decodeBytes((byte[]) registry.get(GOVERNANCE_ARTIFACT_CONFIGURATION_PATH + key)
                    .getContent());
        } catch (Exception e) {
            log.error("An error occurred while obtaining configuration", e);
            return null;
        }
    }

    public boolean setArtifactUIConfiguration(String key, String update) throws RegistryException {
        Registry registry = getConfigSystemRegistry();
        if (RegistryUtils.isRegistryReadOnly(registry.getRegistryContext())) {
            return false;
        }
        try {
            Util.validateOMContent(Util.buildOMElement(update));

            String path = GOVERNANCE_ARTIFACT_CONFIGURATION_PATH + key;
            if(registry.resourceExists(path)) {
            Resource resource = registry.get(path);
            resource.setContent(update);
            registry.put(path, resource);
            }
            return true;
        } catch (Exception e) {
            log.error("An error occurred while saving configuration", e);
            return false;
        }
    }

    public boolean canChange(String path) throws Exception {
        UserRegistry registry = (UserRegistry) getRootRegistry();
        if (registry.getUserName() != null && registry.getUserRealm() != null) {
            if (registry.getUserRealm().getAuthorizationManager().isUserAuthorized(
                    registry.getUserName(), path, ActionConstants.PUT)) {
                Resource resource = registry.get(path);
                String property = resource.getProperty(
                        CommonConstants.RETENTION_WRITE_LOCKED_PROP_NAME);
                return property == null || !Boolean.parseBoolean(property) ||
                        registry.getUserName().equals(
                                resource.getProperty(CommonConstants.RETENTION_USERNAME_PROP_NAME));

            }
        }
        return false;
    }

    /* get available aspects */
    public String[] getAvailableAspects() throws Exception {
        return GovernanceUtils.getAvailableAspects();
    }


    public ArtifactsBean listArtifactsByName(String key, final String name) {
        RegistryUtils.recordStatistics(key);
        UserRegistry governanceRegistry = (UserRegistry) getGovernanceUserRegistry();
        ArtifactsBean bean = new ArtifactsBean();
        try {
            GovernanceArtifactConfiguration configuration =
                    loadAndFindGovernanceArtifactConfiguration(key, getRootRegistry());

            GenericArtifactManager manager = new GenericArtifactManager(governanceRegistry,
                    configuration.getMediaType(), configuration.getArtifactNameAttribute(),
                    configuration.getArtifactNamespaceAttribute(),
                    configuration.getArtifactElementRoot(),
                    configuration.getArtifactElementNamespace(),
                    configuration.getPathExpression(),
                    configuration.getRelationshipDefinitions());

            GenericArtifact[] artifacts = manager.findGenericArtifacts(Collections.<String, List<String>>singletonMap(
                    configuration.getArtifactNameAttribute(), Arrays.asList(name)));

            bean.setNames(configuration.getNamesOnListUI());
            bean.setTypes(configuration.getTypesOnListUI());
            String[] expressions = configuration.getExpressionsOnListUI();
            String[] keys = configuration.getKeysOnListUI();

            if (artifacts != null) {
                List<ArtifactBean> artifactBeans = new LinkedList<ArtifactBean>();
                for (GenericArtifact artifact : artifacts) {
                    ArtifactBean artifactBean = new ArtifactBean();
                    List<String> paths = new ArrayList<String>();
                    List<String> values = new ArrayList<String>();
                    String path =
                            RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + artifact.getPath();
                    artifactBean.setPath(path);
                    for (String expression : expressions) {
                        if (expression != null) {
                            if (expression.contains("@{storagePath}") && artifact.getPath() != null) {
                                paths.add(RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                                        GovernanceUtils
                                                .getPathFromPathExpression(expression, artifact,
                                                        artifact.getPath()));
                            } else {
                                paths.add(RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                                        GovernanceUtils
                                                .getPathFromPathExpression(expression, artifact,
                                                        configuration.getPathExpression()));
                            }
                        } else {
                            paths.add("");
                        }
                    }
                    artifactBean.setValuesB(paths.toArray(new String[paths.size()]));
                    for (String keyForValue : keys) {
                        if (keyForValue != null) {
                            values.add(artifact.getAttribute(keyForValue));
                        } else {
                            values.add("");
                        }
                    }
                    artifactBean.setValuesA(values.toArray(new String[values.size()]));
                    artifactBean.setCanDelete(
                            governanceRegistry.getUserRealm().getAuthorizationManager()
                                    .isUserAuthorized(governanceRegistry.getUserName(),
                                            path, ActionConstants.DELETE));
                    artifactBean.setLCName(artifact.getLifecycleName());
                    artifactBean.setLCState(artifact.getLifecycleState());

                    artifactBean.setCreatedDate(governanceRegistry.get(((GenericArtifactImpl) artifact).getArtifactPath()).getCreatedTime());
                    artifactBean.setLastUpdatedDate(governanceRegistry.get(((GenericArtifactImpl) artifact).getArtifactPath()).getLastModified());
                    artifactBean.setCreatedBy(governanceRegistry.get(((GenericArtifactImpl) artifact).getArtifactPath()).getAuthorUserName());
                    artifactBean.setLastUpdatedBy(governanceRegistry.get(((GenericArtifactImpl) artifact).getArtifactPath()).getLastUpdaterUserName());

                    artifactBeans.add(artifactBean);
                }
                bean.setArtifacts(artifactBeans.toArray(new ArtifactBean[artifactBeans.size()]));
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("An error occurred while obtaining the list of artifacts.", e);
        }
        return bean;
    }

    public boolean addRXTResource(String rxtConfig,String path) throws RegistryException {
        //TODO record stats
         boolean result  = GenericArtifactUtil.addRXTResource(path,rxtConfig, getGovernanceUserRegistry());
            setArtifactUIConfiguration(GenericArtifactUtil.getRXTKeyFromContent(rxtConfig),
                    GenericArtifactUtil.getArtifactUIContentFromConfig(rxtConfig));

        return result;
    }

    public String getRxtAbsPathFromRxtName(String rxtName) {
        return new StringBuilder(GovernanceConstants.RXT_CONFIGS_PATH).
                append("/").
                append(rxtName).
                append(".rxt").toString();
    }

    public String getArtifactViewRequestParams(String key) throws Exception {
        return GenericArtifactUtil.getArtifactViewRequestParams(
                getArtifactContent(new StringBuilder(GenericArtifactUtil.REL_RXT_BASE_PATH).
                append("/").
                append(key).
                append(".rxt").toString()));
    }


    /**
     * return all states of a given LC
     * @param LCName
     * @return
     */
    public String[] getAllLifeCycleState(String LCName){
        String[] LifeCycleStates = null;
        try {
            LifeCycleStates = CommonUtil.getAllLifeCycleStates(getRootRegistry(), LCName);
        } catch (org.wso2.carbon.registry.api.RegistryException e) {
            log.error("An error occurred while obtaining the list of states in "+LCName, e);
        }
        return LifeCycleStates;
    }


}
