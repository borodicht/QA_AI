package client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import config.ApplicationConfig;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.util.List;
import java.util.Map;

public class MistralClient {

        private static final String CHAT_COMPLETIONS_PATH = "/v1/chat/completions";

        public static LlmResponse call(String prompt) {

            try {
                ApplicationConfig config = ApplicationConfig.load();
                Map<String, Object> body = Map.of(
                        "model", config.llmModel(),
                        "messages", List.of(
                                Map.of("role", "system", "content", config.llmSystemPrompt()),
                                Map.of("role", "user", "content", prompt)
                        ),
                        "temperature", 0.2
                );

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
