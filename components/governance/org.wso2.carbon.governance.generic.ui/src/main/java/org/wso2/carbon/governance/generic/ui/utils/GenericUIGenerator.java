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
package org.wso2.carbon.governance.generic.ui.utils;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.api.util.GovernanceConstants;
import org.wso2.carbon.governance.generic.ui.common.dataobjects.AddLink;
import org.wso2.carbon.governance.generic.ui.common.dataobjects.CheckBox;
import org.wso2.carbon.governance.generic.ui.common.dataobjects.CloseAddLink;
import org.wso2.carbon.governance.generic.ui.common.dataobjects.DateField;
import org.wso2.carbon.governance.generic.ui.common.dataobjects.DropDown;
import org.wso2.carbon.governance.generic.ui.common.dataobjects.OptionText;
import org.wso2.carbon.governance.generic.ui.common.dataobjects.TextArea;
import org.wso2.carbon.governance.generic.ui.common.dataobjects.TextField;
import org.wso2.carbon.governance.generic.ui.common.dataobjects.UIComponent;
import org.wso2.carbon.ui.CarbonUIUtil;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;
import java.util.*;

/* This is the class which generate the service UI by reading service-config.xml */
public class GenericUIGenerator {

    private static final Log log = LogFactory.getLog(GenericUIGenerator.class);

    private String dataElement;
    private String dataNamespace;

    public GenericUIGenerator() {
        this(UIGeneratorConstants.DATA_ELEMENT, UIGeneratorConstants.DATA_NAMESPACE);
    }

    public GenericUIGenerator(String dataElement, String dataNamespace) {
        this.dataElement = dataElement;
        this.dataNamespace = dataNamespace;
    }

    //StringBuffer serviceUI;

