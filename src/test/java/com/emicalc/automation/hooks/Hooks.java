package com.emicalc.automation.hooks;

import com.aventstack.extentreports.Status;
import com.emicalc.automation.driver.DriverFactory;
import com.emicalc.automation.reports.ExtentManager;
import com.emicalc.automation.utils.ScreenshotUtils;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.WebDriver;

/**
 * Cucumber per-scenario hooks. The browser is launched ONCE per runner class
 * (see {@link com.emicalc.automation.runners.ChromeRunner} /
 * {@link com.emicalc.automation.runners.EdgeRunner}) and stays open for all
 * 10 scenarios.
 *
 * Hooks only:
 *   - log scenario boundaries
 *   - reset per-scenario state (cookies) so tests don't leak into each other
 *   - drive ExtentReports directly (thread-safe — avoids the buggy
 *     ExtentCucumberAdapter that fails under parallel="tests")
 *   - capture a screenshot on failure
 */
public class Hooks {

    private static final Logger log = LogManager.getLogger(Hooks.class);

    @Before(order = 0)
    public void beforeScenario(Scenario scenario) {
        String browser = currentBrowser();
        String reportName = "[" + browser + "] " + scenario.getName();
        ExtentManager.startScenario(reportName,
                "Source: " + scenario.getUri().toString());
        ExtentManager.logStep(Status.INFO,
                "Starting on <b>" + browser + "</b> | thread=" + Thread.currentThread().getName());

        log.info("======================================================");
        log.info("Scenario START: {}  [thread={}, browser={}]",
                scenario.getName(), Thread.currentThread().getName(), browser);
        log.info("======================================================");

        // Clean session state so each scenario starts cookie-free
        try {
            WebDriver d = DriverFactory.getDriver();
            d.manage().deleteAllCookies();
        } catch (Exception e) {
            log.warn("Could not clear cookies before scenario: {}", e.getMessage());
        }
    }

    @After(order = 1)
    public void afterScenario(Scenario scenario) {
        if (scenario.isFailed()) {
            log.warn("Scenario FAILED: {} — capturing screenshot", scenario.getName());
            byte[] png = ScreenshotUtils.captureAsBytes();
            if (png.length > 0) {
                // Attach to Cucumber's HTML/JSON report
                scenario.attach(png, "image/png", scenario.getName() + "_failure");
                // Attach to our Extent report
                ExtentManager.attachFailureScreenshot(png,
                        "Scenario failed: " + scenario.getName());
            }
            ScreenshotUtils.capture(scenario.getName() + "_FAIL");
        } else {
            ExtentManager.logStep(Status.PASS, "Scenario passed");
        }
        log.info("Scenario END:   {}  status={}", scenario.getName(), scenario.getStatus());
        ExtentManager.endScenario();
    }

    // ---------- helpers ----------

    private String currentBrowser() {
        try {
            WebDriver d = DriverFactory.getDriver();
            if (d instanceof HasCapabilities hc) {
                String name = hc.getCapabilities().getBrowserName();
                return (name == null || name.isBlank()) ? "unknown" : name;
            }
        } catch (Exception ignored) {}
        return "unknown";
    }
}
