/* Pdf를 읽고 페이지별 텍스트를 추출한 뒤 그 결과를 ParsedPdfDocument DTO로 변환
1. ParsedPdfPage 리스트에 페이지 번호 + 페이지 텍스트 저장
2. fullText에 전체 페이지 텍스트 모두 저장
3. 마지막으로 ParsePdfDocument DTO로 반환
 */
package com.youthpolicy.ragchatbot.documents.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.youthpolicy.ragchatbot.documents.parser.dto.ParsedPdfDocument;
import com.youthpolicy.ragchatbot.documents.parser.dto.ParsedPdfPage;
import com.youthpolicy.ragchatbot.documents.parser.error.PdfParsingException;

@Component
public class PdfBoxPdfParser implements PdfParser {

    private static final Logger log = LoggerFactory.getLogger(PdfBoxPdfParser.class);

    @Override
    public ParsedPdfDocument parse(MultipartFile file) {
        // 직접 작성한 내부 검증 메서드
        validatePdfFile(file);
        // Spring StringUtils 유틸: 경로 조작 문자열 정리
        String filename = StringUtils.cleanPath(file.getOriginalFilename());

        // PDFBox Loader: 바이트 배열에서 PDF 문서 객체(PDDocument) 생성
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            // PDFBox PDDocument: 총 페이지 수 조회
            int totalPages = document.getNumberOfPages();
            // PDFBox 텍스트 추출기
            PDFTextStripper stripper = new PDFTextStripper(); //페이지별 텍스트 추출 도구 
            List<ParsedPdfPage> pages = new ArrayList<>(totalPages);
            StringBuilder fullText = new StringBuilder();

            // 페이지별 결과와 전체 결과를 동시에 만드는 과정
            for (int page = 1; page <= totalPages; page++) {
                // PDFBox: 추출 시작/종료 페이지 지정 (한 페이지씩 추출)
                stripper.setStartPage(page);
                stripper.setEndPage(page);
                // PDFBox: 지정 페이지 텍스트 추출
                String pageText = normalizeText(stripper.getText(document));
                pages.add(new ParsedPdfPage(page, pageText));

                if (!pageText.isBlank()) {
                    if (!fullText.isEmpty()) {
                        fullText.append(System.lineSeparator());
                    }
                    fullText.append(pageText);
                }
            }

            ParsedPdfDocument parsed = new ParsedPdfDocument(filename, totalPages, pages, fullText.toString());
            log.info("PDF parsed successfully. file={}, pages={}, chars={}", filename, totalPages, parsed.fullText().length());
            return parsed;
        } catch (IOException ex) {
            // PDF 로딩/파싱 관련 예외를 도메인 예외로 변환
            log.error("Failed to parse PDF file. file={}", filename, ex);
            throw new PdfParsingException("PDF_PARSE_FAILED", "PDF 파싱에 실패했습니다.");
        }
    }

    private void validatePdfFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new PdfParsingException("PDF_FILE_REQUIRED", "PDF 파일이 필요합니다.");
        }
        String filename = file.getOriginalFilename();
        if (!StringUtils.hasText(filename) || !filename.toLowerCase().endsWith(".pdf")) {
            throw new PdfParsingException("PDF_FILE_REQUIRED", "PDF 파일만 파싱할 수 있습니다.");
        }
    }

    private String normalizeText(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\u0000", "").trim();
    }
}
