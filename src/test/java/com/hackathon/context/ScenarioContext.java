package com.hackathon.context;

import com.hackathon.pages.HomeLoanPage;
import com.hackathon.pages.HomePage;
import com.hackathon.pages.LoanCalculatorPage;
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

    public final SoftAssertions softly = new SoftAssertions();

    private final Map<String, Object> bag = new HashMap<>();

    // Stores an arbitrary value under key for later retrieval by other step defs.
    public void put(String key, Object value) { bag.put(key, value); }

    // Retrieves a value previously stored via put(); cast to the call-site type.
    @SuppressWarnings("unchecked")
    public <T> T get(String key) { return (T) bag.get(key); }
}
