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
import org.wso2.carbon.governance.common.GovernanceConfiguration;
import org.wso2.carbon.governance.common.GovernanceConfigurationService;
import org.wso2.carbon.governance.rest.api.internal.PaginationInfo;
import org.wso2.carbon.governance.rest.api.model.AssetState;
import org.wso2.carbon.governance.rest.api.model.AssetStateChange;
import org.wso2.carbon.governance.rest.api.model.TypedList;
import org.wso2.carbon.governance.rest.api.util.Util;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


//TODO - test this
//@RolesAllowed("GOV-REST")
public class Asset {


    public static final String ENDPOINTS = "endpoints";
    public static final String ENDPOINT = "endpoint";
    public static final String ENDPOINT_LIFE_CYCLE = "EndpointLifeCycle";
    public static final String ENDPOINT_LIFE_CYCLE_ACTION_DEACTIVATE = "Deactivate";
    public static final String ENDPOINT_LIFE_CYCLE_ACTION_ACTIVATE = "Activate";
    public static final String ENDPOINT_LIFE_CYCLE_STATE_ACTIVE = "Active";
    public static final String ENDPOINT_MEDIA_TYPE = "application/vnd.wso2-endpoint+xml";
    public static final String CONTENT_TYPE_WSDL = "wsdl";
    public static final String CONTENT_TYPE_WADL = "wadl";
    public static final String CONTENT_TYPE_SWAGGER = "swagger";
    public static final String CONTENT_TYPE_SCHEMA = "schema";
    public static final String CONTENT_TYPE_POLICY = "policy";
    public static final String ATTR_CONTENT_TYPE = "content_type";

    private final Log log = LogFactory.getLog(Asset.class);

    private GovernanceConfiguration governanceConfiguration;

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
        //TODO - Implement special logic to Content-Type Artifacts, e,g - for WSDL return WSDL content not attributes.
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
        return createGovernanceAsset(assetType, (DetachedGenericArtifact) genericArtifact, Util.getBaseURL(uriInfo));
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

    //---- Endpoint REST API  -----------------------

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
        return createGovernanceAsset(ENDPOINTS, (DetachedGenericArtifact) genericArtifact, Util.getBaseURL(uriInfo));
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
    @Path("/endpoints/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response modifyEndpoint(@PathParam("id") String id,
                                   GenericArtifact genericArtifact, @Context UriInfo uriInfo) throws RegistryException {
        //TODO - IMO it's incorrect to allow endpoint edit instead use create/delete through REST API WDYT ?
//        return modifyGovernanceAsset(ENDPOINTS, id, (DetachedGenericArtifact) genericArtifact, Util.getBaseURL(uriInfo));
         return Response.status(Response.Status.NOT_FOUND).build();
    }


    @DELETE
    @Path("/endpoints/{id}")
    public Response deleteEndpoint(@PathParam("id") String id) throws RegistryException {
        return deleteGovernanceAsset("endpoints", id);
    }

    @GET
    @Path("/endpoints/{id}/states")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEndpointStates(@PathParam("id") String id,
                                      @Context UriInfo uriInfo) throws RegistryException {
        String lc = uriInfo.getQueryParameters().getFirst("lc");
        return getEndpointStates(id, lc);
    }

    @POST
    @Path("/endpoints/activate/{id}")
    public Response endpointActivate(@PathParam("id") String id,
                                     @Context UriInfo uriInfo) throws RegistryException {
        return endpointActivate(id);
    }

    @POST
    @Path("/endpoints/deactivate/{id}")
    public Response endpointDeactivate(@PathParam("id") String id,
                                       @Context UriInfo uriInfo) throws RegistryException {
        return endpointDeactivate(id);
    }




    protected Registry getUserRegistry() throws RegistryException {
        CarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        RegistryService registryService = (RegistryService) carbonContext.
                getOSGiService(RegistryService.class, null);
        return registryService.getGovernanceUserRegistry(carbonContext.getUsername(), carbonContext.getTenantId());

    }

