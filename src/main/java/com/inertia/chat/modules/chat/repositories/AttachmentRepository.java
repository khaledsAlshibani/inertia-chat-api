package com.inertia.chat.modules.chat.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.inertia.chat.modules.chat.entities.Attachment;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {}
