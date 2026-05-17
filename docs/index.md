# IAM Platform 文档索引

本文档提供 IAM Platform 项目所有文档的导航索引。

## 快速开始

| 文档 | 说明 | 适合人群 |
|------|------|----------|
| [README.md](../README.md) | 项目概述、快速开始、核心功能 | 所有开发者 |
| [QUICK_REFERENCE.md](../QUICK_REFERENCE.md) | 常用命令速查表 | 日常开发运维 |
| [DEPLOYMENT.md](../DEPLOYMENT.md) | 完整部署指南 | 运维工程师、DevOps |

## 项目文档

### 1. 项目概述与架构

- **[README.md](README.md)**
  - 项目简介
  - 技术栈
  - 核心功能
  - 快速开始
  - 项目结构
  - 多环境配置
  - 开发规范
  - 集成指南

### 2. 部署与运维

- **[DEPLOYMENT.md](../DEPLOYMENT.md)**
  - 中间件连接配置（DEV 环境）
  - Docker 部署
  - 多环境配置
  - CI/CD 构建脚本
  - 监控与运维

- **[SCRIPTS_REFERENCE.md](../SCRIPTS_REFERENCE.md)**
  - ci-build.ps1 详细说明
  - uninstall-env.ps1 详细说明
  - 脚本工作流程
  - 使用示例

- **[DOCKER_OPERATIONS.md](../DOCKER_OPERATIONS.md)**
  - Docker 快速开始
  - 容器管理命令
  - 日志查看
  - 故障排查
  - 资源清理

- **[QUICK_REFERENCE.md](../QUICK_REFERENCE.md)**
  - 环境部署命令
  - 环境卸载命令
  - 状态查看命令
  - 日志查看命令
  - 诊断命令
  - 紧急操作
  - Docker 镜像管理

### 3. 集成与对接

- **[Spring Authorization Server 原理与流程.md](Spring%20Authorization%20Server%20原理与流程.md)**
  - 双 SecurityFilterChain 架构设计
  - OAuth2 授权码完整流程
  - Session 存储与水平扩容机制
  - SavedRequest 登录恢复机制
  - 授权同意页面触发逻辑
  - JWT 签发时机与机制
  - 核心配置详解
  - 数据存储结构
  - 适合：SSO 服务开发者、架构师

- **[第三方服务对接指南.md](第三方服务对接指南.md)**
  - 客户端注册流程
  - Spring Security 客户端配置
  - PKCE 支持配置
  - 自定义用户信息映射
  - Resource Server 模式（API 保护）
  - 刷新令牌流程
  - 注销流程
  - 安全最佳实践
  - 常见问题排查
  - 适合：第三方服务开发者、集成工程师

### 4. 环境配置

- **[KUBELET_DEV_ENVIRONMENT.md](KUBELET_DEV_ENVIRONMENT.md)**
  - DEV 环境 Kubelet 直连部署方案
  - 部署流程说明
  - 技术优势
  - 注意事项

- **[k8s/overlays/dev/HOST_IP_DISCOVERY.md](k8s/overlays/dev/HOST_IP_DISCOVERY.md)**
  - Docker Desktop kind 集群 Host IP 动态发现方案
  - Init Container 配置
  - 验证方法
  - 故障排查

### 5. 数据库与测试数据

- **[src/main/resources/db/dev/README_MOCK_DATA.md](src/main/resources/db/dev/README_MOCK_DATA.md)**
  - DEV 环境 Mock 数据重置指南
  - 测试数据说明
  - 加密说明
  - 常见问题

## 规则与规范

项目规则文件位于 `.qoder/rules/` 目录：

- **[sso_project_overview.md](.qoder/rules/sso_project_overview.md)**
  - 项目概述与核心规范
  - 项目结构规范
  - AI 参与开发规范
  - 安全规范
  - 多环境配置

- **[sso_code_review.md](.qoder/rules/sso_code_review.md)**
  - 代码审查规范
  - 单元测试规范
  - Swagger 文档规范
  - OIDC 安全规范

- **[sso_team_collaboration.md](.qoder/rules/sso_team_collaboration.md)**
  - Git 工作流
  - 分支管理
  - 提交规范
  - 代码审查流程

