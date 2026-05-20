package com.emicalc.automation.driver;

import com.emicalc.automation.config.ConfigReader;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;

import java.time.Duration;

public final class DriverFactory {

    private static WebDriver driver;

    private DriverFactory() {}

    public static WebDriver getDriver() {
        if (driver == null) throw new IllegalStateException("Driver not initialised");
        return driver;
    }

    public static void initDriver(String browser) {
        if (driver != null) return;
        boolean headless = ConfigReader.get().getBoolean("headless");
        driver = switch (browser.toLowerCase()) {
            case "chrome" -> buildChrome(headless);
            case "edge"   -> buildEdge(headless);
            // Firefox commented per hackathon requirement (point p)
            default -> throw new IllegalArgumentException("Unsupported browser: " + browser);
        };
        driver.manage().window().maximize();
        driver.manage().timeouts()
                .implicitlyWait(Duration.ofSeconds(ConfigReader.get().getInt("implicit.wait.seconds")))
                .pageLoadTimeout(Duration.ofSeconds(ConfigReader.get().getInt("page.load.timeout.seconds")));
    }

    public static void quitDriver() {
        if (driver == null) return;
        try { driver.quit(); } catch (Exception ignored) {}
        driver = null;
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

    // Firefox builder kept commented for reference:
    // private static WebDriver buildFirefox(boolean headless) {
    //     FirefoxOptions o = new FirefoxOptions();
    //     if (headless) o.addArguments("-headless");
    //     return new FirefoxDriver(o);
    // }
}
