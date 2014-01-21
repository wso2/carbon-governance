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
<%--<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>--%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
           prefix="carbon" %>

<%@ page
        import="org.apache.axiom.om.OMElement" %>

<%@ page import="org.apache.axiom.om.OMNode" %>
<%@ page
        import="org.apache.axiom.om.impl.builder.StAXOMBuilder" %>
<%@ page
        import="org.apache.axiom.om.xpath.AXIOMXPath" %>
<%@ page
        import="org.wso2.carbon.governance.custom.lifecycles.checklist.stub.beans.xsd.LifecycleBean" %>


<%@ page import="org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property" %>
<%@ page
        import="org.wso2.carbon.governance.custom.lifecycles.history.ui.clients.LifecycleServiceClient" %>
<%@ page
        import="org.wso2.carbon.governance.custom.lifecycles.history.ui.clients.ResourceServiceClient" %>
<%@ page
        import="org.wso2.carbon.governance.custom.lifecycles.history.ui.clients.WSRegistryServiceClient" %>
<%@ page
        import="org.wso2.carbon.governance.custom.lifecycles.history.ui.utils.DurationCalculator" %>
<%@ page import="org.wso2.carbon.registry.common.utils.RegistryUtil" %>
<%@ page import="org.wso2.carbon.registry.core.RegistryConstants" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>

<%@ page
        import="javax.xml.namespace.QName" %>
<%@ page import="java.io.ByteArrayInputStream" %>
<%@ page import="java.util.List" %>

<%
    final String LOG_DEFAULT_PATH = "/_system/governance/repository/components/org.wso2.carbon.governance/lifecycles/history/";
    final String REGISTRY_LIFECYCLE_HISTORY_ORIGINAL_PATH = "registry.lifecycle_history.originalPath";
    final String NOT_AVAILABLE = "N/A";

    String path = RegistryUtil.getPath(request);
    if (path.equals(RegistryConstants.ROOT_PATH) || path.equals(RegistryConstants.SYSTEM_COLLECTION_BASE_PATH)
            || path.equals(RegistryConstants.CONFIG_REGISTRY_BASE_PATH)
            || path.equals(RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH)
            || path.equals(RegistryConstants.LOCAL_REPOSITORY_BASE_PATH))
        return;

    LifecycleBean bean;

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
        if (lifecycleProps == null) {
            lifecycleProps = new Property[0];
        }

        if (lifecycleProps.length == 0) {
            return;
        }
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        ResourceServiceClient resourceServiceClient = new ResourceServiceClient(cookie, config, session);
        WSRegistryServiceClient wsRegistryServiceClient = new WSRegistryServiceClient(cookie, config, session);

        String originalPath =
                resourceServiceClient.getProperty(path, REGISTRY_LIFECYCLE_HISTORY_ORIGINAL_PATH);
        //if REGISTRY_LIFECYCLE_HISTORY_ORIGINAL_PATH property is set use it as the resource path
        //else use the path variable passed with url
        if (originalPath == null) {
            originalPath = path.replaceAll("/", "_");

        } else {
            originalPath = originalPath.replaceAll("/", "_");

        }
        List<OMNode> transitionNodesList = null;
        List<OMNode> associationNodesList = null;
        String content = null;
        //if log file for the particular resource exists read the content
        if (wsRegistryServiceClient.resourceExists(LOG_DEFAULT_PATH + originalPath)) {

            content = resourceServiceClient.getTextContent(LOG_DEFAULT_PATH + originalPath);

            if (content != null) {
                OMElement documentElement = new
                        StAXOMBuilder(new ByteArrayInputStream(content.getBytes())).getDocumentElement();
                // to select 'item' nodes with an attribute 'targetState'
                AXIOMXPath targetStateXpath = new AXIOMXPath("//item[@targetState]");
                // to select nodes with type="association"
                AXIOMXPath associationXpath = new AXIOMXPath("//item/action[@type='association']");
                transitionNodesList = targetStateXpath.selectNodes(documentElement);
                associationNodesList = associationXpath.selectNodes(documentElement);

            }
        }
%>
<carbon:jsi18n
        request="<%=request%>"
        namespace="org.wso2.carbon.governance.custom.lifecycles.history.ui"/>


