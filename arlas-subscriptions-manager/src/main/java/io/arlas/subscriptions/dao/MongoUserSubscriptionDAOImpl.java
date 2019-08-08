/*
 * Licensed to Gisaïa under one or more contributor
 * license agreements. See the NOTICE.txt file distributed with
 * this work for additional information regarding copyright
 * ownership. Gisaïa licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.arlas.subscriptions.dao;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import io.arlas.subscriptions.app.ArlasSubscriptionManagerConfiguration;
import io.arlas.subscriptions.db.mongo.MongoDBManaged;
import io.arlas.subscriptions.exception.ArlasSubscriptionsException;
import io.arlas.subscriptions.model.UserSubscription;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static io.arlas.subscriptions.utils.UUIDHelper.generateUUID;

public class MongoUserSubscriptionDAOImpl implements UserSubscriptionDAO {

    private  MongoCollection<UserSubscription> mongoCollection;

    public MongoUserSubscriptionDAOImpl(ArlasSubscriptionManagerConfiguration configuration, MongoDBManaged mongoDBManaged) throws ArlasSubscriptionsException {
        MongoDatabase mongoDatabase = mongoDBManaged.
                    mongoClient.
                    getDatabase(configuration.getMongoDBConnection().database);
            this.mongoCollection = this.initSubscriptionsCollection(mongoDatabase);
    }

    @Override
    public  List<UserSubscription> getAllUserSubscriptions() throws ArlasSubscriptionsException {

        List<UserSubscription> userSubscriptionFind = new ArrayList<>();
        try(MongoCursor<UserSubscription> userSubscriptions = this.mongoCollection.find().iterator()) {
            while (userSubscriptions.hasNext()) {
                final UserSubscription userSubscription = userSubscriptions.next();
                userSubscriptionFind.add(userSubscription);
            }
        }
        return userSubscriptionFind;
    }

    @Override
    public UserSubscription postUserSubscription(UserSubscription userSubscription) throws ArlasSubscriptionsException {

        UUID uuid = generateUUID();
        userSubscription.setId(uuid.toString());
        userSubscription.setCreated_at(new Date().getTime());
        userSubscription.setModified_at(new Date().getTime());
        userSubscription.setCreated_by_admin(false);
        userSubscription.setDeleted(false);

        this.mongoCollection.insertOne(userSubscription);

        return userSubscription;
    }

    private MongoCollection<UserSubscription> initSubscriptionsCollection(MongoDatabase mongoDatabase) throws ArlasSubscriptionsException {
        boolean collectionExists = mongoDatabase.listCollectionNames()
                .into(new ArrayList<>()).contains("arlas-subscription");
        if(!collectionExists){
            mongoDatabase.createCollection("arlas-subscription");
        }
        return mongoDatabase.getCollection("arlas-subscription",UserSubscription.class) ;
    }
}
