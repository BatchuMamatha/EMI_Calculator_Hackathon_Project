@CarLoan @Smoke
Feature: Car Loan EMI calculation (Problem Statement 1)
  As a buyer financing a 15 Lakh car at 9.5% for 1 year,
  I want emicalculator.net to calculate my EMI and first month's split
  so I know what I owe each month and how much is interest vs principal.

  Background:
    Given the EMI Calculator homepage is open

  @TC01
  Scenario: TC01 - EMI is calculated correctly for 15L at 9.5% for 1 year
    When I select the Car Loan tab
    And I enter loan amount "1500000" interest "9.5" tenure "1" in "Yr"
    Then the displayed EMI should match the formula within tolerance
    And the displayed total interest should match the formula within tolerance

  @TC02
  Scenario: TC02 - First month interest amount matches expected
    When I select the Car Loan tab
    And I enter loan amount "1500000" interest "9.5" tenure "1" in "Yr"
    Then the first month interest for year 2026 should match the formula within tolerance

  @TC03
  Scenario: TC03 - First month principal amount matches expected
    When I select the Car Loan tab
    And I enter loan amount "1500000" interest "9.5" tenure "1" in "Yr"
    Then the first month principal for year 2026 should match the formula within tolerance

  @TC04
  Scenario: TC04 - Car loan EMI summary is exported to Excel
    When I select the Car Loan tab
    And I enter loan amount "1500000" interest "9.5" tenure "1" in "Yr"
    Then the car loan EMI summary is written to Excel
    And the Excel file should have at least 2 rows
