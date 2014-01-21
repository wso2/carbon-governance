package org.wso2.carbon.governance.registry.extensions.interfaces;

import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;

import java.util.Map;

/**
 * This is the interface that is used to write custom executors to lifecycles
 * Executors are code segments that will run once a transition happens
 * */
public interface Execution {

    /**
     * This method is called when the execution class is initialized.
     * All the execution classes are initialized only once.
     *
     * @param parameterMap Static parameter map given by the user. These are the parameters that have been given in the
     *                     lifecycle configuration as the parameters of the executor.
     *
     *                     Eg:- <execution forEvent="Promote" class="org.wso2.carbon.governance.registry.extensions.executors.ServiceVersionExecutor">
                                    <parameter name="currentEnvironment" value="/_system/governance/trunk/"/>
                                    <parameter name="targetEnvironment" value="/_system/governance/branches/testing/"/>
                                    <parameter name="service.mediatype" value="application/vnd.wso2-service+xml"/>
                                </execution>

                           The parameters defined here are passed to the executor using this method.
     * */
    void init(Map parameterMap);

    /**
     * This method will be called when the invoke() method of the default lifecycle implementation is called.
     * Execution logic should reside in this method since the default lifecycle implementation will determine
     * the execution output by looking at the output of this method.
     *
     * @param context The request context that was generated from the registry core for the invoke() call.
     *                The request context contains the resource, resource path and other variables generated during the initial call.
     * @param currentState The current lifecycle state.
     * @param targetState The target lifecycle state.
     * @return Returns whether the execution was successful or not.
     * */
    boolean execute(RequestContext context,String currentState,String targetState);
}
