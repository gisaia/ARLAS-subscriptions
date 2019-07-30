#!/bin/bash
set -e

function clean_docker {
    ./scripts/docker-clean.sh
    echo "===> clean maven repository"
	docker run --rm \
		-w /opt/maven \
		-v $PWD:/opt/maven \
		-v $HOME/.m2:/root/.m2 \
		maven:3.5.0-jdk-8 \
		mvn clean
}

function clean_exit {
    ARG=$?
	echo "===> Exit stage ${STAGE} = ${ARG}"
    clean_docker
    exit $ARG
}
trap clean_exit EXIT

usage(){
	echo "Usage: ./test-integration-stage.sh --stage=MANAGER|MATCHER"
	exit 1
}

for i in "$@"
do
case $i in
    --stage=*)
    STAGE="${i#*=}"
    shift # past argument=value
    ;;
    *)
            # unknown option
    ;;
esac
done

# GO TO PROJECT PATH
SCRIPT_PATH=`cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd`
cd ${SCRIPT_PATH}/..

# CHECK ALV2 DISCLAIMER
if [ $(find ./*/src -name "*.java" -exec grep -L Licensed {} \; | wc -l) -gt 0 ]; then
    echo "ALv2 disclaimer is missing in the following files :"
    find ./*/src -name "*.java" -exec grep -L Licensed {} \;
    exit -1
fi

if [ -z ${STAGE+x} ]; then usage; else echo "Tests stage : ${STAGE}"; fi

function start_stack() {
    # START STACK
    ./scripts/docker-clean.sh
    ./scripts/docker-run.sh --stage=$STAGE --build
}

function test_manager() {
        export ARLAS_SUB_MANAGER_PREFIX="/arlastest"
        export ARLAS_SUB_MANAGER_APP_PATH="/pathtest"
        start_stack
        docker run --rm \
            -w /opt/maven \
            -v $PWD:/opt/maven \
            -v $HOME/.m2:/root/.m2 \
            -e ARLAS_SUB_MANAGER_HOST="arlas-subscriptions-manager" \
            -e ARLAS_PORT="9998" \
            -e ARLAS_PREFIX=${ARLAS_SUB_MANAGER_PREFIX} \
            -e ARLAS_APP_PATH=${ARLAS_SUB_MANAGER_APP_PATH} \
            --net arlas-subscriptions_default \
            maven:3.5.0-jdk-8 \
            mvn -Dit.test=UserSubscriptionManagerServiceIT verify -DskipTests=false  -DfailIfNoTests=false

}

echo "===> run integration tests"
if [ "$STAGE" == "MANAGER" ]; then test_manager; fi