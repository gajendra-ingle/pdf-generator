#!/bin/bash
echo "Creating S3 bucket: pdf-dev-bucket"
awslocal s3 mb s3://pdf-dev-bucket
awslocal s3api put-bucket-cors --bucket pdf-dev-bucket --cors-configuration '{
  "CORSRules": [{
    "AllowedOrigins": ["*"],
    "AllowedMethods": ["GET", "PUT", "POST"],
    "AllowedHeaders": ["*"]
  }]
}'
echo "S3 bucket pdf-dev-bucket created successfully"
