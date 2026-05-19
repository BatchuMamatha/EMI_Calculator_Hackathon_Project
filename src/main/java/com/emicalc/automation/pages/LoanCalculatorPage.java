package com.emicalc.automation.pages;

import com.emicalc.automation.config.ConfigReader;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

/**
 * Page Object for the Loan Calculator page (emicalculator.net/loan-calculator).
 * Three sub-calculators are exposed as tabs:
 *   - EMI Calculator
 *   - Loan Amount Calculator
 *   - Loan Tenure Calculator
 *
 * The hackathon asks for one reusable UI validation across all three sub-tabs;
 * {@link #validateInputs()} is that helper.
 *
 * Uses PageFactory @FindBy throughout — no By references.
 */
public class LoanCalculatorPage extends BasePage {

    // ---------- Menu / sub-tabs ----------
    @FindBy(id = "menu-item-dropdown-2696")           private WebElement loanCalcMenu;
    @FindBy(linkText = "Loan Calculator")             private WebElement loanCalculatorLink;

    @FindBy(xpath = "//a[normalize-space()='EMI Calculator']")          private WebElement tabEmi;
    @FindBy(xpath = "//a[normalize-space()='Loan Amount Calculator']")  private WebElement tabAmount;
    @FindBy(xpath = "//a[normalize-space()='Loan Tenure Calculator']")  private WebElement tabTenure;

    // ---------- Form inputs ----------
    @FindBy(name = "loanamount")   private WebElement loanAmount;
    @FindBy(name = "loaninterest") private WebElement loanInterest;
    @FindBy(name = "loanterm")     private WebElement loanTerm;
    @FindBy(name = "loanemi")      private List<WebElement> loanEmiOptional; // present only on Amount/Tenure tabs

    // Click the wrapping <label>, not the hidden <input type="radio">.
    @FindBy(xpath = "//label[input[@id='loanyears']]")  private WebElement tenureYearsBtn;
    @FindBy(xpath = "//label[input[@id='loanmonths']]") private WebElement tenureMonthsBtn;

    // ---------- Sliders ----------
    // Specific slider IDs differ across sub-tabs:
    //   EMI Calc      -> loanamountslider, loaninterestslider, loantermslider
    //   Amount Calc   -> loaninterestslider, loantermslider, loanemislider
    //   Tenure Calc   -> loanamountslider, loaninterestslider, loanemislider
    // The generic list below matches every slider handle on the page, so the
    // "all sliders displayed" check works on every sub-tab.
    @FindBy(css = "div.ui-slider .ui-slider-handle")
    private List<WebElement> allSliderHandles;

    // ---------- Scale ticks ----------
    @FindBy(css = "#loantermsteps .tick .marker") private List<WebElement> tenureScaleTicks;

    // ---------- Navigation ----------

    public LoanCalculatorPage openViaMenu() {
        actions().moveToElement(loanCalcMenu).perform();
        click(loanCalcMenu);
        wait.until(ExpectedConditions.visibilityOf(loanCalculatorLink));
        click(loanCalculatorLink);
        waitForReady();
        log.info("Navigated to Loan Calculator via menu");
        return this;
    }

    public LoanCalculatorPage openDirect() {
        super.open(ConfigReader.get().get("loan.calculator.url"));
        return this;
    }

    public LoanCalculatorPage selectEmiCalculator()    { click(tabEmi);    return this; }
    public LoanCalculatorPage selectAmountCalculator() { click(tabAmount); return this; }
    public LoanCalculatorPage selectTenureCalculator() { click(tabTenure); return this; }

    // ---------- Reusable validation primitives ----------

    public boolean isLoanAmountTextBoxEnabled() { return isEnabledSafe(loanAmount); }
    public boolean isInterestTextBoxEnabled()   { return isEnabledSafe(loanInterest); }
    public boolean isTenureTextBoxEnabled()     { return isEnabledSafe(loanTerm); }
    public boolean isEmiTextBoxEnabled()        {
        // The EMI input only renders on Amount / Tenure tabs.
        return !loanEmiOptional.isEmpty() && isEnabledSafe(loanEmiOptional.get(0));
    }

    /**
     * Three sliders are always shown — but which three depends on the active
     * sub-tab. We just count visible slider handles instead of asserting any
     * specific ID, so the same check works on EMI / Amount / Tenure tabs.
     */
    public boolean areAllSlidersDisplayed() {
        long visible = allSliderHandles.stream().filter(this::isVisible).count();
        log.info("Visible slider handles found: {}", visible);
        return visible >= 3;
    }

    /**
     * Capture the tenure scale ticks (e.g. "0|5|10|15|20|25|30|" for years vs
     * a denser scale for months). Used to assert the scale changes when the
     * Year/Month toggle is flipped.
     */
    public String tenureScaleSignature() {
        StringBuilder sb = new StringBuilder();
        for (WebElement m : tenureScaleTicks) {
            sb.append(m.getText().trim()).append('|');
        }
        return sb.toString();
    }

    public LoanCalculatorPage switchTenureToMonths() { click(tenureMonthsBtn); return this; }
    public LoanCalculatorPage switchTenureToYears()  { click(tenureYearsBtn);  return this; }

    /**
     * One-shot reusable UI sanity. Each sub-tab can be passed through this
     * helper — it checks every standard input is enabled and every slider
     * rendered. This is the reuse demanded by problem statement step 3.
     */
    public UIValidationResult validateInputs() {
        UIValidationResult r = new UIValidationResult();
        r.loanAmountEnabled = isLoanAmountTextBoxEnabled();
        r.interestEnabled   = isInterestTextBoxEnabled();
        r.tenureEnabled     = isTenureTextBoxEnabled();
        r.emiEnabled        = isEmiTextBoxEnabled();   // only true on Amount/Tenure tabs
        r.slidersVisible    = areAllSlidersDisplayed();
        log.info("UI validation: {}", r);
        return r;
    }

    public static class UIValidationResult {
        public boolean loanAmountEnabled;
        public boolean interestEnabled;
        public boolean tenureEnabled;
        public boolean emiEnabled;
        public boolean slidersVisible;

        @Override public String toString() {
            return String.format(
                "amount=%s, interest=%s, tenure=%s, emi=%s, sliders=%s",
                loanAmountEnabled, interestEnabled, tenureEnabled, emiEnabled, slidersVisible);
        }
    }
}
