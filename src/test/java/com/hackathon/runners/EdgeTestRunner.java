package com.hackathon.runners;

import com.hackathon.base.BaseClass;
import io.cucumber.testng.CucumberOptions;

// Edge-specific runner. Intentionally has NO html/json plugins so it never
// writes to the same report files as TestRunner (Chrome). Both runners share
// Allure and Extent (thread-safe via ThreadLocal + flush in TestListener).
@CucumberOptions(
        features = "src/test/resources/features",
        glue = {
                "com.hackathon.stepdefinitions",
                "com.hackathon.hooks"
        },
        tags = "@Smoke or @Regression or @UI",
        plugin = {
                "pretty",
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
        },
        monochrome = true,
        publish = false
)
public class EdgeTestRunner extends BaseClass {
    // All driver lifecycle + parallel execution lives in BaseClass.
}
