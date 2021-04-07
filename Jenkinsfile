#!groovy

@Library('jenkins-pipeline-libs@master')

import com.xebialabs.pipeline.utils.Branches
import com.xebialabs.pipeline.globals.Globals
import com.xebialabs.pipeline.utils.Touch

pipeline {

    agent none

    parameters {
        string( name: 'jdkVersion',
                defaultValue: 'JDK 8u191',
                description: 'Configuration to run server on an environment with designated jdk version')
        string(name: 'slaveNode', defaultValue: 'xlr||xld', description: 'Node label where steps would be executed.')
    }

    environment {
        REPOSITORY_NAME = 'overthere'
        GRADLE_OPTS = "-XX:MaxPermSize=256m -Xmx1024m  -Djsse.enableSNIExtension=true"
    }

    stages {
        stage('Run test') {
            agent { label params.slaveNode }
            steps {
                withEnv(Globals.javaEnv(this)) {
                    sh "./gradlew clean test"
                }
            }
        }
        stage('Run integration test') {
            agent { label params.slaveNode }
            steps {
                withEnv(Globals.javaEnv(this)) {
                    sh "./gradlew clean itest -Paws.endpoint=https://ec2.eu-west-1.amazonaws.com -Paws.accessKey=${AWS_ACCESS_KEY_ID} -Paws.secretKey=${AWS_SECRET_ACCESS_KEY}"
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