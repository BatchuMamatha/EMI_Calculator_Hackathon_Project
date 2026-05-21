package com.emicalc.automation.stepdefinitions;

import com.emicalc.automation.base.BaseClass;
import com.emicalc.automation.config.ConfigReader;
import com.emicalc.automation.context.ScenarioContext;
import com.emicalc.automation.pages.HomeLoanPage;
import com.emicalc.automation.pages.HomePage;
import com.emicalc.automation.utils.ExcelUtils;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.io.File;

public class HomeLoanSteps {

    private final ScenarioContext ctx;

    // PicoContainer injects the shared ScenarioContext.
    public HomeLoanSteps(ScenarioContext ctx) { this.ctx = ctx; }

    // Hovers the top menu and clicks "Home Loan EMI Calculator" to navigate.
    @When("I open {string} from the Loan Calculators menu")
    public void open_from_menu(String menuItem) {
        if (ctx.homePage == null) ctx.homePage = new HomePage().open();
        if ("Home Loan EMI Calculator".equalsIgnoreCase(menuItem)) {
            ctx.homeLoanPage = new HomeLoanPage().openViaMenu(ctx.homePage);
        } else {
            throw new IllegalArgumentException("Unsupported menu item: " + menuItem);
        }
    }

    // Soft-asserts the current URL contains the home-loan-emi-calculator path.
    @Then("the Home Loan EMI Calculator page should load")
    public void verify_home_loan_loaded() {
        ctx.softly.assertThat(BaseClass.getDriver().getCurrentUrl())
                .as("Home Loan URL").contains("home-loan-emi-calculator");
    }

    // Opens the Home Loan calculator page directly via its URL.
    @Given("the Home Loan EMI Calculator page is open")
    public void open_home_loan_page() { ctx.homeLoanPage = new HomeLoanPage().openDirect(); }

    // Fills the three home loan form inputs.
    @When("I fill the home loan form with amount {string} interest {string} tenure {string} years")
    public void fill_home_loan_form(String amount, String interest, String tenure) {
        ctx.homeLoanPage.fillForm(amount, interest, tenure);
    }

    // Extracts the year-on-year schedule grid into the scenario context.
    @And("I extract the year-on-year schedule")
    public void extract_schedule() { ctx.extractedSchedule = ctx.homeLoanPage.extractYearlySchedule(); }

    // Soft-asserts at least N yearly rows were extracted (header row excluded).
    @Then("the schedule should have at least {int} yearly rows")
    public void schedule_min_rows(int min) {
        ctx.softly.assertThat(ctx.extractedSchedule.size() - 1)
                .as("Yearly rows extracted").isGreaterThanOrEqualTo(min);
    }

    // Writes the extracted schedule to output/HomeLoan_YearlySchedule.xlsx via POI.
    @And("the schedule is stored in the Home Loan Excel file")
    public void store_to_excel() {
        String path = ConfigReader.get().get("excel.home.loan.file");
        ExcelUtils.writeSheet(path, "YearlySchedule", ctx.extractedSchedule);
        ctx.put("homeLoanExcelPath", path);
    }

    // Soft-asserts the Home Loan Excel file exists on disk and is non-empty.
    @And("the Home Loan Excel file should exist on disk")
    public void verify_excel_on_disk() {
        File f = new File((String) ctx.get("homeLoanExcelPath"));
        ctx.softly.assertThat(f.exists() && f.length() > 0)
                .as("Excel not created: %s", f.getAbsolutePath()).isTrue();
    }
}
