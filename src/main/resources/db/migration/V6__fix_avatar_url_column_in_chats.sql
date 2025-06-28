-- Fix avatar_url column naming in chats table
-- Remove incorrect camelCase column and add correct snake_case column

-- Drop the incorrect avatarUrl column if it exists
ALTER TABLE chats 
DROP COLUMN IF EXISTS avatarUrl;

-- Add the correct avatar_url column (snake_case)
ALTER TABLE chats 
ADD COLUMN IF NOT EXISTS avatar_url VARCHAR(255) NULL;