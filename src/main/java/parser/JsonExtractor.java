package parser;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonExtractor {

    public static String extractJson(String text) {

        if (text == null) {
            throw new RuntimeException("LLM returned null");
        }

        // remove markdown fences
        String cleaned = text
                .replace("```json", "")
                .replace("```", "")
                .trim();

        // try to cut everything before first {
        int start = cleaned.indexOf("{");
        int end = cleaned.lastIndexOf("}");

        if (start == -1 || end == -1) {
            throw new RuntimeException("No JSON object found in LLM output");
        }

        String json = cleaned.substring(start, end + 1).trim();
        try {
            new ObjectMapper().readTree(json);
            return json;
        } catch (Exception e) {
            throw new RuntimeException("LLM output does not contain valid JSON", e);
        }
    }
}
