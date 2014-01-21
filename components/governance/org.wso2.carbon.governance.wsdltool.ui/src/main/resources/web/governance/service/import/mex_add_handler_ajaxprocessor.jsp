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
<%@ page import="org.wso2.carbon.registry.common.ui.UIException" %>
<%@ page import="org.wso2.carbon.governance.wsdltool.ui.utils.AddWSDLToolUtil" %>
<%@ page import="org.wso2.carbon.registry.core.RegistryConstants" %>
<%@ page import="org.wso2.carbon.registry.profiles.ui.clients.ProfilesAdminServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="java.net.URLEncoder" %>

<%
    String parentPath = request.getParameter("parentPath");
    try {
        parentPath = URLEncoder.encode(parentPath, "UTF-8");
    } catch (Exception ignore) {}
    String username = request.getParameter("ownerName");
    String error = "There's no user profile with the given owner name";
    try {
        username = RegistryConstants.PROFILES_PATH + username + RegistryConstants.PROFILE_RESOURCE_NAME;
        ProfilesAdminServiceClient client = new ProfilesAdminServiceClient(config,session);
        if(!client.putUserProfile(username)){
        request.setAttribute(CarbonUIMessage.ID,new CarbonUIMessage(error,error,null));
    }
    else{
        AddWSDLToolUtil.addMEXBean(request, config, session);
    }
} catch (UIException e) {

        return;
    }

    String resourcePagePath = "../../../resources/resource.jsp?region=region3&item=resource_browser_menu&path=" + parentPath;

    response.sendRedirect(resourcePagePath);
%>