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

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.wso2.carbon.CarbonConstants;
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
import org.wso2.carbon.governance.rest.api.model.AssociationModel;
import org.wso2.carbon.governance.rest.api.model.LCState;
import org.wso2.carbon.governance.rest.api.model.TypedList;
import org.wso2.carbon.governance.rest.api.util.CommonConstants;
import org.wso2.carbon.governance.rest.api.util.Util;
import org.wso2.carbon.registry.common.ui.utils.TreeNodeBuilderUtil;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.pagination.PaginationContext;
import org.wso2.carbon.registry.core.secure.AuthorizationFailedException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.resource.services.utils.AddResourceUtil;
import org.wso2.carbon.registry.resource.services.utils.CommonUtil;
import org.wso2.carbon.registry.resource.services.utils.GetTextContentUtil;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
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
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
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


@Api(value = "/governance",
             description = "governance Rest api for doing operations on a asset",
             produces = MediaType.APPLICATION_JSON)
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
    public static final String CONTENT_DISPOSITION = "Content-Disposition";

    private final Log log = LogFactory.getLog(Asset.class);

    private GovernanceConfiguration governanceConfiguration;

    @GET
    @Path("/types")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get supported asset list",
            httpMethod = "GET",
            notes = "Fetch available asset type list")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Found the available asset types and returned in body")})
    public Response getTypes() throws RegistryException {
        return getAssetTypes();
    }

    @GET
    @Path("{assetType : [a-zA-Z][a-zA-Z_0-9]*}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get list of assets for provided asset type",
            httpMethod = "GET",
            notes = "Fetch list of assets for provided asset type")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Found list of assets for provided asset type and returned in body"),
            @ApiResponse(code = 404, message = "Given specific asset type not found")})
    public Response getAssets(@PathParam("assetType") String assetType, @Context UriInfo uriInfo,
                              @HeaderParam("X_TENANT") String tenant)
            throws RegistryException {
        return getGovernanceAssets(assetType, uriInfo, tenant);
    }

    @GET
    @Path("{assetType : [a-zA-Z][a-zA-Z_0-9]*}/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get asset for provided asset type and ID",
            httpMethod = "GET",
            notes = "Fetch asset for provided asset type and ID")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Found the asset for provided asset type and ID, then returned in body"),
            @ApiResponse(code = 404, message = "Given ID is not found in specific asset type")})
    public Response getAsset(@PathParam("assetType") String assetType, @PathParam("id") String id)
            throws RegistryException {
        return getGovernanceAsset(assetType, id);
    }

    @GET
    @Path("{assetType : [a-zA-Z][a-zA-Z_0-9]*}/{id}/content")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get content of the provided asset type and ID",
            httpMethod = "GET",
            notes = "Fetch asset for provided asset type and ID")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Found the asset for provided asset type and ID, then returned in body"),
            @ApiResponse(code = 404, message = "Given ID is not found in specific asset type")})
    public Response getAssetRawContent(@PathParam("assetType") String assetType, @PathParam("id") String id)
            throws RegistryException {
        return getRawContentOfGovernanceAsset(assetType, id);
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
    @ApiOperation(value = "Get endpoints of an asset instance",
            httpMethod = "GET",
            notes = "Fetch endpoints of an asset instance")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Found the asset for provided asset type and ID, then returned endpoints if any in body"),
            @ApiResponse(code = 404, message = "Given ID is not found in specific asset type")})
    public Response getAssetEndpoints(@PathParam("assetType") String assetType, @PathParam("id") String id,
                                      @Context UriInfo uriInfo) throws RegistryException {
        return getGovernanceEndpointAssets(assetType, id, uriInfo);
    }

    @POST
    @Path("{assetType : [a-zA-Z][a-zA-Z_0-9]*}")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Add an asset to registry",
            httpMethod = "POST",
            notes = "Add an asset to registry")
    @ApiResponses(value = {@ApiResponse(code = 201, message = "Asset added successfully"),
            @ApiResponse(code = 500, message = "Internal server error occurred")})
    public Response createAsset(@PathParam("assetType") String assetType, GenericArtifact genericArtifact,
                                @Context UriInfo uriInfo) throws RegistryException {
        return createGovernanceAsset(assetType, (DetachedGenericArtifact) genericArtifact, Util.getBaseURL(uriInfo));
    }


    @PUT
    @Path("{assetType : [a-zA-Z][a-zA-Z_0-9]*}/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Update an already added asset",
            httpMethod = "PUT",
            notes = "Update an already added asset")
    @ApiResponses(value = {@ApiResponse(code = 201, message = "Asset updated successfully"),
            @ApiResponse(code = 500, message = "Internal server error occurred")})
    public Response modifyAsset(@PathParam("assetType") String assetType, @PathParam("id") String id,
                                GenericArtifact genericArtifact, @Context UriInfo uriInfo) throws RegistryException {
        return modifyGovernanceAsset(assetType, id, (DetachedGenericArtifact) genericArtifact, Util.getBaseURL(uriInfo));
    }


    @DELETE
    @Path("{assetType : [a-zA-Z][a-zA-Z_0-9]*}/{id}")
    @ApiOperation(value = "Delete an artifact",
            httpMethod = "DELETE",
            notes = "Delete an asset")
    @ApiResponses(value = {@ApiResponse(code = 204, message = "Asset deleted successfully"),
            @ApiResponse(code = 404, message = "Specified asset type or ID not found")})
    public Response deleteAsset(@PathParam("assetType") String assetType, @PathParam("id") String id)
            throws RegistryException {
        return deleteGovernanceAsset(assetType, id);
    }

    @GET
    @Path("{assetType : [a-zA-Z][a-zA-Z_0-9]*}/{id}/states")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get all the current LC states or specified lifecycle state of the asset.",
            httpMethod = "GET",
            notes = "Fetch all the current LC states or specified lifecycle state of the asset.")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Found all the current LC states or specified lifecycle state of the asset.")})
    public Response getAssetStates(@PathParam("assetType") String assetType, @PathParam("id") String id,
                                   @Context UriInfo uriInfo) throws RegistryException {
        String lc = uriInfo.getQueryParameters().getFirst("lc");
        return getGovernanceAssetStates(assetType, id, lc);
    }

    @PUT
    @Path("{assetType : [a-zA-Z][a-zA-Z_0-9]*}/{id}/states")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Update lifecycle state of an asset",
            httpMethod = "PUT",
            notes = "Update lifecycle state of an asset")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Asset updated successfully"),
            @ApiResponse(code = 404, message = "Specified asset not found")})
    public Response updateLCState(@PathParam("assetType") String assetType, @PathParam("id") String id,
                                  AssetStateChange stateChange,
                                  @Context UriInfo uriInfo) throws RegistryException {
        return updateLCState(assetType, id, stateChange);
    }

    //---- Endpoint REST API  -----------------------

    @GET
    @Path("/endpoints")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get all endpoints",
            httpMethod = "GET",
            notes = "Fetch all endpoints")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Found list of endpoints and returned in body"),
            @ApiResponse(code = 404, message = "Specified resource not found")})
    public Response getEndpoints(@Context UriInfo uriInfo, @HeaderParam("X_TENANT") String tenant) throws RegistryException {
        return getGovernanceAssets(ENDPOINTS, uriInfo, tenant);
    }

    @GET
    @Path("/endpoints/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get the endpoint of provided ID",
            httpMethod = "GET",
            notes = "Fetch the endpoint of provided ID")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Found the endpoint of the provided ID and returned in body"),
            @ApiResponse(code = 404, message = "Given ID is not found in endpoints")})
    public Response getEndpoint(@PathParam("id") String id) throws RegistryException {
        return getGovernanceEndpoint(id);
    }


    @POST
    @Path("/endpoints")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Add an endpoint to registry",
            httpMethod = "POST",
            notes = "Add an endpoint to registry")
    @ApiResponses(value = {@ApiResponse(code = 201, message = "Endpoint added successfully"),
            @ApiResponse(code = 404, message = "Specified resource not found"),
            @ApiResponse(code = 500, message = "Internal server error occurred")})
    public Response createEndpoint(GenericArtifact genericArtifact, @Context UriInfo uriInfo)
            throws RegistryException {
        return createGovernanceAsset(ENDPOINTS, (DetachedGenericArtifact) genericArtifact, Util.getBaseURL(uriInfo));
    }

    @POST
    @Path("/endpoints/{assetType : [a-zA-Z][a-zA-Z_0-9]*}")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Add an endpoint with association",
            httpMethod = "POST",
            notes = "Add an endpoint with association")
    @ApiResponses(value = {@ApiResponse(code = 201, message = "Endpoint added with associated successfully"),
            @ApiResponse(code = 404, message = "Specified resource not found")})
    public Response createEndpoint(@PathParam("assetType") String assetType,
                                   GenericArtifact genericArtifact, @Context UriInfo uriInfo) throws RegistryException {
        return createEndpointWithAssociation(assetType, null, uriInfo, genericArtifact);
    }


    @POST
    @Path("/endpoints/{assetType : [a-zA-Z][a-zA-Z_0-9]*}/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Add an endpoint to registry and associate it with provided ID",
            httpMethod = "POST",
            notes = "Add an endpoint to registry and associate it with provided ID")
    @ApiResponses(value = {@ApiResponse(code = 201, message = "Endpoint added and associated with the provided asset ID successfully"),
            @ApiResponse(code = 404, message = "Given asset ID is not found")})
    public Response createEndpoint(@PathParam("assetType") String assetType, @PathParam("id") String id,
                                   GenericArtifact genericArtifact, @Context UriInfo uriInfo) throws RegistryException {
        return createEndpointWithAssociation(assetType, id, uriInfo, genericArtifact);
    }

    @PUT
    @Path("/endpoints/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Update an already added endpoint",
            httpMethod = "PUT",
            notes = "Update an already added endpoint")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "Specified api endpoint not implemented")})
    public Response modifyEndpoint(@PathParam("id") String id,
                                   GenericArtifact genericArtifact, @Context UriInfo uriInfo) throws RegistryException {
        //TODO - IMO it's incorrect to allow endpoint edit instead use create/delete through REST API WDYT ?
//        return modifyGovernanceAsset(ENDPOINTS, id, (DetachedGenericArtifact) genericArtifact, Util.getBaseURL(uriInfo));
         return Response.status(Response.Status.NOT_FOUND).build();
    }


    @DELETE
    @Path("/endpoints/{id}")
    @ApiOperation(value = "Delete an endpoint",
            httpMethod = "DELETE",
            notes = "Delete an endpoint")
    @ApiResponses(value = {@ApiResponse(code = 204, message = "Asset deleted successfully"),
            @ApiResponse(code = 404, message = "Specified ID not found")})
    public Response deleteEndpoint(@PathParam("id") String id) throws RegistryException {
        return deleteGovernanceAsset(ENDPOINTS, id);
    }

    @GET
    @Path("/endpoints/{id}/states")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get the endpoints lifecycle and it's state information",
            httpMethod = "GET",
            notes = "Fetch the endpoints lifecycle and it's state information")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Found the endpoint of the provided ID and returned in body"),
            @ApiResponse(code = 404, message = "Given ID is not found in endpoints")})
    public Response getEndpointStates(@PathParam("id") String id,
                                      @Context UriInfo uriInfo) throws RegistryException {
        String lc = uriInfo.getQueryParameters().getFirst("lc");
        return getEndpointStates(id, lc);
    }

    @POST
    @Path("/endpoints/activate/{id}")
    @ApiOperation(value = "Add endpoint LC state as activate in the provided endpoint ID",
            httpMethod = "POST",
            notes = "Add endpoint LC state as activate in the provided endpoint ID")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Endpoint activated successfully"),
            @ApiResponse(code = 404, message = "Given endpoint ID is not found")})
    public Response endpointActivate(@PathParam("id") String id,
                                     @Context UriInfo uriInfo) throws RegistryException {
        return endpointActivate(id);
    }

    @POST
    @Path("/endpoints/deactivate/{id}")
    @ApiOperation(value = "Add endpoint LC state as deactivate in the provided endpoint ID",
            httpMethod = "POST",
            notes = "Add endpoint LC state as deactivate in the provided endpoint ID")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Endpoint deactivated successfully"),
            @ApiResponse(code = 404, message = "Given endpoint ID is not found")})
    public Response endpointDeactivate(@PathParam("id") String id,
                                       @Context UriInfo uriInfo) throws RegistryException {
        return endpointDeactivate(id);
    }


    @GET
    @Path("{assetType : [a-zA-Z][a-zA-Z_0-9]*}/{id}/associations")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get all the assets which is associated with provided ID",
            httpMethod = "GET",
            notes = "Fetch all the assets which is associated with provided ID")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Found all the assets which is associated with provided ID and returned in body"),
            @ApiResponse(code = 404, message = "Given ID is not found in endpoints")})
    public Response getAssociations(@PathParam("assetType") String assetType, @PathParam("id") String id,
            @Context UriInfo uriInfo) throws RegistryException {
        return getGovernanceAssetAssociation(assetType, id, uriInfo);
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
            if (value != null && !"tenant".equals(entry.getKey())) {
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
        if (artifact != null) {
            // lc == null means user look for all LCs
            AssetState assetState = new AssetState();
            if (lcName != null) {
                LCState lCState = getAssetState(artifact, lcName);
                assetState.setLcState(lCState);
                return Response.ok().entity(assetState).build();

            } else {
                String[] stateNames = artifact.getLifecycleNames();
                if (stateNames != null) {
                    List<LCState> list = new ArrayList<>();
                    for (String name : stateNames) {
                        LCState lCState = getAssetState(artifact, name);
                        list.add(lCState);

                    }
                    assetState.setLcStates(list);
                    return Response.ok().entity(assetState).build();
                }
            }
        }
        return Response.ok().entity(null).build();
    }

    private LCState getAssetState(GenericArtifact artifact, String name) throws RegistryException {
        UserRegistry userRegistry = (UserRegistry) getUserRegistry();
        Resource resource = userRegistry.get(artifact.getPath());
        String artifactLCState = resource.getProperty("registry.lifecycle." + name + ".state");
        String[]  aspects = userRegistry.getAspectActions(artifact.getPath(), name);
        LCState LCState = new LCState(artifactLCState, name);
        for (String action:aspects  ) {
            LCState.addActions(action);
        }
        return LCState;
    }

    private Response modifyGovernanceAsset(String assetType, String id, DetachedGenericArtifact genericArtifact,
                                           String baseURL) throws RegistryException {
        String shortName = Util.getShortName(assetType);
        try {
            GenericArtifactManager manager = getGenericArtifactManager(shortName);
            GenericArtifact artifact = genericArtifact.makeRegistryAware(manager);
            artifact.setId(id);
            manager.updateGenericArtifact(artifact);
            //Use 'generateLink' method with four parameters.
            //Fix for REGISTRY-3129
            URI link = new URL(Util.generateLink(assetType, id, baseURL, false)).toURI();
            return Response.created(link).build();
        } catch (MalformedURLException | URISyntaxException e) {
            throw new GovernanceException(e);
        }
    }

    private Response createGovernanceAsset(String assetType, DetachedGenericArtifact genericArtifact,
                                           String baseURL) throws RegistryException {
        String shortName = Util.getShortName(assetType);
        if (validateAssetType(shortName)){

            if (isContentType(shortName, genericArtifact)) {
                String[][] propertyArray = new String[2][2];
                propertyArray[0][0] = CommonConstants.VERSION;
                propertyArray[0][1] = genericArtifact.getAttribute(CommonConstants.OVERVIEW_VERSION);
                propertyArray[1][0] = CommonConstants.RESOURCE_SOURCE;
                propertyArray[1][1] = CommonConstants.GOVERNANCE_API;

                Registry registry = getUserRegistry();

                // overview_path(storage path) is not mandatory for known asset types such as wsdl, wadl, swagger,etc...
                // For unknown asset types '/_system/governance/' will be appended by the system
                boolean isSuccessful = importResourceWithRegistry(registry,
                        genericArtifact.getAttribute(CommonConstants.OVERVIEW_PATH) + genericArtifact
                                .getAttribute(CommonConstants.OVERVIEW_VERSION),
                        genericArtifact.getAttribute(CommonConstants.OVERVIEW_NAME),
                        genericArtifact.getAttribute(CommonConstants.OVERVIEW_TYPE), null,
                        genericArtifact.getAttribute(CommonConstants.OVERVIEW_URL), null, propertyArray);
                if (isSuccessful) {
                    return Response.status(Response.Status.CREATED).build();
                } else {
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                }
            } else {
                try {
                    GenericArtifactManager manager = getGenericArtifactManager(shortName);
                    GenericArtifact artifact = genericArtifact.makeRegistryAware(manager);
                    manager.addGenericArtifact(artifact);
                    URI link = new URL(Util.generateLink(assetType, artifact.getId(), baseURL, false)).toURI();
                    return Response.created(link).build();
                } catch (MalformedURLException | URISyntaxException e) {
                    throw new GovernanceException(e);
                } catch (GovernanceException e) {
                    if (e.getMessage().contains("already exists at")) {
                        String message = e.getMessage().split("\\.", 2)[1].replaceAll("^\\s+", "");
                        return Response.status(Response.Status.CONFLICT).entity(message).build();
                    }
                }
            }
        }
        return validationFail(shortName);
    }

    /**
     * Adding content type rxt resource to registry.
     *
     * @param registry          registry instance.
     * @param parentPath        storage path if have any.
     * @param resourceName      user defend resource name.
     * @param mediaType         registry media type.
     * @param description       resource description if have any.
     * @param fetchURL          resource URL.
     * @param symlinkLocation   symlink location if have any.
     * @param properties        resource properties.
     * @return isSuccessful     is successful or not.
     * @throws RegistryException
     */
    private boolean importResourceWithRegistry(Registry registry, String parentPath, String resourceName,
            String mediaType, String description, String fetchURL, String symlinkLocation, String[][] properties)
            throws RegistryException {
        if (RegistryUtils.isRegistryReadOnly(registry.getRegistryContext())) {
            return false;
        }

        // Fix for file importation security verification - FileSystemImportationSecurityHotFixTestCase
        if (StringUtils.isNotBlank(fetchURL) && fetchURL.toLowerCase().startsWith("file:")) {
            String msg = "The source URL must not be file in the server's local file system";
            throw new RegistryException(msg);
        }

        // Adding Source URL as property to end of the properties array.
        String[][] newProperties = CommonUtil.setProperties(properties, "sourceURL", fetchURL);

        // Data is directed to below AddResourceUtil.addResource from ImportResourceUtil.importResource
        // Hence resource upload path will now go through put.
        try {
            AddResourceUtil.addResource(CommonUtil.calculatePath(parentPath, resourceName), mediaType, description,
                    GetTextContentUtil.getByteContent(fetchURL), symlinkLocation, registry, newProperties);
        } catch (Exception e) {
            throw new GovernanceException("Error occurred while adding the resource.", e);
        }
        return true;
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


    private Response getGovernanceAssets(String assetType, UriInfo uriInfo, String tenantHeader)
            throws RegistryException {
        String shortName = Util.getShortName(assetType);
        if (validateAssetType(shortName)) {
            PaginationInfo pagination = Util.getPaginationInfo(uriInfo.getQueryParameters(), tenantHeader);
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

                // Check whether artifact is actually does not exists or we are getting null because of anonymous user.
            } else if (isAnonymousUser()) {
                return handleStatusCode(id);

            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } else {
            return validationFail(shortName);
        }
    }

    private Response handleStatusCode(String id) throws RegistryException {
        String artifactPath = GovernanceUtils.getArtifactPath(getUserRegistry(), id);
        try {
            getUserRegistry().get(artifactPath);
        } catch (AuthorizationFailedException e) {
            return Response.status(401).header("WWW-Authenticate", "Basic  Realm=\"WSO2-Registry\"").build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    private boolean isAnonymousUser() {
        return CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME.equals
                (CarbonContext.getThreadLocalCarbonContext().getUsername());
    }

    private Response getRawContentOfGovernanceAsset(String assetType, String id) throws RegistryException {
        String shortName = Util.getShortName(assetType);
        if (validateAssetType(shortName)) {
            GenericArtifact artifact = getUniqueAsset(shortName, id);
            if (artifact != null) {
                Resource resource = getUserRegistry().get(artifact.getPath());
                String mediaType = getMediaTypeForDownloading(artifact.getMediaType());
                if ("wsdl".equals(shortName) || "wadl".equals(shortName) || "schema".equals(shortName) || "policy".equals(shortName)){
                    return Response.ok(changeContent(resource.getContentStream(), getUserRegistry(), artifact.getPath())).type(mediaType).
                            header(CONTENT_DISPOSITION, "filename=" + getFileName(shortName, artifact)).build();
                } else {
                    return Response.ok(resource.getContentStream()).type(mediaType).
                            header(CONTENT_DISPOSITION, "filename=" + getFileName(shortName, artifact)).build();
                }
            // Check whether artifact is actually does not exists or we are getting null because of anonymous user.
            } else if (isAnonymousUser()) {
                return handleStatusCode(id);

            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } else {
            return validationFail(shortName);
        }
    }

    private String getMediaTypeForDownloading(String mediaType) {
        return mediaType.substring(0, mediaType.indexOf('/') + 1) + mediaType.substring(mediaType.indexOf('+') + 1);
    }

    private String getFileName(String shortName, GenericArtifact artifact) throws RegistryException {
        if (isContentType(shortName)) {
            return artifact.getPath().substring(artifact.getPath().lastIndexOf('/') + 1);
        } else {
            GovernanceArtifactConfiguration config = GovernanceUtils.
                    findGovernanceArtifactConfiguration(shortName, getUserRegistry());
            return artifact.getAttribute(config.getArtifactNameAttribute());
        }
    }

    public String changeContent(InputStream content, Registry registry, String currentPath) throws GovernanceException {
        String myString = null;
        List<String> references = new ArrayList<>();
        try {
            myString = IOUtils.toString(content, "UTF-8");
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // Use the factory to create a builder
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(myString));
            Document doc = builder.parse(is);
            NodeList list = doc.getElementsByTagName("*");
            for (int i = 0; i < list.getLength(); i++) {
                Element element = (Element) list.item(i);
                if (element.hasAttribute("schemaLocation")) {
                    references.add(element.getAttribute("schemaLocation"));
                }
            }
            CarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            for (String links : references) {
                String link = RegistryUtils.getAbsolutePath(registry.getRegistryContext(), links);
                String registryUrl = TreeNodeBuilderUtil.calculateAbsolutePath(currentPath, link);
                if (registry.resourceExists(registryUrl)) {
                    GovernanceArtifact artifact = GovernanceUtils.retrieveGovernanceArtifactByPath(registry, registryUrl);
                    //Here we are calculating relative path to governance rest api
                    String newLink = "./../../" + Util.getResourceName(getBelongToAssetShortName(artifact)) + "/" +
                            artifact.getId() + "/content?tenant=" + carbonContext.getTenantDomain();
                    myString = myString.replace(links, newLink);
                }
            }

        } catch (ParserConfigurationException | SAXException | IOException  e) {
            //ignore exception due to swagger and other types
        } catch (RegistryException e) {
            log.warn("Error occurred while reading resource from the regsitry ");
        }
        return myString;
    }

    private boolean isContentType(String shortName) throws RegistryException {
        GovernanceArtifactConfiguration config = GovernanceUtils.findGovernanceArtifactConfiguration(shortName,
                                                                                                     getUserRegistry());
        if (config.getExtension() != null) {
            return true;
        } else {
            return false;
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
                GovernanceArtifact belongToAsset = getBelongtoAsset(artifact);
                if (belongToAsset != null) {
                    includeBelongToAssetInfo(artifact, belongToAsset);
                }
                if (artifact != null) {
                    TypedList<GenericArtifact> typedList = new TypedList<>(GenericArtifact.class, shortName,
                                                                           Arrays.asList(artifact), null);
                    return Response.ok().entity(typedList).build();// Check whether artifact is actually does not exists or we are getting null because of anonymous user.

                    // Check whether artifact is actually does not exists or we are getting null because of anonymous user.
                } else if (isAnonymousUser()) {
                    return handleStatusCode(id);
                }
            }
            return Response.status(Response.Status.NOT_FOUND).build();

        } else {
            return validationFail(shortName);
        }
    }

    private void includeBelongToAssetInfo(GenericArtifact artifact, GovernanceArtifact belongToAsset)
            throws RegistryException {
        String belongToAssetID = getBelongToAssetID(belongToAsset);
        String belongToAssetShortName = getBelongToAssetShortName(belongToAsset);
        artifact.addAttribute(Util.TEMP_BELONG_TO_ASSET_ID, belongToAssetID);
        artifact.addAttribute(Util.TEMP_BELONG_TO_ASSET_SHORT_NAME, belongToAssetShortName);
    }

    private String getBelongToAssetShortName(GovernanceArtifact belongToAsset) throws RegistryException {
        String mediaType = belongToAsset.getMediaType();
        GovernanceArtifactConfiguration configuration = GovernanceUtils
                .findGovernanceArtifactConfigurationByMediaType(mediaType, getUserRegistry());
        if (configuration != null) {
            return configuration.getKey();
        }
        return null;
    }

    private String getBelongToAssetID(GovernanceArtifact belongToAsset) {
        return belongToAsset.getId();
    }

    private GovernanceArtifact getBelongtoAsset(GovernanceArtifact artifact) throws RegistryException {
        Association[] associations = getUserRegistry().getAssociations(artifact.getPath(),
                                                                       Util.ENDPOINT_ASSOCIATION_BELONG_TO);
        if (associations.length > 0) {
            Association association = associations[0];
            if (association != null) {
                String sourcePath = association.getSourcePath();
                GovernanceArtifact source = GovernanceUtils.retrieveGovernanceArtifactByPath(getUserRegistry(),
                                                                                             sourcePath);
                return (GovernanceArtifact) source;
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

    /**
     * This method is used to get all associations.
     *
     * @param assetType association type
     * @param id        artifact id
     * @param uriInfo   uri information
     * @return          all associations.
     * @throws RegistryException
     */
    private Response getGovernanceAssetAssociation(String assetType, String id, UriInfo uriInfo)
            throws RegistryException {
        String shortName = Util.getShortName(assetType);
        if (validateAssetType(shortName)) {
            GenericArtifact currentAsset = getUniqueAsset(shortName, id, uriInfo);
            if (currentAsset != null) {
                List<AssociationModel> associationsList = new ArrayList<>();
                Association[] associations = getUserRegistry()
                        .getAllAssociations(currentAsset.getPath());
                if (associations.length > 0) {
                    for (org.wso2.carbon.registry.core.Association association : associations) {

                        String artifactPath = association.getDestinationPath();

                        GovernanceArtifact artifact = GovernanceUtils.retrieveGovernanceArtifactByPath
                                (getUserRegistry(), artifactPath);

                        String artifactShortName = GovernanceUtils.getArtifactConfigurationByMediaType
                                (getUserRegistry(), artifact.getMediaType()).getKey();

                        String baseURI = Util.getBaseURL(uriInfo);
                        String selfLink = Util.generateLink(artifactShortName, artifact.getId(), baseURI);

                        AssociationModel associationModel = new AssociationModel();
                        associationModel.setAssociationType(association.getAssociationType());
                        associationModel.setAssociationPath(association.getDestinationPath());
                        associationModel.setAssociationArtifactType(artifactShortName);
                        associationModel.setSelfLink(selfLink);
                        associationsList.add(associationModel);
                    }

                    return Response.ok().entity(associationsList).build();
                }
            }
            return Response.status(Response.Status.NOT_FOUND).build();
        } else {
            return validationFail(shortName);
        }
    }
}
