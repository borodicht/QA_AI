package client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.util.List;
import java.util.Map;

public class MistralClient {

    private static final String API_BASE_URL = "https://api.mistral.ai";
    private static final String CHAT_COMPLETIONS_PATH = "/v1/chat/completions";
    private static final String API_KEY = System.getenv("MISTRAL_API_KEY");

    public static LlmResponse call(String prompt) {

        if (API_KEY == null || API_KEY.isBlank()) {
            throw new RuntimeException("MISTRAL_API_KEY not set");
        }

        try {
            Map<String, Object> body = Map.of(
                    "model", "mistral-small-latest",
                    "messages", List.of(
                            Map.of("role", "system", "content", "You are a QA engineer. Return structured output."),
                            Map.of("role", "user", "content", prompt)
                    ),
                    "temperature", 0.2
            );

            Response response = RestAssured.given()
                    .baseUri(API_BASE_URL)
                    .contentType(ContentType.JSON)
                    .header("Authorization", "Bearer " + API_KEY)
                    .body(body)
                    .post(CHAT_COMPLETIONS_PATH);

            String rawBody = response.getBody().asString();
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RuntimeException("Mistral API returned HTTP " + response.statusCode() + ": " + rawBody);
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

    public record LlmResponse(String rawBody, String content) {
    }
}
