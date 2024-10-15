CREATE TABLE bookings(
--    id CHAR(36) NOT NULL,
--    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    id uuid default gen_random_uuid(),
    created_at TIMESTAMPTZ DEFAULT now(),
    receipt_url VARCHAR(255),
    check_in TIMESTAMPTZ NOT NULL,
    check_out TIMESTAMPTZ NOT NULL,
--    user_id CHAR(36) NOT NULL,
    user_id UUID NOT NULL,
--    room_id CHAR(36) NOT NULL,
    room_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL,
    rating INTEGER,
    review VARCHAR(255),
    PRIMARY KEY(id)
)