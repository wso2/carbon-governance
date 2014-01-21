/*
* Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.carbon.governance.client;


import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.core.common.AuthenticationException;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.common.AttributeSearchService;
import org.wso2.carbon.registry.common.ResourceData;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.pagination.PaginationContext;
import org.wso2.carbon.registry.core.pagination.PaginationUtils;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.indexing.stub.generated.ArrayOfString;
import org.wso2.carbon.registry.indexing.stub.generated.ContentSearchAdminServiceStub;
import org.wso2.carbon.registry.indexing.stub.generated.xsd.SearchResultsBean;
import org.wso2.carbon.utils.CarbonUtils;

import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This is the class used implement the search client.
 */
public class WSRegistrySearchClient {

    private static final Log log = LogFactory.getLog(WSRegistrySearchClient.class);
    private String cookie;
    private String epr;
    private ContentSearchAdminServiceStub stub;

    /**
     *
     * @param serverURL service url
     * @param username userName
     * @param password password
     * @param configContext ConfigurationContext
     * @throws RegistryException if failed to initialize the WSRegistrySearchClient.
     */
    public WSRegistrySearchClient(String serverURL, String username, String password,ConfigurationContext configContext)
            throws RegistryException {
        epr = serverURL + "ContentSearchAdminService";
        try {
            authenticate(configContext, serverURL, username, password);
            stub = new ContentSearchAdminServiceStub(configContext, epr);
            ServiceClient client = stub._getServiceClient();
            Options options = client.getOptions();
            options.setManageSession(true);
            options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
            stub._getServiceClient().getOptions().setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
            stub._getServiceClient().getOptions().setTimeOutInMilliSeconds(1000000);

        } catch (Exception axisFault) {
            String msg = "Failed to initiate WSRegistrySearchClient. " + axisFault.getMessage();
            log.error(msg, axisFault);
            throw new RegistryException(msg, axisFault);
        }
    }

    /**
     * Since user provided the cookie no need to authenticate with username and password
     * @param serverURL serverURL
     * @param cookie COOKIE_STRING
     * @throws RegistryException
     */
    public WSRegistrySearchClient(String serverURL, String cookie)throws RegistryException{
        epr = serverURL + "ContentSearchAdminService";
        setCookie(cookie);
        try{
            if (CarbonUtils.isRunningOnLocalTransportMode()) {
                stub = new ContentSearchAdminServiceStub(
                        WSRegistrySearchUtils.getConfigurationContext(), epr);
            } else {
                stub = new ContentSearchAdminServiceStub(epr);
            }
            ServiceClient client = stub._getServiceClient();
            Options options = client.getOptions();
            options.setManageSession(true);
            options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
            stub._getServiceClient().getOptions().setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
            stub._getServiceClient().getOptions().setTimeOutInMilliSeconds(1000000);
        }catch (Exception axisFault){
            String msg = "Failed to initiate WSRegistrySearchClient . " + axisFault.getMessage();
            log.error(msg, axisFault);
            throw new RegistryException(msg, axisFault);
        }
    }

    /**
     *
     * @param ctx ConfigurationContext
     * @param serverURL serverURL
     * @param username  username
     * @param password  password
     * @return  true if success the authentication
     * @throws AxisFault
     * @throws AuthenticationException
     */
    private boolean authenticate(ConfigurationContext ctx, String serverURL, String username, String password)
            throws AxisFault, AuthenticationException {
        String serviceEPR = serverURL + "AuthenticationAdmin";

        AuthenticationAdminStub stub = new AuthenticationAdminStub(ctx, serviceEPR);
        ServiceClient client = stub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        try {
            boolean result = stub.login(username, password, new URL(serviceEPR).getHost());
            if (result) {
                setCookie((String) stub._getServiceClient().getServiceContext().
                        getProperty(HTTPConstants.COOKIE_STRING));
            }
            return result;
        } catch (Exception e) {
            String msg = "Error occurred while logging in";
            throw new AuthenticationException(msg, e);
        }
    }

    /**
     * Initialize the AttributeSearchService and copy pagination context.
     * @throws RegistryException if failed to Initialize.
     */
    public void init() throws RegistryException{

        try {
            if(PaginationContext.getInstance()!=null){
                PaginationUtils.copyPaginationContext(stub._getServiceClient());
            }
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        }
        GovernanceUtils.setAttributeSearchService(new AttributeSearchService() {

            public org.wso2.carbon.registry.indexing.stub.generated.xsd.ResourceData[] resourceDatas;

            @Override
            public ResourceData[] search(UserRegistry userRegistry, Map<String, String> stringStringMap) throws RegistryException {
                throw new UnsupportedOperationException();
            }

            @Override
            public ResourceData[] search(int i, Map<String, String> stringStringMap) throws RegistryException {
                throw new UnsupportedOperationException();
            }

            @Override
            public ResourceData[] search(Map<String, String> stringStringMap) throws RegistryException {

                List<ResourceData> resourceDataList = new ArrayList<ResourceData>();
                List<ArrayOfString> arrayOfStringList = new ArrayList<ArrayOfString>();
                try {
                    for (Map.Entry<String, String> map : stringStringMap.entrySet()) {

                        ArrayOfString arrayOfString = new ArrayOfString();
                        arrayOfString.setArray(new String[]{map.getKey(), map.getValue()});
                        arrayOfStringList.add(arrayOfString);
                    }
                    SearchResultsBean searchResultsBean =
                            stub.getAttributeSearchResults(arrayOfStringList.toArray(
                                    new ArrayOfString[arrayOfStringList.size()]));
                    resourceDatas = searchResultsBean.getResourceDataList();
                    if(resourceDatas!=null && resourceDatas.length >0){
                    for (org.wso2.carbon.registry.indexing.stub.generated.xsd.ResourceData data : resourceDatas) {
                        ResourceData resourceData = new ResourceData();
                        resourceData.setAbsent(data.getAbsent());
                        resourceData.setAuthorUserName(data.getAuthorUserName());
                        resourceData.setAverageRating(data.getAverageRating());
                        resourceData.setAverageStars(data.getAverageStars());
                        resourceData.setCreatedOn(data.getCreatedOn());
                        resourceData.setDeleteAllowed(data.isDeleteAllowedSpecified());
                        resourceData.setDescription(data.getDescription());
                        resourceData.setExternalLink(data.getExternalLink());
                        resourceData.setGetAllowed(data.getGetAllowed());
                        resourceData.setLink(data.getLink());
                        resourceData.setMounted(data.isMountedSpecified());
                        resourceData.setName(data.getName());
                        resourceData.setDeleteAllowed(data.getDeleteAllowed());
                        resourceData.setResourceType(data.getResourceType());
                        resourceData.setResourcePath(data.getResourcePath());
                        resourceData.setRealPath(data.getRealPath());
                        resourceData.setPutAllowed(data.getPutAllowed());
                        resourceDataList.add(resourceData);
                    }
                    }else {
                        return new ResourceData[0];
                    }
                } catch (RemoteException e) {
                    String msg = "Failed to get results";
                    log.error("Failed to get results ",e);
                    throw new RegistryException(msg,e);
                }
                return resourceDataList.toArray(new ResourceData[resourceDataList.size()]);
            }
        });
    }
    public void setCookie(String cookie) {
        this.cookie = cookie;
    }
}
