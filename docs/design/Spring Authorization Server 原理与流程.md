# Spring Authorization Server 原理与流程

## 1. 概述

本文档详细说明基于 Spring Authorization Server 1.2.5 构建的 IAM Platform 认证服务的内部工作原理和核心流程。

### 1.1 技术栈

- **Spring Boot**: 3.2.5
- **Spring Authorization Server**: 1.2.5
- **Spring Security**: 6.x
- **PostgreSQL**: 授权数据持久化
- **Redis**: Session 存储（支持水平扩容）

### 1.2 核心协议

- **OAuth 2.0**: 授权框架（RFC 6749）
- **OpenID Connect 1.0**: 基于 OAuth 2.0 的身份层协议
- **JWT (JSON Web Token)**: 令牌格式（RFC 7519）
- **PKCE**: 防止授权码拦截攻击（RFC 7636）

## 2. 架构设计

### 2.1 双 SecurityFilterChain 架构

SSO 服务采用双 FilterChain 设计，分别处理 OAuth2 端点和表单登录：

```java
// AuthorizationServerConfig.java - Order(1)
@Bean
@Order(1)
public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) {
    OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
    // 处理：/oauth2/authorize, /oauth2/token, /oauth2/jwks, /.well-known/openid-configuration
}

// DefaultSecurityConfig.java - Order(2)
@Bean
@Order(2)
public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) {
    http.securityMatcher("/login", "/register", "/oauth2/consent", "/css/**", "/js/**", "/static/**", "/error")
        .formLogin(form -> form.loginPage("/login").defaultSuccessUrl("/", false));
    // 处理：/login, /register, /oauth2/consent
}
```

**关键点：**
- `applyDefaultSecurity(http)` 内部已调用 `http.securityMatcher(endpointsMatcher)` 设置 OAuth2 端点匹配
- Order(1) 优先级更高，优先匹配 OAuth2 端点
- Order(2) 处理剩余的表单登录和页面请求

### 2.2 核心组件

```
┌─────────────────────────────────────────────────────────┐
│                   IAM Platform Server                       │
├─────────────────────────────────────────────────────────┤
│  接口层 (Interfaces)                                     │
│  ├─ REST API: UserController, OAuth2ClientController    │
│  └─ Web: LoginController, ConsentController             │
├─────────────────────────────────────────────────────────┤
│  应用层 (Application)                                    │
│  ├─ UserApplicationService                              │
│  ├─ OAuth2ClientApplicationService                      │
│  └─ DTO + Assembler                                     │
├─────────────────────────────────────────────────────────┤
│  领域层 (Domain)                                         │
│  ├─ Entity: User, Role, OAuth2Client                    │
│  ├─ Repository 接口                                     │
│  └─ Service: PasswordPolicyService                      │
├─────────────────────────────────────────────────────────┤
│  基础设施层 (Infrastructure)                             │
│  ├─ Config: AuthorizationServerConfig,                  │
│  │         DefaultSecurityConfig                        │
│  ├─ Security: CustomUserDetailsService,                 │
│  │            TokenCustomizer,                          │
│  │            RegisteredClientRepositoryAdapter         │
│  └─ Persistence: JPA Repository, Converter              │
├─────────────────────────────────────────────────────────┤
│  数据存储                                                │
│  ├─ PostgreSQL: 用户、角色、客户端、授权记录             │
│  └─ Redis: Session 存储（水平扩容）                     │
└─────────────────────────────────────────────────────────┘
```

## 3. 核心流程详解

### 3.1 完整 OAuth2 授权码流程

