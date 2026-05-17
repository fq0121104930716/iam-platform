---
trigger: model_decision
description: "涉及环境拆分、版本号管理、镜像标签、CI/CD部署、Kustomize配置时应用此规范"
---
# IAM Platform项目环境拆分、版本管理与CI/CD部署规范

## 1. 多环境架构

### 1.1 环境定义

| 环境 | Profile | 命名空间 | 用途 |
|------|---------|----------|------|
| DEV | `dev` | `iam-platform-dev` | 本地开发联调，默认激活 |
| TEST | `test` | `iam-platform-test` | QA 测试验证 |
| CANARY | `canary` | `iam-platform-prod` | 灰度发布验证，复用PROD命名空间 |
| PROD | `prod` | `iam-platform-prod` | 生产环境 |

### 1.2 Kustomize分层原则

- **Base层**（`k8s/base/`）：所有环境共享的资源模板，不含namespace，不硬编码环境差异值
- **Overlay层**（`k8s/overlays/<env>/`）：环境特异配置覆盖，包含namespace定义、镜像标签、副本数、资源配额等
- CANARY使用 `namePrefix: canary-` 区分资源，不创建独立Namespace资源
- Namespace资源必须由各Overlay独立定义（Kustomize namespace transformer不会修改Namespace资源的metadata.name）

### 1.3 环境配置隔离约束

- 环境差异必须通过 Kustomize Overlay 的 patches 覆盖，禁止在 Base 层硬编码环境特定值
- 敏感配置（Secret）使用占位符，部署前通过 CI/CD Pipeline、Vault 或 kubectl create secret 注入真实值
- DEV 环境的 Secret 可以使用开发默认值，TEST+ 环境禁止在 YAML 中明文写入密码

## 2. 版本管理

### 2.1 版本号来源

- **Maven项目版本**：`pom.xml` 中的 `<version>` 作为默认版本号（如 `1.0.0-SNAPSHOT`）
- **Git标签版本**：CI/CD流水线从Git标签提取版本号（`v1.2.3` → `1.2.3`），优先级高于Maven版本
- **覆盖机制**：CI/CD通过 `-Ddocker.image.version=<版本号>` 覆盖Maven属性

### 2.2 Maven属性定义

```xml
<properties>
    <!-- 版本号，CI/CD 可通过 -Ddocker.image.version=1.2.3 覆盖 -->
    <docker.image.version>${project.version}</docker.image.version>
</properties>
```

各 Maven Profile 的镜像标签规则：

| Profile | docker.image.tag 格式 | 示例 |
|---------|----------------------|------|
| dev | `${docker.image.version}-dev` | `1.0.0-SNAPSHOT-dev` |
| test | `${docker.image.version}-test` | `1.0.0-SNAPSHOT-test` |
| canary | `${docker.image.version}-canary` | `1.0.0-SNAPSHOT-canary` |
| prod | `${docker.image.version}` | `1.2.3` |

### 2.3 Git标签规范

| 标签格式 | 触发环境 | 示例 | 说明 |
|---------|---------|------|------|
| `v<major>.<minor>.<patch>` | PROD | `v1.2.3` | 正式发布，经TEST→CANARY逐级验证后部署 |
| `v<major>.<minor>.<patch>-rc.<n>` | CANARY | `v1.2.3-rc.1` | 发布候选，灰度验证 |
| `v<major>.<minor>.<patch>-beta.<n>` | TEST | `v1.2.3-beta.1` | 测试版本，QA验证 |
| develop分支推送 | DEV | - | 每次合并自动部署 |

### 2.4 版本号约束

- 禁止使用 `latest` 作为镜像标签，每个镜像必须可追溯到具体版本号
- SNAPSHOT 版本仅用于 DEV 环境本地开发，正式发布必须移除 SNAPSHOT 后缀
- PROD 镜像使用纯版本号（不带环境后缀），便于直接识别和回滚
- 版本号递增遵循语义化版本（SemVer）：`MAJOR.MINOR.PATCH`

## 3. 镜像构建

### 3.1 Dockerfile 规范

- 必须使用多阶段构建（build stage + run stage），构建产物不包含 JDK
- `ARG SPRING_PROFILE` 必须通过 `ENV` 传递到运行时：

```dockerfile
ARG SPRING_PROFILE=dev
ENV SPRING_PROFILES_ACTIVE=${SPRING_PROFILE}
```

