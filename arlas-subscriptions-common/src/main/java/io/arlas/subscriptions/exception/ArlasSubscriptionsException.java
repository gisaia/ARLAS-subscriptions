package io.arlas.subscriptions.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class ArlasSubscriptionsException extends Exception {

    private static final long serialVersionUID = 1L;

    protected Response.Status status = Response.Status.INTERNAL_SERVER_ERROR;

    public ArlasSubscriptionsException() {
    }

    public ArlasSubscriptionsException(String message) {
        super(message);
    }

    public ArlasSubscriptionsException(String message, Throwable cause) {
        super(message, cause);
    }


}
