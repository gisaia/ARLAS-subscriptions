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
import io.arlas.subscriptions.dao.MongoUserSubscriptionDAOImpl;
import io.arlas.subscriptions.dao.UserSubscriptionDAO;
import io.arlas.subscriptions.db.mongo.MongoDBManaged;
import io.arlas.subscriptions.exception.ArlasSubscriptionsException;
import io.arlas.subscriptions.model.UserSubscription;

import java.util.List;
import java.util.UUID;

public class SubscriptionManagerService {

    private UserSubscriptionDAO daoDatabase;

    public SubscriptionManagerService(ArlasSubscriptionManagerConfiguration configuration, MongoDBManaged mongoDBManaged) throws  ArlasSubscriptionsException {
        this.daoDatabase = new MongoUserSubscriptionDAOImpl(configuration,mongoDBManaged);
    }

    public List<UserSubscription> getAllUserSubscriptions() throws ArlasSubscriptionsException {
        return  this.daoDatabase.getAllUserSubscriptions();
    }

    public UUID postUserSubscription(UserSubscription userSubscription) throws ArlasSubscriptionsException {
        return  this.daoDatabase.postUserSubscription(userSubscription);
    }
}
