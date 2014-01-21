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

import org.wso2.carbon.governance.generic.ui.utils.GenericUIGenerator;

/**
 * This class adds a link to the UI.
 */
public class AddLink extends UIComponent {

    private String addIconPath;
    private String[] subList;
    private boolean isPath;
    private String startsWith;
    private boolean isDisplay;

    public AddLink(String label, String name, String id, String addIconPath, String widget, String[] subList,
                   boolean isPath, String startsWith, boolean isDisplay, boolean isJSGenerating) {
        super(label, name.replaceAll(" ", ""), id, null, widget.replaceAll(" ", ""), false, null, isJSGenerating);
        this.addIconPath = addIconPath;
        this.subList = subList;
        this.isPath = isPath;
        this.startsWith = startsWith;
        this.isDisplay = isDisplay;
    }

    @Override
    public String generate() {

        StringBuilder link = new StringBuilder();
        link.append("<tr><td colspan=\"3\"><a class=\"icon-link\" style=\"background-image: url(");
        link.append(addIconPath);
        link.append(");\" onclick=\"");

        //creating a JavaScript onclick method name which should be identical ex: addEndpoint_Endpoint
        link.append("add").append(name).append("_").append(widget).append("(").append((isPath ? "'path'" : "''"));
        if (startsWith != null) {
            link.append(",'").append(startsWith).append("')\">");
        } else {
            link.append(")\">");
        }

        //This is the display string for add item ex: Add EndPoint
        link.append("Add ").append(label.replaceAll(" ", "-"));
        link.append("</a></td></tr>");
        link.append("<tr><td colspan=\"3\">")
                .append("<table class=\"styledLeft\" style=\" ").append((!isDisplay ? "display:none;" : ""))
                .append("border: 1px solid rgb(204, 204, 204) ! important;\"><thead>")
                .append(GenericUIGenerator.printSubHeaders(subList))
                .append("</thead><tbody id=\"").append(name).append("Mgt\">");
        return link.toString();
    }

}
