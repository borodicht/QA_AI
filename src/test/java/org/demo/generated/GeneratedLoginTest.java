package org.demo.generated;
import org.demo.ui.BaseUiTest;
import org.demo.ui.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;
public class GeneratedLoginTest extends org.demo.ui.BaseUiTest {

    @Test
    public void login_01_Valid_login() {
        org.demo.ui.LoginPage page = new org.demo.ui.LoginPage(driver);
        page.open();
        page.login("standard_user", "secret_sauce");
        Assert.assertTrue(driver.getCurrentUrl().contains("inventory"));
    }

    @Test
    public void login_02_Invalid_password() {
        org.demo.ui.LoginPage page = new org.demo.ui.LoginPage(driver);
        page.open();
        page.login("standard_user", "secret_sauce");
        Assert.assertTrue(page.isErrorVisible());
    }

    @Test
    public void login_03_Invalid_username() {
        org.demo.ui.LoginPage page = new org.demo.ui.LoginPage(driver);
        page.open();
        page.login("standard_user", "secret_sauce");
        Assert.assertTrue(page.isErrorVisible());
    }

    @Test
    public void login_04_Empty_fields() {
        org.demo.ui.LoginPage page = new org.demo.ui.LoginPage(driver);
        page.open();
        page.login("standard_user", "secret_sauce");
        Assert.assertTrue(page.isErrorVisible());
    }

    @Test
    public void login_05_Locked_user() {
        org.demo.ui.LoginPage page = new org.demo.ui.LoginPage(driver);
        page.open();
        page.login("standard_user", "secret_sauce");
        Assert.assertTrue(page.isErrorVisible());
    }
}