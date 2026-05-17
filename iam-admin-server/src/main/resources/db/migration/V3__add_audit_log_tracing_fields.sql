-- Add tracing and source identification fields to audit log table
ALTER TABLE t_audit_log 
    ADD COLUMN event_id VARCHAR(36),
    ADD COLUMN source_service VARCHAR(50),
    ADD COLUMN trace_id VARCHAR(64);

-- Add indexes for new fields to support efficient querying
CREATE INDEX idx_audit_event_id ON t_audit_log(event_id);
CREATE INDEX idx_audit_source_service ON t_audit_log(source_service);
CREATE INDEX idx_audit_trace_id ON t_audit_log(trace_id);

-- Add comments for documentation
COMMENT ON COLUMN t_audit_log.event_id IS 'Unique event identifier for deduplication (UUID)';
COMMENT ON COLUMN t_audit_log.source_service IS 'Source service name (e.g., iam-admin-service, iam-auth-service)';
COMMENT ON COLUMN t_audit_log.trace_id IS 'Distributed tracing correlation ID for request tracking';
