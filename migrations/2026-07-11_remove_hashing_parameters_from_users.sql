USE `passwordmanager_db`;

-- Hashing parameters are stored inside `password_hash` records.
-- This column stayed NULL for all supported hashing strategies.

ALTER TABLE `users`
    DROP COLUMN `hashing_parameters`;
