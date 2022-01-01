package Uber.controller;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import io.cucumber.testng.CucumberOptions.SnippetType;

/**
 *
 * @author Kent Yeh
 */
@CucumberOptions(plugin = {"pretty", "html:target/cucumber-reports/cucumber.html", "json:target/cucumber-reports/cucumber.json"}, snippets = SnippetType.CAMELCASE,
        features = "src/test/resources/features", glue = "Uber.cucumber")
public class TestCucumber extends AbstractTestNGCucumberTests {

}
