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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.assertj.core.api.Assertions;
import org.testng.Assert;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

public class CarLoanSteps {

    private static final Logger log = LogManager.getLogger(CarLoanSteps.class);
    private final ScenarioContext ctx;

    public CarLoanSteps(ScenarioContext ctx) {
        this.ctx = ctx;
    }

    @Given("the EMI Calculator homepage is open")
    public void open_homepage() {
        ctx.homePage = new HomePage().open();
    }

    @When("I select the Car Loan tab")
    public void select_car_loan_tab() {
        ctx.homePage.selectCarLoanTab();
    }

    @And("I enter loan amount {string} interest {string} tenure {string} in {string}")
    public void enter_loan_details(String amount, String interest, String tenure, String unit) {
        HomePage.TenureUnit u = "Mo".equalsIgnoreCase(unit) || "Mo.".equalsIgnoreCase(unit)
                ? HomePage.TenureUnit.MONTHS
                : HomePage.TenureUnit.YEARS;
        ctx.homePage.enterLoanAmount(amount)
                    .enterInterestRate(interest)
                    .enterTenure(tenure, u);

        // stash inputs for later assertion steps
        ctx.put("principal", Double.parseDouble(amount));
        ctx.put("rate",      Double.parseDouble(interest));
        ctx.put("months",    u == HomePage.TenureUnit.YEARS
                                ? Integer.parseInt(tenure) * 12
                                : Integer.parseInt(tenure));
    }

    @Then("the displayed EMI should match the formula within tolerance")
    public void verify_emi() {
        double p = ctx.get("principal");
        double r = ctx.get("rate");
        int    m = ctx.get("months");
        long expected = Math.round(EMICalculatorUtil.emi(p, r, m));
        long actual   = ctx.homePage.readEmi();
        long tol      = ConfigReader.get().getInt("emi.tolerance");
        log.info("EMI assertion: expected={}, actual={}, tolerance=±{}", expected, actual, tol);
        Assertions.assertThat(Math.abs(actual - expected))
                .as("EMI mismatch: expected %d ± %d, got %d", expected, tol, actual)
                .isLessThanOrEqualTo(tol);
    }

    @And("the displayed total interest should match the formula within tolerance")
    public void verify_total_interest() {
        double p = ctx.get("principal");
        double r = ctx.get("rate");
        int    m = ctx.get("months");
        long expected = Math.round(EMICalculatorUtil.totalInterest(p, r, m));
        long actual   = ctx.homePage.readTotalInterest();
        long tol      = ConfigReader.get().getInt("emi.tolerance") * (long) m;
        // tolerance grows with months because per-EMI rounding compounds
        Assertions.assertThat(Math.abs(actual - expected))
                .as("Total interest mismatch: expected %d ± %d, got %d", expected, tol, actual)
                .isLessThanOrEqualTo(tol);
    }

    @Then("the first month interest for year {int} should match the formula within tolerance")
    public void verify_first_month_interest(int year) {
        double p = ctx.get("principal");
        double r = ctx.get("rate");
        long expected = Math.round(EMICalculatorUtil.firstMonthInterest(p, r));
        long actual   = ctx.homePage.readFirstMonthInterest(year);
        long tol      = ConfigReader.get().getInt("emi.tolerance");
        log.info("1st-month interest: expected={}, actual={}", expected, actual);
        Assertions.assertThat(Math.abs(actual - expected))
                .as("First month interest mismatch: expected %d ± %d, got %d", expected, tol, actual)
                .isLessThanOrEqualTo(tol);
    }

    @Then("the first month principal for year {int} should match the formula within tolerance")
    public void verify_first_month_principal(int year) {
        double p = ctx.get("principal");
        double r = ctx.get("rate");
        int    m = ctx.get("months");
        long expected = Math.round(EMICalculatorUtil.firstMonthPrincipal(p, r, m));
        long actual   = ctx.homePage.readFirstMonthPrincipal(year);
        long tol      = ConfigReader.get().getInt("emi.tolerance");
        log.info("1st-month principal: expected={}, actual={}", expected, actual);
        Assertions.assertThat(Math.abs(actual - expected))
                .as("First month principal mismatch: expected %d ± %d, got %d", expected, tol, actual)
                .isLessThanOrEqualTo(tol);
    }

    @Then("the car loan EMI summary is written to Excel")
    public void write_car_summary() {
        double p = ctx.get("principal");
        double r = ctx.get("rate");
        int    m = ctx.get("months");
        long emi          = ctx.homePage.readEmi();
        long firstInt     = ctx.homePage.readFirstMonthInterest(LocalDate.now().getYear());
        long firstPrinc   = ctx.homePage.readFirstMonthPrincipal(LocalDate.now().getYear());
        long totalInt     = ctx.homePage.readTotalInterest();

        List<List<String>> data = List.of(
            List.of("Field", "Value"),
            List.of("Principal (₹)",              String.valueOf((long) p)),
            List.of("Interest Rate (% p.a.)",     String.valueOf(r)),
            List.of("Tenure (months)",            String.valueOf(m)),
            List.of("EMI (₹)",                    String.valueOf(emi)),
            List.of("First Month Interest (₹)",   String.valueOf(firstInt)),
            List.of("First Month Principal (₹)",  String.valueOf(firstPrinc)),
            List.of("Total Interest (₹)",         String.valueOf(totalInt))
        );
        String path = ConfigReader.get().get("excel.car.loan.file");
        ExcelUtils.writeSheet(path, "CarLoanEMI", data);
        ctx.put("carExcelPath", path);
    }

    @And("the Excel file should have at least {int} rows")
    public void excel_should_have_rows(int rows) {
        String path = ctx.get("carExcelPath");
        Assert.assertTrue(new File(path).exists(), "Excel file not created: " + path);
        int actual = ExcelUtils.rowCount(path, "CarLoanEMI");
        Assertions.assertThat(actual)
                .as("Row count in %s", path)
                .isGreaterThanOrEqualTo(rows);
    }
}
