package com.inertia.chat.modules.chat.utils;

import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import com.inertia.chat.modules.chat.enums.AttachmentType;

public class FileStorageUtil {
    
    // MIME type to extension mapping - aligned with AttachmentType.java
    private static final Map<String, String> MIME_TYPE_EXTENSIONS;
    
    static {
        MIME_TYPE_EXTENSIONS = new HashMap<>();
        
        // IMAGE types
        MIME_TYPE_EXTENSIONS.put("image/png", ".png");
        MIME_TYPE_EXTENSIONS.put("image/jpeg", ".jpg");
        MIME_TYPE_EXTENSIONS.put("image/jpg", ".jpg");
        MIME_TYPE_EXTENSIONS.put("image/webp", ".webp");
        MIME_TYPE_EXTENSIONS.put("image/bmp", ".bmp");
        MIME_TYPE_EXTENSIONS.put("image/tiff", ".tiff");
        MIME_TYPE_EXTENSIONS.put("image/svg+xml", ".svg");
        
        // GIF
        MIME_TYPE_EXTENSIONS.put("image/gif", ".gif");
        
        // VIDEO types  
        MIME_TYPE_EXTENSIONS.put("video/mp4", ".mp4");
        MIME_TYPE_EXTENSIONS.put("video/quicktime", ".mov");
        
        // AUDIO types
        MIME_TYPE_EXTENSIONS.put("audio/mpeg", ".mp3");
        MIME_TYPE_EXTENSIONS.put("audio/ogg", ".ogg");
        MIME_TYPE_EXTENSIONS.put("audio/wav", ".wav");
        MIME_TYPE_EXTENSIONS.put("audio/aac", ".aac");
        MIME_TYPE_EXTENSIONS.put("audio/x-m4a", ".m4a");
        MIME_TYPE_EXTENSIONS.put("audio/flac", ".flac");
        
        // VOICE types
        MIME_TYPE_EXTENSIONS.put("audio/webm", ".webm");
        MIME_TYPE_EXTENSIONS.put("audio/mp4", ".m4a");
        
        // DOCUMENT types
        MIME_TYPE_EXTENSIONS.put("application/pdf", ".pdf");
        MIME_TYPE_EXTENSIONS.put("text/plain", ".txt");
        MIME_TYPE_EXTENSIONS.put("application/msword", ".doc");
        MIME_TYPE_EXTENSIONS.put("application/vnd.openxmlformats-officedocument.wordprocessingml.document", ".docx");
        MIME_TYPE_EXTENSIONS.put("application/vnd.ms-excel", ".xls");
        MIME_TYPE_EXTENSIONS.put("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", ".xlsx");
        MIME_TYPE_EXTENSIONS.put("application/vnd.ms-powerpoint", ".ppt");
        MIME_TYPE_EXTENSIONS.put("application/vnd.openxmlformats-officedocument.presentationml.presentation", ".pptx");
        MIME_TYPE_EXTENSIONS.put("text/csv", ".csv");
        MIME_TYPE_EXTENSIONS.put("application/rtf", ".rtf");
        MIME_TYPE_EXTENSIONS.put("application/xml", ".xml");
        MIME_TYPE_EXTENSIONS.put("text/html", ".html");
        MIME_TYPE_EXTENSIONS.put("application/json", ".json");
    }
    
    /**
     * Gets appropriate file extension based on content type
     * @param contentType the MIME content type
     * @param defaultExtension fallback extension
     * @return file extension including the dot
     */
    public static String getExtensionFromContentType(String contentType, String defaultExtension) {
        if (contentType != null) {
            String extension = MIME_TYPE_EXTENSIONS.get(contentType.toLowerCase());
            if (extension != null) {
                return extension;
            }
        }
        return defaultExtension.startsWith(".") ? defaultExtension : "." + defaultExtension;
    }
    
    /**
     * Generates a unique filename using only UUID + extension based on content type
     * @param file the multipart file
     * @param defaultExtension fallback extension if content type unknown
     * @return unique filename (UUID + extension)
     */
    public static String generateUniqueFileName(MultipartFile file, String defaultExtension) {
        String extension = getExtensionFromContentType(file.getContentType(), defaultExtension);
        return UUID.randomUUID().toString() + extension;
    }
    
    /**
     * Validates if the file is an image type using AttachmentType
     * @param file the multipart file to validate
     * @throws RuntimeException if file is not an image
     */
    public static void validateImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !AttachmentType.isSupported(contentType)) {
            throw new RuntimeException("Unsupported file type for upload");
        }
        
        AttachmentType type = AttachmentType.fromMimeType(contentType);
        if (type != AttachmentType.IMAGE && type != AttachmentType.GIF) {
            throw new RuntimeException("Only image files are allowed for avatars");
        }
    }
    
    /**
     * Validates if the file type is supported by the application
     * @param file the multipart file to validate
     * @throws RuntimeException if file type is not supported
     */
    public static void validateSupportedFile(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !AttachmentType.isSupported(contentType)) {
            throw new RuntimeException("Unsupported file type: " + contentType);
        }
    }
    
    /**
     * Generates a unique filename for regular files based on content type
     * Validates that the file type is supported by the application
     * @param file the multipart file
     * @return unique filename with appropriate extension
     * @throws RuntimeException if file type is not supported
     */
    public static String generateFileName(MultipartFile file) {
        validateSupportedFile(file);
        return generateUniqueFileName(file, "bin");
    }
    
    /**
     * Generates a unique filename for avatar files with validation
     * @param file the multipart file
     * @return unique filename for avatar
     * @throws RuntimeException if file is not an image
     */
    public static String generateAvatarFileName(MultipartFile file) {
        validateImageFile(file);
        return generateUniqueFileName(file, "jpg");
    }
}