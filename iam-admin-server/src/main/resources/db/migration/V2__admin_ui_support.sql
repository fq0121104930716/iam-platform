-- ============================================================
-- V2: Admin UI Support - Multi-Tenant Refactoring & Platform Menus
-- ============================================================
-- Changes:
-- 1. Rename t_person → t_user
-- 2. Create t_user_tenant_mapping (replace TenantAccount)
-- 3. Create t_user_role_mapping (replace TenantAccountRoleMapping)
-- 4. Create t_platform_menu and t_tenant_menu_config
-- 5. Create t_application_resource (replace t_application_permission)
-- 6. Create t_application_tenant_mapping
-- 7. Insert platform management tenant
-- 8. Insert platform menu seed data
-- 9. Migrate existing data
-- ============================================================

-- ============================================================
-- Step 1: Rename t_person → t_user
-- ============================================================

ALTER TABLE t_person RENAME TO t_user;
ALTER INDEX uk_person_code RENAME TO uk_user_code;
ALTER INDEX uk_person_username RENAME TO uk_user_username;
ALTER INDEX uk_person_email RENAME TO uk_user_email;
ALTER INDEX uk_person_phone RENAME TO uk_user_phone;

-- Rename triggers
ALTER TRIGGER update_t_person_updated_at ON t_user RENAME TO update_t_user_updated_at;

-- Rename t_person_external_login table and its columns
ALTER TABLE t_person_external_login RENAME TO t_user_external_login;
ALTER TABLE t_user_external_login RENAME COLUMN person_id TO user_id;

-- Rename indexes
ALTER INDEX idx_person_external_login_person RENAME TO idx_user_external_login_user;
ALTER INDEX idx_person_external_login_provider RENAME TO idx_user_external_login_provider;
ALTER INDEX uk_person_provider RENAME TO uk_user_provider;

-- Update foreign key constraint
ALTER TABLE t_user_external_login
    DROP CONSTRAINT IF EXISTS t_person_external_login_person_id_fkey;
ALTER TABLE t_user_external_login
    ADD CONSTRAINT t_user_external_login_user_id_fkey
    FOREIGN KEY (user_id) REFERENCES t_user(id);

-- Rename trigger after table rename
ALTER TRIGGER update_t_person_external_login_updated_at ON t_user_external_login RENAME TO update_t_user_external_login_updated_at;

-- Update comments
COMMENT ON TABLE t_user IS 'User table (global identity)';
COMMENT ON COLUMN t_user.username IS 'Login username (globally unique)';
COMMENT ON COLUMN t_user.email IS 'Email (globally unique, nullable)';
COMMENT ON COLUMN t_user.phone IS 'Phone number (globally unique, nullable)';
COMMENT ON TABLE t_user_external_login IS 'User external login (third-party OAuth2)';
COMMENT ON COLUMN t_user_external_login.user_id IS 'Associated user ID';

-- ============================================================
-- Step 2: Create t_user_tenant_mapping (replace TenantAccount)
-- ============================================================

CREATE TABLE t_user_tenant_mapping (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES t_user(id),
    tenant_id       BIGINT NOT NULL REFERENCES t_tenant(id),
    account_code    VARCHAR(50),
    employee_no     VARCHAR(50),
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    preferred_language VARCHAR(10) DEFAULT 'zh-CN',
    timezone        VARCHAR(50) DEFAULT 'Asia/Shanghai',
    joined_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    left_at         TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    
    CONSTRAINT uk_user_tenant UNIQUE (user_id, tenant_id),
    CONSTRAINT uk_user_tenant_account_code UNIQUE (tenant_id, account_code),
    CONSTRAINT uk_user_tenant_employee_no UNIQUE (tenant_id, employee_no)
);

CREATE INDEX idx_user_tenant_user ON t_user_tenant_mapping(user_id);
CREATE INDEX idx_user_tenant_tenant ON t_user_tenant_mapping(tenant_id);
CREATE INDEX idx_user_tenant_status ON t_user_tenant_mapping(status);

CREATE TRIGGER update_t_user_tenant_mapping_updated_at
    BEFORE UPDATE ON t_user_tenant_mapping
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

