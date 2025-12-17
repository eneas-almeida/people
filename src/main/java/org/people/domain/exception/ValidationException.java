package org.people.domain.exception;

import io.grpc.Status;

import java.util.Map;

public class ValidationException extends PeopleException {

    private static final String DEFAULT_CODE = "VALIDATION_ERROR";
    private static final int STATUS_CODE = Status.INVALID_ARGUMENT.getCode().value();

    private final Map<String, String> fieldErrors;

    public ValidationException(String message) {
        super(message, DEFAULT_CODE);
        this.fieldErrors = Map.of();
    }

    public ValidationException(String message, Map<String, String> fieldErrors) {
        super(message, DEFAULT_CODE, buildDetails(fieldErrors));
        this.fieldErrors = fieldErrors;
    }

    public ValidationException(String message, String field, String error) {
        super(message, DEFAULT_CODE, String.format("%s: %s", field, error));
        this.fieldErrors = Map.of(field, error);
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }

    @Override
    public int getStatusCode() {
        return STATUS_CODE;
    }

    private static String buildDetails(Map<String, String> fieldErrors) {
        if (fieldErrors == null || fieldErrors.isEmpty()) {
            return null;
        }
        return fieldErrors.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .reduce((a, b) -> a + ", " + b)
                .orElse(null);
    }
}