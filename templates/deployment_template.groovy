#!/usr/bin/env groovy

pipeline {
  agent {
    node {
      label "ansible"
      customWorkspace "${versions_dir}"
    }
  }
  
  triggers { pollSCM('H/2 * * * *') }

  options {
    disableConcurrentBuilds()
    timestamps()
    ansiColor('xterm')
  }

  stages {
    stage("Init") {
      when {
        anyOf {
          branch "development/*"
          branch "release/*"
        }
      }
      environment {
        // CI container environment variables
        LC_ALL = "C.UTF-8"
        LANG = "C.UTF-8"
      }
      steps {
        withCredentials( [string(credentialsId: 'key', variable: 'key') ])
        script {
          currentBuild.displayName = gitBranch
            if (notifySlack) { sendSlackNotification(appName, serviceName, gitBranch, 'Started') }
            docker.withRegistry('http://' + dockerRegistry) {
              docker.image(baseDockerImage).inside {
              try {
                sh "mvn test"
              } catch (e) {
                echo """
                        #################################
                        #  Maven has failed to succeed  #
                        #################################
                """
              }
            }
          }
        }
      }  // steps end
    }  // stage end

    // Stage with Parallel
    stage('Gathering in Parallel') {
      failFast true
      parallel {
        // First stage of the Parallel
        stage("Stage1") {
          when {
            anyOf {
              branch "development/*"
              branch "release/*"
            }
          }
          steps {
            script {
              sh "mvn clean package -DskipTests"
            }
          } // End of steps
        } // End of the First stage of Parallel

        // Second Stage of the Parallel
        stage("Stage2") {
          when { branch "development/*" }
          when { expression { env.GIT_BRANCH =~ /release/ } }
          steps {
            script {
              withCredentials([string(credentialsId: 'user', variable: 'password')])
              sh "docker login ${dockerRegistry} -u jenkins_dev -p '${password}'"
              sh """
                docker push ${releaseImageTagCurrent}
                docker rmi -f ${releaseImageTagCurrent}
              """
            }
          } // End of steps
        } // End of the Second stage of Parallel
      } // End of the Parallel
    } // End of the stage in Parallel
  } // stages end


  post{
    always {
      echo "Running post-build tasks"
      junit allowEmptyResults: true, testResults: '**/junit-report.xml'
      step([$class: 'CoberturaPublisher',
            autoUpdateHealth: false,
            autoUpdateStability: false,
            failNoReports: false,
            coberturaReportFile: '**/coverage-report.xml',
            failUnhealthy: false,
            failUnstable: false,
            maxNumberOfBuilds: 0,
            onlyStable: false,
            sourceEncoding: 'ASCII',
            zoomCoverageChart: false])
       warnings canRunOnFailed: true,
           unstableTotalHigh: "${pylintErrTreshold}",
           parserConfigurations: [[parserName: 'PyLint', pattern: 'pylint.out']]
    }
    success {
      script {
        echo "Build completed with NO Errors"
        if (notificationsEnabled) { sendDeployNotification(appName, thisReleaseVersion, notificationsEnvironment, notificationsInfoFinished) }
      }
    }
    unstable {
      script {
        echo "Build completed with Test Errors"
        if (notificationsEnabled) { sendDeployNotification(appName, thisReleaseVersion, notificationsEnvironment, notificationsInfoRollbackFinished) }
      }
    }
    failure {
      script {
        echo "Build completed with Errors"
        if (notificationsEnabled) { sendDeployNotification(appName, thisReleaseVersion, notificationsEnvironment, notificationsInfoFailedAccounting) }
      }
    }
    aborted {
      script {
        echo "Build aborted"
        if (pullRequestBuild) { postStatus('ABORTED', testsFailed, prInfo['prApiUrl']) }
      }
    }
    cleanup {
      script {
        def ciContainerId = sh(returnStdout: true, script: "docker ps -q -a -f name=${ciContainerName}")
        if (ciContainerId) {
          sh "docker rm -f ${ciContainerId}"
          echo "Build container's been found and removed"
        }
        echo "Trying to perform 'docker image rm' to erase local copy of image"
        sh "docker image rm ${ciImageName} || true"
        try {
          echo "Trying to perform 'git clean' to erase untracked files in the workspace"
          sh "git clean -xdff -e pip-download-cache"
          echo "Cleanup was successfull"
        } catch (e) {
          echo "Doing full cleanup of the workspace due to failed git cleanup"
          cleanWs()
        }
      }
    }
  } // end post
}


// Teams notifications
void sendDeployNotification(String appName, String thisReleaseVersion, String promotionStage, String status) {
  def summary = "${appName} ${thisReleaseVersion} => ${promotionStage}"
  def info = ""
  if (status == notificationsInfoFinished) {
    icon = ':checkered_flag:'
    colorCode = '#00CC00'
    info = "\nRelease deployment and accounting were successful!"
  } else if (status == notificationsInfoRollbackFinished) {
    icon = ':checkered_flag:'
    colorCode = '#00CC00'
    info = "\nRollback operation to previous version was finished!"
  } else if (status == notificationsInfoFailed) {
    icon = ':x:'
    colorCode = '#FF0000'
    info = "\nSomething went wrong :("
  } else if (status == notificationsInfoFailedAccounting) {
    icon = ':x:'
    colorCode = '#FF0000'
    info = "\nSeems like deployment accounting error.\n*But the deployment should be done successfully.*"
  } else if (status == notificationsInfoFailedPreparation) {
    icon = ':x:'
    colorCode = '#FF0000'
    info = "\nSeems like early stage's error.\nIt's highly likely that the deployment wasn't even started."
  } else if (status == notificationsInfoRollbackProcedure) {
    icon = ':rewind:'
    colorCode = '#FF0000'
    info = "\nStarting rollback operation to previous version."
  } else if (status == notificationsInfoRollbackFailed) {
    icon = ':x:'
    colorCode = '#FF0000'
    info = "\nRollback operation to previous version failed!"
  }
  withCredentials([string(credentialsId: 'compliance-dev-slack', variable: 'SLACK_TOKEN')]) {
    slackSend(
      color: "${colorCode}",
      message: "*${summary}: ${status}* ${icon} (<${env.BUILD_URL}|Build URL>)${info}",
      channel: slackChannel,
      failOnError: false,
      teamDomain: slackTeamDomain,
      token: SLACK_TOKEN
    )
  }
  office365ConnectorSend message: "${summary} ${info}", webhookUrl: teamsWebHookUrl, color: colorCode, status: status
}