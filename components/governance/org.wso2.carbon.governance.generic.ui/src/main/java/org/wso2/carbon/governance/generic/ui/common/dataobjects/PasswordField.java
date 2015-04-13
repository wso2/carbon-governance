package org.wso2.carbon.governance.generic.ui.common.dataobjects;

/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * This class enables a password field for rxts.
 * It generates the html content of the password field
 * which hides actual data with '*'s.
 */
public class PasswordField extends UIComponent {

	private String value;
	private boolean hasValue;

	/**
	 * Constructor
	 *
	 * @param label          label
	 * @param name           password field name
	 * @param id             identifier
	 * @param mandatory      is required or not
	 * @param widget         tag name
	 * @param value          field content
	 * @param hasValue       has content
	 * @param tooltip        title
	 * @param isJSGenerating is JS generating or not
	 */
	public PasswordField(String label, String name, String id, String mandatory, String widget, String value,
	                     boolean hasValue, String tooltip, boolean isJSGenerating) {
		super(label, name, id, mandatory, widget, false, tooltip, isJSGenerating);
		this.value = value;
		this.hasValue = hasValue;

	}

	/**
	 * This method generates the html content for for the password field.
	 *
	 * @return generated html content
	 */
	@Override public String generate() {
		StringBuilder element = new StringBuilder();
		String id;
		if (this.id == null) {
			id = "id_" + widget.replaceAll(" ", "") + "_" + name.replaceAll(" ", "");
		} else {
			id = this.id;
		}
		if ("true".equals(mandatory)) {
			if (label != null) {
				element.append("<tr><td class=\"leftCol-big\">").append(label)
				       .append("<span class=\"required\">*</span></td>\n");
			}
			element.append(" <td>").append("<input").append(" type=\"password\" name=\"")
			       .append(widget.replaceAll(" ", "")).append("_").append(name.replaceAll(" ", ""))
			       .append("\" title=\"").append(tooltip).append("\" ")
			       .append((hasValue && value != null ? "value=\"" + value + "\"" : "")).append(" id=\"").append(id)
			       .append("\" style=\"width:200px\"").append("/>").append("</td>");
			if (label != null) {
				element.append("</tr>");
			}
		} else {
			if (label != null) {
				element.append("<tr><td class=\"leftCol-big\">" + label + "</td>\n");
			}
			element.append(" <td>").append("<input").append(" type=\"password\" name=\"")
			       .append(widget.replaceAll(" ", "")).append("_" + name.replaceAll(" ", "")).append("\"  title=\"")
			       .append(tooltip).append("\" ").append((hasValue && value != null ? "value=\"" + value + "\"" : ""))
			       .append(" id=\"").append(id).append("\" style=\"width:200px\"").append("/>").append("</td>");
			if (label != null) {
				element.append("</tr>");
			}
		}
		return element.toString();
	}
}