-- Database initialization script
-- This script runs when the PostgreSQL container starts for the first time

-- Create database if it doesn't exist
SELECT 'CREATE DATABASE backend_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'backend_db');

-- Connect to the backend_db database
\c backend_db;

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Create schema if needed
CREATE SCHEMA IF NOT EXISTS public;

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE backend_db TO backend_user;
GRANT ALL ON SCHEMA public TO backend_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO backend_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO backend_user;

-- Set default privileges for future tables
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO backend_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO backend_user;

-- Create a simple health check table
CREATE TABLE IF NOT EXISTS health_check (
    id SERIAL PRIMARY KEY,
    status VARCHAR(10) DEFAULT 'OK',
    checked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert initial health check record
INSERT INTO health_check (status) VALUES ('OK') ON CONFLICT DO NOTHING;
