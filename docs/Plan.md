# CVFlow — Master Coding Agent Prompt

## 1. ROLE

You are a senior full-stack software engineer responsible for implementing a production-quality SaaS application.

You must:

- Follow the architecture and technical decisions defined in this document.
- Do not replace technologies without a strong technical reason.
- Do not introduce unnecessary complexity.
- Do not add technologies that are not required.
- Prefer clean, maintainable, testable and scalable code.
- Follow SOLID principles and Clean Code practices.
- Keep responsibilities separated between Controller, Service, Repository and external integrations.
- Never silently change product requirements.
- If an implementation detail is not explicitly defined, choose the simplest reasonable solution that fits the existing architecture.
- Do not ask unnecessary questions. Make reasonable implementation decisions and continue.
- Before implementing a feature, inspect the existing code and preserve consistency with the current architecture.

The application should be implemented incrementally and should remain runnable after each major phase.

---

# 2. PROJECT OVERVIEW

Build a SaaS application for managing CVs/resumes.

The core product idea is:

> Write your CV once. Generate every language and version automatically.

The application allows a user to:

- Create multiple Master CVs.
- Edit structured CV information.
- Create language versions.
- Edit language versions independently.
- Translate CV content with AI.
- Improve CV wording with AI.
- Rewrite CV wording with AI.
- Accept or reject AI suggestions.
- Select different CV templates.
- Preview the CV.
- Export the CV as PDF.

The application is primarily intended for professional software developers but the data model should remain generic enough for other professions.

---

# 3. CORE PRODUCT MODEL

The application follows this structure:

```text
Master CV
    |
    +-- Turkish Version
    |
    +-- English Version
    |
    +-- Other future languages
           |
           +-- Template
           |
           +-- PDF
```

Important:

Language versions are independently editable.

Creating a translation does NOT permanently bind the target language version to the source version.

After creation, the user can manually modify the translated version.

AI suggestions must NEVER silently mutate CV content.

The AI always creates a suggestion first.

```text
AI
 ↓
Suggestion
 ↓
PENDING
 ↓
User accepts/rejects
```

---

# 4. MVP AI FEATURES

Implement only these AI operations in MVP:

```text
TRANSLATE
IMPROVE
REWRITE
```

Do NOT implement ATS optimization, job-description matching, job-specific CV tailoring, cover letters or interview generation in MVP.

These can be added later.

---

# 5. TECHNOLOGY STACK

## Backend

```text
Java 25 LTS
Spring Boot 4.1.x
Spring Web
Spring Data JPA
Hibernate
PostgreSQL
Flyway
Lombok
JUnit 5
Mockito
Testcontainers
```

Use Java 25 features where appropriate but do not use unnecessary experimental language features.

## Frontend

```text
React
TypeScript
Vite
React Router
TanStack Query
Zustand
React Hook Form
Zod
Tailwind CSS
```

The frontend is a separate application communicating with the backend through REST APIs.

Do NOT use Next.js.

## AI

Use Google's Gemini API.

Use the Gemini REST API through Spring's `RestClient`.

Do NOT tightly couple the business layer to Gemini.

Use:

```text
AiService
    ↓
AiClient
    ↓
GeminiClient
    ↓
Gemini REST API
```

The model should be configurable through application configuration.

Default model:

```text
gemini-2.5-flash
```

Do not hardcode the API key.

Use:

```text
GEMINI_API_KEY
```

environment variable.

## PDF

Generate PDFs using:

```text
HTML
+
CSS
↓
Chromium
↓
PDF
```

Chromium should run through Docker.

Do not persist generated PDF files in PostgreSQL in MVP.

---

# 6. REPOSITORY STRUCTURE

Use a monorepo:

```text
cvflow/
│
├── backend/
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/
│       │   └── resources/
│       │       ├── application.yml
│       │       └── db/
│       │           └── migration/
│       └── test/
│
├── frontend/
│
├── docker-compose.yml
│
└── README.md
```

Backend and frontend remain separate applications.

---

# 7. BACKEND ARCHITECTURE

Use package-by-feature where appropriate.

Recommended structure:

```text
backend/src/main/java/.../

├── common/
│   ├── exception/
│   ├── response/
│   └── security/
│
├── cv/
│   ├── controller/
│   ├── dto/
│   │   ├── request/
│   │   └── response/
│   ├── entity/
│   ├── mapper/
│   ├── model/
│   ├── repository/
│   └── service/
│
├── ai/
│   ├── client/
│   ├── config/
│   ├── controller/
│   ├── dto/
│   ├── entity/
│   ├── repository/
│   ├── service/
│   └── validator/
│
└── pdf/
    ├── controller/
    ├── service/
    └── template/
```

