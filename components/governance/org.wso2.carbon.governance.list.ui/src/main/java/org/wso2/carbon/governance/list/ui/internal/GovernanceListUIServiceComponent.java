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
package org.wso2.carbon.governance.list.ui.internal;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaxen.JaxenException;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.governance.api.util.GovernanceArtifactConfiguration;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.carbon.ui.CarbonUIAuthenticator;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.ui.MenuAdminClient;
import org.wso2.carbon.ui.UIAuthenticationExtender;
import org.wso2.carbon.ui.deployment.ComponentBuilder;
import org.wso2.carbon.ui.deployment.beans.Component;
import org.wso2.carbon.ui.deployment.beans.Menu;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.namespace.QName;
import java.util.*;

/**
 * The List UI Declarative Service Component.
 *
 * @scr.component name="org.wso2.carbon.governance.list.ui"
 * immediate="true"
 * @scr.reference name="ui.authenticator"
 * interface="org.wso2.carbon.ui.CarbonUIAuthenticator" cardinality="1..1"
 * policy="dynamic" bind="setCarbonUIAuthenticator" unbind="unsetCarbonUIAuthenticator"
 */
@SuppressWarnings({"unused", "JavaDoc"})
public class GovernanceListUIServiceComponent {

    private static Log log = LogFactory.getLog(GovernanceListUIServiceComponent.class);
    private ServiceRegistration serviceRegistration;
    private static final String DEFAULT_LIFECYCLE_GENERATOR_CLASS
            = "org.wso2.carbon.governance.services.ui.utils.LifecycleListPopulator";
    private int menuOrder = 50;

