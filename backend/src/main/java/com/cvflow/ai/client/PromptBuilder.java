package com.cvflow.ai.client;

import com.cvflow.cv.model.LanguageCode;
import org.springframework.stereotype.Component;

@Component
public class PromptBuilder {
    private static final String RULES =
            """
            Rules:
            - Never invent facts, technologies, companies, responsibilities, achievements, metrics or dates.
            - Never change factual information. Preserve meaning.
            - Use professional CV language. Keep output concise.
            - Do not add explanations.
            - Return structured JSON only, matching the required schema.
            """;

    public String improve(String content) {
        return """
                Improve the following CV text with minimal to moderate wording improvements.
                Preserve the original meaning and all factual information.
                Improve grammar, clarity and professional tone without unnecessary rewriting.

                CV text:
                %s

                %s
                """.formatted(content, RULES);
    }

    public String rewrite(String content) {
        return """
                Rewrite the following CV text with substantially stronger wording.
                Remain factually identical to the original. Do not invent any information.

                CV text:
                %s

                %s
                """.formatted(content, RULES);
    }

    public String translate(String content, LanguageCode sourceLanguage, LanguageCode targetLanguage) {
        return """
                Translate the following CV text from %s to %s.
                Preserve the exact meaning. Do not add or remove information.
                Use professional CV terminology. Preserve technical terms, company names and dates.

                CV text:
                %s

                %s
                """.formatted(sourceLanguage, targetLanguage, content, RULES);
    }
}
