-- ============================================================
-- Complete IAM Platform Database Schema Initialization
-- Merged from V1, V2, V3, V5, V6 migration files
-- ============================================================
-- This script combines all migrations into a single initialization script
-- for fresh installations. For existing databases, use individual migration files.
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

-- t_user - User table (renamed from t_person in V2)
CREATE TABLE t_user (
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(50) NOT NULL UNIQUE,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255),  -- Made nullable in V5, migrated to t_user_credential
    nickname        VARCHAR(100),
    avatar_url      VARCHAR(512),
    phone           VARCHAR(20),
    provider        VARCHAR(50),
    provider_user_id VARCHAR(255),
    phone_verified  BOOLEAN NOT NULL DEFAULT FALSE,
    email_verified  BOOLEAN NOT NULL DEFAULT FALSE,
    enabled         BOOLEAN NOT NULL DEFAULT TRUE,
    account_locked  BOOLEAN NOT NULL DEFAULT FALSE,
    user_code       VARCHAR(50) NOT NULL UNIQUE,  -- Added in V6
    last_login_at   TIMESTAMP,                     -- Added in V6
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_user_username ON t_user(username);
CREATE INDEX idx_user_email ON t_user(email);
CREATE INDEX idx_user_phone ON t_user(phone);
CREATE INDEX idx_user_provider ON t_user(provider, provider_user_id);
CREATE UNIQUE INDEX uk_user_code ON t_user(user_code);  -- Added in V6

CREATE TRIGGER update_t_user_updated_at
    BEFORE UPDATE ON t_user
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

COMMENT ON TABLE t_user IS 'User table (global identity)';
COMMENT ON COLUMN t_user.username IS 'Login username (globally unique)';
COMMENT ON COLUMN t_user.email IS 'Email (globally unique, nullable)';
COMMENT ON COLUMN t_user.password_hash IS 'BCrypt hashed password (legacy, migrated to t_user_credential)';
COMMENT ON COLUMN t_user.nickname IS 'Display name';
COMMENT ON COLUMN t_user.avatar_url IS 'Avatar URL';
COMMENT ON COLUMN t_user.phone IS 'Phone number for SMS verification';
COMMENT ON COLUMN t_user.provider IS 'OAuth2 provider (dingtalk, wechat, local, etc.)';
COMMENT ON COLUMN t_user.provider_user_id IS 'Third-party platform user ID';
COMMENT ON COLUMN t_user.phone_verified IS 'Phone number verified flag';
COMMENT ON COLUMN t_user.email_verified IS 'Email verified flag';
COMMENT ON COLUMN t_user.enabled IS 'Account enabled flag';
COMMENT ON COLUMN t_user.account_locked IS 'Account locked flag';
COMMENT ON COLUMN t_user.user_code IS 'User code (e.g. USER-000001)';
COMMENT ON COLUMN t_user.last_login_at IS 'Last login timestamp';

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

-- t_user_external_login - User external login (third-party OAuth2)
CREATE TABLE t_user_external_login (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT NOT NULL REFERENCES t_user(id),
    provider            VARCHAR(50) NOT NULL,
    provider_user_id    VARCHAR(255) NOT NULL,
    access_token        TEXT,
    refresh_token       TEXT,
    expires_at          TIMESTAMP,
    last_used_at        TIMESTAMP,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_user_provider UNIQUE (user_id, provider, provider_user_id)
);

CREATE INDEX idx_user_external_login_user ON t_user_external_login(user_id);
CREATE INDEX idx_user_external_login_provider ON t_user_external_login(provider, provider_user_id);

CREATE TRIGGER update_t_user_external_login_updated_at
    BEFORE UPDATE ON t_user_external_login
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

