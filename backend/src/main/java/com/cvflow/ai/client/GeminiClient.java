package com.cvflow.ai.client;

import com.cvflow.ai.config.GeminiProperties;
import com.cvflow.common.exception.AiServiceException;
import com.cvflow.cv.model.CvContent;
import com.cvflow.cv.model.LanguageCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class GeminiClient implements AiClient {
    private final RestClient geminiRestClient;
    private final GeminiProperties properties;
    private final ObjectMapper objectMapper;
    private final PromptBuilder promptBuilder;
    private final AiOutputValidator outputValidator;

    @Override
    public String improve(String content) {
        return generate(promptBuilder.improve(content), content);
    }

    @Override
    public String rewrite(String content) {
        return generate(promptBuilder.rewrite(content), content);
    }

    @Override
    public String translate(String content, LanguageCode sourceLanguage, LanguageCode targetLanguage) {
        return generate(promptBuilder.translate(content, sourceLanguage, targetLanguage), content);
    }

    @Override
    public CvContent translateContent(
            CvContent content, LanguageCode sourceLanguage, LanguageCode targetLanguage) {
        String contentJson = serializeContent(content);
        String translated = generateText(GeminiApi.Request.ofDocument(
                promptBuilder.translateContent(contentJson, sourceLanguage, targetLanguage)));
        return parseDocument(translated);
    }

    @Override
    public CvContent tailorContent(CvContent content, String jobDescription, LanguageCode language) {
        String contentJson = serializeContent(content);
        String tailored = generateText(GeminiApi.Request.ofDocument(
                promptBuilder.tailorContent(contentJson, jobDescription, language)));
        return parseDocument(tailored);
    }

    @Override
    public String generateCoverLetter(CvContent content, String jobDescription, LanguageCode language) {
        String contentJson = serializeContent(content);
        return generate(
                promptBuilder.coverLetter(contentJson, jobDescription, language), contentJson);
    }

    private String serializeContent(CvContent content) {
        try {
            return objectMapper.writeValueAsString(content == null ? CvContent.empty() : content);
        } catch (Exception ex) {
            throw new AiServiceException("AI_INVALID_RESPONSE", "CV content could not be serialized");
        }
    }

    private CvContent parseDocument(String text) {
        try {
            CvContent parsed = objectMapper.readValue(text, CvContent.class);
            if (parsed == null) {
                throw new AiServiceException("AI_EMPTY_RESPONSE", "AI provider returned no usable content");
            }
            return parsed;
        } catch (AiServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new AiServiceException("AI_INVALID_RESPONSE", "AI provider returned an invalid response");
        }
    }

    private String generate(String prompt, String original) {
        String suggested = parseSuggested(generateText(GeminiApi.Request.of(prompt)));
        var violation = outputValidator.violation(original, suggested);
        if (violation.isEmpty()) {
            return suggested;
        }
        String retry = parseSuggested(generateText(GeminiApi.Request.of(
                promptBuilder.correction(original, suggested, violation.get()))));
        var retryViolation = outputValidator.violation(original, retry);
        if (retryViolation.isEmpty()) {
            return retry;
        }
        throw new AiServiceException(
                "AI_UNRELIABLE_RESPONSE",
                "AI produced unreliable content (invented details). Please try again.");
    }

    private String parseSuggested(String text) {
        try {
            GeminiApi.SuggestedText parsed = objectMapper.readValue(text, GeminiApi.SuggestedText.class);
            if (parsed.suggested() == null || parsed.suggested().isBlank()) {
                throw new AiServiceException("AI_EMPTY_RESPONSE", "AI provider returned no usable content");
            }
            return parsed.suggested();
        } catch (AiServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new AiServiceException("AI_INVALID_RESPONSE", "AI provider returned an invalid response");
        }
    }

    private String generateText(GeminiApi.Request geminiRequest) {
        GeminiApi.Response response;
        try {
            response = geminiRestClient
                    .post()
                    .uri(properties.baseUrl() + "/v1beta/models/" + properties.model() + ":generateContent")
                    .header("x-goog-api-key", properties.apiKey())
                    .body(geminiRequest)
                    .retrieve()
                    .body(GeminiApi.Response.class);
        } catch (RestClientException ex) {
            throw new AiServiceException("AI_SERVICE_ERROR", "AI provider request failed");
        }
        String text = firstText(response);
        if (text == null || text.isBlank()) {
            throw new AiServiceException("AI_EMPTY_RESPONSE", "AI provider returned no usable content");
        }
        return text;
    }

    private String firstText(GeminiApi.Response response) {
        if (response == null || response.candidates() == null || response.candidates().isEmpty()) {
            return null;
        }
        GeminiApi.Candidate candidate = response.candidates().get(0);
        if (candidate == null || candidate.content() == null || candidate.content().parts() == null
                || candidate.content().parts().isEmpty()) {
            return null;
        }
        return candidate.content().parts().get(0).text();
    }
}
