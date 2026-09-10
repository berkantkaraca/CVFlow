dün 22:36
sen deneyimli bir yazılımcısın şuan cv mi türkçe ayarlıyorum. daha sonra bunu pdf e çeviriyorum. aynı adımları ingilizce içinde yapmam gerekiyo. bu işlemi hızlandıran bir saas proje geliştrimek istiyorum. bunun hakkında neler karar vermeliyiz bunları konuşalım. daha sonra ai agent için prompt hazırlayalım.
Kesinlikle. Burada asıl ürün fikri “CV’yi İngilizceye çeviren AI” değil; CV’nin tek bir kaynaktan yönetilip farklı dil/format/versiyonlarda otomatik üretilmesi olabilir.
Bence önce AI agent prompt'una atlamayalım. Önce ürünün sınırlarını ve teknik mimarisini netleştirelim. Sonra bunlardan çok daha kaliteli bir agent prompt'u çıkarabiliriz.
1. Önce problemi doğru tanımlayalım
Senin mevcut akışın kabaca:
Türkçe CV'yi düzenle → PDF oluştur → İngilizce CV'yi ayrıca düzenle → PDF oluştur
Buradaki problem aslında translation değil, synchronization.
Örneğin:
Türkçe CV
 ├── Eğitim
 ├── Deneyim
 ├── Projeler
 ├── Yetenekler
 └── Hakkımda

        ↓ AI / translation

İngilizce CV
 ├── Education
 ├── Experience
 ├── Projects
 ├── Skills
 └── About
Bir deneyim eklediğinde iki CV'yi de tekrar düzenlemek istemiyorsun.
SaaS'ın temel vaadi şu olabilir:
Write your CV once. Generate every language and version automatically.
________________________________________
2. Ürünün temel modeli
Ben CV'yi doğrudan "bir Word/PDF dosyası" olarak modellemezdim.
Bunun yerine CV'yi structured data olarak tutardım.
Örneğin:
{
  "basics": {
    "name": "Berkant Karaca",
    "title": "Software Developer",
    "email": "...",
    "location": "Istanbul, Türkiye"
  },

  "summary": {
    "tr": "...",
    "en": "..."
  },

  "experience": [
    {
      "company": "...",
      "position": "...",
      "startDate": "...",
      "endDate": "...",
      "description": {
        "tr": "...",
        "en": "..."
      }
    }
  ],

  "education": [],
  "projects": [],
  "skills": []
}
Bunun çok büyük avantajı var.
Çünkü:
Data ≠ Presentation
Aynı CV datasından:
Türkçe CV
İngilizce CV
1-page CV
2-page CV
Software Engineer CV
Frontend Developer CV
ATS CV
LinkedIn version
üretebiliriz.
________________________________________
3. Bence SaaS'ın MVP'si
İlk versiyonu gereksiz büyütmeyelim.
MVP
Kullanıcı:
1.	Hesap oluşturur 
2.	CV oluşturur 
3.	CV bilgilerini girer 
4.	Bir template seçer 
5.	Ana dili seçer 
6.	"Translate to English" der 
7.	AI İngilizce versiyonu oluşturur 
8.	Kullanıcı isterse AI önerilerini düzenler 
9.	PDF export eder 
Örneğin:
             ┌──────────────┐
             │   CV Data    │
             └──────┬───────┘
                    │
          ┌─────────┴─────────┐
          ↓                   ↓
      Turkish              English
          │                   │
          └─────────┬─────────┘
                    ↓
              CV Template
                    ↓
                 PDF
________________________________________
4. Ama önemli bir karar: AI ne yapacak?
Burada AI'ı sadece translator yaparsak ürün biraz sıradanlaşır.
Bence AI'ın birkaç ayrı yeteneği olmalı.
A. Translation
Türkçe → İngilizce
Ama kelime kelime translation değil.
CV bağlamına uygun:
"Frontend geliştirme yaptım."
↓
"Developed and maintained scalable frontend applications..."
gibi.
________________________________________
B. CV improvement
Kullanıcı:
"Bu deneyimi daha profesyonel yaz."
diyebilir.
AI:
Before:
Worked on frontend projects.

After:
Developed responsive web applications using React
and TypeScript, improving...
________________________________________
C. ATS optimization
Örneğin:
Optimize this CV for a Software Engineer position.
AI CV'yi analiz eder:
ATS Score: 72

Missing keywords:
- TypeScript
- REST API
- CI/CD

Weak sections:
- Experience
Bu bence ürünün ileride önemli bir özelliği olabilir.
________________________________________
D. Job-specific CV
Bu daha da güçlü.
Kullanıcı iş ilanını yapıştırır:
Job Description
----------------
We are looking for a Senior Frontend Engineer...
Sonra:
Generate a CV optimized for this position.
Sistem mevcut CV'den yeni bir CV versiyonu oluşturur.
Yani:
Master CV
     │
     ├── English CV
     ├── Turkish CV
     ├── Frontend Engineer CV
     ├── Backend Engineer CV
     └── Company X Application CV
Bu noktada ürün basit bir CV editoründen çıkıp Career Document Management ürününe dönüşüyor.
________________________________________
5. Çok önemli: "Master CV"
Ben ürünün merkezine bunu koyardım.
Kullanıcının tek bir:
Master CV
'si olur.
Örneğin:
MASTER CV
│
├── Personal
├── Summary
├── Experience
├── Education
├── Projects
├── Skills
├── Certifications
└── Languages
Bunun üzerine versiyonlar gelir:
Versions

