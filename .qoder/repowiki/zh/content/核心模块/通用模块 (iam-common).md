# 通用模块（iam-common）

<cite>
**本文档引用的文件**
- [ApiResponse.java](file://iam-common/src/main/java/iam/platform/common/api/ApiResponse.java)
- [PageResponse.java](file://iam-common/src/main/java/iam/platform/common/api/PageResponse.java)
- [CreatePersonRequest.java](file://iam-common/src/main/java/iam/platform/common/dto/request/CreatePersonRequest.java)
- [PersonResponse.java](file://iam-common/src/main/java/iam/platform/common/dto/response/PersonResponse.java)
- [BusinessException.java](file://iam-common/src/main/java/iam/platform/common/model/exception/BusinessException.java)
- [UserStatus.java](file://iam-common/src/main/java/iam/platform/common/model/enums/UserStatus.java)
- [AuditEventType.java](file://iam-common/src/main/java/iam/platform/common/model/enums/AuditEventType.java)
- [AuditLog.java](file://iam-common/src/main/java/iam/platform/common/model/annotation/AuditLog.java)
- [RequirePermission.java](file://iam-common/src/main/java/iam/platform/common/model/annotation/RequirePermission.java)
- [Password.java](file://iam-common/src/main/java/iam/platform/common/model/valueobject/Password.java)
- [Email.java](file://iam-common/src/main/java/iam/platform/common/model/valueobject/Email.java)
- [Guard.java](file://iam-common/src/main/java/iam/platform/common/util/Guard.java)
- [TenantContext.java](file://iam-common/src/main/java/iam/platform/common/context/TenantContext.java)
- [GatewayHeaderContextFilter.java](file://iam-common/src/main/java/iam/platform/common/context/GatewayHeaderContextFilter.java)
- [pom.xml](file://iam-common/pom.xml)
</cite>

## 更新摘要
**变更内容**
- 明确了Session和JWT在微服务架构中的使用边界，强化了职责分离
- 简化了租户上下文过滤器职责，移除了Session恢复逻辑
- 强化了租户上下文管理系统的标准化头部传递机制
- 新增了Session仅用于OAuth2 SavedRequest和CAS SLO的专用配置

## 目录
1. [简介](#简介)
2. [项目结构](#项目结构)
3. [核心组件](#核心组件)
4. [架构总览](#架构总览)
5. [详细组件分析](#详细组件分析)
6. [租户上下文管理系统](#租户上下文管理系统)
7. [认证与会话管理边界](#认证与会话管理边界)
8. [依赖分析](#依赖分析)
9. [性能考虑](#性能考虑)
10. [故障排查指南](#故障排查指南)
11. [结论](#结论)
12. [附录](#附录)

## 简介
本文件为通用模块（iam-common）的系统化技术文档，聚焦于统一的数据传输对象（DTO）、枚举类型、异常模型、注解与值对象等共享能力，以及标准化的 API 响应格式、分页响应与错误码规范。文档同时阐述业务异常体系、自定义异常处理机制、错误信息国际化支持建议、通用工具类与校验/安全注解的使用方式，并给出模块间依赖关系、版本兼容性与升级策略，以及在微服务架构中的支撑作用与设计原则。

**更新** 本版本重点更新了租户上下文管理系统的使用边界，明确了Session和JWT在微服务架构中的职责分离，简化了过滤器职责，强化了标准化头部传递机制。

## 项目结构
通用模块采用按职责分层的组织方式：API 响应与分页封装、请求/响应 DTO、领域枚举、异常体系、注解（审计与权限）、值对象（密码、邮箱等）与通用工具类，以及新增的租户上下文管理系统。该结构确保了跨服务复用的一致性与低耦合。

```mermaid
graph TB
subgraph "通用模块iam-common"
API["API 层<br/>ApiResponse, PageResponse"]
DTO["DTO 层<br/>request/response"]
ENUM["枚举层<br/>UserStatus, AuditEventType 等"]
EXC["异常层<br/>BusinessException 及子类"]
ANNOT["注解层<br/>AuditLog, RequirePermission"]
VO["值对象层<br/>Password, Email 等"]
UTIL["工具层<br/>Guard"]
CTX["上下文层<br/>TenantContext, GatewayHeaderContextFilter"]
end
```

**图表来源**
- [ApiResponse.java:1-61](file://iam-common/src/main/java/iam/platform/common/api/ApiResponse.java#L1-L61)
- [PageResponse.java:1-32](file://iam-common/src/main/java/iam/platform/common/api/PageResponse.java#L1-L32)
- [CreatePersonRequest.java:1-37](file://iam-common/src/main/java/iam/platform/common/dto/request/CreatePersonRequest.java#L1-L37)
- [PersonResponse.java:1-30](file://iam-common/src/main/java/iam/platform/common/dto/response/PersonResponse.java#L1-L30)
- [BusinessException.java:1-17](file://iam-common/src/main/java/iam/platform/common/model/exception/BusinessException.java#L1-L17)
- [UserStatus.java:1-8](file://iam-common/src/main/java/iam/platform/common/model/enums/UserStatus.java#L1-L8)
- [AuditEventType.java:1-59](file://iam-common/src/main/java/iam/platform/common/model/enums/AuditEventType.java#L1-L59)
- [AuditLog.java:1-46](file://iam-common/src/main/java/iam/platform/common/model/annotation/AuditLog.java#L1-L46)
- [RequirePermission.java:1-35](file://iam-common/src/main/java/iam/platform/common/model/annotation/RequirePermission.java#L1-L35)
- [Password.java:1-90](file://iam-common/src/main/java/iam/platform/common/model/valueobject/Password.java#L1-L90)
- [Email.java:1-31](file://iam-common/src/main/java/iam/platform/common/model/valueobject/Email.java#L1-L31)
- [Guard.java:1-37](file://iam-common/src/main/java/iam/platform/common/util/Guard.java#L1-L37)
- [TenantContext.java:1-113](file://iam-common/src/main/java/iam/platform/common/context/TenantContext.java#L1-L113)
- [GatewayHeaderContextFilter.java:1-50](file://iam-common/src/main/java/iam/platform/common/context/GatewayHeaderContextFilter.java#L1-L50)

**章节来源**
- [pom.xml:1-75](file://iam-common/pom.xml#L1-L75)

## 核心组件
- API 响应与分页封装：提供统一的响应体结构与分页容器，便于前端与网关统一解析。
- DTO：请求与响应的数据载体，配合 Jakarta Bean Validation 注解进行参数校验。
- 枚举：用户状态、审计事件类型等跨域语义标识。
- 异常体系：抽象业务异常基类，便于上层统一捕获与转换。
- 注解：审计日志与权限控制的声明式能力。
- 值对象：密码与邮箱等强约束的领域值对象，保障数据一致性。
- 工具类：Guard 提供前置条件与状态断言，降低重复校验代码。
- **租户上下文系统**：提供统一的线程安全租户上下文管理，支持多租户场景的身份识别与权限控制。

**章节来源**
- [ApiResponse.java:1-61](file://iam-common/src/main/java/iam/platform/common/api/ApiResponse.java#L1-L61)
- [PageResponse.java:1-32](file://iam-common/src/main/java/iam/platform/common/api/PageResponse.java#L1-L32)
- [CreatePersonRequest.java:1-37](file://iam-common/src/main/java/iam/platform/common/dto/request/CreatePersonRequest.java#L1-L37)
- [PersonResponse.java:1-30](file://iam-common/src/main/java/iam/platform/common/dto/response/PersonResponse.java#L1-L30)
- [BusinessException.java:1-17](file://iam-common/src/main/java/iam/platform/common/model/exception/BusinessException.java#L1-L17)
- [UserStatus.java:1-8](file://iam-common/src/main/java/iam/platform/common/model/enums/UserStatus.java#L1-L8)
- [AuditEventType.java:1-59](file://iam-common/src/main/java/iam/platform/common/model/enums/AuditEventType.java#L1-L59)
- [AuditLog.java:1-46](file://iam-common/src/main/java/iam/platform/common/model/annotation/AuditLog.java#L1-L46)
- [RequirePermission.java:1-35](file://iam-common/src/main/java/iam/platform/common/model/annotation/RequirePermission.java#L1-L35)
- [Password.java:1-90](file://iam-common/src/main/java/iam/platform/common/model/valueobject/Password.java#L1-L90)
- [Email.java:1-31](file://iam-common/src/main/java/iam/platform/common/model/valueobject/Email.java#L1-L31)
- [Guard.java:1-37](file://iam-common/src/main/java/iam/platform/common/util/Guard.java#L1-L37)
- [TenantContext.java:1-113](file://iam-common/src/main/java/iam/platform/common/context/TenantContext.java#L1-L113)
- [GatewayHeaderContextFilter.java:1-50](file://iam-common/src/main/java/iam/platform/common/context/GatewayHeaderContextFilter.java#L1-L50)

## 架构总览
通用模块作为平台级共享库，向上游各服务（如管理端、认证端、BFF 网关）提供统一的响应格式、数据模型、校验规则与安全注解。其设计遵循"低耦合、高内聚、可复用"的原则，避免在公共模块引入具体框架依赖，确保跨服务稳定演进。

**更新** 新增了明确的Session和JWT使用边界，强化了租户上下文管理系统的标准化头部传递机制。

```mermaid
graph TB
subgraph "上游服务"
ADMIN["管理服务"]
AUTH["认证服务"]
BFF["BFF 网关"]
GATEWAY["网关服务"]
end
COMMON["通用模块iam-common"]
ADMIN --> COMMON
AUTH --> COMMON
BFF --> COMMON
GATEWAY --> COMMON
COMMON --> |"响应/分页/DTO"| ADMIN
COMMON --> |"响应/分页/DTO"| AUTH
COMMON --> |"响应/分页/DTO"| BFF
COMMON --> |"租户上下文管理"| GATEWAY
COMMON --> |"租户上下文管理"| ADMIN
COMMON --> |"租户上下文管理"| AUTH
COMMON --> |"租户上下文管理"| BFF
```

**图表来源**
- [ApiResponse.java:1-61](file://iam-common/src/main/java/iam/platform/common/api/ApiResponse.java#L1-L61)
- [PageResponse.java:1-32](file://iam-common/src/main/java/iam/platform/common/api/PageResponse.java#L1-L32)
- [CreatePersonRequest.java:1-37](file://iam-common/src/main/java/iam/platform/common/dto/request/CreatePersonRequest.java#L1-L37)
- [PersonResponse.java:1-30](file://iam-common/src/main/java/iam/platform/common/dto/response/PersonResponse.java#L1-L30)
- [BusinessException.java:1-17](file://iam-common/src/main/java/iam/platform/common/model/exception/BusinessException.java#L1-L17)
- [AuditLog.java:1-46](file://iam-common/src/main/java/iam/platform/common/model/annotation/AuditLog.java#L1-L46)
- [RequirePermission.java:1-35](file://iam-common/src/main/java/iam/platform/common/model/annotation/RequirePermission.java#L1-L35)
- [Password.java:1-90](file://iam-common/src/main/java/iam/platform/common/model/valueobject/Password.java#L1-L90)
- [Email.java:1-31](file://iam-common/src/main/java/iam/platform/common/model/valueobject/Email.java#L1-L31)
- [Guard.java:1-37](file://iam-common/src/main/java/iam/platform/common/util/Guard.java#L1-L37)
- [TenantContext.java:1-113](file://iam-common/src/main/java/iam/platform/common/context/TenantContext.java#L1-L113)
- [GatewayHeaderContextFilter.java:1-50](file://iam-common/src/main/java/iam/platform/common/context/GatewayHeaderContextFilter.java#L1-L50)

## 详细组件分析

### API 响应与分页封装
- ApiResponse：统一响应体字段（状态码、消息、数据、错误列表、时间戳），提供成功、创建、错误三类静态工厂方法；内部嵌套 FieldError 表示字段级错误。
- PageResponse：统一分页返回结构（内容、页码、大小、总数、总页数），提供 of 工厂方法自动计算总页数。

```mermaid
classDiagram
class ApiResponse_T_ {
+int code
+String message
+T data
+FieldError[] errors
+String timestamp
+success(data) ApiResponse_T_
+created(data) ApiResponse_T_
+error(code, message, errors) ApiResponse_T_
}
class FieldError {
+String field
+String message
}
class PageResponse_T_ {
+T[] content
+int page
+int size
+long totalElements
+int totalPages
+of(content, page, size, totalElements) PageResponse_T_
}
ApiResponse_T_ o-- FieldError
PageResponse_T_ ..> "使用" ApiResponse_T_ : "分页包装"
```

**图表来源**
- [ApiResponse.java:1-61](file://iam-common/src/main/java/iam/platform/common/api/ApiResponse.java#L1-L61)
- [PageResponse.java:1-32](file://iam-common/src/main/java/iam/platform/common/api/PageResponse.java#L1-L32)

**章节来源**
- [ApiResponse.java:1-61](file://iam-common/src/main/java/iam/platform/common/api/ApiResponse.java#L1-L61)
- [PageResponse.java:1-32](file://iam-common/src/main/java/iam/platform/common/api/PageResponse.java#L1-L32)

### DTO 设计与校验
- 请求 DTO：以 CreatePersonRequest 为例，使用 Jakarta Bean Validation 注解对字段进行非空、长度、格式等约束，保证入参合法性。
- 响应 DTO：以 PersonResponse 为例，统一输出字段集合，便于前后端契约一致。

```mermaid
classDiagram
class CreatePersonRequest {
+String username
+String email
+String phone
+String password
+String nickname
+String avatarUrl
}
class PersonResponse {
+Long id
+String personCode
+String username
+String email
+String phone
+String nickname
+String avatarUrl
+boolean emailVerified
+boolean phoneVerified
+boolean enabled
+boolean accountLocked
+LocalDateTime lastLoginAt
+LocalDateTime createdAt
+LocalDateTime updatedAt
}
```

**图表来源**
- [CreatePersonRequest.java:1-37](file://iam-common/src/main/java/iam/platform/common/dto/request/CreatePersonRequest.java#L1-L37)
- [PersonResponse.java:1-30](file://iam-common/src/main/java/iam/platform/common/dto/response/PersonResponse.java#L1-L30)

**章节来源**
- [CreatePersonRequest.java:1-37](file://iam-common/src/main/java/iam/platform/common/dto/request/CreatePersonRequest.java#L1-L37)
- [PersonResponse.java:1-30](file://iam-common/src/main/java/iam/platform/common/dto/response/PersonResponse.java#L1-L30)

### 枚举类型
- 用户状态枚举：UserStatus 定义账户可用性状态，用于统一状态表达。
- 审计事件类型：AuditEventType 将审计事件按类别分组，便于统计与过滤。

```mermaid
classDiagram
class UserStatus {
<<enumeration>>
+ACTIVE
+INACTIVE
+LOCKED
}
class AuditEventType {
<<enumeration>>
+LOGIN_SUCCESS
+ROLE_ASSIGN
+PERSON_CREATED
+TENANT_CREATED
+...
}
```

**图表来源**
- [UserStatus.java:1-8](file://iam-common/src/main/java/iam/platform/common/model/enums/UserStatus.java#L1-L8)
- [AuditEventType.java:1-59](file://iam-common/src/main/java/iam/platform/common/model/enums/AuditEventType.java#L1-L59)

**章节来源**
- [UserStatus.java:1-8](file://iam-common/src/main/java/iam/platform/common/model/enums/UserStatus.java#L1-L8)
- [AuditEventType.java:1-59](file://iam-common/src/main/java/iam/platform/common/model/enums/AuditEventType.java#L1-L59)

### 异常体系与错误码规范
- 抽象业务异常：BusinessException 提供 errorCode 与 httpStatus 字段，便于上层统一映射到 API 响应码与 HTTP 状态码。
- 错误码规范建议：采用"模块前缀 + 功能位 + 顺序号"或"领域 + 子域 + 操作 + 结果"的层次化命名，确保全局唯一且可读。
- 错误信息国际化：建议在上层控制器或全局异常处理器中结合 Locale 解析消息模板，通用模块不直接依赖具体国际化实现。

```mermaid
classDiagram
class BusinessException {
<<abstract>>
-String errorCode
-int httpStatus
+BusinessException(errorCode, message, httpStatus)
}
```

**图表来源**
- [BusinessException.java:1-17](file://iam-common/src/main/java/iam/platform/common/model/exception/BusinessException.java#L1-L17)

**章节来源**
- [BusinessException.java:1-17](file://iam-common/src/main/java/iam/platform/common/model/exception/BusinessException.java#L1-L17)

### 审计与权限注解
- 审计注解：@AuditLog 标记方法以记录审计事件，支持事件类型、资源类型、动作描述模板、是否记录参数及敏感字段掩码。
- 权限注解：@RequirePermission 支持单个权限、任一满足（OR）与全部满足（AND）组合，便于细粒度授权控制。

```mermaid
classDiagram
class AuditLog {
<<annotation>>
+AuditEventType value()
+String resourceType()
+String action()
+boolean logParams()
+String[] sensitiveFields()
}
class RequirePermission {
<<annotation>>
+String value()
+String[] anyOf()
+String[] allOf()
}
```

**图表来源**
- [AuditLog.java:1-46](file://iam-common/src/main/java/iam/platform/common/model/annotation/AuditLog.java#L1-L46)
- [RequirePermission.java:1-35](file://iam-common/src/main/java/iam/platform/common/model/annotation/RequirePermission.java#L1-L35)

**章节来源**
- [AuditLog.java:1-46](file://iam-common/src/main/java/iam/platform/common/model/annotation/AuditLog.java#L1-L46)
- [RequirePermission.java:1-35](file://iam-common/src/main/java/iam/platform/common/model/annotation/RequirePermission.java#L1-L35)

### 值对象
- 邮箱值对象：Email 对输入进行正则校验，保证邮箱格式合法。
- 密码值对象：Password 负责密码策略校验与哈希存储，通过函数式接口与外部编码器解耦，避免在通用模块引入 Spring Security 依赖。

```mermaid
classDiagram
class Email {
-String value
+Email(value)
+toString() String
}
class Password {
-String hashedValue
+fromRawPassword(rawPassword, encodeFn) Password
+fromHash(hash) Password
+matches(rawPassword, matchFn) boolean
-validatePolicy(password) void
}
```

**图表来源**
- [Email.java:1-31](file://iam-common/src/main/java/iam/platform/common/model/valueobject/Email.java#L1-L31)
- [Password.java:1-90](file://iam-common/src/main/java/iam/platform/common/model/valueobject/Password.java#L1-L90)

**章节来源**
- [Email.java:1-31](file://iam-common/src/main/java/iam/platform/common/model/valueobject/Email.java#L1-L31)
- [Password.java:1-90](file://iam-common/src/main/java/iam/platform/common/model/valueobject/Password.java#L1-L90)

### 通用工具类
- Guard：提供 notNull、notBlank、state、positive 等断言方法，统一前置条件与状态检查，减少重复校验逻辑。

```mermaid
flowchart TD
Start(["调用 Guard 方法"]) --> CheckNull["检查空值/空白"]
CheckNull --> NullBranch{"是否非法?"}
NullBranch --> |是| ThrowIllegal["抛出非法参数异常"]
NullBranch --> |否| CheckState["检查状态表达式"]
CheckState --> StateBranch{"是否成立?"}
StateBranch --> |否| ThrowInvalidState["抛出无效状态异常"]
StateBranch --> |是| CheckPositive["检查数值正数"]
CheckPositive --> PositiveBranch{"是否大于0?"}
PositiveBranch --> |否| ThrowIllegal
PositiveBranch --> |是| End(["通过"])
```

**图表来源**
- [Guard.java:1-37](file://iam-common/src/main/java/iam/platform/common/util/Guard.java#L1-L37)

**章节来源**
- [Guard.java:1-37](file://iam-common/src/main/java/iam/platform/common/util/Guard.java#L1-L37)

## 租户上下文管理系统

### TenantContext 类设计
TenantContext 是一个线程安全的租户上下文管理器，使用 ThreadLocal 存储当前用户的租户信息，支持多租户场景下的身份识别与权限控制。

**核心特性**：
- 线程安全：使用 ThreadLocal 确保每个线程独立的上下文状态
- 标准化：定义标准 HTTP 头部常量用于租户信息传递
- 容错性：在解析头部值时进行容错处理，避免请求失败
- 清理机制：提供 clear 方法确保线程结束时释放内存

```mermaid
classDiagram
class TenantContext {
+String HEADER_USER_ID
+String HEADER_USER_NAME
+String HEADER_TENANT_ID
+String HEADER_TENANT_ACCOUNT_ID
+String HEADER_USER_ROLES
+String HEADER_USER_PERMISSIONS
+getCurrentUserId() Long
+setCurrentUserId(userId) void
+getCurrentUserName() String
+setCurrentUserName(userName) void
+getCurrentTenantId() Long
+setCurrentTenantId(tenantId) void
+getCurrentTenantAccountId() Long
+setCurrentTenantAccountId(tenantAccountId) void
+populateFromHeaders(request) void
+clear() void
}
class ThreadLocal {
<<static>>
+CURRENT_USER_ID
+CURRENT_USER_NAME
+CURRENT_TENANT_ID
+CURRENT_TENANT_ACCOUNT_ID
}
TenantContext --> ThreadLocal : uses
```

**图表来源**
- [TenantContext.java:14-113](file://iam-common/src/main/java/iam/platform/common/context/TenantContext.java#L14-L113)

### GatewayHeaderContextFilter 过滤器
GatewayHeaderContextFilter 是一个通用的 Servlet 过滤器，负责从标准 HTTP 头部提取租户上下文并填充到 TenantContext 中。

**核心功能**：
- 自动填充：检测网关设置的标准头部并自动填充租户上下文
- 异步支持：继承 OncePerRequestFilter，确保每个请求只执行一次
- 安全清理：在 finally 块中总是清理 ThreadLocal，防止内存泄漏
- 容错处理：即使填充失败也不会影响请求的正常处理

```mermaid
sequenceDiagram
participant Client
participant Gateway
participant Filter
participant TenantContext
participant Business
Client->>Gateway : HTTP Request
Gateway->>Filter : 设置标准头部
Filter->>TenantContext : populateFromHeaders()
TenantContext->>TenantContext : 设置ThreadLocal
Filter->>Business : 执行业务逻辑
Business-->>Filter : 返回响应
Filter->>TenantContext : clear()
Filter-->>Client : HTTP Response
```

**图表来源**
- [GatewayHeaderContextFilter.java:22-49](file://iam-common/src/main/java/iam/platform/common/context/GatewayHeaderContextFilter.java#L22-L49)
- [TenantContext.java:72-111](file://iam-common/src/main/java/iam/platform/common/context/TenantContext.java#L72-L111)

### 标准 HTTP 头部规范
租户上下文系统定义了标准的 HTTP 头部名称，用于在微服务架构中传递租户信息：

- `X-User-Id`: 当前用户的唯一标识
- `X-User-Name`: 当前用户的用户名
- `X-Tenant-Id`: 当前租户的唯一标识
- `X-Tenant-Account-Id`: 当前租户账户的唯一标识
- `X-User-Roles`: 用户的角色列表（JSON 数组格式）
- `X-User-Permissions`: 用户的权限列表（JSON 数组格式）

**章节来源**
- [TenantContext.java:16-22](file://iam-common/src/main/java/iam/platform/common/context/TenantContext.java#L16-L22)
- [TenantContext.java:72-104](file://iam-common/src/main/java/iam/platform/common/context/TenantContext.java#L72-L104)
- [GatewayHeaderContextFilter.java:24-48](file://iam-common/src/main/java/iam/platform/common/context/GatewayHeaderContextFilter.java#L24-L48)

## 认证与会话管理边界

### Session 与 JWT 的职责分离
根据最新的架构设计，Session 和 JWT 在微服务架构中具有明确的职责边界：

**Session 专用场景**：
- OAuth2 授权码流程的 SavedRequest 机制
- CAS 单点登出 (SLO) 的 Session 管理
- Spring Security 内部的安全上下文存储

**JWT 专用场景**：
- 微服务间身份传递
- 租户上下文传递
- API 鉴权
- 前端到后端的无状态认证

```mermaid
graph TB
subgraph "认证架构"
Browser["浏览器"]
BFF["BFF 服务"]
Gateway["API 网关"]
AuthServer["认证服务"]
AdminServer["管理服务"]
subgraph "Session 场景"
OAuth2["OAuth2 授权码流程"]
CAS["CAS 单点登出"]
SSO["Spring Security 内部存储"]
end
subgraph "JWT 场景"
Microservices["微服务间通信"]
API["API 鉴权"]
TenantCtx["租户上下文传递"]
end
Browser --> BFF
BFF --> Gateway
Gateway --> AuthServer
Gateway --> AdminServer
AuthServer -.-> OAuth2
AuthServer -.-> CAS
AuthServer -.-> SSO
AdminServer --> TenantCtx
Gateway --> API
Gateway --> TenantCtx
end
```

**图表来源**
- [TenantAwareAuthenticationFilter.java:35-47](file://iam-auth-server/src/main/java/iam/platform/auth/interfaces/web/filter/TenantAwareAuthenticationFilter.java#L35-L47)
- [JwtUserContextFilter.java:43-65](file://iam-admin-server/src/main/java/iam/platform/admin/infrastructure/security/JwtUserContextFilter.java#L43-L65)

### 过滤器职责简化
**更新** 移除了租户上下文恢复逻辑中的 Session 恢复功能，简化了过滤器职责：

- **TenantAwareAuthenticationFilter**：仅从网关请求头提取租户上下文
- **JwtUserContextFilter**：仅处理 JWT 用户上下文和权限解析
- **GatewayHeaderContextFilter**：专门负责租户上下文的标准化头部处理

这种职责分离确保了：
1. 每个过滤器专注于单一职责
2. 减少了代码复杂性和维护成本
3. 提高了系统的可测试性和可观察性
4. 明确了 Session 和 JWT 的使用边界

**章节来源**
- [TenantAwareAuthenticationFilter.java:35-47](file://iam-auth-server/src/main/java/iam/platform/auth/interfaces/web/filter/TenantAwareAuthenticationFilter.java#L35-L47)
- [JwtUserContextFilter.java:43-65](file://iam-admin-server/src/main/java/iam/platform/admin/infrastructure/security/JwtUserContextFilter.java#L43-L65)
- [GatewayHeaderContextFilter.java:30-40](file://iam-common/src/main/java/iam/platform/common/context/GatewayHeaderContextFilter.java#L30-L40)

## 依赖分析
- 外部依赖：Lombok（生成 POJO 辅助代码）、Jakarta Validation API（校验注解）、Jackson Annotations（JSON 序列化注解）、Servlet API（HttpServletRequest/HttpServletResponse）、Spring Web（OncePerRequestFilter）、Spring Context（EnvironmentAware）、SLF4J API（日志记录）。
- 内部依赖：各组件之间保持松耦合，无循环依赖风险；注解与值对象不依赖具体框架实现，便于跨服务复用。

**更新** 新增了对 Servlet API 和 Spring Web 的依赖，用于支持租户上下文过滤器的功能。

```mermaid
graph TB
POM["pom.xml 依赖声明"]
LOMBOK["Lombok"]
VALID["Jakarta Validation API"]
JACKSON["Jackson Annotations"]
SERVLET["Servlet API"]
SPRING_WEB["Spring Web"]
SPRING_CONTEXT["Spring Context"]
SLF4J["SLF4J API"]
POM --> LOMBOK
POM --> VALID
POM --> JACKSON
POM --> SERVLET
POM --> SPRING_WEB
POM --> SPRING_CONTEXT
POM --> SLF4J
```

**图表来源**
- [pom.xml:18-65](file://iam-common/pom.xml#L18-L65)

**章节来源**
- [pom.xml:18-65](file://iam-common/pom.xml#L18-L65)

## 性能考虑
- 响应序列化：ApiResponse 使用 Jackson 注解排除空字段，减少冗余数据传输。
- 分页计算：PageResponse 的总页数计算为 O(1)，避免额外查询开销。
- 校验注解：在 DTO 上使用注解进行参数校验，可在进入业务逻辑前快速失败，降低后续处理成本。
- 值对象策略：Password 仅保存哈希值，避免在通用模块引入加密实现细节，降低耦合与运行时开销。
- **租户上下文优化**：ThreadLocal 访问为 O(1) 时间复杂度，避免频繁的数据库查询；过滤器只在有网关头部时才执行，支持匿名请求的透明处理；Session 仅用于必要的 OAuth2 和 CAS 场景，减少了不必要的内存占用。

**更新** 新增了Session和JWT职责分离带来的性能优化考虑。

## 故障排查指南
- 参数校验失败：检查请求 DTO 的注解配置与客户端传参是否符合约束。
- 审计日志未记录：确认方法已标注 @AuditLog，且拦截器/切面已在运行环境中启用。
- 权限拒绝：核对 @RequirePermission 的权限表达式与当前用户权限集合。
- 密码匹配失败：确认使用 Password 的 matches 方法并传入正确的匹配函数。
- 异常未统一处理：在上层控制器或全局异常处理器中捕获 BusinessException 并映射到 ApiResponse。
- **租户上下文问题**：检查网关是否正确设置了标准头部；确认 GatewayHeaderContextFilter 是否注册；验证 TenantContext 的 ThreadLocal 是否正确清理；确认 Session 配置是否正确用于 OAuth2 和 CAS 场景。
- **认证边界问题**：确认浏览器到后端的请求是否通过网关；检查 Session 和 JWT 的使用场景是否符合架构设计；验证过滤器链的执行顺序。

**更新** 新增了认证边界和Session/JWT职责分离相关的故障排查指导。

**章节来源**
- [CreatePersonRequest.java:1-37](file://iam-common/src/main/java/iam/platform/common/dto/request/CreatePersonRequest.java#L1-L37)
- [AuditLog.java:1-46](file://iam-common/src/main/java/iam/platform/common/model/annotation/AuditLog.java#L1-L46)
- [RequirePermission.java:1-35](file://iam-common/src/main/java/iam/platform/common/model/annotation/RequirePermission.java#L1-L35)
- [Password.java:1-90](file://iam-common/src/main/java/iam/platform/common/model/valueobject/Password.java#L1-L90)
- [BusinessException.java:1-17](file://iam-common/src/main/java/iam/platform/common/model/exception/BusinessException.java#L1-L17)
- [TenantContext.java:72-111](file://iam-common/src/main/java/iam/platform/common/context/TenantContext.java#L72-L111)
- [GatewayHeaderContextFilter.java:24-48](file://iam-common/src/main/java/iam/platform/common/context/GatewayHeaderContextFilter.java#L24-L48)
- [TenantAwareAuthenticationFilter.java:35-47](file://iam-auth-server/src/main/java/iam/platform/auth/interfaces/web/filter/TenantAwareAuthenticationFilter.java#L35-L47)

## 结论
通用模块通过统一的响应格式、DTO、枚举、异常与注解，构建了平台级的共享基础设施，显著提升了跨服务的一致性与可维护性。其设计强调"零框架依赖、强契约约束、可国际化扩展"，在微服务架构中发挥着承上启下的关键作用。

**更新** 新增的租户上下文管理系统进一步增强了通用模块在多租户场景下的支撑能力，通过标准化的头部传递和线程安全的上下文管理，为整个平台的多租户功能提供了坚实的基础。明确的 Session 和 JWT 使用边界，以及简化的过滤器职责，使得系统架构更加清晰，维护成本更低，安全性更强。

## 附录

### API 响应与分页使用示例（路径指引）
- 成功响应：参考 [ApiResponse.success(...):25-32](file://iam-common/src/main/java/iam/platform/common/api/ApiResponse.java#L25-L32)
- 创建响应：参考 [ApiResponse.created(...):34-41](file://iam-common/src/main/java/iam/platform/common/api/ApiResponse.java#L34-L41)
- 错误响应：参考 [ApiResponse.error(...):43-50](file://iam-common/src/main/java/iam/platform/common/api/ApiResponse.java#L43-L50)
- 分页构造：参考 [PageResponse.of(...):21-30](file://iam-common/src/main/java/iam/platform/common/api/PageResponse.java#L21-L30)

### DTO 使用示例（路径指引）
- 请求 DTO 校验：参考 [CreatePersonRequest:1-37](file://iam-common/src/main/java/iam/platform/common/dto/request/CreatePersonRequest.java#L1-L37)
- 响应 DTO 输出：参考 [PersonResponse:1-30](file://iam-common/src/main/java/iam/platform/common/dto/response/PersonResponse.java#L1-L30)

### 异常与国际化建议（路径指引）
- 抽象异常基类：参考 [BusinessException:1-17](file://iam-common/src/main/java/iam/platform/common/model/exception/BusinessException.java#L1-L17)
- 国际化集成：在上层控制器或全局异常处理器中根据 errorCode 与 Locale 解析消息模板。

### 注解与安全（路径指引）
- 审计注解：参考 [@AuditLog:1-46](file://iam-common/src/main/java/iam/platform/common/model/annotation/AuditLog.java#L1-L46)
- 权限注解：参考 [@RequirePermission:1-35](file://iam-common/src/main/java/iam/platform/common/model/annotation/RequirePermission.java#L1-L35)

### 值对象与工具（路径指引）
- 邮箱值对象：参考 [Email:1-31](file://iam-common/src/main/java/iam/platform/common/model/valueobject/Email.java#L1-L31)
- 密码值对象：参考 [Password:1-90](file://iam-common/src/main/java/iam/platform/common/model/valueobject/Password.java#L1-L90)
- 断言工具：参考 [Guard:1-37](file://iam-common/src/main/java/iam/platform/common/util/Guard.java#L1-L37)

### 租户上下文系统使用示例（新增）
- **租户上下文获取**：参考 [TenantContext.getCurrentUserId():31-37](file://iam-common/src/main/java/iam/platform/common/context/TenantContext.java#L31-L37)
- **租户上下文设置**：参考 [TenantContext.setCurrentTenantId():51-53](file://iam-common/src/main/java/iam/platform/common/context/TenantContext.java#L51-L53)
- **从头部填充**：参考 [TenantContext.populateFromHeaders():72-104](file://iam-common/src/main/java/iam/platform/common/context/TenantContext.java#L72-L104)
- **过滤器注册**：参考 [GatewayHeaderContextFilter.doFilterInternal():24-48](file://iam-common/src/main/java/iam/platform/common/context/GatewayHeaderContextFilter.java#L24-L48)

### 认证边界使用示例（新增）
- **Session 配置**：参考 [application.yml:49-52](file://iam-auth-server/src/main/resources/application.yml#L49-L52)
- **JWT 过滤器**：参考 [JwtUserContextFilter.doFilterInternal():43-65](file://iam-admin-server/src/main/java/iam/platform/admin/infrastructure/security/JwtUserContextFilter.java#L43-L65)
- **简化过滤器**：参考 [TenantAwareAuthenticationFilter.doFilterInternal():35-47](file://iam-auth-server/src/main/java/iam/platform/auth/interfaces/web/filter/TenantAwareAuthenticationFilter.java#L35-L47)