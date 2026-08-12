package client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import config.ApplicationConfig;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MistralClient {

        private static final String CHAT_COMPLETIONS_PATH = "/v1/chat/completions";

        public static LlmResponse call(String prompt) {
            return call(prompt, LlmProfile.DEFAULT);
        }

        public static LlmResponse call(String prompt, String profile) {

            try {
                ApplicationConfig config = ApplicationConfig.load();
                Map<String, Object> body = new LinkedHashMap<>();
                body.put("model", config.llmModel(profile));
                body.put("messages", List.of(
                                Map.of("role", "system", "content", config.llmSystemPrompt(profile)),
                                Map.of("role", "user", "content", prompt)
                        ));
                body.put("temperature", config.llmTemperature(profile));

                Integer maxTokens = config.llmMaxTokens(profile);
                if (maxTokens != null) {
                    body.put("max_tokens", maxTokens);
                }

                Double topP = config.llmTopP(profile);
                if (topP != null) {
                    body.put("top_p", topP);
                }

                Response response = RestAssured.given()
                        .baseUri(config.llmBaseUrl())
                        .contentType(ContentType.JSON)
                        .headers(buildHeaders(config))
                        .body(body)
                        .post(CHAT_COMPLETIONS_PATH);

                String rawBody = response.getBody().asString();
                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    throw new RuntimeException("LLM API returned HTTP " + response.statusCode() + ": " + rawBody);
                }

                JsonNode content = new ObjectMapper().readTree(rawBody)
                        .path("choices").path(0).path("message").path("content");
                if (content.isMissingNode() || content.asText().isBlank()) {
                    throw new RuntimeException("Mistral API response has no assistant content: " + rawBody);
                }
                return new LlmResponse(rawBody, content.asText());

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private static Map<String, String> buildHeaders(ApplicationConfig config) {
            if ("mistral".equals(config.llmProvider())) {
                return Map.of("Authorization", "Bearer " + config.mistralApiKey());
            }
            return Map.of();
        }

        public record LlmResponse(String rawBody, String content) {
        }
}