🇹🇷 Turkish
🇬🇧 English

🎯 Frontend Developer
🎯 Full Stack Developer
🎯 Software Engineer
Böylece kullanıcı aynı bilgiyi 5 kere yazmaz.
________________________________________
6. Translation memory de düşünelim
Bence burada güzel bir teknik özellik var.
Diyelim:
"Yazılım geliştirme uzmanı"
İngilizce:
"Software Development Specialist"
AI bunu bir kere çevirdi.
Sistem bunu hatırlayabilir.
Sonraki CV'lerde benzer ifadelerde aynı terminology kullanılabilir.
Bu özellikle profesyonel CV'lerde tutarlılık sağlar.
________________________________________
7. PDF konusunda kritik karar
Burada sana özellikle şunu öneririm:
PDF'i kaynak format olarak kullanma.
PDF sadece output olsun.
Architecture:
CV JSON
   ↓
Template Engine
   ↓
HTML/CSS
   ↓
PDF
Örneğin:
React
   ↓
CV JSON
   ↓
HTML/CSS template
   ↓
Playwright / Chromium
   ↓
PDF
Bu yaklaşım CV layout'u üzerinde çok daha fazla kontrol verir.
Ayrıca ileride:
PDF
DOCX
HTML
JSON
gibi farklı exportlar eklemek kolaylaşır.
________________________________________
8. Template sistemi
Template'i de CV datasından bağımsız tutardım.
Örneğin:
Template A
Template B
Template C
Aynı CV:
Berkant CV
şeklinde kalır.
Kullanıcı:
Template:
[ Modern ]
[ Minimal ]
[ Professional ]
[ ATS ]
seçebilir.
Sonuç:
Same data
     ↓
Different design
Bu SaaS'ın ölçeklenmesi açısından çok önemli.
________________________________________
9. Versioning
Bunu baştan düşünmek çok değerli.
Mesela:
CV
│
├── v1
├── v2
├── v3
└── v4
Kullanıcı yanlışlıkla AI'ın yaptığı değişiklikleri kaybetmemeli.
Daha iyi:
Master CV
   │
   ├── English
   │     ├── v1
   │     └── v2
   │
   └── Turkish
         ├── v1
         └── v2
Hatta değişiklik geçmişi:
AI changed:
"Developed" → "Engineered"

User accepted ✓
________________________________________
10. SaaS tarafında karar vermemiz gerekenler
Şu anda bence aşağıdaki kararları vermeliyiz.
Ürün
1.	Sadece CV mi? 
2.	CV + cover letter mı? 
3.	LinkedIn profile da dahil mi? 
4.	Job-specific CV olacak mı? 
AI
5.	Sadece translation mı? 
6.	Rewrite / improve olacak mı? 
7.	ATS optimization olacak mı? 
8.	Job description analysis olacak mı? 
9.	AI önerileri otomatik mi uygulanacak, kullanıcı mı onaylayacak? 
Data
10.	Master CV olacak mı? 
11.	Multi-language nasıl tutulacak? 
12.	Version history olacak mı? 
13.	Kullanıcı kendi CV'sini JSON olarak export edebilecek mi? 
Design
14.	Kaç template ile başlayacağız? 
15.	Template sistemi nasıl olacak? 
16.	Kullanıcı template'i ne kadar özelleştirecek? 
Export
17.	PDF 
18.	DOCX 
19.	HTML 
20.	Plain text / ATS version 
SaaS
21.	Free plan ne yapacak? 
22.	Pro plan ne yapacak? 
23.	AI kullanımını nasıl limitleriz? 
24.	Template'ler ücretsiz mi? 
25.	Kullanıcı başına kaç CV? 
________________________________________
11. Ben olsam MVP'yi böyle sınırlarım
İlk versiyonda:
                    CVFlow
                       │
        ┌──────────────┼──────────────┐
        ↓              ↓              ↓
     Master CV      Languages      Templates
        │              │              │
        │          TR / EN        3 templates
        │
        ↓
       AI
        │
   ┌────┼────┐
   ↓    ↓    ↓
Translate Improve Rewrite
        │
        ↓
      Export
        │
       PDF
İlk sürümde ATS/job matching'i yapmazdım.
Çünkü ürünün temel değerini önce doğrulamak daha mantıklı:
"Kullanıcı bir kere CV oluşturup ikinci dili zahmetsizce çıkarıyor mu?"
Eğer kullanıcılar bunu gerçekten seviyorsa ikinci aşamada:
Job Description
       ↓
   AI Analysis
       ↓
Tailored CV
       ↓
Cover Letter
eklenebilir.
________________________________________
12. Teknik stack konusunda da karar vermeliyiz
Ben modern ve nispeten hızlı geliştirilebilir bir stack düşünürdüm:
Frontend
Next.js + TypeScript

UI
Tailwind + shadcn/ui

Backend
Next.js / API routes
veya ayrı backend

Database
PostgreSQL

ORM
Prisma / Drizzle

Auth
Auth.js / Clerk / Supabase Auth

AI
LLM API

Storage
S3-compatible storage

PDF
Playwright / Chromium

Payments
Stripe

