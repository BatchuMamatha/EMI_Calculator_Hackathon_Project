package com.emicalc.automation.pages;

import com.emicalc.automation.base.BaseClass;
import com.emicalc.automation.config.ConfigReader;
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

public abstract class BasePage {

    protected final WebDriver driver;
    protected final WebDriverWait wait;

    protected BasePage() {
        this.driver = BaseClass.getDriver();
        this.wait = new WebDriverWait(driver,
                Duration.ofSeconds(ConfigReader.get().getInt("explicit.wait.seconds")));
        PageFactory.initElements(driver, this);
    }

    protected void open(String url) {
        driver.get(url);
        waitForReady();
    }

    protected void waitForReady() {
        wait.until(d -> "complete".equals(((JavascriptExecutor) d).executeScript("return document.readyState")));
    }

    protected WebElement waitVisible(WebElement el)   { return wait.until(ExpectedConditions.visibilityOf(el)); }
    protected WebElement waitClickable(WebElement el) { return wait.until(ExpectedConditions.elementToBeClickable(el)); }

    protected void click(WebElement el) {
        WebElement e = waitClickable(el);
        scrollIntoView(e);
        try { e.click(); }
        catch (Exception ex) { ((JavascriptExecutor) driver).executeScript("arguments[0].click();", e); }
    }

    protected void typeReplacing(WebElement el, String value) {
        WebElement e = waitVisible(el);
        scrollIntoView(e);
        e.click();
        e.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        e.sendKeys(Keys.DELETE);
        e.sendKeys(value);
        e.sendKeys(Keys.TAB);
    }

    protected String text(WebElement el) { return waitVisible(el).getText().trim(); }

    protected boolean isVisible(WebElement el) {
        try { return el.isDisplayed(); }
        catch (NoSuchElementException | StaleElementReferenceException e) { return false; }
    }

    protected boolean isEnabledSafe(WebElement el) {
        try { return el.isEnabled(); }
        catch (NoSuchElementException | StaleElementReferenceException e) { return false; }
    }

    protected void scrollIntoView(WebElement el) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", el);
    }

    protected void scrollBy(int x, int y) {
        ((JavascriptExecutor) driver).executeScript("window.scrollBy(arguments[0], arguments[1]);", x, y);
    }

    protected Object js(String script, Object... args) {
        return ((JavascriptExecutor) driver).executeScript(script, args);
    }

    protected void waitForJsTruthy(String expression) {
        wait.until(d -> Boolean.TRUE.equals(
                ((JavascriptExecutor) d).executeScript("return Boolean(" + expression + ");")));
    }

    protected Actions actions() { return new Actions(driver); }

    protected WebElement findInListByText(List<WebElement> elements, String value) {
        return elements.stream()
                .filter(e -> { try { return e.getText().trim().equals(value); }
                               catch (StaleElementReferenceException ex) { return false; } })
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No element with text '" + value + "'"));
    }
}
