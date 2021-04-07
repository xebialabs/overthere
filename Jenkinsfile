#!groovy

@Library('jenkins-pipeline-libs@master')

import com.xebialabs.pipeline.utils.Branches
import com.xebialabs.pipeline.globals.Globals
import com.xebialabs.pipeline.utils.Touch

pipeline {

    agent none

    environment {
        REPOSITORY_NAME = 'overthere'
        GRADLE_OPTS = "-XX:MaxPermSize=256m -Xmx1024m  -Djsse.enableSNIExtension=true"
    }

    stages {
        stage('Run test') {
            agent {
                label 'xlr||xld'
            }
            steps {
                sh "./gradlew clean test"
            }
        }
        stage('Run integration test') {
            agent {
                label 'xlr||xld'
            }
            steps {
                sh "./gradlew clean itest -Paws.endpoint=https://ec2.eu-west-1.amazonaws.com -Paws.accessKey=${AWS_ACCESS_KEY_ID} -Paws.secretKey=${AWS_SECRET_ACCESS_KEY}"
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