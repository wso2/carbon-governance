    function initCollapseMenu(_tableId) {
        String.prototype.startsWith = function(s) {
            if (this.indexOf(s) == 0) return true;
            return false;
        }
        var _hbanners = document.getElementById(_tableId).getElementsByTagName("tbody")[0].getElementsByTagName("tr")[0].getElementsByTagName("td")[0].getElementsByTagName("div");
        var collapseVals = new Array();
        var imgUpIds = new Array();
        var imgDownIds = new Array();
        var _false = "false";

        var j = 0;
        for (var i = 0; i < _hbanners.length; i++) {
            var div_id = _hbanners[i].getAttribute('id');

            if ((div_id != null) && (div_id.startsWith("_collapse_id_"))) {
                var collapsed = _hbanners[i].getAttribute('title');
                var div_id_tbody = div_id + "_tbody";


                document.getElementById(div_id).getElementsByTagName("tbody")[0].setAttribute('id', div_id_tbody);
                var row = document.getElementById(div_id).getElementsByTagName("thead")[0].getElementsByTagName("tr")[0];
                var th = document.createElement('th');
                th.setAttribute('style', 'border-left:0;text-align:right;width:1%;padding-top:3px;padding-right:2px');
                th.setAttribute('align', 'right');
                th.setAttribute('valign', 'top');

                var a = document.createElement('a');
                var imgup_id = div_id_tbody + "_imgup";
                var imgdown_id = div_id_tbody + "_imgdown";

                var func = "toggleThis('" + div_id_tbody + "');showHideArrow('" + imgup_id + "');showHideArrow('" + imgdown_id + "')";
                a.setAttribute('onclick', func);

                var img1 = document.createElement('img');
                img1.setAttribute('src', '../admin/images/up-arrow.gif');
                img1.setAttribute('border', '0');
                img1.setAttribute('align', 'top');
                img1.setAttribute('style', '');
                img1.setAttribute('id', imgup_id);

                var img2 = document.createElement('img');
                img2.setAttribute('src', '../admin/images/down-arrow.gif');
                img2.setAttribute('border', '0');
                img2.setAttribute('align', 'top');
                img2.setAttribute('style', 'display: none;');
                img2.setAttribute('id', imgdown_id);

                a.appendChild(img1);
                a.appendChild(img2);
                th.appendChild(a);
                row.appendChild(th);

                if (collapsed == _false) {
                    collapseVals[j] = div_id_tbody;
                    imgUpIds[j] = imgup_id;
                    imgDownIds[j] = imgdown_id;
                    j++;
                }

            }
        }
        //Initial toggle setup
        for (var k = 0; k < collapseVals.length; k++) {
            toggleThis(collapseVals[k]);
            showHideArrow(imgUpIds[k]);
            showHideArrow(imgDownIds[k]);
        }
    }

    function toggleThis(tid) {
        var _id = "#" + tid;
        jQuery(_id).toggle("slow");
    }

    function showHideArrow(divId) {
        var theDiv = document.getElementById(divId);
        if (theDiv.style.display == "none") {
            theDiv.style.display = "";
        } else {
            theDiv.style.display = "none";
        }
    }