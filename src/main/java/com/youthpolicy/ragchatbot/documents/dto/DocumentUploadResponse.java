package com.youthpolicy.ragchatbot.documents.dto;

import java.time.OffsetDateTime;

public record DocumentUploadResponse(
        long id,
        String title,
        String sourceType,
        String source,
        String contentType,
        String status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
