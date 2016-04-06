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

<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.wso2.carbon.governance.generic.ui.clients.ManageGenericArtifactServiceClient" %>
<%@ page import="org.wso2.carbon.governance.generic.stub.beans.xsd.ContentArtifactsBean" %>

<%

    ContentArtifactsBean bean;
    String mediatype = request.getParameter("mediaType");
    String resource_Name = request.getParameter("resourceName");
    String version = request.getParameter("version");
    try {
        ManageGenericArtifactServiceClient listservice = new ManageGenericArtifactServiceClient(config, session);
        bean = listservice.listContentArtifacts(mediatype);
        if (bean.getSize() != 0) {
            for (int i = 0; i < bean.getSize(); i++) {
                String resourceName = bean.getName()[i];
                String path = bean.getPath()[i];
                if (version != null) {
                    //This fix was added to resolve the problem of false resource exists message when a different
                    //version of the  resource exists.
                    if (path.contains(version) && resource_Name.equals(resourceName)) {
                        String[] splitedPath = path.split("/");
                        for (int a = 0; a < splitedPath.length; a++) {
                            if (splitedPath[a] != null && splitedPath[a].equals(version)) {
%>----ResourceExists----<%
            }
        }
    }
} else {
    //Else is introduced to preserve original functionality
    if (resource_Name.equals(resourceName)) {
%>----ResourceExists----<%
                    }
                }
            }
        }
    } catch (Exception e) {
    }

%>



