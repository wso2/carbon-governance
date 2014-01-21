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

import org.wso2.carbon.governance.generic.ui.utils.UIGeneratorConstants;

public class CloseAddLink extends UIComponent {

    private int count;

    public CloseAddLink(String name, int count, boolean isJSGenerating) {
        super(null, name.replaceAll(" ", ""), null, null, null, false, null, isJSGenerating);
        this.count = count;
    }

    @Override
    public String generate() {
        StringBuilder link = new StringBuilder();
        link.append("</tbody></table>")
                .append("<input id=\"").append(name).append("CountTaker\" type=\"hidden\" value=\"")
                .append(count).append("\" name=\"").append(name).append(UIGeneratorConstants.COUNT).append("\"/>\n")
                .append("</td></tr>");
        return link.toString();
    }

}
