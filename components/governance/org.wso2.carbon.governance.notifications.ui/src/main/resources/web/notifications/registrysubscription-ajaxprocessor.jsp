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
<%@ page import="org.wso2.carbon.registry.resource.ui.clients.ResourceServiceClient" %>
<%@ page import="org.wso2.carbon.registry.resource.stub.beans.xsd.ResourceTreeEntryBean" %>
<%@ page import="org.wso2.carbon.utils.multitenancy.MultitenantConstants" %>
<%@ page import="org.wso2.carbon.context.CarbonContext" %>

<%
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    String[] events = null;
    String[] resourceEventNames = null;
    String[] collectionEventNames = null;
    boolean[] isEventVisible = null;
    boolean isResource = true;
    boolean canSubscribeOthers = false;
    boolean canSubscribeOtherRoles = false;
    String username = null;
    String[] roles = null;
    boolean isCollection=true;
    ResourceServiceClient resourceServiceClient=null;
    ResourceTreeEntryBean resourceTreeEntryBean = null;
    boolean isSuperTenant = false;
    resourceServiceClient = new ResourceServiceClient(config, session);
    String path = request.getParameter("path");

    if (resourceServiceClient.getResourceTreeEntry(path) != null) {
        isCollection = resourceServiceClient.getResourceTreeEntry(path).getCollection();
    }

     if(CarbonContext.getThreadLocalCarbonContext().getTenantId() == MultitenantConstants.SUPER_TENANT_ID) {
         isSuperTenant = true;
     }

    try{
        InfoAdminServiceClient client = new InfoAdminServiceClient(cookie, config, session);
        SubscriptionBean subscriptionBean = client.getSubscriptions(request);
        EventTypeBean eventTypeBean = client.getEventTypes(request);
        EventType[] eventTypes = eventTypeBean.getEventTypes();
        events = new String[eventTypes.length];
        resourceEventNames = new String[eventTypes.length];
        collectionEventNames = new String[eventTypes.length];
        isEventVisible = new boolean[eventTypes.length];
        for (int i = 0; i < eventTypes.length; i++) {
            if (eventTypes[i] != null) {
                events[i] = eventTypes[i].getId();
                resourceEventNames[i] = eventTypes[i].getResourceEvent();
                collectionEventNames[i] = eventTypes[i].getCollectionEvent();
                if (eventTypes[i].getId().startsWith("publisher") || eventTypes[i].getId().startsWith("store")){
                    isEventVisible[i] = false;
                } else{
                    isEventVisible[i] = true;
                }
            }
        }
        if (request.getParameter("path") != null) {
            isResource = client.isResource(request);
        }
        canSubscribeOthers = (subscriptionBean.getUserAccessLevel() > 2);
        canSubscribeOtherRoles = (subscriptionBean.getRoleAccessLevel() > 0);
        username = subscriptionBean.getUserName();
        roles = subscriptionBean.getRoles();
    } catch (Exception e){
        response.setStatus(500);
        if (e.getMessage().contains("This method is no longer supported")) {
            %><fmt:bundle basename="org.wso2.carbon.governance.notifications.ui.i18n.Resources"><fmt:message key="remote.subscription"/></fmt:bundle><%
        } else {
            %><%=e.getMessage()%><%
        }
        return;
    }
%>
 <fmt:bundle basename="org.wso2.carbon.governance.notifications.ui.i18n.Resources">
<table class="insideTable" cellpadding="0" cellspacing="0">
        <tr>
            <td class="leftCol-small"><fmt:message key="event"/>&nbsp;<span class="required">*</span></td>
            <td valign="top" style="text-align:left;">
                <select id="eventList" onchange="changeVisibility()">
                    <option value="0"><fmt:message key="select"/></option>
<%
    for (int i = 0; i < events.length; i++) {
        if (isResource) {
            if (resourceEventNames[i] != null && isEventVisible[i]) {
%>
                    <option value="<%=resourceEventNames[i]%>">
                        <% if (events[i].startsWith("custom:")) { %><%=events[i].substring("custom:".length())%>
                        <%} else {%><fmt:message key="<%=events[i]%>"/><%}%>
                    </option>
<%
            }
        } else if (collectionEventNames[i] != null && isEventVisible[i]) {
%>
                    <option value="<%=collectionEventNames[i]%>">
                        <% if (events[i].startsWith("custom:")) { %><%=events[i].substring("custom:".length())%>
                        <%} else {%><fmt:message key="<%=events[i]%>"/><%}%>
                    </option>
<%
        }
    }
%>
                </select>
            </td>
        </tr>
        <tr>
            <td><fmt:message key="notification.method"/>&nbsp;<span class="required">*</span></td>
            <td>
                <select id="notificationMethodList" disabled="disabled" onchange="changeVisibility()">
                    <option value="0"><fmt:message key="select"/></option>
                    <option value="1"><fmt:message key="email"/></option>
                    <option value="2"><fmt:message key="rest"/></option>
                    <option value="3"><fmt:message key="soap"/></option>
                    <option value="4"><fmt:message key="username"/></option>
                    <option value="5"><fmt:message key="role"/></option>
                    <option value="6"><fmt:message key="management.console"/></option>
                    <% if(isSuperTenant) {%>
                    <option value="7"><fmt:message key="jmx"/></option>
                    <%}%>
                </select>
            </td>
        </tr>
        <tr id="subscriptionDataInputRecord" style="display:none">
            <td>
                <div id="subscriptionDataEmail" style="display:none">
                   <fmt:message key="enter.email.prompt"/>&nbsp;<span class="required">*</span>
                </div>
                <div id="subscriptionDataREST" style="display:none">
                    <fmt:message key="enter.url.prompt"/>&nbsp;<span class="required">*</span>
                </div>
                <div id="subscriptionDataSOAP" style="display:none">
                    <fmt:message key="enter.endpoint.prompt"/>&nbsp;<span class="required">*</span>
                </div>
                <div id="subscriptionDataUserProfile" style="display:none">
