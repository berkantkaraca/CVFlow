package com.cvflow.ai.client;

import com.cvflow.cv.model.LanguageCode;

public interface AiClient {
    String improve(String content);

    String rewrite(String content);

    String translate(String content, LanguageCode sourceLanguage, LanguageCode targetLanguage);
}
