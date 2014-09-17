package org.wso2.carbon.governance.api.cache;

import javax.cache.event.CacheEntryCreatedListener;
import javax.cache.event.CacheEntryEvent;
import javax.cache.event.CacheEntryListenerException;

/**
 * Created by pulasthi on 9/17/14.
 */
public class RXTConfigCacheEntryCreatedListener<K, V>  implements CacheEntryCreatedListener<K, V> {
    @Override
    public void entryCreated(CacheEntryEvent<? extends K, ? extends V> cacheEntryEvent) throws CacheEntryListenerException {

    }
}
