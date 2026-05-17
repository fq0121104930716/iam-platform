package iam.platform.auth.infrastructure.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import iam.platform.auth.domain.model.entity.Person;
import iam.platform.common.model.valueobject.PersonCode;
import iam.platform.auth.domain.repository.PersonRepository;
import iam.platform.auth.infrastructure.persistence.entity.PersonExternalLoginPO;
import iam.platform.auth.infrastructure.persistence.repository.PersonExternalLoginJpaRepository;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final PersonRepository personRepository;
    private final PersonExternalLoginJpaRepository externalLoginJpaRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oauth2User.getAttributes();

        log.info("OAuth2 login from provider: {}, attributes: {}", registrationId,
                attributes.keySet());

        String providerUserId = extractProviderUserId(registrationId, attributes);

        // 通过外部登录记录查找已关联的 Person
        Person person = externalLoginJpaRepository
                .findByProviderAndProviderUserId(registrationId, providerUserId)
                .map(externalLogin -> {
                    externalLogin.setLastUsedAt(LocalDateTime.now());
                    externalLoginJpaRepository.save(externalLogin);
                    return personRepository.findById(externalLogin.getPersonId()).orElse(null);
                }).orElseGet(() -> findOrCreatePerson(registrationId, providerUserId, oauth2User,
                        attributes));

        return new CustomOAuth2User(oauth2User, person);
    }

    private String extractProviderUserId(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId) {
            case "dingtalk" -> (String) attributes.get("unionId");
            case "wechat" -> (String) attributes.get("openid");
            default -> (String) attributes.get("id");
        };
    }

    private Person findOrCreatePerson(String provider, String providerUserId, OAuth2User oauth2User,
            Map<String, Object> attributes) {
        String email = (String) attributes.get("email");
        String nickname = (String) attributes.get("nickName");

        // 尝试通过邮箱关联已有 Person
        Person person = null;
        if (email != null) {
            person = personRepository.findByEmail(email).orElse(null);
        }

        if (person == null) {
            // 创建新的 Person
            String username = oauth2User.getName() + "_" + provider;
            person = Person.builder().personCode(PersonCode.generate().getValue())
                    .username(username).email(email).passwordHash("").emailVerified(email != null)
                    .nickname(nickname).enabled(true).accountLocked(false).build();
            person = personRepository.save(person);
            log.info("Created new person from OAuth2 login: {}, provider: {}", person.getUsername(),
                    provider);
        }

        // 创建外部登录关联记录
        PersonExternalLoginPO externalLogin =
                new PersonExternalLoginPO(person.getId(), provider, providerUserId);
        externalLoginJpaRepository.save(externalLogin);

        return person;
    }
}
