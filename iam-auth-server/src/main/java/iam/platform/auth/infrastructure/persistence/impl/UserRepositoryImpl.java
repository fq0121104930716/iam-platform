package iam.platform.auth.infrastructure.persistence.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import iam.platform.auth.domain.model.entity.User;
import iam.platform.auth.domain.repository.UserRepository;
import iam.platform.auth.infrastructure.persistence.entity.UserPO;
import iam.platform.auth.infrastructure.persistence.repository.UserJpaRepository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository jpaRepository;

    @Override
    public User save(User user) {
        UserPO po = toPO(user);
        UserPO savedPo = jpaRepository.save(po);
        return toDomain(savedPo);
    }

    @Override
    public Optional<User> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpaRepository.findByUsername(username).map(this::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public Optional<User> findByPhone(String phone) {
        return jpaRepository.findByPhone(phone).map(this::toDomain);
    }

    @Override
    public Page<User> findAll(Pageable pageable) {
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

    private UserPO toPO(User user) {
        UserPO po = new UserPO();
        po.setId(user.getId());
        po.setUserCode(user.getUserCode());
        po.setUsername(user.getUsername());
        po.setEmail(user.getEmail());
        po.setPhone(user.getPhone());
        po.setPasswordHash(user.getPasswordHash());
        po.setEmailVerified(user.isEmailVerified());
        po.setPhoneVerified(user.isPhoneVerified());
        po.setNickname(user.getNickname());
        po.setAvatarUrl(user.getAvatarUrl());
        po.setEnabled(user.isEnabled());
        po.setAccountLocked(user.isAccountLocked());
        po.setLastLoginAt(user.getLastLoginAt());
        po.setCreatedAt(user.getCreatedAt());
        po.setUpdatedAt(user.getUpdatedAt());
        return po;
    }

    private User toDomain(UserPO po) {
        return User.builder().id(po.getId()).userCode(po.getUserCode())
                .username(po.getUsername()).email(po.getEmail()).phone(po.getPhone())
                .passwordHash(po.getPasswordHash()).emailVerified(po.isEmailVerified())
                .phoneVerified(po.isPhoneVerified()).nickname(po.getNickname())
                .avatarUrl(po.getAvatarUrl()).enabled(po.isEnabled())
                .accountLocked(po.isAccountLocked()).lastLoginAt(po.getLastLoginAt())
                .createdAt(po.getCreatedAt()).updatedAt(po.getUpdatedAt()).build();
    }
}
