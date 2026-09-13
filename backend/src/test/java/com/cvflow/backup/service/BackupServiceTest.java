package com.cvflow.backup.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cvflow.backup.dto.BackupDtos.BackupDocument;
import com.cvflow.backup.dto.BackupDtos.BackupImportResult;
import com.cvflow.backup.dto.BackupDtos.BackupLanguageVersion;
import com.cvflow.backup.dto.BackupDtos.BackupMasterCv;
import com.cvflow.common.exception.InvalidBackupException;
import com.cvflow.common.security.CurrentUserService;
import com.cvflow.cv.entity.MasterCv;
import com.cvflow.cv.model.CvContent;
import com.cvflow.cv.model.CvSection;
import com.cvflow.cv.model.LanguageCode;
import com.cvflow.cv.model.TemplateCode;
import com.cvflow.cv.repository.CvLanguageVersionRepository;
import com.cvflow.cv.repository.MasterCvRepository;
import com.cvflow.cv.repository.UserRepository;
import com.cvflow.cv.service.CvContentCloner;
import com.cvflow.cv.service.SectionOrderValidator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class BackupServiceTest {

    @Mock
    private MasterCvRepository masterCvRepository;

    @Mock
    private CvLanguageVersionRepository languageVersionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CurrentUserService currentUserService;

    private BackupService service;
    private final UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @BeforeEach
    void setup() {
        service = new BackupService(
                masterCvRepository,
                languageVersionRepository,
                userRepository,
                currentUserService,
                new CvContentCloner(new ObjectMapper()),
                new SectionOrderValidator());
        lenient().when(currentUserService.getCurrentUserId()).thenReturn(userId);
    }

    @Test
    void exportAssemblesDocument() {
        MasterCv cv = masterCv();
        when(masterCvRepository.findAllByUserIdOrderByUpdatedAtDesc(userId)).thenReturn(List.of(cv));
        when(languageVersionRepository.findAllByMasterCvId(cv.getId())).thenReturn(List.of());
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        BackupDocument document = service.exportBackup();

        assertEquals(1, document.format());
        assertEquals(1, document.masterCvs().size());
        assertEquals("CV", document.masterCvs().get(0).name());
    }

    @Test
    void importCreatesFreshRows() {
        BackupDocument document = new BackupDocument(
                1, java.time.Instant.now(), "user@example.com", List.of(backupCv()));

        when(masterCvRepository.save(any())).thenAnswer(i -> {
            MasterCv saved = i.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        BackupImportResult result = service.importBackup(document);

        assertEquals(1, result.importedCvs());
        assertEquals(1, result.importedLanguages());
        ArgumentCaptor<MasterCv> captor = ArgumentCaptor.forClass(MasterCv.class);
        verify(masterCvRepository).save(captor.capture());
        assertEquals(userId, captor.getValue().getUserId());
    }

    @Test
    void importRejectsUnknownFormat() {
        assertThrows(
                InvalidBackupException.class,
                () -> service.importBackup(new BackupDocument(999, java.time.Instant.now(), "x", List.of())));
    }

    @Test
    void importRejectsDuplicateLanguage() {
        BackupMasterCv cv = new BackupMasterCv(
                "CV",
                CvContent.empty(),
                CvSection.defaultOrder(),
                List.of(
                        backupLanguage(LanguageCode.EN),
                        backupLanguage(LanguageCode.EN)));
        BackupDocument document = new BackupDocument(1, java.time.Instant.now(), "x", List.of(cv));

        assertThrows(InvalidBackupException.class, () -> service.importBackup(document));
    }

    private MasterCv masterCv() {
        MasterCv cv = new MasterCv();
        cv.setId(UUID.randomUUID());
        cv.setUserId(userId);
        cv.setName("CV");
        cv.setContent(CvContent.empty());
        cv.setSectionOrder(CvSection.defaultOrder());
        cv.setVersion(0L);
        return cv;
    }

    private BackupMasterCv backupCv() {
        return new BackupMasterCv("CV", CvContent.empty(), CvSection.defaultOrder(), List.of(backupLanguage(LanguageCode.EN)));
    }

    private BackupLanguageVersion backupLanguage(LanguageCode code) {
        return new BackupLanguageVersion(code, "English", TemplateCode.MODERN, CvContent.empty(), CvSection.defaultOrder());
    }
}
