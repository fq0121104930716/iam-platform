# CAS单点登出使用示例

## 场景说明

本文档演示如何在客户端应用中集成CAS单点登出功能。

## 前置条件

1. IAM平台已启动并运行
2. CAS服务地址：`http://localhost:9000/cas`
3. 客户端应用已集成CAS Client

## 示例1：基本的登出流程

### 1.1 用户访问应用A

```
用户浏览器 -> http://app-a.example.com/protected
```

应用A检测到未登录，重定向到CAS登录页面：

```
http://localhost:9000/cas/login?service=http://app-a.example.com/cas/callback
```

### 1.2 用户登录成功

CAS验证用户身份后，生成Service Ticket并重定向回应用A：

```
http://app-a.example.com/cas/callback?ticket=ST-abc123
```

应用A验证Ticket成功，建立本地会话。

### 1.3 用户访问应用B

用户访问应用B时，由于CAS会话已存在，自动登录：

```
http://localhost:9000/cas/login?service=http://app-b.example.com/cas/callback
-> 自动重定向到 http://app-b.example.com/cas/callback?ticket=ST-def456
```

此时，CAS会话中记录了两个服务：
- `http://app-a.example.com/cas/callback`
- `http://app-b.example.com/cas/callback`

### 1.4 用户发起登出

用户访问CAS登出页面：

```
GET http://localhost:9000/cas/logout?service=http://app-a.example.com
```

**Front Channel Logout流程：**

1. CAS显示登出进度页面
2. 通过iframe依次调用：
   - `http://app-a.example.com/cas/callback?logoutRequest=<session-id>`
   - `http://app-b.example.com/cas/callback?logoutRequest=<session-id>`
3. 所有服务登出完成后，重定向到 `http://app-a.example.com`

## 示例2：客户端应用集成代码

### 2.1 Spring Boot应用集成

#### 添加依赖

```xml
<dependency>
    <groupId>org.jasig.cas.client</groupId>
    <artifactId>cas-client-core</artifactId>
    <version>3.6.2</version>
</dependency>
```

#### 配置CAS Filter

```java
@Configuration
public class CasConfig {
    
    @Value("${cas.server.url}")
    private String casServerUrl;
    
    @Value("${cas.client.url}")
    private String clientUrl;
    
    // CAS认证Filter
    @Bean
    public FilterRegistrationBean<CasAuthenticationFilter> casAuthenticationFilter() {
        FilterRegistrationBean<CasAuthenticationFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new CasAuthenticationFilter());
        registration.addUrlPatterns("/*");
        registration.addInitParameter("casServerLoginUrl", casServerUrl + "/login");
        registration.addInitParameter("serverName", clientUrl);
        registration.setOrder(2);
        return registration;
    }
    
    // CAS登出Filter - 支持Back Channel
    @Bean
    public FilterRegistrationBean<SingleSignOutFilter> singleSignOutFilter() {
        FilterRegistrationBean<SingleSignOutFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new SingleSignOutFilter());
        registration.addUrlPatterns("/*");
        registration.addInitParameter("casServerUrlPrefix", casServerUrl);
        registration.setOrder(1);
        return registration;
    }
    
    // 登出Servlet
    @Bean
    public ServletRegistrationBean<CasSingleSignOutServlet> casSingleSignOutServlet() {
        ServletRegistrationBean<CasSingleSignOutServlet> registration = new ServletRegistrationBean<>();
        registration.setServlet(new CasSingleSignOutServlet());
        registration.addUrlMappings("/logout");
        return registration;
    }
}
```

### 2.2 手动实现登出端点

如果不使用CAS Client库，可以手动实现：

#### Back Channel登出端点

```java
@RestController
@RequestMapping("/cas")
public class CasLogoutController {
    
    @PostMapping("/logout")
    public void handleBackChannelLogout(
            @RequestParam String logoutRequest,
            HttpServletRequest httpRequest) {
        
        try {
            // 解码logoutRequest (Base64)
            String decoded = new String(
                Base64.getDecoder().decode(logoutRequest), 
                StandardCharsets.UTF_8
            );
            
            // 提取SessionIndex
            String sessionId = extractSessionIndex(decoded);
            
            // 使本地会话失效
            if (sessionId != null) {
                // 根据sessionId找到对应的HttpSession并invalidate
                SessionRegistry.invalidateSession(sessionId);
                log.info("Session {} invalidated via Back Channel Logout", sessionId);
            }
            
        } catch (Exception e) {
            log.error("Failed to process logout request", e);
        }
    }
    
    private String extractSessionIndex(String logoutRequest) {
        int start = logoutRequest.indexOf("<samlp:SessionIndex>");
        if (start == -1) {
            start = logoutRequest.indexOf("<SessionIndex>");
        }
        if (start != -1) {
            int contentStart = logoutRequest.indexOf(">", start) + 1;
            int contentEnd = logoutRequest.indexOf("</", contentStart);
            if (contentEnd != -1) {
                return logoutRequest.substring(contentStart, contentEnd).trim();
            }
        }
        return null;
    }
}
```

