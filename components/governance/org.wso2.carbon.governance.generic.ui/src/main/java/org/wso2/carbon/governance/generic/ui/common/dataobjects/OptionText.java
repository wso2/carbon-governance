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

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringEscapeUtils;
import org.wso2.carbon.governance.generic.ui.utils.UIGeneratorConstants;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.ui.CarbonUIUtil;

public class OptionText extends UIComponent {

    private String originalName;
    private int index;
    private String[] values;
    private String option;
    private String text;
    private boolean isURL;
    private String urlTemplate;
    private boolean isPath;
    private String startsWith;
    private HttpServletRequest request;

    public OptionText(String originalName, int index,String label, String name, String id,  String[] values, String widget,
                      String option, String text, boolean isURL, String urlTemplate,
                      boolean isPath, String tooltip, String startsWith, HttpServletRequest request,boolean isJSGenerating) {
        super(label, name, id, null, widget.replaceAll(" ", ""), false, tooltip, isJSGenerating);
        this.originalName = originalName;
        this.index = index;
        this.values = values;
        this.option = option;
        this.text = text;
        this.isURL = isURL;
        this.urlTemplate = urlTemplate;
        this.isPath = isPath;
        this.startsWith = startsWith;
        this.request = request;
    }

    @Override
    public String generate() {

        if (name == null) {
            name = originalName + index;
        }
        name = name.replaceAll(" ", "");

        StringBuilder dropDown = new StringBuilder();
        dropDown.append("<tr><td class=\"leftCol\"><select name=\"").append(widget).append("_").append(name)
                .append("\" title=\"").append(tooltip).append("\">");
        String id;
        if (this.id == null) {
            id = "id_" + widget + "_" + name;
        } else {
            id = this.id;
        }
        for (int i = 0; i < values.length; i++) {
            dropDown.append("<option value=\"").append(StringEscapeUtils.escapeHtml(values[i])).append("\"");
            if (values[i].equals(option)) {
                dropDown.append(" selected");
            }
            dropDown.append(">");
            dropDown.append(StringEscapeUtils.escapeHtml(values[i]));
            dropDown.append("</option>");
        }
        dropDown.append("</select></td>");
        if (isURL && text != null) {
            String selectResource = " <input style=\"display:none\" id=\"" + id + "_button\" type=\"button\" "
                    + "class=\"button\" value=\"..\" title=\"" + CarbonUIUtil.geti18nString("select.path",
                    "org.wso2.carbon.governance.generic.ui.i18n.Resources", request.getLocale()) + "\" ";
            String selectResourceButton = "$('" + id + "_button').style.display='';";
            StringBuilder selectResourceSb = new StringBuilder(selectResource);
            if (isPath) {
                if (startsWith != null) {
                    selectResourceSb.append("onclick=\"showGovernanceResourceTreeWithCustomPath('").append(id)
                            .append("','").append(startsWith).append("');\"/>");
                } else {
                    selectResourceSb.append("onclick=\"showGovernanceResourceTree('").append(id).append("');\"/>");
                }
            }

            String browsePath = RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH;            

            String div = "<div id=\"" + id + "_link\"><a target=\"_blank\" href=\""
                    + (isPath ? "../resources/resource.jsp?region=region3&item=resource_browser_menu&path=" + browsePath : "")
                    + StringEscapeUtils.escapeHtml((urlTemplate != null ? urlTemplate.replace("@{value}", text) : text))
                    + "\">" + StringEscapeUtils.escapeHtml(text) + "</a>" +
                    "&nbsp;<a onclick=\"$('" + id + "_link').style.display='none';$('" + id +
                    "')." +
                    "style.display='';" + (isPath ? selectResourceButton : "") + "\" title=\"" + CarbonUIUtil.geti18nString("edit",
                    "org.wso2.carbon.governance.generic.ui.i18n.Resources", request.getLocale()) +
                    "\" class=\"icon-link\" style=\"background-image: url('../admin/images/edit.gif');float: none\"></a></div>";
            dropDown.append("<td>").append(div).append("<input style=\"display:none\" type=\"text\" name=\"")
                    .append(widget).append(UIGeneratorConstants.TEXT_FIELD).append("_").append(name).append("\" title=\"")
                    .append(tooltip).append("\" value=\"").append(StringEscapeUtils.escapeHtml(text)).append("\" id=\"")
                    .append(id).append("\" style=\"width:400px\"/>").append((isPath ? selectResourceSb.toString() : "")).append("</td>");
        } else {
            String selectResource = " <input type=\"button\" class=\"button\" value=\"..\" title=\""
                    + CarbonUIUtil.geti18nString("select.path", "org.wso2.carbon.governance.generic.ui.i18n.Resources",
                    request.getLocale()) + "\" onclick=\"showGovernanceResourceTree('" + id + "');\"/>";

            dropDown.append("<td width=500px><input type=\"text\" name=\"").append(widget)
                    .append(UIGeneratorConstants.TEXT_FIELD).append("_").append(name).append("\"  title=\"").append(tooltip)
                    .append("\" ").append((text != null ? " value=\"" + StringEscapeUtils.escapeHtml(text) : ""))
                    .append("\" id=\"").append(id).append("\" style=\"width:400px\"/>").append((isPath ? selectResource : ""))
                    .append("</td>");
        }
        if (originalName != null && widget != null) {
            dropDown.append("<td><a class=\"icon-link\" title=\"delete\" onclick=\"").append("delete")
                    .append(originalName.replaceAll(" ", "")).append("_").append(widget)
                    .append("(this.parentNode.parentNode.rowIndex)\" ")
                    .append("style=\"background-image:url(../admin/images/delete.gif);\">Delete</a></td>");
        }
        dropDown.append("</tr>");
        return dropDown.toString();
    }

}
