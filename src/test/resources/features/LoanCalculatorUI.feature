@LoanCalculator @UI
Feature: Loan Calculator – UI validations across all three sub-calculators (Problem Statement 3)
  From the menu, open Loan Calculator. On the EMI Calculator sub-tab do all
  UI checks on text boxes and sliders, then flip the tenure unit Year ↔ Month
  and verify the slider scale changes. Re-use the same validation on the
  Loan Amount Calculator and Loan Tenure Calculator sub-tabs.

  Background:
    Given the Loan Calculator page is open

  @TC07
  Scenario: TC07 - EMI Calculator text boxes and sliders are operable
    When I select the "EMI Calculator" sub-tab
    Then all visible input fields should be enabled
    And all sliders should be displayed

  @TC08
  Scenario: TC08 - Tenure unit toggle changes the slider scale
    When I select the "EMI Calculator" sub-tab
    And I capture the tenure scale signature
    And I switch the tenure unit to "Mo"
    Then the tenure scale signature should change
    When I switch the tenure unit to "Yr"
    Then the tenure scale signature should match the original

  @TC09
  Scenario: TC09 - Same UI validation re-used on Loan Amount Calculator
    When I select the "Loan Amount Calculator" sub-tab
    Then all visible input fields should be enabled
    And all sliders should be displayed

  @TC10
  Scenario: TC10 - Same UI validation re-used on Loan Tenure Calculator
    When I select the "Loan Tenure Calculator" sub-tab
    Then all visible input fields should be enabled
    And all sliders should be displayed
