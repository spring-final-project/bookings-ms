CREATE TABLE bookings(
    id CHAR(36) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    receipt_url VARCHAR(255),
    check_in TIMESTAMP NOT NULL,
    check_out TIMESTAMP NOT NULL,
    user_id CHAR(36) NOT NULL,
    room_id CHAR(36) NOT NULL,
    status VARCHAR(50) NOT NULL,
    rating INTEGER,
    review VARCHAR(255),
    PRIMARY KEY(id)
)