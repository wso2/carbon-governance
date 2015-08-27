<%--
Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.

WSO2 Inc. licenses this file to you under the Apache License,
Version 2.0 (the "License"); you may not use this file except
in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ page import="org.wso2.carbon.governance.notifications.ui.worklist.HumanTaskClient" %>
<%@ page import="org.wso2.carbon.governance.notifications.ui.worklist.WorkItem" %>
<%@ page import="org.wso2.carbon.context.CarbonContext" %>
<%@ page import="org.wso2.carbon.utils.multitenancy.MultitenantConstants" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="carbon" uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" %>
<%
    String[] roles;
    WorkItem[] workItems;
    boolean isSuperTenant = false;
    try {
        HumanTaskClient client = new HumanTaskClient(config, session);
        roles = client.getRoles(session);
        workItems = client.getWorkItems(request);
        if(CarbonContext.getThreadLocalCarbonContext().getTenantId() == MultitenantConstants.SUPER_TENANT_ID) {
          isSuperTenant = true;
        }

    } catch (Exception ignored) {
        return;
    }
%>
<jsp:include page="../registry_common/registry_common-i18n-ajaxprocessor.jsp"/>
<carbon:jsi18n
        resourceBundle="org.wso2.carbon.governance.notifications.ui.i18n.JSResources"
        request="<%=request%>" namespace="org.wso2.carbon.governance.notifications.ui"/>
<fmt:bundle basename="org.wso2.carbon.governance.notifications.ui.i18n.Resources">

  <a class="view-notification" id="viewNotification"><%=workItems.length%></a>

    <div id="notificationPopupView" class="notificationPopup" style="display:none">
        <div class="popupPointer"></div>
        <div class="popupBox">
            <div class="title"><strong><fmt:message key="work.list.notifications"/></strong></div>
            <div class="notificationElementWrapper">
                <%
					int index = 0;
                    for (WorkItem workItem : workItems) {
					index++;
                %>
                <div class="notificationElement <% if(index%2 == 0) {%>odd<% } %>">
                    <ul>
                        <li class="notificationCell1">#<%=workItem.getId()%></li>
                        <li class="notificationCell3"><%=workItem.getCreatedTime().getTime()%></li>
                    </ul>
                    <div style="clear:both"></div>
                    <div class="notificationDescription" style="overflow-x:auto">
                        <% if (workItem.getPresentationSubject() != null && workItem.getPresentationSubject().getTPresentationSubject() != null) {%>
                        <%=workItem.getPresentationSubject().getTPresentationSubject()%>
                        <% } else if (workItem.getPresentationName() != null && workItem.getPresentationName().getTPresentationName() != null) {%>
                        <%=workItem.getPresentationName().getTPresentationName()%>
                        <% } %>
                    </div>
                    <ul>
                        <li class="notificationCell1"><fmt:message key="work.list.priority"/>: <%=workItem.getPriority()%></li>
                        <li class="notificationCell2"><fmt:message key="work.list.status"/>: <%=workItem.getStatus()%></li>
                        <li class="notificationCell3">
                            <input type="button" onclick="completeTask(<%=workItem.getId()%>)" class="button notificationButton" value="<fmt:message key="work.list.hide"/>" />
                        </li>
                    </ul>
                    <div style="clear:both"></div>
                </div>
                <%
                    }
                %>
            </div>
        </div>
    </div>
    </fmt:bundle>
<script type="text/javascript">
    jQuery('#viewNotification').click(
            function(){
                if(jQuery('#notificationPopupAdd').is(":visible")){
                    jQuery('#notificationPopupAdd').hide();
                }
                jQuery('#notificationPopupView').toggle('slow');
            }
    );
    </script>