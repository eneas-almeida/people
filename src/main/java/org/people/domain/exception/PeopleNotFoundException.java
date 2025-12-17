package org.people.domain.exception;

import io.grpc.Status;

public class PeopleNotFoundException extends PeopleException {

    private static final String DEFAULT_CODE = "PEOPLE_NOT_FOUND";
    private static final int STATUS_CODE = Status.NOT_FOUND.getCode().value();

    public PeopleNotFoundException(String message) {
        super(message, DEFAULT_CODE);
    }

    public PeopleNotFoundException(String message, String details) {
        super(message, DEFAULT_CODE, details);
    }

    public PeopleNotFoundException(Integer peopleId) {
        super(String.format("People with id %d not found", peopleId), DEFAULT_CODE);
    }

    public PeopleNotFoundException(String message, Throwable cause) {
        super(message, DEFAULT_CODE, cause);
    }

    @Override
    public int getStatusCode() {
        return STATUS_CODE;
    }
}