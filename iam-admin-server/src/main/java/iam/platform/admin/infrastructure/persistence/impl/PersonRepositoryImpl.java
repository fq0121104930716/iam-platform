package iam.platform.admin.infrastructure.persistence.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import iam.platform.admin.domain.model.entity.Person;
import iam.platform.admin.domain.repository.PersonRepository;
import iam.platform.admin.infrastructure.persistence.entity.PersonPO;
import iam.platform.admin.infrastructure.persistence.repository.PersonJpaRepository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PersonRepositoryImpl implements PersonRepository {

    private final PersonJpaRepository jpaRepository;

    @Override
    public Person save(Person person) {
        PersonPO po = toPO(person);
        PersonPO savedPo = jpaRepository.save(po);
        return toDomain(savedPo);
    }

    @Override
    public Optional<Person> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Person> findByUsername(String username) {
        return jpaRepository.findByUsername(username).map(this::toDomain);
    }

    @Override
    public Optional<Person> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public Optional<Person> findByPhone(String phone) {
        return jpaRepository.findByPhone(phone).map(this::toDomain);
    }

    @Override
    public Page<Person> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable).map(this::toDomain);
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpaRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByPhone(String phone) {
        return jpaRepository.existsByPhone(phone);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public long countByEnabledTrue() {
        return jpaRepository.countByEnabledTrue();
    }

    @Override
    public long countByCreatedAtBetween(java.time.LocalDateTime start,
            java.time.LocalDateTime end) {
        return jpaRepository.countByCreatedAtBetween(start, end);
    }

    private PersonPO toPO(Person person) {
        PersonPO po = new PersonPO();
        po.setId(person.getId());
        po.setPersonCode(person.getPersonCode());
        po.setUsername(person.getUsername());
        po.setEmail(person.getEmail());
        po.setPhone(person.getPhone());
        po.setPasswordHash(person.getPasswordHash());
        po.setEmailVerified(person.isEmailVerified());
        po.setPhoneVerified(person.isPhoneVerified());
        po.setNickname(person.getNickname());
        po.setAvatarUrl(person.getAvatarUrl());
        po.setEnabled(person.isEnabled());
        po.setAccountLocked(person.isAccountLocked());
        po.setLastLoginAt(person.getLastLoginAt());
        po.setCreatedAt(person.getCreatedAt());
        po.setUpdatedAt(person.getUpdatedAt());
        return po;
    }

    private Person toDomain(PersonPO po) {
        return Person.builder().id(po.getId()).personCode(po.getPersonCode())
                .username(po.getUsername()).email(po.getEmail()).phone(po.getPhone())
                .passwordHash(po.getPasswordHash()).emailVerified(po.isEmailVerified())
                .phoneVerified(po.isPhoneVerified()).nickname(po.getNickname())
                .avatarUrl(po.getAvatarUrl()).enabled(po.isEnabled())
                .accountLocked(po.isAccountLocked()).lastLoginAt(po.getLastLoginAt())
                .createdAt(po.getCreatedAt()).updatedAt(po.getUpdatedAt()).build();
    }
}
