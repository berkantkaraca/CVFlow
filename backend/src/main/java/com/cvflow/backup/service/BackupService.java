package com.cvflow.backup.service;

import com.cvflow.backup.dto.BackupDtos.BackupDocument;
import com.cvflow.backup.dto.BackupDtos.BackupImportResult;
import com.cvflow.backup.dto.BackupDtos.BackupLanguageVersion;
import com.cvflow.backup.dto.BackupDtos.BackupMasterCv;
import com.cvflow.common.exception.InvalidBackupException;
import com.cvflow.common.security.CurrentUserService;
import com.cvflow.cv.entity.CvLanguageVersion;
import com.cvflow.cv.entity.MasterCv;
import com.cvflow.cv.model.CvSection;
import com.cvflow.cv.model.LanguageCode;
import com.cvflow.cv.repository.CvLanguageVersionRepository;
import com.cvflow.cv.repository.MasterCvRepository;
import com.cvflow.cv.repository.UserRepository;
import com.cvflow.cv.service.CvContentCloner;
import com.cvflow.cv.service.SectionOrderValidator;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Full-fidelity backup of user CV data (Master CVs + language versions with content, section
 * order and templates). AI suggestions and content revisions are transient state and are
 * intentionally not included. Import always creates fresh rows with new IDs.
 */
@Service
@RequiredArgsConstructor
public class BackupService {
    private static final int FORMAT_VERSION = 1;

    private final MasterCvRepository masterCvRepository;
    private final CvLanguageVersionRepository languageVersionRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final CvContentCloner cloner;
    private final SectionOrderValidator sectionOrderValidator;

    @Transactional(readOnly = true)
    public BackupDocument exportBackup() {
        UUID userId = currentUserService.getCurrentUserId();
        String email = userRepository
                .findById(userId)
                .map(user -> user.getEmail())
                .orElse("unknown");
        List<MasterCv> cvs = masterCvRepository.findAllByUserIdOrderByUpdatedAtDesc(userId);
        Map<UUID, List<CvLanguageVersion>> versionsByMaster = languageVersionRepository
                .findAllByMasterCvIdIn(cvs.stream().map(MasterCv::getId).toList()).stream()
                .collect(Collectors.groupingBy(CvLanguageVersion::getMasterCvId));
        List<BackupMasterCv> documents = cvs.stream()
                .map(cv -> new BackupMasterCv(
                        cv.getName(),
                        cloner.clone(cv.getContent()),
                        copyOrder(cv.getSectionOrder()),
                        versionsByMaster.getOrDefault(cv.getId(), List.of()).stream()
                                .map(version -> new BackupLanguageVersion(
                                        version.getLanguageCode(),
                                        version.getName(),
                                        version.getTemplateCode(),
                                        cloner.clone(version.getContent()),
                                        copyOrder(version.getSectionOrder())))
                                .toList()))
                .toList();
        return new BackupDocument(FORMAT_VERSION, Instant.now(), email, documents);
    }

    @Transactional
    public BackupImportResult importBackup(BackupDocument document) {
        if (document == null || document.format() != FORMAT_VERSION) {
            throw new InvalidBackupException("Unsupported backup format");
        }
        UUID userId = currentUserService.getCurrentUserId();
        List<BackupMasterCv> entries = nullSafe(document.masterCvs());
        entries.forEach(this::validateMaster);
        List<MasterCv> savedCvs = masterCvRepository.saveAll(entries.stream()
                .map(backupCv -> {
                    MasterCv cv = new MasterCv();
                    cv.setUserId(userId);
                    cv.setName(backupCv.name());
                    cv.setContent(cloner.clone(backupCv.content()));
                    cv.setSectionOrder(copyOrder(backupCv.sectionOrder()));
                    return cv;
                })
                .toList());
        List<CvLanguageVersion> versions = new ArrayList<>();
        for (int i = 0; i < entries.size(); i++) {
            UUID masterId = savedCvs.get(i).getId();
            for (BackupLanguageVersion backupLanguage : nullSafe(entries.get(i).languages())) {
                CvLanguageVersion version = new CvLanguageVersion();
                version.setMasterCvId(masterId);
                version.setLanguageCode(backupLanguage.languageCode());
                version.setName(backupLanguage.name());
                version.setTemplateCode(backupLanguage.templateCode());
                version.setContent(cloner.clone(backupLanguage.content()));
                version.setSectionOrder(copyOrder(backupLanguage.sectionOrder()));
                versions.add(version);
            }
        }
        languageVersionRepository.saveAll(versions);
        return new BackupImportResult(savedCvs.size(), versions.size());
    }

    private void validateMaster(BackupMasterCv backupCv) {
        if (backupCv == null || backupCv.content() == null || backupCv.languages() == null) {
            throw new InvalidBackupException("Backup contains an incomplete CV entry");
        }
        sectionOrderValidator.validate(backupCv.sectionOrder());
        Set<LanguageCode> seen = new HashSet<>();
        for (BackupLanguageVersion language : backupCv.languages()) {
            if (language == null || language.content() == null) {
                throw new InvalidBackupException("Backup contains an incomplete language version");
            }
            sectionOrderValidator.validate(language.sectionOrder());
            if (!seen.add(language.languageCode())) {
                throw new InvalidBackupException(
                        "Backup contains duplicate language '" + language.languageCode() + "'");
            }
        }
    }

    private List<CvSection> copyOrder(List<CvSection> order) {
        return order == null ? List.of() : new ArrayList<>(order);
    }

    private <T> List<T> nullSafe(List<T> items) {
        return items == null ? List.of() : items;
    }
}
