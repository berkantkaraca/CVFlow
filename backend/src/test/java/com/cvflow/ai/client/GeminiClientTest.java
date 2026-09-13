package com.cvflow.ai.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cvflow.common.exception.AiServiceException;
import com.cvflow.cv.model.LanguageCode;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

class GeminiClientTest {
    private HttpServer server;
    private GeminiClient client;
    private volatile int status = 200;
    private volatile String body = geminiBody("{\"suggested\":\"Improved text\"}");
    private volatile String retryBody;
    private final AtomicInteger calls = new AtomicInteger();

    @BeforeEach
    void setup() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/", exchange -> {
            int call = calls.getAndIncrement();
            String responseBody = call == 0 || retryBody == null ? body : retryBody;
            byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(bytes);
            }
        });
        server.start();
        String baseUrl = "http://localhost:" + server.getAddress().getPort();
        client = new GeminiClient(
                RestClient.builder().build(),
                new com.cvflow.ai.config.GeminiProperties("test-key", "gemini-2.5-flash", baseUrl),
                new ObjectMapper(),
                new PromptBuilder(),
                new AiOutputValidator());
    }

    @AfterEach
    void stop() {
        server.stop(0);
    }

    @Test
    void improveReturnsSuggestedText() {
        assertEquals("Improved text", client.improve("Original"));
        assertEquals(1, calls.get());
    }

    @Test
    void rewriteAndTranslateWork() {
        assertEquals("Improved text", client.rewrite("Original"));
        assertEquals("Improved text", client.translate("Original", LanguageCode.TR, LanguageCode.EN));
    }

    @Test
    void emptyCandidatesMapToEmptyResponse() {
        body = "{\"candidates\":[]}";

        AiServiceException ex = assertThrows(AiServiceException.class, () -> client.improve("x"));
        assertEquals("AI_EMPTY_RESPONSE", ex.getCode());
    }

    @Test
    void invalidJsonMapsToInvalidResponse() {
        body = geminiBody("not-json");

        AiServiceException ex = assertThrows(AiServiceException.class, () -> client.improve("x"));
        assertEquals("AI_INVALID_RESPONSE", ex.getCode());
    }

    @Test
    void httpErrorMapsToServiceError() {
        status = 500;
        body = "boom";

        AiServiceException ex = assertThrows(AiServiceException.class, () -> client.improve("x"));
        assertEquals("AI_SERVICE_ERROR", ex.getCode());
        assertTrue(ex.getMessage().contains("failed"));
    }

    @Test
    void retriesOnceWhenSuggestionInventsNumbers() {
        body = geminiBody("{\"suggested\":\"Improved text, 30% faster for 100K users\"}");
        retryBody = geminiBody("{\"suggested\":\"Improved text\"}");

        assertEquals("Improved text", client.improve("Original text"));
        assertEquals(2, calls.get());
    }

    @Test
    void failsWhenRetryAlsoInventsNumbers() {
        body = geminiBody("{\"suggested\":\"Faster by 30%\"}");
        retryBody = geminiBody("{\"suggested\":\"Faster by 40%\"}");

        AiServiceException ex = assertThrows(AiServiceException.class, () -> client.improve("Original text"));
        assertEquals("AI_UNRELIABLE_RESPONSE", ex.getCode());
        assertEquals(2, calls.get());
    }

    @Test
    void translateContentParsesTranslatedDocument() {        body = geminiBody(
                "{\"personal\":{},\"summary\":\"Yazılım geliştirici\",\"experiences\":[],\"education\":[],"
                        + "\"projects\":[],\"skills\":[],\"languages\":[],\"certifications\":[],\"links\":[]}");

        com.cvflow.cv.model.CvContent translated =
                client.translateContent(com.cvflow.cv.model.CvContent.empty(), LanguageCode.EN, LanguageCode.TR);

        assertEquals("Yazılım geliştirici", translated.getSummary());
    }

    @Test
    void translateContentRejectsInvalidDocument() {
        body = geminiBody("{\"summary\":");

        AiServiceException ex = assertThrows(
                AiServiceException.class,
                () -> client.translateContent(com.cvflow.cv.model.CvContent.empty(), LanguageCode.EN, LanguageCode.TR));
        assertEquals("AI_INVALID_RESPONSE", ex.getCode());
    }

    @Test
    void tailorContentParsesDocument() {
        body = geminiBody(
                "{\"personal\":{},\"summary\":\"Tailored summary\",\"experiences\":[],\"education\":[],"
                        + "\"projects\":[],\"skills\":[],\"languages\":[],\"certifications\":[],\"links\":[]}");

        com.cvflow.cv.model.CvContent tailored = client.tailorContent(
                com.cvflow.cv.model.CvContent.empty(), "Hiring Java devs", LanguageCode.EN);

        assertEquals("Tailored summary", tailored.getSummary());
    }

    @Test
    void generateCoverLetterReturnsBody() {
        assertEquals(
                "Improved text",
                client.generateCoverLetter(com.cvflow.cv.model.CvContent.empty(), "JD", LanguageCode.EN));
    }

    private static String geminiBody(String innerText) {
        String escaped = innerText.replace("\\", "\\\\").replace("\"", "\\\"");
        return "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"" + escaped
                + "\",\"thoughtSignature\":\"abc\"}]}}],\"usageMetadata\":{\"totalTokenCount\":1}}";
    }
}
