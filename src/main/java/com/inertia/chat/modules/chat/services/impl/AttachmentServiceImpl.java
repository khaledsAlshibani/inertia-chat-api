package com.inertia.chat.modules.chat.services.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.inertia.chat.modules.chat.entities.Attachment;
import com.inertia.chat.modules.chat.enums.AttachmentType;
import com.inertia.chat.modules.chat.services.AttachmentService;
import com.inertia.chat.modules.chat.storage.FileStorage;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttachmentServiceImpl implements AttachmentService {
    private static final long MAX_SIZE = 10 * 1024 * 1024; // 10MB

    private final FileStorage storage;

    /**
     * Uploads file bytes via FileStorage and returns an unsaved Attachment entity.
     */
    @Override
    public Attachment upload(MultipartFile file) {
        validate(file);

        String url = storage.upload(file);
        return Attachment.builder()
            .type(AttachmentType.fromMimeType(file.getContentType()))
            .size(file.getSize())
            .url(url)
            .fileName(file.getOriginalFilename())
            .build();
    }

    private void validate(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot upload an empty file");
        }
        String ct = file.getContentType();
        if (!AttachmentType.isSupported(ct)) {
            throw new IllegalArgumentException("Unsupported MIME type: " + ct);
        }
        if (file.getSize() > MAX_SIZE) {
            throw new IllegalArgumentException("File size exceeds 10 MB limit");
        }
    }

    /**
     * Processes a batch of files into unsaved Attachment entities.
     */
    @Override
    public List<Attachment> processAttachments(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return List.of();
        }
        return files.stream()
            .map(this::upload)
            .collect(Collectors.toList());
    }
}
