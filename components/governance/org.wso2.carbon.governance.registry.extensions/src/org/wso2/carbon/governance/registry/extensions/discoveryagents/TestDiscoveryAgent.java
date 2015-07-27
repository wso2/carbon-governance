package org.wso2.carbon.governance.registry.extensions.discoveryagents;

import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.DetachedGenericArtifact;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class TestDiscoveryAgent implements DiscoveryAgent {

    @Override public void init(Properties properties) {
        System.out.println("Discovery agent TestDiscoveryAgent class initialized");
    }

    @Override public void close(Properties properties) {

    }

    @Override public Map<String, List<DetachedGenericArtifact>> discoverArtifacts(GenericArtifact server)
            throws DiscoveryAgentException {
        return this.discoverArtifactsInternal(server);
    }


    private Map<String, List<DetachedGenericArtifact>> discoverArtifactsInternal(GenericArtifact server)
            throws DiscoveryAgentException {
        Map<String, List<DetachedGenericArtifact>> map = new HashMap<>();
        List<DetachedGenericArtifact> restArtifacts = new ArrayList<>();
        List<DetachedGenericArtifact> soapArtifacts = new ArrayList<>();

        for (int i = 1; i < 11; i++) {
            DetachedGenericArtifact artifact = (DetachedGenericArtifact) GenericArtifactManager
                    .newDetachedGovernanceArtifact(new QName("TestRESTService" + i),
                                                   "application/vnd.wso2-restservice+xml");
            try {
                artifact.setAttribute("overview_name", "TestRESTService" + i);
                artifact.setAttribute("overview_provider", "admin");
                artifact.setAttribute("overview_context", "/test2");
                artifact.setAttribute("overview_version", "1.0.0");
            } catch (GovernanceException e) {
                throw new DiscoveryAgentException("Exception setting requred attributes", e);
            }
            restArtifacts.add(artifact);
        }
        for (int i = 1; i < 11; i++) {
            DetachedGenericArtifact artifact = (DetachedGenericArtifact) GenericArtifactManager
                    .newDetachedGovernanceArtifact(new QName("TestSOAPService" + i),
                                                   "application/vnd.wso2-soapservice+xml");
            try {
                artifact.setAttribute("overview_name", "TestSOAPService" + i);
                artifact.setAttribute("overview_version", "1.0.0");
            } catch (GovernanceException e) {
                throw new DiscoveryAgentException("Exception setting requred attributes", e);
            }
            soapArtifacts.add(artifact);
        }
        map.put("restservice", restArtifacts);
        map.put("soapservice", soapArtifacts);

        return map;
    }
}
