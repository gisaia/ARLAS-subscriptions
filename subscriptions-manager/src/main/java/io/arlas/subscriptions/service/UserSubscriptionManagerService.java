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

import io.arlas.subscriptions.app.ArlasSubscriptionManagerConfiguration;
import io.arlas.subscriptions.dao.ElasticUserSubscriptionDAOImpl;
import io.arlas.subscriptions.dao.MongoUserSubscriptionDAOImpl;
import io.arlas.subscriptions.dao.UserSubscriptionDAO;
import io.arlas.subscriptions.db.elastic.ElasticDBManaged;
import io.arlas.subscriptions.db.mongo.MongoDBManaged;
import io.arlas.subscriptions.exception.ArlasSubscriptionsException;
import io.arlas.subscriptions.model.UserSubscription;
import io.arlas.subscriptions.utils.JsonSchemaValidator;

import java.io.FileNotFoundException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class UserSubscriptionManagerService {

    private UserSubscriptionDAO daoDatabase;
    private UserSubscriptionDAO daoIndexDatabase;
    private final String ARLAS_SUB_TRIG_SCHEM_PATH = System.getenv("ARLAS_SUB_TRIG_SCHEM_PATH");


    public UserSubscriptionManagerService(ArlasSubscriptionManagerConfiguration configuration, MongoDBManaged mongoDBManaged, ElasticDBManaged elasticDBManaged) throws ArlasSubscriptionsException, FileNotFoundException {

        JsonSchemaValidator jsonSchemaValidator = new JsonSchemaValidator(ARLAS_SUB_TRIG_SCHEM_PATH);
        this.daoDatabase = new MongoUserSubscriptionDAOImpl(configuration,mongoDBManaged,jsonSchemaValidator);
        this.daoIndexDatabase = new ElasticUserSubscriptionDAOImpl(configuration,elasticDBManaged,jsonSchemaValidator);
    }

    public List<UserSubscription> getAllUserSubscriptions(String user, Long before, Boolean active, Boolean expired, boolean getDeleted) throws ArlasSubscriptionsException {
        return  this.daoDatabase.getAllUserSubscriptions(user, before, active, expired, getDeleted);
    }

    public UserSubscription postUserSubscription(UserSubscription userSubscription, boolean createdByAdmin) throws ArlasSubscriptionsException {
        UserSubscription userSubscriptionForIndex = this.daoDatabase.postUserSubscription(userSubscription, createdByAdmin);

        try {
            this.daoIndexDatabase.postUserSubscription(userSubscriptionForIndex, createdByAdmin);
        } catch (ArlasSubscriptionsException e) {
            this.daoDatabase.deleteUserSubscription(userSubscriptionForIndex.getId());
            throw new ArlasSubscriptionsException("Index userSubscription in ES failed",e);
        }
        return userSubscriptionForIndex ;
    }

    public Optional<UserSubscription> getUserSubscription(String user, String id, boolean getDeleted) {
        return this.daoDatabase.getUserSubscription(user, id, getDeleted);
    }

    public void deleteUserSubscription(UserSubscription userSubscription) throws ArlasSubscriptionsException {
        this.daoDatabase.setUserSubscriptionDeletedFlag(userSubscription, true);

        try {
            this.daoIndexDatabase.setUserSubscriptionDeletedFlag(userSubscription, true);
        } catch (ArlasSubscriptionsException e) {
            this.daoDatabase.setUserSubscriptionDeletedFlag(userSubscription, false);
            throw new ArlasSubscriptionsException("userSubscription update in ES failed",e);
        }
    }

    public UserSubscription putUserSubscription(String user, UserSubscription oldUserSubscription, UserSubscription updUserSubscription) throws ArlasSubscriptionsException {
        updUserSubscription.setId(oldUserSubscription.getId());
        updUserSubscription.setCreated_at(oldUserSubscription.getCreated_at());
        updUserSubscription.setModified_at(new Date().getTime());
        updUserSubscription.setCreated_by_admin(oldUserSubscription.getCreated_by_admin());
        updUserSubscription.setDeleted(oldUserSubscription.getDeleted());

        this.daoDatabase.putUserSubscription(updUserSubscription);
        try {
            this.daoIndexDatabase.putUserSubscription(updUserSubscription);
        } catch (ArlasSubscriptionsException e) {
            this.daoDatabase.putUserSubscription(oldUserSubscription);
            throw new ArlasSubscriptionsException("userSubscription update in ES failed",e);
        }
        return updUserSubscription;
    }
}
