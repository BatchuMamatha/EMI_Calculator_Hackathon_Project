"""Generate Project_Documentation.xlsx with 5 sheets (orange theme)."""
from openpyxl import Workbook
from openpyxl.styles import Font, PatternFill, Alignment, Border, Side
from openpyxl.utils import get_column_letter

OUT = r"C:\Users\2485860\Downloads\EMI_Calculator_Hackathon_Project\docs\Project_Documentation.xlsx"

ORANGE_DARK   = "C65911"
ORANGE_MID    = "ED7D31"
ORANGE_LIGHT  = "FCE4D6"
ORANGE_BAND   = "FFF2CC"
WHITE         = "FFFFFF"
BLACK         = "000000"

FONT_NAME = "Arial"
thin = Side(border_style="thin", color="BFBFBF")
BORDER = Border(left=thin, right=thin, top=thin, bottom=thin)

def header_font():
    return Font(name=FONT_NAME, size=11, bold=True, color=WHITE)

def cell_font():
    return Font(name=FONT_NAME, size=10, color=BLACK)

def title_font():
    return Font(name=FONT_NAME, size=14, bold=True, color=WHITE)

def write_sheet(ws, title, headers, rows, col_widths=None):
    ws.sheet_view.showGridLines = False
    # Title row
    last_col = get_column_letter(len(headers))
    ws.merge_cells(f"A1:{last_col}1")
    t = ws["A1"]
    t.value = title
    t.font = title_font()
    t.alignment = Alignment(horizontal="center", vertical="center")
    t.fill = PatternFill("solid", fgColor=ORANGE_DARK)
    ws.row_dimensions[1].height = 26

    # Header row
    for c, h in enumerate(headers, 1):
        cell = ws.cell(row=2, column=c, value=h)
        cell.font = header_font()
        cell.alignment = Alignment(horizontal="center", vertical="center", wrap_text=True)
        cell.fill = PatternFill("solid", fgColor=ORANGE_MID)
        cell.border = BORDER
    ws.row_dimensions[2].height = 38

    # Data rows
    for r, row in enumerate(rows, start=3):
        band = ORANGE_BAND if r % 2 == 0 else ORANGE_LIGHT
        for c, val in enumerate(row, start=1):
            cell = ws.cell(row=r, column=c, value=val)
            cell.font = cell_font()
            cell.alignment = Alignment(horizontal="left", vertical="top", wrap_text=True)
            cell.fill = PatternFill("solid", fgColor=band)
            cell.border = BORDER

    # Column widths
    if col_widths:
        for i, w in enumerate(col_widths, 1):
            ws.column_dimensions[get_column_letter(i)].width = w
    else:
        for i in range(1, len(headers) + 1):
            ws.column_dimensions[get_column_letter(i)].width = 22

    ws.freeze_panes = "A3"


wb = Workbook()
wb.remove(wb.active)

