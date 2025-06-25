-- Add a nullable 'name' column for GROUP chats

ALTER TABLE chats
ADD COLUMN name VARCHAR(255) NULL;
