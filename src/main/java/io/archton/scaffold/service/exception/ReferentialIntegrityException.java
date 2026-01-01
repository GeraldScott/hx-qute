package io.archton.scaffold.service.exception;

/**
 * Thrown when referential integrity would be violated.
 */
public class ReferentialIntegrityException extends RuntimeException {

    private final String entityType;
    private final Long entityId;
    private final String referencedType;
    private final Object referenceInfo;

    public ReferentialIntegrityException(String entityType, Long entityId,
            String referencedType, Object referenceInfo, String message) {
        super(message);
        this.entityType = entityType;
        this.entityId = entityId;
        this.referencedType = referencedType;
        this.referenceInfo = referenceInfo;
    }

    public String getEntityType() {
        return entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public String getReferencedType() {
        return referencedType;
    }

    public Object getReferenceInfo() {
        return referenceInfo;
    }
}
