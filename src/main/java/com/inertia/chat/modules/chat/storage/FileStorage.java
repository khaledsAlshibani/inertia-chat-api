package com.inertia.chat.modules.chat.storage;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorage {
    String upload(MultipartFile file);
    String uploadAvatar(MultipartFile file);
}