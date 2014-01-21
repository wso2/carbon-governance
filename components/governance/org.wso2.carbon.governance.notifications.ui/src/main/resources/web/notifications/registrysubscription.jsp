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
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%@ page import="org.wso2.carbon.governance.notifications.ui.clients.InfoAdminServiceClient" %>
<%@ page import="org.wso2.carbon.governance.notifications.stub.beans.xsd.EventTypeBean" %>
<%@ page import="org.wso2.carbon.governance.notifications.stub.beans.xsd.SubscriptionBean" %>
<%@ page import="org.wso2.carbon.governance.notifications.stub.services.utils.xsd.EventType" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>

<link type="text/css" rel="stylesheet" href="css/menu.css"/>
<link type="text/css" rel="stylesheet" href="css/style.css"/>

<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<jsp:include page="../registry_common/registry_common-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../registry_common/js/registry_validation.js"></script>
<script type="text/javascript" src="../registry_common/js/registry_common.js"></script>
<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../resources/js/resource_util.js"></script>
<carbon:jsi18n
		resourceBundle="org.wso2.carbon.governance.notifications.ui.i18n.JSResources"
		request="<%=request%>" namespace="org.wso2.carbon.governance.notifications.ui"/>
<script type="text/javascript" src="js/notifications.js"></script>

<script type=text/javascript >
    function updateSubscriptionPathToolTip() {
        $('subscriptionPath').title = $('subscriptionPath').value;
    }
</script>

<fmt:bundle basename="org.wso2.carbon.governance.notifications.ui.i18n.Resources">
<carbon:breadcrumb
        label="registry.subscription"
        resourceBundle="org.wso2.carbon.governance.notifications.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>
<style>
.insideTable{
padding:0px !important;
margin:0px !important;
border:none !important;
}
.insideTable td{
padding:0px !important;
margin:0px !important;
border:none !important;
}
</style>        

<div id="middle">
    <h2><fmt:message key="registry.subscription"/></h2>

    <div id="workArea">
<div id="add-subscription-div" style="padding-bottom:10px;">
<form id="subscriptionForm" name="subscriptionForm" onclick="updateSubscriber();" onkeypress="updateSubscriber();" onmousemove="updateSubscriber();"  action="" method="POST">
<table class="styledLeft">
<thead>
<tr>
    <th><fmt:message key="registry.subscription"/></th>
</tr>
</thead>
<tbody>
<tr>
<td class="formRow">
<table class="normal" cellspacing="0">

        <tr>
            <td class="leftCol-small" ><fmt:message key="subscription.path"/>&nbsp;<span class="required">*</span></td>
            <td>
                <input type="text" id="subscriptionPath" disabled="disabled" onchange="updateSubscriber();" onfocus="updateSubscriber();" />

<% if (request.getParameter("edit") == null) { %>
                <input type="button" class="button"
                                       value=".." title="Select Path"
                                       onclick="showResourceTree('subscriptionPath', updateSubscriptionPathToolTip);"/>
<% } %>                
            </td>
        </tr>
<tr>
<td colspan="2" id="subscription-area-div" style="padding:0px !important;margin:0px !important;"></td>
</tr>
</table>
</td>
</tr>
        <tr>
            <td class="buttonRow">
                <% if (request.getParameter("edit") != null) { %>
                <input type="button" id="subscribeButton" class="button registryWriteOperation" value="<fmt:message key="modify"/>"
                        onclick="unsubscribeAndSubscribe('<%=request.getParameter("path")%>','<%=request.getParameter("id")%>','<fmt:message key="are.you.sure.you.want.to.resubscribe"/>');" disabled="disabled"/>&nbsp;
                <% } else { %>
                <input type="button" id="subscribeButton" class="button registryWriteOperation" value="<fmt:message key="subscribe"/>"
                        onclick="subscribe();" disabled="disabled"/>&nbsp;
                        <% } %>
                <input type="button"
                        class="button"
                        value="<fmt:message key="cancel"/>"
                        onclick="cancelAddSubscription();"/>
            </td>
        </tr>
</tbody>
</table>
</form>
<% if (request.getParameter("edit") != null) { %>
<script type="text/javascript">
    sub_path = "<%=request.getParameter("path")%>";
    var path = sub_path;
    $('subscriptionPath').value = path;
    $('subscriptionPath').title = path;
    new Ajax.Updater('subscription-area-div', '../notifications/registrysubscription-ajaxprocessor.jsp', {
        evalScripts: true,
        parameters: {path: path, hierarchicalsubscriptionmethod:"<%=request.getParameter("hierarchicalsubscriptionmethod")%>", digestType: "<%=(request.getParameter("digestType") != null) ? request.getParameter("digestType") : "none"%>", notificationMethod: "<%=request.getParameter("notificationMethod")%>", input: "<%=request.getParameter("input")%>", event: "<%=request.getParameter("event")%>"}
    });
</script>
<% } else { %>
<script type="text/javascript">
    $('subscriptionPath').value = "/";
    $('subscriptionPath').title = "/";
    updateSubscriber();
</script>
<% } %>
</div>
</div>
</div>
</fmt:bundle>
