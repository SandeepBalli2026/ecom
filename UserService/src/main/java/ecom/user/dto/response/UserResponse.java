package ecom.user.dto.response;

import java.util.Date;

/**
 * Response body for every endpoint that returns a user. Deliberately omits
 * {@code password} so it can never leave the service through the API.
 */
public record UserResponse(
        Long id,
        String name,
        String phoneNo,
        String email,
        Date createdAt,
        Date updatedAt,
        Boolean isActive
) {
}
