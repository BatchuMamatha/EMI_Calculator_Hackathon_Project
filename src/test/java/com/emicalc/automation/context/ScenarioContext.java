package com.emicalc.automation.context;

import com.emicalc.automation.pages.HomeLoanPage;
import com.emicalc.automation.pages.HomePage;
import com.emicalc.automation.pages.LoanCalculatorPage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Per-scenario, per-thread state shared across step-def classes. Avoids
 * static fields (which break parallel execution) and avoids passing page
 * objects through every step.
 */
public class ScenarioContext {

    public HomePage homePage;
    public HomeLoanPage homeLoanPage;
    public LoanCalculatorPage loanCalculatorPage;

    public List<List<String>> extractedSchedule;
    public String tenureScaleSignatureBefore;
    public String tenureScaleSignatureAfter;

    private final Map<String, Object> bag = new HashMap<>();

    public void put(String key, Object value) { bag.put(key, value); }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) { return (T) bag.get(key); }
}
