#!/bin/bash
# ================================================================
# Database Setup Script
# Run ONCE on fresh EC2 to set up PostgreSQL
# ================================================================

set -e

AWS_REGION="ap-south-1"

echo "Setting up PostgreSQL..."

# Read password from SSM
DB_PASSWORD=$(aws ssm get-parameter \
    --name "/employee/prod/db/password" \
    --with-decryption \
    --region $AWS_REGION \
    --query 'Parameter.Value' \
    --output text)

# Start PostgreSQL container
docker run -d \
    --name employee_postgres \
    --restart unless-stopped \
    -e POSTGRES_DB=employee_db \
    -e POSTGRES_USER=admin \
    -e POSTGRES_PASSWORD=$DB_PASSWORD \
    -p 5433:5432 \
    postgres:16

echo "Waiting for PostgreSQL to start..."
sleep 15

# Verify it's running
docker exec employee_postgres pg_isready -U admin -d employee_db

echo "PostgreSQL is ready!"