Deployment
Vercel + managed PostgreSQL
Ama burada hemen stack seçmek yerine önce product architecture'ı netleştirmek daha doğru.
________________________________________
Bence bir sonraki konuşmamız gereken en önemli şey
Şu soruya karar verelim:
Bu ürün tam olarak neyi "source of truth" kabul edecek?
Benim önerim:
Master CV → structured data → translations → versions → templates → PDF
Bunu doğru kurarsak ürünün geri kalanı çok daha temiz olur.
Sonrasında sana MVP ekranlarını, database schema'sını, AI workflow'unu, API'leri ve en sonunda da doğrudan Cursor/Claude Code/Gemini gibi bir coding agent'a verebileceğimiz çok detaylı implementation prompt'unu hazırlayabiliriz.
Yok, çok kalmadı. Hatta şu an promptu yazmaya başlamamız mantıklı, çünkü mimarinin ana kararlarını büyük ölçüde verdik.
Şu ana kadar belirlediğimiz şeyler zaten AI agent'a verilecek master promptun büyük kısmını oluşturuyor:
1.	Proje amacı ve ürün mantığı 
2.	Tech stack 
o	Java 25 
o	Spring Boot 4.1.x 
o	React + TypeScript + Vite 
o	PostgreSQL 
o	Gemini 
o	Docker 
3.	Monorepo yapısı 
4.	Database schema 
5.	Entity / model yapıları 
6.	DTO'lar 
7.	Repository'ler 
8.	Service / Controller mimarisi 
9.	Exception handling 
10.	Optimistic locking 
11.	Mock authentication 
12.	CV ve language version API'leri 
13.	AI mimarisi 
14.	AI suggestion mantığı 
15.	Translate / Improve / Rewrite 
16.	Accept / Reject 
17.	PDF mimarisi 
18.	Test stratejisi 
Geriye esas olarak birkaç kritik karar kalıyor:
AI
 ├── GeminiClient
 ├── PromptBuilder
 ├── FieldValidator
 ├── AiService
 ├── Accept / Reject
 └── Bulk Translation

PDF
 ├── HTML templates
 ├── Chromium
 └── PDF endpoint

Frontend
 ├── Sayfalar
 ├── CV Editor
 ├── Preview
 ├── AI suggestion UI
 └── API integration

DevOps
 ├── Docker
 ├── docker-compose
 └── environment variables

Testing
 ├── Backend unit tests
 ├── integration tests
 └── frontend temel testler
Bence artık her sınıfı tek tek yazmaya devam etmek yerine master promptu hazırlamaya geçebiliriz.
Ama promptu tek seferde körlemesine yazmak yerine şöyle yapalım:
MASTER PROMPT
│
├── 1. ROLE
├── 2. PROJECT OVERVIEW
├── 3. PRODUCT REQUIREMENTS
├── 4. TECH STACK
├── 5. ARCHITECTURE
├── 6. DATABASE
├── 7. BACKEND
├── 8. AI
├── 9. PDF
├── 10. FRONTEND
├── 11. DOCKER
├── 12. TESTING
├── 13. CODING RULES
├── 14. IMPLEMENTATION ORDER
└── 15. DEFINITION OF DONE
Özellikle son iki bölüm önemli. Agent'a sadece "bu projeyi yap" demeyeceğiz; hangi sırayla yapacağını, hangi kararları değiştiremeyeceğini ve ne zaman bir işi tamamlanmış sayacağını da söyleyeceğiz.
Yani evet, master prompt aşamasına geldik. Bundan sonra yaptığımız her teknik karar doğrudan prompta girebilir.
evet yaz
Evet. Aşağıdaki promptu AI coding agent'a doğrudan verebileceğin master prompt olarak hazırladım. Agent'ın projeyi sıfırdan oluşturmasını, belirlediğimiz mimariye uymasını ve kendi kafasına göre teknoloji/mimari değiştirmemesini özellikle belirttim.
CVFlow — Master Coding Agent Prompt
1. ROLE
You are a senior full-stack software engineer responsible for implementing a production-quality SaaS application.
You must:
•	Follow the architecture and technical decisions defined in this document.
•	Do not replace technologies without a strong technical reason.
•	Do not introduce unnecessary complexity.
•	Do not add technologies that are not required.
•	Prefer clean, maintainable, testable and scalable code.
•	Follow SOLID principles and Clean Code practices.
•	Keep responsibilities separated between Controller, Service, Repository and external integrations.
•	Never silently change product requirements.
•	If an implementation detail is not explicitly defined, choose the simplest reasonable solution that fits the existing architecture.
•	Do not ask unnecessary questions. Make reasonable implementation decisions and continue.
•	Before implementing a feature, inspect the existing code and preserve consistency with the current architecture.
The application should be implemented incrementally and should remain runnable after each major phase.
________________________________________
2. PROJECT OVERVIEW
Build a SaaS application for managing CVs/resumes.
The core product idea is:
Write your CV once. Generate every language and version automatically.
The application allows a user to:
•	Create multiple Master CVs.
•	Edit structured CV information.
•	Create language versions.
•	Edit language versions independently.
•	Translate CV content with AI.
•	Improve CV wording with AI.
•	Rewrite CV wording with AI.
•	Accept or reject AI suggestions.
•	Select different CV templates.
•	Preview the CV.
•	Export the CV as PDF.
The application is primarily intended for professional software developers but the data model should remain generic enough for other professions.
________________________________________
3. CORE PRODUCT MODEL
The application follows this structure:
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
Important:
Language versions are independently editable.
Creating a translation does NOT permanently bind the target language version to the source version.
After creation, the user can manually modify the translated version.
AI suggestions must NEVER silently mutate CV content.
The AI always creates a suggestion first.
AI
 ↓
Suggestion
 ↓