```
浏览器                   第三方服务                  IAM Platform Server
  │                        │                           │
  │  1. 访问受保护资源       │                           │
  │──────────────────────►│                           │
  │  2. 302 重定向到登录    │                           │
  │◄──────────────────────│                           │
  │                        │                           │
  │  3. 302 重定向到 SSO    │                           │
  │   /oauth2/authorize?   │                           │
  │   response_type=code   │                           │
  │   &client_id=xxx       │                           │
  │   &redirect_uri=xxx    │                           │
  │   &scope=openid        │                           │
  │   &state=xxx           │                           │
  │   &code_challenge=xxx  │                           │
  │──────────────────────────────────────────────────►│
  │                        │                           │
  │  4. 检查登录状态        │                           │
  │   - 未登录：显示登录页  │                           │
  │   - 已登录：继续授权    │                           │
  │◄──────────────────────────────────────────────────│
  │                        │                           │
  │  5. 用户登录            │                           │
  │   POST /login          │                           │
  │   username=admin       │                           │
  │   password=123456      │                           │
  │──────────────────────────────────────────────────►│
  │                        │                           │
  │  6. 登录成功            │                           │
  │   创建 Session (Redis)  │                           │
  │   SavedRequest 恢复     │                           │
  │   重定向回 /oauth2/authorize                      │
  │◄──────────────────────────────────────────────────│
  │                        │                           │
  │  7. 检查授权同意         │                           │
  │   - 首次授权：同意页    │                           │
  │   - 已授权：跳过        │                           │
  │   - 新增 scope：同意页  │                           │
  │◄──────────────────────────────────────────────────│
  │                        │                           │
  │  8. 用户确认授权         │                           │
  │   POST /oauth2/authorize│                          │
  │   approve=true         │                           │
  │──────────────────────────────────────────────────►│
  │                        │                           │
  │  9. 保存授权记录         │                           │
  │   生成授权码            │                           │
  │   重定向回第三方        │                           │
  │   ?code=xxx&state=xxx  │                           │
  │◄──────────────────────────────────────────────────│
  │                        │                           │
  │  10. 换取令牌           │                           │
  │   POST /oauth2/token   │                           │
  │   grant_type=authorization_code                    │
  │   code=xxx             │                           │
  │   code_verifier=xxx    │                           │
  │──────────────────────────────────────────────────►│
  │                        │                           │
  │  11. 返回 JWT Token     │                           │
  │   access_token (JWT)    │                           │
  │   id_token (JWT)        │                           │
  │   refresh_token         │                           │
  │◄──────────────────────────────────────────────────│
  │                        │                           │
  │  12. 第三方验证 JWT     │                           │
  │   建立用户会话          │                           │
  │──────────────────────►│                           │
```

### 3.2 登录状态检查机制

#### Session 存储架构

```yaml
# application.yml
spring:
  session:
    store-type: redis  # Session 存储在 Redis，支持水平扩容
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
```

#### 检查流程

```
用户请求                    SSO Server (FilterChain)
    │                              │
    │  GET /oauth2/authorize       │
    │  Cookie: JSESSIONID=xxx      │
    │─────────────────────────────►│
    │                              │
    │  1. SessionRepositoryFilter  │
    │     从 Redis 加载 Session     │
    │     REDIS: GET spring:session:sessions:xxx
    │                              │
    │  2. SecurityContextPersistenceFilter
    │     从 Session 中提取         │
    │     SecurityContext          │
    │                              │
    │  3. AuthorizationFilter      │
    │     检查认证状态：            │
    │     authentication.isAuthenticated()
    │                              │
    │     ✅ true → 继续处理        │
    │     ❌ false → 跳转登录页     │
    │                              │
```

#### 水平扩容架构

```
                ┌──────────┐
                │  Redis   │
                │ (Session)│
                └────┬─────┘
                     │
        ┌────────────┼────────────┐
        │            │            │
   ┌────▼────┐ ┌────▼────┐ ┌────▼────┐
   │ SSO-1   │ │ SSO-2   │ │ SSO-3   │
   │:9000    │ │:9000    │ │:9000    │
   └─────────┘ └─────────┘ └─────────┘
        ▲            ▲            ▲
        └────────────┼────────────┘
                     │
              ┌──────▼──────┐
              │  负载均衡器   │
              └─────────────┘
```

**关键点：**
- 所有 SSO 实例共享同一个 Redis 集群
- Session 数据集中存储，实现无状态水平扩容
- 用户请求可以被路由到任意实例

### 3.3 SavedRequest 机制

#### 什么是 SavedRequest？

SavedRequest 是 Spring Security 的机制，用于在用户登录成功后**自动重定向到原始请求的 URL**。

#### 工作流程

