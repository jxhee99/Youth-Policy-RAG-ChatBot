package com.youthpolicy.ragchatbot.documents.parser.dto;

import java.util.List;
// PDF 전체 페이지 파싱 결과를 한 객체에 담는다.
public record ParsedPdfDocument(
        String filename,
        int totalPages,
        List<ParsedPdfPage> pages,
        String fullText
) {
}
