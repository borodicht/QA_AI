package agent.skills;

import agent.AgentContext;
import agent.AgentSkill;
import agent.AgentStepResult;
import client.LlmProfile;
import client.MistralClient;
import util.FilesUtil;

public class ScenarioGenerationSkill implements AgentSkill {

    @Override
    public String name() {
        return "GENERATE_SCENARIOS";
    }

    @Override
    public AgentStepResult execute(AgentContext context) {
        MistralClient.LlmResponse response = MistralClient.call(context.scenariosPrompt(), LlmProfile.SCENARIOS);
        FilesUtil.write("generated/scenarios_raw.json", response.rawBody());
        context.scenarios(response.content());
        FilesUtil.write("generated/scenarios.txt", response.content());
        return AgentStepResult.passed(name(), "Scenarios saved to generated/scenarios.txt");
    }
}
