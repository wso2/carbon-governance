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

function unsubscribeAndSubscribe(path, id, reason) {
    var addSuccess = false;
    CARBON.showConfirmationDialog(reason, function() {
        sessionAwareFunction(function() {
            new Ajax.Request('../info/subscription-ajaxprocessor.jsp', {
                method: 'post',
                parameters: {path: path, id: id},
                onSuccess: function(transport) {
                    if (!transport) {return;}
                    subscribe();
                },
                onFailure: function() {

                }
            });
        }, org_wso2_carbon_governance_notifications_ui_jsi18n["session.timed.out"]);
    }, null);
    return addSuccess;
}

function unsubscribe(path, id) {
    CARBON.showConfirmationDialog(org_wso2_carbon_governance_notifications_ui_jsi18n["are.you.sure.you.want.to.unsubscribe"], function() {
        var addSuccess = true;
        sessionAwareFunction(function() {
            new Ajax.Request('../info/subscription-ajaxprocessor.jsp', {
                method: 'post',
                parameters: {path: path, id: id},
                onSuccess: function(transport) {
                    if (!transport) {return;}
                    window.location = "../notifications/notifications.jsp?region=region1&item=governance_notification_menu";
                },
                onFailure: function() {
                    addSuccess = false;
                }
            });
        }, org_wso2_carbon_governance_notifications_ui_jsi18n["session.timed.out"]);
    }, null);
}

