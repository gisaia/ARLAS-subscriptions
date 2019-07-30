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
import io.arlas.client.Pair;
import io.arlas.client.model.Hit;
import io.arlas.client.model.Hits;
import io.arlas.subscriptions.app.ArlasSubscriptionsConfiguration;
import io.arlas.subscriptions.kafka.NotificationOrderKafkaProducer;
import io.arlas.subscriptions.model.NotificationOrder;
import io.arlas.subscriptions.model.SubscriptionEvent;
import io.arlas.subscriptions.model.UserSubscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProductService extends AbstractArlasService {
    private final Logger LOGGER = LoggerFactory.getLogger(ProductService.class);
    private final NotificationOrderKafkaProducer notificationOrderKafkaProducer;

    ProductService(ArlasSubscriptionsConfiguration configuration) {
        this.apiClient = new ApiClient().setBasePath(configuration.arlasServerBasePath);
        this.searchEndpoint = configuration.arlasServerSearchEndpoint;
        this.filterRoot = configuration.arlasServerFilterRoot;
        this.notificationOrderKafkaProducer = NotificationOrderKafkaProducer.build(configuration);
    }

    void processMatchingProducts(SubscriptionEvent event, List<Hit> subscriptions) {
        String searchFilter = filterRoot.replaceAll("\\{md.id}", event.md.id);

        for (Hit hit : subscriptions) {
            LOGGER.debug("processing hit:" + hit.toString());
            try {
                UserSubscription userSubscription = objectMapper.convertValue(hit.getData(), UserSubscription.class);
                LOGGER.debug("userSubscription:" + userSubscription.toString());
                String f = searchFilter
                        + "&" + userSubscription.subscription.hits.filter
                        + "&" + userSubscription.subscription.hits.projection;
                Hits productHits = getItemHits(getQueryParams(f));
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

    private void pushNotificationOrder(Hit hit, SubscriptionEvent event, UserSubscription userSubscription) {
        NotificationOrder notificationOrder = new NotificationOrder();
        notificationOrder.md = event.md;
        notificationOrder.data = hit.getData();
        notificationOrder.collection = event.collection;
        notificationOrder.operation = event.operation;
        notificationOrder.subscription.id = userSubscription.getId();
        notificationOrder.subscription.callback = userSubscription.subscription.callback;
        notificationOrder.userMetadatas = userSubscription.userMetadatas;
        LOGGER.debug("notificationOrder:" + notificationOrder.toString());
        notificationOrderKafkaProducer.send(notificationOrder);
    }


}