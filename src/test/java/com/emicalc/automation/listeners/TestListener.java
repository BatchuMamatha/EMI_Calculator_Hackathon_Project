package com.emicalc.automation.listeners;

import com.emicalc.automation.reports.ExtentManager;
import com.emicalc.automation.utils.ScreenshotUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.File;

/**
 * TestNG listener for suite-level + per-test logging and screenshot on failure.
 * Wired in via testng.xml.
 */
public class TestListener implements ITestListener, ISuiteListener {

    private static final Logger log = LogManager.getLogger(TestListener.class);
    private static final String SCREENSHOTS_DIR = "reports/extent/screenshots";

    // ------- Suite hooks -------
    @Override
    public void onStart(ISuite suite) {
        cleanScreenshotsFolder();
        log.info("######################################################");
        log.info("# SUITE START: {}", suite.getName());
        log.info("######################################################");
    }

    /**
     * Delete every *.png left behind by previous runs. Without this, stale
     * "embedding1.png", "*_FAIL_*.png" and other artefacts pile up and make
     * it impossible to tell which screenshots belong to the current run.
     */
    private void cleanScreenshotsFolder() {
        File dir = new File(SCREENSHOTS_DIR);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                log.warn("Could not create {}", dir.getAbsolutePath());
            }
            return;
        }
        File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".png"));
        if (files == null || files.length == 0) {
            log.info("Screenshots folder already clean: {}", dir.getAbsolutePath());
            return;
        }
        int deleted = 0;
        for (File f : files) {
            if (f.delete()) deleted++;
        }
        log.info("Cleared {} stale screenshot(s) from {}", deleted, dir.getAbsolutePath());
    }

    @Override
    public void onFinish(ISuite suite) {
        // Flush the Extent report once, AFTER both browser threads are done.
        ExtentManager.flush();
        log.info("######################################################");
        log.info("# SUITE END:   {}", suite.getName());
        log.info("######################################################");
    }

    // ------- Test hooks -------
    @Override
    public void onTestStart(ITestResult result) {
        log.info("---- TEST START: {} ----", result.getMethod().getMethodName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        log.info("---- TEST PASS:  {} ----", result.getMethod().getMethodName());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        log.error("---- TEST FAIL:  {} ----", result.getMethod().getMethodName());
        log.error("Reason: {}", String.valueOf(result.getThrowable()));
        // Screenshot is already captured at scenario level by Hooks.@After,
        // with a descriptive name (TCxx_<scenario>_FAIL_<browser>_<ts>.png).
        // Skipping the redundant capture here to keep the screenshot folder clean.
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        log.warn("---- TEST SKIP:  {} ----", result.getMethod().getMethodName());
    }

    @Override
    public void onStart(ITestContext context) {
        log.info("== Test Context Start: {} ==", context.getName());
    }

    @Override
    public void onFinish(ITestContext context) {
        log.info("== Test Context End:   {}  (passed={}, failed={}, skipped={}) ==",
                context.getName(),
                context.getPassedTests().size(),
                context.getFailedTests().size(),
                context.getSkippedTests().size());
    }
}
