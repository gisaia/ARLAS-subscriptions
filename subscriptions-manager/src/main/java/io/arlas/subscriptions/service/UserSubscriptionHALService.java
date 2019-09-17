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

import io.arlas.subscriptions.model.SubscriptionListResource;
import io.arlas.subscriptions.model.UserSubscription;
import io.arlas.subscriptions.model.UserSubscriptionWithLinks;
import org.apache.commons.lang3.tuple.Pair;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UserSubscriptionHALService {

    public UserSubscriptionWithLinks subscriptionWithLinks(UserSubscription subscription, UriInfo uriInfo) {
        String listUri = uriInfo.getRequestUriBuilder().build().toString();
        String subUri = uriInfo.getRequestUriBuilder()
                .path(subscription.getId())
                .replaceQueryParam("page", null)
                .replaceQueryParam("size", null)
                .build().toString();
        Map<String, UserSubscriptionWithLinks.Link> links = new HashMap<>();
        links.put("self", new UserSubscriptionWithLinks.Link("self", subUri, "GET"));
        links.put("list", new UserSubscriptionWithLinks.Link("list", listUri, "GET"));
        links.put("update", new UserSubscriptionWithLinks.Link("update", subUri, "PUT"));
        links.put("delete", new UserSubscriptionWithLinks.Link("delete", subUri, "DELETE"));
        return new UserSubscriptionWithLinks(subscription).withLinks(links);
    }

    public SubscriptionListResource subscriptionListToResource(Pair<Integer, List<UserSubscription>> subscriptionList, UriInfo uriInfo, Integer page, Integer size) {
        SubscriptionListResource subscriptionListResource = new SubscriptionListResource();
        subscriptionListResource.total = subscriptionList.getLeft();
        subscriptionListResource.count = subscriptionList.getRight().size();
        subscriptionListResource.subscriptions = subscriptionList.getRight().stream().map(u -> new UserSubscriptionWithLinks(u)).collect(Collectors.toList());
        subscriptionListResource.subscriptions.replaceAll(u -> subscriptionWithLinks(u, uriInfo));
        subscriptionListResource.links = subListLinks(uriInfo, page, size, subscriptionListResource.total, subscriptionListResource.subscriptions.size());
        return subscriptionListResource;
    }

    private Map<String, UserSubscriptionWithLinks.Link> subListLinks(UriInfo uriInfo, Integer page, Integer size, Integer total, Integer count) {
        UriBuilder uri = uriInfo.getRequestUriBuilder();
        Map<String, UserSubscriptionWithLinks.Link> links = new HashMap<>();
        links.put("self", new UserSubscriptionWithLinks.Link("self", getUri(uri, size, page), "GET"));
        if (page != 1)
            links.put("first", new UserSubscriptionWithLinks.Link("first", getUri(uri, size, 1), "GET"));
        if (page > 1)
            links.put("prev", new UserSubscriptionWithLinks.Link("prev", getUri(uri, size, page-1), "GET"));
        if ((page-1)*size + count < total)
            links.put("next", new UserSubscriptionWithLinks.Link("next", getUri(uri, size, page+1), "GET"));
        if ((page-1)*size + count != total)
            links.put("last", new UserSubscriptionWithLinks.Link("last", getUri(uri, size, new Double(Math.ceil((double)total/(double)size)).intValue()), "GET"));
        return links;
    }

    private String getUri(UriBuilder uri, Integer size, Integer page) {
        return uri.replaceQueryParam("size", size).replaceQueryParam("page", page).build().toString();
    }

}
