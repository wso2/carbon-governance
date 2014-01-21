package org.wso2.carbon.governance.registry.extensions.executors;

import org.wso2.carbon.governance.registry.extensions.interfaces.Execution;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;

import java.util.Map;

import static org.wso2.carbon.governance.registry.extensions.executors.utils.ExecutorConstants.*;

public class DemoteActionExecutor implements Execution{

    private String serviceMediaType = "application/vnd.wso2-service+xml";

    public void init(Map parameterMap) {
        if (parameterMap.get(SERVICE_MEDIA_TYPE_KEY) != null) {
            serviceMediaType = parameterMap.get(SERVICE_MEDIA_TYPE_KEY).toString();
        }
    }

    public boolean execute(RequestContext context, String currentState, String targetState) {
        String mediaType = context.getResource().getMediaType();
        if (mediaType != null && mediaType.equals(serviceMediaType)) {
            context.setResource(null);
        }
        return true;
    }
}
