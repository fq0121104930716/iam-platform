package iam.platform.auth.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * LDAP configuration properties for enterprise AD directory authentication.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "sso.ldap")
public class LdapProperties {

    private boolean enabled = false;
    private String urls = "ldap://ad.example.com:389";
    private String baseDn = "DC=example,DC=com";
    private String bindDn = "CN=sso-service,OU=ServiceAccounts,DC=example,DC=com";
    private String bindPassword;
    private String userSearchBase = "OU=Users";
    private String userSearchFilter = "(sAMAccountName={0})";
    private boolean useSsl = false;
    private int connectTimeout = 5000;
    private int readTimeout = 5000;

    private Pool pool = new Pool();

    @Data
    public static class Pool {
        private int maxActive = 10;
        private int maxIdle = 5;
    }
}