PENDING
 ↓
User accepts/rejects
________________________________________
4. MVP AI FEATURES
Implement only these AI operations in MVP:
TRANSLATE
IMPROVE
REWRITE
Do NOT implement ATS optimization, job-description matching, job-specific CV tailoring, cover letters or interview generation in MVP.
These can be added later.
________________________________________
5. TECHNOLOGY STACK
Backend
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
Use Java 25 features where appropriate but do not use unnecessary experimental language features.
Frontend
React
TypeScript
Vite
React Router
TanStack Query
Zustand
React Hook Form
Zod
Tailwind CSS
The frontend is a separate application communicating with the backend through REST APIs.
Do NOT use Next.js.
AI
Use Google's Gemini API.
Use the Gemini REST API through Spring's RestClient.
Do NOT tightly couple the business layer to Gemini.
Use:
AiService
    ↓
AiClient
    ↓
GeminiClient
    ↓
Gemini REST API
The model should be configurable through application configuration.
Default model:
gemini-2.5-flash
Do not hardcode the API key.
Use:
GEMINI_API_KEY
environment variable.
PDF
Generate PDFs using:
HTML
+
CSS
↓
Chromium
↓
PDF
Chromium should run through Docker.
Do not persist generated PDF files in PostgreSQL in MVP.
________________________________________
6. REPOSITORY STRUCTURE
Use a monorepo:
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
Backend and frontend remain separate applications.
________________________________________
7. BACKEND ARCHITECTURE
Use package-by-feature where appropriate.
Recommended structure:
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
Do not put all classes into generic global packages such as:
controller/
service/
repository/
entity/
Feature separation should remain clear.
________________________________________
8. DATABASE
Use PostgreSQL.
Use Flyway migrations.
Hibernate must use:
spring:
  jpa:
    hibernate:
      ddl-auto: validate
Hibernate must NOT automatically create or modify the schema.
All schema changes must go through Flyway.
Use UUID for IDs.
Use:
@Id
@GeneratedValue(strategy = GenerationType.UUID)
private UUID id;
Use:
Instant
for timestamps.
PostgreSQL timestamp columns:
TIMESTAMP WITH TIME ZONE
________________________________________
9. DATABASE SCHEMA
users
CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);
master_cvs
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
Create an index on:
user_id
cv_language_versions
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
Create an index on:
master_cv_id
cv_ai_suggestions
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
Create an index on:
language_version_id
Do not add GIN indexes for JSONB in MVP unless an actual query requires them.
________________________________________
10. ENUMS
Create:
public enum LanguageCode {
    TR,
    EN
}
public enum TemplateCode {
    MODERN,
    MINIMAL,
    PROFESSIONAL
}
public enum AiOperation {
    TRANSLATE,
    IMPROVE,
    REWRITE
}
public enum AiSuggestionStatus {
    PENDING,
    ACCEPTED,
    REJECTED
}
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
CvSection.defaultOrder() should return:
SUMMARY
EXPERIENCE
PROJECTS
EDUCATION
SKILLS
LANGUAGES
CERTIFICATIONS
LINKS
________________________________________
11. JSONB CV MODEL
CV content is stored as JSONB.
Do NOT create separate relational tables for every CV section in MVP.
Use Java POJOs mapped as JSONB.
Use Hibernate:
@JdbcTypeCode(SqlTypes.JSON)
@Column(columnDefinition = "jsonb", nullable = false)
Create:
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
________________________________________
12. CvContent
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
Lists should default to empty lists.
Provide:
public static CvContent empty()
which creates an empty but valid CV structure.
________________________________________
13. CV MODELS
Experience:
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
Dates are represented as:
YYYY-MM
Do not use YearMonth for JSON/API models.
Education:
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
Project:
public class Project {
    private UUID id;
    private String name;
    private String description;
    private List<String> technologies;
    private String url;
}
Skill:
public class Skill {
    private UUID id;
    private String name;
    private String category;
}
Language:
public class Language {
    private UUID id;
    private String name;
    private String level;
}
Certification:
public class Certification {
    private UUID id;
    private String name;
    private String issuer;
    private String date;
    private String url;
}
Link:
public class Link {
    private UUID id;
    private String label;
    private String url;
}
PersonalInfo:
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
________________________________________
14. ENTITIES
Create:
User
MasterCv
CvLanguageVersion
CvAiSuggestion
Use UUID IDs.
Use @Version Long version on:
MasterCv
CvLanguageVersion
This provides optimistic locking.
Do not manually implement database version increments.
________________________________________
15. MASTER CV ENTITY
MasterCv contains:
id
userId
name
content
createdAt
updatedAt
version
Use:
@CreationTimestamp
and:
@UpdateTimestamp
where appropriate.
The entity should not contain unnecessary relationships.
Using explicit UUID foreign-key fields instead of JPA relationships is intentional for MVP simplicity.
________________________________________
16. LANGUAGE VERSION ENTITY
CvLanguageVersion contains:
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
sectionOrder is stored as JSONB.
There must be only one language version per language for a Master CV.
Enforce this with:
UNIQUE(master_cv_id, language_code)
________________________________________
17. AI SUGGESTION ENTITY
CvAiSuggestion contains:
id
languageVersionId
operation
status
payload
createdAt
updatedAt
The payload is JSONB.
AI suggestions are temporary proposed changes.
The AI suggestion itself does not modify CV content.
________________________________________
18. AI SUGGESTION PAYLOAD
Create:
public class AiSuggestionPayload {

