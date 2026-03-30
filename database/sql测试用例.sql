INSERT INTO users (id, name, email, password_hash, role, avatar)
VALUES
    ('u1', 'Alice', 'alice@example.com', 'hashed_pw_1', 'Customer', NULL),
    ('u2', 'Bob', 'bob@example.com', 'hashed_pw_2', 'Customer', NULL),
    ('u3', 'Dr. Smith', 'smith@example.com', 'hashed_pw_3', 'Specialist', NULL),
    ('u4', 'Dr. Lee', 'lee@example.com', 'hashed_pw_4', 'Specialist', NULL),
    ('u5', 'Admin', 'admin@example.com', 'hashed_pw_5', 'Admin', NULL);

INSERT INTO specialists (user_id, bio, price, status)
VALUES
    ('u3', 'Psychologist with 10 years experience', 100.00, 'Active'),
    ('u4', 'Career consultant and coach', 80.00, 'Active');

INSERT INTO expertise (id, name, description)
VALUES
    ('e1', 'Mental Health', 'Psychological counseling'),
    ('e2', 'Career Advice', 'Career planning and coaching'),
    ('e3', 'Stress Management', 'Stress relief techniques');

INSERT INTO specialist_expertise (specialist_id, expertise_id)
VALUES
    ('u3', 'e1'),
    ('u3', 'e3'),
    ('u4', 'e2');

INSERT INTO slots (id, specialist_id, start_time, end_time, available)
VALUES
    ('s1', 'u3', '2026-03-25 10:00:00', '2026-03-25 11:00:00', TRUE),
    ('s2', 'u3', '2026-03-25 11:00:00', '2026-03-25 12:00:00', TRUE),
    ('s3', 'u4', '2026-03-25 14:00:00', '2026-03-25 15:00:00', TRUE);

INSERT INTO bookings (id, customer_id, specialist_id, slot_id, note, status)
VALUES
    ('b1', 'u1', 'u3', 's1', 'Need help with anxiety', 'Confirmed'),
    ('b2', 'u2', 'u4', 's3', 'Career switch advice', 'Pending');

INSERT INTO booking_history (id, booking_id, status, reason)
VALUES
    ('h1', 'b1', 'Pending', 'Initial booking'),
    ('h2', 'b1', 'Confirmed', 'Specialist accepted'),
    ('h3', 'b2', 'Pending', 'Waiting for confirmation');

INSERT INTO pricing (id, specialist_id, duration, type, amount, currency, detail)
VALUES
    ('p1', 'u3', 60, 'online', 100.00, 'USD', '1 hour online session'),
    ('p2', 'u3', 30, 'online', 60.00, 'USD', '30 min quick session'),
    ('p3', 'u4', 60, 'offline', 80.00, 'USD', 'Face-to-face consultation');