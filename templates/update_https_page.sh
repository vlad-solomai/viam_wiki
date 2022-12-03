#!/bin/bash

ENVIRONMENT=$ENV
WRAPPER_URL=https://${BUILD_PATH}/${ENV}/${PACKAGE_VERSION}.${BUILD_NUMBER}

sed +x

curl -X GET -H "Authorization: Basic password" https://${ENVIRONMENT}/backoffice/gameWrappers/1 > gameWrappers_${BUILD_NUMBER}.json

cat gameWrappers_${BUILD_NUMBER}.json

if [ -n "$WRAPPER_URL" ]; then
    OBJECT_KEY=${WRAPPER_URL/'https://cdn.gameiom.com/'/}
    echo -e "\n [INFO] Updating WRAPPER_URL... \n"
    jq ".wrapperUrl=\"${WRAPPER_URL}\"" gameWrappers_${BUILD_NUMBER}.json > gameWrappers_${BUILD_NUMBER}_tmp.json
    jq ".objectKey=\"${OBJECT_KEY}\"" gameWrappers_${BUILD_NUMBER}_tmp.json > gameWrappers_${BUILD_NUMBER}_updated.json 
    rm gameWrappers_${BUILD_NUMBER}_tmp.json
    cat gameWrappers_${BUILD_NUMBER}_updated.json
    python -m json.tool gameWrappers_${BUILD_NUMBER}_updated.json >> /dev/null || echo "NOT valid JSON"
    curl -k -X PUT -H "Authorization: Basic password" -H 'Accept: application/json' -H 'Content-type: application/json;charset=utf-8' -d @gameWrappers_${BUILD_NUMBER}_updated.json https://${ENVIRONMENT}/backoffice/gameWrappers/1
else
    echo -e "\n [ERROR] WRAPPER_URL is not set, please set \n" || false; exit 1
fi
