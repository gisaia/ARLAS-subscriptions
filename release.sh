#!/bin/bash
set -o errexit -o pipefail

SCRIPT_DIRECTORY="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null && pwd)"
PROJECT_ROOT_DIRECTORY="$SCRIPT_DIRECTORY"

#########################################
#### Variables intialisation ############
#########################################
TEST="YES"
RELEASE="NO"
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
	echo "Usage: ./release.sh -api-major=X -api-minor=Y -api-patch=Z -dev=Z+1 -es=Y [--no-tests]"
  echo " -es |--elastic-range           elasticsearch versions supported"
	echo " -api-major|--api-version       release arlas-subscriptions API major version"
	echo " -api-minor|--api-minor-version release arlas-subscriptions API minor version"
	echo " -api-patch|--api-patch-version release arlas-subscriptions API patch version"
	echo " -dev|--arlas-dev               development arlas-subscriptions version (-SNAPSHOT qualifier will be automatically added)"
	echo " --no-tests                     do not run integration tests"
	echo " --release                      publish artifacts and git push local branches"
	exit 1
}

#########################################
#### Parsing arguments ##################
#########################################
for i in "$@"
do
case $i in
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
    -es=*|--elastic-range=*)
    ELASTIC_RANGE="${i#*=}"
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
    *)
            # unknown option
    ;;
esac
done

ELASTIC_VERSIONS_6=("6.0.1","6.1.3","6.2.4","6.3.2","6.4.3","6.5.4","6.6.2","6.7.2","6.8.1")
case $ELASTIC_RANGE in
    "6")
        ELASTIC_VERSIONS=( "${ELASTIC_VERSIONS_6[@]}" )
        ;;
    *)
        echo "Unknown --elasticsearch-range value"
        echo "Possible values : "
        echo "   -es=6 for versions ${ELASTIC_VERSIONS_6[*]}"
        usage
esac

#########################################
#### Recap of chosen arguments ##########
#########################################

if [ -z ${ELASTIC_VERSIONS+x} ]; then usage;   else echo "Elasticsearch versions support : ${ELASTIC_VERSIONS[*]}"; fi
if [ -z ${API_MAJOR_VERSION+x} ]; then usage;  else    echo "API MAJOR version           : ${API_MAJOR_VERSION}"; fi
if [ -z ${API_MINOR_VERSION+x} ]; then usage;  else    echo "API MINOR version           : ${API_MINOR_VERSION}"; fi
if [ -z ${API_PATCH_VERSION+x} ]; then usage;  else    echo "API PATCH version           : ${API_PATCH_VERSION}"; fi
if [ -z ${ARLAS_DEV+x} ]; then usage;          else    echo "Next development version    : ${ARLAS_DEV}"; fi
                                                       echo "Running tests               : ${TESTS}"
                                                       echo "Release                     : ${RELEASE}"


#########################################
#### Setting versions ###################
#########################################
export ARLAS_SUBSCRIPTIONS_VERSION="${API_MAJOR_VERSION}.${ELASTIC_RANGE}.${API_PATCH_VERSION}"
ARLAS_DEV_VERSION="${API_MAJOR_VERSION}.${ELASTIC_RANGE}.${ARLAS_DEV}"
FULL_API_VERSION=${API_MAJOR_VERSION}"."${API_MINOR_VERSION}"."${API_PATCH_VERSION}
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

echo "=> Build arlas-subscriptions"
docker run \
    -e GROUP_ID="$(id -g)" \
    -e USER_ID="$(id -u)" \
    --mount dst=/mnt/.m2,src="$HOME/.m2/",type=bind \
    --mount dst=/opt/maven,src="$PWD",type=bind \
    --rm \
    gisaia/maven-3.5-jdk8-alpine \
        clean install

##################################################
#### Generate swagger definiton of the API #######
##################################################

echo "=> Start arlas-subscriptions-manager stack"
export ARLAS_SUB_TRIG_SCHEM_PATH="/opt/app/trigger.schema.json"
export ARLAS_SUB_TRIG_SCHEM_PATH_LOCAL="./subscriptions-tests/src/test/resources/trigger.schema.json"
docker-compose --project-name arlas-subscriptions up -d --build elasticsearch
DOCKER_IP=$(docker-machine ip || echo "localhost")
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

itests() {
	echo "=> Run integration tests"
    ./scripts/test-integration.sh
}
if [ "$TESTS" == "YES" ]; then itests; else echo "=> Skip integration tests"; fi



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
        -u gisaia -p ARLAS-subscriptions --token 479b4f9b9390acca5c931dd34e3b7efb21cbf6d0 \
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