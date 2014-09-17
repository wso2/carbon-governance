package org.wso2.carbon.governance.api.cache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import javax.cache.event.CacheEntryEvent;
import javax.cache.event.CacheEntryListenerException;
import javax.cache.event.CacheEntryRemovedListener;

/**
 * Created by pulasthi on 9/17/14.
 */
public class RXTConfigCacheEntryRemovedListener<K, V>  implements CacheEntryRemovedListener<K, V> {
    private static final Log log = LogFactory.getLog(RXTConfigCacheEntryRemovedListener.class);

    @Override
    public void entryRemoved(CacheEntryEvent<? extends K, ? extends V> cacheEntryEvent) throws CacheEntryListenerException {
        Registry registry = PrivilegedCarbonContext.getThreadLocalCarbonContext().getRegistry(RegistryType.SYSTEM_GOVERNANCE);
        try {
            GovernanceUtils.unRegisterArtifactConfigurationByPath(registry, PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(), cacheEntryEvent.getKey().toString());
        } catch (RegistryException e) {
            log.error("Error while adding artifact configurations",e);
        }
    }
}