<%
    if (canSubscribeOthers) {
%>
                    <input type="hidden" id="subscriptionUserProfile" value="" />
<%
    } else {
%>
                    <input type="hidden" id="subscriptionUserProfile" value="<%=username%>" />
<%
    }
%>
                    <fmt:message key="enter.username.prompt"/>&nbsp;<span class="required">*</span>
                </div>

                <div id="subscriptionDataRoleProfile" style="display:none">
<%
    if (canSubscribeOtherRoles) {
%>
                    <input type="hidden" id="subscriptionRoleProfile" value="" />
<%
    } else {
        StringBuffer sb = new StringBuffer();
        for (String role : roles) {
            sb.append(role).append(",");
        }
        String roleList = sb.substring(0, sb.length() - 1);
%>
                    <input type="hidden" id="subscriptionRoleProfile" value="<%=roleList%>" />
<%
    }
%>
                    <fmt:message key="enter.role.prompt"/>&nbsp;<span class="required">*</span>
                </div>
                <div id="subscriptionDataWorkList" style="display:none">
                    <%
                        if (canSubscribeOtherRoles) {
                    %>
                    <input type="hidden" id="subscriptionWorkList" value="" />
                    <%
                    } else {
                        StringBuffer sb = new StringBuffer();
                        for (String role : roles) {
                            sb.append(role).append(",");
                        }
                        String roleList = sb.substring(0, sb.length() - 1);
                    %>
                    <input type="hidden" id="subscriptionWorkList" value="<%=roleList%>" />
                    <%
                        }
                    %>
                    <fmt:message key="enter.role.prompt"/>&nbsp;<span class="required">*</span>
                </div>

                <div id="subscriptionDataJMX" style="display:none"></div>
            </td>
            <td><input type="text" id="subscriptionInput" /></td>
        </tr>
        <tr id="subscriptionDigestType" style="display:none">
            <td><fmt:message key="digest.delivery"/></td>
            <td><select id="subscriptionDigestTypeInput" disabled="disabled">
                <option value="0"><fmt:message key="digest.none"/></option>
                <option value="1"><fmt:message key="digest.hourly"/></option>
                <option value="2"><fmt:message key="digest.daily"/></option>
                <option value="3"><fmt:message key="digest.weekly"/></option>
                <option value="4"><fmt:message key="digest.fortnightly"/></option>
                <option value="5"><fmt:message key="digest.monthly"/></option>
            </select></td>
        </tr>
    <%
        if(isCollection){
    %>
    <div id="hierarchicalSubscriptionInfo" style="display:1">
        <tr>
            <td valign="middle" style="width:30px;text-align:left"><fmt:message key="hierarchical.subcription"/>&nbsp;</td>
            <td valign="top" style="width:70px;text-align:left;">
                <select id="hierarchicalSubscriptionList">
                    <option value="none"><fmt:message key="collection.only"/></option>
                    <option value="*"><fmt:message key="immediate.child"/></option>
                    <option value="#"><fmt:message key="all.child"/></option>
                </select>
            </td>
        </tr>
    </div>
    <%
        }
    %>
</table>
<% if (request.getParameter("notificationMethod") != null) { %>
<script type="text/javascript">
    document.getElementById('eventList').value = "<%=request.getParameter("event")%>";
    var notificationMethod = "<%=request.getParameter("notificationMethod")%>";
    var digestType = "<%=(request.getParameter("digestType") != null) ?
                            request.getParameter("digestType") : "" %>";
    var hierarchicalsubscriptionmethod = "<%=request.getParameter("hierarchicalsubscriptionmethod")%>";
    document.getElementById('subscriptionDataInputRecord').style.display = "";
    if (notificationMethod =="email") {
        document.getElementById('notificationMethodList').value = 1;
    } else if (notificationMethod =="username") {
        document.getElementById('notificationMethodList').value = 4;
    } else if (notificationMethod =="role") {
        document.getElementById('notificationMethodList').value = 5;
    } else if (notificationMethod =="work") {
        document.getElementById('notificationMethodList').value = 6;
    } else if (notificationMethod =="jmx") {
        document.getElementById('notificationMethodList').value = 7;
    } else if (notificationMethod =="html.plain.text") {
        document.getElementById('notificationMethodList').value = 2;
    } else if (notificationMethod =="soap") {
        document.getElementById('notificationMethodList').value = 3;
    }
    changeVisibility();
    if (digestType == "h") {
        document.getElementById('subscriptionDigestTypeInput').value = 1;
    } else if (digestType == "d") {
        document.getElementById('subscriptionDigestTypeInput').value = 2;
    } else if (digestType == "w") {
        document.getElementById('subscriptionDigestTypeInput').value = 3;
    } else if (digestType == "f") {
        document.getElementById('subscriptionDigestTypeInput').value = 4;
    } else if (digestType == "m") {
        document.getElementById('subscriptionDigestTypeInput').value = 5;
    } else {
        document.getElementById('subscriptionDigestTypeInput').value = 0;
    }
    document.getElementById('subscriptionInput').value = "<%=request.getParameter("input")%>";
    
    if (hierarchicalsubscriptionmethod == "none") {
        document.getElementById('hierarchicalSubscriptionList').value = "none";
    } else if (hierarchicalsubscriptionmethod == "star") {
        document.getElementById('hierarchicalSubscriptionList').value = "*";
    }else {
    	 document.getElementById('hierarchicalSubscriptionList').value = "#";
    }
</script>
<% } %>
</fmt:bundle>