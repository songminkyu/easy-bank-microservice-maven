package io.github.songminkyu.accounthex.domain.exception;

/**
 * Exception thrown when an entity is not found.
 */
public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String message) {
        super(message);
    }

    public EntityNotFoundException(Class<?> entityClass, String fieldName, String fieldValue) {
        super(String.format("%s not found with %s: %s", entityClass.getSimpleName(), fieldName, fieldValue));
    }

    public EntityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}