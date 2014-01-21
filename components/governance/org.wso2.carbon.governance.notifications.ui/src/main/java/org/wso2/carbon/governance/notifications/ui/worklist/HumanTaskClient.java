/*
 *  Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.governance.notifications.ui.worklist;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.Stub;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.databinding.types.URI;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.governance.notifications.worklist.stub.WorkListServiceStub;
import org.wso2.carbon.humantask.stub.ui.task.client.api.*;
import org.wso2.carbon.humantask.stub.ui.task.client.api.types.*;
import org.wso2.carbon.registry.common.eventing.WorkListConfig;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.user.mgt.stub.*;
import org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;

public class HumanTaskClient {

    private static final Log log = LogFactory.getLog(HumanTaskClient.class);

    private HumanTaskClientAPIAdminStub htStub;
    private UserAdminStub umStub;
    private WorkListServiceStub wlStub;

    private static WorkListConfig workListConfig = new WorkListConfig();

    public HumanTaskClient(ServletConfig config, HttpSession session) throws AxisFault {
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(
                        CarbonConstants.CONFIGURATION_CONTEXT);
        String backendServerURL =
                workListConfig.getServerURL() != null ? workListConfig.getServerURL() :
                CarbonUIUtil.getServerURL(config.getServletContext(), session);

        htStub = new HumanTaskClientAPIAdminStub(configContext, backendServerURL + "HumanTaskClientAPIAdmin");
        configureServiceClient(htStub, session);

        umStub = new UserAdminStub(configContext, backendServerURL + "UserAdmin");
        configureServiceClient(umStub, session);

        wlStub = new WorkListServiceStub(configContext, backendServerURL + "WorkListService");
        configureServiceClient(wlStub, session);
    }

    private void configureServiceClient(Stub stub, HttpSession session) {
        ServiceClient client;Options options;
        client = stub._getServiceClient();
        options = client.getOptions();
        if (workListConfig.getUsername() != null
                && workListConfig.getPassword() != null && workListConfig.isRemote()) {
            CarbonUtils.setBasicAccessSecurityHeaders(workListConfig.getUsername(),
                    workListConfig.getPassword(), client);
        } else {
            options.setProperty(HTTPConstants.COOKIE_STRING,
                    session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE));
        }
        options.setManageSession(true);
    }

    public WorkItem[] getWorkItems(HttpServletRequest request)
            throws IllegalArgumentFault, IllegalStateFault, IllegalAccessFault, RemoteException {
        if (!CarbonUIUtil.isUserAuthorized(request,
                "/permission/admin/manage/resources/notifications")) {
            return new WorkItem[0];
        }
        TSimpleQueryInput queryInput = new TSimpleQueryInput();
        queryInput.setPageNumber(0);
        queryInput.setSimpleQueryCategory(TSimpleQueryCategory.ASSIGNED_TO_ME);

        TTaskSimpleQueryResultSet resultSet = htStub.simpleQuery(queryInput);
        if (resultSet == null || resultSet.getRow() == null || resultSet.getRow().length == 0) {
            return new WorkItem[0];
        }
        List<WorkItem> workItems = new LinkedList<WorkItem>();
        for (TTaskSimpleQueryResultRow row : resultSet.getRow()) {
            URI id = row.getId();
            workItems.add(new WorkItem(id, row.getPresentationSubject(),
                    row.getPresentationName(), row.getPriority(), row.getStatus(),
                    row.getCreatedTime(), htStub.loadTask(id).getActualOwner().getTUser()));
        }
        return workItems.toArray(new WorkItem[workItems.size()]);
    }

    private static class RoleDetails {
        private FlaggedName[] roleNames;
        private String everyoneRole;

        private RoleDetails(FlaggedName[] roleNames, String everyoneRole) {
            this.roleNames = roleNames;
            this.everyoneRole = everyoneRole;
        }

        public FlaggedName[] getRoleNames() {
            return roleNames;
        }

        public String getEveryoneRole() {
            return everyoneRole;
        }
    }

    public String[] getRoles(HttpSession session) throws RemoteException, 
							UserAdminUserAdminException {            

        FlaggedName[] allRolesNames;
        String everyOneRole;
        Object roleDetails = null;
        if (session!= null) {
            roleDetails = session.getAttribute("roleDetails");
        }
        if (roleDetails != null) {
            allRolesNames = ((RoleDetails)roleDetails).getRoleNames();
            everyOneRole = ((RoleDetails)roleDetails).getEveryoneRole();
        } else {
            allRolesNames = umStub.getRolesOfCurrentUser();
            String adminRole = umStub.getUserRealmInfo().getAdminRole();

            for (FlaggedName role : allRolesNames) {
                String name = role.getItemName();
                if (name.equals(adminRole)) {
                    allRolesNames = umStub.getAllRolesNames("*", -1);
                    break;
                }
            }
            everyOneRole = umStub.getUserRealmInfo().getEveryOneRole();
            if (session!= null) {
                session.setAttribute("roleDetails", new RoleDetails(allRolesNames, everyOneRole));
            }
        }

        List<String> roles = new LinkedList<String>();
        for (FlaggedName role : allRolesNames) {
            String name = role.getItemName();
            if (!name.equals(CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME) && !name.equals(
                    everyOneRole)) {
                roles.add(name);
            }
        }
        return roles.toArray(new String[roles.size()]);
    }

    public void createTask(String role, String description, String priority)
            throws RemoteException {
        wlStub.addTask(role, description, Integer.parseInt(priority));
    }

    public void completeTask(String id)
            throws RemoteException, IllegalArgumentFault, IllegalOperationFault, IllegalAccessFault,
            IllegalStateFault {
        try {
            htStub.start(new URI(id));
            htStub.complete(new URI(id), "<WorkResponse>true</WorkResponse>");
        } catch (URI.MalformedURIException e) {
            log.error("Invalid task identifier", e);
        }
    }

}
