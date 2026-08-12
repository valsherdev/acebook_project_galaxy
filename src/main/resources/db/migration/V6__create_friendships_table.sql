CREATE TABLE friendships (
     id BIGSERIAL PRIMARY KEY,
     user_id BIGINT NOT NULL,
     friend_id BIGINT NOT NULL,
     status VARCHAR(50) NOT NULL DEFAULT 'ACCEPTED',
     CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id),
     CONSTRAINT fk_friend FOREIGN KEY (friend_id) REFERENCES users(id)
);