USE `passwordmanager_db`;

-- MFA is mandatory for every login, so a per-user enable/disable flag is no longer needed.

ALTER TABLE `users`
    DROP COLUMN `mfa_enabled`;
