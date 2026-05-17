# SSL/TLS 证书与配置指南

本目录包含IAM平台使用的SSL/TLS证书、密钥文件、配置文档和启用脚本。

## 📁 目录内容

### 证书文件

| 文件名 | 说明 | 用途 | Git管理 |
|--------|------|------|---------|
| `keystore.p12` | PKCS12格式密钥库 | Spring Boot HTTPS配置 | ❌ 不提交 |
| `private.key` | PEM格式私钥 | JWT签名、SAML签名 | ❌ 不提交 |
| `certificate.crt` | PEM格式证书 | JWT验证、公钥分发 | ✅ 可提交 |
| `certificate.cer` | DER格式证书 | 证书导入、信任库 | ✅ 可提交 |

### 文档与脚本

| 文件 | 说明 |
|------|------|
| `README.md` | 本文档 - SSL完整配置指南 |
| `enable-https.ps1` | HTTPS快速启用脚本 |

---

## 🚀 快速开始

### 方法1：使用脚本（推荐）

```powershell
cd d:\VsCodeProject\iam-platform
.\ssl\enable-https.ps1
.\mvnw spring-boot:run -pl iam-auth-server
```

### 方法2：手动设置环境变量

```powershell
$env:SSL_ENABLED="true"
$env:SSL_KEY_STORE="file:${PWD}/ssl/keystore.p12"
$env:SSL_KEY_STORE_PASSWORD="changeit"
$env:JWK_RSA_PRIVATE_KEY="file:${PWD}/ssl/private.key"
$env:JWK_RSA_PUBLIC_KEY="file:${PWD}/ssl/certificate.crt"
```

### 服务端口

| 服务 | HTTP | HTTPS |
|------|------|-------|
| Auth Server | 9000 | 9000 |
| Gateway | 8080 | 8080 |
| Admin Server | 9002 | 9002 |
| BFF Server | 9010 | 9010 |

---

## 证书信息

- **算法**: RSA 2048位
- **有效期**: 10年 (3650天)
- **主题**: CN=localhost, OU=Development, O=IAM Platform, L=Beijing, ST=Beijing, C=CN
- **密钥库密码**: `changeit`
- **别名**: `mycert`

## Git配置

`.gitignore` 文件已配置，确保敏感文件不被提交：

```gitignore
# SSL certificates (keep only public certs, exclude private keys and keystores)
ssl/*.key
ssl/*.p12
ssl/*.jks
!ssl/*.crt
!ssl/*.cer
!ssl/README.md
```

**说明**：
- ❌ `private.key` - 私钥，不提交
- ❌ `keystore.p12` - 密钥库，不提交
- ✅ `certificate.crt` - 公钥证书，可以提交
- ✅ `certificate.cer` - 公钥证书，可以提交
- ✅ `ssl/README.md` - 说明文档，可以提交

---

## 服务配置

### 已配置SSL的服务

#### 1. iam-auth-server (端口 9000)
- **配置文件**: `iam-auth-server/src/main/resources/application.yml`
- **HTTPS**: 默认禁用，通过环境变量 `SSL_ENABLED=true` 启用
- **JWK密钥**: 使用新生成的证书进行JWT签名
- **Issuer URI**: `https://localhost:9000`

#### 2. iam-gateway (端口 8080)
- **配置文件**: `iam-gateway/src/main/resources/application.yml`
- **HTTPS**: 默认禁用，通过环境变量 `SSL_ENABLED=true` 启用
- **JWT验证**: 从 `https://localhost:9000` 获取公钥

#### 3. iam-admin-server (端口 9002)
- **配置文件**: `iam-admin-server/src/main/resources/application.yml`
- **HTTPS**: 默认禁用，通过环境变量 `SSL_ENABLED=true` 启用
- **OAuth2 Provider**: 指向 `https://localhost:9000`

#### 4. iam-bff-server (端口 9010)
- **配置文件**: `iam-bff-server/src/main/resources/application.yml`
- **HTTPS**: 默认禁用，通过环境变量 `SSL_ENABLED=true` 启用

