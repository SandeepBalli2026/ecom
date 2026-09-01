package ecom.user.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

/** Uniform error response body produced by {@link GlobalExceptionHandler}. */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {

    private final Instant timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final String path;
    private final List<FieldValidationError> fieldErrors;

    /** One rejected field on a validation failure. */
    public record FieldValidationError(String field, String message) {
    }
}
