package org.wso2.carbon.governance.rest.api.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.api.RegistryException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class RegistryExceptionHandler implements ExceptionMapper<RegistryException> {

    private final Log log = LogFactory.getLog(RegistryExceptionHandler.class);

    @Override
    public Response toResponse(RegistryException exception) {
        log.error("Exception during service invocation ", exception);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
}