var subscribeConfirms = 0;
function subscribe() {
    if(subscribeConfirms != 0){
        return;
    }
    subscribeConfirms++;
    var path = $('subscriptionPath').value;
    var endpoint = "";
    var reason = "";
    var eventName = "";
    var digest = "";
    var delimiter = "";

    if($('hierarchicalSubscriptionInfo')!= null){
        if ($('hierarchicalSubscriptionInfo').style.display != null){
            delimiter = $('hierarchicalSubscriptionList').value;
        }
    }

    switch ($('subscriptionDigestTypeInput').value) {
        case "0":
            digest = "";
            break;
        case "1":
            digest = "digest://h/";
            break;
        case "2":
            digest = "digest://d/";
            break;
        case "3":
            digest = "digest://w/";
            break;
        case "4":
            digest = "digest://f/";
            break;
        case "5":
            digest = "digest://m/";
            break;
    }
    if ($('subscriptionDataEmail').style.display == "") {
        reason += validateEmail($('subscriptionInput'));
        endpoint += digest + "mailto:" + $('subscriptionInput').value;
    } else if ($('subscriptionDataUserProfile').style.display == "") {
        reason += validateEmpty($('subscriptionInput'), org_wso2_carbon_governance_notifications_ui_jsi18n["user.name"]);
        if (reason == "") {
            var username = $('subscriptionUserProfile').value;
            if (username != "" && username != $('subscriptionInput').value) {
                reason = org_wso2_carbon_governance_notifications_ui_jsi18n["you.are.only.allowed.to.subscribe.to.your.profile"];
            }
        }
        if (reason == "") {
            reason += validateUserExists($('subscriptionInput').value);
        }
        if (reason == "") {
            reason += validateProfileExists($('subscriptionInput').value);
            if (reason != "") {
                endpoint += digest + "user://" + $('subscriptionInput').value;
                eventName = $('eventList').value;
                CARBON.showConfirmationDialog(reason + " " +
                                              org_wso2_carbon_governance_notifications_ui_jsi18n["are.you.sure.you.want.to.continue"],
                        function() {
                            subscribeConfirms = 0;
                            sessionAwareFunction(function() {
                                new Ajax.Request('../info/subscription-ajaxprocessor.jsp', {
                                    method: 'post',
                                    parameters: {path: path, endpoint: endpoint, eventName: eventName, delimiter:delimiter},
                                    onSuccess: function(transport) {
                                        if (!transport) {return;}
                                        window.location = "../notifications/notifications.jsp?region=region1&item=governance_notification_menu";
                                    },
                                    onFailure: function(transport) {
                                        showRegistryError(transport.responseText);
                                    }
                                });
                            }, org_wso2_carbon_governance_notifications_ui_jsi18n["session.timed.out"], function() {subscribeConfirms = 0});
                        },function() {
                    subscribeConfirms = 0;
                }, function() {
                    subscribeConfirms = 0;
                });
                return;
            }
        }
        endpoint += digest + "user://" + $('subscriptionInput').value;
    } else if ($('subscriptionDataRoleProfile').style.display == "") {
        reason += validateEmpty($('subscriptionInput'), org_wso2_carbon_governance_notifications_ui_jsi18n["role.name"]);
        if (reason == "") {
            var role = $('subscriptionRoleProfile').value;
            if (role != "") {
                var roles = role.split(",");
                var roleFound = false;
                for (var i = 0; i < roles.length; i++) {
                    if (roles[i] == $('subscriptionInput').value) {
                        roleFound = true;
                        break;
                    }
                }
                if (!roleFound) {
                    reason = org_wso2_carbon_governance_notifications_ui_jsi18n["you.are.only.allowed.to.subscribe.to.your.roles"];
                }
            }
        }
        if (reason == "") {
            reason += validateRoleExists($('subscriptionInput').value);
        }
        if (reason == "") {
            reason += validateRoleProfileExists($('subscriptionInput').value);
            if (reason != "") {
                endpoint += digest + "role://" + $('subscriptionInput').value;
                eventName = $('eventList').value;
                CARBON.showConfirmationDialog(reason + " " +
                                              org_wso2_carbon_governance_notifications_ui_jsi18n["are.you.sure.you.want.to.continue"],
                        function() {
                            subscribeConfirms = 0;
                            sessionAwareFunction(function() {
                                new Ajax.Request('../info/subscription-ajaxprocessor.jsp', {
                                    method: 'post',
                                    parameters: {path: path, endpoint: endpoint, eventName: eventName, delimiter:delimiter},
                                    onSuccess: function(transport) {
                                        if (!transport) {return;}
                                        window.location = "../notifications/notifications.jsp?region=region1&item=governance_notification_menu";
                                    },
                                    onFailure: function(transport) {
                                        showRegistryError(transport.responseText);
                                    }
                                });
                            }, org_wso2_carbon_governance_notifications_ui_jsi18n["session.timed.out"], function() {subscribeConfirms = 0});
                        },function() {
                    subscribeConfirms = 0;
                }, function() {
                    subscribeConfirms = 0;
                });
                return;
            }
        }
        endpoint += digest + "role://" + $('subscriptionInput').value;
    } else if ($('subscriptionDataWorkList').style.display == "") {
        reason += validateEmpty($('subscriptionInput'), org_wso2_carbon_governance_notifications_ui_jsi18n["role.name"]);
        if (reason == "") {
            var role = $('subscriptionWorkList').value;
            if (role != "") {
                var roles = role.split(",");
                var roleFound = false;
                for (var i = 0; i < roles.length; i++) {
                    if (roles[i] == $('subscriptionInput').value) {
                        roleFound = true;
                        break;
                    }
                }
                if (!roleFound) {
                    reason = org_wso2_carbon_governance_notifications_ui_jsi18n["you.are.only.allowed.to.subscribe.to.your.roles"];
                }
            }
        }
        if (reason == "") {
            reason += validateRoleExists($('subscriptionInput').value);
        }
        if (reason != "") {
            endpoint += digest + "role://" + $('subscriptionInput').value;
            eventName = $('eventList').value;
            CARBON.showConfirmationDialog(reason + " " +
                    org_wso2_carbon_governance_notifications_ui_jsi18n["are.you.sure.you.want.to.continue"],
                    function() {
                        subscribeConfirms = 0;
                        sessionAwareFunction(function() {
                            new Ajax.Request('../info/subscription-ajaxprocessor.jsp', {
                                method: 'post',
                                parameters: {path: path, endpoint: endpoint, eventName: eventName, delimiter:delimiter},
                                onSuccess: function(transport) {
                                    if (!transport) {return;}
                                    window.location = "../notifications/notifications.jsp?region=region1&item=governance_notification_menu";
                                },
                                onFailure: function(transport) {
                                    showRegistryError(transport.responseText);
                                }
                            });
                        }, org_wso2_carbon_governance_notifications_ui_jsi18n["session.timed.out"], function() {subscribeConfirms = 0});
                    },function() {
                        subscribeConfirms = 0;
                    }, function() {
                        subscribeConfirms = 0;
                    });
            return;
        }
        endpoint += "work://" + $('subscriptionInput').value;
    } else if ($('subscriptionDataJMX').style.display == "") {
        endpoint += "jmx://";
    } else {
        reason += validateUrl($('subscriptionInput'), org_wso2_carbon_governance_notifications_ui_jsi18n["web.service.url"]);
        endpoint += $('subscriptionInput').value;
    }
    if (reason == "") {
        eventName = $('eventList').value;
        var notification = $('notificationMethodList').value;
        switch (notification) {
            case "2":
                var doRest = true;
                sessionAwareFunction(function() {
                    new Ajax.Request('../info/subscription-ajaxprocessor.jsp', {
                        method: 'post',
                        parameters: {path: path, endpoint: endpoint, eventName: eventName, doRest: doRest,delimiter:delimiter},
                        onSuccess: function(transport) {
                            if (!transport) {return;}
                            subscribeConfirms = 0;
                            window.location = "../notifications/notifications.jsp?region=region1&item=governance_notification_menu";
                        },
                        onFailure: function(transport) {
                            showRegistryError(transport.responseText);
                            subscribeConfirms = 0;
                        }
                    });
                }, org_wso2_carbon_governance_notifications_ui_jsi18n["session.timed.out"], function() {subscribeConfirms = 0});
                break;
            default:
                sessionAwareFunction(function() {
                    new Ajax.Request('../info/subscription-ajaxprocessor.jsp', {
                        method: 'post',
                        parameters: {path: path, endpoint: endpoint, eventName: eventName,delimiter:delimiter},
                        onSuccess: function(transport) {
                            if (!transport) {return;}
                            subscribeConfirms = 0;
                            window.location = "../notifications/notifications.jsp?region=region1&item=governance_notification_menu";
                        },
                        onFailure: function(transport) {
                            showRegistryError(transport.responseText);
                            subscribeConfirms = 0;
                        }
                    });
                }, org_wso2_carbon_governance_notifications_ui_jsi18n["session.timed.out"], function() {subscribeConfirms = 0});
                break;
        }
    } else {
        CARBON.showWarningDialog(reason);
        subscribeConfirms = 0;
    }
}

