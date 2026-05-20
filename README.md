# EMI Calculator – Hackathon Test Automation Project

> **Cohort:** INTQEA26QE003
> **System Under Test:** [emicalculator.net](https://emicalculator.net/)
> **Framework:** Java 17 · Selenium 4 · Cucumber 7 (BDD) · TestNG · Apache POI · Allure + Extent · Log4j 2 · Jenkins

A complete hybrid (BDD + keyword + data-driven) test automation suite that
validates three independent end-to-end flows on emicalculator.net, in
parallel across **Chrome and Edge**, and produces customised HTML reports
plus persisted Excel output.

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Business Requirement Document (BRD)](#2-business-requirement-document-brd)
3. [Three End-to-End Flows](#3-three-end-to-end-flows)
4. [Test Plan](#4-test-plan)
5. [Test Strategy](#5-test-strategy)
6. [Test Scenarios and Test Cases](#6-test-scenarios-and-test-cases)
7. [Requirements Traceability Matrix (RTM)](#7-requirements-traceability-matrix-rtm)
8. [Manual Testing and Defects](#8-manual-testing-and-defects)
9. [Automation Framework Architecture](#9-automation-framework-architecture)
10. [Technology Stack](#10-technology-stack)
11. [Folder Structure](#11-folder-structure)
12. [How to Run](#12-how-to-run)
13. [Reports](#13-reports)
14. [CI-CD with Jenkins Git and GitHub](#14-ci-cd-with-jenkins-git-and-github)
15. [Test Execution Summary Report](#15-test-execution-summary-report)
16. [Hackathon Requirements Checklist](#16-hackathon-requirements-checklist)

---

## 1. Project Overview

`emicalculator.net` is a public-facing Indian loan calculator. The hackathon
problem statement asks us to automate three end-to-end flows on it,
validating the maths, the menu navigation and the UI of three sub-calculators,
while building a production-grade test framework that uses every standard
QA component (Maven, POM, Apache POI, BDD, TestNG, custom HTML reports,
parallel multi-browser execution, listeners, Log4j, Jenkins CI/CD).

The framework is **hybrid**:

- **BDD layer** — Gherkin `*.feature` files describe scenarios in plain English. Every Given/When/Then phrase is a reusable **keyword** mapped to a step definition method.
- **Data-driven** — test data is externalised in `config.properties` and parametrised through Gherkin step arguments (`"1500000"`, `"9.5"`, `"1"`, `"Yr"`), so the same step definitions exercise different loan inputs without code change.
- **Keyword-driven** — each Gherkin phrase is itself a keyword; step definitions are the "engine" that interprets them.

The 10 scenarios run in **parallel on Chrome and Edge** (Firefox kept
commented out per requirement), with each browser opened **once per runner**
and reused across all scenarios — fast and resource-efficient.

---

## 2. Business Requirement Document (BRD)

### 2.1 Business Need

Borrowers and finance teams using `emicalculator.net` need confidence that:
- The EMI shown matches the standard EMI formula
- The per-month split (principal vs interest) is correct in the very first instalment
- The full year-by-year amortisation schedule can be exported for offline analysis
- The Loan Calculator's UI remains operable across all three sub-calculators

### 2.2 Stakeholders

<table>
  <thead>
    <tr>
      <th>Stakeholder</th>
      <th>Interest</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>Car/Home loan applicants</td>
      <td>Accurate EMI numbers before signing</td>
    </tr>
    <tr>
      <td>Finance team analysts</td>
      <td>Excel exports for sharing with stakeholders</td>
    </tr>
    <tr>
      <td>QA Lead (Cohort INTQEA26QE003)</td>
      <td>A demonstrable, repeatable automation suite</td>
    </tr>
    <tr>
      <td>QA Team</td>
      <td>Reusable framework for future calculator validations</td>
    </tr>
    <tr>
      <td>CI/CD Owner</td>
      <td>Push-triggered Jenkins runs with reports archived</td>
    </tr>
  </tbody>
</table>

### 2.3 In-Scope Functional Requirements

<table>
  <thead>
    <tr>
      <th>ID</th>
      <th>Requirement</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>R01</td>
      <td>Car loan EMI shall match the EMI formula within ±₹2 tolerance</td>
    </tr>
    <tr>
      <td>R02</td>
      <td>First month interest = Principal × monthly rate; first month principal = EMI − first month interest</td>
    </tr>
    <tr>
      <td>R03</td>
      <td>Car loan EMI summary shall be exportable to Excel via Apache POI</td>
    </tr>
    <tr>
      <td>R04</td>
      <td>Home Loan EMI Calculator shall be reachable from the top menu</td>
    </tr>
    <tr>
      <td>R05</td>
      <td>The year-on-year payment schedule shall be extractable and storable to Excel</td>
    </tr>
    <tr>
      <td>R06</td>
      <td>EMI Calculator sub-tab's text boxes and sliders shall be operable</td>
    </tr>
    <tr>
      <td>R07</td>
      <td>Switching tenure Year ↔ Month shall change the slider scale</td>
    </tr>
    <tr>
      <td>R08</td>
      <td>The same UI validation shall be reusable across all 3 sub-calculators</td>
    </tr>
    <tr>
      <td>R09</td>
      <td>The suite shall execute in parallel on Chrome and Edge (Firefox excluded)</td>
    </tr>
    <tr>
      <td>R10</td>
      <td>Failures shall capture a screenshot attached to the test report</td>
    </tr>
  </tbody>
</table>

### 2.4 Out of Scope

- Native mobile app testing (only web)
- Performance / load testing
- Accessibility audit
- Localisation (only `en-US` validated)

---

## 3. Three End-to-End Flows

The three flows were picked to be **functionally distinct** so each
exercises a different concern (computation, table extraction, UI sanity):

### Flow 1 — Car Loan EMI computation
1. Open emicalculator.net
2. Switch to Car Loan tab
3. Enter ₹15,00,000 / 9.5% / 1 year
4. Read EMI, total interest, first-month split
5. Assert against the formula; export summary to Excel

### Flow 2 — Home Loan year-on-year schedule extraction
1. From the homepage, navigate via Menu → "Home Loan EMI Calculator"
2. Fill ₹25,00,000 / 8.5% / 20 years
3. Read every row of the year-on-year amortisation table (year, principal, interest, total payment, balance, paid-to-date)
4. Persist the table to `output/HomeLoan_YearlySchedule.xlsx` via Apache POI
5. Assert the file exists and ≥ 10 yearly rows are present

### Flow 3 — Loan Calculator UI reuse
1. From the menu, open "Loan Calculator"
2. On the **EMI Calculator** sub-tab — validate text boxes and sliders
3. Toggle tenure Year ↔ Month, assert the slider scale changes and returns
4. Switch to **Loan Amount Calculator** sub-tab — reuse the same validation
5. Switch to **Loan Tenure Calculator** sub-tab — reuse the same validation

These three flows are wholly independent (different pages, different data,
different assertions), satisfying the "pick three end-to-end flows" and
"ensure all three are different flows" requirements.

### Quick worked example (Flow 1)

For Principal = ₹15,00,000, Rate = 9.5% p.a., Tenure = 12 months:

<table>
  <thead>
    <tr>
      <th>Metric</th>
      <th>Value</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>EMI</td>
      <td><strong>₹1,31,524</strong></td>
    </tr>
    <tr>
      <td>First month — Interest</td>
      <td><strong>₹11,875</strong></td>
    </tr>
    <tr>
      <td>First month — Principal</td>
      <td><strong>₹1,19,649</strong></td>
    </tr>
    <tr>
      <td>Total Interest (current year)</td>
      <td><strong>₹78,288</strong></td>
    </tr>
  </tbody>
</table>

(Tolerance: ±₹2 to absorb intermediate rounding on the site.)

---

## 4. Test Plan

### 4.1 Objective

Verify the correctness, reliability and cross-browser stability of the three
selected emicalculator.net flows and demonstrate every required automation
framework component.

### 4.2 Scope

<table>
  <thead>
    <tr>
      <th>In scope</th>
      <th>Out of scope</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>Car / Home / Loan-Calculator flows</td>
      <td>Personal loan, Credit Card EMI</td>
    </tr>
    <tr>
      <td>EMI formula correctness, first-month split</td>
      <td>Tax & insurance overlays</td>
    </tr>
    <tr>
      <td>Year-on-year schedule extraction</td>
      <td>Prepayment-modified schedules</td>
    </tr>
    <tr>
      <td>UI sanity on 3 sub-calculators</td>
      <td>Mobile / responsive viewport tests</td>
    </tr>
    <tr>
      <td>Chrome + Edge parallel</td>
      <td>Firefox, Safari</td>
    </tr>
    <tr>
      <td>Functional + integration (Excel I/O)</td>
      <td>Performance, accessibility</td>
    </tr>
  </tbody>
</table>

### 4.3 Test Deliverables

<table>
  <thead>
    <tr>
      <th>Deliverable</th>
      <th>Location</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>Test Plan (this document)</td>
      <td><a href="README.md"><code>README.md</code></a></td>
    </tr>
    <tr>
      <td>Test Strategy</td>
      <td>This README §5</td>
    </tr>
    <tr>
      <td>BRD</td>
      <td>This README §2</td>
    </tr>
    <tr>
      <td>Test Scenarios + Test Cases + RTM + Defects (Excel)</td>
      <td><a href="docs/Project_Documentation.xlsx"><code>docs/Project_Documentation.xlsx</code></a></td>
    </tr>
    <tr>
      <td>Feature files (BDD)</td>
      <td><a href="src/test/resources/features"><code>src/test/resources/features/</code></a></td>
    </tr>
    <tr>
      <td>Allure report</td>
      <td><code>target/allure-report/index.html</code></td>
    </tr>
    <tr>
      <td>Extent Spark report</td>
      <td><code>reports/extent/SparkReport.html</code></td>
    </tr>
    <tr>
      <td>Cucumber HTML reports</td>
      <td><code>reports/cucumber/{chrome,edge}-cucumber.html</code></td>
    </tr>
    <tr>
      <td>Generated Excel artefacts</td>
      <td><code>output/*.xlsx</code></td>
    </tr>
    <tr>
      <td>Log files</td>
      <td><code>logs/automation.log</code></td>
    </tr>
    <tr>
      <td>Screenshots (every scenario, PASS or FAIL)</td>
      <td><code>screenshots/</code></td>
    </tr>
    <tr>
      <td>Jenkins pipeline definition</td>
      <td><a href="Jenkinsfile"><code>Jenkinsfile</code></a></td>
    </tr>
  </tbody>
</table>

### 4.4 Entry Criteria

- JDK 17 installed and on `PATH`
- Maven 3.9+ installed and on `PATH`
- Chrome and Edge installed (Selenium Manager fetches drivers automatically)
- Network access to `emicalculator.net`

### 4.5 Exit Criteria

- All 10 test cases pass on both Chrome and Edge (20/20 green)
- Excel artefacts (`output/*.xlsx`) produced and asserted on disk
- Allure + Extent + Cucumber HTML reports rendered
- No P1/P2 defects open

### 4.6 Risks & Mitigation

<table>
  <thead>
    <tr>
      <th>Risk</th>
      <th>Mitigation</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>Site DOM changes break locators</td>
      <td>Use multiple locator strategies + <code>@FindBy</code> proxies that re-resolve on each call</td>
    </tr>
    <tr>
      <td>Ad blocks shift the schedule table</td>
      <td>Explicit waits + <code>scrollBy(0,800)</code> before extraction</td>
    </tr>
    <tr>
      <td>Network flakiness</td>
      <td>Selenium Manager caches drivers; explicit timeouts in <code>config.properties</code></td>
    </tr>
    <tr>
      <td>Browser-driver version mismatch</td>
      <td>Selenium 4.16+ uses Selenium Manager — auto-downloads correct driver</td>
    </tr>
    <tr>
      <td>Parallel-execution race conditions</td>
      <td><code>ThreadLocal<WebDriver></code> in <code>DriverFactory</code>; custom thread-safe <code>ExtentManager</code></td>
    </tr>
  </tbody>
</table>

---

## 5. Test Strategy

### 5.1 Testing Types

<table>
  <thead>
    <tr>
      <th>Type</th>
      <th>Coverage</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>Functional</td>
      <td>EMI maths, first-month split, schedule extraction</td>
    </tr>
    <tr>
      <td>Integration</td>
      <td>Apache POI Excel writes, file persistence</td>
    </tr>
    <tr>
      <td>UI sanity</td>
      <td>Text boxes operable, sliders rendered, tab switching</td>
    </tr>
    <tr>
      <td>Cross-browser</td>
      <td>Parallel Chrome + Edge</td>
    </tr>
    <tr>
      <td>Regression</td>
      <td>Tagged <code>@Regression</code> for selective runs (<code>-Dcucumber.filter.tags="@Regression"</code>)</td>
    </tr>
    <tr>
      <td>Smoke</td>
      <td>Tagged <code>@Smoke</code> for fast pre-merge checks</td>
    </tr>
  </tbody>
</table>

### 5.2 Test Approach

- **Hybrid framework** — BDD scenarios (keyword-driven) + parametrised inputs (data-driven) + Page Object Model (POM) for maintainability
- **Independent computation** — `EMICalculatorUtil` calculates the expected EMI in pure Java; the test compares it against the value rendered on the site, so we catch UI bugs *and* maths bugs
- **Locator diversity** — id, name, css, xpath, linkText, partialLinkText all used across page objects (satisfies "different locator techniques")
- **No raw `By` declarations** — every locator lives on a `@FindBy` annotated WebElement field (PageFactory pattern); dynamic locators use JavaScript queries
- **One browser per runner** — opened in `@BeforeClass`, closed in `@AfterClass`; scenarios reuse it, only cookies are cleared between scenarios

### 5.3 Test Environment

<table>
  <thead>
    <tr>
      <th>Component</th>
      <th>Value</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>OS</td>
      <td>Windows 11</td>
    </tr>
    <tr>
      <td>JDK</td>
      <td>17 (LTS)</td>
    </tr>
    <tr>
      <td>Maven</td>
      <td>3.9+</td>
    </tr>
    <tr>
      <td>Browsers</td>
      <td>Chrome 140+, Edge 140+</td>
    </tr>
    <tr>
      <td>Headless mode</td>
      <td>Toggleable via <code>-Dheadless=true</code></td>
    </tr>
  </tbody>
</table>

### 5.4 Tools

<table>
  <thead>
    <tr>
      <th>Tool</th>
      <th>Purpose</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>Selenium 4 + Selenium Manager</td>
      <td>Browser automation; auto-downloads driver binaries</td>
    </tr>
    <tr>
      <td>Cucumber 7 (Java)</td>
      <td>BDD layer, Gherkin parsing</td>
    </tr>
    <tr>
      <td>TestNG 7</td>
      <td>Test orchestration, parallel execution, listeners</td>
    </tr>
    <tr>
      <td>Apache POI 5</td>
      <td>Excel read/write</td>
    </tr>
    <tr>
      <td>ExtentReports 5 + custom adapter</td>
      <td>Customised HTML report (Spark theme)</td>
    </tr>
    <tr>
      <td>Allure 2</td>
      <td>Customised HTML report (alternative)</td>
    </tr>
    <tr>
      <td>Log4j 2</td>
      <td>Structured logging, rolling file appender</td>
    </tr>
    <tr>
      <td>AssertJ + TestNG Assert</td>
      <td>Assertions</td>
    </tr>
    <tr>
      <td>AspectJ Weaver</td>
      <td>Allure step interception</td>
    </tr>
    <tr>
      <td>Maven Surefire</td>
      <td>Test runner integration</td>
    </tr>
    <tr>
      <td>Jenkins + Git + GitHub</td>
      <td>CI/CD pipeline</td>
    </tr>
  </tbody>
</table>

### 5.5 Reporting Cadence

- Each `mvn test` run produces fresh Cucumber HTML, Extent Spark, and Allure raw results
- Allure HTML is generated on demand via `mvn allure:report`
- Jenkins archives all reports per build and renders them via the HTML Publisher / Allure plugin

---

## 6. Test Scenarios and Test Cases

Detailed Test Scenarios and Test Cases are maintained in
[`docs/Project_Documentation.xlsx`](docs/Project_Documentation.xlsx)
(sheets `TestScenarios` and `TestCases`).

### 6.1 Test Scenarios (10)

<table>
  <thead>
    <tr>
      <th>Module</th>
      <th>ID</th>
      <th>Scenario Title</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>Car Loan</td>
      <td>TS01</td>
      <td>Verify EMI calculation for 15L / 9.5% / 1yr</td>
    </tr>
    <tr>
      <td>Car Loan</td>
      <td>TS02</td>
      <td>Verify first month interest amount</td>
    </tr>
    <tr>
      <td>Car Loan</td>
      <td>TS03</td>
      <td>Verify first month principal amount</td>
    </tr>
    <tr>
      <td>Car Loan</td>
      <td>TS04</td>
      <td>Export car loan EMI summary to Excel</td>
    </tr>
    <tr>
      <td>Home Loan</td>
      <td>TS05</td>
      <td>Navigate to Home Loan Calculator via top menu</td>
    </tr>
    <tr>
      <td>Home Loan</td>
      <td>TS06</td>
      <td>Extract year-on-year schedule and store in Excel</td>
    </tr>
    <tr>
      <td>Loan Calculator</td>
      <td>TS07</td>
      <td>EMI Calculator UI inputs and sliders operable</td>
    </tr>
    <tr>
      <td>Loan Calculator</td>
      <td>TS08</td>
      <td>Tenure unit toggle changes slider scale</td>
    </tr>
    <tr>
      <td>Loan Calculator</td>
      <td>TS09</td>
      <td>Reuse same UI validation on Loan Amount Calculator</td>
    </tr>
    <tr>
      <td>Loan Calculator</td>
      <td>TS10</td>
      <td>Reuse same UI validation on Loan Tenure Calculator</td>
    </tr>
  </tbody>
</table>

### 6.2 Test Cases (10) — TC01 to TC10

<table>
  <thead>
    <tr>
      <th>TC</th>
      <th>Feature</th>
      <th>Scenario</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>TC01</td>
      <td><code>CarLoanEMI.feature</code></td>
      <td>EMI for 15L/9.5%/1yr is correct</td>
    </tr>
    <tr>
      <td>TC02</td>
      <td><code>CarLoanEMI.feature</code></td>
      <td>First month interest matches formula</td>
    </tr>
    <tr>
      <td>TC03</td>
      <td><code>CarLoanEMI.feature</code></td>
      <td>First month principal matches formula</td>
    </tr>
    <tr>
      <td>TC04</td>
      <td><code>CarLoanEMI.feature</code></td>
      <td>Car loan summary exported to Excel</td>
    </tr>
    <tr>
      <td>TC05</td>
      <td><code>HomeLoanYearlySchedule.feature</code></td>
      <td>Menu navigation to Home Loan Calculator</td>
    </tr>
    <tr>
      <td>TC06</td>
      <td><code>HomeLoanYearlySchedule.feature</code></td>
      <td>Year-on-year schedule extracted and stored to Excel</td>
    </tr>
    <tr>
      <td>TC07</td>
      <td><code>LoanCalculatorUI.feature</code></td>
      <td>EMI Calc — text boxes + sliders operable</td>
    </tr>
    <tr>
      <td>TC08</td>
      <td><code>LoanCalculatorUI.feature</code></td>
      <td>Tenure Year↔Month flip changes slider scale</td>
    </tr>
    <tr>
      <td>TC09</td>
      <td><code>LoanCalculatorUI.feature</code></td>
      <td>Same validation reused on Loan Amount Calculator</td>
    </tr>
    <tr>
      <td>TC10</td>
      <td><code>LoanCalculatorUI.feature</code></td>
      <td>Same validation reused on Loan Tenure Calculator</td>
    </tr>
  </tbody>
</table>

Each TC in the Excel document has detailed step-level columns (Step #, Step
Description, Step Expected Results, Actual Result, Status, Defect).

---

## 7. Requirements Traceability Matrix (RTM)

Full RTM is maintained in [`docs/Project_Documentation.xlsx`](docs/Project_Documentation.xlsx) → `RTM` sheet. Summary:

<table>
  <thead>
    <tr>
      <th>#</th>
      <th>Feature</th>
      <th>Req ID</th>
      <th>Test Scenario</th>
      <th>Test Case</th>
      <th>Status</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>1</td>
      <td>F01</td>
      <td>R01</td>
      <td>TS01</td>
      <td>TC01</td>
      <td>Pass</td>
    </tr>
    <tr>
      <td>2</td>
      <td>F01</td>
      <td>R02</td>
      <td>TS02</td>
      <td>TC02</td>
      <td>Pass</td>
    </tr>
    <tr>
      <td>3</td>
      <td>F01</td>
      <td>R02</td>
      <td>TS03</td>
      <td>TC03</td>
      <td>Pass</td>
    </tr>
    <tr>
      <td>4</td>
      <td>F01</td>
      <td>R03</td>
      <td>TS04</td>
      <td>TC04</td>
      <td>Pass</td>
    </tr>
    <tr>
      <td>5</td>
      <td>F02</td>
      <td>R04</td>
      <td>TS05</td>
      <td>TC05</td>
      <td>Pass</td>
    </tr>
    <tr>
      <td>6</td>
      <td>F02</td>
      <td>R05</td>
      <td>TS06</td>
      <td>TC06</td>
      <td>Pass</td>
    </tr>
    <tr>
      <td>7</td>
      <td>F03</td>
      <td>R06</td>
      <td>TS07</td>
      <td>TC07</td>
      <td>Pass</td>
    </tr>
    <tr>
      <td>8</td>
      <td>F03</td>
      <td>R07</td>
      <td>TS08</td>
      <td>TC08</td>
      <td>Pass</td>
    </tr>
    <tr>
      <td>9</td>
      <td>F03</td>
      <td>R08</td>
      <td>TS09</td>
      <td>TC09</td>
      <td>Pass</td>
    </tr>
    <tr>
      <td>10</td>
      <td>F03</td>
      <td>R08</td>
      <td>TS10</td>
      <td>TC10</td>
      <td>Pass</td>
    </tr>
    <tr>
      <td>11</td>
      <td>F04</td>
      <td>R09</td>
      <td>—</td>
      <td>—</td>
      <td>Cross-cutting (parallel Chrome+Edge)</td>
    </tr>
    <tr>
      <td>12</td>
      <td>F04</td>
      <td>R10</td>
      <td>—</td>
      <td>—</td>
      <td>Cross-cutting (failure screenshot)</td>
    </tr>
  </tbody>
</table>

---

## 8. Manual Testing and Defects

Before automating, every flow was walked through manually on Chrome and Edge
to validate expected behaviour and surface obvious issues. Defects found and
tracked in [`docs/Project_Documentation.xlsx`](docs/Project_Documentation.xlsx) → `Defects` sheet:

<table>
  <thead>
    <tr>
      <th>Defect ID</th>
      <th>Severity</th>
      <th>Status</th>
      <th>Notes</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>DEF001</td>
      <td>Medium</td>
      <td>Open</td>
      <td>Year-on-year extraction occasionally times out on first run due to ad blocks reflowing — workaround added (scroll + explicit wait)</td>
    </tr>
    <tr>
      <td>DEF002</td>
      <td>Low</td>
      <td>Open</td>
      <td>Tenure scale signature differs between Chrome/Edge at >150% zoom — cosmetic</td>
    </tr>
    <tr>
      <td>DEF003</td>
      <td>Low</td>
      <td>Closed (Won't Fix)</td>
      <td>Switching tabs resets loan amount — third-party site behaviour</td>
    </tr>
    <tr>
      <td>DEF004</td>
      <td>Low</td>
      <td>Open</td>
      <td>First-month row may read stale value if schedule animation incomplete — fixed with explicit wait</td>
    </tr>
  </tbody>
</table>

---

## 9. Automation Framework Architecture

```
                        ┌─────────────────────────────┐
                        │       testng.xml            │
                        │  parallel="tests" thread=4  │
                        └──────────────┬──────────────┘
                                       │
                     ┌─────────────────┴─────────────────┐
                     │                                   │
            ┌────────▼──────────┐               ┌───────▼──────────┐
            │   ChromeRunner    │               │    EdgeRunner    │
            │  @BeforeClass     │               │  @BeforeClass    │
            │  open Chrome 1x   │               │  open Edge 1x    │
            └────────┬──────────┘               └───────┬──────────┘
                     │                                  │
            ┌────────▼──────────┐               ┌───────▼──────────┐
            │ Cucumber-TestNG   │               │ Cucumber-TestNG  │
            │ runs 10 scenarios │               │ runs 10 scenarios│
            └────────┬──────────┘               └───────┬──────────┘
                     │                                  │
                     │   Hooks.@Before/@After           │
                     │   (cookies, screenshot, Extent)  │
                     │                                  │
            ┌────────▼─────────────────────────────────▼───────┐
            │             Step Definitions                     │
            │  CarLoanSteps  HomeLoanSteps  LoanCalculatorSteps│
            └────────┬─────────────────────────────────────────┘
                     │
            ┌────────▼─────────────────────────────────────────┐
            │             Page Objects  (POM)                  │
            │  HomePage  HomeLoanPage  LoanCalculatorPage      │
            │  Locators: @FindBy only (no By in user code)     │
            └────────┬─────────────────────────────────────────┘
                     │
            ┌────────▼─────────────────────────────────────────┐
            │   Utilities                                      │
            │   ConfigReader   DriverFactory (ThreadLocal)     │
            │   ExcelUtils (POI)  ScreenshotUtils  WaitUtils   │
            │   EMICalculatorUtil  ExtentManager (thread-safe) │
            └──────────────────────────────────────────────────┘
```

### Key design decisions

<table>
  <thead>
    <tr>
      <th>Decision</th>
      <th>Reason</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>One browser per runner, not per scenario</td>
      <td>Faster runs, more realistic user session</td>
    </tr>
    <tr>
      <td><code>ThreadLocal<WebDriver></code> in <code>DriverFactory</code></td>
      <td>Safe parallel browsers, no cross-thread interference</td>
    </tr>
    <tr>
      <td>Custom thread-safe <code>ExtentManager</code></td>
      <td>The community <code>extentreports-cucumber7-adapter</code> has a Gson <code>LinkedTreeMap</code> concurrency bug — we drive Extent directly from Cucumber hooks instead</td>
    </tr>
    <tr>
      <td>PageFactory <code>@FindBy</code> everywhere</td>
      <td>Cleaner POM, no <code>By</code> declarations in user code</td>
    </tr>
    <tr>
      <td>Multiple locator strategies (id/name/css/xpath/linkText)</td>
      <td>Demonstrates locator-technique requirement; defensive against DOM changes</td>
    </tr>
    <tr>
      <td>JS-based dynamic locator resolution</td>
      <td>Replaces <code>By.id("year"+n)</code> patterns in a <code>By</code>-free codebase</td>
    </tr>
    <tr>
      <td>Excel writes via Apache POI in step defs</td>
      <td>Direct data extraction → persisted artefact (no intermediate CSV)</td>
    </tr>
  </tbody>
</table>

---

## 10. Technology Stack

<table>
  <thead>
    <tr>
      <th>Layer</th>
      <th>Choice</th>
      <th>Version</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>Language</td>
      <td>Java</td>
      <td>17 (LTS)</td>
    </tr>
    <tr>
      <td>Build</td>
      <td>Maven</td>
      <td>3.9+</td>
    </tr>
    <tr>
      <td>Browser automation</td>
      <td>Selenium WebDriver</td>
      <td>4.16.1</td>
    </tr>
    <tr>
      <td>Driver management</td>
      <td>Selenium Manager (built-in)</td>
      <td>—</td>
    </tr>
    <tr>
      <td>BDD</td>
      <td>Cucumber JVM</td>
      <td>7.15.0</td>
    </tr>
    <tr>
      <td>Test runner</td>
      <td>TestNG</td>
      <td>7.8.0</td>
    </tr>
    <tr>
      <td>DI for Cucumber</td>
      <td>PicoContainer</td>
      <td>7.15.0</td>
    </tr>
    <tr>
      <td>Excel I/O</td>
      <td>Apache POI</td>
      <td>5.2.5</td>
    </tr>
    <tr>
      <td>HTML Report 1</td>
      <td>ExtentReports Spark</td>
      <td>5.1.1</td>
    </tr>
    <tr>
      <td>HTML Report 2</td>
      <td>Allure</td>
      <td>2.25.0</td>
    </tr>
    <tr>
      <td>Logging</td>
      <td>Log4j 2</td>
      <td>2.22.1</td>
    </tr>
    <tr>
      <td>Assertions</td>
      <td>AssertJ + TestNG Assert</td>
      <td>3.25.1 / 7.8.0</td>
    </tr>
    <tr>
      <td>Allure interceptor</td>
      <td>AspectJ Weaver</td>
      <td>1.9.21</td>
    </tr>
    <tr>
      <td>CI/CD</td>
      <td>Jenkins (declarative pipeline)</td>
      <td>—</td>
    </tr>
    <tr>
      <td>Source control</td>
      <td>Git + GitHub</td>
      <td>—</td>
    </tr>
  </tbody>
</table>

---

## 11. Folder Structure

```
EMI_Calculator_Hackathon_Project/
├── pom.xml                         # Maven build
├── testng.xml                      # TestNG suite (parallel Chrome + Edge)
├── Jenkinsfile                     # Declarative Jenkins pipeline
├── .gitignore
├── README.md                       # this file
├── docs/
│   ├── Project_Documentation.xlsx  # User Stories, Test Scenarios, Test Cases, RTM, Defects (5 sheets)
│   └── generate_docs.py            # script that produced the above
├── output/                         # *.xlsx artefacts (generated)
├── reports/
│   ├── extent/SparkReport.html     # Extent (Spark) report
│   ├── extent/screenshots/         # failure screenshots
│   └── cucumber/                   # per-browser Cucumber HTML/JSON
├── logs/                           # Log4j rolling logs
├── src/
│   ├── main/
│   │   ├── java/com/emicalc/automation/
│   │   │   ├── config/             # ConfigReader.java
│   │   │   ├── driver/             # DriverFactory.java (ThreadLocal)
│   │   │   ├── pages/              # BasePage, HomePage, HomeLoanPage, LoanCalculatorPage
│   │   │   ├── reports/            # ExtentManager (thread-safe)
│   │   │   └── utils/              # ExcelUtils, ScreenshotUtils, WaitUtils, EMICalculatorUtil
│   │   └── resources/
│   │       ├── config.properties   # URLs, test data, timeouts
│   │       └── log4j2.xml          # Log4j config
│   └── test/
│       ├── java/com/emicalc/automation/
│       │   ├── context/            # ScenarioContext (per-thread state)
│       │   ├── hooks/              # Hooks.java
│       │   ├── listeners/          # TestListener.java
│       │   ├── runners/            # ChromeRunner, EdgeRunner, FirefoxRunner.java.disabled
│       │   └── stepdefinitions/    # CarLoanSteps, HomeLoanSteps, LoanCalculatorSteps
│       └── resources/
│           ├── allure.properties
│           ├── extent.properties   # (doc-only; we drive Extent directly)
│           ├── extent-config.xml
│           └── features/
│               ├── CarLoanEMI.feature
│               ├── HomeLoanYearlySchedule.feature
│               └── LoanCalculatorUI.feature
└── target/                         # Maven build output (allure-results, surefire-reports)
```

---

## 12. How to Run

### Prerequisites

- JDK 17 on `PATH` — verify with `java -version`
- Maven 3.9+ on `PATH` — verify with `mvn -version`
- Chrome and Edge installed locally

### Run the full suite (parallel Chrome + Edge)

```powershell
mvn clean test
```

### Headless (recommended on CI)

```powershell
mvn clean test -Dheadless=true
```

### Run a tag subset

```powershell
mvn clean test "-Dcucumber.filter.tags=@CarLoan"
mvn clean test "-Dcucumber.filter.tags=@Smoke"
```

### From the IDE

Right-click [`testng.xml`](testng.xml) → **Run As TestNG Suite**.
Both browsers open simultaneously and each runs all 10 scenarios.

---

## 13. Reports

### 13.1 Allure Report

```powershell
mvn allure:report          # generates target/allure-report/index.html
mvn allure:serve           # generates + opens in a local web server
```

Open `target/allure-report/index.html` in a browser.

### 13.2 Extent Spark Report

Auto-generated at `reports/extent/SparkReport.html` after each run. The
thread-safe `ExtentManager` writes one consolidated report covering both
browser runs, with failure screenshots embedded as base64.

### 13.3 Cucumber HTML Reports

Per browser:
- `reports/cucumber/chrome-cucumber.html`
- `reports/cucumber/edge-cucumber.html`

Plus JSON outputs (`chrome-cucumber.json`, `edge-cucumber.json`) for
downstream tooling.

### 13.4 Excel Outputs (Apache POI)

- `output/CarLoan_EMI_Summary.xlsx` — Flow 1 summary
- `output/HomeLoan_YearlySchedule.xlsx` — Flow 2 amortisation table

### 13.5 Logs

Log4j rolling file appender writes to `logs/automation.log`. Rolls daily
and at 10 MB; keeps 10 historical files compressed.

---

## 14. CI-CD with Jenkins Git and GitHub

### 14.1 Git workflow

```powershell
git init
git add .
git commit -m "feat: bootstrap EMI Calculator automation suite"
git branch -M main
git remote add origin https://github.com/<you>/EMI_Calculator_Hackathon_Project.git
git push -u origin main
```

### 14.2 Jenkins setup

1. Create a new **Pipeline** job. Set the script path to `Jenkinsfile`.
2. **Global Tool Configuration** — register:
   - JDK 17 named `JDK17`
   - Maven 3.x named `Maven3`
   - (Optional) Allure Commandline named `Allure`
3. Ensure the agent has Chrome and Edge installed.
4. Add a **GitHub webhook** (Repo → Settings → Webhooks → `https://<jenkins>/github-webhook/`) for push-triggered builds.

### 14.3 Pipeline stages

The [`Jenkinsfile`](Jenkinsfile) declares:

```
Checkout  →  Build  →  Test (parallel Chrome + Edge)  →  Publish Reports  →  Archive Artefacts
```

Parameters exposed to the build:
- `TAG` — optional Cucumber tag filter (`@Smoke`, `@Regression`, etc.)
- `HEADLESS` — boolean, default `true` on CI

Post-build:
- JUnit XML results from `target/surefire-reports/`
- HTML Publisher renders Extent, Cucumber Chrome, Cucumber Edge
- Allure plugin renders the Allure dashboard
- Excel files, logs, screenshots, raw Allure results archived

---

## 15. Test Execution Summary Report

Final run on 2026-05-19:

```
[RemoteTestNG] detected TestNG version 7.8.0
Total tests run: 20, Passes: 20, Failures: 0, Skips: 0
```

### Breakdown

<table>
  <thead>
    <tr>
      <th>TC</th>
      <th>Chrome</th>
      <th>Edge</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>TC01</td>
      <td>Pass</td>
      <td>Pass</td>
    </tr>
    <tr>
      <td>TC02</td>
      <td>Pass</td>
      <td>Pass</td>
    </tr>
    <tr>
      <td>TC03</td>
      <td>Pass</td>
      <td>Pass</td>
    </tr>
    <tr>
      <td>TC04</td>
      <td>Pass</td>
      <td>Pass</td>
    </tr>
    <tr>
      <td>TC05</td>
      <td>Pass</td>
      <td>Pass</td>
    </tr>
    <tr>
      <td>TC06</td>
      <td>Pass</td>
      <td>Pass</td>
    </tr>
    <tr>
      <td>TC07</td>
      <td>Pass</td>
      <td>Pass</td>
    </tr>
    <tr>
      <td>TC08</td>
      <td>Pass</td>
      <td>Pass</td>
    </tr>
    <tr>
      <td>TC09</td>
      <td>Pass</td>
      <td>Pass</td>
    </tr>
    <tr>
      <td>TC10</td>
      <td>Pass</td>
      <td>Pass</td>
    </tr>
  </tbody>
</table>

### Notable assertions verified at run time

<table>
  <thead>
    <tr>
      <th>Assertion</th>
      <th>Value</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>Car loan EMI (15L / 9.5% / 1yr)</td>
      <td>₹1,31,525 (within ±₹2 of expected ₹1,31,524)</td>
    </tr>
    <tr>
      <td>First month interest</td>
      <td>₹11,875 (exact match)</td>
    </tr>
    <tr>
      <td>First month principal</td>
      <td>₹1,19,650 (within tolerance)</td>
    </tr>
    <tr>
      <td>Home loan yearly rows extracted</td>
      <td>21 (≥ 10 required)</td>
    </tr>
    <tr>
      <td>Excel files produced</td>
      <td>2 (CarLoan + HomeLoan), persisted on disk</td>
    </tr>
    <tr>
      <td>Tenure scale signature (Yr)</td>
      <td><code>0|5|10|15|20|25|30|</code></td>
    </tr>
    <tr>
      <td>Tenure scale signature (Mo)</td>
      <td><code>0|60|120|180|240|300|360|</code></td>
    </tr>
  </tbody>
</table>

### Artefacts produced

<table>
  <thead>
    <tr>
      <th>Artefact</th>
      <th>Size</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><code>output/CarLoan_EMI_Summary.xlsx</code></td>
      <td>8 rows</td>
    </tr>
    <tr>
      <td><code>output/HomeLoan_YearlySchedule.xlsx</code></td>
      <td>22 rows (header + 21 years)</td>
    </tr>
    <tr>
      <td>Failure screenshots</td>
      <td>None (clean run)</td>
    </tr>
    <tr>
      <td>Allure raw results</td>
      <td><code>target/allure-results/</code></td>
    </tr>
    <tr>
      <td>Extent Spark report</td>
      <td><code>reports/extent/SparkReport.html</code></td>
    </tr>
  </tbody>
</table>

---

## 16. Hackathon Requirements Checklist

<table>
  <thead>
    <tr>
      <th>#</th>
      <th>Requirement</th>
      <th>Where it lives</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>1</td>
      <td>Pick three end-to-end flows</td>
      <td>§3 (Car / Home / Loan Calculator UI)</td>
    </tr>
    <tr>
      <td>2</td>
      <td>Ensure all three are different flows</td>
      <td>§3 — different pages, data, assertions</td>
    </tr>
    <tr>
      <td>3</td>
      <td>Prepare Test Scenarios & Test Cases</td>
      <td><a href="docs/Project_Documentation.xlsx"><code>docs/Project_Documentation.xlsx</code></a></td>
    </tr>
    <tr>
      <td>4</td>
      <td>Fill RTM</td>
      <td><a href="docs/Project_Documentation.xlsx"><code>docs/Project_Documentation.xlsx</code></a> → <code>RTM</code> sheet</td>
    </tr>
    <tr>
      <td>5</td>
      <td>Manual testing & defects</td>
      <td><a href="docs/Project_Documentation.xlsx"><code>docs/Project_Documentation.xlsx</code></a> → <code>Defects</code> sheet; §8</td>
    </tr>
    <tr>
      <td>6a</td>
      <td>Maven</td>
      <td><a href="pom.xml"><code>pom.xml</code></a></td>
    </tr>
    <tr>
      <td>6b</td>
      <td>Proper folder structure</td>
      <td>§11</td>
    </tr>
    <tr>
      <td>6c</td>
      <td>Apache POI</td>
      <td><a href="src/main/java/com/emicalc/automation/utils/ExcelUtils.java"><code>ExcelUtils.java</code></a> + step defs</td>
    </tr>
    <tr>
      <td>6d</td>
      <td>POM Design Pattern</td>
      <td><a href="src/main/java/com/emicalc/automation/pages"><code>pages/</code></a> — BasePage + 3 concrete pages</td>
    </tr>
    <tr>
      <td>6e</td>
      <td>Data-driven + Keyword-driven (Hybrid)</td>
      <td>Gherkin keywords (<code>Given/When/Then</code>) + parametrised arguments + <code>config.properties</code> test data</td>
    </tr>
    <tr>
      <td>6f</td>
      <td>TestNG</td>
      <td><a href="testng.xml"><code>testng.xml</code></a>, <code>AbstractTestNGCucumberTests</code> runners</td>
    </tr>
    <tr>
      <td>6g</td>
      <td>Exception handling</td>
      <td><code>try-with-resources</code> in POI; defensive catches in <code>ScreenshotUtils</code>, <code>DriverFactory</code>, <code>BasePage</code></td>
    </tr>
    <tr>
      <td>6h</td>
      <td>Different locator techniques</td>
      <td>id, name, css, xpath, linkText, partialLinkText — see <code>HomePage</code> / <code>HomeLoanPage</code></td>
    </tr>
    <tr>
      <td>6i</td>
      <td>Screenshot capture</td>
      <td><a href="src/main/java/com/emicalc/automation/utils/ScreenshotUtils.java"><code>ScreenshotUtils.java</code></a>, invoked in <code>Hooks</code> + <code>TestListener</code></td>
    </tr>
    <tr>
      <td>6j</td>
      <td>Customised HTML Reports (Allure + Extent)</td>
      <td><a href="src/main/java/com/emicalc/automation/reports/ExtentManager.java"><code>ExtentManager.java</code></a> + <code>allure-cucumber7-jvm</code> plugin</td>
    </tr>
    <tr>
      <td>6k</td>
      <td>End-to-end execution</td>
      <td><code>mvn clean test</code> runs the full suite</td>
    </tr>
    <tr>
      <td>6l</td>
      <td>Multiple browser execution</td>
      <td>Chrome + Edge runners, parallel via <code>testng.xml</code></td>
    </tr>
    <tr>
      <td>6m</td>
      <td>Listeners</td>
      <td><a href="src/test/java/com/emicalc/automation/listeners/TestListener.java"><code>TestListener.java</code></a> + Cucumber <code>Hooks</code></td>
    </tr>
    <tr>
      <td>6o</td>
      <td>Log4j</td>
      <td><a href="src/main/resources/log4j2.xml"><code>log4j2.xml</code></a> + rolling file appender</td>
    </tr>
    <tr>
      <td>6p</td>
      <td>Parallel test execution</td>
      <td><code>testng.xml parallel="tests"</code></td>
    </tr>
    <tr>
      <td>6q</td>
      <td>Assertions</td>
      <td>TestNG <code>Assert</code> + AssertJ in every step</td>
    </tr>
    <tr>
      <td>7</td>
      <td>Test Plan & Test Strategy</td>
      <td>§4 and §5</td>
    </tr>
    <tr>
      <td>8</td>
      <td>BRD</td>
      <td>§2</td>
    </tr>
    <tr>
      <td>9</td>
      <td>Test Execution Summary Report + CI/CD (Git/GitHub/Jenkins)</td>
      <td>§15 + §14 + <a href="Jenkinsfile"><code>Jenkinsfile</code></a></td>
    </tr>
  </tbody>
</table>

---

## Help & Troubleshooting

<table>
  <thead>
    <tr>
      <th>Symptom</th>
      <th>Fix</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><code>MojoNotFoundException: allure:reports</code></td>
      <td>The goal is <code>report</code> (singular): <code>mvn allure:report</code></td>
    </tr>
    <tr>
      <td>Only Chrome opens, Edge skipped</td>
      <td>Make sure you're running <code>testng.xml</code> (not a single class). Check <code>parallel="tests"</code> is intact in the suite XML</td>
    </tr>
    <tr>
      <td><code>element click intercepted</code> warnings</td>
      <td>Already mitigated by clicking the <code><label></code> parent of radio inputs</td>
    </tr>
    <tr>
      <td>Allure CLI not found</td>
      <td>Use <code>mvn allure:report</code> (uses the Maven plugin — no separate CLI install needed)</td>
    </tr>
    <tr>
      <td><code>MicrosoftEdge dns error: msedgedriver.azureedge.net</code></td>
      <td>Network blocked? Selenium Manager falls back to the cached driver — safe to ignore</td>
    </tr>
    <tr>
      <td>Excel file locked</td>
      <td>Close any Excel windows that have <code>output/*.xlsx</code> open before re-running</td>
    </tr>
  </tbody>
</table>

---


**Date:** 2026-05-19
**License:** Internal / Hackathon submission
