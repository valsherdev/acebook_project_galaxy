CREATE TABLE profiles (
                    id BIGSERIAL PRIMARY KEY,
                    first_name VARCHAR(250),
                    last_name VARCHAR(250),
                    current_location VARCHAR(250),
                    hometown VARCHAR(250),
                    about_me TEXT,
                    user_id BIGINT UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE
);