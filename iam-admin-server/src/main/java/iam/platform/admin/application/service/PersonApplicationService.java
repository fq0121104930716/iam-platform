package iam.platform.admin.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import iam.platform.common.dto.request.CreatePersonRequest;
import iam.platform.common.dto.request.UpdatePersonRequest;
import iam.platform.common.dto.response.PersonResponse;
import iam.platform.common.model.annotation.AuditLog;
import iam.platform.admin.domain.model.entity.Person;
import iam.platform.common.model.enums.AuditEventType;
import iam.platform.common.model.exception.PersonNotFoundException;
import iam.platform.common.model.valueobject.Password;
import iam.platform.admin.domain.repository.PersonRepository;
import iam.platform.admin.domain.service.PersonUniquenessService;
import iam.platform.common.api.PageResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class PersonApplicationService {

    private final PersonRepository personRepository;
    private final PasswordEncoder passwordEncoder;
    private final PersonUniquenessService personUniquenessService;

    @Transactional
    @AuditLog(value = AuditEventType.PERSON_CREATED, resourceType = "person",
            action = "创建自然人 #{#request.username}")
    public PersonResponse createPerson(CreatePersonRequest request) {
        // Uniqueness validation via domain service
        personUniquenessService.ensureUsernameUnique(request.getUsername());
        personUniquenessService.ensureEmailUnique(request.getEmail(), null);
        personUniquenessService.ensurePhoneUnique(request.getPhone(), null);

        // Password validation + hashing via value object
        Password password =
                Password.fromRawPassword(request.getPassword(), passwordEncoder::encode);

        // Domain factory method handles construction with invariants
        Person person = Person.register(request.getUsername(), request.getEmail(),
                request.getPhone(), password, request.getNickname(), request.getAvatarUrl());

        person = personRepository.save(person);
        log.info("Person created: {} (code: {})", person.getUsername(), person.getPersonCode());
        return toResponse(person);
    }

    public PersonResponse getPerson(Long id) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new PersonNotFoundException("Person not found: " + id));
        return toResponse(person);
    }

    @Transactional
    @AuditLog(value = AuditEventType.PERSON_UPDATED, resourceType = "person",
            action = "更新自然人 ID=#{#id}")
    public PersonResponse updatePerson(Long id, UpdatePersonRequest request) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new PersonNotFoundException("Person not found: " + id));

        // Profile update via domain method
        person.updateProfile(request.getNickname(), request.getAvatarUrl());

        // Email change with uniqueness check
        if (request.getEmail() != null && !request.getEmail().equals(person.getEmail())) {
            personUniquenessService.ensureEmailUnique(request.getEmail(), person.getId());
            person.changeEmail(request.getEmail());
        }

        // Phone change with uniqueness check
        if (request.getPhone() != null && !request.getPhone().equals(person.getPhone())) {
            personUniquenessService.ensurePhoneUnique(request.getPhone(), person.getId());
            person.changePhone(request.getPhone());
        }

        // Enabled state
        if (request.getEnabled() != null) {
            if (request.getEnabled()) {
                person.enable();
            } else {
                person.disable();
            }
        }

        person = personRepository.save(person);
        log.info("Person updated: {}", person.getUsername());
        return toResponse(person);
    }

    @Transactional
    @AuditLog(value = AuditEventType.PERSON_DELETED, resourceType = "person",
            action = "注销自然人 ID=#{#id}")
    public void deletePerson(Long id) {
        if (!personRepository.findById(id).isPresent()) {
            throw new PersonNotFoundException("Person not found: " + id);
        }
        personRepository.deleteById(id);
        log.info("Person deleted: {}", id);
    }

    public PageResponse<PersonResponse> listPersons(int page, int size) {
        Page<Person> personPage = personRepository.findAll(PageRequest.of(page, size));
        return PageResponse.of(personPage.getContent().stream().map(this::toResponse).toList(),
                personPage.getNumber(), personPage.getSize(), personPage.getTotalElements());
    }

    private PersonResponse toResponse(Person person) {
        return PersonResponse.builder().id(person.getId()).personCode(person.getPersonCode())
                .username(person.getUsername()).email(person.getEmail()).phone(person.getPhone())
                .nickname(person.getNickname()).avatarUrl(person.getAvatarUrl())
                .emailVerified(person.isEmailVerified()).phoneVerified(person.isPhoneVerified())
                .enabled(person.isEnabled()).accountLocked(person.isAccountLocked())
                .lastLoginAt(person.getLastLoginAt()).createdAt(person.getCreatedAt())
                .updatedAt(person.getUpdatedAt()).build();
    }
}
