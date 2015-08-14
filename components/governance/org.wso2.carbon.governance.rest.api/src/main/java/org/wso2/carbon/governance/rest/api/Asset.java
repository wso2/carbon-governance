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
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.DetachedGenericArtifact;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceArtifactConfiguration;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.rest.api.model.AssetState;
import org.wso2.carbon.governance.rest.api.model.LCStateChange;
import org.wso2.carbon.governance.rest.api.model.TypedList;
import org.wso2.carbon.governance.rest.api.util.RESTUtil;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//TODO - test this
//@RolesAllowed("GOV-REST")
public class Asset {

    public static final String MAXID_QUERY_PARAM = "maxid";
    public static final String COUNT_QUERY_PARAM = "count";
    public static final String ENDPOINTS = "endpoints";
    private final Log log = LogFactory.getLog(Asset.class);

    @GET
    @Path("/types")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTypes() {
        return getAssetTypes();
    }

    @GET
    @Path("{assetType : [a-zA-Z][a-zA-Z_0-9]*}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAssets(@PathParam("assetType") String assetType, @Context UriInfo uriInfo) {
        String query = createQuery(uriInfo);
        return getGovernanceAssets(assetType, query);
    }

    @GET
    @Path("{assetType : [a-zA-Z][a-zA-Z_0-9]*}/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAsset(@PathParam("assetType") String assetType, @PathParam("id") String id) {
        return getGovernanceAsset(assetType, id);
    }

    @POST
    @Path("{assetType : [a-zA-Z][a-zA-Z_0-9]*}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createAsset(@PathParam("assetType") String assetType, GenericArtifact genericArtifact,
                                @Context UriInfo uriInfo) {
        return persistGovernanceAsset(assetType, (DetachedGenericArtifact) genericArtifact, RESTUtil.getBaseURL(uriInfo));
    }



    @PUT
    @Path("{assetType : [a-zA-Z][a-zA-Z_0-9]*}/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response modifyAsset(@PathParam("assetType") String assetType, @PathParam("id") String id,
                                GenericArtifact genericArtifact, @Context UriInfo uriInfo) {
        return modifyGovernanceAsset(assetType, id, (DetachedGenericArtifact) genericArtifact, RESTUtil.getBaseURL(uriInfo));
    }


    @DELETE
    @Path("{assetType : [a-zA-Z][a-zA-Z_0-9]*}/{id}")
    public Response deleteAsset(@PathParam("assetType") String assetType, @PathParam("id") String id) {
        return deleteGovernanceAsset(assetType, id);
    }


    @GET
    @Path("/endpoints")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEndpoints(@Context UriInfo uriInfo) {
        String query = createQuery(uriInfo);
        return getGovernanceAssets(ENDPOINTS, query);
    }

    @GET
    @Path("/endpoints/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEndpoint(@PathParam("id") String id) {
        return getGovernanceAsset("endpoints", id);
    }


    @POST
    @Path("/endpoints")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createEndpoints(GenericArtifact genericArtifact, @Context UriInfo uriInfo) {
        return persistGovernanceAsset(ENDPOINTS, (DetachedGenericArtifact) genericArtifact, RESTUtil.getBaseURL(uriInfo));
    }

    @PUT
    @Path("endpoints/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response modifyEndpoint(@PathParam("id") String id,
                                   GenericArtifact genericArtifact, @Context UriInfo uriInfo) {
        return modifyGovernanceAsset(ENDPOINTS, id, (DetachedGenericArtifact) genericArtifact, RESTUtil.getBaseURL(uriInfo));
    }


    @DELETE
    @Path("{endpoints/{id}")
    public Response deleteEndpoint(@PathParam("id") String id) {
        return deleteGovernanceAsset("endpoints", id);
    }

    @GET
    @Path("{endpoint/{id}/states")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEndpointStates(@PathParam("id") String id,
                                      @Context UriInfo uriInfo) {
        String lc = uriInfo.getQueryParameters().getFirst("lc");
        return getGovernanceAssetStates(ENDPOINTS, id, lc);
    }

    @GET
    @Path("{assetType : [a-zA-Z][a-zA-Z_0-9]*}/{id}/states")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAssetStates(@PathParam("assetType") String assetType, @PathParam("id") String id,
                                   @Context UriInfo uriInfo) {
        String lc = uriInfo.getQueryParameters().getFirst("lc");
        return getGovernanceAssetStates(assetType, id, lc);
    }


    @PUT
    @Path("{assetType : [a-zA-Z][a-zA-Z_0-9]*}/{id}/states")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateLCState(@PathParam("assetType") String assetType, @PathParam("id") String id,
                                  LCStateChange stateChange,
                                  @Context UriInfo uriInfo) {
        String lc = uriInfo.getQueryParameters().getFirst("lc");
        return updateLCState(assetType, id, stateChange);
    }


