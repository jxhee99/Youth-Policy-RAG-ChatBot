package com.youthpolicy.ragchatbot.documents.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.youthpolicy.ragchatbot.documents.parser.error.PdfParsingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class PdfBoxPdfParserTest {

    private final PdfBoxPdfParser parser = new PdfBoxPdfParser();

    @Test
    void parse_extractsTextForFiveSamplePdfs() throws IOException {
        List<String> firstLines = List.of(
                "youth-policy-sample-1",
                "youth-policy-sample-2",
                "youth-policy-sample-3",
                "youth-policy-sample-4",
                "youth-policy-sample-5"
        );

        for (int i = 0; i < firstLines.size(); i++) {
            byte[] pdfBytes = createPdfBytes(List.of(firstLines.get(i)));
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "sample-" + (i + 1) + ".pdf",
                    "application/pdf",
                    pdfBytes
            );

            var parsed = parser.parse(file);

            assertThat(parsed.totalPages()).isEqualTo(1);
            assertThat(parsed.pages()).hasSize(1);
            assertThat(parsed.pages().getFirst().pageNumber()).isEqualTo(1);
            assertThat(parsed.pages().getFirst().text()).contains(firstLines.get(i));
            assertThat(parsed.fullText()).contains(firstLines.get(i));
        }
    }

    @Test
    void parse_extractsTextByPage() throws IOException {
        byte[] pdfBytes = createPdfBytes(List.of("first-page", "second-page"));
        MockMultipartFile file = new MockMultipartFile("file", "multi-page.pdf", "application/pdf", pdfBytes);

        var parsed = parser.parse(file);

        assertThat(parsed.totalPages()).isEqualTo(2);
        assertThat(parsed.pages()).hasSize(2);
        assertThat(parsed.pages().get(0).pageNumber()).isEqualTo(1);
        assertThat(parsed.pages().get(0).text()).contains("first-page");
        assertThat(parsed.pages().get(1).pageNumber()).isEqualTo(2);
        assertThat(parsed.pages().get(1).text()).contains("second-page");
    }

    @Test
    void parse_throwsWhenInvalidPdf() {
        MockMultipartFile file = new MockMultipartFile("file", "broken.pdf", "application/pdf", "not-a-pdf".getBytes());

        assertThatThrownBy(() -> parser.parse(file))
                .isInstanceOf(PdfParsingException.class)
                .hasMessageContaining("PDF 파싱");
    }

    @Test
    void parse_throwsWhenExtensionIsNotPdf() {
        MockMultipartFile file = new MockMultipartFile("file", "note.txt", "text/plain", "text".getBytes());

        assertThatThrownBy(() -> parser.parse(file))
                .isInstanceOf(PdfParsingException.class)
                .hasMessageContaining("PDF 파일만");
    }

    private byte[] createPdfBytes(List<String> pageTexts) throws IOException {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            for (String pageText : pageTexts) {
                PDPage page = new PDPage();
                document.addPage(page);
                try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
                    stream.beginText();
                    stream.setFont(font, 12);
                    stream.newLineAtOffset(72, 720);
                    stream.showText(pageText);
                    stream.endText();
                }
            }
            document.save(out);
            return out.toByteArray();
        }
    }
}