    private Response getEndpointStates(String id, String lc) throws RegistryException {
        GenericArtifactManager manager = new GenericArtifactManager(getUserRegistry(), ENDPOINT);
        GenericArtifact artifact = manager.getGenericArtifact(id);
        if (artifact != null) {
            String defaultState = artifact.getLifecycleState(ENDPOINT_LIFE_CYCLE);
            if (defaultState != null && ENDPOINT_LIFE_CYCLE_STATE_ACTIVE.equals(defaultState)) {
                runEndpointStateManagementJob(artifact);
            }
            return getGovernanceAssetStates(artifact, lc);
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    private Response endpointActivate(String id) throws RegistryException {
        GenericArtifact artifact = getUniqueAsset(ENDPOINT, id);
        if(artifact != null){
            String currentState = artifact.getLifecycleState(ENDPOINT_LIFE_CYCLE);
            //TODO - If there is a better way to update last update time only then change this line as it's very costly.
            if ("Active".equals(currentState)) {
                getUserRegistry().invokeAspect(artifact.getPath(), ENDPOINT_LIFE_CYCLE,
                                               ENDPOINT_LIFE_CYCLE_ACTION_DEACTIVATE, Collections.<String, String>emptyMap());
            }
            getUserRegistry().invokeAspect(artifact.getPath(), ENDPOINT_LIFE_CYCLE,
                                           ENDPOINT_LIFE_CYCLE_ACTION_ACTIVATE, Collections.<String, String>emptyMap());
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    private Response endpointDeactivate(String id) throws RegistryException {
        GenericArtifact artifact = getUniqueAsset(ENDPOINT, id);
        if(artifact != null){
            String currentState = artifact.getLifecycleState(ENDPOINT_LIFE_CYCLE);
            if ("Active".equals(currentState)) {
                getUserRegistry().invokeAspect(artifact.getPath(), ENDPOINT_LIFE_CYCLE,
                                               ENDPOINT_LIFE_CYCLE_ACTION_DEACTIVATE, Collections.<String, String>emptyMap());
            }
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

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
        if(validateAssetType(shortName)){
            GenericArtifactManager manager = new GenericArtifactManager(getUserRegistry(), shortName);
            GenericArtifact artifact = manager.getGenericArtifact(id);
            if (artifact != null) {
                //TODO - change to Gov level
                if(stateChange.getParameters().size() > 0) {
                    getUserRegistry().invokeAspect(artifact.getPath(), stateChange.getLifecycle(), "itemClick", stateChange.getParameters());
                }
                getUserRegistry().invokeAspect(artifact.getPath(), stateChange.getLifecycle(),
                                               stateChange.getAction(), stateChange.getParameters());
                return getGovernanceAssetStates(artifact, null);
            }
            return Response.ok().build();
        }
        return validationFail(shortName);
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

    private Response createGovernanceAsset(String assetType, DetachedGenericArtifact genericArtifact,
                                           String baseURL) throws RegistryException {
        String shortName = Util.getShortName(assetType);
        if (validateAssetType(shortName)){

            if(isContentType(shortName , genericArtifact)){
                //TODO
                Response.status(Response.Status.CONFLICT).build();
                //createContentAsset(shortName, genericArtifact);
            } else {
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
        }
        return validationFail(shortName);
    }

    private void createContentAsset(String shortName, DetachedGenericArtifact genericArtifact)
            throws GovernanceException {
       //TODO
    }

    private boolean isContentType(String shortName, DetachedGenericArtifact genericArtifact)
            throws GovernanceException {
        String name = shortName.toLowerCase();
        if (shortName != null && !shortName.isEmpty()) {
            if (CONTENT_TYPE_WSDL.equals(name) || CONTENT_TYPE_WADL.equals(name) || CONTENT_TYPE_SWAGGER.equals(name) ||
                CONTENT_TYPE_SCHEMA.equals(name) || CONTENT_TYPE_POLICY.equals(name)) {
                return true;
            } else {
                String contentType = genericArtifact.getAttribute(ATTR_CONTENT_TYPE);
                if (contentType != null && contentType.toLowerCase().equals("true")) {
                    return true;
                }
            }
        }
        return false;
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
        if (validateAssetType(shortName)) {
            GenericArtifactManager manager = getGenericArtifactManager(shortName);
            GenericArtifact artifact = getUniqueAsset(shortName, id);
            if (artifact != null) {
                manager.removeGenericArtifact(artifact.getId());
                return Response.noContent().build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        }
        return validationFail(shortName);
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
                        GovernanceArtifact artifact = GovernanceUtils.retrieveGovernanceArtifactByPath
                                (getUserRegistry(), association.getDestinationPath());
                        if(ENDPOINT_MEDIA_TYPE.equals(artifact.getMediaType())) {
                            endpoints.add(artifact);
                        }
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

    private Response getGovernanceEndpoint(String id) throws RegistryException {
        String shortName = Util.getShortName(ENDPOINTS);
        if (validateAssetType(shortName)) {
            GenericArtifact artifact = getUniqueAsset(shortName, id);
            if (artifact != null) {
                GenericArtifact belongToAsset = getBelongtoAsset(artifact);
                if (belongToAsset != null) {
                    includeBelongToAssetInfo(artifact, belongToAsset);
                }
                if (artifact != null) {
                    TypedList<GenericArtifact> typedList = new TypedList<>(GenericArtifact.class, shortName,
                                                                           Arrays.asList(artifact), null);
                    return Response.ok().entity(typedList).build();
                }
            }
            return Response.status(Response.Status.NOT_FOUND).build();

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

    private void runEndpointStateManagementJob(GenericArtifact artifact) throws RegistryException {
        GovernanceConfiguration configuration = getGovernanceConfiguration();
        if (isEndpointStateManagementEnabled(configuration)) {
            long defaultEndpointActiveTime = getDefaultEndpointActiveTime(configuration);
            long currentActiveDuration = getCurrentActiveDuration(artifact);
            if (currentActiveDuration > defaultEndpointActiveTime) {
                //make endpoint inactive
                getUserRegistry().invokeAspect(artifact.getPath(), ENDPOINT_LIFE_CYCLE,
                                               ENDPOINT_LIFE_CYCLE_ACTION_DEACTIVATE,
                                               Collections.<String, String>emptyMap());
            }
        }
    }

    private long getCurrentActiveDuration(GenericArtifact artifact) throws RegistryException {
        /*
         TODO -
         Following last modified time based duration calculation is not accurate instead use one of following
         approaches.

          1. Register getLifeCycleManagementService as a OSGi service and get current duration.
          2. In case 1. is not efficient use new Cache foe endpoint mgt.
         */
        Resource resource = getUserRegistry().get(artifact.getPath());
        long lastUpdateMS = resource.getLastModified().getTime();
        long currentMS = System.currentTimeMillis();
        long durationSeconds = TimeUnit.MILLISECONDS.toSeconds(currentMS - lastUpdateMS);
        return durationSeconds;
    }

    private long getDefaultEndpointActiveTime(GovernanceConfiguration configuration) {
        return configuration.getDefaultEndpointActiveDuration();
    }

    private boolean isEndpointStateManagementEnabled(GovernanceConfiguration configuration) {
        return configuration.isEndpointStateManagementEnabled();
    }

    private GovernanceConfiguration getGovernanceConfiguration() {
        if (governanceConfiguration == null) {
            GovernanceConfigurationService service = (GovernanceConfigurationService) PrivilegedCarbonContext.
                    getThreadLocalCarbonContext().getOSGiService(GovernanceConfigurationService.class, null);
            governanceConfiguration = service.getGovernanceConfiguration();
        }
        return governanceConfiguration;
    }

}
