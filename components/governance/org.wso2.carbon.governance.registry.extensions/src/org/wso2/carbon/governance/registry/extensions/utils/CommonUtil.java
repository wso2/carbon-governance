/*
 * Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.governance.registry.extensions.utils;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.util.GovernanceArtifactConfiguration;
import org.wso2.carbon.governance.api.util.GovernanceConstants;
import org.wso2.carbon.governance.common.utils.GovernanceUtils;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.CurrentSession;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.extensions.services.RXTStoragePathService;
import org.wso2.carbon.registry.extensions.services.RXTStoragePathServiceImpl;
import org.wso2.carbon.registry.extensions.utils.CommonConstants;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.FileUtil;
import org.xml.sax.SAXException;

import javax.cache.Cache;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.util.*;

import static org.wso2.carbon.governance.api.util.GovernanceUtils.getGovernanceArtifactConfiguration;
import static org.wso2.carbon.governance.api.util.GovernanceUtils.getRXTConfigCache;

public class CommonUtil {

    private static final Log log = LogFactory.getLog(CommonUtil.class);

    private final static Map<String, HashMap<String, String>>
            associationConfigMap = new HashMap<>();
    private final static Map<String, HashMap<String, String>>
            reverseAssociationConfigMap = new HashMap<>();
    private static final Map<String, HashMap<String, String>> associationTypeIconConfigMap = new HashMap();
    public static final String REVERSE_ASSOCIATION = "reverseAssociation";
    public static final String TYPE = "type";
    public static final String ASSOCIATION_CONFIG = "AssociationConfig";
    public static final String ICON_CLASS = "iconClass";

    private static int dependencyGraphMaxDepth = -1;

	private static RXTStoragePathService rxtSPService;

	public static RXTStoragePathService getRxtStoragePathService() {
		return rxtSPService;
	}

	public static void setRxtStoragePathService(RXTStoragePathService rxtSPService) {
		CommonUtil.rxtSPService = rxtSPService;
	}

    public static String[] getAllLifeCycleStates(Registry registry, String lifeCycleName) throws RegistryException {
        boolean isLiteral = true;
        List<String> stateList = new ArrayList<String>();

        String[] allAspects = registry.getAvailableAspects();

//        Check if the given LC name is there in the registry. If not will return null
        if (!Arrays.asList(allAspects).contains(lifeCycleName)) {
            String msg = "There are no lifecycles with the given name";
            throw new RegistryException(msg);
        }

//        Here we are getting the LC configuration from the default LC configuration path.
//        If this fails we use the registry.xml to see whether the LC name is there and will get the resource.
        Resource resource=null;

//        reading the registry.xml to see of the LC configuration is there
        if (!registry.resourceExists(RegistryConstants.CONFIG_REGISTRY_BASE_PATH+
                            RegistryConstants.LIFECYCLE_CONFIGURATION_PATH + lifeCycleName)) {

            /*
            * Getting the registry.xml from the client side is not possible.
            * Therefore if there are no resource in the default life cycle resource store path we consider this life cycle
            * as a static life cycle which has been configured using the registry.xml
            * Since we are unable to read the registry.xml from a client program we throw an exception here.
            * */

            String msg = "The given lifecycle configuration is an static configuration. Unable to read the registry.xml";
            throw new RegistryException(msg);
/*
            try {
                FileInputStream inputStream = new FileInputStream(getConfigFile());
                StAXOMBuilder builder = new StAXOMBuilder(inputStream);
                OMElement configElement = builder.getDocumentElement();

                Iterator aspectElement = configElement.getChildrenWithName(new QName("aspect"));
                while (aspectElement.hasNext()) {
                    OMElement next = (OMElement) aspectElement.next();
                    String name = next.getAttributeValue(new QName("name"));

                    if(name.equals(lifeCycleName)){
                        OMElement element = next.getFirstElement();
                        resource = registry.get(element.getText());
                        isLiteral = false;
                        break;
                    }
                }

            } catch (FileNotFoundException e) {
                throw new RegistryException("", e);
            } catch (XMLStreamException e) {
                throw new RegistryException("", e);
            }
*/
        }else{
            resource = registry.get(RegistryConstants.CONFIG_REGISTRY_BASE_PATH+
                            RegistryConstants.LIFECYCLE_CONFIGURATION_PATH + lifeCycleName);

        }

