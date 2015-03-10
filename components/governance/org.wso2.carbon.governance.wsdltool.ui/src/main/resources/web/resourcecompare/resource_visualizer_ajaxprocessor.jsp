<%--
 ~ Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.registry.resource.ui.clients.ResourceServiceClient" %>
<!-- code mirror libraries -->
<link rel="stylesheet" href="../diffview/lib/codemirror.css">
<%--<link rel=stylesheet href="../diffview/addon/merge/merge.css">--%>
<script src="../diffview/lib/codemirror.js"></script>
<script src="../diffview/mode/javascript/javascript.js"></script>
<script src="../diffview/mode/xml/xml.js"></script>
<%--<script src="../diffview/addon/merge/dep/diff_match_patch.js"></script>--%>
<%--<script src="../diffview/addon/merge/merge.js"></script>--%>
<%
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    String resourceContent;
    try {
        // Get registry resource content
        ResourceServiceClient client = new ResourceServiceClient(cookie, config, session);
        resourceContent = client.getTextContent(request);
    } catch (Exception e) {
        return;
    }
%>
<fmt:bundle basename="org.wso2.carbon.governance.wsdltool.ui.i18n.Resources">
    <div id="resource_panel" style="margin: 0px; width: 744px; height: 744px;">
        <textarea id="resource_panel_textarea" style="margin: 0px; width: 744px; height: 744px;">
            <%=resourceContent.replace("&", "&amp;").replaceAll("<[?]xml[^?]*[?]>", "")%>
        </textarea>
    </div>
</fmt:bundle>
<script>
    var widthOfResourceViewer = '100%';
    var heightOfResourceViewer = '100%';
    var editor = CodeMirror.fromTextArea(document.getElementById("resource_panel_textarea"), {
        mode: "application/xml",
        lineNumbers: false,
        readOnly: true
//        ,
//        lineWrapping: true
    });
    editor.setSize(widthOfResourceViewer, heightOfResourceViewer);
</script>