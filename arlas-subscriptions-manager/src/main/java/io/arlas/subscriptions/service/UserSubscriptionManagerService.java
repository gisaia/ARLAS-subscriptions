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
import org.locationtech.jts.io.ParseException;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class UserSubscriptionManagerService {

    private UserSubscriptionDAO daoDatabase;
    private UserSubscriptionDAO daoIndexDatabase;


    public UserSubscriptionManagerService(ArlasSubscriptionManagerConfiguration configuration, MongoDBManaged mongoDBManaged, ElasticDBManaged elasticDBManaged) throws  ArlasSubscriptionsException {
        this.daoDatabase = new MongoUserSubscriptionDAOImpl(configuration,mongoDBManaged);
        this.daoIndexDatabase = new ElasticUserSubscriptionDAOImpl(configuration,elasticDBManaged);

    }

    public List<UserSubscription> getAllUserSubscriptions() throws ArlasSubscriptionsException {
        return  this.daoDatabase.getAllUserSubscriptions();
    }

    public UserSubscription postUserSubscription(UserSubscription userSubscription) throws ArlasSubscriptionsException, IOException, ParseException {
        UserSubscription userSubscriptionForIndex;
        try{
            userSubscriptionForIndex =  this.daoDatabase.postUserSubscription(userSubscription);
        }catch(ArlasSubscriptionsException e){
            throw new ArlasSubscriptionsException("Insert userSubscription in mongo failed",e);
        }
        try{
            this.daoIndexDatabase.postUserSubscription(userSubscriptionForIndex);
        }catch(ArlasSubscriptionsException e){
            this.daoDatabase.deleteUserSubscription(userSubscriptionForIndex.getId());
            throw new ArlasSubscriptionsException("Index userSubscription in ES failed",e);
        }
        return userSubscriptionForIndex ;
    }

    public Optional<UserSubscription> getUserSubscription(String user, String id) {
        return this.daoDatabase.getUserSubscription(user, id);
    }
}
