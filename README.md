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
6. [Test Data — single source in Excel](#6-test-data--single-source-in-excel)
7. [Reports](#7-reports)
8. [Screenshots](#8-screenshots)
9. [Parallel Execution and ThreadLocal](#9-parallel-execution-and-threadlocal)
10. [Multi-Browser Support](#10-multi-browser-support)
11. [Soft Assertions](#11-soft-assertions)
12. [CI/CD — Git, GitHub, Jenkins](#12-cicd--git-github-jenkins)
13. [Test Plan and Strategy](#13-test-plan-and-strategy)
14. [Business Requirements](#14-business-requirements)
15. [Test Scenarios, Test Cases, RTM, Defects](#15-test-scenarios-test-cases-rtm-defects)
16. [Hackathon Requirements Checklist](#16-hackathon-requirements-checklist)
17. [Troubleshooting](#17-troubleshooting)

---

## 1. Three End-to-End Flows

| Flow | Feature file | What it does |
|---|---|---|
| **Car Loan EMI** | [`CarLoanEMI.feature`](src/test/resources/features/CarLoanEMI.feature) | Switch to Car Loan tab, enter inputs (from TestData.xlsx), verify EMI / first-month interest / first-month principal against the formula, export summary to Excel. |
| **Home Loan year-on-year** | [`HomeLoanYearlySchedule.feature`](src/test/resources/features/HomeLoanYearlySchedule.feature) | Navigate via menu to the dedicated Home Loan page, fill the form, extract the entire year-on-year amortisation table, persist to Excel via Apache POI. |
| **Loan Calculator UI** | [`LoanCalculatorUI.feature`](src/test/resources/features/LoanCalculatorUI.feature) | Validate text boxes + sliders across the three sub-tabs (EMI / Loan Amount / Loan Tenure Calculator); toggle Year ↔ Month and assert the slider scale changes. |

---

## 2. Quick Worked Example (Flow 1)

Principal = ₹15,00,000, Rate = 9.5% p.a., Tenure = 12 months:

| Metric | Value |
|---|---|
| **EMI** | **₹1,31,524** |
| **First month – Interest** | **₹11,875** |
| **First month – Principal** | **₹1,19,649** |
| **Total Interest (current year)** | **₹78,288** |

Tolerance: ±₹2 to absorb intermediate rounding on the site.

---

## 3. Tech Stack

| Layer | Choice |
|---|---|
| Language | Java 17 |
| Build | Maven 3.9+ |
| Browser driver | Selenium 4.16 + built-in Selenium Manager |
| BDD | Cucumber 7 (Gherkin + Java step-defs) |
| Runner | TestNG 7 via `cucumber-testng` |
| DI for steps | Cucumber PicoContainer |
| Excel I/O | Apache POI 5 |
| Reports | Extent (custom thread-safe `ExtentManager`) · Allure · Cucumber HTML |
| Logging | Log4j 2 (rolling file, **no** gzip) |
| Assertions | AssertJ `SoftAssertions` + AssertJ |
| CI | Jenkins declarative pipeline (`Jenkinsfile`) |

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

## 6. Test Data — single source in Excel

Per-TC inputs **do not** live in `config.properties` or in feature files — they live only in [`src/test/resources/testdata/TestData.xlsx`](src/test/resources/testdata/TestData.xlsx).

### Sheet `CarLoan`

| TestCaseID | LoanAmount | InterestRate | Tenure | TenureUnit | VerificationType | Year |
|---|---|---|---|---|---|---|
| TC01 | 1500000 | 9.5 | 1 | Yr | emi | 2026 |
| TC02 | 1500000 | 9.5 | 1 | Yr | first_month_interest | 2026 |
| TC03 | 1500000 | 9.5 | 1 | Yr | first_month_principal | 2026 |
| TC04 | 1500000 | 9.5 | 1 | Yr | excel_export | 2026 |

### Sheet `HomeLoan`

| TestCaseID | LoanAmount | InterestRate | Tenure | MinRows | MenuItem |
|---|---|---|---|---|---|
| TC05 |  |  |  |  | Home Loan EMI Calculator |
| TC06 | 2500000 | 8.5 | 20 | 10 |  |

### Sheet `LoanCalculator`

| TestCaseID | SubTab |
|---|---|
| TC07 | EMI Calculator |
| TC08 | EMI Calculator |
| TC09 | Loan Amount Calculator |
| TC10 | Loan Tenure Calculator |

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

| Report | Path | Notes |
|---|---|---|
| **Extent (custom, thread-safe)** | `reports/extent/ExtentReport_<yyyyMMdd_HHmmss>.html` | Timestamped — previous runs preserved. Configured by [`extent.properties`](src/test/resources/extent.properties) + produced by [`ExtentManager`](src/main/java/com/hackathon/reports/ExtentManager.java). Screenshots embedded via path. |
| **Cucumber HTML** | `reports/cucumber/cucumber.html` | Self-contained, overwrites each run. |
| **Allure** | `target/allure-results/` raw → run `mvn allure:report` → `target/allure-report/index.html` | Cleaned at suite start so each run shows exactly its own scenarios. |
| **Excel outputs** | `output/CarLoan_EMI_Summary.xlsx`, `output/HomeLoan_YearlySchedule.xlsx` | Produced via Apache POI by TC04 / TC06. |
| **Logs** | `logs/automation.log` | Log4j rolling file; rolled files **not** gzipped. |

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

## 12. CI/CD — Git, GitHub, Jenkins

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

| In | Out |
|---|---|
| Car / Home / Loan-Calculator flows | Personal Loan, Credit Card EMI |
| EMI maths, first-month split | Tax & insurance overlays |
| Year-on-year schedule extract | Prepayment scenarios |
| UI sanity on 3 sub-calculators | Responsive / mobile viewport |
| Chrome + Edge in parallel | Firefox, Safari |
| Functional + Integration (Excel) | Performance, accessibility |

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

| ID | Requirement |
|---|---|
| FR-001 | Car loan EMI shall match the formula within ±₹2 |
| FR-002 | First month interest = P × monthly rate; first month principal = EMI − interest |
| FR-003 | Car loan summary shall export to Excel via Apache POI |
| FR-004 | Home Loan calculator shall be reachable from the top menu |
| FR-005 | Year-on-year schedule shall be extractable and storable to Excel |
| FR-006 | EMI Calculator sub-tab inputs and sliders shall be operable |
| FR-007 | Switching tenure Year ↔ Month shall change the slider scale |
| FR-008 | Same UI validation shall be reusable across all 3 sub-calculators |
| FR-009 | Suite shall execute in parallel on Chrome and Edge |
| FR-010 | Failures shall capture a screenshot attached to the report |

---

## 15. Test Scenarios, Test Cases, RTM, Defects

All five tabs (UserStories, TestScenarios, TestCases, RTM, Defects) are maintained in [`docs/Project_Documentation.xlsx`](docs/Project_Documentation.xlsx) (matching the Finding_Hospital reference template style).

### 10 test cases at a glance

| TC | Feature | Description |
|---|---|---|
| TC01 | CarLoanEMI | EMI value calculation |
| TC02 | CarLoanEMI | First month interest |
| TC03 | CarLoanEMI | First month principal |
| TC04 | CarLoanEMI | Export car loan summary to Excel |
| TC05 | HomeLoan | Menu navigation to Home Loan Calculator |
| TC06 | HomeLoan | Year-on-year schedule extraction to Excel |
| TC07 | LoanCalc | EMI Calculator – UI sanity |
| TC08 | LoanCalc | Tenure unit toggle changes slider scale |
| TC09 | LoanCalc | UI validation reused on Loan Amount Calculator |
| TC10 | LoanCalc | UI validation reused on Loan Tenure Calculator |

Each TC runs on **two browsers** → **20 total tests** per suite execution.

---

## 16. Hackathon Requirements Checklist

| # | Item | Where |
|---|---|---|
| a | Maven | [`pom.xml`](pom.xml) |
| b | Proper folder structure | §4 |
| c | Apache POI | [`ExcelUtils`](src/main/java/com/hackathon/utils/ExcelUtils.java) + [`TestDataReader`](src/main/java/com/hackathon/utils/TestDataReader.java) |
| d | POM Design Pattern | [`pages/`](src/main/java/com/hackathon/pages) — BasePage + 3 concrete pages with `@FindBy` |
| e | Data-driven + Keyword-driven (Hybrid) | Gherkin keywords + Excel-driven data via `TestDataReader` |
| f | TestNG | [`testng.xml`](testng.xml), `AbstractTestNGCucumberTests` runner |
| g | Exception handling | try/with-resources in POI, defensive catches in `BasePage`/`Hooks` |
| h | Different locator techniques | id, name, css, xpath, linkText — see `HomePage` / `LoanCalculatorPage` |
| i | Screenshot capture | [`ScreenshotUtils`](src/main/java/com/hackathon/utils/ScreenshotUtils.java), one per scenario, attached to Extent |
| j | Customised HTML reports | Extent (`ExtentManager` + `extent.properties`) + Allure + Cucumber HTML |
| k | End-to-end execution | `mvn clean test` runs all 20 |
| l | Multiple browser execution | Chrome + Edge in parallel (Firefox commented per requirement p) |
| m | Listeners | [`TestListener`](src/test/java/com/hackathon/listeners/TestListener.java) (suite/test level) + Cucumber [`Hooks`](src/test/java/com/hackathon/hooks/Hooks.java) |
| o | Log4j | [`log4j2.xml`](src/test/resources/log4j2.xml) — rolling file, no gzip |
| p | Parallel test execution (Chrome + Edge, no Firefox) | `testng.xml parallel="tests"`, Firefox `<test>` block commented |
| q | Assertions | AssertJ `SoftAssertions` everywhere in step defs |
| - | CI/CD (Git/GitHub/Jenkins) | `.gitignore`, [`Jenkinsfile`](Jenkinsfile) |
| - | Test Plan + Strategy | §13 |
| - | BRD | §14 |
| - | Test Scenarios / RTM / Defects | [`docs/Project_Documentation.xlsx`](docs/Project_Documentation.xlsx) |

---

## 17. Troubleshooting

| Symptom | Fix |
|---|---|
| `MojoNotFoundException: allure:reports` | Goal is singular: `mvn allure:report` |
| Allure shows more tests than were actually run | Stale results in `target/allure-results/`. `TestListener.onStart` wipes it — make sure you're on the latest code. Or just run `mvn clean test`. |
| Only Chrome opens, Edge doesn't | Make sure you're running `testng.xml` (not a single class). Check `parallel="tests"` is intact in the suite XML. |
| `element click intercepted` on tenure radio | Already mitigated — locators target the wrapping `<label>` instead of the hidden `<input>`. |
| Excel `~$TestData.xlsx` lock file in repo | `~$*.xlsx` is in `.gitignore`. Close Excel before running `git add`. |
| `target/generated-sources` keeps coming back | `mvn clean` resets target/. The pom redirects generated sources to existing dirs so empty folders aren't created on Maven CLI builds; Eclipse builds may still create them — harmless. |
| `automation-*.log.gz` file in `logs/` | No longer produced — `log4j2.xml` writes plain `.log` files now. |
| Extent report has both `SparkReport.html` and `ExtentReport_*.html` | Old code wrote `SparkReport.html`. The current code only writes timestamped `ExtentReport_<ts>.html`. `TestListener` wipes the folder on each run so legacy files are removed. |

---

**Author:** Batchu Mamatha — Cohort INTQEA26QE003
**Last Updated:** 2026-05-21
