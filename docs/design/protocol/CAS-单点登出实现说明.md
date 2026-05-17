# CAS单点登出(SLO)实现说明

## 概述

本次实现为IAM平台添加了完整的CAS 3.0单点登出（Single Logout, SLO）功能，支持Back Channel和Front Channel两种登出模式。

## 实现的功能

### 1. Back Channel Logout（服务端到服务端）

**工作原理：**
- CAS Server直接向所有已注册的服务发送HTTP POST请求
- 服务在后台处理登出，无需用户浏览器参与
- 更安全可靠，用户无感知

**实现端点：**
- `POST /cas/logout/backChannel` - 接收Back Channel登出请求

**请求格式：**
```xml
<samlp:LogoutRequest xmlns:samlp="urn:oasis:names:tc:SAML:2.0:protocol"
                     xmlns:saml="urn:oasis:names:tc:SAML:2.0:assertion"
                     ID="_unique-id"
                     Version="2.0"
                     IssueInstant="2024-01-01T00:00:00Z">
    <saml:Issuer>https://sso.example.com</saml:Issuer>
    <samlp:SessionIndex>session-id-here</samlp:SessionIndex>
</samlp:LogoutRequest>
```

### 2. Front Channel Logout（浏览器重定向）

**工作原理：**
- 通过浏览器依次重定向到所有已注册的服务
- 使用隐藏的iframe实现无缝登出
- 用户可以看到登出进度

**实现端点：**
- `GET /cas/logout` - 发起Front Channel登出
- `GET /cas/logout/frontChannel` - 服务登出回调
- `GET /cas/logoutResponse` - 接收登出响应

**流程：**
1. 用户访问 `/cas/logout`
2. 系统显示登出进度页面（`cas-logout-redirect.html`）
3. 通过iframe依次调用各服务的登出URL
4. 所有服务登出完成后重定向到指定页面

### 3. 会话管理与Ticket清理

**CasSloService核心功能：**
- `registerServiceForSession()` - 注册服务到会话（在创建ST时调用）
- `getServicesForSession()` - 获取会话关联的所有服务
- `invalidateSession()` - 使会话失效并清理所有相关Ticket
- `processBackChannelLogout()` - 处理Back Channel登出请求
- `continueFrontChannelLogout()` - 继续Front Channel登出流程

**存储机制：**
- 主存储：Redis（支持分布式部署）
- 降级方案：内存ConcurrentHashMap（Redis不可用时）

### 4. 配置项

在 `application.yml` 中添加了以下CAS配置：

```yaml
sso:
  cas:
    server-url: ${CAS_SERVER_URL:http://localhost:9000/cas}
    ticket-validity-seconds: ${CAS_TICKET_VALIDITY:600}
    ticket-prefix: ${CAS_TICKET_PREFIX:ST}
    single-sign-out-enabled: ${CAS_SLO_ENABLED:true}
    logout-url: ${CAS_LOGOUT_URL:http://localhost:9000/cas/logout}
    login-url: ${CAS_LOGIN_URL:http://localhost:9000/cas/login}
    front-channel-logout-enabled: ${CAS_FRONT_CHANNEL_LOGOUT:true}
    back-channel-logout-enabled: ${CAS_BACK_CHANNEL_LOGOUT:true}
    logout-request-ttl-seconds: ${CAS_LOGOUT_TTL:300}
    send-logout-to-all-services: ${CAS_LOGOUT_ALL_SERVICES:true}
```

## 文件清单

### 新增文件

1. **CasSloHandler.java**
   - 路径：`iam-auth-server/src/main/java/iam/platform/auth/interfaces/web/CasSloHandler.java`
   - 功能：处理所有SLO相关的HTTP请求

2. **CasSloService.java**
   - 路径：`iam-auth-server/src/main/java/iam/platform/auth/application/service/CasSloService.java`
   - 功能：SLO业务逻辑，会话管理，Ticket清理

3. **cas-logout-redirect.html**
   - 路径：`iam-auth-server/src/main/resources/templates/cas-logout-redirect.html`
   - 功能：Front Channel登出进度页面，带动画效果

### 修改文件

1. **CasController.java**
   - 移除了简单的logout方法（由CasSloHandler接管）
   - 添加了CasSloService依赖
   - 在创建ST时注册服务到SLO会话
   - 更新health端点显示SLO状态

2. **CasProperties.java**
   - 添加了SLO相关配置属性

3. **application.yml**
   - 添加了完整的CAS配置段

## 使用示例

### 1. 用户发起登出

```
GET http://localhost:9000/cas/logout?service=https://app.example.com
```

- 如果有已注册的服务：显示登出进度页面，依次登出所有服务
- 如果没有服务：直接完成登出并重定向到service参数指定的URL

### 2. Back Channel登出请求

```bash
POST http://localhost:9000/cas/logout/backChannel
Content-Type: application/x-www-form-urlencoded

logoutRequest=PHNhbWxwOkxvZ291dFJlcXVlc3Q+Li4uPC9zYW1scDpMb2dvdXRSZXF1ZXN0Pg==
```

### 3. 服务集成示例

客户端应用需要实现登出端点来接收CAS的登出请求：

**Back Channel方式：**
```java
@PostMapping("/logout")
public void handleLogout(@RequestParam String logoutRequest) {
    // 解码并处理登出请求
    // 使本地会话失效
    // 清理用户认证状态
}
```

**Front Channel方式：**
```java
@GetMapping("/logout")
public String handleFrontChannelLogout(HttpServletRequest request) {
    // 使会话失效
    request.getSession().invalidate();
    return "logout-success";
}
```

## 安全考虑

1. **Ticket一次性使用**：ST验证后立即删除，防止重放攻击
2. **会话关联追踪**：记录每个会话访问的所有服务，确保登出完整
3. **TTL过期机制**：所有登出相关数据都有过期时间，防止内存泄漏
4. **降级容错**：Redis不可用时自动切换到内存存储

## 后续优化建议

1. **SAML解析库**：当前使用简单的字符串解析，建议引入OpenSAML进行标准SAML消息处理
2. **异步登出**：对于大量服务，可以实现异步并发登出
3. **登出重试机制**：对于登出失败的服务，增加重试逻辑
4. **审计日志**：记录完整的登出流程，便于问题排查
5. **性能优化**：使用Redis Pipeline批量操作，减少网络往返

## 测试建议

1. **单元测试**：测试CasSloService的各个方法
2. **集成测试**：模拟完整的登出流程
3. **端到端测试**：使用真实服务验证Front/Back Channel登出
4. **压力测试**：测试大量服务同时登出的性能
5. **异常测试**：验证Redis不可用时的降级逻辑

## 兼容性

- 兼容CAS 3.0协议
- 支持SAML 2.0 Logout协议格式
- 向后兼容现有的简单登出方式