    protected void activate(ComponentContext context) {
        UIAuthenticationExtender authenticationExtender = new UIAuthenticationExtender() {
            public void onSuccessAdminLogin(HttpServletRequest request, String s, String s1,
                                            String s2) {
                if(CarbonUIUtil.isUserAuthorized(request,"/permission/admin/manage/resources/ws-api")){
                    HttpSession session = request.getSession();
                    String cookie =
                            (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
                    try {
                        WSRegistryServiceClient registry = new WSRegistryServiceClient(s2, cookie);
                        List<GovernanceArtifactConfiguration> configurations =
                                GovernanceUtils.findGovernanceArtifactConfigurations(registry);
                        Map<String, String> customAddUIMap = new LinkedHashMap<String, String>();
                        Map<String, String> customViewUIMap = new LinkedHashMap<String, String>();
                        List<Menu> userCustomMenuItemsList = new LinkedList<Menu>();
                        for (GovernanceArtifactConfiguration configuration : configurations) {
                            Component component = new Component();
                            OMElement uiConfigurations = configuration.getUIConfigurations();
                            String key = configuration.getKey();
                            String configurationPath = RegistryConstants.CONFIG_REGISTRY_BASE_PATH +
                                    RegistryConstants.GOVERNANCE_COMPONENT_PATH +
                                    "/configuration/";
                            String layoutStoragePath = configurationPath
                                    + key;
                            RealmService realmService = registry.getRegistryContext().getRealmService();
                            if (realmService.getTenantUserRealm(realmService.getTenantManager().getTenantId(s1))
                                    .getAuthorizationManager().isUserAuthorized(s, configurationPath, ActionConstants.PUT)
                                    || registry.resourceExists(layoutStoragePath)) {
                                List<Menu> menuList = component.getMenusList();
                                if (uiConfigurations != null) {
                                    ComponentBuilder
                                            .processMenus("artifactType", uiConfigurations, component);
                                    ComponentBuilder.processCustomUIs(uiConfigurations, component);
                                }
                                if (menuList.size() == 0) {
                                    // if no menu definitions were present, define the default ones.
                                    buildMenuList(request, configuration, menuList, key);
                                }
                                userCustomMenuItemsList.addAll(menuList);
                                customAddUIMap.putAll(component.getCustomAddUIMap());
                                Map<String, String> viewUIMap =
                                        component.getCustomViewUIMap();
                                if (viewUIMap.isEmpty()) {
                                    // if no custom UI definitions were present, define the default.
                                    buildViewUI(configuration, viewUIMap, key);
                                }
                                customViewUIMap.putAll(viewUIMap);
                                OMElement layout = configuration.getContentDefinition();
                                if (layout != null && !registry.resourceExists(layoutStoragePath)) {
                                    Resource resource = registry.newResource();
                                    resource.setContent(RegistryUtils.encodeString(layout.toString()));
                                    resource.setMediaType("application/xml");
                                    registry.put(layoutStoragePath, resource);
                                }
                            }
                        }
                        session.setAttribute(MenuAdminClient.USER_CUSTOM_MENU_ITEMS,
                                userCustomMenuItemsList.toArray(
                                        new Menu[userCustomMenuItemsList.size()]));
                        session.setAttribute("customAddUI", customAddUIMap);
                        session.setAttribute("customViewUI",customViewUIMap);
                    } catch (RegistryException e) {
                        log.error("unable to create connection to registry");
                    } catch (org.wso2.carbon.user.api.UserStoreException e) {
                        log.error("unable to realm service");
                    }
                }
            }
        };
        serviceRegistration = context.getBundleContext().registerService(
                UIAuthenticationExtender.class.getName(), authenticationExtender, null);
        log.debug("******* Governance List UI bundle is activated ******* ");
    }

    private void buildViewUI(GovernanceArtifactConfiguration configuration,
                             Map<String, String> viewUIMap, String key) {
        String singularLabel = configuration.getSingularLabel();
        String pluralLabel = configuration.getPluralLabel();

        String lifecycleAttribute = key + "Lifecycle_lifecycleName";

        lifecycleAttribute = BuilLifecycleAttribute(configuration, DEFAULT_LIFECYCLE_GENERATOR_CLASS, lifecycleAttribute);

        if (singularLabel == null || pluralLabel == null) {
            log.error("The singular label and plural label have not " +
                    "been defined for the artifact type: " + key);
        } else {
            String contentURL = configuration.getContentURL();
            if (contentURL != null) {
                if (!contentURL.toLowerCase().equals("default")) {
                    viewUIMap.put(configuration.getMediaType(), contentURL);
                }
            } else {
                String path = "../generic/edit_ajaxprocessor.jsp?hideEditView=true&key=" + key +
                        "&lifecycleAttribute=" + lifecycleAttribute +"&add_edit_breadcrumb=" +
                        singularLabel + "&add_edit_region=region3&add_edit_item=governance_add_" +
                        key + "_menu&breadcrumb=" + singularLabel;
                viewUIMap.put(configuration.getMediaType(), path);
            }
        }
    }

    /*
    * This method is used to capture the lifecycle attribute from the configuration.
    *
    * expected configuration elements are
    *
    * <field type="options">
            <name label="Lifecycle Name" >Lifecycle Name</name>
            <values class="org.wso2.carbon.governance.services.ui.utils.LifecycleListPopulator"/>
        </field>
    *
    * or
    *
    *  <field type="options">
            <name label="Lifecycle Name" >Lifecycle Name</name>
            <values class="com.foo.bar.LifecycleListPopulator" isLifecycle="true"/>
        </field>
    *  */
    private String BuilLifecycleAttribute(GovernanceArtifactConfiguration configuration,
                                          String defaultLifecycleGeneratorClass, String lifecycleAttribute) {
        try {
//            This part checks whether the user has given a lifecycle populates.
//            If not, then we check whether there is an attribute called, "isLifecycle"
//            This attribute will identify the lifecycle attribute from the configuration. 
            OMElement configurationElement = configuration.getContentDefinition();
            String xpathExpression = "//@class";

            AXIOMXPath xpath = new AXIOMXPath(xpathExpression);
            List resultNodes = xpath.selectNodes(configurationElement);

            if (resultNodes != null && resultNodes.size() > 0) {
                String lifecycleParentName = null;
                String lifecycleName = null;

                for (Object resultNode : resultNodes) {
                    OMElement parentElement = ((OMAttribute)resultNode).getOwner();
                    if(parentElement.getAttributeValue(new QName("class")).equals(defaultLifecycleGeneratorClass)){
                        Iterator childrenIterator = parentElement.getParent().getChildrenWithLocalName("name");
                        while (childrenIterator.hasNext()) {
                            OMElement next = (OMElement) childrenIterator.next();
                            lifecycleName = next.getAttributeValue(new QName("label"));
                        }
                        OMElement rootElement = (OMElement) ((OMElement) parentElement.getParent()).getParent();
                        lifecycleParentName = rootElement.getAttributeValue(new QName("name"));
                        break;
                    }else if(parentElement.getAttributeValue(new QName("isLifecycle")) != null && parentElement.getAttributeValue(new QName("isLifecycle")).equals("true")){
                        Iterator childrenIterator = parentElement.getParent().getChildrenWithLocalName("name");
                        while (childrenIterator.hasNext()) {
                            OMElement next = (OMElement) childrenIterator.next();
                            lifecycleName = next.getAttributeValue(new QName("label"));
                        }
                        OMElement rootElement = (OMElement) ((OMElement) parentElement.getParent()).getParent();
                        lifecycleParentName = rootElement.getAttributeValue(new QName("name"));
                        break;
                    }
                }
                if (lifecycleParentName != null && lifecycleName != null) {
                    return convertName(lifecycleParentName.split(" "))
                            + "_" + convertName(lifecycleName.split(" "));
                }
            }
            
        } catch (OMException e) {
            log.error("Governance artifact configuration of configuration key:" + configuration.getKey() + " is invalid", e);
        } catch (JaxenException e) {
            log.error("Error in getting the lifecycle attribute",e);
        }
        return null;
    }

    private String convertName(String[] nameParts) {
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
            if (namePart.equals(nameParts[0])) {
                namePart = namePart.substring(0, i).toLowerCase() + namePart.substring(i);
            }
            if (convertedName == null) {
                convertedName = namePart;
            } else {
                convertedName += namePart;
            }
        }
        return convertedName;
    }

