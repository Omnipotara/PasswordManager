USE `passwordmanager_db`;

-- Adds the metadata needed to decrypt each password entry independently.
-- Run after the user security metadata migrations.

ALTER TABLE `password_entries`
    MODIFY COLUMN `password` TEXT NOT NULL,
    ADD COLUMN `encryption_algorithm` VARCHAR(30) NOT NULL DEFAULT 'AES_GCM' AFTER `password`,
    ADD COLUMN `iv` TEXT NULL AFTER `encryption_algorithm`,
    ADD COLUMN `authentication_tag` TEXT NULL AFTER `iv`,
    ADD COLUMN `encryption_salt` TEXT NULL AFTER `authentication_tag`,
    ADD COLUMN `encryption_parameters` TEXT NULL AFTER `encryption_salt`;

UPDATE `password_entries`
SET `encryption_algorithm` = 'AES_GCM'
WHERE `encryption_algorithm` IS NULL OR `encryption_algorithm` = '';
