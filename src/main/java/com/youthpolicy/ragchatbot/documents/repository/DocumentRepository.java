package com.youthpolicy.ragchatbot.documents.repository;

import com.youthpolicy.ragchatbot.documents.dto.DocumentUploadResponse;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class DocumentRepository {

    private static final String SOURCE_TYPE_FILE = "FILE";
    private static final String STATUS_UPLOADED = "UPLOADED";

    // RowMapper는 DB 조회 결과(ResultSet) 한 행을 자바 객체(DocumentUploadResponse)로 변환하는 함수 타입이다.
    private static final RowMapper<DocumentUploadResponse> ROW_MAPPER = DocumentRepository::mapRow;

    private final JdbcTemplate jdbcTemplate;

    // JdbcTemplate은 Spring이 생성/관리하는 DB 접근 유틸이며, 생성자 주입으로 안전하게 재사용한다.
    public DocumentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public DocumentUploadResponse insertUploadedDocument(String title, String source, String contentType) {
        String sql = """
                INSERT INTO documents (title, source_type, source, content_type, status)
                VALUES (?, ?, ?, ?, ?)
                RETURNING id, title, source_type, source, content_type, status, created_at, updated_at
                """;
        return jdbcTemplate.queryForObject(sql, ROW_MAPPER, title, SOURCE_TYPE_FILE, source, contentType, STATUS_UPLOADED);
    }

    // ResultSet은 JDBC(자바 표준 DB 인터페이스)와 드라이버가 만들어 주는 조회 결과 커서이다.
    // RowMapper 시그니처상 rowNum 인자를 받지만, 현재 매핑에서는 값 자체를 사용하지 않는다.
    private static DocumentUploadResponse mapRow(ResultSet rs, int ignoredRowNum) throws SQLException {
        return new DocumentUploadResponse(
                rs.getLong("id"),
                rs.getString("title"),
                rs.getString("source_type"),
                rs.getString("source"),
                rs.getString("content_type"),
                rs.getString("status"),
                rs.getObject("created_at", java.time.OffsetDateTime.class),
                rs.getObject("updated_at", java.time.OffsetDateTime.class)
        );
    }
}
