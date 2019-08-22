# INTEGRATION TESTS DATASETS

All data is indexed with [DataSetTool](src/test/java/io/arlas/subscriptions/DataSetTool.java) or by extending [AbstractTestWithData](src/test/java/io/arlas/subscriptions/AbstractTestWithData.java).

You can have a closer look to this dataset by running `./scripts/tests-stack.sh` and browse :
* [http://localhost:9999/arlas/swagger#/]()
* [http://localhost:9200/dataset/_search]()
* [http://localhost:9200/subs/_search]()

## Documents collection

#### Document example
@see [Mapping](src/test/resources/dataset.mapping.json)
```json
  {
    "id": "ID__170__20DI",
    "fullname": "My name is ID__170__20DI",
    "params": {
      "country": "Armenia",
      "city": "Albi",
      "job": "Dancer",
      "startdate": 813400,
      "stopdate": 813500,
      "age": 3400
    },
    "geo_params": {
      "centroid": "-20,-170",
      "geometry": {
        "coordinates": [
          [
            [
              -171,
              -19
            ],
            [
              -169,
              -19
            ],
            [
              -169,
              -21
            ],
            [
              -171,
              -21
            ],
            [
              -171,
              -19
            ]
          ]
        ],
        "type": "Polygon"
      }
    }
  }
```

#### Collection description
```json
{
  "collection_name": "geodata",
  "params": {
    "index_name": "dataset",
    "type_name": "mytype",
    "id_path": "id",
    "geometry_path": "geo_params.geometry",
    "centroid_path": "geo_params.centroid",
    "timestamp_path": "params.startdate"
  }
}
```

## Subscriptions collection

#### Subscription
@see [Mapping](src/test/resources/subscriptions.mapping.json)

Only one subscription is indexed in subscription collection.

```json
{
  "id":"1234",
  "modified_at":-1,
  "created_by_admin":false,
  "created_at":1564578988,
  "active":true,
  "title":"Test Subscription",
  "created_by":"gisaia",
  "deleted":false,
  "expires_at":-1,
  "subscription":{
    "trigger":{
      "geometry":"POLYGON((-50 50,50 50,50 -50,-50 -50,-50 50))",
      "job":"[Actor, Announcers, Archeologists, Architect, Brain Scientist]",
      "event":"[UPDATE]"
    },
    "hits":{
      "filter":"f=params.city:eq:Toulouse",
      "projection":"exclude=params.country"
    },
    "callback":"http://myservice.com/mycallback"
  },
  "userMetadatas":{
    "correlationId":"my-correlation-id"
  },
  "centroid":"0,0",
  "geometry":{
    "coordinates":[[[-50.0,50.0],[50.0,50.0],[50.0,-50.0],[-50.0,-50.0],[-50.0,50.0]]],
    "type":"Polygon"
  }
}
```

This subscription will match `UPDATE` events for the following document IDs : 
* ID__10__30DI
* ID_10__30DI
* ID_30__10DI
* ID_30_10DI
* ID_40_0DI
* ID__40_0DI
* ID__20_20DI
* ID_0__40DI
* ID_0_40DI
* ID_10_30DI
* ID_20_20DI
* ID__10_30DI
* ID__30__10DI
* ID__30_10DI
* ID__20__20DI
* ID_20__20DI



#### Collection description
```json
{
  "collection_name": "geodata",
  "params": {
    "index_name": "subs",
    "type_name": "sub_type",
    "id_path": "id",
    "geometry_path": "geometry",
    "centroid_path": "centroid",
    "timestamp_path": "created_at"
  }
}
```