    private Response getAssetTypes() {
        List<String> shortNames = new ArrayList<>();
        try {
            List<GovernanceArtifactConfiguration> configurations = GovernanceUtils.findGovernanceArtifactConfigurations
                    (getUserRegistry());
            for (GovernanceArtifactConfiguration configuration : configurations) {
                shortNames.add(configuration.getSingularLabel());
            }
            return Response.ok().entity(shortNames).build();
        } catch (RegistryException e) {
            log.error(e);
            return Response.serverError().build();
        }
    }


    private String createQuery(UriInfo uriInfo) {
        String requestURI = uriInfo.getRequestUri().toString();
        String path = uriInfo.getAbsolutePath().toString();
        if (requestURI.length() > path.length()) {
            return requestURI.substring(path.length() + 1);
        }
        return "";
    }

    private Response searchGovernanceAssets(String query) throws RegistryException {
        return searchGovernanceAssets("", query);
    }

    private Response searchGovernanceAssets(String assetType, String query)
            throws RegistryException {
           List<GenericArtifact> artifacts = findGovernanceArtifacts(query, assetType);
           if (artifacts.size() > 0) {
               TypedList<GenericArtifact> typedList = new TypedList<>(GenericArtifact.class, assetType, artifacts);
               return Response.ok().entity(typedList).build();
           } else {
               return Response.status(Response.Status.NOT_FOUND).build();
           }
    }

    private List<GenericArtifact> findGovernanceArtifacts(String query, String assetType) throws RegistryException {
        List<GovernanceArtifact> governanceArtifacts = GovernanceUtils.findGovernanceArtifacts(query,
                                                                                               getUserRegistry(), assetType);
        if (governanceArtifacts.size() > 0) {
            List<GenericArtifact> genericArtifacts = new ArrayList<>();
            for (GovernanceArtifact artifact : governanceArtifacts) {
                genericArtifacts.add((GenericArtifact) artifact);
            }
            return genericArtifacts;
        }
        return Collections.emptyList();
    }


    private Response updateLCState(String assetType, String id, LCStateChange stateChange) {
        try {
            String shortName = getShortName(assetType);
            GenericArtifactManager manager = new GenericArtifactManager(getUserRegistry(), shortName);
            GenericArtifact artifact = manager.getGenericArtifact(id);
            if (artifact != null) {
                //TODO - change to Gov level
                getUserRegistry().invokeAspect(artifact.getPath(), stateChange.getLifecycle(),
                                               stateChange.getAction(), stateChange.getParameters());
                return getGovernanceAssetStates(artifact, null);
            }
        } catch (RegistryException e) {
            log.error(e);
        }
        return Response.ok().build();
    }


    private Response getGovernanceAssetStates(String assetType, String id, String lcName) {
        String shortName = getShortName(assetType);
        try {
            GenericArtifactManager manager = new GenericArtifactManager(getUserRegistry(), shortName);
            GenericArtifact artifact = manager.getGenericArtifact(id);
            return getGovernanceAssetStates(artifact, lcName);
        } catch (RegistryException e) {
            log.error(e);
        }
        return null;
    }

    private Response getGovernanceAssetStates(GenericArtifact artifact, String lcName) {
        try {
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
        } catch (RegistryException e) {
            log.error(e);
        }
        return null;
    }

    private Response modifyGovernanceAsset(String assetType, String id, DetachedGenericArtifact genericArtifact,
                                           String baseURL) {
        String shortName = getShortName(assetType);
        try {
            GenericArtifactManager manager = getGenericArtifactManager(shortName);
            GenericArtifact artifact = genericArtifact.makeRegistryAware(manager);
            artifact.setId(id);
            manager.updateGenericArtifact(artifact);
            URI link = new URL(RESTUtil.generateLink(assetType, id, baseURL)).toURI();
            return Response.created(link).build();
        } catch (RegistryException e) {
            e.printStackTrace();
            return Response.serverError().build();

        } catch (MalformedURLException | URISyntaxException e) {
            log.error(e);
            return Response.created(null).build();

        }
    }

    private Response persistGovernanceAsset(String assetType, DetachedGenericArtifact genericArtifact, String baseURL) {
        String shortName = getShortName(assetType);
        try {
            GenericArtifactManager manager = getGenericArtifactManager(shortName);
            GenericArtifact artifact = genericArtifact.makeRegistryAware(manager);
            manager.addGenericArtifact(artifact);
            URI link = new URL(RESTUtil.generateLink(assetType, artifact.getId(), baseURL, false)).toURI();
            return Response.created(link).build();
        } catch (RegistryException | MalformedURLException | URISyntaxException e) {
            log.error(e);
            return Response.serverError().build();
        }
    }

