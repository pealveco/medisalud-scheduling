package co.com.medisalud.model.common.exceptions;

/**
 * Exception thrown when a required domain resource does not exist.
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Creates a not-found exception for a resource and identifier.
     *
     * @param resourceName resource type
     * @param resourceId missing resource identifier
     */
    public ResourceNotFoundException(String resourceName, Object resourceId) {
        super(resourceName + " not found: " + resourceId);
    }
}
