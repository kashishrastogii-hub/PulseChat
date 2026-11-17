-- PulseChat initial schema (Flyway baseline).
-- Names and column types follow Spring Boot's default naming strategies.

CREATE TABLE users (
    id bigint NOT NULL AUTO_INCREMENT,
    email varchar(255) NOT NULL,
    enabled bit(1) NOT NULL,
    password varchar(255) NOT NULL,
    username varchar(30) NOT NULL,
    created_at datetime(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_email (email),
    UNIQUE KEY uk_users_username (username)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE roles (
    id int NOT NULL AUTO_INCREMENT,
    name enum ('ROLE_USER', 'ROLE_ADMIN') DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE user_roles (
    user_id bigint NOT NULL,
    role_id int NOT NULL,
    PRIMARY KEY (user_id, role_id),
    KEY fk_user_roles_role (role_id),
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE conversations (
    id bigint NOT NULL AUTO_INCREMENT,
    created_at datetime(6) NOT NULL,
    updated_at datetime(6) NOT NULL,
    last_message_at datetime(6) NOT NULL,
    last_message_preview varchar(500) DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_conversations_last_message (last_message_at DESC)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE conversation_participants (
    id bigint NOT NULL AUTO_INCREMENT,
    conversation_id bigint NOT NULL,
    user_id bigint NOT NULL,
    joined_at datetime(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_conversation_participant (conversation_id, user_id),
    KEY fk_participants_user (user_id),
    CONSTRAINT fk_participants_conversation FOREIGN KEY (conversation_id) REFERENCES conversations (id) ON DELETE CASCADE,
    CONSTRAINT fk_participants_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE messages (
    id bigint NOT NULL AUTO_INCREMENT,
    conversation_id bigint NOT NULL,
    sender_id bigint NOT NULL,
    content varchar(2000) NOT NULL,
    sent_at datetime(6) NOT NULL,
    delivered_at datetime(6) DEFAULT NULL,
    read_at datetime(6) DEFAULT NULL,
    status enum ('SENT', 'DELIVERED', 'READ') NOT NULL,
    PRIMARY KEY (id),
    KEY idx_messages_conversation_sent (conversation_id, sent_at DESC),
    CONSTRAINT fk_messages_conversation FOREIGN KEY (conversation_id) REFERENCES conversations (id) ON DELETE CASCADE,
    CONSTRAINT fk_messages_sender FOREIGN KEY (sender_id) REFERENCES users (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