Do not put all classes into generic global packages such as:

```text
controller/
service/
repository/
entity/
```

Feature separation should remain clear.

---

# 8. DATABASE

Use PostgreSQL.

Use Flyway migrations.

Hibernate must use:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
```

Hibernate must NOT automatically create or modify the schema.

All schema changes must go through Flyway.

Use UUID for IDs.

Use:

```java
@Id
@GeneratedValue(strategy = GenerationType.UUID)
private UUID id;
```

Use:

```java
Instant
```

for timestamps.

PostgreSQL timestamp columns:

```sql
TIMESTAMP WITH TIME ZONE
```

---

# 9. DATABASE SCHEMA

## users

```sql
CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);
```

## master_cvs

```sql
CREATE TABLE master_cvs (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    name VARCHAR(150) NOT NULL,
    content JSONB NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT fk_master_cv_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);
```

Create an index on:

```text
user_id
```

## cv_language_versions

```sql
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

    CONSTRAINT fk_language_version_master_cv
        FOREIGN KEY (master_cv_id)
        REFERENCES master_cvs(id)
        ON DELETE CASCADE,

    CONSTRAINT uq_master_cv_language
        UNIQUE (master_cv_id, language_code)
);
```

Create an index on:

```text
master_cv_id
```

## cv_ai_suggestions

```sql
CREATE TABLE cv_ai_suggestions (
    id UUID PRIMARY KEY,
    language_version_id UUID NOT NULL,
    operation VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    payload JSONB NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_ai_suggestion_language_version
        FOREIGN KEY (language_version_id)
        REFERENCES cv_language_versions(id)
        ON DELETE CASCADE
);
```

Create an index on:

```text
language_version_id
```

Do not add GIN indexes for JSONB in MVP unless an actual query requires them.

---

# 10. ENUMS

Create:

```java
public enum LanguageCode {
    TR,
    EN
}
```

```java
public enum TemplateCode {
    MODERN,
    MINIMAL,
    PROFESSIONAL
}
```

```java
public enum AiOperation {
    TRANSLATE,
    IMPROVE,
    REWRITE
}
```

```java
public enum AiSuggestionStatus {
    PENDING,
    ACCEPTED,
    REJECTED
}
```

```java
public enum CvSection {
    SUMMARY,
    EXPERIENCE,
    PROJECTS,
    EDUCATION,
    SKILLS,
    LANGUAGES,
    CERTIFICATIONS,
    LINKS
}
```

`CvSection.defaultOrder()` should return:

```text
SUMMARY
EXPERIENCE
PROJECTS
EDUCATION
SKILLS
LANGUAGES
CERTIFICATIONS
LINKS
```

---

# 11. JSONB CV MODEL

CV content is stored as JSONB.

Do NOT create separate relational tables for every CV section in MVP.

Use Java POJOs mapped as JSONB.

Use Hibernate:

```java
@JdbcTypeCode(SqlTypes.JSON)
@Column(columnDefinition = "jsonb", nullable = false)
```

Create:

```text
model/
├── CvContent
├── PersonalInfo
├── Experience
├── Education
├── Project
├── Skill
├── Language
├── Certification
└── Link
```

---

# 12. CvContent

```java
public class CvContent {

    private PersonalInfo personal;

    private String summary;

    private List<Experience> experiences;

    private List<Education> education;

    private List<Project> projects;

    private List<Skill> skills;

    private List<Language> languages;

    private List<Certification> certifications;

