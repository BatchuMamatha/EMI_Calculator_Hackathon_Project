package com.emicalc.automation.reports;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Base64;

/**
 * Thread-safe wrapper around ExtentReports.
 *
 * Why this exists: tech.grasshopper:extentreports-cucumber7-adapter (v1.14.0)
 * calls {@code ExtentReports.setGherkinDialect("en")} from
 * {@code handleStartOfFeature(...)}, which deserialises a shared
 * {@code LinkedTreeMap} via Gson. Two parallel TestNG threads triggering that
 * simultaneously throws {@code java.lang.AssertionError} inside
 * {@code LinkedTreeMap.replaceInParent} — a long-standing Gson/Extent
 * concurrency bug. So we drive ExtentReports directly from Cucumber's @Before
 * and @After hooks instead of using the cucumber adapter plugin.
 *
 * Each Cucumber scenario gets its own ExtentTest, stored in a ThreadLocal so
 * parallel runner threads never share state.
 */
public final class ExtentManager {

    private static final Logger log = LogManager.getLogger(ExtentManager.class);
    private static final String REPORT_PATH = "reports/extent/SparkReport.html";
    private static final ExtentReports extent = new ExtentReports();
    private static final ThreadLocal<ExtentTest> tlTest = new ThreadLocal<>();

    static {
        File dir = new File("reports/extent");
        if (!dir.exists() && !dir.mkdirs()) {
            log.warn("Could not create {}", dir.getAbsolutePath());
        }
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
        log.info("Extent Spark report will be written to {}", new File(REPORT_PATH).getAbsolutePath());
    }

    private ExtentManager() {}

    /** Create an ExtentTest for the current scenario. Called from Hooks.@Before. */
    public static synchronized ExtentTest startScenario(String name, String description) {
        ExtentTest test = extent.createTest(name, description);
        tlTest.set(test);
        return test;
    }

    /** The ExtentTest bound to the current thread (null if none). */
    public static ExtentTest currentScenario() {
        return tlTest.get();
    }

    /** Log a single step into the current scenario's report. */
    public static void logStep(Status status, String details) {
        ExtentTest t = tlTest.get();
        if (t != null) t.log(status, details);
    }

    /** Attach a base64 screenshot to the current scenario as FAIL evidence. */
    public static void attachFailureScreenshot(byte[] png, String label) {
        ExtentTest t = tlTest.get();
        if (t == null || png == null || png.length == 0) return;
        String b64 = Base64.getEncoder().encodeToString(png);
        try {
            t.fail(label,
                    MediaEntityBuilder.createScreenCaptureFromBase64String(b64).build());
        } catch (Exception e) {
            log.warn("Could not attach failure screenshot to Extent: {}", e.getMessage());
        }
    }

    /** Attach a base64 screenshot to the current scenario as PASS evidence. */
    public static void attachPassScreenshot(byte[] png, String label) {
        ExtentTest t = tlTest.get();
        if (t == null || png == null || png.length == 0) return;
        String b64 = Base64.getEncoder().encodeToString(png);
        try {
            t.pass(label,
                    MediaEntityBuilder.createScreenCaptureFromBase64String(b64).build());
        } catch (Exception e) {
            log.warn("Could not attach pass screenshot to Extent: {}", e.getMessage());
        }
    }

    /** Detach the current ThreadLocal binding so this thread can pick up the next scenario. */
    public static void endScenario() {
        tlTest.remove();
    }

    /** Write the accumulated report to disk. Call once at suite end. */
    public static synchronized void flush() {
        extent.flush();
        log.info("Extent report flushed to {}", new File(REPORT_PATH).getAbsolutePath());
    }
}
