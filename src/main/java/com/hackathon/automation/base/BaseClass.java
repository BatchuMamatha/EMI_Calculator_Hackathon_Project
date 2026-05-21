package com.hackathon.automation.base;

import com.hackathon.automation.config.ConfigReader;
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

// BaseClass owns the class-level WebDriver and the @BeforeClass/@AfterClass
// lifecycle. ThreadLocal is used only because the suite runs Chrome and
// Edge in parallel and each runner thread needs its own driver.
public abstract class BaseClass extends AbstractTestNGCucumberTests {

    protected static final ThreadLocal<WebDriver> driver = new ThreadLocal<>();

    // Returns the driver bound to the current thread; throws if not initialised.
    public static WebDriver getDriver() {
        WebDriver d = driver.get();
        if (d == null) throw new IllegalStateException("Driver not initialised on " + Thread.currentThread().getName());
        return d;
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