# =====================================================================
# Sheet 1: UserStories
# =====================================================================
us_headers = [
    "User Story Id", "User Story Name", "Feature Id", "As a/an", "I Want to",
    "so that", "Author", "Created on", "Reviewed by", "Priority",
    "Predecessor User Story", "Successor User Story", "Acceptance Criteria",
]
us_rows = [
    ["US001", "Calculate Car Loan EMI", "F01", "Car buyer",
     "Calculate EMI for a Rs.15 Lakh car loan at 9.5% interest for 1 year tenure",
     "I can plan my monthly outflow before signing the agreement",
     "QA Lead - INTQEA26QE003", "2026-05-19", "Test Manager", "High",
     "-", "US002",
     "Given inputs (15L, 9.5%, 1yr), the site displays EMI = Rs.1,31,524 (±Rs.2 tolerance)"],

    ["US002", "View First Month Interest & Principal Split", "F01", "Car buyer",
     "See the principal vs interest break-up for the very first EMI",
     "I understand how much of my early payments go toward interest",
     "QA Lead - INTQEA26QE003", "2026-05-19", "Test Manager", "High",
     "US001", "US003",
     "First month interest = Rs.11,875 and principal = Rs.1,19,649 (±Rs.2 tolerance)"],

    ["US003", "Export Car Loan EMI Summary to Excel", "F01", "Finance team analyst",
     "Export the calculated car loan EMI summary into an Excel workbook",
     "I can share the numbers with stakeholders offline",
     "QA Lead - INTQEA26QE003", "2026-05-19", "Test Manager", "Medium",
     "US002", "-",
     "CarLoan_EMI_Summary.xlsx contains principal, rate, tenure, EMI, first-month split, total interest"],

    ["US004", "Navigate to Home Loan Calculator via Menu", "F02", "Home loan applicant",
     "Open the dedicated Home Loan EMI Calculator page from the top menu",
     "I can use the page tailored to home loans (taxes, insurance, prepayment)",
     "QA Lead - INTQEA26QE003", "2026-05-19", "Test Manager", "Medium",
     "-", "US005",
     "Clicking 'Home Loan EMI Calculator' under 'Loan Calculators & Widgets' lands on the home-loan-emi-calculator URL"],

    ["US005", "Extract Year-on-Year Payment Schedule", "F02", "Home loan applicant",
     "Fill the home loan form and see the year-on-year amortisation table",
     "I can plan tax benefit claims and prepayment strategy year by year",
     "QA Lead - INTQEA26QE003", "2026-05-19", "Test Manager", "High",
     "US004", "US006",
     "Schedule shows >= 10 yearly rows with columns: Year, Principal, Interest, Total, Balance, Paid To Date"],

    ["US006", "Persist Yearly Schedule to Excel", "F02", "Home loan applicant",
     "Save the extracted year-on-year schedule to an Excel file",
     "I can compare it against quotes from multiple banks",
     "QA Lead - INTQEA26QE003", "2026-05-19", "Test Manager", "Medium",
     "US005", "-",
     "HomeLoan_YearlySchedule.xlsx exists on disk, has YearlySchedule sheet with >=10 data rows"],

    ["US007", "Validate Loan Calculator UI Inputs", "F03", "QA tester",
     "Confirm every text box and slider on the EMI Calculator is enabled and rendered",
     "Users can edit any field and the controls are operable",
     "QA Lead - INTQEA26QE003", "2026-05-19", "Test Manager", "High",
     "-", "US008",
     "Loan Amount, Interest, Tenure text boxes enabled; all three sliders displayed"],

    ["US008", "Tenure Unit Toggle Changes Slider Scale", "F03", "Loan calculator user",
     "Switch the tenure between Year and Month and see the slider scale update",
     "I can plan tenures in my preferred unit and see the right tick marks",
     "QA Lead - INTQEA26QE003", "2026-05-19", "Test Manager", "Medium",
     "US007", "US009",
     "Scale signature differs between Yr and Mo modes and returns to original on flip-back"],

    ["US009", "Reuse UI Validation Across Sub-Calculators", "F03", "QA tester",
     "Re-apply the same UI validation on Loan Amount and Loan Tenure calculators",
     "Quality bar is consistent across all three sub-calculators",
     "QA Lead - INTQEA26QE003", "2026-05-19", "Test Manager", "Medium",
     "US008", "-",
     "Same validateInputs() helper passes on EMI / Amount / Tenure sub-tabs"],

    ["US010", "Parallel Multi-Browser Execution", "F04", "QA Lead",
     "Run the entire test suite in parallel on Chrome and Edge",
     "Both browsers are covered without doubling the wall-clock time",
     "QA Lead - INTQEA26QE003", "2026-05-19", "Test Manager", "High",
     "-", "-",
     "testng.xml parallel='tests', Chrome and Edge runners both execute the 10 scenarios; Firefox kept disabled"],
]
ws1 = wb.create_sheet("UserStories")
write_sheet(ws1, "User Stories - EMI Calculator Hackathon (INTQEA26QE003)",
            us_headers, us_rows,
            col_widths=[12, 32, 10, 14, 38, 38, 22, 13, 16, 10, 18, 18, 50])

