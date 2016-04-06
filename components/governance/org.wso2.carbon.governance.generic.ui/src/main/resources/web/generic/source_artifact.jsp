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

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="carbon" uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" %>

<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.governance.generic.ui.clients.ManageGenericArtifactServiceClient" %>
<%@ page import="org.wso2.carbon.registry.resource.ui.clients.ResourceServiceClient" %>
<%@ page import="org.wso2.carbon.registry.resource.stub.beans.xsd.ContentDownloadBean" %>

<link type="text/css" rel="stylesheet" href="css/menu.css"/>
<link type="text/css" rel="stylesheet" href="css/style.css"/>

<!--Yahoo includes for dom event handling-->
<script src="../yui/build/yahoo-dom-event/yahoo-dom-event.js" type="text/javascript"></script>

<!--EditArea javascript syntax hylighter -->
<script language="javascript" type="text/javascript" src="../editarea/edit_area_full.js"></script>

<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<carbon:jsi18n
        resourceBundle="org.wso2.carbon.governance.generic.ui.i18n.JSResources"
        request="<%=request%>" namespace="org.wso2.carbon.governance.generic.ui"/>
<script type="text/javascript" src="../generic/js/generic.js"></script>
<%
    String temp = "";
    boolean isNew = true;
    boolean viewMode = false;
    ManageGenericArtifactServiceClient client;
    ResourceServiceClient resourceServiceClient;
    String rxtPath = request.getParameter("path");
    String rxtName = request.getParameter("rxtName");
    try {
        client = new ManageGenericArtifactServiceClient(config, session);
        viewMode = request.getParameter("view") != null;

        if (rxtPath != null) {
            resourceServiceClient = new ResourceServiceClient(config, session);
            temp = resourceServiceClient.getTextContent(request);
        } else {
            temp = "<artifactType type=\"application/vnd.wso2-application+xml\" shortName=\"applications\" singularLabel=\"Enterprise Application\" pluralLabel=\"Enterprise Applications\" hasNamespace=\"false\" iconSet=\"9\">\n"
                    +
                    "    <storagePath>/applications/@{name}/@{overview_version}</storagePath>\n" +
                    "\t<nameAttribute>overview_name</nameAttribute>\n" +
                    "    <ui>\n" +
                    "        <list>\n" +
                    "            <column name=\"Name\">\n" +
                    "                <data type=\"path\" value=\"overview_name\" href=\"/applications/@{name}\"/>\n" +
                    "            </column>\n" +
                    "            <column name=\"Version\">\n" +
                    "                <data type=\"path\" value=\"overview_version\" href=\"@{storagePath}\"/>\n" +
                    "            </column>\n" +
                    "        </list>\n" +
                    "    </ui>\n" +
                    "    <content>\n" +
                    "        <table name=\"Overview\">\n" +
                    "            <field type=\"text\" required=\"true\">\n" +
                    "                <name>Name</name>\n" +
                    "            </field>\n" +
                    "            <field type=\"text\" required=\"true\">\n" +
                    "                <name>Version</name>\n" +
                    "            </field>\n" +
                    "            <field type=\"text-area\">\n" +
                    "                <name>Description</name>\n" +
                    "            </field>\n" +
                    "        </table>\n" +
                    "        <table name=\"Assets\">\n" +
                    "            <subheading>\n" +
                    "                <heading>Type</heading>\n" +
                    "                <heading>Path</heading>\n" +
                    "            </subheading>\n" +
                    "            <field type=\"option-text\" maxoccurs=\"unbounded\" path=\"true\" url=\"true\">\n" +
                    "                <name>Asset</name>\n" +
                    "                <values>\n" +
                    "                    <value>Generic</value>\n" +
                    "                    <value>Service</value>\n" +
                    "                </values>\n" +
                    "            </field>\n" +
                    "        </table>\n" +
                    "\t\t<table name=\"Tests\">\n" +
                    "\t\t\t<field type=\"text\" path=\"true\">\n" +
                    "                <name>Test Harness</name>\n" +
                    "            </field>\n" +
                    "        </table>\n" +
                    "        <table name=\"Documentation\" columns=\"3\">\n" +
                    "            <subheading>\n" +
                    "                <heading>Document Type</heading>\n" +
                    "                <heading>URL</heading>\n" +
                    "                <heading>Comment</heading>\n" +
                    "            </subheading>\n" +
                    "            <field type=\"text\">\n" +
                    "                <name>Document Type</name>\n" +
                    "            </field>\n" +
                    "            <field type=\"text\" url=\"true\">\n" +
                    "                <name>URL</name>\n" +
                    "            </field>\n" +
                    "            <field type=\"text-area\">\n" +
                    "                <name>Document Comment</name>\n" +
                    "            </field>\n" +
                    "            <field type=\"text\">\n" +
                    "                <name>Document Type1</name>\n" +
                    "            </field>\n" +
                    "            <field type=\"text\" url=\"true\">\n" +
                    "                <name>URL1</name>\n" +
                    "            </field>\n" +
                    "            <field type=\"text-area\">\n" +
                    "                <name>Document Comment1</name>\n" +
                    "            </field>\n" +
                    "            <field type=\"text\">\n" +
                    "                <name>Document Type2</name>\n" +
                    "            </field>\n" +
                    "            <field type=\"text\" url=\"true\">\n" +
                    "                <name>URL2</name>\n" +
                    "            </field>\n" +
                    "            <field type=\"text-area\">\n" +
                    "                <name>Document Comment2</name>\n" +
                    "            </field>\n" +
                    "        </table>\n" +
                    "    </content>\n" +
                    "</artifactType>";
        }
    } catch (Exception e) {
        response.setStatus(500);
        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
        session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<jsp:include page="../admin/error.jsp?<%=e.getMessage()%>"/>
<%
        return;
    }
