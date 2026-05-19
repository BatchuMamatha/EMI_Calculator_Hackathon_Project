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
                "html:reports/cucumber/edge-cucumber.html",
                "json:reports/cucumber/edge-cucumber.json",
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
        },
        monochrome = true,
        publish = false
)
public class EdgeRunner extends AbstractTestNGCucumberTests {

    @BeforeClass(alwaysRun = true)
    @Parameters("browser")
    public void launchBrowser(String browser) { DriverFactory.initDriver(browser); }

    @AfterClass(alwaysRun = true)
    public void closeBrowser() { DriverFactory.quitDriver(); }
}
