# syntax=docker/dockerfile:1

# 1. Build the React SPA
FROM node:20-alpine AS frontend
WORKDIR /app
ENV CI=false NODE_OPTIONS=--dns-result-order=ipv4first
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci --no-audit --no-fund
COPY frontend/ ./
ARG REACT_APP_API_BASE_URL=/api
ARG REACT_APP_WS_URL=/ws
ENV REACT_APP_API_BASE_URL=${REACT_APP_API_BASE_URL} \
    REACT_APP_WS_URL=${REACT_APP_WS_URL}
RUN npm run build

# 2. Build the Spring Boot jar with the SPA embedded under classpath:/static
FROM maven:3.9-eclipse-temurin-21 AS backend
WORKDIR /app
COPY backend/pom.xml ./
COPY backend/src ./src
COPY --from=frontend /app/build ./src/main/resources/static
RUN mvn -B -q -DskipTests package

# 3. Single runtime container: SPA + REST API + WebSocket endpoint
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=backend /app/target/*.jar /app/app.jar
ENV SPRING_PROFILES_ACTIVE=dev \
    PORT=8080
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD wget -qO- "http://127.0.0.1:${PORT}/actuator/health" >/dev/null 2>&1 || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
