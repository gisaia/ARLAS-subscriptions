# ARLAS-subscriptions

[![Build Status](https://api.travis-ci.org/gisaia/ARLAS-subscriptions.svg?branch=develop)](https://travis-ci.org/gisaia/ARLAS-subscriptions)

## Build

```sh
mvn clean package
```

## Run
You need to set two environment variables to run the stack

ARLAS_SUB_TRIG_SCHEM_PATH : a path in docker container to store the trigger json schema

export ARLAS_SUB_TRIG_SCHEM_PATH="/opt/app/trigger.schema.json"


ARLAS_SUB_TRIG_SCHEM_PATH_LOCAL : a local path to the trigger json schema mounted to ARLAS_SUB_TRIG_SCHEM_PATH in a docker container

export ARLAS_SUB_TRIG_SCHEM_PATH_LOCAL="./arlas-subscriptions-tests/src/test/resources/trigger.schema.json"

To run the manager locally wihtout docker set  ARLAS_SUB_TRIG_SCHEM_PATH to a local path to the trigger json schema

```sh
docker-compose up -d
```

## Integration tests
#### Run test suite

```sh
./scripts/tests-integration.sh
```

#### Start test stack populated with test datasets

```sh
./scripts/tests-stack.sh
```