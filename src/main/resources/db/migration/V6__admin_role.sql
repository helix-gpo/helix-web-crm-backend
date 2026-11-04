-- fresh databases get an unrestricted admin role - existing ones (dev) already have one
INSERT INTO roles (id, name, description, unrestricted, created_at, updated_at, created_by, updated_by, version)
SELECT UUID_TO_BIN(UUID()), 'Administrator', 'Uneingeschränkter Zugriff auf alle Bereiche',
       TRUE, NOW(6), NOW(6), 'system', 'system', 0
    WHERE NOT EXISTS (SELECT 1 FROM roles WHERE unrestricted = TRUE);