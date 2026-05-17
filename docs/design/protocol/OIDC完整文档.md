# OpenID Connect (OIDC) 完整文档

## 目录

1. [OIDC 概述](#1-oidc-概述)
2. [核心概念](#2-核心概念)
3. [OIDC 与 OAuth 2.0 的关系](#3-oidc-与-oauth-20-的关系)
4. [OIDC 认证流程](#4-oidc-认证流程)
5. [ID Token 详解](#5-id-token-详解)
6. [UserInfo 端点](#6-userinfo-端点)
7. [OIDC 发现文档](#7-oidc-发现文档)
8. [客户端注册与配置](#8-客户端注册与配置)
9. [Scope 和 Claims](#9-scope-和-claims)
10. [会话管理](#10-会话管理)
11. [安全考虑](#11-安全考虑)
12. [实现示例](#12-实现示例)
13. [最佳实践](#13-最佳实践)
14. [常见问题](#14-常见问题)

---

## 1. OIDC 概述

OpenID Connect (OIDC) 是基于 OAuth 2.0 协议的身份层协议，允许客户端应用程序验证用户的身份，并获取基本的用户信息。OIDC 1.0 于 2014 年发布，由 OpenID 基金会维护。

### 1.1 核心特性

- **身份验证**: 证明用户是谁（Authentication）
- **授权**: 允许访问特定资源（Authorization）
- **标准化**: 基于 JSON Web Token (JWT) 和 RESTful API
- **互操作性**: 跨平台和语言的标准化实现

### 1.2 协议版本

- **OIDC Core 1.0**: 核心规范
- **OIDC Discovery 1.0**: 发现端点规范
- **OIDC Session Management 1.0**: 会话管理
- **OIDC Front-Channel Logout 1.0**: 前端通道登出
- **OIDC Back-Channel Logout 1.0**: 后端通道登出

---

## 2. 核心概念

### 2.1 角色定义

| 角色 | 说明 | 示例 |
|------|------|------|
| **End-User** | 最终用户 | 登录的用户 |
| **Relying Party (RP)** | 依赖方，使用 OIDC 验证用户身份的应用 | 第三方 Web 应用 |
| **OpenID Provider (OP)** | OIDC 提供者，验证用户身份并提供 ID Token | IAM Platform SSO |

### 2.2 核心组件

| 组件 | 说明 | 端点 |
|------|------|------|
| **Authorization Endpoint** | 授权端点，处理用户认证和授权 | `/oauth2/authorize` |
| **Token Endpoint** | 令牌端点，颁发 Access Token 和 ID Token | `/oauth2/token` |
| **UserInfo Endpoint** | 用户信息端点，返回用户详细信息 | `/userinfo` |
| **JWK Set URI** | JSON Web Key Set，用于验证 Token 签名 | `/oauth2/jwks` |
| **Discovery Endpoint** | 发现端点，提供 OP 配置信息 | `/.well-known/openid-configuration` |

### 2.3 Token 类型

| Token 类型 | 用途 | 格式 | 受众 |
|-----------|------|------|------|
| **ID Token** | 证明用户身份 | JWT | 客户端应用 |
| **Access Token** | 授权访问资源 | JWT 或不透明字符串 | 资源服务器 |
| **Refresh Token** | 刷新 Access Token | 不透明字符串 | 认证服务器 |

---

## 3. OIDC 与 OAuth 2.0 的关系

### 3.1 关系图

```
┌─────────────────────────────────────────┐
│            OpenID Connect 1.0           │
│  ┌───────────────────────────────────┐  │
│  │         OAuth 2.0 Framework       │  │
│  │  ┌─────────────────────────────┐  │  │
│  │  │      Authorization Grant    │  │  │
│  │  │      (Authorization Code)   │  │  │
│  │  └─────────────────────────────┘  │  │
│  └───────────────────────────────────┘  │
└─────────────────────────────────────────┘
```

### 3.2 关键区别

| 维度 | OAuth 2.0 | OIDC |
|------|-----------|------|
| **主要用途** | 授权访问资源 | 身份验证 |
| **核心产物** | Access Token | ID Token |
| **是否包含用户信息** | 否 | 是（在 ID Token 中） |
| **协议层** | 授权框架 | 身份层（基于 OAuth 2.0） |
| **标准化程度** | 基础框架 | 在 OAuth 2.0 上增加标准化 |

### 3.3 OIDC 扩展

OIDC 在 OAuth 2.0 基础上增加了：

1. **ID Token**: JWT 格式的身份证明
2. **UserInfo Endpoint**: 获取用户详细信息
3. **Discovery Document**: 自动发现 OP 配置
4. **Standardized Scopes**: `openid`, `profile`, `email`, `phone`, `address`
5. **Standardized Claims**: 标准化的用户信息字段

---

## 4. OIDC 认证流程

### 4.1 授权码流程（Authorization Code Flow）

这是最常用和最安全的 OIDC 流程，适用于 Web 应用和移动应用。

```mermaid
sequenceDiagram
    participant User as 用户
    participant RP as Relying Party (客户端)
    participant OP as OpenID Provider (IAM SSO)
    
    User->>RP: 1. 访问受保护资源
    RP->>OP: 2. 重定向到授权端点
    Note over RP,OP: /oauth2/authorize?response_type=code<br/>&client_id=xxx&redirect_uri=xxx<br/>&scope=openid profile email<br/>&state=xxx&nonce=xxx
    OP->>User: 3. 显示登录页面
    User->>OP: 4. 输入凭据并登录
    OP->>RP: 5. 重定向回客户端（带授权码）
    Note over OP,RP: redirect_uri?code=AUTH_CODE&state=xxx
    RP->>OP: 6. 用授权码换取 Token
    Note over RP,OP: POST /oauth2/token<br/>grant_type=authorization_code
    OP->>RP: 7. 返回 Token 响应
    Note over OP,RP: {access_token, id_token, refresh_token}
    RP->>OP: 8. 获取用户信息（可选）
    Note over RP,OP: GET /userinfo
    OP->>RP: 9. 返回用户信息
    RP->>User: 10. 建立用户会话
```

### 4.2 请求参数详解

#### 授权请求参数

| 参数 | 必需 | 说明 | 示例 |
|------|------|------|------|
| `response_type` | 是 | 必须为 `code` | `code` |
| `client_id` | 是 | 客户端标识符 | `my-app` |
| `redirect_uri` | 是 | 重定向 URI | `https://app.com/callback` |
| `scope` | 是 | 必须包含 `openid` | `openid profile email` |
| `state` | 推荐 | 防 CSRF 攻击 | `random-state-string` |
| `nonce` | 推荐 | 防重放攻击 | `random-nonce-value` |
| `code_challenge` | 条件 | PKCE 挑战值 | `base64url(sha256(code_verifier))` |
| `code_challenge_method` | 条件 | PKCE 方法 | `S256` |

#### Token 请求参数

| 参数 | 必需 | 说明 | 示例 |
|------|------|------|------|
| `grant_type` | 是 | 必须为 `authorization_code` | `authorization_code` |
| `code` | 是 | 授权码 | `AUTH_CODE` |
| `redirect_uri` | 是 | 必须与授权请求一致 | `https://app.com/callback` |
| `client_id` | 是 | 客户端标识符 | `my-app` |
| `client_secret` | 条件 | 客户端密钥（机密客户端） | `my-secret` |
| `code_verifier` | 条件 | PKCE 验证器 | `random-code-verifier` |

### 4.3 Token 响应

```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer",
  "expires_in": 3600,
  "refresh_token": "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4...",
  "scope": "openid profile email",
  "id_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

---

## 5. ID Token 详解

### 5.1 ID Token 结构

ID Token 是 JWT 格式，包含三个部分：Header、Payload、Signature。

#### Header 示例

```json
{
  "alg": "RS256",
  "typ": "JWT",
  "kid": "key-id-123"
}
```

#### Payload 示例

```json
{
  "iss": "https://sso.iam-platform.com",
  "sub": "user-uuid-12345",
  "aud": "my-app-client-id",
  "exp": 1704067200,
  "iat": 1704063600,
  "auth_time": 1704063500,
  "nonce": "random-nonce-value",
  "email": "user@example.com",
  "email_verified": true,
  "name": "张三",
  "nickname": "zhangsan",
  "roles": ["admin", "user"]
}
```

### 5.2 标准 Claims

| Claim | 类型 | 必需 | 说明 |
|-------|------|------|------|
| `iss` | String | 是 | Issuer，签发者标识符 |
| `sub` | String | 是 | Subject，用户唯一标识符 |
| `aud` | String | 是 | Audience，受众（客户端 ID） |
| `exp` | Number | 是 | Expiration Time，过期时间 |
| `iat` | Number | 是 | Issued At，签发时间 |
| `auth_time` | Number | 条件 | Authentication Time，认证时间 |
| `nonce` | String | 条件 | 防重放攻击随机数 |
| `acr` | String | 可选 | Authentication Context Class Reference |
| `amr` | Array | 可选 | Authentication Methods References |

### 5.3 自定义 Claims

根据请求的 scope，可能包含以下 Claims：

| Scope | Claims |
|-------|--------|
| `profile` | `name`, `nickname`, `picture`, `roles`, `tenant_id` 等 |
| `email` | `email`, `email_verified` |
| `phone` | `phone_number`, `phone_number_verified` |
| `address` | `address` 对象 |

### 5.4 ID Token 验证

客户端必须验证 ID Token 的以下内容：

1. **签名验证**: 使用 OP 的公钥验证 JWT 签名
2. **Issuer 验证**: `iss` 必须匹配预期的 OP 标识符
3. **Audience 验证**: `aud` 必须包含客户端 ID
4. **过期验证**: `exp` 必须在当前时间之后
5. **Nonce 验证**: `nonce` 必须与授权请求中发送的一致
6. **Issued At 验证**: `iat` 必须在合理的时间范围内

---

## 6. UserInfo 端点

### 6.1 端点说明

UserInfo 端点提供关于已认证用户的更多信息。客户端使用 Access Token 访问此端点。

### 6.2 请求示例

```http
GET /userinfo HTTP/1.1
Host: sso.iam-platform.com
Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...
Accept: application/json
```

### 6.3 响应示例

```json
{
  "sub": "user-uuid-12345",
  "name": "张三",
  "nickname": "zhangsan",
  "email": "user@example.com",
  "email_verified": true,
  "roles": ["admin", "user"],
  "tenant_id": 100,
  "tenant_code": "company-a"
}
```

### 6.4 返回的 Claims

返回的 Claims 取决于请求的 scope 和用户的授权。与 ID Token 类似，但可能包含更多信息。

---

## 7. OIDC 发现文档

### 7.1 发现端点

```
GET /.well-known/openid-configuration
```

### 7.2 响应示例

```json
{
  "issuer": "https://sso.iam-platform.com",
  "authorization_endpoint": "https://sso.iam-platform.com/oauth2/authorize",
  "token_endpoint": "https://sso.iam-platform.com/oauth2/token",
  "userinfo_endpoint": "https://sso.iam-platform.com/userinfo",
  "jwks_uri": "https://sso.iam-platform.com/oauth2/jwks",
  "registration_endpoint": "https://sso.iam-platform.com/connect/register",
  "scopes_supported": ["openid", "profile", "email", "phone", "address"],
  "response_types_supported": ["code"],
  "grant_types_supported": ["authorization_code", "refresh_token", "client_credentials"],
  "subject_types_supported": ["public"],
  "id_token_signing_alg_values_supported": ["RS256"],
  "token_endpoint_auth_methods_supported": ["client_secret_basic", "client_secret_post"],
  "claims_supported": ["sub", "iss", "aud", "exp", "iat", "auth_time", "nonce", "email", "email_verified", "name", "nickname", "roles"]
}
```

### 7.3 用途

发现文档允许客户端自动配置与 OP 的交互，无需硬编码端点 URL。

---

## 8. 客户端注册与配置

### 8.1 客户端类型

| 类型 | 说明 | 安全性 | 示例 |
|------|------|--------|------|
| **Confidential Client** | 能安全存储密钥的客户端 | 高 | 服务器端 Web 应用 |
| **Public Client** | 无法安全存储密钥的客户端 | 中 | SPA、移动应用 |

### 8.2 注册参数

| 参数 | 必需 | 说明 |
|------|------|------|
| `client_id` | 是 | 客户端唯一标识符 |
| `client_secret` | 条件 | 客户端密钥（仅机密客户端） |
| `redirect_uris` | 是 | 允许的重定向 URI 列表 |
| `response_types` | 是 | 支持的响应类型（`code`） |
| `grant_types` | 是 | 支持的授权类型 |
| `scope` | 是 | 允许的 scope 列表 |
| `token_endpoint_auth_method` | 是 | Token 端点认证方法 |

### 8.3 数据库配置示例

```sql
INSERT INTO t_oauth2_client (
    client_id,
    client_secret,
    client_name,
    client_authentication_methods,
    authorization_grant_types,
    redirect_uris,
    scopes,
    require_proof_key,
    require_authorization_consent,
    access_token_ttl_seconds,
    refresh_token_ttl_seconds
) VALUES (
    'my-web-app',
    'encrypted-secret',
    'My Web Application',
    'client_secret_basic',
    'authorization_code,refresh_token',
    'https://myapp.com/callback,https://myapp.com/silent-callback',
    'openid,profile,email',
    true,
    true,
    3600,
    86400
);
```

---

## 9. Scope 和 Claims

### 9.1 标准 OIDC Scopes

| Scope | 说明 | 返回的 Claims |
|-------|------|---------------|
| `openid` | 必须，启用 OIDC | `sub`, `iss`, `aud`, `exp`, `iat`, `auth_time`, `nonce` |
| `profile` | 用户基本信息 | `name`, `nickname`, `picture`, `roles`, `tenant_id` 等 |
| `email` | 用户邮箱 | `email`, `email_verified` |
| `phone` | 用户电话 | `phone_number`, `phone_number_verified` |
| `address` | 用户地址 | `address` 对象 |

### 9.2 Claims 映射

| Claim | 说明 | 类型 | 来源 |
|-------|------|------|------|
| `sub` | 用户唯一标识 | String | 用户 UUID |
| `name` | 全名 | String | 用户姓名字段 |
| `nickname` | 昵称 | String | 用户昵称字段 |
| `email` | 邮箱地址 | String | 用户邮箱字段 |
| `email_verified` | 邮箱是否验证 | Boolean | 用户邮箱验证状态 |
| `roles` | 用户角色列表 | Array | 用户角色关联表 |
| `tenant_id` | 租户 ID | Number | 租户上下文 |
| `tenant_code` | 租户编码 | String | 租户编码 |

### 9.3 Scope 驱动的 Claims 过滤

根据 OIDC 规范，应该根据请求的 scope 返回对应的 claims：

```java
@Override
public void customize(JwtEncodingContext context) {
    Set<String> scopes = context.getAuthorizedScopes();
    String username = context.getPrincipal().getName();
    User user = userRepository.findByUsername(username).orElse(null);
    
    if (user == null) {
        return;
    }
    
    // 只在请求 openid 时添加基本标识
    if (scopes.contains("openid")) {
        context.getClaims().claim("sub", user.getId());
    }
    
    // 只在请求 profile 时添加个人信息
    if (scopes.contains("profile")) {
        if (user.getNickname() != null) {
            context.getClaims().claim("nickname", user.getNickname());
        }
        context.getClaims().claim("roles", user.getRoles());
    }
    
    // 只在请求 email 时添加邮箱
    if (scopes.contains("email")) {
        context.getClaims().claim("email", user.getEmail());
        context.getClaims().claim("email_verified", user.isEmailVerified());
    }
}
```

---

## 10. 会话管理

### 10.1 会话管理端点

| 端点 | 说明 |
|------|------|
| `/session` | 检查用户会话状态 |
| `/connect/logout` | 发起登出流程 |

### 10.2 前端通道登出

通过 iframe 通知 RP 用户已登出：

```html
<iframe src="https://rp1.com/logout?state=xxx"></iframe>
<iframe src="https://rp2.com/logout?state=xxx"></iframe>
```

### 10.3 后端通道登出

OP 直接向 RP 的后端端点发送登出请求：

```http
POST https://rp1.com/backchannel-logout
Content-Type: application/json

{
  "logout_token": "eyJhbGciOiJSUzI1NiIs..."
}
```

### 10.4 IAM Platform 会话管理

IAM Platform 使用 Redis 存储会话，支持水平扩展：

```yaml
spring:
  session:
    store-type: redis
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
```

---

## 11. 安全考虑

### 11.1 攻击防护

| 攻击类型 | 防护措施 |
|----------|----------|
| **CSRF** | 使用 `state` 参数 |
| **重放攻击** | 使用 `nonce` 参数 |
| **授权码劫持** | 使用 PKCE |
| **Token 泄露** | 短期 Access Token + Refresh Token |
| **XSS** | 安全存储 Token，使用 HttpOnly Cookie |

### 11.2 PKCE（Proof Key for Code Exchange）

PKCE 用于防止授权码劫持攻击，特别适用于公共客户端：

```
授权请求: /oauth2/authorize?code_challenge=E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM&code_challenge_method=S256
Token 请求: POST /oauth2/token?code_verifier=dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk
```

### 11.3 Token 安全

| Token 类型 | 存储建议 | 传输要求 |
|-----------|----------|----------|
| ID Token | 内存或安全存储 | HTTPS |
| Access Token | 内存或 HttpOnly Cookie | HTTPS |
| Refresh Token | 安全后端存储 | HTTPS |

### 11.4 签名算法

IAM Platform 使用 RS256 算法签名 JWT：

- **算法**: RS256 (RSA with SHA-256)
- **密钥类型**: RSA 非对称加密
- **私钥**: 仅认证服务器持有
- **公钥**: 通过 JWK 端点公开

---

## 12. 实现示例

### 12.1 Spring Security OIDC 客户端配置

```java
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/", "/home").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/")
            );
        return http.build();
    }
}
```

### 12.2 application.yml 配置

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          iam-platform:
            client-id: my-web-app
            client-secret: my-secret
            authorization-grant-type: authorization_code
            redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"
            scope: openid, profile, email
            client-name: IAM Platform
        provider:
          iam-platform:
            issuer-uri: https://sso.iam-platform.com
```

### 12.3 手动 OIDC 流程实现

```java
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final RestTemplate restTemplate;
    private final JwtDecoder jwtDecoder;
    
    @Value("${spring.security.oauth2.client.registration.iam-platform.client-id}")
    private String clientId;
    
    @Value("${spring.security.oauth2.client.registration.iam-platform.client-secret}")
    private String clientSecret;
    
    @Value("${spring.security.oauth2.client.provider.iam-platform.issuer-uri}")
    private String issuerUri;
    
    @GetMapping("/login")
    public RedirectView login() {
        String authUrl = issuerUri + "/oauth2/authorize?"
            + "response_type=code"
            + "&client_id=" + clientId
            + "&redirect_uri=http://localhost:8080/auth/callback"
            + "&scope=openid profile email"
            + "&state=" + generateState()
            + "&nonce=" + generateNonce();
        
        return new RedirectView(authUrl);
    }
    
    @GetMapping("/callback")
    public ResponseEntity<?> callback(@RequestParam String code, @RequestParam String state) {
        // 验证 state
        if (!validateState(state)) {
            return ResponseEntity.badRequest().body("Invalid state");
        }
        
        // 换取 token
        TokenResponse tokenResponse = exchangeCodeForToken(code);
        
        // 验证 ID Token
        Jwt idToken = jwtDecoder.decode(tokenResponse.getIdToken());
        validateIdToken(idToken);
        
        // 获取用户信息
        UserInfo userInfo = getUserInfo(tokenResponse.getAccessToken());
        
        // 建立会话
        establishUserSession(userInfo);
        
        return ResponseEntity.ok(userInfo);
    }
    
    private TokenResponse exchangeCodeForToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(clientId, clientSecret);
        
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("code", code);
        body.add("redirect_uri", "http://localhost:8080/auth/callback");
        
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<TokenResponse> response = restTemplate.postForEntity(
            issuerUri + "/oauth2/token", request, TokenResponse.class);
        
        return response.getBody();
    }
    
    private void validateIdToken(Jwt idToken) {
        // 验证 issuer
        if (!issuerUri.equals(idToken.getIssuer())) {
            throw new RuntimeException("Invalid issuer");
        }
        
        // 验证 audience
        if (!idToken.getAudience().contains(clientId)) {
            throw new RuntimeException("Invalid audience");
        }
        
        // 验证过期时间
        if (idToken.getExpiresAt().isBefore(Instant.now())) {
            throw new RuntimeException("Token expired");
        }
    }
    
    private UserInfo getUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<UserInfo> response = restTemplate.exchange(
            issuerUri + "/userinfo", HttpMethod.GET, entity, UserInfo.class);
        
        return response.getBody();
    }
}
```

---

## 13. 最佳实践

### 13.1 客户端实现

1. **始终使用 HTTPS**: 所有 OIDC 通信必须加密
2. **验证 ID Token**: 严格验证所有 Claims
3. **使用 PKCE**: 即使是机密客户端也推荐使用
4. **安全存储 Token**: 使用内存或安全存储机制
5. **定期刷新 Token**: 在 Access Token 过期前刷新
6. **处理登出**: 实现完整的登出流程

### 13.2 服务端实现

1. **强制 HTTPS**: 拒绝非 HTTPS 请求
2. **验证 redirect_uri**: 严格匹配注册的 URI
3. **使用强随机数**: state 和 nonce 必须随机且唯一
4. **限制 Token 生命周期**: Access Token 短期，Refresh Token 中期
5. **监控异常行为**: 检测暴力破解和异常登录
6. **定期轮换密钥**: 定期更换签名密钥

### 13.3 安全实践

```java
// ✅ 推荐：使用 PKCE
String codeVerifier = generateRandomString(128);
String codeChallenge = base64UrlEncode(sha256(codeVerifier));

// ✅ 推荐：验证 state 参数
if (!session.getAttribute("state").equals(request.getParameter("state"))) {
    throw new SecurityException("Invalid state parameter");
}

// ✅ 推荐：安全存储 Token
session.setAttribute("access_token", tokenResponse.getAccessToken());
// 不要存储在 localStorage 或 Cookie 中

// ✅ 推荐：定期刷新 Token
if (tokenResponse.getExpiresIn() < 300) { // 5 分钟内过期
    refreshToken(tokenResponse.getRefreshToken());
}
```

---

## 14. 常见问题

### 14.1 ID Token 和 Access Token 有什么区别？

| 特性 | ID Token | Access Token |
|------|----------|--------------|
| **用途** | 证明用户身份 | 授权访问资源 |
| **格式** | JWT | JWT 或不透明字符串 |
| **受众** | 客户端应用 | 资源服务器 |
| **验证方** | 客户端验证 | 资源服务器验证 |
| **生命周期** | 通常较短 | 可配置 |

### 14.2 何时使用 UserInfo 端点？

- 当 ID Token 中的信息不足时
- 需要获取最新的用户信息时
- 当 ID Token 过大时（可拆分信息）

### 14.3 如何处理 Token 过期？

1. 监控 Access Token 的 `expires_in` 值
2. 在过期前使用 Refresh Token 获取新 Token
3. 如果 Refresh Token 也过期，要求用户重新登录

### 14.4 为什么需要 nonce 参数？

`nonce` 参数用于防止重放攻击。客户端在授权请求中发送 nonce，OP 将其包含在 ID Token 中，客户端验证 ID Token 中的 nonce 与发送的一致。

### 14.5 如何实现单点登出？

1. 用户向 OP 发起登出请求
2. OP 清除用户会话
3. OP 通知所有 RP 用户已登出（前端或后端通道）
4. RP 清除本地会话

---

## 附录

### A. OIDC 规范参考

- [OpenID Connect Core 1.0](https://openid.net/specs/openid-connect-core-1_0.html)
- [OpenID Connect Discovery 1.0](https://openid.net/specs/openid-connect-discovery-1_0.html)
- [OpenID Connect Session Management 1.0](https://openid.net/specs/openid-connect-session-1_0.html)
- [OAuth 2.0 Authorization Framework](https://datatracker.ietf.org/doc/html/rfc6749)
- [JSON Web Token (JWT)](https://datatracker.ietf.org/doc/html/rfc7519)

### B. 在线工具

- [JWT.io](https://jwt.io) - JWT 解码和验证
- [OIDC Playground](https://oauthoidc.online/oauth-testing) - OIDC 测试工具

### C. 相关文档

- [Spring Authorization Server 原理与流程](./Spring%20Authorization%20Server%20原理与流程.md)
- [Token 机制详解](./Token机制详解.md)
- [统一认证框架](./统一认证框架.md)
- [第三方服务对接指南](./第三方服务对接指南.md)

---

**文档版本**: 1.0  
**最后更新**: 2026-05-17  
**维护者**: IAM Platform 团队