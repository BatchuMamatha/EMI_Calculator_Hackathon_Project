package com.hackathon.runners;

import com.hackathon.base.BaseClass;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(
        features = "src/test/resources/features",
        glue = {
                "com.hackathon.stepdefinitions",
                "com.hackathon.hooks"
        },
        // Default tag expression: run everything tagged @Smoke OR @Regression
        // OR @UI. Override at runtime with -Dcucumber.filter.tags="<expression>".
        tags = "@Smoke or @Regression or @UI",
        plugin = {
                "pretty",
                "html:reports/cucumber/cucumber.html",
                "json:reports/cucumber/cucumber.json",
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
        },
        monochrome = true,
        publish = false
)
public class TestRunner extends BaseClass {
    // All driver lifecycle + parallel execution lives in BaseClass.
}
