#!/bin/bash

mongo <<EOF
var config = {
    "_id": "dbrs",
    "version": 1,
    "members": [
        {
            "_id": 1,
            "host": "mongodb:27017",
            "priority": 2
        },
        {
            "_id": 2,
            "host": "mongo2:27017",
            "priority": 0
        },
        {
            "_id": 3,
            "host": "mongo3:27017",
            "priority": 0
        }
    ]
};
rs.initiate(config, { force: true });
rs.status();
EOF

sleep 20

mongo <<EOF
  use admin;
  admin = db.getSiblingDB("admin");
  admin.createUser(
     {
	      user: "mongouser",
        pwd: "secret",
        roles: [ { role: "root", db: "admin" } ]
     });
     db.getSiblingDB("admin").auth("mongouser", "secret");
     rs.status();
EOF