#### Front Channel登出端点

```java
@GetMapping("/logout")
public String handleFrontChannelLogout(HttpServletRequest request) {
    // 使HTTP会话失效
    if (request.getSession(false) != null) {
        request.getSession().invalidate();
    }
    
    log.info("Front Channel Logout completed for this service");
    
    // 返回简单的登出成功页面
    return "logout-success";
}
```

## 示例3：使用Postman测试

### 3.1 测试Back Channel登出

```http
POST http://localhost:9000/cas/logout/backChannel
Content-Type: application/x-www-form-urlencoded

logoutRequest=PHNhbWxwOkxvZ291dFJlcXVlc3QgeG1sbnM6c2FtbHA9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDpwcm90b2NvbCIgSUQ9Il8xMjM0NTYiIFZlcnNpb249IjIuMCIgSXNzdWVJbnN0YW50PSIyMDI0LTAxLTAxVDAwOjAwOjAwWiI+PHNhbWw6SXNzdWVyPmh0dHBzOi8vc3NvLmV4YW1wbGUuY29tPC9zYW1sOklzc3Vlcj48c2FtbHA6U2Vzc2lvbkluZGV4PnRlc3Qtc2Vzc2lvbi1pZDwvc2FtbHA6U2Vzc2lvbkluZGV4Pjwvc2FtbHA6TG9nb3V0UmVxdWVzdD4=
```

**预期响应：**

```xml
<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'>
    <cas:logoutSuccess/>
</cas:serviceResponse>
```

### 3.2 测试健康检查

```http
GET http://localhost:9000/cas/health
```

**预期响应：**

```json
{
    "status": "UP",
    "protocol": "CAS 3.0",
    "slo": "enabled"
}
```

## 示例4：调试和日志

### 4.1 启用调试日志

在 `application.yml` 中添加：

```yaml
logging:
  level:
    iam.platform.auth.interfaces.web.CasSloHandler: DEBUG
    iam.platform.auth.application.service.CasSloService: DEBUG
    iam.platform.auth.interfaces.web.CasController: DEBUG
```

### 4.2 查看登出流程日志

正常登出流程会输出类似以下日志：

```
2024-01-01 12:00:00 INFO  - CAS logout initiated
2024-01-01 12:00:00 DEBUG - Service registered for SLO session abc123
2024-01-01 12:00:00 INFO  - Front Channel Logout to 2 services
2024-01-01 12:00:01 INFO  - Front Channel Logout callback from service: http://app-a.example.com
2024-01-01 12:00:02 INFO  - Front Channel Logout callback from service: http://app-b.example.com
2024-01-01 12:00:02 INFO  - Session abc123 invalidated, all tickets cleaned up
2024-01-01 12:00:02 INFO  - CAS logout completed
```

## 常见问题

### Q1: 登出后仍然可以访问应用？

**原因：** 应用本地会话未正确失效

**解决方案：**
- 检查是否正确实现了 `/logout` 端点
- 确认 `request.getSession().invalidate()` 被调用
- 检查应用的Session管理逻辑

### Q2: 某些服务没有登出？

**原因：** 服务未正确注册到SLO会话

**解决方案：**
- 确认 `CasController.processCasLogin()` 中调用了 `registerServiceForSession()`
- 检查Redis连接是否正常
- 查看日志确认服务注册情况

### Q3: 登出页面一直加载？

**原因：** Front Channel Logout的iframe请求超时

**解决方案：**
- 检查所有服务的登出端点是否可达
- 确认服务能快速响应登出请求
- 页面有10秒超时自动跳转机制

## 最佳实践

1. **使用Back Channel优先**：更安全，用户体验更好
2. **实现幂等登出**：多次登出请求不会产生错误
3. **添加超时机制**：避免登出流程长时间阻塞
4. **记录审计日志**：追踪登出操作，便于问题排查
5. **测试异常场景**：验证服务不可用时的降级逻辑
