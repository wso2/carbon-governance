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
<%@ page import="org.wso2.carbon.governance.generic.ui.clients.ManageGenericArtifactServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%
    String error = "Wrong Configuration, please re-check the default configuration";
    String update = request.getParameter("payload");
    ManageGenericArtifactServiceClient
            client = new ManageGenericArtifactServiceClient(config,session);
    String defaultPath = "../generic/configure.jsp?region=" + request.getParameter("region") + "&item=" + request.getParameter("item") + "&add_edit_region=" + request.getParameter("add_edit_region") + "&add_edit_item=" + request.getParameter("add_edit_item") + "&key=" + request.getParameter("key") + "&pluralLabel=" + request.getParameter("pluralLabel") + "&singularLabel=" + request.getParameter("singularLabel") + "&lifecycleAttribute=" + request.getParameter("lifecycleAttribute") + "&add_edit_breadcrumb=" + request.getParameter("add_edit_breadcrumb");
    if(client.setArtifactUIConfiguration(request.getParameter("key"), update)){
        String path = "";
        if (CarbonUIUtil.isUserAuthorized(request,
                "/permission/admin/manage/resources/govern/generic/add") &&
                CarbonUIUtil.isUserAuthorized(request,
                "/permission/admin/manage/resources/browse")) {
            path = "../generic/add_edit.jsp?region=" + request.getParameter("add_edit_region") + "&item=" + request.getParameter("add_edit_item") + "&key=" + request.getParameter("key") + "&lifecycleAttribute=" + request.getParameter("lifecycleAttribute") + "&breadcrumb=" + request.getParameter("add_edit_breadcrumb");
        } else {
            path = "../generic/configure.jsp?region=" + request.getParameter("region") + "&item=" + request.getParameter("item") + "&add_edit_region=" + request.getParameter("add_edit_region") + "&add_edit_item=" + request.getParameter("add_edit_item") + "&key=" + request.getParameter("key") + "&pluralLabel=" + request.getParameter("pluralLabel") + "&singularLabel=" + request.getParameter("singularLabel") + "&lifecycleAttribute=" + request.getParameter("lifecycleAttribute") + "&add_edit_breadcrumb=" + request.getParameter("add_edit_breadcrumb");
        }
%>
<script type="text/javascript">
    location.href = "<%=path%>";
</script>
<%
    }
    else{
       %>
<script type="text/javascript">
    CARBON.showErrorDialog("<%=error%>", function() {
        location.href = "<%=defaultPath%>";
        return;
    });
</script>
       <%
       }

   %>



