# Deploy LocalGig to AWS Elastic Beanstalk

This project can run as one Java 21 Spring Boot application. The build script compiles the React app, places it in Spring Boot's static resources, builds the executable JAR, and creates `build/localgig-aws-eb.zip` for upload.

## Build the application bundle on Windows

From the project root in IntelliJ's PowerShell terminal:

```powershell
.\scripts\package-for-aws-eb.ps1
```

The script needs Node.js/npm and the Maven wrapper's ability to download Maven if it isn't already available. It does not require Docker.

## AWS setup

1. In the AWS Console, choose one region and open **Elastic Beanstalk**.
2. Create an application and a **Web server environment** using the **Java SE / Corretto 21** platform. For a short demo, select a single-instance environment.
3. Create or attach a PostgreSQL database. If you attach an RDS database to the Elastic Beanstalk environment, AWS supplies `RDS_HOSTNAME`, `RDS_PORT`, `RDS_DB_NAME`, `RDS_USERNAME`, and `RDS_PASSWORD`; the app reads those variables.
4. Set environment properties `SPRING_PROFILES_ACTIVE=prod` and `LOCALGIG_COOKIE_SECURE=true`. Add `OPENAI_API_KEY` or SMTP variables only if you plan to demo those integrations.
5. In the environment's software/security configuration, ensure the database security group allows PostgreSQL port 5432 from the application environment. Keep the database private.
6. Upload `build/localgig-aws-eb.zip` as the application version and deploy it.
7. Open the Elastic Beanstalk environment URL and check `/api/health`.

## Before confirming AWS resources

Review the selected region, instance type, database size, storage, and estimated charges in your AWS account. AWS resources can incur charges. An RDS database created with an Elastic Beanstalk environment can be deleted when that environment is terminated; retain or back up the database before deleting the environment.

Uploaded resumes/profile images currently use the app's local `uploads` directory. They are suitable for a demo but can be lost when the instance is replaced. Amazon S3 storage is a follow-up improvement for persistent uploads.
