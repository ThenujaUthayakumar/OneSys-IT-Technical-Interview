CREATE TABLE IF NOT EXISTS `employees` (
    `id` INT(11) NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `first_name` VARCHAR(100) NOT NULL,
    `last_name` VARCHAR(100) NOT NULL,
    `address` VARCHAR(100) NOT NULL,
    `contact_number` VARCHAR(100) NOT NULL,
    `email` VARCHAR(100) NOT NULL,
    `attachment` VARCHAR(255) NULL,
    `date_of_birth` DATE NOT NULL,
    `current_age_in_days` INT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP
);