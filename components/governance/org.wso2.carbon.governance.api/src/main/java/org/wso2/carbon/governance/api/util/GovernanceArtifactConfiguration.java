/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.governance.api.util;

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.utils.component.xml.config.ManagementPermission;

import javax.xml.namespace.QName;
import java.util.*;

/**
 * Configuration of a Governance Artifact
 */
public class GovernanceArtifactConfiguration {

    private String mediaType;
    private String extension;
    private boolean hasNamespace;
    private int iconSet = 0;
    private String key;
    private String singularLabel;
    private String pluralLabel;
    private String pathExpression;
    private String lifecycle;
    private String groupingAttribute;
    private OMElement uiConfigurations;
    private List<Association> relationships = new LinkedList<Association>();
    private OMElement contentDefinition;
    private String contentURL;
    private List<ManagementPermission> uiPermissions = new LinkedList<ManagementPermission>();
    private UIListConfiguration[] listConfigurations;
    private String artifactNameAttribute = "overview_name";
    private String artifactNamespaceAttribute = "overview_namespace";
    private String artifactElementRoot = "metadata";
    private String artifactElementNamespace = "http://www.wso2.org/governance/metadata";
    List<String> uniqueAttributes = new ArrayList<>();

    private static final String WIDGET_ELEMENT = "table";
    private static final String ARGUMENT_ELMENT = "field";
    private static final String ARGUMENT_NAME = "name";
    private static final String WIDGET_NAME = "name";
    private static final String TYPE_ATTRIBUTE = "type";
    private static final String MANDETORY_ATTRIBUTE = "required";
    private static final String VALIDATE_ATTRIBUTE = "validate";
    private static final String MAXOCCUR_UNBOUNDED = "unbounded";
    private static final String OPTION_TEXT_FIELD = "option-text";
    private static final String MAXOCCUR_ELEMENT = "maxoccurs";
    public static final String TEXT_FIELD = "text";
    public static final String ENTRY_FIELD = "entry";

    private Map<String, String> contextToLifeCycleMap = new HashMap<String, String>();

    /**
     * Method to obtain the media type.
     *
     * @return the media type.
     */
    public String getMediaType() {
        return mediaType;
    }

    /**
     * Method to set the media type.
     *
     * @param mediaType the media type.
     */
    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    /**
     * Method to get the content URL.
     */
    public String getContentURL() {
        return contentURL;
    }

    /**
     * Method to set the content URL.
     *
     * @param contentURL the content URL.
     */
    public void setContentURL(String contentURL) {
        this.contentURL = contentURL;
    }

    /**
     * Method to obtain the extension.
     *
     * @return the extension.
     */
    public String getExtension() {
        return extension;
    }

    /**
     * Method to set the extension.
     *
     * @param extension the extension.
     */
    public void setExtension(String extension) {
        this.extension = extension;
    }

    /**
     * Method to get whether namespace exists.
     */
    public boolean hasNamespace() {
        return hasNamespace;
    }

    /**
     * Method to set whether namespace exists.
     *
     * @param hasNamespace whether namespace exists.
     */
    public void setHasNamespace(boolean hasNamespace) {
        this.hasNamespace = hasNamespace;
    }

    /**
     * Method to obtain the icon set to be used.
     *
     * @return the icon set to be used.
     */
    public int getIconSet() {
        return iconSet;
    }

    /**
     * Method to set the icon set to be used.
     *
     * @param iconSet the icon set to be used.
     */
    public void setIconSet(int iconSet) {
        this.iconSet = iconSet;
    }

    /**
     * Returns the grouping attribute
     *
     * @return Grouping attribute name
     */
    public String getGroupingAttribute() {
        return groupingAttribute;
    }

    /**
     *
     * @param groupingAttribute attribute to be grouped from
     */
    public void setGroupingAttribute(String groupingAttribute) {
        this.groupingAttribute = groupingAttribute;
    }

    /**
     * Method to obtain the attribute that specifies the artifact name.
     *
     * @return the attribute that specifies the artifact name.
     */
    public String getArtifactNameAttribute() {
        return artifactNameAttribute;
    }

    /**
     * Method to set the attribute that specifies the artifact name.
     *
     * @param artifactNameAttribute the attribute that specifies the artifact name.
     */
    public void setArtifactNameAttribute(String artifactNameAttribute) {
        this.artifactNameAttribute = artifactNameAttribute;
    }

    /**
     * Method to obtain the attribute that specifies the artifact namespace.
     *
     * @return the attribute that specifies the artifact namespace.
     */
    public String getArtifactNamespaceAttribute() {
        return artifactNamespaceAttribute;
    }

