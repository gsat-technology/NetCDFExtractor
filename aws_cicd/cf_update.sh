#!/bin/bash

#source param values
source ./secret/cf_params.sh

#random string for change set name
uuid=$(uuidgen)
tmp=a${uuid:0:6}
rand=`echo $tmp | awk '{print tolower($0)}'`

id=$(aws cloudformation create-change-set \
    --change-set-name $rand \
    --stack-name $1 \
    --template-body file://./cloudformation.yml \
    --capabilities CAPABILITY_IAM \
    --parameters \
                 ParameterKey=CICDName,ParameterValue=$CICDName \
                 ParameterKey=LambdaBucket,ParameterValue=$LambdaBucket \
                 ParameterKey=GithubOwner,ParameterValue=$GithubOwner \
                 ParameterKey=GithubRepoName,ParameterValue=$GithubRepoName \
                 ParameterKey=GithubPersonalAccessToken,ParameterValue=$GithubPersonalAccessToken \
                 ParameterKey=DefaultArtifactsBucket,ParameterValue=$DefaultArtifactsBucket \
                 ParameterKey=JARPath,ParameterValue=$JARPath \
                 ParameterKey=JARFile,ParameterValue=$JARFile \
    --query 'Id' \
    --output text
    )

echo "change set id: $id"

sleep 6

echo "changes: "
aws cloudformation describe-change-set \
   --change-set-name $rand \
   --stack-name $1

echo "update stack (y/n)"
read input_variable

if [ "$input_variable" == "y" ]; then
  aws cloudformation execute-change-set \
     --change-set-name $rand \
     --stack-name $1
else
  "no update"
fi
