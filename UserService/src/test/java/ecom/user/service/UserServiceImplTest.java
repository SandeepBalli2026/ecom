package ecom.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ecom.user.dto.request.CreateUserRequest;
import ecom.user.dto.request.UpdateUserRequest;
import ecom.user.dto.response.UserResponse;
import ecom.user.entity.User;
import ecom.user.exception.DuplicateResourceException;
import ecom.user.exception.ResourceNotFoundException;
import ecom.user.mapper.UserMapper;
import ecom.user.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository, new UserMapper());
    }

    private CreateUserRequest createRequest() {
        return new CreateUserRequest("Ada", "+911234567890", "ada@example.com", "password1");
    }

    private UpdateUserRequest updateRequest() {
        return new UpdateUserRequest("Ada B", "+919999999999", "ada.b@example.com", "password2");
    }

    private User existingUser() {
        return User.builder()
                .id(1L)
                .name("Ada")
                .phoneNo("+911234567890")
                .email("ada@example.com")
                .password("password1")
                .isActive(true)
                .build();
    }

    @Test
    void createUser_persistsAndReturnsResponseWithoutPassword() {
        when(userRepository.existsByEmail("ada@example.com")).thenReturn(false);
        when(userRepository.existsByPhoneNo("+911234567890")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        UserResponse response = userService.createUser(createRequest());

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("ada@example.com");
        assertThat(response.toString()).doesNotContain("password1");
    }

    @Test
    void createUser_throwsWhenEmailAlreadyExists() {
        when(userRepository.existsByEmail("ada@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(createRequest()))
                .isInstanceOf(DuplicateResourceException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_throwsWhenPhoneAlreadyExists() {
        when(userRepository.existsByEmail("ada@example.com")).thenReturn(false);
        when(userRepository.existsByPhoneNo("+911234567890")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(createRequest()))
                .isInstanceOf(DuplicateResourceException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void getUserById_returnsMappedActiveUser() {
        when(userRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(existingUser()));

        assertThat(userService.getUserById(1L).email()).isEqualTo("ada@example.com");
    }

    @Test
    void getUserById_throwsWhenMissing() {
        when(userRepository.findByIdAndIsActiveTrue(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAllUsers_mapsEveryActiveUser() {
        when(userRepository.findAllByIsActiveTrue()).thenReturn(List.of(existingUser()));

        assertThat(userService.getAllUsers()).hasSize(1);
    }

    @Test
    void updateUser_appliesChangesWhenNoConflict() {
        User user = existingUser();
        when(userRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailAndIdNot("ada.b@example.com", 1L)).thenReturn(false);
        when(userRepository.existsByPhoneNoAndIdNot("+919999999999", 1L)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse response = userService.updateUser(1L, updateRequest());

        assertThat(response.name()).isEqualTo("Ada B");
        assertThat(user.getEmail()).isEqualTo("ada.b@example.com");
    }

    @Test
    void updateUser_throwsWhenEmailBelongsToAnotherUser() {
        when(userRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(existingUser()));
        when(userRepository.existsByEmailAndIdNot("ada.b@example.com", 1L)).thenReturn(true);

        assertThatThrownBy(() -> userService.updateUser(1L, updateRequest()))
                .isInstanceOf(DuplicateResourceException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_throwsWhenUserMissing() {
        when(userRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(1L, updateRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteUser_softDeletesByDeactivatingAndSaving() {
        User user = existingUser();
        when(userRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(user));

        userService.deleteUser(1L);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getIsActive()).isFalse();
    }

    @Test
    void deleteUser_throwsWhenUserMissing() {
        when(userRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(1L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(userRepository, never()).save(any());
    }
}
