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

import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//TODO - test this
//@RolesAllowed("GOV-REST")
public class Asset {


    @GET
    @Path("{assetType : [a-zA-Z][a-zA-Z_0-9]*}")
    public Response getAssets(@PathParam("assetType") String assetType, @Context UriInfo uriInfo) {
        MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
        return getGovernanceAssets(assetType, queryParams);
    }

    @GET
    @Path("{assetType : [a-zA-Z][a-zA-Z_0-9]*}/{id}")
    public Response getAsset(@PathParam("assetType") String assetType, @PathParam("id") String id) {
        return getGovernanceAsset(assetType, id);
    }

    @POST
    @Path("{assetType : [a-zA-Z][a-zA-Z_0-9]*}")
    @Consumes("application/json")
    public Response createAsset(@PathParam("assetType") String assetType, GenericArtifact genericArtifact,
                                @Context UriInfo uriInfo) {
        return persistGovernanceAsset(assetType, (DetachedGenericArtifact) genericArtifact, RESTUtil.getBaseURL(uriInfo));
    }

    @PUT
    @Path("{assetType : [a-zA-Z][a-zA-Z_0-9]*}/{id}")
    @Consumes("application/json")
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
    public Response getEndpoints(@Context UriInfo uriInfo) {
        MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
        return getGovernanceAssets("endpoints", queryParams);
    }

    @GET
    @Path("/endpoints/{id}")
    public Response getEndpoint(@PathParam("id") String id) {
        return getGovernanceAsset("endpoints", id);
    }


    @POST
    @Path("/endpoints")
    public Response createEndpoints(GenericArtifact genericArtifact, @Context UriInfo uriInfo) {
        return persistGovernanceAsset("endpoints", (DetachedGenericArtifact) genericArtifact, RESTUtil.getBaseURL(uriInfo));
    }

    @PUT
    @Path("endpoints/{id}")
    @Consumes("application/json")
    public Response modifyEndpoint(@PathParam("id") String id,
                                   GenericArtifact genericArtifact, @Context UriInfo uriInfo) {
        return modifyGovernanceAsset("endpoints", id, (DetachedGenericArtifact) genericArtifact, RESTUtil.getBaseURL(uriInfo));
    }


    @DELETE
    @Path("{endpoints/{id}")
    public Response deleteEndpoint(@PathParam("id") String id) {
        return deleteGovernanceAsset("endpoints", id);
    }

    @GET
    @Path("{endpoint/{id}/states")
    public Response getEndpointStates(@PathParam("id") String id,
                                      @Context UriInfo uriInfo) {
        String lc = uriInfo.getQueryParameters().getFirst("lc");
        return getGovernanceAssetStates("endpoints", id, lc);
    }

    @GET
    @Path("{assetType : [a-zA-Z][a-zA-Z_0-9]*}/{id}/states")
    public Response getAssetStates(@PathParam("assetType") String assetType, @PathParam("id") String id,
                                   @Context UriInfo uriInfo) {
        String lc = uriInfo.getQueryParameters().getFirst("lc");
        return getGovernanceAssetStates(assetType, id, lc);
    }


    @PUT
    @Path("{assetType : [a-zA-Z][a-zA-Z_0-9]*}/{id}/states")
    public Response updateLCState(@PathParam("assetType") String assetType, @PathParam("id") String id,
                                  LCStateChange stateChange,
                                  @Context UriInfo uriInfo) {
        String lc = uriInfo.getQueryParameters().getFirst("lc");
        return updateLCState(assetType, id, stateChange);
    }


    private Response updateLCState(String assetType, String id, LCStateChange stateChange) {
        try {
            String shortName = getShortName(assetType);
            GenericArtifactManager manager = new GenericArtifactManager(getUserRegistry(), shortName);
            GenericArtifact artifact = manager.getGenericArtifact(id);
            if (artifact != null) {
                getUserRegistry().invokeAspect(artifact.getPath(), stateChange.getLifecycle(),
                                               stateChange.getAction(), stateChange.getParameters());
                return getGovernanceAssetStates(assetType, artifact, null);
            }
        } catch (RegistryException e) {
            e.printStackTrace();
        }
        return Response.ok().build();
    }


