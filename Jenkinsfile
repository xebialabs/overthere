#!groovy

@Library('jenkins-pipeline-libs@S-83937')

import com.xebialabs.pipeline.globals.Globals

pipeline {

    agent none

    parameters {
        string( name: 'jdkVersion',
                defaultValue: 'OpenJDK 11.0.12',
                description: 'Configuration to run server on an environment with designated jdk version')
        string(name: 'slaveNode', defaultValue: 'xlr||java-17', description: 'Node label where steps would be executed.')
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
            node('xld') {
                step([$class: 'ClaimPublisher'])
            }
        }
    }
}
