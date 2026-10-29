FROM node:24-alpine AS frontend-build
WORKDIR /build/frontend
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

FROM maven:3.9.11-eclipse-temurin-21 AS backend-build
WORKDIR /build
COPY pom.xml ./
COPY src ./src
COPY --from=frontend-build /build/frontend/dist/ ./src/main/resources/static/
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=backend-build /build/target/localGigWorkplace-0.0.1-SNAPSHOT.jar /app/localgig.jar
COPY start.sh /app/start.sh
EXPOSE 8081
ENTRYPOINT ["sh", "/app/start.sh"]