    private List<Link> links;
}
```

Lists should default to empty lists.

Provide:

```java
public static CvContent empty()
```

which creates an empty but valid CV structure.

---

# 13. CV MODELS

Experience:

```java
public class Experience {
    private UUID id;
    private String company;
    private String position;
    private String location;
    private String startDate;
    private String endDate;
    private boolean current;
    private List<String> description;
}
```

Dates are represented as:

```text
YYYY-MM
```

Do not use `YearMonth` for JSON/API models.

Education:

```java
public class Education {
    private UUID id;
    private String institution;
    private String degree;
    private String field;
    private String location;
    private String startDate;
    private String endDate;
    private String description;
}
```

Project:

```java
public class Project {
    private UUID id;
    private String name;
    private String description;
    private List<String> technologies;
    private String url;
}
```

Skill:

```java
public class Skill {
    private UUID id;
    private String name;
    private String category;
}
```

Language:

```java
public class Language {
    private UUID id;
    private String name;
    private String level;
}
```

Certification:

```java
public class Certification {
    private UUID id;
    private String name;
    private String issuer;
    private String date;
    private String url;
}
```

Link:

```java
public class Link {
    private UUID id;
    private String label;
    private String url;
}
```

PersonalInfo:

```java
public class PersonalInfo {
    private String fullName;
    private String title;
    private String email;
    private String phone;
    private String location;
    private String website;
    private String linkedin;
    private String github;
}
```

---

# 14. ENTITIES

Create:

```text
User
MasterCv
CvLanguageVersion
CvAiSuggestion
```

Use UUID IDs.

Use `@Version Long version` on:

```text
MasterCv
CvLanguageVersion
```

This provides optimistic locking.

Do not manually implement database version increments.

---

# 15. MASTER CV ENTITY

`MasterCv` contains:

```text
id
userId
name
content
createdAt
updatedAt
version
```

Use:

```java
@CreationTimestamp
```

and:

```java
@UpdateTimestamp
```

where appropriate.

The entity should not contain unnecessary relationships.

Using explicit UUID foreign-key fields instead of JPA relationships is intentional for MVP simplicity.

---

# 16. LANGUAGE VERSION ENTITY

`CvLanguageVersion` contains:

```text
id
masterCvId
languageCode
name
templateCode
content
sectionOrder
createdAt
updatedAt
version
```

`sectionOrder` is stored as JSONB.

There must be only one language version per language for a Master CV.

Enforce this with:

```sql
UNIQUE(master_cv_id, language_code)
```

---

# 17. AI SUGGESTION ENTITY

`CvAiSuggestion` contains:

```text
id
languageVersionId
operation
status
payload
createdAt
updatedAt
```

The payload is JSONB.

AI suggestions are temporary proposed changes.

The AI suggestion itself does not modify CV content.

---

# 18. AI SUGGESTION PAYLOAD

Create:

```java
public class AiSuggestionPayload {

    private CvSection section;

    private UUID entityId;

    private String field;

    private String original;

    private String suggested;
}
```

For example:

```json
{
  "section": "EXPERIENCE",
  "entityId": "uuid",
  "field": "description",
  "original": "Developed backend services with Java.",
  "suggested": "Developed backend services using Java."
}
```

---

# 19. API RESPONSE WRAPPER

All JSON API responses should use:

```java
public record ApiResponse<T>(
        boolean success,
        T data,
        ApiError error,
        Instant timestamp
) {}
```

Error:

```java
public record ApiError(
        String code,
        String message,
        Map<String, String> details
) {}
```

Provide helper methods:

```text
ApiResponses.success(data)
ApiResponses.success()
ApiResponses.error(...)
```

PDF binary responses do NOT use this wrapper.

---

# 20. EXCEPTION HANDLING

Create:

```text
ApiException
CvNotFoundException
LanguageVersionNotFoundException
LanguageVersionAlreadyExistsException
SuggestionNotFoundException
InvalidSuggestionStateException
OptimisticLockException
InvalidSectionOrderException
AccessDeniedException
AiServiceException
```

Use a global exception handler.

Map:

```text
404 → Not Found
400 → Validation / invalid request
403 → Access Denied
409 → Conflict / optimistic locking / duplicate
500 → Unexpected server error
```

Validation response:

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Request validation failed",
    "details": {
      "name": "must not be blank"
    }
  },
  "timestamp": "..."
}
```

Never expose stack traces or internal exception details to the client.

---

# 21. MOCK AUTHENTICATION

Authentication is NOT part of MVP.

However, the architecture must support adding authentication later.

Create:

```text
common/security/
├── CurrentUserService
└── CvAccessService
```

For MVP:

```java
private static final UUID MOCK_USER_ID =
        UUID.fromString(
            "00000000-0000-0000-0000-000000000001"
        );
```

`CurrentUserService.getCurrentUserId()` returns this ID.

Flyway V1 must insert:

```sql
INSERT INTO users (
    id,
    email,
    created_at,
    updated_at
)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    'demo@cvsaas.local',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);
```

---

# 22. RESOURCE OWNERSHIP

Never trust a user-provided CV ID.

Every CV/resource operation must verify ownership.

`CvAccessService` must provide:

```java
MasterCv getOwnedCv(UUID cvId);

CvLanguageVersion getOwnedLanguageVersion(UUID languageVersionId);
```

If the resource exists but belongs to another user:

```text
403 ACCESS_DENIED
```

Do not expose whether another user's resource exists.

---

# 23. CV API

Implement:

```text
GET    /api/v1/cvs
POST   /api/v1/cvs
GET    /api/v1/cvs/{cvId}
PUT    /api/v1/cvs/{cvId}
DELETE /api/v1/cvs/{cvId}
POST   /api/v1/cvs/{cvId}/duplicate
```