COMMENT ON TABLE t_user_tenant_mapping IS 'User-Tenant direct mapping (replaces TenantAccount)';
COMMENT ON COLUMN t_user_tenant_mapping.account_code IS 'Account code within tenant';
COMMENT ON COLUMN t_user_tenant_mapping.employee_no IS 'Employee number within tenant';

-- ============================================================
-- Step 3: Create t_user_role_mapping (replace TenantAccountRoleMapping)
-- ============================================================

CREATE TABLE t_user_role_mapping (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES t_user(id),
    tenant_id       BIGINT NOT NULL REFERENCES t_tenant(id),
    role_id         BIGINT NOT NULL REFERENCES t_role(id),
    assigned_at     TIMESTAMP NOT NULL DEFAULT NOW(),
    assigned_by     BIGINT REFERENCES t_user(id),
    
    CONSTRAINT uk_user_role_tenant UNIQUE (user_id, tenant_id, role_id)
);

CREATE INDEX idx_user_role_user ON t_user_role_mapping(user_id);
CREATE INDEX idx_user_role_tenant ON t_user_role_mapping(tenant_id);
CREATE INDEX idx_user_role_role ON t_user_role_mapping(role_id);

COMMENT ON TABLE t_user_role_mapping IS 'User-Role mapping (direct association with tenant context)';

-- ============================================================
-- Step 4: Create platform menu tables
-- ============================================================

