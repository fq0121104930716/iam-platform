-- ============================================================
-- V1: Complete SSO OIDC Schema Initialization
-- Merged from V1-V7: Complete schema with all features
-- Tables: t_role, t_user, t_user_role, t_person, t_tenant, 
--         t_tenant_account, t_organization, t_application,
--         t_audit_log, oauth2_authorization, oauth2_authorization_consent
-- Plus: Default roles, admin user, system permissions
-- ============================================================

-- ============================================================
-- Utility: Trigger function for updated_at
-- ============================================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ============================================================
-- Core Tables: t_role, t_user, t_user_role
-- ============================================================

-- t_role - Role table
CREATE TABLE t_role (
    id          BIGSERIAL PRIMARY KEY,
    code        VARCHAR(50) NOT NULL UNIQUE,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    tenant_id   BIGINT,
    role_type   VARCHAR(20) NOT NULL DEFAULT 'SYSTEM',
    is_system   BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_role_tenant_id ON t_role(tenant_id);

CREATE TRIGGER update_t_role_updated_at
    BEFORE UPDATE ON t_role
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

COMMENT ON TABLE t_role IS 'Role table';
COMMENT ON COLUMN t_role.code IS 'Role code (e.g. ROLE_USER, ROLE_ADMIN)';
COMMENT ON COLUMN t_role.name IS 'Role display name';
COMMENT ON COLUMN t_role.description IS 'Role description';
COMMENT ON COLUMN t_role.tenant_id IS 'Role owning tenant (null for global roles)';
COMMENT ON COLUMN t_role.role_type IS 'Role type: SYSTEM/TENANT_CUSTOM';
COMMENT ON COLUMN t_role.is_system IS 'System built-in role (cannot be deleted)';

-- t_user - Legacy user table (kept for compatibility)
CREATE TABLE t_user (
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(50) NOT NULL UNIQUE,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    nickname        VARCHAR(100),
    avatar_url      VARCHAR(512),
    phone           VARCHAR(20),
    provider        VARCHAR(50),
    provider_user_id VARCHAR(255),
    phone_verified  BOOLEAN NOT NULL DEFAULT FALSE,
    email_verified  BOOLEAN NOT NULL DEFAULT FALSE,
    enabled         BOOLEAN NOT NULL DEFAULT TRUE,
    account_locked  BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_user_username ON t_user(username);
CREATE INDEX idx_user_email ON t_user(email);
CREATE INDEX idx_user_phone ON t_user(phone);
CREATE INDEX idx_user_provider ON t_user(provider, provider_user_id);

CREATE TRIGGER update_t_user_updated_at
    BEFORE UPDATE ON t_user
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

COMMENT ON TABLE t_user IS 'User table (legacy, migrated to t_person)';
COMMENT ON COLUMN t_user.username IS 'Username';
COMMENT ON COLUMN t_user.email IS 'Email';
COMMENT ON COLUMN t_user.password_hash IS 'BCrypt hashed password';
COMMENT ON COLUMN t_user.nickname IS 'Display name';
COMMENT ON COLUMN t_user.avatar_url IS 'Avatar URL';
COMMENT ON COLUMN t_user.phone IS 'Phone number for SMS verification';
COMMENT ON COLUMN t_user.provider IS 'OAuth2 provider (dingtalk, wechat, local, etc.)';
COMMENT ON COLUMN t_user.provider_user_id IS 'Third-party platform user ID';
COMMENT ON COLUMN t_user.phone_verified IS 'Phone number verified flag';
COMMENT ON COLUMN t_user.email_verified IS 'Email verified flag';
COMMENT ON COLUMN t_user.enabled IS 'Account enabled flag';
COMMENT ON COLUMN t_user.account_locked IS 'Account locked flag';

-- t_user_role - User-Role join table (legacy)
CREATE TABLE t_user_role (
    user_id BIGINT NOT NULL REFERENCES t_user(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES t_role(id) ON DELETE CASCADE,
    CONSTRAINT uq_user_role UNIQUE (user_id, role_id),
    CONSTRAINT pk_user_role PRIMARY KEY (user_id, role_id)
);

CREATE INDEX idx_user_role_user_id ON t_user_role(user_id);
CREATE INDEX idx_user_role_role_id ON t_user_role(role_id);

COMMENT ON TABLE t_user_role IS 'User-Role relationship table (legacy)';

-- ============================================================
-- OAuth2 Authorization Tables (Spring Authorization Server)
-- ============================================================

-- oauth2_authorization
CREATE TABLE oauth2_authorization (
    id                            VARCHAR(100) NOT NULL,
    registered_client_id          VARCHAR(100) NOT NULL,
    principal_name                VARCHAR(200) NOT NULL,
    authorization_grant_type      VARCHAR(100) NOT NULL,
    authorized_scopes             VARCHAR(1000),
    attributes                    TEXT,
    state                         VARCHAR(500),
    authorization_code_value      TEXT,
    authorization_code_issued_at  TIMESTAMP,
    authorization_code_expires_at TIMESTAMP,
    authorization_code_metadata   TEXT,
    access_token_value            TEXT,
    access_token_issued_at        TIMESTAMP,
    access_token_expires_at       TIMESTAMP,
    access_token_metadata         TEXT,
    access_token_type             VARCHAR(100),
    access_token_scopes           VARCHAR(1000),
    oidc_id_token_value           TEXT,
    oidc_id_token_issued_at       TIMESTAMP,
    oidc_id_token_expires_at      TIMESTAMP,
    oidc_id_token_metadata        TEXT,
    oidc_id_token_claims          TEXT,
    refresh_token_value           TEXT,
    refresh_token_issued_at       TIMESTAMP,
    refresh_token_expires_at      TIMESTAMP,
    refresh_token_metadata        TEXT,
    user_code_value               TEXT,
    user_code_issued_at           TIMESTAMP,
    user_code_expires_at          TIMESTAMP,
    user_code_metadata            TEXT,
    device_code_value             TEXT,
    device_code_issued_at         TIMESTAMP,
    device_code_expires_at        TIMESTAMP,
    device_code_metadata          TEXT,
    PRIMARY KEY (id)
);

CREATE INDEX idx_oauth2_authorization_client_id ON oauth2_authorization(registered_client_id);
CREATE INDEX idx_oauth2_authorization_principal ON oauth2_authorization(principal_name);
CREATE INDEX idx_oauth2_authorization_state ON oauth2_authorization(state);
CREATE INDEX idx_oauth2_authorization_access_token ON oauth2_authorization(access_token_value);
CREATE INDEX idx_oauth2_authorization_refresh_token ON oauth2_authorization(refresh_token_value);

COMMENT ON TABLE oauth2_authorization IS 'Spring Authorization Server authorization storage';

-- oauth2_authorization_consent
CREATE TABLE oauth2_authorization_consent (
    registered_client_id VARCHAR(100) NOT NULL,
    principal_name       VARCHAR(200) NOT NULL,
    authorities          VARCHAR(1000) NOT NULL,
    PRIMARY KEY (registered_client_id, principal_name)
);

COMMENT ON TABLE oauth2_authorization_consent IS 'Spring Authorization Server consent storage';

-- ============================================================
-- Multi-Tenant Tables: t_tenant, t_person, t_tenant_account
-- ============================================================

-- t_tenant - Tenant table
CREATE TABLE t_tenant (
    id              BIGSERIAL PRIMARY KEY,
    tenant_code     VARCHAR(50) NOT NULL UNIQUE,
    tenant_name     VARCHAR(200) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    max_users       INTEGER DEFAULT 100,
    expires_at      TIMESTAMP,
    contact_email   VARCHAR(255),
    contact_phone   VARCHAR(20),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX uk_tenant_code ON t_tenant(tenant_code);

CREATE TRIGGER update_t_tenant_updated_at
    BEFORE UPDATE ON t_tenant
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

COMMENT ON TABLE t_tenant IS 'Tenant table';
COMMENT ON COLUMN t_tenant.tenant_code IS 'Unique tenant code (e.g. company-a)';
COMMENT ON COLUMN t_tenant.tenant_name IS 'Tenant display name';
COMMENT ON COLUMN t_tenant.status IS 'Tenant status: ACTIVE/SUSPENDED/DELETED';
COMMENT ON COLUMN t_tenant.max_users IS 'Maximum number of users allowed';
COMMENT ON COLUMN t_tenant.expires_at IS 'Tenant expiration time';

-- t_person - Person (natural person) table
CREATE TABLE t_person (
    id              BIGSERIAL PRIMARY KEY,
    person_code     VARCHAR(50) NOT NULL UNIQUE,
    username        VARCHAR(100) NOT NULL UNIQUE,
    email           VARCHAR(255),
    phone           VARCHAR(20),
    password_hash   VARCHAR(255) NOT NULL,
    email_verified  BOOLEAN NOT NULL DEFAULT FALSE,
    phone_verified  BOOLEAN NOT NULL DEFAULT FALSE,
    nickname        VARCHAR(100),
    avatar_url      VARCHAR(512),
    enabled         BOOLEAN NOT NULL DEFAULT TRUE,
    account_locked  BOOLEAN NOT NULL DEFAULT FALSE,
    last_login_at   TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX uk_person_code ON t_person(person_code);
CREATE UNIQUE INDEX uk_person_username ON t_person(username);
CREATE UNIQUE INDEX uk_person_email ON t_person(email) WHERE email IS NOT NULL;
CREATE UNIQUE INDEX uk_person_phone ON t_person(phone) WHERE phone IS NOT NULL;

CREATE TRIGGER update_t_person_updated_at
    BEFORE UPDATE ON t_person
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

COMMENT ON TABLE t_person IS 'Person (natural person) table - global identity';
COMMENT ON COLUMN t_person.person_code IS 'Globally unique person code (e.g. PERSON-000001)';
COMMENT ON COLUMN t_person.username IS 'Login username (globally unique)';
COMMENT ON COLUMN t_person.email IS 'Email (globally unique, nullable)';
COMMENT ON COLUMN t_person.phone IS 'Phone number (globally unique, nullable)';
COMMENT ON COLUMN t_person.password_hash IS 'BCrypt hashed password';
COMMENT ON COLUMN t_person.enabled IS 'Whether account is enabled globally';
COMMENT ON COLUMN t_person.account_locked IS 'Whether account is locked';
COMMENT ON COLUMN t_person.last_login_at IS 'Last login time (any tenant)';

-- t_tenant_account - Tenant Account table
CREATE TABLE t_tenant_account (
    id                  BIGSERIAL PRIMARY KEY,
    person_id           BIGINT NOT NULL REFERENCES t_person(id),
    tenant_id           BIGINT NOT NULL REFERENCES t_tenant(id),
    account_code        VARCHAR(50) NOT NULL,
    employee_no         VARCHAR(50),
    status              VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    joined_at           TIMESTAMP NOT NULL DEFAULT NOW(),
    left_at             TIMESTAMP,
    preferred_language  VARCHAR(10) DEFAULT 'zh-CN',
    timezone            VARCHAR(50) DEFAULT 'Asia/Shanghai',
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_tenant_account_code UNIQUE (tenant_id, account_code),
    CONSTRAINT uk_tenant_employee_no UNIQUE (tenant_id, employee_no)
);

CREATE INDEX idx_tenant_account_person_id ON t_tenant_account(person_id);
CREATE INDEX idx_tenant_account_tenant_id ON t_tenant_account(tenant_id);
CREATE INDEX idx_tenant_account_status ON t_tenant_account(status);

CREATE TRIGGER update_t_tenant_account_updated_at
    BEFORE UPDATE ON t_tenant_account
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

COMMENT ON TABLE t_tenant_account IS 'Tenant account table - user identity within a tenant';
COMMENT ON COLUMN t_tenant_account.person_id IS 'Associated person ID';
COMMENT ON COLUMN t_tenant_account.tenant_id IS 'Associated tenant ID';
COMMENT ON COLUMN t_tenant_account.account_code IS 'Account code within tenant (tenant-unique)';
COMMENT ON COLUMN t_tenant_account.employee_no IS 'Employee number within tenant (tenant-unique)';
COMMENT ON COLUMN t_tenant_account.status IS 'Account status: ACTIVE/SUSPENDED/LEFT';
COMMENT ON COLUMN t_tenant_account.joined_at IS 'Time when joined the tenant';
COMMENT ON COLUMN t_tenant_account.left_at IS 'Time when left the tenant';

-- ============================================================
-- Organization Tables
-- ============================================================

-- t_organization - Organization tree table
CREATE TABLE t_organization (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL REFERENCES t_tenant(id),
    org_code        VARCHAR(50) NOT NULL,
    org_name        VARCHAR(200) NOT NULL,
    org_type        VARCHAR(20) NOT NULL DEFAULT 'DEPARTMENT',
    parent_id       BIGINT REFERENCES t_organization(id),
    level           INTEGER NOT NULL DEFAULT 1,
    path            VARCHAR(500) NOT NULL,
    sort_order      INTEGER DEFAULT 0,
    manager_id      BIGINT,
    phone           VARCHAR(20),
    email           VARCHAR(255),
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    description     VARCHAR(500),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_tenant_org_code UNIQUE (tenant_id, org_code)
);

CREATE INDEX idx_organization_tenant_id ON t_organization(tenant_id);
CREATE INDEX idx_organization_parent_id ON t_organization(parent_id);
CREATE INDEX idx_organization_path ON t_organization(path);
CREATE INDEX idx_organization_level ON t_organization(level);
CREATE INDEX idx_organization_manager_id ON t_organization(manager_id);

CREATE TRIGGER update_t_organization_updated_at
    BEFORE UPDATE ON t_organization
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

COMMENT ON TABLE t_organization IS 'Organization tree table';
COMMENT ON COLUMN t_organization.tenant_id IS 'Owning tenant';
COMMENT ON COLUMN t_organization.org_code IS 'Organization code (e.g. DEPT-HR)';
COMMENT ON COLUMN t_organization.org_name IS 'Organization name';
COMMENT ON COLUMN t_organization.org_type IS 'Organization type: COMPANY/DEPARTMENT/TEAM';
COMMENT ON COLUMN t_organization.parent_id IS 'Parent organization ID (null for root)';
COMMENT ON COLUMN t_organization.level IS 'Tree level (root = 1)';
COMMENT ON COLUMN t_organization.path IS 'Materialized path (e.g. /1/5/12)';
COMMENT ON COLUMN t_organization.sort_order IS 'Sort order within siblings';
COMMENT ON COLUMN t_organization.manager_id IS 'Manager tenant_account ID';
COMMENT ON COLUMN t_organization.status IS 'Organization status: ACTIVE/INACTIVE';

-- t_tenant_account_organization_mapping
CREATE TABLE t_tenant_account_organization_mapping (
    id                  BIGSERIAL PRIMARY KEY,
    tenant_account_id   BIGINT NOT NULL REFERENCES t_tenant_account(id),
    organization_id     BIGINT NOT NULL REFERENCES t_organization(id),
    is_primary          BOOLEAN NOT NULL DEFAULT FALSE,
    position            VARCHAR(100),
    joined_org_at       TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_tenant_account_org UNIQUE (tenant_account_id, organization_id)
);

CREATE INDEX idx_ta_org_mapping_tenant_account ON t_tenant_account_organization_mapping(tenant_account_id);
CREATE INDEX idx_ta_org_mapping_organization ON t_tenant_account_organization_mapping(organization_id);

-- Ensure only one primary organization per tenant account
CREATE UNIQUE INDEX uk_ta_org_mapping_primary
    ON t_tenant_account_organization_mapping(tenant_account_id)
    WHERE is_primary = true;

COMMENT ON TABLE t_tenant_account_organization_mapping IS 'TenantAccount-Organization many-to-many mapping';
COMMENT ON COLUMN t_tenant_account_organization_mapping.is_primary IS 'Whether this is the primary organization';
COMMENT ON COLUMN t_tenant_account_organization_mapping.position IS 'Position title within organization';

-- ============================================================
-- Application & Permission Tables
-- ============================================================

-- t_application - Application table (upgraded from OAuth2 Client)
CREATE TABLE t_application (
    id                          BIGSERIAL PRIMARY KEY,
    app_id                      VARCHAR(100) NOT NULL UNIQUE,
    app_secret                  VARCHAR(500),
    app_name                    VARCHAR(200) NOT NULL,
    tenant_id                   BIGINT REFERENCES t_tenant(id),
    app_type                    VARCHAR(20) NOT NULL DEFAULT 'WEB',
    description                 VARCHAR(500),
    logo_url                    VARCHAR(500),
    status                      VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    home_page_url               VARCHAR(500),
    callback_urls               VARCHAR(2000),
    post_logout_redirect_uris   VARCHAR(2000),
    allowed_scopes              VARCHAR(1000) NOT NULL,
    require_proof_key           BOOLEAN NOT NULL DEFAULT FALSE,
    require_authorization_consent BOOLEAN NOT NULL DEFAULT TRUE,
    access_token_ttl_seconds    INTEGER NOT NULL DEFAULT 3600,
    refresh_token_ttl_seconds   INTEGER NOT NULL DEFAULT 86400,
    enabled                     BOOLEAN NOT NULL DEFAULT TRUE,
    created_at                  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX uk_application_app_id ON t_application(app_id);
CREATE INDEX idx_application_tenant_id ON t_application(tenant_id);
CREATE INDEX idx_application_status ON t_application(status);
CREATE INDEX idx_application_app_type ON t_application(app_type);

CREATE TRIGGER update_t_application_updated_at
    BEFORE UPDATE ON t_application
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

COMMENT ON TABLE t_application IS 'Application table (upgraded from OAuth2 Client)';
COMMENT ON COLUMN t_application.app_id IS 'Application unique identifier (formerly client_id)';
COMMENT ON COLUMN t_application.app_secret IS 'Application secret (AES-256-GCM encrypted)';
COMMENT ON COLUMN t_application.tenant_id IS 'Owning tenant ID';
COMMENT ON COLUMN t_application.app_type IS 'Application type: WEB/MOBILE/API/THIRD_PARTY';
COMMENT ON COLUMN t_application.status IS 'Application status: ACTIVE/INACTIVE/REVIEWING/BLOCKED';
COMMENT ON COLUMN t_application.callback_urls IS 'OAuth2 callback URIs (comma-separated)';
COMMENT ON COLUMN t_application.allowed_scopes IS 'Allowed OAuth2 scopes (comma-separated)';

-- t_application_permission - Application-level permissions
CREATE TABLE t_application_permission (
    id                  BIGSERIAL PRIMARY KEY,
    application_id      BIGINT NOT NULL REFERENCES t_application(id),
    permission_code     VARCHAR(100) NOT NULL,
    permission_name     VARCHAR(200) NOT NULL,
    resource_type       VARCHAR(100) NOT NULL,
    action              VARCHAR(50) NOT NULL,
    description         VARCHAR(500),
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_app_permission_code UNIQUE (application_id, permission_code)
);

CREATE INDEX idx_app_permission_application_id ON t_application_permission(application_id);
CREATE INDEX idx_app_permission_resource_type ON t_application_permission(resource_type, action);

CREATE TRIGGER update_t_application_permission_updated_at
    BEFORE UPDATE ON t_application_permission
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

COMMENT ON TABLE t_application_permission IS 'Application-level permissions';
COMMENT ON COLUMN t_application_permission.permission_code IS 'Permission code (e.g. app:user:read)';
COMMENT ON COLUMN t_application_permission.resource_type IS 'Resource type (e.g. user, order, report)';
COMMENT ON COLUMN t_application_permission.action IS 'Action: READ/WRITE/DELETE/EXECUTE';

-- t_resource_permission - Resource permission table
CREATE TABLE t_resource_permission (
    id                  BIGSERIAL PRIMARY KEY,
    tenant_id           BIGINT REFERENCES t_tenant(id),
    permission_code     VARCHAR(100) NOT NULL,
    permission_name     VARCHAR(200) NOT NULL,
    resource_type       VARCHAR(50) NOT NULL,
    action              VARCHAR(20) NOT NULL,
    description         VARCHAR(500),
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_tenant_permission_code UNIQUE (tenant_id, permission_code)
);

CREATE INDEX idx_resource_permission_tenant ON t_resource_permission(tenant_id);
CREATE INDEX idx_resource_permission_resource_type ON t_resource_permission(resource_type);
CREATE INDEX idx_resource_permission_action ON t_resource_permission(action);

CREATE TRIGGER update_t_resource_permission_updated_at
    BEFORE UPDATE ON t_resource_permission
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

COMMENT ON TABLE t_resource_permission IS 'Resource permission table';
COMMENT ON COLUMN t_resource_permission.tenant_id IS 'Permission owning tenant (null for global permissions)';
COMMENT ON COLUMN t_resource_permission.permission_code IS 'Permission code (e.g. user:read, order:write)';
COMMENT ON COLUMN t_resource_permission.permission_name IS 'Permission display name';
COMMENT ON COLUMN t_resource_permission.resource_type IS 'Resource type (e.g. user, application, report)';
COMMENT ON COLUMN t_resource_permission.action IS 'Action: READ/WRITE/DELETE/EXPORT/APPROVE/EXECUTE';

-- t_role_permission - Role-Permission many-to-many mapping
CREATE TABLE t_role_permission (
    id              BIGSERIAL PRIMARY KEY,
    role_id         BIGINT NOT NULL REFERENCES t_role(id) ON DELETE CASCADE,
    permission_id   BIGINT NOT NULL REFERENCES t_resource_permission(id) ON DELETE CASCADE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_role_permission UNIQUE (role_id, permission_id)
);

CREATE INDEX idx_role_permission_role ON t_role_permission(role_id);
CREATE INDEX idx_role_permission_permission ON t_role_permission(permission_id);

COMMENT ON TABLE t_role_permission IS 'Role-Permission many-to-many mapping';

-- t_tenant_account_role_mapping - TenantAccount-Role mapping
CREATE TABLE t_tenant_account_role_mapping (
    id                  BIGSERIAL PRIMARY KEY,
    tenant_account_id   BIGINT NOT NULL REFERENCES t_tenant_account(id),
    role_id             BIGINT NOT NULL REFERENCES t_role(id),
    assigned_at         TIMESTAMP NOT NULL DEFAULT NOW(),
    assigned_by         VARCHAR(100),

    CONSTRAINT uk_tenant_account_role UNIQUE (tenant_account_id, role_id)
);

CREATE INDEX idx_ta_role_mapping_tenant_account ON t_tenant_account_role_mapping(tenant_account_id);
CREATE INDEX idx_ta_role_mapping_role ON t_tenant_account_role_mapping(role_id);

COMMENT ON TABLE t_tenant_account_role_mapping IS 'TenantAccount-Role many-to-many mapping';
COMMENT ON COLUMN t_tenant_account_role_mapping.assigned_at IS 'Role assignment time';
COMMENT ON COLUMN t_tenant_account_role_mapping.assigned_by IS 'Who assigned this role';

-- t_person_external_login - Person external login (third-party)
CREATE TABLE t_person_external_login (
    id                  BIGSERIAL PRIMARY KEY,
    person_id           BIGINT NOT NULL REFERENCES t_person(id),
    provider            VARCHAR(50) NOT NULL,
    provider_user_id    VARCHAR(255) NOT NULL,
    access_token        TEXT,
    refresh_token       TEXT,
    expires_at          TIMESTAMP,
    last_used_at        TIMESTAMP,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_person_provider UNIQUE (person_id, provider, provider_user_id)
);

CREATE INDEX idx_person_external_login_person ON t_person_external_login(person_id);
CREATE INDEX idx_person_external_login_provider ON t_person_external_login(provider, provider_user_id);

CREATE TRIGGER update_t_person_external_login_updated_at
    BEFORE UPDATE ON t_person_external_login
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

COMMENT ON TABLE t_person_external_login IS 'Person external login (third-party OAuth2)';
COMMENT ON COLUMN t_person_external_login.provider IS 'OAuth2 provider (e.g. google, github, wechat)';
COMMENT ON COLUMN t_person_external_login.provider_user_id IS 'User ID from provider';
COMMENT ON COLUMN t_person_external_login.access_token IS 'Access token (encrypted)';
COMMENT ON COLUMN t_person_external_login.refresh_token IS 'Refresh token (encrypted)';

-- ============================================================
-- Audit Log Table
-- ============================================================

-- t_audit_log - Audit log table
CREATE TABLE t_audit_log (
    id                  BIGSERIAL PRIMARY KEY,
    tenant_id           BIGINT REFERENCES t_tenant(id) ON DELETE SET NULL,
    person_id           BIGINT,  -- No FK constraint: person may be deleted
    username            VARCHAR(100),
    event_type          VARCHAR(30) NOT NULL,
    event_category      VARCHAR(20) NOT NULL,
    resource_id         BIGINT,
    resource_type       VARCHAR(50),
    action              VARCHAR(200),
    ip_address          VARCHAR(45),     -- IPv6 max length
    user_agent          VARCHAR(500),
    request_uri         VARCHAR(500),
    request_params      TEXT,            -- JSON-formatted request parameters
    result              VARCHAR(10) NOT NULL,
    error_message       VARCHAR(2000),
    created_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Indexes for t_audit_log
CREATE INDEX idx_audit_tenant_id ON t_audit_log(tenant_id);
CREATE INDEX idx_audit_person_id ON t_audit_log(person_id);
CREATE INDEX idx_audit_event_type ON t_audit_log(event_type);
CREATE INDEX idx_audit_event_category ON t_audit_log(event_category);
CREATE INDEX idx_audit_resource ON t_audit_log(resource_type, resource_id);
CREATE INDEX idx_audit_result ON t_audit_log(result);
CREATE INDEX idx_audit_created_at ON t_audit_log(created_at);
CREATE INDEX idx_audit_tenant_category_time ON t_audit_log(tenant_id, event_category, created_at);

COMMENT ON TABLE t_audit_log IS 'Audit log table - records all critical operations';
COMMENT ON COLUMN t_audit_log.tenant_id IS 'Tenant ID that owns this audit record (null for system-level operations)';
COMMENT ON COLUMN t_audit_log.person_id IS 'Operator person ID';
COMMENT ON COLUMN t_audit_log.username IS 'Operator username';
COMMENT ON COLUMN t_audit_log.event_type IS 'Event type: LOGIN_SUCCESS, ROLE_ASSIGN, etc.';
COMMENT ON COLUMN t_audit_log.event_category IS 'Event category: AUTHENTICATION, AUTHORIZATION, ACCOUNT, ADMINISTRATION, SESSION';
COMMENT ON COLUMN t_audit_log.resource_id IS 'Related resource ID';
COMMENT ON COLUMN t_audit_log.resource_type IS 'Resource type: user, tenant, role, application, organization';
COMMENT ON COLUMN t_audit_log.action IS 'Operation description';
COMMENT ON COLUMN t_audit_log.ip_address IS 'Client IP address';
COMMENT ON COLUMN t_audit_log.user_agent IS 'Client User-Agent header';
COMMENT ON COLUMN t_audit_log.request_uri IS 'Request URI';
COMMENT ON COLUMN t_audit_log.request_params IS 'Request parameters (JSON, sensitive data masked)';
COMMENT ON COLUMN t_audit_log.result IS 'Operation result: SUCCESS/FAILURE';
COMMENT ON COLUMN t_audit_log.error_message IS 'Error message (on failure)';

-- Cleanup function for old audit logs
CREATE OR REPLACE FUNCTION cleanup_old_audit_logs(retention_days INTEGER DEFAULT 180)
RETURNS INTEGER AS $$
DECLARE
    cutoff_date TIMESTAMP;
    deleted_count INTEGER;
BEGIN
    cutoff_date := NOW() - (retention_days || ' days')::INTERVAL;
    
    DELETE FROM t_audit_log WHERE created_at < cutoff_date;
    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    
    RETURN deleted_count;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION cleanup_old_audit_logs IS 'Cleanup expired audit logs, default retention is 180 days';

-- ============================================================
-- Seed Data: Default Roles
-- ============================================================

INSERT INTO t_role (code, name, description, is_system) VALUES
    ('ROLE_USER', '普通用户', 'Default role for all registered users', TRUE),
    ('ROLE_ADMIN', '管理员', 'Administrator role with full permissions', TRUE)
ON CONFLICT (code) DO NOTHING;

-- ============================================================
-- Seed Data: Default Tenant
-- ============================================================

INSERT INTO t_tenant (id, tenant_code, tenant_name, status, created_at, updated_at)
VALUES (1, 'default', '默认租户', 'ACTIVE', NOW(), NOW())
ON CONFLICT DO NOTHING;

-- Reset tenant sequence
SELECT setval('t_tenant_id_seq', (SELECT COALESCE(MAX(id), 0) FROM t_tenant));

-- ============================================================
-- Seed Data: System Permissions
-- ============================================================

INSERT INTO t_resource_permission (id, tenant_id, permission_code, permission_name, resource_type, action, description) VALUES
(1, NULL, 'user:read', '查看用户', 'user', 'READ', '查看用户列表和详情'),
(2, NULL, 'user:write', '编辑用户', 'user', 'WRITE', '创建和编辑用户'),
(3, NULL, 'user:delete', '删除用户', 'user', 'DELETE', '删除用户'),
(4, NULL, 'user:export', '导出用户', 'user', 'EXPORT', '导出用户数据'),

(5, NULL, 'tenant:read', '查看租户', 'tenant', 'READ', '查看租户列表和详情'),
(6, NULL, 'tenant:write', '编辑租户', 'tenant', 'WRITE', '创建和编辑租户'),
(7, NULL, 'tenant:delete', '删除租户', 'tenant', 'DELETE', '删除租户'),
(8, NULL, 'tenant:activate', '激活租户', 'tenant', 'APPROVE', '激活暂停的租户'),
(9, NULL, 'tenant:suspend', '暂停租户', 'tenant', 'APPROVE', '暂停活跃的租户'),

(10, NULL, 'org:read', '查看组织', 'organization', 'READ', '查看组织树'),
(11, NULL, 'org:write', '编辑组织', 'organization', 'WRITE', '创建和编辑组织'),
(12, NULL, 'org:delete', '删除组织', 'organization', 'DELETE', '删除组织'),

(13, NULL, 'role:read', '查看角色', 'role', 'READ', '查看角色列表和详情'),
(14, NULL, 'role:write', '编辑角色', 'role', 'WRITE', '创建和编辑角色'),
(15, NULL, 'role:delete', '删除角色', 'role', 'DELETE', '删除角色'),
(16, NULL, 'role:assign', '分配角色', 'role', 'WRITE', '为用户分配角色'),

(17, NULL, 'app:read', '查看应用', 'application', 'READ', '查看应用列表和详情'),
(18, NULL, 'app:write', '编辑应用', 'application', 'WRITE', '创建和编辑应用'),
(19, NULL, 'app:delete', '删除应用', 'application', 'DELETE', '删除应用'),
(20, NULL, 'app:activate', '激活应用', 'application', 'APPROVE', '激活应用'),
(21, NULL, 'app:block', '封禁应用', 'application', 'APPROVE', '封禁应用'),

(22, NULL, 'permission:read', '查看权限', 'permission', 'READ', '查看权限列表'),
(23, NULL, 'permission:write', '编辑权限', 'permission', 'WRITE', '创建和编辑权限'),
(24, NULL, 'permission:assign', '分配权限', 'permission', 'WRITE', '为角色分配权限'),

(25, NULL, 'audit:read', '查看审计日志', 'audit', 'READ', '查看审计日志'),
(26, NULL, 'audit:export', '导出审计日志', 'audit', 'EXPORT', '导出审计日志')
ON CONFLICT (tenant_id, permission_code) DO NOTHING;

-- Reset permission sequence
SELECT setval('t_resource_permission_id_seq', (SELECT COALESCE(MAX(id), 0) FROM t_resource_permission));

-- ============================================================
-- Seed Data: Admin User (Legacy t_user for compatibility)
-- ============================================================

-- Insert admin user (BCrypt hash of "Admin@123")
INSERT INTO t_user (username, email, password_hash, nickname, enabled, account_locked, email_verified, provider)
VALUES ('admin', 'admin@example.com',
        '$2b$10$dHn1Kr8cwvaUWaBu/0xcYukFiBsWEVCaABGfkG8bKv8o9R4eLzww.',
        'Administrator', true, false, true, 'local')
ON CONFLICT (username) DO NOTHING;

-- Link admin to ROLE_ADMIN (legacy)
INSERT INTO t_user_role (user_id, role_id)
SELECT u.id, r.id FROM t_user u, t_role r
WHERE u.username = 'admin' AND r.code = 'ROLE_ADMIN'
ON CONFLICT (user_id, role_id) DO NOTHING;

-- ============================================================
-- Seed Data: Admin Person & TenantAccount (New Model)
-- ============================================================

-- Insert admin person
INSERT INTO t_person (
    id, person_code, username, email, password_hash,
    nickname, enabled, account_locked, email_verified, created_at, updated_at
)
VALUES (
    1, 'PERSON-000001', 'admin', 'admin@example.com',
    '$2b$10$dHn1Kr8cwvaUWaBu/0xcYukFiBsWEVCaABGfkG8bKv8o9R4eLzww.',
    'Administrator', true, false, true, NOW(), NOW()
) ON CONFLICT (username) DO NOTHING;

-- Reset person sequence
SELECT setval('t_person_id_seq', (SELECT COALESCE(MAX(id), 0) FROM t_person));

-- Insert admin tenant account
INSERT INTO t_tenant_account (
    person_id, tenant_id, account_code, status, joined_at, created_at, updated_at
)
SELECT 
    p.id, 1, p.username, 'ACTIVE', NOW(), NOW(), NOW()
FROM t_person p
WHERE p.username = 'admin'
ON CONFLICT (tenant_id, account_code) DO NOTHING;

-- Link admin tenant account to ROLE_ADMIN
INSERT INTO t_tenant_account_role_mapping (tenant_account_id, role_id, assigned_at)
SELECT 
    ta.id, r.id, NOW()
FROM t_tenant_account ta
CROSS JOIN t_role r
WHERE ta.person_id = 1 AND ta.tenant_id = 1 AND r.code = 'ROLE_ADMIN'
ON CONFLICT (tenant_account_id, role_id) DO NOTHING;

-- ============================================================
-- Assign Permissions to Roles
-- ============================================================

-- ROLE_ADMIN gets all permissions
INSERT INTO t_role_permission (role_id, permission_id)
SELECT
    r.id AS role_id,
    p.id AS permission_id
FROM t_role r
CROSS JOIN t_resource_permission p
WHERE r.code = 'ROLE_ADMIN'
  AND p.tenant_id IS NULL
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- ROLE_USER gets basic read permissions
INSERT INTO t_role_permission (role_id, permission_id)
SELECT
    r.id AS role_id,
    p.id AS permission_id
FROM t_role r
CROSS JOIN t_resource_permission p
WHERE r.code = 'ROLE_USER'
  AND p.tenant_id IS NULL
  AND p.action = 'READ'
ON CONFLICT (role_id, permission_id) DO NOTHING;
