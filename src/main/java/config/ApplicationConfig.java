package config;

import util.FilesUtil;

import java.util.Properties;

public record ApplicationConfig(
        String url,
        String llmProvider,
        String llmBaseUrl,
        String llmModel,
        String llmSystemPrompt,
        String mistralApiKey
) {

    private static final String DEFAULT_LLM_PROVIDER = "mistral";
    private static final String DEFAULT_MISTRAL_BASE_URL = "https://api.mistral.ai";
    private static final String DEFAULT_MISTRAL_MODEL = "mistral-small-latest";
    private static final String DEFAULT_OLLAMA_BASE_URL = "http://localhost:11434";
    private static final String DEFAULT_OLLAMA_MODEL = "gemma4:latest";
    private static final String DEFAULT_LLM_SYSTEM_PROMPT =
            "You are a QA engineer. Return structured output.";

    public static ApplicationConfig load() {
        try {
            Properties properties = new Properties();
            properties.load(new java.io.StringReader(FilesUtil.read("application.properties")));
            String url = properties.getProperty("application.url", "").trim();
            if (url.isBlank()) {
                throw new RuntimeException("Не задано свойство application.url");
            }
            String llmProvider = properties.getProperty("llm.provider", DEFAULT_LLM_PROVIDER).trim().toLowerCase();
            if (!"mistral".equals(llmProvider) && !"ollama".equals(llmProvider)) {
                throw new RuntimeException("llm.provider должен быть mistral или ollama");
            }
            String defaultBaseUrl = "ollama".equals(llmProvider) ? DEFAULT_OLLAMA_BASE_URL : DEFAULT_MISTRAL_BASE_URL;
            String defaultModel = "ollama".equals(llmProvider) ? DEFAULT_OLLAMA_MODEL : DEFAULT_MISTRAL_MODEL;
            String llmBaseUrl = properties.getProperty("llm.baseUrl", defaultBaseUrl).trim();
            String llmModel = properties.getProperty("llm.model", defaultModel).trim();
            String llmSystemPrompt = properties.getProperty("llm.systemPrompt", DEFAULT_LLM_SYSTEM_PROMPT).trim();
            String mistralApiKey = System.getenv("MISTRAL_API_KEY");
            if (llmBaseUrl.isBlank()) {
                throw new RuntimeException("Не задано свойство llm.baseUrl");
            }
            if (llmModel.isBlank()) {
                throw new RuntimeException("Не задано свойство llm.model");
            }
            if (llmSystemPrompt.isBlank()) {
                throw new RuntimeException("Не задано свойство llm.systemPrompt");
            }
            if ("mistral".equals(llmProvider) && (mistralApiKey == null || mistralApiKey.isBlank())) {
                throw new RuntimeException("Для llm.provider=mistral должна быть установлена переменная MISTRAL_API_KEY");
            }
            return new ApplicationConfig(
                    url,
                    llmProvider,
                    llmBaseUrl,
                    llmModel,
                    llmSystemPrompt,
                    mistralApiKey
            );
        } catch (Exception e) {
            throw new RuntimeException("Не удалось загрузить application.properties", e);
        }
    }
}
