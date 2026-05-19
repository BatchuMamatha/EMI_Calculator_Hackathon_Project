package com.emicalc.automation.pages;

import com.emicalc.automation.config.ConfigReader;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

public class LoanCalculatorPage extends BasePage {

    @FindBy(id = "menu-item-dropdown-2696") private WebElement loanCalcMenu;
    @FindBy(linkText = "Loan Calculator")   private WebElement loanCalculatorLink;

    @FindBy(xpath = "//a[normalize-space()='EMI Calculator']")         private WebElement tabEmi;
    @FindBy(xpath = "//a[normalize-space()='Loan Amount Calculator']") private WebElement tabAmount;
    @FindBy(xpath = "//a[normalize-space()='Loan Tenure Calculator']") private WebElement tabTenure;

    @FindBy(name = "loanamount")   private WebElement loanAmount;
    @FindBy(name = "loaninterest") private WebElement loanInterest;
    @FindBy(name = "loanterm")     private WebElement loanTerm;
    @FindBy(name = "loanemi")      private List<WebElement> loanEmiOptional;

    @FindBy(xpath = "//label[input[@id='loanyears']]")  private WebElement tenureYearsBtn;
    @FindBy(xpath = "//label[input[@id='loanmonths']]") private WebElement tenureMonthsBtn;

    // Generic slider locator: different sub-tabs use different specific IDs.
    @FindBy(css = "div.ui-slider .ui-slider-handle") private List<WebElement> allSliderHandles;

    @FindBy(css = "#loantermsteps .tick .marker") private List<WebElement> tenureScaleTicks;

    public LoanCalculatorPage openViaMenu() {
        actions().moveToElement(loanCalcMenu).perform();
        click(loanCalcMenu);
        wait.until(ExpectedConditions.visibilityOf(loanCalculatorLink));
        click(loanCalculatorLink);
        waitForReady();
        return this;
    }

    public LoanCalculatorPage openDirect() {
        super.open(ConfigReader.get().get("loan.calculator.url"));
        return this;
    }

    public LoanCalculatorPage selectEmiCalculator()    { click(tabEmi);    return this; }
    public LoanCalculatorPage selectAmountCalculator() { click(tabAmount); return this; }
    public LoanCalculatorPage selectTenureCalculator() { click(tabTenure); return this; }

    public boolean isLoanAmountTextBoxEnabled() { return isEnabledSafe(loanAmount); }
    public boolean isInterestTextBoxEnabled()   { return isEnabledSafe(loanInterest); }
    public boolean isTenureTextBoxEnabled()     { return isEnabledSafe(loanTerm); }
    public boolean isEmiTextBoxEnabled() {
        return !loanEmiOptional.isEmpty() && isEnabledSafe(loanEmiOptional.get(0));
    }

    public boolean areAllSlidersDisplayed() {
        return allSliderHandles.stream().filter(this::isVisible).count() >= 3;
    }

    public String tenureScaleSignature() {
        StringBuilder sb = new StringBuilder();
        for (WebElement m : tenureScaleTicks) sb.append(m.getText().trim()).append('|');
        return sb.toString();
    }

    public LoanCalculatorPage switchTenureToMonths() { click(tenureMonthsBtn); return this; }
    public LoanCalculatorPage switchTenureToYears()  { click(tenureYearsBtn);  return this; }

    public UIValidationResult validateInputs() {
        UIValidationResult r = new UIValidationResult();
        r.loanAmountEnabled = isLoanAmountTextBoxEnabled();
        r.interestEnabled   = isInterestTextBoxEnabled();
        r.tenureEnabled     = isTenureTextBoxEnabled();
        r.emiEnabled        = isEmiTextBoxEnabled();
        r.slidersVisible    = areAllSlidersDisplayed();
        return r;
    }

    public static class UIValidationResult {
        public boolean loanAmountEnabled, interestEnabled, tenureEnabled, emiEnabled, slidersVisible;

        @Override public String toString() {
            return String.format("amount=%s, interest=%s, tenure=%s, emi=%s, sliders=%s",
                    loanAmountEnabled, interestEnabled, tenureEnabled, emiEnabled, slidersVisible);
        }
    }
}
