import model.TestCase;
import model.TestSuite;

public class TestGenerator {

    public static void generate() {

        TestSuite suite = TestcasesParser.parse("generated/testcases.json");

        StringBuilder code = new StringBuilder();

        code.append("""
                package org.demo.generated;
                import org.demo.ui.BaseUiTest;
                import org.demo.ui.LoginPage;
                import org.testng.Assert;
                import org.testng.annotations.Test;
                public class GeneratedLoginTest extends org.demo.ui.BaseUiTest {
                """);

        for (TestCase tc : suite.testcases) {
            code.append(generateTest(tc));
        }

        code.append("}");

        FilesUtil.write(
                "src/test/java/org/demo/generated/GeneratedLoginTest.java",
                code.toString()
        );

        System.out.println("Autotests generated.");
    }

    private static String generateTest(TestCase tc) {

        String methodName = tc.id.toLowerCase() + "_" +
                tc.title.replaceAll("[^a-zA-Z]", "_");

        String assertion =
                tc.type.equalsIgnoreCase("positive")
                        ? "Assert.assertTrue(driver.getCurrentUrl().contains(\"inventory\"));"
                        : "Assert.assertTrue(page.isErrorVisible());";

        return """
                
                    @Test
                    public void %s() {
                        org.demo.ui.LoginPage page = new org.demo.ui.LoginPage(driver);
                        page.open();
                        page.login("standard_user", "secret_sauce");
                        %s
                    }
                """.formatted(methodName, assertion);
    }
}