    private CvSection section;

    private UUID entityId;

    private String field;

    private String original;

    private String suggested;
}
For example:
{
  "section": "EXPERIENCE",
  "entityId": "uuid",
  "field": "description",
  "original": "Developed backend services with Java.",
  "suggested": "Developed backend services using Java."
}
________________________________________
19. API RESPONSE WRAPPER
All JSON API responses should use:
public record ApiResponse<T>(
        boolean success,
        T data,
        ApiError error,
        Instant timestamp
) {}
Error:
public record ApiError(
        String code,
        String message,
        Map<String, String> details
) {}
Provide helper methods:
ApiResponses.success(data)
ApiResponses.success()
ApiResponses.error(...)
PDF binary responses do NOT use this wrapper.
________________________________________
20. EXCEPTION HANDLING
Create:
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
Use a global exception handler.
Map:
404 → Not Found
400 → Validation / invalid request
403 → Access Denied
409 → Conflict / optimistic locking / duplicate
500 → Unexpected server error
Validation response:
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
Never expose stack traces or internal exception details to the client.
________________________________________
21. MOCK AUTHENTICATION
Authentication is NOT part of MVP.
However, the architecture must support adding authentication later.
Create:
common/security/
├── CurrentUserService
└── CvAccessService
For MVP:
private static final UUID MOCK_USER_ID =
        UUID.fromString(
            "00000000-0000-0000-0000-000000000001"
        );
CurrentUserService.getCurrentUserId() returns this ID.
Flyway V1 must insert:
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
________________________________________
22. RESOURCE OWNERSHIP
Never trust a user-provided CV ID.
Every CV/resource operation must verify ownership.
CvAccessService must provide:
MasterCv getOwnedCv(UUID cvId);

CvLanguageVersion getOwnedLanguageVersion(UUID languageVersionId);
If the resource exists but belongs to another user:
403 ACCESS_DENIED
Do not expose whether another user's resource exists.
________________________________________
23. CV API
Implement:
GET    /api/v1/cvs
POST   /api/v1/cvs
GET    /api/v1/cvs/{cvId}
PUT    /api/v1/cvs/{cvId}
DELETE /api/v1/cvs/{cvId}
POST   /api/v1/cvs/{cvId}/duplicate
________________________________________
24. CREATE CV
Request:
{
  "name": "Backend Developer CV"
}
Response:
201 Created
The created CV should contain:
empty CvContent
default section order
current mock user
________________________________________
25. UPDATE CV
Request:
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
The supplied version must be used for optimistic locking.
If the client sends an outdated version, return:
409 CONFLICT
Do not overwrite newer data.
________________________________________
26. SECTION ORDER VALIDATION
sectionOrder must:
•	contain only valid CvSection values
•	contain no duplicates
•	represent a valid section order
Create:
InvalidSectionOrderException
Do not silently remove duplicate sections.
Return a validation/conflict error.
________________________________________
27. DUPLICATE CV
Endpoint:
POST /api/v1/cvs/{cvId}/duplicate
Duplicate:
Master CV
+
content
+
section order
+
all language versions
Do NOT duplicate:
AI suggestions
Use deep copies for JSONB content.
The entire operation must be transactional.
Suggested duplicate name:
Original Name (Copy)
________________________________________
28. LANGUAGE VERSION API
Implement:
GET    /api/v1/cvs/{cvId}/languages
POST   /api/v1/cvs/{cvId}/languages
GET    /api/v1/cvs/{cvId}/languages/{language}
PUT    /api/v1/cvs/{cvId}/languages/{language}
DELETE /api/v1/cvs/{cvId}/languages/{language}
________________________________________
29. CREATE LANGUAGE VERSION
Request:
{
  "languageCode": "EN",
  "name": "English",
  "templateCode": "MODERN"
}
The new version initially copies the Master CV content.
Its section order initially copies the Master CV section order.
If that language already exists:
409 CONFLICT
________________________________________
30. LANGUAGE VERSION INDEPENDENCE
Once a language version is created:
Master CV
     |
     | initial copy
     ↓
English Version
The English version becomes independently editable.
Later changes to Master CV must NOT silently overwrite the English version.
________________________________________
31. DTO PACKAGE
Use:
dto/
├── request/
└── response/
Create:
CreateCvRequest
UpdateCvRequest
CvSummaryResponse
CvResponse

CreateLanguageVersionRequest
UpdateLanguageVersionRequest
LanguageVersionSummaryResponse
LanguageVersionResponse

PdfExportRequest
Use Java records for DTOs.
Validate requests with Jakarta Validation.
________________________________________
32. REPOSITORIES
Create:
UserRepository
MasterCvRepository
CvLanguageVersionRepository
CvAiSuggestionRepository
Important methods:
Optional<User> findByEmail(String email);
List<MasterCv>
findAllByUserIdOrderByUpdatedAtDesc(UUID userId);
List<CvLanguageVersion>
findAllByMasterCvId(UUID masterCvId);
Optional<CvLanguageVersion>
findByMasterCvIdAndLanguageCode(
    UUID masterCvId,
    LanguageCode languageCode
);
AI suggestions:
List<CvAiSuggestion>
findAllByLanguageVersionId(UUID languageVersionId);
List<CvAiSuggestion>
findAllByLanguageVersionIdAndStatus(
    UUID languageVersionId,
    AiSuggestionStatus status
);
________________________________________
33. CV CONTENT CLONING
Create:
CvContentCloner
Use Jackson ObjectMapper to perform a deep copy.
Example:
public CvContent clone(CvContent source) {
    return objectMapper.convertValue(
        objectMapper.valueToTree(source),
        CvContent.class
    );
}
Do not perform shallow copies when duplicating CVs.
________________________________________
34. AI ARCHITECTURE
Use:
AiController
    ↓
