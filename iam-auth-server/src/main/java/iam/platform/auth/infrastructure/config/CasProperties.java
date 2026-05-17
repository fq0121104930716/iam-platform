package iam.platform.auth.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * CAS (Central Authentication Service) configuration properties.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "sso.cas")
public class CasProperties {

    /** CAS Server base URL */
    private String serverUrl = "https://sso.example.com/cas";

    /** CAS ticket validity duration in seconds */
    private int ticketValiditySeconds = 600;

    /** CAS ticket prefix */
    private String ticketPrefix = "ST";

    /** Whether to enable single sign out (SLO) */
    private boolean singleSignOutEnabled = true;

    /** CAS logout URL */
    private String logoutUrl = "https://sso.example.com/cas/logout";

    /** CAS login URL */
    private String loginUrl = "https://sso.example.com/cas/login";

    /** Whether to enable Front Channel Logout */
    private boolean frontChannelLogoutEnabled = true;

    /** Whether to enable Back Channel Logout */
    private boolean backChannelLogoutEnabled = true;

    /** Logout request TTL in seconds */
    private int logoutRequestTtlSeconds = 300;

    /** Whether to send logout request to all services */
    private boolean sendLogoutToAllServices = true;
}