---

# 24. CREATE CV

Request:

```json
{
  "name": "Backend Developer CV"
}
```

Response:

```text
201 Created
```

The created CV should contain:

```text
empty CvContent
default section order
current mock user
```

---

# 25. UPDATE CV

Request:

```json
{
  "name": "Backend Developer CV",
  "content": {},
  "sectionOrder": [
    "SUMMARY",
    "EXPERIENCE",
    "PROJECTS",
    "EDUCATION",
    "SKILLS",
    "LANGUAGES",
    "CERTIFICATIONS",
    "LINKS"
  ],
  "version": 5
}
```

The supplied version must be used for optimistic locking.

If the client sends an outdated version, return:

```text
409 CONFLICT
```

Do not overwrite newer data.

---

# 26. SECTION ORDER VALIDATION

`sectionOrder` must:

- contain only valid `CvSection` values
- contain no duplicates
- represent a valid section order

Create:

```text
InvalidSectionOrderException
```

Do not silently remove duplicate sections.

Return a validation/conflict error.

---

# 27. DUPLICATE CV

Endpoint:

```text
POST /api/v1/cvs/{cvId}/duplicate
```

Duplicate:

```text
Master CV
+
content
+
section order
+
all language versions
```

Do NOT duplicate:

```text
AI suggestions
```

Use deep copies for JSONB content.

The entire operation must be transactional.

Suggested duplicate name:

```text
Original Name (Copy)
```

---

# 28. LANGUAGE VERSION API

Implement:

```text
GET    /api/v1/cvs/{cvId}/languages
POST   /api/v1/cvs/{cvId}/languages
GET    /api/v1/cvs/{cvId}/languages/{language}
PUT    /api/v1/cvs/{cvId}/languages/{language}
DELETE /api/v1/cvs/{cvId}/languages/{language}
```

---

# 29. CREATE LANGUAGE VERSION

Request:

```json
{
  "languageCode": "EN",
  "name": "English",
  "templateCode": "MODERN"
}
```

The new version initially copies the Master CV content.

Its section order initially copies the Master CV section order.

If that language already exists:

```text
409 CONFLICT
```

---

# 30. LANGUAGE VERSION INDEPENDENCE

Once a language version is created:

```text
Master CV
     |
     | initial copy
     ↓
English Version
```

The English version becomes independently editable.

Later changes to Master CV must NOT silently overwrite the English version.

---

# 31. DTO PACKAGE

Use:

```text
dto/
├── request/
└── response/
```

Create:

```java
CreateCvRequest
UpdateCvRequest
CvSummaryResponse
CvResponse

CreateLanguageVersionRequest
UpdateLanguageVersionRequest
LanguageVersionSummaryResponse
LanguageVersionResponse

PdfExportRequest
```

Use Java records for DTOs.

Validate requests with Jakarta Validation.

---

# 32. REPOSITORIES

Create:

```java
UserRepository
MasterCvRepository
CvLanguageVersionRepository
CvAiSuggestionRepository
```

Important methods:

```java
Optional<User> findByEmail(String email);
```

```java
List<MasterCv>
findAllByUserIdOrderByUpdatedAtDesc(UUID userId);
```

```java
List<CvLanguageVersion>
findAllByMasterCvId(UUID masterCvId);
```

```java
Optional<CvLanguageVersion>
findByMasterCvIdAndLanguageCode(
    UUID masterCvId,
    LanguageCode languageCode
);
```

AI suggestions:

```java
List<CvAiSuggestion>
findAllByLanguageVersionId(UUID languageVersionId);
```

```java
List<CvAiSuggestion>
findAllByLanguageVersionIdAndStatus(
    UUID languageVersionId,
    AiSuggestionStatus status
);
```

---

# 33. CV CONTENT CLONING

Create:

```text
CvContentCloner
```

Use Jackson `ObjectMapper` to perform a deep copy.

Example:

```java
public CvContent clone(CvContent source) {
    return objectMapper.convertValue(
        objectMapper.valueToTree(source),
        CvContent.class
    );
}
```

Do not perform shallow copies when duplicating CVs.

---

# 34. AI ARCHITECTURE

Use:

```text
AiController
    ↓
AiService
    ↓
AiClient
    ↓
GeminiClient
    ↓
Gemini REST API
```

`AiService` contains business logic.

`GeminiClient` contains provider-specific logic.

Do NOT put Gemini API calls inside `AiService`.

---

# 35. AI CLIENT

Create:

