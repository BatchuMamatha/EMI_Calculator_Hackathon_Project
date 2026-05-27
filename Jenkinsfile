// =====================================================================
// Jenkins declarative pipeline for the EMI Calculator hackathon project.
// Requires the following plugins:
//   Pipeline, Git, GitHub, Timestamper, Build Timeout, JUnit, HTML Publisher,
//   Allure Jenkins Plugin, TestNG Results (optional), Pipeline Stage View
// Global Tools (Manage Jenkins → Tools):
//   JDK 17 named "JDK17", Maven 3.9+ named "Maven3"
// =====================================================================
pipeline {
    agent any

    tools {
        jdk   'JDK17'
        maven 'Maven3'
    }

    options {
        timestamps()
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    parameters {
        choice(
            name: 'TAG',
            choices: ['', '@Smoke', '@Regression', '@UI', '@CarLoan', '@HomeLoan', '@LoanCalculator'],
            description: 'Optional Cucumber tag filter'
        )
        booleanParam(
            name: 'HEADLESS',
            defaultValue: true,
            description: 'Run browsers headless (recommended on CI)'
        )
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                bat 'mvn -B clean compile'
            }
        }

        stage('Test (Chrome + Edge in parallel)') {
            steps {
                script {
                    def tagArg = params.TAG?.trim() ? "-Dcucumber.filter.tags=\"${params.TAG}\"" : ''
                    // 'verify' triggers maven-cucumber-reporting to convert
                    // reports/cucumber/cucumber.json into a static HTML
                    // report under reports/cucumber-html/. -DskipITs avoids
                    // running integration tests (we have none).
                    bat "mvn -B verify -Dheadless=${params.HEADLESS} ${tagArg} -DskipITs"
                }
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'reports/testng/*.xml'
                }
            }
        }

        stage('Publish Reports') {
            steps {
                // Extent Spark report — filenames are timestamped
                // (ExtentReport_<yyyy.MM.dd_HH.mm.ss>.html), so the HTML
                // Publisher uses a glob. The 'keepAll' flag preserves each
                // run's report under the build's artefacts.
                publishHTML(target: [
                    reportName            : 'Extent Report',
                    reportDir             : 'reports/extent',
                    reportFiles           : 'ExtentReport_*.html',
                    keepAll               : true,
                    allowMissing          : true,
                    alwaysLinkToLastBuild : true
                ])

                // Static Cucumber HTML produced by net.masterthought:
                // maven-cucumber-reporting (renders properly from file://).
                publishHTML(target: [
                    reportName            : 'Cucumber Report',
                    reportDir             : 'reports/cucumber-html',
                    reportFiles           : 'overview-features.html',
                    keepAll               : true,
                    allowMissing          : true,
                    alwaysLinkToLastBuild : true
                ])

                // Allure dashboard
                allure(properties: [], results: [[path: 'target/allure-results']])
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts:
                'output/**/*.xlsx, logs/**/*.log, reports/**/*, reports/cucumber-html/**, reports/testng/**, screenshots/**/*, target/allure-results/**',
                allowEmptyArchive: true
        }
        failure {
            echo 'Build failed — see archived screenshots under screenshots/.'
        }
    }
}
