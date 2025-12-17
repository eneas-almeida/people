package org.people.domain.exception;

public abstract class PeopleException extends RuntimeException {

    private final String code;
    private final String details;

    protected PeopleException(String message) {
        super(message);
        this.code = this.getClass().getSimpleName();
        this.details = null;
    }

    protected PeopleException(String message, String code) {
        super(message);
        this.code = code;
        this.details = null;
    }

    protected PeopleException(String message, String code, String details) {
        super(message);
        this.code = code;
        this.details = details;
    }

    protected PeopleException(String message, Throwable cause) {
        super(message, cause);
        this.code = this.getClass().getSimpleName();
        this.details = null;
    }

    protected PeopleException(String message, String code, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.details = null;
    }

    protected PeopleException(String message, String code, String details, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.details = details;
    }

    public String getCode() {
        return code;
    }

    public String getDetails() {
        return details;
    }

    public abstract int getStatusCode();
}