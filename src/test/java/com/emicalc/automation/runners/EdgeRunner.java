package com.emicalc.automation.runners;

import com.emicalc.automation.driver.DriverFactory;
import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;

/**
 * Edge browser runner — mirror of {@link ChromeRunner} but for Microsoft Edge.
 *
 * Browser lifecycle:
 *   @BeforeClass -> open Edge ONCE
 *   all 10 scenarios reuse the same window
 *   @AfterClass  -> close Edge ONCE
 *
 * Runs in parallel with ChromeRunner thanks to testng.xml parallel="tests"
 * and DriverFactory's ThreadLocal driver storage.
 */
@CucumberOptions(
        features = "src/test/resources/features",
        glue = {
                "com.emicalc.automation.stepdefinitions",
                "com.emicalc.automation.hooks"
        },
        plugin = {
                "pretty",
                "html:reports/cucumber/edge-cucumber.html",
                "json:reports/cucumber/edge-cucumber.json",
                // ExtentCucumberAdapter removed - it has a thread-safety bug
                // (GherkinDialect/Gson LinkedTreeMap concurrency). Extent is
                // now driven directly via ExtentManager from Hooks.java.
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
        },
        monochrome = true,
        publish = false
)
public class EdgeRunner extends AbstractTestNGCucumberTests {

    private static final Logger log = LogManager.getLogger(EdgeRunner.class);

    @BeforeClass(alwaysRun = true)
    @Parameters("browser")
    public void launchBrowser(String browser) {
        log.info("[{}] >>> Opening browser ONCE for all scenarios: {}",
                Thread.currentThread().getName(), browser);
        DriverFactory.initDriver(browser);
    }

    @AfterClass(alwaysRun = true)
    public void closeBrowser() {
        log.info("[{}] <<< Closing browser after all scenarios",
                Thread.currentThread().getName());
        DriverFactory.quitDriver();
    }
}
