/*
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.governance.registry.extensions.aspects;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.llom.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.wso2.carbon.mashup.javascript.hostobjects.registry.CollectionHostObject;
import org.wso2.carbon.mashup.javascript.hostobjects.registry.RegistryHostObject;
import org.wso2.carbon.mashup.javascript.hostobjects.registry.ResourceHostObject;
import org.wso2.carbon.mashup.utils.MashupConstants;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.Aspect;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.session.CurrentSession;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.user.core.UserStoreException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class ChecklistLifeCycle extends Aspect {
    private static final Log log = LogFactory.getLog(ChecklistLifeCycle.class);

    public static final String PROMOTE = "promote";
    public static final String DEMOTE = "demote";
    public enum ConditionEnum { isNull, equals, contains, lessThan, greaterThan }

    static class Condition {
        public String property;
        public ConditionEnum condition;
        public String value;

        Condition(String property, String condition, String value) {
            this.property = property;
            this.condition = ConditionEnum.valueOf(condition);
            this.value = value;
        }

        public boolean isTrue(Resource resource) {
            String propVal = resource.getProperty(property);
            if (propVal == null) {
                return condition == ConditionEnum.isNull;
            }

            switch (condition) {
                case equals:
                    return propVal.equals(value);
                case contains:
                    return propVal.indexOf(value) > -1;
                case lessThan:
                    return Integer.parseInt(propVal) < Integer.parseInt(value);
                case greaterThan:
                    return Integer.parseInt(propVal) > Integer.parseInt(value);
                default:
                    return false;
            }
        }

        public String getDescription() {
            StringBuffer ret = new StringBuffer();
            ret.append("Property '");
            ret.append(property);
            ret.append("' ");
            switch (condition) {
                case isNull:
                    ret.append("must be null");
                    break;
                case equals:
                    ret.append("must equal '");
                    ret.append(value);
                    ret.append("'");
                    break;
                case contains:
                    ret.append("must contain '");
                    ret.append(value);
                    ret.append("'");
                    break;
                case lessThan:
                    ret.append("must be less than ");
                    ret.append(value);
                    break;
                case greaterThan:
                    ret.append("must be greater than ");
                    ret.append(value);
                    break;
            }
            return ret.toString();
        }
    }

    private List<String> states = new ArrayList<String>();
    private Map<String, List<Condition>> transitions = new HashMap<String, List<Condition>>();
    private String stateProperty = "registry.lifecycle.Checklist.state";
    private String lifecycleProperty = "registry.LC.name";
    boolean isConfigurationFromResource = false;
    boolean configurationFromResourceExtracted = false;
    String configurationResourcePath = "";
    OMElement configurationElement = null;
    String aspectName = "Checklist";

    // this is to keep the config elements to be retrieved in the gadget sources
    private List<OMElement> configElements = new ArrayList<OMElement>();

    public ChecklistLifeCycle() {
        // Lifecycle with no configuration gets the default set of states, with no conditions.
        states.add("Created");
        states.add("Tested");
        states.add("Deployed");
        states.add("Deprecated");
    }

    public ChecklistLifeCycle(OMElement config) throws RegistryException {
        configElements.add(config);
        String myName = config.getAttributeValue(new QName("name"));
        aspectName = myName;
        myName = myName.replaceAll("\\s", "");
        stateProperty = "registry.lifecycle." + myName + ".state";

        Iterator stateElements = config.getChildElements();
        while (stateElements.hasNext()) {
            OMElement stateEl = (OMElement)stateElements.next();

            /* expected format @ registry.xml
            <aspect name="Checklist" class="org.wso2.carbon.registry.extensions.aspects.ChecklistLifeCycle">
		        <configuration type="resource">/checklists/products</configuration>
	        </aspect>
             */
            if (stateEl.getAttribute(new QName("type")) != null) {
                String type = stateEl.getAttributeValue(new QName("type"));
                if (type.equalsIgnoreCase("resource")) {
                    isConfigurationFromResource = true;
                    configurationResourcePath = stateEl.getText();
                    states.clear();
                    transitions.clear();
                    break;
                }
                else if (type.equalsIgnoreCase("literal")) {
                    isConfigurationFromResource = false;
                    configurationElement = stateEl.getFirstElement();
                    states.clear();
                    transitions.clear();
                    break;
                }
            }

            String name = stateEl.getAttributeValue(new QName("name"));
            if (name == null) {
                throw new IllegalArgumentException("Must have a name attribute for each state");
            }
            states.add(name);
            List<Condition> conditions = null;
            Iterator conditionIterator = stateEl.getChildElements();
            while (conditionIterator.hasNext()) {
                OMElement conditionEl = (OMElement)conditionIterator.next();
                if (conditionEl.getQName().equals(new QName("condition"))) {
                    String property = conditionEl.getAttributeValue(new QName("property"));
                    String condition = conditionEl.getAttributeValue(new QName("condition"));
                    String value = conditionEl.getAttributeValue(new QName("value"));
                    Condition c = new Condition(property, condition, value);
                    if (conditions == null) conditions = new ArrayList<Condition>();
                    conditions.add(c);
                }
            }
            if (conditions != null) {
                transitions.put(name, conditions);
            }
        }
    }

    public void associate(Resource resource, Registry registry) throws RegistryException {
        try {
            String xmlContent = "";
            if (isConfigurationFromResource) {
                /* expected format of the content in the resource
                         <lifecycle>
                            <state name="Created">
                                <checkitem>Testing option 1</checkitem>
                                <checkitem>Testing option 2</checkitem>
                                <checkitem>Testing option 3</checkitem>
                                <checkitem>Testing option 4</checkitem>
                            </state>
                            <state name="Tested">
                                <checkitem>Deploying option 1</checkitem>
                                <checkitem>Deploying option 2</checkitem>
                                <checkitem>Deploying option 3</checkitem>
                                <checkitem>Deploying option 4</checkitem>
                                <checkitem>Deploying option 5</checkitem>
                            </state>
                            <state name="Deployed">
                                <checkitem>Deprecating option 1</checkitem>
                                <checkitem>Deprecating option 2</checkitem>
                                <checkitem>Deprecating option 3</checkitem>
                            </state>
                            <state name="Deprecated">
                            </state>
                        </lifecycle>
                 */
                Resource configurationResource = registry.get(configurationResourcePath);
                xmlContent = RegistryUtils.decodeBytes((byte[])configurationResource.getContent());
                configurationElement =  AXIOMUtil.stringToOM(xmlContent);
            }
            else {
                /* expected format in registry.xml
                *** opening aspect tag ***
                    <configuration type="literal">
                        <lifecycle>
                            <state name="Created">
                                <checkitem>Testing option 1</checkitem>
                                <checkitem>Testing option 2</checkitem>
                                <checkitem>Testing option 3</checkitem>
                                <checkitem>Testing option 4</checkitem>
                            </state>
                            <state name="Tested">
                                <checkitem>Deploying option 1</checkitem>
                                <checkitem>Deploying option 2</checkitem>
                                <checkitem>Deploying option 3</checkitem>
                                <checkitem>Deploying option 4</checkitem>
                                <checkitem>Deploying option 5</checkitem>
                            </state>
                            <state name="Deployed">
                                <checkitem>Deprecating option 1</checkitem>
                                <checkitem>Deprecating option 2</checkitem>
                                <checkitem>Deprecating option 3</checkitem>
                            </state>
                            <state name="Deprecated">
                            </state>
                        </lifecycle>
                    </configuration>
                *** closing aspect tag ***
                */

                // configuration element is populated inside the constructor
            }

            Iterator stateElements = configurationElement.getChildElements();
            int propertyOrder = 0;
            boolean addStates = (states.size() == 0);
            while (stateElements.hasNext()) {
                OMElement stateEl = (OMElement)stateElements.next();
                String name = stateEl.getAttributeValue(new QName("name"));
                if (name == null) {
                    throw new IllegalArgumentException("Must have a name attribute for each state");
                }
                if (addStates) {
                    states.add(name);
                }

                Iterator checkListIterator = stateEl.getChildElements();
                int checklistItemOrder = 0;
                while (checkListIterator.hasNext()) {
                    OMElement itemEl = (OMElement)checkListIterator.next();
                    if (itemEl.getQName().equals(new QName("checkitem"))) {
                        List<String> items = new ArrayList<String>();
                        String itemName = itemEl.getText();
                        if (itemName == null)
                            throw new RegistryException("Checklist items should have a name!");
                        items.add("status:" + name);
                        items.add("name:" + itemName);
                        items.add("value:false");

                        if (itemEl.getAttribute(new QName("order")) != null) {
                            items.add("order:" + itemEl.getAttributeValue(new QName("order")));
                        }
                        else {
                            items.add("order:" + checklistItemOrder);
                        }

                        String resourcePropertyNameForItem
                                = "registry.custom_lifecycle.checklist.option" + propertyOrder + ".item";

                        resource.setProperty(resourcePropertyNameForItem, items);
                        checklistItemOrder++;
                        propertyOrder++;
                    } else if (itemEl.getQName().equals(new QName("js"))) {
                        Iterator scriptElementIterator = itemEl.getChildElements();
                        while (scriptElementIterator.hasNext()) {
                            OMElement scriptItemEl = (OMElement)scriptElementIterator.next();
                            if (scriptItemEl.getQName().equals(new QName("console"))) {
                                StringBuffer lifecycleScript = new StringBuffer();
                                String lifecycleScriptCommand = "";
                                Iterator consoleScriptElementIterator = scriptItemEl.getChildElements();
                                while (consoleScriptElementIterator.hasNext()) {
                                    OMElement consoleScriptItemEl = (OMElement)consoleScriptElementIterator.next();
                                    if (consoleScriptItemEl.getQName().equals(new QName("script"))) {
                                        lifecycleScript.append(consoleScriptItemEl.toString()).append("\n");
                                    }
                                }
                                if (scriptItemEl.getAttribute(new QName("demoteFunction")) != null) {
                                    lifecycleScriptCommand =
                                            scriptItemEl.getAttributeValue(new QName("demoteFunction"));
                                    List<String> items = new ArrayList<String>();
                                    items.add(lifecycleScript.toString());
                                    items.add(lifecycleScriptCommand);
                                    String resourcePropertyNameForItem =
                                            "registry.custom_lifecycle.checklist.js.script.console." +
                                                    name + "." + DEMOTE;
                                    resource.setProperty(resourcePropertyNameForItem, items);
                                }
                                if (scriptItemEl.getAttribute(new QName("promoteFunction")) != null) {
                                    lifecycleScriptCommand =
                                            scriptItemEl.getAttributeValue(new QName("promoteFunction"));
                                    List<String> items = new ArrayList<String>();
                                    items.add(lifecycleScript.toString());
                                    items.add(lifecycleScriptCommand);
                                    String resourcePropertyNameForItem =
                                            "registry.custom_lifecycle.checklist.js.script.console." +
                                                    name + "." + PROMOTE;
                                    resource.setProperty(resourcePropertyNameForItem, items);
                                }
                            } else if (scriptItemEl.getQName().equals(new QName("server"))) {
                                String lifecycleScript = "";
                                String lifecycleScriptCommand = "";
                                Iterator serverScriptElementIterator = scriptItemEl.getChildElements();
                                if (serverScriptElementIterator.hasNext()) {
                                    OMElement serverScriptItemEl = (OMElement)serverScriptElementIterator.next();
                                    if (serverScriptItemEl.getQName().equals(new QName("script"))) {
                                        lifecycleScript += serverScriptItemEl.toString();
                                        lifecycleScript = lifecycleScript.trim();
                                    }
                                }
                                if (scriptItemEl.getAttribute(new QName("demoteFunction")) != null) {
                                    lifecycleScriptCommand =
                                            scriptItemEl.getAttributeValue(new QName("demoteFunction"));
                                    List<String> items = new ArrayList<String>();
                                    items.add(lifecycleScript);
                                    items.add(lifecycleScriptCommand);
                                    String resourcePropertyNameForItem =
                                            "registry.custom_lifecycle.checklist.js.script.server." +
                                                    name + "." + DEMOTE;
                                    resource.setProperty(resourcePropertyNameForItem, items);
                                }
                                if (scriptItemEl.getAttribute(new QName("promoteFunction")) != null) {
                                    lifecycleScriptCommand =
                                            scriptItemEl.getAttributeValue(new QName("promoteFunction"));
                                    List<String> items = new ArrayList<String>();
                                    items.add(lifecycleScript);
                                    items.add(lifecycleScriptCommand);
                                    String resourcePropertyNameForItem =
                                            "registry.custom_lifecycle.checklist.js.script.server." +
                                                    name + "." + PROMOTE;
                                    resource.setProperty(resourcePropertyNameForItem, items);
                                }
                            }
                        }
                    }
                }
            }
        }
        catch (XMLStreamException e) {
            throw new RegistryException("Resource does not contain valid XML configuration: " + e.toString());
        }

        resource.setProperty(stateProperty, states.get(0));
        resource.setProperty(lifecycleProperty, aspectName);
    }

    public void invoke(RequestContext context, String action) throws RegistryException {
        Resource resource = context.getResource();
        String currentState = resource.getProperty(stateProperty);
        int stateIndex = states.indexOf(currentState);
        if (stateIndex == -1) {
            throw new RegistryException("State '" + currentState + "' is not valid!");
        }

        String newState;
        if (PROMOTE.equals(action)) {
            if (stateIndex == states.size() - 1) {
                throw new RegistryException("Can't promote beyond end of configured lifecycle!");
            }

            // Make sure all conditions are met
            List<Condition> conditions = transitions.get(currentState);
            if (conditions != null) {
                for (Condition condition : conditions) {
                    if (!condition.isTrue(resource)) {
                        throw new RegistryException(
                                "Condition failed - " + condition.getDescription());
                    }
                }
            }
            newState = states.get(stateIndex + 1);
        } else if (DEMOTE.equals(action)) {
            if (stateIndex == 0) {
                throw new RegistryException("Can't demote beyond start of configured lifecycle!");
            }
            newState = states.get(stateIndex - 1);
        }
        else {
          return;
        }
//        newState = states.get(stateIndex);
        resource.setProperty(stateProperty, newState);
        context.getRegistry().put(resource.getPath(), resource);
        Properties properties = resource.getProperties();
        String lifecycleScript = "";
        String lifecycleScriptCommand = "";
        for (Map.Entry<Object, Object> e : properties.entrySet()) {
            if (e.getKey() instanceof String) {
                String propName = (String) e.getKey();
                String prefix = "registry.custom_lifecycle.";
                String suffix = "js.script.server." + currentState + "." + action;
                if (propName.startsWith(prefix) && propName.endsWith(suffix)) {
                    Object obj = e.getValue();
                    if (obj != null && obj instanceof List) {
                        List propValues = (List)obj;
                        if (propValues.size() == 2) {
                            if (((String)propValues.get(0)).contains("function ")) {
                                lifecycleScript = (String)propValues.get(0);
                                lifecycleScriptCommand = (String)propValues.get(1) + "()";
                            } else {
                                lifecycleScript = (String)propValues.get(1);
                                lifecycleScriptCommand = (String)propValues.get(0) + "()";
                            }
                            break;
                        }
                    }
                }
            }
        }
        String executableCommand = lifecycleScript + "\n" + lifecycleScriptCommand;
        if (executableCommand.equals("\n")) {
            return;
        }
        executeJS(executableCommand);
	}

    private void executeJS(String script) {
        Context cx = Context.enter();
        try {
            ConfigurationContext configurationContext =
                    MessageContext.getCurrentMessageContext().getConfigurationContext();
            cx.putThreadLocal(MashupConstants.AXIS2_CONFIGURATION_CONTEXT, configurationContext);
            AxisService service = new AxisService();
            service.addParameter(MashupConstants.MASHUP_AUTHOR, CurrentSession.getUser());
            cx.putThreadLocal(MashupConstants.AXIS2_SERVICE, service);
            Scriptable scope = cx.initStandardObjects();
            ScriptableObject.defineClass(scope, ResourceHostObject.class);
            ScriptableObject.defineClass(scope, CollectionHostObject.class);
            ScriptableObject.defineClass(scope, RegistryHostObject.class);
            Object result = cx.evaluateString(scope, script, "<cmd>", 1, null);
            if (result != null && log.isInfoEnabled()) {
                log.debug("JavaScript Result: " + Context.toString(result));
            }
        } catch (IllegalAccessException e) {
            log.error("Unable to defining registry host objects.", e);
        } catch (InstantiationException e) {
            log.error("Unable to instantiate the given registry host object.", e);
        } catch (InvocationTargetException e) {
            log.error("An exception occurred while creating registry host objects.", e);
        } catch (AxisFault e) {
            log.error("Failed to set user name parameter.", e);
        } finally {
            Context.exit();
        }
    }

    public String[] getAvailableActions(RequestContext context) {
        try {
            if (states.size() == 0) {
                Registry registry = context.getRegistry();
                String xmlContent = "";
                if (isConfigurationFromResource) {
                    Resource configurationResource = registry.get(configurationResourcePath);
                    xmlContent = RegistryUtils.decodeBytes((byte[])configurationResource.getContent());
                    configurationElement =  AXIOMUtil.stringToOM(xmlContent);
                }

                Iterator stateElements = configurationElement.getChildElements();
                while (stateElements.hasNext()) {
                    OMElement stateEl = (OMElement)stateElements.next();
                    String name = stateEl.getAttributeValue(new QName("name"));
                    if (name == null) {
                        throw new IllegalArgumentException("Must have a name attribute for each state");
                    }

                    states.add(name);
                }
            }
        }
        catch (Exception e) {
            throw new RuntimeException("Resource does not contain valid XML configuration: " + e.toString());
        }

        ArrayList<String> actions = new ArrayList<String>();
        Resource resource = context.getResource();
        String currentState = resource.getProperty(stateProperty);

        Properties props = resource.getProperties();
        boolean allItemsAreChecked = true;
        for (Map.Entry<Object, Object> e : props.entrySet()) {
            if (((String) e.getKey()).startsWith("registry.custom_lifecycle.checklist.")) {
                List<String> propValues = (List<String>) e.getValue();
                String[] propertyValues = propValues.toArray(new String[propValues.size()]);
                String itemLifeCycleState = null;
                String itemValue = null;

                if (propertyValues != null) {
                      for (int index = 0; index < propertyValues.length; index++) {
                        String item = propertyValues[index];
                        if ((itemLifeCycleState == null) && (item.startsWith("status:"))) {
                            itemLifeCycleState = item.substring(7);
                        }
                        if ((itemValue == null) && (item.startsWith("value:"))) {
                            itemValue = item.substring(6);
                        }
                    }
                }

                if ((itemLifeCycleState != null) && (itemValue != null)) {
                    if (itemLifeCycleState.equalsIgnoreCase(currentState)) {
                        if (itemValue.equalsIgnoreCase("false")) {
                            allItemsAreChecked = false;
                            break;
                        }
                    }
                }
            }
        }

        boolean isDeleteAllowed = false;

        try {
            if (CurrentSession.getUserRealm().getAuthorizationManager().isUserAuthorized(
                    CurrentSession.getUser(), context.getResourcePath().getPath(),
                    ActionConstants.DELETE)) {
                isDeleteAllowed = true;
            }
        } catch (UserStoreException ignored) {}

        int stateIndex = states.indexOf(currentState);
        if (stateIndex > -1 && (stateIndex < states.size() - 1) && isDeleteAllowed) {
            if (allItemsAreChecked)
                actions.add(PROMOTE);
        }
        if (stateIndex > 0 && isDeleteAllowed) {
            actions.add(DEMOTE);
        }
        return actions.toArray(new String[actions.size()]);
    }

    public void dissociate(RequestContext context) {
        Resource resource = context.getResource();
        if (resource != null) {
            resource.removeProperty(stateProperty);
            resource.removeProperty(lifecycleProperty);
        }
    }

    public String getCurrentState(Resource resource) {
        return resource.getProperty(stateProperty);
    }

    public List<OMElement> getConfigElements() {
        return configElements;
    }
}
