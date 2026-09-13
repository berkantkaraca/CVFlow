package com.cvflow.cv.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.cvflow.common.exception.CvNotFoundException;
import com.cvflow.common.exception.OptimisticLockException;
import com.cvflow.common.security.CvAccessService;
import com.cvflow.cv.dto.request.CreateCoverLetterRequest;
import com.cvflow.cv.dto.request.UpdateCoverLetterRequest;
import com.cvflow.cv.dto.response.CoverLetterResponse;
import com.cvflow.cv.entity.CoverLetter;
import com.cvflow.cv.entity.MasterCv;
import com.cvflow.cv.model.CvContent;
import com.cvflow.cv.model.CvSection;
import com.cvflow.cv.model.LanguageCode;
import com.cvflow.cv.repository.CoverLetterRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CoverLetterServiceTest {

    @Mock
    private CoverLetterRepository coverLetterRepository;

    @Mock
    private CvAccessService cvAccessService;

    @Mock
    private com.cvflow.pdf.service.GotenbergPdfClient pdfClient;

    @Mock
    private com.cvflow.pdf.template.LetterTemplate letterTemplate;

    private CoverLetterService service;
    private final UUID cvId = UUID.randomUUID();
    private final UUID letterId = UUID.randomUUID();

    @BeforeEach
    void setup() {
        service = new CoverLetterService(coverLetterRepository, cvAccessService, pdfClient, letterTemplate);
    }

    @Test
    void createStartsWithEmptyBody() {
        when(cvAccessService.getOwnedCv(cvId)).thenReturn(masterCv());
        when(coverLetterRepository.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));

        CoverLetterResponse response = service.create(
                cvId, new CreateCoverLetterRequest(LanguageCode.EN, "Application", null, "Acme", "Dev"));

        assertEquals("", response.body());
        assertEquals(LanguageCode.EN, response.languageCode());
    }

    @Test
    void updateRejectsStaleVersion() {
        when(cvAccessService.getOwnedCv(cvId)).thenReturn(masterCv());
        when(coverLetterRepository.findById(letterId)).thenReturn(Optional.of(letter(0L)));

        assertThrows(
                OptimisticLockException.class,
                () -> service.update(
                        cvId, letterId, new UpdateCoverLetterRequest("T", null, null, null, "body", 3L)));
    }

    @Test
    void getRejectsForeignCv() {
        when(cvAccessService.getOwnedCv(cvId)).thenReturn(masterCv());
        CoverLetter foreign = letter(0L);
        foreign.setMasterCvId(UUID.randomUUID());
        when(coverLetterRepository.findById(letterId)).thenReturn(Optional.of(foreign));

        assertThrows(CvNotFoundException.class, () -> service.get(cvId, letterId));
    }

    private MasterCv masterCv() {
        MasterCv cv = new MasterCv();
        cv.setId(cvId);
        cv.setUserId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        cv.setName("CV");
        cv.setContent(CvContent.empty());
        cv.setSectionOrder(CvSection.defaultOrder());
        cv.setVersion(0L);
        return cv;
    }

    private CoverLetter letter(long version) {
        CoverLetter letter = new CoverLetter();
        letter.setId(letterId);
        letter.setMasterCvId(cvId);
        letter.setLanguageCode(LanguageCode.EN);
        letter.setTitle("Application");
        letter.setBody("body");
        letter.setVersion(version);
        return letter;
    }
}
