package com.emicalc.automation.runners;

import com.emicalc.automation.driver.DriverFactory;
import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;

@CucumberOptions(
        features = "src/test/resources/features",
        glue = {"com.emicalc.automation.stepdefinitions", "com.emicalc.automation.hooks"},
        plugin = {
                "pretty",
                "html:reports/cucumber/chrome-cucumber.html",
                "json:reports/cucumber/chrome-cucumber.json",
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
        },
        monochrome = true,
        publish = false
)
public class ChromeRunner extends AbstractTestNGCucumberTests {

    @BeforeClass(alwaysRun = true)
    @Parameters("browser")
    public void launchBrowser(String browser) { DriverFactory.initDriver(browser); }

    @AfterClass(alwaysRun = true)
    public void closeBrowser() { DriverFactory.quitDriver(); }
}
