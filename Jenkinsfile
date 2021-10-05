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
                sh './codecov -t 458635cc-30eb-41f9-967a-a9299c84b7d3'
            }
        }
    }
}