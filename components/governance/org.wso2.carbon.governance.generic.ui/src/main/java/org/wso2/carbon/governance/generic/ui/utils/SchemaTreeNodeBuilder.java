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

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.wso2.carbon.registry.common.ui.SchemaConstants;
import org.wso2.carbon.registry.common.ui.utils.TreeNode;
import org.wso2.carbon.registry.common.ui.utils.TreeNodeBuilderUtil;
import org.wso2.carbon.registry.common.ui.utils.UIUtil;
import org.wso2.carbon.registry.core.utils.RegistryUtils;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SchemaTreeNodeBuilder {


    private String schemaPath;
    private OMElement schemaElement;
    private String actualSchemaPath;

    public SchemaTreeNodeBuilder(String path,String content) throws Exception{
        schemaPath = path;
        schemaElement = UIUtil.buildOMElement(content);
    }
    public TreeNode buildTree() throws Exception{
        String schemaName = RegistryUtils.getResourceName(schemaPath);
        List<String> attributeList = new ArrayList<String>();
        attributeList.add(SchemaConstants.NAME);
        TreeNode root = new TreeNode(TreeNodeBuilderUtil.generateKeyName(SchemaConstants.SCHEMA, schemaName));
        /* Adding namespace information in to the tree struction */
        addSchemaDataToTreeWithHyperlink(SchemaConstants.XPATH_TARGETNAMESPACE,
                SchemaConstants.TARGETNAMESPACE, root,
                schemaElement, false);
        /* Adding Elements*/
        attributeList.add(SchemaConstants.REF);
        addSchemaDataToTree(SchemaConstants.XPATH_ELEMENTS,SchemaConstants.ELEMENTS,root, schemaElement,attributeList);
        /* Adding attributes */
        attributeList.remove(SchemaConstants.REF);
        addSchemaDataToTree(SchemaConstants.XPATH_ATTRIBUTES,SchemaConstants.ATTRIBUTES,root, schemaElement, attributeList);
        /* Adding Groups */
        addSchemaDataToTree(SchemaConstants.XPATH_GROUPS,SchemaConstants.GROUPS,root, schemaElement,attributeList);
        /* Adding schema includes */
        addSchemaDataToTreeWithHyperlink(SchemaConstants.SCHEMA_INCLUDES_EXPR,SchemaConstants.INCLUDES,root,
                schemaElement, true);
        /* Adding schema import namespaces*/
        attributeList.clear();
        attributeList.add(SchemaConstants.SCHEMALOCATION);
        attributeList.add(SchemaConstants.NAMESPACE);
        addSchemaDataToTreeWithHyperlink(SchemaConstants.SCHEMA_IMPORTS_EXPR,SchemaConstants.IMPORTS,
                root,schemaElement,true);
        return root;
    }

    public void addSchemaDataToTree(String xPath, String headingName, TreeNode root, OMElement scope,
                                    List<String> attributes) throws Exception{
        List<OMElement> list = TreeNodeBuilderUtil.evaluateXPathToElements(xPath,scope);
        TreeNode nodes = new TreeNode(headingName);
        for(OMElement element:list){
            Iterator attributeIt = attributes.iterator();
            String elementValue = "";
            while(attributeIt.hasNext()){
                String attributeName = (String)attributeIt.next();
                if(element.getAttributeValue(new QName(attributeName)) != null){
                    elementValue = elementValue + attributeName + ":" + element.getAttributeValue(new QName(attributeName));
                    TreeNode elementNode = new TreeNode(elementValue);
                    nodes.addChild(elementNode);
                }

            }
        }
        if(nodes.getChildNodes() != null){
            root.addChild(nodes);
        }
    }
    public void addSchemaDataWithAttributesToTree(String xPath, String headingName, TreeNode root, OMElement scope,
                                                  List<String> attributes) throws Exception{
        List<OMAttribute> list = TreeNodeBuilderUtil.evaluateXPathToAttributes(xPath, scope);
        TreeNode nodes = new TreeNode(headingName);
        for(OMAttribute element:list){
            Iterator attributeIt = attributes.iterator();
            String elementValue = "";
            while(attributeIt.hasNext()){
                String attributeName = (String)attributeIt.next();
                if(element.getQName().equals(new QName(attributeName))){
                    elementValue = elementValue + attributeName + ":" + element.getAttributeValue();
                    TreeNode elementNode = new TreeNode(elementValue);
                    nodes.addChild(elementNode);
                }

            }
        }
        if(nodes.getChildNodes() != null){
            root.addChild(nodes);
        }
    }

    public void addSchemaDataToTreeWithHyperlink(String xPath, String headingName, TreeNode root,
                                                 OMElement element, boolean isLink)
            throws Exception{
        List<String> list = TreeNodeBuilderUtil.evaluateXPathToValues(xPath,
                element);
        TreeNode nodes = new TreeNode(headingName);
        for(String urls:list){
            String registryUrl;
            if (isLink) {
                registryUrl = TreeNodeBuilderUtil.calculateAbsolutePath(schemaPath, urls);
                registryUrl = "<a href='" + SchemaConstants.RESOURCE_JSP_PAGE + "?" +
                        SchemaConstants.PATH_REQ_PARAMETER + "=" +
                        registryUrl + "'>" + urls + "</a>";
            } else {
                registryUrl = urls;
            }
            TreeNode elementNode = new TreeNode(registryUrl);
            nodes.addChild(elementNode);
        }
        if(nodes.getChildNodes() != null){
            root.addChild(nodes);
        }
    }

    public void setActualSchemaPath(String actualSchemaPath) {
        this.actualSchemaPath = actualSchemaPath;
    }
}
