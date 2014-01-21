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
package org.wso2.carbon.governance.list.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.services.dataobjects.ServiceImpl;
import org.wso2.carbon.governance.api.util.GovernanceConstants;
import org.wso2.carbon.governance.list.beans.ServiceBean;
import org.wso2.carbon.governance.list.util.filter.FilterGenericArtifact;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.user.core.UserStoreException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ListServiceUtil {
    private static final Log log = LogFactory.getLog(ListServiceUtil.class);
    
    public static ServiceBean fillServiceBean(UserRegistry registry,
                                          String criteria) throws RegistryException {
        ServiceBean bean = new ServiceBean();
        GovernanceArtifact[] artifacts = new GovernanceArtifact[0];
        String defaultServicePath = RegistryUtils.getRelativePathToOriginal(
                registry.getRegistryContext().getServicePath(),
                RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH);

        try {
            artifacts = (new FilterGenericArtifact(criteria, registry,"service")).getArtifacts();
        } catch (RegistryException e) {
            log.error("An error occurred while obtaining the list of services.", e);
        }
        String[] path = new String[artifacts.length];
        String[] name = new String[artifacts.length];
        String[] namespace = new String[artifacts.length];
        String[] LCName = new String[artifacts.length];
        String[] LCState = new String[artifacts.length];
        String[] version = new String[artifacts.length];
        boolean[] canDelete = new boolean[artifacts.length];

        for (int i = 0; i < artifacts.length; i++) {
            bean.increment();
            if (registry.getUserRealm() != null && registry.getUserName() != null) {
                try {
                    canDelete[i] =
                            registry.getUserRealm().getAuthorizationManager().isUserAuthorized(
                                    registry.getUserName(),
                                    RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + path[i],
                                    ActionConstants.DELETE);
                } catch (UserStoreException e) {
                    canDelete[i] = false;
                }
            } else {
                canDelete[i] = false;
            }
            path[i] = ((ServiceImpl)artifacts[i]).getArtifactPath();
            version[i] = artifacts[i].getAttribute(GovernanceConstants.SERVICE_VERSION_ATTRIBUTE);
            name[i] = artifacts[i].getAttribute(GovernanceConstants.SERVICE_NAME_ATTRIBUTE);
            namespace[i] = artifacts[i].getAttribute(GovernanceConstants.SERVICE_NAMESPACE_ATTRIBUTE);
            LCName[i] = ((ServiceImpl)artifacts[i]).getLcName();
            LCState[i] = ((ServiceImpl)artifacts[i]).getLcState();

        }
        bean.setDefaultServicePath(defaultServicePath);
        bean.setNames(name);
        bean.setNamespace(namespace);
        bean.setPath(path);
        bean.setLCName(LCName);
        bean.setLCState(LCState);
        bean.setCanDelete(canDelete);
        bean.setVersion(version);
        sortServicesByName(bean);
        return bean;
    }

   /**
     * Sorts the services by name
     * @param bean  ServiceBean
     */
    private static void sortServicesByName(ServiceBean bean) {

        List<ServiceEntry> serviceEntryList = new ArrayList<ServiceEntry>();
        for(int i=0; i < bean.getPath().length; i++) {
            serviceEntryList.add(new ServiceEntry(bean.getPath()[i], bean.getNames()[i],
                    bean.getNamespace()[i], bean.getLCName()[i], bean.getLCState()[i],
                    bean.getVersion()[i],bean.getCanDelete()[i]));
        }

        Collections.sort(serviceEntryList, new Comparator<ServiceEntry>() {
            public int compare(ServiceEntry se1, ServiceEntry se2) {
                int res = StringComparatorUtil.compare(se2.name, se1.name);
                if (res != 0) return res;

//                return StringComparatorUtil.compare(RegistryUtils.getResourceName(RegistryUtils.getParentPath(se1.path)),
//                        (RegistryUtils.getResourceName(RegistryUtils.getParentPath(se2.path))));
                return StringComparatorUtil.compare(se1.version, se2.version);
            }
        });

        int i = 0;
        for(ServiceEntry se : serviceEntryList) {
            bean.getPath()[i] = se.path;
            bean.getNames()[i] = se.name;
            bean.getNamespace()[i] = se.namespace;
            bean.getLCName()[i]=se.lcname;
            bean.getLCState()[i]=se.lcstate;
            bean.getCanDelete()[i] = se.canDelete;
            bean.getVersion()[i] = se.version;
            i++;
        }

    }
    /**
     * This is a structure used to store a service entry prior to sorting**/
    private static class ServiceEntry {
        private String path,
                name,
                namespace,
                lcname,
                lcstate,
                version;
        private boolean canDelete;
        ServiceEntry(String path, String name, String namespace, String lcname, String lcstate,
                     String version, boolean canDelete) {
            this.path = path;
            this.name = name;
            this.namespace = namespace;
            this.lcname=lcname;
            this.lcstate=lcstate;
            this.canDelete = canDelete;
            this.version = version;
        }
    }
    
}
