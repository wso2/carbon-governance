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

package org.wso2.carbon.governance.registry.extensions.aspects.utils;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.registry.extensions.beans.*;
import org.wso2.carbon.governance.registry.extensions.interfaces.CustomValidations;
import org.wso2.carbon.governance.registry.extensions.interfaces.Execution;
import org.wso2.carbon.registry.core.Resource;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.util.*;

public class Utils {
    private static final Log log = LogFactory.getLog(Utils.class);

    public static OMElement getHistoryInfoElement(String text){
        try {
            String template = "<info></info>";

            OMElement infoElement = AXIOMUtil.stringToOM(template);
            infoElement.setText(text);

            return infoElement;
        } catch (XMLStreamException e) {
            log.error("Unable to build the lifecycle history info element");
        }
        return null;
    }

    public static CustomValidations loadCustomValidators(String className, Map parameterMap) throws Exception {

        CustomValidations customValidations;
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            Class<?> customCodeClass = Class.forName(className, true, loader);
            customValidations = (CustomValidations) customCodeClass.newInstance();
            customValidations.init(parameterMap);

        }  catch (Exception e) {
            String msg = "Unable to load validations class";
            log.error(msg, e);
            throw new Exception(msg,e);
        }
        return customValidations;
    }
    public static Execution loadCustomExecutors(String className, Map parameterMap) throws Exception {

        Execution customExecutors;
        try {
            Class<?> customCodeClass = Utils.class.getClassLoader().loadClass(className);
            customExecutors = (Execution) customCodeClass.newInstance();
            customExecutors.init(parameterMap);

        } catch (Exception e) {
            String msg = "Unable to load executions class";
            log.error(msg, e);
            throw new Exception(msg,e);
        }
        return customExecutors;
    }

    public static PermissionsBean createPermissionBean(OMElement permChild) {
        PermissionsBean permBean = new PermissionsBean();
        permBean.setForEvent(permChild.getAttributeValue(new QName(LifecycleConstants.FOR_EVENT)));
        if (permChild.getAttributeValue(new QName("roles")) != null)
            permBean.setRoles(Arrays.asList(permChild.getAttributeValue(new QName("roles"))
                    .split(",")));
        return permBean;
    }
    
    public static ApprovalBean createApprovalBean(OMElement permChild) {
    	ApprovalBean approveBean = new ApprovalBean();
    	approveBean.setForEvent(permChild.getAttributeValue(new QName(LifecycleConstants.FOR_EVENT)));
        if (permChild.getAttributeValue(new QName("roles")) != null) {
        	String[] roles = permChild.getAttributeValue(new QName("roles")).split(",");
        	if (roles.length == 1 && roles[0].equals("")) {
        		roles = new String[0];
        	}
        	approveBean.setRoles(Arrays.asList(roles));
        }
        if (permChild.getAttributeValue(new QName("votes")) != null) {
        	approveBean.setVotes(Integer.parseInt(permChild.getAttributeValue(new QName("votes"))));
        }
        return approveBean;
    }

    public static CustomCodeBean createCustomCodeBean(OMElement customCodeChild,String type) throws Exception {
        CustomCodeBean customCodeBean = new CustomCodeBean();
        Map<String, String> paramNameValues = new HashMap<String, String>();

        Iterator parameters = customCodeChild.getChildElements();
        while (parameters.hasNext()) {
            // this loop is for the parameter name and values
            OMElement paramChild = (OMElement) parameters.next();
            
            if ((paramChild.getAttributeValue(new QName("value")))!=null) {
            	paramNameValues.put(paramChild.getAttributeValue(new QName(LifecycleConstants.NAME)),
                        paramChild.getAttributeValue(new QName("value")));
			} else {
				if (!(paramChild.getText()).equals("")) {
					paramNameValues.put(paramChild.getAttributeValue(new QName(LifecycleConstants.NAME)),
	                        paramChild.getText());
				} else {
					paramNameValues.put(paramChild.getAttributeValue(new QName(LifecycleConstants.NAME)),
	                        paramChild.getFirstElement().toString());
				}
			}
            
        }
        if (type.equals(LifecycleConstants.VALIDATION)) {
            customCodeBean.setClassObeject(loadCustomValidators(
                    customCodeChild.getAttributeValue(new QName("class")), paramNameValues));
        } else if(type.equals(LifecycleConstants.EXECUTION)) {
            customCodeBean.setClassObeject(loadCustomExecutors(
                    customCodeChild.getAttributeValue(new QName("class")), paramNameValues));
        }
        customCodeBean.setEventName(customCodeChild.getAttributeValue(new QName(LifecycleConstants.FOR_EVENT)));
        return customCodeBean;
    }

    public static void clearCheckItems(Resource resource, String aspectName){
        Properties properties = (Properties) resource.getProperties().clone();
        for (Object o : properties.keySet()) {
            String key = (String) o;
            if(key.startsWith(LifecycleConstants.REGISTRY_CUSTOM_LIFECYCLE_CHECKLIST_OPTION + aspectName)){
                resource.removeProperty(key);
            }
        }
    }
    
    public static void addCheckItems(Resource resource, List<CheckItemBean> currentStateCheckItems, String state, String aspectName){

        if (currentStateCheckItems != null) {
            int order = 0;
            for (CheckItemBean currentStateCheckItem : currentStateCheckItems) {
                List<PermissionsBean> permissions = currentStateCheckItem.getPermissionsBeans();

                List<String> allowedRoles = new ArrayList<String>();

                for (PermissionsBean permission : permissions) {
                    allowedRoles.addAll(permission.getRoles());
                }

                List<String> items = new ArrayList<String>();
                items.add("status:" + state);
                items.add("name:" + currentStateCheckItem.getName());
                items.add("value:false");
                items.add("order:" + order);

                String resourcePropertyNameForItem =
                        LifecycleConstants.REGISTRY_CUSTOM_LIFECYCLE_CHECKLIST_OPTION + aspectName + "." + order
                                + LifecycleConstants.ITEM;
                String resourcePropertyNameForItemPermission =
                        LifecycleConstants.REGISTRY_CUSTOM_LIFECYCLE_CHECKLIST_OPTION + aspectName + "." + order
                                + LifecycleConstants.ITEM_PERMISSION;

                resource.setProperty(resourcePropertyNameForItem, items);
                if(allowedRoles.isEmpty()){
                    resource.setProperty(resourcePropertyNameForItemPermission, resourcePropertyNameForItemPermission);
                }else{
                    resource.setProperty(resourcePropertyNameForItemPermission, allowedRoles);
                }

                order++;
            }
        }
    }   

    public static void addScripts(String state, Resource resource, List<ScriptBean> scriptList, String aspectName) {
        if (scriptList != null) {
            for (ScriptBean scriptBean : scriptList) {
                if (scriptBean.isConsole()) {
                    List<String> items = new ArrayList<String>();
                    items.add(scriptBean.getScript());
                    items.add(scriptBean.getFunctionName());

                    String resourcePropertyNameForScript =
                            LifecycleConstants.REGISTRY_CUSTOM_LIFECYCLE_CHECKLIST_JS_SCRIPT_CONSOLE + aspectName + "." + state
                                    + "." + scriptBean.getEventName();
                    resource.setProperty(resourcePropertyNameForScript, items);
                }
            }
        }
    }

    public static void addTransitionUI(Resource resource,Map<String,String> transitionUI, String aspectName){
        List<String> tobeRemoved = new ArrayList<String>();
        Properties properties = resource.getProperties();
        for (Object key : properties.keySet()) {
            if(key.toString().startsWith(LifecycleConstants.REGISTRY_CUSTOM_LIFECYCLE_CHECKLIST_TRANSITION_UI + aspectName)){
                tobeRemoved.add(key.toString());
            }
        }
        for (String key : tobeRemoved) {
            resource.removeProperty(key);
        }

        if (transitionUI != null) {
            for (Map.Entry<String, String> entry : transitionUI.entrySet()) {
                resource.setProperty(
                        LifecycleConstants.REGISTRY_CUSTOM_LIFECYCLE_CHECKLIST_TRANSITION_UI + aspectName + "." + entry.getKey()
                        ,entry.getValue());
            }
        }
    }

    public static boolean isTransitionAllowed(String[] roles, List<PermissionsBean> permissionsBeans, String eventName) {
        Set<String> permissionSet = new HashSet<String>(Arrays.asList(roles));
        if (permissionsBeans != null) {
            for (PermissionsBean permission : permissionsBeans) {
                if (permission.getForEvent().equals(eventName) && permission.getRoles() != null) {
                    List permRoles = permission.getRoles();
                    permissionSet.retainAll(permRoles);
                }
            }
        }
        return !permissionSet.isEmpty();
    }
    
    public static boolean isCheckItemClickAllowed(String[] roles, List<PermissionsBean> permissionsBeans) {
        Set<String> permissionSet = new HashSet<String>(Arrays.asList(roles));
        if (permissionsBeans != null) {
            for (PermissionsBean permission : permissionsBeans) {
                if (permission.getRoles() != null) {
                    List permRoles = permission.getRoles();
                    permissionSet.retainAll(permRoles);
                }
            }
        }
        return !permissionSet.isEmpty();
    }

    public static String getCheckItemName(List<String> propValues){
        String name = null;

        for (String propValue : propValues) {
            if(propValue.startsWith("name:")){
                name = propValue.split("name:")[1];
            }
        }

        return name;
    }

    public static Map<String,String> extractCheckItemValues(Map<String,String> parameterMap){
        Map<String,String> checkItems = new HashMap<String, String>();

        for (Map.Entry<String, String> entry : parameterMap.entrySet()) {
            if(entry.getKey().endsWith(LifecycleConstants.ITEM)){
                checkItems.put(entry.getKey(),entry.getValue());
            }
        }
        return checkItems;
    }
    
    public static Map<String,String> extractVotesValues(Map<String,String> parameterMap){
        Map<String,String> checkItems = new HashMap<String, String>();

        for (Map.Entry<String, String> entry : parameterMap.entrySet()) {
            if(entry.getKey().endsWith(LifecycleConstants.VOTE)){
                checkItems.put(entry.getKey(),entry.getValue());
            }
        }
        return checkItems;
    }

    public static void populateTransitionExecutors(String currentStateName, OMElement node,
                                                   Map<String,List<CustomCodeBean>> transitionExecution) throws Exception {
        if (!transitionExecution.containsKey(currentStateName)
                && (node.getAttributeValue(new QName(LifecycleConstants.NAME)).equals("transitionExecution"))) {
            List<CustomCodeBean> customCodeBeanList = new ArrayList<CustomCodeBean>();
            Iterator executorsIterator = node.getChildElements();
            while (executorsIterator.hasNext()) {
                OMElement executorChild = (OMElement) executorsIterator.next();
                customCodeBeanList.add(createCustomCodeBean(executorChild, LifecycleConstants.EXECUTION));
            }
            transitionExecution.put(currentStateName, customCodeBeanList);
        }
    }

    public static void populateTransitionUIs(String currentStateName, OMElement node,
                                             Map<String, Map<String,String>> transitionUIs) {
        //                    Adding the transition UIs
        if (!transitionUIs.containsKey(currentStateName)
                && (node.getAttributeValue(new QName(LifecycleConstants.NAME)).equals("transitionUI"))) {
            Map<String,String> uiEventMap = new HashMap<String, String>();
            Iterator uiIterator = node.getChildElements();

            while (uiIterator.hasNext()) {
                OMElement uiElement = (OMElement) uiIterator.next();
                uiEventMap.put(uiElement.getAttributeValue(new QName(LifecycleConstants.FOR_EVENT))
                        ,uiElement.getAttributeValue(new QName("href")));
            }
            transitionUIs.put(currentStateName, uiEventMap);
        }
    }

    public static void populateTransitionScripts(String currentStateName, OMElement node,
                                                 Map<String, List<ScriptBean>> scriptElements) {
        //                  Adding the script elements
        if (!scriptElements.containsKey(currentStateName)
                && (node.getAttributeValue(new QName(LifecycleConstants.NAME)).equals("transitionScripts"))) {
            List<ScriptBean> scriptBeans = new ArrayList<ScriptBean>();
            Iterator scriptIterator = node.getChildElements();

            while (scriptIterator.hasNext()) {
                OMElement script = (OMElement) scriptIterator.next();
                Iterator scriptChildIterator = script.getChildElements();
                while (scriptChildIterator.hasNext()) {
                    OMElement scriptChild = (OMElement) scriptChildIterator.next();
                    scriptBeans.add(new ScriptBean(scriptChild.getQName().getLocalPart().equals("console"),
                            scriptChild.getAttributeValue(new QName("function")),
                            script.getAttributeValue(new QName(LifecycleConstants.FOR_EVENT)),
                            scriptChild.getFirstElement().toString()));
                }
            }
            scriptElements.put(currentStateName, scriptBeans);
        }
    }

    public static void populateTransitionPermissions(String currentStateName, OMElement node,
                                                     Map<String, List<PermissionsBean>> transitionPermission) {
        //                  Adding the transition permissions
        if (!transitionPermission.containsKey(currentStateName)
                && (node.getAttributeValue(new QName(LifecycleConstants.NAME)).equals("transitionPermission"))) {
            List<PermissionsBean> permissionsBeanList = new ArrayList<PermissionsBean>();
            Iterator permissionIterator = node.getChildElements();
            while (permissionIterator.hasNext()) {
                OMElement permChild = (OMElement) permissionIterator.next();
                permissionsBeanList.add(createPermissionBean(permChild));
            }
            transitionPermission.put(currentStateName, permissionsBeanList);
        }
    }

    public static void populateTransitionValidations(String currentStateName, OMElement node,
                                                     Map<String, List<CustomCodeBean>> transitionValidations) throws Exception {
        //                  Adding the state validations
        if (!transitionValidations.containsKey(currentStateName)
                && (node.getAttributeValue(new QName(LifecycleConstants.NAME)).equals("transitionValidation"))) {
            List<CustomCodeBean> customCodeBeanList = new ArrayList<CustomCodeBean>();
            Iterator validationsIterator = node.getChildElements();
            while (validationsIterator.hasNext()) {
                OMElement validationChild = (OMElement) validationsIterator.next();
                customCodeBeanList.add(createCustomCodeBean(validationChild, LifecycleConstants.VALIDATION));
            }
            transitionValidations.put(currentStateName, customCodeBeanList);
        }
    }

    public static void populateCheckItems(String currentStateName, OMElement node,
                                          Map<String, List<CheckItemBean>> checkListItems) throws Exception {
        //                    adding the checkItems
        if (!checkListItems.containsKey(currentStateName)
                && (node.getAttributeValue(new QName(LifecycleConstants.NAME)).equals("checkItems"))) {

            List<CheckItemBean> checkItems = new ArrayList<CheckItemBean>();

            Iterator checkItemIterator = node.getChildElements();
            while (checkItemIterator.hasNext()) {
                CheckItemBean checkItemBean = new CheckItemBean();
                OMElement childElement = (OMElement) checkItemIterator.next();

                //setting the check item name
                checkItemBean.setName(childElement.getAttributeValue(new QName(LifecycleConstants.NAME)));

                //setting the transactionList
                if ((childElement.getAttributeValue(new QName(LifecycleConstants.FOR_EVENT))) != null) {
                    checkItemBean.setEvents(Arrays.asList((childElement
                            .getAttributeValue(new QName(LifecycleConstants.FOR_EVENT))).split(",")));
                }

                Iterator permissionElementIterator = childElement
                        .getChildrenWithName(new QName("permissions"));

                while (permissionElementIterator.hasNext()) {
                    OMElement permissionElement = (OMElement) permissionElementIterator.next();

                    Iterator permissions = permissionElement.getChildElements();
                    List<PermissionsBean> permBeanList = new ArrayList<PermissionsBean>();

                    while (permissions.hasNext()) {
                        OMElement permChild = (OMElement) permissions.next();
                        permBeanList.add(createPermissionBean(permChild));
                    }
                    checkItemBean.setPermissionsBeans(permBeanList);
                }

                Iterator validationsElementIterator = childElement
                        .getChildrenWithName(new QName("validations"));

                while (validationsElementIterator.hasNext()) {
//                          setting the validation bean
                    List<CustomCodeBean> customCodeBeanList = new ArrayList<CustomCodeBean>();
                    OMElement validationElement = (OMElement) validationsElementIterator.next();
                    Iterator validations = validationElement.getChildElements();

//                             this loop is to iterate the validation elements
                    while (validations.hasNext()) {
                        OMElement validationChild = (OMElement) validations.next();
                        customCodeBeanList.add(createCustomCodeBean(validationChild, LifecycleConstants.VALIDATION));
                    }
                    checkItemBean.setValidationBeans(customCodeBeanList);
                }
                checkItems.add(checkItemBean);
            }
            if (checkItems.size() > 0) {
                checkListItems.put(currentStateName, checkItems);
            }
        }
    }
    
	public static void populateTransitionApprovals(String currentStateName, OMElement node,
			Map<String, List<ApprovalBean>> transitionApproval) {
		// Adding the transition approval
		if (!transitionApproval.containsKey(currentStateName) 
				&& (node.getAttributeValue(new QName(LifecycleConstants.NAME)).equals("transitionApproval"))) {
			List<ApprovalBean> approvalBeanList = new ArrayList<ApprovalBean>();
			Iterator approvalIterator = node.getChildElements();
			while (approvalIterator.hasNext()) {
				OMElement approveChild = (OMElement) approvalIterator.next();
				approvalBeanList.add(createApprovalBean(approveChild));
			}
			transitionApproval.put(currentStateName, approvalBeanList);
		}
	}
	
	public static void addTransitionApprovalItems(Resource resource, List<ApprovalBean> approvalBeans, String state, String aspectName){
        if (approvalBeans != null) {
        	int order = 0;
            for (ApprovalBean approvalBean : approvalBeans) {
                
            	List<String> allowedRoles = new ArrayList<String>();
                allowedRoles.addAll(approvalBean.getRoles());               

                List<String> items = new ArrayList<String>();
                items.add("status:" + state);
                items.add("name:" + approvalBean.getForEvent());
                items.add("current:0");
                items.add("votes:" + approvalBean.getVotes());
                items.add("users:");
                items.add("order:" + order);

                String resourcePropertyNameForItem =
                        LifecycleConstants.REGISTRY_CUSTOM_LIFECYCLE_VOTES_OPTION + aspectName + "." + order
                                + LifecycleConstants.VOTE;
                String resourcePropertyNameForVotePermission =
                        LifecycleConstants.REGISTRY_CUSTOM_LIFECYCLE_VOTES_OPTION + aspectName + "." + order
                                + LifecycleConstants.VOTE_PERMISSION;

                resource.setProperty(resourcePropertyNameForItem, items);
                if (allowedRoles.isEmpty()) {
                    resource.setProperty(resourcePropertyNameForVotePermission, resourcePropertyNameForVotePermission);
                } else {
                    resource.setProperty(resourcePropertyNameForVotePermission, allowedRoles);
                }
                order++;
            }
        }
    }

    @SuppressWarnings("unused")
	public static boolean isTransitionApprovalAllowed(String[] roles, List<ApprovalBean> approvalBeans, String eventName, int currentVotes) {
        boolean approvalAllowed = false;
        if (approvalBeans != null) {
            for (ApprovalBean approvalBean : approvalBeans) {
                if (approvalBean.getForEvent().equals(eventName) && currentVotes > approvalBean.getVotes()) {
                	approvalAllowed = true;
                }
            }
        }
        return approvalAllowed;
    }
	
	public static void clearTransitionApprovals(Resource resource, String aspectName) {
        Properties properties = (Properties) resource.getProperties().clone();
        for (Object o : properties.keySet()) {
            String key = (String) o;
            if (key.startsWith(LifecycleConstants.REGISTRY_CUSTOM_LIFECYCLE_VOTES_OPTION + aspectName)) {
                resource.removeProperty(key);
            } else if (key.startsWith(LifecycleConstants.REGISTRY_CUSTOM_LIFECYCLE_USER_VOTE + aspectName)) {
                resource.removeProperty(key);
            }
        }
    }


}