<div class="box1-head" style="height:auto;">
    <table cellspacing="0" cellpadding="0" border="0" style="width: 100%">
        <tr>

            <td valign="top">
                <h2 class="sub-headding-lifecycle">Lifecycle History</h2>
            </td>
            <td align="right" valign="top" class="expanIconCell"><a
                    onclick="showHideCommon('lifecycleHistoryIconExpanded');showHideCommon('lifecycleHistoryIconMinimized');showHideCommon('lifecycleHistoryExpanded');showHideCommon('lifecycleHistoryMinimized');">

                <img
                        src="images/icon-expanded.gif" border="0" align="top"
                        id="lifecycleHistoryIconExpanded" style="display: none;"/> <img
                    src="images/icon-minimized.gif" border="0" align="top"
                    id="lifecycleHistoryIconMinimized"/> </a></td>

        </tr>
    </table>
</div>


<div class="box1-mid-fill" id="lifecycleHistoryMinimized"></div>
<div class="box1-mid" id="lifecycleHistoryExpanded"
     style="max-height:200px;overflow-y:auto;overflow-x:hidden;display:none">

    <%
     //if the history log is properly retrieved or lifecycle is associated only show the history details
        if (content != null && associationNodesList.size() > 0) { %>
    <table class="styledLeft">
        <thead>
        <tr>
            <th>Target State</th>
            <th>User</th>
            <th>Timestamp</th>
            <%if (associationNodesList.size() > 0) {%>
            <th>Duration</th>
            <%}%>
        </tr>
        </thead>
        <tbody>

        <%
            String initialState = null, associationTimestamp = null, initialStateDuration = null,
                    initialUser = null;
            String[] splitByColon;
            //get the latest association action and obtain the 'item' OMElement of it.
            OMElement initialStateOMElement = (OMElement) associationNodesList.get(0).getParent();
            initialState
                    = initialStateOMElement.getAttribute(new QName("state")).getAttributeValue();
            initialUser = initialStateOMElement.getAttribute(new QName("user")).getAttributeValue();
            associationTimestamp = initialStateOMElement.getAttribute(new QName("timestamp")).getAttributeValue();
            //split the timestamp by colon and view only up-to minutes
            splitByColon = associationTimestamp.split(":");

            String associationTimestampToPrint = splitByColon[0] + ":" + splitByColon[1];
            DurationCalculator durationCalculator = new DurationCalculator();

            if (transitionNodesList.size() == 0) {
                initialStateDuration = NOT_AVAILABLE;
            } else {
                OMElement firstTransitionNode = (OMElement)
                        transitionNodesList.get(transitionNodesList.size() - 1);
                String firstTransitionTimestamp =
                        firstTransitionNode.getAttribute(new QName("timestamp")).getAttributeValue();
                //calculate the time duration in the initial state
                initialStateDuration = durationCalculator.calculateDifference(firstTransitionTimestamp, associationTimestamp);

            }
        %>

        <tr>

            <td><%=initialState%>
            </td>
            <td><%=initialUser%>
            </td>
            <td><%=associationTimestampToPrint%>
            </td>
            <% if (initialStateDuration != null) {%>
            <td><%=initialStateDuration%>
            </td>
            <% initialStateDuration = null;
            } else {%>
            <td><%=NOT_AVAILABLE%>
            </td>
            <%}%>
        </tr>


        <%
            String targetState = null, startTimestamp = null, timeDurationString = null, user =
                    null;


            for (int i = transitionNodesList.size() - 1; i >= 0; i--) {
                OMElement currentOMElement = (OMElement) transitionNodesList.get(i);
                targetState = currentOMElement.getAttribute(new QName("targetState")).getAttributeValue();
                user = currentOMElement.getAttribute(new QName("user")).getAttributeValue();
                startTimestamp =
                        currentOMElement.getAttribute(new QName("timestamp")).getAttributeValue();
                splitByColon = startTimestamp.split(":");

                String timeStampToPrint = splitByColon[0] + ":" + splitByColon[1];

                if ((i - 1) >= 0) {
                    OMElement nextOMElement = (OMElement) transitionNodesList.get(i - 1);
                    String endTimestamp =
                            nextOMElement.getAttribute(new QName("timestamp")).getAttributeValue();
                    //get the time duration elapsed in the current state
                    timeDurationString = durationCalculator.calculateDifference(endTimestamp, startTimestamp);
                }
        %>
        <tr>

            <td><%=targetState%>
            </td>
            <td><%=user%>
            </td>
            <td><%=timeStampToPrint%>
            </td>
            <% if (timeDurationString != null) {%>
            <td><%=timeDurationString%>
            </td>
            <% timeDurationString = null;
            } else {%>
            <td>N/A</td>
            <%}%>
        </tr>
        <%
            }
        %>
        </tbody>
    </table>
    <% } else {
        out.println("History not available");
    } %>

</div>
<%
    }
%>