# Docker Compose 使用指南

## 文件说明

本项目采用分层 Docker Compose 配置架构，将中间件和应用服务分离管理：

### 📁 配置文件结构

- **docker-compose.yml** - 主入口文件（同时启动中间件+应用）
- **docker-compose.middleware.yml** - 中间件配置（独立部署）
- **docker-compose.app.yml** - 应用服务配置（依赖中间件）

### 🏗️ 架构分层

**第1层 - 基础中间件**（无依赖）
- PostgreSQL (数据库)
- Redis (缓存与会话存储)
- Prometheus (指标监控，可选)

**第2层 - 消息队列**
- RocketMQ NameServer (消息路由)
- RocketMQ Broker (消息存储)
- RocketMQ Dashboard (管理界面，可选)

**第3层 - 服务治理**
- Nacos (服务注册与配置中心)
- Zipkin (分布式追踪，可选)

**第4层 - 业务服务**（依赖中间件健康）
- IAM Auth Server (端口 9001, 9005)
- IAM Admin Server (端口 9002, 9006)
- IAM Audit Server (端口 9003, 9004)

**第5层 - 聚合层**（依赖业务服务）
- IAM BFF Server (端口 9010)
- IAM Gateway (端口 9000)

## 使用方式

### 场景 1: 启动完整环境（中间件 + 应用）

```bash
# 构建业务服务
mvn clean package -DskipTests

# 启动所有服务（默认使用 docker-compose.yml，--build 强制重新构建镜像）
docker-compose up -d --build
```

### 场景 2: 仅启动中间件（本地开发调试）

如果你只需要中间件进行本地开发调试：

```bash
# 启动完整中间件栈
docker-compose -f docker-compose.middleware.yml up -d

# 或仅启动核心中间件
docker-compose -f docker-compose.middleware.yml up -d postgres redis nacos
```

### 场景 3: 仅启动应用服务

当中间件已经在运行时，单独启动应用服务：

```bash
# 确保中间件已启动
docker-compose -f docker-compose.middleware.yml ps

# 启动应用服务（--build 强制重新构建镜像）
docker-compose -f docker-compose.app.yml up -d --build
```

### 1. 构建业务服务

在启动应用服务之前，需要先构建各个模块：

```bash
# 在项目根目录执行
mvn clean package -DskipTests
```

### 2. 启动所有服务

```bash
# 方式1: 使用主入口文件（推荐，--build 强制重新构建镜像）
docker-compose up -d --build

# 方式2: 分别启动
# 先启动中间件
docker-compose -f docker-compose.middleware.yml up -d

# 等待中间件就绪后，启动应用（--build 强制重新构建镜像）
docker-compose -f docker-compose.app.yml up -d --build
```

服务将按照依赖顺序自动启动：
1. 基础中间件 (PostgreSQL, Redis)
2. 消息队列 (RocketMQ)
3. 服务治理 (Nacos, Zipkin)
4. 业务服务 (Auth, Admin, Audit)
5. 聚合层 (BFF, Gateway)

### 3. 查看服务状态

```bash
# 查看所有服务状态
docker-compose ps

# 查看中间件状态
docker-compose -f docker-compose.middleware.yml ps

# 查看应用状态
docker-compose -f docker-compose.app.yml ps
```

检查关键服务的健康状态：
- PostgreSQL: `healthy`
- Redis: `healthy`
- Nacos: `healthy`
- Zipkin: `healthy`

### 4. 停止所有服务

```bash
# 停止所有服务（中间件+应用）
docker-compose down

# 仅停止应用服务（保留中间件）
docker-compose -f docker-compose.app.yml down

# 仅停止中间件（会同时停止依赖的应用）
docker-compose -f docker-compose.middleware.yml down
```

### 5. 查看日志

```bash
# 查看所有服务日志
docker-compose logs -f

# 查看中间件日志
docker-compose -f docker-compose.middleware.yml logs -f

# 查看应用日志
docker-compose -f docker-compose.app.yml logs -f

# 查看特定服务日志
docker-compose logs -f iam-gateway
docker-compose logs -f iam-auth-server
docker-compose logs -f nacos
```

### 6. 重启服务

```bash
# 重启单个服务
docker-compose restart iam-gateway

# 重启所有应用服务
docker-compose -f docker-compose.app.yml restart

# 重启所有中间件
docker-compose -f docker-compose.middleware.yml restart
```

### 7. 服务依赖关系

```
中间件层 (middleware)
    ↓
应用服务层 (app)
    ↓
完整环境 (docker-compose.yml)
```

**推荐开发流程：**
1. 首次启动：`docker-compose up -d` （一键启动全部）
2. 日常开发：先启动中间件，然后在 IDE 中运行应用
3. 测试部署：分别启动中间件和应用，便于独立管理

## 服务端口映射

### 中间件服务

| 服务 | 端口 | 说明 |
|------|------|------|
| Nacos | 8848, 9848 | 服务注册与配置中心 |
| PostgreSQL | 5432 | 数据库 |
| Redis | 6379 | 缓存 |
| Prometheus | 9090 | 监控 |
| Zipkin | 9410, 9411 | 分布式追踪 |
| RocketMQ NameServer | 9876 | 消息队列名称服务 |
| RocketMQ Broker | 10909, 10911, 10912 | 消息队列代理 |
| RocketMQ Dashboard | 8088 | 消息队列管理界面 |

### 业务服务

| 服务 | 端口 | 说明 |
|------|------|------|
| IAM Gateway | 9000 | API 网关 |
| IAM Auth Server | 9001, 9005 | 认证授权服务 |
| IAM Admin Server | 9002, 9006 | 管理服务 |
| IAM Audit Server | 9003, 9004 | 审计服务 |
| IAM BFF Server | 9010 | 前端代理服务 |

## 网络配置

所有服务都运行在 `iam-network` 桥接网络中,服务间可以通过服务名称互相访问。

**重要说明:**
- `docker-compose.middleware.yml` 创建 `iam-network` 网络
- `docker-compose.app.yml` 使用外部网络 `iam-network` (external: true)
- **必须先启动中间件**,才能启动应用服务

## 数据持久化

中间件数据持久化到宿主机的 `F:\linux\docker\` 目录下：

- PostgreSQL: `F:\linux\docker\postgres\data`
- Redis: `F:\linux\docker\redis\data`
- Prometheus: `F:\linux\docker\prometheus\data`
- RocketMQ: `F:\linux\docker\rocketmq\`

## 环境变量

各服务的环境变量已在对应的 compose 文件中配置，主要包括：

- `SPRING_PROFILES_ACTIVE`: Spring Profile (dev)
- `NACOS_ADDR`: Nacos 地址
- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`: 数据库配置
- `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD`: Redis 配置
- `ROCKETMQ_NAMESRV`: RocketMQ NameServer 地址

## 注意事项

1. **启动顺序**: 必须先启动中间件,再启动应用服务
2. **构建要求**: 启动应用服务前,请确保已执行 `mvn clean package` 构建 JAR 文件
3. **镜像更新**: 应用服务已配置 `pull_policy: build`,每次 `up` 时自动重新构建镜像,无需手动清理旧镜像
4. **HTTPS 配置**: 如需 HTTPS,请配置 SSL 证书(参考 ssl/README.md)
5. **数据持久化**: 中间件数据持久化路径可根据实际情况修改
6. **依赖管理**: `docker-compose.app.yml` 中的应用服务依赖中间件,但不会自动启动中间件
7. **开发调试**: 推荐在 IDE 中直接运行应用代码,仅用 Docker 管理中间件
