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

import io.arlas.client.ApiClient;
import io.arlas.client.ApiException;
import io.arlas.client.model.Hit;
import io.arlas.client.model.Hits;
import io.arlas.subscriptions.app.ArlasSubscriptionsConfiguration;
import io.arlas.subscriptions.kafka.NotificationOrderKafkaProducer;
import io.arlas.subscriptions.model.IndexedUserSubscription;
import io.arlas.subscriptions.model.NotificationOrder;
import io.arlas.subscriptions.model.SubscriptionEvent;
import io.arlas.subscriptions.model.UserSubscription;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductService extends AbstractArlasService {
    private final Logger LOGGER = LoggerFactory.getLogger(ProductService.class);
    private final NotificationOrderKafkaProducer notificationOrderKafkaProducer;
    private final String identityHeader;

    ProductService(ArlasSubscriptionsConfiguration configuration) {
        this.apiClient = new ApiClient().setBasePath(configuration.arlasServerBasePath);
        this.searchEndpoint = configuration.arlasServerSearchEndpoint;
        this.filterRoot = configuration.arlasServerFilterRoot;
        this.notificationOrderKafkaProducer = NotificationOrderKafkaProducer.build(configuration);
        this.identityHeader = configuration.identityHeader;
    }

    void processMatchingProducts(SubscriptionEvent event, List<Hit> subscriptions) {
        String searchFilter = filterRoot.replaceAll("\\{md.id}", event.md.id);

        for (Hit hit : subscriptions) {
            LOGGER.debug("processing hit: " + hit.toString());
            try {
                IndexedUserSubscription userSubscription = objectMapper.convertValue(hit.getData(), IndexedUserSubscription.class);
                LOGGER.debug("indexedUserSubscription: " + userSubscription.toString());
                String f = searchFilter
                        + "&" + userSubscription.subscription.hits.filter
                        + "&" + userSubscription.subscription.hits.projection;
                Hits productHits = getItemHits(getQueryParams(f), getHeaderParams(userSubscription));
                if (productHits.getHits() != null) {
                    productHits.getHits()
                            .stream()
                            .forEach(h -> pushNotificationOrder(h, event, userSubscription));
                }
            } catch (UnsupportedEncodingException e) {
                LOGGER.warn("Parsing exception:", e);
            } catch (ApiException e) {
                LOGGER.warn("Api exception:", e);
            } catch (IOException e) {
                LOGGER.warn("Parsing error:", e);
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
        notificationOrder.md = event.md;
        notificationOrder.collection = event.collection;
        notificationOrder.operation = event.operation;
        // Data fields (projected as specified by subscription creator)
        notificationOrder.data = hit.getData();
        // Subscription fields
        notificationOrder.subscription.id = userSubscription.getId();
        notificationOrder.subscription.callback = userSubscription.subscription.callback;
        // User metadata fields (provided by subscription creator)
        notificationOrder.userMetadatas = userSubscription.userMetadatas;
        LOGGER.debug("notificationOrder: " + notificationOrder.toString());
        notificationOrderKafkaProducer.send(notificationOrder);
    }


}
