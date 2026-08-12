CREATE TABLE comments (
    id BIGSERIAL PRIMARY KEY,
    content VARCHAR(250) NOT NULL,
    post_id BIGINT NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT now()
                      );