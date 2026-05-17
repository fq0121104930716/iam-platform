# Token 验证流程测试指南

## 前置条件

确保 IAM Platform 服务已启动并运行在 http://localhost:9000

```powershell
# 启动服务（如果未运行）
cd d:\VsCodeProject\iam-platform
.\mvnw spring-boot:run
```

## 测试步骤

### 步骤 1: 验证 JWKS 端点（公钥分发）

```powershell
# 获取 JWKS 公钥
curl.exe http://localhost:9000/oauth2/jwks
```

**预期结果：** 返回包含 RSA 公钥的 JSON，例如：
```json
{
  "keys": [
    {
      "kty": "RSA",
      "kid": "xxx",
      "use": "sig",
      "alg": "RS256",
      "n": "...",
      "e": "..."
    }
  ]
}
```

### 步骤 2: 验证 OIDC 服务发现

```powershell
# 获取 OpenID Connect 配置
curl.exe http://localhost:9000/.well-known/openid-configuration
```

**预期结果：** 返回 OIDC 端点配置，包括 issuer、authorization_endpoint、token_endpoint 等

### 步骤 3: 创建测试 OAuth2 客户端

```powershell
# 创建测试客户端（用于获取 Token）
curl.exe -X POST http://localhost:9000/v1/oauth2-clients `
  -H "Content-Type: application/json" `
  -d '{
    "clientName": "Test Client",
    "clientId": "test-client",
    "clientSecret": "test-secret-12345678901234567890",
    "grantTypes": ["client_credentials", "authorization_code"],
    "redirectUris": ["http://localhost:8080/callback"],
    "scopes": ["openid", "profile", "email"],
    "tokenEndpointAuthMethod": "client_secret_basic"
  }'
```

### 步骤 4: 获取 Access Token

```powershell
# 使用 Client Credentials 模式获取 Token
$credential = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("test-client:test-secret-12345678901234567890"))
curl.exe -X POST http://localhost:9000/oauth2/token `
  -H "Authorization: Basic $credential" `
  -H "Content-Type: application/x-www-form-urlencoded" `
  -d "grant_type=client_credentials&scope=openid"
```

**预期结果：** 返回包含 access_token 的 JSON：
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIs...",
  "token_type": "Bearer",
  "expires_in": 3600,
  "scope": "openid"
}
```

### 步骤 5: 解码 JWT Token

复制上一步返回的 `access_token`，然后在 PowerShell 中解码：

```powershell
# 替换为你的实际 token
$token = "eyJhbGciOiJSUzI1NiIs..."

# 解码 Header
$parts = $token.Split('.')
$header = [System.Text.Encoding]::UTF8.GetString(
  [System.Convert]::FromBase64String($parts[0].Replace('-', '+').Replace('_', '/').PadRight([Math]::Ceiling($parts[0].Length / 4) * 4, '='))
)
Write-Host "Header: $header"

# 解码 Payload
$payload = [System.Text.Encoding]::UTF8.GetString(
  [System.Convert]::FromBase64String($parts[1].Replace('-', '+').Replace('_', '/').PadRight([Math]::Ceiling($parts[1].Length / 4) * 4, '='))
)
Write-Host "Payload: $payload"
```

**验证要点：**
- `iss` (Issuer): 应该是 `http://localhost:9000`
- `exp` (Expires): 应该是未来时间（默认 1 小时后）
- `aud` (Audience): 应该是客户端 ID
- `scope`: 应该包含 `openid`

### 步骤 6: 使用 Token 访问受保护资源

```powershell
# 使用 Token 访问 API（替换为你的实际 token）
$token = "eyJhbGciOiJSUzI1NiIs..."
curl.exe http://localhost:9000/v1/users?page=0`&size=10 `
  -H "Authorization: Bearer $token"
```

**预期结果：** 
- 如果 Token 有效且有权限，返回用户列表
- 如果 Token 无效，返回 401 Unauthorized
- 如果 Token 有效但无权限，返回 403 Forbidden

