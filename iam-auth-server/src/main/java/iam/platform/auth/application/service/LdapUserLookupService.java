package iam.platform.auth.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.stereotype.Service;
import iam.platform.auth.domain.model.entity.Person;
import iam.platform.auth.domain.repository.PersonRepository;
import iam.platform.auth.infrastructure.config.LdapProperties;

import java.util.List;

/**
 * Service for looking up LDAP users and mapping them to local Person entities.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LdapUserLookupService {

    private final LdapTemplate ldapTemplate;
    private final PersonRepository personRepository;
    private final LdapProperties properties;

    /**
     * Find a user's DN in LDAP by username.
     */
    public String findUserDn(String username) {
        try {
            LdapQuery query = LdapQueryBuilder.query().base(properties.getUserSearchBase())
                    .filter(properties.getUserSearchFilter(), username);

            List<String> dns = ldapTemplate.search(query, new ContextMapper<String>() {
                @Override
                public String mapFromContext(Object ctx) {
                    try {
                        javax.naming.ldap.LdapContext context = (javax.naming.ldap.LdapContext) ctx;
                        return context.getNameInNamespace();
                    } catch (javax.naming.NamingException e) {
                        throw new RuntimeException("Failed to get DN", e);
                    }
                }
            });

            if (dns.isEmpty()) {
                log.warn("LDAP user not found: {}", username);
                return null;
            }

            return dns.get(0);
        } catch (Exception e) {
            log.error("Failed to search LDAP for user: {}", username, e);
            return null;
        }
    }

    /**
     * Find an existing Person by LDAP username, or create a new one if not found.
     */
    public Person findOrCreatePersonByLdap(String username, String domain) {
        // Try to find existing person by username
        Person person = personRepository.findByUsername(username).orElse(null);

        if (person != null) {
            log.debug("Found existing person for LDAP user: {}, personId={}", username,
                    person.getId());
            return person;
        }

        // Create new person
        log.info("Creating new person for LDAP user: {}", username);
        String email = username + "@" + (domain != null ? domain : "ldap.local");

        person = Person.builder().username(username).email(email).nickname(username)
                .passwordHash("") // No password stored for LDAP users
                .enabled(true).accountLocked(false).build();

        person = personRepository.save(person);

        log.info("Created new person for LDAP user: personId={}", person.getId());
        return person;
    }
}
