#!/bin/bash
set -o errexit -o pipefail

SCRIPT_DIRECTORY="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null && pwd)"
PROJECT_ROOT_DIRECTORY="$SCRIPT_DIRECTORY"

#########################################
#### Variables intialisation ############
#########################################
TEST="YES"
RELEASE="NO"
SKIP_API="NO"
BASEDIR=$PWD

#########################################
#### Cleaning functions #################
#########################################
function clean_docker {
    echo "===> Stop arlas-subscriptions stack"
    docker-compose --project-name arlas-subscriptions down -v
}

function clean_exit {
  ARG=$?
	echo "=> Exit status = $ARG"
	rm -rf pom.xml.versionsBackup
	rm -rf target/tmp || echo "target/tmp already removed"
	clean_docker
	if [ "$RELEASE" == "YES" ]; then
        git checkout -- .
        mvn clean
    else
        echo "=> Skip discard changes";
        git checkout -- pom.xml
        sed -i.bak 's/\"'${FULL_API_VERSION}'\"/\"API_VERSION\"/' subscriptions-manager/src/main/java/io/arlas/subscriptions/rest/UserSubscriptionManagerAbstractController.java
    fi
    exit $ARG
}
trap clean_exit EXIT

#########################################
#### Available arguments ################
#########################################
usage(){
	echo "Usage: ./release.sh -api-major=X -api-minor=Y -api-patch=U -rel=Z -dev=Z+1 -es=Y [--no-tests] [--skip-api]"
	echo " -api-major|--api-version       release arlas-subscriptions API major version"
	echo " -api-minor|--api-minor-version release arlas-subscriptions API minor version"
	echo " -api-patch|--api-patch-version release arlas-subscriptions API patch version"
	echo " -rel|--arlas-release           release arlas-server version"
	echo " -dev|--arlas-dev               development arlas-subscriptions version (-SNAPSHOT qualifier will be automatically added)"
	echo " --no-tests                     do not run integration tests"
	echo " --release                      publish artifacts and git push local branches"
	echo " --skip-api                     do not generate clients APIs"
	exit 1
}

#########################################
#### Parsing arguments ##################
#########################################
for i in "$@"
do
case $i in
    -rel=*|--arlas-release=*)
    ARLAS_REL="${i#*=}"
    shift # past argument=value
    ;;
    -dev=*|--arlas-dev=*)
    ARLAS_DEV="${i#*=}"
    shift # past argument=value
    ;;
    -api-major=*|--api-major-version=*)
    API_MAJOR_VERSION="${i#*=}"
    shift # past argument=value
    ;;
    -api-minor=*|--api-minor-version=*)
    API_MINOR_VERSION="${i#*=}"
    shift # past argument=value
    ;;
    -api-patch=*|--api-patch-version=*)
    API_PATCH_VERSION="${i#*=}"
    shift # past argument=value
    ;;
    --no-tests)
    TESTS="NO"
    shift # past argument with no value
    ;;
    --release)
    RELEASE="YES"
    shift # past argument with no value
    ;;
    --skip-api)
    SKIP_API="YES"
    shift # past argument with no value
    ;;
    *)
            # unknown option
    ;;
esac
done

#########################################
#### Recap of chosen arguments ##########
#########################################

if [ -z ${API_MAJOR_VERSION+x} ]; then usage;  else    echo "API MAJOR version           : ${API_MAJOR_VERSION}"; fi
if [ -z ${API_MINOR_VERSION+x} ]; then usage;  else    echo "API MINOR version           : ${API_MINOR_VERSION}"; fi
if [ -z ${API_PATCH_VERSION+x} ]; then usage;  else    echo "API PATCH version           : ${API_PATCH_VERSION}"; fi
if [ -z ${ARLAS_REL+x} ]; then usage;          else    echo "Release version             : ${ARLAS_REL}"; fi
if [ -z ${ARLAS_DEV+x} ]; then usage;          else    echo "Next development version    : ${ARLAS_DEV}"; fi
                                                       echo "Running tests               : ${TESTS}"
                                                       echo "Release                     : ${RELEASE}"

#########################################
#### Check if you're logged on to repos ###########
#########################################

if [ "$RELEASE" == "YES" -a "$SKIP_API" == "NO" ]; then
    export npmlogin=`npm whoami`
    if  [ -z "$npmlogin"  ] ; then echo "Your are not logged on to npm"; exit -1; else  echo "logged as "$npmlogin ; fi

    if  [ -z "$PIP_LOGIN"  ] ; then echo "Please set PIP_LOGIN environment variable"; exit -1; fi
    if  [ -z "$PIP_PASSWORD"  ] ; then echo "Please set PIP_PASSWORD environment variable"; exit -1; fi
