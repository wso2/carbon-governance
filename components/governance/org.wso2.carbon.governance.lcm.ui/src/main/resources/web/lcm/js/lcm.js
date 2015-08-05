/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

var lifecyleOperationStarted = false;
    function editLC(lifecycleName, viewLC) {
        if (lifecyleOperationStarted) {
            CARBON.showWarningDialog(org_wso2_carbon_governance_lcm_ui_jsi18n["lifecycle.operation.in.progress"]);
            return;
        }
        lifecyleOperationStarted = true;
        sessionAwareFunction(function() {
            lifecyleOperationStarted = false;
            document.location.href = "../lcm/source_lcm.jsp?lifecycleName=" + lifecycleName + (viewLC ? "&view=view" : "");
        }, org_wso2_carbon_governance_lcm_ui_jsi18n["session.timed.out"]);
        lifecyleOperationStarted = false;
    }

    function findUsage(lifecycleName) {
        if (lifecyleOperationStarted) {
            CARBON.showWarningDialog(org_wso2_carbon_governance_lcm_ui_jsi18n["lifecycle.operation.in.progress"]);
            return;
        }
        lifecyleOperationStarted = true;
        sessionAwareFunction(function() {
            lifecyleOperationStarted = false;
            window.location = "../search/advancedSearch.jsp?region=region3&item=" +
                              "registry_search_menu&propertyName=registry.LC.name&rightOp=eq&rightPropertyValue=" +
                              lifecycleName;
        }, org_wso2_carbon_governance_lcm_ui_jsi18n["session.timed.out"]);
        lifecyleOperationStarted = false;
    }

    function deleteLC(lifecycleName) {
        if (lifecyleOperationStarted) {
            CARBON.showWarningDialog(org_wso2_carbon_governance_lcm_ui_jsi18n["lifecycle.operation.in.progress"]);
            return;
        }
        lifecyleOperationStarted = true;
        sessionAwareFunction(function() {
            new Ajax.Request('../lcm/nameinusecheck_lcm_ajaxprocessor.jsp',
            {
                method:'post',
                parameters: {lifecycleName: lifecycleName},
                evalScripts : true,
                asynchronous:false,
                onSuccess: function(transport) {
                    var returnValue = transport.responseText;
                    if(returnValue.search(/----IsInUse----/) != -1){
                        CARBON.showErrorDialog(org_wso2_carbon_governance_lcm_ui_jsi18n["unable.to.delete.lifecycle.in.use"]);
                    } else {
                        CARBON.showConfirmationDialog(org_wso2_carbon_governance_lcm_ui_jsi18n["are.you.sure.you.want.to.delete"] + " " + lifecycleName + "?", function() {
                            new Ajax.Request('../lcm/delete_lcm-ajaxprocessor.jsp', {
                                method: 'post',
                                parameters: {lifecycleName: lifecycleName},
                                onSuccess: function(transport) {
                                    if (!transport) {
                                        lifecyleOperationStarted = false;
                                        return;
                                    }
                                    var message = org_wso2_carbon_governance_lcm_ui_jsi18n["configuration.deleted"];
                                    lifecyleOperationStarted = false;
                                    window.location = "lcm.jsp?region=region3&item=governance_lcm_menu";
                                    CARBON.showInfoDialog(message);
                                },
                                onFailure: function(transport) {
                                {
                                    CARBON.showErrorDialog(transport.responseText);
                                }
                                }
                            });
                        });
                    }
                },
                onFailure: function(transport) {
                    CARBON.showErrorDialog(org_wso2_carbon_governance_lcm_ui_jsi18n["failed.to.delete"] + ' ' + transport.responseText);
                }
            });
        }, org_wso2_carbon_governance_lcm_ui_jsi18n["session.timed.out"]);
        lifecyleOperationStarted = false;
    }

