# SAML 2.0 集成文档

## 📋 目录

- [概述](#概述)
- [架构设计](#架构设计)
- [功能特性](#功能特性)
- [快速开始](#快速开始)
- [配置说明](#配置说明)
- [API接口](#api接口)
- [集成指南](#集成指南)
- [安全考虑](#安全考虑)
- [故障排查](#故障排查)
- [开发指南](#开发指南)
- [参考资源](#参考资源)

---

## 概述

IAM平台现已集成 **OpenSAML 4.3.2** 库，提供完整的企业级 SAML 2.0 IdP（Identity Provider）支持。通过SAML协议，企业可以实现跨域单点登录（SSO），与各类Service Provider（SP）无缝集成。

### 核心价值

- ✅ **企业级SSO** - 支持企业内外部应用的统一认证
- ✅ **标准兼容** - 完全遵循SAML 2.0规范
- ✅ **开箱即用** - 提供完整的元数据生成和断言构建能力
- ✅ **灵活配置** - 支持多种NameID格式和绑定方式

---

## 架构设计

### 组件架构

```
┌─────────────────────────────────────────────────────────┐
│                   SAML SSO Flow                         │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  Service Provider          IAM Auth Server              │
│       (SP)                  (IdP)                       │
│                                                         │
│   1. AuthnRequest ──────────────────────►               │
│       (HTTP-Redirect/POST)                              │
│                                                         │
│                        2. Login Page                    │
│                           ◄───────────                  │
│                                                         │
│   3. User Credentials ────────────────►                 │
│                                                         │
│                        4. Authenticate                  │
│                           + Build Assertion             │
│                                                         │
│   5. SAML Response ◄──────────────────                  │
│      (Auto-submit Form)                                 │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

### 核心组件

```iam.platform.auth
├── interfaces.web
│   └── SamlSsoController          # SAML HTTP端点
├── application.service
│   ├── SamlMetadataGenerator      # 元数据生成
│   └── SamlAssertionBuilder       # 断言构建
├── infrastructure.config
│   ├── OpenSamlConfig             # OpenSAML初始化
│   └── SamlProperties             # 配置属性
└── domain
    ├── model.entity               # 领域模型
    └── repository                 # 数据访问
```

---

## 功能特性

### ✨ 1. SAML元数据生成

**端点**: `GET /saml/metadata`

**描述**: 动态生成符合SAML 2.0规范的IdP元数据XML，供Service Provider配置使用。

**响应类型**: `application/xml`

**特性**:
- 自动生成唯一Entity ID
- 支持多种NameID格式
- 支持HTTP-Redirect和HTTP-POST绑定
- 可配置元数据有效期和缓存时间

**示例响应**:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<EntityDescriptor entityID="https://sso.example.com/saml/metadata" 
                  ID="_uuid" 
                  validUntil="2027-05-17T...">
  <IDPSSODescriptor WantAuthnRequestsSigned="true" 
                    protocolSupportEnumeration="urn:oasis:names:tc:SAML:2.0:protocol">
    <NameIDFormat>urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress</NameIDFormat>
    <NameIDFormat>urn:oasis:names:tc:SAML:2.0:nameid-format:persistent</NameIDFormat>
    <NameIDFormat>urn:oasis:names:tc:SAML:2.0:nameid-format:transient</NameIDFormat>
    <SingleSignOnService Binding="urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect" 
                         Location="https://sso.example.com/saml/sso"/>
    <SingleSignOnService Binding="urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST" 
                         Location="https://sso.example.com/saml/sso"/>
  </IDPSSODescriptor>
</EntityDescriptor>
```

### 🔐 2. SAML SSO认证流程

**端点**: 
- `GET /saml/sso` - 显示SAML登录页面
- `POST /saml/sso` - 处理认证并生成SAML断言

**请求参数**:

| 参数 | 方法 | 必需 | 说明 |
|------|------|------|------|
| `acsUrl` | GET/POST | ✅ | Assertion Consumer Service URL |
| `relayState` | GET/POST | ❌ | 状态传递参数 |
| `username` | POST | ✅ | 用户账号 |
| `password` | POST | ✅ | 用户密码 |

### 📦 3. SAML断言构建

**服务**: `SamlAssertionBuilder`

**生成内容**:
- SAML Response包装
- SAML Assertion主体
  - Subject（NameID）
  - Conditions（有效期、受众限制）
  - AuthnStatement（认证信息）
  - AttributeStatement（用户属性）
- Base64编码输出

---

## 快速开始

### 前置条件

- Java 21+
- Maven 3.8+
- Spring Boot 3.2.5+
- 已配置PostgreSQL数据库

### 步骤1: 验证依赖

项目已自动引入OpenSAML依赖，无需手动添加：

```xml
<!-- 父POM中已配置 -->
<dependency>
    <groupId>org.opensaml</groupId>
    <artifactId>opensaml-core</artifactId>
</dependency>
<dependency>
    <groupId>org.opensaml</groupId>
    <artifactId>opensaml-saml-api</artifactId>
</dependency>
<dependency>
    <groupId>org.opensaml</groupId>
    <artifactId>opensaml-saml-impl</artifactId>
</dependency>
```

### 步骤2: 配置SAML参数

编辑 `application-dev.yml`:

```yaml
sso:
  saml:
    entity-id: "https://your-domain.com/saml/metadata"
    sso-url: "https://your-domain.com/saml/sso"
    assertion-validity-minutes: 5
    sign-assertions: false  # 生产环境建议开启
```

### 步骤3: 启动服务

```bash
cd iam-auth-server
mvn spring-boot:run
```

### 步骤4: 测试元数据端点

```bash
curl http://localhost:8081/saml/metadata | xmllint --format -
```

**预期输出**:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<EntityDescriptor entityID="https://your-domain.com/saml/metadata" ...>
  <IDPSSODescriptor ...>
    ...
  </IDPSSODescriptor>
</EntityDescriptor>
```

---

## 配置说明

### 完整配置示例

```yaml
sso:
  saml:
    # IdP实体ID（唯一标识符）
    entity-id: "https://sso.yourcompany.com/saml/metadata"
    
    # SSO服务URL
    sso-url: "https://sso.yourcompany.com/saml/sso"
    
    # 断言有效期（分钟）
    assertion-validity-minutes: 5
    
    # 签名算法
    signature-algorithm: "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256"
    
    # NameID格式
    name-id-format: "urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress"
    
    # 是否签名断言（生产环境建议开启）
    sign-assertions: false
    
    # 签名密钥配置（sign-assertions=true时需要）
    signing-key-path: "/path/to/keystore.p12"
    signing-key-password: "your-password"
```

### 配置项说明

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `entity-id` | String | `https://sso.example.com/saml/metadata` | IdP唯一标识 |
| `sso-url` | String | `https://sso.example.com/saml/sso` | SSO服务地址 |
| `assertion-validity-minutes` | int | `5` | 断言有效期（分钟） |
| `signature-algorithm` | String | `rsa-sha256` | 签名算法 |
| `name-id-format` | String | `emailAddress` | NameID格式 |
| `sign-assertions` | boolean | `true` | 是否签名断言 |
| `signing-key-path` | String | - | 密钥库路径 |
| `signing-key-password` | String | - | 密钥库密码 |

---

## API接口

### 1. 获取IdP元数据

**请求**:
```http
GET /saml/metadata
Accept: application/xml
```

**响应**: `200 OK` (Content-Type: application/xml)

**示例**:
```bash
curl -X GET http://localhost:8081/saml/metadata
```

---

### 2. 发起SAML SSO（GET）

**请求**:
```http
GET /saml/sso?acsUrl={ACS_URL}&relayState={STATE}
```

**参数**:
- `acsUrl` (必需): SP的断言消费服务URL
- `relayState` (可选): 状态参数，会在响应中原样返回

**响应**: `200 OK` (HTML登录页面)

**示例**:
```bash
curl "http://localhost:8081/saml/sso?acsUrl=https://sp.example.com/acs&relayState=xyz"
```

---

### 3. 处理SAML SSO（POST）

**请求**:
```http
POST /saml/sso
Content-Type: application/x-www-form-urlencoded

username=user@example.com&password=secret&acsUrl=https://sp.example.com/acs&relayState=xyz
```

**响应**: `200 OK` (HTML自动提交表单)

响应内容包含一个自动提交的表单，会将SAML Response POST到SP的ACS URL。

---

## 集成指南

### 与常见SP集成

#### 1. Salesforce

**步骤**:
1. 在Salesforce中启用SAML
2. 导入IAM平台的元数据: `https://your-domain.com/saml/metadata`
3. 配置ACS URL: `https://yourcompany.my.salesforce.com/?saml=1`
4. 设置Entity ID: `https://saml.salesforce.com`
5. 配置NameID格式: `emailAddress`

#### 2. Google Workspace

**步骤**:
1. 进入Google Admin控制台
2. 导航到 Security > Set up single sign-on (SSO) for SAML applications
3. 上传IAM平台元数据或手动配置:
   - ACS URL: `https://www.google.com/a/yourdomain.com/acs`
   - Entity ID: `https://accounts.google.com/samlrp/metadata?entityID=your_domain`
4. 配置属性映射

#### 3. Microsoft Azure AD

**步骤**:
1. 在Azure AD中创建企业应用
2. 选择 "SAML-based Sign-on"
3. 配置:
   - Identifier: IAM平台的entity-id
   - Reply URL: IAM平台的sso-url
4. 下载Azure AD元数据并配置到IAM平台

#### 4. 自定义SP集成

**SP端配置**:
```xml
<!-- SP的元数据示例 -->
<SPSSODescriptor protocolSupportEnumeration="urn:oasis:names:tc:SAML:2.0:protocol">
  <AssertionConsumerService 
    Binding="urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST"
    Location="https://sp.example.com/saml/acs"/>
</SPSSODescriptor>
```

**发起认证请求**:
```java
// SP端发起SAML请求示例
String acsUrl = "https://sp.example.com/saml/acs";
String relayState = "original-page-url";
String idpUrl = "https://iam.example.com/saml/sso";

// 重定向到IdP
response.sendRedirect(idpUrl + "?acsUrl=" + 
    URLEncoder.encode(acsUrl, "UTF-8") + 
    "&relayState=" + relayState);
```

**处理SAML响应**:
```java
// SP端接收SAML Response
@PostMapping("/saml/acs")
public void handleSamlResponse(
    @RequestParam String SAMLResponse,
    @RequestParam(required = false) String RelayState) {
    
    // 1. Base64解码
    String decodedAssertion = new String(Base64.getDecoder().decode(SAMLResponse));
    
    // 2. 验证签名（如果启用）
    // 3. 提取用户信息
    // 4. 创建本地会话
    // 5. 重定向到RelayState
}
```

---

## 安全考虑

### 🔒 生产环境安全建议

#### 1. 启用断言签名

```yaml
sso:
  saml:
    sign-assertions: true
    signing-key-path: "/secure/path/keystore.p12"
    signing-key-password: "${SAML_KEY_PASSWORD}"  # 使用环境变量
```

**生成密钥库**:
```bash
keytool -genkeypair \
  -alias saml-signing \
  -keyalg RSA \
  -keysize 2048 \
  -validity 3650 \
  -keystore saml-keystore.p12 \
  -storetype PKCS12 \
  -storepass your-password
```

#### 2. 启用HTTPS

SAML断言包含敏感用户信息，**必须**使用HTTPS传输：

```yaml
server:
  ssl:
    enabled: true
    key-store: classpath:ssl-keystore.p12
    key-store-password: ${SSL_PASSWORD}
```

#### 3. 配置CORS策略

限制允许的SP域名：

```java
@Configuration
public class SamlSecurityConfig {
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
            "https://sp1.example.com",
            "https://sp2.example.com"
        ));
        // ...
    }
}
```

#### 4. 断言有效期控制

设置合理的断言有效期，避免重放攻击：

```yaml
sso:
  saml:
    assertion-validity-minutes: 3  # 生产环境建议3-5分钟
```

### 安全最佳实践

✅ **DO**:
- 始终使用HTTPS
- 启用断言签名
- 定期轮换签名密钥
- 设置合理的断言有效期
- 验证SP的ACS URL白名单
- 记录所有SAML认证日志

❌ **DON'T**:
- 不要在HTTP环境下使用SAML
- 不要硬编码密钥密码
- 不要设置过长的断言有效期
- 不要信任未签名的断言
- 不要在日志中记录敏感信息

---

## 故障排查

### 常见问题

#### 1. 元数据端点返回404

**症状**: 访问 `/saml/metadata` 返回404错误

**原因**: OpenSAML未正确初始化

**解决方案**:
```bash
# 检查启动日志
grep "OpenSAML" logs/application.log

# 预期输出:
# Initializing OpenSAML library...
# OpenSAML initialized successfully
```

#### 2. 断言验证失败

**症状**: SP报告断言验证失败

**排查步骤**:
1. 检查Entity ID是否匹配
2. 验证时间同步（NTP）
3. 检查签名配置
4. 查看断言内容:

```bash
# 解码SAML Response
echo "BASE64_ENCODED_RESPONSE" | base64 -d | xmllint --format -
```

#### 3. NameID格式不匹配

**症状**: SP无法识别NameID

**解决方案**: 确认SP支持的NameID格式:
```yaml
# 当前实现支持的格式
urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress
urn:oasis:names:tc:SAML:2.0:nameid-format:persistent
urn:oasis:names:tc:SAML:2.0:nameid-format:transient
```

#### 4. ACS URL错误

**症状**: 断言发送到错误的URL

**检查**:
- 确认请求中的 `acsUrl` 参数
- 验证SP配置的ACS URL
- 检查URL编码

### 调试技巧

#### 启用详细日志

```yaml
logging:
  level:
    iam.platform.auth.application.service.SamlMetadataGenerator: DEBUG
    iam.platform.auth.application.service.SamlAssertionBuilder: DEBUG
    iam.platform.auth.interfaces.web.SamlSsoController: DEBUG
```

#### 测试元数据生成

```bash
# 运行单元测试
mvn test -Dtest=SamlMetadataGeneratorTest

# 查看生成的元数据
curl http://localhost:8081/saml/metadata | tee metadata.xml

# 验证XML格式
xmllint --noout metadata.xml && echo "XML valid" || echo "XML invalid"
```

---

## 开发指南

### 扩展开发

#### 添加自定义属性

编辑 `SamlAssertionBuilder.java`:

```java
// 在AttributeStatement中添加自定义属性
<saml2:Attribute Name="department" NameFormat="urn:oasis:names:tc:SAML:2.0:attrname-format:basic">
    <saml2:AttributeValue xsi:type="xs:string">%s</saml2:AttributeValue>
</saml2:Attribute>
```

#### 实现XML签名

```java
public String signAssertion(String assertionXml) {
    // 1. 加载密钥
    Credential signingCredential = loadSigningCredential();
    
    // 2. 创建签名器
    Signature signature = buildSignature(signingCredential);
    
    // 3. 签署断言
    signXMLObject(assertion, signature);
    
    return serializeAssertion(assertion);
}
```

#### 支持Artifact Binding

```java
@GetMapping("/saml/artifact")
public String resolveArtifact(
    @RequestParam String SAMLart,
    @RequestParam String provider) {
    // 实现Artifact解析逻辑
    return resolveSAMLArtifact(SAMLart);
}
```

### 单元测试

```java
@SpringBootTest
class SamlSsoIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void testSamlMetadataEndpoint() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            "/saml/metadata", String.class);
        
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("EntityDescriptor"));
    }
    
    @Test
    void testSamlSsoFlow() {
        // 1. 请求登录页面
        // 2. 提交认证
        // 3. 验证SAML Response
    }
}
```

### 性能优化

1. **元数据缓存**: 元数据可缓存，避免重复生成
2. **连接池**: 配置HTTP连接池用于SP通信
3. **异步处理**: 大批量断言处理可使用异步

---

## 参考资源

### 规范文档

- [SAML 2.0 Core Specification](https://docs.oasis-open.org/security/saml/v2.0/saml-core-2.0-os.pdf)
- [SAML 2.0 Bindings](https://docs.oasis-open.org/security/saml/v2.0/saml-bindings-2.0-os.pdf)
- [SAML 2.0 Profiles](https://docs.oasis-open.org/security/saml/v2.0/saml-profiles-2.0-os.pdf)
- [SAML 2.0 Metadata](https://docs.oasis-open.org/security/saml/v2.0/saml-metadata-2.0-os.pdf)

### OpenSAML

- [OpenSAML 4.x Documentation](https://wiki.shibboleth.net/confluence/display/OS30/Home)
- [OpenSAML Java API](https://shibboleth.net/api/opensaml-java/)
- [Shibboleth Maven Repository](https://build.shibboleth.net/maven/releases)

### 相关工具

- [SAML Validator](https://www.samltool.com/validate_response.php)
- [SAML Decoder](https://www.samltool.com/decode.php)
- [XML Signature Validator](https://www.samltool.com/validate_signature.php)

### 最佳实践

- [NIST SAML Guidelines](https://csrc.nist.gov/publications/detail/sp/800-63/final)
- [SAML Security Considerations](https://docs.oasis-open.org/security/saml/v2.0/saml-sec-consider-2.0-os.pdf)

---

## 版本历史

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.2.0 | 2026-05-17 | 集成OpenSAML 4.3.2，实现完整的SAML 2.0支持 |

---

**文档维护**: IAM平台开发团队  
**最后更新**: 2026-05-17
