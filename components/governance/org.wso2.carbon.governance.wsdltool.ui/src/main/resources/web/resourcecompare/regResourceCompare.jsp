<!--
~ Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.​
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
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.wso2.carbon.governance.wsdltool.ui.clients.WSDLToolServiceClient" %>

<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>
<jsp:include page="resources-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>
<script type="text/javascript" src="../resources/js/resource_util.js"></script>
<script type="text/javascript" src="js/registry-browser.js"></script>
<script type="text/javascript" src="js/resource_compare_util.js"></script>
<script type="text/javascript" src="js/resource_visualize_util.js"></script>
<fmt:bundle basename="org.wso2.carbon.governance.wsdltool.ui.i18n.Resources">
<carbon:breadcrumb
        label="wsdlcompare.breadcrumb"
        resourceBundle="org.wso2.carbon.governance.wsdltool.ui.i18n.Resources"
        topPage="true"
        request="<%=request%>"/>

<script type="text/javascript">
    jQuery(document).ready(function () {
        jQuery("#compare_form").validate({
            submitHandler: function (form) {
                compare_form.action = "regResourceCompare.jsp";
                compare_form.submit();
                return true;
            }
        });
    });
</script>
<script type="text/javascript">
    function showResourceOptionsPane(id) {
        var resourceOptionsRow = document.getElementById('resourceOptionsRow' + id);
        var link = document.getElementById('resourceOptionsExpandLink' + id);
        if (resourceOptionsRow.style.display == 'none') {
            resourceOptionsRow.style.display = '';
            link.style.backgroundImage = 'url(images/up.gif)';
        } else {
            resourceOptionsRow.style.display = 'none';
            link.style.backgroundImage = 'url(images/down.gif)';
        }
    }
</script>

<% boolean submitted = "true".equals(request.getParameter("formSubmitted"));
    String[] membraneDiffsKeys = new String[0];
    String[] membraneDiffsVals = new String[0];
    String path1 = "", path2 = "", type = "", lblView = "" ,lblDiffHeading = "";
    if (submitted) {
        try {
            String resource1Path = request.getParameter("resourcePath1");
            String resource2Path = request.getParameter("resourcePath2");
            if (null == resource1Path || null == resource2Path) { %>
<script type="text/javascript">
    CARBON.showWarningDialog("<fmt:message key="resource.compare.invalid.resource"/>");
</script>
<% } else if ("".equals(resource1Path) || "".equals(resource2Path)) { %>
<%-- If user haven't select the resources--%>
<script type="text/javascript">
    CARBON.showWarningDialog("<fmt:message key="resource.compare.empty.resource"/>");
</script>
<% } else if (resource1Path.equals(resource2Path)) { %>
<%-- Check whether the user have select the same resource as resource1 and resource2--%>
<script type="text/javascript">
    CARBON.showWarningDialog("<fmt:message key="resource.compare.same.resource"/>");
</script>
<% } else {
    // Get the diffview type or the pop-up message
    WSDLToolServiceClient client;
        String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                    (ConfigurationContext) config.getServletContext()
                            .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        client = new WSDLToolServiceClient(configContext, serverURL, cookie);
        String result = client.getDiffViewType(resource1Path, resource2Path);
        if (null != result) {
        // Membrane result
        if (result.equalsIgnoreCase("membrane.diff")) {
            // Set path of the selected resources
            path1 = resource1Path;
            path2 = resource2Path;
            lblView = "View";
            lblDiffHeading = "Resource Differences";

            // Get registry full path for resources
            String resource1FullPath = "/_system/governance/trunk" + resource1Path;
            String resource2FullPath = "/_system/governance/trunk" + resource2Path;
            // Get membrane diff keys
        try {
            membraneDiffsKeys = client.getMembraneDiffArrayResult(resource1FullPath, resource2FullPath, "key");
            // Get membrane diff values
            membraneDiffsVals = client.getMembraneDiffArrayResult(resource1FullPath, resource2FullPath, "value");
        } catch (Exception e) {
                String cause;
                if (e.getCause() != null) {
                    cause = e.getCause().getMessage();
                    cause = cause.replaceAll("\n|\\r|\\t|\\f", "");
                } else {
                    cause = e.getMessage();
                } %>
<script type="text/javascript">
    CARBON.showErrorDialog('<%=cause%>');
</script>
<% }// End-of catch
} else if (result.equalsIgnoreCase("codemirror.diff")) { %>
<script type="text/javascript">
    var resourcePath1 = '/_system/governance/trunk<%=resource1Path%>';
    var resourcePath2 = '/_system/governance/trunk<%=resource2Path%>';
    var resourceType = '<%=type%>';
    // Get code-mirror diff view
    visualizeResourceDiff(resourcePath1, resourcePath2, resourceType);
</script>
<%} else {%>
<script type="text/javascript">
    CARBON.showWarningDialog("<fmt:message key="<%=result%>"/>");
</script>
<% } // end of else
}  // end of if
}
} catch (Exception e) {
    String cause;
    if (e.getCause() != null) {
        cause = e.getCause().getMessage();
        cause = cause.replaceAll("\n|\\r|\\t|\\f", "");
    } else {
        cause = e.getMessage();
    } %>
<script type="text/javascript">
    CARBON.showErrorDialog('<%=cause%>');
</script>
<% }//end-of catch
}//end-of if(submitted) %>