# =====================================================================
# Sheet 2: TestScenarios
# =====================================================================
ts_headers = ["Module", "Scenario ID", "Scenario Title", "Scenario Description", "Requirement ID"]
ts_rows = [
    ["Car Loan", "TS01", "Verify EMI calculation for 15L / 9.5% / 1yr",
     "Compute expected EMI using the formula and compare against the displayed value on emicalculator.net within rupee tolerance.", "R01"],
    ["Car Loan", "TS02", "Verify first month interest amount",
     "Expand the year row and read the first monthly entry; compare interest column to expected P*r value.", "R02"],
    ["Car Loan", "TS03", "Verify first month principal amount",
     "Expand the year row and read the first monthly entry; compare principal column to (EMI - first month interest).", "R02"],
    ["Car Loan", "TS04", "Export car loan EMI summary to Excel",
     "Write principal, rate, tenure, EMI, first-month split and total interest into a workbook via Apache POI.", "R03"],
    ["Home Loan", "TS05", "Navigate to Home Loan Calculator via top menu",
     "Hover the Loan Calculators & Widgets menu, click 'Home Loan EMI Calculator' and verify the URL.", "R04"],
    ["Home Loan", "TS06", "Extract year-on-year schedule and store in Excel",
     "Fill the form, read every yearlypaymentdetails row from the schedule table, and persist to .xlsx.", "R05"],
    ["Loan Calculator", "TS07", "EMI Calculator UI inputs and sliders are operable",
     "Open the EMI sub-tab. Validate amount, interest and tenure text boxes are enabled and all sliders are displayed.", "R06"],
    ["Loan Calculator", "TS08", "Tenure unit toggle changes slider scale",
     "Capture tenure scale tick markers. Toggle Year to Month, capture again, and assert the signature changed (then flips back).", "R07"],
    ["Loan Calculator", "TS09", "Reuse same UI validation on Loan Amount Calculator",
     "Switch to Loan Amount sub-tab and rerun the same validation helper to confirm reuse.", "R08"],
    ["Loan Calculator", "TS10", "Reuse same UI validation on Loan Tenure Calculator",
     "Switch to Loan Tenure sub-tab and rerun the same validation helper to confirm reuse.", "R08"],
]
ws2 = wb.create_sheet("TestScenarios")
write_sheet(ws2, "Test Scenarios - EMI Calculator", ts_headers, ts_rows,
            col_widths=[18, 12, 42, 70, 14])

# =====================================================================
# Sheet 3: TestCases  (one row per step, header info on first step row)
# =====================================================================
tc_headers = [
    "Group", "Test Case ID", "Test Description", "Designer", "Test Type",
    "Test Data", "Complexity", "Preconditions",
    "Steps", "Step Description", "Step Expected Results",
    "Actual Result", "Status", "Defect",
]

def tc(group, tcid, desc, designer, ttype, data, complexity, pre, steps):
    """Build rows for one test case. steps = list of (step_no, step_desc, step_expected)."""
    rows = []
    for i, (sno, sdesc, sexp) in enumerate(steps):
        if i == 0:
            rows.append([group, tcid, desc, designer, ttype, data, complexity, pre,
                         sno, sdesc, sexp, "Not Executed", "Not Run", "-"])
        else:
            rows.append(["", "", "", "", "", "", "", "",
                         sno, sdesc, sexp, "Not Executed", "Not Run", "-"])
    return rows

tc_rows = []

tc_rows += tc("Car Loan", "TC01",
    "Verify EMI is correctly calculated for Rs.15,00,000 at 9.5% for 1 year",
    "QA Engineer", "Functional + BDD",
    "Principal=1500000; Rate=9.5; Tenure=1 Year", "Medium",
    "Browser launched; homepage open; internet available",
    [
        ("S1", "Navigate to https://emicalculator.net/", "Homepage loads with EMI calculator form visible"),
        ("S2", "Click the 'Car Loan' tab", "Car Loan tab becomes active, form re-renders for car loan"),
        ("S3", "Enter Loan Amount = 1500000", "Amount field shows '15,00,000'"),
        ("S4", "Enter Interest = 9.5", "Interest field shows 9.5"),
        ("S5", "Enter Tenure = 1, unit = Yr", "Tenure shows 1 with Yr toggle active"),
        ("S6", "Read EMI from #emiamount", "EMI = Rs.1,31,524 (+/- Rs.2)"),
    ])

