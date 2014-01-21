package org.wso2.carbon.governance.generic.ui.common.dataobjects;

import org.apache.commons.lang.StringEscapeUtils;

public class DropDown extends UIComponent {

    private String[] values;
    private String value;

    public DropDown(String label, boolean markReadonly, String name,String id, String mandatory, String[] values,
                                String widget, String value, String tooltip,boolean isJSGenerating) {
        super(label, name, id, mandatory, widget, markReadonly, tooltip, isJSGenerating);
        this.values = values;
        this.value = value;
    }

    @Override
    public String generate() {
        String id;
        if (this.id == null) {
            id = "id_" + widget.replaceAll(" ", "") + "_" + name.replaceAll(" ", "");
        } else {
            id = this.id;
        }

        StringBuilder dropDown = new StringBuilder();
        if ("true".equals(mandatory)) {
            dropDown.append((label != null ? "<tr><td class=\"leftCol-big\">"
                    + label + "<span class=\"required\">*</span></td>" : "")).append("<td><select id=\"").append(id)
                    .append("\" name=\"").append(widget.replaceAll(" ", "")).append("_").append(name.replaceAll(" ", ""))
                    .append("\" title=\"").append(tooltip).append("\"").append((isReadOnly ? " disabled" : "")).append("\">");
        } else {
            dropDown.append((label != null ? "<tr><td class=\"leftCol-big\">" + label + "</td>" : ""))
                    .append("<td><select id=\"").append(id).append("\" ").append("name=\"").append(widget.replaceAll(" ", ""))
                    .append("_").append(name.replaceAll(" ", "")).append("\" title=\"").append(tooltip)
                    .append("\"").append((isReadOnly ? " disabled" : "")).append(">");
        }

        for (int i = 0; i < values.length; i++) {
            dropDown.append("<option value=\"").append(StringEscapeUtils.escapeHtml(values[i])).append("\"");
            if (values[i].equals(value)) {
                dropDown.append(" selected>");
            } else {
                dropDown.append(">");
            }
            dropDown.append(StringEscapeUtils.escapeHtml(values[i]));
            dropDown.append("</option>");
        }
        dropDown.append("</select></td>");
        if (label != null) {
             dropDown.append("</tr>");
        }
        return dropDown.toString();
    }

}
