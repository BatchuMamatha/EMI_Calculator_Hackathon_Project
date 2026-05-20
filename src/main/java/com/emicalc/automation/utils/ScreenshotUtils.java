package com.emicalc.automation.utils;

import com.emicalc.automation.base.BaseClass;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class ScreenshotUtils {

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    private ScreenshotUtils() {}

    public static String capture(String name) {
        try {
            String fileName = name + "_" + LocalDateTime.now().format(TS) + ".png";
            Path target = Paths.get(System.getProperty("user.dir"), "screenshots", fileName);
            Files.createDirectories(target.getParent());
            File source = ((TakesScreenshot) BaseClass.getDriver()).getScreenshotAs(OutputType.FILE);
            Files.copy(source.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Screenshot saved -> " + target.toAbsolutePath());
            return target.toAbsolutePath().toString();
        } catch (Exception e) {
            System.out.println("Screenshot error -> " + e.getMessage());
            return null;
        }
    }
}
