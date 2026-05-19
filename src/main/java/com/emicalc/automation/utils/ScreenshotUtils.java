package com.emicalc.automation.utils;

import com.emicalc.automation.driver.DriverFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chromium.ChromiumDriver;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Screenshot helper.
 *
 * Captures a FULL-PAGE screenshot (the entire scrollable document, not just
 * the visible viewport). Strategy, in order:
 *
 *   1. Chromium-based browsers (Chrome, Edge) — Chrome DevTools Protocol
 *      command {@code Page.captureScreenshot} with {@code captureBeyondViewport=true}.
 *      Native, single round-trip, no scroll-and-stitch artefacts.
 *
 *   2. Resize-then-capture fallback — set the window height to the document
 *      scrollHeight, take a normal screenshot, restore the original size.
 *      Works on any WebDriver but causes a brief visible resize.
 *
 *   3. Plain viewport screenshot — last resort.
 *
 * All paths return PNG bytes. Failures are logged but never thrown, so a
 * screenshot error never masks the original test failure.
 */
public final class ScreenshotUtils {

    private static final Logger log = LogManager.getLogger(ScreenshotUtils.class);
    private static final DateTimeFormatter TS =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    private ScreenshotUtils() {}

    /**
     * Capture a full-page PNG to {@code reports/extent/screenshots/<name>_<ts>.png}
     * and return the absolute file path. Returns null on failure.
     */
    public static String capture(String name) {
        try {
            byte[] png = captureAsBytes();
            if (png.length == 0) {
                log.warn("Screenshot bytes empty for '{}'", name);
                return null;
            }
            String safeName = name.replaceAll("[^a-zA-Z0-9_-]", "_");
            String fileName = safeName + "_" + LocalDateTime.now().format(TS) + ".png";
            Path target = Path.of("reports", "extent", "screenshots", fileName);
            Files.createDirectories(target.getParent());
            Files.write(target, png);
            log.info("Screenshot saved: {} ({} KB)",
                    target.toAbsolutePath(), png.length / 1024);
            return target.toAbsolutePath().toString();
        } catch (Exception e) {
            log.warn("Failed to capture screenshot '{}': {}", name, e.getMessage());
            return null;
        }
    }

    /**
     * Capture a FULL-PAGE screenshot as bytes. Tries CDP first (Chrome / Edge),
     * then resize-then-capture, then viewport-only as a last resort.
     */
    public static byte[] captureAsBytes() {
        WebDriver driver;
        try {
            driver = DriverFactory.getDriver();
        } catch (Exception e) {
            log.warn("No driver available for screenshot: {}", e.getMessage());
            return new byte[0];
        }

        // ---------- 1) CDP full-page (Chrome / Edge) ----------
        if (driver instanceof ChromiumDriver chromium) {
            byte[] png = captureWithCDP(chromium);
            if (png != null && png.length > 0) return png;
        }

        // ---------- 2) Resize fallback ----------
        byte[] png = captureViaResize(driver);
        if (png != null && png.length > 0) return png;

        // ---------- 3) Viewport-only as last resort ----------
        try {
            return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        } catch (Exception e) {
            log.warn("Viewport screenshot failed: {}", e.getMessage());
            return new byte[0];
        }
    }

    // ---------- private capture strategies ----------

    /**
     * Chrome DevTools Protocol command — captures the entire scrollable
     * document in a single shot. Works for both Chrome and Edge because they
     * both extend {@link ChromiumDriver}.
     */
    private static byte[] captureWithCDP(ChromiumDriver driver) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("captureBeyondViewport", true);
            params.put("format", "png");
            params.put("fromSurface", true);
            // Optional but helpful: capture at the device's actual pixel ratio
            // params.put("optimizeForSpeed", false);

            Map<String, Object> result =
                    driver.executeCdpCommand("Page.captureScreenshot", params);
            Object data = result.get("data");
            if (data instanceof String b64 && !b64.isEmpty()) {
                byte[] png = Base64.getDecoder().decode(b64);
                log.debug("CDP full-page screenshot captured: {} bytes", png.length);
                return png;
            }
            log.warn("CDP captureScreenshot returned no data");
        } catch (Exception e) {
            log.warn("CDP full-page capture failed, will try resize fallback: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Resize the window height to the document's scrollHeight, capture, then
     * restore the original size. Cross-browser fallback.
     */
    private static byte[] captureViaResize(WebDriver driver) {
        Dimension original = null;
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            original = driver.manage().window().getSize();
            Long height = (Long) js.executeScript(
                    "return Math.max(" +
                    "  document.body.scrollHeight, document.documentElement.scrollHeight," +
                    "  document.body.offsetHeight, document.documentElement.offsetHeight," +
                    "  document.body.clientHeight, document.documentElement.clientHeight" +
                    ");");
            if (height != null && height > 0) {
                int target = Math.min(height.intValue() + 100, 16384); // cap at 16K
                driver.manage().window().setSize(new Dimension(original.getWidth(), target));
                // Brief wait for layout
                try { Thread.sleep(250); } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }
            return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        } catch (Exception e) {
            log.warn("Resize-then-capture failed: {}", e.getMessage());
            return null;
        } finally {
            if (original != null) {
                try {
                    driver.manage().window().setSize(original);
                } catch (Exception ignored) {}
            }
        }
    }
}
