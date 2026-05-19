package com.emicalc.automation.reports;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

import java.io.File;
import java.util.Base64;

// ExtentCucumberAdapter is bypassed: it has a Gson LinkedTreeMap concurrency
// bug that fails under parallel="tests". We drive ExtentReports directly.
public final class ExtentManager {

    private static final String REPORT_PATH = "reports/extent/SparkReport.html";
    private static final ExtentReports extent = new ExtentReports();
    private static final ThreadLocal<ExtentTest> tlTest = new ThreadLocal<>();

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
        tlTest.set(test);
        return test;
    }

    public static void logStep(Status status, String details) {
        ExtentTest t = tlTest.get();
        if (t != null) t.log(status, details);
    }

    public static void attachFailureScreenshot(byte[] png, String label) { attach(png, label, true); }
    public static void attachPassScreenshot(byte[] png, String label)    { attach(png, label, false); }

    private static void attach(byte[] png, String label, boolean fail) {
        ExtentTest t = tlTest.get();
        if (t == null || png == null || png.length == 0) return;
        String b64 = Base64.getEncoder().encodeToString(png);
        try {
            var media = MediaEntityBuilder.createScreenCaptureFromBase64String(b64).build();
            if (fail) t.fail(label, media); else t.pass(label, media);
        } catch (Exception ignored) {}
    }

    public static void endScenario() { tlTest.remove(); }

    public static synchronized void flush() { extent.flush(); }
}