### 配置详解

#### 配置变更对比

**之前（绝对路径）**：
```yaml
server:
  ssl:
    key-store: ${SSL_KEY_STORE:C:\Users\voidx\ssl\keystore.p12}

security:
  jwk:
    rsa:
      private-key-location: ${JWK_RSA_PRIVATE_KEY:file:C:\Users\voidx\ssl\private.key}
      public-key-location: ${JWK_RSA_PUBLIC_KEY:file:C:\Users\voidx\ssl\certificate.crt}
```

**之后（相对路径）**：
```yaml
server:
  ssl:
    key-store: ${SSL_KEY_STORE:file:${user.dir}/ssl/keystore.p12}

security:
  jwk:
    rsa:
      private-key-location: ${JWK_RSA_PRIVATE_KEY:file:${user.dir}/ssl/private.key}
      public-key-location: ${JWK_RSA_PUBLIC_KEY:file:${user.dir}/ssl/certificate.crt}
```

#### 迁移优势

- ✅ **跨平台兼容**: 使用 `${user.dir}` 变量，支持Windows、Linux、macOS
- ✅ **版本控制友好**: 证书随项目一起管理，新成员clone项目即可使用
- ✅ **部署简化**: 无需额外配置证书路径，容器化部署更方便

---

## HTTPS 启用方法

### 方法1: 使用脚本（推荐）

```powershell
cd d:\VsCodeProject\iam-platform
.\ssl\enable-https.ps1
.\mvnw spring-boot:run -pl iam-auth-server
```

### 方法2: 环境变量

在启动服务前设置环境变量：

```powershell
$env:SSL_ENABLED="true"
$env:SSL_KEY_STORE="file:${PWD}/ssl/keystore.p12"
$env:SSL_KEY_STORE_PASSWORD="changeit"
$env:JWK_RSA_PRIVATE_KEY="file:${PWD}/ssl/private.key"
$env:JWK_RSA_PUBLIC_KEY="file:${PWD}/ssl/certificate.crt"
```

### 方法3: JVM参数

```powershell
java -jar -DSSL_ENABLED=true -DSSL_KEY_STORE_PASSWORD=changeit iam-auth-server.jar
```

### 方法4: 修改配置文件

直接修改 `application.yml` 或 `application-dev.yml`：

```yaml
server:
  ssl:
    enabled: true
    key-store: file:${user.dir}/ssl/keystore.p12
    key-store-password: changeit
    key-store-type: PKCS12
    key-alias: mycert
```

---

## JWT签名配置

JWK (JSON Web Key) 已配置为使用新生成的证书：

```yaml
security:
  jwk:
    rsa:
      private-key-location: ${JWK_RSA_PRIVATE_KEY:file:${user.dir}/ssl/private.key}
      public-key-location: ${JWK_RSA_PUBLIC_KEY:file:${user.dir}/ssl/certificate.crt}
```

### 自定义JWK密钥

如需使用不同的密钥文件：

```powershell
$env:JWK_RSA_PRIVATE_KEY="file:C:\path\to\your\private.key"
$env:JWK_RSA_PUBLIC_KEY="file:C:\path\to\your\public.crt"
```

---

## 服务访问地址

### HTTP模式（默认）
| 服务 | 地址 |
|------|------|
| Auth Server | http://localhost:9000 |
| Gateway | http://localhost:8080 |
| Admin Server | http://localhost:9002 |
| BFF Server | http://localhost:9010 |

### HTTPS模式（启用SSL后）
| 服务 | 地址 |
|------|------|
| Auth Server | https://localhost:9000 |
| Gateway | https://localhost:8080 |
| Admin Server | https://localhost:9002 |
| BFF Server | https://localhost:9010 |

---

## 开发环境说明

### HTTP vs HTTPS

