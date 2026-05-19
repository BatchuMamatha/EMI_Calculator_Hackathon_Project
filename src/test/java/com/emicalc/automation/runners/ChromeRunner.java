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
 * Chrome browser runner.
 *
 * Browser lifecycle:
 *   @BeforeClass -> open Chrome ONCE
 *   all 10 scenarios reuse the same window
 *   @AfterClass  -> close Chrome ONCE
 *
 * The browser parameter is taken directly from testng.xml (<parameter
 * name="browser" value="chrome"/>) — we do NOT write it to System.getProperty
 * because two parallel runner threads would clobber the same global key.
 */
@CucumberOptions(
        features = "src/test/resources/features",
        glue = {
                "com.emicalc.automation.stepdefinitions",
                "com.emicalc.automation.hooks"
        },
        plugin = {
                "pretty",
                "html:reports/cucumber/chrome-cucumber.html",
                "json:reports/cucumber/chrome-cucumber.json",
                // ExtentCucumberAdapter removed - it has a thread-safety bug
                // (GherkinDialect/Gson LinkedTreeMap concurrency). Extent is
                // now driven directly via ExtentManager from Hooks.java.
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
        },
        monochrome = true,
        publish = false
)
public class ChromeRunner extends AbstractTestNGCucumberTests {

    private static final Logger log = LogManager.getLogger(ChromeRunner.class);

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
