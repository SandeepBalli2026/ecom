package ecom.user.service;

import ecom.user.dto.request.CreateUserRequest;
import ecom.user.dto.request.UpdateUserRequest;
import ecom.user.dto.response.UserResponse;
import ecom.user.entity.User;
import ecom.user.exception.DuplicateResourceException;
import ecom.user.exception.ResourceNotFoundException;
import ecom.user.mapper.UserMapper;
import ecom.user.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Coordinates the User use cases: uniqueness checks, load/persist, and delegation to the
 * mapper and to domain behavior on {@link User}. Holds no mapping or utility logic.
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw DuplicateResourceException.email(request.email());
        }
        if (userRepository.existsByPhoneNo(request.phoneNo())) {
            throw DuplicateResourceException.phoneNo(request.phoneNo());
        }
        User saved = userRepository.save(userMapper.toEntity(request));
        return userMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        return userMapper.toResponse(getActiveUserOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAllByIsActiveTrue()
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = getActiveUserOrThrow(id);
        if (userRepository.existsByEmailAndIdNot(request.email(), id)) {
            throw DuplicateResourceException.email(request.email());
        }
        if (userRepository.existsByPhoneNoAndIdNot(request.phoneNo(), id)) {
            throw DuplicateResourceException.phoneNo(request.phoneNo());
        }
        userMapper.updateEntity(request, user);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public void deleteUser(Long id) {
        User user = getActiveUserOrThrow(id);
        user.deactivate();
        userRepository.save(user);
    }

    private User getActiveUserOrThrow(Long id) {
        return userRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> ResourceNotFoundException.forUser(id));
    }
}
