package ecom.user.mapper;

import ecom.user.dto.request.CreateUserRequest;
import ecom.user.dto.request.UpdateUserRequest;
import ecom.user.dto.response.UserResponse;
import ecom.user.entity.User;
import org.springframework.stereotype.Component;

/**
 * Owns all conversion between User DTOs and the {@link User} entity. Controllers and
 * services contain no field-copying logic.
 */
@Component
public class UserMapper {

    /**
     * Builds a new {@link User} from a create request. {@code id}, timestamps
     * (entity {@code @PrePersist}) and {@code isActive} (entity default) are left unset.
     */
    public User toEntity(CreateUserRequest request) {
        return User.builder()
                .name(request.name())
                .phoneNo(request.phoneNo())
                .email(request.email())
                .password(request.password())
                .build();
    }

    /** Maps an entity to its API representation, omitting {@code password}. */
    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getPhoneNo(),
                user.getEmail(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getIsActive()
        );
    }

    /**
     * Overwrites the mutable profile fields on a managed {@link User}. Leaves {@code id},
     * {@code createdAt} and {@code isActive} untouched; {@code updatedAt} is refreshed by
     * the entity {@code @PreUpdate} callback.
     */
    public void updateEntity(UpdateUserRequest request, User user) {
        user.setName(request.name());
        user.setPhoneNo(request.phoneNo());
        user.setEmail(request.email());
        user.setPassword(request.password());
    }
}
