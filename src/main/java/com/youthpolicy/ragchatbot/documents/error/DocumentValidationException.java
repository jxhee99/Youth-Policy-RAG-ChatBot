package com.youthpolicy.ragchatbot.documents.error;

public class DocumentValidationException extends RuntimeException {

    private final String code;

    public DocumentValidationException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
