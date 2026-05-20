package com.emicalc.automation.reports;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

import java.io.File;

// ExtentCucumberAdapter is bypassed: it has a Gson LinkedTreeMap concurrency
// bug that fails under parallel runs. We drive ExtentReports directly.
// ThreadLocal here is required ONLY because Chrome and Edge runners execute
// in parallel — each thread needs its own current ExtentTest reference.
public final class ExtentManager {

    private static final String REPORT_PATH = "reports/extent/SparkReport.html";
    private static final ExtentReports extent = new ExtentReports();
    private static final ThreadLocal<ExtentTest> TL_TEST = new ThreadLocal<>();

    static {
        new File("reports/extent").mkdirs();
        ExtentSparkReporter spark = new ExtentSparkReporter(REPORT_PATH);
        spark.config().setTheme(Theme.DARK);
        spark.config().setDocumentTitle("EMI Calculator Automation Report");
        spark.config().setReportName("EMI Calculator - INTQEA26QE003 Hackathon");
        spark.config().setTimeStampFormat("yyyy-MM-dd HH:mm:ss");
        extent.attachReporter(spark);
        extent.setSystemInfo("Project", "EMI Calculator Hackathon - INTQEA26QE003");
        extent.setSystemInfo("Site",    "emicalculator.net");
        extent.setSystemInfo("OS",      System.getProperty("os.name"));
        extent.setSystemInfo("Java",    System.getProperty("java.version"));
    }

    private ExtentManager() {}

    public static synchronized ExtentTest startScenario(String name, String description) {
        ExtentTest test = extent.createTest(name, description);
        TL_TEST.set(test);
        return test;
    }

    public static void logStep(Status status, String details) {
        ExtentTest t = TL_TEST.get();
        if (t != null) t.log(status, details);
    }

    public static void attachFailureScreenshot(String filePath, String label) { attach(filePath, label, true); }
    public static void attachPassScreenshot(String filePath, String label)    { attach(filePath, label, false); }

    private static void attach(String filePath, String label, boolean fail) {
        ExtentTest t = TL_TEST.get();
        if (t == null || filePath == null) return;
        try {
            var media = MediaEntityBuilder.createScreenCaptureFromPath(filePath).build();
            if (fail) t.fail(label, media); else t.pass(label, media);
        } catch (Exception ignored) {}
    }

    public static void endScenario() { TL_TEST.remove(); }

    public static synchronized void flush() { extent.flush(); }
}