tc_rows += tc("Car Loan", "TC02",
    "Verify first month interest amount equals Principal x monthly rate",
    "QA Engineer", "Functional + BDD",
    "Principal=1500000; Rate=9.5; Tenure=1 Year", "Medium",
    "Car Loan tab filled with the loan inputs",
    [
        ("S1", "Set up car loan inputs (15L / 9.5% / 1 Yr)", "EMI computed and displayed"),
        ("S2", "Scroll to the schedule table", "Yearly schedule visible"),
        ("S3", "Click the year row for the current year to expand monthly view", "Monthly table for current year becomes visible"),
        ("S4", "Read the first month's Interest cell", "Interest = Rs.11,875 (+/- Rs.2)"),
    ])

tc_rows += tc("Car Loan", "TC03",
    "Verify first month principal amount equals (EMI - first month interest)",
    "QA Engineer", "Functional + BDD",
    "Principal=1500000; Rate=9.5; Tenure=1 Year", "Medium",
    "Car Loan tab filled with the loan inputs",
    [
        ("S1", "Set up car loan inputs (15L / 9.5% / 1 Yr)", "EMI computed and displayed"),
        ("S2", "Expand the current year row in the schedule", "Monthly rows visible"),
        ("S3", "Read the first month's Principal cell", "Principal = Rs.1,19,649 (+/- Rs.2)"),
    ])

tc_rows += tc("Car Loan", "TC04",
    "Verify Car Loan EMI summary is exported to an Excel file with all key fields",
    "QA Engineer", "Functional + Integration",
    "Principal=1500000; Rate=9.5; Tenure=1 Year", "Medium",
    "Test data set in form; output/ directory writable",
    [
        ("S1", "Fill car loan form and capture EMI, first-month split, total interest", "Values read into context"),
        ("S2", "Invoke ExcelUtils.writeSheet(...) to output/CarLoan_EMI_Summary.xlsx", "Workbook created; no IOException"),
        ("S3", "Read row count of 'CarLoanEMI' sheet", "Row count >= 2 (header + data rows)"),
    ])

tc_rows += tc("Home Loan", "TC05",
    "Verify Home Loan EMI Calculator page opens from the top menu",
    "QA Engineer", "UI Navigation + BDD",
    "Menu: 'Loan Calculators & Widgets' > 'Home Loan EMI Calculator'", "Low",
    "Homepage loaded",
    [
        ("S1", "Hover the 'Loan Calculators & Widgets' menu", "Dropdown becomes visible"),
        ("S2", "Click 'Home Loan EMI Calculator' link", "Browser navigates to the dedicated page"),
        ("S3", "Assert current URL contains 'home-loan-emi-calculator'", "URL match passes"),
    ])

tc_rows += tc("Home Loan", "TC06",
    "Extract year-on-year payment schedule and write it to Excel",
    "QA Engineer", "Functional + Integration",
    "Principal=2500000; Rate=8.5; Tenure=20 years", "High",
    "Home Loan EMI Calculator page open",
    [
        ("S1", "Enter loan inputs and trigger calculation", "Schedule table renders below"),
        ("S2", "Scroll to the year-on-year schedule and wait for it to be present", "All yearly rows visible"),
        ("S3", "Iterate over tr.yearlypaymentdetails rows and collect cell values", "2D data grid built with header + 20 rows"),
        ("S4", "Assert at least 10 yearly rows are present", "Row count assertion passes"),
        ("S5", "Write the grid to output/HomeLoan_YearlySchedule.xlsx via Apache POI", "Workbook saved successfully"),
        ("S6", "Assert file exists on disk and size > 0", "File assertion passes"),
    ])

