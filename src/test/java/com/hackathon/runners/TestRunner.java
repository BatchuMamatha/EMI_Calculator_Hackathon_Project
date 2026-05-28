package com.hackathon.runners;

import com.hackathon.base.BaseClass;
import io.cucumber.testng.CucumberOptions;

// Single runner for all browsers.
// testng.xml passes "browser" as a @Parameter; BaseClass reads it in @BeforeClass.
// Cucumber report file paths include the browser name (set as system properties in
// testng.xml) so Chrome and Edge never write to the same file simultaneously.
@CucumberOptions(
        features = "src/test/resources/features",
        glue = {
                "com.hackathon.stepdefinitions",
                "com.hackathon.hooks"
        },
        tags = "@Smoke or @Regression or @UI",
        plugin = {
                "pretty",
                "html:#{systemProperty.cucumber.report.html}",
                "json:#{systemProperty.cucumber.report.json}",
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
        },
        monochrome = true,
        publish = false
)
public class TestRunner extends BaseClass {
    // All driver lifecycle and parallel execution logic lives in BaseClass.
}
