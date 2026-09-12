package com.cvflow.pdf.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.pdf.gotenberg")
public record PdfProperties(String baseUrl) {}
