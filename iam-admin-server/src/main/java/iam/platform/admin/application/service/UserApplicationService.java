package iam.platform.admin.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import iam.platform.common.dto.request.CreateUserRequest;
import iam.platform.common.dto.request.UpdateUserRequest;
import iam.platform.common.dto.response.UserResponse;
import iam.platform.common.model.annotation.AuditLog;
import iam.platform.admin.domain.model.entity.User;
import iam.platform.common.model.enums.AuditEventType;
import iam.platform.common.model.exception.UserNotFoundException;
import iam.platform.admin.domain.repository.UserRepository;
import iam.platform.admin.domain.service.UserUniquenessService;
import iam.platform.common.api.PageResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserApplicationService {

    private final UserRepository userRepository;
    private final UserUniquenessService userUniquenessService;

    @Transactional
    @AuditLog(value = AuditEventType.USER_CREATED, resourceType = "user",
            action = "创建用户 #{#request.username}")
    public UserResponse createUser(CreateUserRequest request) {
        // Uniqueness validation via domain service
        userUniquenessService.ensureUsernameUnique(request.getUsername());
        userUniquenessService.ensureEmailUnique(request.getEmail(), null);
        userUniquenessService.ensurePhoneUnique(request.getPhone(), null);

        // Domain factory method (no password, credential created separately)
        User user = User.register(request.getUsername(), request.getEmail(), request.getPhone(),
                request.getNickname(), request.getAvatarUrl());

        user = userRepository.save(user);
        log.info("User created: {} (code: {})", user.getUsername(), user.getUserCode());
        return toResponse(user);
    }

    public UserResponse getUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + id));
        return toResponse(user);
    }

    @Transactional
    @AuditLog(value = AuditEventType.USER_UPDATED, resourceType = "user", action = "更新用户 ID=#{#id}")
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + id));

        // Profile update via domain method
        user.updateProfile(request.getNickname(), request.getAvatarUrl());

        // Email change with uniqueness check
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            userUniquenessService.ensureEmailUnique(request.getEmail(), user.getId());
            user.changeEmail(request.getEmail());
        }

        // Phone change with uniqueness check
        if (request.getPhone() != null && !request.getPhone().equals(user.getPhone())) {
            userUniquenessService.ensurePhoneUnique(request.getPhone(), user.getId());
            user.changePhone(request.getPhone());
        }

        // Enabled state
        if (request.getEnabled() != null) {
            if (request.getEnabled()) {
                user.enable();
            } else {
                user.disable();
            }
        }

        user = userRepository.save(user);
        log.info("User updated: {}", user.getUsername());
        return toResponse(user);
    }

    @Transactional
    @AuditLog(value = AuditEventType.USER_DELETED, resourceType = "user", action = "注销用户 ID=#{#id}")
    public void deleteUser(Long id) {
        if (!userRepository.findById(id).isPresent()) {
            throw new UserNotFoundException("User not found: " + id);
        }
        userRepository.deleteById(id);
        log.info("User deleted: {}", id);
    }

    public PageResponse<UserResponse> listUsers(int page, int size) {
        Page<User> userPage = userRepository.findAll(PageRequest.of(page, size));
        return PageResponse.of(userPage.getContent().stream().map(this::toResponse).toList(),
                userPage.getNumber(), userPage.getSize(), userPage.getTotalElements());
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder().id(user.getId()).userCode(user.getUserCode())
                .username(user.getUsername()).email(user.getEmail()).phone(user.getPhone())
                .nickname(user.getNickname()).avatarUrl(user.getAvatarUrl())
                .emailVerified(user.isEmailVerified()).phoneVerified(user.isPhoneVerified())
                .enabled(user.isEnabled()).accountLocked(user.isAccountLocked())
                .lastLoginAt(user.getLastLoginAt()).createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt()).build();
    }
}
