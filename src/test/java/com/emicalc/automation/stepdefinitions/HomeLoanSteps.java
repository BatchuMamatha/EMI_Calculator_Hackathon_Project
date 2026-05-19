package com.emicalc.automation.stepdefinitions;

import com.emicalc.automation.config.ConfigReader;
import com.emicalc.automation.context.ScenarioContext;
import com.emicalc.automation.pages.HomeLoanPage;
import com.emicalc.automation.pages.HomePage;
import com.emicalc.automation.utils.ExcelUtils;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.assertj.core.api.Assertions;
import org.testng.Assert;

import java.io.File;

public class HomeLoanSteps {

    private static final Logger log = LogManager.getLogger(HomeLoanSteps.class);
    private final ScenarioContext ctx;

    public HomeLoanSteps(ScenarioContext ctx) {
        this.ctx = ctx;
    }

    @When("I open {string} from the Loan Calculators menu")
    public void open_from_menu(String menuItem) {
        // Lazily open homepage if test starts on this step
        if (ctx.homePage == null) ctx.homePage = new HomePage().open();

        if ("Home Loan EMI Calculator".equalsIgnoreCase(menuItem)) {
            ctx.homeLoanPage = new HomeLoanPage().openViaMenu(ctx.homePage);
        } else {
            throw new IllegalArgumentException("Unsupported menu item in test: " + menuItem);
        }
    }

    @Then("the Home Loan EMI Calculator page should load")
    public void verify_home_loan_loaded() {
        String url = ctx.homeLoanPage != null
                ? ctx.homeLoanPage.toString()
                : "";
        // Read driver URL — simpler than another locator
        String currentUrl = com.emicalc.automation.driver.DriverFactory.getDriver().getCurrentUrl();
        log.info("Current URL after menu navigation: {}", currentUrl);
        Assertions.assertThat(currentUrl)
                .as("Should be on home-loan-emi-calculator page")
                .contains("home-loan-emi-calculator");
    }

    @Given("the Home Loan EMI Calculator page is open")
    public void open_home_loan_page() {
        ctx.homeLoanPage = new HomeLoanPage().openDirect();
    }

    @When("I fill the home loan form with amount {string} interest {string} tenure {string} years")
    public void fill_home_loan_form(String amount, String interest, String tenure) {
        ctx.homeLoanPage.fillForm(amount, interest, tenure);
    }

    @And("I extract the year-on-year schedule")
    public void extract_schedule() {
        ctx.extractedSchedule = ctx.homeLoanPage.extractYearlySchedule();
    }

    @Then("the schedule should have at least {int} yearly rows")
    public void schedule_min_rows(int min) {
        // The header is row 0, so data rows = total - 1
        Assertions.assertThat(ctx.extractedSchedule.size() - 1)
                .as("Yearly rows extracted")
                .isGreaterThanOrEqualTo(min);
    }

    @And("the schedule is stored in the Home Loan Excel file")
    public void store_to_excel() {
        String path = ConfigReader.get().get("excel.home.loan.file");
        ExcelUtils.writeSheet(path, "YearlySchedule", ctx.extractedSchedule);
        ctx.put("homeLoanExcelPath", path);
    }

    @And("the Home Loan Excel file should exist on disk")
    public void verify_excel_on_disk() {
        String path = ctx.get("homeLoanExcelPath");
        File f = new File(path);
        Assert.assertTrue(f.exists() && f.length() > 0,
                "Home Loan Excel not created at " + f.getAbsolutePath());
        log.info("Home Loan Excel persisted at {} ({} bytes)", f.getAbsolutePath(), f.length());
    }
}
