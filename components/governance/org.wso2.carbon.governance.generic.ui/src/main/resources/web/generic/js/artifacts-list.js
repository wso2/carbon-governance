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

function setVisibility() {
    var visible = $('filterByList').value;
    document.getElementById('filterBy').value = visible;
    resetInputVisibility();
    switch (visible) {
        case "1":
            $('lifeCycleList').style.display = "";
            $('inoutListLC').style.display = "";
            $('stateList').style.display = "";
            $('inoutListLCState').style.display = "";
            break;
        default:
            $('id_Search_Val').style.display = "";
            break;
    }

}
//change the visibility of the search components to hidden state
function resetInputVisibility() {
    $('lifeCycleList').style.display = "none";
    $('stateList').style.display = "none";
    $('id_Search_Val').style.display = "none";
    $('inoutListLC').style.display = "none";
    $('inoutListLCState').style.display = "none";

}
//change the sting in to pascal Case e.g overview name -> Overview_Name
function toPascalCase(str) {
    var arr = str.split(/\s|_/);
    for(var i=0,l=arr.length; i<l; i++) {
        arr[i] = arr[i].substr(0,1).toUpperCase() +
            (arr[i].length > 1 ? arr[i].substr(1)+"_" : "_");
    }
    return arr.join("");
}

function changeVisibility() {
    var visible = $('filterByList').value;
    document.getElementById('filterBy').value = visible;
    resetInputVisibility();
    switch (visible) {
        case "1":
            $('lifeCycleList').style.display = "";
            $('inoutListLC').style.display = "";
            $('stateList').style.display = "";
            $('inoutListLCState').style.display = "";
            changeLC();
            break;
        default:
            $('id_Search_Val').style.display = "";
            $('id_Search_Val').value = "";
            break;
    }

}
/**
 This method is called at the page load and when the selected LC is changed in the LC select drop-down
 This method load the state list related to the selected LC and fill the state list drop down using them
 uses the lc_state_list_gen_ajaxprocessor.jsp
 */

function changeLC() {
    if(document.getElementById('filterBy')){
        if(document.getElementById('filterBy').value == "null"||document.getElementById('filterBy').value == "1"){
            var visible = $('lifeCycleList').value;
            var inout = $('inoutListLC').value;
            var lc_state = document.getElementById('searchVal3').value;
            if(visible == "Any"|| inout=="out" ){
                $('stateList').style.display = "none";
                $('inoutListLCState').style.display = "none";
            }else{
                var stateHtml = null;
                new Ajax.Request('../generic/lc_state_list_gen_ajaxprocessor.jsp', {
                    method:'post',
                    parameters: {LCName: visible, LCState: lc_state},

                    onSuccess: function(data) {
                        stateHtml =  eval(data).responseText;
                        $('stateList').outerHTML = "<select id='stateList'>" + stateHtml + "</select>";
                        $('stateList').style.display = "";
                        $('inoutListLCState').style.display = "";
                    },
                    onFailure: function(transport) {
                        CARBON.showErrorDialog("Failed to load all states of "+visible);
                    }
                });
            }
        }else{
            setVisibility();
        }
    }
}

function changeInOutListLC() {

    var visible = $('inoutListLC').value;
    var lc_name = $('lifeCycleList').value;
    if(visible == "out" || lc_name == "Any"){
        $('stateList').style.display = "none";
        $('inoutListLCState').style.display = "none";
    } else {
        var stateHtml = null;
        var lc_state = $('stateList').value;
        new Ajax.Request('../generic/lc_state_list_gen_ajaxprocessor.jsp', {
            method:'post',
            parameters: {LCName: lc_name, LCState: lc_state},

            onSuccess: function(data) {
                stateHtml =  eval(data).responseText;
                $('stateList').outerHTML = "<select id='stateList'>" + stateHtml + "</select>";
                $('stateList').style.display = "";
                $('inoutListLCState').style.display = "";
            },
            onFailure: function(transport) {
                CARBON.showErrorDialog("Failed to load all states of "+visible);
            }
        });
    }
}

