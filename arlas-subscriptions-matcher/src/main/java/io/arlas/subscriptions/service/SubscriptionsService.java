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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Response;
import io.arlas.client.ApiClient;
import io.arlas.client.ApiException;
import io.arlas.client.Pair;
import io.arlas.client.model.Hit;
import io.arlas.client.model.Hits;
import io.arlas.client.model.Link;
import io.arlas.subscriptions.app.ArlasSubscriptionsConfiguration;
import io.arlas.subscriptions.model.SubscriptionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class SubscriptionsService {
    private final Logger LOGGER = LoggerFactory.getLogger(SubscriptionsService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ApiClient apiClient;
    private final String arlasSearchEndpoint;
    private final String subscriptionFilterRoot;

    // utility attributes to avoid building empty objects for each subscription matcher request
    private final List emptyListParams = new ArrayList<>();
    private final Map emptyMapParams = new HashMap<>();
    private final String[] emptyArrayParams = new String[0];
    private final static  String GET = "GET";

    SubscriptionsService(ArlasSubscriptionsConfiguration configuration) {
        this.apiClient = new ApiClient().setBasePath(configuration.arlasBasePath);
        this.arlasSearchEndpoint = configuration.arlasSearchEndpoint;
        this.subscriptionFilterRoot = configuration.subscriptionFilterRoot;
    }

    void searchMatchingSubscriptions(SubscriptionEvent event) {
        String searchFilter = subscriptionFilterRoot.replaceAll("\\{md.geometry}", event.md.geometry)
                .replaceAll("\\{collection}", event.collection)
                .replaceAll("\\{operation}", event.operation);


        try {
            List<Pair> queryParams = getQueryParams(searchFilter);
            Hits hits;
            Link next = null;
            do {
                hits = getHits(queryParams);
                if (hits.getHits() != null) {
                    List<Object> productList = hits.getHits().stream().map(hit -> getMatchingProduct(hit)).collect(Collectors.toList());
                    next = hits.getLinks().get("next");
                    if (next != null) {
                        queryParams = getQueryParams(next.getHref().split("\\?")[1]);
                    }
                }
            } while (hits.getHits() != null && next != null);
        } catch (ApiException e) {
            LOGGER.warn("Api exception:", e);
        } catch (IOException e) {
            LOGGER.warn("Parsing error:", e);
        }
    }

    private List<Pair> getQueryParams(String encodedSearchFilter) throws UnsupportedEncodingException {
        String searchFilter = URLDecoder.decode(encodedSearchFilter, StandardCharsets.UTF_8.toString());
        LOGGER.info("Calling '" + apiClient.getBasePath() + arlasSearchEndpoint + "' with query params: '" + searchFilter + "'");
        return Arrays.stream(searchFilter.split("&"))
                .map(s -> s.split("="))
                .map(p -> new Pair(p[0], p[1]))
                .collect(Collectors.toList());
    }

    private Hits getHits(List<Pair> queryParams) throws ApiException, IOException {
        Call searchCall = apiClient.buildCall(arlasSearchEndpoint, GET,  queryParams,
                emptyListParams, null, emptyMapParams, emptyMapParams, emptyArrayParams, null);
        Response searchResponse = searchCall.execute();
        String body = searchResponse.body().string();
        LOGGER.info(body);
        return objectMapper.readValue(body, Hits.class);
    }

    private Object getMatchingProduct(Hit hit) {
        // TODO
        return hit;
    }
}
