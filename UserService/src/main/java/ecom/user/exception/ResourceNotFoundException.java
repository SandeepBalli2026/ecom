package ecom.user.exception;

/** Thrown when a user id does not resolve to an active user. Mapped to HTTP 404. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException forUser(Long id) {
        return new ResourceNotFoundException("User not found with id: " + id);
    }
}
