<!--
 ~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~ WSO2 Inc. licenses this file to you under the Apache License,
 ~ Version 2.0 (the "License"); you may not use this file except
 ~ in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~    http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing,
 ~ software distributed under the License is distributed on an
 ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~ KIND, either express or implied.  See the License for the
 ~ specific language governing permissions and limitations
 ~ under the License.
 -->
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="carbon" uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.governance.generic.ui.clients.ManageGenericArtifactServiceClient" %>
<%@ page import="org.wso2.carbon.governance.generic.ui.utils.InstalledRxt" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.governance.generic.ui.clients.ManageGenericArtifactServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.governance.generic.ui.utils.InstalledRxt" %>
<%@ page import="java.util.List" %>
<%@ page import="org.wso2.carbon.registry.core.RegistryConstants" %>
<%@ page import="org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient" %>
<%@ page import="org.wso2.carbon.user.core.service.RealmService" %>
<%@ page import="org.wso2.carbon.registry.core.ActionConstants" %>


<link type="text/css" rel="stylesheet" href="css/menu.css"/>
<link type="text/css" rel="stylesheet" href="css/style.css"/>

<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<script type="text/javascript" src="js/generic.js"></script>

<carbon:jsi18n
		resourceBundle="org.wso2.carbon.governance.generic.ui.i18n.JSResources"
		request="<%=request%>" namespace="org.wso2.carbon.governance.generic.ui"/>
<%
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    String[] rxts = null;
    List<InstalledRxt> installedRxts = null;
    ManageGenericArtifactServiceClient client;
     boolean isUserAuthorized = CarbonUIUtil.isUserAuthorized(request,
             "/permission/admin/configure/governance/manage-rxt");
    try {
           client = new ManageGenericArtifactServiceClient(config,session);
           installedRxts = client.getInstalledRXTs(cookie,config,session);

    } catch (Exception e){
        response.setStatus(500);
        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
        session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
        <jsp:include page="../admin/error.jsp"/>
<%
        return;
    }
%>
<fmt:bundle basename="org.wso2.carbon.governance.generic.ui.i18n.Resources">
<carbon:breadcrumb
        label="governance.generic.menu"
        resourceBundle="org.wso2.carbon.governance.generic.ui.i18n.Resources"
        topPage="true"
        request="<%=request%>"/>
<div id="middle">

      <h2>
         <fmt:message key="governance.generic.menu"/>
      </h2>
      <div id="workArea">
<%
    if (installedRxts == null || installedRxts.size() == 0) {
%>
        <div class="registryWriteOperation">
            <fmt:message
                    key="no.generic.artifacts.are.currently.defined.click.add.artifacts.to.upload.new.artifact"/>
        </div>
        <div class="registryNonWriteOperation">
            <fmt:message
                    key="no.artifacts.currently.defined"/>
        </div>
<%
    } else {
%>
          <table class="styledLeft" cellspacing="1" id="genericTable">
              <thead>
                  <tr>
                      <th>
                          <fmt:message key="name"/>
                      </th>
                      <th>
                          <fmt:message key="actions"/>
                      </th>
                  </tr>
              </thead>
              <tbody>

            <%
                for(InstalledRxt installedRxt:installedRxts) {
                String rxt = installedRxt.getRxt();
                String rxtPath = client.getRxtAbsPathFromRxtName(rxt);
            %>
                  <tr>
                      <td>
                          <%=rxt%>
                      </td>
                      <td>
                        <% if(!isUserAuthorized ) { %>
                          <a class="icon-link" style="background-image: url(../admin/images/edit.gif);" href="source_artifact.jsp?view=true&path=<%=rxtPath%>"><fmt:message key="view"/></a>
                          <a class="icon-link" style="background-image:url(../generic/images/delete-desable.gif);color:#aaa !important;cursor:default;"><fmt:message key="delete"/></a>
                          <%}else { %>
                          <a class="icon-link" style="background-image: url(../admin/images/edit.gif);" href="source_artifact.jsp?path=<%=rxtPath%>&rxtName=<%=rxt%>"><fmt:message key="view.edit"/></a>
                          <%if(installedRxt.isDeleteAllowed()) { %>
                            <a href="#" onclick="deleteArtifact('<%=rxtPath%>','/','../generic/generic_artifact.jsp?region=region3&item=governance_generic_menu')" class="icon-link registryWriteOperation" style="background-image:url(../admin/images/delete.gif);"><fmt:message key="delete"/></a>
                          <% } else { %>
                          <a class="icon-link" style="background-image:url(../generic/images/delete-desable.gif);color:#aaa !important;cursor:default;"><fmt:message key="delete"/></a>
                          <% } } %>
                      </td>

                  </tr>
          <%
              }
          %>
              </tbody>
          </table>
<%
    }
%>
          <script type="text/javascript">
              alternateTableRows('genericTable', 'tableEvenRow', 'tableOddRow');
          </script>

          <div class="registryWriteOperation" style="height:25px;">
                            <%
                                    String user = (String) session.getAttribute("logged-user");
                                    String tenantDomain = (String) session.getAttribute("tenantDomain");

                                            WSRegistryServiceClient registry = new WSRegistryServiceClient(tenantDomain, cookie);
                                    RealmService realmService = registry.getRegistryContext().getRealmService();

                                            String configurationPath = RegistryConstants.CONFIG_REGISTRY_BASE_PATH +
                                                RegistryConstants.GOVERNANCE_COMPONENT_PATH +
                                                "/configuration/";
                                    if(realmService.getTenantUserRealm(realmService.getTenantManager().getTenantId(tenantDomain))
                                                .getAuthorizationManager().isUserAuthorized(user, configurationPath, ActionConstants.PUT))
                                {
                            %>
                              <a class="icon-link" style="background-image: url(../admin/images/add.gif);" href="source_artifact.jsp"><fmt:message key="add.new.artifact"/></a>
                            <% } %>
          </div>
      </div>


  </div>
</fmt:bundle>