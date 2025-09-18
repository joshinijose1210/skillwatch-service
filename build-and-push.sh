#!/bin/bash
set -e

# Variables
AWS_REGION="us-west-2"
REPO_NAME="skillwatch-app"
TAG="latest"

# Get AWS Account ID
ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)

# Get ECR URI from CDKTF output or AWS Console
REPO_URI="${ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${REPO_NAME}"

# Authenticate Docker to ECR
aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $REPO_URI

# Build Docker image
docker build -t $REPO_URI:$TAG .

# Push Docker image to ECR
docker push $REPO_URI:$TAG

echo "Image pushed: $REPO_URI:$TAG"