COMMENT ON TABLE t_user_external_login IS 'User external login (third-party OAuth2)';
COMMENT ON COLUMN t_user_external_login.user_id IS 'Associated user ID';
COMMENT ON COLUMN t_user_external_login.provider IS 'OAuth2 provider (e.g. google, github, wechat)';
COMMENT ON COLUMN t_user_external_login.provider_user_id IS 'User ID from provider';
COMMENT ON COLUMN t_user_external_login.access_token IS 'Access token (encrypted)';
COMMENT ON COLUMN t_user_external_login.refresh_token IS 'Refresh token (encrypted)';

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
-- Multi-Tenant Tables: t_tenant, t_user_tenant_mapping
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

-- t_user_tenant_mapping - User-Tenant direct mapping (replaces TenantAccount from V2)
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

-- t_user_organization_mapping (renamed from t_tenant_account_organization_mapping in V2)
CREATE TABLE t_user_organization_mapping (
    id                  BIGSERIAL PRIMARY KEY,
    user_tenant_mapping_id   BIGINT NOT NULL REFERENCES t_user_tenant_mapping(id),
    organization_id     BIGINT NOT NULL REFERENCES t_organization(id),
    is_primary          BOOLEAN NOT NULL DEFAULT FALSE,
    position            VARCHAR(100),
    joined_org_at       TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_user_org_mapping UNIQUE (user_tenant_mapping_id, organization_id)
);

CREATE INDEX idx_user_org_mapping_user_tenant ON t_user_organization_mapping(user_tenant_mapping_id);
CREATE INDEX idx_user_org_mapping_organization ON t_user_organization_mapping(organization_id);

-- Ensure only one primary organization per user-tenant mapping
CREATE UNIQUE INDEX uk_user_org_mapping_primary
    ON t_user_organization_mapping(user_tenant_mapping_id)
    WHERE is_primary = true;

COMMENT ON TABLE t_user_organization_mapping IS 'User-Organization mapping (via UserTenantMapping)';
COMMENT ON COLUMN t_user_organization_mapping.user_tenant_mapping_id IS 'Associated UserTenantMapping ID';
COMMENT ON COLUMN t_user_organization_mapping.is_primary IS 'Whether this is the primary organization';
COMMENT ON COLUMN t_user_organization_mapping.position IS 'Position title within organization';

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

-- t_application_resource - Application resources (menu, button, API) - from V2
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

-- t_application_tenant_mapping - from V2
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

-- t_user_role_mapping - User-Role mapping (direct association with tenant context) - from V2
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
-- Platform Menu Tables - from V2
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
-- User Credential Table - from V5
-- ============================================================

CREATE TABLE t_user_credential (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT NOT NULL REFERENCES t_user(id) ON DELETE CASCADE,
    credential_type     VARCHAR(30) NOT NULL,
    credential_value    TEXT NOT NULL,
    algorithm           VARCHAR(30) DEFAULT 'BCRYPT',
    is_primary          BOOLEAN NOT NULL DEFAULT FALSE,
    expires_at          TIMESTAMP,
    last_used_at        TIMESTAMP,
    status              VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    description         VARCHAR(255),
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by          VARCHAR(100) DEFAULT 'system',

    CONSTRAINT chk_credential_type CHECK (credential_type IN ('PASSWORD', 'CERTIFICATE')),
    CONSTRAINT chk_credential_status CHECK (status IN ('ACTIVE', 'EXPIRED', 'REVOKED'))
);

CREATE INDEX idx_cred_user_id ON t_user_credential(user_id);
CREATE INDEX idx_cred_user_type ON t_user_credential(user_id, credential_type);
CREATE INDEX idx_cred_expires ON t_user_credential(expires_at)
    WHERE expires_at IS NOT NULL AND status = 'ACTIVE';

-- Unique constraint: each user can have only one primary credential per type
CREATE UNIQUE INDEX uk_user_type_primary ON t_user_credential(user_id, credential_type)
    WHERE is_primary = TRUE;

CREATE TRIGGER update_t_user_credential_updated_at
    BEFORE UPDATE ON t_user_credential
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