AiService
    ↓
AiClient
    ↓
GeminiClient
    ↓
Gemini REST API
AiService contains business logic.
GeminiClient contains provider-specific logic.
Do NOT put Gemini API calls inside AiService.
________________________________________
35. AI CLIENT
Create:
public interface AiClient {

    String improve(String content);

    String rewrite(String content);

    String translate(
        String content,
        LanguageCode sourceLanguage,
        LanguageCode targetLanguage
    );
}
Implement:
GeminiClient
________________________________________
36. GEMINI CONFIGURATION
Use:
app:
  ai:
    gemini:
      api-key: ${GEMINI_API_KEY}
      model: gemini-2.5-flash
      base-url: https://generativelanguage.googleapis.com
Do not commit API keys.
Do not put secrets in source code.
________________________________________
37. GEMINI REST CLIENT
Use Spring RestClient.
Configure:
base URL
x-goog-api-key
Content-Type: application/json
Call:
/v1beta/models/{model}:generateContent
Do not use a blocking external API call without a reasonable timeout.
Configure connection/read timeouts.
Gemini failures must be converted into application-level AiServiceException.
Do not leak Gemini's raw response to the frontend.
________________________________________
38. GEMINI STRUCTURED OUTPUT
Use Gemini structured output.
The response must be JSON.
Use:
responseMimeType = application/json
Use a JSON schema similar to:
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
The Gemini response should conceptually be:
{
  "suggested": "..."
}
Parse the JSON using Jackson.
If parsing fails:
AI_INVALID_RESPONSE
If Gemini returns no usable candidate:
AI_EMPTY_RESPONSE
________________________________________
39. AI PROMPT RULES
Every AI prompt must enforce:
•	Never invent facts.
•	Never invent technologies.
•	Never invent companies.
•	Never invent responsibilities.
•	Never invent achievements.
•	Never invent metrics.
•	Never invent dates.
•	Never change factual information.
•	Preserve meaning.
•	Use professional CV language.
•	Keep output concise.
•	Do not add unnecessary explanations.
•	Return structured JSON only.
________________________________________
40. IMPROVE
Improve should:
•	preserve the original meaning
•	preserve factual information
•	improve grammar
•	improve clarity
•	improve professional tone
•	improve CV-oriented wording
•	avoid unnecessary rewriting
Example:
Input:
Developed backend services with Java.
Possible output:
Developed backend services using Java.
The model must not introduce unsupported technologies.
________________________________________
41. REWRITE
Rewrite should:
•	substantially improve wording
•	make the text stronger
•	remain factually identical
•	not invent information
Difference:
IMPROVE
→ minimal/moderate wording improvement

REWRITE
→ stronger rephrasing
________________________________________
42. TRANSLATE
Translation must:
•	preserve exact meaning
•	not add information
•	not remove information
•	use professional CV terminology
•	preserve technical terminology
•	preserve company names where appropriate
•	preserve dates
•	preserve factual information
MVP languages:
TR
EN
The architecture should make adding languages later easy.
________________________________________
43. AI API
Implement:
POST /api/v1/ai/translate
POST /api/v1/ai/improve
POST /api/v1/ai/rewrite
POST /api/v1/ai/suggestions/{id}/accept
POST /api/v1/ai/suggestions/{id}/reject
________________________________________
44. IMPROVE REQUEST
public record ImproveRequest(
    @NotNull UUID languageVersionId,
    @NotNull CvSection section,
    UUID entityId,
    @NotBlank String field,
    @NotBlank String content
) {}
Rewrite uses the same structure.
________________________________________
45. TRANSLATE REQUEST
public record TranslateRequest(
    @NotNull UUID sourceLanguageVersionId,
    @NotNull UUID targetLanguageVersionId
) {}
________________________________________
46. AI SUGGESTION RESPONSE
public record AiSuggestionResponse(
    UUID id,
    AiOperation operation,
    AiSuggestionStatus status,
    AiSuggestionPayload payload,
    Instant createdAt
) {}
________________________________________
47. FIELD VALIDATION
Never allow AI requests to modify arbitrary object fields.
Create:
CvFieldValidator
Allowed fields:
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
Invalid field:
400 INVALID_FIELD
The validator must also verify that entityId is appropriate for sections containing lists.
Do not use reflection to allow arbitrary field modification.
________________________________________
48. AI SERVICE
AiService responsibilities:
•	validate ownership
•	validate section
•	validate field
•	retrieve current content
•	call AiClient
•	create suggestion
•	persist suggestion as PENDING
•	return suggestion
It must NOT directly modify CV content when generating a suggestion.
________________________________________
49. ACCEPT SUGGESTION
Flow:
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
All steps must happen inside one transaction.
If applying the suggestion fails, the transaction must roll back.
________________________________________
50. REJECT SUGGESTION
Flow:
Suggestion PENDING
       ↓
validate ownership
       ↓
status = REJECTED
No CV content should change.
________________________________________
51. INVALID SUGGESTION STATE
Only:
PENDING
suggestions can be accepted or rejected.
Trying to accept/reject:
ACCEPTED
REJECTED
must return:
409 CONFLICT
with an appropriate error code.
________________________________________
52. BULK TRANSLATION
Translation should support full-CV translation.
The preferred MVP implementation is:
Source CV
    ↓
