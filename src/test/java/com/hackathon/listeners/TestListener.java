package com.hackathon.listeners;

import com.hackathon.reports.ExtentManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.File;

public class TestListener implements ITestListener, ISuiteListener {

    private static final Logger log = LogManager.getLogger(TestListener.class);
    private static final String SCREENSHOTS_DIR = "screenshots";

    // Wipes leftover screenshots and logs the suite start.
    @Override
    public void onStart(ISuite suite) {
        cleanScreenshotsFolder();
        log.info("SUITE START: {}", suite.getName());
    }

    // Flushes the Extent report once both browser threads finish.
    @Override
    public void onFinish(ISuite suite) {
        ExtentManager.flush();
        log.info("SUITE END: {}", suite.getName());
    }

    // Deletes every *.png in screenshots/ so stale runs do not pile up.
    private void cleanScreenshotsFolder() {
        File dir = new File(SCREENSHOTS_DIR);
        if (!dir.exists()) { dir.mkdirs(); return; }
        File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".png"));
        if (files == null) return;
        int deleted = 0;
        for (File f : files) if (f.delete()) deleted++;
        if (deleted > 0) log.info("Cleared {} stale screenshot(s)", deleted);
    }

    // Logs the start of an individual TestNG test method.
    @Override public void onTestStart(ITestResult r)   { log.info("TEST START: {}", r.getMethod().getMethodName()); }

    // Logs a passing TestNG test method.
    @Override public void onTestSuccess(ITestResult r) { log.info("TEST PASS:  {}", r.getMethod().getMethodName()); }

    // Logs a skipped TestNG test method.
    @Override public void onTestSkipped(ITestResult r) { log.warn("TEST SKIP:  {}", r.getMethod().getMethodName()); }

    // Logs a failing TestNG test method with the underlying exception.
    @Override
    public void onTestFailure(ITestResult r) {
        log.error("TEST FAIL:  {} - {}", r.getMethod().getMethodName(), String.valueOf(r.getThrowable()));
    }

    // Logs the start of a TestNG <test> context.
    @Override public void onStart(ITestContext c)  { log.info("Context START: {}", c.getName()); }

    // Logs context end with pass/fail/skip counters.
    @Override
    public void onFinish(ITestContext c) {
        log.info("Context END: {} (passed={}, failed={}, skipped={})",
                c.getName(), c.getPassedTests().size(), c.getFailedTests().size(), c.getSkippedTests().size());
    }
}
