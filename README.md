# 청년 정책 RAG 챗봇

청년 정책 문서를 기반으로 답변하는 RAG(Retrieval-Augmented Generation) 챗봇 프로젝트입니다.  
문서를 업로드하면 임베딩/벡터 검색을 통해 관련 근거를 찾고, LLM이 답변을 생성합니다.

## 핵심 목표

- 정책 정보 탐색을 쉽게 만드는 챗봇 제공
- 웹사이트에 쉽게 붙일 수 있는 SDK 형태 제공
- Docker 기반으로 재현 가능한 개발/배포 환경 구성

## 예정 기능 (MVP)

- 문서 업로드 (PDF/TXT)
- 문서 청킹 및 임베딩 저장
- pgvector 유사도 검색
- `POST /api/chat` SSE 스트리밍 응답
- 기본 웹 SDK 위젯

## 기술 스택

- Backend: Spring Boot
- Database: PostgreSQL + pgvector
- AI: OpenAI Embedding / Chat API
- Admin UI: React
- SDK: Vanilla JavaScript
- Deploy: Docker, Docker Compose

## 빠른 시작

### 1) 환경 변수 준비

`.env.example`를 참고해서 `.env` 파일을 작성합니다.

```env
POSTGRES_USER=rag_user
POSTGRES_PASSWORD=your_password
POSTGRES_DB=rag_chatbot
```

### 2) DB 컨테이너 실행

```bash
docker compose up -d
```

### 3) pgvector extension 활성화

```bash
docker exec -it rag-postgres psql -U rag_user -d rag_chatbot
```

```sql
CREATE EXTENSION IF NOT EXISTS vector;
SELECT extname FROM pg_extension WHERE extname = 'vector';
```