one structured Gemini request
    ↓
translated structured content
    ↓
compare source/target fields
    ↓
create individual suggestions
Do not create one Gemini API call for every field.
Avoid unnecessary API calls.
If translation becomes slow in the future, asynchronous processing can be introduced.
Do NOT add Kafka or RabbitMQ in MVP.
________________________________________
53. ACCEPT ALL
Provide a mechanism for accepting all pending suggestions belonging to a translation operation.
Acceptance should happen in one transaction.
If one suggestion fails, the transaction should roll back.
Do not partially apply a bulk acceptance.
________________________________________
54. FRONTEND PRODUCT STRUCTURE
Frontend should be a React + TypeScript + Vite application.
Recommended structure:
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
Use feature-based organization.
________________________________________
55. FRONTEND PAGES
MVP pages:
Dashboard
CV List / Management
CV Editor
Language Version Editor
AI Suggestions
CV Preview
________________________________________
56. CV EDITOR
The main CV editor should have:
LEFT
→ structured section editor

RIGHT
→ live CV preview
Example:
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
________________________________________
57. SECTION MANAGEMENT
The user must be able to:
•	edit sections
•	add entries
•	remove entries
•	reorder sections
•	reorder list entries where appropriate
Section order should be persisted as:
sectionOrder
Drag-and-drop can be used on the frontend.
________________________________________
58. AUTOSAVE
Implement:
debounced autosave ≈ 800ms
Also provide a manual:
Save
button.
Manual Save should persist immediately.
Use TanStack Query for server state.
Use Zustand only for appropriate local UI/application state.
Do not put all server state into Zustand.
________________________________________
59. OPTIMISTIC LOCKING FRONTEND
The frontend must send the latest:
version
with update requests.
If backend returns:
409 CONFLICT
because of optimistic locking:
•	do not silently overwrite data
•	show an appropriate conflict message
•	refresh or provide a recovery mechanism
________________________________________
60. AI UI
For editable fields provide actions such as:
Improve
Rewrite
Translate
When an AI suggestion arrives:
Original
    ↓
