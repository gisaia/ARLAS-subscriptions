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
    docker-compose --project-name arlas-subscriptions up -d ${BUILD_OPTS} mongodb elasticsearch subscriptions-manager
    wait_manager
}

function run_dummy {
    echo "===> start dummy stack"
    docker-compose --project-name arlas-subscriptions up -d ${BUILD_OPTS} arlas-server
    echo "===> wait for arlas-server up and running"
    docker run --net arlas-subscriptions_default --rm busybox sh -c 'i=1; until nc -w 2 arlas-server 9999; do if [ $i -lt 100 ]; then sleep 1; else break; fi; i=$(($i + 1)); done'
}

function run_all {
    echo "===> start all stack"
    docker-compose --project-name arlas-subscriptions up -d ${BUILD_OPTS}
    echo "===> wait for arlas-server up and running"
    docker run --net arlas-subscriptions_default --rm busybox sh -c 'i=1; until nc -w 2 arlas-server 9999; do if [ $i -lt 100 ]; then sleep 1; else break; fi; i=$(($i + 1)); done'
    wait_manager
}

function wait_manager {
    echo "===> wait for arlas-subscriptions-manager to be up and running"

    if ! docker run --net arlas-subscriptions_default --rm busybox sh -c 'i=1; until nc -w 2 subscriptions-manager 9998; do if [ $i -lt 100 ]; then sleep 1; else exit 1; fi; i=$(($i + 1)); done'; then

        >&2 echo "arlas-subscriptions-manager unreachable."
        >&2 echo "Logs:"
        >&2 docker logs subscriptions-manager
        exit 1

    fi
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
if [ "$STAGE" == "DUMMY" ]; then run_dummy;fi
if [ "$STAGE" == "ALL" ]; then run_all;fi