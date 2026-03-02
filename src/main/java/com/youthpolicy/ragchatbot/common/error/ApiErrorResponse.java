package com.youthpolicy.ragchatbot.common.error;

import java.time.OffsetDateTime;

public record ApiErrorResponse(
        String code,
        String message,
        OffsetDateTime timestamp
) {
    public static ApiErrorResponse of(String code, String message) {
        return new ApiErrorResponse(code, message, OffsetDateTime.now());
    }
}
