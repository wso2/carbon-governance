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
<%@ page import="org.wso2.carbon.governance.generic.stub.beans.xsd.StoragePathBean" %>
<%@ page import="org.wso2.carbon.governance.generic.ui.clients.ManageGenericArtifactServiceClient" %>
<%@ page import="org.wso2.carbon.governance.generic.ui.utils.GenericUtil" %>
<%@ page import="org.wso2.carbon.registry.core.RegistryConstants" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.wso2.carbon.registry.extensions.utils.CommonConstants" %>
<script type="text/javascript" src="../resources/js/resource_util.js"></script>
<script type="text/javascript" src="../yui/build/utilities/utilities.js"></script>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<jsp:include page="../registry_common/registry_common-i18n-ajaxprocessor.jsp"/>
<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>
<jsp:include page="../resources/resource_exists_ajaxprocessor.jsp"/>
<script type="text/javascript" src="../registry_common/js/registry_validation.js"></script>
<fmt:bundle basename="org.wso2.carbon.governance.generic.ui.i18n.Resources">
    <carbon:jsi18n
            resourceBundle="org.wso2.carbon.governance.generic.ui.i18n.JSResources"
            request="<%=request%>" namespace="org.wso2.carbon.governance.generic.ui"/>
<%
    String breadcrumb = request.getParameter("breadcrumb");
    String singularLabel = request.getParameter("singularLabel");
    String mediaType = request.getParameter("mediaType").replace(" ", "+");
    String extension = request.getParameter("extension");
    String key = request.getParameter("key");
    StoragePathBean bean;
    try {
        ManageGenericArtifactServiceClient service = new ManageGenericArtifactServiceClient(config, session);
        bean = service.getStoragePath(key);
        for (int i = 0; i < bean.getSize(); i++) {
            bean.getNames()[i] = GenericUtil.getDataElementName(bean.getNames()[i]);
        }
    } catch (Exception e) {

%>
<script type="text/javascript">
    CARBON.showErrorDialog("<%=e.getMessage()%>",function(){
        location.href="../admin/index.jsp";
        return;
    });

</script>
<%
        return;
    }
    if (breadcrumb == null) {
        breadcrumb = "Artifact";
    }
    String url = "generic/list_content.jsp?region=" + request.getParameter("region") + "&item=" +
            request.getParameter("item").replace("_add_", "_list_") + "&singularLabel=" +
            singularLabel + "&pluralLabel=" + request.getParameter("pluralLabel") + "&key=" + key +
            "&extension=" + extension + "&mediaType=" + mediaType + "&breadcrumb=" + breadcrumb +
            "&hasNamespace=" + request.getParameter("hasNamespace");
    String errorURL = "generic/add_content.jsp?region=" + request.getParameter("region") + "&item=" +
            request.getParameter("item") + "&singularLabel=" + singularLabel + "&pluralLabel=" +
            request.getParameter("pluralLabel") + "&key=" + key + "&extension=" + extension +
            "&mediaType=" + mediaType + "&breadcrumb=" + breadcrumb + "&hasNamespace=" +
            request.getParameter("hasNamespace") + "&errorUpload=errorUpload";
    boolean isUploadError = false;
    if (request.getParameter("errorUpload") != null) {
        isUploadError = true;
        String error = request.getParameter("msg");
%>
<script type="text/javascript">
    CARBON.showErrorDialog("<fmt:message key="unable.to.upload.file"/> " + "<%=(error != null) ? error : ""%>");
</script>
<%
    }
