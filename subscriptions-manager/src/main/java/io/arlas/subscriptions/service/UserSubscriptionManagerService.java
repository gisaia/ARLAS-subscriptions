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

package io.arlas.subscriptions.service;

import com.mongodb.client.MongoCursor;
import io.arlas.subscriptions.configuration.ArlasSubscriptionManagerConfiguration;
import io.arlas.subscriptions.dao.ElasticUserSubscriptionDAOImpl;
import io.arlas.subscriptions.dao.MongoUserSubscriptionDAOImpl;
import io.arlas.subscriptions.dao.UserSubscriptionDAO;
import io.arlas.subscriptions.db.elastic.ElasticDBManaged;
import io.arlas.subscriptions.db.mongo.MongoDBManaged;
import io.arlas.subscriptions.exception.ArlasSubscriptionsException;
import io.arlas.subscriptions.logger.ArlasLogger;
import io.arlas.subscriptions.logger.ArlasLoggerFactory;
import io.arlas.subscriptions.model.UserSubscription;
import io.arlas.subscriptions.utils.JsonSchemaValidator;
import org.apache.commons.lang3.tuple.Pair;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import static io.arlas.subscriptions.app.ArlasSubscriptionsManager.MANAGER;

public class UserSubscriptionManagerService {
    public final ArlasLogger logger = ArlasLoggerFactory.getLogger(UserSubscriptionManagerService.class, MANAGER);

    private final UserSubscriptionDAO daoDatabase;
    private final UserSubscriptionDAO daoIndexDatabase;

    public UserSubscriptionManagerService(ArlasSubscriptionManagerConfiguration configuration, MongoDBManaged mongoDBManaged, ElasticDBManaged elasticDBManaged) throws ArlasSubscriptionsException, FileNotFoundException {
        String ARLAS_SUB_TRIG_SCHEM_PATH = System.getenv("ARLAS_SUB_TRIG_SCHEM_PATH");
        JsonSchemaValidator jsonSchemaValidator = new JsonSchemaValidator(ARLAS_SUB_TRIG_SCHEM_PATH);
        this.daoDatabase = new MongoUserSubscriptionDAOImpl(configuration.mongoDBConfiguration,mongoDBManaged,jsonSchemaValidator);
        this.daoIndexDatabase = new ElasticUserSubscriptionDAOImpl(configuration.elasticDBConfiguration, configuration.triggerConfiguration,elasticDBManaged,jsonSchemaValidator);
    }

    public Pair<Integer, List<UserSubscription>> getAllUserSubscriptions(String user, Long before, Long after, Boolean active, Boolean started, Boolean expired, boolean deleted, Boolean createdByAdmin, Integer page,
                                                                         Integer size) throws ArlasSubscriptionsException {
        return  this.daoDatabase.getAllUserSubscriptions(user, before, after, active, started, expired, deleted, createdByAdmin, page, size);
    }

    public UserSubscription postUserSubscription(UserSubscription userSubscription, boolean createdByAdmin) throws ArlasSubscriptionsException {
        UserSubscription userSubscriptionForIndex = this.daoDatabase.postUserSubscription(userSubscription, createdByAdmin);

        try {
            this.daoIndexDatabase.postUserSubscription(userSubscriptionForIndex, createdByAdmin);
        } catch (ArlasSubscriptionsException e) {
            this.daoDatabase.deleteUserSubscription(userSubscriptionForIndex.getId());
            throw new ArlasSubscriptionsException("Index subscription in ES failed: " + e.getMessage());
        }
        return userSubscriptionForIndex;
    }

    public Optional<UserSubscription> getUserSubscription(String id, Optional<String> user, boolean deleted) throws ArlasSubscriptionsException {
        return this.daoDatabase.getSubscription(id, user, deleted);
    }

    public void deleteUserSubscription(UserSubscription userSubscription) throws ArlasSubscriptionsException {
        this.daoDatabase.setUserSubscriptionDeletedFlag(userSubscription, true);

        try {
            this.daoIndexDatabase.setUserSubscriptionDeletedFlag(userSubscription, true);
        } catch (ArlasSubscriptionsException e) {
            this.daoDatabase.setUserSubscriptionDeletedFlag(userSubscription, false);
            throw new ArlasSubscriptionsException("Delete subscription in ES failed: " + e.getMessage());
        }
    }

    public UserSubscription putUserSubscription(UserSubscription oldUserSubscription, UserSubscription updUserSubscription) throws ArlasSubscriptionsException {
        updUserSubscription.setId(oldUserSubscription.getId());
        updUserSubscription.setCreated_at(oldUserSubscription.getCreated_at());
        updUserSubscription.setModified_at(new Date().getTime()/ 1000L);
        updUserSubscription.setCreated_by_admin(oldUserSubscription.getCreated_by_admin());
        updUserSubscription.setDeleted(oldUserSubscription.getDeleted());

        this.daoDatabase.putUserSubscription(updUserSubscription);
        try {
            this.daoIndexDatabase.putUserSubscription(updUserSubscription);
        } catch (ArlasSubscriptionsException e) {
            this.daoDatabase.putUserSubscription(oldUserSubscription);
            throw new ArlasSubscriptionsException("Update subscription in ES failed: " + e.getMessage());
        }
        return updUserSubscription;
    }

    public void syncDBtoIndex() {
        logger.info("SyncDBtoIndex request received");
        Pair<Long, MongoCursor<UserSubscription>> allSubs = ((MongoUserSubscriptionDAOImpl) this.daoDatabase).getAllUserSubscriptions();
        logger.info("Total number of documents to index: " + allSubs.getLeft());

        try (MongoCursor<UserSubscription> cursor = allSubs.getRight()) {
            ((ElasticUserSubscriptionDAOImpl) daoIndexDatabase).initBulkProcessor(allSubs.getLeft());
            allSubs.getRight().forEachRemaining(subscription -> ((ElasticUserSubscriptionDAOImpl) daoIndexDatabase).addToBulkProcessor(subscription));
            ((ElasticUserSubscriptionDAOImpl) daoIndexDatabase).finaliseBulkProcessor();
        } catch (ArlasSubscriptionsException e) {
            logger.error("Can't do sync: " + e.getMessage());
        } finally {
            logger.info("SyncDBtoIndex finished");
        }
    }

}
