USE `passwordmanager_db`;

-- Run after 2026-07-08_replace_username_with_email.sql.
-- The existing `salt` column is kept because it is used for encryption key derivation.

ALTER TABLE `users`
    CHANGE COLUMN `password` `password_hash` TEXT NOT NULL;

ALTER TABLE `users`
    ADD COLUMN `hashing_parameters` TEXT NULL AFTER `hashing_algorithm`,
    ADD COLUMN `mfa_enabled` TINYINT(1) NOT NULL DEFAULT 0 AFTER `hashing_parameters`;

UPDATE `users`
SET `hashing_parameters` = NULL
WHERE `hashing_parameters` IS NULL;