```
用户浏览器               SSO Server                  Redis/Session
    │                       │                            │
    │  1. 直接访问授权端点   │                            │
    │  GET /oauth2/authorize│                            │
    │──────────────────────►│                            │
    │                       │                            │
    │                       │  2. 检查登录状态            │
    │                       │  ❌ 未登录                  │
    │                       │                            │
    │                       │  3. 保存原始请求到 Session  │
    │                       │  SavedRequest:             │
    │                       │  - URL: /oauth2/authorize  │
    │                       │  - Method: GET             │
    │                       │  - Parameters: {...}       │
    │                       │                            │
    │                       │  SET spring:session:...    │
    │                       │  savedRequest=...          │
    │                       │───────────────────────────►│
    │                       │                            │
    │  4. 302 重定向到登录页 │                            │
    │◄──────────────────────│                            │
    │  Location: /login     │                            │
    │                       │                            │
    │  5. 用户登录           │                            │
    │  POST /login          │                            │
    │  username=admin       │                            │
    │  password=123456      │                            │
    │──────────────────────►│                            │
    │                       │                            │
    │                       │  6. 验证成功                │
    │                       │  从 Session 读取 SavedRequest
    │                       │───────────────────────────►│
    │                       │                            │
    │                       │  7. 302 重定向回原始 URL    │
    │◄──────────────────────│                            │
    │  Location: /oauth2/authorize?response_type=code&...
    │                       │                            │
    │  8. 继续授权流程       │                            │
    │──────────────────────►│                            │
```

### 3.4 授权同意机制

#### 何时显示同意页面？

SSO 服务器在以下情况下显示 `/oauth2/consent` 授权同意页面：

**触发条件（需同时满足）：**

1. **用户已登录**：用户在 SSO 服务器有有效的 Session
2. **客户端配置要求同意**：`require_authorization_consent = true`
3. **授权记录不存在或不完整**：
   - 首次授权（数据库中无授权记录）
   - 请求的 scope 超出了之前授权的范围（新增权限）
   - 授权记录已过期

#### 授权记录存储

```sql
-- 数据库表：oauth2_authorization_consent
CREATE TABLE oauth2_authorization_consent (
    registered_client_id VARCHAR(100) NOT NULL,
    principal_name       VARCHAR(200) NOT NULL,
    authorities          VARCHAR(1000) NOT NULL,  -- 格式：SCOPE_openid,SCOPE_profile
    PRIMARY KEY (registered_client_id, principal_name)
);

-- 示例数据
INSERT INTO oauth2_authorization_consent (
    registered_client_id,
    principal_name,
    authorities
) VALUES (
    'demo-client-001',
    'admin',
    'SCOPE_openid,SCOPE_profile,SCOPE_email'
);
```

#### 场景对比

| 场景 | 已授权 Scopes | 请求 Scopes | 是否显示同意页 |
|------|---------------|-------------|----------------|
| 首次授权 | 无 | openid, profile | ✅ 是 |
| 相同 scope | openid, profile | openid, profile | ❌ 否 |
| 新增 scope | openid, profile | openid, profile, email | ✅ 是 |
| 客户端不要求同意 | openid, profile | openid, profile, email | ❌ 否 |

#### 框架内部比较逻辑

Spring Authorization Server 内部的 scope 比较逻辑（简化版）：

```java
// OAuth2AuthorizationCodeRequestAuthenticationProvider（框架内部）
boolean requiresConsent(RegisteredClient client, 
                        Set<String> requestedScopes,
                        Set<String> authorizedScopes) {
    // 1. 如果客户端不要求同意，直接返回 false
    if (!client.getClientSettings().isRequireAuthorizationConsent()) {
        return false;
    }
    
    // 2. 如果没有已授权的 scopes，需要同意
    if (authorizedScopes.isEmpty()) {
        return true;
    }
    
    // 3. 检查是否有新增的 scope
    for (String scope : requestedScopes) {
        if (!authorizedScopes.contains("SCOPE_" + scope)) {
            return true;  // 发现新增 scope
        }
    }
    
    // 4. 所有 scopes 都已授权
    return false;
}
```

**关键点：**
- Scope 比较时使用 `SCOPE_` 前缀
- 只要有一个新的 scope 就会触发重新同意
- 比较逻辑在框架内部，项目代码只需配置 ConsentService

#### 授权同意流程

