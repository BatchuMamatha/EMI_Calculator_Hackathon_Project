package com.emicalc.automation.base;

import com.emicalc.automation.config.ConfigReader;
import io.cucumber.testng.AbstractTestNGCucumberTests;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import java.time.Duration;

// BaseClass declares the class-level WebDriver and owns the @BeforeClass /
// @AfterClass lifecycle. Concrete TestRunner classes extend this. The
// ThreadLocal is used ONLY to support parallel Chrome + Edge + Firefox runs
// (testng.xml parallel="tests"); each runner thread keeps its own driver.
public abstract class BaseClass extends AbstractTestNGCucumberTests {

    protected static final ThreadLocal<WebDriver> driver = new ThreadLocal<>();

    public static WebDriver getDriver() {
        WebDriver d = driver.get();
        if (d == null) throw new IllegalStateException("Driver not initialised on " + Thread.currentThread().getName());
        return d;
    }

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

    @AfterClass(alwaysRun = true)
    public void closeBrowser() {
        WebDriver d = driver.get();
        if (d == null) return;
        try { d.quit(); } catch (Exception ignored) {}
        driver.remove();
    }

    private static WebDriver buildChrome(boolean headless) {
        ChromeOptions o = new ChromeOptions();
        o.addArguments("--remote-allow-origins=*", "--disable-notifications", "--disable-popup-blocking",
                "--disable-blink-features=AutomationControlled");
        if (headless) o.addArguments("--headless=new", "--window-size=1920,1080");
        return new ChromeDriver(o);
    }

    private static WebDriver buildEdge(boolean headless) {
        EdgeOptions o = new EdgeOptions();
        o.addArguments("--remote-allow-origins=*", "--disable-notifications", "--disable-popup-blocking");
        if (headless) o.addArguments("--headless=new", "--window-size=1920,1080");
        return new EdgeDriver(o);
    }

    private static WebDriver buildFirefox(boolean headless) {
        FirefoxOptions o = new FirefoxOptions();
        if (headless) o.addArguments("-headless");
        return new FirefoxDriver(o);
    }
}
