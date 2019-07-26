#!/bin/bash
set -e

function clean_exit {
    ARG=$?
    exit $ARG
}
trap clean_exit EXIT

echo "===> stop arlas-subscriptions stack"
docker-compose  --project-name arlas-subscriptions down -v