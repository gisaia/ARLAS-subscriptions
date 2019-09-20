# ARLAS-subscriptions

[![Build Status](https://api.travis-ci.org/gisaia/ARLAS-subscriptions.svg?branch=develop)](https://travis-ci.org/gisaia/ARLAS-subscriptions)

# Overview
**Warning: Work in Progress**

ARLAS-subscriptions is a service used to generate notification orders (i.e. a message containing information to be
pushed to a user by a *notification service* \[not provided by ARLAS\]), according to some criteria subscribed by a user
on an ARLAS-server collection (e.g. a user wants to be notified of any new 'document' created in Europe).

The service relies on ARLAS-server to perform its logic. On top of the 'document' collection already configured to be 
displayed by ARLAS, a new collection is to be created to manage the 'subscriptions'. 

It is composed of 2 components:
* `Matcher`: kafka based service (consumer and producer) receiving new *events* related to ARLAS server 'documents' and 
executing the matching logic on ARLAS server subscriptions collection. When matching documents are found, a kafka message
is produced (aka *notification order*)
* `Manager`: REST based API used to interact with the subscriptions' database (comprised of a MongoDB database and an 
Elasticsearch index).
  * Create subscriptions
  * Read one or a list of subscription(s)
  * Update subscriptions
  * Delete subscriptions
  
The `manager` exposes two sets of API: one aimed at end-users and one aimed at administrator or operational users. Access
control must be setup in order to prevent misusage of the endpoints.

An authentication mechanism based on an HTTP header can optionally be activated (see configuration) to manage user rights,
but the service managing security and user rights is not provided nor managed by ARLAS. Please contact us if you need
support on this matter.

# Pre-requisites
The service relies on the following external components:
- Kafka (1.1.1) and Zookeeper
- MongoDB (3.4)
- Elasticsearch
- ARLAS-server

A 'subscription' index has to be created in Elasticsearch.
```shell script
curl -XPUT 'http://$ELASTIC_HOST:9200/subscription'
curl -XPUT 'http://$ELASTIC_HOST:9200/subscription/sub_type/_mapping' -d example.mapping.json -H 'Content-Type: application/json'
```
The `trigger` property of the mapping must conform to a JSON schema defined according to your 'document' index 
and provided to the `manager` in its startup configuration (see `ARLAS_SUB_TRIG_SCHEM_PATH` in [Manager configuration]).
An example of mapping and trigger JSON schema can be found in the **docs** directory of the project.

The related collection must also be created in ARLAS Server. For instance:
```shell script
curl -X PUT "http://$ARLAS_HOST:9999/arlas/collections/subscriptions?pretty=false" \ 
  -H  "accept: application/json;charset=utf-8" \ 
  -H  "Content-Type: application/json;charset=utf-8" \ 
  -d "{\"index_name\": \"subscriptions\",  \"type_name\": \"sub_type\",  \"id_path\": \"id\",  \"geometry_path\": \"geometry\",  \"centroid_path\": \"centroid\",  \"timestamp_path\": \"created_at\"}"
```

The `manager` and `matcher` can then be started.

## Global configuration


## Manager configuration
Environment variable name | Description | Example
--- | --- | ---
ARLAS_SUB_ELASTIC_NODES | Elasticsearch server:port containing the subscriptions index | localhost:9300
ARLAS_SUB_ELASTIC_SNIFFING | Elasticsearch client sniffing | false
ARLAS_SUB_ELASTIC_CLUSTER | Elasticsearch cluster name containing the subscriptions index | elasticsearch
ARLAS_SUB_ELASTIC_INDEX | Elasticsearch index name for the subscriptions | subscription
ARLAS_SUB_ELASTIC_TYPE | Elasticsearch type for the subscriptions | sub_type
ARLAS_SUB_GEOM_KEY | Property name of the trigger in the subscription mapping containing the geometry (mandatory) | geometry
ARLAS_SUB_CENT_KEY | Property name of the trigger in the subscription mapping containing the centroid (optional; is calculated from the geometry if absent) | centroid
ARLAS_SUB_IDENTITY_HEADER | HTTP header name containing the user id | (empty)
ARLAS_SUB_IDENTITY_ADMIN | User id for admin endpoints | admin
ARLAS_SUB_TRIG_SCHEM_PATH | Path to the trigger JSON schema (mandatory) | /opt/app/trigger.schema.json
ARLAS_SUB_TRIG_SCHEM_PATH_LOCAL | Local path to the trigger JSON schema which should be mounted in the docker container | ./subscriptions-tests/src/test/resources/trigger.schema.json

## Matcher configuration
Environment variable name | Description | Example
--- | --- | ---
KAFKA_BROKERS | Server name:port of the kafka broker to use | kafka:9092
KAFKA_CONSUMER_POLL_TIMEOUT | Kafka consumer poll timeout (ms) | 10
KAFKA_BATCH_SIZE | Kafka consumer batch size | 10
KAFKA_CONSUMER_GROUP_ID | Kafka consumer group id | subscription_events_consumer_group
KAFKA_TOPIC_SUBSCRIPTION_EVENTS | Kafka consumer topic for new events | subscription_events
KAFKA_TOPIC_NOTIFICATION_ORDERS | Kafka producer topic for notification order | notification_orders
ARLAS_SUBSCRIPTIONS_BASE_PATH | ARLAS server for the subscriptions collection | http://localhost:9999/arlas
ARLAS_SUBSCRIPTIONS_SEARCH_ENDPOINT | ARLAS server search endpoint for the subscriptions collection | /explore/subscriptions/_search
ARLAS_SUBSCRIPTIONS_FILTER_ROOT | ARLAS server filter request to be applied to the subscription collection in order to find matching subscriptions | gintersect=(object.geometry)&f=subscription.trigger.event:eq:(event)&f=subscription.trigger.job:eq:(object.job)&f=active:eq:true&f=deleted:eq:false&f=expires_at:gt:now&f=starts_at:lte:now&sort=id}
ARLAS_SERVER_BASE_PATH | ARLAS server for the documents collection | http://localhost:9999/arlas
ARLAS_SERVER_SEARCH_ENDPOINT | ARLAS server search endpoint for the documents collection | /explore/geodata/_search
ARLAS_SERVER_FILTER_ROOT | ARLAS server filter request to be applied to the documents collection in order to find matching documents | f=id:eq:(object.id)

## Build

```sh
mvn clean package
```

## Run
Warning: ARLAS_SUB_TRIG_SCHEM_PATH and ARLAS_SUB_TRIG_SCHEM_PATH_LOCAL must be set. See [Manager configuration]

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

# Packaging

## Helm

ARLAS-subscriptions is packaged as a Helm chart, which can be found here: [helm/arlas-subscriptions](helm/arlas-subscriptions) (documentation embedded).
