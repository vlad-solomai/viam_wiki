#!/usr/bin/env groovy
//import libs to parse Json output
import groovy.json.JsonOutput

pipeline {
  agent any
  stages {
    stage('Branch annihilation') {
      steps {
        script {
          // ---- variables ----
          def repoUrl = "ssh://git@git.com:7999/${params.project}/${params.repoName}.git"
          println repoUrl
          def stableBranches = params.stableBranches.split(',').collect {it}
          println stableBranches
          def repoName = ''
          def distProject = ''
          def preRepoName = repoUrl.replaceAll("ssh://git@git.com:7999/${params.project}/", "").replaceAll(".git", "")
        
          // ---- work with Data format ----
          def today = new Date()
          def dateFormat = 'yyyy-MM-dd'
          def dateBackupFormat = today.format(dateFormat)
          def twoWeeks = (today -14).format(dateFormat)
          def todayMillis = Date.parse(dateFormat, today.format(dateFormat)).time.div(1000)
          def twoWeeksMillis = Date.parse(dateFormat, twoWeeks).time.div(1000)
          
          // ---- repository name parsing ---
          if (preRepoName == "repo_usersettings") {
            repoName = preRepoName
            imageName = "image_" + repoName
          } else if (preRepoName == "repo_repository") {
            repoName = "repo_upload"
            imageName = imageName = "image_" + repoName
          } else if (preRepoName == "repo_worker" ||
            preRepoName == "repo_worker") {
            repoName = preRepoName.replaceAll('_worker', '')
            imageName = "image_" + repoName + "_worker"
          } else if (preRepoName == "module_repo") {
            repoName = preRepoName
            imageName = "image_" + repoName
          } else {
            repoName = preRepoName.replaceAll('_api', '')
            imageName = "image_repo_" + repoName
          }
          
          def serviceDir = "${preRepoName}.git"
          try {
            git branch: "master", url: "ssh://git@git.com:51022/repo.git", credentialsId: "jenkins_gitlab"
            println "repository is using now"

            // ---- create directories for work ----
            def backupDir = "repository_backups"
            if (!fileExists(backupDir)) {
              sh "mkdir -p ${backupDir}"
              println "Directory '${backupDir}' was created\n"
            }
            if (!fileExists(serviceDir)) {
              sshagent(['jenkins2-id']) {
              sh "git clone --mirror ${repoUrl}"
              println "Repository '${repoName}' was clonned\n"
            }
          }
          
          def archiveDirectory = "${backupDir}/${preRepoName}.git.${dateBackupFormat}_${BUILD_NUMBER}"
          if (!fileExists(archiveDirectory)) {
            sh "cp -rp ${preRepoName}.git ${archiveDirectory}"
            println "Backup for '${repoName}' was created in '${backupDir}' directory\n"
          }         
          if (params.project == "exrf") {
            distProject = "rf"
          } else if (params.project == "exmgn")
            distProject = "mgn"
          } else if (params.project == "exmdm")
            distProject = "mdm"
          } else if (params.project == "exmcr")
            distProject = "mc"
          }

          // ---- work with Json output ----
          sh "rvm 2.2.4@us do bundle install"
          def eventOutput= sh(returnStdout: true, script: "rvm 2.2.4@us do bundle exec ruby ./event.rb -j search install env azure_1 dist ${distProject}")
          def dataInt = readJSON text: eventOutput
          def intVersion = dataInt.event.value.version
          def intBranches = []
          intVersion.each {
            def configOutput = sh(returnStdout: true, script: "rvm 2.2.4@us do bundle exec ruby ./config.rb -l -j distrib ${distProject} ${it}")
            def dataService = readJSON text: configOutput
            if (dataService.containsKey(imageName)) {
              def serviceVersion = dataService[imageName].split(':')[1]
              intBranches.push("release/" + serviceVersion)
            }
          }

          // ---- work with repo branches ----
          def getBranch = sh(script: "git --git-dir=${preRepoName}.git for-each-ref --sort=-committerdate refs/heads/ --format='%(committerdate:short)%09%(refname:short)'", returnStdout: true)
          def getBranchList = getBranch.readLines().collect {it}
          def getBranchMap = [:]
          getBranchList.each {
            def branchCreationDate = it.split("\\s")[0]
            def branchName = it.split("\\s")[1]
            getBranchMap.put(branchName, branchCreationDate)
          }
          def allBranches = getBranchMap.keySet()
          println "List of all branches:\n" + allBranches
          println "\nList of INT branches:\n" + intBranches.unique() + "\n"
          getBranchMap.each { map ->
            def branchMillis = Date.parse(dateFormat, map.value).time.div(1000)
            if (!intBranches.unique().contains(map.key) && map.key != 'development' && !map.key.startsWith("release/1.4") && branchMillis < twoWeeksMillis && !stableBranches.contains(map.key)) {
              def data = [
                name: "refs/heads/${map.key}",
                dryRun: false
              ]

              httpRequest authentication: gitUserCredentials, contentType: 'APPLICATION_JSON', httpMode: 'DELETE', requestBody: JsonOutput.toJson(data), quiet: false, url: "https://git.com/rest/branch-utils/1.0/projects/~viamolos/repos/${preRepoName}/branches"
              println map.key + " dated by: " + map.value + " was removed"
            }
          }
          dir(serviceDir) { deleteDir() }
          } catch(e) {
            println "Error occurs, going to remove service directory"
            dir(serviceDir) { deleteDir() }
            throw e
          }
        }
      }
    }
  }
}