    /**
     * Method to set the attribute that specifies the artifact namespace.
     *
     * @param artifactNamespaceAttribute the attribute that specifies the artifact namespace.
     */
    public void setArtifactNamespaceAttribute(String artifactNamespaceAttribute) {
        this.artifactNamespaceAttribute = artifactNamespaceAttribute;
    }

    public List<String> getUniqueAttributes() {
        return uniqueAttributes;
    }

    public void setUniqueAttributes(List<String> uniqueAttributes) {
        this.uniqueAttributes = uniqueAttributes;
    }

    /**
     * Method to obtain the name of the root element of an XML governance artifact.
     *
     * @return the name of the root element of an XML governance artifact.
     */
    @SuppressWarnings("unused")
    public String getArtifactElementRoot() {
        return artifactElementRoot;
    }

    /**
     * Method to set the name of the root element of an XML governance artifact.
     *
     * @param artifactElementRoot the name of the root element of an XML governance artifact.
     */
    public void setArtifactElementRoot(String artifactElementRoot) {
        this.artifactElementRoot = artifactElementRoot;
    }

    /**
     * Method to obtain the namespace of the root element of an XML governance artifact.
     *
     * @return the namespace of the root element of an XML governance artifact.
     */
    public String getArtifactElementNamespace() {
        return artifactElementNamespace;
    }

    /**
     * Method to set the namespace of the root element of an XML governance artifact.
     *
     * @param artifactElementNamespace the namespace of the root element of an XML governance
     *                                 artifact.
     */
    public void setArtifactElementNamespace(String artifactElementNamespace) {
        this.artifactElementNamespace = artifactElementNamespace;
    }

    /**
     * Method to retrieve the relationship details.
     *
     * @return the relationship details.
     */
    public Association[] getRelationshipDefinitions() {
        return relationships.toArray(new Association[relationships.size()]);
    }

    /**
     * Method to set the relationship details.
     *
     * @param relationships the relationship details.
     */
    public void setRelationshipDefinitions(Association[] relationships) {
        this.relationships = Arrays.asList(relationships);
    }

    /**
     * Method to obtain the content definition element.
     *
     * @return the content definition element.
     */
    @SuppressWarnings("unused")
    public OMElement getContentDefinition() {
        return contentDefinition;
    }

    /**
     * Method to set the content definition element.
     *
     * @param contentDefinition the content definition element.
     */
    public void setContentDefinition(OMElement contentDefinition) {
        this.contentDefinition = contentDefinition;
    }

    /**
     * Method to obtain the UI configurations element.
     *
     * @return the UI configurations element.
     */
    @SuppressWarnings("unused")
    public OMElement getUIConfigurations() {
        return uiConfigurations;
    }

    /**
     * Method to set the UI configurations element.
     *
     * @param uiConfigurations the UI configurations element.
     */
    public void setUIConfigurations(OMElement uiConfigurations) {
        this.uiConfigurations = uiConfigurations;
    }

    /**
     * Method to obtain the UI permissions element.
     *
     * @return the UI permissions element.
     */
    @SuppressWarnings("unused")
    public ManagementPermission[] getUIPermissions() {
        return uiPermissions.toArray(new ManagementPermission[uiPermissions.size()]);
    }

    /**
     * Method to set the UI permissions element.
     *
     * @param uiPermissions the UI permissions element.
     */
    public void setUIPermissions(ManagementPermission[] uiPermissions) {
        this.uiPermissions = Arrays.asList(uiPermissions);
    }

    /**
     * Method to set the configuration for the artifact list UI.
     *
     * @param listConfigurations the configuration for the artifact list UI.
     */
    public void setUIListConfigurations(OMElement listConfigurations) {
        Iterator iterator =
                listConfigurations.getChildrenWithName(
                        new QName("column"));
        List<UIListConfiguration> configurations =
                new LinkedList<UIListConfiguration>();
        while (iterator.hasNext()) {
            OMElement configurationElement = (OMElement) iterator.next();
            OMElement dataElement =
                    configurationElement.getFirstChildWithName(
                            new QName("data"));
            if (dataElement != null) {
                String name = configurationElement.getAttributeValue(new QName("name"));
                String key = dataElement.getAttributeValue(new QName("value"));
                String type = dataElement.getAttributeValue(new QName("type"));
                String expression = dataElement.getAttributeValue(new QName("href"));
                if (key != null && name != null) {
                    configurations.add(new UIListConfiguration(key, type, name, expression));
                }
            }

        }
        this.listConfigurations =
                configurations.toArray(new UIListConfiguration[configurations.size()]);
    }

