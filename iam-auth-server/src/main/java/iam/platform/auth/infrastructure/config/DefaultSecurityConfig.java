package iam.platform.auth.infrastructure.config;

import iam.platform.auth.application.service.AuthenticationApplicationService;
import iam.platform.auth.application.service.CompositeAuthenticationProvider;
import iam.platform.auth.application.service.routing.ProtocolRouter;
import iam.platform.auth.infrastructure.security.CustomOAuth2UserService;
import iam.platform.auth.interfaces.web.filter.TenantAwareAuthenticationFilter;
import iam.platform.auth.interfaces.web.filter.UnifiedAuthenticationFilter;
import iam.platform.auth.interfaces.web.handler.UnifiedAuthenticationFailureHandler;
import iam.platform.auth.interfaces.web.handler.UnifiedAuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Default security configuration.
 * 
 * REFACTORED: Split into pure configuration layer to eliminate circular dependencies. - Uses
 * ObjectProvider for AuthenticationApplicationService to defer bean resolution - No longer creates
 * direct cycles with application services - Filter chain configuration is isolated from business
 * logic
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class DefaultSecurityConfig {

        private final CustomOAuth2UserService customOAuth2UserService;
        private final TenantAwareAuthenticationFilter tenantAwareAuthenticationFilter;
        private final ObjectProvider<AuthenticationApplicationService> authenticationApplicationServiceProvider;
        private final ObjectProvider<ProtocolRouter> protocolRouterProvider;
        private final CompositeAuthenticationProvider compositeAuthenticationProvider;
        private final ApplicationEventPublisher eventPublisher;

        @Bean
        @Order(2)
        public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http,
                        AuthenticationManager authManager) throws Exception {
                http.authorizeHttpRequests(authorize -> authorize
                                .requestMatchers("/css/**", "/js/**", "/static/**", "/error")
                                .permitAll().requestMatchers("/login", "/register", "/auth/code/**")
                                .permitAll().anyRequest().authenticated())

                                // Remove .formLogin() - replaced by UnifiedAuthenticationFilter
                                .oauth2Login(oauth2 -> oauth2.loginPage("/login")
                                                .userInfoEndpoint(userInfo -> userInfo.userService(
                                                                customOAuth2UserService))
                                                .successHandler(unifiedAuthenticationSuccessHandler()))

                                .logout(logout -> logout.logoutSuccessUrl("/login?logout")
                                                .permitAll().invalidateHttpSession(true))

                                // Unified filter replaces Spring's built-in
                                // UsernamePasswordAuthenticationFilter
                                .addFilterAt(unifiedAuthenticationFilter(authManager),
                                                UsernamePasswordAuthenticationFilter.class)

                                // Simplified tenant filter runs after the unified auth filter
                                .addFilterAfter(tenantAwareAuthenticationFilter,
                                                UnifiedAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public UnifiedAuthenticationFilter unifiedAuthenticationFilter(
                        AuthenticationManager authManager) {
                UnifiedAuthenticationFilter filter = new UnifiedAuthenticationFilter("/login");
                filter.setAuthenticationManager(authManager);
                filter.setAuthenticationSuccessHandler(unifiedAuthenticationSuccessHandler());
                filter.setAuthenticationFailureHandler(
                                new UnifiedAuthenticationFailureHandler(eventPublisher));
                return filter;
        }

        @Bean
        public UnifiedAuthenticationSuccessHandler unifiedAuthenticationSuccessHandler() {
                // Use ObjectProvider to get beans lazily, avoiding circular dependency
                return new UnifiedAuthenticationSuccessHandler(
                                authenticationApplicationServiceProvider.getObject(),
                                protocolRouterProvider.getObject());
        }

        @Bean
        public AuthenticationManager authenticationManager(HttpSecurity httpSecurity)
                        throws Exception {
                AuthenticationManagerBuilder builder =
                                httpSecurity.getSharedObject(AuthenticationManagerBuilder.class);
                builder.authenticationProvider(compositeAuthenticationProvider);
                return builder.build();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }
}
