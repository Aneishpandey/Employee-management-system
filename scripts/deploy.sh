#!/bin/bash
set -e

AWS_REGION="ap-south-1"
ECR_REGISTRY="208549351005.dkr.ecr.ap-south-1.amazonaws.com"
ECR_REPOSITORY="employee-app-dev"
CONTAINER_NAME="employee_app"
APP_PORT="8083"

echo "=== Fetching secrets from SSM ==="
DB_PASSWORD=$(aws ssm get-parameter --name "/employee/prod/db/password" --with-decryption --region $AWS_REGION --query 'Parameter.Value' --output text)
DB_USERNAME=$(aws ssm get-parameter --name "/employee/prod/db/username" --region $AWS_REGION --query 'Parameter.Value' --output text)
DB_URL=$(aws ssm get-parameter --name "/employee/prod/db/url" --region $AWS_REGION --query 'Parameter.Value' --output text)
JWT_SECRET=$(aws ssm get-parameter --name "/employee/prod/jwt/secret" --with-decryption --region $AWS_REGION --query 'Parameter.Value' --output text)
echo "Secrets fetched!"

echo "=== Logging into ECR ==="
aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $ECR_REGISTRY

echo "=== Pulling latest image ==="
docker pull $ECR_REGISTRY/$ECR_REPOSITORY:latest

echo "=== Stopping old container ==="
docker stop $CONTAINER_NAME 2>/dev/null || true
docker rm $CONTAINER_NAME 2>/dev/null || true

echo "=== Starting new container ==="
docker run -d \
    --name $CONTAINER_NAME \
    --restart unless-stopped \
    -p $APP_PORT:$APP_PORT \
    -e SPRING_DATASOURCE_URL=$DB_URL \
    -e SPRING_DATASOURCE_USERNAME=$DB_USERNAME \
    -e SPRING_DATASOURCE_PASSWORD=$DB_PASSWORD \
    -e JWT_SECRET=$JWT_SECRET \
    -e SERVER_PORT=$APP_PORT \
    $ECR_REGISTRY/$ECR_REPOSITORY:latest

echo "=== Waiting for health check ==="
for i in $(seq 1 12); do
    HTTP=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:$APP_PORT/actuator/health 2>/dev/null)
    if [ "$HTTP" = "200" ]; then
        echo "App is healthy!"
        break
    fi
    if [ $i -eq 12 ]; then
        echo "ERROR: App failed health check!"
        docker logs --tail 50 $CONTAINER_NAME
        exit 1
    fi
    echo "Attempt $i/12 - waiting 10s..."
    sleep 10
done

echo "=== Deployment successful! ==="
