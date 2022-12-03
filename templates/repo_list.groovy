#!/usr/bin/env groovy

def reponame = repo.replaceAll('192.168.1.:80/COM/', '').replaceAll('.git', '')

if (reponame == "versions-1-ro-fork" || reponame == "versions-2-ro-fork") {
  def gettags = ("git --git-dir=/data/jenkins/workspace/stash-to-gitlab/${reponame}/ for-each-ref --sort=-committerdate refs/heads/ --format='%(refname:short)' --count=100").execute()
  gettags.waitFor()

  def allBranches = gettags.text.readLines().collect {
    it.replaceAll("\\'", '').trim()
  }

  def rcBranches = allBranches.findAll {p -> p.startsWith('rc/')}

  return rcBranches

} else if (reponame == "3_versions") {
  def gettags = ("git --git-dir=/data/jenkins/workspace/stash-to-gitlab/${reponame}/ for-each-ref --sort=-committerdate refs/heads/ --format='%(refname:short)' --count=100").execute()
  gettags.waitFor()

  def allBranches = gettags.text.readLines().collect {
    it.replaceAll("\\'", '').trim()
  }

 return allBranches

} else {
  def gettags = ("git --git-dir=/data/jenkins/workspace/stash-to-gitlab/${reponame}/ for-each-ref --sort=-committerdate refs/heads/ --format='%(refname:short)' --count=100").execute()
  gettags.waitFor()

  def allBranches = gettags.text.readLines().collect {
    it.replaceAll("\\'", '').trim()
  }

  def branch1 = "development"
  def branch2 = "development/latest"

  def development = allBranches.find {p -> p == branch2}
  if (development == branch2){
    def devBranch = [development]

    allBranches.remove(branch2)
    def newForm = devBranch + allBranches

    return newForm
  } else {
    development = allBranches.find {p -> p == branch1}
    def devBranch = [development]

    allBranches.remove(branch1)
    def newForm = devBranch + allBranches

    return newForm
  }
}