package generator;

import client.MistralClient;
import util.FilesUtil;

public class PageObjectGenerator {

    public static final String PATH =
            "src/test/java/org/demo/generated/pages/GeneratedPageObject.java";

    public static String generate(String url, String html) {
        String prompt = FilesUtil.read("prompts/03_page_object_java.txt")
                .replace("{{APPLICATION_URL}}", url)
                .replace("{{HTML}}", html);
        FilesUtil.write("generated/page_object_prompt.txt", prompt);

        MistralClient.LlmResponse response = MistralClient.call(prompt);
        FilesUtil.write("generated/page_object_raw.json", response.rawBody());
        FilesUtil.write("generated/page_object_llm.txt", response.content());

        String javaCode = JavaCodeExtractor.extract(response.content());
        if (!javaCode.contains("package org.demo.generated.pages;")
                || !javaCode.contains("class GeneratedPageObject")) {
            throw new RuntimeException("Сгенерированный Page Object не соответствует контракту");
        }
        FilesUtil.write(PATH, javaCode);
        return javaCode;
    }
}
