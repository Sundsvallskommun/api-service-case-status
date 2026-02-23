-- Add new columns
ALTER TABLE execution_information ADD COLUMN id VARCHAR(255);
ALTER TABLE execution_information ADD COLUMN service_name VARCHAR(255);

-- Migrate existing data (existing rows are SupportManagement)
UPDATE execution_information SET id = UUID(), service_name = 'SupportManagement' WHERE id IS NULL;

-- Switch primary key
ALTER TABLE execution_information DROP PRIMARY KEY;
ALTER TABLE execution_information MODIFY id VARCHAR(255) NOT NULL;
ALTER TABLE execution_information ADD PRIMARY KEY (id);

-- Add unique constraint to prevent duplicate service entries per municipality
ALTER TABLE execution_information ADD CONSTRAINT uq_municipality_service UNIQUE (municipality_id, service_name);
