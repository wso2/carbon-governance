/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

function deleteArtifact(pathToDelete, parentPath,redirectpath) {
    sessionAwareFunction(function() {
        CARBON.showConfirmationDialog(org_wso2_carbon_governance_generic_ui_jsi18n["are.you.sure.you.want.to.delete"]
            + "<strong>'" + pathToDelete + "'</strong> " + org_wso2_carbon_governance_generic_ui_jsi18n["permanently"],
            function() {

            var addSuccess = true;
            new Ajax.Request('../generic/delete_ajaxprocessor.jsp', {
                method:'post',
                parameters: {pathToDelete: pathToDelete, parentPath: parentPath},

                onSuccess: function() {
                    location.href=redirectpath;

                },

                onFailure: function(transport) {
                    addSuccess = false;
                    CARBON.showErrorDialog(org_wso2_carbon_registry_resource_ui_jsi18n["failed.to.delete"] +
                        " <strong>'" + pathToDelete + "'</strong>. " + transport.responseText);
                }
            });

        }, null);

    }, org_wso2_carbon_governance_generic_ui_jsi18n["session.timed.out"]);
}


function saveRXT(_path,_rxtName) {
    sessionAwareFunction(function() {
        var _payload = editAreaLoader.getValue("payload");
        new Ajax.Request('../generic/save_artifact_ajaxprocessor.jsp', {
            method:'post',
            parameters: {payload:_payload,path:_path, rxtName:_rxtName},

            onSuccess: function() {
                var message = org_wso2_carbon_governance_generic_ui_jsi18n["configuration.saved"];
                CARBON.showInfoDialog(message, function() {
                    window.location = "generic_artifact.jsp?region=region3&item=governance_generic_menu";
                });
            },

            onFailure: function(transport) {
                var responseText = transport.responseText;
                CARBON.showErrorDialog(org_wso2_carbon_governance_generic_ui_jsi18n["failed.to.save"] +
                    transport.responseText);
            }
        });

    }, org_wso2_carbon_governance_generic_ui_jsi18n["session.timed.out"]);
}

function addRXT() {
    sessionAwareFunction(function() {
        var _payload = editAreaLoader.getValue("payload");
        new Ajax.Request('../generic/save_artifact_ajaxprocessor.jsp', {
            method:'post',
            parameters: {payload:_payload},

            onSuccess: function() {
                var message = org_wso2_carbon_governance_generic_ui_jsi18n["configuration.saved"];
                CARBON.showInfoDialog(message, function() {
                    window.location = "generic_artifact.jsp?region=region3&item=governance_generic_menu";
                });
            },

            onFailure: function(transport) {
                var responseText = transport.responseText;
                CARBON.showErrorDialog(org_wso2_carbon_governance_generic_ui_jsi18n["failed.to.save"] +
                        transport.responseText);
            }
        });

    }, org_wso2_carbon_governance_generic_ui_jsi18n["session.timed.out"]);
}