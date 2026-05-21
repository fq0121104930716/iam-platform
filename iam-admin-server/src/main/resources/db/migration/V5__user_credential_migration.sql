-- ============================================================
-- V5: User Credential Migration
-- Create t_user_credential table and migrate existing password data
-- ============================================================
-- Goals:
-- 1. Create t_user_credential table for persistent credentials
-- 2. Migrate existing t_user.password_hash to t_user_credential
-- 3. Make t_user.password_hash nullable (backward compatible, not deleted yet)
-- ============================================================

-- ============================================================
-- Step 1: Create t_user_credential table
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

-- ============================================================
-- Step 2: Create indexes
-- ============================================================

CREATE INDEX idx_cred_user_id ON t_user_credential(user_id);
CREATE INDEX idx_cred_user_type ON t_user_credential(user_id, credential_type);
CREATE INDEX idx_cred_expires ON t_user_credential(expires_at)
    WHERE expires_at IS NOT NULL AND status = 'ACTIVE';

-- Unique constraint: each user can have only one primary credential per type
CREATE UNIQUE INDEX uk_user_type_primary ON t_user_credential(user_id, credential_type)
    WHERE is_primary = TRUE;

-- ============================================================
-- Step 3: Create updated_at trigger
-- ============================================================

CREATE TRIGGER update_t_user_credential_updated_at
    BEFORE UPDATE ON t_user_credential
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================================
-- Step 4: Data migration - migrate existing password_hash to t_user_credential
-- ============================================================

INSERT INTO t_user_credential (user_id, credential_type, credential_value, algorithm, is_primary, status, created_at, updated_at, created_by)
SELECT id, 'PASSWORD', password_hash, 'BCRYPT', TRUE, 'ACTIVE', created_at, updated_at, 'migration'
FROM t_user
WHERE password_hash IS NOT NULL AND password_hash != ''
ON CONFLICT DO NOTHING;

-- ============================================================
-- Step 5: Backward compatibility - make password_hash nullable
-- Do not delete the column immediately, keep a transition period
-- ============================================================

ALTER TABLE t_user ALTER COLUMN password_hash DROP NOT NULL;

-- ============================================================
-- Step 6: Table comments
-- ============================================================

COMMENT ON TABLE t_user_credential IS 'User authentication credentials - stores persistent credentials (passwords, certificates)';
COMMENT ON COLUMN t_user_credential.user_id IS 'Associated user ID';
COMMENT ON COLUMN t_user_credential.credential_type IS 'Credential type: PASSWORD, CERTIFICATE';
COMMENT ON COLUMN t_user_credential.credential_value IS 'Encrypted credential value (BCrypt hash for passwords, PEM for certificates)';
COMMENT ON COLUMN t_user_credential.algorithm IS 'Encryption algorithm used: BCRYPT, SHA256, RSA, etc.';
COMMENT ON COLUMN t_user_credential.is_primary IS 'Whether this is the primary credential for its type';
COMMENT ON COLUMN t_user_credential.expires_at IS 'Credential expiration time (NULL = never expires)';
COMMENT ON COLUMN t_user_credential.last_used_at IS 'Last time this credential was used for authentication';
COMMENT ON COLUMN t_user_credential.status IS 'Credential status: ACTIVE, EXPIRED, REVOKED';
