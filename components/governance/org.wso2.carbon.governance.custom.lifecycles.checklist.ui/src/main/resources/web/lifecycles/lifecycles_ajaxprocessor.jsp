<!--
~ Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
           prefix="carbon" %>

<%@ page import="org.wso2.carbon.governance.custom.lifecycles.checklist.stub.beans.xsd.LifecycleBean" %>
<%@ page
        import="org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.LifecycleActions" %>
<%@ page
        import="org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property" %>
<%@ page
        import="org.wso2.carbon.governance.custom.lifecycles.checklist.ui.clients.LifecycleServiceClient" %>
<%@ page import="org.wso2.carbon.registry.common.utils.RegistryUtil" %>
<%@ page import="org.wso2.carbon.registry.core.RegistryConstants" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.util.List" %>
<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>
<%@ page import="org.wso2.carbon.governance.custom.lifecycles.checklist.ui.clients.LifecycleManagementServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.governance.custom.lifecycles.checklist.ui.Beans.CurrentStateDurationBean" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.wso2.carbon.governance.lcm.stub.beans.xsd.CheckpointBean" %>

<%
    class CheckListItem implements Comparable {
        String lifeCycleStatus;
        String name;
        String value;
        String order;
        String propertyName;
        String visible;

        public String getVisible() {
            return visible;
        }

        public void setVisible(String visible) {
            this.visible = visible;
        }

        @SuppressWarnings("unused")
        public String getPropertyName() {
            return propertyName;
        }

        @SuppressWarnings("unused")
        public void setPropertyName(String propertyName) {
            this.propertyName = propertyName;
        }

        @SuppressWarnings("unused")
        public String getLifeCycleStatus() {
            return lifeCycleStatus;
        }

        public void setLifeCycleStatus(String lifeCycleStatus) {
            this.lifeCycleStatus = lifeCycleStatus.replace("."," ");
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getOrder() {
            return order;
        }

        public void setOrder(String order) {
            this.order = order;
        }

        @SuppressWarnings("unused")
        public CheckListItem(String lifeCycleStatus, String name, String value, String order) {
            this.lifeCycleStatus = lifeCycleStatus;
            this.name = name;
            this.value = value;
            this.order = order;
        }

        public CheckListItem() {

        }

        public boolean matchLifeCycleStatus(String status, boolean ignoreCase) {
            if ((lifeCycleStatus == null) || (status == null)) {
                return false;
            }

            if (ignoreCase)
                return lifeCycleStatus.equalsIgnoreCase(status);
            else
                return lifeCycleStatus.equals(status);
        }

        public boolean matchLifeCycleStatus(String status) {
            return matchLifeCycleStatus(status, true);
        }

        public int compareTo(Object anotherItem) {
            if (!(anotherItem instanceof CheckListItem))
                return 0;

            try {
                CheckListItem item = (CheckListItem) anotherItem;
                int otherItemOrder = Integer.parseInt(item.getOrder());
                int itemOrder = Integer.parseInt(order);

                return itemOrder - otherItemOrder;
            } catch (Exception e) {
                /* suppressing any parsing errors, since order is not "that" important to consider. */
            }
            return 0;
        }
    }

	class ApproveItem extends CheckListItem {
		private int currentVote;
		private int requiredVote;
		
		public int getCurrentVote() {
			return currentVote;
		}
		
		public void setCurrentVote(int currentVote) {
			this.currentVote = currentVote;
		}
		
		public int getRequiredVote() {
			return requiredVote;
		}
		
		public void setRequiredVote(int requiredVote) {
			this.requiredVote = requiredVote;
		}			
	}

    String path = RegistryUtil.getPath(request);
    String lcName = request.getParameter("aspect");

    // lifecycle portlet is not displayed for root or items under system.
    if (path.equals(RegistryConstants.ROOT_PATH) || path.equals(RegistryConstants.SYSTEM_COLLECTION_BASE_PATH)
            || path.equals(RegistryConstants.CONFIG_REGISTRY_BASE_PATH)
            || path.equals(RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH)
            || path.equals(RegistryConstants.LOCAL_REPOSITORY_BASE_PATH)) {
        return;
    }

    LifecycleBean bean;
    String[] roleNames;

    try {
        LifecycleServiceClient lifecycleServiceClient = new LifecycleServiceClient(config, session);

        bean = lifecycleServiceClient.getLifecycleBean(path);
    } catch (Exception e) {
        bean = null;
    }

    if (bean != null) {
        if (bean.getLink()) {
            return;
        }

        Property[] lifecycleProps = bean.getLifecycleProperties();
        roleNames =  bean.getRolesOfUser();

        if (lifecycleProps == null) {
            lifecycleProps = new Property[0];
        }
        
        Property[] lifecycleVotes = bean.getLifecycleApproval();
        if (lifecycleVotes == null) {
        	lifecycleVotes = new Property[0];
        }

        LifecycleActions[] actionsAvailable = bean.getAvailableActions();
        List<String> actionsNameList = new ArrayList<String>();

        for (int lcIndex = 0; lcIndex < actionsAvailable.length; lcIndex++) {
            if(actionsAvailable[lcIndex] != null) {
                actionsNameList.add(actionsAvailable[lcIndex].getLifecycle());
            }
        }

        String[] aspectsToAdd = bean.getAspectsToAdd();
        List<String> availableAspectsToAdd = new ArrayList<String>();

        for(int aspectList = 0 ; aspectList < aspectsToAdd.length ; aspectList++) {
            if(!actionsNameList.contains(aspectsToAdd[aspectList])) {
                availableAspectsToAdd.add(aspectsToAdd[aspectList]);
            }
        }

        String[] availableAspectsToAddArray = availableAspectsToAdd.toArray(new String[availableAspectsToAdd.size()]);
%>
<carbon:jsi18n
        resourceBundle="org.wso2.carbon.governance.custom.lifecycles.checklist.ui.i18n.JSResources"
        request="<%=request%>" namespace="org.wso2.carbon.governance.custom.lifecycles.checklist.ui"/>
<script type="text/javascript" src="../lifecycles/js/lifecycles.js"></script>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>

<fmt:bundle
        basename="org.wso2.carbon.governance.custom.lifecycles.checklist.ui.i18n.Resources">

<div class="box1-head" style="height:auto;">
    <table cellspacing="0" cellpadding="0" border="0" style="width: 100%">
        <tr>

            <td valign="top">
                <h2 class="sub-headding-lifecycle"><fmt:message key="lifecycle"/></h2>
            </td>
            <td align="right" valign="top" class="expanIconCell"><a
                    onclick="showHideCommon('lifecycleIconExpanded');showHideCommon('lifecycleIconMinimized');showHideCommon('lifecycleExpanded');showHideCommon('lifecycleMinimized');">
                <% if (lifecycleProps.length == 0) { %> <img
                    src="images/icon-expanded.gif" border="0" align="top"
                    id="lifecycleIconExpanded" style="display: none;"/> <img
                    src="images/icon-minimized.gif" border="0" align="top"
                    id="lifecycleIconMinimized"/> <% } else { %> <img
                    src="images/icon-expanded.gif" border="0" align="top"
                    id="lifecycleIconExpanded"/> <img src="images/icon-minimized.gif"
                                                      border="0" align="top" id="lifecycleIconMinimized"
                                                      style="display: none;"/> <% } %></a></td>

        </tr>
    </table>
</div>

<% if (lifecycleProps.length == 0) { %>
<div class="box1-mid-fill" id="lifecycleMinimized"></div>
<div class="box1-mid" id="lifecycleExpanded" style="display: none;" >
<% } else { %>
<div class="box1-mid-fill" id="lifecycleMinimized" style="display: none;"></div>
<div class="box1-mid" id="lifecycleExpanded">
<% } %>
<div id="lifecycleSum"></div>

<!-- Life cycle add box -->
<% if (availableAspectsToAddArray.length > 0 && bean.getLoggedIn() && !bean.getVersionView() && bean.getPutAllowed() && bean.getShowAddDelete() &&
        bean.getAspectsToAdd() != null && bean.getAspectsToAdd().length != 0) { %>
<div class="icon-link-ouside registryWriteOperation"><a class="icon-link registryWriteOperation"
                                                        style="background-image: url(../admin/images/add.gif);"
                                                        href="javascript:void(0)"
                                                        onclick="javascript:showHideCommon('add-lifecycle-div');if($('add-lifecycle-div').style.display!='none')$('aspect').focus();">
    <fmt:message key="add.lifecycle"/></a></div>

<div class="registryWriteOperation" id="add-lifecycle-div"
     <%--style="display: none; padding-bottom: 10px;">--%>
     style="padding-bottom: 10px;">
    <form>
        <table class="styledLeft">
            <thead>
            <tr>
                <th><fmt:message key="enable.lifecycle"/></th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td><input type="hidden" id="aspectResourcePath"
                           name="resourcePath" value="<%=bean.getPathWithVersion()%>"/> <select
                        id="aspect" name="aspect" style="width: 130px">
                    <%
                        for (String anAspectsToAdd : availableAspectsToAddArray) {
                    %>
                    <option value="<%=anAspectsToAdd%>"><%=anAspectsToAdd%>
                    </option>
                    <%
                        }
                    %>
                </select></td>
            </tr>
            <tr>
                <td class="buttonRow"><input type="button" class="button"
                                             value="<fmt:message key="add"/>" onclick="addAspect();"/> <input
                        type="button" class="button" value="<fmt:message key="cancel"/>"
                        onclick="showHideCommon('add-lifecycle-div');"/></td>
            </tr>
            </tbody>
        </table>
    </form>
</div>
    <%if (lifecycleProps.length == 0) { %>
    <!-- Life cycle add box ends -->
    <div id="lifecyclesSummary" class="summeryStyle"><fmt:message
        key="no.lifecycles"/></div>
    <% } %>
<%
} else if (lifecycleProps.length == 0) {
%>
<div id="lifecyclesSummary" class="summeryStyle"><fmt:message
        key="no.lifecycles"/></div>
<%
    }
    if (lifecycleProps.length > 0) {
%>

<!-- Showing associated life cycles to view -->
<div class="registryShowOperation" id="show-lifecycle-div">
    <form>
        <table class="styledLeft">
            <thead>
            <tr>
                <th>Attached Life-Cycles</th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td>
                    <select id="attachedAspect" name="attachedAspect" onchange="refreshUpdatedLifeCyclesSection('<%=path%>')" style="width: 130px">
                        <%
                            int lifeCycleIndex = 0;

                            for (int aspIndex = 0; aspIndex < actionsAvailable.length; aspIndex++) {
                                String lifeCycleName = actionsAvailable[aspIndex].getLifecycle();
                                boolean isSelected = (lcName != null && lifeCycleName.equalsIgnoreCase(lcName)) ;
                                if(isSelected) {
                                    lifeCycleIndex = aspIndex ;
                                }
                        %>
                        <option value="<%=lifeCycleName%>" <%if(isSelected) {%>selected<%}%> ><%=lifeCycleName%>
                        </option>
                        <%
                            }
                        %>
                    </select></td>
            </tr>
            </tbody>
        </table>
    </form>
</div> <!-- END of associated lifecycle listing------>

<!-- START life cycle listing box -->
<div id="aspectList">
<table class="styledLeft">
<tbody>
<tr>
    <td>
        <%
            if(lcName == null || ("".equals(lcName))) {
                lcName = actionsAvailable[0].getLifecycle();
                lifeCycleIndex = 0;
            }

            String lifeCycleLongName = "";
            String lifeCycleState = "";

            for (Property property : lifecycleProps) {
                String propName = property.getKey();
                String[] propValues = property.getValues();

                if (propValues != null && propValues.length != 0) {
                    String value = propValues[0];
                    String prefix = "registry.lifecycle.";
                    String suffix = ".state";
                    if (propName.startsWith(prefix) && propName.endsWith(suffix) && propName.contains(lcName)) {
                        lifeCycleState = value;
                        // Grab the text in between...
                        String lifecycleName = propName.substring(prefix.length(),
                                propName.length() - suffix.length());

                        if (lifeCycleLongName.equals(""))
                            lifeCycleLongName = lifecycleName;
                    }
                }
            }

            String lifecycleName = "";
            LifecycleActions[] availableActions = bean.getAvailableActions();

            if (availableActions != null && availableActions.length > 0) {
                LifecycleActions lifecycleActions = availableActions[lifeCycleIndex];
                if (lifecycleActions != null) {
                    lifecycleName = lifecycleActions.getLifecycle();
                }
            }

            ArrayList<CheckListItem> checkListItems = new ArrayList<CheckListItem>();
            List<String> permissionList = new ArrayList();

            String defaultLC = null ;

            for (Property property : lifecycleProps) {
                String prefix = "registry.custom_lifecycle.checklist.";
                String permissionSuffix = ".item.permission";
                String propName = property.getKey();
                String[] propValues = property.getValues();

                if(propName.equals("registry.LC.name")) {
                    defaultLC = propValues[0];
                }

                if(propName.startsWith(prefix) && propName.endsWith(permissionSuffix) && propName.contains(lcName)){
                    for (String role : roleNames) {
                        for (String propValue : propValues) {
                            String key = propName.replace(prefix,"").replace(permissionSuffix,"");
                            if(propValue.equals(role)){
                                permissionList.add(key);
                            } else if(propValue.startsWith(prefix) && propValue.endsWith(permissionSuffix)){
                                permissionList.add(key);
                            }
                        }
                    }
                }
            }

            for (Property property : lifecycleProps) {
                String prefix = "registry.custom_lifecycle.checklist.";
                String suffix = ".item";
                CheckListItem checkListItem = new CheckListItem();

                String propName = property.getKey();
                String[] propValues = property.getValues();

                checkListItem.setVisible("false");

                if ((propName.startsWith(prefix) && propName.endsWith(suffix) && propName.contains(lcName))) {
                    if (propValues != null && propValues.length > 2) {
                        for (String param : propValues) {
                            if ((param.startsWith("status:"))) {
                                checkListItem.setLifeCycleStatus(param.substring(7));
                            }
                            if ((param.startsWith("name:"))) {
                                checkListItem.setName(param.substring(5));
                            }
                            if ((param.startsWith("value:"))) {
                                checkListItem.setValue(param.substring(6));
                            }
                            if ((param.startsWith("order:"))) {
                                checkListItem.setOrder(param.substring(6));
                            }
                        }
                    }

                    String key = propName.replace(prefix,"").replace(suffix,"");
                    if(permissionList.contains(key)){
                        checkListItem.setVisible("true");
                    }
                }

                if (checkListItem.matchLifeCycleStatus(lifeCycleState)) {
                    checkListItems.add(checkListItem);
                }
            }

            String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
            ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
                    .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
            String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
            LifecycleManagementServiceClient lifecycleManagementServiceClient;

            String currentLifecycleStateDuration;
            String currentLifecycleStateDurationColour = null;
            lifecycleManagementServiceClient = new LifecycleManagementServiceClient(cookie, serverURL,
                    configContext);
            CurrentStateDurationBean currentStateDurationBean = lifecycleManagementServiceClient
                    .getLifecycleCurrentStateDuration(path, lifecycleName);

            if (currentStateDurationBean != null) {
                currentLifecycleStateDuration = currentStateDurationBean.getDuration();
                CheckpointBean checkpointBean = currentStateDurationBean.getCheckpointBean();
                if (checkpointBean != null) {
                    currentLifecycleStateDurationColour = checkpointBean.getDurationColour();
                } else {
                    currentLifecycleStateDurationColour = lifecycleManagementServiceClient
                            .currentStateDurationDefaultColour;
                }
            } else {
                currentLifecycleStateDuration = lifecycleManagementServiceClient.timeNotAvailableMessage;
            }

            Collections.sort(checkListItems);

            if (!bean.getVersionView()) {
        %>

        <div style="height:0px;">
            <input type="hidden" id="resPath" name="resPath" value="<%=bean.getPathWithVersion()%>"/>
            <input type="hidden" id="aspectName" name="aspectName" value="<%=lifecycleName%>"/>
            <% if (bean.getPutAllowed() && bean.getShowAddDelete()) { %>
            <a class="icon-link registryWriteOperation"
               style="float:right;background-image:url(../admin/images/delete.gif);"
               onclick="removeAspect();" title="<fmt:message key="delete"/>">
                <% } %>
            </a>
        </div>
        <% } %>
        <div>
            <table cellpadding="0" cellspacing="5" border="0">
                <tbody>
                <tr>
                    <th><fmt:message key="lifecycle.name"/>:</th>
                    <td style="border:0;padding-right:20px;"><%=lifeCycleLongName%>
                    </td>
                </tr>
                <tr>
                    <th><fmt:message key="lifecycle.state"/>:</th>
                    <td style="border:0"><%=lifeCycleState%>
                    </td>
                </tr>
                <tr>
                    <th><fmt:message key="lifecycle.currentLifecycleStateDuration"/>:</th>
                    <% if(StringUtils.isNotEmpty(currentLifecycleStateDurationColour)) {%>
                    <td style="border:0; color: <%=currentLifecycleStateDurationColour%>">
                        <%=currentLifecycleStateDuration%></td>
                    <%} else {%>
                    <td style="border:0;"><%=currentLifecycleStateDuration%></td>
                    <%}%>
                </tr>
                <tr>
                    <th>Make this default:</th>
                    <td style="border:0">
                        <% if(lcName.equals(defaultLC)) {%>
                        <input type="checkbox" onclick="onChangeDefaultLifeCycle(this, '<%=path%>', '<%=lcName%>')" id="defaultLcCheckBox" checked/>
                        <%} else {%>
                        <input type="checkbox" onclick="onChangeDefaultLifeCycle(this, '<%=path%>', '<%=lcName%>')" id="defaultLcCheckBox"/>
                        <%}%>
                    </td>
                </tr>
                <%
                    if (checkListItems.size() > 0) {
                %>
                <tr>
                    <th><fmt:message key="checklist.header"/> : </th>
                    <td style="border:0"></td>
                </tr>
                <% } %>
                </tbody>
            </table>
        </div>

        <div>
            <table style="margin-bottom: 15px;" class="styledLeft" id="myTable">
                <tbody>
                <%

                    int index = 0;
                    for (CheckListItem item : checkListItems) {
                        if ((index % 2) == 0) { %>
                <tr class="tableEvenRow">
                            <%      } else { %>

                <tr class="tableOddRow">
                    <% } %>
                    <td>
                        <%
                            if (item.getValue().equalsIgnoreCase("true")) {
                        %> <input type="checkbox" class="registryWriteOperation" id="option<%=index%>"
                                  value="true" checked
                            <% if (!bean.getPutAllowed() || !bean.getLoggedIn() || bean.getVersionView() || "false".equals(item.getVisible())) {%>
                                  disabled="disabled" <%}%>
                                  onclick="loadCustomUI('<%=bean.getPathWithVersion()%>', '<%=lifecycleName%>', 'itemClick','<%=bean.getMediaType()%>','','')">
                        <input type="checkbox" class="registryNonWriteOperation" id="optionX<%=index%>"
                               value="true" checked disabled="disabled">
                        <b><%=item.getName()%>
                        </b><br>
                        <% } else {
                        %> <input type="checkbox" class="registryWriteOperation" id="option<%=index%>"
                                  value="true"
                            <% if (!bean.getPutAllowed() || !bean.getLoggedIn() || bean.getVersionView()|| "false".equals(item.getVisible())) {%>
                                  disabled="disabled" <%}%>
                                  onclick="loadCustomUI('<%=bean.getPathWithVersion()%>', '<%=lifecycleName%>', 'itemClick','<%=bean.getMediaType()%>','','')">
                        <input type="checkbox" class="registryNonWriteOperation" id="optionX<%=index%>"
                               value="true" disabled="disabled">
                        <b><%=item.getName()%>
                        </b><br>
                        <% } %>
                    </td>
                </tr>
                <%
                        index++;
                    }
                %>
                <input type="hidden" id="itemcount" value="<%=index%>">
                </tbody>
            </table>
        </div>
        
        <% 
        	ArrayList<ApproveItem> approveListItems = new ArrayList<ApproveItem>();
        	if (lifecycleVotes != null && lifecycleVotes.length > 0) { %>
		<%
		
		ArrayList approvePermissionList = new ArrayList();
		
		for (Property property : lifecycleVotes) {
		    String prefix = "registry.custom_lifecycle.votes.";
		    String permissionSuffix = ".vote.permission";
		    String propName = property.getKey();
		    String[] propValues = property.getValues();

		    if(propName.startsWith(prefix) && propName.endsWith(permissionSuffix) && propName.contains(lcName)){
		        for (String role : roleNames) {
		            for (String propValue : propValues) {
		                String key = propName.replace(prefix,"").replace(permissionSuffix,"");
		                if(propValue.equals(role)){
		                	approvePermissionList.add(key);
		                }else if(propValue.startsWith(prefix) && propValue.endsWith(permissionSuffix)){
		                	approvePermissionList.add(key);
		                }
		            }
		        }
		    }
		}

		for (Property property : lifecycleVotes) {
		    String prefix = "registry.custom_lifecycle.votes.";
		    String suffix = ".vote";
		    ApproveItem approveItem = new ApproveItem();
		
		    String propName = property.getKey();
		    String[] propValues = property.getValues();
		
		    approveItem.setVisible("false");
		
		    if ((propName.startsWith(prefix) && propName.endsWith(suffix) && propName.contains(lcName))) {
		        if (propValues != null && propValues.length > 2) {
		            for (String param : propValues) {
		                if ((param.startsWith("status:"))) {
		                	approveItem.setLifeCycleStatus(param.substring(7));
		                }
		                if ((param.startsWith("name:"))) {
		                	approveItem.setName(param.substring(5));
		                }
		                if ((param.startsWith("uservote:"))) {
		                	approveItem.setValue(param.substring(9));
		                }
		                if ((param.startsWith("votes:"))) {
		                	approveItem.setRequiredVote(Integer.parseInt(param.substring(6)));
		                }
		                if ((param.startsWith("current:"))) {
		                	approveItem.setCurrentVote(Integer.parseInt(param.substring(8)));
		                }
		                if ((param.startsWith("order:"))) {
		                	approveItem.setOrder(param.substring(6));
                        }
		            }
		        }

		        String key = propName.replace(prefix,"").replace(suffix,"");

		        if(approvePermissionList.contains(key)){
		        	approveItem.setVisible("true");
		        }
		    }

		    if (approveItem.matchLifeCycleStatus(lifeCycleState)) {
		    	approveListItems.add(approveItem);
		    }
		}	
		Collections.sort(approveListItems);
		%>
		 <div>
	            <table cellpadding="0" cellspacing="5" border="0">
	                <tbody>
		                <tr>
		                    <th><fmt:message key="lifecycle.approvals"/> : </th>
		                </tr>
		 			</tbody>
	            </table>
	        </div>
		 <div>
            <table style="margin-bottom: 15px;" class="styledLeft" id="myTable">
                <tbody>
				<%
				int count  = 0;
				 for (ApproveItem approvelItem : approveListItems) {
				%>
					<% if ((count % 2) == 0) { %>
	                	<tr class="tableEvenRow">
	                 <% } else { %>
	                	<tr class="tableOddRow">
	                <% } %>
	                <td>
	                <%
	                   if (approvelItem.getValue().equalsIgnoreCase("true")) {
	                %>
	                	<input type="checkbox" class="registryWriteOperation" id="vote<%=count%>" value="true" onclick="loadCustomUI('<%=bean.getPathWithVersion()%>', '<%=lifecycleName%>', 'voteClick','<%=bean.getMediaType()%>','','')"  checked="checked"
	                	<% if (!bean.getPutAllowed() || !bean.getLoggedIn() || bean.getVersionView() || "false".equals(approvelItem.getVisible())) {%> disabled="disabled" <%} %>
	                	 >
	                 	<b><%=approvelItem.getName()%>
	                 <%} else {%>
	                	<input type="checkbox" class="registryWriteOperation" id="vote<%=count%>" value="true" onclick="loadCustomUI('<%=bean.getPathWithVersion()%>', '<%=lifecycleName%>', 'voteClick','<%=bean.getMediaType()%>','','')"
	                	<% if (!bean.getPutAllowed() || !bean.getLoggedIn() || bean.getVersionView() || "false".equals(approvelItem.getVisible())) {%> disabled="disabled" <%} %>
	                	>
						<b><%=approvelItem.getName()%>   
	                <%}%> 
					</b> <% if(approvelItem.getRequiredVote()- approvelItem.getCurrentVote() >= 0) { %>
							<span id="remainVote" style="padding-left:10px"> <fmt:message key="lifecycle.approvalsoutof"><fmt:param value="<%=approvelItem.getCurrentVote()%>"/><fmt:param value="<%=approvelItem.getRequiredVote()%>"/></fmt:message> </span>
						 <% } else { %>
						 	<span id="remainVote" style="padding-left:10px"><fmt:message key="lifecycle.votereched"><fmt:param value="<%=approvelItem.getCurrentVote()%>"/></fmt:message></span>
						 <% } %>
					<br>
					
				<% 
					count++;
				 } 
				 %>
				 <input type="hidden" id="approvalCount" value="<%=count%>">
				</td>
                </tr>
 				</tbody>
            </table>
     	</div>
	
	<% } %>
        
    </td>
</tr>
<%
    if (availableActions != null && availableActions.length > 0) { %>

<tr>
    <td class="buttonRow">
        <div id="lifeCycleButtons">
            <%
                LifecycleActions lifecycleActions = availableActions[lifeCycleIndex];
                if (lifecycleActions != null) {
                    String lifecycle = lifecycleActions.getLifecycle();
                    String[] actions = lifecycleActions.getActions();
                    if (actions == null) actions = new String[0];

                    for (String action : actions) {
                        if (bean.getLoggedIn() && !bean.getVersionView() && bean.getPutAllowed()) {
                            String lifecycleScript = "";
                            String lifecycleScriptCommand = "";
                            String customUILink = "";

                            for (Property property : lifecycleProps) {
                                String propName = property.getKey();
                                if(propName.equals("registry.custom_lifecycle.checklist.transition.ui." + lcName + "." + action)){
                                    String[] propertyValues = property.getValues();

                                    if(propertyValues.length==1) {
                                        customUILink = propertyValues[0];
                                        break;
                                    }

                                }
                            }

                            for (Property property : lifecycleProps) {
                                String propName = property.getKey();
                                String prefix = "registry.custom_lifecycle.checklist.js.script.console";
                                String suffix = lifeCycleState + "." + action;
                                if (propName.startsWith(prefix) && propName.endsWith(suffix) && propName.contains(lcName) ) {
                                    String propValues[] = property.getValues();

                                    if (propValues != null && propValues.length == 2) {
                                        if (propValues[0].contains("function()")||propValues[0].contains("function "+propValues[1]+"()")) {
                                            lifecycleScript = propValues[0];
                                            if (!customUILink.equals("")) {
                                                lifecycleScriptCommand = "'" + propValues[1] + "'";
                                            } else {
                                                lifecycleScriptCommand = "function() {" +
                                                        propValues[1] + "();}";
                                            }
                                        } else {
                                            lifecycleScript = propValues[1];
                                            if (!customUILink.equals("")) {
                                                lifecycleScriptCommand = "'" + propValues[0] + "'";
                                            } else {
                                                lifecycleScriptCommand = "function() {" +
                                                        propValues[0] + "();}";
                                            }
                                        }
                                        break;
                                    }
                                }
                            }
            %>
            <%=StringEscapeUtils.unescapeXml(lifecycleScript)%>
            <%
            boolean actionDisable = false;
            if (!approveListItems.isEmpty()) {
            	for (ApproveItem approvelItem : approveListItems) {
            		if(approvelItem.getName().equals(action) && (approvelItem.getRequiredVote()-approvelItem.getCurrentVote() > 0)){
            			actionDisable = true;
            		}
            	}
            }
            
            %>
            
            <input class="button registryWriteOperation" type="button" id="<%=action%>" <%if(actionDisable){ %> disabled="disabled" <%} %>
                   value="<fmt:message key="action.lifecycle"><fmt:param value="<%=action%>"/></fmt:message>"
                   onclick="loadCustomUI('<%=bean.getPathWithVersion()%>', '<%=lifecycle%>', '<%=action%>','<%=bean.getMediaType()%>'
                           ,'<%=customUILink%>'<% if (!lifecycleScriptCommand.equals("")) {%>,
                           <%=lifecycleScriptCommand%><%}%>);reloadLifecycleHistoryView('<%=path%>')"/>
            <input class="button registryNonWriteOperation" type="button" disabled="disabled"
                   value="<fmt:message key="action.lifecycle"><fmt:param value="<%=action%>"/></fmt:message>"/>
            <% }
            }
            } %>
        </div>
    </td>
</tr>
<%
    } %>
</tbody>
</table>
</div>
<!-- END lifecycle listing box -->
<% } %>
<div id="customUIDiv" style="display:block">

</div>
</div>
</fmt:bundle>
<% }

%>

