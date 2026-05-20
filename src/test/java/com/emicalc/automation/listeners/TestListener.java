package com.emicalc.automation.listeners;

import com.emicalc.automation.reports.ExtentManager;
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

    @Override
    public void onStart(ISuite suite) {
        cleanScreenshotsFolder();
        log.info("SUITE START: {}", suite.getName());
    }

    @Override
    public void onFinish(ISuite suite) {
        ExtentManager.flush();
        log.info("SUITE END: {}", suite.getName());
    }

    private void cleanScreenshotsFolder() {
        File dir = new File(SCREENSHOTS_DIR);
        if (!dir.exists()) { dir.mkdirs(); return; }
        File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".png"));
        if (files == null) return;
        int deleted = 0;
        for (File f : files) if (f.delete()) deleted++;
        if (deleted > 0) log.info("Cleared {} stale screenshot(s)", deleted);
    }

    @Override public void onTestStart(ITestResult r)   { log.info("TEST START: {}", r.getMethod().getMethodName()); }
    @Override public void onTestSuccess(ITestResult r) { log.info("TEST PASS:  {}", r.getMethod().getMethodName()); }
    @Override public void onTestSkipped(ITestResult r) { log.warn("TEST SKIP:  {}", r.getMethod().getMethodName()); }

    @Override
    public void onTestFailure(ITestResult r) {
        log.error("TEST FAIL:  {} — {}", r.getMethod().getMethodName(), String.valueOf(r.getThrowable()));
    }

    @Override public void onStart(ITestContext c)  { log.info("Context START: {}", c.getName()); }

    @Override
    public void onFinish(ITestContext c) {
        log.info("Context END: {} (passed={}, failed={}, skipped={})",
                c.getName(), c.getPassedTests().size(), c.getFailedTests().size(), c.getSkippedTests().size());
    }
}
