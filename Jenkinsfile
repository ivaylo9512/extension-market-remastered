pipeline {
    agent any
    triggers {
        pollSCM '* * * * *'
    }
    tools {
        jdk 'jdk-16'
    }
    stages {
        stage('Build') {
            steps {
                sh 'java -version'
                sh "chmod +x gradlew"
                sh './gradlew assemble'
            }
        }
        stage('Test') {
            steps {
                sh 'java -version'
                sh "chmod +x gradlew"
                sh './gradlew test'
            }
        }
        stage('Publish Test Coverage Report') {
            steps {
                step([$class: 'JacocoPublisher',
                    execPattern: '**/build/jacoco/*.exec',
                    classPattern: '**/build/classes',
                    sourcePattern: 'src/main/java',
                    exclusionPattern: 'src/test*'
                ])
                sh 'curl -Os https://uploader.codecov.io/latest/linux/codecov'
                sh 'chmod +x codecov'
                sh './codecov -t 4bc3bf10-3fac-4553-adb6-7e0fee35e0ad'
            }
        }
    }
}