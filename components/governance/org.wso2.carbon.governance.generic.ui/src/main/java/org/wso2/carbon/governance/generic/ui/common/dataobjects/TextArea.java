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
package org.wso2.carbon.governance.generic.ui.common.dataobjects;

import org.apache.commons.lang.StringEscapeUtils;
import org.wso2.carbon.governance.generic.ui.utils.UIGeneratorConstants;

public class TextArea extends UIComponent {

    private String value;
    private int height;
    private int width;
    private boolean isRichText;
    private boolean isSkipName;

    public TextArea(String label, String name, String id, String mandatory, String widget,
                    String value, int height, int width, boolean isReadOnly,
                    boolean isRichText, String tooltip, boolean isSkipName, boolean isJSGenerating) {
        super(label, name, id, mandatory, widget, isReadOnly, tooltip, isJSGenerating);
        this.value = value;
        this.height = height;
        this.width = width;
        this.isRichText = isRichText;
        this.isSkipName = isSkipName;
    }

    @Override
    public String generate() {

        StringBuilder element = new StringBuilder();
        String id;
        String partialName = widget.replaceAll(" ", "") + "_" + name.replaceAll(" ", "");
        if (this.id == null) {
            id = "id_" + partialName;
        } else {
            id = this.id;
        }
        String customAtt = widget.replaceAll(" ", "") + "_" + name.replaceAll(" ", "");

        StringBuilder size = new StringBuilder("style=\"");
        value = StringEscapeUtils.escapeHtml(value);
        if (height > 0) {
            size.append("height:").append(height).append("px;");
        }
        if (width > 0) {
            size.append("width:").append(width).append("px\"");
        } else {
            size.append("width:").append(UIGeneratorConstants.DEFAULT_WIDTH).append("px\"");
        }
        if (isSkipName) {
             element.append("<td><textarea  name=\"").append(partialName)
                     .append("\" title=\"").append(tooltip).append("\" id=\"id_").append(partialName)
                     .append("\" ").append(size).append((isReadOnly ? " readonly" : "")).append(" >")
                     .append((value != null ? StringEscapeUtils.escapeHtml(value) : "")).append("</textarea></td>");
              return element.toString();
        }
        
        if ("true".equals(mandatory)) {
            if (isRichText) {
                element.append("<td class=\"leftCol-big\">").append(label).append("<span class=\"required\">*</span></td>")
                        .append(" <td  style=\"font-size:8px\" class=\"yui-skin-sam\"><textarea  name=\"")
                        .append(partialName).append("\" title=\"")
                        .append(tooltip).append("\" id=\"").append(id).append("\" ").append(size)
                        .append((isReadOnly ? " readonly" : "")).append(" >").append((value != null ? value : ""))
                        .append("</textarea>");
                element = appendRichTextScript(element, width, height, widget, name, partialName);
                element.append("</td></tr>");

            } else {
                element.append("<tr><td class=\"leftCol-big\">").append(label)
                        .append("<span class=\"required\">*</span></td> <td><textarea  name=\"")
                        .append(partialName).append("\" title=\"")
                        .append(tooltip).append("\" id=\"").append(id).append("\" ").append(size)
                        .append((isReadOnly ? " readonly" : "")).append(" >").append((value != null ? value : ""))
                        .append("</textarea>");
                //element = appendEmptyScript(element, widget, name, partialName);
                element.append("</td></tr>");

            }
        } else {
            if (isRichText) {
                element.append("<tr><td class=\"leftCol-big\">").append(label).append("<span class=\"required\">*</span></td>")
                        .append(" <td  style=\"font-size:8px\" class=\"yui-skin-sam\"><textarea  name=\"")
                        .append(partialName).append("\" title=\"")
                        .append(tooltip).append("\" id=\"").append(id).append("\" ").append(size)
                        .append((isReadOnly ? " readonly" : "")).append(" >").append((value != null ? value : ""))
                        .append("</textarea>");
                element = appendRichTextScript(element, width, height, widget, name, partialName);
                element.append("</td></tr>");

            } else {
                if (label != null) {
                    element.append("<tr><td class=\"leftCol-big\">").append(label).append("</td>");
                }
                element.append("<td><textarea  name=\"").append(partialName).append("\" title=\"").append(tooltip)
                        .append("\" id=\"").append(id).append("\" ").append(size).append((isReadOnly ? " readonly" : ""))
                        .append(" >").append((value != null ? value : "")).append("</textarea>");
                //element = appendEmptyScript(element, widget, name);
                if (label != null) {
                    element.append("</td></tr>");
                }

            }
        }
        
        return element.toString();
    }

    private StringBuilder appendRichTextScript(StringBuilder element, int width, int height, String widget, String name,
                                               String attrName) {
        String eleName = "id_" + attrName;
        String ele_id = "_id_" + attrName;
        String fun_name = "set_" + eleName;
        String richTextAttrName = "yui_txt_" + eleName;
        element.append("<script>\n")
                .append("\n").append("var ").append(richTextAttrName).append(";\n")
                .append("(function() {\n")
                .append("    var Dom = YAHOO.util.Dom,\n")
                .append("        Event = YAHOO.util.Event;\n")
                .append("    \n")
                .append("    var myConfig = {\n")
                .append("        height: '").append("120px',\n")
                .append("        width: '").append("400px',\n")
                .append("        dompath: true,\n")
                .append("        focusAtStart: true\n")
                .append("    };\n")
                .append("\n")
                .append("    YAHOO.log('Create the Editor..', 'info', 'example');\n")
                .append("    ").append(richTextAttrName).append(" = new YAHOO.widget.SimpleEditor('").append(eleName).append("', myConfig);\n")
                .append("    ").append(richTextAttrName).append(".render();\n")
                .append("\n")
                .append("})();\n");

        element.append("function ").append(fun_name).append("(){\n")
                .append("        var form1 = document.getElementById('CustomUIForm');\n")
                .append("        var newInput = document.createElement('input');\n")
                .append("        newInput.setAttribute('type','hidden');\n")
                .append("        newInput.setAttribute('name','").append(attrName).append("');\n")
                .append("        newInput.setAttribute('id','").append(ele_id).append("');\n")
                .append("        form1.appendChild(newInput);")

                .append("    var contentText=\"\";\n")
                .append("    ").append(richTextAttrName).append(".saveHTML();\n")
                .append("    contentText = ").append(richTextAttrName).append(".get('textarea').value;\n")
                .append("    document.getElementById(\"").append(ele_id).append("\").value = contentText;\n")
                .append("}");

        element.append("</script>");

        return element;
    }

    private StringBuilder appendEmptyScript(StringBuilder element, String widget, String name, String partialName) {
            //Create a empty JS function to avoid errors in rich text false state;
            String eleName = "id_" + partialName;
            String fun_name = "set_" + eleName;
            element.append("<script>\n");
            element.append("function ").append(fun_name).append("(){}");
            element.append("</script>");
            return element;
    }


}
