ALTER TABLE employees
ADD COLUMN reference_number VARCHAR(50) UNIQUE NULL;

ALTER TABLE employees
ADD COLUMN gender VARCHAR(100);