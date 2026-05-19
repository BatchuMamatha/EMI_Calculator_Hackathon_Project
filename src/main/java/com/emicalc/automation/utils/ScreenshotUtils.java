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

/**
 * Screenshot helper — captures the current browser window (the visible
 * viewport) using Selenium's standard {@link TakesScreenshot} interface.
 *
 * Files are saved to {@code reports/extent/screenshots/<name>_<ts>.png}.
 * Failures are logged but never thrown, so a screenshot error never masks
 * the original test failure.
 */
public final class ScreenshotUtils {

    private static final Logger log = LogManager.getLogger(ScreenshotUtils.class);
    private static final DateTimeFormatter TS =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    private ScreenshotUtils() {}

    /**
     * Capture a window PNG to disk and return the absolute file path.
     * Returns null on failure.
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

    /** PNG bytes of the current browser window — used for attachments to reports. */
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
