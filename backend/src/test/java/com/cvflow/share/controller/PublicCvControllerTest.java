package com.cvflow.share.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cvflow.auth.service.JwtService;
import com.cvflow.cv.entity.CvLanguageVersion;
import com.cvflow.cv.model.CvContent;
import com.cvflow.cv.model.CvSection;
import com.cvflow.cv.model.LanguageCode;
import com.cvflow.cv.model.TemplateCode;
import com.cvflow.cv.repository.CvLanguageVersionRepository;
import com.cvflow.pdf.template.TemplateRenderer;
import com.cvflow.share.entity.ShareLink;
import com.cvflow.share.service.ShareService;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PublicCvController.class)
@AutoConfigureMockMvc(addFilters = false)
class PublicCvControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ShareService shareService;

    @MockitoBean
    private CvLanguageVersionRepository languageVersionRepository;

    @MockitoBean
    private TemplateRenderer templateRenderer;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void publicCvReturnsHtml() throws Exception {
        ShareLink link = new ShareLink();
        UUID versionId = UUID.randomUUID();
        link.setLanguageVersionId(versionId);
        when(shareService.resolvePublic("token")).thenReturn(link);
        CvLanguageVersion version = new CvLanguageVersion();
        version.setId(versionId);
        version.setMasterCvId(UUID.randomUUID());
        version.setLanguageCode(LanguageCode.EN);
        version.setName("English");
        version.setTemplateCode(TemplateCode.MODERN);
        version.setContent(CvContent.empty());
        version.setSectionOrder(CvSection.defaultOrder());
        when(languageVersionRepository.findById(versionId)).thenReturn(Optional.of(version));
        when(templateRenderer.render(
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any()))
                .thenReturn("<html></html>");

        mvc.perform(get("/api/v1/public/cv/token"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));
    }
}
