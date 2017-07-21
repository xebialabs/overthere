#!groovy

pipeline {
    agent none

    environment {
        GRADLE_OPTS = '-XX:MaxPermSize=256m -Xmx1024m  -Djsse.enableSNIExtension=false'
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '10', artifactDaysToKeepStr: '7', artifactNumToKeepStr: '5'))
        skipDefaultCheckout()
        timeout(time: 2, unit: 'HOURS')
        timestamps()
        ansiColor('xterm')
    }

    stages {

        stage('Build') {
            tools {
                jdk 'JDK 8u60'
            }
            steps {
                parallel("Build Linux": {
                    node('linux') {
                        checkout scm
                        sh './gradlew clean test'
                    }
                },
                "Build Windows": {
                    node('windows') {
                        checkout scm
                        bat './gradlew.bat clean test'
                    }
                })
            }
        }

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
                script {
                    parallel("ITest Linux": {
                        node('linux') {
                            checkout scm
                            unstash name: 'overcast-instances'
                            try {
                                sh './gradlew itest'
                            }catch (e) {
                                echo 'Itests failed'
                                throw e
                            } finally {
                                junit '**/build/itest-results/*.xml'
                            }
                        }
                    },
                    "ITest Windows": {
                        node('windows') {
                            checkout scm
                            unstash name: 'overcast-instances'
                            try {
                                bat './gradlew.bat itest'
                            }catch (e) {
                                echo 'Itests failed'
                                throw e
                            } finally {
                                junit '**/build/itest-results/*.xml'
                            }
                        }
                    })
                }
            }
            post {
                always {
                    checkout scm
                    unstash name: 'overcast-instances'
                    echo 'post teardown'
                    sh './gradlew overcastTeardown -i'
                }
            }

        }

    }

}
