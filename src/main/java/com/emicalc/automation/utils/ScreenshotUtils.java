package com.emicalc.automation.utils;

import com.emicalc.automation.driver.DriverFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class ScreenshotUtils {

    private static final Logger log = LogManager.getLogger(ScreenshotUtils.class);
    private static final DateTimeFormatter TS =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    private ScreenshotUtils() {}

    /**
     * Capture a PNG to reports/extent/screenshots/<name>_<timestamp>.png and
     * return the absolute file path. Returns null on failure (never throws —
     * we do not want a screenshot failure to mask the original test failure).
     */
    public static String capture(String name) {
        try {
            WebDriver driver = DriverFactory.getDriver();
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            String safeName = name.replaceAll("[^a-zA-Z0-9_-]", "_");
            String fileName = safeName + "_" + LocalDateTime.now().format(TS) + ".png";
            Path target = Path.of("reports", "extent", "screenshots", fileName);
            Files.createDirectories(target.getParent());
            Files.copy(src.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
            log.info("Screenshot saved: {}", target.toAbsolutePath());
            return target.toAbsolutePath().toString();
        } catch (Exception e) {
            log.warn("Failed to capture screenshot '{}': {}", name, e.getMessage());
            return null;
        }
    }

    /** byte[] form for attaching to Allure / Cucumber scenarios. */
    public static byte[] captureAsBytes() {
        try {
            WebDriver driver = DriverFactory.getDriver();
            return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        } catch (Exception e) {
            log.warn("Failed to capture screenshot bytes: {}", e.getMessage());
            return new byte[0];
        }
    }
}
