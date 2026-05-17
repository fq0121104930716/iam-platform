package iam.platform.auth.infrastructure.config;

import lombok.RequiredArgsConstructor;
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
import iam.platform.auth.application.service.AuthenticationApplicationService;
import iam.platform.auth.application.service.routing.ProtocolRouter;
import iam.platform.auth.infrastructure.security.CompositeAuthenticationProvider;
import iam.platform.auth.infrastructure.security.CustomOAuth2UserService;
import iam.platform.auth.infrastructure.security.TenantAwareAuthenticationFilter;
import iam.platform.auth.infrastructure.security.UnifiedAuthenticationFailureHandler;
import iam.platform.auth.infrastructure.security.UnifiedAuthenticationFilter;
import iam.platform.auth.infrastructure.security.UnifiedAuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class DefaultSecurityConfig {

        private final CustomOAuth2UserService customOAuth2UserService;
        private final TenantAwareAuthenticationFilter tenantAwareAuthenticationFilter;
        private final AuthenticationApplicationService authenticationApplicationService;
        private final ProtocolRouter protocolRouter;
        private final CompositeAuthenticationProvider compositeAuthenticationProvider;

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
                filter.setAuthenticationFailureHandler(new UnifiedAuthenticationFailureHandler());
                return filter;
        }

        @Bean
        public UnifiedAuthenticationSuccessHandler unifiedAuthenticationSuccessHandler() {
                return new UnifiedAuthenticationSuccessHandler(authenticationApplicationService,
                                protocolRouter);
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
