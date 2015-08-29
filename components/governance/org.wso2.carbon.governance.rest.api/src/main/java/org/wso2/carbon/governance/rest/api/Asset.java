/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.governance.rest.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.DetachedGenericArtifact;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceArtifactConfiguration;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.rest.api.internal.PaginationInfo;
import org.wso2.carbon.governance.rest.api.model.AssetState;
import org.wso2.carbon.governance.rest.api.model.AssetStateChange;
import org.wso2.carbon.governance.rest.api.model.TypedList;
import org.wso2.carbon.governance.rest.api.util.Util;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.pagination.PaginationContext;
import org.wso2.carbon.registry.core.service.RegistryService;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

//TODO - test this
//@RolesAllowed("GOV-REST")
public class Asset {


    public static final String ENDPOINTS = "endpoints";
    public static final String ENDPOINT = "endpoint";
    private final Log log = LogFactory.getLog(Asset.class);

    @GET
    @Path("/types")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTypes() throws RegistryException {
        return getAssetTypes();
    }

    @GET
    @Path("{assetType : [a-zA-Z][a-zA-Z_0-9]*}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAssets(@PathParam("assetType") String assetType, @Context UriInfo uriInfo)
            throws RegistryException {
        return getGovernanceAssets(assetType, uriInfo);
    }

    @GET
    @Path("{assetType : [a-zA-Z][a-zA-Z_0-9]*}/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAsset(@PathParam("assetType") String assetType, @PathParam("id") String id)
            throws RegistryException {
        return getGovernanceAsset(assetType, id);
    }

    @GET
    @Path("{assetType : [a-zA-Z][a-zA-Z_0-9]*}/endpoints")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAssetEndpoints(@PathParam("assetType") String assetType, @Context UriInfo uriInfo)
            throws RegistryException {
        return getGovernanceEndpointAssets(assetType, null, uriInfo);
    }

    @GET
    @Path("{assetType : [a-zA-Z][a-zA-Z_0-9]*}/{id}/endpoints")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAssetEndpoints(@PathParam("assetType") String assetType, @PathParam("id") String id,
                                      @Context UriInfo uriInfo) throws RegistryException {
        return getGovernanceEndpointAssets(assetType, id, uriInfo);
    }

    @POST
    @Path("{assetType : [a-zA-Z][a-zA-Z_0-9]*}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createAsset(@PathParam("assetType") String assetType, GenericArtifact genericArtifact,
                                @Context UriInfo uriInfo) throws RegistryException {
        return persistGovernanceAsset(assetType, (DetachedGenericArtifact) genericArtifact, Util.getBaseURL(uriInfo));
    }


    @PUT
    @Path("{assetType : [a-zA-Z][a-zA-Z_0-9]*}/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response modifyAsset(@PathParam("assetType") String assetType, @PathParam("id") String id,
                                GenericArtifact genericArtifact, @Context UriInfo uriInfo) throws RegistryException {
        return modifyGovernanceAsset(assetType, id, (DetachedGenericArtifact) genericArtifact, Util.getBaseURL(uriInfo));
    }


    @DELETE
    @Path("{assetType : [a-zA-Z][a-zA-Z_0-9]*}/{id}")
    public Response deleteAsset(@PathParam("assetType") String assetType, @PathParam("id") String id)
            throws RegistryException {
        return deleteGovernanceAsset(assetType, id);
    }


    @GET
    @Path("/endpoints")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEndpoints(@Context UriInfo uriInfo) throws RegistryException {
        return getGovernanceAssets(ENDPOINTS, uriInfo);
    }

    @GET
    @Path("/endpoints/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEndpoint(@PathParam("id") String id) throws RegistryException {
        return getGovernanceEndpoint(id);
    }


    @POST
    @Path("/endpoints")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createEndpoint(GenericArtifact genericArtifact, @Context UriInfo uriInfo)
            throws RegistryException {
        return persistGovernanceAsset(ENDPOINTS, (DetachedGenericArtifact) genericArtifact, Util.getBaseURL(uriInfo));
    }

    @POST
    @Path("/endpoints/{assetType : [a-zA-Z][a-zA-Z_0-9]*}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createEndpoint(@PathParam("assetType") String assetType,
                                   GenericArtifact genericArtifact, @Context UriInfo uriInfo) throws RegistryException {
        return createEndpointWithAssociation(assetType, null, uriInfo, genericArtifact);
    }


    @POST
    @Path("/endpoints/{assetType : [a-zA-Z][a-zA-Z_0-9]*}/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createEndpoint(@PathParam("assetType") String assetType, @PathParam("id") String id,
                                   GenericArtifact genericArtifact, @Context UriInfo uriInfo) throws RegistryException {
        return createEndpointWithAssociation(assetType, id, uriInfo, genericArtifact);
    }

    @PUT
    @Path("endpoints/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response modifyEndpoint(@PathParam("id") String id,
                                   GenericArtifact genericArtifact, @Context UriInfo uriInfo) throws RegistryException {
        return modifyGovernanceAsset(ENDPOINTS, id, (DetachedGenericArtifact) genericArtifact, Util.getBaseURL(uriInfo));
    }


    @DELETE
    @Path("{endpoints/{id}")
    public Response deleteEndpoint(@PathParam("id") String id) throws RegistryException {
        return deleteGovernanceAsset("endpoints", id);
    }

    @GET
    @Path("{endpoint/{id}/states")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEndpointStates(@PathParam("id") String id,
                                      @Context UriInfo uriInfo) throws RegistryException {
        String lc = uriInfo.getQueryParameters().getFirst("lc");
        return getGovernanceAssetStates(ENDPOINTS, id, lc);
    }

    @GET
    @Path("{assetType : [a-zA-Z][a-zA-Z_0-9]*}/{id}/states")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAssetStates(@PathParam("assetType") String assetType, @PathParam("id") String id,
                                   @Context UriInfo uriInfo) throws RegistryException {
        String lc = uriInfo.getQueryParameters().getFirst("lc");
        return getGovernanceAssetStates(assetType, id, lc);
    }


    @PUT
    @Path("{assetType : [a-zA-Z][a-zA-Z_0-9]*}/{id}/states")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateLCState(@PathParam("assetType") String assetType, @PathParam("id") String id,
                                  AssetStateChange stateChange,
                                  @Context UriInfo uriInfo) throws RegistryException {
        return updateLCState(assetType, id, stateChange);
    }

    protected Registry getUserRegistry() throws RegistryException {
        CarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        RegistryService registryService = (RegistryService) carbonContext.
                getOSGiService(RegistryService.class, null);
        return registryService.getGovernanceUserRegistry(carbonContext.getUsername(), carbonContext.getTenantId());

    }

    private Response createEndpointWithAssociation(String assetType, String id, UriInfo uriInfo,
                                                   GenericArtifact genericArtifact) throws RegistryException {
        String shortName = Util.getShortName(assetType);
        if (validateAssetType(shortName)) {
            GenericArtifactManager manager = getGenericArtifactManager(ENDPOINT);
            GenericArtifact newArtifact = ((DetachedGenericArtifact) genericArtifact).makeRegistryAware(manager);
            Response response = persistGovernanceAsset(ENDPOINTS, manager, newArtifact, Util.getBaseURL(uriInfo));
            createEndpointAssociation(shortName, id, uriInfo, newArtifact);
            return response;
        }
        return validationFail(shortName);
    }

    private void createEndpointAssociation(String shortName, String id, UriInfo uriInfo,
                                           GovernanceArtifact genericArtifact) throws RegistryException {
        GenericArtifact source = getUniqueAsset(shortName, id, uriInfo);
        if (source != null) {
            source.addBidirectionalAssociation(Util.ENDPOINT_ASSOCIATION_BELONG_TO, Util.ENDPOINT_ASSOCIATION_USE,
                                               genericArtifact);
        }
    }

    private String createQuery(UriInfo uriInfo) {
        StringBuilder builder = new StringBuilder("");
        MultivaluedMap<String, String> queryParam = uriInfo.getQueryParameters();
        Util.excludePaginationParameters(queryParam);
        Iterator<Map.Entry<String, List<String>>> iterator = queryParam.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, List<String>> entry = iterator.next();
            String value = entry.getValue().get(0);
            if (value != null) {
                builder.append(entry.getKey() + "=" + value);
            }
            if (iterator.hasNext()) {
                builder.append("&");
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Query : " + builder.toString());
        }
        return builder.toString();
    }

    private Response getAssetTypes() throws RegistryException {
        List<String> shortNames = new ArrayList<>();
        List<GovernanceArtifactConfiguration> configurations = GovernanceUtils.findGovernanceArtifactConfigurations
                (getUserRegistry());
        for (GovernanceArtifactConfiguration configuration : configurations) {
            shortNames.add(configuration.getSingularLabel());
        }
        return Response.ok().entity(shortNames).build();
    }


    private Response updateLCState(String assetType, String id, AssetStateChange stateChange) throws RegistryException {
        String shortName = Util.getShortName(assetType);
        GenericArtifactManager manager = new GenericArtifactManager(getUserRegistry(), shortName);
        GenericArtifact artifact = manager.getGenericArtifact(id);
        if (artifact != null) {
            //TODO - change to Gov level
            getUserRegistry().invokeAspect(artifact.getPath(), stateChange.getLifecycle(),
                                           stateChange.getAction(), stateChange.getParameters());
            return getGovernanceAssetStates(artifact, null);
        }
        return Response.ok().build();
    }


    private Response getGovernanceAssetStates(String assetType, String id, String lcName) throws RegistryException {
        String shortName = Util.getShortName(assetType);
        GenericArtifactManager manager = new GenericArtifactManager(getUserRegistry(), shortName);
        GenericArtifact artifact = manager.getGenericArtifact(id);
        return getGovernanceAssetStates(artifact, lcName);
    }

    private Response getGovernanceAssetStates(GenericArtifact artifact, String lcName) throws RegistryException {
        AssetState assetState = null;
        if (artifact != null) {
            // lc == null means user look for all LCs
            if (lcName != null) {
                String state = artifact.getLifecycleState(lcName);
                assetState = new AssetState(state);
            } else {
                String[] stateNames = artifact.getLifecycleNames();
                if (stateNames != null) {
                    assetState = new AssetState();
                    for (String name : stateNames) {
                        assetState.addState(name, artifact.getLifecycleState(name));
                    }
                }
            }
        }
        return Response.ok().entity(assetState).build();
    }

    private Response modifyGovernanceAsset(String assetType, String id, DetachedGenericArtifact genericArtifact,
                                           String baseURL) throws RegistryException {
        String shortName = Util.getShortName(assetType);
        try {
            GenericArtifactManager manager = getGenericArtifactManager(shortName);
            GenericArtifact artifact = genericArtifact.makeRegistryAware(manager);
            artifact.setId(id);
            manager.updateGenericArtifact(artifact);
            URI link = new URL(Util.generateLink(assetType, id, baseURL)).toURI();
            return Response.created(link).build();
        } catch (MalformedURLException | URISyntaxException e) {
            throw new GovernanceException(e);
        }
    }

    private Response persistGovernanceAsset(String assetType, DetachedGenericArtifact genericArtifact,
                                            String baseURL) throws RegistryException {
        String shortName = Util.getShortName(assetType);
        try {
            GenericArtifactManager manager = getGenericArtifactManager(shortName);
            GenericArtifact artifact = genericArtifact.makeRegistryAware(manager);
            manager.addGenericArtifact(artifact);
            URI link = new URL(Util.generateLink(assetType, artifact.getId(), baseURL, false)).toURI();
            return Response.created(link).build();
        } catch (MalformedURLException | URISyntaxException e) {
            throw new GovernanceException(e);
        }
    }

    private Response persistGovernanceAsset(String assetType, GenericArtifactManager manager, GenericArtifact
            genericArtifact, String baseURL) throws RegistryException {
        try {
            manager.addGenericArtifact(genericArtifact);
            URI link = new URL(Util.generateLink(assetType, genericArtifact.getId(), baseURL, false)).toURI();
            return Response.created(link).build();
        } catch (MalformedURLException | URISyntaxException e) {
            throw new GovernanceException(e);
        }
    }

    private Response deleteGovernanceAsset(String assetType, String id) throws RegistryException {
        String shortName = Util.getShortName(assetType);
        GenericArtifactManager manager = getGenericArtifactManager(shortName);
        GenericArtifact artifact = getUniqueAsset(shortName, id);
        if (artifact != null) {
            manager.removeGenericArtifact(artifact.getId());
            return Response.noContent().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }


    private Response getGovernanceAssets(String assetType, UriInfo uriInfo) throws RegistryException {
        String shortName = Util.getShortName(assetType);
        if (validateAssetType(shortName)) {
            PaginationInfo pagination = Util.getPaginationInfo(uriInfo.getQueryParameters());
            String query = createQuery(uriInfo);
            pagination.setQuery(query);
            List<GenericArtifact> artifacts = getAssetList(shortName, query, pagination);
            if (artifacts.size() > 0) {
                if (artifacts.size() >= pagination.getCount()) {
                    pagination.setMorePages(true);
                }
                TypedList<GenericArtifact> typedList = new TypedList<>(GenericArtifact.class, shortName, artifacts, pagination);
                return Response.ok().entity(typedList).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } else {
            return validationFail(shortName);
        }
    }

    private Response getGovernanceEndpointAssets(String assetType, String id,
                                                 UriInfo uriInfo) throws RegistryException {
        String shortName = Util.getShortName(assetType);
        if (validateAssetType(shortName)) {
            GenericArtifact currentAsset = getUniqueAsset(shortName, id, uriInfo);
            if (currentAsset != null) {
                List<GovernanceArtifact> endpoints = new ArrayList<>();
                Association[] associations = getUserRegistry().getAssociations(currentAsset.getPath(),
                                                                               Util.ENDPOINT_ASSOCIATION_BELONG_TO);
                if (associations.length > 0) {
                    for (Association association : associations) {
                        GovernanceArtifact endpoint = GovernanceUtils.retrieveGovernanceArtifactByPath
                                (getUserRegistry(), association.getDestinationPath());
                        endpoints.add(endpoint);
                    }
                    TypedList<GovernanceArtifact> typedList = new TypedList<>(GovernanceArtifact.class, ENDPOINT,
                                                                              endpoints, null);
                    return Response.ok().entity(typedList).build();
                }

            }
            return Response.status(Response.Status.NOT_FOUND).build();
        } else {
            return validationFail(shortName);
        }
    }

    private Response getGovernanceAsset(String assetType, String id) throws RegistryException {
        String shortName = Util.getShortName(assetType);
        if (validateAssetType(shortName)) {
            GenericArtifact artifact = getUniqueAsset(shortName, id);
            if (artifact != null) {
                TypedList<GenericArtifact> typedList = new TypedList<>(GenericArtifact.class, shortName,
                                                                       Arrays.asList(artifact), null);
                return Response.ok().entity(typedList).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } else {
            return validationFail(shortName);
        }
    }

    private GenericArtifactManager getGenericArtifactManager(String shortName) throws RegistryException {
        return new GenericArtifactManager(getUserRegistry(), shortName);
    }

    private List<GenericArtifact> getAssetList(String assetType, String query, PaginationInfo pagination)
            throws RegistryException {
        Registry registry = getUserRegistry();
        GenericArtifactManager artifactManager = new GenericArtifactManager(registry, assetType);
        if (pagination != null) {
            PaginationContext.init(pagination.getStart(), pagination.getCount(), pagination.getSortOrder(),
                                   pagination.getSortBy(), pagination.getLimit());
        }
        GenericArtifact[] genericArtifacts = artifactManager.findGovernanceArtifacts(query);
        PaginationContext.destroy();
        return Arrays.asList(genericArtifacts);
    }

    private GenericArtifact getUniqueAsset(String assetType, String id) throws RegistryException {
        Registry registry = getUserRegistry();
        GenericArtifactManager artifactManager = new GenericArtifactManager(registry, assetType);
        return artifactManager.getGenericArtifact(id);
    }

    private GenericArtifact getUniqueAsset(String shortName, String id, UriInfo uriInfo) throws RegistryException {
        if (id != null) {
            return getUniqueAsset(shortName, id);
        } else {
            String query = createQuery(uriInfo);
            List<GenericArtifact> artifacts = getAssetList(shortName, query, null);
            if (artifacts.size() == 1) {
                return artifacts.get(0);
            }
        }
        return null;
    }

    private boolean validateAssetType(String assetType) throws RegistryException {
        if (assetType != null) {
            Registry registry = getUserRegistry();
            for (GovernanceArtifactConfiguration artifactConfiguration :
                    GovernanceUtils.findGovernanceArtifactConfigurations(registry)) {
                if (artifactConfiguration.getKey().equals(assetType)) {
                    return true;
                }
            }
        }
        return false;
    }

    private Response validationFail(String assetType) {
        return Response.status(Response.Status.NOT_FOUND).entity("Asset type " + assetType + " not found.").build();
    }

    public Response getGovernanceEndpoint(String id) throws RegistryException {
        String shortName = Util.getShortName(ENDPOINTS);
        if (validateAssetType(shortName)) {
            GenericArtifact artifact = getUniqueAsset(shortName, id);
            GenericArtifact belongToAsset = getBelongtoAsset(artifact);
            includeBelongToAssetInfo(artifact, belongToAsset);
            if (artifact != null) {
                TypedList<GenericArtifact> typedList = new TypedList<>(GenericArtifact.class, shortName,
                                                                       Arrays.asList(artifact), null);
                return Response.ok().entity(typedList).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } else {
            return validationFail(shortName);
        }
    }

    private void includeBelongToAssetInfo(GenericArtifact artifact, GenericArtifact belongToAsset)
            throws RegistryException {
        String belongToAssetID = getBelongToAssetID(belongToAsset);
        String belongToAssetShortName = getBelongToAssetShortName(belongToAsset);
        artifact.addAttribute(Util.TEMP_BELONG_TO_ASSET_ID, belongToAssetID);
        artifact.addAttribute(Util.TEMP_BELONG_TO_ASSET_SHORT_NAME, belongToAssetShortName);
    }

    private String getBelongToAssetShortName(GenericArtifact belongToAsset) throws RegistryException {
        String mediaType = belongToAsset.getMediaType();
        GovernanceArtifactConfiguration configuration = GovernanceUtils
                .findGovernanceArtifactConfigurationByMediaType(mediaType, getUserRegistry());
        if (configuration != null) {
            return configuration.getKey();
        }
        return null;
    }

    private String getBelongToAssetID(GenericArtifact belongToAsset) {
        return belongToAsset.getId();
    }

    private GenericArtifact getBelongtoAsset(GenericArtifact artifact) throws RegistryException {
        Association[] associations = getUserRegistry().getAssociations(artifact.getPath(),
                                                                       Util.ENDPOINT_ASSOCIATION_BELONG_TO);
        if (associations.length > 0) {
            Association association = associations[0];
            if (association != null) {
                String sourcePath = association.getSourcePath();
                GovernanceArtifact source = GovernanceUtils.retrieveGovernanceArtifactByPath(getUserRegistry(),
                                                                                             sourcePath);
                return (GenericArtifact) source;
            }
        }
        return null;
    }

}