<div id="middle">
    <h2><fmt:message key="resource.compare.header.text"/></h2>

    <div id="workArea">
        <form id="compare_form" method="POST" action="regResourceCompare.jsp">
            <input type="hidden" name="formSubmitted" value="true"/>
            <table cellpadding="0" cellspacing="0" border="0" class="styledLeft noBorders">
                <thead>
                <tr>
                    <th colspan="2"><fmt:message key="resource.compare.browse.resource"/></th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td colspan="2" style="padding-bottom:10px;">
                        <table>
                            <tr>
                                <td><fmt:message key="resource.compare.resource1.location"/>
                                    <span class="required">*</span></td>
                                <td>
                                    <table cellspacing="0">
                                        <tr>
                                            <td class="nopadding" style="border:none !important">
                                                <input type="text" name="resourcePath1" id="resourcePath1"
                                                       class="required"
                                                       value="<%=path1%>" size="80" readonly="readonly"/>
                                            </td>
                                            <td class="nopadding" style="border:none !important">
                                                <a href="#" class="registry-picker-icon-link"
                                                   style="padding-left:30px"
                                                   onclick="showRegistryBrowser('resourcePath1','/_system/governance/trunk');">
                                                    <fmt:message key="gov.registry"/>
                                                </a>
                                            </td>
                                            <td class="nopadding" style="border:none !important">
                                                <a href="#" class="nopadding"
                                                   style="padding-left:30px;padding-bottom: 0px"
                                                   onclick="visualizeResource('/_system/governance/trunk<%=path1%>','wsdl')"
                                                   style="background-image:url(images/editshred.png);">
                                                    <%=lblView%>
                                                </a>
                                            </td>
                                        </tr>
                                    </table>
                                </td>
                            </tr>
                            <tr>
                                <td><fmt:message key="resource.compare.resource2.location"/>
                                    <span class="required">*</span></td>
                                <td>
                                    <table cellspacing="0">
                                        <tr>
                                            <td class="nopadding" style="border:none !important">
                                                <input type="text" name="resourcePath2" id="resourcePath2"
                                                       class="required"
                                                       value="<%=path2%>" size="80" readonly="readonly"/>
                                            </td>
                                            <td class="nopadding" style="border:none !important">
                                                <a href="#" class="registry-picker-icon-link"
                                                   style="padding-left:30px"
                                                   onclick="showRegistryBrowser('resourcePath2','/_system/governance/trunk')">
                                                    <fmt:message key="gov.registry"/>
                                                </a>
                                            </td>
                                            <td class="nopadding" style="border:none !important">
                                                <a href="#"
                                                   style="padding-left:30px;padding-bottom: 0px"
                                                   onclick="visualizeResource('/_system/governance/trunk<%=path2%>', 'wsdl')"
                                                   style="background-image:url(images/editshred.png);">
                                                    <%=lblView%>
                                                </a>
                                            </td>
                                        </tr>
                                    </table>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
                <tr>
                    <td colspan="2" class="buttonRow">
                        <input type="submit" class="button submit"
                               value="<fmt:message key="resource.compare"/>"/>
                    </td>
                </tr>
                </tbody>
            </table>
    <!--Start WSDL compare result div -->
            <div id="resourceContent" align="center">
                <br><br>
                <div align="left"><h2><%=lblDiffHeading%></h2></div>
                <br>
                <table id="resourceOptionsTable" class="styledInner" cellspacing="0" width="80%">
                    <% for (int i = 0; i < membraneDiffsKeys.length; i++) { %>
                    <thead>
                    <tr>
                        <th colspan="2">
                            <a id="resourceOptionsExpandLink<%=i%>" class="icon-link"
                               style="background-image: url(images/down.gif);"
                               onclick="showResourceOptionsPane(<%=i%>)">
                                <%=membraneDiffsKeys[i]%>
                            </a>
                        </th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr id="resourceOptionsRow<%=i%>" style="display:none;">
                        <td style="padding: 0px !important;">
                            <table cellpadding="0" cellspacing="0" class="styledInner" width="100%"
                                   style="margin-left:0px;">
                                <tr>
                                    <td colspan="2">
                                        <table class="normal-nopadding">
                                            <tr>
                                                <td style="width:120px">
                                                        <pre>
                                                            <%=membraneDiffsVals[i]%>
                                                        </pre>
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    </tbody>
                    <% } %>
                </table>
            </div>
    <!--End WSDL compare result div -->
        </form>
</div>
</fmt:bundle>