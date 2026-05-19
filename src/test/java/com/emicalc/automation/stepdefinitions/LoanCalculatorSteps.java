package com.emicalc.automation.stepdefinitions;

import com.emicalc.automation.context.ScenarioContext;
import com.emicalc.automation.pages.LoanCalculatorPage;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.assertj.core.api.Assertions;

public class LoanCalculatorSteps {

    private final ScenarioContext ctx;

    public LoanCalculatorSteps(ScenarioContext ctx) { this.ctx = ctx; }

    @Given("the Loan Calculator page is open")
    public void open_loan_calc() { ctx.loanCalculatorPage = new LoanCalculatorPage().openDirect(); }

    @When("I select the {string} sub-tab")
    public void select_sub_tab(String tab) {
        switch (tab) {
            case "EMI Calculator"         -> ctx.loanCalculatorPage.selectEmiCalculator();
            case "Loan Amount Calculator" -> ctx.loanCalculatorPage.selectAmountCalculator();
            case "Loan Tenure Calculator" -> ctx.loanCalculatorPage.selectTenureCalculator();
            default -> throw new IllegalArgumentException("Unknown sub-tab: " + tab);
        }
    }

    @Then("all visible input fields should be enabled")
    public void verify_inputs_enabled() {
        LoanCalculatorPage.UIValidationResult r = ctx.loanCalculatorPage.validateInputs();
        Assertions.assertThat(r.loanAmountEnabled).as("Loan amount enabled").isTrue();
        Assertions.assertThat(r.interestEnabled).as("Interest enabled").isTrue();
        Assertions.assertThat(r.tenureEnabled).as("Tenure enabled").isTrue();
    }

    @And("all sliders should be displayed")
    public void verify_sliders_displayed() {
        Assertions.assertThat(ctx.loanCalculatorPage.areAllSlidersDisplayed())
                .as("All sliders rendered").isTrue();
    }

    @And("I capture the tenure scale signature")
    public void capture_signature() { ctx.tenureScaleSignatureBefore = ctx.loanCalculatorPage.tenureScaleSignature(); }

    @And("I switch the tenure unit to {string}")
    public void switch_unit(String unit) {
        if (unit.toLowerCase().startsWith("mo")) ctx.loanCalculatorPage.switchTenureToMonths();
        else                                     ctx.loanCalculatorPage.switchTenureToYears();
        ctx.tenureScaleSignatureAfter = ctx.loanCalculatorPage.tenureScaleSignature();
    }

    @Then("the tenure scale signature should change")
    public void signature_should_change() {
        Assertions.assertThat(ctx.tenureScaleSignatureAfter)
                .as("Scale changed").isNotEqualTo(ctx.tenureScaleSignatureBefore);
    }

    @Then("the tenure scale signature should match the original")
    public void signature_should_match_original() {
        Assertions.assertThat(ctx.loanCalculatorPage.tenureScaleSignature())
                .as("Scale returns to original").isEqualTo(ctx.tenureScaleSignatureBefore);
    }
}
