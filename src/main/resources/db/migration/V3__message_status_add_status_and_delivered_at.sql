-- 1. Create the enum type
CREATE TYPE message_status_type AS ENUM (
  'SENT',
  'DELIVERED',
  'READ'
);

-- 2. Add columns
ALTER TABLE message_statuses
  ADD COLUMN status message_status_type NOT NULL DEFAULT 'SENT';

ALTER TABLE message_statuses
  ADD COLUMN delivered_at TIMESTAMP;

-- 3. Rename old column
ALTER TABLE message_statuses
  RENAME COLUMN is_read TO was_read;

-- 4. Backfill data with explicit casting
UPDATE message_statuses
SET
  status = CASE 
             WHEN was_read THEN 'READ'::message_status_type 
             ELSE 'SENT'::message_status_type 
           END,
  read_at = CASE 
              WHEN was_read THEN NOW() 
              ELSE NULL 
            END;

-- 5. Drop the old column
ALTER TABLE message_statuses
  DROP COLUMN was_read;

-- 6. Set default again (optional, but good for clarity)
ALTER TABLE message_statuses
  ALTER COLUMN status SET DEFAULT 'SENT';
