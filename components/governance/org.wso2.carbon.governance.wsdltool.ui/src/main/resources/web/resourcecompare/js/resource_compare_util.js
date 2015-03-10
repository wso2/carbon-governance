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
function visualizeResourceDiff(resourcePath1, resourcePath2, type) {
    //This value is passed to address the cashing issue in IE
    var loadingContent = '<div class="ajax-loading-message"> <img src="images/ajax-loader.gif" align="top"/> <span>' +
        org_wso2_carbon_governance_wsdltool_ui_jsi18n["resource.diff.visualizer.loading"] + '</span> </div>';
    CARBON.showPopupDialog(loadingContent,
        org_wso2_carbon_governance_wsdltool_ui_jsi18n["resource.diff.visualizer"], 650, false, '', 1025);
    navigateToResourceDiffVisualizer(resourcePath1, resourcePath2, type);
}

function navigateToResourceDiffVisualizer(resourcePath1, resourcePath2, type) {
    sessionAwareFunction(function () {
        window.onload = function () {
            var dialog = $('dialog');
            dialog.innerHTML = "<iframe frameborder='0' scrolling='no' width='1025px' height='650px' " +
                "src='resource_diff_visualizer_ajaxprocessor.jsp?resourcePath1=" + resourcePath1 +
                "&resourcePath2=" + resourcePath2 + "&type=" + type + "'></iframe>";
        }
    }, org_wso2_carbon_governance_wsdltool_ui_jsi18n["session.timed.out"]);
}
