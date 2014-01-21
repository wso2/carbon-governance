package org.wso2.carbon.governance.registry.eventing.handlers.utils.events;

import org.wso2.carbon.registry.common.eventing.RegistryEvent;

public class LifeCycleApprovedEvent<T> extends RegistryEvent<T> {

	private String resourcePath = null;

	public static final String EVENT_NAME = "LifeCycleApproved";

	public LifeCycleApprovedEvent() {
		super();
	}

	/**
	 * Construct the Registry Event by using the message
	 * 
	 * @param message
	 *            any Object
	 */
	public LifeCycleApprovedEvent(T message) {
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