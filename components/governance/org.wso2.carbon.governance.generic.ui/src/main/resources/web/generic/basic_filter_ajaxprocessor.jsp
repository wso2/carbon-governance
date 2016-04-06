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
    ManageGenericArtifactServiceClient client = new ManageGenericArtifactServiceClient(config, session);
    String dataName = request.getParameter("dataName");
    String dataNamespace = request.getParameter("dataNamespace");
    GenericUIGenerator uigen = new GenericUIGenerator(dataName, dataNamespace);
    OMElement head = uigen
            .getUIConfiguration(client.getArtifactUIConfiguration(Encode.forJava(request.getParameter("key"))), request,
                    config, session);
    OMElement criteria = uigen.getDataFromUIForBasicFilter(head, request);
    session.setAttribute("criteria", criteria.toString());

    String searchValue = Encode.forUriComponent(request.getParameter("searchvalule"));
    String feild = Encode.forUriComponent(request.getParameter("filterBy"));
    String region = Encode.forUriComponent(request.getParameter("region"));
    String item = Encode.forUriComponent(request.getParameter("item"));
    String singularLabel = Encode.forUriComponent(request.getParameter("singularLabel"));
    String pluralLabel = Encode.forUriComponent(request.getParameter("pluralLabel"));
    String key = Encode.forUriComponent(request.getParameter("key"));
    String breadcrumb = Encode.forUriComponent(request.getParameter("breadcrumb"));

    response.sendRedirect(
            "../generic/list.jsp?filterBy=" + feild + "&searchValue=" + searchValue + "&filter=filter&region=" + region
                    + "&item=" + item + "&dataName=" + Encode.forUriComponent(dataName) + "&singularLabel="
                    + singularLabel + "&pluralLabel=" + pluralLabel + "&dataNamespace=" + Encode
                    .forUriComponent(dataNamespace) + "&key=" + key + "&breadcrumb=" + breadcrumb);
%>