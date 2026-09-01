package ecom.user.exception;

/** Thrown when an email or phone number already belongs to another user. Mapped to HTTP 409. */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }

    public static DuplicateResourceException email(String value) {
        return new DuplicateResourceException("A user already exists with email: " + value);
    }

    public static DuplicateResourceException phoneNo(String value) {
        return new DuplicateResourceException("A user already exists with phoneNo: " + value);
    }
}