    private Response getGovernanceAssetStates(String assetType, String id, String lcName) {
        String shortName = getShortName(assetType);
        try {
            GenericArtifactManager manager = new GenericArtifactManager(getUserRegistry(), shortName);
            GenericArtifact artifact = manager.getGenericArtifact(id);
            return getGovernanceAssetStates(assetType, artifact, lcName);
        } catch (RegistryException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Response getGovernanceAssetStates(String assetType, GenericArtifact artifact, String lcName) {
        String shortName = getShortName(assetType);
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
                        if (stateNames.length == 1) {
                            assetState = new AssetState(artifact.getLifecycleState(stateNames[0]));
                        } else if (stateNames.length > 0) {
                            assetState = new AssetState();
                            for (String name : stateNames) {
                                assetState.addState(name, artifact.getLifecycleState(name));
                            }
                        }
                    }
                }
            }
            return Response.ok().entity(assetState).build();
        } catch (RegistryException e) {
            e.printStackTrace();
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
            e.printStackTrace();
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
        } catch (RegistryException e) {
            e.printStackTrace();
            return Response.serverError().build();

        } catch (MalformedURLException | URISyntaxException e) {
            e.printStackTrace();
            return Response.created(null).build();

        }
    }

    private Response deleteGovernanceAsset(String assetType, String id) {
        String shortName = getShortName(assetType);
        try {
            GenericArtifactManager manager = getGenericArtifactManager(shortName);
            //TODO - check and remove following line
            GenericArtifact artifact = getUniqueAsset(shortName, id);
            if (artifact != null) {
                manager.removeGenericArtifact(artifact.getId());
                return Response.noContent().build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (RegistryException e) {
            return Response.created(null).build();
        }


    }


    public Response getGovernanceAssets(String assetType, String name, String version,
                                        MultivaluedMap<String, String> queryParams) {
        String shortName = getShortName(assetType);
        if (validateAssetType(shortName)) {
            List<GenericArtifact> artifacts = getAssetList(shortName, queryParams);
            TypedList<GenericArtifact> typedList = new TypedList<>(GenericArtifact.class, shortName, artifacts);
            return Response.ok().entity(typedList).build();
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

    public Response getGovernanceAssets(String assetType, MultivaluedMap<String, String> queryParams) {
        return getGovernanceAssets(assetType, null, null, queryParams);
    }


    public Response getGovernanceAssets(String assetType, String name, MultivaluedMap<String, String> queryParams) {
        return getGovernanceAssets(assetType, name, null, queryParams);
    }

    private int getMaxid(MultivaluedMap<String, String> queryParams) {
        if (queryParams.get("maxid") != null) {
            String maxid = queryParams.get("maxid").get(0);
            if (maxid != null || !"".equals(maxid)) {
                return Integer.valueOf(maxid);
            }
        }
        return 0;
    }

    private int getCount(MultivaluedMap<String, String> queryParams) {
        if (queryParams.get("count") != null) {
            String maxid = queryParams.get("count").get(0);
            if (maxid != null || !"".equals(maxid)) {
                return Integer.valueOf(maxid);
            }
        }
        return 20;
    }

    private String getShortName(String assetType) {
        return assetType.substring(0, assetType.length() - 1);
    }


    private List<GenericArtifact> getAssetList(String assetType, MultivaluedMap<String, String> queryParams) {
        List<GenericArtifact> artifacts = new ArrayList<>();
        try {
            Registry registry = getUserRegistry();
            GenericArtifactManager artifactManager = new GenericArtifactManager(registry, assetType);
            Map<String, List<String>> criteria = createCriteria(queryParams);
            GenericArtifact[] genericArtifacts = artifactManager.findGenericArtifacts(criteria);
            artifacts = Arrays.asList(genericArtifacts);
        } catch (RegistryException e) {
            e.printStackTrace();
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
            e.printStackTrace();
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
                e.printStackTrace();
            }

        }
        return false;
    }

    protected Registry getUserRegistry() throws RegistryException {
        CarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        RegistryService registryService = (RegistryService) carbonContext.
                getOSGiService(RegistryService.class);
        return registryService.getGovernanceUserRegistry(carbonContext.getUsername(), carbonContext.getTenantId());

    }

    private Response validationFail(String assetType) {
        return Response.status(Response.Status.NOT_FOUND).entity("Asset type " + assetType + " not found.").build();
    }

}
