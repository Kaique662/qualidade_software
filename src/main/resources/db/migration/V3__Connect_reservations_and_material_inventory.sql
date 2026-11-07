CREATE TABLE reservation (
    id BIGSERIAL PRIMARY KEY,
    resource_type VARCHAR(16) NOT NULL CHECK (resource_type IN ('ROOM', 'PROFESSOR', 'MATERIAL')),
    resource_id VARCHAR(64) NOT NULL,
    requester_id VARCHAR(64) NOT NULL,
    requester_role VARCHAR(24) NOT NULL,
    restricted BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(16) NOT NULL DEFAULT 'SOLICITADA',
    starts_at TIMESTAMPTZ NOT NULL,
    ends_at TIMESTAMPTZ NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 1 CHECK (quantity > 0),
    requested_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    approved_by VARCHAR(64),
    approved_at TIMESTAMPTZ,
    CHECK (ends_at > starts_at)
);

ALTER TABLE reservation
    ADD CONSTRAINT reservation_valid_status
    CHECK (status IN ('SOLICITADA', 'APROVADA', 'EM_USO', 'CONCLUIDA', 'REJEITADA', 'CANCELADA', 'NAO_COMPARECEU'));

ALTER TABLE room_reservation
    ADD COLUMN reservation_id BIGINT REFERENCES reservation(id) ON DELETE CASCADE;

ALTER TABLE teacher_agenda
    ADD COLUMN reservation_id BIGINT REFERENCES reservation(id) ON DELETE CASCADE;

UPDATE room_reservation SET status = 'APROVADA' WHERE status = 'ACEITA';
UPDATE teacher_agenda SET status = 'APROVADA' WHERE status = 'ACEITA';

ALTER TABLE room_reservation ALTER COLUMN status SET DEFAULT 'SOLICITADA';
ALTER TABLE teacher_agenda ALTER COLUMN status SET DEFAULT 'SOLICITADA';
ALTER TABLE room_reservation
    ADD CONSTRAINT room_reservation_valid_status
    CHECK (status IN ('SOLICITADA', 'APROVADA', 'EM_USO', 'CONCLUIDA', 'REJEITADA', 'CANCELADA', 'NAO_COMPARECEU'));
ALTER TABLE teacher_agenda
    ADD CONSTRAINT teacher_agenda_valid_status
    CHECK (status IN ('SOLICITADA', 'APROVADA', 'EM_USO', 'CONCLUIDA', 'REJEITADA', 'CANCELADA', 'NAO_COMPARECEU'));

ALTER TABLE room_reservation
    DROP CONSTRAINT IF EXISTS room_reservation_resource_id_time_range_excl;
ALTER TABLE room_reservation
    ADD CONSTRAINT room_reservation_no_active_overlap
    EXCLUDE USING gist (resource_id WITH =, time_range WITH &&)
    WHERE (status IN ('SOLICITADA', 'APROVADA', 'EM_USO'));

ALTER TABLE teacher_agenda
    DROP CONSTRAINT IF EXISTS teacher_agenda_professor_id_time_range_excl;
ALTER TABLE teacher_agenda
    ADD CONSTRAINT teacher_agenda_no_active_overlap
    EXCLUDE USING gist (professor_id WITH =, time_range WITH &&)
    WHERE (status IN ('SOLICITADA', 'APROVADA', 'EM_USO'));

CREATE TABLE material_reservation (
    id BIGSERIAL PRIMARY KEY,
    reservation_id BIGINT NOT NULL UNIQUE REFERENCES reservation(id) ON DELETE CASCADE,
    resource_id VARCHAR(64) NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    time_range TSTZRANGE NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'SOLICITADA',
    CHECK (NOT isempty(time_range) AND lower_inc(time_range) AND NOT upper_inc(time_range)),
    CHECK (status IN ('SOLICITADA', 'APROVADA', 'EM_USO', 'CONCLUIDA', 'REJEITADA', 'CANCELADA', 'NAO_COMPARECEU'))
);

CREATE INDEX idx_material_reservation_resource_range
    ON material_reservation USING gist (resource_id, time_range);
CREATE INDEX idx_material_reservation_active
    ON material_reservation (resource_id, status);
CREATE INDEX idx_reservation_resource_status
    ON reservation (resource_id, status);

CREATE TABLE reservation_audit_event (
    id BIGSERIAL PRIMARY KEY,
    reservation_id BIGINT REFERENCES reservation(id) ON DELETE SET NULL,
    resource_id VARCHAR(64) NOT NULL,
    actor_id VARCHAR(64) NOT NULL,
    action VARCHAR(32) NOT NULL,
    outcome VARCHAR(16) NOT NULL CHECK (outcome IN ('SUCCEEDED', 'REJECTED')),
    previous_status VARCHAR(16),
    new_status VARCHAR(16),
    details VARCHAR(500),
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_reservation_audit_resource_date
    ON reservation_audit_event (resource_id, occurred_at DESC);
CREATE INDEX idx_reservation_audit_actor_date
    ON reservation_audit_event (actor_id, occurred_at DESC);