%>
<script type="text/javascript">

    function viewAddUI() {
        var addSelector = document.getElementById('addMethodSelector');
        var selectedValue = addSelector.options[addSelector.selectedIndex].value;

        var uploadUI = document.getElementById('uploadUI');
        var importUI = document.getElementById('importUI');

        if (selectedValue == "upload") {

            uploadUI.style.display = "";
            importUI.style.display = "none";

        } else if (selectedValue == "import") {

            uploadUI.style.display = "none";
            importUI.style.display = "";

        }
    }
    var callback =
    {
        success:handleSuccess,
        failure:handleFailure
    };

    function handleSuccess(o) {
        window.location = '../<%=url%>';
    }

    function handleFailure(o) {
        var buttonRow = document.getElementById('buttonRow');
        var waitMessage = document.getElementById('waitMessage');

        buttonRow.style.display = "";
        waitMessage.style.display = "none";
        if (o.responseText) {
            CARBON.showErrorDialog("<fmt:message key="unable.to.upload.file"/> "+o.responseText);
        } else {
            CARBON.showErrorDialog("<fmt:message key="unable.to.upload.file"/>");
        }
    }

    function submitImportFormAsync() {
        var storagePath = '<%=RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH%>' + document.getElementById("irStoragePath").value;
        var properties = document.getElementById("irProperties").value;
        <% for (int i = 0; i < bean.getSize(); i++) {
        %>
        storagePath = storagePath.replace("@{<%=bean.getNames()[i]%>}", document.getElementById('ir<%=bean.getNames()[i]%>').value);
        properties += '<%=bean.getNames()[i]%>^^' + document.getElementById('ir<%=bean.getNames()[i]%>').value + '^|^';
        <%
        }
        %>
        document.getElementById("irProperties").value = properties.substring(0, properties.length - 3);
        storagePath = storagePath.replace("@{name}", document.getElementById("irResourceName").value);
        document.getElementById("irParentPath").value = storagePath.substring(0, storagePath.lastIndexOf("/"));
        document.getElementById("irResourceName").value = storagePath.substring(storagePath.lastIndexOf("/") + 1);

        var form = document.getElementById("ImportForm");
        YAHOO.util.Connect.setForm(form, false, false);
        YAHOO.util.Connect.asyncRequest("POST", form.getAttribute("action"), callback, null);
    }

    function submitUploadForm() {
        var storagePath = '<%=RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH%>' + document.getElementById("uStoragePath").value;
        var properties = document.getElementById("uProperties").value;
        <% for (int i = 0; i < bean.getSize(); i++) {
        %>
        storagePath = storagePath.replace("@{<%=bean.getNames()[i]%>}", document.getElementById('u<%=bean.getNames()[i]%>').value);
        properties += '<%=bean.getNames()[i]%>^^' + document.getElementById('u<%=bean.getNames()[i]%>').value + '^|^';
        <%
        }
        %>
        document.getElementById("uProperties").value = properties.substring(0, properties.length - 3);
        storagePath = storagePath.replace("@{name}", document.getElementById("uResourceName").value);
        document.getElementById("uPath").value = storagePath.substring(0, storagePath.lastIndexOf("/"));
        document.getElementById("uResourceName").value = storagePath.substring(storagePath.lastIndexOf("/") + 1);

        var form = document.getElementById("uploadForm");
        form.submit();
    }

    function clearAll() {
        document.getElementById('uResourceFile').value = "";
        document.getElementById('uResourceName').value = "";
        document.getElementById('irFetchURL').value = "";
        document.getElementById('irResourceName').value = "";
        document.getElementById('irversion').value = "";
    }

    function addFile() {
        sessionAwareFunction(function() {
        var reason = "";
        var addSelector = document.getElementById('addMethodSelector');
        var selectedValue = addSelector.options[addSelector.selectedIndex].value;

        if (selectedValue == "upload") {
            var rForm = document.forms["uploadForm"];
            var uResourceFile = document.getElementById('uResourceFile');
            var uResourceName = document.getElementById('uResourceName');

            //reason += validateEmpty(uResourceFile, "<fmt:message key="file.or.zip.file"><fmt:param value="<%=singularLabel%>"/></fmt:message>");
            if (uResourceFile.value == null || uResourceFile.value == "") {
                reason += org_wso2_carbon_registry_common_ui_jsi18n["the.required.field"] + " "+
                        "<fmt:message key="file.or.zip.file"><fmt:param value="<%=singularLabel%>"/></fmt:message>"+
                        " " + org_wso2_carbon_registry_common_ui_jsi18n["not.filled"] + "<br />";
            }

            if (reason == "") {
                reason += validateEmpty(uResourceName, "<fmt:message key="name"/>");
            }

            <% for (int i = 0; i < bean.getSize(); i++) {
            %>
            if (reason == "") {
                reason += validateEmpty(document.getElementById('u<%=bean.getNames()[i]%>'), '<%=bean.getLabels()[i]%>');
            }
            <%
            }
            %>
            var resourceName= rForm.filename.value;
            var mediatype = rForm.mediaType.value;
            var version = document.getElementById('uversion').value;
            
            //validating the version field.
            if (version.length > 0) {
                var regexp = new RegExp("<%=CommonConstants.SERVICE_VERSION_REGEX.replace("\\","\\\\") %>", "i");
                if (!version.match(regexp)) {
                    CARBON.showWarningDialog(org_wso2_carbon_governance_generic_ui_jsi18n["version.error.1"]
                            + " " + version + " " + org_wso2_carbon_governance_generic_ui_jsi18n["version.error.2"]);
                    return;
                }
            }
            if (reason == "") {
               reason += validateGenericResourceExists(resourceName,mediatype,version);
            }

            if (reason != "") {
                CARBON.showWarningDialog(reason);
                return;
            }

            submitUploadForm();

        } else if (selectedValue == "import") {

	    var rForm = document.forms["importForm"];
            var irFetchURL = document.getElementById('irFetchURL');
            var irResourceName = document.getElementById('irResourceName');

            reason += validateEmpty(irFetchURL, "<fmt:message key="file.url"><fmt:param value="<%=singularLabel%>"/></fmt:message>");
            if (reason == "") {
                reason += validateUrl(irFetchURL, "<fmt:message key="file.url"><fmt:param value="<%=singularLabel%>"/></fmt:message>");
            }

            if (reason == "") {
                reason += validateEmpty(irResourceName, "<fmt:message key="name"/>");
            }

            <% for (int i = 0; i < bean.getSize(); i++) {
            %>
            if (reason == "") {
                reason += validateEmpty(document.getElementById('ir<%=bean.getNames()[i]%>'), '<%=bean.getLabels()[i]%>');
            }
            <%
            }
            %>
	    var resourceName= rForm.resourceName.value;
            var mediatype = rForm.mediaType.value;
           var version = document.getElementById('irversion').value;
            
            if (reason == "") {
               reason += validateGenericResourceExists(resourceName,mediatype,version);
            }
            if (reason != "") {
                CARBON.showWarningDialog(reason);
                return;
            }


            var buttonRow = document.getElementById('buttonRow');
            var waitMessage = document.getElementById('waitMessage');

            buttonRow.style.display = "none";
            waitMessage.style.display = "";

            submitImportFormAsync();
        }
        }, org_wso2_carbon_governance_generic_ui_jsi18n["session.timed.out"]);
    }

    function validateGenericResourceExists(resourceName,mediaType,version)
    {
    var error = "";
    var differentiate = "differentiate";
    new Ajax.Request('generic_resource_exists_ajaxprocessor.jsp',
        {
            method:'post',
            parameters: {resourceName:resourceName,mediaType:mediaType,version:version, differentiate:differentiate,random:getRandom()},
            asynchronous:false,
            onSuccess: function(transport) {
                var returnValue = transport.responseText;
                if (returnValue.search(/----ResourceExists----/) != -1){
			
                    error = org_wso2_carbon_governance_generic_ui_jsi18n["resource.exists"];
                } 
            },
            onFailure: function() {

            }
        });
    return error;
    }


    function fillResourceImportDetails() {
        var filePath = document.getElementById('irFetchURL').value;
        var filename = resolveName(filePath);

        document.getElementById('irResourceName').value = filename;
    }

    function fillResourceUploadDetails() {
        var filePath = document.getElementById('uResourceFile').value;
        var filename = resolveName(filePath);

        // deriving the media type.
        if (filename.search(/\.<%=extension%>$/i) >= 0) {
            // so it is a single file.
            document.getElementById('uMediaType').value = '<%=mediaType%>';
            document.getElementById('uploadName').style.display = "";
            document.getElementById('uProperties').value = "";
        } else if (filename.search(/\.(zip|gar)$/i) >= 0) {
            // so it is a zip
            document.getElementById('uMediaType').value = "application/vnd.wso2.governance-archive";
            document.getElementById('uploadName').style.display = "none";
            document.getElementById('uProperties').value = 'registry.mediaType^^<%=mediaType%>' + '^|^';
        } else {
            document.getElementById('uResourceFile').value = "";
            CARBON.showWarningDialog("<fmt:message key="only.filetypes.allowed"><fmt:param value="<%=extension%>"/></fmt:message>");
        }

        document.getElementById('uResourceName').value = filename;
    }

    function resolveName(filepath) {
        var filename = "";
        if (filepath.indexOf("\\") != -1) {
            filename = filepath.substring(filepath.lastIndexOf('\\') + 1, filepath.length);
        } else {
            filename = filepath.substring(filepath.lastIndexOf('/') + 1, filepath.length);
        }
        if (filename.search(/\.[^?]*$/i) < 0) {
            filename = filename.replace("?", ".");
            var suffix = '.<%=extension%>';
            if (filename.indexOf(".") > 0) {
                filename = filename.substring(0, filename.lastIndexOf(".")) + suffix;
            } else {
                filename = filename + suffix;
            }
        }
        var notAllowedChars = "!@#;%^*+={}|<>";
        for (i = 0; i < notAllowedChars.length; i ++) {
            var c = notAllowedChars.charAt(i);
            filename = filename.replace(c, "_");
        }
        return filename;
    }

