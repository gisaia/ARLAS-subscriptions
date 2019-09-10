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

package io.arlas.subscriptions.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserSubscriptionWithLinks extends UserSubscription {

    @JsonProperty("_links")
    public Map<String, Link> links;

    public UserSubscriptionWithLinks(UserSubscription u) {
        setId(u.getId());
        setCreated_at(u.getCreated_at());
        setCreated_by_admin(u.getCreated_by_admin());
        setDeleted(u.getDeleted());
        setModified_at(u.getModified_at());
        this.created_by = u.created_by;
        this.active = u.active;
        this.starts_at = u.starts_at;
        this.expires_at = u.expires_at;
        this.title = u.title;
        this.subscription = u.subscription;
        this.userMetadatas = u.userMetadatas;
    }

    public UserSubscriptionWithLinks withLinks(Map<String, Link> links) {
        this.links = links;
        return this;
    }

    static public class Link {
        public Link(String relation, String href, String method) {
            this(relation, href, "application/json", method);
        }

        public Link(String relation, String href, String type, String method) {
            this.relation = relation;
            this.href = href;
            this.type = type;
            this.method = method;
        }

        @NotNull
        @JsonProperty(required = true)
        public String relation;
        @NotEmpty
        @JsonProperty(required = true)
        public String href;
        @NotNull
        @JsonProperty(required = true)
        public String type;
        @NotNull
        @JsonProperty(required = true)
        public String method;
    }
}
