# IAM Platform 文档索引

本文档提供 IAM Platform 项目所有文档的导航索引。

## 快速开始

| 文档 | 说明 | 适合人群 |
|------|------|----------|
| [README.md](../README.md) | 项目概述、快速开始、核心功能 | 所有开发者 |
| [统一认证框架.md](design/统一认证框架.md) | 统一认证框架详细说明 | 架构师、核心开发者 |
| [第三方服务对接指南.md](design/第三方服务对接指南.md) | 第三方服务对接SSO指南 | 第三方服务开发者 |
| [ssl/README.md](../ssl/README.md) | SSL/HTTPS配置指南 | 所有开发者、运维人员 |

## 项目文档

### 1. 项目概述与架构

- **[README.md](../README.md)**
  - 项目简介
  - 技术栈
  - 核心功能
  - 快速开始
  - 项目结构
  - 多环境配置
  - 开发规范
  - 集成指南

### 2. 设计文档 (docs/design/)

#### 2.1 架构与设计

- **[统一认证框架.md](design/统一认证框架.md)**
  - 统一认证框架详细设计
  - 架构原理
  - 核心流程
  - 适合：架构师、核心开发者

- **[Spring Authorization Server 原理与流程.md](design/Spring%20Authorization%20Server%20原理与流程.md)**
  - 双 SecurityFilterChain 架构设计
  - OAuth2 授权码完整流程
  - Session 存储与水平扩容机制
  - SavedRequest 登录恢复机制
  - 授权同意页面触发逻辑
  - JWT 签发时机与机制
  - 核心配置详解
  - 数据存储结构
  - 适合：SSO 服务开发者、架构师

- **[Token机制详解.md](design/Token机制详解.md)**
  - Token 类型与格式
  - JWT 结构与解析
  - Token 签发与验证
  - Token 刷新机制
  - 适合：所有开发者

#### 2.2 协议文档 (docs/design/protocol/)

- **[OIDC完整文档.md](design/protocol/OIDC完整文档.md)**
  - OIDC协议完整说明
  - 授权码流程
  - Token机制
  - 适合：所有开发者

- **[SAML-支持文档.md](design/protocol/SAML-支持文档.md)**
  - SAML 2.0 协议支持
  - IdP metadata 生成
  - OpenSAML 集成
  - 适合：需要SAML集成的开发者

- **[CAS-单点登出实现说明.md](design/protocol/CAS-单点登出实现说明.md)**
  - CAS SLO 实现原理
  - 技术架构
  - 核心流程
  - 适合：开发者、架构师

- **[CAS-单点登出使用示例.md](design/protocol/CAS-单点登出使用示例.md)**
  - 使用示例
  - 配置方法
  - 适合：第三方服务开发者

### 3. 集成与对接

- **[第三方服务对接指南.md](design/第三方服务对接指南.md)**
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

### 4. SSL/TLS 配置

- **[ssl/README.md](../ssl/README.md)** ⭐ **权威文档**
  - 证书文件管理
  - 快速开始指南
  - 服务配置详解
  - HTTPS 启用方法（4种方式）
  - JWT 签名配置
  - 故障排查
  - 启用脚本：`ssl/enable-https.ps1`
  - 适合：所有开发者、运维人员

### 5. 测试与验证

- **[TOKEN_VALIDATION_TEST.md](TOKEN_VALIDATION_TEST.md)**
  - Token 验证流程测试指南
  - 测试步骤与命令
  - 常见问题排查
  - 适合：测试人员、开发者

### 6. 项目管理

- **[未完成特性清单.md](未完成特性清单.md)**
  - 未完成功能清单
  - 功能优先级
  - 适合：项目经理、开发者

### 7. 文档管理

- **[文档整理总结.md](文档整理总结.md)**
  - 文档整理记录
  - 文档结构规范
  - 维护指南

## 按场景查找文档

### 我是第三方服务开发者，如何对接 SSO？

1. 阅读 [第三方服务对接指南.md](design/第三方服务对接指南.md) 了解对接流程
2. 按照指南注册客户端并配置 Spring Security
3. 参考 [Spring Authorization Server 原理与流程.md](design/Spring%20Authorization%20Server%20原理与流程.md) 了解底层机制

### 我需要了解 SSO 内部工作原理

