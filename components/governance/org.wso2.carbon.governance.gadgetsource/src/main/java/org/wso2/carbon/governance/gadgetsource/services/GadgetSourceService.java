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
package org.wso2.carbon.governance.gadgetsource.services;

import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.governance.gadgetsource.util.CommonUtil;
import org.wso2.carbon.registry.common.services.RegistryAbstractAdmin;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.extensions.handlers.utils.WSDLProcessor;
import org.wso2.carbon.governance.gadgetsource.beans.*;
import org.wso2.carbon.governance.gadgetsource.util.CommonUtil;
import org.wso2.carbon.governance.gadgetsource.util.Populator;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;

public class GadgetSourceService extends RegistryAbstractAdmin {
    /*
    1. List of stored Services
    2. List of stored Schema (may be sorted as; mostly shared schema first..)
    3. Services belong to a particular lifecycle state (e.g. List all services in "tested" lifecycle state)
    4. Pie chart of services split by lifecycle
    5. Analysis of schema sharing (percentage of WSDLs that share schemas)
    6. histogram of how many services are on v1, v2, v3, etc */

    public LifecyclePiechartGadgetBean getLifecyclePiechartGadgetData() throws RegistryException
    {

        Registry registry = getRootRegistry();
        LifecycleInfoBean[] lifecycles = Populator.populateLifecycle(registry);

        // creating dummy data if no lifecycle exists..
        if (lifecycles == null || lifecycles.length == 0) {
            lifecycles = Populator.populateLifecycleDummyData();
        }

        LifecyclePiechartGadgetBean data = new LifecyclePiechartGadgetBean();
        data.setLifecycles(lifecycles);

        return data;
    }

    public SchemaSharingInfoGadgetBean getSchemaSharingInfoGadgetData()
    {
        return null;
    }

    public ServiceInfoGadgetBean getServiceInfoGadgetData()
    {
        return null;
    }

    public ServiceVersionHistogramGadgetBean getServiceVersionHistogramGadgetData()
    {
        return null;
    }
}
