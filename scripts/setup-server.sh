#!/bin/bash
# ================================================================
# Server Setup Script
# Run ONCE on a fresh EC2 instance
# ================================================================

set -e

echo "Setting up EC2 server for Employee Management System..."

# Update system
sudo apt update && sudo apt upgrade -y

# Install Docker
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker ubuntu
sudo usermod -aG docker deploy 2>/dev/null || true

# Install Docker Compose
sudo apt install docker-compose-plugin -y

# Install Nginx
sudo apt install nginx -y
sudo systemctl enable nginx
sudo systemctl start nginx

# Install AWS CLI
sudo snap install aws-cli --classic

# Create app directory
sudo mkdir -p /app
sudo chown ubuntu:ubuntu /app

# Create logs directory
sudo mkdir -p /var/log/ems
sudo chown ubuntu:ubuntu /var/log/ems

echo "Server setup complete!"
echo "Next steps:"
echo "1. Run setup-db.sh to start PostgreSQL"
echo "2. Run deploy.sh to deploy the application"
