package com.emicalc.automation.utils;

import com.emicalc.automation.config.ConfigReader;
import com.emicalc.automation.driver.DriverFactory;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Thin wrapper on top of WebDriverWait. Keeps step-defs/pages free of
 * boilerplate Duration.ofSeconds() calls and lets us tune timeouts globally
 * from config.properties.
 *
 * All overloads take {@link WebElement} (typically the lazy proxy created by
 * PageFactory's @FindBy) — no By in user-facing signatures.
 */
public final class WaitUtils {

    private WaitUtils() {}

    private static WebDriverWait getWait() {
        int s = ConfigReader.get().getInt("explicit.wait.seconds");
        return new WebDriverWait(DriverFactory.getDriver(), Duration.ofSeconds(s));
    }

    public static WebElement visible(WebElement element) {
        return getWait().until(ExpectedConditions.visibilityOf(element));
    }

    public static WebElement clickable(WebElement element) {
        return getWait().until(ExpectedConditions.elementToBeClickable(element));
    }

    public static boolean textEquals(WebElement element, String expected) {
        return getWait().until(ExpectedConditions.textToBePresentInElement(element, expected));
    }

    public static boolean attributeContains(WebElement element, String attr, String value) {
        return getWait().until(ExpectedConditions.attributeContains(element, attr, value));
    }

    public static <V> V until(ExpectedCondition<V> condition) {
        return getWait().until(condition);
    }
}
