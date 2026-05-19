package com.emicalc.automation.pages;

import com.emicalc.automation.config.ConfigReader;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindAll;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.ArrayList;
import java.util.List;

/**
 * Page Object for the dedicated Home Loan EMI Calculator page reached via
 * the menu (Loan Calculators & Widgets -> Home Loan EMI Calculator).
 *
 * Uses PageFactory @FindBy; no By references in user-written code.
 */
public class HomeLoanPage extends BasePage {

    // ---------- Menu navigation ----------
    @FindBy(id = "menu-item-dropdown-2696") private WebElement loanCalcMenu;
    @FindBy(linkText = "Home Loan EMI Calculator") private WebElement homeLoanMenuLink;

    // ---------- Form inputs ----------
    // FindAll falls back from the dedicated-page id to the shared name attribute
    @FindAll({
            @FindBy(id = "homeloanamount"),
            @FindBy(name = "loanamount")
    })
    private WebElement loanAmount;

    @FindAll({
            @FindBy(id = "homeloaninterest"),
            @FindBy(name = "loaninterest")
    })
    private WebElement loanInterest;

    @FindAll({
            @FindBy(id = "homeloanterm"),
            @FindBy(name = "loanterm")
    })
    private WebElement loanTenure;

    // ---------- Yearly schedule rows ----------
    @FindBy(css = "tr.yearlypaymentdetails") private List<WebElement> yearlyRows;

    // ---------- Navigation ----------

    public HomeLoanPage openViaMenu(HomePage homePage) {
        actions().moveToElement(loanCalcMenu).perform();
        click(loanCalcMenu);
        wait.until(ExpectedConditions.visibilityOf(homeLoanMenuLink));
        click(homeLoanMenuLink);
        waitForReady();
        log.info("Navigated to Home Loan EMI Calculator via menu");
        return this;
    }

    public HomeLoanPage openDirect() {
        super.open(ConfigReader.get().get("home.loan.url"));
        return this;
    }

    // ---------- Fill ----------

    public HomeLoanPage fillForm(String amount, String rate, String tenureYears) {
        typeReplacing(loanAmount, amount);
        typeReplacing(loanInterest, rate);
        typeReplacing(loanTenure, tenureYears);
        return this;
    }

    // ---------- Extract ----------

    /**
     * Walk the yearly schedule table and return it as header + data grid.
     * Columns: Year, Principal, Interest, Total Payment, Balance, Loan Paid To Date.
     */
    public List<List<String>> extractYearlySchedule() {
        // Scroll the table into view so lazy ad blocks above don't shift layout
        scrollBy(0, 800);
        wait.until(d -> !yearlyRows.isEmpty());

        if (yearlyRows.isEmpty()) {
            throw new IllegalStateException("Yearly schedule table is empty — page may not have rendered");
        }

        List<List<String>> grid = new ArrayList<>();
        grid.add(List.of("Year", "Principal", "Interest", "Total Payment", "Balance", "Loan Paid To Date"));

        for (WebElement row : yearlyRows) {
            // Read cells via JS to avoid By.tagName lookups
            @SuppressWarnings("unchecked")
            List<String> cells = (List<String>) js(
                    "return Array.from(arguments[0].querySelectorAll('td')).map(td => td.innerText.trim());",
                    row);
            if (cells.size() < 5) continue;

            String year       = cells.get(0);
            String principal  = cells.get(1);
            String interest   = cells.get(2);
            String total      = cells.size() > 3 ? cells.get(3) : "";
            String balance    = cells.size() > 4 ? cells.get(4) : "";
            String paidToDate = cells.size() > 5 ? cells.get(5) : "";

            grid.add(List.of(year, principal, interest, total, balance, paidToDate));
        }
        log.info("Extracted {} yearly rows from Home Loan schedule", grid.size() - 1);
        return grid;
    }
}