//        here we get the resource content and build a OMElement from it
        try {
            String xmlContent = RegistryUtils.decodeBytes((byte[])resource.getContent());
            OMElement configurationElement =  AXIOMUtil.stringToOM(xmlContent);

//            if the config type is literal we take the lifecycle element from it
            if(isLiteral){
                OMElement typeElement = configurationElement.getFirstElement();
                configurationElement = typeElement.getFirstElement();
            }

//            this is to see whether this is the new configuration or the old one
            Iterator statesElement = configurationElement.getChildrenWithName(new QName("scxml"));

//            if it is the new configuration we use the scxml parser to get all the elements
            if(statesElement.hasNext()){
                while (statesElement.hasNext()) {
                    OMElement scxmlElement = (OMElement) statesElement.next();
                    Iterator stateElements = scxmlElement.getChildrenWithName(new QName("state"));
                    while (stateElements.hasNext()) {
                        OMElement next = (OMElement) stateElements.next();
                        stateList.add(next.getAttributeValue(new QName("id")));
                    }
                }
            }
            else{
                Iterator states = configurationElement.getChildElements();
                while (states.hasNext()) {
                    OMElement next = (OMElement) states.next();
                    stateList.add(next.getAttributeValue(new QName("name")));
                }
            }


        } catch (XMLStreamException e) {
            throw new RegistryException("", e);
        }

        String[] retArray =  new String[stateList.size()];
        return stateList.toArray(retArray);

    }

    public static void addRxtConfigs(Registry systemRegistry, int tenantId) throws RegistryException {
        loadAssociationConfig();
        Cache<String,Boolean> rxtConfigCache = getRXTConfigCache(GovernanceConstants.RXT_CONFIG_CACHE_ID);
        String rxtDir = CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator +
                "resources" + File.separator + "rxts";
        File file = new File(rxtDir);
        if(!file.exists()){
            return;
        }
        //create a FilenameFilter
        FilenameFilter filenameFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                //if the file extension is .rxt return true, else false
                return name.endsWith(".rxt");
            }
        };
        String[] rxtFilePaths = file.list(filenameFilter);
        if(rxtFilePaths.length == 0){
            return;
        }
        for (String rxtPath : rxtFilePaths) {
            String resourcePath = RegistryConstants.GOVERNANCE_COMPONENT_PATH +
                    RegistryConstants.PATH_SEPARATOR + "types" + RegistryConstants.PATH_SEPARATOR + rxtPath;
            int currentTenantId = CurrentSession.getTenantId();
            CurrentSession.setTenantId(tenantId);
            RegistryContext registryContext = systemRegistry.getRegistryContext();
            String absolutePath = RegistryUtils.getAbsolutePath(registryContext, resourcePath);
            if (registryContext.isSystemResourcePathRegistered(absolutePath)) {
                CurrentSession.setTenantId(currentTenantId);
                continue;
            } else {
                registryContext.registerSystemResourcePath(absolutePath);
                CurrentSession.setTenantId(currentTenantId);
            }
            try {
                String rxtConfigRelativePath = RegistryUtils.getRelativePathToOriginal(GovernanceConstants.RXT_CONFIGS_PATH,
                        RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH);

                if (!systemRegistry.resourceExists(rxtConfigRelativePath)) {
                    Collection collection = systemRegistry.newCollection();
                    systemRegistry.put(rxtConfigRelativePath, collection);
                }

                Resource rxtCollection = systemRegistry.get(rxtConfigRelativePath);
                String rxtName = resourcePath.substring(resourcePath.lastIndexOf("/") + 1).split("\\.")[0];
                if (!systemRegistry.resourceExists(resourcePath)) {
                    String propertyName = "registry." + rxtName;
                    if (rxtCollection.getProperty(propertyName) == null) {
                        rxtCollection.setProperty(propertyName, "true");
                        String rxt = FileUtil.readFileToString(rxtDir + File.separator + rxtPath);
                        Resource resource = systemRegistry.newResource();
                        resource.setContent(rxt.getBytes());
                        resource.setMediaType(CommonConstants.RXT_MEDIA_TYPE);
                        systemRegistry.put(resourcePath, resource);
                        rxtConfigCache.put(resourcePath,true);
	                    GovernanceArtifactConfiguration configuration = getGovernanceArtifactConfiguration(rxt);
	                    String mediaType = configuration.getMediaType();
	                    String storagePath = configuration.getPathExpression();
	                    addStoragePath(mediaType, storagePath);
                    }
                } else {
	                Resource resource = systemRegistry.get(resourcePath);
	                Object content = resource.getContent();
	                String elementString;
	                if (content instanceof String) {
		                elementString = (String) content;
	                } else {
		                elementString = RegistryUtils.decodeBytes((byte[]) content);
	                }
	                GovernanceArtifactConfiguration configuration = getGovernanceArtifactConfiguration(elementString);
	                String mediaType = configuration.getMediaType();
	                String storagePath = configuration.getPathExpression();
	                addStoragePath(mediaType, storagePath);
                    if (log.isDebugEnabled()) {
                        log.debug("RXT " + rxtName + " already exists.");
                    }

                }

            } catch (IOException e) {
                String msg = "Failed to read rxt files";
                throw new RegistryException(msg, e);
            } catch (RegistryException e) {
                String msg = "Failed to add rxt to registry ";
                throw new RegistryException(msg, e);
            }
        }
    }

    /**
     * This method reads the Association Config xml and populates the association and reverse association map.
     */
    public static void loadAssociationConfig() {
        String associationConfigFile =
                CarbonUtils.getCarbonConfigDirPath() + File.separator + GovernanceUtils.GOVERNANCE_CONFIG_FILE;
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new File(associationConfigFile));
            doc.getDocumentElement().normalize();
            NodeList associationConfigNode = doc.getElementsByTagName(ASSOCIATION_CONFIG);
            NodeList nodeList = associationConfigNode.item(0).getChildNodes();

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node association = nodeList.item(i);
                if (association.getNodeType() == Node.ELEMENT_NODE) {
                    HashMap<String, String> associationMap = new HashMap<>();
                    HashMap<String, String> reverseAssociationMap = new HashMap<>();
                    HashMap iconClassMap = new HashMap();
                    NodeList childNodeList = association.getChildNodes();

                    if (childNodeList != null) {
                        for (int j = 0; j < childNodeList.getLength(); j++) {
                            Node types = childNodeList.item(j);
                            if (types.getNodeType() == Node.ELEMENT_NODE) {
                                associationMap.put(types.getNodeName(), types.getFirstChild().getNodeValue());
                                if (((Element) types).hasAttribute(REVERSE_ASSOCIATION)) {
                                    reverseAssociationMap.put(types.getNodeName(),
                                            ((Element) types).getAttribute(REVERSE_ASSOCIATION));
                                }

                                if(((Element)types).hasAttribute(ICON_CLASS)) {
                                    iconClassMap.put(types.getNodeName(), ((Element)types).getAttribute(ICON_CLASS));
                                }
                            }
                        }
                    }
                    associationConfigMap.put(((Element) association).getAttribute(TYPE), associationMap);
                    reverseAssociationConfigMap.put(((Element) association).getAttribute(TYPE), reverseAssociationMap);
                    associationTypeIconConfigMap.put(((Element)association).getAttribute(TYPE), iconClassMap);
                }
            }
        } catch (FileNotFoundException e) {
            log.error("Failed to find the governance.xml", e);
        } catch (ParserConfigurationException | SAXException e) {
            log.error("Failed to parse the governance.xml", e);
        } catch (IOException e) {
            log.error("Error while reading the governance.xml", e);
        }
    }

    /**
     * This method is used by greg-publisher-api.js to read the association configuration map.
     * @param shortName     short name of the artifact type.
     * @return              map containing association type as key and
     */
    public static HashMap<String, String> getAssociationConfig(String shortName){
        if(associationConfigMap.size() == 0){
            log.warn("Failed to find association mappings");
            return null;
        }
        if(associationConfigMap.containsKey(shortName)){
            return associationConfigMap.get(shortName);
        }else{
            return null;
        }
    }

    /**
     * This method is used by greg-publisher-api.js to read the reverse association type for a given short name and
     * association type.
     * @param shortName         short name of the artifact type.
     * @param associationType   association type from source asset to destination asset.
     * @return                  reverse association type from destination asset to source asset.
     */
    public static String getReverseAssociationType(String shortName, String associationType){
        if(reverseAssociationConfigMap.size() == 0){
            log.warn("Failed to find reverse association mappings");
            return null;
        }
        if(reverseAssociationConfigMap.containsKey(shortName) && reverseAssociationConfigMap.get(shortName)
                .containsKey(associationType)){
            return reverseAssociationConfigMap.get(shortName).get(associationType);
        }else{
            return null;
        }
    }

    /**
     * This method is used by greg-publisher-api.js to read the reverse association type for a given short name and
     * association type when removing the bi-directional association. If the association removal is done from the
     * destination asset, then this method will determine association type from source asset to destination asset.
     * @param shortName         short name of the artifact type.
     * @param associationType   association type from source asset to destination asset.
     * @return                  reverse association type from source asset to destination asset.
     */
    public static String getAssociationTypeForRemoveOperation (String shortName, String
            associationType){
        if(reverseAssociationConfigMap.size() == 0){
            log.warn("Failed to find association mappings");
            return null;
        }
        String reverseAssociationType = null;
        if(reverseAssociationConfigMap.containsKey(shortName)){
            for (Map.Entry<String, String> entry : reverseAssociationConfigMap.get(shortName).entrySet()){
                if(associationType.equals(entry.getValue())) {
                    reverseAssociationType = entry.getKey();
                    break;
                }
            }
            return reverseAssociationType;
        }else{
            return null;
        }
    }

    public static HashMap<String, String> getAssociationWithIcons(String shortName) {
        if (associationTypeIconConfigMap.size() == 0) {
            log.warn("Failed to find association mappings");
            return null;
        }
        if (associationTypeIconConfigMap.containsKey(shortName)) {
            return associationTypeIconConfigMap.get(shortName);
        } else {
            return null;
        }
    }

    public static void loadDependencyGraphMaxDepthConfig() {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(getConfigFile());
            StAXOMBuilder builder = new StAXOMBuilder(
                    CarbonUtils.replaceSystemVariablesInXml(fileInputStream));
            OMElement configElement = builder.getDocumentElement();
            OMElement dependencyGraphMaxDepthConfig = configElement.getFirstChildWithName(
                    new QName("dependencyGraphMaxDepth"));
            if (dependencyGraphMaxDepthConfig != null) {
                dependencyGraphMaxDepth = Integer.parseInt(dependencyGraphMaxDepthConfig.getText());
            }
        } catch (FileNotFoundException | GovernanceException e) {
            log.error("Failed to find the registry.xml", e);
        } catch (CarbonException e) {
            log.error("Could not replace system variables in registry.xml", e);
        } catch (XMLStreamException e) {
            log.error("could not build registry.xml OM", e);
        }
    }

    // Get registry.xml instance.
    private static File getConfigFile() throws GovernanceException {
        String configPath = CarbonUtils.getRegistryXMLPath();
        if (configPath != null) {
            File registryXML = new File(configPath);
            if (!registryXML.exists()) {
                String msg = "Registry configuration file (registry.xml) file does " +
                        "not exist in the path " + configPath;
                throw new GovernanceException(msg);
            }
            return registryXML;
        } else {
            String msg = "Cannot find registry.xml";
            throw new GovernanceException(msg);
        }
    }

    public static int getDependencyGraphMaxDepth(){
        return dependencyGraphMaxDepth;
    }

    public static void addStoragePath(String mediaType, String storagePath) {
        RXTStoragePathServiceImpl service = (RXTStoragePathServiceImpl) getRxtStoragePathService();
        service.addStoragePath(mediaType, storagePath);
    }

    public static void removeStoragePath(String mediaType) {
        RXTStoragePathServiceImpl service = (RXTStoragePathServiceImpl) getRxtStoragePathService();
        service.removeStoragePath(mediaType);
    }

    // handling the possibility that handlers are not called within each other.
    private static ThreadLocal<Boolean> clearMetaDataInProgress = new ThreadLocal<Boolean>() {
        protected Boolean initialValue() {
            return false;
        }
    };

    public static boolean isMetaDataClearLockAvailable() {
        return !clearMetaDataInProgress.get();
    }

    public static void acquireMetaDataClearLock() {
        clearMetaDataInProgress.set(true);
    }

    public static void releaseMetaDataClearLock() {
        clearMetaDataInProgress.set(false);
    }
}
