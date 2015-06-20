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

public class TextField extends UIComponent {

    private String value;
    private boolean isURL;
    private String urlTemplate;
    private String startsWith;
    private boolean hasValue;
    protected boolean isPath;
    private HttpServletRequest request;
    private String customAtt;

    public TextField(String label, String name,String id, String mandatory, String widget,
                           String value, boolean isURL, String urlTemplate, boolean isPath,
                           boolean isReadOnly, boolean hasValue, String tooltip, String startsWith,
                           HttpServletRequest request,boolean isJSGenerating){
        super(label, name,id, mandatory, widget,isReadOnly,tooltip,isJSGenerating);
        this.value = value;
        this.isURL = isURL;
        this.urlTemplate = urlTemplate;
        this.hasValue = hasValue;
        this.startsWith = startsWith;
        this.isPath = isPath;
        this.request = request;
    }

    @Override
    public String generate() {

        StringBuilder element = new StringBuilder();

        String id;
        if (this.id == null) {
            id = "id_" + widget.replaceAll(" ", "") + "_" + name.replaceAll(" ", "");
        } else {
            id = this.id;
        }

        customAtt = widget.replaceAll(" ", "").replace("_","") + "_" + name.replaceAll(" ", "");

        String selectResource = " <input id=\"" + id + "_button\" type=\"button\" class=\"button\" value=\"..\" title=\""
                + CarbonUIUtil.geti18nString("select.path", "org.wso2.carbon.governance.generic.ui.i18n.Resources",
                request.getLocale()) + "\" ";
        StringBuilder selectResourceSb = new StringBuilder(selectResource);
        String selectResourceButton = "$('" + id + "_button').style.display='';";
        if (value != null) {
            value = StringEscapeUtils.escapeHtml(value);
        }
        
        if (isPath) {
            if (startsWith != null) {
                selectResourceSb.append((isJSGenerating ? "  onclick=\"showGovernanceResourceTreeWithCustomPath(\\'"
                        + id + "\\',\\'" + startsWith + "\\')" : "  onclick=\"showGovernanceResourceTreeWithCustomPath('"
                        + id + "','" + startsWith + "')")).append(";\"/>");
            } else {
                selectResourceSb.append((isJSGenerating ? "onclick=\"showGovernanceResourceTree(\\'" + id + "\\')"
                        : "onclick=\"showGovernanceResourceTree(\'" + id + "\')")).append(";\"/>");
            }
        }

        String browsePath = RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH;

        String div = null;
        if (isURL && value != null) {
            if (isJSGenerating) {
                div = "<div id=\"" + id + "_link\"><a target=\"_blank\" href=\"" + (isPath ? "../resources/resource.jsp"
                        + "?region=region3&item=resource_browser_menu&path=" + browsePath : "") + (urlTemplate != null
                        ? urlTemplate.replace("@{value}", value) : value) + "\">" + value + "</a>&nbsp;" + (!isReadOnly
                        ? "<a onclick=\"$(\\'" + id + "_link\\').style.display=\\'none\\';$(\\'" + id + "\\')."
                        + "style.display=\\'\\';" + (isPath ? selectResourceButton : "") + "\" title=\""
                        + CarbonUIUtil.geti18nString("edit", "org.wso2.carbon.governance.generic.ui.i18n.Resources",
                        request.getLocale()) + "\" class=\"icon-link\" style=\"background-image: "
                        + "url(\\'../admin/images/edit.gif\\');float: none\"></a>" : "") + "</div>";
            } else {
                div = "<div id=\"" + id + "_link\"><a target=\"_blank\" href=\"" + (isPath ? "../resources/resource.jsp?"
                        + "region=region3&item=resource_browser_menu&path=" + browsePath : "") + (urlTemplate != null
                        ? urlTemplate.replace("@{value}", value) : value) + "\">" + value + "</a>" +
                        "&nbsp;" + (!isReadOnly ? "<a onclick=\"$('" + id + "_link').style.display='none';$('" + id
                        + "').style.display='';" + (isPath ? selectResourceButton : "") + "\" title=\""
                        + CarbonUIUtil.geti18nString("edit", "org.wso2.carbon.governance.generic.ui.i18n.Resources",
                        request.getLocale()) + "\" class=\"icon-link\" style=\"background-image: url('../admin/images/edit.gif')"
                        + ";float: none\"></a>" : "") + "</div>";
            }
        }
       
        //+ (hasValue ? "value=\"" + value + "\"" : "") +
        if ("true".equals(mandatory)) {
            if (label != null) {
                element.append("<tr><td class=\"leftCol-big\">").append(label)
                        .append("<span class=\"required\">*</span></td>\n");
            }
            element.append(" <td>").append((isURL && div != null ? div : "")).append("<input")
                    .append(" customatt=\"").append(customAtt.replaceAll(" ", "")).append("\"")
                    .append((isURL && hasValue && value != null ? " style=\"display:none\"" : ""))
                    .append(" type=\"text\" name=\"").append(widget.replaceAll(" ", "")).append("_")
                    .append(name.replaceAll(" ", "")).append("\" title=\"").append(tooltip).append("\" ")
                    .append((hasValue && value != null ?  "value=\"" + value + "\"" : "")).append(" id=\"").append(id)
                    .append("\" style=\"width:200px\"").append((isReadOnly ? " readonly" : "")).append("/>")
                    .append((isPath ? selectResourceSb.toString() : "")).append("</td>");
            if (label != null) {
                element.append("</tr>");
            }
        } else {
            if (label != null) {
                element.append("<tr><td class=\"leftCol-big\">" + label + "</td>\n");
            }
            element.append(" <td>").append((isURL && div != null ? div : "")).append("<input")
                    .append(" customatt=\"").append(customAtt.replaceAll(" ", "")).append("\"")
                    .append((isURL && hasValue && value != null ? " style=\"display:none\"" : ""))
                    .append(" type=\"text\" name=\"").append(widget.replaceAll(" ", ""))
                    .append("_" + name.replaceAll(" ", "")).append("\"  title=\"").append(tooltip).append("\" ")
                    .append((hasValue && value != null ? "value=\"" + value + "\"" : "")).append(" id=\"").append(id)
                    .append("\" style=\"width:200px\"").append((isReadOnly ? " readonly" : "")).append("/>")
                    .append((isPath ? selectResourceSb.toString() : ""));
            element.append("</td>");
            if (label != null) {
                element.append("</tr>");
            }
        }
        return element.toString();
    }

}