tc_rows += tc("Loan Calculator", "TC07",
    "Verify EMI Calculator sub-tab: all text boxes are enabled and sliders displayed",
    "QA Engineer", "UI + BDD",
    "URL: emicalculator.net/loan-calculator/", "Low",
    "Loan Calculator page loaded",
    [
        ("S1", "Click the 'EMI Calculator' sub-tab", "EMI Calculator tab is active"),
        ("S2", "Check Loan Amount text box is enabled", "isEnabled() returns true"),
        ("S3", "Check Interest text box is enabled", "isEnabled() returns true"),
        ("S4", "Check Loan Tenure text box is enabled", "isEnabled() returns true"),
        ("S5", "Check all three sliders are displayed", "All sliders visible in DOM"),
    ])

tc_rows += tc("Loan Calculator", "TC08",
    "Verify tenure unit toggle (Year <-> Month) changes the slider scale",
    "QA Engineer", "UI + BDD",
    "EMI Calculator sub-tab active", "Medium",
    "Default tenure unit = Yr",
    [
        ("S1", "Capture tenure scale tick markers (signature A)", "Signature A captured (e.g. '0|5|10|15|20|25|30|')"),
        ("S2", "Click tenure unit = Mo", "Slider scale repaints with month-level ticks"),
        ("S3", "Capture tenure scale signature B and assert B != A", "Signature B differs from A"),
        ("S4", "Click tenure unit = Yr", "Slider scale repaints"),
        ("S5", "Capture tenure scale signature C and assert C == A", "Signature returns to original"),
    ])

tc_rows += tc("Loan Calculator", "TC09",
    "Re-use the same UI validation on the Loan Amount Calculator sub-tab",
    "QA Engineer", "UI Reuse + BDD",
    "URL: emicalculator.net/loan-calculator/", "Low",
    "Loan Calculator page loaded",
    [
        ("S1", "Click the 'Loan Amount Calculator' sub-tab", "Tab becomes active"),
        ("S2", "Run the validateInputs() helper", "Returns UIValidationResult"),
        ("S3", "Assert amount, interest, tenure boxes are enabled", "Assertion passes"),
        ("S4", "Assert all sliders are displayed", "Assertion passes"),
    ])

tc_rows += tc("Loan Calculator", "TC10",
    "Re-use the same UI validation on the Loan Tenure Calculator sub-tab",
    "QA Engineer", "UI Reuse + BDD",
    "URL: emicalculator.net/loan-calculator/", "Low",
    "Loan Calculator page loaded",
    [
        ("S1", "Click the 'Loan Tenure Calculator' sub-tab", "Tab becomes active"),
        ("S2", "Run the validateInputs() helper", "Returns UIValidationResult"),
        ("S3", "Assert amount, interest, EMI boxes are enabled", "Assertion passes"),
        ("S4", "Assert all sliders are displayed", "Assertion passes"),
    ])

ws3 = wb.create_sheet("TestCases")
write_sheet(ws3, "Test Cases - EMI Calculator", tc_headers, tc_rows,
            col_widths=[15, 12, 38, 14, 16, 28, 12, 30, 8, 38, 36, 16, 14, 12])

# =====================================================================
# Sheet 4: RTM
# =====================================================================
rtm_headers = ["Serial No", "Feature ID", "Requirement ID", "Requirement Description",
               "Test Scenario ID", "Test Case ID", "Defect ID", "Remarks"]
