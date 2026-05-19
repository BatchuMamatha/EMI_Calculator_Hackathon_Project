package com.emicalc.automation.pages;

import com.emicalc.automation.config.ConfigReader;
import com.emicalc.automation.utils.EMICalculatorUtil;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

/**
 * Page Object for the emicalculator.net homepage. Uses Selenium PageFactory
 * with {@code @FindBy} annotations — no {@code By} declarations.
 *
 * Mix of locator strategies (id, name, css, xpath) is intentional to satisfy
 * the "different locator techniques" hackathon requirement.
 */
public class HomePage extends BasePage {

    // ---------- TABS (id) ----------
    @FindBy(id = "home-loan")     private WebElement homeLoanTab;
    @FindBy(id = "personal-loan") private WebElement personalLoanTab;
    @FindBy(id = "car-loan")      private WebElement carLoanTab;

    // ---------- INPUTS (name) ----------
    @FindBy(name = "loanamount")   private WebElement loanAmount;
    @FindBy(name = "loaninterest") private WebElement loanInterest;
    @FindBy(name = "loanterm")     private WebElement loanTerm;

    // ---------- TENURE TOGGLE (label parent, not the hidden radio input) ----------
    // The <input type="radio"> is overlaid by a Bootstrap <label class="btn">,
    // so clicking the input is intercepted. Click the label instead.
    @FindBy(xpath = "//label[input[@id='loanyears']]")  private WebElement tenureYears;
    @FindBy(xpath = "//label[input[@id='loanmonths']]") private WebElement tenureMonths;

    // ---------- SLIDERS (css) ----------
    @FindBy(css = "#loanamountslider .ui-slider-handle")   private WebElement loanAmountSlider;
    @FindBy(css = "#loaninterestslider .ui-slider-handle") private WebElement loanInterestSlider;
    @FindBy(css = "#loantermslider .ui-slider-handle")     private WebElement loanTermSlider;

    // ---------- RESULT TILES (xpath) ----------
    @FindBy(xpath = "//div[@id='emiamount']//p/span")        private WebElement emiAmount;
    @FindBy(xpath = "//div[@id='emitotalinterest']//p/span") private WebElement totalInterest;
    @FindBy(xpath = "//div[@id='emitotalamount']//p/span")   private WebElement totalPayment;

    // ---------- YEAR ROWS (css list — filtered by text in Java) ----------
    @FindBy(css = "tr.yearlypaymentdetails td.paymentyear") private List<WebElement> yearCells;

    // ---------- ACTIONS ----------

    public HomePage open() {
        super.open(ConfigReader.get().get("base.url"));
        waitForReady();
        return this;
    }

    public HomePage selectCarLoanTab() {
        click(carLoanTab);
        wait.until(ExpectedConditions.attributeContains(carLoanTab, "class", "active"));
        log.info("Car Loan tab selected");
        return this;
    }

    public HomePage selectHomeLoanTab() {
        click(homeLoanTab);
        wait.until(ExpectedConditions.attributeContains(homeLoanTab, "class", "active"));
        return this;
    }

    public HomePage selectPersonalLoanTab() {
        click(personalLoanTab);
        wait.until(ExpectedConditions.attributeContains(personalLoanTab, "class", "active"));
        return this;
    }

    public HomePage enterLoanAmount(String amount) {
        typeReplacing(loanAmount, amount);
        return this;
    }

    public HomePage enterInterestRate(String rate) {
        typeReplacing(loanInterest, rate);
        return this;
    }

    public HomePage enterTenure(String tenure, TenureUnit unit) {
        // Pick unit first — the on-page handler re-converts the value otherwise.
        if (unit == TenureUnit.YEARS) click(tenureYears);
        else click(tenureMonths);
        typeReplacing(loanTerm, tenure);
        return this;
    }

    // ---------- READS ----------

    public long readEmi() {
        // Result tile updates async after blur; waitVisible covers the settle window.
        return EMICalculatorUtil.parseIndianCurrency(text(emiAmount));
    }

    public long readTotalInterest() {
        return EMICalculatorUtil.parseIndianCurrency(text(totalInterest));
    }

    public long readTotalPayment() {
        return EMICalculatorUtil.parseIndianCurrency(text(totalPayment));
    }

    /** Read first month's principal for the given year row. */
    public long readFirstMonthPrincipal(int year) {
        expandYearRow(year);
        String value = (String) js(
                "var el = document.querySelector('#monthyear' + arguments[0] + " +
                "        ' .monthlypaymentcontainer tbody tr:first-child td:nth-child(2)');" +
                " return el ? el.innerText : '';", year);
        return EMICalculatorUtil.parseIndianCurrency(value);
    }

    /** Read first month's interest for the given year row. */
    public long readFirstMonthInterest(int year) {
        expandYearRow(year);
        String value = (String) js(
                "var el = document.querySelector('#monthyear' + arguments[0] + " +
                "        ' .monthlypaymentcontainer tbody tr:first-child td:nth-child(3)');" +
                " return el ? el.innerText : '';", year);
        return EMICalculatorUtil.parseIndianCurrency(value);
    }

    /**
     * Expand the year row so the inner monthly table becomes visible. Uses
     * the yearCells @FindBy list (filtered by text) and JavaScript to inspect
     * the monthly container's CSS display value — no {@code By} required.
     */
    private void expandYearRow(int year) {
        scrollBy(0, 800);
        WebElement row = findInListByText(yearCells, String.valueOf(year));
        scrollIntoView(row);
        String currentDisplay = (String) js(
                "var el = document.querySelector('#monthyear' + arguments[0] + ' .monthlypaymentcontainer');" +
                " return el ? getComputedStyle(el).display : 'none';", year);
        if ("none".equalsIgnoreCase(currentDisplay)) {
            row.click();
        }
        waitForJsTruthy(
                "(function(){" +
                "  var el = document.querySelector('#monthyear" + year + " .monthlypaymentcontainer');" +
                "  return el && getComputedStyle(el).display !== 'none';" +
                "})()");
    }

    // ---------- UI CHECKS ----------

    public boolean isLoanAmountSliderDisplayed()   { return isVisible(loanAmountSlider); }
    public boolean isLoanInterestSliderDisplayed() { return isVisible(loanInterestSlider); }
    public boolean isLoanTermSliderDisplayed()     { return isVisible(loanTermSlider); }

    public enum TenureUnit { YEARS, MONTHS }
}