```
用户浏览器                  SSO Server
    │                          │
    │  GET /oauth2/authorize   │
    │  ?scope=openid profile   │
    │─────────────────────────►│
    │                          │
    │  检查登录状态             │
    │  ✅ 用户已登录             │
    │                          │
    │  检查 require_authorization_consent
    │  ✅ = true                │
    │                          │
    │  查询授权记录             │
    │  SELECT authorities       │
    │  FROM oauth2_authorization_consent
    │  WHERE registered_client_id = ?
    │    AND principal_name = ? │
    │                          │
    │  结果：空（首次授权）      │
    │                          │
    │  302 重定向到同意页       │
    │◄─────────────────────────│
    │  Location: /oauth2/consent?
    │  clientName=My App&
    │  scopes=openid,profile
    │                          │
    │  显示同意页面             │
    │  [Approve] [Deny]        │
    │                          │
    │  用户点击 Approve         │
    │  POST /oauth2/authorize   │
    │  approve=true            │
    │─────────────────────────►│
    │                          │
    │  保存授权记录             │
    │  INSERT INTO oauth2_authorization_consent
    │  VALUES ('client-id', 'user', 
    │          'SCOPE_openid,SCOPE_profile')
    │                          │
    │  生成授权码               │
    │  重定向回第三方应用       │
    │◄─────────────────────────│
```

### 3.5 JWT 签发机制

#### 重要说明

**JWT 不是在所有场景下都会签发！**

#### 两种认证模式对比

| 认证方式 | 是否签发 JWT | 认证机制 | 使用场景 |
|---------|-------------|---------|----------|
| **表单登录** (`POST /login`) | ❌ **不签发 JWT** | 基于 Session | 用户在 SSO 登录页面登录 |
| **OAuth2 Token 端点** (`POST /oauth2/token`) | ✅ **签发 JWT** | 基于 Token | 第三方应用换取令牌 |

#### 表单登录流程（不签发 JWT）

```
用户浏览器                  SSO Server                  Redis
    │                          │                          │
    │  1. 访问 SSO 登录页      │                          │
    │  GET /login              │                          │
    │─────────────────────────►│                          │
    │                          │                          │
    │  2. 显示登录表单          │                          │
    │◄─────────────────────────│                          │
    │                          │                          │
    │  3. 提交登录表单          │                          │
    │  POST /login             │                          │
    │  username=admin          │                          │
    │  password=123456         │                          │
    │─────────────────────────►│                          │
    │                          │                          │
    │  4. 验证密码 (BCrypt)     │                          │
    │  ✅ 验证成功              │                          │
    │                          │                          │
    │  5. 创建 SecurityContext  │                          │
    │  保存到 Session           │                          │
    │                          │  SET spring:session:...  │
    │                          │─────────────────────────►│
    │                          │                          │
    │  6. 返回 Set-Cookie       │                          │
    │  JSESSIONID=xxx           │                          │
    │◄─────────────────────────│                          │
    │                          │                          │
    │  7. 后续请求携带 Cookie   │                          │
    │  Cookie: JSESSIONID=xxx  │                          │
    │─────────────────────────►│                          │
    │                          │  GET spring:session:...  │
    │                          │─────────────────────────►│
    │                          │                          │
    │                          │  返回 Authentication 对象 │
    │                          │◄─────────────────────────│
    │                          │                          │
    │  8. ✅ 用户已登录          │                          │
    │  （基于 Session，无 JWT）  │                          │
    │◄─────────────────────────│                          │
```

**关键点：**
- 表单登录创建的是 **HttpSession**，不是 JWT
- Session 数据存储在 **Redis** 中
- 用户状态通过 **Cookie (JSESSIONID)** 维护
- 支持水平扩容（多个 SSO 实例共享 Redis Session）

#### OAuth2 Token 流程（签发 JWT）

```
第三方应用                  SSO Server
    │                          │
    │  POST /oauth2/token      │
    │  grant_type=authorization_code
    │  code=xxx                │
    │  client_id=xxx           │
    │  client_secret=xxx       │
    │─────────────────────────►│
    │                          │
    │  验证授权码               │
    │  验证客户端凭据           │
    │                          │
    │  签发 JWT Token           │
    │  - Access Token (JWT)     │
    │  - ID Token (JWT)         │
    │  - Refresh Token          │
    │                          │
    │  返回 Token 响应          │
    │  {                       │
    │    "access_token": "eyJ...",
    │    "id_token": "eyJ...",  │
    │    "token_type": "Bearer" │
    │  }                       │
    │◄─────────────────────────│
```

**关键点：**
- JWT 只在 **Token 端点** 签发
- JWT 用于第三方应用访问 API 时的身份验证
- JWT 是**无状态的**，不需要存储在服务器

#### JWT 自定义 Claims

通过 `TokenCustomizer` 组件添加自定义 Claims：