CREATE TABLE t_platform_menu (
    id              BIGSERIAL PRIMARY KEY,
    menu_code       VARCHAR(50) NOT NULL UNIQUE,
    menu_name       VARCHAR(100) NOT NULL,
    icon            VARCHAR(50),
    path            VARCHAR(100),
    sort_order      INTEGER DEFAULT 0,
    parent_id       BIGINT REFERENCES t_platform_menu(id),
    description     VARCHAR(500),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TRIGGER update_t_platform_menu_updated_at
    BEFORE UPDATE ON t_platform_menu
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

COMMENT ON TABLE t_platform_menu IS 'Platform function menu definitions';

CREATE TABLE t_tenant_menu_config (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL REFERENCES t_tenant(id),
    menu_id         BIGINT NOT NULL REFERENCES t_platform_menu(id),
    enabled         BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    
    CONSTRAINT uk_tenant_menu UNIQUE (tenant_id, menu_id)
);

CREATE INDEX idx_tenant_menu_tenant ON t_tenant_menu_config(tenant_id);
CREATE INDEX idx_tenant_menu_menu ON t_tenant_menu_config(menu_id);

COMMENT ON TABLE t_tenant_menu_config IS 'Tenant menu configuration (which platform menus are visible to tenant)';

-- ============================================================
-- Step 5: Create t_application_resource (enhanced application permissions)
-- ============================================================

CREATE TABLE t_application_resource (
    id              BIGSERIAL PRIMARY KEY,
    application_id  BIGINT NOT NULL REFERENCES t_application(id),
    resource_code   VARCHAR(100) NOT NULL,
    resource_name   VARCHAR(100) NOT NULL,
    resource_type   VARCHAR(20) NOT NULL,
    icon            VARCHAR(50),
    path            VARCHAR(200),
    api_path        VARCHAR(200),
    api_method      VARCHAR(10),
    sort_order      INTEGER DEFAULT 0,
    parent_id       BIGINT REFERENCES t_application_resource(id),
    description     VARCHAR(500),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    
    CONSTRAINT uk_app_resource_code UNIQUE (application_id, resource_code)
);

CREATE INDEX idx_app_resource_app ON t_application_resource(application_id);
CREATE INDEX idx_app_resource_type ON t_application_resource(resource_type);
CREATE INDEX idx_app_resource_parent ON t_application_resource(parent_id);

CREATE TRIGGER update_t_application_resource_updated_at
    BEFORE UPDATE ON t_application_resource
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

COMMENT ON TABLE t_application_resource IS 'Application resources (menu, button, API)';
COMMENT ON COLUMN t_application_resource.resource_code IS 'Resource code (e.g. app:oa:user:read)';
COMMENT ON COLUMN t_application_resource.resource_type IS 'Resource type: MENU/BUTTON/API';
COMMENT ON COLUMN t_application_resource.path IS 'Frontend route path (for MENU)';
COMMENT ON COLUMN t_application_resource.api_path IS 'Backend API path (for API)';
COMMENT ON COLUMN t_application_resource.api_method IS 'HTTP method (GET/POST/PUT/DELETE)';

-- ============================================================
-- Step 6: Create t_application_tenant_mapping
-- ============================================================

CREATE TABLE t_application_tenant_mapping (
    id              BIGSERIAL PRIMARY KEY,
    application_id  BIGINT NOT NULL REFERENCES t_application(id),
    tenant_id       BIGINT NOT NULL REFERENCES t_tenant(id),
    enabled         BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    
    CONSTRAINT uk_app_tenant UNIQUE (application_id, tenant_id)
);

CREATE INDEX idx_app_tenant_app ON t_application_tenant_mapping(application_id);
CREATE INDEX idx_app_tenant_tenant ON t_application_tenant_mapping(tenant_id);

COMMENT ON TABLE t_application_tenant_mapping IS 'Application-Tenant mapping (which apps are available to which tenants)';

-- ============================================================
-- Step 7: Insert platform management tenant
-- ============================================================

INSERT INTO t_tenant (id, tenant_code, tenant_name, status, max_users, created_at, updated_at)
VALUES (0, 'iam-platform', 'IAM Platform Management', 'ACTIVE', 10, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- Reset tenant sequence
SELECT setval('t_tenant_id_seq', (SELECT COALESCE(MAX(id), 0) FROM t_tenant WHERE id > 0));

-- ============================================================
-- Step 8: Insert platform menu seed data
-- ============================================================

INSERT INTO t_platform_menu (menu_code, menu_name, icon, path, sort_order) VALUES
    ('dashboard', '仪表盘', 'Odometer', '/dashboard', 1),
    ('tenant-management', '租户管理', 'OfficeBuilding', '/tenants', 2),
    ('user-management', '用户管理', 'User', '/users', 3),
    ('organization-management', '组织管理', 'School', '/organizations', 4),
    ('application-management', '应用管理', 'Grid', '/applications', 5),
    ('role-permission', '角色权限', 'Key', '/roles', 6),
    ('audit-log', '审计日志', 'Document', '/audit', 7),
    ('session-management', '会话管理', 'Monitor', '/sessions', 8),
    ('system-settings', '系统设置', 'Setting', '/settings', 9)
ON CONFLICT (menu_code) DO NOTHING;

-- Assign all platform menus to platform management tenant
INSERT INTO t_tenant_menu_config (tenant_id, menu_id, enabled)
SELECT 
    (SELECT id FROM t_tenant WHERE tenant_code = 'iam-platform'),
    id,
    TRUE
FROM t_platform_menu
ON CONFLICT DO NOTHING;

-- ============================================================
-- Step 9: Migrate existing data from TenantAccount to UserTenantMapping
-- ============================================================

-- Migrate t_tenant_account → t_user_tenant_mapping
INSERT INTO t_user_tenant_mapping (
    user_id, 
    tenant_id, 
    account_code, 
    employee_no, 
    status, 
    preferred_language, 
    timezone,
    joined_at, 
    left_at,
    created_at,
    updated_at
)
SELECT 
    ta.person_id,
    ta.tenant_id,
    ta.account_code,
    ta.employee_no,
    ta.status,
    ta.preferred_language,
    ta.timezone,
    ta.joined_at,
    ta.left_at,
    ta.created_at,
    ta.updated_at
FROM t_tenant_account ta
ON CONFLICT (user_id, tenant_id) DO NOTHING;

-- Migrate t_tenant_account_role_mapping → t_user_role_mapping
INSERT INTO t_user_role_mapping (
    user_id,
    tenant_id,
    role_id,
    assigned_at,
    assigned_by
)
SELECT 
    ta.person_id,
    ta.tenant_id,
    tarm.role_id,
    tarm.assigned_at,
    CASE 
        WHEN tarm.assigned_by LIKE 'person-%' THEN SUBSTRING(tarm.assigned_by FROM 7)::BIGINT
        ELSE NULL
    END
FROM t_tenant_account_role_mapping tarm
JOIN t_tenant_account ta ON tarm.tenant_account_id = ta.id
ON CONFLICT (user_id, tenant_id, role_id) DO NOTHING;

-- Migrate t_tenant_account_organization_mapping (update FK reference)
-- Note: This table still references t_tenant_account, we need to update it
ALTER TABLE t_tenant_account_organization_mapping 
    RENAME COLUMN tenant_account_id TO user_tenant_mapping_id;

-- Update the foreign key to reference the new table
ALTER TABLE t_tenant_account_organization_mapping
    DROP CONSTRAINT t_tenant_account_organization_mapping_tenant_account_id_fkey;

ALTER TABLE t_tenant_account_organization_mapping
    ADD CONSTRAINT t_ta_org_mapping_user_tenant_id_fkey
    FOREIGN KEY (user_tenant_mapping_id)
    REFERENCES t_user_tenant_mapping(id)
    ON DELETE CASCADE;

-- Update the mapping IDs to point to new user_tenant_mapping
UPDATE t_tenant_account_organization_mapping taom
SET user_tenant_mapping_id = utm.id
FROM t_tenant_account ta
JOIN t_user_tenant_mapping utm ON ta.person_id = utm.user_id AND ta.tenant_id = utm.tenant_id
WHERE taom.user_tenant_mapping_id = ta.id;

COMMENT ON TABLE t_tenant_account_organization_mapping IS 'User-Organization mapping (via UserTenantMapping)';
COMMENT ON COLUMN t_tenant_account_organization_mapping.user_tenant_mapping_id IS 'Associated UserTenantMapping ID';

-- ============================================================
-- Step 10: Add dashboard and session permissions
-- ============================================================

INSERT INTO t_resource_permission (tenant_id, permission_code, permission_name, resource_type, action, description) VALUES
    (NULL, 'dashboard:read', '查看仪表盘', 'dashboard', 'READ', '查看管理控制台仪表盘'),
    (NULL, 'session:read', '查看会话', 'session', 'READ', '查看SSO会话信息'),
    (NULL, 'session:delete', '管理会话', 'session', 'DELETE', '强制用户下线')
ON CONFLICT (tenant_id, permission_code) DO NOTHING;

-- Assign new permissions to ROLE_ADMIN
INSERT INTO t_role_permission (role_id, permission_id)
SELECT 
    r.id,
    p.id
FROM t_role r
CROSS JOIN t_resource_permission p
WHERE r.code = 'ROLE_ADMIN'
  AND p.permission_code IN ('dashboard:read', 'session:read', 'session:delete')
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- ============================================================
-- Step 11: Update existing admin user to platform admin
-- ============================================================

-- Link admin user to platform management tenant
INSERT INTO t_user_tenant_mapping (
    user_id, tenant_id, account_code, status, joined_at, created_at, updated_at
)
SELECT 
    u.id, 
    0, -- iam-platform tenant
    u.username, 
    'ACTIVE', 
    NOW(), 
    NOW(), 
    NOW()
FROM t_user u
WHERE u.username = 'admin'
ON CONFLICT (user_id, tenant_id) DO NOTHING;

-- Grant platform admin role to admin user (if ROLE_PLATFORM_ADMIN exists)
-- Note: This will be created in a later migration or manually
INSERT INTO t_user_role_mapping (user_id, tenant_id, role_id, assigned_at)
SELECT 
    u.id,
    0,
    r.id,
    NOW()
FROM t_user u
CROSS JOIN t_role r
WHERE u.username = 'admin' 
  AND r.code = 'ROLE_ADMIN'
ON CONFLICT (user_id, tenant_id, role_id) DO NOTHING;

-- ============================================================
-- Verification Queries (for testing)
-- ============================================================

-- Verify table renames
-- SELECT COUNT(*) FROM t_user;
-- SELECT COUNT(*) FROM t_user_tenant_mapping;
-- SELECT COUNT(*) FROM t_user_role_mapping;

-- Verify platform tenant
-- SELECT * FROM t_tenant WHERE tenant_code = 'iam-platform';

-- Verify platform menus
-- SELECT * FROM t_platform_menu ORDER BY sort_order;

-- Verify menu config for platform tenant
-- SELECT pm.menu_code, pm.menu_name, tmc.enabled 
-- FROM t_tenant_menu_config tmc
-- JOIN t_platform_menu pm ON tmc.menu_id = pm.id
-- JOIN t_tenant t ON tmc.tenant_id = t.id
-- WHERE t.tenant_code = 'iam-platform';
