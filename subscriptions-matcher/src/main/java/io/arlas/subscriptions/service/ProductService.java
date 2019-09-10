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
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.client.ApiClient;
import io.arlas.server.client.ApiException;
import io.arlas.server.client.model.Hit;
import io.arlas.server.client.model.Hits;
import io.arlas.subscriptions.app.ArlasSubscriptionsConfiguration;
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

    ProductService(ArlasSubscriptionsConfiguration configuration) {
        this.apiClient = new ApiClient().setBasePath(configuration.arlasServerBasePath);
        this.searchEndpoint = configuration.arlasServerSearchEndpoint;
        this.filterRoot = configuration.arlasServerFilterRoot;
        this.notificationOrderKafkaProducer = NotificationOrderKafkaProducer.build(configuration);
        this.identityHeader = configuration.identityHeader;
    }

    void processMatchingProducts(SubscriptionEvent event, List<Hit> subscriptions) throws JsonProcessingException, ParseException {

        String searchFilter = JSONValueInjector.inject(filterRoot, event);

        for (Hit hit : subscriptions) {
            logger.debug("processing hit: " + hit.toString());
            try {
                IndexedUserSubscription userSubscription = objectMapper.convertValue(hit.getData(), IndexedUserSubscription.class);
                logger.debug("indexedUserSubscription: " + userSubscription.toString());
                String f = searchFilter
                        + "&" + userSubscription.subscription.hits.filter
                        + "&" + userSubscription.subscription.hits.projection;
                Hits productHits = getItemHits(getQueryParams(f), getHeaderParams(userSubscription));
                if (productHits.getHits() != null) {
                    productHits.getHits()
                            .stream()
                            .forEach(h -> pushNotificationOrder(h, event, userSubscription));
                }
            } catch (ApiException | IOException | ArlasSubscriptionsException | ArlasException e) {
                logger.warn("Error while fetching matching products: " + e.getMessage());
            }
        }
    }

    private Map<String, String> getHeaderParams(UserSubscription userSubscription) {
        Map headerParams = new HashMap<>();
        if (!StringUtils.isEmpty(identityHeader)) {
            headerParams.put(identityHeader, userSubscription.created_by);
        }
        return headerParams;
    }

    private void pushNotificationOrder(Hit hit, SubscriptionEvent event, IndexedUserSubscription userSubscription) {
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
        // User metadata fields (provided by subscription creator)
        notificationOrder.put("user_metadata", userSubscription.userMetadatas);
        logger.debug("notificationOrder: " + notificationOrder.toString());
        notificationOrderKafkaProducer.send(notificationOrder);
    }


}
