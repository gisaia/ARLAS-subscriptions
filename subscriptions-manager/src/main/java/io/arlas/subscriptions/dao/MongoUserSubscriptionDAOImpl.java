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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoException;
import com.mongodb.MongoTimeoutException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Facet;
import com.mongodb.client.model.Sorts;
import io.arlas.subscriptions.app.ArlasSubscriptionManagerConfiguration;
import io.arlas.subscriptions.db.mongo.MongoDBManaged;
import io.arlas.subscriptions.exception.ArlasSubscriptionsException;
import io.arlas.subscriptions.model.UserSubscription;
import io.arlas.subscriptions.model.mongo.MongoDBConnection;
import io.arlas.subscriptions.utils.JsonSchemaValidator;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.json.JsonWriterSettings;
import org.everit.json.schema.ValidationException;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.set;
import static io.arlas.subscriptions.utils.UUIDHelper.generateUUID;

public class MongoUserSubscriptionDAOImpl implements UserSubscriptionDAO {

    public static final String ARLAS_SUBSCRIPTION_DB_NAME = "arlas-subscription";

    private MongoCollection<UserSubscription> mongoCollectionSub;
    private MongoCollection<Document> mongoCollectionDoc;
    private JsonSchemaValidator jsonSchemaValidator;
    private static ObjectMapper mapper = new ObjectMapper();
    private static JsonWriterSettings jsonWritterSettings = JsonWriterSettings.builder()
            .int64Converter((value, writer) -> writer.writeNumber(value.toString()))
            .build();

    public MongoUserSubscriptionDAOImpl(MongoDBConnection mongoDBConnection, MongoDBManaged mongoDBManaged, JsonSchemaValidator jsonSchemaValidator) throws ArlasSubscriptionsException {
        MongoDatabase mongoDatabase = mongoDBManaged.mongoClient.getDatabase(mongoDBConnection.database);
        this.mongoCollectionSub = this.initSubscriptionsCollection(mongoDatabase);
        this.mongoCollectionDoc = mongoDatabase.getCollection(ARLAS_SUBSCRIPTION_DB_NAME);
        this.jsonSchemaValidator = jsonSchemaValidator;
    }

    @Override
    public Pair<Integer, List<UserSubscription>> getAllUserSubscriptions(String user, Long before, Long after, Boolean active, Boolean started, Boolean expired, boolean deleted, Boolean createdByAdmin, Integer page,
                                                                         Integer size) throws ArlasSubscriptionsException {
        try {
            List<Bson> filters = new ArrayList<>();
            if (user != null)
                filters.add(eq("created_by", user));
            if (!deleted)
                filters.add(eq("deleted", Boolean.FALSE));
            if (active != null)
                filters.add(eq("active", active));
            if (started != null)
                filters.add(started ? lte("starts_at", System.currentTimeMillis() / 1000l) : gt("started", System.currentTimeMillis() / 1000l));
            if (createdByAdmin != null)
                filters.add(eq("created_by_admin", createdByAdmin));
            if (expired != null)
                filters.add(expired ? lte("expires_at", System.currentTimeMillis() / 1000l) : gt("expires_at", System.currentTimeMillis() / 1000l));
            if (before != null)
                filters.add(lte("created_at", before));
            if (after != null)
                filters.add(gte("created_at", after));

            List<Bson> aggregate = new ArrayList<>();
            aggregate.add(sort(Sorts.descending("created_at")));
            if (!filters.isEmpty()) {
                aggregate.add(match(and(filters)));
            }
            aggregate.add(facet(
                    new Facet("subList", skip(size * (page - 1)), limit(size)),
                    new Facet("totalCount", count())
            ));

            Document aggResult = this.mongoCollectionDoc.aggregate(aggregate).first();

            Integer total = ((List<Document>) aggResult.get("totalCount")).size() == 1 ? (Integer)((List<Document>) aggResult.get("totalCount")).get(0).get("count") : new Integer(0);
            List<UserSubscription> subList = ((List<Document>) aggResult.get("subList")).stream().map(d -> convertDocument(d)).collect(Collectors.toList());
            return Pair.of(total, subList);
        } catch (MongoException e) {
            throw new ArlasSubscriptionsException("Get subscriptions from mongoDB failed:" + e.getMessage());
        }
    }

