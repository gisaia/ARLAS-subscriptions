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

import io.arlas.subscriptions.exception.ArlasSubscriptionsException;
import io.arlas.subscriptions.model.UserSubscription;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Optional;

public interface UserSubscriptionDAO {

    Pair<Integer, List<UserSubscription>> getAllUserSubscriptions(String user, Long before, Long after, Boolean active, Boolean started, Boolean expired, boolean deleted, Boolean createdByAdmin, Integer page,
                                                                  Integer size) throws ArlasSubscriptionsException;

    UserSubscription postUserSubscription(UserSubscription userSubscription, boolean createdByAdmin) throws ArlasSubscriptionsException;

    void deleteUserSubscription(String ref) throws ArlasSubscriptionsException;

    Optional<UserSubscription> getSubscription(String id, Optional<String> user, boolean deleted) throws ArlasSubscriptionsException;

    void setUserSubscriptionDeletedFlag(UserSubscription userSubscription, boolean isDeleted) throws ArlasSubscriptionsException;

    void putUserSubscription(UserSubscription updUserSubscription) throws ArlasSubscriptionsException;
}
