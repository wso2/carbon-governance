package org.wso2.carbon.governance.platform.extensions.mediation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.platform.extensions.util.MediationUtils;
import org.wso2.carbon.ntask.core.Task;

import java.util.Map;

public class MediationArtifactPopulatorTask implements Task {

    private static final Log log = LogFactory.getLog(MediationArtifactPopulatorTask.class);

    @Override
    public void setProperties(Map<String, String> properties) {
        MediationUtils.setUserName(properties.get("userName"));
        MediationUtils.setPassword(properties.get("password"));
        MediationUtils.setServerEpr(properties.get("serverUrl"));
        MediationUtils.setProxyArtifactKey(properties.get("proxyArtifactKey"));
        MediationUtils.setEndpointArtifactKey(properties.get("endpointArtifactKey"));
        MediationUtils.setSequenceArtifactKey(properties.get("sequenceArtifactKey"));
    }

    @Override
    public void init() {
       log.info("MediationArtifactPopulatorTask initialized..");
    }

    @Override
    public void execute() {
        try {
            MediationUtils.populateMediationArtifacts();
        } catch (Exception e) {
          log.error("Error while performing MediationArtifactPopulatorTask" + e.getMessage());
        }
    }


}
