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

<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.governance.generic.ui.clients.ManageGenericArtifactServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%
    String error = "Wrong Configuration, please re-check the default configuration";
    String update = Encode.forJava(request.getParameter("payload"));
    ManageGenericArtifactServiceClient client = new ManageGenericArtifactServiceClient(config, session);
    String defaultPath =
            "../generic/configure.jsp?region=" + Encode.forUriComponent(request.getParameter("region")) + "&item="
                    + Encode.forUriComponent(request.getParameter("item")) + "&add_edit_region=" + Encode
                    .forUriComponent(request.getParameter("add_edit_region")) + "&add_edit_item=" + Encode
                    .forUriComponent(request.getParameter("add_edit_item")) + "&key=" + Encode
                    .forUriComponent(request.getParameter("key")) + "&pluralLabel=" + Encode
                    .forUriComponent(request.getParameter("pluralLabel")) + "&singularLabel=" + Encode
                    .forUriComponent(request.getParameter("singularLabel")) + "&lifecycleAttribute=" + Encode
                    .forUriComponent(request.getParameter("lifecycleAttribute")) + "&add_edit_breadcrumb=" + Encode
                    .forUriComponent(request.getParameter("add_edit_breadcrumb"));

    if (client.setArtifactUIConfiguration(request.getParameter("key"), update)) {
        String path = "";
        if (CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/resources/govern/generic/add")
                && CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/resources/browse")) {
            path = "../generic/add_edit.jsp?region=" + Encode.forUriComponent(request.getParameter("add_edit_region"))
                    + "&item=" + Encode.forUriComponent(request.getParameter("add_edit_item")) + "&key=" + Encode
                    .forUriComponent(request.getParameter("key")) + "&lifecycleAttribute=" + Encode
                    .forUriComponent(request.getParameter("lifecycleAttribute")) + "&breadcrumb=" + Encode
                    .forUriComponent(request.getParameter("add_edit_breadcrumb"));
        } else {
            path = "../generic/configure.jsp?region=" + Encode.forUriComponent(request.getParameter("region"))
                    + "&item=" + Encode.forUriComponent(request.getParameter("item")) + "&add_edit_region=" + Encode
                    .forUriComponent(request.getParameter("add_edit_region")) + "&add_edit_item=" + Encode
                    .forUriComponent(request.getParameter("add_edit_item")) + "&key=" + Encode
                    .forUriComponent(request.getParameter("key")) + "&pluralLabel=" + Encode
                    .forUriComponent(request.getParameter("pluralLabel")) + "&singularLabel=" + Encode.forUriComponent
                    .getParameter("singularLabel"))
            +"&lifecycleAttribute=" + Encode.forUriComponent(request.getParameter("lifecycleAttribute"))
                    + "&add_edit_breadcrumb=" + Encode.forUriComponent(request.getParameter("add_edit_breadcrumb"));
        }
%>
<script type="text/javascript">
    location.href = "<%=path%>";
</script>
<%
} else {
%>
<script type="text/javascript">
    CARBON.showErrorDialog("<%=error%>", function () {
        location.href = "<%=defaultPath%>";
        return;
    });
</script>
<%
    }

%>



