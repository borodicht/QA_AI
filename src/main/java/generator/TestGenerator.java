package generator;

import client.MistralClient;
import util.FilesUtil;

import java.util.LinkedHashSet;
import java.util.Set;

public class TestGenerator {

    private static final String OUTPUT_PATH =
            "src/test/java/org/demo/generated/GeneratedUiTest.java";

    public static String generate(String testcasesJson, String baseUiTest, String pageObject) {
        Set<String> availableMethods = PageObjectContractAnalyzer.extractPublicMethods(pageObject);
        String prompt = FilesUtil.read("prompts/04_autotests_java.txt")
                .replace("{{TESTCASES}}", testcasesJson)
                .replace("{{BASE_UI_TEST}}", baseUiTest)
                .replace("{{PAGE_OBJECT}}", pageObject)
                .replace("{{AVAILABLE_PAGE_OBJECT_METHODS}}",
                        PageObjectContractAnalyzer.formatMethodsForPrompt(availableMethods));

        FilesUtil.write("generated/autotests_prompt.txt", prompt);
        MistralClient.LlmResponse response = MistralClient.call(prompt);
        FilesUtil.write("generated/autotests_raw.json", response.rawBody());
        FilesUtil.write("generated/autotests_llm.txt", response.content());

        String javaCode = JavaCodeExtractor.extract(response.content());
        validate(javaCode, availableMethods);
        FilesUtil.write(OUTPUT_PATH, javaCode);
        return javaCode;
    }

    private static void validate(String javaCode, Set<String> availableMethods) {
        if (!javaCode.contains("package org.demo.generated;")
                || !javaCode.contains("class GeneratedUiTest")
                || !javaCode.contains("extends GeneratedBaseUiTest")
                || !javaCode.contains("@Test")) {
            throw new RuntimeException("Generated test does not satisfy the required Java test contract");
        }

        Set<String> calledMethods = PageObjectContractAnalyzer.extractCalledPageMethods(javaCode);
        LinkedHashSet<String> missingMethods = new LinkedHashSet<>(calledMethods);
        missingMethods.removeAll(availableMethods);
        if (!missingMethods.isEmpty()) {
            throw new RuntimeException(
                    "Generated test calls methods absent in GeneratedPageObject: " + missingMethods
                            + ". Available methods: " + availableMethods);
        }
    }
}
