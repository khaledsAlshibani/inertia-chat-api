-- Fix message status column type mismatch
-- Convert from PostgreSQL enum to VARCHAR to match Hibernate @Enumerated(EnumType.STRING)

-- 1. Add a temporary VARCHAR column
ALTER TABLE message_statuses 
ADD COLUMN IF NOT EXISTS status_temp VARCHAR(20);

-- 2. Copy data from enum column to VARCHAR column
UPDATE message_statuses 
SET status_temp = status::text;

-- 3. Drop the enum column
ALTER TABLE message_statuses 
DROP COLUMN IF EXISTS status;

-- 4. Rename the temp column to the original name
ALTER TABLE message_statuses 
RENAME COLUMN status_temp TO status;

-- 5. Add NOT NULL constraint and default value
ALTER TABLE message_statuses 
ALTER COLUMN status SET NOT NULL;

ALTER TABLE message_statuses 
ALTER COLUMN status SET DEFAULT 'SENT';

-- 6. Drop the enum type if no other tables use it
DROP TYPE IF EXISTS message_status_type;