%>
<fmt:bundle basename="org.wso2.carbon.governance.generic.ui.i18n.Resources">
    <carbon:breadcrumb
            label="generic.artifact.source"
            resourceBundle="org.wso2.carbon.governance.generic.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>
    <script type="text/javascript">

        function cancelSequence() {
            document.location.href = "generic_artifact.jsp?region=region3&item=governance_generic_menu";
        }
        YAHOO.util.Event.onDOMReady(function () {
            editAreaLoader.init({
                id: "payload"        // textarea id
                , syntax: "xml"            // syntax to be uses for highgliting
                , start_highlight: true        // to display with highlight mode on start-up
                , allow_resize: "both"
                , min_height: 250
            });
        });

    </script>
    <div id="middle">
        <h2><fmt:message key="generic.artifact.source"/></h2>
        <div id="workArea">
            <form id="generic.artifact.source.form" method="post" action="save_artifact_ajaxprocessor.jsp">
                <input type="hidden" name="rxtName" id="rxtName" value="<%=Encode.forHtml(rxtName)%>"/>
                <table class="styledLeft" cellspacing="0" cellpadding="0">
                    <thead>
                    <tr>
                        <th>
                            <span style="float: left; position: relative; margin-top: 2px;"><fmt:message
                                    key="generic.artifact"/></span>
                        </th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td>
                            <textarea id="payload"
                                      style="border: 0px solid rgb(204, 204, 204); width: 99%; height: 275px; margin-top: 5px;"
                                      name="payload" rows="30" class="codepress html linenumbers-on"
                                      wrap="off"><%=temp%></textarea>
                        </td>
                    </tr>
                    <tr>
                        <td class="buttonRow">
                            <% if (!viewMode) { %>
                            <% if (rxtPath == null) { %>
                            <input class="button registryWriteOperation" type="button" onclick="addRXT()"
                                   value="<fmt:message key="save"/>"/>
                            <% } else { %>
                            <input class="button registryWriteOperation" type="button"
                                   onclick="saveRXT('<%=Encode.forHtml(rxtPath)%>','<%=Encode.forHtml(rxtName)%>')"
                                   value="<fmt:message key="save"/>"/>
                            <% } %>
                            <input class="button registryNonWriteOperation" type="button" disabled="disabled"
                                   value="<fmt:message key="save"/>"/>
                            <input class="button" type="button" value="<fmt:message key="cancel"/>"
                                   onclick="javascript: cancelSequence(); return false;"/>
                            <% } else { %>
                            <input class="button" type="button" value="<fmt:message key="back"/>"
                                   onclick="javascript: cancelSequence(); return false;"/>
                            <% } %>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </form>
        </div>
    </div>
</fmt:bundle>
