---
trigger: always_on
---
# IAM Platform项目概述与核心规范

## 1. 项目概述

**项目名称：** IAM Platform Authentication Service
**项目目标：** 基于 OpenID Connect 1.0 协议的统一身份认证服务（SSO），为微服务架构提供集中式认证授权能力
**技术栈：** Java 21, Spring Boot 3.2.5, Spring Authorization Server 1.2.5, PostgreSQL, Redis, Flyway, Maven, Docker, Kubernetes
**核心依赖：** Lombok, MapStruct 1.5.5, Springdoc 2.3.0, Testcontainers 1.19.7, Thymeleaf
**AI角色：** 辅助开发、代码审查、测试生成、文档维护

## 2. 项目结构规范

```
src/main/java/sso/oidc
├── application                # 应用层 - 用例、DTO、Assembler
│   ├── assembler              # 对象映射器（MapStruct）
│   ├── dto
│   │   ├── request            # 请求 DTO
│   │   └── response           # 响应 DTO
│   └── service                # 应用服务（UserApplicationService, OAuth2ClientApplicationService 等）
├── domain                     # 领域层 - 实体、值对象、领域服务
│   ├── model
│   │   ├── entity             # 领域实体（User, Role, OAuth2Client）
│   │   ├── enums              # 枚举（UserStatus, ClientType 等）
│   │   ├── exception          # 领域异常
│   │   └── valueobject        # 值对象（Email, Password 等）
│   ├── repository             # 仓储接口
│   └── service                # 领域服务（PasswordPolicyService 等）
├── infrastructure             # 基础设施层 - 数据库、安全、外部服务
│   ├── config                 # 配置类（AuthorizationServerConfig, DefaultSecurityConfig 等）
│   ├── persistence            # 持久化实现
│   │   ├── converter          # 属性转换器
│   │   ├── entity             # JPA 实体
│   │   ├── impl               # 仓储实现
│   │   └── repository         # Spring Data JPA Repository
│   └── security               # 安全组件
│       ├── CustomUserDetailsService
│       ├── RegisteredClientRepositoryAdapter
│       └── TokenCustomizer
├── interfaces                 # 接口层 - REST API + Web 页面
│   ├── rest
│   │   └── common             # 通用组件（ApiResponse, GlobalExceptionHandler）
│   └── web                    # Thymeleaf Web 控制器（登录、注册、授权同意）
└── SsoOidcApplication.java

src/test/java/sso/oidc
├── integration                # 集成测试（使用 Testcontainers）
└── unit                       # 单元测试
```

## 3. AI参与开发规范

### 3.1 AI角色定义

- **AI开发员：** 负责生成基础代码、单元测试
- **AI审查员：** 负责代码质量检查、安全漏洞扫描
- **AI文档员：** 负责API文档、技术文档生成

### 3.2 AI工作流程

1. **需求分析：** AI生成需求文档和任务分解
2. **代码生成：** AI根据规范生成基础代码框架
3. **人工审核：** 开发人员审核AI生成的代码
4. **测试生成：** AI生成单元测试和集成测试
5. **文档维护：** AI自动更新相关文档

### 3.3 AI使用限制

- AI生成的代码必须经过人工审核
- 敏感操作（如数据库DDL）需要人工确认
- AI不能直接访问生产环境
- AI生成的密码、密钥需要重新生成
- AI实现非业务功能应该优先查询Maven仓库是否已有实现
- AI引入依赖前需要人工确认

## 4. 安全规范

### 4.1 数据安全

- 敏感数据加密存储（OAuth2 Client Secret 使用 AES 加密）
- 数据库连接使用 SSL
- 定期备份重要数据
- 访问控制最小权限原则

### 4.2 OIDC/OAuth2 安全

- RSA 密钥对安全存储（DEV 使用密钥文件，PROD 通过 K8s Secret/Vault 注入）
- 强制 Public Client 使用 PKCE（Proof Key for Code Exchange）
- Token 生命周期合理控制（Access Token 默认 1 小时，Refresh Token 默认 24 小时）
- JWT 签名使用 RS256 非对称加密
- 防止 Token 重放攻击
- 授权同意页面必须经过用户确认

### 4.3 应用安全

- 防止 SQL 注入
- 防止 XSS 攻击（Thymeleaf 默认转义）
- 防止 CSRF 攻击（表单页面启用 CSRF）
- 输入参数验证（Jakarta Validation）
- 错误信息不泄露敏感信息
- 密码使用 BCrypt 加密存储（单向，不可逆）

### 4.4 AI安全

- AI训练数据脱敏处理
- AI生成代码人工审核
- AI访问权限控制
- AI操作日志记录

