/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.governance.gadgets.resourceimpact.util;

import org.wso2.carbon.governance.gadgets.resourceimpact.beans.AssociationBean;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.util.ArrayList;
import java.util.List;

public class CommonUtil {

    public static AssociationBean[] getAssociations(String path, Registry registry,
                                                    boolean isDependency) throws RegistryException {
        List<AssociationBean> associationBeanList = new ArrayList<AssociationBean>();

        Association[] associations = registry.getAllAssociations(path);

        for (Association association : associations) {
            if ((!isDependency && path.equals(association.getSourcePath()) &&
                    !association.getDestinationPath().contains(";version:")) ||
                    (isDependency && association.getAssociationType().equals("depends") &&
                            path.equals(association.getDestinationPath()) &&
                            !association.getSourcePath().contains(";version:"))) {
                AssociationBean bean = new AssociationBean();
                bean.setAssociationType(association.getAssociationType());
                bean.setDestinationPath(association.getDestinationPath());
                bean.setSourcePath(association.getSourcePath());

                associationBeanList.add(bean);
            }
        }

        return associationBeanList.toArray(new AssociationBean[associationBeanList.size()]);
    }
}
