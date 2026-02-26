<%--
~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~ WSO2 Inc. licenses this file to you under the Apache License,
~ Version 2.0 (the "License"); you may not use this file except
~ in compliance with the License.
~ You may obtain a copy of the License at
~
~ http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~ KIND, either express or implied. See the License for the
~ specific language governing permissions and limitations
~ under the License.
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ page import="org.wso2.carbon.registry.resource.ui.processors.DeleteProcessor" %>
<%@ page import="org.wso2.carbon.governance.generic.ui.utils.GenericUtil" %>

<%
    if (!"post".equalsIgnoreCase(request.getMethod())) {
        response.sendError(405);
        return;
    }
    String errorMessage = null;
    String contextPath = (request.getContextPath().equals("") || request.getContextPath()
            .equals("/")) ? "" : request.getContextPath();
    HttpSession session1;
    boolean authenticated = false;

    // Get the user's current authenticated session - if any exists.
    session1 = request.getSession();
    Boolean authenticatedObj = (Boolean) session1.getAttribute("authenticated");
    if (authenticatedObj != null) {
        authenticated = authenticatedObj.booleanValue();
    }
    if (authenticated) {
        try {
            DeleteProcessor.process(request, response, config);
            if (session != null) {
                GenericUtil.buildMenuItems(request, (String) session.getAttribute("logged-user"),
                        (String) session.getAttribute("tenantDomain"), (String) session.getAttribute("ServerURL"));
            }
        } catch (Exception e) {
            errorMessage = e.getMessage();
        }
    } else {
        errorMessage = "Unauthenticated Request";
        response.setStatus(401);
        response.sendRedirect(contextPath + "/admin/login.jsp");
    }
%>

<% if (errorMessage != null && response.getStatus() != 401) {
    response.setStatus(500);
%><%=errorMessage%><%
    } %>