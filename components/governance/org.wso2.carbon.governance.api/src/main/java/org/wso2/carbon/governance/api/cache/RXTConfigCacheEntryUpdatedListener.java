package org.wso2.carbon.governance.api.cache;


import javax.cache.event.CacheEntryEvent;
import javax.cache.event.CacheEntryListener;
import javax.cache.event.CacheEntryListenerException;
import javax.cache.event.CacheEntryUpdatedListener;

/**
 * Created by pulasthi on 9/16/14.
 */
public class RXTConfigCacheEntryUpdatedListener<K, V>  implements CacheEntryUpdatedListener<K, V> {

    @Override
    public void entryUpdated(CacheEntryEvent<? extends K, ? extends V> cacheEntryEvent) throws CacheEntryListenerException {

    }
}
