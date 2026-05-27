# EMI Calculator Hackathon Automation Project

> **Cohort:** INTQEA26QE003
> **Author:** Team SKRUM
> **Repo:** https://github.com/BatchuMamatha/EMI_Calculator_Hackathon_Project
> **System Under Test:** [emicalculator.net](https://emicalculator.net/)

A **Selenium 4 + Cucumber 7 (BDD) + TestNG** automation suite that drives
[emicalculator.net](https://emicalculator.net/), runs in **parallel on Chrome and
Edge** (10 scenarios x 2 browsers = **20 tests** per run), reads every piece of
per-TC data from **one Excel workbook**, collects failures with **AssertJ soft
assertions**, captures a screenshot for every scenario, persists output via
**Apache POI**, and publishes **three HTML reports** plus rolling **Log4j 2**
logs.

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Quick Worked Example](#2-quick-worked-example)
3. [Architecture and Execution Flow](#3-architecture-and-execution-flow)
4. [Tech Stack](#4-tech-stack)
5. [Folder Structure](#5-folder-structure)
6. [Concept Deep Dives](#6-concept-deep-dives)
7. [End to End Flow when you run mvn test](#7-end-to-end-flow-when-you-run-mvn-test)
8. [How to Run](#8-how-to-run)
9. [Reports and Artefacts](#9-reports-and-artefacts)
10. [Continuous Integration with Jenkins](#10-continuous-integration-with-jenkins)
11. [Test Plan and Strategy](#11-test-plan-and-strategy)
12. [Business Requirements](#12-business-requirements)
13. [Test Scenarios Test Cases RTM Defects](#13-test-scenarios-test-cases-rtm-defects)
14. [Hackathon Requirements Checklist](#14-hackathon-requirements-checklist)
15. [Troubleshooting](#15-troubleshooting)

---

## 1. Project Overview

This project automates **three end-to-end flows** on `emicalculator.net`:

<table>
  <thead>
    <tr>
      <th>Flow</th>
      <th>Feature file</th>
      <th>What it verifies</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><strong>Car Loan EMI</strong></td>
      <td><code>CarLoanEMI.feature</code></td>
      <td>Switches to the Car Loan tab, fills inputs read from <code>TestData.xlsx</code>, asserts the displayed EMI / first-month interest / first-month principal match the standard EMI formula (within +/- 2 rupees), and exports the summary to Excel via Apache POI.</td>
    </tr>
    <tr>
      <td><strong>Home Loan year-on-year extract</strong></td>
      <td><code>HomeLoanYearlySchedule.feature</code></td>
      <td>Navigates via top menu to the dedicated Home Loan page, fills the form, scrapes every row of the year-by-year amortisation table and persists it to <code>output/HomeLoan_YearlySchedule.xlsx</code>.</td>
    </tr>
    <tr>
      <td><strong>Loan Calculator UI</strong></td>
      <td><code>LoanCalculatorUI.feature</code></td>
      <td>Validates text-boxes + sliders on every sub-tab (EMI / Loan Amount / Loan Tenure Calculator); toggles tenure Year vs Month and asserts the slider scale changes.</td>
    </tr>
  </tbody>
</table>

The whole suite is **hybrid framework**:

<ul>
  <li><strong>BDD layer</strong> - Gherkin <code>.feature</code> files describe scenarios in business English.</li>
  <li><strong>Keyword-driven</strong> - every Gherkin step phrase is a keyword mapped to a Java step-definition.</li>
  <li><strong>Data-driven</strong> - per-TC inputs (loan amount, rate, tenure, sub-tab, verification type) live in <code>src/test/resources/testdata/TestData.xlsx</code> and are loaded at runtime by <code>TestDataReader</code>.</li>
  <li><strong>POM (Page Object Model)</strong> - every UI element is a <code>@FindBy</code> field on a Page class; step-defs never touch <code>By</code> directly.</li>
  <li><strong>Parallel + multi-browser</strong> - <code>testng.xml</code> launches Chrome and Edge as two parallel <code>&lt;test&gt;</code> threads; <code>BaseClass</code> isolates each thread with a <code>ThreadLocal&lt;WebDriver&gt;</code>.</li>
</ul>

---

## 2. Quick Worked Example

For Principal = Rs.15,00,000, Rate = 9.5% p.a., Tenure = 12 months the
suite asserts the following against the formula
`E = P x r x (1+r)^n / ((1+r)^n - 1)`:

<table>
  <thead>
    <tr><th>Metric</th><th>Expected</th></tr>
  </thead>
  <tbody>
    <tr><td>EMI</td><td><strong>Rs.1,31,524</strong></td></tr>
    <tr><td>First month - Interest</td><td><strong>Rs.11,875</strong> (Principal x monthly rate)</td></tr>
    <tr><td>First month - Principal</td><td><strong>Rs.1,19,649</strong> (EMI - first month interest)</td></tr>
    <tr><td>Total Interest (12 months)</td><td><strong>Rs.78,288</strong></td></tr>
  </tbody>
</table>

Tolerance: +/-2 rupees, to absorb rounding the site does internally.

---

## 3. Architecture and Execution Flow

```
                  +---------------------+
                  |     testng.xml      |
                  |  parallel="tests"   |
                  +----------+----------+
                             |
              +--------------+--------------+
              |                             |
   +----------v-----------+        +--------v-------------+
   |  <test>=Chrome        |        |  <test>=Edge          |
   |  thread 1             |        |  thread 2             |
   |  TestRunner extends   |        |  TestRunner extends   |
   |  BaseClass            |        |  BaseClass            |
   +----------+------------+        +----------+------------+
              |                                |
       @BeforeClass                      @BeforeClass
   open Chrome (TL_DRIVER)         open Edge (TL_DRIVER)
              |                                |
   Cucumber AbstractTestNGCucumberTests        |
   iterates pickles (scenarios)                |
              |                                |
       For each scenario:                For each scenario:
       Hooks.@Before                     Hooks.@Before
         ExtentTest.start                  ExtentTest.start
         clearCookies                      clearCookies
       Step Defs                          Step Defs
         TestDataReader.get(sheet,TC)      ...
         Page Objects (@FindBy)            ...
         ctx.softly.assertThat(...)        ...
       Hooks.@After                      Hooks.@After
         ScreenshotUtils.capture           ...
         Extent attach (path)              ...
         softly.assertAll()                ...
              |                                |
       @AfterClass                       @AfterClass
       quit Chrome                       quit Edge
              |                                |
              +----------------+---------------+
                               |
              TestListener.onFinish(suite)
                ExtentManager.flush()
                Allure raw -> mvn allure:report
```

Three reports drop onto disk (timestamped Extent, single Cucumber HTML,
Allure raw), screenshots accumulate in `screenshots/`, Excel artefacts in
`output/`, and `logs/automation.log` records everything.

---

## 4. Tech Stack

<table>
  <thead>
    <tr><th>Layer</th><th>Choice</th><th>Why</th></tr>
  </thead>
  <tbody>
    <tr><td>Language</td><td>Java 17</td><td>LTS, switch expressions, var, records</td></tr>
    <tr><td>Build</td><td>Maven 3.9+</td><td>standard Java build; surefire runs TestNG</td></tr>
    <tr><td>Browser driver</td><td>Selenium 4.16 with built-in Selenium Manager</td><td>auto-downloads chromedriver / msedgedriver / geckodriver</td></tr>
    <tr><td>BDD</td><td>Cucumber JVM 7</td><td>Gherkin, runtime <code>@Given/@When/@Then</code></td></tr>
    <tr><td>Test runner</td><td>TestNG via <code>cucumber-testng</code></td><td>parallel <code>&lt;test&gt;</code> blocks, <code>@Parameters</code>, listeners</td></tr>
    <tr><td>Step-def DI</td><td>Cucumber PicoContainer</td><td>injects fresh <code>ScenarioContext</code> per scenario</td></tr>
    <tr><td>Excel</td><td>Apache POI 5</td><td>reads <code>TestData.xlsx</code>, writes loan summary + amortisation</td></tr>
    <tr><td>Assertions</td><td>AssertJ <code>SoftAssertions</code></td><td>collects all failures, flushes at <code>@After</code></td></tr>
    <tr><td>Reports</td><td>Extent (Spark) + Allure + Cucumber HTML</td><td>three views of the same run</td></tr>
    <tr><td>Logging</td><td>Log4j 2</td><td>rolling file (no gzip), console + <code>logs/automation.log</code></td></tr>
    <tr><td>Allure interceptor</td><td>AspectJ Weaver (javaagent)</td><td>required so Allure can hook steps</td></tr>
    <tr><td>CI</td><td>Jenkins declarative pipeline (<code>Jenkinsfile</code>)</td><td>checkout, build, test, publish reports</td></tr>
    <tr><td>SCM</td><td>Git + GitHub</td><td>public repo</td></tr>
  </tbody>
</table>

---

## 5. Folder Structure

```
EMI_Calculator_Hackathon_Project/
|
| -- pom.xml                          # Maven build + dependencies + surefire
| -- testng.xml                       # parallel="tests" Chrome + Edge runner config
| -- Jenkinsfile                      # declarative pipeline
| -- README.md
| -- .gitignore
|
| -- docs/
|     | -- Project_Documentation.xlsx # User Stories / Scenarios / TC / RTM / Defects
|     | -- generate_docs.py           # regenerates the above
|     | -- generate_testdata.py       # creates src/test/resources/testdata/TestData.xlsx
|     | -- convert_md_tables.py       # turns README pipe tables into <table> for Eclipse
|
| -- src/
|     | -- main/java/com/hackathon/
|     |     | -- base/
|     |     |     | -- BaseClass.java          # multi-browser + ThreadLocal driver
|     |     | -- config/
|     |     |     | -- ConfigReader.java       # singleton properties loader
|     |     | -- pages/
|     |     |     | -- BasePage.java           # POM helpers, PageFactory.initElements
|     |     |     | -- HomePage.java           # main calculator page
|     |     |     | -- HomeLoanPage.java       # dedicated Home Loan page
|     |     |     | -- LoanCalculatorPage.java # 3 sub-tab calculator
|     |     | -- reports/
|     |     |     | -- ExtentManager.java      # thread-safe Extent driver
|     |     | -- utils/
|     |     |     | -- EMICalculatorUtil.java  # pure-Java EMI math
|     |     |     | -- ExcelUtils.java         # POI write/read
|     |     |     | -- ScreenshotUtils.java    # OutputType.FILE capture
|     |     |     | -- TestDataReader.java     # loads TestData.xlsx by TC id
|     |
|     | -- test/
|     |     | -- java/com/hackathon/
|     |     |     | -- context/
|     |     |     |     | -- ScenarioContext.java     # per-scenario state + SoftAssertions
|     |     |     | -- hooks/
|     |     |     |     | -- Hooks.java               # @Before / @After
|     |     |     | -- listeners/
|     |     |     |     | -- TestListener.java        # suite-level cleanup + logging
|     |     |     | -- runners/
|     |     |     |     | -- TestRunner.java          # the single runner; extends BaseClass
|     |     |     | -- stepdefinitions/
|     |     |     |     | -- CarLoanSteps.java
|     |     |     |     | -- HomeLoanSteps.java
|     |     |     |     | -- LoanCalculatorSteps.java
|     |     |
|     |     | -- resources/
|     |           | -- config.properties           # URLs, timeouts, output paths (no test data)
|     |           | -- extent.properties           # ExtentManager config
|     |           | -- log4j2.xml                  # Log4j config
|     |           | -- allure.properties           # allure.results.directory
|     |           | -- features/
|     |           |     | -- CarLoanEMI.feature           (TC01-TC04)
|     |           |     | -- HomeLoanYearlySchedule.feature (TC05-TC06)
|     |           |     | -- LoanCalculatorUI.feature       (TC07-TC10)
|     |           | -- testdata/
|     |                 | -- TestData.xlsx          # ALL per-TC inputs live here
|
| -- (gitignored runtime artefacts)
|     | -- logs/        automation.log
|     | -- output/      generated Excel files
|     | -- reports/     extent + cucumber HTML
|     | -- screenshots/ per-scenario PNGs
|     | -- target/      Maven build (classes, allure-results, surefire-reports)
```

---

## 6. Concept Deep Dives

### 6.1 Maven build (pom.xml)

`pom.xml` declares all dependencies and configures three plugins:

<table>
  <thead>
    <tr><th>Plugin</th><th>Role</th></tr>
  </thead>
  <tbody>
    <tr>
      <td><strong>maven-compiler-plugin</strong></td>
      <td>Compiles Java 17 sources. <code>&lt;proc&gt;none&lt;/proc&gt;</code> disables annotation processing; <code>generatedSourcesDirectory</code> is redirected to <code>classes/</code> so no empty <code>target/generated-sources/</code> folder is created.</td>
    </tr>
    <tr>
      <td><strong>maven-surefire-plugin</strong></td>
      <td>Runs the test phase. Points at <code>testng.xml</code>, injects <code>cucumber.filter.tags</code> and <code>allure.results.directory</code> system properties, loads the AspectJ javaagent so Allure intercepts Cucumber steps, and disables TestNG default listeners (so the old <code>test-output/</code> folder is no longer created).</td>
    </tr>
    <tr>
      <td><strong>allure-maven</strong></td>
      <td>Exposes <code>mvn allure:report</code> and <code>mvn allure:serve</code> to convert <code>target/allure-results/</code> into a browsable Allure HTML.</td>
    </tr>
  </tbody>
</table>

### 6.2 BDD with Cucumber

A `.feature` file is plain English; each line maps to a Java method via
the regex on `@Given / @When / @Then / @And`.

**Example - CarLoanEMI.feature**

```gherkin
@CarLoan @Smoke
Feature: Car Loan EMI calculation
  Background:
    Given the EMI Calculator homepage is open

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

`Scenario Outline` + `Examples` is Cucumber's data-driven idiom: the
runner creates one test per row. Combined with `TestData.xlsx` lookup,
the feature file only carries the **TC id**, not the actual numbers.

**Tags** organise scenarios (`@Smoke`, `@Regression`, `@UI`, `@TC04`).
The runner filters by tag expression via
`-Dcucumber.filter.tags="@Smoke"`.

### 6.3 Page Object Model

Every UI page is a class extending `BasePage`. Locators are declared with
`@FindBy` and PageFactory initialises the proxies in the constructor:

```java
public class HomePage extends BasePage {

    @FindBy(id = "car-loan")          private WebElement carLoanTab;
    @FindBy(name = "loanamount")      private WebElement loanAmount;
    @FindBy(name = "loaninterest")    private WebElement loanInterest;
    @FindBy(name = "loanterm")        private WebElement loanTerm;
    @FindBy(xpath = "//div[@id='emiamount']//p/span") private WebElement emiAmount;

    public HomePage selectCarLoanTab() {
        click(carLoanTab);
        wait.until(ExpectedConditions.attributeContains(carLoanTab, "class", "active"));
        return this;
    }

    public long readEmi() {
        return EMICalculatorUtil.parseIndianCurrency(text(emiAmount));
    }
}
```

`BasePage` centralises `click`, `typeReplacing`, `text`, `isVisible`,
`scrollIntoView`, JS execution, Actions, `findInListByText`. Mixed locator
strategies (id, name, css, xpath, linkText) satisfy the "different locator
techniques" hackathon requirement.

### 6.4 Hooks (Cucumber lifecycle)

`Hooks.java` runs **once per scenario** (not per step). It is wired in
because `TestRunner.@CucumberOptions(glue = {...})` includes the package
`com.hackathon.hooks`.

<table>
  <thead>
    <tr><th>Hook</th><th>What it does</th></tr>
  </thead>
  <tbody>
    <tr>
      <td><code>@Before(order = 0)</code></td>
      <td>Reads the browser name from the driver, extracts the <code>@TCxx</code> tag, creates a new ExtentTest node, logs an INFO banner, and clears all cookies so each scenario starts with a clean session.</td>
    </tr>
    <tr>
      <td><code>@After(order = 1)</code></td>
      <td>Decides PASS/FAIL by checking <code>scenario.isFailed()</code> AND <code>ctx.softly.errorsCollected()</code>, captures a screenshot named <code>TCxx_&lt;slug&gt;_&lt;PASSED|FAILED&gt;_&lt;browser&gt;_&lt;ts&gt;.png</code>, attaches it to Extent (pass or fail), flushes the SoftAssertions (which is what actually fails the scenario in Cucumber), and in a <code>finally</code> block ends the Extent test.</td>
    </tr>
  </tbody>
</table>

Hooks receive `ScenarioContext` via PicoContainer constructor injection -
the same instance is also injected into the three step-def classes,
so they share state for the lifetime of one scenario.

### 6.5 TestNG Listener

`TestListener` implements `ISuiteListener` + `ITestListener`. It is
attached in `testng.xml`. Responsibilities:

<table>
  <thead>
    <tr><th>Event</th><th>Action</th></tr>
  </thead>
  <tbody>
    <tr><td><code>onStart(ISuite)</code></td><td>Deletes stale <code>screenshots/*.png</code> and <code>target/allure-results/*</code> so each run starts clean. <em>Does not</em> wipe <code>reports/extent/</code> because Extent reports already carry a unique timestamp.</td></tr>
    <tr><td><code>onFinish(ISuite)</code></td><td>Calls <code>ExtentManager.flush()</code> once, after both browser threads finish.</td></tr>
    <tr><td><code>onTestStart/Success/Failure/Skipped</code></td><td>Logs each TestNG <code>@Test</code> method (Cucumber's <code>runScenario</code>) via Log4j.</td></tr>
  </tbody>
</table>

### 6.6 TestRunner and BaseClass

There is a **single runner** - `TestRunner.java`:

```java
@CucumberOptions(
    features = "src/test/resources/features",
    glue     = { "com.hackathon.stepdefinitions", "com.hackathon.hooks" },
    tags     = "@Smoke or @Regression or @UI",
    plugin   = { "pretty",
                 "html:reports/cucumber/cucumber.html",
                 "json:reports/cucumber/cucumber.json",
                 "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm" },
    monochrome = true,
    publish    = false
)
public class TestRunner extends BaseClass { }
```

`TestRunner` is referenced **twice** in `testng.xml` - once under
`<test name="Chrome-Tests">` with `<parameter name="browser" value="chrome"/>`
and once under `<test name="Edge-Tests">` with `value="edge"`. With
`parallel="tests"` each `<test>` block runs in its own TestNG worker
thread.

`BaseClass` is the abstract parent. It:

<ul>
  <li>declares <code>protected static final ThreadLocal&lt;WebDriver&gt; driver</code> - the class-level WebDriver, isolated per thread so Chrome and Edge can't collide.</li>
  <li>exposes <code>public static WebDriver getDriver()</code> - the only access point used by pages, screenshots, and step-defs.</li>
  <li>declares <code>@BeforeClass launchBrowser(@Parameters("browser") String b)</code> - reads <code>headless</code> from <code>config.properties</code>, builds the right driver (Chrome / Edge / Firefox), maximises the window, sets implicit + page-load timeouts, and stores the driver in the ThreadLocal.</li>
  <li>declares <code>@AfterClass closeBrowser()</code> - quits the driver and removes the ThreadLocal binding so the next runner instance starts clean.</li>
</ul>

Firefox is fully implemented; uncomment the third `<test>` block in
`testng.xml` to enable it.

### 6.7 Data-driven testing - TestData.xlsx

`TestDataReader` loads `src/test/resources/testdata/TestData.xlsx`
through Apache POI, caches every row keyed by sheet + TestCaseID, and
returns a `Map<String, String>` to step-defs.

<table>
  <thead>
    <tr><th>Sheet</th><th>Columns</th><th>Rows</th></tr>
  </thead>
  <tbody>
    <tr>
      <td><strong>CarLoan</strong></td>
      <td>TestCaseID, LoanAmount, InterestRate, Tenure, TenureUnit, VerificationType, Year</td>
      <td>TC01..TC04</td>
    </tr>
    <tr>
      <td><strong>HomeLoan</strong></td>
      <td>TestCaseID, LoanAmount, InterestRate, Tenure, MinRows, MenuItem</td>
      <td>TC05, TC06</td>
    </tr>
    <tr>
      <td><strong>LoanCalculator</strong></td>
      <td>TestCaseID, SubTab</td>
      <td>TC07..TC10</td>
    </tr>
  </tbody>
</table>

To change a loan amount or rate, edit the spreadsheet - no Java change
required.

### 6.8 Soft Assertions

Every assertion in step-defs uses `ctx.softly.assertThat(...)`. Failures
are **collected**, not thrown:

```java
ctx.softly.assertThat(Math.abs(actual - expected))
        .as("EMI: expected %d +/- %d, got %d", expected, tol, actual)
        .isLessThanOrEqualTo(tol);
```

`Hooks.@After` calls `ctx.softly.assertAll()` at the end of the scenario.
If any assertion failed, that single call throws a multi-assertion error
listing every failure, and Cucumber records the scenario as FAILED.

This means a single scenario can verify EMI + total interest + first
month split in one run and report all three failures together instead
of stopping at the first one.

### 6.9 Logging - Log4j 2

`log4j2.xml` declares two appenders:

<table>
  <thead>
    <tr><th>Appender</th><th>Output</th></tr>
  </thead>
  <tbody>
    <tr><td>Console</td><td>Standard out (visible in the IDE / Jenkins console)</td></tr>
    <tr><td>RollingFile</td><td><code>logs/automation.log</code>, rolled daily or at 10 MB, max 10 history files. <strong>No gzip</strong> - rolled files keep the <code>.log</code> extension for easy viewing.</td></tr>
  </tbody>
</table>

Logger `com.hackathon` is set to INFO; Selenium is at WARN; Cucumber at
INFO.

### 6.10 Screenshots

`ScreenshotUtils.capture(name)` uses the standard Selenium pattern:

```java
File source = ((TakesScreenshot) BaseClass.getDriver())
        .getScreenshotAs(OutputType.FILE);
Path target = Paths.get(System.getProperty("user.dir"),
        "screenshots", name + "_" + timestamp + ".png");
Files.copy(source.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
```

Filename pattern:
`TCxx_<scenario-slug>_<PASSED|FAILED>_<browser>_<yyyyMMdd_HHmmss_SSS>.png`

A screenshot is captured for **every** scenario (pass or fail). Pass
screenshots attach to the Extent test via `t.pass(label, mediaFromPath)`;
fail screenshots via `t.fail(label, mediaFromPath)`. The folder is wiped
at suite start by `TestListener` so old captures don't pile up.

### 6.11 Reports

<table>
  <thead>
    <tr><th>Report</th><th>Where</th><th>How it's produced</th></tr>
  </thead>
  <tbody>
    <tr>
      <td><strong>Extent (Spark)</strong></td>
      <td><code>reports/extent/ExtentReport_&lt;yyyy.MM.dd_HH.mm.ss&gt;.html</code></td>
      <td>Driven by our thread-safe <code>ExtentManager</code> directly from <code>Hooks</code>. Reads <code>extent.properties</code> for path / theme / title / timestamp toggle. Previous run files are <em>not</em> overwritten (timestamp is unique).</td>
    </tr>
    <tr>
      <td><strong>Cucumber HTML</strong></td>
      <td><code>reports/cucumber/cucumber.html</code></td>
      <td>Cucumber's built-in <code>html:</code> plugin (configured on <code>TestRunner</code>). Overwrites each run.</td>
    </tr>
    <tr>
      <td><strong>Allure</strong></td>
      <td><code>target/allure-results/</code> raw -> <code>mvn allure:report</code> -> <code>target/allure-report/index.html</code></td>
      <td><code>AllureCucumber7Jvm</code> Cucumber plugin writes UUID-named JSONs into <code>allure-results/</code>, then the Allure Maven plugin renders the dashboard.</td>
    </tr>
  </tbody>
</table>

We deliberately do **not** use `tech.grasshopper:extentreports-cucumber7-adapter`
- it has a Gson `LinkedTreeMap` concurrency bug that fires under
`parallel="tests"`. `ExtentManager` is our thread-safe replacement.

### 6.12 Parallel execution and ThreadLocal

`testng.xml` sets `parallel="tests" thread-count="4"`. The two
`<test>` blocks become two TestNG worker threads, each instantiating
`TestRunner` and calling its `@BeforeClass`. Because `driver` is a
`ThreadLocal<WebDriver>` static field on `BaseClass`, each worker thread
gets its own Chrome / Edge instance and they never see each other's
driver.

`data-provider-thread-count="1"` keeps each runner's scenarios sequential
within that thread (so the single browser is reused across all 10
scenarios for that runner).

### 6.13 Dependency injection - Cucumber PicoContainer

`cucumber-picocontainer` is on the classpath, which means Cucumber will
construct step-defs and `Hooks` with their dependencies wired up
automatically.

`ScenarioContext` is the **only** shared instance per scenario:

```java
public class ScenarioContext {
    public HomePage homePage;
    public HomeLoanPage homeLoanPage;
    public LoanCalculatorPage loanCalculatorPage;
    public List<List<String>> extractedSchedule;
    public final SoftAssertions softly = new SoftAssertions();
    private final Map<String, Object> bag = new HashMap<>();
    public void put(String k, Object v) { bag.put(k, v); }
    public <T> T get(String k) { return (T) bag.get(k); }
}
```

PicoContainer instantiates it once per scenario and supplies it to
`Hooks(ScenarioContext)`, `CarLoanSteps(ScenarioContext)`,
`HomeLoanSteps(ScenarioContext)`, and `LoanCalculatorSteps(ScenarioContext)`.

---

## 7. End to End Flow when you run mvn test

```
mvn clean test
   |
   | -- Maven Surefire reads testng.xml
   |
   | -- TestListener.onStart(ISuite)
   |       wipe screenshots/, wipe target/allure-results/
   |
   | -- spawn 2 TestNG worker threads (parallel="tests")
   |       thread-1: chrome             thread-2: edge
   |          |                                 |
   |          v                                 v
   |   TestRunner instance #1            TestRunner instance #2
   |   @BeforeClass launchBrowser("chrome")  ... launchBrowser("edge")
   |     ChromeDriver -> ThreadLocal     EdgeDriver -> ThreadLocal
   |          |                                 |
   |          | AbstractTestNGCucumberTests iterates pickles
   |          | runScenario(pickle1)            runScenario(pickle1)
   |          |   Hooks.@Before                   Hooks.@Before
   |          |     ExtentTest.create               ExtentTest.create
   |          |     clearCookies                    clearCookies
   |          |   Step defs                       Step defs
   |          |     TestDataReader.get("CarLoan","TC01")
   |          |     ctx.homePage = new HomePage().open()
   |          |     ctx.homePage.selectCarLoanTab()...
   |          |     ctx.softly.assertThat(emi).isLessThanOrEqualTo(tol)
   |          |   Hooks.@After
   |          |     ScreenshotUtils.capture("TC01_..._PASSED_chrome_...png")
   |          |     ExtentManager.attachPassScreenshot(path, label)
   |          |     ctx.softly.assertAll()
   |          |     ExtentManager.endScenario()
   |          | runScenario(pickle2) ... 10 scenarios total
   |          |
   |   @AfterClass closeBrowser()       @AfterClass closeBrowser()
   |     driver.quit()                    driver.quit()
   |
   | -- TestListener.onFinish(ISuite)
   |       ExtentManager.flush()
   |       writes reports/extent/ExtentReport_<ts>.html
   |
   `-- BUILD SUCCESS
```

---

## 8. How to Run

### Prerequisites

<ul>
  <li>JDK 17 on <code>PATH</code> (verify: <code>java -version</code>)</li>
  <li>Maven 3.9+ on <code>PATH</code> (verify: <code>mvn -version</code>)</li>
  <li>Chrome and Edge installed (Selenium Manager fetches the matching driver binary)</li>
  <li>Optional: Firefox if you uncomment the Firefox <code>&lt;test&gt;</code> block</li>
</ul>

### From the command line

```powershell
# Full suite: 10 scenarios x Chrome + Edge = 20 tests, in parallel
mvn clean test

# Headless (recommended on CI / no display)
mvn clean test -Dheadless=true

# Tag subset
mvn clean test "-Dcucumber.filter.tags=@Smoke"
mvn clean test "-Dcucumber.filter.tags=@CarLoan"
mvn clean test "-Dcucumber.filter.tags=@TC04"

# Generate the Allure HTML dashboard
mvn allure:report                # writes target/allure-report/index.html
mvn allure:serve                 # opens it in a local browser
```

### From the IDE (Eclipse / IntelliJ)

Right-click `testng.xml` and choose **Run As -> TestNG Suite**. Two
browsers open simultaneously, each runs 10 scenarios, then both close.

---

## 9. Reports and Artefacts

After a successful run:

<table>
  <thead>
    <tr><th>Artefact</th><th>Path</th></tr>
  </thead>
  <tbody>
    <tr><td>Extent Spark report</td><td><code>reports/extent/ExtentReport_&lt;ts&gt;.html</code></td></tr>
    <tr><td>Cucumber HTML (static, masterthought)</td><td><code>reports/cucumber-html/overview-features.html</code> (after <code>mvn verify</code>)</td></tr>
    <tr><td>Cucumber HTML (raw, Cucumber 7 SPA)</td><td><code>reports/cucumber/cucumber.html</code> (may render blank from file://, see Troubleshooting)</td></tr>
    <tr><td>Cucumber JSON (input for masterthought)</td><td><code>reports/cucumber/cucumber.json</code></td></tr>
    <tr><td>Allure raw results</td><td><code>target/allure-results/</code></td></tr>
    <tr><td>Allure HTML dashboard</td><td><code>target/allure-report/index.html</code> (after <code>mvn allure:report</code>)</td></tr>
    <tr><td>Car loan summary Excel</td><td><code>output/CarLoan_EMI_Summary.xlsx</code></td></tr>
    <tr><td>Home loan schedule Excel</td><td><code>output/HomeLoan_YearlySchedule.xlsx</code></td></tr>
    <tr><td>Per-scenario screenshots</td><td><code>screenshots/*.png</code></td></tr>
    <tr><td>Log file</td><td><code>logs/automation.log</code></td></tr>
    <tr><td>Surefire XML (JUnit format)</td><td><code>target/surefire-reports/*.xml</code></td></tr>
  </tbody>
</table>

---

## 10. Continuous Integration with Jenkins

`Jenkinsfile` declares a five-stage declarative pipeline:

<table>
  <thead>
    <tr><th>Stage</th><th>Action</th></tr>
  </thead>
  <tbody>
    <tr><td>Checkout</td><td><code>checkout scm</code></td></tr>
    <tr><td>Build</td><td><code>mvn -B clean compile</code></td></tr>
    <tr><td>Test (Chrome + Edge in parallel)</td><td><code>mvn -B test -Dheadless=&lt;param&gt; &lt;tag-arg&gt;</code></td></tr>
    <tr><td>Publish Reports</td><td><code>publishHTML</code> for Extent + Cucumber; <code>junit</code> for surefire XML</td></tr>
    <tr><td>Archive Artefacts</td><td>Excel files, logs, full <code>reports/</code> tree, screenshots, allure results</td></tr>
  </tbody>
</table>

**Required Jenkins plugins:** Pipeline, Git, GitHub, Timestamper, Build
Timeout, JUnit, HTML Publisher, Allure Jenkins Plugin (optional),
TestNG Results (optional), Pipeline Stage View.

**Global Tools (Manage Jenkins -> Tools):** JDK 17 named `JDK17`, Maven
3.9+ named `Maven3`.

**Parameters exposed in the pipeline:**

<ul>
  <li><code>TAG</code> - choice of <code>@Smoke</code>, <code>@Regression</code>, <code>@UI</code>, <code>@CarLoan</code>, <code>@HomeLoan</code>, <code>@LoanCalculator</code> (or empty for all)</li>
  <li><code>HEADLESS</code> - boolean, default true</li>
</ul>

---

## 11. Test Plan and Strategy

### Objective

Validate that emicalculator.net computes EMI / first-month split /
amortisation schedule correctly, that the menu navigation works, and
that the Loan Calculator UI stays operable across all three sub-tabs -
on **both Chrome and Edge in parallel**.

### Scope

<table>
  <thead>
    <tr><th>In scope</th><th>Out of scope</th></tr>
  </thead>
  <tbody>
    <tr>
      <td>Car / Home / Loan-Calculator flows</td>
      <td>Personal Loan, Credit Card EMI</td>
    </tr>
    <tr>
      <td>EMI maths, first-month split, total interest</td>
      <td>Tax + insurance overlays, prepayment scenarios</td>
    </tr>
    <tr>
      <td>Year-on-year schedule extract</td>
      <td>Mobile viewports, responsive layout</td>
    </tr>
    <tr>
      <td>UI sanity on 3 sub-calculators</td>
      <td>Accessibility (WCAG), performance</td>
    </tr>
    <tr>
      <td>Chrome + Edge in parallel</td>
      <td>Firefox, Safari (Firefox is implemented but disabled)</td>
    </tr>
  </tbody>
</table>

### Entry / Exit criteria

<ul>
  <li>Entry: JDK 17, Maven, Chrome, Edge installed; internet access to emicalculator.net.</li>
  <li>Exit: All 20 tests pass (10 scenarios x 2 browsers); 2 Excel artefacts present; Extent / Allure / Cucumber HTML produced.</li>
</ul>

### Risks and mitigations

<table>
  <thead><tr><th>Risk</th><th>Mitigation</th></tr></thead>
  <tbody>
    <tr><td>Third-party site DOM changes</td><td>Multiple locator strategies; PageFactory proxies re-resolve on each call.</td></tr>
    <tr><td>Ad blocks shift the schedule table</td><td>Explicit waits + <code>scrollBy(0, 800)</code> before extraction.</td></tr>
    <tr><td>Browser-driver version drift</td><td>Selenium Manager auto-downloads the right driver per run.</td></tr>
    <tr><td>Parallel race conditions</td><td><code>ThreadLocal&lt;WebDriver&gt;</code> on <code>BaseClass</code>; <code>ThreadLocal&lt;ExtentTest&gt;</code> on <code>ExtentManager</code>.</td></tr>
  </tbody>
</table>

### Approach summary

<ul>
  <li><strong>Hybrid framework</strong>: BDD + keyword + data-driven + POM.</li>
  <li><strong>Independent maths</strong>: <code>EMICalculatorUtil</code> recomputes the expected value in pure Java; the assertion is against the displayed value with a +/- 2 rupee tolerance.</li>
  <li><strong>Soft assertions</strong>: all per-scenario failures are collected then reported together at <code>@After</code>.</li>
  <li><strong>Screenshot every scenario</strong>: pass or fail, embedded in Extent.</li>
</ul>

---

## 12. Business Requirements

Borrowers and finance teams using `emicalculator.net` need confidence
that:

<ul>
  <li>The EMI shown matches the standard EMI formula.</li>
  <li>The per-month split is correct in the very first instalment.</li>
  <li>The year-by-year amortisation schedule can be exported for offline analysis.</li>
  <li>The Loan Calculator UI remains operable across all three sub-calculators.</li>
</ul>

Functional requirements mapped to RTM:

<table>
  <thead><tr><th>ID</th><th>Requirement</th></tr></thead>
  <tbody>
    <tr><td>FR-001</td><td>Car loan EMI matches the formula within +/-2</td></tr>
    <tr><td>FR-002</td><td>First-month interest = P x monthly rate; first-month principal = EMI - interest</td></tr>
    <tr><td>FR-003</td><td>Car loan summary exportable to Excel via Apache POI</td></tr>
    <tr><td>FR-004</td><td>Home Loan calculator reachable from top menu</td></tr>
    <tr><td>FR-005</td><td>Year-on-year schedule extractable and storable to Excel</td></tr>
    <tr><td>FR-006</td><td>EMI Calculator inputs + sliders operable</td></tr>
    <tr><td>FR-007</td><td>Switching tenure Year vs Month changes the slider scale</td></tr>
    <tr><td>FR-008</td><td>Same UI validation reusable across all 3 sub-calculators</td></tr>
    <tr><td>FR-009</td><td>Suite executes in parallel on Chrome and Edge</td></tr>
    <tr><td>FR-010</td><td>Failures capture a screenshot attached to the report</td></tr>
  </tbody>
</table>

---

## 13. Test Scenarios Test Cases RTM Defects

All five sheets (UserStories, TestScenarios, TestCases, RTM, Defects)
live in [`docs/Project_Documentation.xlsx`](docs/Project_Documentation.xlsx)
(matching the Finding_Hospital reference template style).

10 test cases at a glance:

<table>
  <thead><tr><th>TC</th><th>Feature</th><th>What it verifies</th></tr></thead>
  <tbody>
    <tr><td>TC01</td><td>CarLoanEMI</td><td>EMI value calculation</td></tr>
    <tr><td>TC02</td><td>CarLoanEMI</td><td>First month interest</td></tr>
    <tr><td>TC03</td><td>CarLoanEMI</td><td>First month principal</td></tr>
    <tr><td>TC04</td><td>CarLoanEMI</td><td>Export car loan summary to Excel</td></tr>
    <tr><td>TC05</td><td>HomeLoan</td><td>Menu navigation to Home Loan Calculator</td></tr>
    <tr><td>TC06</td><td>HomeLoan</td><td>Year-on-year schedule extraction to Excel</td></tr>
    <tr><td>TC07</td><td>LoanCalc</td><td>EMI Calculator - UI sanity</td></tr>
    <tr><td>TC08</td><td>LoanCalc</td><td>Tenure unit toggle changes slider scale</td></tr>
    <tr><td>TC09</td><td>LoanCalc</td><td>UI validation reused on Loan Amount Calculator</td></tr>
    <tr><td>TC10</td><td>LoanCalc</td><td>UI validation reused on Loan Tenure Calculator</td></tr>
  </tbody>
</table>

Each TC runs on **two browsers** -> 20 total tests per suite execution.

---

## 14. Hackathon Requirements Checklist

<table>
  <thead><tr><th>Item</th><th>Where in the project</th></tr></thead>
  <tbody>
    <tr><td>Maven</td><td><code>pom.xml</code></td></tr>
    <tr><td>Proper folder structure</td><td>See section 5</td></tr>
    <tr><td>Apache POI</td><td><code>ExcelUtils.java</code> + <code>TestDataReader.java</code></td></tr>
    <tr><td>POM Design Pattern</td><td><code>pages/</code> - BasePage + 3 concrete pages with @FindBy</td></tr>
    <tr><td>Data driven + Keyword driven (Hybrid)</td><td>Gherkin keywords + <code>TestData.xlsx</code> driven by <code>TestDataReader</code></td></tr>
    <tr><td>TestNG</td><td><code>testng.xml</code>, <code>cucumber-testng</code>, listener wiring</td></tr>
    <tr><td>Exception handling</td><td>try-with-resources in POI; defensive catches in <code>BasePage</code>, <code>Hooks</code>, <code>ScreenshotUtils</code></td></tr>
    <tr><td>Different locator techniques</td><td>id, name, css, xpath, linkText across pages</td></tr>
    <tr><td>Screenshot capture</td><td><code>ScreenshotUtils</code> - per scenario, attached to Extent</td></tr>
    <tr><td>Customised HTML reports (Allure + Extent)</td><td>Allure plugin + custom <code>ExtentManager</code></td></tr>
    <tr><td>End to end execution</td><td><code>mvn clean test</code></td></tr>
    <tr><td>Multiple browser execution</td><td>Chrome + Edge in parallel (Firefox supported, off by default per requirement p)</td></tr>
    <tr><td>Listeners</td><td><code>TestListener</code> (suite + test) + Cucumber <code>Hooks</code></td></tr>
    <tr><td>Log4j</td><td><code>log4j2.xml</code> - rolling file, no gzip</td></tr>
    <tr><td>Parallel test execution</td><td><code>testng.xml parallel="tests"</code>, Firefox <code>&lt;test&gt;</code> commented</td></tr>
    <tr><td>Assertions</td><td>AssertJ <code>SoftAssertions</code> everywhere in step-defs</td></tr>
    <tr><td>CI/CD with Git + GitHub + Jenkins</td><td><code>.gitignore</code> + <code>Jenkinsfile</code> + GitHub repo</td></tr>
    <tr><td>Test Plan + Strategy</td><td>Section 11</td></tr>
    <tr><td>BRD</td><td>Section 12</td></tr>
    <tr><td>Test Scenarios / RTM / Defects</td><td><code>docs/Project_Documentation.xlsx</code></td></tr>
  </tbody>
</table>

---

## 15. Troubleshooting

<table>
  <thead><tr><th>Symptom</th><th>Fix</th></tr></thead>
  <tbody>
    <tr><td><code>MojoNotFoundException: allure:reports</code></td><td>Goal is singular: <code>mvn allure:report</code>.</td></tr>
    <tr><td>Allure shows more tests than actually ran</td><td>Stale results in <code>target/allure-results/</code>. <code>TestListener.onStart</code> already wipes it; otherwise run <code>mvn clean test</code>.</td></tr>
    <tr><td>Only Chrome opens, Edge doesn't</td><td>Make sure you're running <code>testng.xml</code> (not a single class). Confirm <code>parallel="tests"</code> in the suite XML.</td></tr>
    <tr><td><code>Element click intercepted</code> on tenure radio</td><td>Already mitigated - locators target the wrapping <code>&lt;label&gt;</code> instead of the hidden <code>&lt;input&gt;</code>.</td></tr>
    <tr><td>Excel <code>~$TestData.xlsx</code> lock file</td><td><code>~$*.xlsx</code> is in <code>.gitignore</code>. Close Excel before <code>git add</code>.</td></tr>
    <tr><td><code>target/generated-sources/</code> keeps reappearing</td><td><code>pom.xml</code> redirects them to <code>classes/</code> for Maven CLI builds. Eclipse builds may still create them - harmless.</td></tr>
    <tr><td><code>automation-*.log.gz</code> in <code>logs/</code></td><td>No longer produced - <code>log4j2.xml</code> writes plain <code>.log</code> files now.</td></tr>
    <tr><td>Extent report has both <code>SparkReport.html</code> and timestamped files</td><td>Old code wrote <code>SparkReport.html</code>. Current code only writes timestamped <code>ExtentReport_&lt;ts&gt;.html</code>. <code>SparkReport.html</code> can be deleted manually.</td></tr>
    <tr><td>Jenkins <code>allure</code> step fails with "Invalid parameter"</td><td>Allure Jenkins Plugin is not installed or is an old version. Either install the plugin or remove the <code>allure([...])</code> block from <code>Jenkinsfile</code>; you can still generate Allure locally via <code>mvn allure:report</code>.</td></tr>
    <tr>
      <td>Cucumber HTML <strong>shows blank white page</strong> in the browser</td>
      <td>The default <code>reports/cucumber/cucumber.html</code> is a JavaScript single-page-app that browsers block via <code>file://</code>. Run <code>mvn verify</code> to generate the proper static report at <code>reports/cucumber-html/overview-features.html</code> (powered by <code>net.masterthought:maven-cucumber-reporting</code>) and open <em>that</em> instead.</td>
    </tr>
  </tbody>
</table>

---

**Author:** Team SKRUM
**Cohort:** INTQEA26QE003
**Last Updated:** 2026-05-26
