package com.inertia.chat.modules.chat.storage;

import java.io.InputStream;
import java.nio.file.*;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import jakarta.annotation.PostConstruct;

@Service
@ConditionalOnExpression("'${spring.profiles.active:dev}'.equals('dev') and !'${aws.s3.enable:false}'.equals('true')")
public class LocalFileStorage implements FileStorage {
    private Path baseDir;
    private Path avatarsDir;

    @PostConstruct
    public void init() {
        this.baseDir = Paths.get("uploads");  
        this.avatarsDir = Paths.get("uploads/avatars");
        try {
            Files.createDirectories(baseDir);
        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to create uploads directory", e);
        }
    }

    @Override
    public String upload(MultipartFile file) {
        return uploadToDirectory(file, baseDir, "/uploads/");
    }

    public String uploadAvatar(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("Only image files are allowed for avatars");
        }

        return uploadToDirectory(file, avatarsDir, "/uploads/avatars/");
    }
    
    private String uploadToDirectory(MultipartFile file, Path targetDir, String urlPrefix) {
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = "." + originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        }
        
        String fileName = UUID.randomUUID().toString() + extension;
        Path target = targetDir.resolve(fileName);

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to write file to disk", e);
        }

        return urlPrefix + fileName;
    }
}
