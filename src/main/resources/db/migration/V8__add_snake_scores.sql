CREATE TABLE snake_scores (
                              id BIGSERIAL PRIMARY KEY,
                              username VARCHAR(255) NOT NULL,
                              score INTEGER NOT NULL,
                              created_at TIMESTAMP NOT NULL
);