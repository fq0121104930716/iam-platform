package iam.platform.admin.domain.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import iam.platform.common.model.exception.ConflictException;
import iam.platform.admin.domain.repository.UserRepository;
import iam.platform.admin.domain.service.UserUniquenessService;

@Service
@RequiredArgsConstructor
public class UserUniquenessServiceImpl implements UserUniquenessService {

    private final UserRepository userRepository;

    @Override
    public void ensureUsernameUnique(String username) {
        if (username != null && userRepository.existsByUsername(username)) {
            throw new ConflictException("Username already exists: " + username);
        }
    }

    @Override
    public void ensureEmailUnique(String email, Long excludeUserId) {
        if (email == null || email.isBlank()) {
            return;
        }
        var existing = userRepository.findByEmail(email);
        if (existing.isPresent() && !existing.get().getId().equals(excludeUserId)) {
            throw new ConflictException("Email already exists: " + email);
        }
    }

    @Override
    public void ensurePhoneUnique(String phone, Long excludeUserId) {
        if (phone == null || phone.isBlank()) {
            return;
        }
        var existing = userRepository.findByPhone(phone);
        if (existing.isPresent() && !existing.get().getId().equals(excludeUserId)) {
            throw new ConflictException("Phone already exists: " + phone);
        }
    }
}
