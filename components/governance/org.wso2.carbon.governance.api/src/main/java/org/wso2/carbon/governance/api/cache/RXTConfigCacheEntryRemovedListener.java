package org.wso2.carbon.governance.api.cache;

import javax.cache.event.CacheEntryEvent;
import javax.cache.event.CacheEntryListenerException;
import javax.cache.event.CacheEntryRemovedListener;

/**
 * Created by pulasthi on 9/17/14.
 */
public class RXTConfigCacheEntryRemovedListener<K, V>  implements CacheEntryRemovedListener<K, V> {
    @Override
    public void entryRemoved(CacheEntryEvent<? extends K, ? extends V> cacheEntryEvent) throws CacheEntryListenerException {

    }
}
