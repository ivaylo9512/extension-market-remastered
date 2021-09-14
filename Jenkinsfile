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
                sh './codecov -t 74a4cd20-4a4c-40da-8fb5-2d3d388cb4fc'
            }
        }
    }
}