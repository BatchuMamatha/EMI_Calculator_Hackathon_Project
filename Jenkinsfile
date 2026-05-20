// =====================================================================
// Jenkins declarative pipeline for the EMI Calculator hackathon project.
// Requires:
//   - JDK 17 installed on the agent and named "JDK17" in Jenkins Global Tools
//   - Maven 3.9+ installed and named "Maven3" in Jenkins Global Tools
//   - Chrome and Edge installed on the agent (Selenium Manager fetches drivers)
//   - Optional: Allure Commandline configured as "Allure" for the Allure step
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
        choice(name: 'TAG',
               choices: ['', '@Smoke', '@Regression', '@CarLoan', '@HomeLoan', '@LoanCalculator', '@UI'],
               description: 'Optional Cucumber tag filter')
        booleanParam(name: 'HEADLESS', defaultValue: true,
                     description: 'Run browsers headless (recommended on CI)')
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

        stage('Test (parallel Chrome + Edge)') {
            steps {
                script {
                    def tagArg = params.TAG?.trim() ? "-Dcucumber.filter.tags=\"${params.TAG}\"" : ''
                    bat "mvn -B test -Dheadless=${params.HEADLESS} ${tagArg}"
                }
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Publish Reports') {
            steps {
                publishHTML(target: [
                    reportName : 'Extent Report',
                    reportDir  : 'reports/extent',
                    reportFiles: 'SparkReport.html',
                    keepAll    : true, allowMissing: true, alwaysLinkToLastBuild: true
                ])
                publishHTML(target: [
                    reportName : 'Cucumber Chrome',
                    reportDir  : 'reports/cucumber',
                    reportFiles: 'chrome-cucumber.html',
                    keepAll    : true, allowMissing: true, alwaysLinkToLastBuild: true
                ])
                publishHTML(target: [
                    reportName : 'Cucumber Edge',
                    reportDir  : 'reports/cucumber',
                    reportFiles: 'edge-cucumber.html',
                    keepAll    : true, allowMissing: true, alwaysLinkToLastBuild: true
                ])

                // If Allure plugin is installed, this will render the dashboard.
                allure includeProperties: false,
                       jdk: '',
                       results: [[path: 'target/allure-results']]
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts:
                'output/**/*.xlsx, logs/**/*.log, reports/**/*, screenshots/**/*, target/allure-results/**',
                allowEmptyArchive: true
        }
        failure {
            echo 'Build failed — see archived screenshots under screenshots/.'
        }
    }
}
