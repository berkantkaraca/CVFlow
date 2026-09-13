package com.cvflow.ai.client;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * Guards against the most common AI fabrication: numbers, percentages and metrics that do not
 * appear in the original text (e.g. invented "30%" improvements or user counts).
 */
@Component
public class AiOutputValidator {
    private static final Pattern NUMBERS = Pattern.compile("\\d[\\d.,]*\\s*[KkMmB%]?");

    public Optional<String> violation(String original, String suggested) {
        if (suggested == null || suggested.isBlank()) {
            return Optional.of("empty suggestion");
        }
        Set<String> allowed = numbers(original);
        Set<String> invented = new HashSet<>(numbers(suggested));
        invented.removeAll(allowed);
        if (!invented.isEmpty()) {
            return Optional.of("invented numbers or metrics not present in the original: "
                    + String.join(", ", invented.stream().sorted().toList()));
        }
        return Optional.empty();
    }

    private Set<String> numbers(String text) {
        Set<String> found = new HashSet<>();
        if (text == null) {
            return found;
        }
        Matcher matcher = NUMBERS.matcher(text);
        while (matcher.find()) {
            found.add(normalize(matcher.group()));
        }
        return found;
    }

    private String normalize(String token) {
        return token.replaceAll("[\\s,.]", "").toUpperCase();
    }
}
