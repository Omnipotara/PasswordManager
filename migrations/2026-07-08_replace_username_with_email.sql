USE `passwordmanager_db`;

ALTER TABLE `users`
    DROP PRIMARY KEY,
    ADD PRIMARY KEY (`id`);

ALTER TABLE `users`
    CHANGE COLUMN `username` `email` VARCHAR(254) NOT NULL;

UPDATE `users`
SET `email` = CONCAT(LOWER(`email`), '@example.local')
WHERE `email` NOT LIKE '%_@_%._%';

ALTER TABLE `users`
    ADD UNIQUE KEY `email` (`email`);
