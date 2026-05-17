# Docker Compose 使用指南

## 文件说明

本项目已将 Docker Compose 配置拆分为两个独立的文件：

- **docker-compose.middleware.yml**: 中间件服务配置
  - Nacos (服务注册与配置中心)
  - PostgreSQL (数据库)
  - Redis (缓存与会话存储)
  - Prometheus (指标监控)
  - Zipkin (分布式追踪)
  - RocketMQ (消息队列)

- **docker-compose.app.yml**: 业务服务配置
  - IAM BFF Server (端口 9010)
  - IAM Auth Server (端口 9001, 9005)
  - IAM Admin Server (端口 9002, 9006)
  - IAM Audit Server (端口 9003, 9004)
  - IAM Gateway (端口 9000)

## 使用方式

### 1. 启动所有中间件

```bash
docker-compose -f docker-compose.middleware.yml up -d
```

### 2. 等待中间件就绪

检查中间件健康状态：

```bash
docker-compose -f docker-compose.middleware.yml ps
```

确保所有服务状态为 `healthy` 或 `running`。

### 3. 构建业务服务

在启动业务服务之前，需要先构建各个模块：

```bash
# 在项目根目录执行
mvn clean package -DskipTests
```

### 4. 启动业务服务

```bash
docker-compose -f docker-compose.app.yml up -d
```

### 5. 或者同时启动所有服务

```bash
docker-compose -f docker-compose.middleware.yml -f docker-compose.app.yml up -d
```

### 6. 停止服务

停止中间件：

```bash
docker-compose -f docker-compose.middleware.yml down
```

停止业务服务：

```bash
docker-compose -f docker-compose.app.yml down
```

停止所有服务：

```bash
docker-compose -f docker-compose.middleware.yml -f docker-compose.app.yml down
```

### 7. 查看日志

查看特定服务日志：

```bash
# 查看 BFF 服务日志
docker-compose -f docker-compose.app.yml logs -f iam-bff-service

# 查看认证服务日志
docker-compose -f docker-compose.app.yml logs -f iam-auth-server
```

### 8. 重启服务

```bash
# 重启单个服务
docker-compose -f docker-compose.app.yml restart iam-bff-service

# 重启所有业务服务
docker-compose -f docker-compose.app.yml restart
```

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

所有服务都运行在 `iam-network` 桥接网络中，服务间可以通过服务名称互相访问。

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

1. 启动业务服务前，请确保中间件服务已完全就绪
2. 首次启动需要先执行 `mvn clean package` 构建 JAR 文件
3. 如果使用 HTTPS，需要配置 SSL 证书（参考 ssl/README.md）
4. 数据持久化路径可根据实际情况修改