rtm_rows = [
    [1,  "F01", "R01", "Car loan EMI shall match the EMI formula within Rs.2 tolerance",        "TS01", "TC01", "-",    "Core calculation - smoke"],
    [2,  "F01", "R02", "First month interest shall equal Principal x monthly rate",             "TS02", "TC02", "-",    "Verified on year row for current year"],
    [3,  "F01", "R02", "First month principal shall equal EMI minus first month interest",      "TS03", "TC03", "-",    "Same scenario, principal column"],
    [4,  "F01", "R03", "Car loan EMI summary shall be exportable to Excel",                     "TS04", "TC04", "-",    "Uses Apache POI"],
    [5,  "F02", "R04", "Home Loan calculator shall be reachable from the top menu",             "TS05", "TC05", "-",    "Menu hover + click"],
    [6,  "F02", "R05", "Year-on-year payment schedule shall be extractable and storable to Excel", "TS06", "TC06", "DEF001", "First run flake possible due to ad reflow"],
    [7,  "F03", "R06", "EMI Calculator sub-tab text boxes and sliders shall be operable",       "TS07", "TC07", "-",    "Baseline UI check"],
    [8,  "F03", "R07", "Switching tenure Year <-> Month shall change the slider scale",         "TS08", "TC08", "-",    "Signature comparison"],
    [9,  "F03", "R08", "The same UI validation shall be reusable across all 3 sub-calculators", "TS09", "TC09", "-",    "Loan Amount calculator"],
    [10, "F03", "R08", "The same UI validation shall be reusable across all 3 sub-calculators", "TS10", "TC10", "-",    "Loan Tenure calculator"],
    [11, "F04", "R09", "Suite shall execute in parallel on Chrome and Edge (Firefox excluded)", "-",    "-",    "-",    "Covered by testng.xml + per-browser runners"],
    [12, "F04", "R10", "Failures shall capture a screenshot attached to the test report",       "-",    "-",    "-",    "Hooks.afterScenario + Extent adapter"],
]
ws4 = wb.create_sheet("RTM")
write_sheet(ws4, "Requirements Traceability Matrix (RTM)", rtm_headers, rtm_rows,
            col_widths=[10, 10, 14, 60, 14, 12, 10, 36])

# =====================================================================
# Sheet 5: Defects
# =====================================================================
def_headers = ["Serial No", "Defect ID", "Description", "Reproducible",
               "Steps to Reproduce", "Severity", "Priority", "Reported By",
               "Status", "Remarks"]
def_rows = [
    [1, "DEF001",
     "Year-on-year schedule extraction occasionally times out on first run due to ad blocks reflowing the table position",
     "Intermittent",
     "1) Open https://emicalculator.net/home-loan-emi-calculator/ in a slow network\n2) Fill 25L / 8.5% / 20yr\n3) Immediately scroll to schedule and try to extract rows",
     "Medium", "Medium", "Automation Suite (TC06)", "Open",
     "Workaround: explicit wait + scrollBy(0,800) added in HomeLoanPage.extractYearlySchedule()"],

    [2, "DEF002",
     "Tenure scale signature returns a different ordering on Edge vs Chrome under high zoom (>=150%)",
     "Reproducible",
     "1) Set browser zoom to 150%\n2) Open Loan Calculator > EMI tab\n3) Capture scale signature\n4) Compare across Chrome and Edge",
     "Low", "Low", "QA Engineer", "Open",
     "Cosmetic - does not affect functional flow; defer to next sprint"],

    [3, "DEF003",
     "When tenure is entered in 'Mo' before selecting Car Loan tab, the loan amount slider re-snaps to default",
     "Reproducible",
     "1) Open homepage (Home Loan default)\n2) Switch tenure to Mo, enter 24\n3) Switch to Car Loan tab\n4) Observe loan amount has reset to 3,00,000",
     "Low", "Low", "QA Engineer", "Closed - Won't Fix",
     "Third-party site behaviour - not under our control"],

    [4, "DEF004",
     "First month row sometimes reads stale value if the schedule has not finished animating",
     "Intermittent",
     "1) Run TC02 in headed mode at fast speed\n2) Observe occasional 1-rupee mismatch",
     "Low", "Medium", "Automation Suite (TC02)", "Open",
     "Add explicit wait on visibility of the inner monthly tbody before reading"],
]
ws5 = wb.create_sheet("Defects")
write_sheet(ws5, "Defects Log - EMI Calculator", def_headers, def_rows,
            col_widths=[10, 10, 50, 14, 50, 12, 12, 22, 18, 40])

wb.save(OUT)
print(f"Saved {OUT}")
