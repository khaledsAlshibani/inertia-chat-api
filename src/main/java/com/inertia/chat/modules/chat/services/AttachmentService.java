package com.inertia.chat.modules.chat.services;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.inertia.chat.modules.chat.entities.Attachment;

public interface AttachmentService {
    Attachment upload(MultipartFile file);
    List<Attachment> processAttachments(List<MultipartFile> files);
} 