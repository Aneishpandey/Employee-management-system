-- ============================================================
-- V2: Create departments table
-- Must run BEFORE employees (employees reference departments)
-- ============================================================

CREATE TABLE departments (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255)
);

-- Index for fast name lookups
CREATE INDEX idx_departments_name ON departments(name);
