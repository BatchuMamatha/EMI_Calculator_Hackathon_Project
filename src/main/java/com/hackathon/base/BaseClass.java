package com.hackathon.base;

import com.hackathon.config.ConfigReader;
import io.cucumber.testng.AbstractTestNGCucumberTests;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import java.time.Duration;

// BaseClass owns the WebDriver lifecycle and the @BeforeClass/@AfterClass hooks.
// ThreadLocal isolates each browser thread's driver when Chrome and Edge run in parallel.
public abstract class BaseClass extends AbstractTestNGCucumberTests {

    protected static final ThreadLocal<WebDriver> driver      = new ThreadLocal<>();
    private   static final ThreadLocal<String>    browserName = new ThreadLocal<>();

    // Returns the driver bound to the current thread; throws if not initialised.
    public static WebDriver getDriver() {
        WebDriver d = driver.get();
        if (d == null) throw new IllegalStateException("Driver not initialised on " + Thread.currentThread().getName());
        return d;
    }

    // Returns the browser name (chrome / edge / firefox) for the current thread.
    public static String getBrowserName() {
        String b = browserName.get();
        return b != null ? b : "chrome";
    }

    // Overrides AbstractTestNGCucumberTests.setUpClass() so that cucumber.report.*
    // system properties are set BEFORE the TestNGCucumberRunner reads @CucumberOptions
    // (the parent's setUpClass does that). Without this override the runner would
    // create literal files named "#{systemProperty.cucumber.report.html}".
    // Overrides AbstractTestNGCucumberTests.setUpClass() so that cucumber.report.*
    // system properties are set BEFORE the TestNGCucumberRunner reads @CucumberOptions.
    // Parameters are read directly from the TestNG XML test context (same source as
    // @Parameters, but without changing the method signature which would break @Override).
    @Override
    @BeforeClass(alwaysRun = true)
    public void setUpClass(ITestContext context) {
        String browser    = param(context, "browser",               "chrome");
        String reportHtml = param(context, "cucumber.report.html",  "reports/cucumber/cucumber.html");
        String reportJson = param(context, "cucumber.report.json",  "reports/cucumber/cucumber.json");
        browserName.set(browser.toLowerCase());
        System.setProperty("cucumber.report.html", reportHtml);
        System.setProperty("cucumber.report.json", reportJson);
        try { super.setUpClass(context); } catch (Exception e) { throw new RuntimeException(e); }
    }

    // Reads a testng.xml <parameter> value, falling back to defaultValue if absent.
    private static String param(ITestContext ctx, String name, String defaultValue) {
        String v = ctx.getCurrentXmlTest().getParameter(name);
        return v != null ? v : defaultValue;
    }

    // Launches the browser passed via TestNG @Parameter once per runner class.
    @BeforeClass(alwaysRun = true)
    @Parameters("browser")
    public void launchBrowser(@Optional("chrome") String browser) {
        if (driver.get() != null) return;
        boolean headless = ConfigReader.get().getBoolean("headless");
        WebDriver d = switch (browser.toLowerCase()) {
            case "chrome"  -> buildChrome(headless);
            case "edge"    -> buildEdge(headless);
            case "firefox" -> buildFirefox(headless);
            default -> throw new IllegalArgumentException("Unsupported browser: " + browser);
        };
        d.manage().window().maximize();
        d.manage().timeouts()
                .implicitlyWait(Duration.ofSeconds(ConfigReader.get().getInt("implicit.wait.seconds")))
                .pageLoadTimeout(Duration.ofSeconds(ConfigReader.get().getInt("explicit.page.load.timeout.seconds")));
        driver.set(d);
    }

    // Quits the browser at the end of the runner class.
    @AfterClass(alwaysRun = true)
    public void closeBrowser() {
        WebDriver d = driver.get();
        if (d == null) return;
        try { d.quit(); } catch (Exception ignored) {}
        driver.remove();
        browserName.remove();
    }

    // Builds a configured ChromeDriver (headless if requested).
    private static WebDriver buildChrome(boolean headless) {
        ChromeOptions o = new ChromeOptions();
        o.addArguments("--remote-allow-origins=*", "--disable-notifications", "--disable-popup-blocking",
                "--disable-blink-features=AutomationControlled");
        if (headless) o.addArguments("--headless=new", "--window-size=1920,1080");
        return new ChromeDriver(o);
    }

    // Builds a configured EdgeDriver (headless if requested).
    private static WebDriver buildEdge(boolean headless) {
        EdgeOptions o = new EdgeOptions();
        o.addArguments("--remote-allow-origins=*", "--disable-notifications", "--disable-popup-blocking");
        if (headless) o.addArguments("--headless=new", "--window-size=1920,1080");
        return new EdgeDriver(o);
    }

    // Builds a configured FirefoxDriver (headless if requested).
    private static WebDriver buildFirefox(boolean headless) {
        FirefoxOptions o = new FirefoxOptions();
        if (headless) o.addArguments("-headless");
        return new FirefoxDriver(o);
    }
}
