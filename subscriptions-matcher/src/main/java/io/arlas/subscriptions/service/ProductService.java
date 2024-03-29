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

import com.fasterxml.jackson.core.JsonProcessingException;
import io.arlas.commons.exceptions.ArlasException;
import io.arlas.server.client.ApiClient;
import io.arlas.server.client.ApiException;
import io.arlas.server.client.model.ArlasHit;
import io.arlas.server.client.model.Hits;
import io.arlas.subscriptions.app.ArlasSubscriptionsMatcherConfiguration;
import io.arlas.subscriptions.exception.ArlasSubscriptionsException;
import io.arlas.subscriptions.kafka.NotificationOrderKafkaProducer;
import io.arlas.subscriptions.logger.ArlasLogger;
import io.arlas.subscriptions.logger.ArlasLoggerFactory;
import io.arlas.subscriptions.model.IndexedUserSubscription;
import io.arlas.subscriptions.model.NotificationOrder;
import io.arlas.subscriptions.model.SubscriptionEvent;
import io.arlas.subscriptions.model.UserSubscription;
import io.arlas.subscriptions.utils.JSONValueInjector;
import org.apache.commons.lang3.StringUtils;
import org.locationtech.jts.io.ParseException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.arlas.subscriptions.app.ArlasSubscriptionsMatcher.MATCHER;

public class ProductService extends AbstractArlasService {
    private final ArlasLogger logger = ArlasLoggerFactory.getLogger(ProductService.class, MATCHER);
    private final NotificationOrderKafkaProducer notificationOrderKafkaProducer;
    private final String identityHeader;

    ProductService(ArlasSubscriptionsMatcherConfiguration configuration, NotificationOrderKafkaProducer notificationOrderKafkaProducer) {
        this.apiClient = new ApiClient().setBasePath(configuration.arlasServerBasePath);
        this.searchEndpoint = configuration.arlasServerSearchEndpoint;
        this.filterRoot = configuration.arlasServerFilterRoot;
        this.notificationOrderKafkaProducer = notificationOrderKafkaProducer;
        this.identityHeader = configuration.identityConfiguration.identityHeader;
    }

    void processMatchingProducts(SubscriptionEvent event, List<ArlasHit> subscriptions) throws JsonProcessingException, ParseException {

        try {
            String searchFilter = JSONValueInjector.inject(filterRoot, event);

            for (ArlasHit hit : subscriptions) {
                try {
                    IndexedUserSubscription userSubscription = objectMapper.convertValue(hit.getData(), IndexedUserSubscription.class);
                    userSubscription.setId((String) ((Map<String, Object>) hit.getData()).get("id"));
                    logger.trace("indexedUserSubscription: " + userSubscription);
                    String f = searchFilter
                            + "&" + userSubscription.subscription.hits.filter;
                    if (!StringUtils.isBlank(userSubscription.subscription.hits.projection)) {
                        f += "&" + userSubscription.subscription.hits.projection;
                    }
                    Hits productHits = getItemHits(getQueryParams(f), getHeaderParams(userSubscription));
                    if (productHits.getHits() != null && !productHits.getHits().isEmpty()) {
                        productHits.getHits()
                                .forEach(h -> pushNotificationOrder(h, event, userSubscription));
                    } else {
                        logger.warn("Could not find product in catalog: " + f);
                    }
                } catch (ApiException | IOException | ArlasSubscriptionsException | ArlasException e) {
                    logger.warn("Error while fetching matching products: " + e.getMessage());
                }
            }
        } catch (ArlasSubscriptionsException e) {
            logger.warn("Error while fetching matching products: " + e.getMessage());
        }
    }

    private Map<String, String> getHeaderParams(UserSubscription userSubscription) {
        Map<String, String> headerParams = new HashMap<>();
        if (!StringUtils.isEmpty(identityHeader)) {
            headerParams.put(identityHeader, userSubscription.created_by);
        }
        return headerParams;
    }

    private void pushNotificationOrder(ArlasHit hit, SubscriptionEvent event, IndexedUserSubscription userSubscription) {
        NotificationOrder notificationOrder = new NotificationOrder();
        // Event fields (copied from event message)
        for (Object key : event.keySet()) {
            notificationOrder.put(key,event.get(key));
        }
        // Data fields (projected as specified by subscription creator)
        notificationOrder.put("data", hit.getData());
        // Subscription fields
        Map<String, Object> subSummary = new HashMap<>();
        subSummary.put("id", userSubscription.getId());
        subSummary.put("callback", userSubscription.subscription.callback);
        notificationOrder.put("subscription", subSummary);
        // User metadata fields (provided by subscription creator)
        notificationOrder.put("user_metadata", userSubscription.userMetadatas);
        logger.trace("notificationOrder: " + notificationOrder);
        notificationOrderKafkaProducer.send(notificationOrder);
    }


}
