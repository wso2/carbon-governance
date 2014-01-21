package org.wso2.carbon.governance.generic.ui.common.dataobjects;

/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.lang.StringEscapeUtils;
import org.wso2.carbon.governance.generic.ui.utils.UIGeneratorConstants;

public class DateField extends UIComponent {

    private String value;
    private boolean isSkipName;

    public DateField(String label, String name, String id, String mandatory, String widget, String value,
                     boolean isReadOnly, String tooltip, boolean isSkipName,boolean isJSGenerating) {
        super(label, name, id, mandatory, widget, isReadOnly, tooltip, isJSGenerating);
        this.value = value;
        this.isSkipName = isSkipName;
    }

    @Override
    public String generate() {
        StringBuilder element = new StringBuilder();
        String id;
        String dateFieldName = widget.replaceAll(" ", "") + "_" + name.replaceAll(" ", "");

        if (this.id == null) {
            id = "id_" + dateFieldName;
        } else {
            id = this.id;
        }
        if (value != null) {
             value = StringEscapeUtils.escapeHtml(value);
        }
        if (isSkipName) {
            element.append("</tr>");
        }
        if (label != null ){
            element.append("<td class=\"leftCol-big\">").append((label != null ? label : "") );
        }
        if ("true".equals(mandatory)) {
            element.append("<span class=\"required\">*</span>");
        }
        if (label != null ) {
            element.append("</td>");
        }

        element.append("<td>");
        if (!isReadOnly) {
            element.append("<a class=\"icon-link\" style=\"background-image: ")
                    .append("url( ../admin/images/calendar.gif);\" ")
                    .append((isJSGenerating? "onclick=\"jQuery(\\'#" + id + "\\').datepicker(\\'show\\');\""
                            : "onclick=\"jQuery(\'#" + id + "\').datepicker(\'show\');\"")+
                            " href=\"javascript:void(0)\"></a>");
        }
        element.append("<input type=\"text\" name=\"").append(dateFieldName).append("\" title=\"").append(tooltip)
                .append("\" style=\"width:").append(UIGeneratorConstants.DATE_WIDTH).append("px\"")
                .append(isReadOnly ? " readonly" : "").append(" id=\"").append(id).append("\" value=\"")
                .append((value != null ? value : "")).append("\" />");
        element.append("</td>");

        if (isSkipName) {
            element.append("</tr>");
        }
        return element.toString();
    }

}
