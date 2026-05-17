package iam.platform.gateway.infrastructure.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * Gateway OAuth2登录成功处理器 登录成功后重定向到首页或原始请求路径
 */
@Slf4j
public class GatewayAuthenticationSuccessHandler implements ServerAuthenticationSuccessHandler {

    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange exchange,
            Authentication authentication) {
        ServerHttpResponse response = exchange.getExchange().getResponse();

        // 从OAuth2AuthenticationToken中提取用户信息
        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            String name = oauthToken.getName();
            log.info("OAuth2 login successful for user: {}", name);
        }

        // 登录成功后重定向到首页
        response.setStatusCode(org.springframework.http.HttpStatus.FOUND);
        response.getHeaders().setLocation(URI.create("/"));

        return response.setComplete();
    }
}
