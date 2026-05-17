# IAM Platform 项目未完成逻辑清单

> 生成时间: 2026-05-13
> 检查范围: 全部业务逻辑、服务实现、基础设施集成

---

## 一、当前可实现的未完成功能

### 1.1 验证码服务 - 真实 SMS/Email 集成

**位置:** `VerificationCodeServiceImpl.java` (第 31-33 行, 第 42-44 行)

**当前状态:**
- ✅ Redis 验证码生成、存储、验证逻辑已完整实现
- ✅ 已添加速率限制（60秒内只能发送一次，每日最多10次）
- ✅ 已使用 SecureRandom 替代 Random 生成验证码
- ⚠️ SMS 发送：仅打印日志，未集成真实短信服务
- ⚠️ Email 发送：仅打印日志，未集成真实邮件服务

**TODO 标记:**
```java
// TODO: 集成真实的 SMS 服务(如阿里云、腾讯云)
// 开发环境下打印到日志,生产环境应调用 SMS API

// TODO: 集成真实的邮件服务(如 Spring Mail)
// 开发环境下打印到日志,生产环境应发送邮件
```

**实现建议:**
- 引入 `spring-boot-starter-mail` 实现邮件发送
- 引入阿里云/腾讯云 SMS SDK 实现短信发送
- 使用策略模式或配置开关区分开发/生产环境
- 可先实现 Mock 服务用于测试

**优先级:** 🔴 高 (影响验证码登录功能完整性)
**状态:** ✅ 已完成安全加固和速率限制，等待第三方服务集成

---

### 1.2 User 与 Person 模型关系不一致

**位置:** 
- `User.java` - 遗留的旧用户模型（已标记为兼容保留）
- `Person.java` - 新的自然人模型
- `CustomUserDetailsService.java` - 已迁移到 Person
- `VerificationCodeServiceImpl.java` - ✅ 已迁移到 Person

**问题描述:**
项目已从 User 模型迁移到 Person 模型(多租户改造)，验证码服务已完全迁移到 Person 模型:
- ✅ `VerificationCodeService.findOrCreatePersonByPhone()` 现在创建 Person 对象
- ✅ `VerificationCodeApplicationService` 返回的 Authentication 基于 Person
- ✅ 验证码登录已关联多租户体系

**影响范围:**
- ✅ 短信/邮箱验证码登录流程已完成迁移
- ⚠️ 第三方 OAuth2 登录流程(可能也使用 User，待检查)

**实现建议:**
1. ✅ 将 `VerificationCodeService` 接口改为返回 `Person` 而非 `User`
2. ✅ 更新 `findOrCreatePersonByPhone/Email` 方法，改为创建 Person
3. ⚠️ 删除或标记 `User` 实体为 `@Deprecated`（保留兼容性）
4. ✅ 统一验证码服务使用 Person 模型

**优先级:** 🔴 高 (数据模型不一致会导致运行时错误)
**状态:** ✅ 已完成核心迁移

---

### 1.3 TenantAwareAuthenticationFilter 的 extractPersonId 方法未实现

**位置:** `TenantAwareAuthenticationFilter.java` (第 161-168 行)

**当前状态:**
```java
private Long extractPersonId(Authentication auth) {
    Object principal = auth.getPrincipal();
    if (principal instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
        // Extract person ID from username by looking up Person repository
        String username = userDetails.getUsername();
        return personRepository.findByUsername(username)
                .map(Person::getId)
                .orElse(null);
    }
    return null;
}
```

**问题:** 
- ✅ 已实现通过 username 查询 Person 获取 ID
- ✅ 与 `SsoSessionService.extractPersonId()` 逻辑保持一致
- ✅ 租户上下文现在可以正确建立

**优先级:** 🔴 高 (租户识别功能完全失效)
**状态:** ✅ 已完成实现

---

### 1.4 User 实体 roles 字段初始化问题

**位置:** `User.java` (第 30 行)

**当前状态:**
```java
private Set<Role> roles = new HashSet<>();
```

**问题:**
- ✅ 已在字段声明时初始化，避免 Builder 模式下的 null 风险
- ✅ getter 中的 null 检查仍保留，作为防御性编程

