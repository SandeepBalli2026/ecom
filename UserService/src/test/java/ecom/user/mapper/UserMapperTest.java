package ecom.user.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import ecom.user.dto.request.CreateUserRequest;
import ecom.user.dto.request.UpdateUserRequest;
import ecom.user.dto.response.UserResponse;
import ecom.user.entity.User;
import java.util.Date;
import org.junit.jupiter.api.Test;

class UserMapperTest {

    private final UserMapper mapper = new UserMapper();

    @Test
    void toEntity_copiesClientFields_andLeavesGeneratedFieldsUnset() {
        CreateUserRequest request = new CreateUserRequest("Ada Lovelace", "+911234567890",
                "ada@example.com", "password1");

        User entity = mapper.toEntity(request);

        assertThat(entity.getName()).isEqualTo("Ada Lovelace");
        assertThat(entity.getPhoneNo()).isEqualTo("+911234567890");
        assertThat(entity.getEmail()).isEqualTo("ada@example.com");
        assertThat(entity.getPassword()).isEqualTo("password1");
        assertThat(entity.getId()).isNull();
        assertThat(entity.getCreatedAt()).isNull();
        assertThat(entity.getUpdatedAt()).isNull();
    }

    @Test
    void toResponse_copiesSafeFields_andExposesNoPassword() {
        Date now = new Date();
        User user = User.builder()
                .id(7L)
                .name("Grace Hopper")
                .phoneNo("1234567890")
                .email("grace@example.com")
                .password("secret-hash")
                .createdAt(now)
                .updatedAt(now)
                .isActive(true)
                .build();

        UserResponse response = mapper.toResponse(user);

        assertThat(response.id()).isEqualTo(7L);
        assertThat(response.name()).isEqualTo("Grace Hopper");
        assertThat(response.phoneNo()).isEqualTo("1234567890");
        assertThat(response.email()).isEqualTo("grace@example.com");
        assertThat(response.createdAt()).isEqualTo(now);
        assertThat(response.updatedAt()).isEqualTo(now);
        assertThat(response.isActive()).isTrue();
        assertThat(response.toString()).doesNotContain("secret-hash");
    }

    @Test
    void updateEntity_overwritesMutableFields_andLeavesIdentityAndCreatedAtUntouched() {
        Date created = new Date(0L);
        User user = User.builder()
                .id(3L)
                .name("Old Name")
                .phoneNo("1111111111")
                .email("old@example.com")
                .password("old-pass")
                .createdAt(created)
                .updatedAt(created)
                .isActive(true)
                .build();

        mapper.updateEntity(new UpdateUserRequest("New Name", "2222222222",
                "new@example.com", "new-pass1"), user);

        assertThat(user.getName()).isEqualTo("New Name");
        assertThat(user.getPhoneNo()).isEqualTo("2222222222");
        assertThat(user.getEmail()).isEqualTo("new@example.com");
        assertThat(user.getPassword()).isEqualTo("new-pass1");
        assertThat(user.getId()).isEqualTo(3L);
        assertThat(user.getCreatedAt()).isEqualTo(created);
        assertThat(user.getIsActive()).isTrue();
    }
}
