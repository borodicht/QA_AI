package generator;

import client.MistralClient;
import util.FilesUtil;

public class TestGenerator {

    private static final String OUTPUT_PATH =
            "src/test/java/org/demo/generated/GeneratedLoginTest.java";

    public static String generate(String testcasesJson) {
        String prompt = FilesUtil.read("prompts/05_autotests_java.txt")
                .replace("{{TESTCASES}}", testcasesJson)
                .replace("{{BASE_UI_TEST}}", FilesUtil.read("src/test/java/org/demo/ui/BaseUiTest.java"))
                .replace("{{LOGIN_PAGE}}", FilesUtil.read("src/test/java/org/demo/ui/LoginPage.java"));

        FilesUtil.write("generated/autotests_prompt.txt", prompt);
        MistralClient.LlmResponse response = MistralClient.call(prompt);
        FilesUtil.write("generated/autotests_raw.json", response.rawBody());
        FilesUtil.write("generated/autotests_llm.txt", response.content());

        String javaCode = JavaCodeExtractor.extract(response.content());
        validate(javaCode);
        FilesUtil.write(OUTPUT_PATH, javaCode);
        return javaCode;
    }

    private static void validate(String javaCode) {
        if (!javaCode.contains("package org.demo.generated;")
                || !javaCode.contains("class GeneratedLoginTest")
                || !javaCode.contains("extends BaseUiTest")
                || !javaCode.contains("@Test")) {
            throw new RuntimeException("Generated test does not satisfy the required Java test contract");
        }
    }
}