    private Response deleteGovernanceAsset(String assetType, String id) {
        String shortName = getShortName(assetType);
        try {
            GenericArtifactManager manager = getGenericArtifactManager(shortName);
            GenericArtifact artifact = getUniqueAsset(shortName, id);
            if (artifact != null) {
                manager.removeGenericArtifact(artifact.getId());
                return Response.noContent().build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (RegistryException e) {
            log.error(e);
            return Response.created(null).build();
        }


    }


    public Response getGovernanceAssets(String assetType, String query) {
        String shortName = getShortName(assetType);
        if (validateAssetType(shortName)) {
            List<GenericArtifact> artifacts = getAssetList(shortName, query);
            if (artifacts.size() > 0) {
                TypedList<GenericArtifact> typedList = new TypedList<>(GenericArtifact.class, shortName, artifacts);
                return Response.ok().entity(typedList).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } else {
            return validationFail(shortName);
        }
    }

    public Response getGovernanceAsset(String assetType, String id) {
        String shortName = getShortName(assetType);
        if (validateAssetType(shortName)) {
            GenericArtifact artifact = getUniqueAsset(shortName, id);
            if (artifact != null) {
                TypedList<GenericArtifact> typedList = new TypedList<>(GenericArtifact.class, shortName, Arrays.asList(artifact));
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

    private int getMaxid(MultivaluedMap<String, String> queryParams) {
        if (queryParams.get(MAXID_QUERY_PARAM) != null) {
            String maxid = queryParams.get(MAXID_QUERY_PARAM).get(0);
            if (maxid != null || !"".equals(MAXID_QUERY_PARAM)) {
                return Integer.valueOf(MAXID_QUERY_PARAM);
            }
        }
        return 0;
    }

    private int getCount(MultivaluedMap<String, String> queryParams) {
        if (queryParams.get(COUNT_QUERY_PARAM) != null) {
            String count = queryParams.get(COUNT_QUERY_PARAM).get(0);
            if (count != null || !"".equals(COUNT_QUERY_PARAM)) {
                return Integer.valueOf(COUNT_QUERY_PARAM);
            }
        }
        return 20;
    }

    private String getShortName(String assetType) {
        return assetType.substring(0, assetType.length() - 1);
    }


    private List<GenericArtifact> getAssetList(String assetType, String query) {
        List<GenericArtifact> artifacts = new ArrayList<>();
        try {
            Registry registry = getUserRegistry();
            GenericArtifactManager artifactManager = new GenericArtifactManager(registry, assetType);
            GenericArtifact[] genericArtifacts = artifactManager.findGovernanceArtifacts(query);
            artifacts = Arrays.asList(genericArtifacts);
        } catch (RegistryException e) {
            log.error(e);
        }
        return artifacts;
    }

    private Map<String, List<String>> createCriteria(MultivaluedMap<String, String> queryParams) {
        Map<String, List<String>> criteria = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : queryParams.entrySet()) {
            String key = formatKey(entry.getKey());
            for (String value : entry.getValue()) {
                if (value != null && !"".equals(value)) {
                    criteria.put(key, formatValue(value));
                }
            }
        }
        return criteria;
    }

    private List<String> formatValue(String value) {
        List<String> values = new ArrayList<>();
        values.addAll(Arrays.asList(value.split(",")));
        return values;
    }

    private String formatKey(String key) {
        if (key.indexOf("_") == -1) {
            //Assume this is belong to "overview_"
            return "overview_" + key;
        }
        return key;
    }

    private GenericArtifact getUniqueAsset(String assetType, String id) {
        try {
            Registry registry = getUserRegistry();
            GenericArtifactManager artifactManager = new GenericArtifactManager(registry, assetType);
            return artifactManager.getGenericArtifact(id);
        } catch (RegistryException e) {
            log.error(e);
        }
        return null;
    }

    private boolean validateAssetType(String assetType) {
        if (assetType != null) {
            try {
                Registry registry = getUserRegistry();
                for (GovernanceArtifactConfiguration artifactConfiguration :
                        GovernanceUtils.findGovernanceArtifactConfigurations(registry)) {
                    if (artifactConfiguration.getKey().equals(assetType)) {
                        return true;
                    }
                }
            } catch (RegistryException e) {
                log.error(e);
            }
        }
        return false;
    }

    protected Registry getUserRegistry() throws RegistryException {
        CarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        RegistryService registryService = (RegistryService) carbonContext.
                getOSGiService(RegistryService.class, null);
        return registryService.getGovernanceUserRegistry(carbonContext.getUsername(), carbonContext.getTenantId());

    }

    private Response validationFail(String assetType) {
        return Response.status(Response.Status.NOT_FOUND).entity("Asset type " + assetType + " not found.").build();
    }

}
