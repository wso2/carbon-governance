package org.wso2.carbon.governance.platform.extensions.util;

/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaxen.JaxenException;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.common.AuthenticationException;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactFilter;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceConstants;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.platform.extensions.client.ConfigManagementClient;
import org.wso2.carbon.governance.platform.extensions.mediation.ArtifactBean;
import org.wso2.carbon.governance.platform.extensions.mediation.EndpointBean;
import org.wso2.carbon.governance.platform.extensions.mediation.ProxyBean;
import org.wso2.carbon.governance.platform.extensions.mediation.SequenceBean;
import org.wso2.carbon.ntask.core.TaskManager;
import org.wso2.carbon.ntask.core.service.TaskService;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.xml.namespace.QName;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.*;

public class MediationUtils {


    private static String userName = "admin";
    private static String password = "admin";
    private static String serverEpr = "https://localhost:9444/services/";
    private static String proxyArtifactKey = "proxy";
    private static String sequenceArtifactKey = "sequence";
    private static String endpointArtifactKey = "endpoint";

    private static TaskManager taskManager;
    private static final Log log = LogFactory.getLog(MediationUtils.class);
    private static RegistryService registryService;

    public static String getUserName() {
        return userName;
    }

    public static String getPassword() {
        return password;
    }

    public static String getServerEpr() {
        return serverEpr;
    }

    public static String getProxyArtifactKey() {
        return proxyArtifactKey;
    }

    public static String getSequenceArtifactKey() {
        return sequenceArtifactKey;
    }

    public static String getEndpointArtifactKey() {
        return endpointArtifactKey;
    }

    public static void setUserName(String userName) {
        MediationUtils.userName = userName;
    }

    public static void setPassword(String password) {
        MediationUtils.password = password;
    }

    public static void setServerEpr(String serverEpr) {
        MediationUtils.serverEpr = serverEpr;
    }

    public static void setProxyArtifactKey(String proxyArtifactKey) {
        MediationUtils.proxyArtifactKey = proxyArtifactKey;
    }

    public static void setSequenceArtifactKey(String sequenceArtifactKey) {
        MediationUtils.sequenceArtifactKey = sequenceArtifactKey;
    }

    public static void setEndpointArtifactKey(String endpointArtifactKey) {
        MediationUtils.endpointArtifactKey = endpointArtifactKey;
    }

    public static TaskManager getTaskManager() {
        return taskManager;
    }

    public static void setTaskManager(TaskManager taskManager) {
        MediationUtils.taskManager = taskManager;
    }

    public static ConfigurationContext getConfigurationContext() {
        return configurationContext;
    }

    public static void setConfigurationContext(ConfigurationContext configurationContext) {
        MediationUtils.configurationContext = configurationContext;
    }

    public static TaskService getTaskService() {
        return taskService;
    }

    public static void setTaskService(TaskService taskService) {
        MediationUtils.taskService = taskService;
    }

    public static RegistryService getRegistryService() {
        return registryService;
    }

    public static void setRegistryService(RegistryService registryService) {
        MediationUtils.registryService = registryService;
    }

    private static ConfigurationContext configurationContext;
    private static TaskService taskService;

    /**
     * this method used to initialized the ArtifactManager
     *
     * @param registry Registry
     * @param key      , key name of the key
     * @return GenericArtifactManager
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException
     *          if failed to initialized GenericArtifactManager
     */
    public static GenericArtifactManager getArtifactManager(Registry registry, String key)
            throws GovernanceException {
        GenericArtifactManager artifactManager;

        try {
            GovernanceUtils.loadGovernanceArtifacts((UserRegistry) registry);
            artifactManager = new GenericArtifactManager(registry, key);
        } catch (RegistryException e) {
            String msg = "Failed to initialize GenericArtifactManager";
            log.error(msg, e);
            throw new GovernanceException(msg, e);
        }
        return artifactManager;
    }