1. 阅读 [Spring Authorization Server 原理与流程.md](design/Spring%20Authorization%20Server%20原理与流程.md)
2. 了解双 FilterChain 架构和授权流程
3. 查看核心配置和数据存储结构

### 我需要对接 OAuth2/OIDC 协议

1. 先阅读 [第三方服务对接指南.md](design/第三方服务对接指南.md) 的快速开始
2. 了解授权码流程和 JWT 机制
3. 参考原理文档深入理解协议细节

### 我是新开发者，如何快速上手？

1. 阅读 [README.md](../README.md) 了解项目
2. 查看 [统一认证框架.md](design/统一认证框架.md) 了解架构设计
3. 按照快速开始章节本地运行

### 我需要配置 SSL/HTTPS

1. 阅读 [ssl/README.md](../ssl/README.md)（权威文档）
2. 使用 `ssl/enable-https.ps1` 脚本快速启用
3. 按照文档指南配置HTTPS

### 我需要实现 CAS 单点登出

1. 阅读 [CAS-单点登出实现说明.md](design/protocol/CAS-单点登出实现说明.md) 了解原理
2. 参考 [CAS-单点登出使用示例.md](design/protocol/CAS-单点登出使用示例.md) 进行配置

### 我需要集成 SAML

1. 阅读 [SAML-支持文档.md](design/protocol/SAML-支持文档.md)
2. 了解 OpenSAML 集成方法
3. 配置 IdP metadata

### 我需要了解 OIDC 协议

1. 阅读 [OIDC完整文档.md](design/protocol/OIDC完整文档.md)
2. 了解授权码流程和Token机制
3. 参考第三方服务对接指南进行集成

### 我需要排查 Token 相关问题

1. 阅读 [Token机制详解.md](design/Token机制详解.md) 了解 Token 机制
2. 使用 [TOKEN_VALIDATION_TEST.md](TOKEN_VALIDATION_TEST.md) 进行测试
3. 查看日志中的认证失败信息

### 我需要编写代码

1. 查看 `.qoder/rules/` 目录下的项目规范
2. 参考现有代码风格和架构设计
3. 查看相关功能文档了解实现细节

### 我需要了解项目进度

1. 查看 [未完成特性清单.md](未完成特性清单.md)
2. 了解已完成和待完成的功能
3. 确定下一步开发计划

## 文档维护说明

### 文档更新原则

1. **单一职责：** 每个文档专注于一个主题
2. **避免重复：** 通用内容放在一个地方，其他地方引用
3. **保持同步：** 代码变更时同步更新相关文档
4. **清晰导航：** 文档之间互相引用，方便跳转
5. **权威文档：** 对于同一主题，指定一个权威文档，其他作为历史参考

### 文档结构

```
项目根目录/
├── README.md                          # 项目概述（入口文档）
├── ssl/                               # SSL证书与配置
│   ├── README.md                      # ⭐ SSL权威文档
│   ├── enable-https.ps1               # HTTPS启用脚本
│   ├── keystore.p12                   # 密钥库（不提交）
│   ├── private.key                    # 私钥（不提交）
│   ├── certificate.crt                # 公钥证书
│   └── certificate.cer                # DER证书
├── docs/
│   ├── index.md                       # 本文档（文档索引）
│   ├── TOKEN_VALIDATION_TEST.md       # Token测试指南（测试类）
│   ├── 未完成特性清单.md               # 功能清单（管理类）
│   ├── design/                        # 设计文档目录
│   │   ├── 统一认证框架.md             # 架构设计
│   │   ├── Spring Authorization Server 原理与流程.md # 认证原理
│   │   ├── Token机制详解.md           # Token设计
│   │   ├── 第三方服务对接指南.md       # 集成设计
│   │   └── protocol/                  # 协议文档目录
│   │       ├── OIDC完整文档.md         # OIDC协议
│   │       ├── SAML-支持文档.md        # SAML协议
│   │       ├── CAS-单点登出实现说明.md  # CAS协议
│   │       └── CAS-单点登出使用示例.md  # CAS协议示例
│   ├── plan/                          # Qoder临时计划（空）
│   └── archived/                      # 归档文档
└── .qoder/rules/                      # 项目规范
```

### 贡献指南

1. 新增功能时同步更新相关文档
2. 修改配置时更新对应的说明文档
3. 对于同一主题的文档，维护一个权威版本
4. 历史文档保留作为参考，但明确标注为"历史参考"
5. 定期清理过时或重复的文档
