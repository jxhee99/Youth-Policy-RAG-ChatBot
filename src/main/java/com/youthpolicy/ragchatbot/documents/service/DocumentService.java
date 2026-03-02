package com.youthpolicy.ragchatbot.documents.service;

import com.youthpolicy.ragchatbot.documents.dto.DocumentUploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface DocumentService {
    DocumentUploadResponse upload(MultipartFile file);
}
