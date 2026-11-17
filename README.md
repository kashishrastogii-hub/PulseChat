# PulseChat — Real-Time 1:1 Chat Application

A full-stack messaging application built with **Spring Boot, React, and MySQL**. Users register, sign in with JWT, find other users, open a direct conversation, and exchange messages in real time over WebSocket. Messages are persisted in MySQL and delivered to each participant's private STOMP queue.

## Why this project exists

This repository turns the following internship responsibilities into working, demonstrable code:

| Internship bullet | Where it lives in this repo |
| --- | --- |
| Full-stack app with Spring Boot, ReactJS, MySQL | `backend/`, `frontend/`, `docker-compose.yml` |
| Secure authentication and authorization using JWT | `security/jwt/*`, `security/WebSecurityConfig.java`, `security/StompAuthInterceptor.java` |
| RESTful APIs for user management and data exchange | `controllers/`, `services/`, `repositories/`, `dto/` |
| WebSocket real-time communication with efficient broadcasting | `config/WebSocketConfig.java`, `services/impls/StompChatNotifier.java`, `websocket/ChatWebSocketController.java` |
| Efficient SQL, exception handling, service-layer logic | indexed/FK-backed Flyway schema in `db/migration/V1__init.sql`, `exceptions/GlobalExceptionHandler.java`, `services/impls/*` |
| Unit testing with JUnit and API validation via Postman | `backend/src/test/`, `postman/PulseChat.postman_collection.json` |

## Features (v1)

- Email/password sign-up and sign-in, BCrypt hashing, JWT issuance.
- Rate-limited auth endpoints (Bucket4j, 10 requests/minute/IP by default).
- User search by username or email; create-or-reuse a direct conversation between two users.
- Paginated message history, unread counts, and read receipts.
- WebSocket (STOMP over native WebSocket) message delivery authenticated with the same JWT.
- REST fallback endpoint for sending messages (useful for integration tests and Postman).
- Single-container Docker build serving the React SPA and the API on one port.

## Tech stack

| Layer | Technology |
| --- | --- |
| Backend | Java 21, Spring Boot 3.2, Spring Security (JWT), Spring Data JPA, Flyway, Bean Validation, Bucket4j, springdoc-openapi, Actuator |
| Realtime | Spring WebSocket + STOMP (`SimpMessagingTemplate`) |
| Frontend | React 18, React Router, axios, `@stomp/stompjs`, react-hook-form, react-hot-toast, lucide-react |
| Database | MySQL 8 |
| Tests | JUnit 5, Mockito, spring-security-test, H2 (test slice) |

## Quick start — local development

### Prerequisites

- Java 21+, Maven 3.9+ (or use the included Maven wrapper from the original project)
- Node.js 18+ and npm
- MySQL 8+ (or Docker)

### 1. Start MySQL

```sh
docker compose up -d db
```

This exposes MySQL on host port **3307** and creates database `pulsechat`. Alternatively create the database yourself:

```sql
CREATE DATABASE pulsechat;
```

### 2. Run the backend

```sh
cd backend
copy .env.example .env   # Windows; on macOS/Linux: cp .env.example .env
mvn spring-boot:run
```

Edit `backend/.env` to set `DB_PASSWORD` and a unique `JWT_SECRET` (32+ characters). The API is available at <http://localhost:8080/api>; Swagger UI at <http://localhost:8080/swagger-ui.html>. On first boot Flyway creates the schema and the role seeder inserts `ROLE_USER` / `ROLE_ADMIN`.

### 3. Run the frontend

```sh
cd frontend
copy .env.example .env
npm install
npm start
```

Open <http://localhost:3000>. Register two accounts (for example `alice` and `bob`) in two browsers, search for the other username, and start chatting.

### 4. Run everything in Docker instead

```sh
docker compose up --build
```

Open <http://localhost:8080>. The React build is embedded in the Spring Boot jar, so the SPA and API share one origin. Set `JWT_SECRET` in the root `.env` first.

## API overview

All responses use the envelope `{ "success": boolean, "message": string, "data": T }`.

