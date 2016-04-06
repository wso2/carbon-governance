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
<%@ page import="org.wso2.carbon.governance.generic.ui.clients.ManageGenericArtifactServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>

<link type="text/css" rel="stylesheet" href="css/menu.css"/>
<link type="text/css" rel="stylesheet" href="css/style.css"/>

<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<script type="text/javascript" src="js/tinyxmlsax.js"></script>
<script type="text/javascript" src="js/tinyxmlw3cdom.js"></script>

<!--Yahoo includes for dom event handling-->
<script src="../yui/build/yahoo-dom-event/yahoo-dom-event.js" type="text/javascript"></script>

<!--EditArea javascript syntax hylighter -->
<script language="javascript" type="text/javascript" src="../editarea/edit_area_full.js"></script>

<carbon:jsi18n
        resourceBundle="org.wso2.carbon.governance.generic.ui.i18n.Resources"
        request="<%=request%>" namespace="org.wso2.carbon.governance.generic.ui"/>
<%
    String content = null;
    try {
        ManageGenericArtifactServiceClient client = new ManageGenericArtifactServiceClient(config, session);
        content = client.getArtifactUIConfiguration(request.getParameter("key"));
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
            label="<%=request.getParameter("breadcrumb")%>"
            topPage="false"
            request="<%=request%>"/>
    <script type="text/javascript">

        /*
         function validateAndSaveConfiguration() {

         var xmlURL = editAreaLoader.getValue("payloadEditor");
         var _schema="service-ui-config";

         new Ajax.Request('../services/xmlconfig_validator_ajaxprocessor.jsp',
         {
         method:'post',
         parameters: { target_xml: xmlURL,schema: _schema},
         onSuccess: function(transport) {
         var returnValue = transport.responseText;
         if (returnValue.search(/---XMLSchemaValidated----/) != -1) {
         SaveConfiguration();
         } else {
         CARBON.showErrorDialog(transport.responseText);
         }
         },

         onFailure: function(transport) {
         CARBON.showErrorDialog(transport.responseText);
         return;
         }
         });
         }
         */

        function SaveConfiguration() {
            sessionAwareFunction(function () {
                var CustomUIForm = document.getElementById('generic.config.form');
                var rawconfig = editAreaLoader.getValue("payloadEditor");
                if (rawconfig.indexOf("?>") > -1) {
                    rawconfig = rawconfig.substring(rawconfig.indexOf("?>") + 2);
                }
                try {
                    var domParser = new DOMImplementation();
                    currentconfigDoc = domParser.loadXML(rawconfig);
                    $('payload').value = editAreaLoader.getValue("payloadEditor");
                    CustomUIForm.submit();
                }
                catch (e) {
                    reason = "<fmt:message key="message1"/> !";
                    CARBON.showWarningDialog(reason);
                }
            }, "<fmt:message key="session.timed.out"/>");
        }

        function cancelSequence() {
            sessionAwareFunction(function () {
                document.location.href = "../generic/configure.jsp?<%=request.getQueryString()%>";
            }, "<fmt:message key="session.timed.out"/>");
        }
        YAHOO.util.Event.onDOMReady(function () {
            editAreaLoader.init({
                id: "payloadEditor"        // textarea id
                , syntax: "xml"            // syntax to be uses for highgliting
                , start_highlight: true        // to display with highlight mode on start-up
                , allow_resize: "both"
                , min_height: 250
            });
        })

    </script>
    <div id="middle">
        <h2><fmt:message key="configure.artifacts"><fmt:param
                value="<%=Encode.forHtml(request.getParameter("pluralLabel"))%>"/></fmt:message></h2>
        <div id="workArea">
            <form id="generic.config.form" method="post" action="configure_rxt.jsp">
                <input type="hidden" name="add_edit_region"
                       value="<%=Encode.forHtml(request.getParameter("add_edit_region"))%>"/>
                <input type="hidden" name="add_edit_item"
                       value="<%=Encode.forHtml(request.getParameter("add_edit_item"))%>"/>
                <input type="hidden" name="region" value="<%=Encode.forHtml(request.getParameter("region"))%>"/>
                <input type="hidden" name="item" value="<%=Encode.forHtml(request.getParameter("item"))%>"/>
                <input type="hidden" name="key" value="<%=Encode.forHtml(request.getParameter("key"))%>"/>
                <input type="hidden" name="pluralLabel"
                       value="<%=Encode.forHtml(request.getParameter("pluralLabel"))%>"/>
                <input type="hidden" name="singularLabel"
                       value="<%=Encode.forHtml(request.getParameter("singularLabel"))%>"/>
                <input type="hidden" name="lifecycleAttribute"
                       value="<%=Encode.forHtml(request.getParameter("lifecycleAttribute"))%>"/>
                <input type="hidden" name="add_edit_breadcrumb"
                       value="<%=Encode.forHtml(request.getParameter("add_edit_breadcrumb"))%>"/>
                <input type="hidden" name="breadcrumb" value="<%=Encode.forHtml(request.getParameter("breadcrumb"))%>"/>
                <table class="styledLeft" cellspacing="0" cellpadding="0">
                    <thead>
                    <tr>
                        <th>
                            <span style="float: left; position: relative; margin-top: 2px;"><fmt:message
                                    key="message"><fmt:param
                                    value="<%=request.getParameter("singularLabel")%>"/></fmt:message></span>
                        </th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td>
                            <textarea id="payloadEditor"
                                      style="border: 0px solid rgb(204, 204, 204); width: 99%; height: 275px; margin-top: 5px;"
                                      name="payloadEditor" rows="30"><%=content%></textarea>
                            <textarea style="display:none" name="payload" id="payload"><%=content%></textarea>
                        </td>
                    </tr>
                    <tr>
                        <td class="buttonRow">
                            <input class="button registryWriteOperation" type="button" onclick="SaveConfiguration()"
                                   value="<fmt:message key="save"/>"/>
                            <input class="button registryNonWriteOperation" type="button" disabled="disabled"
                                   value="<fmt:message key="save"/>"/>
                            <input class="button" type="button" value="<fmt:message key="reset"/>"
                                   onclick="javascript: cancelSequence(); return false;"/>
                        </td>
                    </tr>

                    </tbody>
                </table>
            </form>
        </div>
    </div>
</fmt:bundle>
    
