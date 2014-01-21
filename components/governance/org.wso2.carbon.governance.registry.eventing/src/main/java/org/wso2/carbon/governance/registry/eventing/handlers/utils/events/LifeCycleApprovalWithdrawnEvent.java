package org.wso2.carbon.governance.registry.eventing.handlers.utils.events;

import org.wso2.carbon.registry.common.eventing.RegistryEvent;

public class LifeCycleApprovalWithdrawnEvent<T> extends RegistryEvent<T> {

	private String resourcePath = null;

	public static final String EVENT_NAME = "LifeCycleApprovalWithdrawn";

	public LifeCycleApprovalWithdrawnEvent() {
		super();
	}

	/**
	 * Construct the Registry Event by using the message
	 * 
	 * @param message
	 *            any Object
	 */
	public LifeCycleApprovalWithdrawnEvent(T message) {
		super(message);
	}

	public void setResourcePath(String resourcePath) {
		this.resourcePath = resourcePath;
		setTopic(TOPIC_SEPARATOR + EVENT_NAME + resourcePath);
		setOperationDetails(resourcePath, EVENT_NAME,
				RegistryEvent.ResourceType.UNKNOWN);
	}

	public String getResourcePath() {
		return resourcePath;
	}
}