```java
@Component
@RequiredArgsConstructor
public class TokenCustomizer implements OAuth2TokenCustomizer<JwtEncodingContext> {
    private final UserRepository userRepository;

    @Override
    public void customize(JwtEncodingContext context) {
        String username = context.getPrincipal().getName();
        userRepository.findByUsername(username).ifPresent(user -> {
            // 添加自定义 claims
            context.getClaims().claim("email", user.getEmail());
            if (user.getNickname() != null) {
                context.getClaims().claim("nickname", user.getNickname());
            }
            context.getClaims().claim("roles",
                    user.getRoles().stream()
                        .map(Role::getCode)
                        .collect(Collectors.toList()));
        });
    }
}
```

#### JWT 结构示例

**ID Token:**
```json
{
  "iss": "http://sso-server:9000",
  "sub": "user-uuid-12345",
  "aud": "a1b2c3d4e5f6g7h8",
  "exp": 1704067200,
  "iat": 1704063600,
  "auth_time": 1704063500,
  "nonce": "n-0S6_WzA2Mj",
  "email": "user@example.com",
  "nickname": "张三",
  "roles": ["ADMIN", "USER"]
}
```

**Access Token:**
```json
{
  "iss": "http://sso-server:9000",
  "sub": "user-uuid-12345",
  "aud": "a1b2c3d4e5f6g7h8",
  "exp": 1704067200,
  "iat": 1704063600,
  "scope": "openid profile email",
  "email": "user@example.com",
  "nickname": "张三",
  "roles": ["ADMIN", "USER"]
}
```

## 4. 核心配置

### 4.1 授权服务器配置

```java
@Configuration
public class AuthorizationServerConfig {
    
    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(
            HttpSecurity http) throws Exception {
        
        // 应用默认的 OAuth2 授权服务器安全配置
        // 内部会设置 securityMatcher 匹配 OAuth2 端点
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
        
        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
            .oidc(Customizer.withDefaults());
        
        // 未认证时重定向到登录页
        http.exceptionHandling(exceptions -> exceptions
            .defaultAuthenticationEntryPointFor(
                new LoginUrlAuthenticationEntryPoint("/login"),
                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
            )
        );
        
        // 启用 JWT Resource Server 支持
        http.oauth2ResourceServer(rs -> rs.jwt(Customizer.withDefaults()));
        
        return http.build();
    }
}
```

### 4.2 表单登录配置

```java
@Configuration
public class DefaultSecurityConfig {
    
    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(
            HttpSecurity http) throws Exception {
        
        http.securityMatcher("/login", "/register", "/oauth2/consent", 
                "/css/**", "/js/**", "/static/**", "/error")
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/css/**", "/js/**", "/static/**", "/error")
                    .permitAll()
                .requestMatchers("/login", "/register")
                    .permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", false)  // ⚠️ 必须是 false
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );
        
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### 4.3 授权同意服务配置

```java
@Configuration
public class JdbcOAuth2AuthorizationServiceConfig {
    
