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

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import io.arlas.subscriptions.app.ArlasSubscriptionManagerConfiguration;
import io.arlas.subscriptions.db.mongo.MongoDBManaged;
import io.arlas.subscriptions.exception.ArlasSubscriptionsException;
import io.arlas.subscriptions.model.UserSubscription;
import io.arlas.subscriptions.utils.JsonSchemaValidator;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.everit.json.schema.ValidationException;

import java.util.*;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.set;
import static io.arlas.subscriptions.utils.UUIDHelper.generateUUID;

public class MongoUserSubscriptionDAOImpl implements UserSubscriptionDAO {

    private MongoCollection<UserSubscription> mongoCollection;
    private JsonSchemaValidator jsonSchemaValidator;

    public MongoUserSubscriptionDAOImpl(ArlasSubscriptionManagerConfiguration configuration, MongoDBManaged mongoDBManaged,JsonSchemaValidator jsonSchemaValidator) throws ArlasSubscriptionsException {
        MongoDatabase mongoDatabase = mongoDBManaged.mongoClient.getDatabase(configuration.getMongoDBConnection().database);
        this.mongoCollection = this.initSubscriptionsCollection(mongoDatabase);
        this.jsonSchemaValidator = jsonSchemaValidator;
    }

    @Override
    public List<UserSubscription> getAllUserSubscriptions(String user, Long before, Boolean active, Boolean expired, boolean getDeleted, Integer page, Integer size) throws ArlasSubscriptionsException {
        List<Bson> filters = new ArrayList<>();
        if (user != null) filters.add(eq("created_by", user));
        if (!getDeleted) filters.add(eq("deleted", Boolean.FALSE));
        if (active != null) filters.add(eq("active", active));
        if (expired != null) filters.add(expired ? lte("expires_at", System.currentTimeMillis()/1000) : gt("expires_at", System.currentTimeMillis()/1000));
        if (before != null) filters.add(lte("created_at", before));

        List<UserSubscription> userSubscriptionFind = new ArrayList<>();
        try (MongoCursor<UserSubscription> userSubscriptions = this.mongoCollection.find(and(filters)).skip(size * (page - 1)).limit(size).iterator()) {
            while (userSubscriptions.hasNext()) {
                final UserSubscription userSubscription = userSubscriptions.next();
                userSubscriptionFind.add(userSubscription);
            }
        }
        return userSubscriptionFind;
    }

    @Override
    public Optional<UserSubscription> getUserSubscription(String user, String id, boolean getDeleted) {
        List<Bson> filters = new ArrayList<>();
        filters.add(eq("_id", id));
        if (user != null) filters.add(eq("created_by", user));
        if (!getDeleted) filters.add(eq("deleted", Boolean.FALSE));

        return Optional.ofNullable(this.mongoCollection.find(and(filters)).first());
    }

    @Override
    public void setUserSubscriptionDeletedFlag(UserSubscription userSubscription, boolean isDeleted) throws ArlasSubscriptionsException {
        try {
            if (!this.mongoCollection.updateOne(eq("_id", userSubscription.getId()), set("deleted", isDeleted)).wasAcknowledged()) {
                throw new ArlasSubscriptionsException("userSubscription update in DB not acknowledged");
            }
        } catch (MongoException e) {
            throw new ArlasSubscriptionsException("userSubscription update in DB failed",e);
        }
    }

    @Override
    public UserSubscription postUserSubscription(UserSubscription userSubscription, boolean createdByAdmin) throws ArlasSubscriptionsException {
        try {
            this.jsonSchemaValidator.validJsonObjet(userSubscription.subscription.trigger);
            UUID uuid = generateUUID();
            userSubscription.setId(uuid.toString());
            userSubscription.setCreated_at(new Date().getTime());
            userSubscription.setModified_at(new Date().getTime());
            userSubscription.setCreated_by_admin(createdByAdmin);
            userSubscription.setDeleted(false);
            this.mongoCollection.insertOne(userSubscription);
        } catch (ValidationException e) {
            throw new ArlasSubscriptionsException("Error in validation of trigger json schema :" + e.getErrorMessage(),e);
        } catch (MongoException e) {
            throw new ArlasSubscriptionsException("Can not insert userSubscription in mongo.", e);
        }
        return userSubscription;
    }

    @Override
    public void putUserSubscription(UserSubscription updUserSubscription) throws ArlasSubscriptionsException {
        try {
            this.jsonSchemaValidator.validJsonObjet(updUserSubscription.subscription.trigger);
            if (!this.mongoCollection.replaceOne(eq("_id", updUserSubscription.getId()), updUserSubscription).wasAcknowledged()) {
                throw new ArlasSubscriptionsException("userSubscription update in DB not acknowledged");
            }
        } catch (ValidationException e) {
            throw new ArlasSubscriptionsException("Error in validation of trigger json schema :" + e.getErrorMessage(),e);
        } catch (MongoException e) {
            throw new ArlasSubscriptionsException("userSubscription update in DB failed",e);
        } catch (Exception e) {
            throw new ArlasSubscriptionsException("Can not update userSubscription in mongo.", e);
        }
    }

    @Override
    public void deleteUserSubscription(String ref) throws ArlasSubscriptionsException {
        this.mongoCollection.deleteOne(new Document("_id", ref));
    }

    private MongoCollection<UserSubscription> initSubscriptionsCollection(MongoDatabase mongoDatabase) throws ArlasSubscriptionsException {
        boolean collectionExists = mongoDatabase.listCollectionNames()
                .into(new ArrayList<>()).contains("arlas-subscription");
        if (!collectionExists) {
            mongoDatabase.createCollection("arlas-subscription");
        }
        return mongoDatabase.getCollection("arlas-subscription",UserSubscription.class) ;
    }
}