COMMENT ON TABLE t_user_credential IS 'User authentication credentials - stores persistent credentials (passwords, certificates)';
COMMENT ON COLUMN t_user_credential.user_id IS 'Associated user ID';
COMMENT ON COLUMN t_user_credential.credential_type IS 'Credential type: PASSWORD, CERTIFICATE';
COMMENT ON COLUMN t_user_credential.credential_value IS 'Encrypted credential value (BCrypt hash for passwords, PEM for certificates)';
COMMENT ON COLUMN t_user_credential.algorithm IS 'Encryption algorithm used: BCRYPT, SHA256, RSA, etc.';
COMMENT ON COLUMN t_user_credential.is_primary IS 'Whether this is the primary credential for its type';
COMMENT ON COLUMN t_user_credential.expires_at IS 'Credential expiration time (NULL = never expires)';
COMMENT ON COLUMN t_user_credential.last_used_at IS 'Last time this credential was used for authentication';
COMMENT ON COLUMN t_user_credential.status IS 'Credential status: ACTIVE, EXPIRED, REVOKED';

-- ============================================================
-- Application Permission Table
-- ============================================================

CREATE TABLE t_application_permission (
    id              BIGSERIAL PRIMARY KEY,
    application_id  BIGINT NOT NULL REFERENCES t_application(id) ON DELETE CASCADE,
    permission_code VARCHAR(100) NOT NULL,
    permission_name VARCHAR(200) NOT NULL,
    resource_type   VARCHAR(100) NOT NULL,
    action          VARCHAR(50) NOT NULL,
    description     VARCHAR(500),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_app_permission_code UNIQUE (application_id, permission_code)
);

CREATE INDEX idx_app_permission_app ON t_application_permission(application_id);

CREATE TRIGGER update_t_application_permission_updated_at
    BEFORE UPDATE ON t_application_permission
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

COMMENT ON TABLE t_application_permission IS 'Application-level permission definitions';
COMMENT ON COLUMN t_application_permission.application_id IS 'Associated application ID';
COMMENT ON COLUMN t_application_permission.permission_code IS 'Permission code (e.g. app:oa:user:read)';
COMMENT ON COLUMN t_application_permission.resource_type IS 'Resource type: MENU, BUTTON, API';
COMMENT ON COLUMN t_application_permission.action IS 'Action: READ, WRITE, DELETE, EXPORT, APPROVE, EXECUTE';

-- ============================================================
-- Note: Audit log tables are managed in iam-audit-server module
-- See: iam-audit-server/src/main/resources/db/migration/V1__audit_schema_initialization.sql
-- ============================================================

-- ============================================================
-- Seed Data: Default Roles
-- ============================================================

INSERT INTO t_role (code, name, description, is_system) VALUES
    ('ROLE_USER', '普通用户', 'Default role for all registered users', TRUE),
    ('ROLE_ADMIN', '管理员', 'Administrator role with full permissions', TRUE)
ON CONFLICT (code) DO NOTHING;

-- ============================================================
-- Seed Data: Default Tenants
-- ============================================================

-- Insert default tenant
INSERT INTO t_tenant (id, tenant_code, tenant_name, status, created_at, updated_at)
VALUES (1, 'default', '默认租户', 'ACTIVE', NOW(), NOW())
ON CONFLICT DO NOTHING;

