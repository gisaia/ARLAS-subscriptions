#!/bin/bash
set -e

BUILD_OPTS="--no-build"

for i in "$@"
do
case $i in
    -s=*|--stage=*)
    STAGE="${i#*=}"
    shift # past argument=value
    ;;
    --build)
    BUILD_OPTS="--build"
    shift # past argument with no value
    ;;
    *)
            # unknown option
    ;;
esac
done

function clean_exit {
    ARG=$?
    exit $ARG
}

function run_manager {
    echo "===> start arlas-subscriptions-manager stack"
    docker-compose --project-name arlas-subscriptions up -d ${BUILD_OPTS} mongodb subscriptions-manager
    echo "===> wait for arlas-subscriptions-manage up and running"
    docker run --net arlas-subscriptions_default --rm busybox sh -c 'i=1; until nc -w 2 arlas-subscriptions-manager 9998; do if [ $i -lt 30 ]; then sleep 1; else break; fi; i=$(($i + 1)); done'
}

trap clean_exit EXIT

export ARLAS_SUBSCRIPTIONS_VERSION=`xmlstarlet sel -t -v /_:project/_:version pom.xml`

# GO TO PROJECT PATH
SCRIPT_PATH=`cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd`
cd ${SCRIPT_PATH}/..

# PACKAGE
echo "===> compile arlas-subscriptions"
docker run --rm \
    -w /opt/maven \
	-v $PWD:/opt/maven \
	-v $HOME/.m2:/root/.m2 \
	maven:3.5.0-jdk-8 \
	mvn clean install
echo "arlas-subscriptions:${ARLAS_SUBSCRIPTIONS_VERSION}"
if [ "$STAGE" == "MANAGER" ]; then run_manager;fi

