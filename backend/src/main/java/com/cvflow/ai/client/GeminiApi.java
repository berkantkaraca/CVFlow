package com.cvflow.ai.client;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

public final class GeminiApi {
    private GeminiApi() {}

    public record Request(List<Content> contents, GenerationConfig generationConfig) {
        public static Request of(String prompt) {
            return new Request(
                    List.of(new Content(List.of(new Part(prompt)))),
                    new GenerationConfig(
                            "application/json",
                            new ResponseSchema(
                                    "OBJECT",
                                    new ResponseProperties(new SchemaProperty("STRING")),
                                    List.of("suggested"))));
        }

        public static Request ofDocument(String prompt) {
            return new Request(
                    List.of(new Content(List.of(new Part(prompt)))), new GenerationConfig("application/json", null));
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Content(List<Part> parts) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Part(String text) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record GenerationConfig(String responseMimeType, ResponseSchema responseSchema) {}

    public record ResponseSchema(String type, ResponseProperties properties, List<String> required) {}

    public record ResponseProperties(SchemaProperty suggested) {}

    public record SchemaProperty(String type) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(List<Candidate> candidates) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Candidate(Content content) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SuggestedText(String suggested) {}
}
