package auth.res_server.demo.service.impl;

import auth.res_server.demo.domain.Role;
import auth.res_server.demo.domain.User;
import auth.res_server.demo.dto.user.CreateUser;
import auth.res_server.demo.dto.user.UpdateUser;
import auth.res_server.demo.dto.user.UserResponse;
import auth.res_server.demo.repository.RoleRepository;
import auth.res_server.demo.repository.UserRepository;
import auth.res_server.demo.service.EmailVerificationService;
import auth.res_server.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService;

    @Override
    public List<UserResponse> getAll() {
        return List.of();
    }

    @Override
    public UserResponse getById(String id) {
        return null;
    }

    @Override
    public void createUser(CreateUser request) {
        // Check duplicates
        if (userRepository.existsByUsername(request.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }
        List<Role> roleEntities = Optional.ofNullable(request.roles())
                .filter(roles -> !roles.isEmpty())
                .map(roles -> roles.stream()
                        .map(this::toFullRoleName)
                        .distinct()
                        .map(fullName -> roleRepository.findByName(fullName)
                                .orElseThrow(() -> new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Role not found: " + fullName
                                )))
                        .toList())
                .orElseGet(() -> {
                    // Default fallback
                    return List.of(roleRepository.findByName("ROLE_USER")
                            .orElseThrow(() -> new ResponseStatusException(
                                    HttpStatus.INTERNAL_SERVER_ERROR,
                                    "Default role ROLE_USER not found"
                            )));
                });
        // Generate UUID (if not using @PrePersist in entity)
        String uuid = UUID.randomUUID().toString();

        // Build user using Builder pattern
        User user = User.builder()
                .uuid(uuid)
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .email(request.email())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .phoneNumber(request.phoneNumber())
                .gender(request.gender())
                .dob(request.dob())
                .provider("local")
                .roles(roleEntities)
                .enabled(true)
                .emailVerified(false)
                .build();

        User savedUser = userRepository.save(user);

        // Send verification email
        emailVerificationService.sendVerificationEmail(savedUser);
    }

    @Override
    public void updateUserById(UpdateUser updateUser, String id) {
        // TODO: Implement user update
    }

    @Override
    public void deleteUserById(String id) {
        // TODO: Implement user deletion
    }

    private String toFullRoleName(String input) {
        if (input == null) return null;
        String trimmed = input.trim().toUpperCase();
        return trimmed.startsWith("ROLE_") ? trimmed : "ROLE_" + trimmed;
    }
}
