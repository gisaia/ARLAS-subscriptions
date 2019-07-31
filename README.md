# ARLAS-subscriptions

[![Build Status](https://api.travis-ci.org/gisaia/ARLAS-subscriptions.svg?branch=develop)](https://travis-ci.org/gisaia/ARLAS-subscriptions)

## Build

```sh
mvn clean package
```

## Run

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