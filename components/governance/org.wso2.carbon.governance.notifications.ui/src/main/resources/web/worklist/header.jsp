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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="carbon" uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" %>
<%
    String[] roles;
    WorkItem[] workItems;
    try {
        HumanTaskClient client = new HumanTaskClient(config, session);
        roles = client.getRoles(session);
        workItems = client.getWorkItems(request);
    } catch (Exception ignored) {
        return;
    }
%>
<!-- <script type="text/javascript" src="../ajax/js/prototype.js"></script> -->
<jsp:include page="../registry_common/registry_common-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../registry_common/js/registry_validation.js"></script>
<script type="text/javascript" src="../registry_common/js/registry_common.js"></script>
<carbon:jsi18n
        resourceBundle="org.wso2.carbon.governance.notifications.ui.i18n.JSResources"
        request="<%=request%>" namespace="org.wso2.carbon.governance.notifications.ui"/>
<script type="text/javascript">

    function createTask() {
        var role = jQuery('#workListRoleInput').val();
        var description = jQuery('#workListDescriptionInput').val();
        var priority = jQuery('#workListPriorityInput').val();
        sessionAwareFunction(function() {
            jQuery.ajax({
                url:"../worklist/create-task-ajaxprocessor.jsp",
                async: false,
                type: "post",
                data:{role: role, description: description, priority: priority}
            })
                    .fail(function (jqXHR, textStatus, errorThrown){
                        showRegistryError(jqXHR.responseText);
                    });
        }, org_wso2_carbon_governance_notifications_ui_jsi18n["session.timed.out"]);
    }

    function completeTask(id) {
        sessionAwareFunction(function() {
            jQuery.ajax({
                url:"../worklist/complete-task-ajaxprocessor.jsp",
                type: "post",
                data:{id: id}
            })
                .done(function() {
                    updateNotifications();
                })
                .fail(function (jqXHR, textStatus, errorThrown){
                    showRegistryError(jqXHR.responseText);
                });
        }, org_wso2_carbon_governance_notifications_ui_jsi18n["session.timed.out"]);
        jQuery('#notificationPopupView').toggle('slow');
    }

    function updateNotifications() {
    	jQuery.ajax({
            url:"../worklist/view-notifications-ajaxprocessor.jsp",
            type: "post"
        })
            .done(function (response, textStatus, jqXHR){
                jQuery('#view-notifications-container').html(response);
            });
    }

</script>

<fmt:bundle basename="org.wso2.carbon.governance.notifications.ui.i18n.Resources">
<li class="right" id="view-notifications-container">
    <jsp:include page="view-notifications-ajaxprocessor.jsp" />
</li>
<li class="middle">|</li>
<%--<li class="right">--%>
    <%--<a class="add-notification" id="addNotification">+</a>--%>
    <%--<div id="notificationPopupAdd" class="notificationPopup" style="display:none">--%>
        <%--<div class="popupPointer"></div>--%>
        <%--<div class="popupBox">--%>
            <%--<div class="title"><strong><fmt:message key="work.list.add.new.notifications"/></strong></div>--%>

            <%--<table class="notificationAddTable">--%>
                <%--<tr>--%>
                    <%--<td><fmt:message key="work.list.role"/></td>--%>
                    <%--<td>--%>
                        <%--<select id="workListRoleInput">--%>
                            <%--<%--%>
                                <%--for (String role : roles) {--%>
                            <%--%>--%>
                            <%--<option><%=role%></option>--%>
                            <%--<%--%>
                                <%--}--%>
                            <%--%>--%>
                        <%--</select>--%>
                    <%--</td>--%>
                <%--</tr>--%>
                <%--<tr>--%>
                    <%--<td colspan="2">--%>
                        <%--<div><fmt:message key="work.list.description"/></div>--%>
                        <%--<textarea id="workListDescriptionInput"></textarea>--%>
                    <%--</td>--%>
                <%--</tr>--%>
                <%--<tr>--%>
                    <%--<td><fmt:message key="work.list.priority"/></td>--%>
                    <%--<td>--%>
                        <%--<select id="workListPriorityInput">--%>
                            <%--<%--%>
                                <%--for (int i = 1; i < 11; i++) {--%>
                            <%--%>--%>
                            <%--<option <% if (i == 5) { %>selected="selected"<% } %> ><%=i%></option>--%>
                            <%--<%--%>
                                <%--}--%>
                            <%--%>--%>
                        <%--</select>--%>
                    <%--</td>--%>
                <%--</tr>--%>
            <%--</table>--%>

            <%--<div class="title">--%>
                <%--<input type="button" class="button notificationButton" value="<fmt:message key="work.list.create"/>" id="addNotificationCreateButton" />--%>
                <%--<input type="button" value="<fmt:message key="work.list.cancel"/>" id="addNotificationCancelButton" />--%>
            <%--</div>--%>
        <%--</div>--%>
    <%--</div>--%>
