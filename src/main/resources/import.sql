CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Insert roles
INSERT INTO roles (id, name)
VALUES (1, 'MANAGER'), (2, 'USER')
ON CONFLICT DO NOTHING;

-- Insert manager and test user
INSERT INTO users (id, date_created, date_modified, version, email, first_name, last_name, password)
VALUES
  (1, now() :: timestamptz(0), now() :: timestamptz(0), 0, 'john.doe@example.com', 'John', 'Doe',
   crypt('john.doe', gen_salt('bf', 10))),
  (2, now() :: timestamptz(0), now() :: timestamptz(0), 0, 'fred.bloggs@example.com', 'Fred', 'Bloggs',
   crypt('fred.bloggs', gen_salt('bf', 10)))
ON CONFLICT DO NOTHING;

-- Insert roleNames for users
INSERT INTO users_roles (user_id, role_id)
VALUES (1, 1), (1, 2), (2, 2)
ON CONFLICT DO NOTHING;

-- Insert root folders
INSERT INTO folders (id, date_created, date_modified, version, name, root, owner_id, parent_id)
VALUES
  (1, now() :: timestamptz(0), now() :: timestamptz(0), 0, uuid_generate_v4(), TRUE, 1, NULL),
  (2, now() :: timestamptz(0), now() :: timestamptz(0), 0, uuid_generate_v4(), TRUE, 2, NULL)
ON CONFLICT DO NOTHING;