| Method | Path | Auth | Purpose |
| --- | --- | --- | --- |
| POST | `/api/auth/signup` | no | Register and receive a JWT |
| POST | `/api/auth/signin` | no | Sign in with username or email |
| GET | `/api/users/me` | JWT | Current user profile |
| GET | `/api/users/search?query=` | JWT | Find users to chat with |
| POST | `/api/conversations` | JWT | Create or reuse a 1:1 conversation |
| GET | `/api/conversations` | JWT | List conversations, newest first |
| GET | `/api/conversations/{id}` | JWT | Conversation detail |
| POST | `/api/conversations/{id}/read` | JWT | Mark incoming messages as read |
| GET | `/api/conversations/{id}/messages?page=&size=` | JWT | Paginated history (newest page first) |
| POST | `/api/conversations/{id}/messages` | JWT | Send a message over REST; also broadcasts over WebSocket |

Import `postman/PulseChat.postman_collection.json` to run the same requests. The sign-up/sign-in requests automatically store the JWT in a collection variable.

## WebSocket protocol

- Endpoint: `ws://localhost:8080/ws` (STOMP over native WebSocket).
- Connect header: `Authorization: Bearer <jwt>` (a plain `token` header also works).
- Subscriptions:
  - `/user/queue/messages` — persisted messages, sent to both sender (ack) and recipient (live).
  - `/user/queue/read` — read receipts for the other participant.
  - `/user/queue/errors` — validation/authorization failures.
- Publish:
  - `/app/chat.send` with `{ "conversationId": 1, "content": "Hello" }`.
  - `/app/chat.read` with `{ "conversationId": 1 }`.

`StompAuthInterceptor` validates the JWT on CONNECT and attaches a real `Principal`; `MessageServiceImpl` additionally verifies that the sender belongs to the conversation before saving anything.

## Configuration

| Variable | Default | Description |
| --- | --- | --- |
| `PORT` | `8080` | HTTP port |
| `DB_URL` | `jdbc:mysql://localhost:3306/pulsechat` | JDBC URL |
| `DB_USERNAME` / `DB_PASSWORD` | `root` / empty | Database credentials |
| `JWT_SECRET` | development default | Must be 32+ characters in production |
| `JWT_EXPIRATION_MS` | `86400000` | Token lifetime (24 h) |
| `AUTH_RATE_LIMIT` | `10` | Auth requests per minute per IP |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000,http://localhost:3001` | Comma-separated origins; empty for same-origin |
| `SPRING_PROFILES_ACTIVE` | `dev` | `dev` uses `ddl-auto=update`; `prod` uses `validate` and checks the JWT secret |

Frontend build-time variables: `REACT_APP_API_BASE_URL` (default `http://localhost:8080/api`) and `REACT_APP_WS_URL` (derived from the API URL when omitted).

## Tests

```sh
cd backend
mvn test
```

Covers JWT auth flows, conversation creation/reuse and ownership checks, message persistence and broadcasting data, plus MockMvc controller tests for validation and status codes. `ChatAppApplicationTests` boots the whole context against H2.

## Project structure

```
.
├── Dockerfile                 # single container: React SPA + Spring Boot API
├── docker-compose.yml         # MySQL + app
├── postman/                   # importable API collection
├── backend/
│   ├── src/main/java/com/fullstack/chatapp/
│   │   ├── config/            # security, CORS, WebSocket, OpenAPI, SPA routing
│   │   ├── controllers/       # REST endpoints
│   │   ├── dto/               # request/response records and classes
│   │   ├── exceptions/        # typed exceptions + global handler
│   │   ├── models/            # JPA entities and enums
│   │   ├── repositories/      # Spring Data JPA queries
│   │   ├── security/          # filters, JWT, STOMP CONNECT interceptor
│   │   ├── services/          # interfaces, implementations, STOMP notifier
│   │   └── websocket/         # @MessageMapping handlers
│   └── src/main/resources/db/migration/V1__init.sql
└── frontend/
    └── src/
        ├── api/               # axios client + WebSocket URL resolution
        ├── components/        # chat UI components
        ├── contexts/          # auth state
        ├── hooks/             # useChat (STOMP lifecycle)
        ├── pages/             # login, register, chat, 404
        └── services/          # API wrappers
```
