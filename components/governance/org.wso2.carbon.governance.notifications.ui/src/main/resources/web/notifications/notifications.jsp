<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%--
~  Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~  Licensed under the Apache License, Version 2.0 (the "License");
~  you may not use this file except in compliance with the License.
~  You may obtain a copy of the License at
~
~        http://www.apache.org/licenses/LICENSE-2.0
~
~  Unless required by applicable law or agreed to in writing, software
~  distributed under the License is distributed on an "AS IS" BASIS,
~  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
~  See the License for the specific language governing permissions and
~  limitations under the License.
--%>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%@ page import="org.wso2.carbon.governance.notifications.stub.services.utils.xsd.SubscriptionInstance" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.governance.notifications.ui.clients.InfoAdminServiceClient" %>
<%@ page import="org.wso2.carbon.governance.notifications.stub.beans.xsd.SubscriptionBean" %>
<%@ page import="org.wso2.carbon.governance.notifications.stub.beans.xsd.EventTypeBean" %>
<%@ page import="org.wso2.carbon.governance.notifications.stub.services.utils.xsd.EventType" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.registry.common.eventing.RegistryEvent" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.registry.core.RegistryConstants" %>
<%@ page import="org.wso2.carbon.registry.common.ui.UIConstants" %>
<%@ page import="org.wso2.carbon.governance.notifications.ui.clients.SubscriptionsUIUtils" %>
<%@ page import="java.net.URLEncoder" %>

<link type="text/css" rel="stylesheet" href="css/menu.css"/>
<link type="text/css" rel="stylesheet" href="css/style.css"/>
<link type="text/css" rel="stylesheet" href="../resources/css/registry.css"/>

<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<jsp:include page="../registry_common/registry_common-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../registry_common/js/registry_validation.js"></script>
<script type="text/javascript" src="../registry_common/js/registry_common.js"></script>
<carbon:jsi18n
		resourceBundle="org.wso2.carbon.governance.notifications.ui.i18n.JSResources"
		request="<%=request%>" namespace="org.wso2.carbon.governance.notifications.ui"/>
<%
    String requestedPage = request.getParameter(UIConstants.REQUESTED_PAGE);
%>
<script type="text/javascript" src="js/notifications.js"></script>
<script type="text/javascript" src="js/paginate.js"></script>

<carbon:breadcrumb
        label="notifications"
        resourceBundle="org.wso2.carbon.governance.notifications.ui.i18n.Resources"
        topPage="true"
        request="<%=request%>"/>

<%
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    String[] events = null;
    String[] resourceEventNames = null;
    String[] collectionEventNames = null;
    SubscriptionInstance[] subscriptions = null;
    boolean canUnsubscribe = false;
    try{
        InfoAdminServiceClient client = new InfoAdminServiceClient(cookie, config, session);
        SubscriptionBean subscriptionBean = client.getSubscriptions(request);
        subscriptions = subscriptionBean.getSubscriptionInstances();
        EventTypeBean eventTypeBean = client.getEventTypes(request);
        EventType[] eventTypes = eventTypeBean.getEventTypes();
        events = new String[eventTypes.length];
        resourceEventNames = new String[eventTypes.length];
        collectionEventNames = new String[eventTypes.length];
        for (int i = 0; i < eventTypes.length; i++) {
            if (eventTypes[i] != null) {
                events[i] = eventTypes[i].getId();
                resourceEventNames[i] = eventTypes[i].getResourceEvent();
                collectionEventNames[i] = eventTypes[i].getCollectionEvent();
            }
        }
        canUnsubscribe = (subscriptionBean.getUserAccessLevel() > 0);
    } catch (Exception e){
        response.setStatus(500);
        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
        session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
        <jsp:include page="../admin/error.jsp?<%=e.getMessage()%>"/>
<%
        return;
    }

