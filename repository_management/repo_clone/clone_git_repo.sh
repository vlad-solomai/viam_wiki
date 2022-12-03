#!/bin/bash 

repo_list = ("repo1" "repo2" "repo3")

for REPO_NAME in repo_list; do
    remote_url_pull=ssh://git@1.1.1.1:1111/${REPO_NAME}.git
    remote_url_push=ssh://git@2.2.2.2:2222/${REPO_NAME}.git
    echo -e "\n\n\n"
    echo "# Working with ${REPO_NAME}"
    echo -e "\n\n\n"
  
    cache_dir="${REPO_NAME}"
    mkdir -vp "${cache_dir}"
    cd "${cache_dir}"

    if git rev-parse --resolve-git-dir . ; then
	    true
    else
        git clone --mirror "${remote_url_pull}" .
        git remote set-url --push origin "${remote_url_push}"
    fi

    git fetch -p origin
    git push --mirror

done 
