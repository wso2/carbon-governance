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
<%@ page import="org.wso2.carbon.governance.custom.lifecycles.checklist.ui.processors.InvokeAspectProcessor" %>
<%@ page import="org.wso2.carbon.registry.extensions.utils.CommonConstants" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.regex.Pattern" %>
<%@ page import="java.net.URLDecoder" %>
<%@ page import="org.wso2.carbon.registry.core.utils.RegistryUtils" %>
<%
    try {
        boolean isViewVersion = false;
        boolean preserveOriginal = true;
        boolean viewDependencies = true;
        boolean viewPreserveOriginal = false;
        String displayMediaType = "";
        String currentEnvironment = "";
        String[] preserveOrigParam = null;
        String[] viewVersion = null;
        String[] parameters = request.getParameterValues("parameterMap");

        String path = request.getParameter("path");
        String aspect = request.getParameter("aspect");
        String action = request.getParameter("action");
        String callBack = request.getParameter("callBack");

        if (callBack == null || callBack.trim().length() == 0) {
            callBack = "''";
        }

        if (parameters != null) {
            for (String parameter : parameters) {
                parameter = URLDecoder.decode(parameter, "utf-8");
                if (parameter.contains("&")) {
                    String[] joinedParams = parameter.split("&");
                    for (String joinedParam : joinedParams) {
                        if (joinedParam.split("=")[0].equals("preserveOriginal")) {
                            preserveOrigParam = joinedParam.split("=");
                        } else if (joinedParam.split("=")[0].equals("viewVersion")) {
                            viewVersion = joinedParam.split("=");
                            isViewVersion = !Boolean.parseBoolean(viewVersion[1]);
                        } else if(joinedParam.split("=")[0].equals("displayMediaType")){
                            displayMediaType = joinedParam.split("=")[1];
                        } else if(joinedParam.split("=")[0].equals("showDependencies")){
                            viewDependencies = Boolean.parseBoolean(joinedParam.split("=")[1]);
                        } else if(joinedParam.split("=")[0].equals("viewPreserveOriginal")){
                            viewPreserveOriginal = Boolean.parseBoolean(joinedParam.split("=")[1]);
                        }else if(joinedParam.split("=")[0].equals("currentEnvironment")){
                            currentEnvironment = joinedParam.split("=")[1];
                        }
                    }
                } else {
                    if (parameter.split("=")[0].equals("preserveOriginal")) {
                        preserveOrigParam = parameter.split("=");
                    } else if (parameter.split("=")[0].equals("viewVersion")) {
                        viewVersion = parameters[0].split("=");
                        isViewVersion = !Boolean.parseBoolean(viewVersion[1]);
                    }else if(parameter.split("=")[0].equals("displayMediaType")){
                        displayMediaType = parameter.split("=")[1];
                    } else if(parameter.split("=")[0].equals("showDependencies")){
                        viewDependencies = Boolean.parseBoolean(parameter.split("=")[1]);
                    } else if(parameter.split("=")[0].equals("viewPreserveOriginal")){
                        viewPreserveOriginal = Boolean.parseBoolean(parameter.split("=")[1]);
                    }else if(parameter.split("=")[0].equals("currentEnvironment")){
                        currentEnvironment = parameter.split("=")[1];
                    }
                }
            }
        }

        if (preserveOrigParam != null) {
            String preserveOrg = "";
            if (preserveOrigParam[0].equals("preserveOriginal")) {
                preserveOrg = preserveOrigParam[1];
            }
            preserveOriginal = !Boolean.toString(false).equals(preserveOrg);
        }

        if (isViewVersion) {
            try {
                String versionString = "preserveOriginal" + "^^" + preserveOriginal + "^|^";

%>
<script type="text/javascript">

    invokeAspect('<%=path%>', '<%=aspect%>', '<%=action%>', <%=callBack%>, '<%=versionString%>');
</script>
<%

    } catch (Exception ex) {
        ex.printStackTrace();
        response.setStatus(500);
        ex.getMessage();
        return;

    }
%>
<%
} else {
    String mediaType = request.getParameter("mediaType");
    if (preserveOrigParam != null && preserveOrigParam.length != 0) {
        preserveOriginal = Boolean.parseBoolean(preserveOrigParam[1]);
    }
        List<String> otherDependencies = new ArrayList<String>();

        if (displayMediaType == null || displayMediaType.equals("")) {
            displayMediaType = CommonConstants.SERVICE_MEDIA_TYPE.replace(".", "[.]").replace("+", "[+]");
        }

        if (!(Pattern.compile(displayMediaType).matcher(mediaType).find())) {
%>
<script type="text/javascript">
    invokeAspect('<%=path%>', '<%=aspect%>', '<%=action%>', <%=callBack%>, "");
</script>
<%
} else {
    String[] associations;
    if (!viewDependencies) {
        associations = new String[1];
        associations[0] = path;
    } else {
        associations = InvokeAspectProcessor.getAllDependencies(request, config);
    }
%>
<script type="text/javascript" src="../lifecycles/js/lifecycles.js"></script>
<script type="text/javascript">
    proceedAction = function (path, aspect, action, mediaType, preserveOriginal, callBack) {
        lifecyleOperationStarted = false;

        document.getElementById(org_wso2_carbon_governance_custom_lifecycles_checklist_ui_jsi18n["proceed"]).disabled = true;

        var versionString = "preserveOriginal" + "^^" + preserveOriginal + "^|^";
        var table = $('versionTable');
        var rows = table.getElementsByTagName('input');

        var regexp = new RegExp("^\\d+[.]\\d+[.]\\d+(-[a-zA-Z0-9]+)?$", "i");

        for (var i = 0; i < rows.length; i++) {
            var obj = rows[i];
            if ((obj.type == "text")) {
                if (obj.value == null | obj.value.trim == "") {
                    document.getElementById(org_wso2_carbon_governance_custom_lifecycles_checklist_ui_jsi18n["proceed"]).disabled = false;
                    showRegistryError(obj.getAttribute("id") + ' ' + org_wso2_carbon_governance_custom_lifecycles_checklist_ui_jsi18n["version.can.not.be.empty"]);
                    return;
                }
                else if (!jQuery.trim(obj.value).match(regexp)) {
                    document.getElementById(org_wso2_carbon_governance_custom_lifecycles_checklist_ui_jsi18n["proceed"]).disabled = false;
                    showRegistryError(org_wso2_carbon_governance_custom_lifecycles_checklist_ui_jsi18n["version.error.1"]
                            + " " + obj.getAttribute("id") + " " + org_wso2_carbon_governance_custom_lifecycles_checklist_ui_jsi18n["version.error.2"]);
                    return;
                }
                versionString = versionString + obj.name + "^^" + jQuery.trim(obj.value) + "^|^";
            } else if ((obj.type == "checkbox")) {
                versionString = versionString + obj.name + "^^" + obj.checked + "^|^";
            } else if ((obj.type == "hidden")) {
                versionString = versionString + obj.name + "^^" + "0.0.0" + "^|^";
            }

        }

        invokeAspect(path, aspect, action, callBack, versionString,org_wso2_carbon_governance_custom_lifecycles_checklist_ui_jsi18n["proceed"]);
    }
    cancelVersionBox = function (path, aspect) {
        refreshLifecyclesSection(path, aspect);
    }
</script>

<fmt:bundle
        basename="org.wso2.carbon.governance.custom.lifecycles.checklist.ui.i18n.Resources">
    <form id="dependencyVersionFrom" name="dependencyVersion">

        <%
            if (associations.length > 0) {
        %>
        <table class="styledLeft" id="versionTable">
            <tbody>
            <tr>
                <td>Resource</td>
                <td>Version</td>
            </tr>
            <%
                for (String association : associations) {
                    String assoName = association.substring(association.lastIndexOf("/") + 1);
                    String tmp;
                    if(association.equals("/")) {
                        tmp = association;
                    } else {
                        tmp = association.substring(association.indexOf("/_system"));
                    }
                    if (currentEnvironment == null || tmp.startsWith(currentEnvironment)) {
            %>
            <tr>
                <td>
                    <%=assoName%>
                    <span class="required">*</span>
                </td>
                <td>
                    <input type="text" name="<%=association%>" id="<%=assoName %>"
                           style="width:140px;"/>
                </td>
            </tr>
            <%
                    } else {
                        otherDependencies.add(association);
                    }
                }
                if(viewPreserveOriginal){
            %>
            <tr>
                <td colspan="2"><fmt:message key="preserve.original"/>: <input type="checkbox" name="preserveOriginal"
                        <% if (preserveOriginal) {%> checked="checked" <%} %> value="true"
                                                                               id="preserveOriginal"/></td>
            </tr>
            <%
                }
            %>
            <tr>
                <td colspan="2">
                    <input class="button registryWriteOperation" type="button" id="<fmt:message key="proceed"/>"
                           value="<fmt:message key="proceed"/>"
                           onclick="proceedAction('<%=path%>', '<%=aspect%>', '<%=action%>','<%=mediaType%>',
                                   '<%=preserveOriginal%>' ,<%=callBack%>)"/>
                    <input class="button registryWriteOperation" type="button"
                           value="<fmt:message key="cancel"/>"
                           onclick="cancelVersionBox('<%=path%>', '<%=aspect%>')"/>
                </td>
            </tr>
            <%
                }
                for (String otherDependency : otherDependencies) {
                    String assoName = otherDependency.substring(otherDependency.lastIndexOf("/") + 1);
            %>
            <input type="hidden" name="<%=otherDependency%>" id="<%=assoName %>"
                   style="width:140px;"/>
            <%
                }
            %>
            </tbody>
        </table>
    </form>
</fmt:bundle>
<%
                }
      }
} catch (Exception ex) {
    ex.printStackTrace();
    response.setStatus(500);
%><%=ex.getMessage()%><%
        return;

    }


%>