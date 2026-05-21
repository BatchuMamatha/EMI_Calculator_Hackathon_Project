package com.emicalc.automation.stepdefinitions;

import com.emicalc.automation.context.ScenarioContext;
import com.emicalc.automation.pages.LoanCalculatorPage;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class LoanCalculatorSteps {

    private final ScenarioContext ctx;

    // PicoContainer injects the shared ScenarioContext.
    public LoanCalculatorSteps(ScenarioContext ctx) { this.ctx = ctx; }

    // Opens the Loan Calculator page directly via its URL.
    @Given("the Loan Calculator page is open")
    public void open_loan_calc() { ctx.loanCalculatorPage = new LoanCalculatorPage().openDirect(); }

    // Clicks the named sub-tab (EMI / Loan Amount / Loan Tenure Calculator).
    @When("I select the {string} sub-tab")
    public void select_sub_tab(String tab) {
        switch (tab) {
            case "EMI Calculator"         -> ctx.loanCalculatorPage.selectEmiCalculator();
            case "Loan Amount Calculator" -> ctx.loanCalculatorPage.selectAmountCalculator();
            case "Loan Tenure Calculator" -> ctx.loanCalculatorPage.selectTenureCalculator();
            default -> throw new IllegalArgumentException("Unknown sub-tab: " + tab);
        }
    }

    // Soft-asserts that loan amount, interest and tenure text boxes are enabled.
    @Then("all visible input fields should be enabled")
    public void verify_inputs_enabled() {
        LoanCalculatorPage.UIValidationResult r = ctx.loanCalculatorPage.validateInputs();
        ctx.softly.assertThat(r.loanAmountEnabled).as("Loan amount enabled").isTrue();
        ctx.softly.assertThat(r.interestEnabled).as("Interest enabled").isTrue();
        ctx.softly.assertThat(r.tenureEnabled).as("Tenure enabled").isTrue();
    }

    // Soft-asserts at least three slider handles are visible on the current sub-tab.
    @And("all sliders should be displayed")
    public void verify_sliders_displayed() {
        ctx.softly.assertThat(ctx.loanCalculatorPage.areAllSlidersDisplayed())
                .as("All sliders rendered").isTrue();
    }

    // Captures the tenure scale tick markers before any toggle changes them.
    @And("I capture the tenure scale signature")
    public void capture_signature() {
        ctx.tenureScaleSignatureBefore = ctx.loanCalculatorPage.tenureScaleSignature();
    }

    // Flips tenure unit to Yr or Mo and re-reads the new scale signature.
    @And("I switch the tenure unit to {string}")
    public void switch_unit(String unit) {
        if (unit.toLowerCase().startsWith("mo")) ctx.loanCalculatorPage.switchTenureToMonths();
        else                                     ctx.loanCalculatorPage.switchTenureToYears();
        ctx.tenureScaleSignatureAfter = ctx.loanCalculatorPage.tenureScaleSignature();
    }

    // Soft-asserts the new scale signature differs from the captured baseline.
    @Then("the tenure scale signature should change")
    public void signature_should_change() {
        ctx.softly.assertThat(ctx.tenureScaleSignatureAfter)
                .as("Scale changed").isNotEqualTo(ctx.tenureScaleSignatureBefore);
    }

    // Soft-asserts the scale signature returns to the original after flipping back.
    @Then("the tenure scale signature should match the original")
    public void signature_should_match_original() {
        ctx.softly.assertThat(ctx.loanCalculatorPage.tenureScaleSignature())
                .as("Scale returns to original").isEqualTo(ctx.tenureScaleSignatureBefore);
    }
}