-- Insert platform management tenant (from V2)
INSERT INTO t_tenant (id, tenant_code, tenant_name, status, max_users, created_at, updated_at)
VALUES (0, 'iam-platform', 'IAM Platform Management', 'ACTIVE', 10, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- Reset tenant sequence (ensure it starts after the max ID)
SELECT setval('t_tenant_id_seq', (SELECT COALESCE(MAX(id), 0) FROM t_tenant), true);

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
(26, NULL, 'audit:export', '导出审计日志', 'audit', 'EXPORT', '导出审计日志'),

-- Dashboard and session permissions (from V2)
(27, NULL, 'dashboard:read', '查看仪表盘', 'dashboard', 'READ', '查看管理控制台仪表盘'),
(28, NULL, 'session:read', '查看会话', 'session', 'READ', '查看SSO会话信息'),
(29, NULL, 'session:delete', '管理会话', 'session', 'DELETE', '强制用户下线')
ON CONFLICT (tenant_id, permission_code) DO NOTHING;

-- Reset permission sequence (ensure it starts after the max ID)
SELECT setval('t_resource_permission_id_seq', (SELECT COALESCE(MAX(id), 0) FROM t_resource_permission), true);

-- ============================================================
-- Seed Data: Admin User
-- ============================================================

-- Insert admin user (BCrypt hash of "Admin@123")
INSERT INTO t_user (
    id, user_code, username, email, password_hash, nickname, 
    enabled, account_locked, email_verified, provider, 
    last_login_at, created_at, updated_at
)
VALUES (
    1, 'USER-000001', 'admin', 'admin@example.com',
    '$2b$10$dHn1Kr8cwvaUWaBu/0xcYukFiBsWEVCaABGfkG8bKv8o9R4eLzww.',
    'Administrator', true, false, true, 'local',
    NULL, NOW(), NOW()
) ON CONFLICT (username) DO NOTHING;

-- Reset user sequence (ensure it starts after the max ID)
SELECT setval('t_user_id_seq', (SELECT COALESCE(MAX(id), 0) FROM t_user), true);

-- Link admin to ROLE_ADMIN (legacy)
INSERT INTO t_user_role (user_id, role_id)
SELECT u.id, r.id FROM t_user u, t_role r
WHERE u.username = 'admin' AND r.code = 'ROLE_ADMIN'
ON CONFLICT (user_id, role_id) DO NOTHING;

-- ============================================================
-- Seed Data: Admin User Credential (from V5)
-- ============================================================

-- Migrate admin user password to credential table
INSERT INTO t_user_credential (
    user_id, credential_type, credential_value, algorithm, is_primary, 
    status, created_at, updated_at, created_by
)
SELECT 
    id, 'PASSWORD', password_hash, 'BCRYPT', TRUE, 'ACTIVE', 
    created_at, updated_at, 'system'
FROM t_user
WHERE username = 'admin' AND password_hash IS NOT NULL
ON CONFLICT DO NOTHING;

-- ============================================================
-- Seed Data: Admin User-Tenant Mapping (from V2)
-- ============================================================

-- Link admin user to default tenant
INSERT INTO t_user_tenant_mapping (
    user_id, tenant_id, account_code, status, joined_at, created_at, updated_at
)
SELECT 
    u.id, 1, u.username, 'ACTIVE', NOW(), NOW(), NOW()
FROM t_user u
WHERE u.username = 'admin'
ON CONFLICT (user_id, tenant_id) DO NOTHING;

-- Link admin user to platform management tenant
INSERT INTO t_user_tenant_mapping (
    user_id, tenant_id, account_code, status, joined_at, created_at, updated_at
)
SELECT 
    u.id, 0, u.username, 'ACTIVE', NOW(), NOW(), NOW()
FROM t_user u
WHERE u.username = 'admin'
ON CONFLICT (user_id, tenant_id) DO NOTHING;

-- Grant admin role to admin user for default tenant
INSERT INTO t_user_role_mapping (user_id, tenant_id, role_id, assigned_at)
SELECT 
    u.id, 1, r.id, NOW()
FROM t_user u
CROSS JOIN t_role r
WHERE u.username = 'admin' AND r.code = 'ROLE_ADMIN'
ON CONFLICT (user_id, tenant_id, role_id) DO NOTHING;

-- Grant admin role to admin user for platform tenant
INSERT INTO t_user_role_mapping (user_id, tenant_id, role_id, assigned_at)
SELECT 
    u.id, 0, r.id, NOW()
FROM t_user u
CROSS JOIN t_role r
WHERE u.username = 'admin' AND r.code = 'ROLE_ADMIN'
ON CONFLICT (user_id, tenant_id, role_id) DO NOTHING;

-- ============================================================
-- Seed Data: Platform Menus (from V2)
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