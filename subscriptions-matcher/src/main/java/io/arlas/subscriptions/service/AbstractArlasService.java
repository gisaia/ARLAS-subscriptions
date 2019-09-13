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
import io.arlas.client.model.Hits;
import io.arlas.subscriptions.exception.ArlasSubscriptionsException;
import io.arlas.subscriptions.logger.ArlasLogger;
import io.arlas.subscriptions.logger.ArlasLoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static io.arlas.subscriptions.app.ArlasSubscriptionsMatcher.MATCHER;

public class AbstractArlasService {
    private final ArlasLogger logger = ArlasLoggerFactory.getLogger(AbstractArlasService.class, MATCHER);
    final ObjectMapper objectMapper = new ObjectMapper();
    ApiClient apiClient;
    String searchEndpoint;
    String filterRoot;

    // utility attributes to avoid building empty objects for each subscription matcher request
    final List emptyListParams = new ArrayList<>();
    final Map emptyMapParams = new HashMap<>();
    final String[] emptyArrayParams = new String[0];
    final static  String GET = "GET";

    List<Pair> getQueryParams(String encodedSearchFilter) throws UnsupportedEncodingException {
        String searchFilter = URLDecoder.decode(encodedSearchFilter, StandardCharsets.UTF_8.toString());
        logger.debug("Calling '" + apiClient.getBasePath() + searchEndpoint + "' with query params: '" + searchFilter + "'");
        return Arrays.stream(searchFilter.split("&"))
                .map(s -> s.split("="))
                .map(p -> new Pair(p[0], p[1]))
                .collect(Collectors.toList());
    }

    Hits getItemHits(List<Pair> queryParams) throws ApiException, IOException, ArlasSubscriptionsException {
        return getItemHits(queryParams, emptyMapParams);
    }

    Hits getItemHits(List<Pair> queryParams, Map<String, String> headerParams) throws ApiException, IOException, ArlasSubscriptionsException {
        Call searchCall = apiClient.buildCall(searchEndpoint, GET, queryParams,
                emptyListParams, null, headerParams, emptyMapParams, emptyArrayParams, null);
        Response searchResponse = searchCall.execute();
        String body = searchResponse.body().string();
        logger.debug("body="+body);
        if (searchResponse.isSuccessful()) {
            return objectMapper.readValue(body, Hits.class);
        } else {
            if (searchResponse.code() == 404) {
                logger.fatal("Arlas collection for subscription not found: " + searchEndpoint);
                System.exit(1);
            }
            throw new ArlasSubscriptionsException("Error while interrogating Catalog: " + body);
        }
    }
}
