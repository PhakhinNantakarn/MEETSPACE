CREATE TABLE IF NOT EXISTS room_image (
    id VARCHAR(36) PRIMARY KEY,
    content BLOB NOT NULL
);

CREATE TABLE IF NOT EXISTS app_user (
    id VARCHAR(36) PRIMARY KEY,
    email VARCHAR(160) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    role VARCHAR(10) NOT NULL,
    self_registered BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS room (
    id VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    name_th VARCHAR(100) NOT NULL,
    location VARCHAR(120) NOT NULL,
    location_th VARCHAR(120) NOT NULL,
    capacity INT NOT NULL,
    approval BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    amenities VARCHAR(300) NOT NULL DEFAULT '',
    color VARCHAR(20) NOT NULL DEFAULT 'indigo',
    image_url VARCHAR(300) NOT NULL DEFAULT ''
);

CREATE TABLE IF NOT EXISTS booking (
    id VARCHAR(36) PRIMARY KEY,
    room_id VARCHAR(20) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    title VARCHAR(160) NOT NULL,
    booking_date VARCHAR(10) NOT NULL,
    start_time VARCHAR(5) NOT NULL,
    end_time VARCHAR(5) NOT NULL,
    participants INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    series_id VARCHAR(36),
    request_key VARCHAR(80) NOT NULL,
    created_at VARCHAR(40) NOT NULL,
    updated_at VARCHAR(40) NOT NULL,
    reason VARCHAR(300) NOT NULL DEFAULT '',
    CONSTRAINT fk_booking_room FOREIGN KEY (room_id) REFERENCES room(id),
    CONSTRAINT fk_booking_user FOREIGN KEY (user_id) REFERENCES app_user(id),
    CONSTRAINT uq_booking_request UNIQUE (user_id, request_key, booking_date)
);

ALTER TABLE room ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE booking ADD COLUMN IF NOT EXISTS booker VARCHAR(100) NOT NULL DEFAULT '';

CREATE TABLE IF NOT EXISTS booking_event (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    booking_id VARCHAR(36) NOT NULL,
    kind VARCHAR(30) NOT NULL,
    event_at VARCHAR(40) NOT NULL,
    actor VARCHAR(100) NOT NULL,
    CONSTRAINT fk_event_user FOREIGN KEY (user_id) REFERENCES app_user(id),
    CONSTRAINT fk_event_booking FOREIGN KEY (booking_id) REFERENCES booking(id)
);

CREATE INDEX IF NOT EXISTS idx_booking_room_date ON booking(room_id, booking_date);
CREATE INDEX IF NOT EXISTS idx_booking_user ON booking(user_id);
CREATE INDEX IF NOT EXISTS idx_event_user ON booking_event(user_id);
