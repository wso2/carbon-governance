package org.wso2.carbon.governance.api.cache;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import javax.cache.event.CacheEntryEvent;
import javax.cache.event.CacheEntryListener;
import javax.cache.event.CacheEntryListenerException;
import javax.cache.event.CacheEntryUpdatedListener;

/**
 * Created by pulasthi on 9/16/14.
 */
public class RXTConfigCacheEntryUpdatedListener<K, V>  implements CacheEntryUpdatedListener<K, V> {
    private static final Log log = LogFactory.getLog(RXTConfigCacheEntryUpdatedListener.class);
    @Override
    public void entryUpdated(CacheEntryEvent<? extends K, ? extends V> cacheEntryEvent) throws CacheEntryListenerException {
        Registry registry = PrivilegedCarbonContext.getThreadLocalCarbonContext().getRegistry(RegistryType.SYSTEM_GOVERNANCE);
        try {
            GovernanceUtils.registerArtifactConfigurationByPath(registry, PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(), cacheEntryEvent.getKey().toString());
        } catch (RegistryException e) {
            log.error("Error while adding artifact configurations",e);
        }
    }
}
