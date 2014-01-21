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
<%
    String lc_name = request.getParameter("lc_name");
    String lc_state = request.getParameter("lc_state");
    String lc_in_out = request.getParameter("lc_in_out");
    String lc_state_in_out = request.getParameter("lc_state_in_out");
    if(lc_state!="0"){
//response.sendRedirect("../generic/list.jsp?lc_name="+lc_name+"&lc_state="+lc_state+"&lc_in_out="+lc_in_out+"&lc_state_in_out="+lc_state_in_out+"&filter=filter&region=" + request.getParameter("region") + "&item=" + request.getParameter("item") + "&dataName=" + request.getParameter("dataName") + "&singularLabel=" + request.getParameter("singularLabel") + "&pluralLabel=" + request.getParameter("pluralLabel") + "&dataNamespace=" + request.getParameter("dataNamespace") + "&key=" + request.getParameter("key") + "&breadcrumb=" + request.getParameter("breadcrumb"));
        response.sendRedirect("../generic/" + (request.getParameter("isContent") != null ?
                                               "list_content.jsp" : "list.jsp") + "?lc_name="+lc_name+"&lc_state="+lc_state+"&lc_in_out="+lc_in_out+"&lc_state_in_out="+lc_state_in_out+"&filter=filter&region=" +
                              request.getParameter("region") + "&item=" + request.getParameter("item") + "&dataName=" +
                              request.getParameter("dataName") + "&singularLabel=" +
                              request.getParameter("singularLabel") + "&pluralLabel=" +
                              request.getParameter("pluralLabel") + "&dataNamespace=" +
                              request.getParameter("dataNamespace") + "&key=" + request.getParameter("key") +
                              "&breadcrumb=" + request.getParameter("breadcrumb") + "&hasNamespace=" +
                              request.getParameter("hasNamespace") + "&mediaType=" +
                              request.getParameter("mediaType").replace(" ", "+"));
    } else{
//        response.sendRedirect("../generic/list.jsp?lc_name="+lc_name+"&lc_in_out="+lc_in_out+"&lc_state_in_out="+lc_state_in_out+"&filter=filter&region=" + request.getParameter("region") + "&item=" + request.getParameter("item") + "&dataName=" + request.getParameter("dataName") + "&singularLabel=" + request.getParameter("singularLabel") + "&pluralLabel=" + request.getParameter("pluralLabel") + "&dataNamespace=" + request.getParameter("dataNamespace") + "&key=" + request.getParameter("key") + "&breadcrumb=" + request.getParameter("breadcrumb"));
        response.sendRedirect("../generic/" + (request.getParameter("isContent") != null ?
                                               "list_content.jsp" : "list.jsp") + "?lc_name="+lc_name+"&lc_in_out="+lc_in_out+"&lc_state_in_out="+lc_state_in_out+"&filter=filter&region=" +
                              request.getParameter("region") + "&item=" + request.getParameter("item") + "&dataName=" +
                              request.getParameter("dataName") + "&singularLabel=" +
                              request.getParameter("singularLabel") + "&pluralLabel=" +
                              request.getParameter("pluralLabel") + "&dataNamespace=" +
                              request.getParameter("dataNamespace") + "&key=" + request.getParameter("key") +
                              "&breadcrumb=" + request.getParameter("breadcrumb") + "&hasNamespace=" +
                              request.getParameter("hasNamespace") + "&mediaType=" +
                              request.getParameter("mediaType").replace(" ", "+"));
    }
%>
