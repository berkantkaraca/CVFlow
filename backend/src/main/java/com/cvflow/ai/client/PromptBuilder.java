package com.cvflow.ai.client;

import com.cvflow.cv.model.LanguageCode;
import org.springframework.stereotype.Component;

@Component
public class PromptBuilder {    private static final String RULES =
            """
            Rules:
            - Never invent facts, technologies, tools, companies, products, responsibilities,
              achievements, metrics, numbers, percentages, dates or proper nouns.
            - Every number, percentage, date and proper noun in the output must appear
              verbatim in the input. If the input has no numbers, the output must have none.
            - Never change factual information. Preserve meaning.
            - Change as few words as possible while achieving the goal.
            - If the input is short, keep the output short. Do not pad with invented detail.
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

    public String translateContent(String contentJson, LanguageCode sourceLanguage, LanguageCode targetLanguage) {
        return """
                Translate every human-readable string value in the following CV JSON document from %s to %s.
                Preserve the exact meaning of each string. Do not add or remove information.
                Use professional CV terminology. Preserve technical terms, company names and dates.
                Keep all JSON keys, the document structure and every id value exactly as they are.
                Translate only the human-readable string values.
                Return the full translated JSON document and nothing else.

                CV JSON:
                %s

                %s
                """.formatted(sourceLanguage, targetLanguage, contentJson, RULES);
    }

    public String correction(String original, String badSuggestion, String violations) {
        return """
                Your previous response violated the rules in the ways listed below.
                Produce a corrected response that keeps every factual detail identical
                to the original text. Change wording only.

                Violations:
                %s

                Original text:
                %s

                Rejected response:
                %s

                %s
                """.formatted(violations, original, badSuggestion, RULES);
    }
}
