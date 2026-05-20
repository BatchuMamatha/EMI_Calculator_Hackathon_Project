@CarLoan @Smoke
Feature: Car Loan EMI calculation (Problem Statement 1)
  Verify that emicalculator.net computes the car-loan EMI and the first
  month's interest/principal split correctly, and that the summary can be
  exported to Excel.

  Background:
    Given the EMI Calculator homepage is open

  Scenario Outline: <tc> - <name>
    When I select the Car Loan tab
    And I enter loan amount "<amount>" interest "<interest>" tenure "<tenure>" in "<unit>"
    Then the "<check>" for year <year> should match the formula within tolerance

    Examples:
      | tc   | name                    | amount  | interest | tenure | unit | check                  | year |
      | TC01 | EMI value               | 1500000 | 9.5      | 1      | Yr   | emi                    | 2026 |
      | TC02 | First month interest    | 1500000 | 9.5      | 1      | Yr   | first month interest   | 2026 |
      | TC03 | First month principal   | 1500000 | 9.5      | 1      | Yr   | first month principal  | 2026 |

  @TC04
  Scenario: TC04 - Car loan EMI summary is exported to Excel
    When I select the Car Loan tab
    And I enter loan amount "1500000" interest "9.5" tenure "1" in "Yr"
    Then the car loan EMI summary is written to Excel
    And the Excel file should have at least 2 rows
