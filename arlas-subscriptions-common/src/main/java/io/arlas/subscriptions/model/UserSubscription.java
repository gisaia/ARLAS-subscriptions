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

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.Objects;

public class UserSubscription {
    private String id;
    private Long created_at;
    private Long modified_at;
    private Boolean created_by_admin;
    private Boolean deleted;

    @NotEmpty
    @JsonProperty(required = true)
    public String created_by;
    @NotNull
    @JsonProperty(required = true)
    public Boolean active;
    @NotNull
    @JsonProperty(required = true)
    public Long expires_at;
    @NotEmpty
    @JsonProperty(required = true)
    public String title;
    @NotNull
    @JsonProperty(required = true)
    public Subscription subscription = new Subscription();
    @NotNull
    @JsonProperty(required = true)
    public Map<String, Object>  userMetadatas;

    static public class Subscription {
        @NotNull
        @JsonProperty(required = true)
        public Map<String, Object> trigger;
        @NotEmpty
        @JsonProperty(required = true)
        public String callback;
        @NotNull
        @JsonProperty(required = true)
        public Hits hits;
    }

    static public class Hits {
        @NotEmpty
        @JsonProperty(required = true)
        public String filter;
        @NotEmpty
        @JsonProperty(required = true)
        public String projection;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public String getId() {
        return id;
    }
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public Long getCreated_at() {
        return created_at;
    }
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public Long getModified_at() {
        return modified_at;
    }
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public Boolean getCreated_by_admin() {
        return created_by_admin;
    }
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public Boolean getDeleted() { return deleted;}

    public void setId(String id) {
        this.id = id;
    }

    public void setCreated_at(Long created_at) {
        this.created_at = created_at;
    }

    public void setModified_at(Long modified_at) {
        this.modified_at = modified_at;
    }

    public void setCreated_by_admin(Boolean created_by_admin) {
        this.created_by_admin = created_by_admin;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }
}


