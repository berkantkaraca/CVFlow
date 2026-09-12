package com.cvflow.ai.client;

import com.cvflow.ai.config.GeminiProperties;
import com.cvflow.common.exception.AiServiceException;
import com.cvflow.cv.model.CvContent;
import com.cvflow.cv.model.LanguageCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.ObjectMapper;

@Component
public class GeminiClient implements AiClient {
    private final RestClient restClient;
    private final GeminiProperties properties;
    private final ObjectMapper objectMapper;
    private final PromptBuilder promptBuilder;

    public GeminiClient(
            RestClient geminiRestClient,
            GeminiProperties properties,
            ObjectMapper objectMapper,
            PromptBuilder promptBuilder) {
        this.restClient = geminiRestClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.promptBuilder = promptBuilder;
    }

    @Override
    public String improve(String content) {
        return generate(promptBuilder.improve(content));
    }

    @Override
    public String rewrite(String content) {
        return generate(promptBuilder.rewrite(content));
    }

    @Override
    public String translate(String content, LanguageCode sourceLanguage, LanguageCode targetLanguage) {
        return generate(promptBuilder.translate(content, sourceLanguage, targetLanguage));
    }

    @Override
    public CvContent translateContent(
            CvContent content, LanguageCode sourceLanguage, LanguageCode targetLanguage) {
        String contentJson;
        try {
            contentJson = objectMapper.writeValueAsString(content == null ? CvContent.empty() : content);
        } catch (Exception ex) {
            throw new AiServiceException("AI_INVALID_RESPONSE", "CV content could not be serialized");
        }
        String translated = generateText(promptBuilder.translateContent(contentJson, sourceLanguage, targetLanguage));
        try {
            CvContent parsed = objectMapper.readValue(translated, CvContent.class);
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

    private String generate(String prompt) {
        String text = generateText(prompt);
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

    private String generateText(String prompt) {
        GeminiApi.Response response;
        try {
            response = restClient
                    .post()
                    .uri(properties.baseUrl() + "/v1beta/models/" + properties.model() + ":generateContent")
                    .header("x-goog-api-key", properties.apiKey())
                    .body(GeminiApi.Request.of(prompt))
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
