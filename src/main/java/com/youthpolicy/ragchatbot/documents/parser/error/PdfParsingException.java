package com.youthpolicy.ragchatbot.documents.parser.error;

public class PdfParsingException extends RuntimeException {

    private final String code;

    public PdfParsingException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
