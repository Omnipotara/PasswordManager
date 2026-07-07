USE `passwordmanager_db`;

ALTER TABLE `users`
    MODIFY COLUMN `password` TEXT NOT NULL,
    ADD COLUMN `hashing_algorithm` VARCHAR(30) NOT NULL DEFAULT 'BCRYPT' AFTER `salt`;

UPDATE `users`
SET `hashing_algorithm` = 'BCRYPT'
WHERE `hashing_algorithm` IS NULL OR `hashing_algorithm` = '';
