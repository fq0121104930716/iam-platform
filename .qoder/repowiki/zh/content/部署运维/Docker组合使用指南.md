# Docker组合使用指南

<cite>
**本文档引用的文件**
- [docker-compose.yml](file://docker-compose.yml)
- [docker-compose.app.yml](file://docker-compose.app.yml)
- [docker-compose.middleware.yml](file://docker-compose.middleware.yml)
- [docker-compose.ui.yml](file://docker-compose.ui.yml)
- [DOCKER-COMPOSE-USAGE.md](file://DOCKER-COMPOSE-USAGE.md)
- [.dockerignore](file://.dockerignore)
- [iam-bff-server/Dockerfile](file://iam-bff-server/Dockerfile)
- [iam-auth-server/Dockerfile](file://iam-auth-server/Dockerfile)
- [iam-admin-server/Dockerfile](file://iam-admin-server/Dockerfile)
- [iam-audit-server/Dockerfile](file://iam-audit-server/Dockerfile)
- [iam-gateway/Dockerfile](file://iam-gateway/Dockerfile)
- [iam-admin-ui/Dockerfile](file://iam-admin-ui/Dockerfile)
- [iam-admin-ui/nginx.conf](file://iam-admin-ui/nginx.conf)
- [iam-bff-server/bootstrap.yml](file://iam-bff-server/bootstrap.yml)
- [iam-auth-server/bootstrap.yml](file://iam-auth-server/bootstrap.yml)
- [iam-admin-server/bootstrap.yml](file://iam-admin-server/bootstrap.yml)
- [iam-gateway/bootstrap.yml](file://iam-gateway/bootstrap.yml)
</cite>

## 更新摘要
**所做更改**
- 新增独立UI服务配置章节，详细介绍IAM Admin UI的部署和配置
- 更新了配置文件结构说明，反映统一入口文件和UI独立部署的双重架构
- 优化了中间件服务配置，标准化重启策略为"unless-stopped"
- 更新了架构概览图，展示新的三层部署架构
- 新增UI服务独立部署的使用场景和最佳实践
- 更新了故障排除指南，增加UI服务相关的问题诊断

## 目录
1. [简介](#简介)
2. [项目结构](#项目结构)
3. [核心组件](#核心组件)
4. [架构概览](#架构概览)
5. [详细组件分析](#详细组件分析)
6. [UI服务独立部署](#ui服务独立部署)
7. [依赖关系分析](#依赖关系分析)
8. [性能考虑](#性能考虑)
9. [故障排除指南](#故障排除指南)
10. [结论](#结论)

## 简介

本指南详细介绍了IAM平台项目的Docker组合使用方法。该项目采用微服务架构，通过Docker Compose将中间件服务、业务服务和UI服务进行分层部署，提供了完整的开发、测试和生产环境部署方案。

**更新** 项目现已采用三层架构的Docker Compose部署模式，通过`docker-compose.yml`作为统一入口协调中间件和应用服务的部署，同时新增`docker-compose.ui.yml`专门用于独立部署前端管理界面，为不同使用场景提供灵活的部署选项。

IAM平台包含七个核心服务：网关服务、认证授权服务、管理服务、审计服务、BFF服务、前端管理界面和一套完整的中间件基础设施，包括服务注册与配置中心、数据库、缓存、监控和消息队列系统。

## 项目结构

项目采用三层分层的Docker部署策略，通过统一入口文件协调中间件和应用服务的部署，同时支持UI服务的独立部署：

```mermaid
graph TB
subgraph "统一Docker Compose入口"
A[docker-compose.yml] --> B[基础中间件层]
A --> C[业务服务层]
end
subgraph "UI独立部署"
D[docker-compose.ui.yml] --> E[IAM Admin UI]
end
subgraph "基础中间件层"
F[PostgreSQL数据库]
G[Redis缓存]
H[Prometheus监控]
I[RocketMQ消息队列]
J[Nacos服务注册]
K[Zipkin追踪]
end
subgraph "业务服务层"
L[IAM Auth Server]
M[IAM Admin Server]
N[IAM Audit Server]
O[IAM BFF Server]
P[IAM Gateway]
end
B --> F
B --> G
B --> H
B --> I
B --> J
B --> K
C --> L
C --> M
C --> N
C --> O
C --> P
E --> F
E --> G
E --> H
E --> I
E --> J
E --> K
```

**图表来源**
- [docker-compose.yml:10-83](file://docker-compose.yml#L10-L83)
- [docker-compose.ui.yml:8-18](file://docker-compose.ui.yml#L8-L18)
- [docker-compose.middleware.yml:17-163](file://docker-compose.middleware.yml#L17-L163)
- [docker-compose.app.yml:1-121](file://docker-compose.app.yml#L1-L121)

**章节来源**
- [docker-compose.yml:1-83](file://docker-compose.yml#L1-L83)
- [docker-compose.ui.yml:1-18](file://docker-compose.ui.yml#L1-L18)
- [docker-compose.middleware.yml:1-163](file://docker-compose.middleware.yml#L1-L163)
- [docker-compose.app.yml:1-121](file://docker-compose.app.yml#L1-L121)

## 核心组件

### 中间件服务组件

中间件服务提供整个系统的基础设施支持，包括：

| 服务类型 | 服务名称 | 端口映射 | 主要功能 | 重启策略 |
|---------|----------|----------|----------|----------|
| 数据库 | PostgreSQL | 5432 | 关系型数据存储 | unless-stopped |
| 缓存 | Redis | 6379 | 内存缓存、会话存储 | unless-stopped |
| 监控 | Prometheus | 9090 | 指标收集与监控 | unless-stopped |
| 消息队列 | RocketMQ | 9876, 10909, 10911, 10912 | 异步消息处理 | unless-stopped |
| 服务注册与配置中心 | Nacos | 8848, 9848 | 服务发现、配置管理 | unless-stopped |
| 追踪 | Zipkin | 9410, 9411 | 分布式链路追踪 | unless-stopped |

### 业务服务组件

业务服务提供核心功能实现：

| 服务名称 | 容器端口 | 主要职责 | 重启策略 |
|----------|----------|----------|----------|
| IAM Auth Server | 9001, 9005 | 认证授权、用户管理 | unless-stopped |
| IAM Admin Server | 9002, 9006 | 管理控制台、权限管理 | unless-stopped |
| IAM Audit Server | 9003, 9004 | 审计日志、合规管理 | unless-stopped |
| IAM BFF Server | 9010, 9011 | 前端代理、服务聚合 | unless-stopped |
| IAM Gateway | 9000, 9080 | API网关、请求路由 | unless-stopped |

### UI服务组件

前端管理界面提供用户友好的管理控制台：

| 服务名称 | 容器端口 | 主要职责 | 重启策略 |
|----------|----------|----------|----------|
| IAM Admin UI | 3000:80 | 管理控制台前端界面 | no |

**章节来源**
- [docker-compose.middleware.yml:22-163](file://docker-compose.middleware.yml#L22-L163)
- [docker-compose.app.yml:6-121](file://docker-compose.app.yml#L6-L121)
- [docker-compose.ui.yml:8-18](file://docker-compose.ui.yml#L8-L18)

## 架构概览

系统采用三层微服务架构，通过Docker容器化部署，实现了服务间的松耦合和高可用性。新的三层架构提供了更灵活的部署体验：

```mermaid
graph TB
subgraph "客户端层"
A[浏览器]
B[移动应用]
C[第三方集成]
end
subgraph "网关层"
D[IAM Gateway]
E[IAM BFF Server]
end
subgraph "服务层"
F[IAM Auth Server]
G[IAM Admin Server]
H[IAM Audit Server]
end
subgraph "UI层"
I[IAM Admin UI]
end
subgraph "中间件层"
J[PostgreSQL]
K[Redis]
L[RocketMQ]
M[Nacos]
N[Prometheus]
O[Zipkin]
end
A --> D
B --> D
C --> D
D --> E
E --> F
E --> G
E --> H
F --> J
F --> K
F --> L
F --> M
G --> J
G --> K
G --> L
G --> M
H --> J
H --> K
H --> L
H --> M
I --> D
I --> E
I --> F
I --> G
I --> H
J --> N
K --> N
L --> N
M --> N
O --> N
```

**图表来源**
- [docker-compose.yml:58-83](file://docker-compose.yml#L58-L83)
- [docker-compose.ui.yml:8-18](file://docker-compose.ui.yml#L8-L18)
- [docker-compose.middleware.yml:22-163](file://docker-compose.middleware.yml#L22-L163)

## 详细组件分析

### Docker Compose配置分析

#### 统一入口文件配置

新的`docker-compose.yml`文件通过`extends`机制整合了中间件和应用服务配置：

```mermaid
flowchart TD
A[启动统一入口] --> B[加载中间件配置]
B --> C[启动基础中间件]
C --> D[启动消息队列]
D --> E[启动服务治理]
E --> F[加载应用配置]
F --> G[启动业务服务]
G --> H[启动聚合层]
H --> I[服务全部就绪]
```

**图表来源**
- [docker-compose.yml:14-47](file://docker-compose.yml#L14-L47)
- [docker-compose.yml:58-83](file://docker-compose.yml#L58-L83)

#### 中间件服务配置

中间件服务配置保持了原有的独立特性，确保基础设施的稳定性，并标准化了重启策略：

```mermaid
sequenceDiagram
participant Middleware as 中间件层
participant Postgres as PostgreSQL
participant Redis as Redis
participant Nacos as Nacos
Middleware->>Postgres : 启动数据库
Postgres-->>Middleware : 健康检查通过
Middleware->>Redis : 启动缓存
Redis-->>Middleware : 健康检查通过
Middleware->>Nacos : 启动服务注册
Nacos-->>Middleware : 健康检查通过
```

**图表来源**
- [docker-compose.middleware.yml:36-41](file://docker-compose.middleware.yml#L36-L41)
- [docker-compose.middleware.yml:52-57](file://docker-compose.middleware.yml#L52-L57)
- [docker-compose.middleware.yml:144-148](file://docker-compose.middleware.yml#L144-L148)

#### 业务服务配置

业务服务配置展示了服务间的依赖关系和通信模式：

```mermaid
sequenceDiagram
participant Client as 客户端
participant Gateway as IAM Gateway
participant BFF as IAM BFF Server
participant Auth as IAM Auth Server
participant Admin as IAM Admin Server
Client->>Gateway : 请求访问
Gateway->>BFF : 转发请求
BFF->>Auth : 用户认证
Auth-->>BFF : 认证结果
BFF->>Admin : 权限验证
Admin-->>BFF : 权限结果
BFF-->>Gateway : 处理结果
Gateway-->>Client : 返回响应
```

**图表来源**
- [docker-compose.app.yml:82-98](file://docker-compose.app.yml#L82-L98)
- [docker-compose.app.yml:100-118](file://docker-compose.app.yml#L100-L118)

**章节来源**
- [docker-compose.yml:1-83](file://docker-compose.yml#L1-L83)
- [docker-compose.middleware.yml:1-163](file://docker-compose.middleware.yml#L1-L163)
- [docker-compose.app.yml:1-121](file://docker-compose.app.yml#L1-L121)

### Dockerfile构建配置

所有服务都使用统一的基础镜像配置，确保构建的一致性和可维护性：

```mermaid
classDiagram
class BaseDockerfile {
+FROM eclipse-temurin : 21-jre-alpine
+WORKDIR /app
+COPY target/*.jar app.jar
+EXPOSE port
+ENTRYPOINT ["java", "-jar", "app.jar"]
}
class BFF_Dockerfile {
+EXPOSE 9010 9011
+依赖 : BaseDockerfile
}
class Auth_Dockerfile {
+EXPOSE 9001 9005
+依赖 : BaseDockerfile
}
class Admin_Dockerfile {
+EXPOSE 9002 9006
+依赖 : BaseDockerfile
}
class Audit_Dockerfile {
+EXPOSE 9003 9004
+依赖 : BaseDockerfile
}
class Gateway_Dockerfile {
+EXPOSE 9000 9080
+依赖 : BaseDockerfile
}
class AdminUIDockerfile {
+FROM node : 20-alpine
+COPY iam-admin-ui/package.json
+RUN npm install
+COPY iam-admin-ui/
+RUN npm run build
+依赖 : Nginx
}
BaseDockerfile <|-- BFF_Dockerfile
BaseDockerfile <|-- Auth_Dockerfile
BaseDockerfile <|-- Admin_Dockerfile
BaseDockerfile <|-- Audit_Dockerfile
BaseDockerfile <|-- Gateway_Dockerfile
BaseDockerfile <|-- AdminUIDockerfile
```

**图表来源**
- [iam-bff-server/Dockerfile:1-10](file://iam-bff-server/Dockerfile#L1-L10)
- [iam-auth-server/Dockerfile:1-10](file://iam-auth-server/Dockerfile#L1-L10)
- [iam-admin-server/Dockerfile:1-10](file://iam-admin-server/Dockerfile#L1-L10)
- [iam-audit-server/Dockerfile:1-10](file://iam-audit-server/Dockerfile#L1-L10)
- [iam-gateway/Dockerfile:1-10](file://iam-gateway/Dockerfile#L1-L10)
- [iam-admin-ui/Dockerfile:1-15](file://iam-admin-ui/Dockerfile#L1-L15)

**章节来源**
- [iam-bff-server/Dockerfile:1-10](file://iam-bff-server/Dockerfile#L1-L10)
- [iam-auth-server/Dockerfile:1-10](file://iam-auth-server/Dockerfile#L1-L10)
- [iam-admin-server/Dockerfile:1-10](file://iam-admin-server/Dockerfile#L1-L10)
- [iam-audit-server/Dockerfile:1-10](file://iam-audit-server/Dockerfile#L1-L10)
- [iam-gateway/Dockerfile:1-10](file://iam-gateway/Dockerfile#L1-L10)
- [iam-admin-ui/Dockerfile:1-15](file://iam-admin-ui/Dockerfile#L1-L15)

### 配置文件分析

Spring Cloud Nacos配置展示了服务发现和配置管理的实现：

```mermaid
flowchart LR
A[bootstrap.yml] --> B[Nacos配置]
B --> C[服务发现]
B --> D[配置中心]
B --> E[命名空间]
B --> F[元数据管理]
C --> G[服务注册]
C --> H[健康检查]
D --> I[动态配置]
D --> J[配置热更新]
```

**图表来源**
- [iam-bff-server/bootstrap.yml:1-10](file://iam-bff-server/bootstrap.yml#L1-L10)
- [iam-auth-server/bootstrap.yml:1-10](file://iam-auth-server/bootstrap.yml#L1-L10)
- [iam-admin-server/bootstrap.yml:1-10](file://iam-admin-server/bootstrap.yml#L1-L10)
- [iam-gateway/bootstrap.yml:1-10](file://iam-gateway/bootstrap.yml#L1-L10)

**章节来源**
- [iam-bff-server/bootstrap.yml:1-10](file://iam-bff-server/bootstrap.yml#L1-L10)
- [iam-auth-server/bootstrap.yml:1-10](file://iam-auth-server/bootstrap.yml#L1-L10)
- [iam-admin-server/bootstrap.yml:1-10](file://iam-admin-server/bootstrap.yml#L1-L10)
- [iam-gateway/bootstrap.yml:1-10](file://iam-gateway/bootstrap.yml#L1-L10)

## UI服务独立部署

### IAM Admin UI配置

IAM Admin UI是一个独立的前端管理界面，通过Vite构建并使用Nginx提供静态资源服务：

```mermaid
flowchart TD
A[IAM Admin UI] --> B[Vite构建]
B --> C[Vue 3应用]
C --> D[Element Plus UI]
D --> E[Pinia状态管理]
B --> F[Nginx服务]
F --> G[静态资源]
F --> H[API代理]
H --> I[网关服务]
```

**图表来源**
- [docker-compose.ui.yml:8-18](file://docker-compose.ui.yml#L8-L18)
- [iam-admin-ui/Dockerfile:1-15](file://iam-admin-ui/Dockerfile#L1-L15)
- [iam-admin-ui/nginx.conf:13-29](file://iam-admin-ui/nginx.conf#L13-L29)

### UI服务特点

- **独立部署**：可单独启动和管理，不依赖其他服务
- **开发友好**：支持热重载和开发服务器
- **生产优化**：使用Nginx提供高性能静态资源服务
- **API代理**：内置API请求转发功能
- **端口映射**：默认暴露3000端口供开发使用

### UI服务启动方式

```bash
# 启动UI服务
docker-compose -f docker-compose.ui.yml up -d

# 停止UI服务
docker-compose -f docker-compose.ui.yml down

# 查看UI服务日志
docker-compose -f docker-compose.ui.yml logs -f

# 重启UI服务
docker-compose -f docker-compose.ui.yml restart iam-admin-ui
```

**章节来源**
- [docker-compose.ui.yml:1-18](file://docker-compose.ui.yml#L1-L18)
- [iam-admin-ui/Dockerfile:1-15](file://iam-admin-ui/Dockerfile#L1-L15)
- [iam-admin-ui/nginx.conf:1-37](file://iam-admin-ui/nginx.conf#L1-L37)

## 依赖关系分析

### 服务依赖图

```mermaid
graph TB
subgraph "外部依赖"
A[PostgreSQL数据库]
B[Redis缓存]
C[RocketMQ消息队列]
D[Nacos服务注册]
E[Prometheus监控]
F[Zipkin追踪]
end
subgraph "内部服务"
G[IAM Auth Server]
H[IAM Admin Server]
I[IAM Audit Server]
J[IAM BFF Server]
K[IAM Gateway]
L[IAM Admin UI]
end
A --> G
A --> H
A --> I
B --> G
B --> H
B --> I
B --> J
B --> K
C --> G
C --> H
C --> I
D --> G
D --> H
D --> I
D --> J
D --> K
E --> G
E --> H
E --> I
E --> J
E --> K
F --> G
F --> H
F --> I
G --> J
G --> K
H --> J
H --> K
I --> J
I --> K
J --> K
L --> K
L --> J
L --> G
L --> H
L --> I
```

**图表来源**
- [docker-compose.app.yml:6-121](file://docker-compose.app.yml#L6-L121)
- [docker-compose.middleware.yml:22-163](file://docker-compose.middleware.yml#L22-L163)
- [docker-compose.ui.yml:8-18](file://docker-compose.ui.yml#L8-L18)

### 网络拓扑

所有服务都运行在同一个Docker网络中，实现了服务间的直接通信：

```mermaid
graph LR
subgraph "iam-network桥接网络"
A[PostgreSQL]
B[Redis]
C[RocketMQ NameServer]
D[Nacos]
E[IAM Auth Server]
F[IAM Admin Server]
G[IAM Audit Server]
H[IAM BFF Server]
I[IAM Gateway]
J[IAM Admin UI]
end
A -.-> E
A -.-> F
A -.-> G
B -.-> E
B -.-> F
B -.-> G
B -.-> H
B -.-> I
B -.-> J
C -.-> E
C -.-> F
C -.-> G
D -.-> E
D -.-> F
D -.-> G
D -.-> H
D -.-> I
D -.-> J
H -.-> I
J -.-> I
J -.-> H
J -.-> E
J -.-> F
J -.-> G
J -.-> A
J -.-> B
```

**图表来源**
- [docker-compose.middleware.yml:144-163](file://docker-compose.middleware.yml#L144-L163)
- [docker-compose.app.yml:100-121](file://docker-compose.app.yml#L100-L121)
- [docker-compose.ui.yml:8-18](file://docker-compose.ui.yml#L8-L18)

**章节来源**
- [docker-compose.middleware.yml:144-163](file://docker-compose.middleware.yml#L144-L163)
- [docker-compose.app.yml:100-121](file://docker-compose.app.yml#L100-L121)
- [docker-compose.ui.yml:8-18](file://docker-compose.ui.yml#L8-L18)

## 性能考虑

### 资源配置优化

中间件服务采用了合理的资源配置策略：

- **PostgreSQL**: 使用专用数据目录，确保数据持久化
- **Redis**: 配置了密码认证和持久化存储
- **Prometheus**: 独立的配置文件和数据目录
- **RocketMQ**: 分离的NameServer和Broker组件
- **Nacos**: 单机模式配置，适合开发环境使用

### 启动顺序优化

三层架构定义了明确的服务启动依赖关系：

1. **基础中间件优先启动**: PostgreSQL、Redis等核心基础设施
2. **消息队列服务启动**: RocketMQ集群组件
3. **服务治理组件启动**: Nacos、Zipkin等治理工具
4. **业务服务按需启动**: 根据依赖关系启动
5. **UI服务最后启动**: 确保上游服务完全就绪

### 重启策略标准化

所有中间件服务现在采用统一的重启策略"unless-stopped"，确保服务在容器重启时能够自动恢复：

- **PostgreSQL**: `restart: unless-stopped`
- **Redis**: `restart: unless-stopped`
- **Prometheus**: `restart: unless-stopped`
- **RocketMQ**: `restart: unless-stopped`
- **Nacos**: `restart: unless-stopped`
- **Zipkin**: `restart: unless-stopped`

**章节来源**
- [docker-compose.middleware.yml:36-163](file://docker-compose.middleware.yml#L36-L163)

## 故障排除指南

### 常见问题诊断

```mermaid
flowchart TD
A[服务启动失败] --> B{检查统一入口}
B --> C{中间件就绪?}
C --> |否| D[检查中间件配置]
C --> |是| E{应用服务依赖?}
E --> |失败| F[检查服务间通信]
E --> |成功| G{UI服务问题?}
G --> |失败| H[检查UI配置]
G --> |成功| I{构建问题?}
I --> |失败| J[检查JAR文件构建]
I --> |成功| K[检查环境变量]
H --> L[检查UI端口映射]
J --> M[重新构建应用]
F --> N[检查网络配置]
K --> O[检查配置文件]
L --> P[确认UI服务依赖]
M --> Q[确认服务依赖]
N --> Q
O --> Q
P --> Q
```

### 日志查看方法

```bash
# 查看统一入口的所有服务日志
docker-compose logs -f

# 查看中间件服务日志
docker-compose -f docker-compose.middleware.yml logs -f

# 查看应用服务日志
docker-compose -f docker-compose.app.yml logs -f

# 查看UI服务日志
docker-compose -f docker-compose.ui.yml logs -f

# 查看特定服务日志
docker-compose logs -f iam-gateway
docker-compose logs -f iam-auth-server
docker-compose logs -f iam-admin-ui
docker-compose logs -f nacos
```

### 重启策略

```bash
# 重启统一入口中的特定服务
docker-compose restart iam-gateway

# 重启所有应用服务
docker-compose -f docker-compose.app.yml restart

# 重启所有中间件服务
docker-compose -f docker-compose.middleware.yml restart

# 重启UI服务
docker-compose -f docker-compose.ui.yml restart iam-admin-ui
```

### UI服务特殊问题

- **端口冲突**: UI服务默认使用3000端口，如被占用可在docker-compose.ui.yml中修改
- **API代理问题**: 检查nginx.conf中的代理配置是否指向正确的网关地址
- **静态资源加载**: 确认Nginx配置中的静态资源路径正确
- **开发模式**: UI服务支持热重载，开发时可直接访问http://localhost:3000

**章节来源**
- [DOCKER-COMPOSE-USAGE.md:136-165](file://DOCKER-COMPOSE-USAGE.md#L136-L165)
- [docker-compose.yml:1-83](file://docker-compose.yml#L1-L83)
- [docker-compose.ui.yml:1-18](file://docker-compose.ui.yml#L1-L18)

## 结论

本Docker组合配置为IAM平台提供了完整的三层容器化部署解决方案。通过引入统一的`docker-compose.yml`入口文件和独立的`docker-compose.ui.yml`配置，项目实现了部署方式的重大升级：

- **三层架构设计**: 通过`extends`机制整合中间件、应用和UI配置
- **灵活部署选项**: 支持一键启动完整环境、独立部署中间件/应用/UI
- **向后兼容**: 保持原有独立配置文件的兼容性
- **标准化重启策略**: 统一使用"unless-stopped"确保服务稳定性
- **清晰的分层结构**: 明确的基础设施、服务和UI层划分
- **简化的依赖管理**: 通过统一入口文件管理复杂的依赖关系
- **UI独立部署**: 为前端开发和生产部署提供专门的解决方案

该配置适用于开发、测试和生产环境，为IAM平台的持续集成和部署提供了更加灵活和高效的解决方案。三层架构不仅简化了部署流程，还保持了系统的可维护性和扩展性，为未来的架构演进奠定了良好的基础。

**更新** 新增的UI服务独立部署功能特别适合前端开发团队，可以在不启动完整后端服务的情况下快速迭代前端界面，提高了开发效率和部署灵活性。