    private void buildMenuList(HttpServletRequest request,
                               GovernanceArtifactConfiguration configuration, List<Menu> menuList,
                               String key) {
        String singularLabel = configuration.getSingularLabel();
        String pluralLabel = configuration.getPluralLabel();
        boolean hasNamespace = configuration.hasNamespace();
        String lifecycleAttribute = key + "Lifecycle_lifecycleName";

        lifecycleAttribute = BuilLifecycleAttribute(configuration, DEFAULT_LIFECYCLE_GENERATOR_CLASS, lifecycleAttribute);

        if (singularLabel == null || pluralLabel == null) {
            log.error("The singular label and plural label have not " +
                    "been defined for the artifact type: " + key);
        } else {
            int iconSet = configuration.getIconSet();
            if (CarbonUIUtil.isUserAuthorized(request,
                    "/permission/admin/manage/resources/govern/" + key + "/add") &&
                    CarbonUIUtil.isUserAuthorized(request,
                            "/permission/admin/manage/resources/browse") &&
                    CarbonUIUtil.isUserAuthorized(request,
                            "/permission/admin/manage/resources/ws-api")) {
                Menu addMenu = new Menu();
                addMenu.setId("governance_add_" + key + "_menu");
                addMenu.setI18nKey(singularLabel);
                addMenu.setParentMenu("add_sub_menu");
                if (configuration.getExtension() == null) {
                    addMenu.setLink("../generic/add_edit.jsp");
                    addMenu.setUrlParameters("key=" + key + "&lifecycleAttribute=" +
                            lifecycleAttribute + "&breadcrumb=" + singularLabel);
                } else {
                    addMenu.setLink("../generic/add_content.jsp");
                    addMenu.setUrlParameters("key=" + key + "&lifecycleAttribute=" +
                            lifecycleAttribute + "&breadcrumb=" + singularLabel + "&mediaType=" +
                            configuration.getMediaType() + "&extension=" +
                            configuration.getExtension() + "&singularLabel=" + singularLabel +
                            "&pluralLabel=" + pluralLabel + "&hasNamespace" + hasNamespace);
                }
                addMenu.setRegion("region3");
                addMenu.setOrder(String.valueOf(menuOrder));
                addMenu.setStyleClass("manage");
                if (iconSet > 0) {
                    addMenu.setIcon("../generic/images/add" + iconSet + ".png");
                } else {
                    addMenu.setIcon("../images/add.gif");
                }
                addMenu.setAllPermissionsRequired(true);
                addMenu.setRequirePermission(
                        new String[]{"/permission/admin/manage/resources/govern/" + key + "/add",
                                "/permission/admin/manage/resources/browse",
                                "/permission/admin/manage/resources/ws-api"});
                menuList.add(addMenu);
            }

            if (CarbonUIUtil.isUserAuthorized(request,
                    "/permission/admin/manage/resources/govern/" + key + "/list") &&
                    CarbonUIUtil.isUserAuthorized(request,
                            "/permission/admin/manage/resources/ws-api")) {
                Menu listMenu = new Menu();
                listMenu.setId("governance_list_" + key + "_menu");
                listMenu.setI18nKey(pluralLabel);
                listMenu.setParentMenu("list_sub_menu");
                if (configuration.getExtension() == null) {
                    listMenu.setLink("../generic/list.jsp");
                    listMenu.setUrlParameters("key=" + key + "&breadcrumb=" + pluralLabel +
                            "&singularLabel=" + singularLabel + "&pluralLabel=" + pluralLabel);
                } else {
                    listMenu.setLink("../generic/list_content.jsp");
                    listMenu.setUrlParameters("key=" + key + "&lifecycleAttribute=" +
                            lifecycleAttribute + "&breadcrumb=" + singularLabel + "&mediaType=" +
                            configuration.getMediaType() + "&singularLabel=" + singularLabel +
                            "&pluralLabel=" + pluralLabel + "&hasNamespace=" + hasNamespace);
                }
                listMenu.setRegion("region3");
                listMenu.setOrder(String.valueOf(menuOrder));
                listMenu.setStyleClass("manage");
                if (iconSet > 0) {
                    listMenu.setIcon("../generic/images/list" + iconSet + ".png");
                } else {
                    listMenu.setIcon("../images/list.gif");
                }
                listMenu.setAllPermissionsRequired(true);
                listMenu.setRequirePermission(
                        new String[]{"/permission/admin/manage/resources/govern/" + key + "/list",
                                "/permission/admin/manage/resources/ws-api"});
                menuList.add(listMenu);
            }
            /*if (CarbonUIUtil.isUserAuthorized(request,
                    "/permission/admin/configure/governance/" + key + "-ui")) {
                Menu configureMenu = new Menu();
                configureMenu.setId("governance_" + key + "_config_menu");
                configureMenu.setI18nKey(pluralLabel);
                configureMenu.setParentMenu("configure_menu");
                configureMenu.setLink("../generic/configure.jsp");
                configureMenu.setUrlParameters("key=" + key + "&breadcrumb=" + pluralLabel +
                        "&add_edit_region=region3&add_edit_item=governance_add_" + key + "_menu" +
                        "&lifecycleAttribute=" + lifecycleAttribute +
                        "&add_edit_breadcrumb=" + singularLabel + "&singularLabel=" + singularLabel +
                        "&pluralLabel=" + pluralLabel);
                configureMenu.setRegion("region1");
                configureMenu.setOrder("40");
                configureMenu.setStyleClass("manage");
                if (iconSet > 0) {
                    configureMenu.setIcon("../generic/images/configure" + iconSet + ".png");
                } else {
                    configureMenu.setIcon("../generic/images/services1.gif");
                }
                configureMenu.setRequirePermission(
                        new String[]{"/permission/admin/configure/governance/" + key + "-ui"});
                menuList.add(configureMenu);
            }*/
            menuOrder++;
        }
    }

    protected void deactivate(ComponentContext context) {
        if (serviceRegistration != null) {
            serviceRegistration.unregister();
            serviceRegistration = null;
        }
        log.debug("Governance List UI bundle is deactivated ");
    }

    protected void setCarbonUIAuthenticator(CarbonUIAuthenticator uiAuthenticator) {
    }

    protected void unsetCarbonUIAuthenticator(CarbonUIAuthenticator uiAuthenticator) {
    }
}
