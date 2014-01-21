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
<%@ page import="org.apache.axiom.om.OMElement" %>
<%@ page import="org.wso2.carbon.governance.generic.ui.clients.ManageGenericArtifactServiceClient" %>
<%@ page import="org.wso2.carbon.governance.generic.ui.utils.GenericUIGenerator" %>
<%
String artby_name = request.getParameter("Name");
    String searchValue = request.getParameter("searchvalule");
    String feild = request.getParameter("filterBy");
response.sendRedirect("../generic/" + (request.getParameter("isContent") != null ?
        "list_content.jsp" : "list.jsp") + "?artby_name="+artby_name+"&filter="+artby_name+"&region=" +
        request.getParameter("region") + "&item=" + request.getParameter("item") + "&dataName=" +
        request.getParameter("dataName") + "&singularLabel=" +
        request.getParameter("singularLabel") + "&pluralLabel=" +
        request.getParameter("pluralLabel") + "&dataNamespace=" +
        request.getParameter("dataNamespace") + "&searchValue="+searchValue+ "&filterBy=" + feild+ "&key="  +request.getParameter("key") +
        "&breadcrumb=" + request.getParameter("breadcrumb") + "&hasNamespace=" +
        request.getParameter("hasNamespace") + "&mediaType=" +
        request.getParameter("mediaType").replace(" ", "+"));
%>
