package ecom.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request body for {@code POST /api/users}. Carries and validates the client-supplied
 * fields; {@code id}, timestamps and {@code isActive} are never accepted from the client.
 */
public record CreateUserRequest(

        @NotBlank
        @Size(max = 100)
        String name,

        @NotBlank
        @Pattern(regexp = "^\\+?[0-9]{10,15}$",
                message = "phoneNo must be 10-15 digits, optional leading +")
        String phoneNo,

        @NotBlank
        @Email
        @Size(max = 255)
        String email,

        @NotBlank
        @Size(min = 8, max = 100)
        String password
) {

    @Override
    public String toString() {
        return "CreateUserRequest{name=%s, phoneNo=%s, email=%s, password=****}"
                .formatted(name, phoneNo, email);
    }
}
