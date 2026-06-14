# CineStack Infrastructure Notes

The first deployment target is AWS:

- Frontend: AWS Amplify Hosting for the Next.js app in `frontend/`
- Backend: AWS App Runner from a Docker image pushed to ECR
- Database: AWS RDS PostgreSQL
- Secrets: App Runner environment variables or AWS Secrets Manager
- Logs: CloudWatch

Required backend environment variables:

```text
DATABASE_URL
DATABASE_USERNAME
DATABASE_PASSWORD
JWT_SECRET
TMDB_API_KEY
CORS_ALLOWED_ORIGINS
```

Suggested rollout:

1. Create RDS PostgreSQL and allow inbound traffic from the backend service.
2. Push the backend Docker image to ECR.
3. Create an App Runner service with the backend image and environment variables.
4. Deploy the frontend to Amplify with `NEXT_PUBLIC_API_BASE_URL` set to the App Runner URL.
5. Confirm `/actuator/health`, auth, movie search, watchlist creation, and public profile pages.

Future IaC can be added with AWS CDK or Terraform once the deployed MVP is stable.

