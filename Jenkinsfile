#!groovy
@Library('devtools-global-library') _

def projectName = "thidwick-examples"
def serviceName = "thidwick-starter"

def repo = "git@orahub.oraclecorp.com:appdev-cloud-rnpd/thidwick-starter.git"
def directory = "thidwick-starter"

def ver
def isMaster = (env.gitlabSourceBranch == env.gitlabTargetBranch && env.gitlabTargetBranch == "master")
def server = "ArtifactoryDenver"
def gradleBuildInfo = null
def artifactory_repo = "osvc-release-local"


pipeline {

    agent {
        label "thidwick-base"
    }

    options {
        timestamps()
        durabilityHint('PERFORMANCE_OPTIMIZED')
        buildDiscarder(logRotator(numToKeepStr: "30"))
        gitlabCommitStatus("${serviceName}")
    }


    stages {

        stage("Clone") {
            options {
                timeout(time: 5, unit: 'MINUTES')
            }
            steps {
                deleteDir()

                script {
                    orahubSCM {
                        repoUrl = repo
                        checkOutDir = directory
                    }

                    dir("${env.WORKSPACE}/${directory}") {
                        def props = readProperties file: "version.properties"
                        ver = "${props.major}.${props.minor}.${props.patch}"
                        if (isMaster) {
                            ver = "${ver}.${BUILD_NUMBER}"
                        } else {
                            ver = "${ver}.${BUILD_NUMBER}-SNAPSHOT"
                        }

                        currentBuild.displayName = "${ver}"
                    }
                }
            }
        }

        stage("Build") {
            environment {
                BUILD_VERSION = "${ver}"
            }
            steps {
                script {
                    gradleBuildInfo = osvcGradle {
                        gradleTool = "local"
                        serverID = "${server}"
                        usesGradlePlugin = true
                        deployRepo = artifactory_repo
                        rootDirectory = directory
                        buildGradleFile = "build.gradle"
                        gradleTasks = "build"
                    }
                }
            }
        }

        stage("Push to Docker") {
            options {
                timeout(time: 5, unit: 'MINUTES')
            }
            steps {
                dir("${env.WORKSPACE}/${directory}") {
                    sh "docker build -t ${projectName}/${serviceName}:latest ."
                }
            }
            post {
                success {
                    script {
                        osvcOciRegistryDocker {
                            sourceTag = "${projectName}/${serviceName}:latest"
                            destinationTag = "${serviceName}:${ver}"
                            dockerRegistry = "DEV"
                            reponame = "${projectName}"
                        }

                        //stash includes: "source/helm/**", name: 'ServiceChart'
                    }
                }
            }
        }

    }

}

