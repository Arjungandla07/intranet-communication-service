CREATE TABLE channels (
    channel_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    state_flag INTEGER NOT NULL CHECK (state_flag IN (0, 1)),
    latest_message_id BIGINT,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE users (
    user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role VARCHAR(20) NOT NULL CHECK (role IN ('TERMINAL', 'CONTROLLER')),
    auth_secret VARCHAR(255) NOT NULL
);

CREATE TABLE channel_members (
    channel_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    member_role VARCHAR(20) NOT NULL,
    PRIMARY KEY (channel_id, user_id),
    CONSTRAINT fk_cm_channel FOREIGN KEY (channel_id)
        REFERENCES channels(channel_id),
    CONSTRAINT fk_cm_user FOREIGN KEY (user_id)
        REFERENCES users(user_id)
);

CREATE TABLE messages (
    message_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    channel_id BIGINT NOT NULL,
    sender_type VARCHAR(20) NOT NULL CHECK (sender_type IN ('TERMINAL', 'CONTROLLER')),
    content_type VARCHAR(20) NOT NULL CHECK (content_type IN ('TEXT', 'IMAGE', 'VIDEO', 'VOICE')),
    text_body CLOB,
    media_path VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_msg_channel FOREIGN KEY (channel_id)
        REFERENCES channels(channel_id)
);

ALTER TABLE channels
ADD CONSTRAINT fk_latest_message
FOREIGN KEY (latest_message_id)
REFERENCES messages(message_id);

CREATE INDEX idx_messages_channel_time
ON messages(channel_id, created_at DESC);