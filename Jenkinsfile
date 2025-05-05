#!groovy

@Library('jenkins-pipeline-libs@master')

import com.xebialabs.pipeline.globals.Globals

pipeline {

    agent none

    parameters {
        string( name: 'jdkVersion',
                defaultValue: Globals.jdk21Version,
                description: 'Configuration to run server on an environment with designated jdk version')
        string(name: 'slaveNode', defaultValue: 'xld21', description: 'Node label where steps would be executed.')
    }
    environment {
        REPOSITORY_NAME = 'overthere'
        GRADLE_OPTS = "-XX:MaxMetaspaceSize=256m -Xmx1024m  -Djsse.enableSNIExtension=true"
    }

    stages {
        stage('Run test') {
            agent { label params.slaveNode }
            steps {
                withEnv(Globals.java21Env(this, Globals.jdk21Version)) {
                    sh "./gradlew clean test"
                }
            }
        }
        stage('Run integration test') {
            agent { label params.slaveNode }
            steps {
                withEnv(Globals.java21Env(this, Globals.jdk21Version)) {
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
            node('xld21') {
                step([$class: 'ClaimPublisher'])
            }
        }
    }
}