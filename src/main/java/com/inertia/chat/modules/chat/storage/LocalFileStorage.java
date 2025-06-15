package com.inertia.chat.modules.chat.storage;

import java.io.InputStream;
import java.nio.file.*;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import jakarta.annotation.PostConstruct;

@Service
@Profile("dev")
public class LocalFileStorage implements FileStorage {
    private Path baseDir;


    @PostConstruct
    public void init() {
        this.baseDir = Paths.get("uploads");  
        try {
            Files.createDirectories(baseDir);
        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to create uploads directory", e);
        }
    }

@Override
public String upload(MultipartFile file) {
    String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
    Path target = baseDir.resolve(fileName);

    try (InputStream in = file.getInputStream()) {
        Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
    } catch (java.io.IOException e) {
        throw new RuntimeException("Failed to write file to disk", e);
    }

    // Return URL for accessing the file via HTTP
    return "/uploads/" + fileName;
}

}
