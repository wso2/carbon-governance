package org.wso2.carbon.governance.registry.extensions.discoveryagents;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.DetachedGenericArtifact;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.registry.extensions.internal.GovernanceRegistryExtensionsDataHolder;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public abstract class DiscoveryAgentExecutorSupport {

    public static final String RESOURCE_SOURCE_PROPERTY = "resource.source";
    public static final String SOURCE_GREG_DISCOVERY = "greg-discovery";
    public static final String RESOURCE_ORIGIN_PROPERTY = "resource.origin";
    public static final String RESOURCE_DISCOVERY_SEQNO_PROPERTY = "resource.discovery_seqno";
    public static final String NAME_VERSION_SEPARATER = ":";

    public static final String ON_ORPHAN_ARTIFACT_PROPERTY = "onOrphanArtifact";
    public static final String ON_EXIST_ARTIFACT_PROPERTY = "onExistArtifact";

    public static final String CONFIG_FILE_PARAMETER = "configFile";
    public static final String DEFAULT_CONFIG_FILE = "discoveryagent.properties";
    public static final String SERVERS_PROPERTY = "servers";
    //TODO - load mediaType from RXT itself.
    public static final String SERVER_RXT_SHORT_NAME = "server";
    public static final String SERVER_RXT_OVERVIEW_NAME = "overview_name";
    public static final String SERVER_RXT_OVERVIEW_VERSION = "overview_version";
    public static final String SERVER_ID_SEPARATER = ",";
    public static final String ARTIFACT_ADDED = "added";
    public static final String ARTIFACT_REMOVED = "removed";
    public static final String ARTIFACT_REPLACED = "replaced";
    public static final String ARTIFACT_IGNORED = "ignored";

    private final Log log = LogFactory.getLog(DiscoveryAgentExecutorSupport.class);


    private OrphanArtifactStrategy onOrphanArtifactStrategy;
    private ExistArtifactStrategy onExistArtifactStrategy;


    protected void updateMaintenanceInfo(GenericArtifact artifact, String seqNo, String originProperty)
            throws GovernanceException {
        artifact.addAttribute(RESOURCE_SOURCE_PROPERTY, SOURCE_GREG_DISCOVERY);
        artifact.addAttribute(RESOURCE_ORIGIN_PROPERTY, originProperty);
        artifact.addAttribute(RESOURCE_DISCOVERY_SEQNO_PROPERTY, seqNo);
    }

    protected Map<String, List<String>> persistDiscoveredArtifacts(Registry registry,
                                                                   Map<String, List<DetachedGenericArtifact>> newArtifacts,
                                                                   GenericArtifact serverArtifact, String seqNo,
                                                                   String originProperty)
            throws RegistryException {
        Map<String, List<String>> feedback = initFeedbackMap();
        for (Map.Entry<String, List<DetachedGenericArtifact>> artifactEntry : newArtifacts.entrySet()) {
            String shortName = artifactEntry.getKey();
            List<DetachedGenericArtifact> artifacts = artifactEntry.getValue();
            GenericArtifactManager artifactManager = getGenericArtifactManager(registry, shortName);
            for (DetachedGenericArtifact artifact : artifacts) {
                persistNewArtifact(artifactManager, artifact, shortName, serverArtifact, seqNo, originProperty, feedback);
            }
        }
        return feedback;
    }

    protected void persistNewArtifact(GenericArtifactManager artifactManager, DetachedGenericArtifact artifact,
                                      String shortName, GenericArtifact server, String seqNo,
                                      String originProperty, Map<String, List<String>> feedback)
            throws GovernanceException {
        if (artifactManager.isExists(artifact)) {
            switch (onExistArtifactStrategy) {
                case IGNORE:
                    log.info("Ignored already existing artifact" + artifact);
                    feedback.get(ARTIFACT_IGNORED).add(shortName + ":" + artifact.getQName().getLocalPart());
                    break;
                case REMOVE:
                    //If artifact is already exists, delete and then add new artifact.
                    artifactManager.removeGenericArtifact(artifact);
                    log.info("Removed already existing artifact" + artifact);
                    addNewGenericArtifact(artifactManager, artifact, server, seqNo, originProperty);
                    feedback.get(ARTIFACT_REPLACED).add(shortName + ":" + artifact.getQName().getLocalPart());
                    break;
                case CUSTOM:
                    customizeExistArtifactStrategy(artifactManager, artifact, seqNo, originProperty);
                    break;
            }
        } else {
            addNewGenericArtifact(artifactManager, artifact, server, seqNo, originProperty);
            feedback.get(ARTIFACT_ADDED).add(shortName + ":" + artifact.getQName().getLocalPart());
        }
    }

    private Map<String, List<String>> initFeedbackMap() {
        Map<String, List<String>> map = new HashMap<>();
        map.put(ARTIFACT_ADDED, new ArrayList<String>());
        map.put(ARTIFACT_REMOVED, new ArrayList<String>());
        map.put(ARTIFACT_REPLACED, new ArrayList<String>());
        map.put(ARTIFACT_IGNORED, new ArrayList<String>());
        return map;
    }

    protected GenericArtifact addNewGenericArtifact(GenericArtifactManager artifactManager,
                                                    DetachedGenericArtifact artifact, GenericArtifact server,
                                                    String seqNo, String originProperty)
            throws GovernanceException {
        GenericArtifact newArtifact = artifact.makeRegistryAware(artifactManager);
        updateMaintenanceInfo(newArtifact, seqNo, originProperty);
        artifactManager.addGenericArtifact(newArtifact);
        addServerToArtifactAssociation(newArtifact, server);
        return newArtifact;
    }

    protected void addServerToArtifactAssociation(GenericArtifact source,
                                                  GenericArtifact destination) throws GovernanceException {
        //TODO
        source.addBidirectionalAssociation("avialbleOn", "contains", destination);
    }


    protected void handleOrphanArtifacts(Registry registry, Set<String> shortNames, String seqNo, String originProperty)
            throws RegistryException {
        switch (onOrphanArtifactStrategy) {
            case IGNORE:
                log.info("Ignored handling orphan artifact");
                break;
            case REMOVE:
                for (String shortName : shortNames) {
                    removeOrphanArtifacts(getGenericArtifactManager(registry, shortName), seqNo, originProperty);
                }
                break;
            case CUSTOM:
                for (String shortName : shortNames) {
                    customizeOrphanArtifactStrategy(getGenericArtifactManager(registry, shortName), seqNo, originProperty);
                }
                break;
        }
    }

    protected void removeOrphanArtifacts(GenericArtifactManager genericArtifactManager, String seqNo,
                                         String originProperty) throws RegistryException {
        for (GenericArtifact artifact : findOrphanArtifacts(genericArtifactManager, seqNo, originProperty)) {
            removeDerivedAssociations(artifact);
            genericArtifactManager.removeGenericArtifact(artifact.getId());
            log.info("Removed orphan artifact belong to " + originProperty + " server : " + artifact);
        }
    }

    protected void removeDerivedAssociations(GenericArtifact artifact) {
        //TODO
    }

    protected List<GenericArtifact> findOrphanArtifacts(GenericArtifactManager genericArtifactManager, String seqNo,
                                                        String originProperty) throws GovernanceException {
        List<GenericArtifact> orphanArtifacts = new ArrayList<>();
        Map<String, List<String>> options = new HashMap<>();
        options.put("propertyName", Arrays.asList(RESOURCE_ORIGIN_PROPERTY));
        options.put("rightOp", Arrays.asList("eq"));
        options.put("rightPropertyValue", Arrays.asList(originProperty));
        Registry govRegistry;
        try {
            govRegistry = getGovRegistry();
        } catch (RegistryException e) {
            throw new GovernanceException(e);
        }
        if (govRegistry != null) {
            for (GenericArtifact artifact : genericArtifactManager.findGenericArtifacts(options)) {
                Resource resource = null;
                try {
                    resource = govRegistry.get(artifact.getPath());
                } catch (RegistryException e) {
                    //We still have to check other artifacts so continue...
                }
                if (resource != null) {
                    String currentSeqNo = resource.getProperty("resource.discovery_seqno");
                    if (currentSeqNo == null || !seqNo.equals(currentSeqNo)) {
                        orphanArtifacts.add(artifact);
                    }
                }
            }
        }
        return orphanArtifacts;
    }

    protected void customizeOrphanArtifactStrategy(GenericArtifactManager genericArtifactManager, String seqNo,
                                                   String originProperty) {
        throw new UnsupportedOperationException("Override customizeOrphanArtifactStrategy method in a subclass of" +
                                                " DiscoveryAgentExecutorTask  ");
    }

    protected void customizeExistArtifactStrategy(GenericArtifactManager artifactManager, GenericArtifact artifact,
                                                  String seqNo, String originProperty) throws GovernanceException {

        throw new UnsupportedOperationException("Override customizeExistArtifactStrategy method in a subclass of" +
                                                " DiscoveryAgentExecutorTask  ");
    }

    protected void setExistArtifactStrategy(Map<String, String> properties) {
        String existProperty = properties.get(ON_EXIST_ARTIFACT_PROPERTY);
        onExistArtifactStrategy = ExistArtifactStrategy.valueOf(existProperty.toUpperCase());
    }

    protected void setOrphanArtifactStrategy(Map<String, String> properties) {
        String orphanProperty = properties.get(ON_ORPHAN_ARTIFACT_PROPERTY);
        onOrphanArtifactStrategy = OrphanArtifactStrategy.valueOf(orphanProperty.toUpperCase());
    }


    protected String getOriginProperty(GenericArtifact serverArtifact) throws GovernanceException {
        String serverName = serverArtifact.getAttribute(SERVER_RXT_OVERVIEW_NAME);
        String serverVersion = serverArtifact.getAttribute(SERVER_RXT_OVERVIEW_VERSION);
        if (serverVersion != null) {
            serverName = serverName.concat(serverVersion);
        }
        return serverName;
    }

    protected Registry getConfigRegistry() throws RegistryException {
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(CarbonConstants.REGISTRY_SYSTEM_USERNAME);
        return GovernanceRegistryExtensionsDataHolder.getInstance().getRegistryService().getConfigSystemRegistry();
    }

    protected Registry getGovRegistry() throws RegistryException {
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(CarbonConstants.REGISTRY_SYSTEM_USERNAME);
        return GovernanceRegistryExtensionsDataHolder.getInstance().getRegistryService().getGovernanceSystemRegistry();
    }

    protected String getSequnceNo() {
        return UUID.randomUUID().toString();
    }

    protected GenericArtifactManager getGenericArtifactManager(Registry registry, String mediaType)
            throws RegistryException {
        return new GenericArtifactManager(registry, mediaType);
    }


}
