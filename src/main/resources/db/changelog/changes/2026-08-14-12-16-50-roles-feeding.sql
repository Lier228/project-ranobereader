--liquibase formatted sql

--changeset ranobereader:004-insert-default-roles
INSERT INTO roles (name)
VALUES ('ROLE_USER'),
       ('ROLE_TRANSLATOR'),
       ('ROLE_ADMIN')
ON CONFLICT (name) DO NOTHING;

--changeset ranobereader:005-seed-default-users
INSERT INTO users (email, password, full_name, enabled) VALUES
('admin@ranobe.kz', '$2a$10$fv89hkB1lXCSYIzGIbmCduLQJ8pC6kTFouVnpFmDyJoQhdq7SdtXq', 'Admin', true),
('translator@ranobe.kz', '$2a$10$fv89hkB1lXCSYIzGIbmCduLQJ8pC6kTFouVnpFmDyJoQhdq7SdtXq', 'Translator', true),
('user@ranobe.kz', '$2a$10$fv89hkB1lXCSYIzGIbmCduLQJ8pC6kTFouVnpFmDyJoQhdq7SdtXq', 'Reader', true)
ON CONFLICT (email) DO NOTHING;

--changeset ranobereader:006-seed-user-roles
INSERT INTO users_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.email = 'admin@ranobe.kz' AND r.name IN ('ROLE_ADMIN', 'ROLE_TRANSLATOR', 'ROLE_USER')
ON CONFLICT DO NOTHING;

INSERT INTO users_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.email = 'translator@ranobe.kz' AND r.name IN ('ROLE_TRANSLATOR', 'ROLE_USER')
ON CONFLICT DO NOTHING;

INSERT INTO users_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.email = 'user@ranobe.kz' AND r.name = 'ROLE_USER'
ON CONFLICT DO NOTHING;