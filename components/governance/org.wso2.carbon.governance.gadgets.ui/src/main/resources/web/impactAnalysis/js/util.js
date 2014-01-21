function showResourceBox(place) {
    $j("#dialogs").append('<div id="dialog"></div>');
    showResourceTree(place, callbackOnOk);
}
;

function callbackOnOk() {
    makeRequestCall('../gauges/gadgets/flash/flashdata-ajaxprocessor.jsp', {funcName:'getResourceImpact', path: $j('#regPath').val(), reverse: 'true'}, init);
};

function showError(msg) {
//    console.info('showing error');
    $j('#msg').text(msg);
    $j('#msg').toggle('fast',
            function() {
                $j('#msg').focus();
                var callbck = function() {
                    if ($j('#msg').is(":visible")) {
                        $j('#msg').hide('slow');
                    }
                };
                $j('#msg').blur(callbck);
                $j('#msg').click(callbck);
            }
            );
}
;

var jsonDummy = {
    "id":"c13310d5-0860-47bd-af27-0888698cdaf6",
    "name":"Resource Impact",
    "data": { }, "children":[
        { "id" : "c02f8a09-56e7-4333-b0e9-db716b133f6e" ,"name" : "Dummy" ,"data" : {} , "children" : [
            { "id" : "c2343874-e23c-4f27-a611-a4fee53412c9" ,"name" : "text" ,"data" : {} , "children" : [
                { "id" : "718385c0-41b9-47c5-ab37-d6fbc023f6ca" ,"name" : "wsdl_with_EncrOnlyAnonymous.wsdl" ,"data" : {} , "children" : [
                    { "id" : "3980523e-9330-40e5-9fb0-ff715c9ce88f" ,"name" : "wsdl_with_EncrOnlyAnonymous.wsdl" ,"data" : {} , "children" : [
                        { "id" : "6fdc23b6-9f5e-4673-8110-55a4fec214f9" ,"name" : "depends" ,"data" : {} , "children" : [
                            { "id" : "47f41ccc-ec15-4989-ba34-75acc1420a5d" ,"name" : "ep-SimpleStockQuoteService1M-SimpleStockQuoteService1MHttpSoap11Endpoint" ,"data" : {} , "children" : [] }
                            ,
                            { "id" : "2613b3de-e7f8-4b24-8a5c-e2d52a0540a7" ,"name" : "ep-SimpleStockQuoteService1M-SimpleStockQuoteService1MHttpsSoap11Endpoint" ,"data" : {} , "children" : [] }
                            ,
                            { "id" : "45361def-228f-43c2-800d-b0660b26e56c" ,"name" : "ep-SimpleStockQuoteService1M-SimpleStockQuoteService1MHttpSoap12Endpoint" ,"data" : {} , "children" : [] }
                            ,
                            { "id" : "8fcbad95-ebe0-44cb-aaec-c9522f8c19d7" ,"name" : "ep-SimpleStockQuoteService1M-SimpleStockQuoteService1MHttpsSoap12Endpoint" ,"data" : {} , "children" : [] }

                        ] }
                        ,
                        { "id" : "67e5ad6b-47f0-4743-945a-4e14b2a7e04c" ,"name" : "usedBy" ,"data" : {} , "children" : [
                            { "id" : "6913e065-427e-4198-9067-c772b302db18" ,"name" : "EncrOnlyAnonymous.xml" ,"data" : {} , "children" : [] }
                            ,
                            { "id" : "1f84dc55-ee2a-4a9d-bae0-f3dcf04e2f39" ,"name" : "SimpleStockQuoteService1M" ,"data" : {} , "children" : [] }

                        ] }
                    ] }
                ] }

            ] }
        ] }
    ]
};

function canJSON(value) {
    try {
        JSON.stringify(value);
        return true;
    } catch (ex) {
//        console.log(ex)
        return false;
    }
}
;