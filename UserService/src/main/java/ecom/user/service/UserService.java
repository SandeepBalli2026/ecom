package ecom.user.service;

import ecom.user.dto.request.CreateUserRequest;
import ecom.user.dto.request.UpdateUserRequest;
import ecom.user.dto.response.UserResponse;
import java.util.List;

/** Use-case contract for User CRUD operations. */
public interface UserService {

    UserResponse createUser(CreateUserRequest request);

    UserResponse getUserById(Long id);

    List<UserResponse> getAllUsers();

    UserResponse updateUser(Long id, UpdateUserRequest request);

    void deleteUser(Long id);
}
