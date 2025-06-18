-- Ensure 'deleted', 'deleted_at', and 'edited_at' columns exist in 'messages' table.
-- These are used for soft-deletion and message edit tracking.

ALTER TABLE messages
ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS edited_at TIMESTAMP;