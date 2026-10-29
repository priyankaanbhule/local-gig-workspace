# Deploy LocalGig on Render

The Render Blueprint builds the React app and Spring Boot API into one Docker web service and creates PostgreSQL for it. The React app and API then use the same origin, so browser sign-in cookies work without separate CORS configuration.

## Before deploying

1. Push the localGigWorkplace repository to a GitHub repository you control. The active React app is in frontend/; the active Spring Boot app is in src/.
2. In Render, create a Blueprint and connect that GitHub repository. Render reads render.yaml from the repository root.
3. Review the Blueprint resources and confirm the free web service and free PostgreSQL database before creating them.
4. Wait for the service deploy to complete, then open its onrender.com URL. The health endpoint is /api/health.

## Optional API and email features

Add these values under the web service's Environment page, then redeploy:

- OPENAI_API_KEY: a secret API key for the AI cover-letter draft.
- LOCALGIG_MAIL_ENABLED=true, LOCALGIG_SMTP_HOST, LOCALGIG_SMTP_PORT, LOCALGIG_SMTP_USERNAME, LOCALGIG_SMTP_PASSWORD, and LOCALGIG_MAIL_FROM: SMTP values for password-reset emails.

Keep all API keys and mail passwords in Render's environment settings. Do not put them in React code or commit them to Git.

## Free-plan limits for a demo

Render's free web service can sleep when idle, and a free PostgreSQL database expires after 30 days. A free web service also has no persistent disk, so uploaded resumes and profile images can disappear after a restart or redeploy. Use this setup for a short demo; choose paid storage or object storage if uploaded files must persist.
