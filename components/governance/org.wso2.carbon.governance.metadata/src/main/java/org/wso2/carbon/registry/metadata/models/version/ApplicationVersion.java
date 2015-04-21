package org.wso2.carbon.registry.metadata.models.version;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.metadata.Constants;
import org.wso2.carbon.registry.metadata.Util;
import org.wso2.carbon.registry.metadata.VersionBase;
import org.wso2.carbon.registry.metadata.exception.MetadataException;
import org.wso2.carbon.registry.metadata.models.service.HTTPService;

import java.util.List;
import java.util.Map;

public class ApplicationVersion extends VersionBase {

    //  Variables defined for internal implementation purpose
    private static String mediaType = "vnd.wso2.version/application+xml;version=1";
    private static final Log log = LogFactory.getLog(ApplicationVersion.class);

    public ApplicationVersion(String mediaType, String name, Registry registry) throws MetadataException {
        super(mediaType, name, registry);
    }

    @Override
    public String getUUID() {
        return uuid;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void addSerivce(HTTPService httpService) throws MetadataException {
        HTTPService.add(registry, httpService);
        Util.createAssociation(registry, uuid, httpService.getUUID(), Constants.ASSOCIATION_USEDBY);
        Util.createAssociation(registry, httpService.getUUID(), uuid, Constants.ASSOCIATION_DEPENDSON);
    }

    public HTTPService [] getServices() {
        return null;
    }

    public void removeService(String uuid) {

    }

    public static void add(Registry registry, VersionBase metadata) throws MetadataException {
        add(registry, metadata, generateMetadataStoragePath(
                metadata.getBaseName()
                , metadata.getName()
                , metadata.getRootStoragePath()));

    }

    public static void update(Registry registry, VersionBase metadata) throws MetadataException {
        update(registry, metadata, generateMetadataStoragePath(
                metadata.getBaseName()
                , metadata.getName()
                , metadata.getRootStoragePath()));
    }


    /**
     * @return all meta data instances and their children that denotes from this particular media type
     */
    public static ApplicationVersion[] getAll(Registry registry) throws MetadataException {
        List<VersionBase> list = getAll(registry, mediaType);
        return list.toArray(new ApplicationVersion[list.size()]);
    }

    /**
     * Search all meta data instances of this particular type with the given search attributes
     *
     * @param criteria Key value map that has search attributes
     * @return
     */
    public static ApplicationVersion[] find(Registry registry, Map<String, String> criteria) throws MetadataException {
        List<VersionBase> list = find(registry, criteria, mediaType);
        return list.toArray(new ApplicationVersion[list.size()]);
    }

    /**
     * Returns the meta data instance that can be identified by the given UUID
     *
     * @param uuid - UUID of the metadata insatnce
     * @return meta data from the UUID
     */
    public static ApplicationVersion get(Registry registry, String uuid) throws MetadataException {
        return (ApplicationVersion) get(registry, uuid, mediaType);
    }

}
