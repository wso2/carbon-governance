package org.wso2.carbon.registry.metadata.models.protocol;

import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.metadata.Base;
import org.wso2.carbon.registry.metadata.VersionBase;
import org.wso2.carbon.registry.metadata.exception.MetadataException;

import java.util.List;
import java.util.Map;

public class Protocol extends Base {
    public Protocol(String mediaType, String name, Registry registry) throws MetadataException {
        super(mediaType, name, registry);
    }

    public Protocol(String mediaType, String name, Registry registry, VersionBase version) throws MetadataException {
        super(mediaType, name, registry, version);
    }

    public Protocol(String mediaType, String name, String uuid, Map<String, List<String>> propertyBag, Map<String, List<String>> attributeMap, Registry registry) throws MetadataException {
        super(mediaType, name, uuid, propertyBag, attributeMap, registry);
    }
}