## 5. 多环境配置

### 5.1 环境概览

| 环境 | Profile | 配置文件 | Docker Tag 格式 | 用途 |
|------|---------|----------|-----------------|------|
| DEV（开发） | `dev` | application-dev.yml | `<version>-dev` | 本地开发，默认激活 |
| TEST（测试） | `test` | application-test.yml | `<version>-test` | QA 测试 |
| CANARY（灰度） | `canary` | application-canary.yml | `<version>-canary` | 灰度发布验证 |
| PROD（生产） | `prod` | application-prod.yml | `<version>` | 生产环境 |

> **镜像标签详细规范**请参阅 `sso_environment_version_cicd.md`

### 5.2 环境差异

| 配置项 | DEV | TEST | CANARY | PROD |
|--------|-----|------|--------|------|
| 连接池大小 | 10 | 20 | 30 | 50 |
| Show SQL | 开启 | 关闭 | 关闭 | 关闭 |
| Swagger UI | 开启 | 开启 | 关闭 | 关闭 |
| 日志级别 | DEBUG | DEBUG | INFO | INFO |
| Thymeleaf Cache | 关闭 | 关闭 | 开启 | 开启 |
| 敏感信息默认值 | 占位符 | 无默认值 | 无默认值 | 无默认值 |

### 5.3 激活方式

- **环境变量：** `SPRING_PROFILES_ACTIVE=prod`
- **Maven Profile：** `mvn spring-boot:run -Pprod -Ddocker.image.version=1.2.3`
- **Docker：** `docker build --build-arg SPRING_PROFILE=prod -t iam-platform-service:1.2.3 .`
- **JVM参数：** `-Dspring.profiles.active=prod`
- **CI/CD脚本：** `./ci-build.ps1 -Environment prod -Version 1.2.3 -Registry registry.example.com -Push -Deploy`

### 5.4 环境变量

- `SPRING_PROFILES_ACTIVE`：激活的环境 Profile
- `DB_HOST` / `DB_PORT` / `DB_NAME`：数据库连接
- `DB_USERNAME` / `DB_PASSWORD`：数据库认证
- `REDIS_HOST` / `REDIS_PORT` / `REDIS_PASSWORD`：Redis 连接
- `ENCRYPTION_KEY`：AES 加密密钥（32字符，PROD 必须注入）
- `OIDC_ISSUER_URI`：OIDC Issuer URI
- `JWK_RSA_PRIVATE_KEY` / `JWK_RSA_PUBLIC_KEY`：RSA 密钥文件路径（PROD 通过 K8s Secret 挂载）

### 5.5 配置项分层读取规范

**原则：** 所有配置项必须通过 Spring Boot 配置机制读取，禁止在代码中直接调用 `System.getenv()`、`System.getProperty()` 或硬编码配置值。

**配置优先级（从高到低）：**

```
1. 命令行参数                --security.encryption.key=xxx
2. Java 系统属性            -Dsecurity.encryption.key=xxx
3. 环境变量                 ENCRYPTION_KEY=xxx
4. application-{profile}.yml  (如 application-dev.yml)
5. application.yml            （基础默认值）
```

**实现规范：**

- **配置属性类：** 使用 `@ConfigurationProperties` 绑定配置前缀，统一放在 `iam.platform.infrastructure.config` 包下
  ```java
  @Configuration
  @ConfigurationProperties(prefix = "security.encryption")
  public class EncryptionProperties {
      private String key;
  }
  ```

- **配置注入：** 通过构造函数注入使用配置属性类
  ```java
  public class EncryptedStringConverter implements AttributeConverter<String, String> {
      private final String key;
      public EncryptedStringConverter(EncryptionProperties properties) {
          this.key = properties != null ? properties.getKey() : null;
      }
  }
  ```

- **application.yml 回退值：** 基础配置中的占位符必须设置空默认值，避免保留未解析的占位符字符串
  ```yaml
  security:
    encryption:
      key: ${ENCRYPTION_KEY:}
  ```

- **Profile 默认值：** 各环境 Profile 可设置环境特定的安全默认值
  ```yaml
  # application-dev.yml
  security:
    encryption:
      key: ${ENCRYPTION_KEY:dev-default-key-32chars-long}
  ```

- **空值处理：** 当配置值为 null 或空白时，业务代码应优雅降级（如跳过加密），禁止抛出异常导致启动失败

**禁止事项：**
- ❌ 禁止使用 `System.getenv("ENCRYPTION_KEY")` 直接读取环境变量
- ❌ 禁止使用 `System.getProperty("key")` 直接读取系统属性
- ❌ 禁止在代码中硬编码任何环境相关的配置值
