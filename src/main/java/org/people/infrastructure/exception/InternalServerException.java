package org.people.infrastructure.exception;

import io.grpc.Status;
import org.people.domain.exception.PeopleException;

public class InternalServerException extends PeopleException {

    private static final String DEFAULT_CODE = "INTERNAL_SERVER_ERROR";
    private static final int STATUS_CODE = Status.INTERNAL.getCode().value();

    public InternalServerException(String message) {
        super(message, DEFAULT_CODE);
    }

    public InternalServerException(String message, String code) {
        super(message, code);
    }

    public InternalServerException(String message, Throwable cause) {
        super(message, DEFAULT_CODE, cause);
    }

    public InternalServerException(String message, String code, Throwable cause) {
        super(message, code, cause);
    }

    @Override
    public int getStatusCode() {
        return STATUS_CODE;
    }
}