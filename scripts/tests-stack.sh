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
    exit $ARG
}
trap clean_exit EXIT


# GO TO PROJECT PATH
SCRIPT_PATH=`cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd`
cd ${SCRIPT_PATH}/..


function start_stack() {
    # START STACK
    ./scripts/docker-clean.sh
    ./scripts/docker-run.sh --stage=ALL --build
}


start_stack

echo "===> load test dataset"
docker run --rm \
    -w /opt/maven \
    -v $PWD:/opt/maven \
    -v $HOME/.m2:/root/.m2 \
    -e ARLAS_HOST="arlas-server" \
    -e ARLAS_PORT="9999" \
    -e ARLAS_SUB_ELASTIC_NODES="elasticsearch:9200" \
    --net arlas-subscriptions_default \
    maven:3.5.0-jdk-8 \
    mvn exec:java -Dexec.mainClass="io.arlas.subscriptions.DataSetTool" -Dexec.classpathScope=test -pl subscriptions-tests

echo "===> Enjoy arlas-server API on http://localhost:9999/arlas/swagger"