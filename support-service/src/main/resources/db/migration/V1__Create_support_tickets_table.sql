CREATE TABLE support_tickets (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    rental_id BIGINT,
    subject VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    category VARCHAR(30) NOT NULL DEFAULT 'OTHER',
    assigned_to VARCHAR(200),
    resolution_notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_support_tickets_user_id   ON support_tickets(user_id);
CREATE INDEX idx_support_tickets_rental_id ON support_tickets(rental_id);
CREATE INDEX idx_support_tickets_status    ON support_tickets(status);
CREATE INDEX idx_support_tickets_priority  ON support_tickets(priority);
CREATE INDEX idx_support_tickets_created   ON support_tickets(created_at DESC);