- ENTRYPOINT 引用 ENV 而非 ARG：`-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}`
- 运行时镜像必须使用非 root 用户（`appuser`），启用安全上下文

### 3.2 构建脚本

- **本地开发**：使用 `build.ps1`（仅编译验证，不涉及打包和镜像构建）
- **CI/CD**：使用 `ci-build.ps1`（支持 `-Environment`、`-Version`、`-Registry`、`-Push`、`-Deploy`、`-SkipTests` 参数）
- 版本号自动解析优先级：`-Version` 参数 > Git标签 > Maven project.version
- 禁止在 CI/CD 中跳过测试（`-SkipTests`）部署到 TEST+ 环境

## 4. CI/CD部署

### 4.1 渐进式发布流程

```
DEV → TEST → CANARY → PROD
(自动)  (自动)   (手动审批)  (手动审批)
```

每个阶段验证通过后才推进到下一环境，禁止跳级部署。

### 4.2 Kustomize 镜像注入

CI/CD部署时**禁止手动修改** `kustomization.yaml` 中的镜像标签，必须通过 `kustomize edit set image` 动态注入：

```bash
cd k8s/overlays/<env>
kustomize edit set image iam-platform-service=<registry>/iam-platform-service:<tag>
kubectl apply -k .
```

`kustomization.yaml` 中的 `newTag` 仅为本地预览的默认值，不代表实际部署版本。

### 4.3 部署验证

部署到任何环境后必须验证：

- Pod健康状态：`kubectl -n <namespace> get pods`
- 启动探针通过：应用可正常响应健康检查
- 镜像版本正确：`kubectl -n <namespace> get deploy -o jsonpath='{.spec.template.spec.containers[0].image}'`
- CANARY环境额外验证流量路由标签 `canary: "true"` 已正确注入

### 4.4 服务访问规范

**优先通过网关访问原则：**

- **部署验证和测试必须优先通过 Ingress/Gateway 访问服务**，而非直接使用 `kubectl port-forward`
- Ingress 配置位于 `k8s/base/ingress.yaml`，各环境可通过 Overlay 自定义 host 规则
- 仅在以下情况允许使用 `kubectl port-forward`：
  - Ingress 尚未配置或不可用
  - 调试内部端口（如 management port 9001）
  - 临时排查网络问题
- 本地开发环境需配置 hosts 文件，将 Ingress host 指向 K8s 节点 IP 或 Docker Desktop 网关

### 4.5 回滚策略

- **K8s回滚**：`kubectl rollout undo deployment/iam-platform-service -n <namespace>`
- **版本回退**：重新部署指定版本的镜像标签
- **PROD回滚**必须先在CANARY验证回滚版本的兼容性

### 4.6 环境变量与配置管理

| 变量 | DEV | TEST+ | 说明 |
|------|-----|-------|------|
| `SPRING_PROFILES_ACTIVE` | ConfigMap注入 | ConfigMap注入 | 由Kustomize Overlay覆盖 |
| `DB_HOST/PORT/NAME` | localhost | K8s Service DNS | 环境差异 |
| `DB_USERNAME/PASSWORD` | 开发默认值 | CI/CD注入 | 禁止明文提交 |
| `REDIS_PASSWORD` | 开发默认值 | CI/CD注入 | 禁止明文提交 |
| `ENCRYPTION_KEY` | 开发占位符 | CI/CD注入 | PROD必须注入32字符密钥 |
| `OIDC_ISSUER_URI` | http://localhost:9000 | K8s Service DNS | OIDC Issuer URI |

### 4.7 中间件依赖管理

- **依赖确认原则：** 部署服务前必须确认所需中间件（如 PostgreSQL、Redis）已就绪且可连接
- **禁止自动创建：** 当检测到中间件缺失时，**禁止**自动执行 `docker run` 或类似命令创建容器，避免资源冲突、版本不匹配或环境隔离问题
- **用户确认流程：** 若中间件未就绪，应向用户明确说明缺失的组件、预期影响，并提供可选方案（如：引导用户手动启动、使用现有实例、或确认后由运维人员统一创建）
- **配置校验优先：** 在部署脚本或启动流程中，优先通过健康检查或连接探测验证中间件可用性，而非默认假设本地 Docker 环境可用
