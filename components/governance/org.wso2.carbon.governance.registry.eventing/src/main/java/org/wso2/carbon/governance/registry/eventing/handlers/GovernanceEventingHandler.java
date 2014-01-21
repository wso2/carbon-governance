/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.governance.registry.eventing.handlers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.api.util.GovernanceConstants;
import org.wso2.carbon.governance.registry.eventing.handlers.utils.events.CheckListItemCheckedEvent;
import org.wso2.carbon.governance.registry.eventing.handlers.utils.events.CheckListItemUncheckedEvent;
import org.wso2.carbon.governance.registry.eventing.handlers.utils.events.LifeCycleApprovalNeededEvent;
import org.wso2.carbon.governance.registry.eventing.handlers.utils.events.LifeCycleApprovalWithdrawnEvent;
import org.wso2.carbon.governance.registry.eventing.handlers.utils.events.LifeCycleApprovedEvent;
import org.wso2.carbon.governance.registry.eventing.handlers.utils.events.LifeCycleCreatedEvent;
import org.wso2.carbon.governance.registry.eventing.handlers.utils.events.LifeCycleDeletedEvent;
import org.wso2.carbon.governance.registry.eventing.handlers.utils.events.LifeCycleStateChangedEvent;
import org.wso2.carbon.governance.registry.eventing.internal.Utils;
import org.wso2.carbon.registry.common.eventing.RegistryEvent;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.session.CurrentSession;
import org.wso2.carbon.registry.core.utils.RegistryUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class GovernanceEventingHandler extends Handler {
    private static final Log log = LogFactory.getLog(GovernanceEventingHandler.class);

    public void init(String defaultNotificationEndpoint) {
        Utils.setDefaultNotificationServiceURL(defaultNotificationEndpoint);
    }

    public GovernanceEventingHandler() {
        try {
            Utils.getRegistryNotificationService().registerEventType("checklist.item.checked", CheckListItemCheckedEvent.EVENT_NAME, CheckListItemCheckedEvent.EVENT_NAME);
            /*Utils.getRegistryNotificationService().registerEventTypeExclusion("checklist.item.checked", "/");
            Utils.getRegistryNotificationService().registerEventTypeExclusion("checklist.item.checked", "/system");
            Utils.getRegistryNotificationService().registerEventTypeExclusion("checklist.item.checked", "/system/.*");*/
            Utils.getRegistryNotificationService().registerEventType("checklist.item.unchecked", CheckListItemUncheckedEvent.EVENT_NAME, CheckListItemUncheckedEvent.EVENT_NAME);
            /*Utils.getRegistryNotificationService().registerEventTypeExclusion("checklist.item.unchecked", "/");
            Utils.getRegistryNotificationService().registerEventTypeExclusion("checklist.item.unchecked", "/system");
            Utils.getRegistryNotificationService().registerEventTypeExclusion("checklist.item.unchecked", "/system/.*");*/
            Utils.getRegistryNotificationService().registerEventType("lifecycle.state.changed", LifeCycleStateChangedEvent.EVENT_NAME, LifeCycleStateChangedEvent.EVENT_NAME);
            /*Utils.getRegistryNotificationService().registerEventTypeExclusion("lifecycle.state.changed", "/");
            Utils.getRegistryNotificationService().registerEventTypeExclusion("lifecycle.state.changed", "/system");
            Utils.getRegistryNotificationService().registerEventTypeExclusion("lifecycle.state.changed", "/system/.*");*/
            Utils.getRegistryNotificationService().registerEventType("lifecycle.deleted", LifeCycleDeletedEvent.EVENT_NAME, LifeCycleDeletedEvent.EVENT_NAME);
            /*Utils.getRegistryNotificationService().registerEventTypeExclusion("lifecycle.deleted", "/");
            Utils.getRegistryNotificationService().registerEventTypeExclusion("lifecycle.deleted", "/system");
            Utils.getRegistryNotificationService().registerEventTypeExclusion("lifecycle.deleted", "/system/.*");*/
            Utils.getRegistryNotificationService().registerEventType("lifecycle.created", LifeCycleCreatedEvent.EVENT_NAME, LifeCycleCreatedEvent.EVENT_NAME);
            /*Utils.getRegistryNotificationService().registerEventTypeExclusion("lifecycle.created", "/");
            Utils.getRegistryNotificationService().registerEventTypeExclusion("lifecycle.created", "/system");
            Utils.getRegistryNotificationService().registerEventTypeExclusion("lifecycle.created", "/system/.*");*/
            Utils.getRegistryNotificationService().registerEventType("lifecycle.approved", LifeCycleApprovedEvent.EVENT_NAME, LifeCycleApprovedEvent.EVENT_NAME);
            Utils.getRegistryNotificationService().registerEventType("lifecycle.approval.need", LifeCycleApprovalNeededEvent.EVENT_NAME, LifeCycleApprovalNeededEvent.EVENT_NAME);
            Utils.getRegistryNotificationService().registerEventType("lifecycle.approval.withdrawn", LifeCycleApprovalWithdrawnEvent.EVENT_NAME, LifeCycleApprovalWithdrawnEvent.EVENT_NAME);
        } catch (Exception e) {
            handleException("Unable to register Event Types", e);
        }
    }

    public void put(RequestContext requestContext) throws RegistryException {
        String path = requestContext.getResourcePath().getPath();
        String relativePath = RegistryUtils.getRelativePath(requestContext.getRegistryContext(),
                path);
        Resource oldResource = requestContext.getOldResource();
        Resource newResource = requestContext.getResource();
        if (oldResource == null || newResource == null ||
            oldResource.getProperties() == null || newResource.getProperties() == null) {
            return;
        }
        Properties props = oldResource.getProperties();
        Properties newProps = newResource.getProperties();
        String lcName = newResource.getProperty("registry.LC.name");
        String oldLcName = oldResource.getProperty("registry.LC.name");
        if (lcName == null && oldLcName != null) {
            RegistryEvent<String> event = new LifeCycleDeletedEvent<String>(
                    "[" + oldLcName + "] The LifeCycle was deleted for resource at "+relativePath+".");
            ((LifeCycleDeletedEvent)event).setResourcePath(relativePath);
            event.setParameter("LifecycleName", oldLcName);
            event.setTenantId(CurrentSession.getCallerTenantId());
            try {
                notify(event, requestContext.getRegistry(), relativePath);
            } catch (Exception e) {
                handleException("Unable to send notification for Put Operation", e);
            }
            return;
        } else if (lcName != null && oldLcName == null) {
            RegistryEvent<String> event = new LifeCycleCreatedEvent<String>(
                    "[" + lcName + "] The LifeCycle was created for resource at "+relativePath+".");
            ((LifeCycleCreatedEvent)event).setResourcePath(relativePath);
            event.setParameter("LifecycleName", lcName);
            event.setTenantId(CurrentSession.getCallerTenantId());
            try {
                notify(event, requestContext.getRegistry(), relativePath);
            } catch (Exception e) {
                handleException("Unable to send notification for Put Operation", e);
            }
            // Below notification send when lifecycle assigned assert and at least one approval doesn't required 
            // Preconditioned checklistitem check. 
            if(sendInitialNotification(requestContext, relativePath)){
            	RegistryEvent<String> approvalEvent = new LifeCycleApprovalNeededEvent<String>(
                        "[" + lcName + "] The LifeCycle was created and some transitions are awating for approval, resource locate at "+ relativePath +".");
           				((LifeCycleApprovalNeededEvent)approvalEvent).setResourcePath(relativePath);
           		approvalEvent.setParameter("LifecycleName", lcName);
           		approvalEvent.setTenantId(CurrentSession.getCallerTenantId());
           		try {
           			notify(approvalEvent, requestContext.getRegistry(), relativePath);
                } catch (Exception e) {
                	handleException("Unable to send notification for Put Operation", e);
                }
           		return;
            }                  
            return;
        }
        
        for (Map.Entry<Object, Object> e : props.entrySet()) {
            String propKey = (String) e.getKey();
            if (propKey.matches("registry\\p{Punct}.*\\p{Punct}checklist\\p{Punct}.*")) {
                List<String> propValues = (List<String>) e.getValue();
                List<String> newPropValues = (List<String>) newProps.get(propKey);
                if ((propValues == null) || (newPropValues == null))
                    continue;
                if ((propValues.size() > 2) && (newPropValues.size() > 2)) {
                    String oldName = null;
                    String oldValue = null;
                    String oldLifeCycleState = null;
                    String newName = null;
                    String newValue = null;
                    String newLifeCycleState = null;                   
                    for (String param : propValues) {
                        if (param.startsWith("status:")) {
                            oldLifeCycleState = param.substring(7);
                        } else if (param.startsWith("name:")) {
                            oldName = param.substring(5);
                        } else if (param.startsWith("value:")) {
                            oldValue = param.substring(6);
                        }
                    }
                    for (String param : newPropValues) {
                        if (param.startsWith("status:")) {
                            newLifeCycleState = param.substring(7);
                        } else if (param.startsWith("name:")) {
                            newName = param.substring(5);
                        } else if (param.startsWith("value:")) {
                            newValue = param.substring(6);
                        }
                    }
                    if (oldName.equalsIgnoreCase(newName) &&
                        oldLifeCycleState.equalsIgnoreCase(newLifeCycleState) &&
                        !oldValue.equalsIgnoreCase(newValue)) {
                        RegistryEvent<String> event = null;
                        if (oldValue.equals(Boolean.toString(Boolean.TRUE)) ||
                            oldValue.equals(Boolean.toString(Boolean.FALSE))) {                            
                            if (oldValue.equals(Boolean.toString(Boolean.TRUE))) {
                                event = new CheckListItemUncheckedEvent<String>(
                                    "[" + lcName + "] The CheckList item '" + oldName + "' of LifeCycle State '" +
                                    oldLifeCycleState + "' was Unchecked for resource at "+relativePath +".");
                                ((CheckListItemUncheckedEvent)event).setResourcePath(relativePath);
                            } else {
                                event = new CheckListItemCheckedEvent<String>(
                                    "[" + lcName + "] The CheckList item '" + oldName + "' of LifeCycle State '" +
                                    oldLifeCycleState + "' was Checked for resource at "+relativePath +".");
                                ((CheckListItemCheckedEvent)event).setResourcePath(relativePath);
                            }
                        } else {
                            // In here we are un-aware of the changes that happened.
                            event = new CheckListItemCheckedEvent<String>(
                                    "[" + lcName + "] The State of the CheckList item '" + oldName + "' of LifeCycle State '" +
                                    oldLifeCycleState + "' was changed for resource at "+relativePath +".");
                            ((CheckListItemCheckedEvent)event).setResourcePath(relativePath);
                        }
                        event.setParameter("LifecycleName", lcName);
                        event.setParameter("LifecycleState", oldLifeCycleState);
                        event.setParameter("CheckItem", oldName);
                        event.setTenantId(CurrentSession.getCallerTenantId());
                        try {
                            notify(event, requestContext.getRegistry(), relativePath);
                        } catch (Exception ex) {
                            handleException("Unable to send notification for Put Operation", ex);
                        }
                    }
                }
            }
        }
        invokeApprovalNotification(requestContext, relativePath);
    }

   @SuppressWarnings("unchecked")
    public void invokeAspect(RequestContext requestContext) throws RegistryException {
        Map<String, String> parameters = new HashMap<String, String>();
        Resource resource = requestContext.getOldResource();
        String oldPath = resource.getPath();
        String stateKey = null;
        Properties props = resource.getProperties();
        String lcName = resource.getProperty("registry.LC.name");
        boolean isEnvironmentChange=false;
        for (Object key : props.keySet()) {
            String propKey = (String)key;
            if (propKey.matches("registry\\p{Punct}lifecycle\\p{Punct}.*\\p{Punct}state")) {
                stateKey = propKey;
                break;
            }
        }
        if (stateKey == null) {
            return;
        }
        String relativeOldPath = RegistryUtils.getRelativePath(requestContext.getRegistryContext(),
                oldPath);
        String oldState = resource.getProperty(stateKey);
        if ((requestContext.getAspect() == null) || (requestContext.getAction() == null)) {
            return;
        }
        if (!requestContext.isProcessingComplete()) {
            Object parameterNames = requestContext.getProperty("parameterNames");
            if (parameterNames != null) {
                for (String key : (Set<String>)parameterNames) {
                    parameters.put(key, (String)requestContext.getProperty(key));
                }
                requestContext.getAspect().invoke(requestContext, requestContext.getAction(),
                        parameters);
            } else {
                requestContext.getAspect().invoke(requestContext, requestContext.getAction());
            }
            requestContext.setProcessingComplete(true);
        }
        String path = requestContext.getResourcePath().getPath();
        String relativePath = RegistryUtils.getRelativePath(requestContext.getRegistryContext(),
                path);
        if (!requestContext.getRegistry().resourceExists(path)) {
            return;
        }
        resource = requestContext.getRegistry().get(path);

        if (resource == null) {
            return;
        }

        if(resource.getProperty(GovernanceConstants.REGISTRY_IS_ENVIRONMENT_CHANGE)!=null &&
                !resource.getProperty(GovernanceConstants.REGISTRY_IS_ENVIRONMENT_CHANGE).isEmpty()){
            isEnvironmentChange = Boolean.parseBoolean(resource.getProperty(GovernanceConstants.REGISTRY_IS_ENVIRONMENT_CHANGE));
            resource.removeProperty(GovernanceConstants.REGISTRY_IS_ENVIRONMENT_CHANGE);
            requestContext.setResource(resource);
            requestContext.getRegistry().put(path,resource);
        }
        String newState = resource.getProperty(stateKey);
        if (oldState != null && oldState.equalsIgnoreCase(newState)) {
            return;
        }
        String extendedMessage = "";
        if (!oldPath.equals(path)) {
            if (resource instanceof Collection) {
                extendedMessage = ". The collection has moved from: '" + relativeOldPath + "' to: '" +
                                  relativePath + "'.";
            } else {
                extendedMessage = ". The resource has moved from: '" + relativeOldPath + "' to: '" +
                                  relativePath + "'.";
            }
        } else {
            extendedMessage = " for resource at " + path + ".";
        }
        RegistryEvent<String> event = new LifeCycleStateChangedEvent<String>("[" + lcName + "] The LifeCycle State Changed from '" +
                oldState + "' to '" + newState+"'" + extendedMessage);
        event.setParameter("LifecycleName", lcName);
        event.setParameter("OldLifecycleState", oldState);
        event.setParameter("NewLifecycleState", newState);
        if(isEnvironmentChange){
            ((LifeCycleStateChangedEvent)event).setResourcePath(relativeOldPath);
        }else{
            ((LifeCycleStateChangedEvent)event).setResourcePath(relativePath);
        }
        event.setTenantId(CurrentSession.getCallerTenantId());
        try {
            notify(event, requestContext.getRegistry(), relativePath);
        } catch (Exception e) {
            handleException("Unable to send notification for Aspect Invoke Operation", e);
        }
        // When LC move one stage to another, Approval notification may send to user
        if(sendInitialNotification(requestContext, relativePath)){        	
        	RegistryEvent<String> approveEvent = new LifeCycleApprovalNeededEvent<String>(
        			"[" + lcName + "] The LifeCycle State '" + newState +
                    "' required approval for state transitions, for resource locate at "+relativePath +".");
        	approveEvent.setParameter("LifecycleName", lcName);
        	approveEvent.setParameter("OldLifecycleState", oldState);
        	approveEvent.setParameter("NewLifecycleState", newState);
            if(isEnvironmentChange){
                ((LifeCycleApprovalNeededEvent)approveEvent).setResourcePath(relativeOldPath);
            }else{
                ((LifeCycleApprovalNeededEvent)approveEvent).setResourcePath(relativePath);
            }
            approveEvent.setTenantId(CurrentSession.getCallerTenantId());
       		try {
                notify(approveEvent, requestContext.getRegistry(), relativePath);
            } catch (Exception e) {
            	handleException("Unable to send notification for Put Operation", e);
            }
        }                  
    }

    protected void notify(RegistryEvent event, Registry registry, String path) throws Exception {
        try {
            if (Utils.getRegistryNotificationService() == null) {
                log.debug("Eventing service is unavailable.");
                return;
            }
            if (registry == null || registry.getEventingServiceURL(path) == null) {
                Utils.getRegistryNotificationService().notify(event);
                return;
            } else if (Utils.getDefaultNotificationServiceURL() == null) {
                log.error("Governance Eventing Handler is not properly initialized");
            } else if (registry.getEventingServiceURL(path).equals(Utils.getDefaultNotificationServiceURL())) {
                Utils.getRegistryNotificationService().notify(event);
                return;
            } else {
                Utils.getRegistryNotificationService().notify(event, registry.getEventingServiceURL(path));
                return;
            }
        } catch (RegistryException e) {
            log.error("Unable to send notification", e);
        }
        log.error("Unable to send notification");
    }

    private void handleException(String message, Exception e) {
        log.error(message, e);
    }
    /**
     * Send notification when Approval voting happened /approval notification removed
     * 		Or when approval is enabled(after user click on checklist)
     * @param requestContext
     * @param relativePath
     */
    private void invokeApprovalNotification(RequestContext requestContext, String relativePath ){
    	Resource oldResource = requestContext.getOldResource();
        Resource newResource = requestContext.getResource();
        
        Properties props = oldResource.getProperties();
        Properties newProps = newResource.getProperties();
        
    	String lcName = newResource.getProperty("registry.LC.name");
       
        
    	for (Map.Entry<Object, Object> e : props.entrySet()) {
            String propKey = (String) e.getKey();            
            if(propKey.matches("registry\\p{Punct}.*\\p{Punct}votes\\p{Punct}.*")){
            	 List<String> propValues = (List<String>) e.getValue();
                 List<String> newPropValues = (List<String>) newProps.get(propKey);
                 if ((propValues == null) || (newPropValues == null))
                     continue;
                 if ((propValues.size() > 2) && (newPropValues.size() > 2)) {
	                 String oldName = null;
	                 String oldValue = null;
	                 int oldVote = 0;
	                 String oldLifeCycleState = null;
	                 String newName = null;
	                 String newValue = null;
	                 int newVote = 0;
	                 String newLifeCycleState = null;
	                 for (String param : propValues) {
	                     if (param.startsWith("status:")) {
	                         oldLifeCycleState = param.substring(7);
	                     } else if (param.startsWith("name:")) {
	                         oldName = param.substring(5);
	                     } else if (param.startsWith("current:")) {
	                         oldValue = param.substring(8);
	                         oldVote = Integer.parseInt(oldValue);
	                     }
	                 }
	                 for (String param : newPropValues) {
	                     if (param.startsWith("status:")) {
	                         newLifeCycleState = param.substring(7);
	                     } else if (param.startsWith("name:")) {
	                         newName = param.substring(5);
	                     } else if (param.startsWith("current:")) {
	                         newValue = param.substring(8);
	                         newVote = Integer.parseInt(newValue);
	                     }
	                 }
	                 if (oldName.equalsIgnoreCase(newName) &&
	                         oldLifeCycleState.equalsIgnoreCase(newLifeCycleState) &&
	                         !oldValue.equalsIgnoreCase(newValue)) {
	                	 RegistryEvent<String> event = null;
	                	 if(newVote > oldVote){
	                		 event = new LifeCycleApprovedEvent<String>(
	                                 "[" + lcName + "] LifeCycle State '" + oldLifeCycleState + "', transitions event '" + oldName + "'" +
	                                 		 " was approved for resource at "+relativePath +".");
	                             ((LifeCycleApprovedEvent)event).setResourcePath(relativePath);
	                	 }else{
	                		 event = new LifeCycleApprovalWithdrawnEvent<String>(
	                				 "[" + lcName + "] LifeCycle State '" + oldLifeCycleState + "' transitions event '" + oldName + "'" +
	                                 		 " approvel was removed for resource at "+relativePath +".");
	                             ((LifeCycleApprovalWithdrawnEvent)event).setResourcePath(relativePath);
	                	 }
	                	 event.setParameter("LifecycleName", lcName);
	                     event.setParameter("LifecycleState", oldLifeCycleState);
	                     event.setParameter("CheckItem", oldName);
	                     event.setTenantId(CurrentSession.getCallerTenantId());
	                     try {
	                         notify(event, requestContext.getRegistry(), relativePath);
	                     } catch (Exception ex) {
	                         handleException("Unable to send notification for Put Operation", ex);
	                     }                	 
	                 }
                 }  
            }
    	}
    }
    
    /**
     * 
     * @param requestContext
     * @param relativePath
     * @return boolean True value return when at least one approval doesn't dependent on check list.
     */
    private boolean sendInitialNotification(RequestContext requestContext, String relativePath) {    	
        Resource newResource = requestContext.getResource(); 
        Properties newProps = newResource.getProperties();    	
    	boolean sendNotification =  false;    	
    	for (Map.Entry<Object, Object> e : newProps.entrySet()) {
            String propKey = (String) e.getKey();
            if (propKey.matches("registry\\p{Punct}.*\\p{Punct}votes\\p{Punct}.*")) {               
                List<String> newPropValues = (List<String>) newProps.get(propKey);
                if (newPropValues == null)
                    continue;
                if (newPropValues.size() > 2) {                   
                    String newName = null;
                    for (String param : newPropValues) {
                        if (param.startsWith("name:")) {
                            newName = param.substring(5);
                        }
                    }
                    if(newName !=  null && !newName.isEmpty()){
                    	sendNotification = true;
                    	return sendNotification;
                    }
                }
            }
    	}
    	return sendNotification;
	}
}

