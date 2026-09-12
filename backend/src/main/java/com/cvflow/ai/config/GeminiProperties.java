package com.cvflow.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ai.gemini")
public record GeminiProperties(String apiKey, String model, String baseUrl) {}