/*
     function saveAndValidateLC(lifecycleName, isNew,override) {

          var xmlContent = editAreaLoader.getValue("payload");
          var _schema = "lifecycle-config";
           new Ajax.Request('../services/xmlconfig_validator_ajaxprocessor.jsp',
            {
                method:'post',
                parameters: { target_xml: xmlContent,schema: _schema},
                onSuccess: function(transport) {
                 var returnValue = transport.responseText;
                 if (returnValue.search(/---XMLSchemaValidated----/) != -1) {
                   saveLC(lifecycleName, isNew,override);
                 } else {
                     CARBON.showErrorDialog(transport.responseText);
                 }
                },

                onFailure: function(transport) {
                    CARBON.showErrorDialog(transport.responseText);
                    return;
                }
            });
     }
*/

    function saveLC(lifecycleName, isNew,override) {
        if (lifecyleOperationStarted) {
            CARBON.showWarningDialog(org_wso2_carbon_governance_lcm_ui_jsi18n["lifecycle.operation.in.progress"]);
            return;
        }
        lifecyleOperationStarted = true;
        sessionAwareFunction(function() {
            var param1 = "";
            if (isNew) {
                param1 = editAreaLoader.getValue("payload");
            } else {
                param1 = lifecycleName;
            }

            new Ajax.Request('../lcm/nameinusecheck_lcm_ajaxprocessor.jsp',
            {
                method:'post',
                parameters: {lifecycleName: param1},
                evalScripts : true,
                asynchronous:false,
                onSuccess: function(transport) {
                    var returnValue = transport.responseText;
                    if (returnValue.search(/----IsInUse----/) != -1) {
                        if (lifecycleName != "null") {
                            CARBON.showConfirmationDialog(lifecycleName + " " +org_wso2_carbon_governance_lcm_ui_jsi18n["lifecycle.operation.edit.warn"],function() {
                                saveLCPlayLoad(lifecycleName, isNew,override);           		          		
                            },function() {	});
                        } else {
                            CARBON.showErrorDialog(org_wso2_carbon_governance_lcm_ui_jsi18n["unable.to.save.lifecycle.in.use"] );
                        }
                    } else {
                        saveLCPlayLoad(lifecycleName, isNew,override);
                    }
                },
                onFailure: function(transport) {
                    CARBON.showErrorDialog(org_wso2_carbon_governance_lcm_ui_jsi18n["failed.to.save"] + ' ' + transport.responseText);
                }
            });
        }, org_wso2_carbon_governance_lcm_ui_jsi18n["session.timed.out"]);
        lifecyleOperationStarted = false;
    }
    
    function saveLCPlayLoad(lifecycleName, isNew,override) {
        var payloadVar = editAreaLoader.getValue("payload");
        if (payloadVar != "") {
            new Ajax.Request('../lcm/save_lcm-ajaxprocessor.jsp', {
                method: 'post',
                parameters: {lifecycleName: lifecycleName, isNew: isNew, payload: payloadVar, updateOverride: override},
                onSuccess: function(transport) {
                    if (!transport) {
                        lifecyleOperationStarted = false;
                        return;
                    }
                   // var message = org_wso2_carbon_governance_lcm_ui_jsi18n["configuration.saved"];
                    lifecyleOperationStarted = false;
                    window.location = "lcm.jsp?region=region3&item=governance_lcm_menu";
                   // CARBON.showInfoDialog(message, function() {
                   //     window.location = "lcm.jsp?region=region3&item=governance_lcm_menu";
                   // });
                },
                onFailure: function(transport) {
                    var responseText = transport.responseText;

                    if (responseText.lastIndexOf("Another user has already modified this resource") != -1) {
                        CARBON.showConfirmationDialog("Another user has already modified this resource. Do you want to continue",
                            function(){
                                saveLC(lifecycleName, isNew, "true");
                            }
                            , false
                            , false)
                    }
                    else {
                        CARBON.showErrorDialog(transport.responseText);
                    }
                }
            });
        } else {
            var message = org_wso2_carbon_governance_lcm_ui_jsi18n["configuration.empty"];
            CARBON.showWarningDialog(message);
        }
    }
    