**优先级:** 🟡 中 (潜在 NPE 风险)
**状态:** ✅ 已修复

---

### 1.5 验证码登录未关联多租户体系

**位置:** `VerificationCodeApplicationService.java` (第 27-63 行)

**问题:**
- ✅ 验证码登录成功后现在会建立租户上下文
- ✅ 自动识别用户的 TenantAccount 数量
- ✅ 单租户自动选择，多租户返回基础认证等待 UI 选择
- ✅ 使用 TenantAwareAuthenticationToken 替代普通 Token
- ✅ 加载用户在该租户的权限

**实现逻辑:**
1. ✅ 验证码登录成功后，查找或创建 Person
2. ✅ 查找该 Person 的 TenantAccount
3. ✅ 如果只有一个 TenantAccount，自动选择并建立 TenantAwareAuthenticationToken
4. ✅ 如果有多个，返回基础认证，由 UI 处理租户选择
5. ✅ 如果没有 TenantAccount，返回基础认证（需要先入驻租户）

**优先级:** 🟡 中 (功能可用但不符合多租户规范)
**状态:** ✅ 已完成实现

---

## 二、暂时无法实现的功能(需外部依赖)

### 2.1 OAuth2 第三方登录 Provider 配置

**位置:** `CustomOAuth2UserService.java`

**当前状态:**
- 基础框架已搭建
- 需要配置具体的 OAuth2 Provider (GitHub, Google, 微信等)

**阻塞原因:**
- 需要在第三方平台注册应用获取 Client ID/Secret
- 需要配置回调 URL
- 不同 Provider 的用户信息格式不同，需要适配

**TODO 建议:**
```java
// TODO: 配置 OAuth2 Provider (GitHub/Google/微信等)
// 1. 在第三方平台注册应用
// 2. 配置 application.yml 中的 spring.security.oauth2.client
// 3. 实现用户信息映射逻辑
```

**优先级:** 🟢 低 (核心功能不依赖)

---

### 2.2 Redis 集群/哨兵模式

**当前状态:**
- 使用单机 Redis 模式
- 配置文件中有 Redis 连接配置

**阻塞原因:**
- 生产环境可能需要 Redis 集群或哨兵模式
- 需要运维团队提供集群地址和配置

**TODO 建议:**
```yaml
# application-prod.yml
# TODO: 生产环境配置 Redis 集群
# spring:
#   redis:
#     cluster:
#       nodes: ${REDIS_CLUSTER_NODES}
```

**优先级:** 🟢 低 (部署时配置)

---

### 2.3 审计日志异步处理优化

**位置:** `AuditLogEvent.java`, `AuditLogEventListener.java`

**当前状态:**
- 使用 Spring Event 机制实现审计日志
- 目前是同步处理(在同一事务中)

**优化建议:**
```java
// TODO: 生产环境建议使用 @Async 异步处理审计日志
// 1. 启用 @EnableAsync
// 2. 在 Listener 上添加 @Async
// 3. 配置线程池
// 4. 考虑使用消息队列(RabbitMQ/Kafka)进一步解耦
```

**优先级:** 🟡 中 (性能优化，非功能阻塞)

---

## 三、代码质量问题(非功能未完成)

### 3.1 Random 实例未使用 SecureRandom

**位置:** `VerificationCodeServiceImpl.java` (第 112 行)

**当前:**
```java
private String generateCode() {
    Random random = new Random();
    return String.format("%0" + CODE_LENGTH + "d", random.nextInt(1000000));
}
```

**建议:**
```java
private static final SecureRandom SECURE_RANDOM = new SecureRandom();

private String generateCode() {
    return String.format("%0" + CODE_LENGTH + "d", SECURE_RANDOM.nextInt(1000000));
}
```

**原因:** 验证码属于安全敏感数据，应使用加密安全的随机数生成器

---

### 3.2 缺少速率限制

**位置:** `VerificationCodeServiceImpl.java`

**问题:** 
- 未限制验证码发送频率(如 60 秒内只能发送一次)
- 未限制每日发送上限
- 存在被滥用风险

