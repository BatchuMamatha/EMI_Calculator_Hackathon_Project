package com.emicalc.automation.stepdefinitions;

import com.emicalc.automation.context.ScenarioContext;
import com.emicalc.automation.pages.LoanCalculatorPage;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.assertj.core.api.Assertions;

public class LoanCalculatorSteps {

    private static final Logger log = LogManager.getLogger(LoanCalculatorSteps.class);
    private final ScenarioContext ctx;

    public LoanCalculatorSteps(ScenarioContext ctx) {
        this.ctx = ctx;
    }

    @Given("the Loan Calculator page is open")
    public void open_loan_calc() {
        ctx.loanCalculatorPage = new LoanCalculatorPage().openDirect();
    }

    @When("I select the {string} sub-tab")
    public void select_sub_tab(String tab) {
        switch (tab) {
            case "EMI Calculator"          -> ctx.loanCalculatorPage.selectEmiCalculator();
            case "Loan Amount Calculator"  -> ctx.loanCalculatorPage.selectAmountCalculator();
            case "Loan Tenure Calculator"  -> ctx.loanCalculatorPage.selectTenureCalculator();
            default -> throw new IllegalArgumentException("Unknown sub-tab: " + tab);
        }
    }

    @Then("all visible input fields should be enabled")
    public void verify_inputs_enabled() {
        LoanCalculatorPage.UIValidationResult r = ctx.loanCalculatorPage.validateInputs();
        // On the EMI Calculator tab the EMI field doesn't exist — we don't
        // require r.emiEnabled to be true. We require the always-visible
        // amount/interest/tenure fields to be operable.
        Assertions.assertThat(r.loanAmountEnabled)
                .as("Loan amount text box enabled").isTrue();
        Assertions.assertThat(r.interestEnabled)
                .as("Interest text box enabled").isTrue();
        Assertions.assertThat(r.tenureEnabled)
                .as("Tenure text box enabled").isTrue();
    }

    @And("all sliders should be displayed")
    public void verify_sliders_displayed() {
        Assertions.assertThat(ctx.loanCalculatorPage.areAllSlidersDisplayed())
                .as("All three sliders rendered").isTrue();
    }

    @And("I capture the tenure scale signature")
    public void capture_signature() {
        ctx.tenureScaleSignatureBefore = ctx.loanCalculatorPage.tenureScaleSignature();
        log.info("Tenure scale signature (before): {}", ctx.tenureScaleSignatureBefore);
    }

    @And("I switch the tenure unit to {string}")
    public void switch_unit(String unit) {
        if ("Mo".equalsIgnoreCase(unit) || "Months".equalsIgnoreCase(unit)) {
            ctx.loanCalculatorPage.switchTenureToMonths();
        } else {
            ctx.loanCalculatorPage.switchTenureToYears();
        }
        ctx.tenureScaleSignatureAfter = ctx.loanCalculatorPage.tenureScaleSignature();
        log.info("Tenure scale signature (after {}): {}", unit, ctx.tenureScaleSignatureAfter);
    }

    @Then("the tenure scale signature should change")
    public void signature_should_change() {
        Assertions.assertThat(ctx.tenureScaleSignatureAfter)
                .as("Scale should differ between Year and Month modes")
                .isNotEqualTo(ctx.tenureScaleSignatureBefore);
    }

    @Then("the tenure scale signature should match the original")
    public void signature_should_match_original() {
        String now = ctx.loanCalculatorPage.tenureScaleSignature();
        Assertions.assertThat(now)
                .as("Scale should return to original after flipping back")
                .isEqualTo(ctx.tenureScaleSignatureBefore);
    }
}
