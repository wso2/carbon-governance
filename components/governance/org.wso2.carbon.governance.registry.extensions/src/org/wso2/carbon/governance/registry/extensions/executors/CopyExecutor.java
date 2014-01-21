package org.wso2.carbon.governance.registry.extensions.executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.registry.extensions.executors.utils.ExecutorConstants;
import org.wso2.carbon.governance.registry.extensions.executors.utils.Utils;
import org.wso2.carbon.governance.registry.extensions.interfaces.Execution;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourcePath;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.extensions.utils.CommonConstants;

import java.util.HashMap;
import java.util.Map;

import static org.wso2.carbon.governance.registry.extensions.aspects.utils.Utils.getHistoryInfoElement;
import static org.wso2.carbon.governance.registry.extensions.executors.utils.Utils.addNewId;
import static org.wso2.carbon.governance.registry.extensions.executors.utils.Utils.populateParameterMap;

public class CopyExecutor implements Execution {
    private static final Log log = LogFactory.getLog(ServiceVersionExecutor.class);
    private static final String KEY = ExecutorConstants.RESOURCE_VERSION;
    protected Map parameterMap;

    //    To track whether we need to move comments,tags,ratings and all the associations.
    private boolean copyComments = false;
    private boolean copyTags = false;
    private boolean copyRatings = false;
    private boolean copyAllAssociations = false;

    public void init(Map map) {
        parameterMap = map;

        if(parameterMap.get(ExecutorConstants.COPY_COMMENTS) != null){
            copyComments = Boolean.parseBoolean((String) parameterMap.get(ExecutorConstants.COPY_COMMENTS));
        }
        if(parameterMap.get(ExecutorConstants.COPY_TAGS) != null){
            copyTags = Boolean.parseBoolean((String) parameterMap.get(ExecutorConstants.COPY_TAGS));
        }
        if(parameterMap.get(ExecutorConstants.COPY_RATINGS) != null){
            copyRatings = Boolean.parseBoolean((String) parameterMap.get(ExecutorConstants.COPY_RATINGS));
        }
        if(parameterMap.get(ExecutorConstants.COPY_ASSOCIATIONS) != null){
            copyAllAssociations = Boolean.parseBoolean((String) parameterMap.get(ExecutorConstants.COPY_ASSOCIATIONS));
        }
    }

    public boolean execute(RequestContext requestContext, String currentState, String targetState) {

        String resourcePath = requestContext.getResource().getPath();
        String newPath;

//        Now we are going to get the list of parameters from the context and add it to a map
        Map<String, String> currentParameterMap = new HashMap<String, String>();

//        Here we are populating the parameter map that was given from the UI
        if (!populateParameterMap(requestContext, currentParameterMap)) {
            log.error("Failed to populate the parameter map");
            return false;
        }

//        This section is there to add a version to the path if needed.
//        This is all based on the lifecycle configuration and the configuration should be as follows.
//        path = /_system/governance/environment/{@version}
//        Also for this the user has to have a transition UI where he can give the version
        String currentEnvironment = getReformattedPath((String) parameterMap.get(ExecutorConstants.CURRENT_ENVIRONMENT),
                KEY, currentParameterMap.get(resourcePath));
        String targetEnvironment = getReformattedPath((String) parameterMap.get(ExecutorConstants.TARGET_ENVIRONMENT),
                KEY, currentParameterMap.get(resourcePath));

        if(resourcePath.startsWith(currentEnvironment)){
            newPath = resourcePath.substring(currentEnvironment.length());
            newPath = targetEnvironment + newPath;
        }else{
            log.warn("Resource is not in the given environment");
            return true;
        }

        try {
            doCopy(requestContext, resourcePath, newPath);
            Resource newResource = requestContext.getRegistry().get(newPath);

            if(newResource.getUUID() != null){
                addNewId(requestContext.getRegistry(), newResource, newPath);
            }

            requestContext.setResource(newResource);
            requestContext.setResourcePath(new ResourcePath(newPath));

//            Copying comments
            copyComments(requestContext.getRegistry(), newPath, resourcePath);

//           Copying tags
            copyTags(requestContext.getRegistry(), newPath, resourcePath);

//           Copying ratings. We only copy the average ratings
            copyRatings(requestContext.getSystemRegistry(), newPath, resourcePath);

//           Copying all the associations.
//           We avoid copying dependencies here
            copyAllAssociations(requestContext.getRegistry(), newPath, resourcePath);

            return true;
        } catch (RegistryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return false;
        }

    }
    public String getReformattedPath(String originalPath, String key, String value){
        if(key == null || value == null){
            return originalPath;
        }
        return originalPath.replace(key,value);
    }

    private void copyAllAssociations(Registry registry, String newPath, String path) throws RegistryException {
        if (copyAllAssociations) {
            Utils.copyAssociations(registry, newPath, path);
        }
    }

    private void copyRatings(Registry registry, String newPath, String path) throws RegistryException {
        if (copyRatings) {
            Utils.copyRatings(registry, newPath, path);
        }
    }

    private void copyTags(Registry registry, String newPath, String path) throws RegistryException {
        if (copyTags) {
            Utils.copyTags(registry, newPath, path);
        }
    }

    private void copyComments(Registry registry, String newPath, String path) throws RegistryException {
        if (copyComments) {
            Utils.copyComments(registry, newPath, path);
        }
    }

    protected void doCopy(RequestContext requestContext, String resourcePath, String newPath)
            throws RegistryException {
        requestContext.getRegistry().copy(resourcePath, newPath);
    }
}

