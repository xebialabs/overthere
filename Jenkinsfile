#!groovy

@Library('jenkins-pipeline-libs@master')

import com.xebialabs.pipeline.globals.Globals

pipeline {

    agent { label 'java-17' }

    parameters {
        string( name: 'jdkVersion',
                defaultValue: Globals.jdk17Version,
                description: 'Configuration to run server on an environment with designated jdk version')
        string(name: 'slaveNode', defaultValue: 'java-17', description: 'Node label where steps would be executed.')
    }
    environment {
        REPOSITORY_NAME = 'overthere'
        GRADLE_OPTS = "-XX:MaxPermSize=256m -Xmx1024m  -Djsse.enableSNIExtension=true"
    }

    stages {
        stage('Run test') {
            agent { label params.slaveNode }
            steps {
                withEnv(Globals.java17Env(this, Globals.jdk17Version)) {
                    sh "./gradlew clean test"
                }
            }
        }
        stage('Run integration test') {
            agent { label params.slaveNode }
            steps {
                withEnv(Globals.java17Env(this, Globals.jdk17Version)) {
                    sh "./gradlew clean itest"
                }
            }
            post {
                always {
                    junit '**/build/itest-results/*.xml'
                }
            }
        }
    }
    post {
        always {
            node('java-17') {
                step([$class: 'ClaimPublisher'])
            }
        }
    }
}