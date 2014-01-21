function submitSubscription(page, pageNumber){
    sessionAwareFunction(function(){
        location.href="notifications.jsp?region=region1&item=governance_notification_menu&requestedPage="+pageNumber;

    }, org_wso2_carbon_governance_notifications_ui_jsi18n["session.timed.out"])

}