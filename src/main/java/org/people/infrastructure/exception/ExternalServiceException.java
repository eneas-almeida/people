package org.people.infrastructure.exception;

import io.grpc.Status;
import org.people.domain.exception.PeopleException;

public class ExternalServiceException extends PeopleException {

    private static final String DEFAULT_CODE = "EXTERNAL_SERVICE_ERROR";
    private static final int STATUS_CODE = Status.UNAVAILABLE.getCode().value();

    private final String serviceName;
    private final int httpStatusCode;

    public ExternalServiceException(String message, String serviceName) {
        super(message, DEFAULT_CODE);
        this.serviceName = serviceName;
        this.httpStatusCode = 0;
    }

    public ExternalServiceException(String message, String serviceName, int httpStatusCode) {
        super(message, DEFAULT_CODE, String.format("HTTP Status: %d", httpStatusCode));
        this.serviceName = serviceName;
        this.httpStatusCode = httpStatusCode;
    }

    public ExternalServiceException(String message, String serviceName, Throwable cause) {
        super(message, DEFAULT_CODE, cause);
        this.serviceName = serviceName;
        this.httpStatusCode = 0;
    }

    public ExternalServiceException(String message, String serviceName, int httpStatusCode, Throwable cause) {
        super(message, DEFAULT_CODE, String.format("HTTP Status: %d", httpStatusCode), cause);
        this.serviceName = serviceName;
        this.httpStatusCode = httpStatusCode;
    }

    public String getServiceName() {
        return serviceName;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    @Override
    public int getStatusCode() {
        return STATUS_CODE;
    }
}