- **[sso_environment_version_cicd.md](.qoder/rules/sso_environment_version_cicd.md)**
  - 环境版本管理
  - 镜像标签规范
  - CI/CD 流程
  - Kustomize 配置规范

## 按场景查找文档

### 我是第三方服务开发者，如何对接 SSO？

1. 阅读 [第三方服务对接指南.md](第三方服务对接指南.md) 了解对接流程
2. 按照指南注册客户端并配置 Spring Security
3. 参考 [Spring Authorization Server 原理与流程.md](Spring%20Authorization%20Server%20原理与流程.md) 了解底层机制

### 我需要了解 SSO 内部工作原理

1. 阅读 [Spring Authorization Server 原理与流程.md](Spring%20Authorization%20Server%20原理与流程.md)
2. 了解双 FilterChain 架构和授权流程
3. 查看核心配置和数据存储结构

### 我需要对接 OAuth2/OIDC 协议

1. 先阅读 [第三方服务对接指南.md](第三方服务对接指南.md) 的快速开始
2. 了解授权码流程和 JWT 机制
3. 参考原理文档深入理解协议细节

### 我是新开发者，如何快速上手？

1. 阅读 [README.md](../README.md) 了解项目
2. 按照快速开始章节本地运行
3. 查看 [QUICK_REFERENCE.md](../QUICK_REFERENCE.md) 了解常用命令

### 我需要部署到某个环境

1. 查看 [DEPLOYMENT.md](../DEPLOYMENT.md) 了解部署流程
2. 使用 [QUICK_REFERENCE.md](../QUICK_REFERENCE.md) 中的命令快速部署
3. 参考 [SCRIPTS_REFERENCE.md](../SCRIPTS_REFERENCE.md) 了解脚本参数

### DEV 环境部署遇到问题

1. 查看 [DEPLOYMENT.md](../DEPLOYMENT.md) 的中间件连接配置章节
2. 如果是数据库问题，查看 [README_MOCK_DATA.md](../src/main/resources/db/dev/README_MOCK_DATA.md)

### 我需要编写代码

1. 查看 [sso_project_overview.md](../.qoder/rules/sso_project_overview.md) 了解项目规范
2. 查看 [sso_code_review.md](../.qoder/rules/sso_code_review.md) 了解代码审查要求
3. 查看 [sso_team_collaboration.md](../.qoder/rules/sso_team_collaboration.md) 了解 Git 工作流

### 我需要排查问题

1. 使用 [QUICK_REFERENCE.md](../QUICK_REFERENCE.md) 中的诊断命令
2. 使用 [DOCKER_OPERATIONS.md](../DOCKER_OPERATIONS.md) 中的 Docker 命令
3. 查看 [DEPLOYMENT.md](../DEPLOYMENT.md) 的监控与运维章节

## 文档维护说明

### 文档更新原则

1. **单一职责：** 每个文档专注于一个主题
2. **避免重复：** 通用内容放在一个地方，其他地方引用
3. **保持同步：** 代码变更时同步更新相关文档
4. **清晰导航：** 文档之间互相引用，方便跳转

### 文档结构

```
项目根目录/
├── README.md                          # 项目概述（入口文档）
├── DEPLOYMENT.md                      # 部署指南
├── DOCKER_OPERATIONS.md               # Docker 操作速查
├── SCRIPTS_REFERENCE.md               # 脚本详细说明
├── QUICK_REFERENCE.md                 # 命令速查
├── docs/
│   └── index.md                       # 本文档
├── src/main/resources/db/dev/
│   └── README_MOCK_DATA.md            # Mock 数据管理
└── .qoder/rules/
    ├── sso_project_overview.md        # 项目规范
    ├── sso_code_review.md             # 代码审查规范
    ├── sso_team_collaboration.md      # 团队协作规范
    └── sso_environment_version_cicd.md # 环境版本规范
```

### 贡献指南

1. 新增功能时同步更新相关文档
2. 修改部署流程时更新 DEPLOYMENT.md
3. 添加新脚本时更新 SCRIPTS_REFERENCE.md 和 QUICK_REFERENCE.md
4. 变更配置时更新对应的说明文档