- **开发环境默认**: HTTP (SSL_ENABLED=false)
- **生产环境建议**: HTTPS (SSL_ENABLED=true)

### 浏览器证书警告

由于使用的是自签名证书，浏览器会显示安全警告。开发环境可以：

1. 点击"高级" → "继续访问"
2. 或将证书导入系统信任库

---

## 重新生成证书

如需重新生成证书：

```powershell
# 删除旧证书（保留公开证书）
Remove-Item ssl/private.key,ssl/keystore.p12 -Force -ErrorAction SilentlyContinue

# 生成新的PKCS12密钥库
keytool -genkeypair `
  -alias mycert `
  -keyalg RSA `
  -keysize 2048 `
  -storetype PKCS12 `
  -keystore ssl/keystore.p12 `
  -validity 3650 `
  -storepass changeit `
  -keypass changeit `
  -dname "CN=localhost, OU=Development, O=IAM Platform, L=Beijing, ST=Beijing, C=CN"

# 导出证书
keytool -exportcert `
  -alias mycert `
  -keystore ssl/keystore.p12 `
  -storepass changeit `
  -rfc > ssl/certificate.crt
```

---

## 安全注意事项

### 开发环境
1. 证书文件位于项目根目录 `ssl/`
2. 默认SSL未启用，使用HTTP模式
3. 启用HTTPS需要设置环境变量
4. 浏览器会显示证书警告（自签名证书）

### 生产环境
1. 使用正式的CA签发证书替换自签名证书
2. 修改默认密码 `changeit`
3. 不要将私钥提交到版本控制系统
4. 使用密钥管理服务存储密码
5. 定期轮换证书
6. 监控证书过期时间
7. 备份密钥库文件到安全位置

### 团队协作
1. 私钥文件不会提交到Git
2. 新成员需要自行生成或获取证书
3. 可以参考本文档生成新证书

---

## 故障排查

### 问题: 找不到证书文件

**解决方案**:
```powershell
# 检查证书是否存在
Test-Path ssl/keystore.p12
Test-Path ssl/private.key
Test-Path ssl/certificate.crt

# 如果不存在，重新生成（见上方"重新生成证书"章节）
```

### 问题: SSL握手失败

**解决方案**:
```powershell
# 检查证书文件是否存在
Test-Path ssl/keystore.p12

# 验证密钥库密码
keytool -list -keystore ssl/keystore.p12 -storepass changeit
```

### 问题: 路径解析失败

**解决方案**:
```powershell
# 确认当前工作目录
pwd

# 验证路径
Resolve-Path ssl/keystore.p12
```

### 问题: JWT签名失败

**解决方案**:
```powershell
# 检查私钥文件
Get-Content ssl/private.key | Select-Object -First 2

# 验证JWK配置
# 查看日志中的 JWK 加载信息
```

### 问题: 证书过期

**解决方案**:
```powershell
# 查看证书详细信息
keytool -list -v -keystore ssl/keystore.p12 -storepass changeit
```

---

## 验证清单

部署SSL配置后，请验证以下项目：

- [ ] 证书文件已复制到项目目录
- [ ] iam-auth-server 配置已更新
- [ ] iam-gateway 配置已更新
- [ ] iam-admin-server 配置已更新
- [ ] iam-bff-server 配置已更新
- [ ] .gitignore 已更新保护私钥
- [ ] enable-https.ps1 脚本已更新
- [ ] 所有文档已更新

---

## 相关配置引用

- [AuthorizationServerConfig.java](file:///d:/VsCodeProject/iam-platform/iam-auth-server/src/main/java/iam/platform/auth/infrastructure/config/AuthorizationServerConfig.java) - JWK配置
- [JwkProperties.java](file:///d:/VsCodeProject/iam-platform/iam-auth-server/src/main/java/iam/platform/auth/infrastructure/config/JwkProperties.java) - JWK属性类

## 相关文档

- [docs/index.md](file:///d:/VsCodeProject/iam-platform/docs/index.md) - 项目文档索引
