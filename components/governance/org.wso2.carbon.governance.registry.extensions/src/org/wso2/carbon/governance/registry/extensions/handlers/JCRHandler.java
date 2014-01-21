package org.wso2.carbon.governance.registry.extensions.handlers;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.wso2.carbon.governance.api.common.GovernanceArtifactManager;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceArtifactConfiguration;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.extensions.utils.CommonUtil;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;


public class JCRHandler extends Handler {

    private String key = null;

    public void setKey(String key) {
        this.key = key;
    }

    public void put(RequestContext requestContext) throws RegistryException {
        if (!CommonUtil.isUpdateLockAvailable()) {
            return;
        }
        CommonUtil.acquireUpdateLock();
        try {
            UserRegistry registry = (UserRegistry) GovernanceUtils.getGovernanceSystemRegistry(
                    requestContext.getSystemRegistry());
            Resource resource = requestContext.getResource();
            String tempKey = key;
            if (tempKey == null) {
                List<GovernanceArtifactConfiguration> governanceArtifactConfigurations =
                        GovernanceUtils.findGovernanceArtifactConfigurations(registry);
                GovernanceUtils.loadGovernanceArtifacts(registry, governanceArtifactConfigurations);
                for (GovernanceArtifactConfiguration configuration : governanceArtifactConfigurations) {
                    if (configuration.getMediaType().equals(resource.getMediaType())) {
                        tempKey = configuration.getKey();
                        break;
                    }
                }
            }
            GenericArtifactManager manager = new GenericArtifactManager(registry, tempKey);
            Object content = resource.getContent();
            try {
                String contentString;
                if (content instanceof String) {
                    contentString = (String) content;
                } else {
                    contentString = RegistryUtils.decodeBytes((byte[])content);
                }
                Resource oldResource = requestContext.getOldResource();
                String oldContentString = "";
                if (oldResource != null) {
                    Object oldContent = oldResource.getContent();
                    if (oldContent instanceof String) {
                        oldContentString = (String) oldContent;
                    } else {
                        oldContentString = RegistryUtils.decodeBytes((byte[])oldContent);
                    }
                }
                if (oldResource == null || !contentString.equals(oldContentString)) {
                    OMElement contentElement = AXIOMUtil.stringToOM(contentString);
                    GenericArtifact genericArtifact = manager.newGovernanceArtifact(contentElement);
                    String[] attributeKeys = genericArtifact.getAttributeKeys();
                    for (String key : attributeKeys) {
                        resource.setProperty(key, Arrays.asList(genericArtifact.getAttributes(key)));
                    }
                } else {
                    Properties properties = resource.getProperties();
                    GenericArtifact genericArtifact = manager.newGovernanceArtifact(new QName("Dummy"));
                    for (Object key : properties.keySet()) {
                        String keyString = (String) key;
                        if (keyString.contains("_") && !RegistryUtils.isHiddenProperty(keyString)) {
                            List<String> propertyValues = resource.getPropertyValues(keyString);
                            if (propertyValues.size() > 1) {
                                genericArtifact.setAttributes(keyString,
                                        propertyValues.toArray(new String[propertyValues.size()]));
                            } else {
                                genericArtifact.setAttribute(keyString, propertyValues.get(0));
                            }
                        }
                    }
                    new GovernanceArtifactManager(null, null, null, null,
                            "metadata", "http://www.wso2.org/governance/metadata", null, null) {
                        protected void setContent(GovernanceArtifact artifact,
                                                  Resource resource) throws GovernanceException {
                            super.setContent(artifact, resource);
                        }
                    }.setContent(genericArtifact, resource);
                }
            } catch (XMLStreamException e) {
                throw new RegistryException("Unable to parse content", e);
            }
        } finally {
            CommonUtil.releaseUpdateLock();
        }
    }
}
