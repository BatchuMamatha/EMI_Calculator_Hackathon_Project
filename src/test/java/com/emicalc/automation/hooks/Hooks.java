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
 * Per-scenario responsibilities here:
 *   - log scenario boundaries
 *   - reset session state (cookies) so tests don't leak into each other
 *   - drive ExtentReports directly (thread-safe — avoids the buggy
 *     ExtentCucumberAdapter that fails under parallel="tests")
 *   - capture a screenshot for EVERY scenario (PASS or FAIL), saved to
 *     reports/extent/screenshots/ with a descriptive filename:
 *         TCxx_<scenario_slug>_<PASS|FAIL>_<browser>_<timestamp>.png
 *     and attached inline to the Cucumber/Allure/Extent reports.
 */
public class Hooks {

    private static final Logger log = LogManager.getLogger(Hooks.class);

    @Before(order = 0)
    public void beforeScenario(Scenario scenario) {
        String browser = currentBrowser();
        String tcId = extractTcId(scenario);
        String reportName = String.format("[%s] %s %s", browser, tcId, scenario.getName());

        ExtentManager.startScenario(reportName,
                "Source: " + scenario.getUri().toString());
        ExtentManager.logStep(Status.INFO,
                "Starting on <b>" + browser + "</b> | thread=" + Thread.currentThread().getName());

        log.info("======================================================");
        log.info("Scenario START: {} {}  [thread={}, browser={}]",
                tcId, scenario.getName(), Thread.currentThread().getName(), browser);
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
        String browser = currentBrowser();
        String tcId    = extractTcId(scenario);
        String status  = scenario.isFailed() ? "FAIL" : "PASS";

        // ---- ALWAYS capture a screenshot (per requirement) ----
        byte[] png = ScreenshotUtils.captureAsBytes();
        if (png.length > 0) {
            // 1) On-disk PNG with self-describing name
            String fileBase = String.format("%s_%s_%s_%s",
                    tcId, sanitise(scenario.getName()), status, browser);
            ScreenshotUtils.capture(fileBase);

            // 2) Embed in Cucumber + Allure reports
            scenario.attach(png, "image/png",
                    String.format("%s | %s | %s", tcId, status, browser));

            // 3) Embed in the Extent report
            String label = String.format("%s — %s on %s", tcId, status, browser);
            if (scenario.isFailed()) {
                ExtentManager.attachFailureScreenshot(png, label);
            } else {
                ExtentManager.attachPassScreenshot(png, label);
            }
        }

        if (scenario.isFailed()) {
            log.warn("Scenario FAILED: {} {}", tcId, scenario.getName());
        } else {
            ExtentManager.logStep(Status.PASS, "Scenario passed");
        }
        log.info("Scenario END:   {} {}  status={}", tcId, scenario.getName(), scenario.getStatus());
        ExtentManager.endScenario();
    }

    // ---------- helpers ----------

    /** Read the {@code @TCxx} Gherkin tag if present, else return TCXX. */
    private String extractTcId(Scenario scenario) {
        return scenario.getSourceTagNames().stream()
                .filter(t -> t.matches("@TC\\d+"))
                .findFirst()
                .map(t -> t.substring(1))   // strip leading '@'
                .orElse("TCXX");
    }

    /** Reduce a scenario name to a filename-safe slug. */
    private String sanitise(String s) {
        if (s == null) return "scenario";
        String slug = s.replaceAll("[^a-zA-Z0-9]+", "_").replaceAll("_+", "_");
        return slug.length() > 60 ? slug.substring(0, 60) : slug;
    }

    private String currentBrowser() {
        try {
            WebDriver d = DriverFactory.getDriver();
            if (d instanceof HasCapabilities hc) {
                String name = hc.getCapabilities().getBrowserName();
                return (name == null || name.isBlank()) ? "unknown" : name.toLowerCase();
            }
        } catch (Exception ignored) {}
        return "unknown";
    }
}
