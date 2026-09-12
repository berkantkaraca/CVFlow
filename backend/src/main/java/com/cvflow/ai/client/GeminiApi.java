package com.cvflow.ai.client;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

public final class GeminiApi {
    private GeminiApi() {}

    public record Request(List<Content> contents, GenerationConfig generationConfig) {
        public static Request of(String prompt) {
            return new Request(
                    List.of(new Content(List.of(new Part(prompt)))),
                    new GenerationConfig(
                            "application/json",
                            new ResponseSchema(
                                    "OBJECT", new SchemaProperty("STRING"), List.of("suggested"))));
        }
    }

    public record Content(List<Part> parts) {}

    public record Part(String text) {}

    public record GenerationConfig(String responseMimeType, ResponseSchema responseSchema) {}

    public record ResponseSchema(String type, SchemaProperty suggested, List<String> required) {}

    public record SchemaProperty(String type) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(List<Candidate> candidates) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Candidate(Content content) {}

    public record SuggestedText(String suggested) {}
}
