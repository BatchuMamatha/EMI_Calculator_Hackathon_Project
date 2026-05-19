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

/**
 * TestNG listener for suite-level + per-test logging and screenshot on failure.
 * Wired in via testng.xml.
 */
public class TestListener implements ITestListener, ISuiteListener {

    private static final Logger log = LogManager.getLogger(TestListener.class);

    // ------- Suite hooks -------
    @Override
    public void onStart(ISuite suite) {
        log.info("######################################################");
        log.info("# SUITE START: {}", suite.getName());
        log.info("######################################################");
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
        ScreenshotUtils.capture("testng_fail_" + result.getMethod().getMethodName());
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
