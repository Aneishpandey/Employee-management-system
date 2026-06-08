-- ============================================================
-- V3: Create employees table
-- References departments via foreign key
-- ============================================================

CREATE TABLE employees (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(100)   NOT NULL,
    email           VARCHAR(100)   NOT NULL UNIQUE,
    salary          DECIMAL(10, 2) NOT NULL CHECK (salary >= 0),
    department_id   BIGINT REFERENCES departments(id) ON DELETE SET NULL
);

-- Index for fast lookups
CREATE INDEX idx_employees_email         ON employees(email);
CREATE INDEX idx_employees_department_id ON employees(department_id);
