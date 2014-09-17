package org.wso2.carbon.governance.api.cache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.uddi.utils.GovernanceUtil;
import org.wso2.carbon.registry.core.Registry;

import javax.cache.event.CacheEntryCreatedListener;
import javax.cache.event.CacheEntryEvent;
import javax.cache.event.CacheEntryListenerException;

/**
 * Created by pulasthi on 9/17/14.
 */
public class RXTConfigCacheEntryCreatedListener<K, V>  implements CacheEntryCreatedListener<K, V> {
    private static final Log log = LogFactory.getLog(RXTConfigCacheEntryCreatedListener.class);
    @Override
    public void entryCreated(CacheEntryEvent<? extends K, ? extends V> cacheEntryEvent) throws CacheEntryListenerException {
        Registry registry = PrivilegedCarbonContext.getThreadLocalCarbonContext().getRegistry(RegistryType.SYSTEM_GOVERNANCE);
        try {
            GovernanceUtils.registerArtifactConfigurationByPath(registry,PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(),cacheEntryEvent.getKey().toString());
        } catch (RegistryException e) {
            log.error("Error while adding artifact configurations",e);
        }
    }
}
