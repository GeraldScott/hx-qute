package io.archton.scaffold.service.exception;

/**
 * Thrown when a unique constraint would be violated.
 */
public class UniqueConstraintException extends RuntimeException {

    private final String fieldName;
    private final Object fieldValue;

    public UniqueConstraintException(String fieldName, Object fieldValue, String message) {
        super(message);
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getFieldValue() {
        return fieldValue;
    }
}
