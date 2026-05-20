package com.emicalc.automation.hooks;

import com.aventstack.extentreports.Status;
import com.emicalc.automation.context.ScenarioContext;
import com.emicalc.automation.driver.DriverFactory;
import com.emicalc.automation.reports.ExtentManager;
import com.emicalc.automation.utils.ScreenshotUtils;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.WebDriver;

public class Hooks {

    private final ScenarioContext ctx;

    public Hooks(ScenarioContext ctx) { this.ctx = ctx; }

    @Before(order = 0)
    public void beforeScenario(Scenario scenario) {
        String browser = currentBrowser();
        String tcId = extractTcId(scenario);
        ExtentManager.startScenario(
                String.format("[%s] %s %s", browser, tcId, scenario.getName()),
                "Source: " + scenario.getUri());
        ExtentManager.logStep(Status.INFO, "Starting on <b>" + browser + "</b>");
        try { DriverFactory.getDriver().manage().deleteAllCookies(); }
        catch (Exception ignored) {}
    }

    @After(order = 1)
    public void afterScenario(Scenario scenario) {
        String browser = currentBrowser();
        String tcId    = extractTcId(scenario);

        // Detect any collected soft-assertion failures WITHOUT clearing them
        boolean softFailed = !ctx.softly.errorsCollected().isEmpty();
        boolean failed     = scenario.isFailed() || softFailed;
        String  status     = failed ? "FAILED" : "PASSED";

        String name = String.format("%s_%s_%s_%s",
                tcId, sanitise(scenario.getName()), status, browser);
        String path = ScreenshotUtils.capture(name);

        if (path != null) {
            String label = String.format("%s - %s on %s", tcId, status, browser);
            if (failed) ExtentManager.attachFailureScreenshot(path, label);
            else        ExtentManager.attachPassScreenshot(path, label);
        }
        if (!failed) ExtentManager.logStep(Status.PASS, "Scenario passed");

        // Flush soft asserts — will throw if any were collected, marking the
        // scenario as failed in Cucumber. Screenshot is already captured above.
        ctx.softly.assertAll();
    }

    private String extractTcId(Scenario scenario) {
        return scenario.getSourceTagNames().stream()
                .filter(t -> t.matches("@TC\\d+"))
                .findFirst()
                .map(t -> t.substring(1))
                .orElse("TCXX");
    }

    private String sanitise(String s) {
        if (s == null) return "scenario";
        String slug = s.replaceAll("[^a-zA-Z0-9]+", "_");
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
