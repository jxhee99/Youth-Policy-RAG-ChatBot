package com.youthpolicy.ragchatbot.documents.parser.dto;
// PDF 한 페이지 결과만 담는다.
public record ParsedPdfPage(
        int pageNumber,
        String text
) {
}
