package com.youthpolicy.ragchatbot.documents.parser;

import org.springframework.web.multipart.MultipartFile;

import com.youthpolicy.ragchatbot.documents.parser.dto.ParsedPdfDocument;

// PDF 파싱 기능의 표준 계약(인터페이스). - 지금은 PdfBoxPdfParser를 쓰지만, 나중에 다른 파서 라이브러리로 바꾸기 쉬움
// 실제 구현체(PDFBox 등)를 바꿔도 서비스 코드는 이 인터페이스만 의존하도록 분리한다.
public interface PdfParser {
    // 업로드된 PDF 파일을 받아 페이지별 텍스트/전체 텍스트를 반환한다.
    ParsedPdfDocument parse(MultipartFile file);
}
