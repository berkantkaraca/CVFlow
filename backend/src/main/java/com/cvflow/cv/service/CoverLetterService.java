package com.cvflow.cv.service;

import com.cvflow.common.exception.CvNotFoundException;
import com.cvflow.common.exception.OptimisticLockException;
import com.cvflow.common.security.CvAccessService;
import com.cvflow.cv.dto.request.CreateCoverLetterRequest;
import com.cvflow.cv.dto.request.UpdateCoverLetterRequest;
import com.cvflow.cv.dto.response.CoverLetterResponse;
import com.cvflow.cv.dto.response.CoverLetterSummaryResponse;
import com.cvflow.cv.entity.CoverLetter;
import com.cvflow.cv.entity.MasterCv;
import com.cvflow.cv.repository.CoverLetterRepository;
import com.cvflow.pdf.service.GotenbergPdfClient;
import com.cvflow.pdf.template.LetterTemplate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CoverLetterService {
    private final CoverLetterRepository coverLetterRepository;
    private final CvAccessService cvAccessService;
    private final GotenbergPdfClient pdfClient;
    private final LetterTemplate letterTemplate;

    @Transactional(readOnly = true)
    public List<CoverLetterSummaryResponse> list(UUID cvId) {
        MasterCv cv = cvAccessService.getOwnedCv(cvId);
        return coverLetterRepository.findAllByMasterCvIdOrderByUpdatedAtDesc(cv.getId()).stream()
                .map(letter -> new CoverLetterSummaryResponse(
                        letter.getId(), letter.getTitle(), letter.getUpdatedAt(), letter.getVersion()))
                .toList();
    }

    @Transactional(readOnly = true)
    public CoverLetterResponse get(UUID cvId, UUID letterId) {
        return toResponse(getOwnedInCv(cvId, letterId));
    }

    @Transactional
    public CoverLetterResponse create(UUID cvId, CreateCoverLetterRequest request) {
        MasterCv cv = cvAccessService.getOwnedCv(cvId);
        CoverLetter letter = new CoverLetter();
        letter.setMasterCvId(cv.getId());
        letter.setLanguageCode(request.languageCode());
        letter.setTitle(request.title());
        letter.setRecipient(request.recipient());
        letter.setCompany(request.company());
        letter.setPosition(request.position());
        letter.setBody("");
        return toResponse(coverLetterRepository.saveAndFlush(letter));
    }

    @Transactional
    public CoverLetterResponse update(UUID cvId, UUID letterId, UpdateCoverLetterRequest request) {
        CoverLetter letter = getOwnedInCv(cvId, letterId);
        if (!letter.getVersion().equals(request.version())) {
            throw new OptimisticLockException();
        }
        letter.setTitle(request.title());
        letter.setRecipient(request.recipient());
        letter.setCompany(request.company());
        letter.setPosition(request.position());
        letter.setBody(request.body() == null ? "" : request.body());
        return toResponse(coverLetterRepository.saveAndFlush(letter));
    }

    @Transactional
    public void delete(UUID cvId, UUID letterId) {
        coverLetterRepository.delete(getOwnedInCv(cvId, letterId));
    }

    @Transactional(readOnly = true)
    public PdfLetter exportPdf(UUID cvId, UUID letterId) {
        CoverLetter letter = getOwnedInCv(cvId, letterId);
        MasterCv cv = cvAccessService.getOwnedCv(cvId);
        String html = letterTemplate.render(letter, cv.getContent(), letter.getLanguageCode());
        byte[] pdf = pdfClient.convertHtmlToPdf(html);
        return new PdfLetter(pdf, "cover-letter.pdf");
    }

    public record PdfLetter(byte[] content, String filename) {}

    private CoverLetter getOwnedInCv(UUID cvId, UUID letterId) {
        MasterCv cv = cvAccessService.getOwnedCv(cvId);
        CoverLetter letter =
                coverLetterRepository.findById(letterId).orElseThrow(CvNotFoundException::new);
        if (!letter.getMasterCvId().equals(cv.getId())) {
            throw new CvNotFoundException();
        }
        return letter;
    }

    private CoverLetterResponse toResponse(CoverLetter letter) {
        return new CoverLetterResponse(
                letter.getId(),
                letter.getMasterCvId(),
                letter.getLanguageCode(),
                letter.getTitle(),
                letter.getRecipient(),
                letter.getCompany(),
                letter.getPosition(),
                letter.getBody(),
                letter.getCreatedAt(),
                letter.getUpdatedAt(),
                letter.getVersion());
    }
}