    private UserSubscription convertDocument(Document document) {
        try {
            UserSubscription userSubscription = mapper.readValue(document.toJson(jsonWritterSettings), UserSubscription.class);
            // these fields are READ_ONLY so we need to set them manually
            userSubscription.setId((String)document.get("_id"));
            userSubscription.setCreated_at((Long)document.get("created_at"));
            userSubscription.setModified_at((Long)document.get("modified_at"));
            userSubscription.setCreated_by_admin((Boolean)document.get("created_by_admin"));
            userSubscription.setDeleted((Boolean)document.get("deleted"));
            return userSubscription;
        } catch (IOException e) {
            // not expected as we are reading a document that we wrote ourselves
            throw new RuntimeException("Failed to parse JSON from MongoDB " + document.toJson(jsonWritterSettings) + ": " + e.getMessage());
        }
    }

    @Override
    public Optional<UserSubscription> getUserSubscription(String user, String id, boolean deleted) throws ArlasSubscriptionsException {
        List<Bson> filters = new ArrayList<>();
        filters.add(eq("_id", id));
        if (user != null)
            filters.add(eq("created_by", user));
        if (!deleted)
            filters.add(eq("deleted", Boolean.FALSE));

        try {
            return Optional.ofNullable(this.mongoCollectionSub.find(and(filters)).first());
        } catch (MongoException e) {
            throw new ArlasSubscriptionsException("Get subscription from mongoDB failed:" + e.getMessage());
        }

    }

    @Override
    public void setUserSubscriptionDeletedFlag(UserSubscription userSubscription, boolean isDeleted) throws ArlasSubscriptionsException {
        try {
            if (!this.mongoCollectionSub.updateOne(eq("_id", userSubscription.getId()), set("deleted", isDeleted)).wasAcknowledged()) {
                throw new ArlasSubscriptionsException("Update deleted flag in mongoDB not acknowledged");
            }
        } catch (MongoException e) {
            throw new ArlasSubscriptionsException("Update deleted flag in mongoDB failed:" + e.getMessage());
        }
    }

    @Override
    public UserSubscription postUserSubscription(UserSubscription userSubscription, boolean createdByAdmin) throws ArlasSubscriptionsException {
        try {
            this.jsonSchemaValidator.validJsonObjet(userSubscription.subscription.trigger);
            UUID uuid = generateUUID();
            userSubscription.setId(uuid.toString());
            userSubscription.setCreated_at(new Date().getTime()/1000l);
            userSubscription.setModified_at(new Date().getTime()/1000l);
            userSubscription.setCreated_by_admin(createdByAdmin);
            userSubscription.setDeleted(false);
            this.mongoCollectionSub.insertOne(userSubscription);
        } catch (ValidationException e) {
            throw new ArlasSubscriptionsException("Error in validation of trigger json schema: " + e.getErrorMessage());
        } catch (MongoException e) {
            throw new ArlasSubscriptionsException("Insert subscription in mongoDB failed:" + e.getMessage());
        }
        return userSubscription;
    }

    @Override
    public void putUserSubscription(UserSubscription updUserSubscription) throws ArlasSubscriptionsException {
        try {
            this.jsonSchemaValidator.validJsonObjet(updUserSubscription.subscription.trigger);
            if (!this.mongoCollectionSub.replaceOne(eq("_id", updUserSubscription.getId()), updUserSubscription).wasAcknowledged()) {
                throw new ArlasSubscriptionsException("Update subscription in mongoDB not acknowledged");
            }
        } catch (ValidationException e) {
            throw new ArlasSubscriptionsException("Error in validation of trigger json schema: " + e.getErrorMessage());
        } catch (MongoException e) {
            throw new ArlasSubscriptionsException("Update subscription in mongoDB failed: " + e.getMessage());
        }
    }

    @Override
    public void deleteUserSubscription(String ref) throws ArlasSubscriptionsException {
        try {
            this.mongoCollectionSub.deleteOne(new Document("_id", ref));
        } catch (MongoException e) {
            throw new ArlasSubscriptionsException("Delete subscription in mongoDB failed:" + e.getMessage());
        }
    }

    private MongoCollection<UserSubscription> initSubscriptionsCollection(MongoDatabase mongoDatabase) throws ArlasSubscriptionsException {
        try {
            boolean collectionExists = mongoDatabase.listCollectionNames().into(new ArrayList<>()).contains("arlas-subscription");
            if (!collectionExists) {
                mongoDatabase.createCollection(ARLAS_SUBSCRIPTION_DB_NAME);
            }
            return mongoDatabase.getCollection(ARLAS_SUBSCRIPTION_DB_NAME, UserSubscription.class);
        } catch (MongoTimeoutException e) {
            throw new ArlasSubscriptionsException("Unable to connect to MongoDB: " + e.getMessage());
        }
    }
}
