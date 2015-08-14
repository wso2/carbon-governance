package org.wso2.carbon.governance.rest.api.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.api.exception.GovernanceException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class GovernanceExceptionHandler implements ExceptionMapper<GovernanceException> {

    private final Log log = LogFactory.getLog(GovernanceExceptionHandler.class);

    @Override
    public Response toResponse(GovernanceException exception) {
        log.error("Exception during service invocation ", exception);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
}
