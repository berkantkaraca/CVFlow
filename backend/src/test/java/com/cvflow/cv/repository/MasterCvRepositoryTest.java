package com.cvflow.cv.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cvflow.cv.entity.MasterCv;
import com.cvflow.cv.model.CvContent;
import com.cvflow.cv.model.CvSection;
import com.cvflow.cv.model.Experience;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
class MasterCvRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer("postgres:17");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MasterCvRepository repository;

    private final UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Test
    void persistsJsonbContentAndSectionOrder() {
        MasterCv cv = new MasterCv();
        cv.setUserId(userId);
        cv.setName("JSONB CV");
        CvContent content = CvContent.empty();
        content.setSummary("Summary");
        Experience exp = new Experience();
        exp.setId(UUID.randomUUID());
        exp.setCompany("Acme");
        content.setExperiences(new ArrayList<>(List.of(exp)));
        cv.setContent(content);
        cv.setSectionOrder(CvSection.defaultOrder());

        MasterCv saved = repository.save(cv);

        MasterCv loaded = repository.findById(saved.getId()).orElseThrow();
        assertEquals("Summary", loaded.getContent().getSummary());
        assertEquals(1, loaded.getContent().getExperiences().size());
        assertEquals("Acme", loaded.getContent().getExperiences().get(0).getCompany());
        assertEquals(CvSection.defaultOrder(), loaded.getSectionOrder());
    }

    @Test
    void findsAllByUserOrderedByUpdatedAt() {
        MasterCv first = savedCv("First");
        MasterCv second = savedCv("Second");

        List<MasterCv> all = repository.findAllByUserIdOrderByUpdatedAtDesc(userId);

        assertTrue(all.size() >= 2);
        assertTrue(all.stream().anyMatch(c -> c.getId().equals(first.getId())));
        assertTrue(all.stream().anyMatch(c -> c.getId().equals(second.getId())));
    }

    @Test
    void versionIncrementsOnUpdate() {
        MasterCv saved = savedCv("Versioned");
        Long v1 = saved.getVersion();

        saved.setName("Versioned 2");
        MasterCv updated = repository.save(saved);

        assertEquals(v1 + 1, updated.getVersion());
    }

    private MasterCv savedCv(String name) {
        MasterCv cv = new MasterCv();
        cv.setUserId(userId);
        cv.setName(name);
        cv.setContent(CvContent.empty());
        cv.setSectionOrder(CvSection.defaultOrder());
        return repository.save(cv);
    }
}
