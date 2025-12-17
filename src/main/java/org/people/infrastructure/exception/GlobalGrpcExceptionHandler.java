package org.people.infrastructure.exception;

import io.grpc.*;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;
import org.people.domain.exception.BusinessRuleException;
import org.people.domain.exception.PeopleException;
import org.people.domain.exception.PeopleNotFoundException;
import org.people.domain.exception.ValidationException;
import org.people.infrastructure.logging.LogContext;
import org.people.infrastructure.logging.Logger;

import java.util.Map;

@GrpcAdvice
public class GlobalGrpcExceptionHandler {

    private static final Logger logger = Logger.getLogger(GlobalGrpcExceptionHandler.class);

    @GrpcExceptionHandler(PeopleNotFoundException.class)
    public Status handlePeopleNotFoundException(PeopleNotFoundException ex) {
        logger.warn("People not found - code: {}, message: {}", ex.getCode(), ex.getMessage());

        return Status.NOT_FOUND
                .withDescription(ex.getMessage())
                .withCause(ex);
    }

    @GrpcExceptionHandler(ValidationException.class)
    public Status handleValidationException(ValidationException ex) {
        LogContext.add("validation_errors", ex.getDetails());
        logger.warn("Validation error - code: {}, message: {}, errors: {}",
                ex.getCode(), ex.getMessage(), ex.getFieldErrors());

        return Status.INVALID_ARGUMENT
                .withDescription(ex.getMessage() + (ex.getDetails() != null ? " - " + ex.getDetails() : ""))
                .withCause(ex);
    }

    @GrpcExceptionHandler(BusinessRuleException.class)
    public Status handleBusinessRuleException(BusinessRuleException ex) {
        logger.warn("Business rule violation - code: {}, message: {}", ex.getCode(), ex.getMessage());

        return Status.FAILED_PRECONDITION
                .withDescription(ex.getMessage())
                .withCause(ex);
    }

    @GrpcExceptionHandler(ExternalServiceException.class)
    public Status handleExternalServiceException(ExternalServiceException ex) {
        LogContext.add("external_service", ex.getServiceName());
        LogContext.add("http_status_code", String.valueOf(ex.getHttpStatusCode()));

        logger.error("External service error - service: {}, code: {}, message: {}, httpStatus: {}",
                ex.getServiceName(), ex.getCode(), ex.getMessage(), ex.getHttpStatusCode(), ex);

        return Status.UNAVAILABLE
                .withDescription(String.format("External service '%s' is unavailable: %s",
                        ex.getServiceName(), ex.getMessage()))
                .withCause(ex);
    }

    @GrpcExceptionHandler(InternalServerException.class)
    public Status handleInternalServerException(InternalServerException ex) {
        LogContext.setError(ex.getClass().getSimpleName());
        LogContext.setErrorMessage(ex.getMessage());

        logger.error("Internal server error - code: {}, message: {}", ex.getCode(), ex.getMessage(), ex);

        return Status.INTERNAL
                .withDescription("An internal error occurred")
                .withCause(ex);
    }

    @GrpcExceptionHandler(PeopleException.class)
    public Status handlePeopleException(PeopleException ex) {
        LogContext.setError(ex.getClass().getSimpleName());
        LogContext.setErrorMessage(ex.getMessage());

        logger.error("People exception - code: {}, message: {}", ex.getCode(), ex.getMessage(), ex);

        Status.Code grpcCode = mapToGrpcCode(ex.getStatusCode());
        return Status.fromCode(grpcCode)
                .withDescription(ex.getMessage())
                .withCause(ex);
    }

    @GrpcExceptionHandler(Exception.class)
    public Status handleGenericException(Exception ex) {
        LogContext.setError(ex.getClass().getSimpleName());
        LogContext.setErrorMessage(ex.getMessage());

        logger.error("Unexpected error occurred", Map.of(
                "exception_type", ex.getClass().getName(),
                "message", ex.getMessage() != null ? ex.getMessage() : "No message"
        ), ex);

        return Status.INTERNAL
                .withDescription("An unexpected error occurred")
                .withCause(ex);
    }

    private Status.Code mapToGrpcCode(int statusCode) {
        for (Status.Code code : Status.Code.values()) {
            if (code.value() == statusCode) {
                return code;
            }
        }
        return Status.Code.UNKNOWN;
    }
}