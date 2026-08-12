package agent.skills;

import agent.AgentContext;
import agent.AgentSkill;
import agent.AgentStepResult;
import client.LlmProfile;
import client.MistralClient;
import util.FilesUtil;

public class SummarySkill implements AgentSkill {

    @Override
    public String name() {
        return "GENERATE_AGENT_SUMMARY";
    }

    @Override
    public AgentStepResult execute(AgentContext context) {
        String prompt = FilesUtil.read("prompts/06_agent_summary.txt")
                .replace("{{USER_GOAL}}", context.userGoal())
                .replace("{{AGENT_TRACE}}", context.formatTrace())
                .replace("{{TEST_RUN_LOG}}", nullToEmpty(context.testRunLog()))
                .replace("{{ALLURE_RESULTS}}", nullToEmpty(context.allureResults()))
                .replace("{{BUG_REPORT}}", nullToEmpty(context.bugReportJson()));
        FilesUtil.write("generated/agent_summary_prompt.txt", prompt);

        MistralClient.LlmResponse response = MistralClient.call(prompt, LlmProfile.SUMMARY);
        FilesUtil.write("generated/agent_summary_raw.json", response.rawBody());
        FilesUtil.write("generated/agent_summary.txt", response.content());
        context.finalReport(response.content());
        return AgentStepResult.passed(name(), "Summary saved to generated/agent_summary.txt");
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
