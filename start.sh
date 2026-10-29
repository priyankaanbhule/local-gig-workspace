#!/bin/sh
set -eu

# Render provides a PostgreSQL URI; Spring's PostgreSQL driver expects a JDBC URL.
if [ -n "${DATABASE_URL:-}" ]; then
  database_authority_and_path="${DATABASE_URL#*://}"
  database_authority_and_path="${database_authority_and_path#*@}"
  export LOCALGIG_DB_URL="jdbc:postgresql://${database_authority_and_path}"
fi

# Password-reset emails should return users to the deployed single-origin app.
if [ -z "${LOCALGIG_FRONTEND_URL:-}" ] && [ -n "${RENDER_EXTERNAL_HOSTNAME:-}" ]; then
  export LOCALGIG_FRONTEND_URL="https://${RENDER_EXTERNAL_HOSTNAME}"
fi

exec java -XX:MaxRAMPercentage=75 -jar /app/localgig.jar
