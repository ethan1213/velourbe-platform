CREATE TABLE maintenance_issues (
    id BIGSERIAL PRIMARY KEY,
    scooter_id BIGINT NOT NULL,
    issue_type VARCHAR(120) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'REPORTED',
    resolution_notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    resolved_at TIMESTAMP
);

CREATE INDEX idx_maintenance_issues_scooter_id ON maintenance_issues(scooter_id);
CREATE INDEX idx_maintenance_issues_status ON maintenance_issues(status);
CREATE INDEX idx_maintenance_issues_created ON maintenance_issues(created_at DESC);
