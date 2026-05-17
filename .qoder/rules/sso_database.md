---
glob: "*.sql"
---
# IAM Platform项目数据库迁移规范

## 1. 数据库迁移

使用 Flyway 管理数据库版本，迁移脚本位于 `src/main/resources/db/migration/`：

- V1: 初始化 schema（t_role, t_user, t_user_role, t_oauth2_client, t_oauth2_authorization, t_oauth2_authorization_consent）
- V2: 初始化默认角色（ROLE_USER, ROLE_ADMIN）

DEV 环境 seed 数据位于 `src/main/resources/db/dev/mock_data.sql`

**注意：** Flyway 必须显式配置 `encoding: UTF-8` 以支持中文 SQL 脚本。

## 2. 迁移脚本编写规范

- 迁移脚本一旦应用不可修改，如需调整应新建迁移脚本
- 脚本命名遵循 Flyway 默认约定：`V<版本号>__<描述>.sql`
- SQL 关键字使用大写，表名/列名使用 snake_case
- 表名统一使用 `t_` 前缀
- 每个迁移脚本应包含正向 SQL，必要时补充回滚说明注释
- 包含中文内容的 SQL 脚本必须使用 UTF-8 编码保存

## 3. 数据库设计规范

### 3.1 通用列规范

- 所有表必须包含 `created_at`（TIMESTAMP, NOT NULL, DEFAULT NOW()）
- 所有表必须包含 `updated_at`（TIMESTAMP, NOT NULL, DEFAULT NOW()，通过触发器自动更新）
- 主键统一使用 `id`（BIGSERIAL 或 VARCHAR(36) UUID）

### 3.2 触发器

所有业务表使用统一的 `update_updated_at_column()` 触发器函数：

```sql
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
```

### 3.3 注释规范

所有表和列必须添加 `COMMENT ON` 注释：

```sql
COMMENT ON TABLE t_user IS '用户表';
COMMENT ON COLUMN t_user.username IS '用户名';
```

### 3.4 索引

- 唯一约束列自动创建唯一索引
- 频繁查询的列添加普通索引
- 联合查询使用复合索引

### 3.5 表关系

- 使用外键约束保证数据一致性
- 多对多关系使用中间表（如 `t_user_role`）
- 级联删除使用 `ON DELETE CASCADE`
