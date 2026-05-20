package com.emicalc.automation.context;

import com.emicalc.automation.pages.HomeLoanPage;
import com.emicalc.automation.pages.HomePage;
import com.emicalc.automation.pages.LoanCalculatorPage;
import org.assertj.core.api.SoftAssertions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScenarioContext {

    public HomePage homePage;
    public HomeLoanPage homeLoanPage;
    public LoanCalculatorPage loanCalculatorPage;

    public List<List<String>> extractedSchedule;
    public String tenureScaleSignatureBefore;
    public String tenureScaleSignatureAfter;

    // Soft assertions collected during the scenario; flushed in Hooks.@After
    public final SoftAssertions softly = new SoftAssertions();

    private final Map<String, Object> bag = new HashMap<>();

    public void put(String key, Object value) { bag.put(key, value); }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) { return (T) bag.get(key); }
}