    @Bean
    public OAuth2AuthorizationConsentService authorizationConsentService(
            JdbcTemplate jdbcTemplate,
            RegisteredClientRepository registeredClientRepository) {
        
        return new JdbcOAuth2AuthorizationConsentService(
            jdbcTemplate, 
            registeredClientRepository
        );
    }
}
```

### 4.4 用户认证服务

```java
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    
    private final UserRepository userRepository;
    
    @Override
    public UserDetails loadUserByUsername(String username) 
            throws UsernameNotFoundException {
        
        return userRepository.findByUsername(username)
            .map(this::buildUserDetails)
            .orElseThrow(() -> 
                new UsernameNotFoundException("User not found: " + username)
            );
    }
    
    private UserDetails buildUserDetails(User domainUser) {
        return User.builder()
            .username(domainUser.getUsername())
            .password(domainUser.getPasswordHash())  // BCrypt 加密
            .disabled(!domainUser.isEnabled())
            .accountLocked(domainUser.isAccountLocked())
            .authorities(domainUser.getRoles().stream()
                .map(Role::getCode)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList()))
            .build();
    }
}
```

## 5. 数据存储

### 5.1 数据库表结构

#### OAuth2 客户端表

```sql
CREATE TABLE t_oauth2_client (
    id                              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id                       VARCHAR(100) NOT NULL UNIQUE,
    client_secret                   VARCHAR(500) NOT NULL,  -- AES-256-GCM 加密
    client_name                     VARCHAR(200) NOT NULL,
    client_authentication_methods   VARCHAR(1000) NOT NULL,
    authorization_grant_types       VARCHAR(1000) NOT NULL,
    redirect_uris                   VARCHAR(2000),
    post_logout_redirect_uris       VARCHAR(2000),
    scopes                          VARCHAR(1000) NOT NULL,
    require_proof_key               BOOLEAN NOT NULL DEFAULT FALSE,
    require_authorization_consent   BOOLEAN NOT NULL DEFAULT TRUE,
    access_token_ttl_seconds        INTEGER NOT NULL DEFAULT 3600,
    refresh_token_ttl_seconds       INTEGER NOT NULL DEFAULT 86400,
    enabled                         BOOLEAN NOT NULL DEFAULT TRUE,
    created_at                      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at                      TIMESTAMP NOT NULL DEFAULT NOW()
);
```

#### 授权记录表

```sql
CREATE TABLE oauth2_authorization (
    id                            VARCHAR(100) NOT NULL PRIMARY KEY,
    registered_client_id          VARCHAR(100) NOT NULL,
    principal_name                VARCHAR(200) NOT NULL,
    authorization_grant_type      VARCHAR(100) NOT NULL,
    authorized_scopes             VARCHAR(1000),
    attributes                    TEXT,
    state                         VARCHAR(500),
    authorization_code_value      TEXT,
    authorization_code_issued_at  TIMESTAMP,
    authorization_code_expires_at TIMESTAMP,
    authorization_code_metadata   TEXT,
    access_token_value            TEXT,
    access_token_issued_at        TIMESTAMP,
    access_token_expires_at       TIMESTAMP,
    access_token_metadata         TEXT,
    access_token_type             VARCHAR(100),
    access_token_scopes           VARCHAR(1000),
    oidc_id_token_value           TEXT,
    oidc_id_token_issued_at       TIMESTAMP,
    oidc_id_token_expires_at      TIMESTAMP,
    oidc_id_token_metadata        TEXT,
    refresh_token_value           TEXT,
    refresh_token_issued_at       TIMESTAMP,
    refresh_token_expires_at      TIMESTAMP,
    refresh_token_metadata        TEXT,
    PRIMARY KEY (id)
);

CREATE INDEX idx_oauth2_authorization_client_id 
    ON oauth2_authorization(registered_client_id);
CREATE INDEX idx_oauth2_authorization_principal 
    ON oauth2_authorization(principal_name);
```

#### 授权同意表

```sql
CREATE TABLE oauth2_authorization_consent (
    registered_client_id VARCHAR(100) NOT NULL,
    principal_name       VARCHAR(200) NOT NULL,
    authorities          VARCHAR(1000) NOT NULL,  -- SCOPE_xxx,SCOPE_yyy
    PRIMARY KEY (registered_client_id, principal_name)
);
```

### 5.2 Redis Session 存储

```bash
# Redis 中存储的 Session 数据结构
KEY: spring:session:sessions:<session-id>
TYPE: Hash

FIELDS:
  - sessionAttr:SPRING_SECURITY_CONTEXT (SecurityContext 序列化数据)
  - sessionAttr:SPRING.SECURITY.SAVEDREQUEST (SavedRequest 数据)
  - lastAccessedTime (最后访问时间)
  - maxInactiveInterval (最大不活跃间隔)
  - creationTime (创建时间)
```

## 6. 安全机制

### 6.1 密码加密

- **算法**: BCrypt（单向，不可逆）
- **强度**: 默认 strength = 10
- **实现**: `BCryptPasswordEncoder`

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

### 6.2 Client Secret 加密

- **算法**: AES-256-GCM
- **密钥长度**: 32 字符（严格）
- **配置**: 通过 `EncryptionProperties` 配置类读取

```yaml
security:
  encryption:
    key: ${ENCRYPTION_KEY:dev-default-key-32chars-long}
