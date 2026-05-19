package com.emicalc.automation.driver;

import com.emicalc.automation.config.ConfigReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;

import java.time.Duration;

/**
 * Thread-safe driver provider. Each TestNG test thread (Chrome / Edge) keeps
 * its own WebDriver instance via {@link ThreadLocal} so parallel execution
 * doesn't cross wires.
 *
 * Selenium 4 has built-in "Selenium Manager" which auto-downloads the right
 * driver binary for the installed browser, so no WebDriverManager dependency
 * is needed.
 */
public final class DriverFactory {

    private static final Logger log = LogManager.getLogger(DriverFactory.class);
    private static final ThreadLocal<WebDriver> TL_DRIVER = new ThreadLocal<>();

    private DriverFactory() {}

    public static WebDriver getDriver() {
        WebDriver d = TL_DRIVER.get();
        if (d == null) {
            throw new IllegalStateException(
                    "WebDriver not initialised on thread " + Thread.currentThread().getName()
                    + " — call DriverFactory.initDriver(browser) first.");
        }
        return d;
    }

    public static void initDriver(String browser) {
        if (TL_DRIVER.get() != null) {
            log.warn("Driver already initialised on {}, reusing", Thread.currentThread().getName());
            return;
        }
        boolean headless = ConfigReader.get().getBoolean("headless");
        WebDriver driver = switch (browser.toLowerCase()) {
            case "chrome" -> buildChrome(headless);
            case "edge"   -> buildEdge(headless);
            // ----- Firefox intentionally commented out per hackathon requirement (point p) -----
            // case "firefox" -> buildFirefox(headless);
            default -> throw new IllegalArgumentException("Unsupported browser: " + browser);
        };

        driver.manage().window().maximize();
        driver.manage().timeouts()
                .implicitlyWait(Duration.ofSeconds(ConfigReader.get().getInt("implicit.wait.seconds")))
                .pageLoadTimeout(Duration.ofSeconds(ConfigReader.get().getInt("page.load.timeout.seconds")));
        TL_DRIVER.set(driver);
        log.info("Initialised {} driver on thread {}", browser, Thread.currentThread().getName());
    }

    public static void quitDriver() {
        WebDriver d = TL_DRIVER.get();
        if (d != null) {
            try {
                d.quit();
            } catch (Exception e) {
                log.warn("Error while quitting driver: {}", e.getMessage());
            } finally {
                TL_DRIVER.remove();
                log.info("Driver quit on thread {}", Thread.currentThread().getName());
            }
        }
    }

    // ---------- private builders ----------

    private static WebDriver buildChrome(boolean headless) {
        ChromeOptions opts = new ChromeOptions();
        opts.addArguments("--remote-allow-origins=*");
        opts.addArguments("--disable-notifications");
        opts.addArguments("--disable-popup-blocking");
        opts.addArguments("--disable-blink-features=AutomationControlled");
        if (headless) {
            opts.addArguments("--headless=new", "--window-size=1920,1080");
        }
        return new ChromeDriver(opts);
    }

    private static WebDriver buildEdge(boolean headless) {
        EdgeOptions opts = new EdgeOptions();
        opts.addArguments("--remote-allow-origins=*");
        opts.addArguments("--disable-notifications");
        opts.addArguments("--disable-popup-blocking");
        if (headless) {
            opts.addArguments("--headless=new", "--window-size=1920,1080");
        }
        return new EdgeDriver(opts);
    }

    // ----- Firefox builder kept here in commented form for reference -----
    /*
    private static WebDriver buildFirefox(boolean headless) {
        FirefoxOptions opts = new FirefoxOptions();
        if (headless) opts.addArguments("-headless");
        return new FirefoxDriver(opts);
    }
    */
}
