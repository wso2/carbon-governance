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
package org.wso2.carbon.governance.registry.extensions.handlers;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.utils.RegistryUtils;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

/**
 * A handler which can manage project assets.
 */
@SuppressWarnings("unused")
public class ProjectHandler extends Handler {

    private static Log log = LogFactory.getLog(ProjectHandler.class);

    public void put(RequestContext requestContext) throws RegistryException {
        Resource resource = requestContext.getResource();
        Object content = resource.getContent();
        String contentString;
        if (content instanceof String) {
            contentString = (String) content;
        } else {
            contentString = RegistryUtils.decodeBytes((byte[]) content);
        }
        OMElement payload;
        try {
            payload = AXIOMUtil.stringToOM(contentString);
        } catch (XMLStreamException e) {
            String msg = "Unable to serialize resource content";
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
        OMNamespace namespace = payload.getNamespace();
        String namespaceURI = namespace.getNamespaceURI();
        OMElement definition =
                payload.getFirstChildWithName(new QName(namespaceURI, "definition"));
        OMElement projectPath =
                definition.getFirstChildWithName(new QName(namespaceURI, "projectPath"));
        String projectMetadataPath = null;
        if (projectPath != null) {
            projectMetadataPath = RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                    projectPath.getText();
        }

        Resource metadataResource = requestContext.getRegistry().get(projectMetadataPath);

        String remainingWork = metadataResource.getProperty("Remaining Work");
        String scheduledWork = metadataResource.getProperty("Scheduled Work");
        String actualWork = metadataResource.getProperty("Actual Work");

        String remainingCost = metadataResource.getProperty("Remaining Cost");
        String scheduledCost = metadataResource.getProperty("Scheduled Cost");
        String actualCost = metadataResource.getProperty("Actual Cost");

        String duration = metadataResource.getProperty("Duration");
        String startDate = metadataResource.getProperty("Start Date");
        String endDate = metadataResource.getProperty("Finish Date");

        OMFactory factory = payload.getOMFactory();
        OMElement work = payload.getFirstChildWithName(new QName(namespaceURI, "work"));
        if (work == null) {
            work = factory.createOMElement("work", namespace, payload);
        }
        OMElement remainingWorkElement =
                work.getFirstChildWithName(new QName(namespaceURI, "remaining"));
        if (remainingWorkElement == null) {
            remainingWorkElement = factory.createOMElement("remaining", namespace, work);
        }
        remainingWorkElement.setText(remainingWork);
        OMElement actualWorkElement =
                work.getFirstChildWithName(new QName(namespaceURI, "actual"));
        if (actualWorkElement == null) {
            actualWorkElement = factory.createOMElement("actual", namespace, work);
        }
        actualWorkElement.setText(remainingWork);
        OMElement scheduledWorkElement =
                work.getFirstChildWithName(new QName(namespaceURI, "scheduled"));
        if (scheduledWorkElement == null) {
            scheduledWorkElement = factory.createOMElement("scheduled", namespace, work);
        }
        scheduledWorkElement.setText(remainingWork);

        OMElement cost = payload.getFirstChildWithName(new QName(namespaceURI, "cost"));
        if (cost == null) {
            cost = factory.createOMElement("cost", namespace, payload);
        }
        OMElement remainingCostElement =
                cost.getFirstChildWithName(new QName(namespaceURI, "remaining"));
        if (remainingCostElement == null) {
            remainingCostElement = factory.createOMElement("remaining", namespace, cost);
        }
        remainingCostElement.setText(remainingCost);
        OMElement actualCostElement =
                cost.getFirstChildWithName(new QName(namespaceURI, "actual"));
        if (actualCostElement == null) {
            actualCostElement = factory.createOMElement("actual", namespace, cost);
        }
        actualCostElement.setText(remainingCost);
        OMElement scheduledCostElement =
                cost.getFirstChildWithName(new QName(namespaceURI, "scheduled"));
        if (scheduledCostElement == null) {
            scheduledCostElement = factory.createOMElement("scheduled", namespace, cost);
        }
        scheduledCostElement.setText(remainingCost);

        OMElement timeline = payload.getFirstChildWithName(new QName(namespaceURI, "timeline"));
        if (timeline == null) {
            timeline = factory.createOMElement("timeline", namespace, payload);
        }
        OMElement durationElement =
                timeline.getFirstChildWithName(new QName(namespaceURI, "duration"));
        if (durationElement == null) {
            durationElement = factory.createOMElement("duration", namespace, timeline);
        }
        durationElement.setText(duration);
        OMElement startDateElement =
                timeline.getFirstChildWithName(new QName(namespaceURI, "startDate"));
        if (startDateElement == null) {
            startDateElement = factory.createOMElement("startDate", namespace, timeline);
        }
        startDateElement.setText(startDate);
        OMElement endDateElement =
                timeline.getFirstChildWithName(new QName(namespaceURI, "endDate"));
        if (endDateElement == null) {
            endDateElement = factory.createOMElement("endDate", namespace, timeline);
        }
        endDateElement.setText(endDate);

        resource.setContent(payload.toString());
    }
}
