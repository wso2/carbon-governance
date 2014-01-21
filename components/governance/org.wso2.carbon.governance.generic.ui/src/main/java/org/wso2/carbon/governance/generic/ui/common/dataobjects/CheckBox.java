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

/**
 * This call adds a checkbox to the UI.
 */
public class CheckBox extends UIComponent {

    private String value;
    private boolean isSkipName;

    public CheckBox(String name, String id, String widget, String value, String tooltip, boolean isSkipName,
                    boolean isJSGenerating) {
        super(null, name, id, null, widget.replaceAll(" ", ""), false, tooltip, isJSGenerating);
        this.value = value;
        this.isSkipName = isSkipName;
    }

    @Override
    public String generate() {
        StringBuilder checkBoxHtml = new StringBuilder();
        checkBoxHtml.append((isSkipName ? "<tr><td class=\"leftCol-big\">" + name + "</td>\n" : ""))
                .append("<td><input type=\"checkbox\" name=\"").append(widget).append("_")
                .append(name.replaceAll(" ", "")).append("\" value=\"true\" title=\"").append(tooltip).append("\"");

        if (Boolean.toString(true).equals(value)) {
            return checkBoxHtml.append(" checked=\"checked\" /></td>").toString();
        } else {
            return checkBoxHtml.append("/></td>").toString();
        }
    }

}
