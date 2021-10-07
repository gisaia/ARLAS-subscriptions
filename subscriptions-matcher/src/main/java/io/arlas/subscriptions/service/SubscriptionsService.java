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

import io.arlas.server.client.ApiClient;
import io.arlas.server.client.ApiException;
import io.arlas.server.client.Pair;
import io.arlas.server.client.model.Hit;
import io.arlas.server.client.model.Hits;
import io.arlas.server.client.model.Link;
import io.arlas.server.core.exceptions.ArlasException;
import io.arlas.subscriptions.app.ArlasSubscriptionsMatcherConfiguration;
import io.arlas.subscriptions.exception.ArlasSubscriptionsException;
import io.arlas.subscriptions.logger.ArlasLogger;
import io.arlas.subscriptions.logger.ArlasLoggerFactory;
import io.arlas.subscriptions.model.SubscriptionEvent;
import io.arlas.subscriptions.utils.JSONValueInjector;
import org.locationtech.jts.io.ParseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static io.arlas.subscriptions.app.ArlasSubscriptionsMatcher.MATCHER;

public class SubscriptionsService extends AbstractArlasService {
    private final ArlasLogger logger = ArlasLoggerFactory.getLogger(SubscriptionsService.class, MATCHER);

    SubscriptionsService(ArlasSubscriptionsMatcherConfiguration configuration) {
        this.apiClient = new ApiClient().setBasePath(configuration.subscriptionsBasePath);
        this.searchEndpoint = configuration.subscriptionsSearchEndpoint;
        this.filterRoot = configuration.subscriptionFilterRoot;
    }

    List<Hit> searchMatchingSubscriptions(SubscriptionEvent event) throws ParseException {

        List<Hit> result = new ArrayList();

        try {
            String searchFilter = JSONValueInjector.inject(filterRoot, event);

            Hits items;
            Link next = null;
            List<Pair> queryParams = getQueryParams(searchFilter);
            do {
                items = getItemHits(queryParams);
                if (items.getHits() != null) {
                    result.addAll(items.getHits());
                    next = items.getLinks() != null ? items.getLinks().get("next") : null;
                    if (next != null) {
                        queryParams = getQueryParams(next.getHref().split("\\?")[1].replace("before=&", ""));
                    }
                }
            } while (items.getHits() != null && next != null);
        } catch (ApiException | ArlasSubscriptionsException | IOException | ArlasException e) {
            logger.warn("Error while fetching matching subscriptions: " + e.getMessage());
        }

        return result;
    }
}
