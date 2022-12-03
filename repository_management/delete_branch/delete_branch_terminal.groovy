#!/usr/bin/groovy

// import libs to parse Json output
import groovy.json.JsonSlurper
import groovy.json.JsonOutput

// variables
def repoUrl = "ssh://git@git.com:7999/~viamolos/repo.git"
def userName = "+"
def userToken = "+"
def repoName = ''
def stableBranches = ["release/1.1.1", "release/1.1.2"]
def imageName = ''
def preRepoName = repoUrl.replaceAll('ssh://git@git.com:7999/~viamolos/', '').replaceAll('.git', '')

// ---- work with Data format ----
def today = new Date()
def dateFormat = 'yyyy-MM-dd'
def dateBackupFormat = today.format(dateFormat)
def twoWeeks = (today -14).format(dateFormat)
def todayMillis = Date.parse(dateFormat, today.format(dateFormat)).time.div(1000)
def twoWeeksMillis = Date.parse(dateFormat, twoWeeks).time.div(1000)

// ---- repository name parsing ---
if (preRepoName == "repo_user") {
  repoName = preRepoName
  imageName = "image_" + repoName
} else if (preRepoName == "repo_repository") {
  repoName = "repo_upload"
  imageName = "image_" + repoName
} else if (preRepoName == "repo_worker" || preRepoName == "repo_worker") {
  repoName = preRepoName.replaceAll('_worker', '')
  imageName = "image_" + repoName + "_worker"
} else if (preRepoName == "module_repo") {
  repoName = preRepoName
  imageName = "image_" + repoName
} else {
  repoName = preRepoName.replaceAll('_api', '')
  imageName = "image_service_" + repoName
}

def serviceDir = new File("${repoName}")

try {
  // ---- create directories for work ----
  if ( !serviceDir.exists() ) {
    serviceDir.mkdirs()
    println "Directory '${serviceDir}' was created"
  }
  def cloneRepo = ("git clone --mirror ${repoUrl}").execute(null, serviceDir)
  cloneRepo.waitFor()
  println "Repository '${repoName}' was clonned into '${serviceDir}' directory\n"

  def backupDir = new File("repository_backups")
  if ( !backupDir.exists() ) {
    backupDir.mkdirs()
    println "Directory '${backupDir}' was created\n"
  }

  def archiveDirectory = new File("${backupDir}/${preRepoName}.git.${dateBackupFormat}.tar.gz")
  if ( !archiveDirectory.exists() ) {
    def archiveService = ("tar -czvf ../${archiveDirectory} ${preRepoName}.git").execute(null, serviceDir)
    archiveService.waitFor()
    println "Backup for '${preRepoName}' repository was created in '${backupDir}' directory\n"
  }

  def updateRepo = ("git pull").execute()
  updateRepo.waitFor()
  println "SkyNet repository was updated"

  // ---- work with Json output ----
  def dataInt = "./event.rb -j search install env azure_1 dist rf".execute().text

  def jsonSlurper = new JsonSlurper()
  def intVersionList = jsonSlurper.parseText(dataInt)
  def intVersion = intVersionList.event.value.version
  def intBranches = []

  intVersion.each {
    def dataService = "./config.rb -l -j distrib rf ${it}".execute().text
    def serviceVersionList = jsonSlurper.parseText(dataService)
    if (serviceVersionList.containsKey(imageName)) {
      def serviceVersion = (serviceVersionList[imageName].split(':')[1])
      intBranches.push("release/" + serviceVersion)
    }
  }

  // ---- work with repo branches ----
  def getBranch = ("git --git-dir=${repoName}/${preRepoName}.git for-each-ref --sort=-committerdate refs/heads/ --format='%(committerdate:short)%09%(refname:short)'").execute()
  getBranch.waitFor()

  def getBranchList = getBranch.text.readLines().collect {it}
  def getBranchMap = [:]
  getBranchList.each {
    def branchCreationDate = it.split("\\s")[0].replaceFirst("\\'", '')
    def branchName = it.split("\\s")[1]
    if(branchName.endsWith("'")){
      branchName = branchName.substring(0, branchName.length() - 1)
      getBranchMap.put(branchName, branchCreationDate)
    }
  }

  def allBranches = getBranchMap.keySet()
  println "List of all branches:\n" + allBranches
  println "\nList of INT branches:\n" + intBranches.unique() + "\n"

  stableBranches.each { extraBranch -> assert allBranches.contains(extraBranch) }

  def fileJsonBranch = new File("bitbacketBranch.json")
  def bitbacketCredentials = new File("bitbacketCredentials.txt")
  bitbacketCredentials.write "${userName}:${userToken}"
  bitbacketCredentials.eachLine { line ->
    def login = line.split(":")[0]
    def token = line.split(":")[1]

    getBranchMap.each { map ->
      def branchMillis = Date.parse(dateFormat, map.value).time.div(1000)
      if (!intBranches.unique().contains(map.key) && map.key != 'development' && map.key != 'development/april2019' && !map.key.startsWith("release/1.4") && branchMillis < twoWeeksMillis && !stableBranches.contains(map.key)) {
        def data = [
          name: "refs/heads/${map.key}",
          dryRun: false
        ]

        def jsonBranch = JsonOutput.toJson(data)
        def jsonBranchBeauty = JsonOutput.prettyPrint(jsonBranch)
        fileJsonBranch.write(jsonBranchBeauty)
        def deleteBranch = [ 'bash', '-c', "curl -u ${login}:${token} -X DELETE -H 'content-type: application/json' https://git@git.com:7999/rest/branch-utils/1.0/projects/~viamolos/repos/${preRepoName}/branches -d @bitbacketBranch.json" ].execute().text
        println deleteBranch
        println map.key + " dated by: " + map.value + " was removed"
      }
    }
  }

  println fileJsonBranch.delete()
  println bitbacketCredentials.delete()
  serviceDir.deleteDir()

} catch(e) {
  println "Error occurs, going to remove service directory"
  serviceDir.deleteDir()
  throw e
}