    /**
     * Method to obtain the key that can be used to locate an artifact.
     *
     * @return the key that can be used to locate an artifact.
     */
    public String getKey() {
        return key;
    }

    /**
     * Method to set the key that can be used to locate an artifact.
     *
     * @param key the key that can be used to locate an artifact.
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Method to obtain the label used in singular representations of the artifact.
     *
     * @return the label used in singular representations of the artifact.
     */
    public String getSingularLabel() {
        return singularLabel;
    }

    /**
     * Method to set the label used in singular representations of the artifact.
     *
     * @param singularLabel the label used in singular representations of the artifact.
     */
    public void setSingularLabel(String singularLabel) {
        this.singularLabel = singularLabel;
    }

    /**
     * Method to obtain the label used in plural representations of the artifact.
     *
     * @return the label used in plural representations of the artifact.
     */
    public String getPluralLabel() {
        return pluralLabel;
    }

    /**
     * Method to set the label used in plural representations of the artifact.
     *
     * @param  pluralLabel the label used in plural representations of the artifact.
     */
    public void setPluralLabel(String pluralLabel) {
        this.pluralLabel = pluralLabel;
    }

    /**
     * Method to obtain the expression that can be used to compute where to store the artifact.
     *
     * @return the expression that can be used to compute where to store the artifact.
     */
    public String getPathExpression() {
        return pathExpression;
    }

    /**
     * Method to set the expression that can be used to compute where to store the artifact.
     *
     * @param pathExpression the expression that can be used to compute where to store the artifact.
     */
    public void setPathExpression(String pathExpression) {
        this.pathExpression = pathExpression;
        setUniqueAttributes();
    }

    /**
     * Method to obtain the lifecycle associated with the artifact configuration
     *
     * @return Lifecycle name associated with the artifact configuration
     */
    public String getLifecycle() {
        return lifecycle;
    }

    /**
     *
     * @param context name of the context which life cycle is needed
     * @return lifecycle name of the context. Or the default attached lifecycle
     */
    public String getLifeCycleOfContext(String context) {
        String lc = contextToLifeCycleMap.get(context);

        if(lc == null) {
            lc = getLifecycle();
        }

        return lc;
    }

    /**
     *
     * @param context context name
     * @param lcName life-cyle name
     */
    public void addLifeCycleToContext(String context, String lcName) {
        contextToLifeCycleMap.put(context, lcName);
    }

    /**
     * Method to associate lifecycle with the artifact configuration
     *
     * @param lifecycle Name of the lifecycle associated with the artifact configuration
     */
    public void setLifecycle(String lifecycle) {
        this.lifecycle = lifecycle;
    }

    /**
     * Method to obtain the list of keys that will be used to generate the artifact list UI.
     *
     * @return the list of keys that will be used to generate the artifact list UI.
     */
    public String[] getKeysOnListUI() {
        String[] keysOnListUI = new String[listConfigurations.length];
        for (int i = 0; i < listConfigurations.length; i++) {
            keysOnListUI[i] = listConfigurations[i].getKey();
        }
        return keysOnListUI;
    }

    /**
     * Method to obtain the list of names that will be displayed on the artifact list UI.
     *
     * @return the list of names that will be displayed on the artifact list UI.
     */
    public String[] getNamesOnListUI() {
        String[] namesOnListUI = new String[listConfigurations.length];
        for (int i = 0; i < listConfigurations.length; i++) {
            namesOnListUI[i] = listConfigurations[i].getName();
        }
        return namesOnListUI;
    }

    /**
     * Method to obtain the list of types that will be used to populate the content on the artifact
     * list UI.
     *
     * @return the list of types that will be used to populate the content on the artifact list UI.
     */
    public String[] getTypesOnListUI() {
        String[] typesOnListUI = new String[listConfigurations.length];
        for (int i = 0; i < listConfigurations.length; i++) {
            typesOnListUI[i] = listConfigurations[i].getType();
        }
        return typesOnListUI;
    }

    /**
     * Method to obtain the list of expressions that will be used to populate the content on the
     * artifact list UI.
     *
     * @return the list of expressions that will be used to populate the content on the artifact
     *         list UI.
     */
    public String[] getExpressionsOnListUI() {
        String[] expressionsOnListUI = new String[listConfigurations.length];
        for (int i = 0; i < listConfigurations.length; i++) {
            expressionsOnListUI[i] = listConfigurations[i].getExpression();
        }
        return expressionsOnListUI;
    }

