package iam.platform.gateway.infrastructure.security;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebFluxSecurity
public class GatewaySecurityConfig {

    /**
     * 链1 - OAuth2 Client链 (@Order(1)) - 处理浏览器登录流程
     */
    @Bean
    @Order(1)
    public SecurityWebFilterChain oauth2ClientFilterChain(ServerHttpSecurity http) {
        return http
                .securityMatcher(ServerWebExchangeMatchers.pathMatchers("/login/**", "/oauth2/authorization/**", "/login/oauth2/code/**"))
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/login/**", "/oauth2/**").permitAll()
                        .anyExchange().authenticated())
                .oauth2Login(oauth2 -> oauth2
                        .authenticationSuccessHandler(new GatewayAuthenticationSuccessHandler()))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .build();
    }

    /**
     * 链2 - Resource Server链 (@Order(2)) - 验证JWT
     */
    @Bean
    @Order(2)
    public SecurityWebFilterChain resourceServerFilterChain(ServerHttpSecurity http) {
        return http
                .securityMatcher(ServerWebExchangeMatchers.pathMatchers("/admin/**"))
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(HttpMethod.OPTIONS).permitAll()
                        .anyExchange().authenticated())
                .oauth2ResourceServer(rs -> rs
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(new JwtAuthenticationConverter()))
                        .authenticationEntryPoint(new JwtAuthenticationEntryPoint()))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .build();
    }

    /**
     * 链3 - 默认链 (@Order(3)) - 放行公开路径
     */
    @Bean
    @Order(3)
    public SecurityWebFilterChain defaultFilterChain(ServerHttpSecurity http) {
        return http
                .securityMatcher(ServerWebExchangeMatchers.pathMatchers("/auth/**", "/static/**", "/favicon.ico", "/error",
                        "/actuator/**", "/.well-known/**", "/oauth2/jwks"))
                .authorizeExchange(exchanges -> exchanges.anyExchange().permitAll())
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .build();
    }

    /**
     * JWT 认证入口点 - 处理 Token 验证失败的情况
     */
    public static class JwtAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {

        @Override
        public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException e) {
            // 设置 HTTP 401 状态码
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

            // 构建友好的错误响应
            String errorMessage = e != null ? e.getMessage() : "未提供有效的认证令牌";
            String errorType = classifyError(e);
            
            String body = String.format(
                "{\"error\":\"%s\",\"error_description\":\"%s\",\"path\":\"%s\"}",
                errorType,
                errorMessage.replace("\"", "\\\""),
                exchange.getRequest().getPath().value()
            );

            // 写入响应体
            DataBuffer buffer = exchange.getResponse().bufferFactory()
                    .wrap(body.getBytes(StandardCharsets.UTF_8));
            return exchange.getResponse().writeWith(Mono.just(buffer));
        }

        /**
         * 根据异常类型分类错误
         */
        private String classifyError(AuthenticationException e) {
            if (e == null) {
                return "unauthorized";
            }
            
            String message = e.getMessage();
            if (message == null) {
                return "invalid_token";
            }

            // 根据错误消息判断错误类型
            if (message.contains("expired") || message.contains("exp")) {
                return "token_expired";
            } else if (message.contains("signature") || message.contains("invalid")) {
                return "invalid_signature";
            } else if (message.contains("issuer") || message.contains("iss")) {
                return "invalid_issuer";
            } else if (message.contains("Bearer")) {
                return "missing_token";
            } else {
                return "invalid_token";
            }
        }
    }
}
