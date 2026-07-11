package org.demo.generated;

import org.demo.ui.BaseUiTest;
import org.demo.ui.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class GeneratedLoginTest extends BaseUiTest {

    @Test
    public void testSuccessfulLogin() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open();
        loginPage.login("standard_user", "secret_sauce");
        Assert.assertTrue(driver.getCurrentUrl().contains("/inventory.html"), "Expected to be on products page after successful login");
    }

    @Test
    public void testLoginWithInvalidPassword() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open();
        loginPage.login("standard_user", "invalid_password");
        Assert.assertTrue(loginPage.isErrorVisible(), "Expected error message to be visible when using invalid password");
    }

    @Test
    public void testLoginWithEmptyCredentials() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open();
        loginPage.login("", "");
        Assert.assertTrue(loginPage.isErrorVisible(), "Expected error message to be visible when using empty credentials");
    }

    @Test
    public void testLoginWithLockedUser() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open();
        loginPage.login("locked_out_user", "secret_sauce");
        Assert.assertTrue(loginPage.isErrorVisible(), "Expected error message to be visible when using locked user credentials");
    }
}