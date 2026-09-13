package com.cvflow.billing.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.billing")
public record BillingProperties(int freeAiCallsMonthly, int freeCvsMax) {}