```java
public interface AiClient {

    String improve(String content);

    String rewrite(String content);

    String translate(
        String content,
        LanguageCode sourceLanguage,
        LanguageCode targetLanguage
    );
}
```

Implement:

```text
GeminiClient
```

---

# 36. GEMINI CONFIGURATION

Use:

```yaml
app:
  ai:
    gemini:
      api-key: ${GEMINI_API_KEY}
      model: gemini-2.5-flash
      base-url: https://generativelanguage.googleapis.com
```

Do not commit API keys.

Do not put secrets in source code.

---

# 37. GEMINI REST CLIENT

Use Spring `RestClient`.

Configure:

```text
base URL
x-goog-api-key
Content-Type: application/json
```

Call:

```text
/v1beta/models/{model}:generateContent
```

Do not use a blocking external API call without a reasonable timeout.

Configure connection/read timeouts.

Gemini failures must be converted into application-level `AiServiceException`.

Do not leak Gemini's raw response to the frontend.

---

# 38. GEMINI STRUCTURED OUTPUT

Use Gemini structured output.

The response must be JSON.

Use:

```text
responseMimeType = application/json
```

Use a JSON schema similar to:

```json
{
  "type": "OBJECT",
  "properties": {
    "suggested": {
      "type": "STRING"
    }
  },
  "required": [
    "suggested"
  ]
}
```

The Gemini response should conceptually be:

```json
{
  "suggested": "..."
}
```

Parse the JSON using Jackson.

If parsing fails:

```text
AI_INVALID_RESPONSE
```

If Gemini returns no usable candidate:

```text
AI_EMPTY_RESPONSE
```

---

# 39. AI PROMPT RULES

Every AI prompt must enforce:

- Never invent facts.
- Never invent technologies.
- Never invent companies.
- Never invent responsibilities.
- Never invent achievements.
- Never invent metrics.
- Never invent dates.
- Never change factual information.
- Preserve meaning.
- Use professional CV language.
- Keep output concise.
- Do not add unnecessary explanations.
- Return structured JSON only.

---

# 40. IMPROVE

Improve should:

- preserve the original meaning
- preserve factual information
- improve grammar
- improve clarity
- improve professional tone
- improve CV-oriented wording
- avoid unnecessary rewriting

Example:

Input:

```text
Developed backend services with Java.
```

Possible output:

```text
Developed backend services using Java.
```

The model must not introduce unsupported technologies.

---

# 41. REWRITE

Rewrite should:

- substantially improve wording
- make the text stronger
- remain factually identical
- not invent information

Difference:

```text
IMPROVE
→ minimal/moderate wording improvement

REWRITE
→ stronger rephrasing
```

---

# 42. TRANSLATE

Translation must:

- preserve exact meaning
- not add information
- not remove information
- use professional CV terminology
- preserve technical terminology
- preserve company names where appropriate
- preserve dates
- preserve factual information

MVP languages:

```text
TR
EN
```

The architecture should make adding languages later easy.

---

# 43. AI API

Implement:

```text
POST /api/v1/ai/translate
POST /api/v1/ai/improve
POST /api/v1/ai/rewrite
POST /api/v1/ai/suggestions/{id}/accept
POST /api/v1/ai/suggestions/{id}/reject
```

---

# 44. IMPROVE REQUEST

```java
public record ImproveRequest(
    @NotNull UUID languageVersionId,
    @NotNull CvSection section,
    UUID entityId,
    @NotBlank String field,
    @NotBlank String content
) {}
```

Rewrite uses the same structure.

---

# 45. TRANSLATE REQUEST

```java
public record TranslateRequest(
    @NotNull UUID sourceLanguageVersionId,
    @NotNull UUID targetLanguageVersionId
) {}
```

---

# 46. AI SUGGESTION RESPONSE

```java
public record AiSuggestionResponse(
    UUID id,
    AiOperation operation,
    AiSuggestionStatus status,
    AiSuggestionPayload payload,
    Instant createdAt
) {}
```

---

# 47. FIELD VALIDATION

Never allow AI requests to modify arbitrary object fields.

Create:

```text
CvFieldValidator
```

Allowed fields:

```text
SUMMARY
    summary

EXPERIENCE
    company
    position
    description

EDUCATION
    institution
    degree
    field
    description

PROJECTS
    name
    description

SKILLS
    name
    category

LANGUAGES
    name
    level

CERTIFICATIONS
    name
    issuer
    date

LINKS
    label
    url
```

Invalid field:

```text
400 INVALID_FIELD
```

The validator must also verify that `entityId` is appropriate for sections containing lists.

Do not use reflection to allow arbitrary field modification.

---

# 48. AI SERVICE

`AiService` responsibilities:

