import com.fasterxml.jackson.databind.ObjectMapper;

public class PipelineMain {

    public static void main(String[] args) {
        System.out.println("=== AI QA PIPELINE STARTED ===");

        // STAGE 1. BUILD PROMPT FROM CHECKLIST
        String prompt = PromptEngine.buildPrompt(
                "prompts/01_scenarios_from_checklist.txt",
                "checklist_login.txt"
        );
        FilesUtil.write("generated/final_prompt.txt", prompt);

        // STAGE 2. PII SCAN & MASK
        PiiReport report = PiiScanner.scan(prompt);
        FilesUtil.write("generated/pii_report.txt", report.toText());

        String finalPrompt = prompt;

        if (report.hasFindings()) {
            System.out.println("PII detected. Masking input.");
            finalPrompt = PiiMasker.mask(prompt);
            FilesUtil.write("generated/prompt_masked.txt", finalPrompt);
        }

        // STAGE 3. GENERATE SCENARIOS
        String rawScenarios = MistralClient.call(finalPrompt);
        FilesUtil.write("generated/scenarios_raw.json", rawScenarios);

        String scenarios = extractAssistantContent(rawScenarios);
        FilesUtil.write("generated/ai_output.txt", scenarios);

        // ================================
        // STAGE 4. GENERATE JSON TESTCASES
        String jsonPrompt = FilesUtil.read("prompts/02_testcases_json.txt")
                .replace("{{SCENARIOS}}", scenarios);

        FilesUtil.write("generated/testcases_prompt.txt", jsonPrompt);

        String rawJson = MistralClient.call(jsonPrompt);
        FilesUtil.write("generated/testcases_raw.json", rawJson);

        String llmJsonText = extractAssistantContent(rawJson);
        FilesUtil.write("generated/testcases_llm.txt", llmJsonText);

        String pureJson = JsonExtractor.extractJson(llmJsonText);
        FilesUtil.write("generated/testcases.json", pureJson);

        System.out.println("Testcases generated: generated/testcases.json");

        // STAGE 5. GENERATE AUTOTESTS
        TestGenerator.generate();
        System.out.println("Autotests generated.");

        // STAGE 6. AI CODE REVIEW
        String generatedTest = FilesUtil.read(
                "src/test/java/org/demo/generated/GeneratedLoginTest.java"
        );

        String reviewPrompt = FilesUtil.read("prompts/03_code_review.txt")
                .replace("{{CODE}}", generatedTest);

        FilesUtil.write("generated/code_review_prompt.txt", reviewPrompt);

        String rawReview = MistralClient.call(reviewPrompt);
        FilesUtil.write("generated/code_review_raw.json", rawReview);

        String review = extractAssistantContent(rawReview);
        FilesUtil.write("generated/code_review.txt", review);

        System.out.println("AI code review saved: generated/code_review.txt");

        // STAGE. AI BUG REPORT (DESIGN-TIME)
        String checklist = FilesUtil.read("checklist_login.txt");
        String testcases = FilesUtil.read("generated/testcases.json");
        String codeReview = FilesUtil.read("generated/code_review.txt");

        String bugPrompt = FilesUtil.read("prompts/04_bug_report.txt")
                .replace("{{CHECKLIST}}", checklist)
                .replace("{{TESTCASES}}", testcases)
                .replace("{{REVIEW}}", codeReview);

        FilesUtil.write("generated/bug_report_prompt.txt", bugPrompt);

        String rawBug = MistralClient.call(bugPrompt);
        FilesUtil.write("generated/bug_report_raw.json", rawBug);

        String bugText = extractAssistantContent(rawBug);
        FilesUtil.write("generated/bug_report_llm.txt", bugText);

        String pureBugJson = JsonExtractor.extractJson(bugText);
        FilesUtil.write("generated/bug_report.json", pureBugJson);

        System.out.println("Bug report saved: generated/bug_report.json");

        System.out.println("=== AI QA PIPELINE FINISHED ===");
    }

    private static String extractAssistantContent(String rawJson) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readTree(rawJson)
                    .path("choices").get(0)
                    .path("message")
                    .path("content")
                    .asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract assistant content from LLM response", e);
        }
    }
}