%>
<fmt:bundle basename="org.wso2.carbon.governance.notifications.ui.i18n.Resources">
<div id="middle">
    <h2><fmt:message key="manage.notifications"/></h2>
    <%
        if(subscriptions != null && subscriptions.length != 0){
            int start;
            int end;
            int itemsPerPage = (int)(RegistryConstants.ITEMS_PER_PAGE * 1.5);
            int pageNumber;
            int numberOfPages;

            if(requestedPage != null && requestedPage.length()>0){
                pageNumber = new Integer(requestedPage);
            } else{
                pageNumber = 1;
            }

            numberOfPages=1;
            if(subscriptions.length % itemsPerPage==0){
                numberOfPages =  subscriptions.length / itemsPerPage;
            }
            else{
                numberOfPages =  (subscriptions.length / itemsPerPage)+1;
            }

            if(subscriptions.length < itemsPerPage){
                start = 0;
                end = subscriptions.length;
            }
            else{
                start = (pageNumber - 1) * itemsPerPage;
                end = (pageNumber - 1) * itemsPerPage + itemsPerPage;
            }

            SubscriptionInstance[] paginatedSubscriptions = SubscriptionsUIUtils.getPaginatedSubscriptions(start,itemsPerPage,subscriptions);
    %>
    <div id="workArea">
        <div class="registryWriteOperation" style="<%=paginatedSubscriptions.length == 0 || paginatedSubscriptions[0] == null?"":"display:none"%>">
            <fmt:message
                    key="no.subscriptions.are.currently.defined.click.add.subscription.to.create.a.new.subscription"/>
        </div>
        <div class="registryNonWriteOperation" style="<%=paginatedSubscriptions.length == 0 || paginatedSubscriptions[0] == null?"":"display:none"%>">
            <fmt:message
                    key="no.subscriptions.are.currently.defined"/>
        </div>
<%
    if (!(paginatedSubscriptions.length == 0 || paginatedSubscriptions[0] == null)) {
%>
    <table cellpadding="0" border="0" class="styledLeft" id="subscriptionsTable">
    <thead>
        <tr>
            <th><fmt:message key="path"/></th>
            <th><fmt:message key="event"/></th>
            <th><fmt:message key="owner"/></th>
            <th><fmt:message key="notification"/></th>
            <th><fmt:message key="subscriber"/></th>
            <th><fmt:message key="actions"/></th>
        </tr>
    </thead>
    <tbody>
<%
        for (SubscriptionInstance subscription : paginatedSubscriptions) {
            if (subscription == null) {
                continue;
            }
            String address = null;
            boolean isDigest = subscription.getDigestType() != null &&
                    !subscription.getDigestType().equals("");
            String notificationMethod = subscription.getNotificationMethod();
            if (notificationMethod.equals("email") || notificationMethod.equals("username") ||
                    notificationMethod.equals("role") || notificationMethod.equals("work")) {
                address = subscription.getAddress().substring(7);
                if (isDigest) {
                    address = address.substring(11);
                }
            } else if (!notificationMethod.equals("jmx") && !notificationMethod.equals("work")) {
                address = subscription.getAddress();
                if(notificationMethod.equals("html.plain.text")) {
                    notificationMethod = "rest";
                } else if(notificationMethod.equals("soap")) {
                    notificationMethod = "soap";
                }
            }
            String notificationMethodPrompt = "enter." + notificationMethod + ".prompt";
            String eventName = subscription.getEventName();
            String owner = subscription.getOwner();
            String path = subscription.getTopic();
            String hierarchicalSubscriptionMethod = "none";
            if(path.contains("#")||path.contains("*")){
            	hierarchicalSubscriptionMethod = (path.contains("#"))?"hash":"star"; 
                String tempPath=path.substring(RegistryEvent.TOPIC_PREFIX.length()+1, path.lastIndexOf("/"));
                if(tempPath.contains("/")){
                    path = tempPath.split("/",2)[1];
                }else{
                    path="/";
                }
            }else{
                path = (path.substring(RegistryEvent.TOPIC_PREFIX.length()+1, path.length())).split("/",2)[1];
            }
            if(!path.startsWith("/")){
                path="/"+path;
            }
            String encodedPath = path;
            try {
                encodedPath = URLEncoder.encode(encodedPath, "UTF-8");
            } catch (Exception ignore) {}
            //TODO: Handle displaying long path names
            String eventId = null;
            for (int i = 0; i < events.length; i++) {
                if (eventName.equals(resourceEventNames[i]) || eventName.equals(collectionEventNames[i])) {
                    eventId = events[i];
                    break;
                }
            }
            if (eventId != null) {   //&region=region3&item=resource_browser_menu
%>
        <tr>
            <td>
                <% if (CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/resources/browse")) { %>
                <a href="../resources/resource.jsp?region=region3&item=resource_browser_menu&path=<%=encodedPath%>"><%=path%></a>
                <% } else { %>
                <%=path%>
                <% }%>
            </td>
            <td>
                <% if (eventId.startsWith("custom:")) { %><%=eventId.substring("custom:".length())%>
                <%} else {%><fmt:message key="<%=eventId%>"/><%}%>
            </td>
            <td><%=((owner == null) ? "" : owner)%></td>
<%
                if (notificationMethod.equals("username") && CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/user-profiles")) {
%>
            <td>
                <a href="../userprofile/index.jsp?username=<%=address%>&region=region5&item=userprofiles_menu"><fmt:message key="<%=notificationMethod%>"/></a>
            </td>
<%--
            //TODO: FIXME: After role-profiles are implemented, fix this as appropriate.
<%
                } else if (notificationMethod.equals("role") && CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/user-profiles")) {
%>
            <td>
                <a href="../userprofile/index.jsp?username=<%=address%>&region=region5&item=userprofiles_menu"><fmt:message key="<%=notificationMethod%>"/></a>
            </td>
--%>
<%
                } else {
%>
            <td>
                <abbr <% if (address != null) { %> title='<fmt:message key="<%=notificationMethodPrompt%>"/>: <%=address%>' <% } %>><fmt:message key="<%=notificationMethod%>"/></abbr>
            </td>
<%
                }
    if(notificationMethod.equals("soap") || notificationMethod.equals("rest"))  {
%>
            <td><a href="<%=address %>"><%=notificationMethod + "_endpoint"%></a></td>

<%} else { %>
            <td><%=address == null? notificationMethod :address%></td>

<%
    }
    if (canUnsubscribe) {

%>
            <td>
                <a href="registrysubscription.jsp?edit=edit&hierarchicalsubscriptionmethod=<%=hierarchicalSubscriptionMethod%>&notificationMethod=<%=subscription.getNotificationMethod() + (isDigest ? "&digestType=" + subscription.getDigestType() : "")%>&path=<%=encodedPath%>&input=<%=address%>&id=<%=subscription.getId()%>&event=<%=eventName%>" class="icon-link registryWriteOperation" style="background-image: url(../admin/images/edit.gif);"><fmt:message key="edit"/></a>
                <a href="javascript:void(0)" onclick="unsubscribe('<%=path%>','<%=subscription.getId()%>');" class="icon-link registryWriteOperation" style="background-image: url(../admin/images/delete.gif);"><fmt:message key="delete"/></a>
            </td>
<%
                } else {
%>
            <td valign="top" style="text-align:right;width:20%"></td>
<%
                }
%>
        </tr>
<%
            }
        }
%>
	</tbody>
    </table>
    <script type="text/javascript">
        alternateTableRows('subscriptionsTable', 'tableEvenRow', 'tableOddRow');
    </script>
<%
    }
%>
        <div id="subscriptionOptionTable" class="registryWriteOperation" style="height:20px;margin-top:5px;">

                    <a class="icon-link"
                       href="registrysubscription.jsp"
                       style="background-image: url(../admin/images/add.gif);">
                        <fmt:message key="add.registry.subscription"/>
                    </a>
        </div>

    </div>
<carbon:resourcePaginator pageNumber="<%=pageNumber%>" numberOfPages="<%=numberOfPages%>"
                                  resourceBundle="org.wso2.carbon.governance.notifications.ui.i18n.Resources"
                                  nextKey="next" prevKey="prev"
                                  paginationFunction="submitSubscription(1,{0})"/>
<%
    }else  {
%>
    <div id="workArea">
        <div class="registryWriteOperation">
            <fmt:message
                    key="no.subscriptions.are.currently.defined.click.add.subscription.to.create.a.new.subscription"/>
        </div>
        <div class="registryNonWriteOperation">
            <fmt:message
                    key="no.subscriptions.are.currently.defined"/>
        </div>
        <div id="subscriptionOptionTable" class="registryWriteOperation" style="height:20px;margin-top:5px;">

                    <a class="icon-link"
                       href="registrysubscription.jsp"
                       style="background-image: url(../admin/images/add.gif);">
                        <fmt:message key="add.registry.subscription"/>
                    </a>
        </div>
    </div>
<%
    }
%>
</div>
</fmt:bundle>