var sub_path;

function updateSubscriber() {
    if (!$('subscriptionPath') || sub_path == $('subscriptionPath').value) {
        return;
    }
    sub_path = $('subscriptionPath').value;
    var path = sub_path;
    sessionAwareFunction(function() {
        new Ajax.Request('../notifications/registrysubscription-ajaxprocessor.jsp', {
            method: 'post',
            parameters: {path: path},
            onSuccess: function(transport) {
                $('subscription-area-div').innerHTML = transport.responseText;
            },
            onFailure: function(transport) {
                CARBON.showErrorDialog(transport.responseText, cancelAddSubscription, cancelAddSubscription);
            }
        });
    }, org_wso2_carbon_governance_notifications_ui_jsi18n["session.timed.out"]);
}

function cancelAddSubscription() {
    window.location = "../notifications/notifications.jsp?region=region1&item=governance_notification_menu";
}

function changeVisibility() {
    var visible = $('eventList').value;
    switch (visible) {
        case "0":
            $('notificationMethodList').disabled = true;
            visible = "0";
            break;
        default:
            $('notificationMethodList').disabled = false;
            visible = $('notificationMethodList').value;
            break;
    }
    resetInputVisibility();
    switch (visible) {
        case "1":
            $('subscriptionDataInputRecord').style.display = "";
            $('subscriptionDataEmail').style.display = "";
            $('subscriptionDigestType').style.display = "";
            $('subscriptionDigestTypeInput').disabled = false;
            $('subscribeButton').disabled = false;
            break;
        case "2":
            $('subscriptionDataInputRecord').style.display = "";
            $('subscriptionDataREST').style.display = "";
            $('subscribeButton').disabled = false;
            break;
        case "3":
            $('subscriptionDataInputRecord').style.display = "";
            $('subscriptionDataSOAP').style.display = "";
            $('subscribeButton').disabled = false;
            break;
        case "4":
            $('subscriptionDataInputRecord').style.display = "";
            $('subscriptionDataUserProfile').style.display = "";
            $('subscriptionDigestType').style.display = "";
            $('subscriptionDigestTypeInput').disabled = false;
            $('subscribeButton').disabled = false;
            break;
        case "5":
            $('subscriptionDataInputRecord').style.display = "";
            $('subscriptionDataRoleProfile').style.display = "";
            $('subscriptionDigestType').style.display = "";
            $('subscriptionDigestTypeInput').disabled = false;
            $('subscribeButton').disabled = false;
            break;
        case "6":
            $('subscriptionDataInputRecord').style.display = "";
            $('subscriptionDataWorkList').style.display = "";
            $('subscribeButton').disabled = false;
            break;
        case "7":
            $('subscriptionDataInputRecord').style.display = "";
            $('subscriptionDataJMX').style.display = "";
            $('subscribeButton').disabled = false;
            $('subscriptionInput').style.display = "none";
            break;
    }

}