    private String getDataElementName(String widgetName) {
        if (widgetName == null || widgetName.length() == 0) {
            return null;
        }
        String[] nameParts = widgetName.split("_");
        String convertedName = null;
        //  making widget name camel case
        for (String namePart : nameParts) {
            int i;
            for (i = 0; i < namePart.length(); i++) {
                char c = namePart.charAt(i);
                if (!Character.isLetter(c) || Character.isLowerCase(c)) {
                    break;
                }
            }
            namePart = namePart.substring(0, i).toLowerCase() + namePart.substring(i);
            if (convertedName == null) {
                convertedName = namePart;
            } else {
                convertedName += "_" + namePart;
            }
        }
        if (convertedName == null) {
            return null;
        }

        return convertedName.replaceAll(" ", "").replaceAll("-", "");
    }

    /**
     * Method to obtain the list of keys, in the form used in GovernanceArtifact
     * getAttributes/setAttributes methods, for attributes need to be regex validated.
     *
     * @return  list of keys in the form used in GovernanceArtifact
     *          getAttributes/setAttributes methods
     */
    public List<Map> getValidationAttributes() {
        List<Map> res = new ArrayList<Map>();

        List<String> id = new ArrayList<String>();
        Iterator it = contentDefinition.getChildrenWithName(new QName(WIDGET_ELEMENT));
        while (it.hasNext()) {
            OMElement widget = (OMElement) it.next();
            String widgetName = widget.getAttributeValue(new QName(null, ARGUMENT_NAME));
            Iterator arguments = widget.getChildrenWithLocalName(ARGUMENT_ELMENT);
            OMElement arg = null;
            while (arguments.hasNext()) {
                arg = (OMElement) arguments.next();
                if (ARGUMENT_ELMENT.equals(arg.getLocalName())) {
                    //check the validation fields and get the id's of them
                    String value = arg.getAttributeValue(new QName(null,
                            VALIDATE_ATTRIBUTE));

                    if (value != null && !"".equals(value)) {
                        String elementType = arg.getAttributeValue(new QName(null, TYPE_ATTRIBUTE));
                        String name = arg.getFirstChildWithName(new QName(null, ARGUMENT_NAME)).getText();
                        List<String> keys = new ArrayList<String>();

                        if (OPTION_TEXT_FIELD.equals(elementType)) {
                            if (MAXOCCUR_UNBOUNDED.equals(
                                    arg.getAttributeValue(new QName(null, MAXOCCUR_ELEMENT)))) {
                                Map<String, Object> map = new HashMap<String, Object>();

                                keys.add(getDataElementName(widgetName + "_" + ENTRY_FIELD));
                                map.put("keys", keys);
                                map.put("name", name);
                                map.put("regexp", value);
                                map.put("properties", "unbounded");

                                res.add(map);
                            } else {
                                Map<String, Object> map = new HashMap<String, Object>();

                                keys.add(getDataElementName(widgetName + "_"  + name));
                                keys.add(getDataElementName(widgetName + "_"  + TEXT_FIELD +
                                        name));
                                map.put("keys", keys);
                                map.put("name", name);
                                map.put("regexp", value);

                                res.add(map);
                            }
                        } else {
                            Map<String, Object> map = new HashMap<String, Object>();

                            keys.add(getDataElementName(widgetName + "_"  + name));
                            map.put("keys", keys);
                            map.put("name", name);
                            map.put("regexp", value);

                            res.add(map);
                        }
                    }
                }
            }
        }
        return res;
    }

    protected void setUniqueAttributes() {
        if (pathExpression != null && !pathExpression.isEmpty()) {
            String[] pathSegments = pathExpression.split("/");
            for (String pathSegment : pathSegments) {
                if (pathSegment.startsWith("@")) {
                    String attribute = pathSegment.substring(1);
                    attribute = attribute.replace("{", "").replace("}", "");
                    if(attribute.indexOf("_") == -1){
                        attribute = "overview_".concat(attribute);
                    }
                    uniqueAttributes.add(attribute);
                }
            }
        }
    }

    private static class UIListConfiguration {

        private String key;
        private String type;
        private String name;
        private String expression;

        private UIListConfiguration(String key, String type, String name, String expression) {
            this.key = key;
            this.type = type;
            this.name = name;
            this.expression = expression;
        }

        public String getKey() {
            return key;
        }

        public String getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public String getExpression() {
            return expression;
        }
    }
}