**建议:**
```java
// 在 sendSmsCode/sendEmailCode 中添加
String rateLimitKey = "sms:rate:" + phone;
Boolean exists = redisTemplate.hasKey(rateLimitKey);
if (Boolean.TRUE.equals(exists)) {
    throw new IllegalStateException("Please wait before requesting another code");
}
redisTemplate.opsForValue().set(rateLimitKey, "1", Duration.ofSeconds(60));
```

---

## 四、完整 TODO 清单汇总

| 编号 | 位置 | 描述 | 类型 | 优先级 | 预计工作量 | 状态 |
|------|------|------|------|--------|------------|------|
| 1 | VerificationCodeServiceImpl:31 | 集成真实 SMS 服务 | 可实现 | 🔴 高 | 2-3 天 | ⚠️ 待第三方集成 |
| 2 | VerificationCodeServiceImpl:42 | 集成真实 Email 服务 | 可实现 | 🔴 高 | 1-2 天 | ⚠️ 待第三方集成 |
| 3 | VerificationCodeServiceImpl | 迁移 User → Person 模型 | 可实现 | 🔴 高 | 3-4 天 | ✅ 已完成 |
| 4 | TenantAwareAuthenticationFilter:161 | 实现 extractPersonId | 可实现 | 🔴 高 | 0.5 天 | ✅ 已完成 |
| 5 | User.java:30 | 修复 roles 初始化 | 可实现 | 🟡 中 | 0.5 天 | ✅ 已完成 |
| 6 | VerificationCodeApplicationService | 验证码登录关联多租户 | 可实现 | 🟡 中 | 2-3 天 | ✅ 已完成 |
| 7 | CustomOAuth2UserService | 配置 OAuth2 Provider | 需外部依赖 | 🟢 低 | 1-2 天/Provider | ⏳ 待实施 |
| 8 | application-prod.yml | Redis 集群配置 | 需外部依赖 | 🟢 低 | 0.5 天 | ⏳ 待实施 |
| 9 | AuditLogEventListener | 异步处理优化 | 性能优化 | 🟡 中 | 1 天 | ⏳ 待实施 |
| 10 | VerificationCodeServiceImpl:112 | 使用 SecureRandom | 代码质量 | 🟡 中 | 0.5 小时 | ✅ 已完成 |
| 11 | VerificationCodeServiceImpl | 添加速率限制 | 安全加固 | 🟡 中 | 1 天 | ✅ 已完成 |

---

## 五、实施建议

### 第一阶段(核心功能完善) - ✅ 已完成
1. ✅ 修复 `TenantAwareAuthenticationFilter.extractPersonId()` (任务 4)
2. ✅ 统一 User → Person 模型迁移 (任务 3)
3. ✅ 修复 User.roles 初始化问题 (任务 5)
4. ✅ 验证码登录关联多租户 (任务 6)

### 第二阶段(外部服务集成) - 预计 1-2 周
5. ⚠️ 集成 Email 服务 (任务 2)
6. ⚠️ 集成 SMS 服务 (任务 1)
7. ✅ 添加速率限制 (任务 11)
8. ✅ 使用 SecureRandom (任务 10)

### 第三阶段(优化与增强) - 按需实施
9. ⏳ OAuth2 Provider 配置 (任务 7)
10. ⏳ 审计日志异步化 (任务 9)
11. ⏳ Redis 集群配置 (任务 8)

---

## 六、快速修复建议(已完成)

以下任务已完成:

1. ✅ **TenantAwareAuthenticationFilter.extractPersonId()** - 已实现，参考 SsoSessionService 的逻辑
2. ✅ **User.roles 初始化** - 已在字段声明时初始化
3. ✅ **SecureRandom** - 已替换 Random
4. ✅ **速率限制** - 已在 Redis 中添加计数器（60秒频率限制 + 每日10次上限）

**新增完成:**
5. ✅ **User → Person 模型迁移** - 验证码服务已完全迁移到 Person 模型
6. ✅ **验证码登录关联多租户** - 已实现租户上下文自动建立和权限加载

---

**备注:** 
- 本清单基于代码静态分析生成，建议结合集成测试验证
- 优先级标注基于对核心功能的影响程度
- 预计工作量为单人开发估算，实际情况可能因团队经验而异
