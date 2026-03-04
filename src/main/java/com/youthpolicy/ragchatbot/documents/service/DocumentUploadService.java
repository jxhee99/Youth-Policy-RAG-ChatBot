package com.youthpolicy.ragchatbot.documents.service;

import com.youthpolicy.ragchatbot.documents.dto.DocumentUploadResponse;
import com.youthpolicy.ragchatbot.documents.error.DocumentValidationException;
import com.youthpolicy.ragchatbot.documents.parser.PdfParser;
import com.youthpolicy.ragchatbot.documents.repository.DocumentRepository;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentUploadService implements DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentUploadService.class);

    // 업로드 허용 확장자와 DB에 저장할 표준 Content-Type 매핑
    private static final Map<String, String> ALLOWED_EXTENSIONS = Map.of(
            "pdf", "application/pdf",
            "txt", "text/plain",
            "doc", "application/msword",
            "docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    private final DocumentRepository documentRepository;
    private final PdfParser pdfParser;

    public DocumentUploadService(DocumentRepository documentRepository, PdfParser pdfParser) {
        this.documentRepository = documentRepository;
        this.pdfParser = pdfParser;
    }

    @Override
    public DocumentUploadResponse upload(MultipartFile file) {
        // 1) 파일 유효성 검증
        validateFile(file);
        // 2) 파일명 정리 및 메타데이터 추출
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = getExtension(originalFilename);
        String contentType = ALLOWED_EXTENSIONS.get(extension);
        String title = extractTitle(originalFilename);
        if ("pdf".equals(extension)) {
            var parsed = pdfParser.parse(file);
            log.info("Upload PDF parsed. file={}, pages={}", parsed.filename(), parsed.totalPages());
        }
        // 3) 문서 메타데이터 저장 (실제 파일 저장/파싱은 다음 단계에서 처리)
        return documentRepository.insertUploadedDocument(title, originalFilename, contentType);
    }

    private void validateFile(MultipartFile file) {
        // 빈 요청/빈 파일/파일명/확장자 순서로 실패를 빠르게 차단
        if (file == null) {
            throw new DocumentValidationException("FILE_REQUIRED", "파일이 필요합니다.");
        }
        if (file.isEmpty()) {
            throw new DocumentValidationException("EMPTY_FILE", "빈 파일은 업로드할 수 없습니다.");
        }
        String filename = file.getOriginalFilename();
        if (!StringUtils.hasText(filename)) {
            throw new DocumentValidationException("INVALID_FILENAME", "파일 이름이 유효하지 않습니다.");
        }
        String extension = getExtension(filename);
        if (!ALLOWED_EXTENSIONS.containsKey(extension)) {
            throw new DocumentValidationException(
                    "UNSUPPORTED_FILE_TYPE",
                    "지원하지 않는 파일 형식입니다. pdf, txt, doc, docx만 가능합니다."
            );
        }
    }

    private String getExtension(String filename) {
        // 확장자가 없거나 "."으로 끝나는 파일명은 거부
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            throw new DocumentValidationException("INVALID_EXTENSION", "파일 확장자가 필요합니다.");
        }
        return filename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }

    private String extractTitle(String filename) {
        // "a.pdf" -> "a" 형태로 제목 추출
        int dotIndex = filename.lastIndexOf('.');
        String title = (dotIndex > 0) ? filename.substring(0, dotIndex) : filename;
        if (!StringUtils.hasText(title)) {
            throw new DocumentValidationException("INVALID_TITLE", "문서 제목을 파일명에서 추출할 수 없습니다.");
        }
        return title;
    }
}
