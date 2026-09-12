CREATE TABLE cv_content_revisions (
    id UUID PRIMARY KEY,
    document_type VARCHAR(20) NOT NULL,
    document_id UUID NOT NULL,
    content JSONB NOT NULL,
    section_order JSONB NOT NULL,
    version BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX idx_cv_content_revisions_document ON cv_content_revisions(document_type, document_id, created_at DESC);
