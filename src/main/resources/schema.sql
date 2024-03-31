CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE TABLE IF NOT EXISTS roles (
    id uuid DEFAULT uuid_generate_v4 (),
    name VARCHAR(50) NOT NULL UNIQUE,
    created timestamptz DEFAULT current_timestamp,
    PRIMARY KEY (id)
);
CREATE TABLE IF NOT EXISTS users (
    id uuid DEFAULT uuid_generate_v4 (),
    username VARCHAR(255),
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    firstname VARCHAR(50) NOT NULL,
    middlename VARCHAR(255),
    lastname VARCHAR(50) NOT NULL,
    auth_provider VARCHAR(255),
    phone_number VARCHAR(50),
    created timestamptz DEFAULT current_timestamp,
    PRIMARY KEY (id)
);
CREATE TABLE IF NOT EXISTS users_roles (
    user_id uuid,
    role_id uuid,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (role_id) REFERENCES roles(id)
);


CREATE TABLE IF NOT EXISTS messages (
    id uuid DEFAULT uuid_generate_v4(),
    message TEXT NOT NULL,
    sender_id uuid NOT NULL,
    receiver_id uuid,
    group_id uuid,
    message_type VARCHAR(15) NOT NULL,
    created timestamptz DEFAULT current_timestamp,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS groups (
    id uuid DEFAULT uuid_generate_v4(),
    group_count SMALLINT NOT NULL,
    group_name VARCHAR(255) NOT NULL,
    group_description TEXT,
    created timestamptz DEFAULT current_timestamp,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS profiles (
    id uuid DEFAULT uuid_generate_v4(),
    user_id uuid NOT NULL UNIQUE,
    description TEXT,
    profile_image_url varchar(255),
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS groups_users (
    user_id uuid,
    group_id uuid,
    PRIMARY KEY (user_id, group_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (group_id) REFERENCES groups(id)
);

CREATE TABLE IF NOT EXISTS groups_admins (
    user_id uuid,
    group_id uuid,
    PRIMARY KEY (user_id, group_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (group_id) REFERENCES groups(id)
);

-- DO $$
-- BEGIN
--     IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'request_status_enum') THEN
--         CREATE TYPE request_status_enum AS ENUM ('PENDING', 'REJECTED', 'ACCEPTED');
--     END IF;
-- END
-- $$;


-- CREATE TYPE IF NOT EXISTS request_status_enum AS ENUM ('PENDING', 'REJECTED', 'ACCEPTED'); uncomment this when launching
CREATE TABLE IF NOT EXISTS connections (
    id uuid DEFAULT uuid_generate_v4(),
    requester_id uuid,
    receiver_id uuid,
    request_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    PRIMARY KEY (id),
    FOREIGN KEY (requester_id) REFERENCES users(id),
    FOREIGN KEY (receiver_id) REFERENCES users(id)
);