```

### 6.3 JWT 签名

- **算法**: RS256（非对称加密）
- **密钥**: RSA 密钥对
  - DEV 环境：`src/main/resources/keys/private.pem`
  - PROD 环境：通过 K8s Secret 注入

### 6.4 PKCE 支持

- **方法**: S256（SHA-256）
- **强制**: Public Client 必须使用 PKCE
- **配置**: `require_proof_key = true`

## 7. Token 生命周期

| Token 类型 | 默认有效期 | 说明 |
|------------|-----------|------|
| Access Token | 1 小时 (3600s) | 用于 API 访问 |
| Refresh Token | 24 小时 (86400s) | 用于刷新 Access Token |
| ID Token | 1 小时 (3600s) | 用于身份验证 |
| Authorization Code | 5 分钟 (300s) | 一次性使用，换取 Token |

## 8. 支持的 Scope

| Scope | 说明 | 返回的 Claims |
|-------|------|---------------|
| `openid` | 必须，启用 OIDC | `sub`, `iss`, `aud`, `exp`, `iat` |
| `profile` | 用户基本信息 | `nickname`, `picture`, `roles` |
| `email` | 用户邮箱 | `email`, `email_verified` |

## 9. OIDC 发现端点

```
GET /.well-known/openid-configuration
```

**响应示例：**
```json
{
  "issuer": "http://sso-server:9000",
  "authorization_endpoint": "http://sso-server:9000/oauth2/authorize",
  "token_endpoint": "http://sso-server:9000/oauth2/token",
  "userinfo_endpoint": "http://sso-server:9000/userinfo",
  "jwks_uri": "http://sso-server:9000/oauth2/jwks",
  "response_types_supported": ["code"],
  "grant_types_supported": ["authorization_code", "refresh_token", "client_credentials"],
  "scopes_supported": ["openid", "profile", "email"],
  "token_endpoint_auth_methods_supported": ["client_secret_basic", "client_secret_post"],
  "id_token_signing_alg_values_supported": ["RS256"]
}
```

## 10. 关键设计决策

### 10.1 为什么使用双 FilterChain？

**原因：**
- OAuth2 端点和表单登录需要不同的安全策略
- OAuth2 端点需要支持多种认证方式（Basic Auth、JWT）
- 表单登录只需要 Cookie/Session 认证

**实现：**
- Order(1): OAuth2 端点，支持多种认证
- Order(2): 表单登录，仅支持 Cookie/Session

### 10.2 为什么使用 Redis Session？

**原因：**
- 支持水平扩容
- Session 集中存储，多实例共享
- 高性能读写
- 支持 Session 过期和清理

**替代方案对比：**
- **In-Memory Session**: 无法水平扩容
- **JWT Session**: 无法主动失效
- **Redis Session**: ✅ 支持扩容、可主动失效、高性能

### 10.3 为什么 JWT 不在登录时签发？

**原因：**
- 表单登录是用户与 SSO 之间的交互，使用 Session 更合适
- JWT 是给第三方应用使用的，在 Token 端点签发
- 分离关注点，提高安全性

**架构优势：**
- Session 用于 SSO 内部状态管理
- JWT 用于跨服务身份传递
- 两者各司其职，互不干扰

## 11. 常见问题

### 11.1 为什么反复显示授权同意页面？

**可能原因：**
1. 客户端配置 `require_authorization_consent = true`
2. 每次请求的 scope 不同
3. 授权记录被删除或过期

**排查：**
```sql
-- 检查客户端配置
SELECT require_authorization_consent 
FROM t_oauth2_client 
WHERE client_id = 'your-client-id';

-- 查看授权记录
SELECT * 
FROM oauth2_authorization_consent 
WHERE registered_client_id = 'your-client-id' 
  AND principal_name = 'username';
```

### 11.2 登录后仍然提示未登录？

**可能原因：**
1. Redis 连接失败
2. Cookie 未正确发送
3. 多个 SSO 实例使用不同的 Redis

**排查：**
```bash
# 检查 Redis 连接
redis-cli ping  # 应返回 PONG

# 检查 Cookie
# 浏览器开发者工具 -> Application -> Cookies
```

### 11.3 JWT 验证失败？

**可能原因：**
- JWK Set URI 配置错误
- 密钥 KeyID 不一致

**排查：**
```bash
# 检查 JWK 端点
curl http://sso-server:9000/oauth2/jwks

# 检查 JWT 的 kid 头
echo "eyJhbGciOiJSUzI1NiIs..." | cut -d'.' -f1 | base64 -d
```

## 12. 参考资料

- [Spring Authorization Server 官方文档](https://docs.spring.io/spring-authorization-server/reference/)
- [OAuth 2.0 Authorization Framework](https://datatracker.ietf.org/doc/html/rfc6749)
- [OpenID Connect Core 1.0](https://openid.net/specs/openid-connect-core-1_0.html)
- [PKCE](https://datatracker.ietf.org/doc/html/rfc7636)
- [JWT](https://datatracker.ietf.org/doc/html/rfc7519)
- [Spring Security 官方文档](https://docs.spring.io/spring-security/reference/)
