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