### 步骤 7: 测试 Token 验证安全性

```powershell
# 测试过期 Token
curl.exe http://localhost:9000/v1/users `
  -H "Authorization: Bearer eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJ0ZXN0IiwiZXhwIjoxMDAwMDAwMDAwfQ.invalid"
```

**预期结果：** 应该返回 401 Unauthorized，证明系统正确拒绝了无效 Token

## Token 验证流程说明

当客户端携带 Bearer Token 访问受保护资源时，验证流程如下：

```
1. HTTP 请求到达
   Authorization: Bearer eyJhbGciOiJSUzI1NiIs...
        ↓
2. BearerTokenAuthenticationFilter 拦截请求
        ↓
3. 提取 Token 并创建 BearerTokenAuthentication
        ↓
4. JwtAuthenticationProvider 处理认证
        ↓
5. JwtDecoder 解析 Token
   - 使用 JWKSource 提供的 RSA 公钥
   - 验证签名（RS256 算法）
        ↓
6. 验证 Token Claims
   - iss (Issuer): 验证签发者
   - exp (Expiration): 验证未过期
   - aud (Audience): 验证受众
   - 其他自定义 Claims
        ↓
7. 创建 JwtAuthenticationToken
        ↓
8. SecurityContextHolder 设置认证信息
        ↓
9. 授权检查（基于角色/权限）
        ↓
10. 允许访问或返回 403 Forbidden
```

## 关键配置点

### 1. JWT 解码器配置
位置：[AuthorizationServerConfig.java](file:///d:/VsCodeProject/iam-platform/src/main/java/sso/oidc/infrastructure/config/AuthorizationServerConfig.java#L84-L86)

```java
@Bean
public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
    return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
}
```

### 2. JWK 密钥源配置
位置：[AuthorizationServerConfig.java](file:///d:/VsCodeProject/iam-platform/src/main/java/sso/oidc/infrastructure/config/AuthorizationServerConfig.java#L60-L81)

从 PEM 文件加载 RSA 密钥对，生成稳定的 KeyID

### 3. Resource Server 启用
位置：[AuthorizationServerConfig.java](file:///d:/VsCodeProject/iam-platform/src/main/java/sso/oidc/infrastructure/config/AuthorizationServerConfig.java#L54)

```java
http.oauth2ResourceServer(rs -> rs.jwt(Customizer.withDefaults()));
```

### 4. Token 自定义
位置：[TokenCustomizer.java](file:///d:/VsCodeProject/iam-platform/src/main/java/sso/oidc/infrastructure/security/TokenCustomizer.java)

在 Token 中添加自定义 Claims：email、nickname、roles

## 常见问题排查

### 问题 1: 服务未启动
```powershell
# 检查端口是否监听
netstat -ano | Select-String ":9000"

# 查看日志
# 在另一个终端运行服务，观察启动日志
```

### 问题 2: 数据库连接失败
检查 PostgreSQL 是否运行，以及 application-dev.yml 中的数据库配置

### 问题 3: Redis 连接失败
检查 Redis 是否运行，以及 application-dev.yml 中的 Redis 配置

### 问题 4: Token 验证失败
- 检查 JWKS 端点是否可访问
- 验证 Token 格式是否正确（应该是 JWT 格式）
- 检查 Token 是否过期
- 查看应用日志中的认证失败信息

## 测试检查清单

- [ ] JWKS 端点返回有效的 RSA 公钥
- [ ] OIDC 配置端点返回正确的端点信息
- [ ] 能够成功创建 OAuth2 客户端
- [ ] 能够使用 Client Credentials 获取 Token
- [ ] Token 是正确的 JWT 格式
- [ ] Token 包含必需的 Claims（iss, exp, aud, sub）
- [ ] Token 包含自定义 Claims（roles, email, nickname）
- [ ] 使用有效 Token 可以访问受保护资源
- [ ] 使用无效 Token 被正确拒绝（401）
- [ ] 使用过期 Token 被正确拒绝（401）
- [ ] Token 签名验证通过 RSA 公钥
