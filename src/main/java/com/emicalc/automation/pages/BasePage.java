package com.emicalc.automation.pages;

import com.emicalc.automation.config.ConfigReader;
import com.emicalc.automation.driver.DriverFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Base Page Object using Selenium's PageFactory pattern. Concrete pages
 * declare their locators with {@code @FindBy} annotations on {@code WebElement}
 * fields — the framework never holds {@code By} instances directly, satisfying
 * the "remove By class from the code files" requirement.
 *
 * Helper primitives here all operate on {@link WebElement}, not {@code By}.
 * For genuinely dynamic locators (e.g. "the row whose id is year2026"), pages
 * either:
 *   - declare a {@code List<WebElement>} with a class/CSS @FindBy and filter
 *     in Java, or
 *   - use the protected JavaScript helpers below.
 */
public abstract class BasePage {

    protected static final Logger log = LogManager.getLogger(BasePage.class);
    protected final WebDriver driver;
    protected final WebDriverWait wait;

    protected BasePage() {
        this.driver = DriverFactory.getDriver();
        this.wait = new WebDriverWait(driver,
                Duration.ofSeconds(ConfigReader.get().getInt("explicit.wait.seconds")));
        // Initialise @FindBy proxies on the concrete subclass instance
        PageFactory.initElements(driver, this);
    }

    // ---------- navigation / page state ----------

    protected void open(String url) {
        log.info("GET {}", url);
        driver.get(url);
        waitForReady();
    }

    protected void waitForReady() {
        wait.until(d -> "complete".equals(
                ((JavascriptExecutor) d).executeScript("return document.readyState")));
    }

    // ---------- element interactions (all WebElement, no By) ----------

    protected WebElement waitVisible(WebElement element) {
        return wait.until(ExpectedConditions.visibilityOf(element));
    }

    protected WebElement waitClickable(WebElement element) {
        return wait.until(ExpectedConditions.elementToBeClickable(element));
    }

    protected void click(WebElement element) {
        WebElement el = waitClickable(element);
        scrollIntoView(el);
        try {
            el.click();
        } catch (Exception ex) {
            log.warn("Native click failed — falling back to JS click: {}", ex.getMessage());
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
        }
    }

    protected void typeReplacing(WebElement element, String value) {
        WebElement el = waitVisible(element);
        scrollIntoView(el);
        el.click();
        // Select-all then type. The site formats inputs (commas) on blur so
        // this is the most reliable approach across browsers.
        el.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        el.sendKeys(Keys.DELETE);
        el.sendKeys(value);
        el.sendKeys(Keys.TAB);
    }

    protected String text(WebElement element) {
        return waitVisible(element).getText().trim();
    }

    protected boolean isVisible(WebElement element) {
        try {
            return element.isDisplayed();
        } catch (NoSuchElementException | StaleElementReferenceException e) {
            return false;
        }
    }

    protected boolean isEnabledSafe(WebElement element) {
        try {
            return element.isEnabled();
        } catch (NoSuchElementException | StaleElementReferenceException e) {
            return false;
        }
    }

    // ---------- JS helpers (used in place of dynamic By.id/By.css) ----------

    protected void scrollIntoView(WebElement el) {
        ((JavascriptExecutor) driver)
                .executeScript("arguments[0].scrollIntoView({block:'center', inline:'center'});", el);
    }

    protected void scrollBy(int x, int y) {
        ((JavascriptExecutor) driver).executeScript("window.scrollBy(arguments[0], arguments[1]);", x, y);
    }

    protected void scrollToBottom() {
        ((JavascriptExecutor) driver)
                .executeScript("window.scrollTo(0, document.body.scrollHeight);");
    }

    protected Object js(String script, Object... args) {
        return ((JavascriptExecutor) driver).executeScript(script, args);
    }

    /** Wait until a JS expression evaluates truthy. Used for dynamic state. */
    protected void waitForJsTruthy(String script) {
        wait.until(d -> Boolean.TRUE.equals(
                ((JavascriptExecutor) d).executeScript("return Boolean(" + script + ");")));
    }

    // ---------- mouse / list helpers ----------

    protected Actions actions() {
        return new Actions(driver);
    }

    /** Return the first element from a {@code @FindBy} list whose visible text equals {@code value}. */
    protected WebElement findInListByText(List<WebElement> elements, String value) {
        return elements.stream()
                .filter(e -> {
                    try { return e.getText().trim().equals(value); }
                    catch (StaleElementReferenceException ex) { return false; }
                })
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No element with text '" + value + "'"));
    }
}