- validate ownership
- validate section
- validate field
- retrieve current content
- call `AiClient`
- create suggestion
- persist suggestion as `PENDING`
- return suggestion

It must NOT directly modify CV content when generating a suggestion.

---

# 49. ACCEPT SUGGESTION

Flow:

```text
Suggestion PENDING
       ↓
validate ownership
       ↓
validate suggestion state
       ↓
validate target
       ↓
apply suggested value
       ↓
status = ACCEPTED
       ↓
transaction commit
```

All steps must happen inside one transaction.

If applying the suggestion fails, the transaction must roll back.

---

# 50. REJECT SUGGESTION

Flow:

```text
Suggestion PENDING
       ↓
validate ownership
       ↓
status = REJECTED
```

No CV content should change.

---

# 51. INVALID SUGGESTION STATE

Only:

```text
PENDING
```

suggestions can be accepted or rejected.

Trying to accept/reject:

```text
ACCEPTED
REJECTED
```

must return:

```text
409 CONFLICT
```

with an appropriate error code.

---

# 52. BULK TRANSLATION

Translation should support full-CV translation.

The preferred MVP implementation is:

```text
Source CV
    ↓
one structured Gemini request
    ↓
translated structured content
    ↓
compare source/target fields
    ↓
create individual suggestions
```

Do not create one Gemini API call for every field.

Avoid unnecessary API calls.

If translation becomes slow in the future, asynchronous processing can be introduced.

Do NOT add Kafka or RabbitMQ in MVP.

---

# 53. ACCEPT ALL

Provide a mechanism for accepting all pending suggestions belonging to a translation operation.

Acceptance should happen in one transaction.

If one suggestion fails, the transaction should roll back.

Do not partially apply a bulk acceptance.

---

# 54. FRONTEND PRODUCT STRUCTURE

Frontend should be a React + TypeScript + Vite application.

Recommended structure:

```text
frontend/
├── src/
│   ├── api/
│   ├── components/
│   ├── features/
│   │   ├── cvs/
│   │   ├── languages/
│   │   ├── ai/
│   │   └── pdf/
│   ├── layouts/
│   ├── pages/
│   ├── stores/
│   ├── types/
│   ├── hooks/
│   ├── router/
│   └── main.tsx
```

Use feature-based organization.

---

# 55. FRONTEND PAGES

MVP pages:

```text
Dashboard
CV List / Management
CV Editor
Language Version Editor
AI Suggestions
CV Preview
```

---

# 56. CV EDITOR

The main CV editor should have:

```text
LEFT
→ structured section editor

RIGHT
→ live CV preview
```

Example:

```text
+----------------------+----------------------------+
| CV Editor            | CV Preview                 |
|                      |                            |
| Personal Information |        CV DOCUMENT         |
| Summary              |                            |
| Experience           |                            |
| Education            |                            |
| Projects             |                            |
| Skills               |                            |
| Languages            |                            |
| Certifications       |                            |
| Links                |                            |
+----------------------+----------------------------+
```

---

# 57. SECTION MANAGEMENT

The user must be able to:

- edit sections
- add entries
- remove entries
- reorder sections
- reorder list entries where appropriate

Section order should be persisted as:

```text
sectionOrder
```

Drag-and-drop can be used on the frontend.

---

# 58. AUTOSAVE

Implement:

```text
debounced autosave ≈ 800ms
```

Also provide a manual:

```text
Save
```

button.

Manual Save should persist immediately.

Use TanStack Query for server state.

Use Zustand only for appropriate local UI/application state.

Do not put all server state into Zustand.

---

# 59. OPTIMISTIC LOCKING FRONTEND

The frontend must send the latest:

```text
version
```

with update requests.

If backend returns:

```text
409 CONFLICT
```

because of optimistic locking:

- do not silently overwrite data
- show an appropriate conflict message
- refresh or provide a recovery mechanism

---

# 60. AI UI

For editable fields provide actions such as:

```text
Improve
Rewrite
Translate
```

When an AI suggestion arrives:

```text
Original
    ↓
Suggested
```

The user should be able to:

```text
Accept
Reject
```

For bulk translation:

```text
Accept All
```

must be available.

AI-generated content must be visually distinguishable from saved content.

---

# 61. TEMPLATE SYSTEM

MVP templates:

```text
MODERN
MINIMAL
PROFESSIONAL
```

Templates should be implemented as HTML/CSS rendering strategies.

Do NOT build a template designer in MVP.

The user selects a template for each language version.

---

# 62. LIVE PREVIEW

The frontend should render a CV preview based on:

```text
CvContent
+
sectionOrder
+
templateCode
```

