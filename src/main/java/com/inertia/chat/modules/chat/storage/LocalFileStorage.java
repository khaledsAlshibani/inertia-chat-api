package com.inertia.chat.modules.chat.storage;

import java.io.InputStream;
import java.nio.file.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import jakarta.annotation.PostConstruct;
import com.inertia.chat.modules.chat.utils.FileStorageUtil;

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
            Files.createDirectories(avatarsDir);
        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to create uploads directories", e);
        }
    }

    @Override
    public String upload(MultipartFile file) {
        String fileName = FileStorageUtil.generateFileName(file);
        return uploadToDirectory(file, fileName, baseDir, "/uploads/");
    }

    @Override
    public String uploadAvatar(MultipartFile file) {
        String fileName = FileStorageUtil.generateAvatarFileName(file);
        return uploadToDirectory(file, fileName, avatarsDir, "/uploads/avatars/");
    }
    
    private String uploadToDirectory(MultipartFile file, String fileName, Path targetDir, String urlPrefix) {
        Path target = targetDir.resolve(fileName);

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to write file to disk", e);
        }

        return urlPrefix + fileName;
    }
}
