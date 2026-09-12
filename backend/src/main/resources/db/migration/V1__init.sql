CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE master_cvs (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    name VARCHAR(150) NOT NULL,
    content JSONB NOT NULL,
    section_order JSONB NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_master_cv_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE INDEX idx_master_cvs_user_id ON master_cvs(user_id);

CREATE TABLE cv_language_versions (
    id UUID PRIMARY KEY,
    master_cv_id UUID NOT NULL,
    language_code VARCHAR(10) NOT NULL,
    name VARCHAR(150) NOT NULL,
    template_code VARCHAR(50) NOT NULL,
    content JSONB NOT NULL,
    section_order JSONB NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_language_version_master_cv FOREIGN KEY (master_cv_id) REFERENCES master_cvs(id) ON DELETE CASCADE,
    CONSTRAINT uq_master_cv_language UNIQUE (master_cv_id, language_code)
);
CREATE INDEX idx_cv_language_versions_master_cv_id ON cv_language_versions(master_cv_id);

CREATE TABLE cv_ai_suggestions (
    id UUID PRIMARY KEY,
    language_version_id UUID NOT NULL,
    operation VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    payload JSONB NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_ai_suggestion_language_version FOREIGN KEY (language_version_id) REFERENCES cv_language_versions(id) ON DELETE CASCADE
);
CREATE INDEX idx_cv_ai_suggestions_language_version_id ON cv_ai_suggestions(language_version_id);

INSERT INTO users (id, email, created_at, updated_at)
VALUES ('00000000-0000-0000-0000-000000000001', 'demo@cvsaas.local', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
