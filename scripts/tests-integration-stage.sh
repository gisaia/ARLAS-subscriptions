#!/bin/bash
set -e
export ARLAS_SUB_TRIG_SCHEM_PATH="/opt/app/trigger.schema.json"
export ARLAS_SUB_TRIG_SCHEM_PATH_LOCAL="./subscriptions-tests/src/test/resources/trigger.schema.json"
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
        start_stack
        docker run --rm \
            -w /opt/maven \
            -v $PWD:/opt/maven \
            -v $HOME/.m2:/root/.m2 \
            -e ARLAS_SUB_MANAGER_HOST="arlas-subscriptions-manager" \
            -e ARLAS_SUB_MANAGER_PORT="9998" \
            -e MONGO_HOST="mongodb" \
            -e MONGO_PORT="27017" \
            -e MONGO_DATABASE="subscription" \
            -e ARLAS_ELASTIC_HOST="elasticsearch" \
            -e ARLAS_SUB_ELASTIC_NODES="elasticsearch:9300" \
            -e ARLAS_SUB_ELASTIC_SNIFFING="false" \
            -e ARLAS_SUB_ELASTIC_INDEX="subs" \
            -e ARLAS_SUB_ELASTIC_TYPE="sub_type"\
            -e ARLAS_SUB_ELASTIC_CLUSTER="elasticsearch" \
            --net arlas-subscriptions_default \
            maven:3.5.0-jdk-8 \
            mvn -Dit.test=UserSubscriptionManagerServiceIT verify -DskipTests=false  -DfailIfNoTests=false
}

function test_manager_auth() {
        export ARLAS_SUB_IDENTITY_HEADER="x-identity"
        start_stack
        docker run --rm \
            -w /opt/maven \
            -v $PWD:/opt/maven \
            -v $HOME/.m2:/root/.m2 \
            -e ARLAS_SUB_MANAGER_HOST="arlas-subscriptions-manager" \
            -e ARLAS_SUB_MANAGER_PORT="9998" \
            -e MONGO_HOST="mongodb" \
            -e MONGO_PORT="27017" \
            -e MONGO_DATABASE="subscription" \
            -e ARLAS_ELASTIC_HOST="elasticsearch" \
            -e ARLAS_SUB_ELASTIC_NODES="elasticsearch:9300" \
            -e ARLAS_SUB_ELASTIC_SNIFFING="false" \
            -e ARLAS_SUB_ELASTIC_INDEX="subs" \
            -e ARLAS_SUB_ELASTIC_TYPE="sub_type"\
            -e ARLAS_SUB_ELASTIC_CLUSTER="elasticsearch" \
            -e ARLAS_SUB_IDENTITY_HEADER="x-identity" \
            --net arlas-subscriptions_default \
            maven:3.5.0-jdk-8 \
            mvn -Dit.test=UserSubscriptionAuthManagerServiceIT verify -DskipTests=false  -DfailIfNoTests=false
}

function test_matcher() {
        start_stack
        docker run --rm \
            -w /opt/maven \
            -v $PWD:/opt/maven \
            -v $HOME/.m2:/root/.m2 \
            -e ARLAS_HOST="arlas-server" \
            -e ARLAS_ELASTIC_HOST="elasticsearch" \
            -e ARLAS_SERVER_BASE_PATH="http://arlas-server:9999/arlas" \
            -e ARLAS_SUBSCRIPTIONS_BASE_PATH="http://arlas-server:9999/arlas" \
            --net arlas-subscriptions_default \
            maven:3.5.0-jdk-8 \
            mvn -Dit.test=SubscriptionsMatcherIT verify -DskipTests=false  -DfailIfNoTests=false
}

function test_dummy() {
        start_stack
        docker run --rm \
            -w /opt/maven \
            -v $PWD:/opt/maven \
            -v $HOME/.m2:/root/.m2 \
            -e ARLAS_SUB_MANAGER_HOST="arlas-subscriptions-manager" \
            -e ARLAS_HOST="arlas-server" \
            -e ARLAS_PORT="9999" \
            -e ARLAS_ELASTIC_HOST="elasticsearch" \
            -e ARLAS_ELASTIC_PORT="9300" \
            --net arlas-subscriptions_default \
            maven:3.5.0-jdk-8 \
            mvn -Dit.test=DummyIT verify -DskipTests=false  -DfailIfNoTests=false
}

echo "===> run integration tests"
if [ "$STAGE" == "MANAGER_AUTH" ]; then test_manager_auth; fi
if [ "$STAGE" == "MANAGER" ]; then test_manager; fi
if [ "$STAGE" == "MATCHER" ]; then test_matcher; fi
if [ "$STAGE" == "DUMMY" ]; then test_dummy; fi