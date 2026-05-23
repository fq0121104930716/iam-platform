package iam.platform.audit.infrastructure.security;

import iam.platform.common.context.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Trusts the upstream gateway: extracts user identity from X-User-* headers forwarded by
 * iam-gateway after JWT validation, and establishes a Spring Security Authentication.
 *
 * <p>This avoids any direct dependency on iam-auth-server at startup or runtime.
 */
@Slf4j
@Component
public class GatewayAuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String userId = request.getHeader(TenantContext.HEADER_USER_ID);

        if (userId != null && !userId.isEmpty()) {
            try {
                TenantContext.populateFromHeaders(request);

                String roles = request.getHeader(TenantContext.HEADER_USER_ROLES);
                String permissions = request.getHeader(TenantContext.HEADER_USER_PERMISSIONS);

                Collection<GrantedAuthority> authorities = parseAuthorities(roles, permissions);
                Authentication auth =
                        new UsernamePasswordAuthenticationToken(userId, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);

                log.debug("Gateway auth established: userId={}, tenantId={}",
                        userId, TenantContext.getCurrentTenantId());
            } catch (Exception e) {
                log.error("Failed to establish auth from gateway headers: {}", e.getMessage());
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private Collection<GrantedAuthority> parseAuthorities(String roles, String permissions) {
        Collection<GrantedAuthority> authorities = new java.util.ArrayList<>();

        if (roles != null && !roles.isEmpty()) {
            String[] roleArray = roles.replaceAll("[\\[\\]\"]", "").split(",");
            Arrays.stream(roleArray).map(String::trim).filter(r -> !r.isEmpty())
                    .forEach(r -> authorities.add(new SimpleGrantedAuthority("ROLE_" + r)));
        }

        if (permissions != null && !permissions.isEmpty()) {
            String[] permArray = permissions.replaceAll("[\\[\\]\"]", "").split(",");
            Arrays.stream(permArray).map(String::trim).filter(p -> !p.isEmpty())
                    .forEach(p -> authorities.add(new SimpleGrantedAuthority(p)));
        }

        return authorities.isEmpty()
                ? Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                : authorities;
    }
}
