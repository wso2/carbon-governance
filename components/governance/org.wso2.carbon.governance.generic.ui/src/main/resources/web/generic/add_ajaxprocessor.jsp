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
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="org.apache.axiom.om.OMElement" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.governance.generic.ui.clients.ManageGenericArtifactServiceClient" %>
<%@ page import="org.wso2.carbon.governance.generic.ui.utils.ManageGenericArtifactUtil" %>
<%@ page import="org.wso2.carbon.governance.generic.ui.utils.GenericUIGenerator" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="org.wso2.carbon.registry.common.ui.UIException" %>
<%
    String error1 = "Your modification cause replacement of another resource!";
    String dataName = request.getParameter("dataName");
    String dataNamespace = request.getParameter("dataNamespace");
    GenericUIGenerator uigen = new GenericUIGenerator(dataName, dataNamespace);
    ManageGenericArtifactServiceClient client = new ManageGenericArtifactServiceClient(config, session);
    OMElement head = null;
    //check whether this is adding a new artifact or this is editing the artifact content
    if (request.getAttribute("content") == null) {
        head = uigen.getUIConfiguration(client.getArtifactUIConfiguration(request.getParameter("key")), request, config,
                session);
    } else {
        head = (OMElement) request.getAttribute("content");
    }
    String registryArtifactPath = null;
    String currentPath = request.getParameter("currentPath");
    try {
        String effectivePath = ManageGenericArtifactUtil
                .addArtifactContent(head, request, config, session, dataName, dataNamespace, currentPath);
        if (effectivePath != null) {
            try {
                //REGISTRY-698
                //                if(request.getParameter("path")!=null){
                //                    effectivePath = request.getParameter("path");
                //                }
                registryArtifactPath = effectivePath;
            } catch (Exception ignore) {
            }
            String resourcePagePath =
                    "../resources/resource.jsp?region=region3&item=resource_browser_menu&path=" + URLEncoder
                            .encode(registryArtifactPath, "Utf-8");
            response.sendRedirect(resourcePagePath);
        } else {
            request.setAttribute(CarbonUIMessage.ID, new CarbonUIMessage(error1, error1, null));
            CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, error1, null);
            session.setAttribute(CarbonUIMessage.ID, uiMsg);

%>

<jsp:forward page="../admin/error.jsp"/>

<%
    }
} catch (Exception e) {

    String errorMsg = e.getMessage();
    String error;
    if (errorMsg != null) {
        if (errorMsg.contains("contains one or more illegal characters")) {
            error = "Failed to add the artifact, Special characters are not allowed in the name fields";
        } else if (errorMsg.contains("Governance artifact") && errorMsg.contains("already exists")) {
            error = "Failed to add the artifact, Governance artifact already exists";
        } else {
            error = errorMsg.replace("org.apache.axis2.AxisFault:", "").trim();
        }
    } else {
        error = "An unknown error has occurred, please see the error log";
    }

%>
<script type="text/javascript">
    window.location = '../generic/add_edit.jsp?region=<%=Encode.forUriComponent(request.getParameter("region"))%>&item=<%=Encode.forUriComponent(request.getParameter("item"))%>&key=<%=Encode.forUriComponent(request.getParameter("key"))%>&lifecycleAttribute=<%=Encode.forUriComponent(request.getParameter("lifecycleAttribute"))%>&breadcrumb=<%=Encode.forUriComponent(request.getParameter("breadcrumb"))%>&wsdlError=<%=URLEncoder.encode(error, "UTF-8")%>';
</script>
<%
    }
%>