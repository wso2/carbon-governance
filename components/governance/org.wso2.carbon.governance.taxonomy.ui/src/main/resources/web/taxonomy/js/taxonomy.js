/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

/**
 * This method will redirects to the the edit window
 * @param taxonomyName name of the taxonomy
 * @param viewTaxonomy
 */
function editTaxonomy(taxonomyName, viewTaxonomy) {
    sessionAwareFunction(function () {
        document.location.href = "../taxonomy/source_taxonomy.jsp?taxonomyName=" + taxonomyName + (viewTaxonomy ? "&view=view" : "");
    }, org_wso2_carbon_governance_taxonomy_ui_jsi18n["session.timed.out"]);


}

/**
 * This method will invoke delete method in jsp
 *
 * @param taxonomyName name of the taxonomy
 */
function deleteTaxonomy(taxonomyName) {
    sessionAwareFunction(function () {
        CARBON.showConfirmationDialog(org_wso2_carbon_governance_taxonomy_ui_jsi18n["are.you.sure.you.want.to.delete"] +
            " ?", function () {
            new Ajax.Request('../taxonomy/delete_taxonomy-ajaxprocessor.jsp', {
                method: 'post',
                parameters: {taxonomyName: taxonomyName},
                onSuccess: function (transport) {
                    if (!transport) {
                        return;
                    }
                    window.location = "../taxonomy/taxonomy.jsp?region=region3&item=governance_taxonomy_menu";
                },
                onFailure: function (transport) {
                    {
                        CARBON.showErrorDialog(transport.responseText);
                    }
                }
            });
        });
    }, org_wso2_carbon_governance_taxonomy_ui_jsi18n["session.timed.out"]);
}

/**
 * This method will invoke the save method in jsp
 *
 * @param taxonomyName taxonomy name
 * @param isNew new taxonomy or existing one. (edit mode)
 * @param override
 */
function saveTaxonomy(taxonomyName, isNew, override) {
    sessionAwareFunction(function () {
        var payloadVar = editAreaLoader.getValue("payload");
        if (payloadVar != "") {
            new Ajax.Request('../taxonomy/save_taxonomy-ajaxprocessor.jsp', {
                method: 'post',
                parameters: {payload: payloadVar, taxonomyName: taxonomyName, isNew: isNew, updateOverride: override},
                onSuccess: function (transport) {

                    window.location = "../taxonomy/taxonomy.jsp?region=region3&item=governance_taxonomy_menu";
                },
                onFailure: function (transport) {

                    CARBON.showErrorDialog(transport.responseText);
                }
            });
        } else {
            var message = org_wso2_carbon_governance_taxonomy_ui_jsi18n["configuration.empty"];
            CARBON.showWarningDialog(message);
        }
    }, org_wso2_carbon_governance_taxonomy_ui_jsi18n["session.timed.out"]);
}

