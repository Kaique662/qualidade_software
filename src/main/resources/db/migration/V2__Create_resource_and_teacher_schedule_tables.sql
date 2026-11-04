CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE TABLE room_reservation (
    id BIGSERIAL PRIMARY KEY,
    resource_id VARCHAR(64) NOT NULL,
    time_range TSTZRANGE NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'ACEITA',
    EXCLUDE USING gist (resource_id WITH =, time_range WITH &&)
);

CREATE TABLE teacher_agenda (
    id BIGSERIAL PRIMARY KEY,
    professor_id VARCHAR(64) NOT NULL,
    time_range TSTZRANGE NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'ACEITA',
    EXCLUDE USING gist (professor_id WITH =, time_range WITH &&)
);

CREATE INDEX idx_room_reservation_resource ON room_reservation (resource_id);
CREATE INDEX idx_teacher_agenda_professor ON teacher_agenda (professor_id);