Suggested
The user should be able to:
Accept
Reject
For bulk translation:
Accept All
must be available.
AI-generated content must be visually distinguishable from saved content.
________________________________________
61. TEMPLATE SYSTEM
MVP templates:
MODERN
MINIMAL
PROFESSIONAL
Templates should be implemented as HTML/CSS rendering strategies.
Do NOT build a template designer in MVP.
The user selects a template for each language version.
________________________________________
62. LIVE PREVIEW
The frontend should render a CV preview based on:
CvContent
+
sectionOrder
+
templateCode
The preview should closely resemble the generated PDF.
Avoid creating a completely different frontend-only design from the PDF template.
The same conceptual template structure should drive both.
________________________________________
63. PDF GENERATION
Endpoint:
POST /api/v1/cvs/{cvId}/export/pdf
Request:
{
  "languageCode": "EN"
}
Response:
Content-Type: application/pdf
Use:
Content-Disposition: attachment; filename="cv.pdf"
Do not wrap the PDF in ApiResponse.
________________________________________
64. PDF ARCHITECTURE
Use:
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
Generated PDF should not be stored in PostgreSQL.
________________________________________
65. DOCKER
Create Docker Compose for local development.
At minimum:
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
Chromium/PDF infrastructure should also be container-compatible.
Do not require Docker for basic backend unit tests.
________________________________________
66. APPLICATION CONFIGURATION
Default:
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
For production, secrets must be provided through environment variables.
________________________________________
67. TESTING STRATEGY
Implement three levels of testing.
Unit Tests
Test:
CvContent.empty()
CvContentCloner
CvMapper
CvFieldValidator
section order validation
DTO validation where useful
Service Tests
Use:
JUnit 5
Mockito
Test:
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
Integration Tests
Use:
Testcontainers PostgreSQL
Test:
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
Verify actual JSONB persistence and optimistic locking.
________________________________________
68. CODE QUALITY
Follow these rules:
•	Prefer constructor injection.
•	Use @RequiredArgsConstructor where appropriate.
•	Avoid field injection.
•	Use DTOs at API boundaries.
•	Do not expose JPA entities directly from controllers.
•	Keep controllers thin.
•	Put business logic in services.
•	Keep repositories focused on persistence.
•	Keep external API logic isolated.
•	Avoid giant service classes.
•	Avoid giant controllers.
•	Avoid duplicated code.
•	Avoid unnecessary abstractions.
•	Use meaningful names.
•	Prefer immutable DTOs using records.
•	Validate all external input.
•	Never trust frontend validation alone.
•	Never hardcode secrets.
•	Never expose internal stack traces.
•	Use transactions intentionally.
•	Use @Transactional(readOnly = true) for read operations where appropriate.
•	Use @Transactional for mutation operations.
•	Do not use open-in-view.
________________________________________
69. SECURITY RULES
Even though authentication is mocked in MVP:
•	Always validate ownership.
•	Never trust IDs supplied by the frontend.
•	Never allow arbitrary JSON fields to be modified through AI.
•	Never allow arbitrary reflection-based field modification.
•	Never expose Gemini API key to the frontend.
•	Gemini API calls must only happen from the backend.
•	Never store API keys in Git.
•	Never return internal exception details.
•	Validate URLs and user-provided content where appropriate.
•	Prepare the architecture for future JWT authentication.
________________________________________
70. API DOCUMENTATION
Add OpenAPI/Swagger documentation if practical after the core API is stable.
Do not let API documentation delay core implementation.
Document:
•	endpoints
•	request models
•	response models
•	error responses
•	PDF endpoint
________________________________________
71. IMPLEMENTATION ORDER
Implement the project in this exact general order.
Phase 1 — Project foundation
Backend project
PostgreSQL
Docker Compose
Flyway
application.yml
basic health check
Verify:
Spring Boot starts
PostgreSQL connects
Flyway succeeds
Phase 2 — CV core
Implement:
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
Then test:
create
get
list
update
delete
duplicate
Phase 3 — Language versions
Implement:
CvLanguageVersion
repository
DTOs
service
controller
Then test:
create
list
get
update
delete
duplicate with CV
Phase 4 — AI
Implement:
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
Then test AI behavior with mocked AiClient.
Phase 5 — Bulk translation
Implement:
structured CV translation
changes
individual suggestions
Accept All
Phase 6 — PDF
Implement:
template renderer
Modern
Minimal
Professional
Chromium
PDF endpoint
Phase 7 — Frontend
Implement:
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
Phase 8 — Integration and polish
Implement:
error states
loading states
empty states
optimistic locking UI
responsive layout
tests
README
Docker improvements
________________________________________
72. DO NOT IMPLEMENT YET
Do NOT add:
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
unless explicitly requested later.
This is intentionally a modular monolith MVP.
Do not over-engineer it.
________________________________________
73. IMPORTANT ARCHITECTURAL PRINCIPLE
The application should initially be:
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
Do not turn this into microservices.
The code should be modular enough that parts can be extracted later if the product actually requires it.
________________________________________
74. README
Create a useful README containing:
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
Include commands such as:
docker compose up -d
and:
mvn spring-boot:run
________________________________________
75. ENVIRONMENT VARIABLES
Document:
GEMINI_API_KEY
Do not commit:
.env
API keys
passwords
credentials
Provide an example environment file if useful:
.env.example
with placeholders only.
________________________________________
76. DEFINITION OF DONE
A feature is NOT considered complete simply because the code compiles.
For every major feature:
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
The project should compile and tests should pass after each major phase.
________________________________________
77. AGENT BEHAVIOR
When implementing:
1.	Inspect the existing project.
2.	Understand the current architecture.
3.	Do not overwrite working code unnecessarily.
4.	Implement one logical phase at a time.
5.	Compile after significant changes.
6.	Run tests after significant changes.
7.	Fix compilation/test failures before continuing.
8.	Keep changes focused.
9.	Do not introduce unrelated refactors.
10.	Do not replace the chosen stack.
11.	Do not invent product requirements.
12.	Prefer simple solutions.
13.	Explain important architectural decisions briefly when needed.
If an existing implementation conflicts with this specification, prefer this specification unless the existing implementation is clearly more correct and the change is necessary.
________________________________________
78. FINAL GOAL
The final MVP should allow a user to:
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
The result should be a clean, professional and extensible SaaS foundation.
The most important product rule is:
AI assists the user. AI never silently controls the user's CV.
The most important technical rule is:
Keep the MVP simple, modular, testable and production-oriented without premature infrastructure.
Bu prompt artık kodlama aşamasına geçmek için yeterli seviyede. Agent'a verdiğinde özellikle en alttaki Implementation Order kısmı sayesinde tek seferde her şeyi birbirine karıştırmak yerine aşamalı ilerlemesini sağlayacak.
Bence bundan sonraki en doğru adım, bunu Cursor / Antigravity / Claude Code / Gemini CLI gibi agent'lara özel biraz daha "uygulama emri" formatına çevirmek. Çünkü yukarıdaki doküman mimari spec olarak çok iyi; fakat coding agent için Phase 1'i yap → test et → Phase 2'ye geç şeklinde daha operasyonel bir versiyon daha başarılı olur.
birde proje adı öner
Tabii. Bu proje için isim seçerken bence CV + AI + dil/template yönetimi çağrışımı yapmalı ama ileride sadece CV çevirisi yapan bir ürün gibi görünmemeli.
Benim favorilerim:
1.	CVora — kısa, SaaS ürünü gibi; CV + aura çağrışımı 
2.	CVFlow — CV oluşturma → dil → template → PDF akışını iyi anlatıyor 
3.	CVForge — CV'yi şekillendirme/oluşturma hissi 
4.	ResumeFlow — daha global ve açıklayıcı 
5.	CVCraft — CV'yi profesyonelce "işleme" fikri 
6.	CVSync — Master CV → dil versiyonları mantığına uygun 
7.	CVPilot — AI destekli CV asistanı hissi 
8.	CVStack — farklı CV versiyonlarını yönetme fikrine uygun 
9.	ResumeKit — daha sade ve ürün odaklı 
10.	CVNest — tüm CV versiyonlarının tek yerde tutulması fikri 
Ben olsam
CVFlow seçerdim.
Çünkü ürünün ana fikrini doğrudan karşılıyor:
Master CV
   ↓
Language
   ↓
AI Improve / Rewrite / Translate
   ↓
Template
   ↓
PDF
Yani:
CVFlow — Write once. Generate every version.
Alternatif olarak daha startup/marka ismi gibi, CV kelimesini doğrudan içermeyen isimler de bulabiliriz. Bence o yönde daha iyi isimler çıkabilir.


