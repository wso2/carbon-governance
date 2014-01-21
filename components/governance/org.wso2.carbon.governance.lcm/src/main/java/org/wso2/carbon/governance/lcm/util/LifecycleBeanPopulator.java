/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.governance.lcm.util;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.scxml.io.SCXMLParser;
import org.wso2.carbon.governance.lcm.beans.*;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.xml.sax.InputSource;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.CharArrayReader;
import java.util.Iterator;

public class LifecycleBeanPopulator {

    private static final Log log = LogFactory.getLog(LifecycleBeanPopulator.class);

    public static String serializeLifecycleBean(LifecycleBean bean) throws XMLStreamException {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement aspect = factory.createOMElement("aspect", null);
        aspect.addAttribute(factory.createOMAttribute("name", null, bean.getName()));
        aspect.addAttribute(factory.createOMAttribute("class", null, bean.getReflectionClassName()));
        OMElement configuration = factory.createOMElement("configuration", null);
        configuration.addAttribute(factory.createOMAttribute("type", null, "literal"));
        OMElement lifecycle = factory.createOMElement("lifecycle", null);

        LifecycleStateBean[] states = bean.getStates();
        for(LifecycleStateBean lifecycleState:states) {
            OMElement state = factory.createOMElement("state", null);
            state.addAttribute(factory.createOMAttribute("name", null, lifecycleState.getStateName()));
            if (lifecycleState.getLocation() != null)
                state.addAttribute(factory.createOMAttribute("location", null, lifecycleState.getLocation()));

            ChecklistBean checklistBean = lifecycleState.getChecklist();
            if (checklistBean != null) {
                String[] items = checklistBean.getItems();
                for (String item:items) {
                    OMElement itemElement = factory.createOMElement("checkitem", null);
                    itemElement.setText(item);
                    state.addChild(itemElement);
                }
            }
            PermissionBean[] permissionBeans = lifecycleState.getPermissions();
            if (permissionBeans != null && permissionBeans.length > 0) {
                OMElement permissionsElement = factory.createOMElement("permissions", null);
                for (PermissionBean permissionBean: permissionBeans) {
                    OMElement permissionElement = factory.createOMElement("permission", null);
                    permissionElement.addAttribute(factory.createOMAttribute("action", null, permissionBean.getAction()));
//                    permissionElement.addAttribute(factory.createOMAttribute("roles", null, permissionBean.getRoles()));
                    permissionsElement.addChild(permissionElement);
                }
                state.addChild(permissionsElement);
            }
            JSBean js = lifecycleState.getJs();
            if (js != null) {
                OMElement jsElement = factory.createOMElement("js", null);
                ScriptBean consoleScriptBean = js.getConsoleScript();
                ScriptFunctionBean[] consoleScriptFunctionBeans = js.getConsoleFunctions();
                if (consoleScriptBean != null) {
                    OMElement consoleElement = factory.createOMElement("console", null);
                    ScriptElementBean[] scripts = consoleScriptBean.getScripts();
                    if (scripts != null && scripts.length > 0) {
                        for (ScriptElementBean script : scripts) {
                            OMElement scriptElement = factory.createOMElement("script", null);
                            if (script.getSrc() != null) {
                                scriptElement.addAttribute(factory.createOMAttribute("src",
                                        null, script.getSrc()));
                            }
                            scriptElement.addAttribute(factory.createOMAttribute("type",
                                        null, "text/javascript")); 
                            scriptElement.setText(script.getContent());
                            consoleElement.addChild(scriptElement);
                        }
                    }
                    if (consoleScriptFunctionBeans != null && consoleScriptFunctionBeans.length > 0) {
                        for (ScriptFunctionBean consoleScriptFunctionBean : consoleScriptFunctionBeans) {
                            consoleElement.addAttribute(factory.createOMAttribute(
                                    consoleScriptFunctionBean.getName(), null,
                                    consoleScriptFunctionBean.getValue()));
                        }
                    }
                    jsElement.addChild(consoleElement);
                }
                ScriptBean serverScriptBean = js.getServerScript();
                ScriptFunctionBean[] serverScriptFunctionBeans = js.getServerFunctions();
                if (serverScriptBean != null) {
                    OMElement serverElement = factory.createOMElement("server", null);
                    ScriptElementBean[] scripts = serverScriptBean.getScripts();
                    if (scripts != null && scripts.length > 0) {
                        for (ScriptElementBean script : scripts) {
                            OMElement scriptElement = factory.createOMElement("script", null);
                            if (script.getSrc() != null) {
                                scriptElement.addAttribute(factory.createOMAttribute("src",
                                        null, script.getSrc()));
                            }
                            scriptElement.addAttribute(factory.createOMAttribute("type",
                                        null, "text/javascript"));
                            scriptElement.setText(script.getContent());
                            serverElement.addChild(scriptElement);
                        }
                    }
                    if (serverScriptFunctionBeans != null && serverScriptFunctionBeans.length > 0) {
                        for (ScriptFunctionBean serverScriptFunctionBean : serverScriptFunctionBeans) {
                            serverElement.addAttribute(factory.createOMAttribute(
                                    serverScriptFunctionBean.getName(), null,
                                    serverScriptFunctionBean.getValue()));
                        }
                    }
                    jsElement.addChild(serverElement);
                }
                state.addChild(jsElement);
            }
            String extensionsXmlString = lifecycleState.getExtensionsXml();
            if (extensionsXmlString != null && extensionsXmlString.length() > 0) {
                OMElement extensionsElement = factory.createOMElement("extensions", null);
                OMElement dummyElement = AXIOMUtil.stringToOM("<dummy>" + extensionsXmlString + "</dummy>");
                Iterator dummyElementItr = dummyElement.getChildElements();
                while (dummyElementItr.hasNext()) {
                    OMElement elem = (OMElement)dummyElementItr.next();
                    if (elem != null) {
                        extensionsElement.addChild(elem);
                    }
                }
                state.addChild(extensionsElement);
            }
            lifecycle.addChild(state);
        }
        configuration.addChild(lifecycle);
        aspect.addChild(configuration);
        
        return aspect.toString();
    }

