# Clone Git Repo
### Skills summary:
- **#bash**
- **#git**

### Description:
Script `clone_git_repo.sh` create full clone of git repository:
1. Create working directory.
2. Clone repository data and push it to another repo.
```sh
 > git clone --mirror ssh://git@1.1.1.1:1111/REPO_NAME.git .
 > git remote set-url --push origin ssh://git@2.2.2.2:2222/REPO_NAME.git
 > git fetch -p origin
 > git push --mirror ssh://git@2.2.2.2:2222/REPO_NAME.git
 ```
