-- ============================================================
-- V1__audit_schema_initialization.sql
-- IAM Audit Server Base Schema
-- ============================================================
-- This script creates the core audit tables for iam-audit-server
-- Migrated from iam-admin-server V1__complete_schema_initialization.sql
-- ============================================================

-- ============================================================
-- Step 1: Create t_audit_log - Core audit log table
-- ============================================================

CREATE TABLE IF NOT EXISTS t_audit_log (
    id                  BIGSERIAL PRIMARY KEY,
    tenant_id           BIGINT,  -- No FK constraint: may reference external tenant table
    user_id             BIGINT,  -- Operator user ID (renamed from person_id)
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
    trace_id            VARCHAR(100),    -- Distributed tracing ID
    span_id             VARCHAR(100),    -- Span ID for request chain
    parent_span_id      VARCHAR(100),    -- Parent span ID
    encrypted_fields    VARCHAR(200),    -- JSON array of encrypted field names
    created_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Indexes for t_audit_log
CREATE INDEX idx_audit_tenant_id ON t_audit_log(tenant_id);
CREATE INDEX idx_audit_user_id ON t_audit_log(user_id);
CREATE INDEX idx_audit_event_type ON t_audit_log(event_type);
CREATE INDEX idx_audit_event_category ON t_audit_log(event_category);
CREATE INDEX idx_audit_resource ON t_audit_log(resource_type, resource_id);
CREATE INDEX idx_audit_result ON t_audit_log(result);
CREATE INDEX idx_audit_created_at ON t_audit_log(created_at);
CREATE INDEX idx_audit_tenant_category_time ON t_audit_log(tenant_id, event_category, created_at);
CREATE INDEX idx_audit_failure ON t_audit_log(result) WHERE result = 'FAILURE';
CREATE INDEX idx_audit_trace_id ON t_audit_log(trace_id);

COMMENT ON TABLE t_audit_log IS 'Audit log table - records all critical operations';
COMMENT ON COLUMN t_audit_log.tenant_id IS 'Tenant ID that owns this audit record (null for system-level operations)';
COMMENT ON COLUMN t_audit_log.user_id IS 'Operator user ID';
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
COMMENT ON COLUMN t_audit_log.trace_id IS 'Distributed tracing ID for request correlation';
COMMENT ON COLUMN t_audit_log.span_id IS 'Span ID for request chain tracing';
COMMENT ON COLUMN t_audit_log.parent_span_id IS 'Parent span ID for hierarchical tracing';
COMMENT ON COLUMN t_audit_log.encrypted_fields IS 'JSON array of encrypted field names';

-- ============================================================
-- Step 2: Create t_alert_rule - Alert rule definitions
-- ============================================================

CREATE TABLE IF NOT EXISTS t_alert_rule (
    id                      BIGSERIAL PRIMARY KEY,
    rule_code               VARCHAR(50) NOT NULL UNIQUE,
    rule_name               VARCHAR(100) NOT NULL,
    event_type_filter       VARCHAR(500),
    condition_expression    TEXT,
    threshold               INTEGER,
    time_window_seconds     INTEGER,
    severity                VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    enabled                 BOOLEAN NOT NULL DEFAULT TRUE,
    notification_channels   VARCHAR(500),
    created_at              TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by              VARCHAR(100)
);

CREATE INDEX idx_alert_rule_enabled ON t_alert_rule(enabled);

COMMENT ON TABLE t_alert_rule IS 'Alert rule definitions for real-time monitoring';
COMMENT ON COLUMN t_alert_rule.rule_code IS 'Unique rule identifier';
COMMENT ON COLUMN t_alert_rule.event_type_filter IS 'Comma-separated event types to monitor';
COMMENT ON COLUMN t_alert_rule.condition_expression IS 'SpEL or JSON condition expression';
COMMENT ON COLUMN t_alert_rule.threshold IS 'Threshold count for triggering alert';
COMMENT ON COLUMN t_alert_rule.time_window_seconds IS 'Time window in seconds for threshold evaluation';
COMMENT ON COLUMN t_alert_rule.severity IS 'Alert severity: LOW, MEDIUM, HIGH, CRITICAL';
COMMENT ON COLUMN t_alert_rule.notification_channels IS 'Comma-separated notification channels: EMAIL, WEBHOOK';

-- ============================================================
-- Step 3: Create t_alert_record - Triggered alert records
-- ============================================================

CREATE TABLE IF NOT EXISTS t_alert_record (
    id                  BIGSERIAL PRIMARY KEY,
    rule_id             BIGINT NOT NULL REFERENCES t_alert_rule(id) ON DELETE CASCADE,
    triggered_at        TIMESTAMP NOT NULL DEFAULT NOW(),
    event_count         INTEGER,
    sample_event_ids    TEXT,
    severity            VARCHAR(20) NOT NULL,
    status              VARCHAR(20) NOT NULL DEFAULT 'NEW',
    notified_at         TIMESTAMP,
    resolved_at         TIMESTAMP,
    resolved_by         VARCHAR(100)
);

CREATE INDEX idx_alert_rule_id ON t_alert_record(rule_id);
CREATE INDEX idx_alert_status ON t_alert_record(status);
CREATE INDEX idx_alert_triggered_at ON t_alert_record(triggered_at);

COMMENT ON TABLE t_alert_record IS 'Triggered alert records';
COMMENT ON COLUMN t_alert_record.sample_event_ids IS 'JSON array of related event IDs';
COMMENT ON COLUMN t_alert_record.status IS 'Alert status: NEW, ACKNOWLEDGED, RESOLVED';

-- ============================================================
-- Step 4: Create t_siem_endpoint - SIEM integration endpoints
-- ============================================================

CREATE TABLE IF NOT EXISTS t_siem_endpoint (
    id                          BIGSERIAL PRIMARY KEY,
    endpoint_name               VARCHAR(100) NOT NULL,
    endpoint_type               VARCHAR(20) NOT NULL,
    endpoint_url                VARCHAR(500),
    auth_config                 TEXT,
    format                      VARCHAR(20) NOT NULL DEFAULT 'JSON',
    enabled                     BOOLEAN NOT NULL DEFAULT FALSE,
    batch_size                  INTEGER DEFAULT 100,
    batch_interval_seconds      INTEGER DEFAULT 30,
    last_export_at              TIMESTAMP
);

CREATE INDEX idx_siem_enabled ON t_siem_endpoint(enabled);

COMMENT ON TABLE t_siem_endpoint IS 'SIEM (Security Information and Event Management) endpoint configurations';
COMMENT ON COLUMN t_siem_endpoint.endpoint_type IS 'Endpoint type: SYSLOG, HTTP, KAFKA';
COMMENT ON COLUMN t_siem_endpoint.auth_config IS 'Encrypted authentication configuration (JSON)';
COMMENT ON COLUMN t_siem_endpoint.format IS 'Export format: CEF, LEEF, JSON';

-- ============================================================
-- Step 5: Create t_compliance_report - Compliance report metadata
-- ============================================================

CREATE TABLE IF NOT EXISTS t_compliance_report (
    id              BIGSERIAL PRIMARY KEY,
    report_code     VARCHAR(50) NOT NULL UNIQUE,
    report_type     VARCHAR(30) NOT NULL,
    period_start    TIMESTAMP,
    period_end      TIMESTAMP,
    generated_at    TIMESTAMP,
    generated_by    VARCHAR(100),
    status          VARCHAR(20) NOT NULL DEFAULT 'GENERATING',
    file_path       VARCHAR(500),
    summary_json    TEXT
);

CREATE INDEX idx_compliance_type ON t_compliance_report(report_type);
CREATE INDEX idx_compliance_status ON t_compliance_report(status);
CREATE INDEX idx_compliance_period ON t_compliance_report(period_start, period_end);

COMMENT ON TABLE t_compliance_report IS 'Compliance report generation metadata';
COMMENT ON COLUMN t_compliance_report.report_type IS 'Report type: SOC2, GDPR, ISO27001, CUSTOM';
COMMENT ON COLUMN t_compliance_report.status IS 'Report status: GENERATING, COMPLETED, FAILED';
COMMENT ON COLUMN t_compliance_report.summary_json IS 'Report summary as JSON';

-- ============================================================
-- Step 6: Create cleanup function for old audit logs
-- ============================================================

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
-- Step 7: Seed Data - Default Alert Rules
-- ============================================================

INSERT INTO t_alert_rule (rule_code, rule_name, event_type_filter, condition_expression, threshold, time_window_seconds, severity, notification_channels, created_by) VALUES
('BRUTE_FORCE_DETECTION', '暴力破解检测', 'LOGIN_FAILURE', '{"type": "count", "dimension": "ip_address"}', 10, 300, 'HIGH', 'EMAIL,WEBHOOK', 'system'),
('ACCOUNT_LOCKOUT_CASCADE', '账户锁定级联', 'ACCOUNT_LOCKED', '{"type": "count"}', 5, 600, 'CRITICAL', 'EMAIL,WEBHOOK', 'system'),
('PRIVILEGE_ESCALATION', '权限提升', 'ROLE_ASSIGN,PERMISSION_CHANGE', '{"type": "any"}', 1, 0, 'HIGH', 'EMAIL,WEBHOOK', 'system'),
('OFF_HOURS_ACCESS', '非工作时间访问', 'LOGIN_SUCCESS', '{"type": "time_range", "start": "22:00", "end": "06:00"}', 1, 0, 'MEDIUM', 'EMAIL', 'system'),
('MASS_DATA_EXPORT', '大量数据导出', 'EXPORT', '{"type": "count", "dimension": "user_id"}', 100, 60, 'HIGH', 'EMAIL,WEBHOOK', 'system')
ON CONFLICT (rule_code) DO NOTHING;
