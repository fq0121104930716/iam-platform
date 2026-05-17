package iam.platform.auth.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

/**
 * LDAP configuration for enterprise AD directory authentication.
 */
@Configuration
public class LdapConfig {

    private final LdapProperties properties;

    public LdapConfig(LdapProperties properties) {
        this.properties = properties;
    }

    @Bean
    public LdapContextSource ldapContextSource() {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(properties.getUrls());
        contextSource.setBase(properties.getBaseDn());
        contextSource.setUserDn(properties.getBindDn());
        contextSource.setPassword(properties.getBindPassword());

        // Connection pool settings
        contextSource.setPooled(properties.getPool().getMaxActive() > 0);

        return contextSource;
    }

    @Bean
    public LdapTemplate ldapTemplate() {
        return new LdapTemplate(ldapContextSource());
    }
}
