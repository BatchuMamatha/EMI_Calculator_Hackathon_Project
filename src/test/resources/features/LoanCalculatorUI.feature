@LoanCalculator @UI
Feature: Loan Calculator UI validations (Problem Statement 3)
  Verify that text boxes and sliders on every sub-tab are operable, and
  that toggling the tenure unit Year/Month changes the slider scale.

  Background:
    Given the Loan Calculator page is open

  Scenario Outline: <tc> - UI validation on <subtab>
    When I select the "<subtab>" sub-tab
    Then all visible input fields should be enabled
    And all sliders should be displayed

    Examples:
      | tc   | subtab                    |
      | TC07 | EMI Calculator            |
      | TC09 | Loan Amount Calculator    |
      | TC10 | Loan Tenure Calculator    |

  @TC08
  Scenario: TC08 - Tenure unit toggle changes the slider scale
    When I select the "EMI Calculator" sub-tab
    And I capture the tenure scale signature
    And I switch the tenure unit to "Mo"
    Then the tenure scale signature should change
    When I switch the tenure unit to "Yr"
    Then the tenure scale signature should match the original