    public static boolean deserializeLifecycleBean(String configuration,Registry registry) throws Exception {
       OMElement configurationElement = CommonUtil.buildOMElement(configuration);
       return deserializeLifecycleBean(configurationElement,registry);
    }

    public static boolean deserializeLifecycleBean(OMElement configurationElement,Registry registry) throws Exception{
        CommonUtil.validateOMContent(configurationElement,
                CommonUtil.getLifecycleSchemaValidator(CommonUtil.getLifecycleSchemaLocation()));

        try {
            OMElement scxmlElement = null;
            OMElement lifecycleElement = null;
            OMElement configuration = configurationElement.getFirstElement();
            String type = configuration.getAttributeValue(new QName("type"));

            if(type.equals("literal")){
                lifecycleElement = configuration.getFirstElement();
            }else if(type.equals("resource")){
                String resourcePath = configuration.getText();
                if(registry.resourceExists(resourcePath)){
                    Resource resource = registry.get(resourcePath);
                    if (resource.getContent() != null) {
                        if(resource.getContent() instanceof String){
                            lifecycleElement = CommonUtil.buildOMElement((String) resource.getContent());
                        }else if(resource.getContent() instanceof byte[]){
                            lifecycleElement = CommonUtil.buildOMElement(RegistryUtils.decodeBytes((byte[]) resource.getContent()));
                        }else{
                            String msg = "Could not find valid lifecycle configuration";
                            log.error(msg);
                            throw new RegistryException(msg);
                        }
                    }else {
                        String msg = "Resource does not contain a valid lifecycle configuration";
                        log.error(msg);
                        throw new RegistryException(msg);
                    }
                }else{
                    String msg = "Resource not found at " + resourcePath;
                    log.error(msg);
                    throw new RegistryException(msg);
                }
            } else {
                String msg = "The type must be either literal or resource";
                log.error(msg);
                throw new RegistryException(msg);
            }
            scxmlElement = lifecycleElement.getFirstElement();

            CommonUtil.validateOMContent(scxmlElement);
            CommonUtil.validateLifeCycle(scxmlElement);

//            Validating whether this complies to the scxml specification.
            SCXMLParser.parse(new InputSource(
                    new CharArrayReader((scxmlElement.toString()).toCharArray())), null);

//            Validating whether the data model is correct
            if(!CommonUtil.validateSCXMLDataModel(scxmlElement)){
                throw new RegistryException("Failed to validate the data model. Invalid forEvent found");
            }

        } catch (RegistryException e) {
            throw e;
        } catch (Exception e) {
            String msg =e.getMessage() + ". " + "Please check whether there are any whitespaces in state names";
            log.error(msg,e);
            throw new RegistryException(msg);
        }
        return true;
    }
//    Seems like we don't need this method. The scxml parser does the parsing
    /*private static boolean checkWhiteSpacesInConfig(OMElement scxmlContent){
        try {
            String xpathExpression = "//pre:transition";

            AXIOMXPath xpath = new AXIOMXPath(xpathExpression);
            xpath.addNamespace("pre",scxmlContent.getNamespace().getNamespaceURI());
            List resultNodes = xpath.selectNodes(scxmlContent);

            if (resultNodes != null && resultNodes.size() > 0) {
                for (Object resultNode : resultNodes) {
                    OMElement targetElement = (OMElement) resultNode;
                    String targetName = targetElement.getAttributeValue(new QName("target"));
                    if(targetName.trim().contains(" ")){
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            return false;
        }
        return true;
    }*/
}
