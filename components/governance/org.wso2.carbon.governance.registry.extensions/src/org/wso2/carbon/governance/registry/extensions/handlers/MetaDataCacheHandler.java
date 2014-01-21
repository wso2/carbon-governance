package org.wso2.carbon.governance.registry.extensions.handlers;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.api.cache.ArtifactCache;
import org.wso2.carbon.governance.api.cache.ArtifactCacheManager;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.registry.extensions.utils.CommonUtil;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.session.CurrentSession;
import org.wso2.carbon.registry.core.utils.RegistryUtils;

import java.lang.String;

/**
 * This handler used to handle the Meta Data resources(wsdl,schema,policy, and rxt) cache
 */
public class MetaDataCacheHandler extends Handler {
    private static final Log log = LogFactory.getLog(MetaDataCacheHandler.class);

    @Override
    public void put(RequestContext requestContext) throws RegistryException {
       clearPreFetchArtifact(requestContext);
    }

    @Override
    public void delete(RequestContext requestContext) throws RegistryException {
        clearPreFetchArtifact(requestContext);
    }

   /**
     *  Clear meta data cache
     * @param requestContext RequestContext
     */
    private void clearPreFetchArtifact(RequestContext requestContext) throws RegistryException {
        if(!CommonUtil.isMetaDataClearLockAvailable()){
            return;
        }
        CommonUtil.acquireMetaDataClearLock();
        try{
            Resource resource = requestContext.getResource();
            if (resource == null || resource.getUUID() == null) {
               return;
            }
        String mediaType = resource.getMediaType();
        String artifactPath = null;
        try {
            artifactPath = GovernanceUtils.getArtifactPath(requestContext.getRegistry(), resource.getUUID());
        } catch (GovernanceException e) {
            String msg = "Failed to get path of artifact id = " + resource.getUUID();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }

        if (mediaType == null || artifactPath == null) {
            return;
        }
        if (mediaType.matches("application/.[a-zA-Z0-9.-]+\\+xml")) {
            ArtifactCache artifactCache =
                    ArtifactCacheManager.getCacheManager().
                            getTenantArtifactCache(CurrentSession.getTenantId());
            String cachePath = RegistryUtils.getRelativePathToOriginal(artifactPath,
                    RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH);
            if (artifactCache != null) {
                if (artifactCache.getArtifact(cachePath) != null) {
                    artifactCache.invalidateArtifact(cachePath);
                }
            }
        }
        }finally {
            CommonUtil.releaseMetaDataClearLock();
        }
    }
}
