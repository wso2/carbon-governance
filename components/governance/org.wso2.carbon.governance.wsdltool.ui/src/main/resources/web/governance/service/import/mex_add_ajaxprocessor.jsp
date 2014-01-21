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

<%
    String parentPath = request.getParameter("parentPath");
%>

<br/>
<form id="customUIForm" action="../governance/service/import/mex_add_handler_ajaxprocessor.jsp" method="post">
<input type="hidden" name="parentPath" value="<%=parentPath%>"/>
<table cellspacing="0" cellpadding="0" border="0" style="width:100%" class="styledLeft">
    <thead>
        <tr>
            <th colspan="2">WSDL Service Import Tool</th>
        </tr>
    </thead>
    <tbody>
        <tr>
			<td>WSDL URL<span class="required">*</span></td>
			<td>
				<input type="text" name="wsdlURL" style="width:100%"/>
				<div id="urlHelpText" class="helpText" style="color: rgb(154, 154, 154);"> Give the full url of the resource to fetch content from URL </div>
			</td>
        </tr>
        <tr>
            <td>Owner</td>
            <td><input type="text" name="ownerName" style="width:100%"/></td>
        </tr>
        <tr>
            <td>Description</td>
            <td><textarea id="description" rows="3" cols="1" name="description" style="width:100%"></textarea></td>
        </tr>
        <tr>
			<td class="buttonRow" colspan="2">
            	<input type="submit" class="button registryWriteOperation" value="Import Service Details"/>
			</td>
        </tr>
    </tbody>
</table>
</form>

<br/>

