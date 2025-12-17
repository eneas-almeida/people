package org.people.domain.exception;

import io.grpc.Status;

public class BusinessRuleException extends PeopleException {

    private static final String DEFAULT_CODE = "BUSINESS_RULE_VIOLATION";
    private static final int STATUS_CODE = Status.FAILED_PRECONDITION.getCode().value();

    public BusinessRuleException(String message) {
        super(message, DEFAULT_CODE);
    }

    public BusinessRuleException(String message, String code) {
        super(message, code);
    }

    public BusinessRuleException(String message, String code, String details) {
        super(message, code, details);
    }

    public BusinessRuleException(String message, Throwable cause) {
        super(message, DEFAULT_CODE, cause);
    }

    @Override
    public int getStatusCode() {
        return STATUS_CODE;
    }
}