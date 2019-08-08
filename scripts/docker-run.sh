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

    # Allow errors on cleanup
    set +e

    if [[ "$ARG" != 0 ]]; then
        # In case of error, print containers logs (if any)
        docker-compose logs
    fi

    exit $ARG
}

function run_manager {
    echo "===> start arlas-subscriptions-manager stack"
    docker-compose --project-name arlas-subscriptions up -d ${BUILD_OPTS} arlas-subscriptions-manager
    echo "===> wait for arlas-subscriptions-manager up and running"
    docker run --net arlas-subscriptions_default --rm busybox sh -c 'i=1; until nc -w 2 arlas-subscriptions-manager 9998; do if [ $i -lt 100 ]; then sleep 1; else break; fi; i=$(($i + 1)); done'
}

function run_matcher {
    echo "===> start arlas-subscriptions-matcher stack"
    docker-compose --project-name arlas-subscriptions up -d ${BUILD_OPTS} arlas-subscriptions-matcher
    echo "===> wait for arlas-subscriptions-matcher up and running"
     while [ `docker logs arlas-subscriptions-matcher --tail 10 | grep -c "org.eclipse.jetty.server.Server: Started"` -lt 1 ]
     do
        sleep 2
     done
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
    echo "===> wait for arlas-subscriptions-manager up and running"
    docker run --net arlas-subscriptions_default --rm busybox sh -c 'i=1; until nc -w 2 arlas-subscriptions-manager 9998; do if [ $i -lt 100 ]; then sleep 1; else break; fi; i=$(($i + 1)); done'
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
if [ "$STAGE" == "MATCHER" ]; then run_matcher;fi
if [ "$STAGE" == "DUMMY" ]; then run_dummy;fi
if [ "$STAGE" == "ALL" ]; then run_all;fi