-- Add a nullable 'avatarUrl' column for GROUP chats

ALTER TABLE chats
ADD COLUMN avatarUrl VARCHAR(255) NULL;