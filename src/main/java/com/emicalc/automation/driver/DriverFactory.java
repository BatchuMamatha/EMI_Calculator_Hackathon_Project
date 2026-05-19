package com.emicalc.automation.driver;

import com.emicalc.automation.config.ConfigReader;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;

import java.time.Duration;

public final class DriverFactory {

    private static final ThreadLocal<WebDriver> TL = new ThreadLocal<>();

    private DriverFactory() {}

    public static WebDriver getDriver() {
        WebDriver d = TL.get();
        if (d == null) throw new IllegalStateException("Driver not initialised on " + Thread.currentThread().getName());
        return d;
    }

    public static void initDriver(String browser) {
        if (TL.get() != null) return;
        boolean headless = ConfigReader.get().getBoolean("headless");
        WebDriver d = switch (browser.toLowerCase()) {
            case "chrome" -> buildChrome(headless);
            case "edge"   -> buildEdge(headless);
            // Firefox commented per hackathon requirement (point p)
            default -> throw new IllegalArgumentException("Unsupported browser: " + browser);
        };
        d.manage().window().maximize();
        d.manage().timeouts()
                .implicitlyWait(Duration.ofSeconds(ConfigReader.get().getInt("implicit.wait.seconds")))
                .pageLoadTimeout(Duration.ofSeconds(ConfigReader.get().getInt("page.load.timeout.seconds")));
        TL.set(d);
    }

    public static void quitDriver() {
        WebDriver d = TL.get();
        if (d == null) return;
        try { d.quit(); } catch (Exception ignored) {}
        TL.remove();
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
