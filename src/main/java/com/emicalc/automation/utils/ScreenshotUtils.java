package com.emicalc.automation.utils;

import com.emicalc.automation.driver.DriverFactory;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class ScreenshotUtils {

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    private ScreenshotUtils() {}

    public static String capture(String name) {
        try {
            File src = ((TakesScreenshot) DriverFactory.getDriver()).getScreenshotAs(OutputType.FILE);
            String safe = name.replaceAll("[^a-zA-Z0-9_-]", "_");
            Path target = Path.of("reports", "extent", "screenshots",
                    safe + "_" + LocalDateTime.now().format(TS) + ".png");
            Files.createDirectories(target.getParent());
            Files.copy(src.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
            return target.toAbsolutePath().toString();
        } catch (Exception e) {
            return null;
        }
    }

    public static byte[] captureAsBytes() {
        try {
            return ((TakesScreenshot) DriverFactory.getDriver()).getScreenshotAs(OutputType.BYTES);
        } catch (Exception e) {
            return new byte[0];
        }
    }
}