fi


#########################################
#### Setting versions ###################
#########################################
export ARLAS_SUBSCRIPTIONS_VERSION="${API_MAJOR_VERSION}.${API_MINOR_VERSION}.${ARLAS_REL}"
ARLAS_DEV_VERSION="${API_MAJOR_VERSION}.${API_MINOR_VERSION}.${ARLAS_DEV}"
FULL_API_VERSION=${API_MAJOR_VERSION}"."${API_MINOR_VERSION}"."${API_PATCH_VERSION}
API_DEV_VERSION=${API_MAJOR_VERSION}"."${API_MINOR_VERSION}"."${ARLAS_DEV}

echo "Release : ${ARLAS_SUBSCRIPTIONS_VERSION}"
echo "API     : ${FULL_API_VERSION}"
echo "Dev     : ${ARLAS_DEV_VERSION}"


#########################################
#### Ongoing release process ############
#########################################

echo "=> Get develop branch"
if [ "$RELEASE" == "YES" ]; then
    git checkout develop
    git pull origin develop
else echo "=> Skip develop checkout"; fi

echo "=> Update project version"
mvn clean
mvn versions:set -DnewVersion=${ARLAS_SUBSCRIPTIONS_VERSION}
sed -i.bak 's/\"API_VERSION\"/\"'${FULL_API_VERSION}'\"/' subscriptions-manager/src/main/java/io/arlas/subscriptions/rest/UserSubscriptionManagerAbstractController.java

# Helm chart: Update version
sed -i.bak "s/^appVersion: .*\$/appVersion: \"${ARLAS_SUBSCRIPTIONS_VERSION}\"/" helm/arlas-subscriptions/Chart.yaml
sed -i.bak 's/^version: .*$/version: '${ARLAS_SUBSCRIPTIONS_VERSION}'/' helm/arlas-subscriptions/Chart.yaml

# Helm chart: Update docker images' versions
# Using `ruamel` to preserve comments & format

mv helm/arlas-subscriptions/values.yaml helm/arlas-subscriptions/values.yaml.old

docker run \
  --entrypoint bash \
  --env ARLAS_SUBSCRIPTIONS_VERSION \
  --mount dst=/mnt/input.yaml,src="$PWD/helm/arlas-subscriptions/values.yaml.old",type=bind,readonly \
  python:3.7-slim-buster -c "
pip install --upgrade pip >/dev/null 2>/dev/null
pip install ruamel.yaml==0.17.21 >/dev/null 2>/dev/null

python -c '

import pathlib
import sys

import ruamel.yaml

