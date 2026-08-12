package agent.skills;

import agent.AgentContext;
import agent.AgentSkill;
import agent.AgentStepResult;
import util.FilesUtil;
import util.TestRunExecutor;

public class AllureResultsSkill implements AgentSkill {

    @Override
    public String name() {
        return "READ_ALLURE_RESULTS";
    }

    @Override
    public AgentStepResult execute(AgentContext context) {
        String allureReportLog = TestRunExecutor.buildAllureReport();
        FilesUtil.write("generated/allure_report.log", allureReportLog);
        String allureResults = FilesUtil.readFilesBySuffix("target/allure-results", "-result.json");
        context.allureResults(allureResults);
        FilesUtil.write("generated/allure_results.txt", allureResults);
        return AgentStepResult.passed(name(), "Allure results saved to generated/allure_results.txt");
    }
}
