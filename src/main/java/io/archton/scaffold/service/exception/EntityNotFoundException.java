package io.archton.scaffold.service.exception;

/**
 * Thrown when an entity is not found.
 */
public class EntityNotFoundException extends RuntimeException {

    private final String entityType;
    private final Long entityId;

    public EntityNotFoundException(String entityType, Long entityId) {
        super(entityType + " with ID " + entityId + " not found.");
        this.entityType = entityType;
        this.entityId = entityId;
    }

    public String getEntityType() {
        return entityType;
    }

    public Long getEntityId() {
        return entityId;
    }
}
