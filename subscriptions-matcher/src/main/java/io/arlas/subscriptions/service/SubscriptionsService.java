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
import io.arlas.client.ApiClient;
import io.arlas.client.ApiException;
import io.arlas.client.Pair;
import io.arlas.client.model.Hit;
import io.arlas.client.model.Hits;
import io.arlas.client.model.Link;
import io.arlas.subscriptions.app.ArlasSubscriptionsConfiguration;
import io.arlas.subscriptions.exception.ArlasSubscriptionsException;
import io.arlas.subscriptions.model.SubscriptionEvent;
import io.arlas.subscriptions.utils.JSONValueInjector;
import org.locationtech.jts.io.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SubscriptionsService extends AbstractArlasService {
    private final Logger LOGGER = LoggerFactory.getLogger(SubscriptionsService.class);

    SubscriptionsService(ArlasSubscriptionsConfiguration configuration) {
        this.apiClient = new ApiClient().setBasePath(configuration.subscriptionsBasePath);
        this.searchEndpoint = configuration.subscriptionsSearchEndpoint;
        this.filterRoot = configuration.subscriptionFilterRoot;
    }

    List<Hit> searchMatchingSubscriptions(SubscriptionEvent event) throws JsonProcessingException, ParseException {

        List<Hit> result = new ArrayList();

        String searchFilter = JSONValueInjector.inject(filterRoot, event);

        try {
            Hits items;
            Link next = null;
            List<Pair> queryParams = getQueryParams(searchFilter);
            do {
                items = getItemHits(queryParams);
                if (items.getHits() != null) {
                    result.addAll(items.getHits());
                    next = items.getLinks() != null ? items.getLinks().get("next") : null;
                    if (next != null) {
                        queryParams = getQueryParams(next.getHref().split("\\?")[1]);
                    }
                }
            } while (items.getHits() != null && next != null);
        } catch (ApiException|IOException| ArlasSubscriptionsException e) {
            LOGGER.warn("Error while fetching matching subscriptions", e);
        }

        return result;
    }
}
