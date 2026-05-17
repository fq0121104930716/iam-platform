package iam.platform.admin.domain.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import iam.platform.common.model.exception.ConflictException;
import iam.platform.admin.domain.repository.PersonRepository;
import iam.platform.admin.domain.service.PersonUniquenessService;

@Service
@RequiredArgsConstructor
public class PersonUniquenessServiceImpl implements PersonUniquenessService {

    private final PersonRepository personRepository;

    @Override
    public void ensureUsernameUnique(String username) {
        if (username != null && personRepository.existsByUsername(username)) {
            throw new ConflictException("Username already exists: " + username);
        }
    }

    @Override
    public void ensureEmailUnique(String email, Long excludePersonId) {
        if (email == null || email.isBlank()) {
            return;
        }
        var existing = personRepository.findByEmail(email);
        if (existing.isPresent() && !existing.get().getId().equals(excludePersonId)) {
            throw new ConflictException("Email already exists: " + email);
        }
    }

    @Override
    public void ensurePhoneUnique(String phone, Long excludePersonId) {
        if (phone == null || phone.isBlank()) {
            return;
        }
        var existing = personRepository.findByPhone(phone);
        if (existing.isPresent() && !existing.get().getId().equals(excludePersonId)) {
            throw new ConflictException("Phone already exists: " + phone);
        }
    }
}
