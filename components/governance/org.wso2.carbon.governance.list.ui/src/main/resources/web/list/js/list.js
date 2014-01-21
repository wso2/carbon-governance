function deleteService(pathToDelete, parentPath,redirectpath) {
    sessionAwareFunction(function() {
        CARBON.showConfirmationDialog(org_wso2_carbon_governance_list_ui_jsi18n["are.you.sure.you.want.to.delete"] + "<strong>'" + pathToDelete + "'</strong> " + org_wso2_carbon_governance_list_ui_jsi18n["permanently"], function() {

            var addSuccess = true;
            new Ajax.Request('../resources/delete_ajaxprocessor.jsp', {
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

    }, org_wso2_carbon_governance_list_ui_jsi18n["session.timed.out"]);
}

function submitFilterForm() {
    sessionAwareFunction(function() {
        var advancedSearchForm = $('filterForm');
        advancedSearchForm.submit();
    }, org_wso2_carbon_governance_list_ui_jsi18n["session.timed.out"]);
}

function clearAll(){
    var table = $('#_innerTable');
    var Inputrows = table.getElementsByTagName('input');

    for (var i = 0; i < Inputrows.length; i++) {
         if (Inputrows[i].type == "text") {
            Inputrows[i].value = "";
         } else if (Inputrows[i].type == "checkbox") {
             Inputrows[i].checked = false;
         }
    }

    var TextAreas = table.getElementsByTagName('textarea');
    for (var i = 0; i < TextAreas.length; i++) {
        TextAreas[i].value = "";
    }
    var SelectAreas = table.getElementsByTagName('select');
    for (var i = 0; i < SelectAreas.length; i++) {
        SelectAreas[i].selectedIndex = 0;
    }
}

function loadPagedList(page, filter, contextName, pageName, itemName) {
    if (filter == true) {
        window.location = "../" + contextName + "/" + pageName + ".jsp?filter=filter&region=region3&item=governance_list_" + itemName + "menu&page=" + page;
    } else {
        window.location = "../" + contextName + "/" + pageName + ".jsp?region=region3&item=governance_list_" + itemName + "_menu&page=" + page;

    }
}

function downloadDependencies(path) {
    sessionAwareFunction(function() {
            new Ajax.Request('../list/download_util_ajaxprocessor.jsp',
            {
                method:'post',
                parameters: {path: path},

                onSuccess: function(transport) {
                  var str = transport.responseText.trim();
                  var resp = str.substring(str.indexOf('{')+1).split('}')[0].trim();
                  var url = resp.split('**')[0];
                  var hasDependencies = resp.split('**')[1];
                  downloadWithDependencies(url,hasDependencies);
                },

                onFailure: function() {
                    CARBON.showErrorDialog(transport.responseText);
                }
            });

    }, org_wso2_carbon_governance_list_ui_jsi18n["session.timed.out"]);
}