The preview should closely resemble the generated PDF.

Avoid creating a completely different frontend-only design from the PDF template.

The same conceptual template structure should drive both.

---

# 63. PDF GENERATION

Endpoint:

```text
POST /api/v1/cvs/{cvId}/export/pdf
```

Request:

```json
{
  "languageCode": "EN"
}
```

Response:

```text
Content-Type: application/pdf
```

Use:

```text
Content-Disposition: attachment; filename="cv.pdf"
```

Do not wrap the PDF in `ApiResponse`.

---

# 64. PDF ARCHITECTURE

Use:

```text
CvContent
   ↓
Template Renderer
   ↓
HTML
   ↓
Chromium
   ↓
PDF bytes
   ↓
HTTP response
```

Generated PDF should not be stored in PostgreSQL.

---

# 65. DOCKER

Create Docker Compose for local development.

At minimum:

```yaml
services:

  postgres:
    image: postgres:17
    container_name: cvflow-postgres
    environment:
      POSTGRES_DB: cv_saas
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    restart: unless-stopped
```

Chromium/PDF infrastructure should also be container-compatible.

Do not require Docker for basic backend unit tests.

---

# 66. APPLICATION CONFIGURATION

Default:

```yaml
spring:
  application:
    name: cvflow-backend

  datasource:
    url: jdbc:postgresql://localhost:5432/cv_saas
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false
    properties:
      hibernate:
        format_sql: true

  flyway:
    enabled: true
    locations: classpath:db/migration

server:
  port: 8080

app:
  ai:
    gemini:
      api-key: ${GEMINI_API_KEY}
      model: gemini-2.5-flash
      base-url: https://generativelanguage.googleapis.com
```

For production, secrets must be provided through environment variables.

---

# 67. TESTING STRATEGY

Implement three levels of testing.

## Unit Tests

Test:

```text
CvContent.empty()
CvContentCloner
CvMapper
CvFieldValidator
section order validation
DTO validation where useful
```

## Service Tests

Use:

```text
JUnit 5
Mockito
```

Test:

```text
CV create
CV update
CV delete
CV duplicate

language creation
duplicate language prevention
language update
language delete

AI suggestion creation
AI suggestion ownership
accept
reject
invalid state
```

## Integration Tests

Use:

```text
Testcontainers PostgreSQL
```

Test:

```text
Spring Boot
+
PostgreSQL
+
Flyway
+
Hibernate
+
repositories
+
controllers
```

Verify actual JSONB persistence and optimistic locking.

---

# 68. CODE QUALITY

Follow these rules:

- Prefer constructor injection.
- Use `@RequiredArgsConstructor` where appropriate.
- Avoid field injection.
- Use DTOs at API boundaries.
- Do not expose JPA entities directly from controllers.
- Keep controllers thin.
- Put business logic in services.
- Keep repositories focused on persistence.
- Keep external API logic isolated.
- Avoid giant service classes.
- Avoid giant controllers.
- Avoid duplicated code.
- Avoid unnecessary abstractions.
- Use meaningful names.
- Prefer immutable DTOs using records.
- Validate all external input.
- Never trust frontend validation alone.
- Never hardcode secrets.
- Never expose internal stack traces.
- Use transactions intentionally.
- Use `@Transactional(readOnly = true)` for read operations where appropriate.
- Use `@Transactional` for mutation operations.
- Do not use `open-in-view`.

---

# 69. SECURITY RULES

Even though authentication is mocked in MVP:

- Always validate ownership.
- Never trust IDs supplied by the frontend.
- Never allow arbitrary JSON fields to be modified through AI.
- Never allow arbitrary reflection-based field modification.
- Never expose Gemini API key to the frontend.
- Gemini API calls must only happen from the backend.
- Never store API keys in Git.
- Never return internal exception details.
- Validate URLs and user-provided content where appropriate.
- Prepare the architecture for future JWT authentication.

---

# 70. API DOCUMENTATION

Add OpenAPI/Swagger documentation if practical after the core API is stable.

Do not let API documentation delay core implementation.

Document:

- endpoints
- request models
- response models
- error responses
- PDF endpoint

---

# 71. IMPLEMENTATION ORDER

Implement the project in this exact general order.

## Phase 1 — Project foundation

```text
Backend project
PostgreSQL
Docker Compose
Flyway
application.yml
basic health check
```

Verify:

```text
Spring Boot starts
PostgreSQL connects
Flyway succeeds
```

## Phase 2 — CV core

Implement:

```text
User
MasterCv
CvContent
repositories
DTOs
mapper
exceptions
GlobalExceptionHandler
CurrentUserService
CvAccessService
CvService
CvController
```

