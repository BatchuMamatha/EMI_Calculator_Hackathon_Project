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
6. [Test Scenarios & Test Cases](#6-test-scenarios--test-cases)
7. [Requirements Traceability Matrix (RTM)](#7-requirements-traceability-matrix-rtm)
8. [Manual Testing & Defects](#8-manual-testing--defects)
9. [Automation Framework Architecture](#9-automation-framework-architecture)
10. [Technology Stack](#10-technology-stack)
11. [Folder Structure](#11-folder-structure)
12. [How to Run](#12-how-to-run)
13. [Reports](#13-reports)
14. [CI/CD with Jenkins, Git, GitHub](#14-cicd-with-jenkins-git-github)
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

| Stakeholder | Interest |
|---|---|
| Car/Home loan applicants | Accurate EMI numbers before signing |
| Finance team analysts | Excel exports for sharing with stakeholders |
| QA Lead (Cohort INTQEA26QE003) | A demonstrable, repeatable automation suite |
| QA Team | Reusable framework for future calculator validations |
| CI/CD Owner | Push-triggered Jenkins runs with reports archived |

### 2.3 In-Scope Functional Requirements

| ID  | Requirement |
|-----|-------------|
| R01 | Car loan EMI shall match the EMI formula within ±₹2 tolerance |
| R02 | First month interest = Principal × monthly rate; first month principal = EMI − first month interest |
| R03 | Car loan EMI summary shall be exportable to Excel via Apache POI |
| R04 | Home Loan EMI Calculator shall be reachable from the top menu |
| R05 | The year-on-year payment schedule shall be extractable and storable to Excel |
| R06 | EMI Calculator sub-tab's text boxes and sliders shall be operable |
| R07 | Switching tenure Year ↔ Month shall change the slider scale |
| R08 | The same UI validation shall be reusable across all 3 sub-calculators |
| R09 | The suite shall execute in parallel on Chrome and Edge (Firefox excluded) |
| R10 | Failures shall capture a screenshot attached to the test report |

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

| Metric                        | Value         |
| ----------------------------- | ------------- |
| EMI                           | **₹1,31,524** |
| First month — Interest        | **₹11,875**   |
| First month — Principal       | **₹1,19,649** |
| Total Interest (current year) | **₹78,288**   |

(Tolerance: ±₹2 to absorb intermediate rounding on the site.)

---

## 4. Test Plan

### 4.1 Objective

Verify the correctness, reliability and cross-browser stability of the three
selected emicalculator.net flows and demonstrate every required automation
framework component.

### 4.2 Scope

| In scope | Out of scope |
|---|---|
| Car / Home / Loan-Calculator flows | Personal loan, Credit Card EMI |
| EMI formula correctness, first-month split | Tax & insurance overlays |
| Year-on-year schedule extraction | Prepayment-modified schedules |
| UI sanity on 3 sub-calculators | Mobile / responsive viewport tests |
| Chrome + Edge parallel | Firefox, Safari |
| Functional + integration (Excel I/O) | Performance, accessibility |

### 4.3 Test Deliverables

| Deliverable | Location |
|---|---|
| Test Plan (this document) | [`README.md`](README.md) |
| Test Strategy | This README §5 |
| BRD | This README §2 |
| Test Scenarios + Test Cases + RTM + Defects (Excel) | [`docs/Project_Documentation.xlsx`](docs/Project_Documentation.xlsx) |
| Feature files (BDD) | [`src/test/resources/features/`](src/test/resources/features) |
| Allure report | `target/allure-report/index.html` |
| Extent Spark report | `reports/extent/SparkReport.html` |
| Cucumber HTML reports | `reports/cucumber/{chrome,edge}-cucumber.html` |
| Generated Excel artefacts | `output/*.xlsx` |
| Log files | `logs/automation.log` |
| Screenshots on failure | `reports/extent/screenshots/` |
| Jenkins pipeline definition | [`Jenkinsfile`](Jenkinsfile) |

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

| Risk | Mitigation |
|---|---|
| Site DOM changes break locators | Use multiple locator strategies + `@FindBy` proxies that re-resolve on each call |
| Ad blocks shift the schedule table | Explicit waits + `scrollBy(0,800)` before extraction |
| Network flakiness | Selenium Manager caches drivers; explicit timeouts in `config.properties` |
| Browser-driver version mismatch | Selenium 4.16+ uses Selenium Manager — auto-downloads correct driver |
| Parallel-execution race conditions | `ThreadLocal<WebDriver>` in `DriverFactory`; custom thread-safe `ExtentManager` |

---

## 5. Test Strategy

### 5.1 Testing Types

| Type | Coverage |
|---|---|
| Functional | EMI maths, first-month split, schedule extraction |
| Integration | Apache POI Excel writes, file persistence |
| UI sanity | Text boxes operable, sliders rendered, tab switching |
| Cross-browser | Parallel Chrome + Edge |
| Regression | Tagged `@Regression` for selective runs (`-Dcucumber.filter.tags="@Regression"`) |
| Smoke | Tagged `@Smoke` for fast pre-merge checks |

### 5.2 Test Approach

- **Hybrid framework** — BDD scenarios (keyword-driven) + parametrised inputs (data-driven) + Page Object Model (POM) for maintainability
- **Independent computation** — `EMICalculatorUtil` calculates the expected EMI in pure Java; the test compares it against the value rendered on the site, so we catch UI bugs *and* maths bugs
- **Locator diversity** — id, name, css, xpath, linkText, partialLinkText all used across page objects (satisfies "different locator techniques")
- **No raw `By` declarations** — every locator lives on a `@FindBy` annotated WebElement field (PageFactory pattern); dynamic locators use JavaScript queries
- **One browser per runner** — opened in `@BeforeClass`, closed in `@AfterClass`; scenarios reuse it, only cookies are cleared between scenarios

### 5.3 Test Environment

| Component | Value |
|---|---|
| OS | Windows 11 |
| JDK | 17 (LTS) |
| Maven | 3.9+ |
| Browsers | Chrome 140+, Edge 140+ |
| Headless mode | Toggleable via `-Dheadless=true` |

### 5.4 Tools

| Tool | Purpose |
|---|---|
| Selenium 4 + Selenium Manager | Browser automation; auto-downloads driver binaries |
| Cucumber 7 (Java) | BDD layer, Gherkin parsing |
| TestNG 7 | Test orchestration, parallel execution, listeners |
| Apache POI 5 | Excel read/write |
| ExtentReports 5 + custom adapter | Customised HTML report (Spark theme) |
| Allure 2 | Customised HTML report (alternative) |
| Log4j 2 | Structured logging, rolling file appender |
| AssertJ + TestNG Assert | Assertions |
| AspectJ Weaver | Allure step interception |
| Maven Surefire | Test runner integration |
| Jenkins + Git + GitHub | CI/CD pipeline |

### 5.5 Reporting Cadence

- Each `mvn test` run produces fresh Cucumber HTML, Extent Spark, and Allure raw results
- Allure HTML is generated on demand via `mvn allure:report`
- Jenkins archives all reports per build and renders them via the HTML Publisher / Allure plugin

---

## 6. Test Scenarios & Test Cases

Detailed Test Scenarios and Test Cases are maintained in
[`docs/Project_Documentation.xlsx`](docs/Project_Documentation.xlsx)
(sheets `TestScenarios` and `TestCases`).

### 6.1 Test Scenarios (10)

| Module | ID | Scenario Title |
|---|---|---|
| Car Loan | TS01 | Verify EMI calculation for 15L / 9.5% / 1yr |
| Car Loan | TS02 | Verify first month interest amount |
| Car Loan | TS03 | Verify first month principal amount |
| Car Loan | TS04 | Export car loan EMI summary to Excel |
| Home Loan | TS05 | Navigate to Home Loan Calculator via top menu |
| Home Loan | TS06 | Extract year-on-year schedule and store in Excel |
| Loan Calculator | TS07 | EMI Calculator UI inputs and sliders operable |
| Loan Calculator | TS08 | Tenure unit toggle changes slider scale |
| Loan Calculator | TS09 | Reuse same UI validation on Loan Amount Calculator |
| Loan Calculator | TS10 | Reuse same UI validation on Loan Tenure Calculator |

### 6.2 Test Cases (10) — TC01 to TC10

| TC | Feature | Scenario |
|---|---|---|
| TC01 | `CarLoanEMI.feature` | EMI for 15L/9.5%/1yr is correct |
| TC02 | `CarLoanEMI.feature` | First month interest matches formula |
| TC03 | `CarLoanEMI.feature` | First month principal matches formula |
| TC04 | `CarLoanEMI.feature` | Car loan summary exported to Excel |
| TC05 | `HomeLoanYearlySchedule.feature` | Menu navigation to Home Loan Calculator |
| TC06 | `HomeLoanYearlySchedule.feature` | Year-on-year schedule extracted and stored to Excel |
| TC07 | `LoanCalculatorUI.feature` | EMI Calc — text boxes + sliders operable |
| TC08 | `LoanCalculatorUI.feature` | Tenure Year↔Month flip changes slider scale |
| TC09 | `LoanCalculatorUI.feature` | Same validation reused on Loan Amount Calculator |
| TC10 | `LoanCalculatorUI.feature` | Same validation reused on Loan Tenure Calculator |

Each TC in the Excel document has detailed step-level columns (Step #, Step
Description, Step Expected Results, Actual Result, Status, Defect).

---

## 7. Requirements Traceability Matrix (RTM)

Full RTM is maintained in [`docs/Project_Documentation.xlsx`](docs/Project_Documentation.xlsx) → `RTM` sheet. Summary:

| # | Feature | Req ID | Test Scenario | Test Case | Status |
|---|---|---|---|---|---|
| 1 | F01 | R01 | TS01 | TC01 | Pass |
| 2 | F01 | R02 | TS02 | TC02 | Pass |
| 3 | F01 | R02 | TS03 | TC03 | Pass |
| 4 | F01 | R03 | TS04 | TC04 | Pass |
| 5 | F02 | R04 | TS05 | TC05 | Pass |
| 6 | F02 | R05 | TS06 | TC06 | Pass |
| 7 | F03 | R06 | TS07 | TC07 | Pass |
| 8 | F03 | R07 | TS08 | TC08 | Pass |
| 9 | F03 | R08 | TS09 | TC09 | Pass |
| 10 | F03 | R08 | TS10 | TC10 | Pass |
| 11 | F04 | R09 | — | — | Cross-cutting (parallel Chrome+Edge) |
| 12 | F04 | R10 | — | — | Cross-cutting (failure screenshot) |

---

## 8. Manual Testing & Defects

Before automating, every flow was walked through manually on Chrome and Edge
to validate expected behaviour and surface obvious issues. Defects found and
tracked in [`docs/Project_Documentation.xlsx`](docs/Project_Documentation.xlsx) → `Defects` sheet:

| Defect ID | Severity | Status | Notes |
|---|---|---|---|
| DEF001 | Medium | Open | Year-on-year extraction occasionally times out on first run due to ad blocks reflowing — workaround added (scroll + explicit wait) |
| DEF002 | Low | Open | Tenure scale signature differs between Chrome/Edge at >150% zoom — cosmetic |
| DEF003 | Low | Closed (Won't Fix) | Switching tabs resets loan amount — third-party site behaviour |
| DEF004 | Low | Open | First-month row may read stale value if schedule animation incomplete — fixed with explicit wait |

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

| Decision | Reason |
|---|---|
| One browser per runner, not per scenario | Faster runs, more realistic user session |
| `ThreadLocal<WebDriver>` in `DriverFactory` | Safe parallel browsers, no cross-thread interference |
| Custom thread-safe `ExtentManager` | The community `extentreports-cucumber7-adapter` has a Gson `LinkedTreeMap` concurrency bug — we drive Extent directly from Cucumber hooks instead |
| PageFactory `@FindBy` everywhere | Cleaner POM, no `By` declarations in user code |
| Multiple locator strategies (id/name/css/xpath/linkText) | Demonstrates locator-technique requirement; defensive against DOM changes |
| JS-based dynamic locator resolution | Replaces `By.id("year"+n)` patterns in a `By`-free codebase |
| Excel writes via Apache POI in step defs | Direct data extraction → persisted artefact (no intermediate CSV) |

---

## 10. Technology Stack

| Layer | Choice | Version |
|---|---|---|
| Language | Java | 17 (LTS) |
| Build | Maven | 3.9+ |
| Browser automation | Selenium WebDriver | 4.16.1 |
| Driver management | Selenium Manager (built-in) | — |
| BDD | Cucumber JVM | 7.15.0 |
| Test runner | TestNG | 7.8.0 |
| DI for Cucumber | PicoContainer | 7.15.0 |
| Excel I/O | Apache POI | 5.2.5 |
| HTML Report 1 | ExtentReports Spark | 5.1.1 |
| HTML Report 2 | Allure | 2.25.0 |
| Logging | Log4j 2 | 2.22.1 |
| Assertions | AssertJ + TestNG Assert | 3.25.1 / 7.8.0 |
| Allure interceptor | AspectJ Weaver | 1.9.21 |
| CI/CD | Jenkins (declarative pipeline) | — |
| Source control | Git + GitHub | — |

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

## 14. CI/CD with Jenkins, Git, GitHub

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

| TC | Chrome | Edge |
|---|---|---|
| TC01 | Pass | Pass |
| TC02 | Pass | Pass |
| TC03 | Pass | Pass |
| TC04 | Pass | Pass |
| TC05 | Pass | Pass |
| TC06 | Pass | Pass |
| TC07 | Pass | Pass |
| TC08 | Pass | Pass |
| TC09 | Pass | Pass |
| TC10 | Pass | Pass |

### Notable assertions verified at run time

| Assertion | Value |
|---|---|
| Car loan EMI (15L / 9.5% / 1yr) | ₹1,31,525 (within ±₹2 of expected ₹1,31,524) |
| First month interest | ₹11,875 (exact match) |
| First month principal | ₹1,19,650 (within tolerance) |
| Home loan yearly rows extracted | 21 (≥ 10 required) |
| Excel files produced | 2 (CarLoan + HomeLoan), persisted on disk |
| Tenure scale signature (Yr) | `0\|5\|10\|15\|20\|25\|30\|` |
| Tenure scale signature (Mo) | `0\|60\|120\|180\|240\|300\|360\|` |

### Artefacts produced

| Artefact | Size |
|---|---|
| `output/CarLoan_EMI_Summary.xlsx` | 8 rows |
| `output/HomeLoan_YearlySchedule.xlsx` | 22 rows (header + 21 years) |
| Failure screenshots | None (clean run) |
| Allure raw results | `target/allure-results/` |
| Extent Spark report | `reports/extent/SparkReport.html` |

---

## 16. Hackathon Requirements Checklist

| # | Requirement | Where it lives |
|---|---|---|
| 1 | Pick three end-to-end flows | §3 (Car / Home / Loan Calculator UI) |
| 2 | Ensure all three are different flows | §3 — different pages, data, assertions |
| 3 | Prepare Test Scenarios & Test Cases | [`docs/Project_Documentation.xlsx`](docs/Project_Documentation.xlsx) |
| 4 | Fill RTM | [`docs/Project_Documentation.xlsx`](docs/Project_Documentation.xlsx) → `RTM` sheet |
| 5 | Manual testing & defects | [`docs/Project_Documentation.xlsx`](docs/Project_Documentation.xlsx) → `Defects` sheet; §8 |
| 6a | Maven | [`pom.xml`](pom.xml) |
| 6b | Proper folder structure | §11 |
| 6c | Apache POI | [`ExcelUtils.java`](src/main/java/com/emicalc/automation/utils/ExcelUtils.java) + step defs |
| 6d | POM Design Pattern | [`pages/`](src/main/java/com/emicalc/automation/pages) — BasePage + 3 concrete pages |
| 6e | Data-driven + Keyword-driven (Hybrid) | Gherkin keywords (`Given/When/Then`) + parametrised arguments + `config.properties` test data |
| 6f | TestNG | [`testng.xml`](testng.xml), `AbstractTestNGCucumberTests` runners |
| 6g | Exception handling | `try-with-resources` in POI; defensive catches in `ScreenshotUtils`, `DriverFactory`, `BasePage` |
| 6h | Different locator techniques | id, name, css, xpath, linkText, partialLinkText — see `HomePage` / `HomeLoanPage` |
| 6i | Screenshot capture | [`ScreenshotUtils.java`](src/main/java/com/emicalc/automation/utils/ScreenshotUtils.java), invoked in `Hooks` + `TestListener` |
| 6j | Customised HTML Reports (Allure + Extent) | [`ExtentManager.java`](src/main/java/com/emicalc/automation/reports/ExtentManager.java) + `allure-cucumber7-jvm` plugin |
| 6k | End-to-end execution | `mvn clean test` runs the full suite |
| 6l | Multiple browser execution | Chrome + Edge runners, parallel via `testng.xml` |
| 6m | Listeners | [`TestListener.java`](src/test/java/com/emicalc/automation/listeners/TestListener.java) + Cucumber `Hooks` |
| 6o | Log4j | [`log4j2.xml`](src/main/resources/log4j2.xml) + rolling file appender |
| 6p | Parallel test execution | `testng.xml parallel="tests"` |
| 6q | Assertions | TestNG `Assert` + AssertJ in every step |
| 7 | Test Plan & Test Strategy | §4 and §5 |
| 8 | BRD | §2 |
| 9 | Test Execution Summary Report + CI/CD (Git/GitHub/Jenkins) | §15 + §14 + [`Jenkinsfile`](Jenkinsfile) |

---

## Help & Troubleshooting

| Symptom | Fix |
|---|---|
| `MojoNotFoundException: allure:reports` | The goal is `report` (singular): `mvn allure:report` |
| Only Chrome opens, Edge skipped | Make sure you're running `testng.xml` (not a single class). Check `parallel="tests"` is intact in the suite XML |
| `element click intercepted` warnings | Already mitigated by clicking the `<label>` parent of radio inputs |
| Allure CLI not found | Use `mvn allure:report` (uses the Maven plugin — no separate CLI install needed) |
| `MicrosoftEdge dns error: msedgedriver.azureedge.net` | Network blocked? Selenium Manager falls back to the cached driver — safe to ignore |
| Excel file locked | Close any Excel windows that have `output/*.xlsx` open before re-running |

---

**Author:** QA Lead — Cohort INTQEA26QE003
**Date:** 2026-05-19
**License:** Internal / Hackathon submission
