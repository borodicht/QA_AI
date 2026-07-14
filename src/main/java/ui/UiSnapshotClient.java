package ui;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class UiSnapshotClient {

    private static final int MAX_HTML_LENGTH = 100_000;

    public static String loadHtml(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .header("User-Agent", "AI-QA-Pipeline/1.0")
                    .GET()
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RuntimeException("Страница вернула HTTP " + response.statusCode());
            }
            String html = response.body();
            return html.length() > MAX_HTML_LENGTH ? html.substring(0, MAX_HTML_LENGTH) : html;
        } catch (Exception e) {
            throw new RuntimeException("Не удалось получить HTML страницы: " + url, e);
        }
    }
}
