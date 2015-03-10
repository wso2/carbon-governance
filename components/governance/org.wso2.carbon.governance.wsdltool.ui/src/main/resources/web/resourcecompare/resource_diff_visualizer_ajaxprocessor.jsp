<!--
~ Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.​
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
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%--<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>--%>
<jsp:include page="resources-i18n-ajaxprocessor.jsp"/>

<script type="text/javascript" src="../yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="../yui/build/container/container_core-min.js"></script>
<script type="text/javascript" src="../yui/build/yahoo/yahoo-min.js"></script>
<script type="text/javascript" src="../yui/build/event/event-min.js"></script>
<script type="text/javascript" src="../yui/build/connection/connection-min.js"></script>
<script type="text/javascript" src="../resources/js/resource_util.js"></script>
<script type="text/javascript" src="../yui/build/utilities/utilities.js"></script>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<script type="text/javascript" src="js/resource_compare_util.js"></script>

<!-- code mirror libraries -->
<link rel="stylesheet" href="../diffview/lib/codemirror.css">
<link rel=stylesheet href="../diffview/addon/merge/merge.css">

<script src="../diffview/lib/codemirror.js"></script>
<script src="../diffview/mode/javascript/javascript.js"></script>
<script src="../diffview/mode/xml/xml.js"></script>
<script src="../diffview/addon/merge/dep/diff_match_patch.js"></script>
<script src="../diffview/addon/merge/merge.js"></script>
<script>
    window.onload = function() {
        // Get resource path
        var resourcePath1 = '<%=request.getParameter("resourcePath1")%>';
        var resourcePath2 = '<%=request.getParameter("resourcePath2")%>';
        // Get resource type
        var type = '<%=request.getParameter("type")%>';
        document.getElementById('diff_panel1').hidden = true;
        document.getElementById('diff_panel2').hidden = true;
        // Get resource1 content
        getResource1Content(resourcePath1,resourcePath2, type);
    };
    // Function for get resource1 content
    function getResource1Content(resourcePath1, resourcePath2, type) {
            new Ajax.Request('display_panel1_content_ajaxprocessor.jsp',
                    {
                        method:'get',
                        parameters: {path: resourcePath1,random:getRandom() },
                        onSuccess: function(transport) {
                            document.getElementById("diff_panel1").innerHTML = transport.responseText;
                            getResource2Content(resourcePath2, type);
                        },
                        onFailure: function(transport) {
                            if (trim(transport.responseText)) {
                                CARBON.showWarningDialog(org_wso2_carbon_governance_wsdltool_ui_jsi18n["unsupported.media.type.to.display"] + ": " + transport.responseText);
                            } else {
                                CARBON.showWarningDialog(org_wso2_carbon_governance_wsdltool_ui_jsi18n["unsupported.media.type.to.display"]);
                            }
                        }
                    });

            var textDiv = document.getElementById("diff_panel1");
            textDiv.style.display = "block";
    }
    // Function for get resource 2 content
    function getResource2Content(resourcePath2, type) {
            new Ajax.Request('display_panel2_content_ajaxprocessor.jsp',
                    {
                        method:'get',
                        parameters: { path: resourcePath2,random:getRandom() },
                        onSuccess: function(transport) {
                            var widthOfResourceViewer = '100%';
                            var heightOfResourceViewer = '100%';
                            document.getElementById("diff_panel2").innerHTML = transport.responseText;
                            dv = CodeMirror.MergeView(document.getElementById("placeholder"), {
                                value: document.getElementById("panel1_ta").innerHTML.replace(/&lt;/g, '<').replace(/&gt;/g, '>'),
                                orig: document.getElementById("panel2_ta").innerHTML.replace(/&lt;/g, '<').replace(/&gt;/g, '>'),
                                lineNumbers: true,
                                mode: "xml",
                                highlightDifferences: true,
                                showCursorWhenSelecting: true
                            });
                            dv.setSize(widthOfResourceViewer, heightOfResourceViewer);
                        },
                        onFailure: function(transport) {
                            if (trim(transport.responseText)) {
                                CARBON.showWarningDialog(org_wso2_carbon_governance_wsdltool_ui_jsi18n["unsupported.media.type.to.display"] + ": " + transport.responseText);
                            } else {
                                CARBON.showWarningDialog(org_wso2_carbon_governance_wsdltool_ui_jsi18n["unsupported.media.type.to.display"]);
                            }
                        }
                    });
            var textDiv = document.getElementById("diff_panel2");
            textDiv.style.display = "block";
    }
</script>
<fmt:bundle basename="org.wso2.carbon.governance.wsdltool.ui.i18n.Resources">
    <div id="generalContentDiv">
        <!-- the placeholder for the merge/kdiff interface -->
        <div id="placeholder" style="margin: 0px; width: 1000px; height: 600px;"></div>
        <!-- JS code for the first panel -->
        <div id="diff_panel1" hidden="true"></div>
        <!-- JS code for the second panel -->
        <div id="diff_panel2" hidden="true"></div>
    </div>
</fmt:bundle>


