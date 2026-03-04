package com.youthpolicy.ragchatbot.documents.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.youthpolicy.ragchatbot.documents.dto.DocumentUploadResponse;
import com.youthpolicy.ragchatbot.documents.error.DocumentValidationException;
import com.youthpolicy.ragchatbot.documents.parser.PdfParser;
import com.youthpolicy.ragchatbot.documents.parser.dto.ParsedPdfDocument;
import com.youthpolicy.ragchatbot.documents.parser.dto.ParsedPdfPage;
import com.youthpolicy.ragchatbot.documents.repository.DocumentRepository;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class DocumentUploadServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private PdfParser pdfParser;

    @InjectMocks
    private DocumentUploadService documentUploadService;

    @Test
    void upload_throwsWhenFileIsEmpty() {
        MockMultipartFile file = new MockMultipartFile("file", "empty.pdf", "application/pdf", new byte[0]);

        assertThatThrownBy(() -> documentUploadService.upload(file))
                .isInstanceOf(DocumentValidationException.class)
                .hasMessageContaining("빈 파일");
        verifyNoInteractions(pdfParser, documentRepository);
    }

    @Test
    void upload_throwsWhenExtensionIsUnsupported() {
        MockMultipartFile file = new MockMultipartFile("file", "image.png", "image/png", "abc".getBytes());

        assertThatThrownBy(() -> documentUploadService.upload(file))
                .isInstanceOf(DocumentValidationException.class)
                .hasMessageContaining("지원하지 않는 파일 형식");
        verifyNoInteractions(pdfParser, documentRepository);
    }

    @Test
    void upload_savesMetadataWhenFileIsValid() {
        MockMultipartFile file = new MockMultipartFile("file", "policy-guide.pdf", "application/pdf", "pdf-content".getBytes());
        DocumentUploadResponse response = new DocumentUploadResponse(
                1L,
                "policy-guide",
                "FILE",
                "policy-guide.pdf",
                "application/pdf",
                "UPLOADED",
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );
        when(documentRepository.insertUploadedDocument(eq("policy-guide"), eq("policy-guide.pdf"), eq("application/pdf")))
                .thenReturn(response);
        when(pdfParser.parse(file)).thenReturn(new ParsedPdfDocument(
                "policy-guide.pdf",
                1,
                List.of(new ParsedPdfPage(1, "content")),
                "content"
        ));

        documentUploadService.upload(file);

        verify(pdfParser).parse(file);
        verify(documentRepository).insertUploadedDocument(eq("policy-guide"), eq("policy-guide.pdf"), eq("application/pdf"));
    }

    @Test
    void upload_savesMetadataWhenDocxFileIsValid() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "youth-policy.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "docx-content".getBytes()
        );
        String expectedContentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        DocumentUploadResponse response = new DocumentUploadResponse(
                2L,
                "youth-policy",
                "FILE",
                "youth-policy.docx",
                expectedContentType,
                "UPLOADED",
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );
        when(documentRepository.insertUploadedDocument(eq("youth-policy"), eq("youth-policy.docx"), eq(expectedContentType)))
                .thenReturn(response);

        documentUploadService.upload(file);

        verifyNoInteractions(pdfParser);
        verify(documentRepository).insertUploadedDocument(eq("youth-policy"), eq("youth-policy.docx"), eq(expectedContentType));
    }
}
