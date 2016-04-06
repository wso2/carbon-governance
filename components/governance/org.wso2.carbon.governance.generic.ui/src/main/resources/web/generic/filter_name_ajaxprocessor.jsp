<!--
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
-->

<%@ page import="org.apache.axiom.om.OMElement" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.governance.generic.ui.clients.ManageGenericArtifactServiceClient" %>
<%@ page import="org.wso2.carbon.governance.generic.ui.utils.GenericUIGenerator" %>

<%
    String artby_name = request.getParameter("Name");
    String searchValue = request.getParameter("searchvalule");
    String feild = request.getParameter("filterBy");
    response.sendRedirect("../generic/" + (request.getParameter("isContent") != null ? "list_content.jsp" : "list.jsp")
            + "?artby_name=" + Encode.forUriComponent(artby_name) + "&filter=" + Encode.forUriComponent(artby_name)
            + "&region=" +
            Encode.forUriComponent(request.getParameter("region")) + "&item=" + Encode
            .forUriComponent(request.getParameter("item")) + "&dataName=" +
            Encode.forUriComponent(request.getParameter("dataName")) + "&singularLabel=" +
            Encode.forUriComponent(request.getParameter("singularLabel")) + "&pluralLabel=" +
            Encode.forUriComponent(request.getParameter("pluralLabel")) + "&dataNamespace=" +
            Encode.forUriComponent(request.getParameter("dataNamespace")) + "&searchValue=" + Encode
            .forUriComponent(searchValue) + "&filterBy=" + Encode.forUriComponent(feild) + "&key=" + Encode
            .forUriComponent(request.getParameter("key")) +
            "&breadcrumb=" + Encode.forUriComponent(request.getParameter("breadcrumb")) + "&hasNamespace=" +
            Encode.forUriComponent(request.getParameter("hasNamespace")) + "&mediaType=" +
            Encode.forUriComponent(request.getParameter("mediaType").replace(" ", "+")));
%>
