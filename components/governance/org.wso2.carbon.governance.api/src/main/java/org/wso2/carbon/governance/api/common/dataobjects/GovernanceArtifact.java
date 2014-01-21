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
package org.wso2.carbon.governance.api.common.dataobjects;

import org.wso2.carbon.governance.api.exception.GovernanceException;

import javax.xml.namespace.QName;
import java.util.Map;


public interface GovernanceArtifact {
    /**
     * Returns the QName of the artifact.
     *
     * @return the QName of the artifact
     */
    QName getQName();

    /**
     * Returns the id of the artifact
     *
     * @return the id
     */
    String getId();

    /**
     * Set the id
     *
     * @param id the id
     */
    void setId(String id);

    /**
     * Returns the path of the artifact, need to save the artifact before
     * getting the path.
     *
     * @return here we return the path of the artifact.
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException
     *          if an error occurred.
     */
    String getPath() throws GovernanceException;

    /**
     * Returns the name of the lifecycle associated with this artifact.
     *
     * @return the name of the lifecycle associated with this artifact.
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException
     *          if an error occurred.
     */
    String getLifecycleName() throws GovernanceException;

    /**
     * Associates the named lifecycle with the artifact
     *
     * @param name the name of the lifecycle to be associated with this artifact.
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException
     *          if an error occurred.
     */
    void attachLifecycle(String name) throws GovernanceException;

    /**
     * Returns the state of the lifecycle associated with this artifact.
     *
     * @return the state of the lifecycle associated with this artifact.
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException
     *          if an error occurred.
     */
    String getLifecycleState() throws GovernanceException;

    /**
     * Adding an attribute to the artifact. The artifact should be saved to get effect the change.
     * In the case of a single-valued attribute, this method will set or replace the existing
     * attribute with the provided value. In the case of a multi-valued attribute, this method will
     * append the provided value to the existing list.
     *
     * @param key   the key.
     * @param value the value.
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException
     *          throws if the operation failed.
     */
    void addAttribute(String key, String value) throws GovernanceException;

    /**
     * Set/Update an attribute with multiple values. The artifact should be saved to get effect the
     * change.
     *
     * @param key       the key
     * @param newValues the value
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException
     *          throws if the operation failed.
     */
    void setAttributes(String key, String[] newValues) throws GovernanceException;

    /**
     * Set/Update an attribute with a single value. The artifact should be saved to get effect the
     * change. This method will replace the existing attribute with the provided value. In the case
     * of a multi-valued attribute this will remove all existing values. If you want to append the
     * provided value to a list values of a multi-valued attribute, use the addAttribute method
     * instead.
     *
     * @param key      the key
     * @param newValue the value
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException
     *          throws if the operation failed.
     */
    void setAttribute(String key, String newValue) throws GovernanceException;

    /**
     * Returns the attribute of a given key.
     *
     * @param key the key
     * @return the value of the attribute, if there are more than one attribute for the key this
     *         returns the first value.
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException
     *          throws if the operation failed.
     */
    String getAttribute(String key) throws GovernanceException;

    /**
     * Returns the available attribute keys
     *
     * @return an array of attribute keys.
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException
     *          throws if the operation failed.
     */
    String[] getAttributeKeys() throws GovernanceException;

    /**
     * Returns the attribute values for a key.
     *
     * @param key the key.
     * @return attribute values for the key.
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException
     *          throws if the operation failed.
     */
    String[] getAttributes(String key) throws GovernanceException;

    /**
     * Remove attribute with the given key. The artifact should be saved to get effect the change.
     *
     * @param key the key
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException
     *          throws if the operation failed.
     */
    void removeAttribute(String key) throws GovernanceException;

    /**
     * Get dependencies of an artifacts. The artifacts should be saved, before calling this method.
     *
     * @return an array of dependencies of this artifact.
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException
     *          throws if the operation failed.
     */
    GovernanceArtifact[] getDependencies() throws GovernanceException;

    /**
     * Get dependents of an artifact. The artifacts should be saved, before calling this method.
     *
     * @return an array of artifacts that is dependent on this artifact.
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException
     *          throws if the operation failed.
     */
    GovernanceArtifact[] getDependents() throws GovernanceException;

    /**
     * Get all lifecycle actions for the current state of the lifecycle
     *
     * @return Action set which can be invoked
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException
     *          throws if the operation failed.
     */
    public String[] getAllLifecycleActions() throws GovernanceException;

    /**
     * Promote the artifact to the next state of the lifecycle
     *
     * @param action lifecycle action tobe invoked
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException
     *          throws if the operation failed.
     */
    void invokeAction(String action) throws GovernanceException;

    /**
     * Promote the artifact to the next state of the lifecycle
     *
     * @param action     lifecycle action tobe invoked
     * @param parameters extra parameters needed when promoting
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException
     *          throws if the operation failed.
     */
    void invokeAction(String action, Map<String, String> parameters) throws GovernanceException;

    /**
     * Retrieve name set of the checklist items
     *
     * @return Checklist item name set
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException
     *          throws if the operation failed.
     */
    String[] getAllCheckListItemNames() throws GovernanceException;

    /**
     * Check the checklist item
     *
     * @param order order of the checklist item need to checked
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException
     *          throws if the operation failed.
     */
    void checkLCItem(int order) throws GovernanceException;

    /**
     * Check whether the given ordered lifecycle checklist item is checked or not
     *
     * @param order order of the checklist item need to unchecked
     * @return whether the given ordered lifecycle checklist item is checked or not
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException
     *          throws if the operation failed.
     */
    public boolean isLCItemChecked(int order) throws GovernanceException;

    /**
     * Un-check the checklist item
     *
     * @param order order of the checklist item need to unchecked
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException
     *          throws if the operation failed.
     */
    void uncheckLCItem(int order) throws GovernanceException;

    /**
     * Retrieve action set which need votes
     *
     * @return Action set which can vote
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException
     *          throws if the operation failed.
     */
    public String[] getAllVotingItems() throws GovernanceException;

    /**
     * Vote for an action
     *
     * @param order order of the action which need to be voted
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException
     *          throws if the operation failed.
     */
    public void vote(int order) throws GovernanceException;

    /**
     * Check whether the current user voted for given order event
     *
     * @param order order of the action which need to be voted
     * @return whether the current user voted for the given order event
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException
     *          throws if the operation failed.
     */
    public boolean isVoted(int order) throws GovernanceException;

    /**
     * Unvote for an action
     *
     * @param order order of the action which need to be unvoted
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException
     *          throws if the operation failed.
     */
    public void unvote(int order) throws GovernanceException;

}
