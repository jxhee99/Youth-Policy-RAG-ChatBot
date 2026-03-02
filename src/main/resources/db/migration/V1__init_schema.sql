-- Flyway Migration Template
-- File: V1__init_schema.sql
-- Goal: Initialize base schema for RAG chatbot

-- 0) Enable required extension
CREATE EXTENSION IF NOT EXISTS vector;

-- 1) documents
CREATE TABLE IF NOT EXISTS documents (
    id BIGSERIAL PRIMARY KEY,
    title TEXT NOT NULL,
    source_type TEXT NOT NULL
        CHECK (source_type IN ('URL', 'FILE', 'MANUAL')),
    source TEXT NOT NULL,
    content_type TEXT NOT NULL,
    status TEXT NOT NULL
        CHECK (status IN ('UPLOADED', 'PARSED', 'EMBEDDED', 'FAILED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 2) chunks
CREATE TABLE IF NOT EXISTS chunks (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL
        REFERENCES documents(id) ON DELETE CASCADE,
    chunk_index INTEGER NOT NULL
        CHECK (chunk_index >= 0),
    content TEXT NOT NULL
        CHECK (length(content) > 0),
    embedding vector(1536) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (document_id, chunk_index)
);

-- 3) settings (single row with id=1)
CREATE TABLE IF NOT EXISTS settings (
    id INTEGER PRIMARY KEY
        CHECK (id = 1),
    system_prompt TEXT NOT NULL,
    theme_color VARCHAR(20) NOT NULL,
    widget_position TEXT NOT NULL
        CHECK (widget_position IN ('bottom-right', 'bottom-left', 'top-right', 'top-left')),
    starter_questions JSONB NOT NULL DEFAULT '[]'::jsonb,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 4) indexes
CREATE INDEX IF NOT EXISTS idx_documents_status
    ON documents (status);

CREATE INDEX IF NOT EXISTS idx_documents_created_at
    ON documents (created_at DESC);

-- 벡터 ANN 인덱스
CREATE INDEX IF NOT EXISTS idx_chunks_embedding_cosine
    ON chunks USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100);


-- TODO (later): add vector index after data grows
-- Example:
-- CREATE INDEX idx_chunks_embedding_cosine
--     ON chunks USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100);

-- 5) optional seed for settings
-- TODO: Replace system_prompt with your own prompt text
-- INSERT INTO settings (id, system_prompt, theme_color, widget_position, starter_questions)
-- VALUES (1, 'YOUR_SYSTEM_PROMPT', '#2563EB', 'bottom-right', '[]'::jsonb)
-- ON CONFLICT (id) DO NOTHING;