function resetInputVisibility() {
    $('subscribeButton').disabled = true;
    $('subscriptionInput').value = "";
    $('subscriptionInput').style.display = "";
//    $('subscriptionInput').style.background = 'White';
    $('subscriptionDataInputRecord').style.display = "none";
    $('subscriptionDataEmail').style.display = "none";
    $('subscriptionDataREST').style.display = "none";
    $('subscriptionDataSOAP').style.display = "none";
    $('subscriptionDataUserProfile').style.display = "none";
    $('subscriptionDataRoleProfile').style.display = "none";
    $('subscriptionDataWorkList').style.display = "none";
    $('subscriptionDataJMX').style.display = "none";
    $('subscriptionDigestType').style.display = "none";
    $('subscriptionDigestTypeInput').value = 0;
    $('subscriptionDigestTypeInput').disabled = false;
}

function validateUserExists(username) {
    var error = "";
    new Ajax.Request('../info/is_user_valid-ajaxprocessor.jsp',
        {
            method:'post',
            parameters: {username: username},
            asynchronous:false,
            onSuccess: function(transport) {
                var returnValue = transport.responseText;
                if (returnValue.search(/----UserExists----/) == -1){
                    error = org_wso2_carbon_governance_notifications_ui_jsi18n["no.valid.user.exists"] + " <strong>" + username + "</strong>.";
                }
            },
            onFailure: function() {

            }
        });
    return error;
}

function validateProfileExists(username) {
    var error = "";
    new Ajax.Request('../info/is_profile_valid-ajaxprocessor.jsp',
        {
            method:'post',
            parameters: {username: username},
            asynchronous:false,
            onSuccess: function(transport) {
                var returnValue = transport.responseText;
                if (returnValue.search(/----ProfileExists----/) == -1){
                    error = org_wso2_carbon_governance_notifications_ui_jsi18n["no.email.exists.on.default.profile"] + " <strong>" + username + "</strong>.";
                }
            },
            onFailure: function() {

            }
        });
    return error;
}

function validateRoleExists(role) {
    var error = "";
    new Ajax.Request('../info/is_role_valid-ajaxprocessor.jsp',
        {
            method:'post',
            parameters: {role: role},
            asynchronous:false,
            onSuccess: function(transport) {
                var returnValue = transport.responseText;
                if (returnValue.search(/----RoleExists----/) == -1){
                    error = org_wso2_carbon_governance_notifications_ui_jsi18n["no.valid.role.exists"] + " <strong>" + role + "</strong>.";
                }
            },
            onFailure: function() {

            }
        });
    return error;
}

function validateRoleProfileExists(role) {
    var error = "";
    new Ajax.Request('../info/is_role_profile_valid-ajaxprocessor.jsp',
        {
            method:'post',
            parameters: {role: role},
            asynchronous:false,
            onSuccess: function(transport) {
                var returnValue = transport.responseText;
                if (returnValue.search(/----RoleProfileExists----/) == -1){
                    error = org_wso2_carbon_governance_notifications_ui_jsi18n["no.email.exists.on.default.role.profile"] + " <strong>" + role + "</strong>.";
                }
            },
            onFailure: function() {

            }
        });
    return error;
}