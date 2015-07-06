/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.governance.generic.ui.clients;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.governance.generic.stub.ManageGenericArtifactServiceStub;
import org.wso2.carbon.governance.generic.stub.beans.xsd.ArtifactsBean;
import org.wso2.carbon.governance.generic.stub.beans.xsd.ContentArtifactsBean;
import org.wso2.carbon.governance.generic.stub.beans.xsd.StoragePathBean;
import org.wso2.carbon.governance.generic.ui.utils.GenericUtil;
import org.wso2.carbon.governance.generic.ui.utils.InstalledRxt;
import org.wso2.carbon.governance.generic.ui.utils.ManageGenericArtifactUtil;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.pagination.PaginationContext;
import org.wso2.carbon.registry.core.pagination.PaginationUtils;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.carbon.user.core.service.RealmService;


import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ManageGenericArtifactServiceClient {

    private static final Log log = LogFactory.getLog(ManageGenericArtifactServiceClient.class);

    private ManageGenericArtifactServiceStub stub;
    private String epr;

    private HttpSession session;

    @SuppressWarnings("unused")
    public ManageGenericArtifactServiceClient(
            String cookie, String backendServerURL, ConfigurationContext configContext)
            throws RegistryException {

        epr = backendServerURL + "ManageGenericArtifactService";

        try {
            stub = new ManageGenericArtifactServiceStub(configContext, epr);

            ServiceClient client = stub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

        } catch (AxisFault axisFault) {
            String msg = "Failed to initiate ManageGenericArtifactServiceClient. " +
                    axisFault.getMessage();
            log.error(msg, axisFault);
            throw new RegistryException(msg, axisFault);
        }
    }

    public ManageGenericArtifactServiceClient(ServletConfig config, HttpSession session)
            throws RegistryException {
        this.session = session;
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config.
                getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        epr = backendServerURL + "ManageGenericArtifactService";

        try {
            stub = new ManageGenericArtifactServiceStub(configContext, epr);

            ServiceClient client = stub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

        } catch (AxisFault axisFault) {
            String msg = "Failed to initiate ManageGenericArtifactServiceClient. " +
                    axisFault.getMessage();
            log.error(msg, axisFault);
            throw new RegistryException(msg, axisFault);
        }
    }

    public String addArtifact(String key, String info, String lifecycleAttribute) throws Exception {
        return stub.addArtifact(key, info, lifecycleAttribute);
    }

    public String editArtifact(String path, String key, String info, String lifecycleAttribute)
            throws Exception {
        return stub.editArtifact(path != null ? path : "", key, info, lifecycleAttribute);
    }

    public ArtifactsBean listArtifacts(String key, String criteria) throws Exception {
        ArtifactsBean artifactsBean;
        try {
            if (PaginationContext.getInstance() == null) {
                return stub.listArtifacts(key, criteria);
            }
            PaginationUtils.copyPaginationContext(stub._getServiceClient());
            artifactsBean = stub.listArtifacts(key, criteria);
            int rowCount = PaginationUtils.getRowCount(stub._getServiceClient());
            session.setAttribute("row_count",Integer.toString(rowCount));
        } finally {
            PaginationContext.destroy();
        }
        return artifactsBean;
    }

    public ArtifactsBean listArtifactsByName(String key, String name) throws Exception {
        ArtifactsBean artifactsBean;
        try {
            if (PaginationContext.getInstance() == null) {
                return stub.listArtifactsByName(key, name);
            }
            PaginationUtils.copyPaginationContext(stub._getServiceClient());
            artifactsBean = stub.listArtifactsByName(key, name);
            int rowCount = PaginationUtils.getRowCount(stub._getServiceClient());
            session.setAttribute("row_count", Integer.toString(rowCount));
        } finally {
            PaginationContext.destroy();
        }
        return artifactsBean;

    }

    public ArtifactsBean listArtifactsByLC(String key, String LCName, String LCState, String LCInOut, String LCStateInOut)
            throws Exception {
        ArtifactsBean artifactsBean;
        try {
            if (PaginationContext.getInstance() == null) {
                return stub.listArtifactsByLC(key, LCName, LCState, LCInOut, LCStateInOut);
            }
            PaginationUtils.copyPaginationContext(stub._getServiceClient());
            artifactsBean = stub.listArtifactsByLC(key, LCName, LCState, LCInOut, LCStateInOut);
            int rowCount = PaginationUtils.getRowCount(stub._getServiceClient());
            session.setAttribute("row_count",Integer.toString(rowCount));
        } finally {
            PaginationContext.destroy();
        }
        return artifactsBean;
    }

    public ContentArtifactsBean listContentArtifacts(String mediaType) throws Exception {
        ContentArtifactsBean contentArtifactsBean;
        try {
            if (PaginationContext.getInstance() == null) {
                return stub.listContentArtifacts(mediaType);
            }
            PaginationUtils.copyPaginationContext(stub._getServiceClient());
            contentArtifactsBean = stub.listContentArtifacts(mediaType);
            int rowCount = PaginationUtils.getRowCount(stub._getServiceClient());
            session.setAttribute("row_count", Integer.toString(rowCount));
        } finally {
            PaginationContext.destroy();
        }
        return contentArtifactsBean;
    }

    public ContentArtifactsBean listContentArtifactsByLC(String mediaType, String LCName, String LCState,
                                                         String LCInOut, String LCStateInOut) throws Exception {
        ContentArtifactsBean contentArtifactsBean;
        try {
            if (PaginationContext.getInstance() == null) {
                return stub.listContentArtifactsByLC(mediaType, LCName, LCState, LCInOut, LCStateInOut);
            }
            PaginationUtils.copyPaginationContext(stub._getServiceClient());
            contentArtifactsBean = stub.listContentArtifactsByLC(mediaType, LCName, LCState, LCInOut, LCStateInOut);
            int rowCount = PaginationUtils.getRowCount(stub._getServiceClient());
            session.setAttribute("row_count", Integer.toString(rowCount));
        } finally {
            PaginationContext.destroy();
        }
        return contentArtifactsBean;
    }

    public ContentArtifactsBean listContentArtifactsByName(String mediaType, String criteria)
            throws Exception {
        ContentArtifactsBean contentArtifactsBean;
        try {
            if (PaginationContext.getInstance() == null) {
                return stub.listContentArtifactsByName(mediaType, criteria);
            }
            PaginationUtils.copyPaginationContext(stub._getServiceClient());
            contentArtifactsBean = stub.listContentArtifactsByName(mediaType, criteria);
            int rowCount = PaginationUtils.getRowCount(stub._getServiceClient());
            session.setAttribute("row_count", Integer.toString(rowCount));
        } finally {
            PaginationContext.destroy();
        }
        return contentArtifactsBean;
    }

    public StoragePathBean getStoragePath(String key) throws Exception {
        return stub.getStoragePath(key);
    }

    public String getArtifactContent(String path) throws Exception {
        return stub.getArtifactContent(path);
    }

    public String getArtifactUIConfiguration(String key) throws Exception {
        return stub.getArtifactUIConfiguration(key);
    }

    public boolean setArtifactUIConfiguration(String key, String content) throws Exception {
        return stub.setArtifactUIConfiguration(key, content);
    }

    public boolean canChange(String path) throws Exception {
        return stub.canChange(path);
    }

    /* get available aspects */
    public String[] getAvailableAspects() throws Exception {
        return stub.getAvailableAspects();
    }

    public boolean addRXTResource(HttpServletRequest request, String config,String path)
            throws Exception {
                boolean result = false ;
                        String user = (String) session.getAttribute("logged-user");
                String tenantDomain = (String) session.getAttribute("tenantDomain");

                        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
                WSRegistryServiceClient registry = new WSRegistryServiceClient(tenantDomain, cookie);
                RealmService realmService = registry.getRegistryContext().getRealmService();

                        String configurationPath = RegistryConstants.CONFIG_REGISTRY_BASE_PATH +
                                RegistryConstants.GOVERNANCE_COMPONENT_PATH +
                                "/configuration/";
                if(realmService.getTenantUserRealm(realmService.getTenantManager().getTenantId(tenantDomain))
                                .getAuthorizationManager().isUserAuthorized(user, configurationPath, ActionConstants.PUT))
                {
                            result = stub.addRXTResource(config, path);
                    HttpSession session = request.getSession();
                    if (session != null) {
                            GenericUtil.buildMenuItems(request, getSessionParam(session, "logged-user"),
                                            getSessionParam(session, "tenantDomain"),
                                            getSessionParam(session, "ServerURL"));
                    }
                }

        return result;
    }

    private String getSessionParam(HttpSession session, String name) {
        return (String) session.getAttribute(name);
    }

    public List<InstalledRxt> getInstalledRXTs(String cookie, ServletConfig config, HttpSession session) throws Exception {
        List<InstalledRxt> listInstalledRxts = new ArrayList<InstalledRxt>();
        String user = (String) session.getAttribute("logged-user");
        String tenantDomain = (String) session.getAttribute("tenantDomain");
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);

        String adminCookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        WSRegistryServiceClient registry = new WSRegistryServiceClient(backendServerURL, adminCookie);
        RealmService realmService = registry.getRegistryContext().getRealmService();

        String configurationPath = RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                                   RegistryConstants.GOVERNANCE_COMPONENT_PATH +
                                   "/types/";
        if (realmService.getTenantUserRealm(realmService.getTenantManager().getTenantId(tenantDomain))
                        .getAuthorizationManager().isUserAuthorized(user, configurationPath, ActionConstants.GET)) {
            Collection collection = (Collection) registry.get(configurationPath);
            String[] resources = collection.getChildren();
            for (int i = 0; i < resources.length; i++) {
                if (resources[i] != null && resources[i].contains("/")) {
                    String rxt = resources[i].substring(resources[i].lastIndexOf("/") + 1).split("\\.")[0];
                    InstalledRxt rxtObj = new InstalledRxt();
                    rxtObj.setRxt(rxt);
                    if (realmService.getTenantUserRealm(realmService.getTenantManager().getTenantId(tenantDomain))
                                    .getAuthorizationManager()
                                    .isUserAuthorized(user, resources[i], ActionConstants.GET)) {
                        rxtObj.setDeleteAllowed();
                    }
                    listInstalledRxts.add(rxtObj);
                }
            }

        }
        if (listInstalledRxts.size() > 1) {
            Collections.sort(listInstalledRxts, InstalledRxt.installedRxtComparator);
        }
        return listInstalledRxts;

    }

    public String getRxtAbsPathFromRxtName(String name) throws Exception {
     return stub.getRxtAbsPathFromRxtName(name);
    }

    public String getArtifactViewRequestParams(String key) throws Exception {
        return stub.getArtifactViewRequestParams(key);
    }

    public String[] getAllLifeCycleState(String LCName) throws RemoteException {
        return stub.getAllLifeCycleState(LCName);
    }
}
