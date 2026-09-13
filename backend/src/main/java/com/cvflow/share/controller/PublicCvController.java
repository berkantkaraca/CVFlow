package com.cvflow.share.controller;

import com.cvflow.common.exception.ShareNotFoundException;
import com.cvflow.cv.entity.CvLanguageVersion;
import com.cvflow.cv.repository.CvLanguageVersionRepository;
import com.cvflow.pdf.template.TemplateRenderer;
import com.cvflow.share.entity.ShareLink;
import com.cvflow.share.service.ShareService;
import lombok.RequiredArgsConstructor;import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/cv")
@RequiredArgsConstructor
public class PublicCvController {
    private final ShareService shareService;
    private final CvLanguageVersionRepository languageVersionRepository;
    private final TemplateRenderer templateRenderer;

    @GetMapping(value = "/{token}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> publicCv(@PathVariable String token) {
        ShareLink link = shareService.resolvePublic(token);
        CvLanguageVersion version = languageVersionRepository
                .findById(link.getLanguageVersionId())
                .orElseThrow(ShareNotFoundException::new);
        String html = templateRenderer.render(
                version.getContent(),
                version.getSectionOrder(),
                version.getTemplateCode(),
                version.getLanguageCode());
        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
    }
}
