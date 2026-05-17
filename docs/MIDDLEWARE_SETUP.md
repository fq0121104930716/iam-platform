# 本地中间件环境配置说明

## 更新日期
2026-05-17

## 更新内容

基于实际运行的Docker容器，重新生成了本地开发环境的中间件配置。

## 中间件配置信息

### 1. Nacos (服务注册与配置中心)
- **容器名**: nacos
- **镜像**: nacos/nacos-server:v2.3.2
- **端口**: 8848 (HTTP), 9848 (gRPC)
- **认证**: nacos/nacos
- **命名空间**: iam-platform-dev
- **JVM配置**: 
  - XMS/XMX: 1g
  - XMN: 512m
  - 其他参数已优化

### 2. PostgreSQL (关系型数据库)
- **容器名**: postgres
- **镜像**: dhi.io/postgres:18-alpine3.23-dev
- **端口**: 5432
- **认证**: 
  - 用户名: postgres
  - 密码: STPass123!
- **数据目录**: /var/lib/postgresql/18/data
- **初始数据库**: 无（应用启动时自动创建）

### 3. Redis (缓存与Session存储)
- **容器名**: redis
- **镜像**: redis:8.6
- **端口**: 6379
- **认证**: 
  - 密码: iam59!z$
- **配置文件**: ./redis/redis.conf
- **数据持久化**: AOF enabled
- **内存限制**: 256mb

### 4. Prometheus (指标监控)
- **容器名**: prometheus
- **镜像**: ubuntu/prometheus:3.11-24.04_stable
- **端口**: 9090
- **控制台**: http://localhost:9090
- **配置文件**: ./prometheus/prometheus.yml
- **认证**: 无需认证

### 5. Zipkin (链路追踪)
- **容器名**: zipkin
- **镜像**: openzipkin/zipkin:3
- **端口**: 9410, 9411
- **控制台**: http://localhost:9411
- **认证**: 无需认证

## 配置文件说明

### docker-compose.yml
已更新为与实际运行容器一致的配置，移除了应用服务（gateway、auth、admin、bff），仅保留中间件服务。

### redis/redis.conf
新增Redis配置文件，包含：
- 密码认证
- AOF持久化
- 内存管理策略
- 性能优化参数

### prometheus/prometheus.yml
新增Prometheus配置文件，包含：
- IAM平台各服务的metrics采集配置
- 15秒采集间隔
- 使用host.docker.internal访问宿主机服务

## 使用方式

### 启动所有中间件
```bash
cd d:\VsCodeProject\iam-platform
docker-compose up -d
```

### 检查服务状态
```bash
# 方式1: 使用docker-compose
docker-compose ps

# 方式2: 使用PowerShell检查脚本
cd ~/.qoder/skills/local-middleware-manager/scripts
.\check-env.ps1
```

### 停止所有中间件
```bash
docker-compose stop
```

### 清理所有数据（慎用）
```bash
docker-compose down -v
```

## 连接字符串

### PostgreSQL
```
postgresql://postgres:STPass123!@localhost:5432/[database]
```

### Redis
```
redis://:iam59!z$@localhost:6379/0
```

### Nacos (Spring Boot)
```yaml
spring:
  cloud:
    nacos:
      config:
        server-addr: localhost:8848
        namespace: iam-platform-dev
      discovery:
        server-addr: localhost:8848
        namespace: iam-platform-dev
```

## 注意事项

1. **端口占用**: 确保以下端口未被占用：8848, 9848, 5432, 6379, 9090, 9410, 9411
2. **数据安全**: 执行 `docker-compose down -v` 会删除所有数据，请先备份
3. **资源占用**: 所有服务同时运行约占用4-6GB内存
4. **首次启动**: Nacos首次启动需要1-2分钟，请耐心等待
5. **Windows环境**: 使用Docker Desktop时，确保WSL2后端已启用

## 故障排查

### 服务无法启动
```bash
# 查看详细日志
docker-compose logs [service-name]

# 检查端口占用
netstat -ano | findstr [port]
```

### 无法连接到PostgreSQL
```bash
# 测试连接
docker exec postgres pg_isready -U postgres

# 使用psql连接
docker exec -it postgres psql -U postgres
```

### Redis连接被拒绝
```bash
# 测试连接
docker exec redis redis-cli -a 'iam59!z$' ping

# 检查配置
docker exec redis cat /etc/redis/redis.conf
```

## 相关文档

- Skill文档: ~/.qoder/skills/local-middleware-manager/SKILL.md
- 配置参考: ~/.qoder/skills/local-middleware-manager/reference.md
- 检查脚本: ~/.qoder/skills/local-middleware-manager/scripts/check-env.ps1
- 管理脚本: ~/.qoder/skills/local-middleware-manager/scripts/manage-middleware.ps1