    public static UserRegistry getGovernanceRegistry() throws RegistryException {
        return registryService.getGovernanceSystemRegistry(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
    }

    public static String authenticate(ConfigurationContext ctx, String serviceURL, String username, String password)
            throws AxisFault, AuthenticationException {
        String cookie = null;
        String serviceEndpoint = serviceURL + "AuthenticationAdmin";

        AuthenticationAdminStub stub = new AuthenticationAdminStub(ctx, serviceEndpoint);
        ServiceClient client = stub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        try {
            boolean result = stub.login(username, password, new URL(serviceEndpoint).getHost());
            if (result) {
                cookie = (String) stub._getServiceClient().getServiceContext().getProperty(HTTPConstants.COOKIE_STRING);
            }
            return cookie;
        } catch (Exception e) {
            String msg = "Error occurred while logging in";
            throw new AuthenticationException(msg, e);
        }
    }

    public static String getSynapseConfig() throws AuthenticationException, RemoteException {
        String cookie = authenticate(getConfigurationContext(), getServerEpr(), getUserName(), getPassword());
        ConfigManagementClient configManagementClient = new ConfigManagementClient(cookie, getServerEpr(), getConfigurationContext());
        return configManagementClient.getSynapseConfig();
    }


    public static ProxyBean[] getProxies(OMElement synapseConfig) throws JaxenException {

        List<ProxyBean> proxyBeanList = new ArrayList<ProxyBean>();
        AXIOMXPath xpath = new AXIOMXPath("//ns:proxy");
        xpath.addNamespace("ns", MediationArtifactConstants.SYNAPSE_ROOT_NAMESPACE);
        List<OMElement> proxyList = xpath.selectNodes(synapseConfig);

        for (OMElement proxy : proxyList) {
            ProxyBean proxyBean = new ProxyBean();

            proxyBean.setName(proxy.getAttributeValue(new QName("name")));
            proxyBean.setTransports(proxy.getAttributeValue(new QName("transports")));
            proxyBean.setPinnedServers(proxy.getAttributeValue(new QName("pinnedServers")));
            proxyBean.setStartOnLoad(proxy.getAttributeValue(new QName("startOnLoad")));
            proxyBean.setTrace(proxy.getAttributeValue(new QName("trace")));
            proxyBean.setServiceGroup(proxy.getAttributeValue(new QName("serviceGroup")));

            //Fetching target attributes
            AXIOMXPath proxyXpath = new AXIOMXPath("//ns:target");
            proxyXpath.addNamespace("ns", MediationArtifactConstants.SYNAPSE_ROOT_NAMESPACE);
            OMElement target = (OMElement) proxyXpath.selectSingleNode(proxy);
            proxyBean.setInSequence(target.getAttributeValue(new QName("inSequence")));
            proxyBean.setOutSequence(target.getAttributeValue(new QName("outSequence")));
            proxyBean.setFaultSequence(target.getAttributeValue(new QName("faultSequence")));


            //Fetching target attributes

            Iterator inSeqIt = target.getChildrenWithLocalName("inSequence");

            if (inSeqIt.hasNext()) {
                OMElement inSeqEl = (OMElement) inSeqIt.next();
                Iterator sequenceIt = inSeqEl.getChildrenWithLocalName("sequence");
                while (sequenceIt.hasNext()) {
                    OMElement sequence = (OMElement) sequenceIt.next();
                    String sequenceKey = sequence.getAttributeValue(new QName("key"));
                    if (sequenceKey != null) {
                        proxyBean.getSequenceList().add(sequenceKey);
                    }
                }
            }

            Iterator outSeqIt = target.getChildrenWithLocalName("outSequence");
            if (outSeqIt.hasNext()) {
                OMElement outSeqEl = (OMElement) outSeqIt.next();
                Iterator sequenceIt = outSeqEl.getChildrenWithLocalName("sequence");
                while (sequenceIt.hasNext()) {
                    OMElement sequence = (OMElement) sequenceIt.next();
                    String sequenceKey = sequence.getAttributeValue(new QName("key"));
                    if (sequenceKey != null) {
                        proxyBean.getSequenceList().add(sequenceKey);
                    }
                }
            }

            Iterator endpointIt = proxy.getChildrenWithLocalName("send");
            while (endpointIt.hasNext()) {
                OMElement sequence = (OMElement) endpointIt.next();
                String sequenceKey = sequence.getAttributeValue(new QName("receive"));
                if (sequenceKey != null) {
                    proxyBean.getSequenceList().add(sequenceKey);
                }
                if (sequence.getChildrenWithLocalName("endpoint").hasNext()) {
                    OMElement epEl = (OMElement) sequence.getChildrenWithLocalName("endpoint").next();
                    String endpointKey = epEl.getAttributeValue(new QName("key"));
                    proxyBean.getEndpointList().add(endpointKey);
                }
            }


            //Fetching publish wsdl param
            proxyXpath = new AXIOMXPath("//ns:publishWSDL");
            proxyXpath.addNamespace("ns", MediationArtifactConstants.SYNAPSE_ROOT_NAMESPACE);
            OMElement publishWSDL = (OMElement) proxyXpath.selectSingleNode(proxy);
            String publishWsdl = null;
            if (publishWSDL != null && publishWSDL.getAttributeValue(new QName("uri")) != null) {
                proxyBean.setPublishWSDL(publishWSDL.getAttributeValue(new QName("uri")));
            } else if (publishWSDL != null && publishWSDL.getAttributeValue(new QName("key")) != null) {
                proxyBean.setPublishWSDL(publishWSDL.getAttributeValue(new QName("key")));
            }

            //Fetching endpoint
            proxyXpath = new AXIOMXPath("//ns:endpoint");
            proxyXpath.addNamespace("ns", MediationArtifactConstants.SYNAPSE_ROOT_NAMESPACE);
            OMElement endpoint = (OMElement) proxyXpath.selectSingleNode(proxy);
            endpoint.getAttributeValue(new QName("name"));
            String endPoint;
            if (endpoint != null && endpoint.getAttributeValue(new QName("name")) != null) {
                proxyBean.setEndPoint(endpoint.getAttributeValue(new QName("name")));
            } else if (endpoint != null && endpoint.getAttributeValue(new QName("key")) != null) {
                proxyBean.setEndPoint(endpoint.getAttributeValue(new QName("key")));
                proxyBean.getEndpointList().add(endpoint.getAttributeValue(new QName("key")));
            }

            //Fetching policies
            List<String> policyList = new ArrayList<String>();
            proxyXpath = new AXIOMXPath("//ns:policy");
            proxyXpath.addNamespace("ns", MediationArtifactConstants.SYNAPSE_ROOT_NAMESPACE);
            List<OMElement> policies = proxyXpath.selectNodes(proxy);
            for (OMElement policy : policies) {
                policyList.add(policy.getAttributeValue(new QName("key")));
            }
            proxyBean.setPolicies(policyList);

            //Fetching parameters
            List<String> parameterList = new ArrayList<String>();
            proxyXpath = new AXIOMXPath("//ns:parameter");
            proxyXpath.addNamespace("ns", MediationArtifactConstants.SYNAPSE_ROOT_NAMESPACE);
            List<OMElement> parameters = proxyXpath.selectNodes(proxy);
            for (OMElement param : parameters) {
                parameterList.add(param.getAttributeValue(new QName("name")));
            }
            proxyBean.setParameters(parameterList);
            proxyBeanList.add(proxyBean);
        }
        return proxyBeanList.toArray(new ProxyBean[proxyBeanList.size()]);
    }


    public static SequenceBean[] getSequences(OMElement synapseConfig) throws JaxenException {

        List<SequenceBean> sequenceBeanList = new ArrayList<SequenceBean>();
        AXIOMXPath xpath = new AXIOMXPath("//ns:sequence[not(@key)]");
        xpath.addNamespace("ns", MediationArtifactConstants.SYNAPSE_ROOT_NAMESPACE);
        List<OMElement> sequenceList = xpath.selectNodes(synapseConfig);

        for (OMElement sequence : sequenceList) {
            SequenceBean sequenceBean = new SequenceBean();

            sequenceBean.setName(sequence.getAttributeValue(new QName("name")));
            sequenceBean.setOnErrorSequence(sequence.getAttributeValue(new QName("onError")));
            sequenceBean.setTrace(sequence.getAttributeValue(new QName("trace")));

            List<String> mediatorList = new ArrayList<String>();
            Iterator it = sequence.getChildElements();
            while (it.hasNext()) {
                OMElement ele = (OMElement) it.next();
                mediatorList.add(ele.getLocalName());

                if ("sequence".equals(ele.getLocalName())) {
                    String sequenceKey = ele.getAttributeValue(new QName("key"));
                    if (sequenceKey != null) {
                        sequenceBean.getDependentSequenceList().add(sequenceKey);
                    }
                }

                if ("send".equals(ele.getLocalName())) {
                    String sequenceKey = ele.getAttributeValue(new QName("receive"));
                    if (sequenceKey != null) {
                        sequenceBean.getDependentSequenceList().add(sequenceKey);
                    }

                    if (sequence.getChildrenWithLocalName("endpoint").hasNext()) {
                        OMElement epEl = (OMElement) ele.getChildrenWithLocalName("endpoint").next();
                        String endpointKey = epEl.getAttributeValue(new QName("key"));
                        sequenceBean.getEndpointList().add(endpointKey);
                    }
                }

            }
            sequenceBean.setMediators(mediatorList);
            sequenceBeanList.add(sequenceBean);
        }
        return sequenceBeanList.toArray(new SequenceBean[sequenceBeanList.size()]);
    }


    public static EndpointBean[] getEndpoints(OMElement synapseConfig) throws JaxenException {

        List<EndpointBean> endpointBeanList = new ArrayList<EndpointBean>();
        AXIOMXPath xpath = new AXIOMXPath("ns:endpoint[not(@key)]");
        xpath.addNamespace("ns", MediationArtifactConstants.SYNAPSE_ROOT_NAMESPACE);
        List<OMElement> endpointList = xpath.selectNodes(synapseConfig);

        for (OMElement endpoint : endpointList) {
            EndpointBean endpointBean = new EndpointBean();
            endpointBean.setName(endpoint.getAttributeValue(new QName("name")));
            Iterator it = endpoint.getChildrenWithLocalName("address");
            String address = null;
            if (it.hasNext()) {
                OMElement addressEl = (OMElement) it.next();
                address = addressEl != null ? addressEl.getAttributeValue(new QName("uri")) : null;
            }
            endpointBean.setAddress(address);
            endpointBeanList.add(endpointBean);
        }
        return endpointBeanList.toArray(new EndpointBean[endpointBeanList.size()]);
    }

    public static void populateMediationArtifacts() throws RegistryException {
            PrivilegedCarbonContext.startTenantFlow();
        try {
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(
                    MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
            GovernanceUtils.loadGovernanceArtifacts(getGovernanceRegistry());

            populateArtifacts(getEndpoints(AXIOMUtil.stringToOM(getSynapseConfig())),
                    new GenericArtifactManager(getGovernanceRegistry(), MediationUtils.getEndpointArtifactKey()));
            populateArtifacts(getSequences(AXIOMUtil.stringToOM(getSynapseConfig())),
                    new GenericArtifactManager(getGovernanceRegistry(), MediationUtils.getSequenceArtifactKey()));
            populateArtifacts(getProxies(AXIOMUtil.stringToOM(getSynapseConfig())),
                    new GenericArtifactManager(getGovernanceRegistry(), MediationUtils.getProxyArtifactKey()));

            log.info("Finished Populating the Mediation Artifacts..!");
        } catch (Exception e) {
            throw new RegistryException("Failed to obtain proxy services from ESB node  " + MediationUtils.getServerEpr() + e.getMessage());
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    public static void populateArtifacts(ArtifactBean[] artifactBeans, GenericArtifactManager artifactManager) throws RegistryException {
        for (ArtifactBean artifactBean : artifactBeans) {
            GenericArtifact artifact = getArtifactFromRegistry(artifactBean.getName(), artifactManager);
            if (artifact == null) {
                addNewArtifact(artifactBean, artifactManager);
            } else {
                updateArtifact(artifact, artifactBean, artifactManager);
            }
        }
        performArtifactDeletion(artifactBeans, artifactManager);
    }

    private static void performArtifactDeletion(ArtifactBean[] artifactBeans, GenericArtifactManager artifactManager) throws GovernanceException {
        for (GenericArtifact genericArtifact : artifactManager.getAllGenericArtifacts()) {
            boolean artifactExists = false;
            for (ArtifactBean artifactBean : artifactBeans) {
                if (genericArtifact.getAttribute(MediationArtifactConstants.NAME_FIELD).equals(artifactBean.getName())) {
                    artifactExists = true;
                    break;
                }
            }
            if (!artifactExists) {
                artifactManager.removeGenericArtifact(genericArtifact.getId());
                log.info("Removing artifact " + genericArtifact.getAttribute(MediationArtifactConstants.NAME_FIELD));
            }
        }
    }

    private static void updateArtifact(GenericArtifact artifact, ArtifactBean artifactBean, GenericArtifactManager artifactManager) throws RegistryException {
        if (artifactBean instanceof ProxyBean) {
            updateProxy(artifact, (ProxyBean) artifactBean, artifactManager);
        } else if (artifactBean instanceof SequenceBean) {
            updateSequence(artifact, (SequenceBean) artifactBean, artifactManager);
        } else if (artifactBean instanceof EndpointBean) {
            updateEndpoint(artifact, (EndpointBean) artifactBean, artifactManager);
        }
    }

    private static void updateEndpoint(GenericArtifact artifact, EndpointBean endpointBean, GenericArtifactManager artifactManager) throws GovernanceException {
        artifact.setAttribute(MediationArtifactConstants.VERSION_FIELD, MediationArtifactConstants.ARTIFACT_COMMON_VERSION);
        artifact.setAttribute(MediationArtifactConstants.ADDRESS_FIELD, endpointBean.getAddress());
        artifactManager.updateGenericArtifact(artifact);
        log.info("Successfully updated the Endpoint artifact " + endpointBean.getName());

    }

    private static void updateSequence(GenericArtifact artifact, SequenceBean sequenceBean, GenericArtifactManager artifactManager) throws RegistryException {
        artifact.setAttribute(MediationArtifactConstants.VERSION_FIELD, MediationArtifactConstants.ARTIFACT_COMMON_VERSION);
        artifact.setAttribute(MediationArtifactConstants.ON_ERROR_SEQ_FIELD, sequenceBean.getOnErrorSequence());
        artifact.setAttribute(MediationArtifactConstants.TRACE_FIELD, sequenceBean.getTrace());
        artifactManager.updateGenericArtifact(artifact);
        addAssociationsForSequence(sequenceBean, artifact);
        log.info("Successfully updated the Sequence artifact " + sequenceBean.getName());
    }

    private static void updateProxy(GenericArtifact artifact, ProxyBean proxyBean, GenericArtifactManager artifactManager) throws RegistryException {
        artifact.setAttribute(MediationArtifactConstants.VERSION_FIELD, MediationArtifactConstants.ARTIFACT_COMMON_VERSION);
        artifact.setAttribute(MediationArtifactConstants.TRANSPORT_FIELD, proxyBean.getTransports());
        artifact.setAttribute(MediationArtifactConstants.START_ON_LOAD_FIELD, proxyBean.getStartOnLoad());
        artifact.setAttribute(MediationArtifactConstants.TRACE_FIELD, proxyBean.getTrace());
        artifact.setAttribute(MediationArtifactConstants.PINNED_SERVERS_FIELD, proxyBean.getPinnedServers());
        artifact.setAttribute(MediationArtifactConstants.SERVICE_GROUP_FIELD, proxyBean.getServiceGroup());
        artifact.setAttribute(MediationArtifactConstants.IN_SEQUENCE_FIELD, proxyBean.getInSequence());
        artifact.setAttribute(MediationArtifactConstants.OUT_SEQUENCE_FIELD, proxyBean.getOutSequence());
        artifact.setAttribute(MediationArtifactConstants.FAULT_SEQUENCE_FIELD, proxyBean.getFaultSequence());
        artifact.setAttribute(MediationArtifactConstants.ENDPOINT_FIELD, proxyBean.getEndPoint());
        artifact.setAttribute(MediationArtifactConstants.PUBLISH_WSDL_FIELD, proxyBean.getPublishWSDL());
        artifact.setAttribute(MediationArtifactConstants.ENABLE_ADDRESSING_FIELD, proxyBean.getEnableAddressing());
        artifact.setAttribute(MediationArtifactConstants.ENABLE_SEC_FIELD, proxyBean.getEnableSecurity());
        artifact.setAttribute(MediationArtifactConstants.ENABLE_RM_FIELD, proxyBean.getEnableRM());
        artifactManager.updateGenericArtifact(artifact);
        addAssociationsForProxy(proxyBean, artifact);
        log.info("Successfully updated the Proxy artifact " + proxyBean.getName());

    }


    private static void addNewArtifact(ArtifactBean artifactBean, GenericArtifactManager artifactManager) throws RegistryException {
        if (artifactBean instanceof ProxyBean) {
            addNewProxy((ProxyBean) artifactBean, artifactManager);
        } else if (artifactBean instanceof SequenceBean) {
            addNewSequence((SequenceBean) artifactBean, artifactManager);
        } else if (artifactBean instanceof EndpointBean) {
            addNewEndpoint((EndpointBean) artifactBean, artifactManager);
        }


    }


    private static void addNewEndpoint(EndpointBean endpointBean, GenericArtifactManager artifactManager) throws GovernanceException {
        GenericArtifact artifact = artifactManager.newGovernanceArtifact(new QName(MediationArtifactConstants.ARTIFACT_COMMON_NAMESPACE, endpointBean.getName()));
        artifact.addAttribute(MediationArtifactConstants.NAME_FIELD, endpointBean.getName());
        artifact.addAttribute(MediationArtifactConstants.VERSION_FIELD, MediationArtifactConstants.ARTIFACT_COMMON_VERSION);
        artifact.addAttribute(MediationArtifactConstants.ADDRESS_FIELD, endpointBean.getAddress());
        artifactManager.addGenericArtifact(artifact);
        log.info("Successfully created a new Endpoint artifact " + endpointBean.getName());
    }


    private static void addNewSequence(SequenceBean sequenceBean, GenericArtifactManager artifactManager) throws RegistryException {
        GenericArtifact artifact = artifactManager.newGovernanceArtifact(new QName(MediationArtifactConstants.ARTIFACT_COMMON_NAMESPACE, sequenceBean.getName()));
        artifact.addAttribute(MediationArtifactConstants.NAME_FIELD, sequenceBean.getName());
        artifact.addAttribute(MediationArtifactConstants.VERSION_FIELD, MediationArtifactConstants.ARTIFACT_COMMON_VERSION);
        artifact.addAttribute(MediationArtifactConstants.ON_ERROR_SEQ_FIELD, sequenceBean.getOnErrorSequence());
        artifact.addAttribute(MediationArtifactConstants.TRACE_FIELD, sequenceBean.getTrace());
        artifactManager.addGenericArtifact(artifact);
        addAssociationsForSequence(sequenceBean, artifact);
        log.info("Successfully created a new Sequence artifact " + sequenceBean.getName());
    }

    private static void addNewProxy(ProxyBean proxyBean, GenericArtifactManager artifactManager) throws RegistryException {
        GenericArtifact artifact = artifactManager.newGovernanceArtifact(new QName(MediationArtifactConstants.ARTIFACT_COMMON_NAMESPACE, proxyBean.getName()));
        artifact.addAttribute(MediationArtifactConstants.NAME_FIELD, proxyBean.getName());
        artifact.addAttribute(MediationArtifactConstants.VERSION_FIELD, MediationArtifactConstants.ARTIFACT_COMMON_VERSION);
        artifact.addAttribute(MediationArtifactConstants.TRANSPORT_FIELD, proxyBean.getTransports());
        artifact.addAttribute(MediationArtifactConstants.START_ON_LOAD_FIELD, proxyBean.getStartOnLoad());
        artifact.addAttribute(MediationArtifactConstants.TRACE_FIELD, proxyBean.getTrace());
        artifact.addAttribute(MediationArtifactConstants.PINNED_SERVERS_FIELD, proxyBean.getPinnedServers());
        artifact.addAttribute(MediationArtifactConstants.SERVICE_GROUP_FIELD, proxyBean.getServiceGroup());
        artifact.addAttribute(MediationArtifactConstants.IN_SEQUENCE_FIELD, proxyBean.getInSequence());
        artifact.addAttribute(MediationArtifactConstants.OUT_SEQUENCE_FIELD, proxyBean.getOutSequence());
        artifact.addAttribute(MediationArtifactConstants.FAULT_SEQUENCE_FIELD, proxyBean.getFaultSequence());
        artifact.addAttribute(MediationArtifactConstants.ENDPOINT_FIELD, proxyBean.getEndPoint());
        artifact.addAttribute(MediationArtifactConstants.PUBLISH_WSDL_FIELD, proxyBean.getPublishWSDL());
        artifact.addAttribute(MediationArtifactConstants.ENABLE_ADDRESSING_FIELD, proxyBean.getEnableAddressing());
        artifact.addAttribute(MediationArtifactConstants.ENABLE_SEC_FIELD, proxyBean.getEnableSecurity());
        artifact.addAttribute(MediationArtifactConstants.ENABLE_RM_FIELD, proxyBean.getEnableRM());
        artifactManager.addGenericArtifact(artifact);
        addAssociationsForProxy(proxyBean, artifact);
        log.info("Successfully created a new Proxy artifact " + proxyBean.getName());
    }

    private static void addAssociationsForProxy(ProxyBean proxyBean, GenericArtifact artifact) throws RegistryException {

        cleanupDependencies(proxyBean.getEndpointList().toArray(new String[proxyBean.getEndpointList().size()]), artifact,MediationUtils.getEndpointArtifactKey());
        cleanupDependencies(proxyBean.getSequenceList().toArray(new String[proxyBean.getSequenceList().size()]), artifact,MediationUtils.getSequenceArtifactKey());

        for (String endpoint : proxyBean.getEndpointList()) {
            GenericArtifact ep = getArtifactFromRegistry(endpoint, MediationUtils.getEndpointArtifactKey());
            if (ep != null) {
                addArtifactAssociation(artifact.getPath(), ep.getPath(), GovernanceConstants.DEPENDS);
                addArtifactAssociation(ep.getPath(), artifact.getPath(), GovernanceConstants.USED_BY);

            }
        }

        for (String sequence : proxyBean.getSequenceList()) {
            GenericArtifact seq = getArtifactFromRegistry(sequence, MediationUtils.getSequenceArtifactKey());
            if (seq != null) {
                addArtifactAssociation(artifact.getPath(), seq.getPath(), GovernanceConstants.DEPENDS);
                addArtifactAssociation(seq.getPath(), artifact.getPath(), GovernanceConstants.USED_BY);
            }
        }

    }

    private static void cleanupDependencies(String[] dependencyList, GenericArtifact proxyArtifact,String key) throws RegistryException {
        String path = RegistryUtils.getAbsolutePathToOriginal(proxyArtifact.getPath(), RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH);

        Association[] associations = getRootRegistry().getAssociations(path, GovernanceConstants.DEPENDS);
        for (Association association : associations) {
            boolean dependencyExists = false;
            boolean hasAssociations = false;

            for (String dependency : dependencyList) {
                if (association.getSourcePath().equals(path)) {
                    hasAssociations = true;
                    GenericArtifact artifact = getArtifactFromRegistry(dependency, key);
                    if (dependency != null && artifact.getPath().equals(association.getDestinationPath())) {
                        dependencyExists = true;
                        break;
                    }
                }

            }
             if (hasAssociations && !dependencyExists) {
                    getRootRegistry().removeAssociation(association.getSourcePath(),
                            association.getDestinationPath(), GovernanceConstants.DEPENDS);
             }
        }
    }

    private static void addArtifactAssociation(String scrPath, String destPath, String type) throws RegistryException {
        String absSrcPath = RegistryUtils.getAbsolutePathToOriginal(scrPath, RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH);
        String absDestPath = RegistryUtils.getAbsolutePathToOriginal(destPath, RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH);
        getRootRegistry().addAssociation(absSrcPath, absDestPath, type);
    }

    private static Registry getRootRegistry() throws RegistryException {
        return registryService.getRegistry(PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername(), PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
    }

    private static void addAssociationsForSequence(SequenceBean sequenceBean, GenericArtifact artifact) throws RegistryException {

        cleanupDependencies(sequenceBean.getDependentSequenceList().toArray(new String[sequenceBean.getDependentSequenceList().size()]), artifact,MediationUtils.getSequenceArtifactKey());
        cleanupDependencies(sequenceBean.getEndpointList().toArray(new String[sequenceBean.getEndpointList().size()]), artifact,MediationUtils.getEndpointArtifactKey());

        for (String endpoint : sequenceBean.getEndpointList()) {
            GenericArtifact ep = getArtifactFromRegistry(endpoint, MediationUtils.getEndpointArtifactKey());
            if (ep != null) {
                addArtifactAssociation(artifact.getPath(), ep.getPath(), GovernanceConstants.DEPENDS);
                addArtifactAssociation(ep.getPath(), artifact.getPath(), GovernanceConstants.USED_BY);
            }
        }

        for (String sequence : sequenceBean.getDependentSequenceList()) {
            GenericArtifact seq = getArtifactFromRegistry(sequence, MediationUtils.getSequenceArtifactKey());
            if (seq != null) {
                addArtifactAssociation(artifact.getPath(), seq.getPath(), GovernanceConstants.DEPENDS);
                addArtifactAssociation(seq.getPath(), artifact.getPath(), GovernanceConstants.USED_BY);
            }
        }

    }


    private static GenericArtifact getArtifactFromRegistry(final String name, String key) throws RegistryException {
        GenericArtifactManager artifactManager = new GenericArtifactManager(getGovernanceRegistry(), key);
        GenericArtifact[] artifacts = artifactManager.findGenericArtifacts(new GenericArtifactFilter() {
            public boolean matches(GenericArtifact genericArtifact) throws GovernanceException {
                return genericArtifact.getAttribute(MediationArtifactConstants.NAME_FIELD).equals(name);
            }
        });
        if (artifacts.length != 0) {
            return artifacts[0];
        }
        return null;
    }


    private static GenericArtifact getArtifactFromRegistry(final String name, GenericArtifactManager artifactManager) throws RegistryException {
        GenericArtifact[] artifacts = artifactManager.findGenericArtifacts(new GenericArtifactFilter() {
            public boolean matches(GenericArtifact genericArtifact) throws GovernanceException {
                return genericArtifact.getAttribute(MediationArtifactConstants.NAME_FIELD).equals(name) &&
                        genericArtifact.getAttribute(MediationArtifactConstants.VERSION_FIELD).equals(
                                MediationArtifactConstants.ARTIFACT_COMMON_VERSION);
            }
        });
        if (artifacts.length != 0) {
            return artifacts[0];
        }
        return null;
    }

}
