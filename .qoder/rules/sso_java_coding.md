---
glob: "*.java"
---
# IAM Platform项目Java编码与API设计规范

## 1. 编码规范

### 1.1 命名规范

| 类型 | 规范 | 示例 |
|------|------|------|
| 包名 | 全小写，点分隔 | `iam.platform.application.service` |
| 类名 | PascalCase | `UserApplicationService` |
| 方法名 | camelCase | `createUser()` |
| 变量名 | camelCase | `userName` |
| 常量名 | UPPER_SNAKE_CASE | `MAX_LOGIN_ATTEMPTS` |
| JPA实体后缀 | PO | `UserPO` |
| DTO请求后缀 | Request | `CreateUserRequest` |
| DTO响应后缀 | Response | `UserResponse` |
| 仓储实现后缀 | Impl | `UserRepositoryImpl` |
| 异常后缀 | Exception | `UserNotFoundException` |

### 1.2 代码风格

- 使用 Lombok 减少样板代码（`@RequiredArgsConstructor`、`@Getter`、`@Data`、`@Builder` 等）
- 使用 MapStruct 进行对象映射（Assembler 层）
- 方法长度不超过 **50 行**
- 类长度不超过 **500 行**
- 单元测试覆盖率不低于 **80%**

### 1.3 Lombok 使用规范

```java
// 领域实体 - 使用 @Data + @Builder + @NoArgsConstructor + @AllArgsConstructor
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class User { ... }

// JPA 实体 - 不使用 @Data，显式定义 getter/setter（避免 Hibernate 懒加载问题）
@Entity
@RequiredArgsConstructor
@Getter
public class UserPO {
    // 手动定义 setter
    public void setUsername(String username) { ... }
}

// DTO - 使用 @Data + @Builder + @NoArgsConstructor + @AllArgsConstructor
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CreateUserRequest { ... }
```

### 1.4 MapStruct 使用规范

```java
@Mapper(componentModel = "spring")
public interface UserAssembler {
    UserResponse toResponse(User domain);
    List<UserResponse> toResponseList(List<User> domains);
    // 需要自定义映射时使用 @Mapping
}
```

### 1.5 注释规范

```java
/**
 * 用户应用服务 - 处理用户管理相关业务逻辑
 *
 * @author Developer
 * @version 1.0
 * @since 2024-01-01
 */
@Service
@RequiredArgsConstructor
public class UserApplicationService {

    /**
     * 创建新用户
     *
     * @param request 创建用户请求
     * @return 创建成功的用户响应
     * @throws UserAlreadyExistsException 当用户名或邮箱已存在时抛出
     */
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        // 实现逻辑
    }
}
```

## 2. API设计规范

### 2.1 RESTful设计原则

- 使用名词复数表示资源集合（`/users`、`/roles`、`/clients`）
- 使用 HTTP 方法表示操作类型（GET / POST / PUT / DELETE）
- 返回标准的 HTTP 状态码

### 2.2 请求/响应格式

```json
// 成功响应示例
{
  "code": 200,
  "message": "Success",
  "data": {
    "id": 1,
    "username": "admin",
    "email": "admin@example.com"
  },
  "timestamp": "2024-05-08 10:30:45"
}

// 创建成功响应示例
{
  "code": 201,
  "message": "Created",
  "data": { "id": 1, "name": "New Client" }
}

// 错误响应示例
{
  "code": 400,
  "message": "Validation Error",
  "errors": [
    {
      "field": "username",
      "message": "Username is required"
    }
  ],
  "timestamp": "2024-05-08 10:30:45"
}
```

### 2.3 状态码规范

| 状态码 | 含义 |
|--------|------|
| 200 OK | 请求成功 |
| 201 Created | 资源创建成功 |
| 400 Bad Request | 请求参数错误 |
| 401 Unauthorized | 未认证 |
| 403 Forbidden | 无权限 |
| 404 Not Found | 资源不存在 |
| 409 Conflict | 资源冲突（如用户名已存在） |
| 500 Internal Server Error | 服务器内部错误 |

### 2.4 Controller 模板

```java
@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "User management API")
public class UserController {
    private final UserApplicationService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create user")
    public ApiResponse<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        return ApiResponse.created(service.createUser(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public ApiResponse<UserResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(service.getUser(id));
    }
}
```
