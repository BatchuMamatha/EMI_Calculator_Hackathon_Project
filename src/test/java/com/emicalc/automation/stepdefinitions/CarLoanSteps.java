package com.emicalc.automation.stepdefinitions;

import com.emicalc.automation.config.ConfigReader;
import com.emicalc.automation.context.ScenarioContext;
import com.emicalc.automation.pages.HomePage;
import com.emicalc.automation.utils.EMICalculatorUtil;
import com.emicalc.automation.utils.ExcelUtils;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

public class CarLoanSteps {

    private final ScenarioContext ctx;

    // PicoContainer injects the shared ScenarioContext.
    public CarLoanSteps(ScenarioContext ctx) { this.ctx = ctx; }

    // Opens the EMI Calculator homepage and stores the page object in context.
    @Given("the EMI Calculator homepage is open")
    public void open_homepage() { ctx.homePage = new HomePage().open(); }

    // Selects the Car Loan tab on the homepage.
    @When("I select the Car Loan tab")
    public void select_car_loan_tab() { ctx.homePage.selectCarLoanTab(); }

    // Fills the three loan inputs and stores parsed values in context for later asserts.
    @And("I enter loan amount {string} interest {string} tenure {string} in {string}")
    public void enter_loan_details(String amount, String interest, String tenure, String unit) {
        HomePage.TenureUnit u = unit.toLowerCase().startsWith("mo")
                ? HomePage.TenureUnit.MONTHS : HomePage.TenureUnit.YEARS;
        ctx.homePage.enterLoanAmount(amount).enterInterestRate(interest).enterTenure(tenure, u);
        ctx.put("principal", Double.parseDouble(amount));
        ctx.put("rate",      Double.parseDouble(interest));
        ctx.put("months",    u == HomePage.TenureUnit.YEARS
                ? Integer.parseInt(tenure) * 12 : Integer.parseInt(tenure));
    }

    // Dispatch step for Scenario Outline: picks the verification by name.
    @Then("the {string} for year {int} should match the formula within tolerance")
    public void verify_check(String check, int year) {
        switch (check.toLowerCase()) {
            case "emi" -> verify_emi();
            case "total interest" -> verify_total_interest();
            case "first month interest" -> verify_first_month_interest(year);
            case "first month principal" -> verify_first_month_principal(year);
            default -> throw new IllegalArgumentException("Unknown verification: " + check);
        }
    }

    // Soft-asserts the displayed EMI matches the formula within tolerance.
    @Then("the displayed EMI should match the formula within tolerance")
    public void verify_emi() {
        double p = ctx.get("principal"); double r = ctx.get("rate"); int m = ctx.get("months");
        long expected = Math.round(EMICalculatorUtil.emi(p, r, m));
        long actual   = ctx.homePage.readEmi();
        long tol      = ConfigReader.get().getInt("emi.tolerance");
        ctx.softly.assertThat(Math.abs(actual - expected))
                .as("EMI: expected %d +/- %d, got %d", expected, tol, actual)
                .isLessThanOrEqualTo(tol);
    }

    // Soft-asserts the displayed total interest matches the formula within tolerance.
    @And("the displayed total interest should match the formula within tolerance")
    public void verify_total_interest() {
        double p = ctx.get("principal"); double r = ctx.get("rate"); int m = ctx.get("months");
        long expected = Math.round(EMICalculatorUtil.totalInterest(p, r, m));
        long actual   = ctx.homePage.readTotalInterest();
        long tol      = ConfigReader.get().getInt("emi.tolerance") * (long) m;
        ctx.softly.assertThat(Math.abs(actual - expected))
                .as("Total interest: expected %d +/- %d, got %d", expected, tol, actual)
                .isLessThanOrEqualTo(tol);
    }

    // Soft-asserts the first month interest cell equals Principal x monthly rate.
    @Then("the first month interest for year {int} should match the formula within tolerance")
    public void verify_first_month_interest(int year) {
        double p = ctx.get("principal"); double r = ctx.get("rate");
        long expected = Math.round(EMICalculatorUtil.firstMonthInterest(p, r));
        long actual   = ctx.homePage.readFirstMonthInterest(year);
        long tol      = ConfigReader.get().getInt("emi.tolerance");
        ctx.softly.assertThat(Math.abs(actual - expected))
                .as("First month interest: expected %d +/- %d, got %d", expected, tol, actual)
                .isLessThanOrEqualTo(tol);
    }

    // Soft-asserts the first month principal cell equals EMI minus first month interest.
    @Then("the first month principal for year {int} should match the formula within tolerance")
    public void verify_first_month_principal(int year) {
        double p = ctx.get("principal"); double r = ctx.get("rate"); int m = ctx.get("months");
        long expected = Math.round(EMICalculatorUtil.firstMonthPrincipal(p, r, m));
        long actual   = ctx.homePage.readFirstMonthPrincipal(year);
        long tol      = ConfigReader.get().getInt("emi.tolerance");
        ctx.softly.assertThat(Math.abs(actual - expected))
                .as("First month principal: expected %d +/- %d, got %d", expected, tol, actual)
                .isLessThanOrEqualTo(tol);
    }

    // Writes the car loan EMI summary (8 rows) to output/CarLoan_EMI_Summary.xlsx via POI.
    @Then("the car loan EMI summary is written to Excel")
    public void write_car_summary() {
        double p = ctx.get("principal"); double r = ctx.get("rate"); int m = ctx.get("months");
        int year = LocalDate.now().getYear();
        List<List<String>> data = List.of(
                List.of("Field", "Value"),
                List.of("Principal (Rs.)",             String.valueOf((long) p)),
                List.of("Interest Rate (% p.a.)",      String.valueOf(r)),
                List.of("Tenure (months)",             String.valueOf(m)),
                List.of("EMI (Rs.)",                   String.valueOf(ctx.homePage.readEmi())),
                List.of("First Month Interest (Rs.)",  String.valueOf(ctx.homePage.readFirstMonthInterest(year))),
                List.of("First Month Principal (Rs.)", String.valueOf(ctx.homePage.readFirstMonthPrincipal(year))),
                List.of("Total Interest (Rs.)",        String.valueOf(ctx.homePage.readTotalInterest()))
        );
        String path = ConfigReader.get().get("excel.car.loan.file");
        ExcelUtils.writeSheet(path, "CarLoanEMI", data);
        ctx.put("carExcelPath", path);
    }

    // Soft-asserts the generated Excel file exists and contains at least N rows.
    @And("the Excel file should have at least {int} rows")
    public void excel_should_have_rows(int rows) {
        String path = ctx.get("carExcelPath");
        File f = new File(path);
        ctx.softly.assertThat(f.exists())
                .as("Excel not created: %s", path).isTrue();
        if (f.exists()) {
            ctx.softly.assertThat(ExcelUtils.rowCount(path, "CarLoanEMI"))
                    .as("Row count in %s", path).isGreaterThanOrEqualTo(rows);
        }
    }
}
