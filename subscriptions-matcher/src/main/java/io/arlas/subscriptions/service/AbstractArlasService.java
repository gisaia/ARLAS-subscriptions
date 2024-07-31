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
import io.arlas.client.ApiClient;
import io.arlas.client.ApiException;
import io.arlas.client.Pair;
import io.arlas.client.api.ExploreApi;
import io.arlas.client.model.Hits;
import io.arlas.commons.exceptions.ArlasException;
import io.arlas.server.core.utils.ParamsParser;
import io.arlas.subscriptions.exception.ArlasSubscriptionsException;
import io.arlas.subscriptions.logger.ArlasLogger;
import io.arlas.subscriptions.logger.ArlasLoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.arlas.subscriptions.app.ArlasSubscriptionsMatcher.MATCHER;

public class AbstractArlasService {
    private final ArlasLogger logger = ArlasLoggerFactory.getLogger(AbstractArlasService.class, MATCHER);
    final ObjectMapper objectMapper = new ObjectMapper();
    String basePath;
    String collection;
    String filterRoot;

    List<Pair> getQueryParams(String encodedSearchFilter) throws UnsupportedEncodingException {
        String searchFilter = URLDecoder.decode(encodedSearchFilter, StandardCharsets.UTF_8);
        logger.debug("Calling '" + basePath + collection + "' with query params: '" + searchFilter + "'");
        return Arrays.stream(searchFilter.split("&"))
                .map(s -> s.split("="))
                .map(p -> new Pair(p[0], p.length == 1 ? "" : p[1]))
                .collect(Collectors.toList());
    }

    Hits getItemHits(List<Pair> queryParams) throws ApiException, IOException, ArlasSubscriptionsException, ArlasException {
        return getItemHits(queryParams, Collections.emptyMap());
    }

    Hits getItemHits(List<Pair> queryParams, Map<String, String> headerParams)
            throws ApiException, IOException, ArlasException, ArlasSubscriptionsException {

        validateArlasQueryParams(queryParams);
        ApiClient apiClient = new ApiClient().setBasePath(basePath);
        if (headerParams != null) {
            headerParams.forEach(apiClient::addDefaultHeader);
        }
        try {
            return new ExploreApi(apiClient)
                    .search(collection,
                            getQPList(queryParams, "f"),
                            getQPList(queryParams, "q"),
                            getQP(queryParams, "dateformat").orElse(null),
                            getQP(queryParams, "righthand").map(Boolean::parseBoolean).orElse(null),
                            getQP(queryParams, "pretty").map(Boolean::parseBoolean).orElse(null),
                            getQP(queryParams, "flat").map(Boolean::parseBoolean).orElse(null),
                            getQP(queryParams, "include").orElse(null),
                            getQP(queryParams, "exclude").orElse(null),
                            getQP(queryParams, "returned_geometries").orElse(null),
                            getQP(queryParams, "size").map(Long::parseLong).orElse(null),
                            getQP(queryParams, "from").map(Long::parseLong).orElse(null),
                            getQP(queryParams, "sort").orElse(null),
                            getQP(queryParams, "after").orElse(null),
                            getQP(queryParams, "before").orElse(null),
                            getQP(queryParams, "max-age-cache").map(Integer::parseInt).orElse(null));
        } catch (ApiException e) {
            if (e.getCode() == 404) {
                logger.fatal("Arlas collection for subscription not found: " + collection);
                System.exit(1);
            }
            throw new ArlasSubscriptionsException("Error while interrogating Catalog: " + e.getResponseBody());
        }
    }

    Optional<String> getQP(List<Pair> queryParams, String name) {
        return queryParams.stream().filter(p -> p.getName().equals(name)).map(Pair::getValue).findFirst();
    }
    List<String> getQPList(List<Pair> queryParams, String name) {
        return queryParams.stream().filter(p -> p.getName().equals(name)).map(Pair::getValue).toList();
    }
    private void validateArlasQueryParams(List<Pair> queryParams) throws ArlasException {

        Function<String, List<String>> getListOfStringFromQueryParams = (String key) -> queryParams.stream()
                .filter(p -> p.getName().equals(key))
                .map(Pair::getValue)
                .collect(Collectors.toList());

        Function<String, String> getStringFromQueryParams = (String key) -> queryParams.stream()
                .filter(p -> p.getName().equals(key))
                .map(Pair::getValue)
                .findFirst()
                .orElse(null);

        ParamsParser.getFilter(null,
                getListOfStringFromQueryParams.apply("f"),
                getListOfStringFromQueryParams.apply("q"),
                getStringFromQueryParams.apply("dateformat"),
                null);
    }

}
