/*
*  Copyright (c) 2005-2010,2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.util.GovernanceConstants;
import org.wso2.carbon.governance.lcm.tasks.LCNotificationScheduler;
import org.wso2.carbon.governance.lcm.tasks.events.LifecycleNotificationEvent;
import org.wso2.carbon.governance.registry.eventing.handlers.utils.events.CheckListItemCheckedEvent;
import org.wso2.carbon.governance.registry.eventing.handlers.utils.events.CheckListItemUncheckedEvent;
import org.wso2.carbon.governance.registry.eventing.handlers.utils.events.LifeCycleApprovalNeededEvent;
import org.wso2.carbon.governance.registry.eventing.handlers.utils.events.LifeCycleApprovalWithdrawnEvent;
import org.wso2.carbon.governance.registry.eventing.handlers.utils.events.LifeCycleApprovedEvent;
import org.wso2.carbon.governance.registry.eventing.handlers.utils.events.LifeCycleCreatedEvent;
import org.wso2.carbon.governance.registry.eventing.handlers.utils.events.LifeCycleDeletedEvent;
import org.wso2.carbon.governance.registry.eventing.handlers.utils.events.LifeCycleStateChangedEvent;
import org.wso2.carbon.governance.registry.eventing.handlers.utils.events.PublisherCheckListItemCheckedEvent;
import org.wso2.carbon.governance.registry.eventing.handlers.utils.events.PublisherCheckListItemUncheckedEvent;
import org.wso2.carbon.governance.registry.eventing.handlers.utils.events.PublisherLifeCycleStateChangedEvent;
import org.wso2.carbon.governance.registry.eventing.handlers.utils.events.PublisherResourceUpdatedEvent;
import org.wso2.carbon.governance.registry.eventing.handlers.utils.events.StoreLifeCycleStateChangedEvent;
import org.wso2.carbon.governance.registry.eventing.handlers.utils.events.StoreResourceUpdatedEvent;
import org.wso2.carbon.governance.registry.eventing.internal.EventDataHolder;
import org.wso2.carbon.registry.common.eventing.RegistryEvent;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.config.Mount;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.session.CurrentSession;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.eventing.services.EventingServiceImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class GovernanceEventingHandler extends Handler {
    private static final Log log = LogFactory.getLog(GovernanceEventingHandler.class);

    /**
     * Constant used to validate vote click.
     */
    private final String vote_click = "voteClick";

    /**
     * Constant used to validate item click.
     */
    private final String item_click = "itemClick";

    public void init(String defaultNotificationEndpoint) {
        EventDataHolder.getInstance().setDefaultNotificationServiceURL(defaultNotificationEndpoint);
    }

    public GovernanceEventingHandler() {
        try {
            EventDataHolder.getInstance().getRegistryNotificationService().registerEventType("checklist.item.checked", CheckListItemCheckedEvent.EVENT_NAME, CheckListItemCheckedEvent.EVENT_NAME);
            /*Utils.getRegistryNotificationService().registerEventTypeExclusion("checklist.item.checked", "/");
            Utils.getRegistryNotificationService().registerEventTypeExclusion("checklist.item.checked", "/system");
            Utils.getRegistryNotificationService().registerEventTypeExclusion("checklist.item.checked", "/system/.*");*/
            EventDataHolder.getInstance().getRegistryNotificationService().registerEventType("checklist.item.unchecked", CheckListItemUncheckedEvent.EVENT_NAME, CheckListItemUncheckedEvent.EVENT_NAME);
            /*Utils.getRegistryNotificationService().registerEventTypeExclusion("checklist.item.unchecked", "/");
            Utils.getRegistryNotificationService().registerEventTypeExclusion("checklist.item.unchecked", "/system");
            Utils.getRegistryNotificationService().registerEventTypeExclusion("checklist.item.unchecked", "/system/.*");*/
            EventDataHolder.getInstance().getRegistryNotificationService().registerEventType("lifecycle.state.changed", LifeCycleStateChangedEvent.EVENT_NAME, LifeCycleStateChangedEvent.EVENT_NAME);
            /*Utils.getRegistryNotificationService().registerEventTypeExclusion("lifecycle.state.changed", "/");
            Utils.getRegistryNotificationService().registerEventTypeExclusion("lifecycle.state.changed", "/system");
            Utils.getRegistryNotificationService().registerEventTypeExclusion("lifecycle.state.changed", "/system/.*");*/
            EventDataHolder.getInstance().getRegistryNotificationService().registerEventType("lifecycle.deleted", LifeCycleDeletedEvent.EVENT_NAME, LifeCycleDeletedEvent.EVENT_NAME);
            /*Utils.getRegistryNotificationService().registerEventTypeExclusion("lifecycle.deleted", "/");
            Utils.getRegistryNotificationService().registerEventTypeExclusion("lifecycle.deleted", "/system");
            Utils.getRegistryNotificationService().registerEventTypeExclusion("lifecycle.deleted", "/system/.*");*/
            EventDataHolder.getInstance().getRegistryNotificationService().registerEventType("lifecycle.created", LifeCycleCreatedEvent.EVENT_NAME, LifeCycleCreatedEvent.EVENT_NAME);
            /*Utils.getRegistryNotificationService().registerEventTypeExclusion("lifecycle.created", "/");
            Utils.getRegistryNotificationService().registerEventTypeExclusion("lifecycle.created", "/system");
            Utils.getRegistryNotificationService().registerEventTypeExclusion("lifecycle.created", "/system/.*");*/
            EventDataHolder.getInstance().getRegistryNotificationService().registerEventType("lifecycle.approved", LifeCycleApprovedEvent.EVENT_NAME, LifeCycleApprovedEvent.EVENT_NAME);
            EventDataHolder.getInstance().getRegistryNotificationService().registerEventType("lifecycle.approval.need", LifeCycleApprovalNeededEvent.EVENT_NAME, LifeCycleApprovalNeededEvent.EVENT_NAME);
            EventDataHolder.getInstance().getRegistryNotificationService().registerEventType("lifecycle.approval.withdrawn", LifeCycleApprovalWithdrawnEvent.EVENT_NAME, LifeCycleApprovalWithdrawnEvent.EVENT_NAME);
            EventDataHolder.getInstance().getRegistryNotificationService().registerEventType("lifecycle.checkpoint.notification", LifecycleNotificationEvent.EVENT_NAME, LifecycleNotificationEvent.EVENT_NAME);

            EventDataHolder.getInstance().getRegistryNotificationService().registerEventType("publisher.checklist.checked", PublisherCheckListItemCheckedEvent.EVENT_NAME, null);
            EventDataHolder.getInstance().getRegistryNotificationService().registerEventType("publisher.checklist.unchecked", PublisherCheckListItemUncheckedEvent.EVENT_NAME, null);
            EventDataHolder.getInstance().getRegistryNotificationService().registerEventType("publisher.lifecycle.state.changed", PublisherLifeCycleStateChangedEvent.EVENT_NAME, null);
            EventDataHolder.getInstance().getRegistryNotificationService().registerEventType("publisher.update", PublisherResourceUpdatedEvent.EVENT_NAME,null);
            EventDataHolder.getInstance().getRegistryNotificationService().registerEventType("store.lifecycle.state.changed", StoreLifeCycleStateChangedEvent.EVENT_NAME, null);
            EventDataHolder.getInstance().getRegistryNotificationService().registerEventType("store.update", StoreResourceUpdatedEvent.EVENT_NAME, null);

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

        boolean isNotCollection = !(requestContext.getResource() instanceof Collection);

        if (oldResource == null || newResource == null ||
            oldResource.getProperties() == null || newResource.getProperties() == null) {
            return;
        }
        Properties props = oldResource.getProperties();
        Properties newProps = newResource.getProperties();
        String lcName = newResource.getProperty("registry.LC.name");
        String oldLcName = oldResource.getProperty("registry.LC.name");
        List oldLifecycleList = (List) props.get("registry.Aspects");
        List newLifecycleList = (List) newProps.get("registry.Aspects");
        boolean isNotificationSent = false;
        if (lcName == null && oldLcName != null) {
            StringBuilder messageBuilder = new StringBuilder("[").append(oldLcName)
                    .append("] The LifeCycle was deleted for resource at ").append(relativePath).append(".");
            RegistryEvent<String> event = new LifeCycleDeletedEvent<String>(messageBuilder.toString());
            ((LifeCycleDeletedEvent)event).setResourcePath(relativePath);
            event.setParameter("LifecycleName", oldLcName);
            event.setTenantId(CurrentSession.getCallerTenantId());
            try {
                notify(event, requestContext.getRegistry(), relativePath);
                isNotificationSent = true;
            } catch (Exception e) {
                handleException("Unable to send notification for Put Operation", e);
            }
            return;
        } else if (lcName != null && oldLcName != null && !lcName.equals(oldLcName)
                && oldLifecycleList.size() < newLifecycleList.size()) {
            // Add lifecycle checkpoint notification scheduler data when lifecycle state is attached.
            List stateList = (List) newProps.get("registry.lifecycle." + lcName + ".state");
            if (stateList != null) {
                String lifecycleState = (String) stateList.get(0);
                addLCNotificationScheduler(newResource, lcName, lifecycleState, false);
            }
        } else if (lcName != null && oldLcName == null) {
            // Adding scheduler entry when a service is created. This handles the scheduler entries for attaching
            // default lifecycle schedulers at the service creation time.
            List statesList = (List) newProps.get("registry.lifecycle." + lcName + ".state");
            if (statesList != null) {
                // Getting 0 index because its the initial state of a lifecycle
                String lifecycleState = (String) statesList.get(0);
                addLCNotificationScheduler(newResource, lcName, lifecycleState, false);
            }
            StringBuilder messageBuilder = new StringBuilder("[").append(lcName)
                    .append("] The LifeCycle was created for resource at ").append(relativePath).append(".");
            RegistryEvent<String> event = new LifeCycleCreatedEvent<String>(messageBuilder.toString());
            ((LifeCycleCreatedEvent)event).setResourcePath(relativePath);
            event.setParameter("LifecycleName", lcName);
            event.setTenantId(CurrentSession.getCallerTenantId());
            try {
                notify(event, requestContext.getRegistry(), relativePath);
                isNotificationSent = true;
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
                    isNotificationSent = true;
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
                    if(oldName!=null && oldValue!=null) {
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
                            createPublisherLCNotification(event, requestContext, relativePath);
                            isNotificationSent = true;
                        }
                    }
                }
            }
        }
        if (oldResource != null && sendNotifications(requestContext,relativePath) && isNotCollection && !isNotificationSent) {
            RegistryEvent<String> pubEvent = new PublisherResourceUpdatedEvent<String>("The resource at path " + relativePath + " was updated.");
            ((PublisherResourceUpdatedEvent)pubEvent).setResourcePath(relativePath);
            pubEvent.setParameter("RegistryOperation", "put");
            pubEvent.setTenantId(CurrentSession.getCallerTenantId());
            try {
                notify(pubEvent, requestContext.getRegistry(), relativePath);
            } catch (Exception e) {
                handleException("Unable to send notification for Update Operation", e);
            }

            RegistryEvent<String> storeEvent = new StoreResourceUpdatedEvent<String>("The resource at path " + relativePath + " was updated.");
            ((StoreResourceUpdatedEvent)storeEvent).setResourcePath(relativePath);
            storeEvent.setParameter("RegistryOperation", "put");
            storeEvent.setTenantId(CurrentSession.getCallerTenantId());

            try {
                notify(storeEvent, requestContext.getRegistry(), relativePath);
            } catch (Exception e) {
                handleException("Unable to send notification for Update Operation", e);
            }

        }
        invokeApprovalNotification(requestContext, relativePath);
    }

   @SuppressWarnings("unchecked")
    public void invokeAspect(RequestContext requestContext) throws RegistryException {
        Map<String, String> parameters = new HashMap<String, String>();
        Resource resource = requestContext.getOldResource();
        String oldPath = resource.getPath();

        Properties props = resource.getProperties();
        //String lcName = resource.getProperty("registry.LC.name");
        boolean isEnvironmentChange=false;
        List<String> properties = new ArrayList<String>();
        Map<String, String> changeStates = new HashMap<String, String>();
        Map<String, String> oldStates = new HashMap<String, String>();


        for (Object key : props.keySet()) {
            String propKey = (String)key;
            if (propKey.matches("registry\\p{Punct}lifecycle\\p{Punct}.*\\p{Punct}state")) {
                properties.add(propKey);
            }
        }
        if (properties.size() == 0) {
            return;
        }
        String relativeOldPath = RegistryUtils.getRelativePath(requestContext.getRegistryContext(),oldPath);
        for (String  property :  properties){
            String oldState = resource.getProperty(property);
            oldStates.put(property,oldState);
        }
        //String oldState = resource.getProperty(stateKey);
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

       for (String  property :  properties){
           String newState = resource.getProperty(property);
           changeStates.put(property,newState);
       }
        //String newState = resource.getProperty(stateKey);
       // Add lifecycle checkpoint notification scheduler data when lifecycle state changes.
       String action = requestContext.getAction();
       // Filtering lifecycle actions.
       if ((!vote_click.equals(action) && !item_click.equals(action))) {
           for (String  property :  properties){
               String lcName = getLCName(property);
               addLCNotificationScheduler(resource, lcName, changeStates.get(property), true);
           }
       }
       String oldState = null;
       String newState = null;
       for (String  property :  properties){
           if(changeStates.get(property) != null && oldStates.get(property) != null && !changeStates.get(property).equals(oldStates.get(property))){
               String lcName = getLCName(property);
               oldState = oldStates.get(property);
               newState = changeStates.get(property);
               String extendedMessage = "";
               boolean isUpdateOnly = false;
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
                   isUpdateOnly = true;
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
               if (isUpdateOnly) {
                   //Send Store Notification
                   sendPublisherLCNotification(lcName,oldState,newState,isEnvironmentChange,relativeOldPath,relativePath,requestContext, extendedMessage);
                   //end of the store notification

                   //Send publisher Notification
                   sendStoreLCNotification(lcName,oldState,newState,isEnvironmentChange,relativeOldPath,relativePath,requestContext, extendedMessage);
                   //end of the publisher notification
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
           } else {
               continue;
           }
       }

    }

    protected void notify(RegistryEvent event, Registry registry, String path) throws Exception {
        try {
            if (EventDataHolder.getInstance().getRegistryNotificationService() == null) {
                log.debug("Eventing service is unavailable.");
                return;
            }
            if (registry == null || registry.getEventingServiceURL(path) == null) {
                EventDataHolder.getInstance().getRegistryNotificationService().notify(event);
                return;
            } else if (EventDataHolder.getInstance().getDefaultNotificationServiceURL() == null) {
                log.error("Governance Eventing Handler is not properly initialized");
            } else if (registry.getEventingServiceURL(path).equals(
                    EventDataHolder.getInstance().getDefaultNotificationServiceURL())) {
                EventDataHolder.getInstance().getRegistryNotificationService().notify(event);
                return;
            } else {
                EventDataHolder.getInstance().getRegistryNotificationService()
                               .notify(event, registry.getEventingServiceURL(path));
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
                     if(oldName!=null && oldValue!=null){
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

    /**
     * This method is used to add schedulers to lifecycle checkpoints. These schedulers will be triggered by a
     * schedule task. Checkpoints are added with to the lifecycle state and lifecycle name.
     *
     * @param resource          resource which the scheduler needs to be added.
     * @param lifecycleName     lifecycle name of the scheduler
     * @param lifecycleState    lifecycle state of the scheduler.
     * @param isInvokeAspect    does this add scheduler method is called when invoking a lifecycle.
     */
    private void addLCNotificationScheduler(Resource resource, String lifecycleName, String lifecycleState, boolean
            isInvokeAspect) {
        LCNotificationScheduler lifecycleNotificationScheduler =
                new LCNotificationScheduler();
        try {
            lifecycleNotificationScheduler
                    .addScheduler((ResourceImpl) resource, lifecycleName, lifecycleState, isInvokeAspect);
        } catch (GovernanceException e) {
            log.error("Lifecycle '" + lifecycleName + "'checkpoint addition failed for state " + lifecycleState, e);
        }
    }

    private String getLCName(String property){
        String[] peps = property.split("\\p{Punct}");
        if (peps != null && peps.length == 4){
              return peps[2];
        }
        return null;
    }

    private void sendStoreLCNotification(String lcName, String oldState, String newState,boolean isEnvironmentChange, String relativeOldPath, String relativePath,RequestContext requestContext, String extendedMessage){
        //Send Store Notification

        RegistryEvent<String> storeLCChanges = new StoreLifeCycleStateChangedEvent<String>("[" + lcName + "] The LifeCycle State Changed from '" +
                                                                                           oldState + "' to '" + newState+"'"+ extendedMessage);
        storeLCChanges.setParameter("LifecycleName", lcName);
        storeLCChanges.setParameter("OldLifecycleState", oldState);
        storeLCChanges.setParameter("NewLifecycleState", newState);
        if(isEnvironmentChange){
            ((StoreLifeCycleStateChangedEvent)storeLCChanges).setResourcePath(relativeOldPath);
        }else{
            ((StoreLifeCycleStateChangedEvent)storeLCChanges).setResourcePath(relativePath);
        }
        storeLCChanges.setTenantId(CurrentSession.getCallerTenantId());
        try {
            notify(storeLCChanges, requestContext.getRegistry(), relativePath);
        } catch (Exception e) {
            handleException("Unable to send notification for Aspect Invoke Operation", e);
        }
        //end of the store notification
    }

    private void sendPublisherLCNotification(String lcName, String oldState, String newState,boolean isEnvironmentChange, String relativeOldPath, String relativePath,RequestContext requestContext, String extendedMessage){
        //Send Store Notification
        RegistryEvent<String> publisherLCChanges = new PublisherLifeCycleStateChangedEvent<String>("[" + lcName + "] The LifeCycle State Changed from '" +
                                                                                                   oldState + "' to '" + newState+"'"+ extendedMessage);
        publisherLCChanges.setParameter("LifecycleName", lcName);
        publisherLCChanges.setParameter("OldLifecycleState", oldState);
        publisherLCChanges.setParameter("NewLifecycleState", newState);
        if(isEnvironmentChange){
            ((PublisherLifeCycleStateChangedEvent)publisherLCChanges).setResourcePath(relativeOldPath);
        }else{
            ((PublisherLifeCycleStateChangedEvent)publisherLCChanges).setResourcePath(relativePath);
        }
        publisherLCChanges.setTenantId(CurrentSession.getCallerTenantId());
        try {
            notify(publisherLCChanges, requestContext.getRegistry(), relativePath);
        } catch (Exception e) {
            handleException("Unable to send notification for Aspect Invoke Operation", e);
        }
        //end of the store notification
    }


    private void createPublisherLCNotification(RegistryEvent event, RequestContext requestContext, String relativePath) {
        if (event instanceof CheckListItemUncheckedEvent) {
            RegistryEvent checkListEvent = new PublisherCheckListItemUncheckedEvent<String>(event.getMessage().toString());
            ((PublisherCheckListItemUncheckedEvent)checkListEvent).setResourcePath(relativePath);
            checkListEvent.setParameter("LifecycleState", ((Map<String, String>)event.getParameters()).get("LifecycleState"));
            checkListEvent.setParameter("CheckItem", ((Map<String, String>)event.getParameters()).get("CheckItem"));
            checkListEvent.setParameter("LifecycleName", ((Map<String, String>)event.getParameters()).get("LifecycleName"));
            checkListEvent.setTenantId(CurrentSession.getCallerTenantId());
            try {
                notify(checkListEvent, requestContext.getRegistry(), relativePath);
            } catch (Exception e) {
                handleException("Unable to send notification for Aspect Invoke Operation", e);
            }

        } else if (event instanceof CheckListItemCheckedEvent) {
            RegistryEvent checkListEvent = new PublisherCheckListItemCheckedEvent<String>(event.getMessage().toString());
            ((PublisherCheckListItemCheckedEvent)checkListEvent).setResourcePath(relativePath);
            checkListEvent.setParameter("LifecycleState", ((Map<String, String>)event.getParameters()).get("LifecycleState"));
            checkListEvent.setParameter("CheckItem", ((Map<String, String>)event.getParameters()).get("CheckItem"));
            checkListEvent.setTenantId(CurrentSession.getCallerTenantId());
            checkListEvent.setParameter("LifecycleName", ((Map<String, String>)event.getParameters()).get("LifecycleName"));
            try {
                notify(checkListEvent, requestContext.getRegistry(), relativePath);
            } catch (Exception e) {
                handleException("Unable to send notification for Aspect Invoke Operation", e);
            }
        }

    }

    /**
     * Method to get the actual depth of the request
     */
    private int getRequestDepth(RequestContext requestContext){
        int requestDepth = -1;
        if (requestContext.getRegistry().getRegistryContext() != null &&
            requestContext.getRegistry().getRegistryContext().getDataAccessManager() != null &&
            requestContext.getRegistry().getRegistryContext().getDataAccessManager().getDatabaseTransaction() != null) {
            requestDepth =
                    requestContext.getRegistry().getRegistryContext().getDataAccessManager().getDatabaseTransaction()
                                  .getNestedDepth();
        }
        return requestDepth;
    }

    private boolean sendNotifications(RequestContext requestContext, String relativePath) {

        boolean isMountPath = false;
        List<String> mediatypes = EventingServiceImpl.listOfMediaTypes;
        String resourceMediaType = null;
        if (requestContext.getResource() != null) {
            resourceMediaType = requestContext.getResource().getMediaType();
        }
        List<Mount> mounts = requestContext.getRegistry().getRegistryContext().getMounts();
        for (Mount mount : mounts) {
            String mountPath = mount.getPath();
            if (relativePath.startsWith(mountPath)) {
                isMountPath = true;
            }
        }
        if (isMountPath) {
            if (getRequestDepth(requestContext) != 1) {
                return false;
            } else {
                return true;
            }
        } else {
            int requestDepth = getRequestDepth(requestContext);
            if (!(requestDepth == 1 || requestDepth == 3)) {
                if (requestDepth == 2 && resourceMediaType != null && !mediatypes.contains(resourceMediaType)) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return true;
            }
        }

    }
}

