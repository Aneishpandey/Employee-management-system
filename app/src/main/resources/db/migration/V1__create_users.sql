-- ============================================================
-- V1: Create users table
-- This table stores authenticated users of the system
-- ============================================================

CREATE TABLE users (
    id          BIGSERIAL PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    email       VARCHAR(100) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index for fast username lookups during login
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email    ON users(email);
