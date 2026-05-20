@HomeLoan @Regression
Feature: Home Loan EMI Calculator – year-on-year schedule extract (Problem Statement 2)

  @TC05
  Scenario: TC05 - Navigate to Home Loan EMI Calculator via the top menu
    Given the EMI Calculator homepage is open
    When I open "Home Loan EMI Calculator" from the Loan Calculators menu
    Then the Home Loan EMI Calculator page should load

  @TC06
  Scenario: TC06 - Year-on-year schedule is extracted and stored in Excel
    Given the Home Loan EMI Calculator page is open
    When I fill the home loan form with amount "2500000" interest "8.5" tenure "20" years
    And I extract the year-on-year schedule
    Then the schedule should have at least 10 yearly rows
    And the schedule is stored in the Home Loan Excel file
    And the Home Loan Excel file should exist on disk
