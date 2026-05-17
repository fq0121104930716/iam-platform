---
trigger: model_decision
description: "执行Git操作、代码提交或团队协作相关任务时应用此规范"
---
# IAM Platform项目团队协作规范

## 1. Git工作流

采用 **Git Flow** 分支模型，分支与环境映射如下：

### 1.1 分支与环境映射

| 分支 | 命名 | 环境 | 部署方式 |
|------|------|------|----------|
| master | `master` | PROD（生产） | 全量部署 |
| canary | `canary/<version>` | CANARY（灰度） | 灰度发布，逐步扩量 |
| release | `release/<version>` | TEST（测试） | 自动部署 |
| develop | `develop` | DEV（开发） | 自动部署 |
| feature | `feature/<name>` | - | 本地开发 / PR 预览 |
| hotfix | `hotfix/<name>` | - | 紧急修复 |

### 1.2 常规发布流程

```
feature/* → develop → release/* → canary/* → master
   (DEV)      (DEV)    (TEST)     (CANARY)    (PROD)
```

1. 从 `develop` 创建 `feature/<name>`，开发完成后合并回 `develop`
2. `develop` 自动部署到 **DEV** 环境，进行开发联调
3. 从 `develop` 创建 `release/<version>`，自动部署到 **TEST** 环境，进行 QA 测试
4. QA 通过后，从 `release/<version>` 创建 `canary/<version>`，部署到 **CANARY** 灰度环境
5. 灰度验证通过后，合并 `release/<version>` 到 `master`，全量部署到 **PROD**
6. 同步合并 `release/<version>` 回 `develop`

### 1.3 灰度发布策略

- 灰度阶段流量比例：5% → 20% → 50% → 100%
- 每个阶段观察至少 30 分钟，监控错误率、延迟、资源使用
- 灰度期间发现问题，立即回滚 `canary/<version>`，流量切回 `master`
- 灰度全量通过后，删除 `canary/<version>` 分支

### 1.4 紧急修复流程

1. 从 `master` 创建 `hotfix/<name>`
2. 修复并验证后，合并到 `master`（PROD）和 `develop`（DEV）
3. 如需灰度验证，先走 `canary/<version>` 再全量

### 1.5 分支保护规则

- `master`：禁止直接推送，必须通过 PR 合并，至少 1 人审批
- `develop`：禁止直接推送，必须通过 PR 合并
- `release/*`：只接受 bug 修复提交，不接受新功能
- `canary/*`：只接受灰度配置调整，不接受功能变更

## 2. Git标签与CI/CD触发

### 2.1 Git标签规范

| 标签格式 | 触发环境 | 示例 |
|---------|---------|------|
| `v<major>.<minor>.<patch>` | PROD（经TEST→CANARY逐级验证） | `v1.2.3` |
| `v<major>.<minor>.<patch>-rc.<n>` | CANARY | `v1.2.3-rc.1` |
| `v<major>.<minor>.<patch>-beta.<n>` | TEST | `v1.2.3-beta.1` |
| develop分支推送 | DEV | - |

- 标签必须以 `v` 前缀开头，版本号遵循语义化版本（SemVer）
- 标签一旦推送不可修改，如需修正必须创建新标签
- PROD标签只能在 `master` 分支上打

### 2.2 CI/CD镜像标签策略

- 禁止使用 `latest` 作为镜像标签
- DEV镜像标签：`<version>-dev`（如 `1.0.0-SNAPSHOT-dev`）
- TEST镜像标签：`<version>-test`（如 `1.2.3-beta.1-test`）
- CANARY镜像标签：`<version>-canary`（如 `1.2.3-rc.1-canary`）
- PROD镜像标签：`<version>`（如 `1.2.3`，不带环境后缀）

### 2.3 CI/CD部署约束

- 渐进式发布：DEV → TEST → CANARY → PROD，禁止跳级部署
- Kustomize 镜像标签必须通过 `kustomize edit set image` 动态注入，禁止手动修改 kustomization.yaml
- 部署到 TEST+ 环境必须经过测试，禁止使用 `-SkipTests`
- PROD 和 CANARY 部署需要手动审批

## 3. 代码提交规范

- 提交信息使用英文
- 提交信息格式：`<type>(<scope>): <subject>`
- 类型包括：feat、fix、docs、style、refactor、test、chore
- 提交信息要简洁明了

示例：

```
feat(user): add user registration endpoint
fix(auth): resolve PKCE verification failure
feat(client): add OAuth2 client rotation secret API
chore(deps): upgrade spring boot to 3.2.5
docs(readme): update deployment instructions
```

## 4. 团队沟通

- 每日站会同步开发进度
- 使用项目管理工具跟踪任务
- 重要决策需要团队讨论
- 定期进行技术分享和培训