    public OMElement getUIConfiguration(String content, HttpServletRequest request,
                                        ServletConfig config, HttpSession session) throws Exception {
        OMElement omElement = null;
        try {
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(content));
            StAXOMBuilder builder = new StAXOMBuilder(reader);
            omElement = builder.getDocumentElement();
        } catch (XMLStreamException e) {
            log.error("Unable to parse the UI configuration.", e);
        }
        return omElement;
    }


    public String printWidgetWithValues(OMElement widget, OMElement data,
                                        boolean isFilterOperation, HttpServletRequest request,
                                        ServletConfig config) {
        return printWidgetWithValues(widget, data, isFilterOperation, true, true, request, config);
    }

    public String printWidgetWithValues(OMElement widget, OMElement data,
                                        boolean isFilterOperation, boolean markReadonly, boolean hasValue, HttpServletRequest request,
                                        ServletConfig config) {
        if (isFilterOperation && Boolean.toString(false).equals(
                widget.getAttributeValue(new QName(null, UIGeneratorConstants.FILTER_ATTRIBUTE)))) {
            return "";
        }
        int columns = 2; //default value of number of columns is 2
        String widgetName = widget.getAttributeValue(new QName(null, UIGeneratorConstants.ARGUMENT_NAME));
        boolean collapsed = true;  // Default collapse set to true
        String widgetCollapse = widget.getAttributeValue(new QName(null, UIGeneratorConstants.WIDGET_COLLAPSED));
        if (widgetCollapse != null) {
            collapsed = Boolean.valueOf(widgetCollapse);
        }

        String divId = "_collapse_id_" + widgetName.replaceAll(" ", "");

        OMElement dataHead = null;
        if (data != null) {
            dataHead = GenericUtil.getChildWithName(data, widgetName, dataNamespace);
        }
        if (widget.getAttributeValue(new QName(null, UIGeneratorConstants.WIDGET_COLUMN)) != null) {
            columns = Integer.parseInt(widget.getAttributeValue(new QName(null, UIGeneratorConstants.WIDGET_COLUMN)));
        }
        Iterator subHeadingIt = widget.getChildrenWithName(new QName(null, UIGeneratorConstants.SUBHEADING_ELEMENT));
        StringBuilder table = new StringBuilder();
        table.append("<div id=\"" + divId + "\"  " + "onmouseover='title=\"\"' onmouseout='title=\"" + String.valueOf(collapsed)
                + "\"'" + " title=\"" + String.valueOf(collapsed) + "\"><table class=\"normal-nopadding\" cellspacing=\"0\">");
        List<String> subList = new ArrayList<String>();
        OMElement sub = null;
        if (subHeadingIt != null && subHeadingIt.hasNext()) {
            sub = (OMElement) subHeadingIt.next(); // NO need to have multiple subheading elements in a single widget element
        }
        if (sub != null && UIGeneratorConstants.SUBHEADING_ELEMENT.equals(sub.getLocalName())) {
            Iterator headingList = sub.getChildrenWithLocalName(UIGeneratorConstants.HEADING_ELEMENT);
            while (headingList.hasNext()) {
                OMElement subheading = (OMElement) headingList.next();
                subList.add(subheading.getText());
            }
            if (subList.size() > columns) {
                /*This is the place where special scenario comes in to play with number of columns other
              than having two columns
                */
                return ""; // TODO: throw an exception
            }
        }
        table.append(printMainHeader(widgetName, columns));

        String widgetMaxOccurs = widget.getAttributeValue(new QName(null, UIGeneratorConstants.MAXOCCUR_ELEMENT));
        //skip print heading for unbounded area

        if (subList.size() > 2 && widgetMaxOccurs == null) {
            //if the column size is not 2 we print sub-headers first before going in to loop
            //In this table there should not be any field with maxOccurs unbounded//
            table.append(printSubHeaders(subList.toArray(new String[subList.size()])));
        }
        Iterator arguments = widget.getChildrenWithLocalName(UIGeneratorConstants.ARGUMENT_ELMENT);
        int columnCount = 0;
        int rowCount = 0;
        OMElement inner = null;

        if (widgetMaxOccurs != null) {
            if (UIGeneratorConstants.MAXOCCUR_UNBOUNDED.equals(widgetMaxOccurs)) {
                processElements(widget, data, request, config, widgetName, table, subList);
            }
        } else {
	        while (arguments.hasNext()) {
	            OMElement arg = (OMElement) arguments.next();
	            String maxOccurs = "";
	            if (UIGeneratorConstants.ARGUMENT_ELMENT.equals(arg.getLocalName())) {
	                if (isFilterOperation && Boolean.toString(false).equals(
	                        arg.getAttributeValue(new QName(null, UIGeneratorConstants.FILTER_ATTRIBUTE)))) {
	                    continue;
	                }
	                rowCount++; //this variable used to find the which raw is in and use this to print the sub header
	                String elementType = arg.getAttributeValue(new QName(null, UIGeneratorConstants.TYPE_ATTRIBUTE));
	                String tooltip = arg.getAttributeValue(new QName(null,
	                        UIGeneratorConstants.TOOLTIP_ATTRIBUTE));
	                if (tooltip == null) {
	                    tooltip = "";
	                }
	                tooltip = StringEscapeUtils.escapeHtml(tooltip);
	                //Read the maxOccurs value
	                maxOccurs = arg.getAttributeValue(new QName(null, UIGeneratorConstants.MAXOCCUR_ELEMENT));
	                if (maxOccurs != null) {
	                    if (!UIGeneratorConstants.MAXOCCUR_BOUNDED.equals(maxOccurs) && !UIGeneratorConstants.MAXOCCUR_UNBOUNDED.equals(maxOccurs)) {
	                        //if user has given something else other than unbounded
	                        return ""; //TODO: throw an exception
	                    }
	                    if (!UIGeneratorConstants.MAXOCCUR_UNBOUNDED.equals(maxOccurs)) {
	                        //if maxOccurs is not unbounded then print the sub header otherwise we will show the adding link
	                        if (rowCount == 1) {
	                            // We print the sub header only when we parse the first element otherwise we'll print sub header for each field element
	                            table.append(printSubHeaders(subList.toArray(new String[subList.size()])));
	                        }
	                    }
	                } else {
						if (subList.size() == 2 && rowCount == 1) {
	                        // We print the sub header only when we parse the first element otherwise we'll print sub header for each field element
	                        // sub headers are printed in this position only if column number is exactly 2//
	                        table.append(printSubHeaders(subList.toArray(new String[subList.size()])));
	                    }
	                }
                    if (dataHead != null) {
                        //if the data xml contains the main element then get the element contains value
                        inner = GenericUtil.getChildWithName(dataHead, arg.getFirstChildWithName(
                                new QName(null, UIGeneratorConstants.ARGUMENT_NAME)).getText().replaceAll(" ", "-"),
                                dataNamespace);
                    }
                    if (UIGeneratorConstants.TEXT_FIELD.equals(elementType)) {
                        columnCount = handleTextField(isFilterOperation, markReadonly, hasValue, request, columns,
                                widgetName, table, columnCount, inner, arg, tooltip);
                    } else if (UIGeneratorConstants.DATE_FIELD.equals(elementType)) {
                        columnCount = handleDateField(isFilterOperation, markReadonly, columns, widgetName, table,
                                columnCount, inner, arg, tooltip);
                    } else if (UIGeneratorConstants.OPTION_FIELD.equals(elementType)) {
                        columnCount = handleOptionField(isFilterOperation, markReadonly, request, config, columns, widgetName, table,
                                columnCount, inner, arg, tooltip);
                    } else if (UIGeneratorConstants.CHECKBOX_FIELD.equals(elementType)) {
                        columnCount = handleCheckBox(columns, widgetName, table, columnCount, inner, arg, tooltip);
                    } else if (UIGeneratorConstants.TEXT_AREA_FIELD.equals(elementType)) {
                        columnCount = handleTextArea(isFilterOperation, markReadonly, columns, widgetName, table,
                                columnCount, inner, arg, tooltip);
                    } else if (UIGeneratorConstants.OPTION_TEXT_FIELD.equals(elementType)) {
                        inner = handleOptionTextField(request, config, widgetName, dataHead, table, subList, inner, arg,
                                maxOccurs, tooltip);
                    }
                }
            }
        }
        table.append("</table></div>");
        return table.toString();
    }

    private void processElements(OMElement widget, OMElement data, HttpServletRequest request, ServletConfig config,
                                 String widgetName, StringBuilder table, List<String> subList) {
        List<OMElement> dataElements =  new ArrayList<OMElement>();
        if (data != null) {
            dataElements = GenericUtil.getChildsWithName(data, widgetName, dataNamespace);
        }

        // Here generating map with RXT table name --> Field names --> Values
        List<Map<String, String>> addedValues = new ArrayList<Map<String, String>>();
        int addedItemsCount = addRxtProperties(dataElements, addedValues);

        boolean isDisplay = false;
        if (addedItemsCount != 0) {
            isDisplay = true;
        }

        // Generating headers if user not specify the headers
        if (subList.size() == 0) {
            addHeaders(widget, subList);
        }

        UIComponent addLink =  new AddLink(widgetName, widgetName, null, UIGeneratorConstants.ADD_ICON_PATH,
                    widgetName, subList.toArray(new String[subList.size() + 1]), false, null, isDisplay, false);
        table.append(addLink.generate());

        for (int i = 0; i < addedItemsCount; i++) {
            Iterator argument = widget.getChildrenWithLocalName(UIGeneratorConstants.ARGUMENT_ELMENT);
            int a = 0;
            table.append("<tr>");

            while (argument.hasNext()) {
                OMElement arg = (OMElement) argument.next();
                String elementType = arg.getAttributeValue(new QName(null, UIGeneratorConstants.TYPE_ATTRIBUTE));

                if (UIGeneratorConstants.ARGUMENT_ELMENT.equals(arg.getLocalName())) {
                    String name = arg.getFirstChildWithName(new QName(null, UIGeneratorConstants.ARGUMENT_NAME)).getText();
                    String label = null;
                    if (subList.size() == 0) {
                        label = arg.getAttributeValue(new QName(UIGeneratorConstants.ARGUMENT_LABEL));
                        if (label == null) {
                            label = name;
                        }
                    }

                    String value = arg.getAttributeValue(new QName(null, UIGeneratorConstants.DEFAULT_ATTRIBUTE));
                    if (value == null) {
                        String key = GenericUtil.getDataElementName(name);
                        value = addedValues.get(i).get(key);
                    }

                    String elementId = "id_" + widgetName.replaceAll(" ", "") + "_" + name.replaceAll(" ", "") + "_" + (i + 1);
                    name = name.replaceAll(" ", "") + "_" + (i + 1);

                    boolean isReadOnly = false;

                    if ("true".equals(arg.getAttributeValue(new QName(null, UIGeneratorConstants.READONLY_ATTRIBUTE)))) {
                        isReadOnly = true;
                    }
                    String tooltip = arg.getAttributeValue(new QName(null, UIGeneratorConstants.TOOLTIP_ATTRIBUTE));
                    if (tooltip == null) {
                        tooltip = "";
                    }
                    tooltip = StringEscapeUtils.escapeHtml(tooltip);

                    if (UIGeneratorConstants.DATE_FIELD.equals(elementType)) {
                        UIComponent dateField = new DateField(label, name, elementId, null, widgetName,
                                value, isReadOnly, tooltip, false, false);
                        table.append(dateField.generate());
                    } else if (UIGeneratorConstants.CHECKBOX_FIELD.equals(elementType)) {
                        UIComponent checkBox  = new CheckBox(name, elementId, widgetName, value, tooltip, false, false);
                        table.append(checkBox.generate());

                    } else if (UIGeneratorConstants.OPTION_FIELD.equals(elementType)) {
                        List<String> optionValues = getOptionValues(arg, request, config);
                        DropDown dropDown = new DropDown(label, isReadOnly, name, elementId, null, optionValues.toArray(
                                new String[optionValues.size()]), widgetName, value, tooltip, false);
                        table.append(dropDown.generate());
                    } else if (UIGeneratorConstants.TEXT_AREA_FIELD.equals(elementType)) {
                        int height = -1;
                        int width = 200;
                        String heightString = arg.getAttributeValue(new QName(null, UIGeneratorConstants.HEIGHT_ATTRIBUTE));
                        if (heightString != null) {
                            try {
                                height = Integer.parseInt(heightString);
                            } catch (NumberFormatException ignored) {
                            }
                        }
                        String widthString = arg.getAttributeValue(new QName(null, UIGeneratorConstants.WIDTH_ATTRIBUTE));
                        if (widthString != null) {
                            try {
                                width = Integer.parseInt(widthString);
                            } catch (NumberFormatException ignored) {
                            }
                        }
                        UIComponent textArea  = new TextArea(label, name, elementId, null, widgetName, value, height, width,
                                isReadOnly, false, tooltip, false, false);
                        table.append(textArea.generate());
                    } else {
                        boolean isURL = Boolean.toString(true).equals(arg.getAttributeValue(new QName(null,
                                                        UIGeneratorConstants.URL_ATTRIBUTE)));
                        String urlTemplate = arg.getAttributeValue(
                        new QName(null, UIGeneratorConstants.URL_TEMPLATE_ATTRIBUTE));
                        boolean isPath = Boolean.toString(true).equals(arg.getAttributeValue(
                        new QName(null, UIGeneratorConstants.PATH_ATTRIBUTE)));
                        String startsWith = arg.getAttributeValue(new QName(null, UIGeneratorConstants.PATH_START_WITH));
                        UIComponent textField  = new TextField(label, name, elementId, null, widgetName, value, isURL, urlTemplate,
                                isPath, isReadOnly, true, tooltip, startsWith, request, false);
                        table.append(textField.generate());
                    }
                }
                a++;
            }
            table.append(printDeleteWidget(widgetName));
            table.append("</tr>");
        }
        table.append(printCloseAddLink(widgetName, addedItemsCount));
    }

    private OMElement handleOptionTextField(HttpServletRequest request, ServletConfig config, String widgetName,
                                             OMElement dataHead, StringBuilder table, List<String> subList, OMElement inner,
                                             OMElement arg, String maxOccurs, String tooltip) {
        String optionValue = null;
        if (UIGeneratorConstants.MAXOCCUR_UNBOUNDED.equals(maxOccurs)) {
            // This is the code segment to run in maxoccur unbounded situation
//                        String addedItems = "0";
//                        if(dataHead != null){
//                            addedItems = dataHead.getFirstChildWithName(new QName(null,UIGeneratorConstants.COUNT)).getText();
//                        }
            OMElement firstChildWithName = arg.getFirstChildWithName(new QName(null, UIGeneratorConstants.ARGUMENT_NAME));
            String name = firstChildWithName.getText();
            String label = firstChildWithName.getAttributeValue(new QName(UIGeneratorConstants.ARGUMENT_LABEL));

            if (label == null) {
                label = name;
            }
            boolean isURL = Boolean.toString(true).equals(arg.getAttributeValue(
                    new QName(null, UIGeneratorConstants.URL_ATTRIBUTE)));
            String urlTemplate = arg.getAttributeValue(new QName(null, UIGeneratorConstants.URL_TEMPLATE_ATTRIBUTE));
            boolean isPath = Boolean.toString(true).equals(arg.getAttributeValue(
                    new QName(null, UIGeneratorConstants.PATH_ATTRIBUTE)));

            String startsWith = arg.getAttributeValue(new QName(null, UIGeneratorConstants.PATH_START_WITH));

            // String addedOptionValues [] = new String[Integer.parseInt(addedItems)];
            // String addedValues[] = new String[Integer.parseInt(addedItems)];
            List<String> addedOptionValues = new ArrayList<String>();
            List<String> addedValues = new ArrayList<String>();
            int addedItemsCount = 0;
            if (dataHead != null) {
                //if the element contains value is not null get the value
                // with option-text field we put text value like this text_value.replaceAll(" ","-")
                addedItemsCount = fillOptionValues(dataHead, addedOptionValues, addedValues);
            }
            /* if there are no added items headings of the table will hide,else display */
            boolean isDisplay = false;

            if (addedItemsCount == 0) {
                isDisplay = false;
            } else if (addedItemsCount > 0) {
                isDisplay = true;
            }
            UIComponent addLink =  new AddLink(label, name, null, UIGeneratorConstants.ADD_ICON_PATH,
                    widgetName, subList.toArray(new String[subList.size() + 1]), isPath, startsWith, isDisplay, false);

            table.append(addLink.generate());
            List<String> optionValues = getOptionValues(arg, request, config);
            if (addedItemsCount > 0) {
                // This is the place where we fill already added entries
                for (int i = 0; i < addedItemsCount; i++) {
                    String addedOptionValue = addedOptionValues.get(i);
                    String addedValue = addedValues.get(i);
                    if (addedOptionValue != null && addedValue != null) {
                        UIComponent optionText = new OptionText(name, (i + 1), null, null, null,
                                                               optionValues.toArray(new String[optionValues.size()]),
                                                               widgetName, addedOptionValue, addedValue,
                                                               isURL, urlTemplate, isPath, tooltip, startsWith, request, false);
                        table.append(optionText.generate());
                    }
                }
            }
            UIComponent closeAddLink = new CloseAddLink(name, addedItemsCount, false);
            table.append(closeAddLink.generate()); // add the previously added items and then close the tbody
        } else {
            OMElement firstChildWithName = arg.getFirstChildWithName(
                    new QName(null, UIGeneratorConstants.ARGUMENT_NAME));
            String name = firstChildWithName.getText();
            String label = firstChildWithName.getAttributeValue(
                    new QName(UIGeneratorConstants.ARGUMENT_LABEL));
            String value = null;

            String startsWith = arg.getAttributeValue(new QName(null, UIGeneratorConstants.PATH_START_WITH));

            if (label == null) {
                label = name;
            }

            boolean isURL = Boolean.toString(true).equals(arg.getAttributeValue(
                    new QName(null, UIGeneratorConstants.URL_ATTRIBUTE)));
            String urlTemplate = arg.getAttributeValue(
                    new QName(null, UIGeneratorConstants.URL_TEMPLATE_ATTRIBUTE));
            boolean isPath = Boolean.toString(true).equals(arg.getAttributeValue(
                    new QName(null, UIGeneratorConstants.PATH_ATTRIBUTE)));
            if (dataHead != null) {
                //if the element contains value is not null get the value
                // with option-text field we put text value like this text_value.replaceAll(" ","-")

                inner = GenericUtil.getChildWithName(dataHead, UIGeneratorConstants.TEXT_FIELD +
                        arg.getFirstChildWithName(new QName(null, UIGeneratorConstants.ARGUMENT_NAME)).getText(),
                        dataNamespace);
                if (inner != null) {
                    value = inner.getText();
                }
                OMElement optionValueElement = GenericUtil.getChildWithName(dataHead, arg.getFirstChildWithName(
                        new QName(null, UIGeneratorConstants.ARGUMENT_NAME)).getText(), dataNamespace);
                if (optionValueElement != null) {
                    optionValue = optionValueElement.getText();
                }

            }
            List<String> optionValues = getOptionValues(arg, request, config);
            UIComponent optionText = new OptionText(null, 0, label, name, null, optionValues.toArray(
                    new String[optionValues.size()]), widgetName, optionValue, value, isURL, urlTemplate, isPath,
                    tooltip, startsWith, request, false);
            table.append(optionText.generate());
        }
        return inner;
    }

    private int handleOptionField(boolean isFilterOperation, boolean markReadonly, HttpServletRequest request,
                                  ServletConfig config, int columns, String widgetName, StringBuilder table,
                                  int columnCount, OMElement inner, OMElement arg, String tooltip) {
        OMElement firstChildWithName = arg.getFirstChildWithName(
                new QName(null, UIGeneratorConstants.ARGUMENT_NAME));
        String mandat = arg.getAttributeValue(new QName(null, UIGeneratorConstants.MANDETORY_ATTRIBUTE));
        String name = firstChildWithName.getText();
        String label = firstChildWithName.getAttributeValue(
                new QName(UIGeneratorConstants.ARGUMENT_LABEL));
        String optionValue = null;
        boolean isReadOnly = false;

        if (markReadonly && "true".equals(arg.getAttributeValue(new QName(null, UIGeneratorConstants.READONLY_ATTRIBUTE)))) {
            isReadOnly = true;
        }


        if (label == null) {
            label = name;
        }

        if (inner != null) {
            //if the element contains value is not null get the value
            optionValue = inner.getText();
        }
        List<String> optionValues = getOptionValues(arg, request, config);
        if (isFilterOperation) {
            optionValues.add(0, "");
        }
        if (columns > 2) {
            if (columnCount == 0) {
                table.append("<tr>");
            }
            UIComponent dropDown = new DropDown(null, isReadOnly, name, null, null, optionValues.toArray(
                    new String[optionValues.size()]), widgetName, optionValue, tooltip, false);
            table.append(dropDown.generate());

            columnCount++;
            if (columnCount == columns) {
                table.append("</tr>");
                columnCount = 0;
            }

        } else {
            UIComponent dropDown = new DropDown(label, isReadOnly, name, null, mandat,
                        optionValues.toArray(new String[optionValues.size()]),
                        widgetName, optionValue, tooltip, false);
            table.append(dropDown.generate());
        }
        return columnCount;
    }

    private int handleDateField(boolean isFilterOperation, boolean markReadonly, int columns, String widgetName, StringBuilder table,
                                int columnCount, OMElement inner, OMElement arg, String tooltip) {
        String mandet = arg.getAttributeValue(new QName(null, UIGeneratorConstants.MANDETORY_ATTRIBUTE));
        boolean isReadOnly = false;
        String value = null;

        if (markReadonly && "true".equals(arg.getAttributeValue(new QName(null, UIGeneratorConstants.READONLY_ATTRIBUTE)))) {
            isReadOnly = true;
        }
        if (isFilterOperation) {
            mandet = "false";
        }
        if (inner != null) {
            //if the element contains value is not null get the value
            value = inner.getText();
        }

        if (columns > 2) {
            if (columnCount == 0) {
                table.append("<tr>");
            }
            UIComponent dateField = new DateField(null, arg.getFirstChildWithName(new QName(null,
                    UIGeneratorConstants.ARGUMENT_NAME)).getText(), null, null, widgetName, value, isReadOnly, tooltip,
                    false, false);
            table.append(dateField.generate());

            columnCount++;
            if (columnCount == columns) {
                table.append("</tr>");
                columnCount = 0;
            }
        } else {
            OMElement firstChildWithName = arg.getFirstChildWithName(new QName(null, UIGeneratorConstants.ARGUMENT_NAME));
            String name = firstChildWithName.getText();
            String label = firstChildWithName.getAttributeValue(new QName(UIGeneratorConstants.ARGUMENT_LABEL));

            if (label == null) {
                label = name;
            }

            UIComponent dateField = new DateField(label, name, null, mandet, widgetName, value, isReadOnly, tooltip,
                    true, false);
            table.append(dateField.generate());
        }
        return columnCount;
    }

    private int handleTextField(boolean isFilterOperation, boolean markReadonly, boolean hasValue,
                                HttpServletRequest request, int columns, String widgetName, StringBuilder table,
                                int columnCount, OMElement inner, OMElement arg, String tooltip) {
        String value;
        String mandat = arg.getAttributeValue(new QName(null, UIGeneratorConstants.MANDETORY_ATTRIBUTE));

        boolean isReadOnly = false;

        if (markReadonly && "true".equals(arg.getAttributeValue(new QName(null, UIGeneratorConstants.READONLY_ATTRIBUTE)))) {
            isReadOnly = true;
        }
        if (isFilterOperation) {
            mandat = "false";
        }
        boolean isURL = Boolean.toString(true).equals(arg.getAttributeValue(new QName(null, UIGeneratorConstants.URL_ATTRIBUTE)));
        String urlTemplate = arg.getAttributeValue(new QName(null, UIGeneratorConstants.URL_TEMPLATE_ATTRIBUTE));
        boolean isPath = Boolean.toString(true).equals(arg.getAttributeValue(
                new QName(null, UIGeneratorConstants.PATH_ATTRIBUTE)));
        String startsWith = arg.getAttributeValue(new QName(null, UIGeneratorConstants.PATH_START_WITH));
        if (inner != null) {
            //if the element contains value is not null get the value
            value = inner.getText();
        } else {
            value = arg.getAttributeValue(new QName(null, UIGeneratorConstants.DEFAULT_ATTRIBUTE));
        }
        if (columns > 2) {
            if (columnCount == 0) {
                table.append("<tr>");
            }
            UIComponent textField = new TextField(null, arg.getFirstChildWithName(new QName(null,
                        UIGeneratorConstants.ARGUMENT_NAME)).getText(), null, null, widgetName,
                        value, isURL, urlTemplate, isPath, isReadOnly, hasValue, tooltip, startsWith, request, false);
            table.append(textField.generate());

            columnCount++;
            if (columnCount == columns) {
                table.append("</tr>");
                columnCount = 0;
            }

        } else {
            OMElement firstChildWithName = arg.getFirstChildWithName(new QName(null, UIGeneratorConstants.ARGUMENT_NAME));
            String name = firstChildWithName.getText();
            String label = firstChildWithName.getAttributeValue(new QName(UIGeneratorConstants.ARGUMENT_LABEL));

            if (label == null) {
                label = name;
            }
            UIComponent text =  new TextField(label, name, null, mandat, widgetName, value,
                        isURL, urlTemplate, isPath, isReadOnly, hasValue, tooltip, startsWith, request, false);
            table.append(text.generate());
        }
        return columnCount;
    }

    private int handleTextArea(boolean isFilterOperation, boolean markReadonly, int columns, String widgetName,
                               StringBuilder table, int columnCount, OMElement inner, OMElement arg, String tooltip) {
        String mandet = arg.getAttributeValue(new QName(null, UIGeneratorConstants.MANDETORY_ATTRIBUTE));
        String richText = arg.getAttributeValue(new QName(null, UIGeneratorConstants.IS_RICH_TEXT));
        String value = null;
        boolean isReadOnly = false;

        if (markReadonly && "true".equals(arg.getAttributeValue(new QName(null, UIGeneratorConstants.READONLY_ATTRIBUTE)))) {
            isReadOnly = true;
        }

        boolean isRichText = false; //By default rich text is off
        if (richText != null) {
            isRichText = Boolean.valueOf(richText);
        }

        if (isFilterOperation) {
            mandet = "false";
        }
        if (inner != null) {
            //if the element contains value is not null get the value
            value = inner.getText();
        }
        int height = -1;
        int width = -1;
        String heightString = arg.getAttributeValue(new QName(null, UIGeneratorConstants.HEIGHT_ATTRIBUTE));
        if (heightString != null) {
            try {
                height = Integer.parseInt(heightString);
            } catch (NumberFormatException ignored) {
            }
        }
        String widthString = arg.getAttributeValue(new QName(null, UIGeneratorConstants.WIDTH_ATTRIBUTE));
        if (widthString != null) {
            try {
                width = Integer.parseInt(widthString);
            } catch (NumberFormatException ignored) {
            }
        }
        if (columns > 2) {
            if (columnCount == 0) {
                table.append("<tr>");
            }
            UIComponent textArea = new TextArea(null, arg.getFirstChildWithName(new QName(null,
                    UIGeneratorConstants.ARGUMENT_NAME)).getText(), null, null, widgetName, value, height, width,
                    isReadOnly, false, tooltip, true, false);
            table.append(textArea.generate());

            columnCount++;
            if (columnCount == columns) {
                table.append("</tr>");
                columnCount = 0;
            }
        } else {
            OMElement firstChildWithName = arg.getFirstChildWithName(
                    new QName(null, UIGeneratorConstants.ARGUMENT_NAME));
            String name = firstChildWithName.getText();
            String label = firstChildWithName.getAttributeValue(
                    new QName(UIGeneratorConstants.ARGUMENT_LABEL));

            if (label == null) {
                label = name;
            }

            UIComponent textArea = new TextArea(label, name, null, mandet, widgetName, value,
                                                 height, width, isReadOnly, isRichText, tooltip, false, false);
            table.append(textArea.generate());

        }
        return columnCount;
    }

    private int fillOptionValues(OMElement dataHead, List<String> addedOptionValues, List<String> addedValues) {
        Iterator itemChildIt = dataHead.getChildElements();
        int i = 0;
        while (itemChildIt.hasNext()) {
            // get all the filled values to the newly added fields
            Object itemChildObj = itemChildIt.next();
            if (!(itemChildObj instanceof OMElement)) {
                continue;
            }
            OMElement itemChildEle = (OMElement) itemChildObj;

            if (!(itemChildEle.getQName().equals(new QName(dataNamespace,
                    UIGeneratorConstants.ENTRY_FIELD)))) {
                continue;
            }

            String entryText = itemChildEle.getText();
            String entryKey = null;
            String entryVal;
            int colonIndex = entryText.indexOf(":");
            if (colonIndex < entryText.length() - 1) {
                entryKey = entryText.substring(0, colonIndex);
                entryText = entryText.substring(colonIndex + 1);
            }
            entryVal = entryText;

            if (entryKey != null && !entryKey.equals("")) {
                addedOptionValues.add(entryKey);
            } else {
                addedOptionValues.add("0");
            }

            if (entryVal != null) {
                addedValues.add(entryVal);
            }

            i++;
        }
        return i;
    }

    private int handleCheckBox(int columns, String widgetName, StringBuilder table, int columnCount, OMElement inner,
                               OMElement arg, String tooltip) {
        String name = arg.getFirstChildWithName(new QName(null, UIGeneratorConstants.ARGUMENT_NAME)).getText();
        String optionValue = null;
        if (inner != null) {
            //if the element contains value is not null get the value
            optionValue = inner.getText();
        }
        if (columns > 1) {
            if (columnCount == 0) {
                table.append("<tr>");
            }
            UIComponent checkBox  = new CheckBox(name, null, widgetName, optionValue, tooltip, true, false);
            table.append(checkBox.generate());
            columnCount++;
            if (columnCount == columns) {
                table.append("</tr>");
                columnCount = 0;
            }

        } else {
            UIComponent checkBox  = new CheckBox(name, null, widgetName, optionValue, tooltip, true, false);
            table.append(checkBox.generate());
        }
        return columnCount;
    }

    private int addRxtProperties(List<OMElement> dataElements, List<Map<String, String>> addedValues) {
        int addedItemsCount = 0;
        for (int i = 0; i < dataElements.size(); i++) {
            OMElement dataHeadElements = dataElements.get(i);
            Iterator itemChildIt = dataHeadElements.getChildElements();
            int a = 0;
            Map<String, String> values = new HashMap<String, String>();
            while (itemChildIt.hasNext()) {
                // get all the filled values to the newly added fields
                Object itemChildObj = itemChildIt.next();
                if (!(itemChildObj instanceof OMElement)) {
                    continue;
                }
                OMElement itemChildEle = (OMElement) itemChildObj;
                values.put(itemChildEle.getLocalName(), itemChildEle.getText());
                a++;
            }
        addedValues.add(values);
        addedItemsCount++;
        }
        return addedItemsCount;
    }

    private void addHeaders(OMElement widget, List<String> subList) {
        Iterator headerElements = widget.getChildrenWithLocalName(UIGeneratorConstants.ARGUMENT_ELMENT);
        while (headerElements.hasNext()) {
             OMElement arg = (OMElement) headerElements.next();
             String name = arg.getFirstChildWithName(new QName(null, UIGeneratorConstants.ARGUMENT_NAME)).getText();
             if (UIGeneratorConstants.ARGUMENT_ELMENT.equals(arg.getLocalName())) {
                 String label = arg.getAttributeValue(new QName(UIGeneratorConstants.ARGUMENT_LABEL));
                 if (label == null) {
                     label = name;
                 }
                 subList.add(label);
             }
        }
    }

    public String printMainHeader(String header, int columns) {
        StringBuilder head = new StringBuilder();
        head.append("<thead><tr><th style=\"border-right:0\" colspan=\"" + columns + "\">");
        head.append(header);
        head.append("</th></tr></thead>");
        return head.toString();

    }

    public static String printSubHeaders(String[] headers) {
        StringBuilder subHeaders = new StringBuilder();
        subHeaders.append("<tr>");
        for (String header : headers) {
            subHeaders.append("<td class=\"sub-header\">");
            subHeaders.append((header == null) ? "" : header);
            subHeaders.append("</td>");
        }
        subHeaders.append("<td class=\"sub-header\"></td>");
        subHeaders.append("</tr>");
        return subHeaders.toString();
    }

    public String printCloseAddLink(String name, int count) {
    	name = name.replaceAll("-", "");
    	name = name.replaceAll(" ", "");
        StringBuilder link = new StringBuilder();
        link.append("</tbody></table>");
        link.append("<input id=\"" + name.replaceAll(" ", "").replaceAll("-", "") + "CountTaker\" type=\"hidden\" value=\"" +
                count + "\" name=\"");
        link.append(name.replaceAll(" ", "").replaceAll("-", "") + UIGeneratorConstants.COUNT + "\"/>\n");

        link.append("</td></tr>");
        return link.toString();
    }

    /* This is the method which extract information from the UI and embedd them to xml using value elements */
    public OMElement getDataFromUI(OMElement head, HttpServletRequest request) {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace namespace = fac.createOMNamespace(dataNamespace, "");
        OMElement data = fac.createOMElement(dataElement, namespace);
        Iterator it = head.getChildrenWithName(new QName(UIGeneratorConstants.WIDGET_ELEMENT));
        while (it.hasNext()) {
            OMElement widget = (OMElement) it.next();
            String widgetName = widget.getAttributeValue(new QName(null, UIGeneratorConstants.ARGUMENT_NAME));
            OMElement widgetData = fac.createOMElement(GenericUtil.getDataElementName(widgetName),
                    namespace);
            Iterator arguments = widget.getChildrenWithLocalName(UIGeneratorConstants.ARGUMENT_ELMENT);
            OMElement arg = null;
            

            String widgetMaxOccurs = widget.getAttributeValue(new QName(null, UIGeneratorConstants.MAXOCCUR_ELEMENT));
			if (widgetMaxOccurs != null) {
				if (UIGeneratorConstants.MAXOCCUR_UNBOUNDED.equals(widgetMaxOccurs)) {
					String count = request.getParameter(widgetName.replaceAll(" ", "") +UIGeneratorConstants.COUNT);
					for (int i = 0; i < Integer.parseInt(count); i++) {  			 
						arguments =  widget.getChildrenWithLocalName(UIGeneratorConstants.ARGUMENT_ELMENT);
            			int a = 0;
            			OMElement entryElement = fac.createOMElement(GenericUtil.getDataElementName(widgetName),namespace);
            			boolean isAllBlank  = true;
						while (arguments.hasNext()) {
							arg = (OMElement) arguments.next();
							String name = arg.getFirstChildWithName(new QName(null, UIGeneratorConstants.ARGUMENT_NAME)).getText();
							String elementType = name.replaceAll(" ", "");
							String webElement = widgetName.replaceAll(" ", "") + "_" + elementType.replaceAll(" ", "") + "_" + (i + 1);
							String input = request.getParameter(webElement);
							//String input2 = request.getParameter(widgetName + "_" + elementType + "_" + (i + 1));
							if (input == null) {
								input = "";
							} else {
								isAllBlank = false;
							}
							OMElement innerElemnt = fac.createOMElement(GenericUtil.getDataElementName(name), namespace);
							innerElemnt.setText(input);
							entryElement.addChild(innerElemnt);
							a++;
						}
						// widgetData.addChild(entryElement);
						if (!isAllBlank) {
							data.addChild(entryElement);
						}           				 
            		 } 		
            	}
            }else{                        
	            while (arguments.hasNext()) {
	                arg = (OMElement) arguments.next();
	                if (UIGeneratorConstants.ARGUMENT_ELMENT.equals(arg.getLocalName())) {
	                    String elementType = arg.getAttributeValue(new QName(null, UIGeneratorConstants.TYPE_ATTRIBUTE));
	                    String name = arg.getFirstChildWithName(new QName(null, UIGeneratorConstants.ARGUMENT_NAME)).getText();
	                    if (UIGeneratorConstants.OPTION_TEXT_FIELD.equals(elementType)) {
	                        if (UIGeneratorConstants.MAXOCCUR_UNBOUNDED.equals(
	                                arg.getAttributeValue(new QName(null, UIGeneratorConstants.MAXOCCUR_ELEMENT)))) {
	                            //implement the new way of extracting data if the maxoccurs unbounded happend in option-text field
	                            String count = request.getParameter(name.replaceAll(" ", "") + UIGeneratorConstants.COUNT);
	
								for (int i = 0; i < Integer.parseInt(count); i++) {
									String entryValue = "";
									String input = request.getParameter(widgetName.replaceAll(" ", "") + "_" + name.replaceAll(" ", "") + (i + 1));
									if (input != null && !("".equals(input))) {
										entryValue += input;
									}
									entryValue += ":";
									String inputTextValue = request.getParameter(widgetName.replaceAll(" ", "") + 
									                                             UIGeneratorConstants.TEXT_FIELD +
									                                             "_" + name.replaceAll(" ", "") + (i + 1));
									if (inputTextValue != null && !("".equals(inputTextValue))) {
										entryValue += inputTextValue;
									}
									if (!":".equals(entryValue)) {
										OMElement entryElement = fac.createOMElement(UIGeneratorConstants.ENTRY_FIELD,
										                                             namespace);
										entryElement.setText(entryValue);
										widgetData.addChild(entryElement);
									}
								}
	
	                        }
	                        // if maxoccurs unbounded is not mentioned use the default behaviour
	                        else {
	                            String input = request.getParameter(widgetName.replaceAll(" ", "") + "_" +
	                                    name.replaceAll(" ", ""));
	                            if (input != null && !("".equals(input))) {
									OMElement text = fac.createOMElement(GenericUtil.getDataElementName(name),
									                                     namespace);
	                                text.setText(input);
	                                widgetData.addChild(text);
	                            }
	                            String inputOption = request.getParameter(widgetName.replaceAll(" ", "") +
	                                    UIGeneratorConstants.TEXT_FIELD +  "_" + name.replaceAll(" ", ""));
	                            if (inputOption != null && !("".equals(inputOption))) {
	                                OMElement value = fac.createOMElement(
	                                        GenericUtil.getDataElementName(UIGeneratorConstants.TEXT_FIELD + name),
	                                        namespace);
	                                value.setText(inputOption);
	                                widgetData.addChild(value);
	                            }
	                        }
	                    } else {
	                        String input = request.getParameter(widgetName.replaceAll(" ", "") + "_" +
	                                name.replaceAll(" ", ""));
	                        OMElement text = null;
	
	                        if (input != null && !("".equals(input))) {
	                            text = fac.createOMElement(GenericUtil.getDataElementName(name), namespace);
	                            text.setText(input);
	                            widgetData.addChild(text);	
	                        } else {
	                            if (name.equals("Name") && widgetName.equalsIgnoreCase("overview")) {
	                                text = fac.createOMElement(GenericUtil.getDataElementName(name), namespace);
	                                text.setText(GovernanceConstants.DEFAULT_SERVICE_NAME);
	                                widgetData.addChild(text);
	                            }
	                            if (name.equals("Namespace")) {
	                                text = fac.createOMElement(GenericUtil.getDataElementName(name), namespace);
	                                text.setText(UIGeneratorConstants.DEFAULT_NAMESPACE);
	                                widgetData.addChild(text);
	                            }
	                        }
	
	                    }
	                }
	            }
	            data.addChild(widgetData);
            }
           
        }
        return GenericUtil.addExtraElements(data, request);
    }

    public List<Map> getTooltipList(OMElement head) {
        List<Map> res = new ArrayList<Map>();

        List<String> id = new ArrayList<String>();
        Iterator it = head.getChildrenWithName(new QName(UIGeneratorConstants.WIDGET_ELEMENT));
        while (it.hasNext()) {
            OMElement widget = (OMElement) it.next();
            String widgetName = widget.getAttributeValue(new QName(null, UIGeneratorConstants.ARGUMENT_NAME));
            Iterator arguments = widget.getChildrenWithLocalName(UIGeneratorConstants.ARGUMENT_ELMENT);
            OMElement arg = null;
            while (arguments.hasNext()) {
                arg = (OMElement) arguments.next();
                if (UIGeneratorConstants.ARGUMENT_ELMENT.equals(arg.getLocalName())) {
                    String elementType = arg.getAttributeValue(new QName(null, UIGeneratorConstants.TYPE_ATTRIBUTE));
                    String name = arg.getFirstChildWithName(new QName(null, UIGeneratorConstants.ARGUMENT_NAME)).getText();

                    //check the validation fields and get the id's of them
                    String value = arg.getAttributeValue(new QName(null,
                            UIGeneratorConstants.TOOLTIP_ATTRIBUTE));
                    if (value != null && !"".equals(value)) {
                        if (UIGeneratorConstants.OPTION_TEXT_FIELD.equals(elementType)) {
                            if (UIGeneratorConstants.MAXOCCUR_UNBOUNDED.equals(
                                    arg.getAttributeValue(new QName(null, UIGeneratorConstants.MAXOCCUR_ELEMENT)))) {
                                Map<String, Object> map = new HashMap<String, Object>();
                                List ids = new ArrayList<String>();
                                ids.add(widgetName.replaceAll(" ",
                                        "_") + "_" + name.replaceAll("" + " ", "-"));
                                ids.add(widgetName.replaceAll(" ", "_") + UIGeneratorConstants
                                        .TEXT_FIELD + "_" + name.replaceAll("" + " ", "-"));
                                map.put("ids", ids);
                                map.put("tooltip", value);
                                map.put("properties", "unbounded");
                                res.add(map);
                            } else {
                                Map<String, Object> map = new HashMap<String, Object>();
                                List ids = new ArrayList<String>();
                                ids.add(widgetName.replaceAll(" ",
                                        "_") + "_" + name.replaceAll("" + " ", "-"));
                                ids.add(widgetName.replaceAll(" ", "_") + UIGeneratorConstants
                                        .TEXT_FIELD + "_" + name.replaceAll("" + " ", "-"));
                                map.put("ids", ids);
                                map.put("tooltip", value);
                                res.add(map);
                            }
                        } else {
                            Map<String, Object> map = new HashMap<String, Object>();
                            List ids = new ArrayList<String>();
                            ids.add(widgetName.replaceAll(" ", "_") + "_" + name.replaceAll("" +
                                    " ", "-"));
                            map.put("ids", ids);
                            map.put("tooltip", value);
                            res.add(map);
                        }
                    }
                }
            }
        }
        return res;
    }

    public OMElement getDataFromUIForBasicFilter(OMElement head, HttpServletRequest request) {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace namespace = fac.createOMNamespace(dataNamespace, "");
        OMElement data = fac.createOMElement(dataElement, namespace);
        Iterator it = head.getChildrenWithName(new QName(UIGeneratorConstants.WIDGET_ELEMENT));
        while (it.hasNext()) {
            OMElement widget = (OMElement) it.next();
            String widgetName = widget.getAttributeValue(new QName(null, UIGeneratorConstants.ARGUMENT_NAME));
            OMElement widgetData = fac.createOMElement(GenericUtil.getDataElementName(widgetName),
                                                       namespace);
            Iterator arguments = widget.getChildrenWithLocalName(UIGeneratorConstants.ARGUMENT_ELMENT);
            OMElement arg = null;
            while (arguments.hasNext()) {
                arg = (OMElement) arguments.next();
                if (UIGeneratorConstants.ARGUMENT_ELMENT.equals(arg.getLocalName())) {
                    String elementType = arg.getAttributeValue(new QName(null, UIGeneratorConstants.TYPE_ATTRIBUTE));
                    String name = arg.getFirstChildWithName(new QName(null, UIGeneratorConstants.ARGUMENT_NAME)).getText();
                    if (UIGeneratorConstants.OPTION_TEXT_FIELD.equals(elementType)) {
                        if (UIGeneratorConstants.MAXOCCUR_UNBOUNDED.equals(
                                arg.getAttributeValue(new QName(null, UIGeneratorConstants.MAXOCCUR_ELEMENT)))) {
                            continue;

                        }
                        // if maxoccurs unbounded is not mentioned use the default behaviour
                        else {
                            String input = request.getParameter(widgetName.replaceAll(" ", "_") + "_" +
                                                                name.replaceAll(" ", "-"));
                            if (input != null && !("".equals(input))) {
                                OMElement text = fac.createOMElement(GenericUtil.getDataElementName(name),
                                                                     namespace);
                                text.setText(input);
                                widgetData.addChild(text);
                            }
                            String inputOption = request.getParameter(widgetName.replaceAll(" ", "_") +
                                                                      UIGeneratorConstants.TEXT_FIELD +
                                                                      "_" + name.replaceAll(" ", "-"));
                            if (inputOption != null && !("".equals(inputOption))) {
                                OMElement value = fac.createOMElement(
                                        GenericUtil.getDataElementName(UIGeneratorConstants.TEXT_FIELD + name),
                                        namespace);
                                value.setText(inputOption);
                                widgetData.addChild(value);
                            }
                        }
                    } else {
                        String input = request.getParameter(widgetName.replaceAll(" ", "_") + "_" +
                                                            name.replaceAll(" ", "-"));
                        OMElement text = null;

                        if (input != null && !("".equals(input))) {
                            text = fac.createOMElement(GenericUtil.getDataElementName(name), namespace);
                            text.setText(input);
                            widgetData.addChild(text);

                        } else {
                            if (name.equals("Name")) {
                                text = fac.createOMElement(GenericUtil.getDataElementName(name), namespace);
                                text.setText(GovernanceConstants.DEFAULT_SERVICE_NAME);
                                widgetData.addChild(text);
                            }
                        }

                    }
                }
            }
            data.addChild(widgetData);
        }
        return GenericUtil.addExtraElements(data, request);
    }

    public List<Map> getValidationAttributes(OMElement head) {
        List<Map> res = new ArrayList<Map>();

        List<String> id = new ArrayList<String>();
        Iterator it = head.getChildrenWithName(new QName(UIGeneratorConstants.WIDGET_ELEMENT));
        while (it.hasNext()) {
            OMElement widget = (OMElement) it.next();
            String widgetName = widget.getAttributeValue(new QName(null, UIGeneratorConstants.ARGUMENT_NAME));
            boolean isUnboundedTable = UIGeneratorConstants.MAXOCCUR_UNBOUNDED.equals(widget.getAttributeValue(new QName(null, UIGeneratorConstants.MAXOCCUR_ELEMENT)));
            Iterator arguments = widget.getChildrenWithLocalName(UIGeneratorConstants.ARGUMENT_ELMENT);
            OMElement arg = null;
            while (arguments.hasNext()) {
                arg = (OMElement) arguments.next();
                if (UIGeneratorConstants.ARGUMENT_ELMENT.equals(arg.getLocalName())) {
                    String elementType = arg.getAttributeValue(new QName(null, UIGeneratorConstants.TYPE_ATTRIBUTE));
                    String name = arg.getFirstChildWithName(new QName(null, UIGeneratorConstants.ARGUMENT_NAME)).getText();

                    //check the validation fields and get the id's of them
                    String value = arg.getAttributeValue(new QName(null,
                            UIGeneratorConstants.VALIDATE_ATTRIBUTE));
                    if (value != null && !"".equals(value)) {
                        if (UIGeneratorConstants.OPTION_TEXT_FIELD.equals(elementType)) {
                            if (UIGeneratorConstants.MAXOCCUR_UNBOUNDED.equals(
                                    arg.getAttributeValue(new QName(null, UIGeneratorConstants.MAXOCCUR_ELEMENT)))) {
                                Map<String, Object> map = new HashMap<String, Object>();
                                List ids = new ArrayList<String>();
                                ids.add(widgetName.replaceAll(" ",
                                        "") + "_" + name.replaceAll("" + " ", "-"));
                                ids.add(widgetName.replaceAll(" ", "_") + UIGeneratorConstants
                                        .TEXT_FIELD + "_" + name.replaceAll("" + " ", "-"));
                                map.put("ids", ids);
                                map.put("name", name);
                                map.put("regexp", value);
                                map.put("properties", "unbounded");
                                res.add(map);
                            } else {
                                Map<String, Object> map = new HashMap<String, Object>();
                                List ids = new ArrayList<String>();
                                ids.add(widgetName.replaceAll(" ",
                                        "_") + "_" + name.replaceAll("" + " ", "-"));
                                ids.add(widgetName.replaceAll(" ", "_") + UIGeneratorConstants
                                        .TEXT_FIELD + "_" + name.replaceAll("" + " ", "-"));
                                map.put("ids", ids);
                                map.put("name", name);
                                map.put("regexp", value);
                                res.add(map);
                            }
                        } else {
                            Map<String, Object> map = new HashMap<String, Object>();
                            List ids = new ArrayList<String>();
                                ids.add(widgetName.replaceAll(" ", "") + "_" + name.replaceAll("" +
                                    " ", "-"));
                            map.put("ids", ids);
                            map.put("name", name);
                            map.put("regexp", value);
                            map.put("unboundedTable", isUnboundedTable);
                            res.add(map);
                        }
                    }
                }
            }
        }
        return res;
    }

    

    


    public String[] getMandatoryIdList(OMElement head) {
        List<String> id = new ArrayList<String>();
        Iterator it = head.getChildrenWithName(new QName(UIGeneratorConstants.WIDGET_ELEMENT));
        while (it.hasNext()) {
            OMElement widget = (OMElement) it.next();
            String widgetName = widget.getAttributeValue(new QName(null, UIGeneratorConstants.ARGUMENT_NAME));
            Iterator arguments = widget.getChildrenWithLocalName(UIGeneratorConstants.ARGUMENT_ELMENT);
            OMElement arg = null;
            while (arguments.hasNext()) {
                arg = (OMElement) arguments.next();
                if (UIGeneratorConstants.ARGUMENT_ELMENT.equals(arg.getLocalName())) {
                    String name = arg.getFirstChildWithName(new QName(null, UIGeneratorConstants.ARGUMENT_NAME)).getText();
                    //check the mandatory fields and get the id's of them
                    String mandatory = arg.getAttributeValue(new QName(null, UIGeneratorConstants.MANDETORY_ATTRIBUTE));
                    if (mandatory != null && "true".equals(mandatory)) {
                        id.add("id_" + widgetName.replaceAll(" ", "") + "_" + name.replaceAll(" ", ""));
                    }
                }
            }
        }
        return id.toArray(new String[id.size()]);
    }

    public String[] getKeyList(OMElement head, String[] keys) {
        List<String> id = new ArrayList<String>();
        Iterator it = head.getChildrenWithName(new QName(UIGeneratorConstants.WIDGET_ELEMENT));

        while (it.hasNext()) {
            OMElement widget = (OMElement) it.next();
            String widgetName = widget.getAttributeValue(new QName(null, UIGeneratorConstants.ARGUMENT_NAME));
            Iterator arguments = widget.getChildrenWithLocalName(UIGeneratorConstants.ARGUMENT_ELMENT);
            OMElement arg = null;

            while (arguments.hasNext()) {
                arg = (OMElement) arguments.next();
                if (UIGeneratorConstants.ARGUMENT_ELMENT.equals(arg.getLocalName())) {
                    String name = arg.getFirstChildWithName(new QName(null, UIGeneratorConstants.ARGUMENT_NAME)).getText();
                    String key = widgetName.replaceAll(" ", "") + "_" + name.replaceAll(" ", "");
                        if(Arrays.asList(keys).contains(key.toLowerCase())){
                        id.add(key);
                        }
                }
            }
        }
        return id.toArray(new String[id.size()]);
    }

    public String getLabelValue(OMElement head, String feild) {
        Iterator it = head.getChildrenWithName(new QName(UIGeneratorConstants.WIDGET_ELEMENT));

        while (it.hasNext()) {
            OMElement widget = (OMElement) it.next();
            String widgetName = widget.getAttributeValue(new QName(null, UIGeneratorConstants.ARGUMENT_NAME));
            Iterator arguments = widget.getChildrenWithLocalName(UIGeneratorConstants.ARGUMENT_ELMENT);
            OMElement arg = null;

            while (arguments.hasNext()) {
                arg = (OMElement) arguments.next();
                if (UIGeneratorConstants.ARGUMENT_ELMENT.equals(arg.getLocalName())) {
                    String name =
                            arg.getFirstChildWithName(new QName(null, UIGeneratorConstants.ARGUMENT_NAME)).getText();
                    String key = widgetName.replaceAll(" ", "") + "_" + name.replaceAll(" ", "");
                    if (feild.toLowerCase().equals(key.toLowerCase())) {
                        String label = null;
                        label = arg.getFirstChildWithName(new QName(null, UIGeneratorConstants.ARGUMENT_NAME))
                                   .getAttributeValue(
                                           new QName(UIGeneratorConstants.ARGUMENT_LABEL));
                        if (label == null) {
                            label = name;
                        }
                        return label;
                    }
                }
            }
        }
        return null;
    }

    public String[] getMandatoryNameList(OMElement head) {
        List<String> name = new ArrayList<String>();
        Iterator it = head.getChildrenWithName(new QName(UIGeneratorConstants.WIDGET_ELEMENT));
        while (it.hasNext()) {
            OMElement widget = (OMElement) it.next();
            Iterator arguments = widget.getChildrenWithLocalName(UIGeneratorConstants.ARGUMENT_ELMENT);
            OMElement arg = null;
            while (arguments.hasNext()) {
                arg = (OMElement) arguments.next();
                if (UIGeneratorConstants.ARGUMENT_ELMENT.equals(arg.getLocalName())) {
                    String name_element = arg.getFirstChildWithName(new QName(null, UIGeneratorConstants.ARGUMENT_NAME)).getText();
                    String mandatory = arg.getAttributeValue(new QName(null, UIGeneratorConstants.MANDETORY_ATTRIBUTE));
                    if (mandatory != null && "true".equals(mandatory)) {
                        name.add(name_element);
                    }
                }
            }
        }
        return name.toArray(new String[name.size()]);
    }

    public String[] getUnboundedTooltipList(OMElement head) {
        List<String> tooltips = new ArrayList<String>();
        Iterator it = head.getChildrenWithName(new QName(UIGeneratorConstants.WIDGET_ELEMENT));
        while (it.hasNext()) {
            OMElement widget = (OMElement) it.next();
            Iterator arguments = widget.getChildrenWithLocalName(UIGeneratorConstants.ARGUMENT_ELMENT);
            OMElement arg = null;
            while (arguments.hasNext()) {
                arg = (OMElement) arguments.next();
                if (UIGeneratorConstants.ARGUMENT_ELMENT.equals(arg.getLocalName())) {
                    //check the unbounded fields and get the names of them
                    if (UIGeneratorConstants.OPTION_TEXT_FIELD.equals(arg.getAttributeValue(new QName(null, UIGeneratorConstants.TYPE_ATTRIBUTE)))) {
                        //previous check is used to check the max occur unbounded only with option-text fields with other fields it will ignore
                        if (UIGeneratorConstants.MAXOCCUR_UNBOUNDED.equals(arg.getAttributeValue(new QName(null,
                                UIGeneratorConstants.MAXOCCUR_ELEMENT)))) {
                            String tooltip = arg.getAttributeValue(new QName(null,
                                    UIGeneratorConstants.TOOLTIP_ATTRIBUTE));
                            if (tooltip == null) {
                                tooltip = "";
                            }
                            tooltips.add(tooltip);
                        }
                    }
                }
            }
        }
        return tooltips.toArray(new String[tooltips.size()]);
    }

    public String[] getUnboundedNameList(OMElement head) {
        List<String> name = new ArrayList<String>();
        Iterator it = head.getChildrenWithName(new QName(UIGeneratorConstants.WIDGET_ELEMENT));
        while (it.hasNext()) {
            OMElement widget = (OMElement) it.next();
            Iterator arguments = widget.getChildrenWithLocalName(UIGeneratorConstants.ARGUMENT_ELMENT);
            OMElement arg = null;
            while (arguments.hasNext()) {
                arg = (OMElement) arguments.next();
                if (UIGeneratorConstants.ARGUMENT_ELMENT.equals(arg.getLocalName())) {
                    //check the unbounded fields and get the names of them
                    if (UIGeneratorConstants.OPTION_TEXT_FIELD.equals(arg.getAttributeValue(new QName(null, UIGeneratorConstants.TYPE_ATTRIBUTE)))) {
                        //previous check is used to check the max occur unbounded only with option-text fields with other fields it will ignore
                        if (UIGeneratorConstants.MAXOCCUR_UNBOUNDED.equals(arg.getAttributeValue(new QName(null,
                                UIGeneratorConstants.MAXOCCUR_ELEMENT)))) {
                            name.add(arg.getFirstChildWithName(new QName(null, UIGeneratorConstants.ARGUMENT_NAME)).getText());
                        }
                    }
                }
            }
        }
        return name.toArray(new String[name.size()]);
    }

    public String[] getUnboundedWidgetList(OMElement head) {
        List<String> widgetList = new ArrayList<String>();
        Iterator it = head.getChildrenWithName(new QName(UIGeneratorConstants.WIDGET_ELEMENT));
        while (it.hasNext()) {
            OMElement widget = (OMElement) it.next();
            Iterator arguments = widget.getChildrenWithLocalName(UIGeneratorConstants.ARGUMENT_ELMENT);
            OMElement arg = null;
            while (arguments.hasNext()) {
                arg = (OMElement) arguments.next();
                if (UIGeneratorConstants.ARGUMENT_ELMENT.equals(arg.getLocalName())) {
                    //check the unbounded fields and get the widget names of them
                    if (UIGeneratorConstants.OPTION_TEXT_FIELD.equals(arg.getAttributeValue(new QName(null, UIGeneratorConstants.TYPE_ATTRIBUTE)))) {
                        //previous check is used to check the max occur unbounded only with option-text fields with other fields it will ignore
                        if (UIGeneratorConstants.MAXOCCUR_UNBOUNDED.equals(arg.getAttributeValue(
                                new QName(null, UIGeneratorConstants.MAXOCCUR_ELEMENT)))) {
                            widgetList.add(widget.getAttributeValue(new QName(null, UIGeneratorConstants.WIDGET_NAME)));
                        }
                    }
                }
            }
        }
        return widgetList.toArray(new String[widgetList.size()]);
    }

    public String[][] getDateIdAndNameList(OMElement head, OMElement data, boolean markReadOnly) {
        List<String[]> result = new ArrayList<String[]>();
        Iterator it = head.getChildrenWithName(new QName(UIGeneratorConstants.WIDGET_ELEMENT));
        while (it.hasNext()) {
            OMElement widget = (OMElement) it.next();
            String widgetName = widget.getAttributeValue(new QName(null, UIGeneratorConstants.ARGUMENT_NAME));
            
            String widgetMaxOccurs = widget.getAttributeValue(new QName(null, UIGeneratorConstants.MAXOCCUR_ELEMENT));
            OMElement arg = null;
            
            if (widgetMaxOccurs != null ){
            	List<OMElement> dataElements =  new ArrayList<OMElement>();
                if (data != null) {
                   dataElements = GenericUtil.getChildsWithName(data, widgetName, dataNamespace);
                }

    			List<String[]> addedValues = new ArrayList<String[]>();
    			int addedItemsCount = 0;
    			for (int i = 0; i < dataElements.size(); i++) {
    				OMElement dataHeadElements = dataElements.get(i);
    				Iterator itemChildIt = dataHeadElements.getChildElements();
    				int a = 0;
    				List<String> values = new ArrayList<String>();
    				while (itemChildIt.hasNext()) {					
    					Object itemChildObj = itemChildIt.next();
    					if (!(itemChildObj instanceof OMElement)) {
    						continue;
    					}
    					OMElement itemChildEle = (OMElement) itemChildObj;
    					values.add(itemChildEle.getText());
    					a++;
    				}
    				addedValues.add(values.toArray(new String[values.size()]));
    				addedItemsCount++;
    			}
    			for (int i = 0; i < addedItemsCount; i++) {
    				Iterator arguments = widget.getChildrenWithLocalName(UIGeneratorConstants.ARGUMENT_ELMENT);
    				while (arguments.hasNext()) {
    					arg = (OMElement) arguments.next();
    					if (UIGeneratorConstants.ARGUMENT_ELMENT.equals(arg.getLocalName())) {
                            if (UIGeneratorConstants.DATE_FIELD.equals(arg.getAttributeValue(new QName
                                    (null, UIGeneratorConstants.TYPE_ATTRIBUTE)))) {
                            	if (markReadOnly && "true".equals(arg.getAttributeValue(new QName(null,
                                        UIGeneratorConstants.READONLY_ATTRIBUTE)))) {
                                    continue;
                                }
                            	
                            	String name = arg.getFirstChildWithName(new QName(null, UIGeneratorConstants.ARGUMENT_NAME)).getText();
                            	String elementId = widgetName.replaceAll(" ", "") + "_" + name.replaceAll(" ", "")+"_"+(i+1);
                                String[] idAndName = new String[2];
                                idAndName[0] = "id_" + elementId;
                                idAndName[1] = name;
                                result.add(idAndName);             
                            }
    					}
    				}
				} 
    			
            } else {
            	Iterator arguments = widget.getChildrenWithLocalName(UIGeneratorConstants.ARGUMENT_ELMENT);
            	while (arguments.hasNext()) {
                    arg = (OMElement) arguments.next();
                    if (UIGeneratorConstants.ARGUMENT_ELMENT.equals(arg.getLocalName())) {
                        if (UIGeneratorConstants.DATE_FIELD.equals(arg.getAttributeValue(new QName
                                (null, UIGeneratorConstants.TYPE_ATTRIBUTE)))) {
                            if (markReadOnly && "true".equals(arg.getAttributeValue(new QName(null,
                                    UIGeneratorConstants.READONLY_ATTRIBUTE)))) {
                                continue;
                            }
                            String name = arg.getFirstChildWithName(new QName(null, UIGeneratorConstants.ARGUMENT_NAME)).getText();
                            String[] idAndName = new String[2];
                            idAndName[0] = "id_" + widgetName.replaceAll(" ",
                                        "") + "_" + name.replaceAll(" ", "");
                            idAndName[1] = name;
                            result.add(idAndName);                        
                        }
                    }
                }
            }
            
            
            
            
        }
        return result.toArray(new String[result.size()][2]);
    }

    public String[][] getUnboundedValues(OMElement head, HttpServletRequest request,
                                         ServletConfig config) {
        List<String[]> values = new ArrayList<String[]>();
        Iterator it = head.getChildrenWithName(new QName(UIGeneratorConstants.WIDGET_ELEMENT));
        while (it.hasNext()) {
            OMElement widget = (OMElement) it.next();
            Iterator arguments = widget.getChildrenWithLocalName(UIGeneratorConstants.ARGUMENT_ELMENT);
            OMElement arg = null;
            while (arguments.hasNext()) {
                arg = (OMElement) arguments.next();
                if (UIGeneratorConstants.ARGUMENT_ELMENT.equals(arg.getLocalName())) {
                    //check the unbounded fields and get the values of drop-down in option-text type
                    if (UIGeneratorConstants.OPTION_TEXT_FIELD.equals(arg.getAttributeValue(new QName(null, UIGeneratorConstants.TYPE_ATTRIBUTE)))) {
                        //previous check is used to check the max occur unbounded only with option-text fields with other fields it will ignore
                        if (UIGeneratorConstants.MAXOCCUR_UNBOUNDED.equals(
                                arg.getAttributeValue(new QName(null, UIGeneratorConstants.MAXOCCUR_ELEMENT)))) {
                            List<String> inner = getOptionValues(arg, request, config);
                            values.add(inner.toArray(new String[inner.size()]));
                        }
                    }
                }
            }
        }
        return values.toArray(new String[0][0]);
    }

    private List<String> getOptionValues(OMElement arg, HttpServletRequest request,
                                         ServletConfig config) {
        OMElement values = arg.getFirstChildWithName(new QName(null,
                UIGeneratorConstants.OPTION_VALUES));
        Iterator iterator = values.getChildrenWithLocalName(UIGeneratorConstants.OPTION_VALUE);
        List<String> inner = new ArrayList<String>();
        if (iterator != null && iterator.hasNext()) {
            while (iterator.hasNext()) {
                inner.add(((OMElement) iterator.next()).getText());
            }
            return inner;
        } else {
            try {
                String className = values.getAttributeValue(new QName(null,
                        UIGeneratorConstants.OPTION_VALUE_CLASS));
                ClassLoader loader = Thread.currentThread().getContextClassLoader();
                Class<?> populatorClass = Class.forName(className, true, loader);

                DropDownDataPopulator populator = (DropDownDataPopulator) populatorClass.newInstance();
                String[] list = populator.getList(request, config);
                return new ArrayList<String>(Arrays.asList(list));
            } catch (ClassNotFoundException e) {
                log.error("Unable to load populator class", e);
            } catch (InstantiationException e) {
                log.error("Unable to load populator class", e);
            } catch (IllegalAccessException e) {
                log.error("Unable to load populator class", e);
            }
        }
        return inner;
    }
    
    public String getUnboundedWidgets(OMElement head, HttpServletRequest request, ServletConfig config) {
        Iterator it = head.getChildrenWithName(new QName(UIGeneratorConstants.WIDGET_ELEMENT));
        StringBuilder builder = new StringBuilder();
        while (it.hasNext()) {
            OMElement widget = (OMElement) it.next();
            String widgetMaxOccurs = widget.getAttributeValue(new QName(null, UIGeneratorConstants.MAXOCCUR_ELEMENT));
            String widgetName = widget.getAttributeValue(new QName(null, UIGeneratorConstants.ARGUMENT_NAME));
            if(widgetMaxOccurs != null){
            	if (UIGeneratorConstants.MAXOCCUR_UNBOUNDED.equals(widgetMaxOccurs)) {
            		
            		builder.append("function add"+widgetName.replaceAll(" ", "") + "_" + widgetName.replaceAll(" ", "")+"(){");
            		widgetName = widgetName.replaceAll(" ", "");
            		builder.append("var endpointMgt = document.getElementById('"+widgetName+"Mgt');");
            		builder.append("endpointMgt.parentNode.style.display = '';");
            		builder.append("var epCountTaker = document.getElementById('"+widgetName+"CountTaker'); ");
            		builder.append("var "+widgetName+"Count = parseInt(epCountTaker.value);");
            		builder.append(""+widgetName+"Count++;");
            		builder.append("epCountTaker.value = "+widgetName+"Count;");
                    builder.append("var theTr = document.createElement('TR');");
                    
                    Iterator arguments = widget.getChildrenWithLocalName(UIGeneratorConstants.ARGUMENT_ELMENT);
                    OMElement arg = null;
                    int a = 0;
                    boolean isDateFieldAdded = false;
                    while (arguments.hasNext()) {
                    	
                        arg = (OMElement) arguments.next();
                        String elementType = arg.getAttributeValue(new QName(null, UIGeneratorConstants.TYPE_ATTRIBUTE));
                        boolean isReadOnly = false;
                    	
	                    if ("true".equals(arg.getAttributeValue(new QName(null, UIGeneratorConstants.READONLY_ATTRIBUTE)))) {
	                        isReadOnly = true;
	                    }
	                    String tooltip = arg.getAttributeValue(new QName(null,
		                        UIGeneratorConstants.TOOLTIP_ATTRIBUTE));
		                if (tooltip == null) {
		                    tooltip = "";
		                }
		                tooltip = StringEscapeUtils.escapeHtml(tooltip);
		                
		                String name = arg.getFirstChildWithName(new QName(null, UIGeneratorConstants.ARGUMENT_NAME)).getText();
                        
                        if (UIGeneratorConstants.ARGUMENT_ELMENT.equals(arg.getLocalName())) {
                        	builder.append("var theTd"+a+" = document.createElement('TD');");
                        	builder.append("var rowId = '"+widgetName+"Count"+elementType+"_"+a+"_'+(epCountTaker.value -1);");
                        	
                        	if(UIGeneratorConstants.DATE_FIELD.equals(elementType)){
                        		 
                        		UIComponent dateField = new DateField(null,name, null, null,widgetName, null, false, tooltip,false,true);
                        		builder.append("theTd"+a+".innerHTML = '" + dateField.generate() + "';");
                        		isDateFieldAdded = true;
                        		
                            } else if (UIGeneratorConstants.CHECKBOX_FIELD.equals(elementType)) {
                            	
                            	UIComponent checkBox  = new CheckBox(name, null, widgetName, null,tooltip,false,true);    	                        
    	                        builder.append("theTd"+a+".innerHTML = '" + checkBox.generate() + "';");
    	                        
                            } else if (UIGeneratorConstants.OPTION_FIELD.equals(elementType)) {                          	
                            	
                            	String startsWith = arg.getAttributeValue(new QName(null,UIGeneratorConstants.PATH_START_WITH));
                            	List<String> optionValues = getOptionValues(arg, request, config);
    	
    	                        DropDown dropDown = new DropDown(null, isReadOnly, name, null, null,optionValues.toArray(new String[optionValues.size()]), widgetName, null, tooltip,true);
                            	builder.append("theTd"+a+".innerHTML = '" + dropDown.generate() + "';");
    	                        
                            }else if (UIGeneratorConstants.TEXT_AREA_FIELD.equals(elementType)) {
                            	int height = -1;
        	                    int width = 200;
        	                    String heightString = arg.getAttributeValue(new QName(null, UIGeneratorConstants.HEIGHT_ATTRIBUTE));
        	                    if (heightString != null) {
        	                        try {
        	                            height = Integer.parseInt(heightString);
        	                        } catch (NumberFormatException ignored) {
        	                        }
        	                    }
        	                    String widthString = arg.getAttributeValue(new QName(null, UIGeneratorConstants.WIDTH_ATTRIBUTE));
        	                    if (widthString != null) {
        	                        try {
        	                            width = Integer.parseInt(widthString);
        	                        } catch (NumberFormatException ignored) {
        	                        }
        	                    }
        	                    
        	                    UIComponent textArea  = new TextArea(null, name, null, null, widgetName, null, height, width, isReadOnly, false, tooltip, false,true);
        	                    builder.append("theTd"+a+".innerHTML = '" + textArea.generate() + "';");
                            } else {
                            	
                            	 boolean isURL = Boolean.toString(true).equals(arg.getAttributeValue(
         	                            new QName(null, UIGeneratorConstants.URL_ATTRIBUTE)));
         	                    String urlTemplate = arg.getAttributeValue(
         	                            new QName(null, UIGeneratorConstants.URL_TEMPLATE_ATTRIBUTE));
         	                    boolean isPath = Boolean.toString(true).equals(arg.getAttributeValue(
         	                            new QName(null, UIGeneratorConstants.PATH_ATTRIBUTE)));
         	                    String startsWith = arg.getAttributeValue(new QName(null,UIGeneratorConstants.PATH_START_WITH));
                            	
                            	UIComponent textArea  = new TextField(null, name, null, null, widgetName, null, isURL, urlTemplate, isPath, isReadOnly, false, tooltip, startsWith, request,true);
                            	
                            	builder.append("theTd"+a+".innerHTML = '" + textArea.generate() + "';");
                            }
                        	
                        	//check the unbounded fields and get the widget names of them    
                        	builder.append("theTr.appendChild(theTd"+a+");");
                        }
                        a++;
                    }
                    builder.append("var theTddelete = document.createElement('TD');");
                	builder.append("var td3Inner = '<a class=\"icon-link\" title=\"delete\" onclick=\"delete"+widgetName+"_"+widgetName+"(this.parentNode.parentNode.rowIndex)\" style=\"background-image:url(../admin/images/delete.gif);\">Delete</a>';");
                	builder.append("theTddelete.innerHTML = td3Inner;");
                	builder.append("theTr.appendChild(theTddelete);");

                	builder.append("var dateArr = new Array();");
                	builder.append("var dateArrSize = 0;");

                    /*Following code segment contains a loop to make ids and names of elements appearing
                    * in the row, unique.
                    * The way they are made unique is for "_' + "+widgetName+"Count is appended
                    * to each name and id.*/

                	builder.append("jQuery('*',theTr).each(function(){");
                    builder.append("var idAttr = jQuery(this).attr('id');");
                    builder.append("if (typeof idAttr !== typeof undefined && idAttr !== false && idAttr != \"\") {");
                    builder.append("idAttr = jQuery(this).attr('id') + '_' + ");
                    builder.append(widgetName);
                    builder.append("Count;");
                    builder.append("var jdateId = '#' + jQuery(this).id;");
                    builder.append("if (theTr.innerHTML.indexOf(jdateId) !== -1) {");
                    builder.append("dateArr[dateArrSize] = idAttr;");
                    builder.append("dateArrSize++;");
                    builder.append("}");
                    builder.append("jQuery(this).attr('id',idAttr);");
                    builder.append("}");
                    builder.append("var nameAttr = jQuery(this).attr('name');");
                    builder.append("if (typeof nameAttr !== typeof undefined && ");
                    builder.append("nameAttr !== false && nameAttr != \"\") {");
                    builder.append("nameAttr = jQuery(this).attr('name') +'_'+ ");
                    builder.append(widgetName);
                    builder.append("Count;");
                    builder.append("jQuery(this).attr('name',nameAttr);");
                    builder.append("}");
                    builder.append("});");

                	
                    builder.append("endpointMgt.appendChild(theTr);");
                    
                    if(isDateFieldAdded){
                    	 builder.append("for (var i = 0; i < dateArr.length; ++i) {var elementId = dateArr[i];var datePickValue = \"#\"+elementId;jQuery(datePickValue).datepicker();}");
                    }
                    
                    builder.append("}");
                    
                    builder.append("function delete"+widgetName+"_"+widgetName+"(index){");
                    builder.append("var endpointMgt = document.getElementById('"+widgetName+"Mgt');");
                    builder.append("endpointMgt.parentNode.style.display = '';");
                    builder.append("endpointMgt.parentNode.deleteRow(index);");

                    builder.append("var table = endpointMgt.parentNode;");
                    builder.append("var rows = table.getElementsByTagName('input');");

                    builder.append("if (rows != null & rows.length == 0) {");
                    builder.append("    endpointMgt.parentNode.style.display = 'none';");
                    builder.append("}");
                    builder.append("}");
            	}
            }            
        }
        return builder.toString();
    }
    
	public String printWidget(String widget,String values,String id) {
		
		StringBuilder content = new StringBuilder();
		if(UIGeneratorConstants.DATE_FIELD.equals(widget)){
			content.append("<td><a class=\"icon-link\" style=\"background-image: url( ../admin/images/calendar.gif);\" onclick=\"jQuery('#" + id + "').datepicker( 'show' );\" href=\"javascript:void(0)\"></a>");
			content.append("<input type=\"text\" name=\"" + id + "\" value=\""+ values +"\" id=\"" + id + "\" style=\"width:200px\"/></td>");			
		} else if(UIGeneratorConstants.TEXT_AREA_FIELD.equals(widget)){
			content.append("<td><textarea type=\"text\" name=\"" + id + "\" id=\"" + id + "\" style=\"width:200px\">"+ values +"</textarea></td>");			
		} else if(UIGeneratorConstants.TEXT_FIELD.equals(widget)){
			content.append("<td><input type=\"text\" name=\"" + id + "\" value=\""+ values +"\" id=\"" + id + "\" style=\"width:200px\"/></td>");			
		} else if(UIGeneratorConstants.CHECKBOX_FIELD.equals(widget)){
			content.append("<td><input type=\"checkbox\" name=\"" + id + "\" value=\""+ values +"\" id=\"" + id + "\" style=\"width:200px\"/></td>");			
		} else{
			content.append("<td><input type=\"text\" name=\"" + id + "\" value=\""+ values +"\" id=\"" + id + "\" style=\"width:200px\"/></td>");			
		}
			
		return content.toString();
	}
	
	public String printTextFieldX(String id, String mandatory,boolean isPath, boolean isReadOnly,
            String tooltip, String startsWith,String value, HttpServletRequest request) {
		StringBuilder element = new StringBuilder();
        String selectResource = "";       
        if (isPath) {
            if (startsWith != null) {
                selectResource = " <input type=\"button\" class=\"button\" value=\"..\" title=\"" + CarbonUIUtil.geti18nString("select.path",
                        "org.wso2.carbon.governance.services.ui.i18n.Resources", request.getLocale()) + "\" onclick=\"showGovernanceResourceTreeWithCustomPath('" + id + "' ,'" + startsWith + "');\"/>";
            } else {
                selectResource = " <input type=\"button\" class=\"button\" value=\"..\" title=\"" + CarbonUIUtil.geti18nString("select.path",
                        "org.wso2.carbon.governance.services.ui.i18n.Resources", request.getLocale()) + "\" onclick=\"showGovernanceResourceTree('" + id + "');\"/>";
            }
        }
        if ("true".equals(mandatory)) {
            element.append("<td><span class=\"required\">*</span><input type=\"text\" name=\"" + id
                    + "\" title=\"" + tooltip + "\" id=\"" + id + "\" " + (value != null ?  "value=\"" + value + "\"" :"") +" style=\"width:" + 
                    UIGeneratorConstants
                    .DEFAULT_WIDTH+ "px\"" + (isReadOnly ? " readonly" : "") + "/>" + (isPath ? selectResource : "") + "</td>");
        } else {
            element.append("<td><input type=\"text\" name=\"" + id
                    + "\" title=\"" + tooltip + "\" id=\"" + id + "\" " + (value != null ?  "value=\"" + value + "\"" :"") +" style=\"width:" + (value != null ?  "value=\"" + value + "\"" :"") +
                    UIGeneratorConstants
                    .DEFAULT_WIDTH+ "px\"" + (isReadOnly ? " readonly" : "") + "/>" + (isPath ? selectResource : "") + "</td>");
        }        
        return element.toString();
	}
	
	public String printDateFeild(String id, String value, boolean isReadOnly,String tooltip) {
		 StringBuilder element = new StringBuilder();
		 value = StringEscapeUtils.escapeHtml(value);
		 element.append("<td>");
	        if (!isReadOnly) {
	            element.append("<a class=\"icon-link\" style=\"background-image: " +
	                    "url( ../admin/images/calendar.gif);\" onclick=\"jQuery('#" + id + "')" +
	                    ".datepicker( 'show' );\" href=\"javascript:void(0)\"></a>");

	        }
	     element.append("<input type=\"text\" name=\"" + id + "\" title=\"" + tooltip + "\" style=\"width:" + UIGeneratorConstants.DATE_WIDTH 
	        		+ "px\"" + (isReadOnly ? " readonly" : "") + " id=\"" + id + "\" " 
	        		+ "value=\"" + value + "\" />" + "</td>");
		 return element.toString();		
	}
	
	public String printDropDownFeild(String id, String[] values,String value, String tooltip) {
		StringBuilder dropDown = new StringBuilder();
		dropDown.append("<td><select id=\"id_" + id + "\" " +
                "name=\"" + id + "\" title=\"" + tooltip + "\">");

        for (int i = 0; i < values.length; i++) {
            dropDown.append("<option value=\"" + StringEscapeUtils.escapeHtml(values[i]) +
                    "\"");
            if (values[i].equals(value)) {
                dropDown.append(" selected>");
            } else {
                dropDown.append(">");
            }
            dropDown.append(StringEscapeUtils.escapeHtml(values[i]));
            dropDown.append("</option>");
        }
        dropDown.append("</select></td>");
		return dropDown.toString();
	}
	
	public String printCheckboxFeild(String id, String value, String tooltip) {
		if (Boolean.toString(true).equals(value)) {
            return "<td><input type=\"checkbox\" checked=\"checked\" name=\"" + id +
                    "\" value=\"true\" title=\"" + tooltip + "\"/></td>";
        } else {
            return "<td><input type=\"checkbox\" name=\"" + id +
            		"\" value=\"true\" title=\"" + tooltip + "\"/></td>";
        }
	}
	
	
	public String printDeleteWidget(String widget) {
		
		StringBuilder content = new StringBuilder();		
		content.append("<td><a class=\"icon-link\" title=\"delete\" onclick=\"delete"+widget.replaceAll(" ", "")+"_"+widget.replaceAll(" ", "")+"(this.parentNode.parentNode.rowIndex)\" style=\"background-image:url(../admin/images/delete.gif);\">Delete</a></td>");			
		return content.toString();
	}

}