Then test:

```text
create
get
list
update
delete
duplicate
```

## Phase 3 — Language versions

Implement:

```text
CvLanguageVersion
repository
DTOs
service
controller
```

Then test:

```text
create
list
get
update
delete
duplicate with CV
```

## Phase 4 — AI

Implement:

```text
GeminiConfig
GeminiClient
GeminiRequest
GeminiResponse
PromptBuilder
CvFieldValidator
AiService
AiController
suggestion persistence
accept
reject
```

Then test AI behavior with mocked `AiClient`.

## Phase 5 — Bulk translation

Implement:

```text
structured CV translation
changes
individual suggestions
Accept All
```

## Phase 6 — PDF

Implement:

```text
template renderer
Modern
Minimal
Professional
Chromium
PDF endpoint
```

## Phase 7 — Frontend

Implement:

```text
React setup
routing
dashboard
CV list
CV editor
language versions
live preview
AI suggestions
autosave
PDF export
```

## Phase 8 — Integration and polish

Implement:

```text
error states
loading states
empty states
optimistic locking UI
responsive layout
tests
README
Docker improvements
```

---

# 72. DO NOT IMPLEMENT YET

Do NOT add:

```text
Kafka
RabbitMQ
Redis
Elasticsearch
Kubernetes
microservices
event sourcing
CQRS
Saga
OAuth
social login
payment system
subscription billing
ATS optimization
job matching
cover letters
template designer
AI agents
vector database
RAG
```

unless explicitly requested later.

This is intentionally a modular monolith MVP.

Do not over-engineer it.

---

# 73. IMPORTANT ARCHITECTURAL PRINCIPLE

The application should initially be:

```text
                 ┌──────────────┐
                 │    React     │
                 │  TypeScript  │
                 └──────┬───────┘
                        │ REST
                        ↓
              ┌───────────────────┐
              │ Spring Boot       │
              │ Modular Monolith  │
              └───────┬───────────┘
                      │
          ┌───────────┼───────────┐
          ↓           ↓           ↓
        CV           AI          PDF
          │           │           │
          ↓           ↓           ↓
     PostgreSQL     Gemini     Chromium
```

Do not turn this into microservices.

The code should be modular enough that parts can be extracted later if the product actually requires it.

---

# 74. README

Create a useful README containing:

```text
Project overview
Architecture
Technology stack
Requirements
Environment variables
Local setup
Docker setup
Database
API overview
AI setup
PDF setup
Testing
Project structure
```

Include commands such as:

```bash
docker compose up -d
```

and:

```bash
mvn spring-boot:run
```

---

# 75. ENVIRONMENT VARIABLES

Document:

```text
GEMINI_API_KEY
```

Do not commit:

```text
.env
API keys
passwords
credentials
```

Provide an example environment file if useful:

```text
.env.example
```

with placeholders only.

---

# 76. DEFINITION OF DONE

A feature is NOT considered complete simply because the code compiles.

For every major feature:

```text
Code implemented
        ↓
Validation implemented
        ↓
Exception handling implemented
        ↓
Ownership checked
        ↓
Tests added
        ↓
Application starts
        ↓
API manually testable
```

The project should compile and tests should pass after each major phase.

---

# 77. AGENT BEHAVIOR

When implementing:

1. Inspect the existing project.
2. Understand the current architecture.
3. Do not overwrite working code unnecessarily.
4. Implement one logical phase at a time.
5. Compile after significant changes.
6. Run tests after significant changes.
7. Fix compilation/test failures before continuing.
8. Keep changes focused.
9. Do not introduce unrelated refactors.
10. Do not replace the chosen stack.
11. Do not invent product requirements.
12. Prefer simple solutions.
13. Explain important architectural decisions briefly when needed.

If an existing implementation conflicts with this specification, prefer this specification unless the existing implementation is clearly more correct and the change is necessary.

---

# 78. FINAL GOAL

The final MVP should allow a user to:

```text
1. Open the application
2. Create a Master CV
3. Enter structured CV information
4. Reorder sections
5. Create an English version
6. Edit the English version independently
7. Use AI Improve
8. Use AI Rewrite
9. Translate the CV
10. Review AI suggestions
11. Accept or reject suggestions
12. Accept all translation suggestions
13. Select a template
14. Preview the CV
15. Export the CV as PDF
```

The result should be a clean, professional and extensible SaaS foundation.

The most important product rule is:

> AI assists the user. AI never silently controls the user's CV.

The most important technical rule is:

> Keep the MVP simple, modular, testable and production-oriented without premature infrastructure.