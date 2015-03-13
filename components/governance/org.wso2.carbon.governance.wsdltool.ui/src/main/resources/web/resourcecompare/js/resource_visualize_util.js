/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
function visualizeResource(path, type) {
    //This value is passed to address the cashing issue in IE
    var loadingContent = '<div class="ajax-loading-message"> <img src="../resources/images/ajax-loader.gif" align="top"/> <span>' +
        org_wso2_carbon_governance_wsdltool_ui_jsi18n["resource.visualizer.loading"] + '</span> </div>';
    CARBON.showPopupDialog(loadingContent,
        org_wso2_carbon_governance_wsdltool_ui_jsi18n["resource.visualizer"], 500, false);
    navigateToResourceVisualizer(path, path, type);
}

function navigateToResourceVisualizer(rootPath, path, type) {
    sessionAwareFunction(function () {
        var dialog = $('dialog');
        dialog.innerHTML = "<input type='button' value='" + org_wso2_carbon_governance_wsdltool_ui_jsi18n["scroll.to.top"] +
            "' onclick='navigateToResourceVisualizer(\"" + rootPath + "\", \"" + rootPath + "\", \"" + type + "\")' " +
            "style='margin-right: 5px; margin-top: 5px;' /></div>" +
            "<iframe frameborder='0' scrolling='auto' width='750px' height='440px' " +
            "src='resource_visualizer_ajaxprocessor.jsp?rootPath=" + rootPath +
            "&path=" + path + "&type=" + type + "'></iframe>";
    }, org_wso2_carbon_governance_wsdltool_ui_jsi18n["session.timed.out"]);
}