yaml = ruamel.yaml.YAML()
helm_values = yaml.load(pathlib.Path(\"/mnt/input.yaml\"))

helm_values[\"manager\"][\"image\"][\"tag\"] = \"$ARLAS_SUBSCRIPTIONS_VERSION\"
helm_values[\"matcher\"][\"image\"][\"tag\"] = \"$ARLAS_SUBSCRIPTIONS_VERSION\"

yaml.dump(helm_values, sys.stdout)
'
" > helm/arlas-subscriptions/values.yaml

rm -f helm/arlas-subscriptions/values.yaml.old


echo "=> Build arlas-subscriptions"
docker run \
    -e GROUP_ID="$(id -g)" \
    -e USER_ID="$(id -u)" \
    --mount dst=/mnt/.m2,src="$HOME/.m2/",type=bind \
    --mount dst=/opt/maven,src="$PWD",type=bind \
    --rm \
    maven:3.8.5-openjdk-17 \
        clean install

##################################################
#### Generate swagger definiton of the API #######
##################################################

echo "=> Start arlas-subscriptions-manager stack"
export ARLAS_SUB_TRIG_SCHEM_PATH="/opt/app/trigger.schema.json"
export ARLAS_SUB_TRIG_SCHEM_PATH_LOCAL="./subscriptions-tests/src/test/resources/trigger.schema.json"
docker-compose --project-name arlas-subscriptions up -d --build elasticsearch mongodb mongo2 mongo3
DOCKER_IP=$(docker-machine ip || echo "localhost")
sleep 10
echo "===> configure replica set on mongodb"
docker exec mongodb /scripts/rs-init.sh
docker run --net arlas-subscriptions_default --rm busybox sh -c 'i=1; until nc -w 2 elasticsearch 9200; do if [ $i -lt 100 ]; then sleep 1; else break; fi; i=$(($i + 1)); done'
curl -X PUT "${DOCKER_IP}:9200/subs" -H 'Content-Type: application/json' -d'{}'
curl -X PUT "${DOCKER_IP}:9200/subs/_mapping/sub_type" -H 'Content-Type: application/json' -d @"./subscriptions-tests/src/test/resources/arlas.subtest.mapping.json"
docker-compose --project-name arlas-subscriptions up -d --build arlas-subscriptions-manager arlas-subscriptions-matcher
echo "===> wait for arlas-subscriptions-manager up and running"
docker run --net arlas-subscriptions_default --rm busybox sh -c 'i=1; until nc -w 2 arlas-subscriptions-manager 9998; do if [ $i -lt 100 ]; then sleep 1; else break; fi; i=$(($i + 1)); done'

echo "=> Get swagger documentation"
mkdir -p target/tmp || echo "target/tmp exists"
i=1; until curl -XGET http://${DOCKER_IP}:9998/arlas-subscriptions-manager/swagger.json -o target/tmp/swagger.json; do if [ $i -lt 60 ]; then sleep 1; else break; fi; i=$(($i + 1)); done
i=1; until curl -XGET http://${DOCKER_IP}:9998/arlas-subscriptions-manager/swagger.yaml -o target/tmp/swagger.yaml; do if [ $i -lt 60 ]; then sleep 1; else break; fi; i=$(($i + 1)); done

mkdir -p openapi
cp target/tmp/swagger.yaml openapi
cp target/tmp/swagger.json openapi

echo "=> Stop arlas-subscriptions-manager stack"
docker-compose --project-name arlas-subscriptions down -v

echo "=> Generate API documentation"
cd subscriptions-manager/
mvn "-Dswagger.input=../openapi/swagger.json" "-Dswagger.output=../docs/api" swagger2markup:convertSwagger2markup
cd ..

itests() {
	echo "=> Run integration tests"
    ./scripts/test-integration.sh
}
if [ "$TESTS" == "YES" ]; then itests; else echo "=> Skip integration tests"; fi

#########################################
#### Generate API clients ###############
#########################################

if [ "$SKIP_API" == "YES" ]; then
  echo "=> Skipping generation of API clients"
else
  echo "=> Generate API clients"
  ls target/tmp/

  mkdir -p target/tmp/typescript-fetch
  docker run --rm \
      -e GROUP_ID="$(id -g)" \
      -e USER_ID="$(id -u)" \
      --mount dst=/input/api.json,src="$PWD/target/tmp/swagger.json",type=bind,ro \
      --mount dst=/output,src="$PWD/target/tmp/typescript-fetch",type=bind \
    gisaia/swagger-codegen-2.4.14 \
          -l typescript-fetch --additional-properties modelPropertyNaming=snake_case

  mkdir -p target/tmp/python-api
  docker run --rm \
      -e GROUP_ID="$(id -g)" \
      -e USER_ID="$(id -u)" \
      --mount dst=/input/api.json,src="$PWD/target/tmp/swagger.json",type=bind,ro \
      --mount dst=/input/config.json,src="$PROJECT_ROOT_DIRECTORY/conf/swagger/python-config.json",type=bind,ro \
      --mount dst=/output,src="$PWD/target/tmp/python-api",type=bind \
    gisaia/swagger-codegen-2.4.14 \
          -l python --type-mappings GeoJsonObject=object

  echo "=> Build Typescript API "${FULL_API_VERSION}
  cd ${BASEDIR}/target/tmp/typescript-fetch/
  cp ${BASEDIR}/conf/npm/package-build.json package.json
  cp ${BASEDIR}/conf/npm/tsconfig-build.json .
  npm version --no-git-tag-version ${FULL_API_VERSION}
  npm install
  npm run build-release
  npm run postbuild
  cd ${BASEDIR}

  echo "=> Publish Typescript API "
  cp ${BASEDIR}/conf/npm/package-publish.json ${BASEDIR}/target/tmp/typescript-fetch/dist/package.json
  cd ${BASEDIR}/target/tmp/typescript-fetch/dist
  npm version --no-git-tag-version ${FULL_API_VERSION}


  if [ "$RELEASE" == "YES" ]; then
      npm publish || echo "Publishing on npm failed ... continue ..."
  else echo "=> Skip npm api publish"; fi


  echo "=> Build Python API "${FULL_API_VERSION}
  cd ${BASEDIR}/target/tmp/python-api/
  cp ${BASEDIR}/conf/python/setup.py setup.py
  sed -i.bak 's/\"api_subscriptions_version\"/\"'${FULL_API_VERSION}'\"/' setup.py

  docker run \
        -e GROUP_ID="$(id -g)" \
        -e USER_ID="$(id -u)" \
        --mount dst=/opt/python,src="$PWD",type=bind \
        --rm \
        gisaia/python-3-alpine \
              setup.py sdist bdist_wheel

  echo "=> Publish Python API "
  if [ "$RELEASE" == "YES" ]; then
      docker run --rm \
          -w /opt/python \
        -v $PWD:/opt/python \
        python:3 \
        /bin/bash -c  "pip install twine ; twine upload dist/* -u ${PIP_LOGIN} -p ${PIP_PASSWORD}"
       ### At this stage username and password of Pypi repository should be set
  else echo "=> Skip python api publish"; fi
fi

cd ${BASEDIR}

if [ "$RELEASE" == "YES" ]; then
    echo "=> Tag arlas-subscriptions-manager docker image"
    docker tag gisaia/arlas-subscriptions-manager:${ARLAS_SUBSCRIPTIONS_VERSION} gisaia/arlas-subscriptions-manager:latest
    echo "=> Tag arlas-subscriptions-matcher docker image"
    docker tag gisaia/arlas-subscriptions-matcher:${ARLAS_SUBSCRIPTIONS_VERSION} gisaia/arlas-subscriptions-matcher:latest
    echo "=> Push arlas-subscriptions-manager docker image"
    docker push gisaia/arlas-subscriptions-manager:${ARLAS_SUBSCRIPTIONS_VERSION}
    docker push gisaia/arlas-subscriptions-manager:latest
    echo "=> Push arlas-subscriptions-matcher docker image"
    docker push gisaia/arlas-subscriptions-matcher:${ARLAS_SUBSCRIPTIONS_VERSION}
    docker push gisaia/arlas-subscriptions-matcher:latest
else echo "=> Skip docker push image"; fi

if [ "$RELEASE" == "YES" ]; then
    echo "=> Generate CHANGELOG.md"
    git tag v${ARLAS_SUBSCRIPTIONS_VERSION}
    git push origin v${ARLAS_SUBSCRIPTIONS_VERSION}
    #@see scripts/build-github-changelog-generator.sh in ARLAS-server project if you need a fresher version of this tool
    docker run -it --rm -v "$(pwd)":/usr/local/src/your-app gisaia/github-changelog-generator:latest github_changelog_generator \
        -u gisaia -p ARLAS-subscriptions --token ${GITHUB_CHANGELOG_TOKEN} \
        --no-pr-wo-labels --no-issues-wo-labels --no-unreleased --issue-line-labels API,conf,security,documentation \
        --exclude-labels type:duplicate,type:question,type:wontfix,type:invalid \
        --bug-labels type:bug \
        --enhancement-labels  type:enhancement \
        --breaking-labels type:breaking \
        --enhancement-label "**New stuff:**" --issues-label "**Miscellaneous:**" --since-tag v0.0.1
    git add CHANGELOG.md
    git tag -d v${ARLAS_SUBSCRIPTIONS_VERSION}
    git push origin :v${ARLAS_SUBSCRIPTIONS_VERSION}
    echo "=> Commit release version"
    git add openapi/swagger.json
    git add openapi/swagger.yaml
    git add docs/api
    git commit -a -m "release version ${ARLAS_SUBSCRIPTIONS_VERSION}"
    git tag v${ARLAS_SUBSCRIPTIONS_VERSION}
    git push origin v${ARLAS_SUBSCRIPTIONS_VERSION}
    git push origin develop

    echo "=> Merge develop into master"
    git checkout master
    git pull origin master
    git merge origin/develop
    git push origin master

    echo "=> Rebase develop"
    git checkout develop
    git pull origin develop
    git rebase origin/master
else echo "=> Skip git push master"; fi

echo "=> Update project version for develop"
mvn versions:set -DnewVersion=${ARLAS_DEV_VERSION}-SNAPSHOT

echo "=> Update REST API version in JAVA source code"
sed -i.bak 's/\"'${FULL_API_VERSION}'\"/\"API_VERSION\"/' subscriptions-manager/src/main/java/io/arlas/subscriptions/rest/UserSubscriptionManagerAbstractController.java

if [ "$RELEASE" == "YES" ]; then
    sed -i.bak 's/\"'${FULL_API_VERSION}'\"/\"'${API_DEV_VERSION}-SNAPSHOT'\"/' openapi/swagger.yaml
    sed -i.bak 's/\"'${FULL_API_VERSION}'\"/\"'${API_DEV_VERSION}-SNAPSHOT'\"/' openapi/swagger.json
    git add openapi/swagger.json
    git add openapi/swagger.yaml
    git commit -a -m "development version ${ARLAS_DEV_VERSION}-SNAPSHOT"
    git push origin develop
else echo "=> Skip git push develop"; fi