-- restricted role: may only read tenants
INSERT INTO roles (id, name, description, unrestricted, created_at, updated_at, created_by, updated_by, version)
VALUES (UUID_TO_BIN('00000000-0000-0000-0000-0000000000a2'), 'Mandanten lesen', NULL, FALSE,
        NOW(6), NOW(6), 'test', 'test', 0);

INSERT INTO role_permissions (role_id, entity_type, action)
VALUES (UUID_TO_BIN('00000000-0000-0000-0000-0000000000a2'), 'TENANT', 'READ');

INSERT INTO employees (id, email, first_name, last_name, role_id, cognito_sub, active,
                       created_at, updated_at, created_by, updated_by, version)
VALUES (UUID_TO_BIN('00000000-0000-0000-0000-0000000000e1'), 'admin@test.de', 'Ada', 'Admin',
        (SELECT id FROM roles WHERE unrestricted = TRUE LIMIT 1), 'admin-sub', TRUE,
    NOW(6), NOW(6), 'test', 'test', 0),
       (UUID_TO_BIN('00000000-0000-0000-0000-0000000000e2'), 'reader@test.de', 'Rita', 'Reader',
        UUID_TO_BIN('00000000-0000-0000-0000-0000000000a2'), 'reader-sub', TRUE,
        NOW(6), NOW(6), 'test', 'test', 0),
       (UUID_TO_BIN('00000000-0000-0000-0000-0000000000e3'), 'former@test.de', 'Fritz', 'Former',
        (SELECT id FROM roles WHERE unrestricted = TRUE LIMIT 1), 'inactive-sub', FALSE,
        NOW(6), NOW(6), 'test', 'test', 0);