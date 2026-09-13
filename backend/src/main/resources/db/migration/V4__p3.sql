CREATE TABLE cover_letters (
    id UUID PRIMARY KEY,
    master_cv_id UUID NOT NULL,
    language_code VARCHAR(10) NOT NULL,
    title VARCHAR(150) NOT NULL,
    recipient VARCHAR(255),
    company VARCHAR(255),
    position VARCHAR(255),
    body TEXT NOT NULL DEFAULT '',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_cover_letter_master_cv FOREIGN KEY (master_cv_id) REFERENCES master_cvs(id) ON DELETE CASCADE
);
CREATE INDEX idx_cover_letters_master_cv_id ON cover_letters(master_cv_id);

CREATE TABLE share_links (
    id UUID PRIMARY KEY,
    token VARCHAR(64) NOT NULL UNIQUE,
    master_cv_id UUID NOT NULL,
    language_version_id UUID NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_share_link_master_cv FOREIGN KEY (master_cv_id) REFERENCES master_cvs(id) ON DELETE CASCADE,
    CONSTRAINT fk_share_link_language_version FOREIGN KEY (language_version_id) REFERENCES cv_language_versions(id) ON DELETE CASCADE
);
CREATE INDEX idx_share_links_master_cv_id ON share_links(master_cv_id);

CREATE TABLE ai_usage (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    operation VARCHAR(30) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_ai_usage_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE INDEX idx_ai_usage_user_created ON ai_usage(user_id, created_at DESC);