</script>
<carbon:breadcrumb
        label="<%=breadcrumb%>"
        resourceBundle="org.wso2.carbon.governance.generic.ui.i18n.Resources"
        topPage="true"
        request="<%=request%>"/>
<script type="text/javascript">
</script>
<div id="middle">

    <h2><fmt:message key="add.file"><fmt:param value="<%=singularLabel%>"/></fmt:message></h2>

    <div id="workArea">

        <table class="styledLeft">
            <thead>
            <tr>
                <th><fmt:message key="add.file.table.heading"><fmt:param value="<%=singularLabel%>"/></fmt:message></th>
            </tr>
            </thead>

            <tr>
                <td class="formRow">
                    <table width="100%">
                        <tr>
                            <td>
                                <div>
                                    <select id="addMethodSelector" onchange="viewAddUI()">
                                        <option value="import" <%= !isUploadError ?
                                                "selected=\"selected\"" : "" %> ><fmt:message
                                                key="import.file.from.url"><fmt:param value="<%=singularLabel%>"/></fmt:message></option>
                                        <option value="upload" <%= isUploadError ?
                                                "selected=\"selected\"" : "" %> ><fmt:message
                                                key="upload.file.from.file"><fmt:param value="<%=singularLabel%>"/></fmt:message></option>
                                    </select>
                                </div>
                                <br/>

                                <div id="importUI" <%= isUploadError ? "style=\"display:none;\"" :
                                        "" %> >
                                    <form action="../resources/import_resource_ajaxprocessor.jsp"
                                          method="post"
                                          id="ImportForm" name="importForm">
                                        <input type="hidden" id="irStoragePath" name="irStoragePath" value="<%=bean.getStoragePath()%>"/>
                                        <input type="hidden" name="printerror" value="true"/>
                                        <input type="hidden" id="irParentPath" name="parentPath" value="/"/>
                                        <input type="hidden" id="irProperties" name="properties" value=""/>
                                        <input type="hidden" name="mediaType"
                                               value='<%=mediaType%>'/>
                                            <%--<input type="hidden" name="isAsync" value="true"/>--%>
                                        <table class="normal">
                                            <tr>
                                                <td><fmt:message key="file.url"><fmt:param value="<%=singularLabel%>"/></fmt:message> <span
                                                        class="required">*</span></td>
                                                <td><input type="text"
                                                           onchange="fillResourceImportDetails()"
                                                           name="fetchURL" style="width:400px"
                                                           id="irFetchURL"/>
                                                </td>
                                            </tr>
                                            <tr>
                                                <td><fmt:message key="name"/> <span
                                                        class="required">*</span></td>
                                                <td><input type="text" name="resourceName"
                                                           style="width:400px"
                                                           id="irResourceName"/></td>
                                            </tr>
                                            <% for (int i = 0; i < bean.getSize(); i++) {
                                            %>
                                            <tr>
                                                <td><%=bean.getLabels()[i]%><span
                                                        class="required">*</span></td>
                                                <td><input type="text" name="ir<%=bean.getNames()[i]%>"
                                                           style="width:400px"
                                                           id="ir<%=bean.getNames()[i]%>"/></td>
                                            </tr>
                                            <%
                                            }
                                            %>
                                        </table>
                                    </form>
                                </div>
                                <div id="uploadUI" <%= !isUploadError ? "style=\"display:none;\"" :
                                        "" %> >
                                    <form method="post"
                                          name="uploadForm"
                                          id="uploadForm"
                                          action="../../fileupload/resource"
                                          enctype="multipart/form-data" target="_self">
                                        <input type="hidden" id="uStoragePath" name="uStoragePath" value="<%=bean.getStoragePath()%>"/>
                                        <input type="hidden" id="uPath" name="path" value="/"/>
                                        <input type="hidden" id="uProperties" name="properties" value=""/>
                                        <input type="hidden" id="uMediaType" name="mediaType"/>
                                        <input type="hidden" id="uDescription" name="description"
                                               value=""/>
                                        <input type="hidden" id="uRedirect" name="redirect"
                                               value='<%=url%>'/>
                                        <input type="hidden" id="uErrorRedirect"
                                               name="errorRedirect"
                                               value='<%=errorURL%>'/>

                                        <table class="normal">
                                            <tr>
                                                <td><fmt:message key="file.or.zip.file"><fmt:param value="<%=singularLabel%>"/></fmt:message> <span
                                                        class="required">*</span></td>
                                                <td><p>
                                                    <input id="uResourceFile" type="file"
                                                           name="upload" size="50"
                                                           style="background-color:#cccccc"
                                                           onchange="fillResourceUploadDetails()"
                                                           onkeypress="return blockManual(event)"/>
                                                </p>

                                                    <p>
                                                        <fmt:message
                                                                key="possible.uploadable.formats"><fmt:param value="<%=singularLabel%>"/></fmt:message>
                                                    </p>
                                                </td>
                                            </tr>
                                            <tr id="uploadName" style="display:none;">
                                                <td><fmt:message key="name"/> <span
                                                        class="required">*</span></td>
                                                <td><input type="text" name="filename"
                                                           style="width:400px"
                                                           id="uResourceName"/></td>
                                            </tr>
                                            <% for (int i = 0; i < bean.getSize(); i++) {
                                            %>
                                            <tr>
                                                <td><%=bean.getLabels()[i]%><span
                                                        class="required">*</span></td>
                                                <td><input type="text" name="u<%=bean.getNames()[i]%>"
                                                           style="width:400px"
                                                           id="u<%=bean.getNames()[i]%>"/></td>
                                            </tr>
                                            <%
                                                }
                                            %>
                                        </table>

                                    </form>
                                </div>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
            <tr id="buttonRow">
                <td class="buttonRow">
                    <input class="button registryWriteOperation" type="button" onClick="addFile();"
                           value='<fmt:message key="add"/>'/>
                    <input class="button registryNonWriteOperation" type="button"
                           disabled="disabled"
                           value='<fmt:message key="add"/>'/>
                    <input type="button" id="#_1" value="<fmt:message key="clear"/>" class="button"
                           onclick="clearAll()"/>
                </td>
            </tr>
            <tr id="waitMessage" style="display:none">
                <td>
                    <div style="font-size:13px !important;margin-top:10px;margin-bottom:10px;margin-left:5px !important"
                         class="ajax-loading-message"><img
                            src="images/ajax-loader.gif" align="left" hspace="20"/><fmt:message
                            key="please.wait.until.file.is.added"/>...
                    </div>
                </td>
            </tr>
        </table>
    </div>
</div>
<script type="text/javascript">
    jQuery(document).ready(function() {
        var addSelector = document.getElementById('addMethodSelector');
        addSelector.selectedIndex = <%= isUploadError? "1" : "0" %>;
    });
</script>
</fmt:bundle>