<%--</li>--%>
</fmt:bundle>
<%--<li class="middle">|--%>
<%--<script type="text/javascript">--%>
    <%--jQuery('#addNotification').click(--%>
            <%--function(){--%>
                <%--if(jQuery('#notificationPopupView').is(":visible")){--%>
                    <%--jQuery('#notificationPopupView').hide();--%>
                <%--}--%>
                <%--jQuery('#notificationPopupAdd').toggle('slow');--%>
            <%--}--%>
    <%--);--%>
    <%--jQuery('#addNotificationCancelButton').click(function(){--%>
        <%--jQuery('#notificationPopupAdd').toggle('slow');--%>
    <%--});--%>
    <%--jQuery('#addNotificationCreateButton').click(function(){--%>
        <%--createTask();--%>
		<%--updateNotifications();--%>
        <%--jQuery('#notificationPopupAdd').toggle('slow');--%>
    <%--});--%>
	<%--function updateNotifications(){--%>
        <%--jQuery.ajax({--%>
            <%--url:"../worklist/view-notifications-ajaxprocessor.jsp",--%>
            <%--success:function(data){--%>
                <%--jQuery('#view-notifications-container').html(data);--%>
            <%--}--%>
        <%--});--%>
    <%--}--%>
<%--</script>--%>
    <style>
        div.header-links a.add-notification{
            background-color:#63ba25;
            color:#fff;
            border:solid 1px #3d3d3d;
            padding:2px 5px;
            cursor:pointer;
        }
        div.header-links a.view-notification{
            background-color:#696969;

            background-image: -webkit-gradient( linear, left top, left bottom, from( #696969 ), to( #3f3f3f ) ); /* mozilla - FF3.6+ */
            background-image: -moz-linear-gradient( top, #696969 0%, #3f3f3f 100% );

            color:#fff;
            border:solid 1px #3d3d3d;
            padding:2px 5px;
            cursor:pointer;
        }
        .notificationPopup{
            position:absolute;
            width:380px;
            margin-top:12px;
            background-color:#f0f0f0;
            color:#464646;
			z-index:2000;
        }
        .notificationPopup div.popupPointer{
            background:transparent url(../worklist/images/popupPointer.png) no-repeat right top;
            width:21px;
            height:11px;
            margin-top:-10px;
            margin-left:5px;
            position:absolute;
        }
        .notificationPopup div.popupBox{
            border:solid 1px #bebebe;
            -moz-box-shadow: 0 0 5px 5px #888;
            -webkit-box-shadow: 0 0 5px 5px#888;
            box-shadow: 0 0 5px 5px #888;
        }
        .notificationPopup div.notificationElementWrapper{
            height:290px;
            overflow-y:auto;
        }
        .notificationPopup div.odd{
            background-color:#ccc;
        }
        .notificationPopup div.notificationElement{
            border-bottom:solid 1px #bebebe;
        }
        .notificationPopup div.title{
            background-color:#fefefe;
            background: -webkit-gradient(linear, 0 0, 0 100%, from(#fefefe), to(#f6f6f6)) !important;
            background: -moz-linear-gradient(top, #fefefe, #f6f6f6) !important;
            filter: progid:DXImageTransform.Microsoft.gradient(startColorstr='#fefefe', endColorstr='#f6f6f6') !important;
            -ms-filter: progid:DXImageTransform.Microsoft.gradient(startColorStr='#fefefe', endColorStr='#f6f6f6') !important;


            color:#2b6274;
            font-size:13px;
            height:36px;
            line-height:36px;
            padding:0px 10px;
            border-bottom:solid 1px #5596ab;
        }
        .notificationPopup div.title a {
            cursor:pointer;
        }
        .notificationPopup ul{
            width:100%;
            line-height:30px;
        }
        .notificationPopup ul li.notificationCell3{
            float:right;
        }
        .notificationPopup ul li.notificationCell1{
            width:80px;
        }
        .notificationPopup ul li{
            padding:5px;
        }
        .notificationButton:hover,.notificationButton:focus{
            background: #003c91;
        }
        .notificationButton{
            color:#fff !important;
            text-shadow:none !important;

            background: -webkit-gradient(linear, 0 0, 0 100%, from(#0055cc), to(#003c91)) !important;
            background: -moz-linear-gradient(top, #0055cc, #003c91) !important;
            filter: progid:DXImageTransform.Microsoft.gradient(startColorstr='#0055cc', endColorstr='#003c91') !important;
            -ms-filter: progid:DXImageTransform.Microsoft.gradient(startColorStr='#0055cc', endColorStr='#003c91') !important;
            padding:2px 5px !important;
        }
        .notificationPopup div.notificationDescription{
            border:solid 1px #dcdcdc;
            padding:5px;
            line-height:20px;
            margin:5px;
        }
        .notificationPopup label{
            float:left;
        }
        .notificationPopup div.formElementWrapper{
            float:right;
        }
        .notificationAddTable{
            margin:10px;
            width:100%;
        }
        .notificationAddTable td{
            padding:5px !important;
        }
        .notificationAddTable textarea{
            width:90%;
        }
		div#header-div div.header-links div.right-links div.notificationElement ul{
			background-image: none;
			padding: 0;
		}
    </style></li>
