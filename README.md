# IAM Platform Authentication Service

基于 OpenID Connect 1.0 协议的统一身份认证服务（SSO），为微服务架构提供集中式认证授权能力。

## 快速导航

| 文档 | 说明 |
|------|------|
| [文档索引](docs/index.md) | 完整文档导航索引 |
| [部署指南](DEPLOYMENT.md) | Docker 构建、部署、运维监控 |
| [Docker操作速查](DOCKER_OPERATIONS.md) | Docker 命令快速参考 |
| [脚本参考](SCRIPTS_REFERENCE.md) | ci-build.ps1 和 uninstall-env.ps1 详细说明 |
| [命令速查](QUICK_REFERENCE.md) | 环境管理常用命令速查表 |
| [Mock 数据管理](src/main/resources/db/dev/README_MOCK_DATA.md) | DEV 环境测试数据重置指南 |

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 21 | LTS 版本 |
| Spring Boot | 3.2.5 | 应用框架 |
| Spring Authorization Server | 1.2.5 | OIDC Provider |
| PostgreSQL | 14+ | 主数据库 |
| Redis | 7+ | 缓存 + 分布式 Session |
| Maven | 3.8+ | 构建工具 |
| Docker | - | 容器化部署 |
| Lombok | - | 减少样板代码 |
| MapStruct | 1.5.5 | 对象映射 |
| Thymeleaf | - | 服务端渲染（登录/注册页） |
| Springdoc | 2.3.0 | API 文档 |
| Testcontainers | 1.19.7 | 集成测试 |

## 核心功能

### OIDC Provider

- ✅ Authorization Code Grant + PKCE
- ✅ Client Credentials Grant
- ✅ Refresh Token
- ✅ OIDC Discovery
- ✅ JWKS Endpoint
- ✅ UserInfo Endpoint
- ✅ Token Revocation

### 用户管理

- ✅ 用户注册与登录
- ✅ 用户 CRUD API
- ✅ 密码管理（BCrypt 加密）
- ✅ 账户状态管理

### OAuth2 客户端管理

- ✅ 客户端注册与配置
- ✅ Client Secret 管理（AES-256-GCM 加密）
- ✅ 授权类型与作用域配置

### 角色权限管理

- ✅ 角色 CRUD
- ✅ 角色分配
- ✅ RBAC 权限控制

## 快速开始

### 环境要求

- JDK 21+
- PostgreSQL 14+
- Redis 7+
- Maven 3.8+

### 本地运行

```bash
# 编译项目
./mvnw clean compile

# 运行服务（默认 dev 环境）
./mvnw spring-boot:run
```

### 访问服务

启动后可访问以下端点：

| 端点 | URL | 说明 |
|------|-----|------|
| Swagger UI | http://localhost:9000/swagger-ui.html | API 文档（DEV/TEST 环境） |
| OIDC Discovery | http://localhost:9000/.well-known/openid-configuration | OIDC 元数据 |
| 登录页面 | http://localhost:9000/login | Web 登录 |
| 健康检查 | http://localhost:9001/actuator/health | 应用健康状态 |

## 项目结构

本项目采用 **Clean Architecture** + **Domain-Driven Design** 架构：

```
src/main/java/sso/oidc
├── application/           # 应用层 - 用例编排、DTO、Assembler
├── domain/                # 领域层 - 实体、值对象、领域服务、仓储接口
├── infrastructure/        # 基础设施层 - 数据库、安全、配置实现
├── interfaces/            # 接口层 - REST API + Web 控制器
└── SsoOidcApplication.java
```

详细架构说明请参阅项目 Wiki。

## 开发与规范

### 代码规范

- 使用 Lombok 减少样板代码
- 方法长度 ≤ 50 行，类长度 ≤ 500 行
- 单元测试覆盖率 ≥ 80%
- 遵循 RESTful API 设计规范

### Git 工作流

采用 Git Flow 分支模型：

```
feature/* → develop → release/* → canary/* → master
   (DEV)      (DEV)    (TEST)     (CANARY)    (PROD)
```

详细分支策略和提交规范请参阅 [sso_team_collaboration.md](.qoder/rules/sso_team_collaboration.md)。

### 安全规范

- 用户密码：BCrypt 单向加密
- Client Secret：AES-256-GCM 对称加密
- JWT 签名：RS256 非对称加密
- Public Client 强制使用 PKCE
- Token 生命周期：Access Token 1h，Refresh Token 24h

完整安全规范请参阅 [sso_project_overview.md](.qoder/rules/sso_project_overview.md#4-安全规范)。

### AI 协作规范

- AI 生成代码必须人工审核
- 敏感操作（DDL、密钥）需人工确认
- AI 不能直接访问生产环境
- 引入依赖前需人工确认

## 多环境配置

项目支持四套环境，通过 Spring Profile 实现配置隔离：

| 环境 | Profile | Docker Tag | 用途 |
|------|---------|------------|------|
| DEV | `dev` | `<version>-dev` | 本地开发（默认激活） |
| TEST | `test` | `<version>-test` | QA 测试 |
| CANARY | `canary` | `<version>-canary` | 灰度发布验证 |
| PROD | `prod` | `<version>` | 生产环境 |

详细环境差异、激活方式和配置说明请参阅 [DEPLOYMENT.md](DEPLOYMENT.md#多环境配置)。

## Docker 部署

使用 Docker 进行容器化部署：

```powershell
# 一键构建并部署
./ci-build.ps1 -TargetEnvironment dev -Version 1.0.0-SNAPSHOT -Deploy
```

完整部署指南、环境配置和回滚操作请参阅 [DEPLOYMENT.md](DEPLOYMENT.md)。

## 数据库管理

数据库表结构由 Hibernate 自动管理（ddl-auto: validate），初始数据通过 SQL 脚本手动导入：

| 脚本 | 说明 |
|------|------|
| V1__complete_schema_initialization.sql | 完整表结构初始化与种子数据 |

> 注意：项目已从 Flyway 迁移至 Hibernate DDL 验证模式，确保数据库 schema 与 JPA 实体一致。

## 集成指南

### 与 Resource Server 集成

本项目作为 OIDC Provider，其他微服务可通过以下方式对接：

1. 添加 `spring-boot-starter-oauth2-resource-server` 依赖
2. 配置 JWT 验证：
   ```yaml
   spring:
     security:
       oauth2:
         resourceserver:
           jwt:
             issuer-uri: http://iam-platform-service:9000
   ```
3. JWT 包含 `roles`、`email`、`nickname` 等自定义 Claims
4. 从 JWT 中提取用户身份和角色进行 RBAC 权限控制
