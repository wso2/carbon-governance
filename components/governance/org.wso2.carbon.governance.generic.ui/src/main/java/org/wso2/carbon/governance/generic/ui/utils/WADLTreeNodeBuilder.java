package org.wso2.carbon.governance.generic.ui.utils;

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.registry.common.ui.utils.TreeNode;
import org.wso2.carbon.registry.common.ui.utils.TreeNodeBuilderUtil;
import org.wso2.carbon.registry.common.ui.utils.UIUtil;
import org.wso2.carbon.registry.core.utils.RegistryUtils;

import javax.xml.namespace.QName;
import java.util.Iterator;

public class WADLTreeNodeBuilder {

    private OMElement wadlElement;
    private String wadlPath;
    private String wadlNamespace;

    public WADLTreeNodeBuilder(String wadlPath, String wadlContent) throws Exception {
        wadlElement = UIUtil.buildOMElement(wadlContent);
        this.wadlPath = wadlPath;
    }

    public TreeNode buildTree() throws Exception {
        // prepare the root tree node.
        String wadlName = RegistryUtils.getResourceName(wadlPath);
        wadlNamespace = wadlElement.getNamespace().getNamespaceURI();
        TreeNode root = new TreeNode(TreeNodeBuilderUtil.generateKeyName("WADL", wadlName));


        OMElement grammarsElement = wadlElement.getFirstChildWithName(new QName(wadlNamespace, "grammars"));
        if(grammarsElement != null){
            TreeNode grammarsNode = new TreeNode("Grammars");
            Iterator<OMElement> grammarElements = grammarsElement.
                    getChildrenWithName(new QName(wadlNamespace, "include"));
            while (grammarElements.hasNext()){
                grammarsNode.addChild(new TreeNode("Include",
                                grammarElements.next().getAttributeValue(new QName("href"))));
            }
            root.addChild(grammarsNode) ;
        }

        TreeNode resourcesNode = new TreeNode("Resources");
        OMElement resourcesElement = wadlElement.
                getFirstChildWithName(new QName(wadlNamespace, "resources"));
        resourcesNode.addChild(new TreeNode("Base",
                resourcesElement.getAttributeValue(new QName("base"))));
        Iterator<OMElement> resourceElements = resourcesElement.
                getChildrenWithName(new QName(wadlNamespace, "resource"));
        while (resourceElements.hasNext()){
            addResourceNodesRecursively(resourceElements.next(), resourcesNode);
        }
        root.addChild(resourcesNode);

        return root;
    }

    private void addResourceNodesRecursively(OMElement resourceElement, TreeNode parentNode){
        String path = resourceElement.getAttributeValue(new QName("path"));
        TreeNode resourceNode = new TreeNode(TreeNodeBuilderUtil.generateKeyName("Resource", path));

        Iterator<OMElement> methodElements = resourceElement.
                getChildrenWithName(new QName(wadlNamespace, "method"));
        while (methodElements.hasNext()){
            addMethodNode(methodElements.next(), resourceNode);
        }

        Iterator<OMElement> childResourceElements = resourceElement.
                getChildrenWithName(new QName(wadlNamespace, "resource"));
        while (childResourceElements.hasNext()){
            addResourceNodesRecursively(childResourceElements.next(), resourceNode);
        }

        parentNode.addChild(resourceNode);
    }

    private void addMethodNode(OMElement methodElement, TreeNode resourceNode){
        String methodName = methodElement.getAttributeValue(new QName("name"));
        TreeNode methodNode;

        OMElement requestElement = methodElement.
                getFirstChildWithName(new QName(wadlNamespace, "request"));
        Iterator<OMElement> responseElements = methodElement.
                getChildrenWithName(new QName(wadlNamespace, "response"));

        if(requestElement != null || responseElements.hasNext()){
            methodNode = new TreeNode(TreeNodeBuilderUtil.generateKeyName("Method", methodName));
        } else {
            methodNode = new TreeNode("Method", methodName);
        }

        if(requestElement != null){
            addRequestNode(requestElement, methodNode);
        }

        while (responseElements.hasNext()){
            addResponseNode(responseElements.next(), methodNode);
        }

        resourceNode.addChild(methodNode);
    }

    private void addRequestNode(OMElement requestElement, TreeNode methodNode){
        TreeNode requestNode = new TreeNode("Request");
        Iterator<OMElement> paramElements = requestElement.getChildrenWithName(new QName(wadlNamespace, "param"));
        while (paramElements.hasNext()){
            OMElement paramElement = paramElements.next();
            String paramName = paramElement.getAttributeValue(new QName("name"));
            TreeNode paramNode = new TreeNode(TreeNodeBuilderUtil.
                    generateKeyName("Parameter", paramName));

            String paramType = paramElement.getAttributeValue(new QName("type"));
            if(paramType != null){
                paramNode.addChild(new TreeNode("Type", paramType));
            }

            String paramStyle = paramElement.getAttributeValue(new QName("style"));
            if(paramStyle != null){
                paramNode.addChild(new TreeNode("Style", paramStyle));
            }

            requestNode.addChild(paramNode);
        }

        methodNode.addChild(requestNode);
    }

    private void addResponseNode(OMElement responseElement, TreeNode methodNode){
        TreeNode responseNode = new TreeNode("Response");
        OMElement representationElement = responseElement.
                getFirstChildWithName(new QName(wadlNamespace, "representation"));
        if(representationElement != null){
            responseNode.addChild(new TreeNode("Mediatype",
                    representationElement.getAttributeValue(new QName("mediaType"))));
        }

        methodNode.addChild(responseNode);
    }
}
