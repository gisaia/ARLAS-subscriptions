#!/bin/bash
set -o errexit -o pipefail

function clean_exit {
    ARG=$?
	echo "===> Exit status = ${ARG}"
    exit $ARG
}
trap clean_exit EXIT

usage(){
	echo "Usage: ./test-integration.sh"
	exit 1
}

# GO TO PROJECT PATH
SCRIPT_PATH=`cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd`
cd ${SCRIPT_PATH}/../..

# TESTS SUITE
./scripts/ci/tests-integration-stage.sh --stage=MANAGER
./scripts/ci/tests-integration-stage.sh --stage=MANAGER_AUTH
./scripts/ci/tests-integration-stage.sh --stage=MATCHER
