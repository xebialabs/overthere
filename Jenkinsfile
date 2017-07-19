#!groovy

pipeline {
    agent none

    options {
        buildDiscarder(logRotator(numToKeepStr: '10', artifactDaysToKeepStr: '7', artifactNumToKeepStr: '5'))
        skipDefaultCheckout()
        timeout(time: 2, unit: 'HOURS')
        timestamps()
        ansiColor('xterm')
    }

    stages {

        // TODO do the normal build with unit tests

        stage('Overcast Setup') {
            agent {
                node {
                    label 'linux'
                }
            }
            tools {
                jdk 'JDK 8u60'
            }
            steps {
                checkout scm
                echo 'test'

                sh './gradlew overcastSetup -i'
            }
            post {
                success {
                    stash includes: 'build/overcast/instances.json', name: 'overcast-instances'
                }
            }
        }

        stage('Integration Test') {
            agent {
                node {
                    label 'linux'
                }
            }
            tools {
                jdk 'JDK 8u60'
            }

            steps {
                parallel("ITest Linux": {
                    node('linux') {
                        checkout scm
                        unstash name: 'overcast-instances'
                        echo 'test'
                    }
                },
                "ITest Windows": {
                    node('windows') {
                        checkout scm
                        unstash name: 'overcast-instances'
                        echo 'test'
                    }
                })
            }
            post {
                always {
                    checkout scm
                    unstash name: 'overcast-instances'
                    echo 'test'
                    // sh './gradlew overcastTeardown -i'
                }
            }

        }

    }

}
