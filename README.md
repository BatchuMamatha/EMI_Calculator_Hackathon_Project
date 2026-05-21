# EMI Calculator – Hackathon Test Automation Project

> **Cohort:** INTQEA26QE003
> **System Under Test:** [emicalculator.net](https://emicalculator.net/)
> **Repo:** https://github.com/BatchuMamatha/EMI_Calculator_Hackathon_Project

A Selenium 4 + Cucumber 7 (BDD) + TestNG automation suite that runs **20 tests in parallel on Chrome and Edge** (10 scenarios × 2 browsers) against [emicalculator.net](https://emicalculator.net/). All per-TC inputs live in a single Excel file, soft assertions collect all failures, screenshots are captured for every scenario, and three HTML reports (Cucumber, Extent, Allure) are produced per run.

---

## Table of Contents

1. [Three End-to-End Flows](#1-three-end-to-end-flows)
2. [Quick Worked Example (Flow 1)](#2-quick-worked-example-flow-1)
3. [Tech Stack](#3-tech-stack)
4. [Folder Structure](#4-folder-structure)
5. [How to Run](#5-how-to-run)
6. [Test Data in Excel](#6-test-data-in-excel)
7. [Reports](#7-reports)
8. [Screenshots](#8-screenshots)
9. [Parallel Execution and ThreadLocal](#9-parallel-execution-and-threadlocal)
10. [Multi-Browser Support](#10-multi-browser-support)
11. [Soft Assertions](#11-soft-assertions)
12. [CI CD Pipeline](#12-ci-cd-pipeline)
13. [Test Plan and Strategy](#13-test-plan-and-strategy)
14. [Business Requirements](#14-business-requirements)
15. [Test Scenarios, Test Cases, RTM, Defects](#15-test-scenarios-test-cases-rtm-defects)
16. [Hackathon Requirements Checklist](#16-hackathon-requirements-checklist)
17. [Troubleshooting](#17-troubleshooting)

---

## 1. Three End-to-End Flows

<table>
  <thead>
    <tr>
      <th>Flow</th>
      <th>Feature file</th>
      <th>What it does</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><strong>Car Loan EMI</strong></td>
      <td><a href="src/test/resources/features/CarLoanEMI.feature"><code>CarLoanEMI.feature</code></a></td>
      <td>Switch to Car Loan tab, enter inputs (from TestData.xlsx), verify EMI / first-month interest / first-month principal against the formula, export summary to Excel.</td>
    </tr>
    <tr>
      <td><strong>Home Loan year-on-year</strong></td>
      <td><a href="src/test/resources/features/HomeLoanYearlySchedule.feature"><code>HomeLoanYearlySchedule.feature</code></a></td>
      <td>Navigate via menu to the dedicated Home Loan page, fill the form, extract the entire year-on-year amortisation table, persist to Excel via Apache POI.</td>
    </tr>
    <tr>
      <td><strong>Loan Calculator UI</strong></td>
      <td><a href="src/test/resources/features/LoanCalculatorUI.feature"><code>LoanCalculatorUI.feature</code></a></td>
      <td>Validate text boxes + sliders across the three sub-tabs (EMI / Loan Amount / Loan Tenure Calculator); toggle Year ↔ Month and assert the slider scale changes.</td>
    </tr>
  </tbody>
</table>

---

## 2. Quick Worked Example (Flow 1)

Principal = ₹15,00,000, Rate = 9.5% p.a., Tenure = 12 months:

<table>
  <thead>
    <tr>
      <th>Metric</th>
      <th>Value</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><strong>EMI</strong></td>
      <td><strong>₹1,31,524</strong></td>
    </tr>
    <tr>
      <td><strong>First month – Interest</strong></td>
      <td><strong>₹11,875</strong></td>
    </tr>
    <tr>
      <td><strong>First month – Principal</strong></td>
      <td><strong>₹1,19,649</strong></td>
    </tr>
    <tr>
      <td><strong>Total Interest (current year)</strong></td>
      <td><strong>₹78,288</strong></td>
    </tr>
  </tbody>
</table>

Tolerance: ±₹2 to absorb intermediate rounding on the site.

---

## 3. Tech Stack

<table>
  <thead>
    <tr>
      <th>Layer</th>
      <th>Choice</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>Language</td>
      <td>Java 17</td>
    </tr>
    <tr>
      <td>Build</td>
      <td>Maven 3.9+</td>
    </tr>
    <tr>
      <td>Browser driver</td>
      <td>Selenium 4.16 + built-in Selenium Manager</td>
    </tr>
    <tr>
      <td>BDD</td>
      <td>Cucumber 7 (Gherkin + Java step-defs)</td>
    </tr>
    <tr>
      <td>Runner</td>
      <td>TestNG 7 via <code>cucumber-testng</code></td>
    </tr>
    <tr>
      <td>DI for steps</td>
      <td>Cucumber PicoContainer</td>
    </tr>
    <tr>
      <td>Excel I/O</td>
      <td>Apache POI 5</td>
    </tr>
    <tr>
      <td>Reports</td>
      <td>Extent (custom thread-safe <code>ExtentManager</code>) · Allure · Cucumber HTML</td>
    </tr>
    <tr>
      <td>Logging</td>
      <td>Log4j 2 (rolling file, <strong>no</strong> gzip)</td>
    </tr>
    <tr>
      <td>Assertions</td>
      <td>AssertJ <code>SoftAssertions</code> + AssertJ</td>
    </tr>
    <tr>
      <td>CI</td>
      <td>Jenkins declarative pipeline (<code>Jenkinsfile</code>)</td>
    </tr>
  </tbody>
</table>

---

## 4. Folder Structure

```
EMI_Calculator_Hackathon_Project/
├── pom.xml
├── testng.xml                                 # single TestRunner, 2 <test> blocks (chrome/edge)
├── Jenkinsfile
├── README.md
├── .gitignore
│
├── docs/                                      # generated documentation
│   ├── Project_Documentation.xlsx             # User Stories / Scenarios / TC / RTM / Defects
│   ├── generate_docs.py
│   └── generate_testdata.py
│
├── src/
│   ├── main/java/com/hackathon/
│   │   ├── base/         BaseClass.java                # Chrome/Edge/Firefox + ThreadLocal driver
│   │   ├── config/       ConfigReader.java
│   │   ├── pages/        BasePage / HomePage / HomeLoanPage / LoanCalculatorPage
│   │   ├── reports/      ExtentManager.java            # thread-safe Extent
│   │   └── utils/        EMICalculatorUtil / ExcelUtils / ScreenshotUtils / TestDataReader
│   │
│   └── test/
│       ├── java/com/hackathon/
│       │   ├── context/         ScenarioContext.java     # per-scenario state + SoftAssertions
│       │   ├── hooks/           Hooks.java               # @Before/@After
│       │   ├── listeners/       TestListener.java        # suite-level cleanup + logging
│       │   ├── runners/         TestRunner.java          # only runner; extends BaseClass
│       │   └── stepdefinitions/ CarLoanSteps / HomeLoanSteps / LoanCalculatorSteps
│       │
│       └── resources/
│           ├── allure.properties
│           ├── config.properties                    # URLs, timeouts, output paths (NO test data)
│           ├── extent.properties                    # timestamped report toggle
│           ├── log4j2.xml
│           ├── features/                            # 3 .feature files (data-light, TC IDs only)
│           └── testdata/
│               └── TestData.xlsx                    # ← single source of all per-TC inputs
│
└── (gitignored runtime: logs/, output/, reports/, screenshots/, target/, .allure/)
```

---

## 5. How to Run

### Prerequisites

- JDK 17 on `PATH`
- Maven 3.9+ on `PATH`
- Chrome and Edge installed locally

### Run the full suite (Chrome + Edge in parallel)

```powershell
mvn clean test
```

### Headless (for CI)

```powershell
mvn clean test -Dheadless=true
```

### Run a tag subset

```powershell
mvn clean test "-Dcucumber.filter.tags=@CarLoan"
mvn clean test "-Dcucumber.filter.tags=@TC04"
mvn clean test "-Dcucumber.filter.tags=@Smoke"
```

### From the IDE

Right-click [`testng.xml`](testng.xml) → **Run As → TestNG Suite**. Two browser windows pop up at the same time, each runs all 10 scenarios, then closes.

---

## 6. Test Data in Excel

Per-TC inputs **do not** live in `config.properties` or in feature files — they live only in [`src/test/resources/testdata/TestData.xlsx`](src/test/resources/testdata/TestData.xlsx).

### Sheet `CarLoan`

<table>
  <thead>
    <tr>
      <th>TestCaseID</th>
      <th>LoanAmount</th>
      <th>InterestRate</th>
      <th>Tenure</th>
      <th>TenureUnit</th>
      <th>VerificationType</th>
      <th>Year</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>TC01</td>
      <td>1500000</td>
      <td>9.5</td>
      <td>1</td>
      <td>Yr</td>
      <td>emi</td>
      <td>2026</td>
    </tr>
    <tr>
      <td>TC02</td>
      <td>1500000</td>
      <td>9.5</td>
      <td>1</td>
      <td>Yr</td>
      <td>first_month_interest</td>
      <td>2026</td>
    </tr>
    <tr>
      <td>TC03</td>
      <td>1500000</td>
      <td>9.5</td>
      <td>1</td>
      <td>Yr</td>
      <td>first_month_principal</td>
      <td>2026</td>
    </tr>
    <tr>
      <td>TC04</td>
      <td>1500000</td>
      <td>9.5</td>
      <td>1</td>
      <td>Yr</td>
      <td>excel_export</td>
      <td>2026</td>
    </tr>
  </tbody>
</table>

### Sheet `HomeLoan`

<table>
  <thead>
    <tr>
      <th>TestCaseID</th>
      <th>LoanAmount</th>
      <th>InterestRate</th>
      <th>Tenure</th>
      <th>MinRows</th>
      <th>MenuItem</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>TC05</td>
      <td></td>
      <td></td>
      <td></td>
      <td></td>
      <td>Home Loan EMI Calculator</td>
    </tr>
    <tr>
      <td>TC06</td>
      <td>2500000</td>
      <td>8.5</td>
      <td>20</td>
      <td>10</td>
      <td></td>
    </tr>
  </tbody>
</table>

### Sheet `LoanCalculator`

<table>
  <thead>
    <tr>
      <th>TestCaseID</th>
      <th>SubTab</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>TC07</td>
      <td>EMI Calculator</td>
    </tr>
    <tr>
      <td>TC08</td>
      <td>EMI Calculator</td>
    </tr>
    <tr>
      <td>TC09</td>
      <td>Loan Amount Calculator</td>
    </tr>
    <tr>
      <td>TC10</td>
      <td>Loan Tenure Calculator</td>
    </tr>
  </tbody>
</table>

### How step defs read it

[`TestDataReader`](src/main/java/com/hackathon/utils/TestDataReader.java) caches rows by sheet + TC id. Step defs call `TestDataReader.get("CarLoan", "TC01")` and read the columns they need. To change an input — edit the spreadsheet, no code change required.

### Feature files now look like

```gherkin
Scenario Outline: <tc> - <description>
  Given I have car loan test data for "<tc>"
  When I select the Car Loan tab and enter the loan details from the test data
  Then the configured calculation should match the formula

  Examples:
    | tc   | description           |
    | TC01 | EMI value             |
    | TC02 | First month interest  |
    | TC03 | First month principal |
```

No hardcoded numbers anywhere in Gherkin.

---

## 7. Reports

Each `mvn test` (or `testng.xml` run) produces **three** HTML reports + one Excel artefact set:

<table>
  <thead>
    <tr>
      <th>Report</th>
      <th>Path</th>
      <th>Notes</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><strong>Extent (custom, thread-safe)</strong></td>
      <td><code>reports/extent/ExtentReport_<yyyyMMdd_HHmmss>.html</code></td>
      <td>Timestamped — previous runs preserved. Configured by <a href="src/test/resources/extent.properties"><code>extent.properties</code></a> + produced by <a href="src/main/java/com/hackathon/reports/ExtentManager.java"><code>ExtentManager</code></a>. Screenshots embedded via path.</td>
    </tr>
    <tr>
      <td><strong>Cucumber HTML</strong></td>
      <td><code>reports/cucumber/cucumber.html</code></td>
      <td>Self-contained, overwrites each run.</td>
    </tr>
    <tr>
      <td><strong>Allure</strong></td>
      <td><code>target/allure-results/</code> raw → run <code>mvn allure:report</code> → <code>target/allure-report/index.html</code></td>
      <td>Cleaned at suite start so each run shows exactly its own scenarios.</td>
    </tr>
    <tr>
      <td><strong>Excel outputs</strong></td>
      <td><code>output/CarLoan_EMI_Summary.xlsx</code>, <code>output/HomeLoan_YearlySchedule.xlsx</code></td>
      <td>Produced via Apache POI by TC04 / TC06.</td>
    </tr>
    <tr>
      <td><strong>Logs</strong></td>
      <td><code>logs/automation.log</code></td>
      <td>Log4j rolling file; rolled files <strong>not</strong> gzipped.</td>
    </tr>
  </tbody>
</table>

### Generate the Allure HTML

```powershell
mvn allure:report          # static HTML at target/allure-report/index.html
mvn allure:serve           # opens it in a local web server
```

### Why we don't use `ExtentCucumberAdapter`

`tech.grasshopper:extentreports-cucumber7-adapter` v1.14.0 races on a shared Gson `LinkedTreeMap` in `setGherkinDialect()` under parallel runs and intermittently throws `java.lang.AssertionError`. Our `ExtentManager` drives `ExtentReports` directly from Cucumber hooks — thread-safe, timestamped output, no adapter.

---

## 8. Screenshots

Saved to **`screenshots/`** at project root. Filename pattern:

```
<TCid>_<scenario-slug>_<PASSED|FAILED>_<browser>_<yyyyMMdd_HHmmss_SSS>.png
```

Example: `TC01_EMI_value_PASSED_chrome_20260521_153012_287.png`

- Captured for **every** scenario (pass or fail) by [`Hooks.@After`](src/test/java/com/hackathon/hooks/Hooks.java).
- Embedded inline in the Extent report by file path.
- Folder is wiped at the start of every run by `TestListener.onStart(ISuite)` so stale captures don't accumulate.

---

## 9. Parallel Execution and ThreadLocal

- [`testng.xml`](testng.xml) declares `parallel="tests"` with two `<test>` blocks (`browser=chrome` and `browser=edge`). Each runs in its own thread.
- [`BaseClass.driver`](src/main/java/com/hackathon/base/BaseClass.java) is a `ThreadLocal<WebDriver>` — each runner thread keeps its own browser instance so they don't fight over a shared driver.
- Browser opens **once per runner** in `@BeforeClass` and closes in `@AfterClass` — both runner threads open at the same time, share the wall clock, and finish in roughly half the time of sequential execution.

---

## 10. Multi-Browser Support

`BaseClass.launchBrowser(String browser)` supports three browsers:

- `chrome`
- `edge`
- `firefox`

Currently `testng.xml` runs Chrome and Edge in parallel; Firefox is left commented out per hackathon requirement (point p). To enable Firefox, uncomment the third `<test>` block in `testng.xml`.

---

## 11. Soft Assertions

Every step-def assertion uses AssertJ's `SoftAssertions` so a single scenario reports **all** its failures, not just the first:

```java
ctx.softly.assertThat(actual)
        .as("EMI: expected %d ± %d, got %d", expected, tol, actual)
        .isLessThanOrEqualTo(tol);
```

- `ScenarioContext.softly` is freshly constructed per scenario by PicoContainer.
- `Hooks.@After` calls `ctx.softly.assertAll()` at the end of the scenario — that's what fails the scenario in Cucumber/Allure/Extent if any assertion was queued.

---

## 12. CI CD Pipeline

### Git workflow

```powershell
git clone https://github.com/BatchuMamatha/EMI_Calculator_Hackathon_Project.git
cd EMI_Calculator_Hackathon_Project
mvn clean test
```

After local edits:

```powershell
git add <files>
git commit -m "msg"
git push
```

### Jenkins

[`Jenkinsfile`](Jenkinsfile) declares the pipeline:

```
Checkout → Build → Test (Chrome + Edge in parallel) → Publish Reports → Archive Artefacts
```

Set up in Jenkins:
1. Create a new **Pipeline** job, point it at this GitHub repo.
2. Register tools in **Global Tool Configuration**: JDK 17 named `JDK17`, Maven 3 named `Maven3`, optionally Allure Commandline named `Allure`.
3. Ensure the agent has Chrome + Edge installed (Selenium Manager downloads drivers automatically).
4. Optionally add a GitHub webhook for push-triggered builds.

Parameters exposed in the pipeline:
- `TAG` — Cucumber tag expression
- `HEADLESS` — boolean, default `true` on CI

---

## 13. Test Plan and Strategy

### Objective

Verify three flows on `emicalculator.net` are functionally correct and the UI is operable across two browsers in parallel.

### Scope

<table>
  <thead>
    <tr>
      <th>In</th>
      <th>Out</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>Car / Home / Loan-Calculator flows</td>
      <td>Personal Loan, Credit Card EMI</td>
    </tr>
    <tr>
      <td>EMI maths, first-month split</td>
      <td>Tax & insurance overlays</td>
    </tr>
    <tr>
      <td>Year-on-year schedule extract</td>
      <td>Prepayment scenarios</td>
    </tr>
    <tr>
      <td>UI sanity on 3 sub-calculators</td>
      <td>Responsive / mobile viewport</td>
    </tr>
    <tr>
      <td>Chrome + Edge in parallel</td>
      <td>Firefox, Safari</td>
    </tr>
    <tr>
      <td>Functional + Integration (Excel)</td>
      <td>Performance, accessibility</td>
    </tr>
  </tbody>
</table>

### Entry criteria

- JDK 17, Maven 3.9+ installed
- Chrome and Edge installed
- Network access to `emicalculator.net`

### Exit criteria

- All 20 tests pass on a single `mvn clean test`
- Excel artefacts present in `output/`
- Extent / Allure / Cucumber HTML reports produced

### Approach (hybrid framework)

- **BDD / keyword-driven** — Gherkin phrases like `Given I have car loan test data for "TC01"` are themselves keywords mapped to Java step-defs.
- **Data-driven** — all per-TC inputs live in `TestData.xlsx` and are loaded by `TestDataReader`.
- **POM** — page objects under `com.hackathon.pages` using `@FindBy`.
- **Multi-browser** — Chrome + Edge launched in parallel via TestNG, isolated by `ThreadLocal<WebDriver>`.

---

## 14. Business Requirements

Borrowers and finance teams using `emicalculator.net` need confidence that:

- The EMI shown matches the standard EMI formula
- The per-month split is correct in the very first instalment
- The year-by-year amortisation schedule can be exported for offline analysis
- The Loan Calculator's UI remains operable across all three sub-calculators

Functional requirements (mapped to RTM in `Project_Documentation.xlsx`):

<table>
  <thead>
    <tr>
      <th>ID</th>
      <th>Requirement</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>FR-001</td>
      <td>Car loan EMI shall match the formula within ±₹2</td>
    </tr>
    <tr>
      <td>FR-002</td>
      <td>First month interest = P × monthly rate; first month principal = EMI − interest</td>
    </tr>
    <tr>
      <td>FR-003</td>
      <td>Car loan summary shall export to Excel via Apache POI</td>
    </tr>
    <tr>
      <td>FR-004</td>
      <td>Home Loan calculator shall be reachable from the top menu</td>
    </tr>
    <tr>
      <td>FR-005</td>
      <td>Year-on-year schedule shall be extractable and storable to Excel</td>
    </tr>
    <tr>
      <td>FR-006</td>
      <td>EMI Calculator sub-tab inputs and sliders shall be operable</td>
    </tr>
    <tr>
      <td>FR-007</td>
      <td>Switching tenure Year ↔ Month shall change the slider scale</td>
    </tr>
    <tr>
      <td>FR-008</td>
      <td>Same UI validation shall be reusable across all 3 sub-calculators</td>
    </tr>
    <tr>
      <td>FR-009</td>
      <td>Suite shall execute in parallel on Chrome and Edge</td>
    </tr>
    <tr>
      <td>FR-010</td>
      <td>Failures shall capture a screenshot attached to the report</td>
    </tr>
  </tbody>
</table>

---

## 15. Test Scenarios, Test Cases, RTM, Defects

All five tabs (UserStories, TestScenarios, TestCases, RTM, Defects) are maintained in [`docs/Project_Documentation.xlsx`](docs/Project_Documentation.xlsx) (matching the Finding_Hospital reference template style).

### 10 test cases at a glance

<table>
  <thead>
    <tr>
      <th>TC</th>
      <th>Feature</th>
      <th>Description</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>TC01</td>
      <td>CarLoanEMI</td>
      <td>EMI value calculation</td>
    </tr>
    <tr>
      <td>TC02</td>
      <td>CarLoanEMI</td>
      <td>First month interest</td>
    </tr>
    <tr>
      <td>TC03</td>
      <td>CarLoanEMI</td>
      <td>First month principal</td>
    </tr>
    <tr>
      <td>TC04</td>
      <td>CarLoanEMI</td>
      <td>Export car loan summary to Excel</td>
    </tr>
    <tr>
      <td>TC05</td>
      <td>HomeLoan</td>
      <td>Menu navigation to Home Loan Calculator</td>
    </tr>
    <tr>
      <td>TC06</td>
      <td>HomeLoan</td>
      <td>Year-on-year schedule extraction to Excel</td>
    </tr>
    <tr>
      <td>TC07</td>
      <td>LoanCalc</td>
      <td>EMI Calculator – UI sanity</td>
    </tr>
    <tr>
      <td>TC08</td>
      <td>LoanCalc</td>
      <td>Tenure unit toggle changes slider scale</td>
    </tr>
    <tr>
      <td>TC09</td>
      <td>LoanCalc</td>
      <td>UI validation reused on Loan Amount Calculator</td>
    </tr>
    <tr>
      <td>TC10</td>
      <td>LoanCalc</td>
      <td>UI validation reused on Loan Tenure Calculator</td>
    </tr>
  </tbody>
</table>

Each TC runs on **two browsers** → **20 total tests** per suite execution.

---

## 16. Hackathon Requirements Checklist

<table>
  <thead>
    <tr>
      <th>#</th>
      <th>Item</th>
      <th>Where</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>a</td>
      <td>Maven</td>
      <td><a href="pom.xml"><code>pom.xml</code></a></td>
    </tr>
    <tr>
      <td>b</td>
      <td>Proper folder structure</td>
      <td>§4</td>
    </tr>
    <tr>
      <td>c</td>
      <td>Apache POI</td>
      <td><a href="src/main/java/com/hackathon/utils/ExcelUtils.java"><code>ExcelUtils</code></a> + <a href="src/main/java/com/hackathon/utils/TestDataReader.java"><code>TestDataReader</code></a></td>
    </tr>
    <tr>
      <td>d</td>
      <td>POM Design Pattern</td>
      <td><a href="src/main/java/com/hackathon/pages"><code>pages/</code></a> — BasePage + 3 concrete pages with <code>@FindBy</code></td>
    </tr>
    <tr>
      <td>e</td>
      <td>Data-driven + Keyword-driven (Hybrid)</td>
      <td>Gherkin keywords + Excel-driven data via <code>TestDataReader</code></td>
    </tr>
    <tr>
      <td>f</td>
      <td>TestNG</td>
      <td><a href="testng.xml"><code>testng.xml</code></a>, <code>AbstractTestNGCucumberTests</code> runner</td>
    </tr>
    <tr>
      <td>g</td>
      <td>Exception handling</td>
      <td>try/with-resources in POI, defensive catches in <code>BasePage</code>/<code>Hooks</code></td>
    </tr>
    <tr>
      <td>h</td>
      <td>Different locator techniques</td>
      <td>id, name, css, xpath, linkText — see <code>HomePage</code> / <code>LoanCalculatorPage</code></td>
    </tr>
    <tr>
      <td>i</td>
      <td>Screenshot capture</td>
      <td><a href="src/main/java/com/hackathon/utils/ScreenshotUtils.java"><code>ScreenshotUtils</code></a>, one per scenario, attached to Extent</td>
    </tr>
    <tr>
      <td>j</td>
      <td>Customised HTML reports</td>
      <td>Extent (<code>ExtentManager</code> + <code>extent.properties</code>) + Allure + Cucumber HTML</td>
    </tr>
    <tr>
      <td>k</td>
      <td>End-to-end execution</td>
      <td><code>mvn clean test</code> runs all 20</td>
    </tr>
    <tr>
      <td>l</td>
      <td>Multiple browser execution</td>
      <td>Chrome + Edge in parallel (Firefox commented per requirement p)</td>
    </tr>
    <tr>
      <td>m</td>
      <td>Listeners</td>
      <td><a href="src/test/java/com/hackathon/listeners/TestListener.java"><code>TestListener</code></a> (suite/test level) + Cucumber <a href="src/test/java/com/hackathon/hooks/Hooks.java"><code>Hooks</code></a></td>
    </tr>
    <tr>
      <td>o</td>
      <td>Log4j</td>
      <td><a href="src/test/resources/log4j2.xml"><code>log4j2.xml</code></a> — rolling file, no gzip</td>
    </tr>
    <tr>
      <td>p</td>
      <td>Parallel test execution (Chrome + Edge, no Firefox)</td>
      <td><code>testng.xml parallel="tests"</code>, Firefox <code><test></code> block commented</td>
    </tr>
    <tr>
      <td>q</td>
      <td>Assertions</td>
      <td>AssertJ <code>SoftAssertions</code> everywhere in step defs</td>
    </tr>
    <tr>
      <td>-</td>
      <td>CI/CD (Git/GitHub/Jenkins)</td>
      <td><code>.gitignore</code>, <a href="Jenkinsfile"><code>Jenkinsfile</code></a></td>
    </tr>
    <tr>
      <td>-</td>
      <td>Test Plan + Strategy</td>
      <td>§13</td>
    </tr>
    <tr>
      <td>-</td>
      <td>BRD</td>
      <td>§14</td>
    </tr>
    <tr>
      <td>-</td>
      <td>Test Scenarios / RTM / Defects</td>
      <td><a href="docs/Project_Documentation.xlsx"><code>docs/Project_Documentation.xlsx</code></a></td>
    </tr>
  </tbody>
</table>

---

## 17. Troubleshooting

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
      <td>Goal is singular: <code>mvn allure:report</code></td>
    </tr>
    <tr>
      <td>Allure shows more tests than were actually run</td>
      <td>Stale results in <code>target/allure-results/</code>. <code>TestListener.onStart</code> wipes it — make sure you're on the latest code. Or just run <code>mvn clean test</code>.</td>
    </tr>
    <tr>
      <td>Only Chrome opens, Edge doesn't</td>
      <td>Make sure you're running <code>testng.xml</code> (not a single class). Check <code>parallel="tests"</code> is intact in the suite XML.</td>
    </tr>
    <tr>
      <td><code>element click intercepted</code> on tenure radio</td>
      <td>Already mitigated — locators target the wrapping <code><label></code> instead of the hidden <code><input></code>.</td>
    </tr>
    <tr>
      <td>Excel <code>~$TestData.xlsx</code> lock file in repo</td>
      <td><code>~$*.xlsx</code> is in <code>.gitignore</code>. Close Excel before running <code>git add</code>.</td>
    </tr>
    <tr>
      <td><code>target/generated-sources</code> keeps coming back</td>
      <td><code>mvn clean</code> resets target/. The pom redirects generated sources to existing dirs so empty folders aren't created on Maven CLI builds; Eclipse builds may still create them — harmless.</td>
    </tr>
    <tr>
      <td><code>automation-*.log.gz</code> file in <code>logs/</code></td>
      <td>No longer produced — <code>log4j2.xml</code> writes plain <code>.log</code> files now.</td>
    </tr>
    <tr>
      <td>Extent report has both <code>SparkReport.html</code> and <code>ExtentReport_*.html</code></td>
      <td>Old code wrote <code>SparkReport.html</code>. The current code only writes timestamped <code>ExtentReport_<ts>.html</code>. <code>TestListener</code> wipes the folder on each run so legacy files are removed.</td>
    </tr>
  </tbody>
</table>

---

**Author:** Team SKRUM — Cohort INTQEA26QE003
**Last Updated:** 2026-05-21
