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
<%@ taglib prefix="carbon" uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.governance.lcm.ui.clients.LifeCycleManagementServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>

<link type="text/css" rel="stylesheet" href="css/menu.css"/>
<link type="text/css" rel="stylesheet" href="css/style.css"/>

<!--Yahoo includes for dom event handling-->
<script src="../yui/build/yahoo-dom-event/yahoo-dom-event.js" type="text/javascript"></script>

<!--EditArea javascript syntax hylighter -->
<script language="javascript" type="text/javascript" src="../editarea/edit_area_full.js"></script>

<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<carbon:jsi18n
		resourceBundle="org.wso2.carbon.governance.lcm.ui.i18n.JSResources"
		request="<%=request%>" namespace="org.wso2.carbon.governance.lcm.ui"/>
<script type="text/javascript" src="js/lcm.js"></script>
<%
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    String temp = "";
    boolean isNew = true;
    boolean viewMode = false;
    try{
        LifeCycleManagementServiceClient client = new LifeCycleManagementServiceClient(cookie, config, session);
        viewMode = request.getParameter("view") != null;
        if (request.getParameter("lifecycleName") != null) {
            temp = client.getLifeCycleConfiguration(request);
            isNew = false;
        } else {
//            temp = "<!-- Following is a sample Life Cycle. Please edit it according to your requirements -->\n" +
//            "<aspect name=\"SampleLifeCycle\" class=\"org.wso2.carbon.governance.registry.extensions.aspects.ChecklistLifeCycle\">\n" +
//            "\t<configuration type=\"literal\">\n" +
//            "\t\t<lifecycle>\n" +
//            "\t\t\t<state name=\"Initialize\">\n" +
//            "\t\t\t\t<checkitem>Requirements Gathered</checkitem>\n" +
//            "\t\t\t\t<checkitem>Architecture Finalized</checkitem>\n" +
//            "\t\t\t\t<checkitem>High Level Design Completed</checkitem>\n" +
//            "\t\t\t</state>\n" +
//            "\t\t\t<state name=\"Designed\">\n" +
//            "\t\t\t\t<checkitem>Code Completed</checkitem>\n" +
//            "\t\t\t\t<checkitem>WSDL, Schema Created</checkitem>\n" +
//            "\t\t\t\t<checkitem>QoS Created</checkitem>\n" +
//            "\t\t\t</state>\n" +
//            "\t\t\t<state name=\"Created\">\n" +
//            "\t\t\t\t<checkitem>Effective Inspection Completed</checkitem>\n" +
//            "\t\t\t\t<checkitem>Test Cases Passed</checkitem>\n" +
//            "\t\t\t\t<checkitem>Smoke Test Passed</checkitem>\n" +
//            "\t\t\t</state>\n" +
//            "\t\t\t<state name=\"Tested\">\n" +
//            "\t\t\t\t<checkitem>Service Configuration</checkitem>\n" +
//            "\t\t\t</state>    \n" +
//            "\t\t\t<state name=\"Deployed\">\n" +
//            "\t\t\t\t<checkitem>Service Configuration</checkitem>\n" +
//            "\t\t\t</state>\n" +
//            "\t\t\t<state name=\"Deprecated\">\n" +
//            "\t\t\t</state>\n" +
//            "\t\t</lifecycle>\n" +
//            "\t</configuration>\n" +
//            "</aspect>";
            temp = "<aspect name=\"SampleLifeCycle\" class=\"org.wso2.carbon.governance.registry.extensions.aspects.DefaultLifeCycle\">\n" +
                    "    <configuration type=\"literal\">\n" +
                    "        <lifecycle>\n" +
                    "            <scxml xmlns=\"http://www.w3.org/2005/07/scxml\"\n" +
                    "                   version=\"1.0\"\n" +
                    "                   initialstate=\"Development\">\n" +
                    "                <state id=\"Development\">\n" +
                    "                    <datamodel>\n" +
                    "                        <data name=\"checkItems\">\n" +
                    "                            <item name=\"Code Completed\" forEvent=\"\">\n" +
                    "                                <!--<permissions>\n" +
                    "                                    <permission roles=\"\"/>\n" +
                    "                                </permissions>\n" +
                    "                                <validations>\n" +
                    "                                    <validation forEvent=\"\" class=\"\">\n" +
                    "                                        <parameter name=\"\" value=\"\"/>\n" +
                    "                                    </validation>\n" +
                    "                                </validations>-->\n" +
                    "                            </item>\n" +
                    "                            <item name=\"WSDL, Schema Created\" forEvent=\"\">\n" +
                    "                            </item>\n" +
                    "                            <item name=\"QoS Created\" forEvent=\"\">\n" +
                    "                            </item>\n" +
                    "                        </data>\n" +
                    "                        <!--<data name=\"transitionValidation\">\n" +
                    "                            <validation forEvent=\"\" class=\"\">\n" +
                    "                                <parameter name=\"\" value=\"\"/>\n" +
                    "                            </validation>\n" +
                    "                        </data>\n" +
                    "                        <data name=\"transitionPermission\">\n" +
                    "                            <permission forEvent=\"\" roles=\"\"/>\n" +
                    "                        </data>\n" +
                    "                        <data name=\"transitionScripts\">\n" +
                    "                            <js forEvent=\"\">\n" +
                    "                                <console function=\"\">\n" +
                    "                                    <script type=\"text/javascript\">\n" +
                    "                                    </script>\n" +
                    "                                </console>\n" +
                    "                                <server function=\"\">\n" +
                    "                                    <script type=\"text/javascript\"></script>\n" +
                    "                                </server>\n" +
                    "                            </js>\n" +
                    "                        </data>\n" +
                    "                        <data name=\"transitionApproval\">\n" +
                    "                            <approval forEvent=\"Promote\" roles=\"\" votes=\"2\"/>\n" +
                    "                        </data>-->\n" +
                    "                    </datamodel>\n" +
                    "                    <transition event=\"Promote\" target=\"Tested\"/>                  \n" +
                    "                </state>\n" +
                    "                <state id=\"Tested\">\n" +
                    "                    <datamodel>\n" +
                    "                        <data name=\"checkItems\">\n" +
                    "                            <item name=\"Effective Inspection Completed\" forEvent=\"\">\n" +
                    "                            </item>\n" +
                    "                            <item name=\"Test Cases Passed\" forEvent=\"\">\n" +
                    "                            </item>\n" +
                    "                            <item name=\"Smoke Test Passed\" forEvent=\"\">\n" +
                    "                            </item>\n" +
                    "                        </data>\n" +
                    "                    </datamodel>\n" +
                    "                    <transition event=\"Promote\" target=\"Production\"/>\n" +
                    "                    <transition event=\"Demote\" target=\"Development\"/>\n" +
                    "                </state>\n" +
                    "                <state id=\"Production\">  \n" +
                    "                    <transition event=\"Demote\" target=\"Tested\"/>\n" +
                    "                </state>                \n" +
                    "            </scxml>\n" +
                    "        </lifecycle>\n" +
                    "    </configuration>\n" +
                    "</aspect>\n" +
                    "" ;
        }
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
<fmt:bundle basename="org.wso2.carbon.governance.lcm.ui.i18n.Resources">
<carbon:breadcrumb
        label="life.cycle.source"
        resourceBundle="org.wso2.carbon.governance.lcm.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>
<script type="text/javascript">

    function cancelSequence() {
        document.location.href = "lcm.jsp?region=region3&item=governance_lcm_menu";
    }
    YAHOO.util.Event.onDOMReady(function() {
        editAreaLoader.init({
            id : "payload"        // textarea id
            ,syntax: "xml"            // syntax to be uses for highgliting
            ,start_highlight: true        // to display with highlight mode on start-up
            ,allow_resize: "both"
            ,min_height:250
        });
    });

</script>
<div id="middle">
    <h2><fmt:message key="life.cycle.source"/></h2>
    <div id="workArea">
        <form id="life.cycle.source.form" method="post" action="save_lcm-ajaxprocessor.jsp">
            <table class="styledLeft" cellspacing="0" cellpadding="0">
                <thead>
                <tr>
                    <th>
                        <span style="float: left; position: relative; margin-top: 2px;"><fmt:message key="life.cycle"/></span>
                    </th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td>
                        <textarea id="payload" style="border: 0px solid rgb(204, 204, 204); width: 99%; height: 275px; margin-top: 5px;" name="payload" rows="30" class="codepress html linenumbers-on" wrap="off"><%=temp%></textarea>
                    </td>
                </tr>
                <tr>
                    <td class="buttonRow">
                        <% if (!viewMode) { %>
                        <input class="button registryWriteOperation" type="button" onclick="saveLC('<%=request.getParameter("lifecycleName")%>', <%=Boolean.toString(isNew)%>,'false')" value="<fmt:message key="save"/>"/>
                        <input class="button registryNonWriteOperation" type="button" disabled="disabled" value="<fmt:message key="save"/>"/>
                        <input class="button" type="button" value="<fmt:message key="cancel"/>" onclick="javascript: cancelSequence(); return false;"/>
                        <% } else { %>
                        <input class="button registryWriteOperation" type="button" onclick="saveLC('<%=request.getParameter("lifecycleName")%>', <%=Boolean.toString(isNew)%>,'false')" value="<fmt:message key="save"/>"/>
                        <input class="button registryNonWriteOperation" type="button" disabled="disabled" value="<fmt:message key="save"/>"/>
                        <input class="button" type="button" value="<fmt:message key="cancel"/>" onclick="javascript: cancelSequence(); return false;"/>
                        <% } %>
                    </td>
                </tr>
                </tbody>
            </table>
        </form>
    </div>
</div>
</fmt:bundle>
<%--<script>--%>
<%--$('payload').innerHTML = format_xml($('payload').value);--%>
<%--</script>--%>
