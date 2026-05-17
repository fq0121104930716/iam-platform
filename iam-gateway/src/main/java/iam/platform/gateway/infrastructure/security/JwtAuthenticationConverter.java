package iam.platform.gateway.infrastructure.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * JWT Authentication Converter - 从JWT中提取权限信息（响应式版本）
 */
public class JwtAuthenticationConverter implements Converter<Jwt, Mono<AbstractAuthenticationToken>> {

    @Override
    public Mono<AbstractAuthenticationToken> convert(Jwt jwt) {
        // 从JWT的realm_access或自定义claim中提取权限
        Collection<String> roles = extractRoles(jwt);

        return Mono.just(new JwtAuthenticationToken(jwt,
                roles.stream()
                        .map(role -> new org.springframework.security.core.authority.SimpleGrantedAuthority(role))
                        .collect(Collectors.toList()),
                jwt.getSubject()));
    }

    @SuppressWarnings("unchecked")
    private Collection<String> extractRoles(Jwt jwt) {
        // 尝试从realm_access.claims.roles提取
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null && realmAccess.containsKey("roles")) {
            return (Collection<String>) realmAccess.get("roles");
        }

        // 尝试从自定义roles claim提取
        Object rolesClaim = jwt.getClaim("roles");
        if (rolesClaim instanceof Collection) {
            return (Collection<String>) rolesClaim;
        }

        return Collections.emptyList();
    }
}
