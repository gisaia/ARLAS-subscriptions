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

public class UserSubscription {
    public String id;
    public String created_by;
    public Boolean created_by_admin;
    public Long created_at;
    public Long modified_at;

    public Boolean active;
    public Long expires_at;
    public Boolean deleted;
    public String title;
    public Subscription subscription = new Subscription();
    public UserMetadata userMetadatas = new UserMetadata();

    static public class Subscription {
        public Object trigger;
        public String callback;
        public Hits hits;
    }

    static public class Hits {
        public String filter;
        public String projection;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setCreated_by(String created_by) {
        this.created_by = created_by;
    }

    public void setCreated_by_admin(Boolean created_by_admin) {
        this.created_by_admin = created_by_admin;
    }

    public void setCreated_at(Long created_at) {
        this.created_at = created_at;
    }

    public void setModified_at(Long modified_at) {
        this.modified_at = modified_at;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public void setExpires_at(Long expires_at) {
        this.expires_at = expires_at;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public void setTitle(String title) {
        this.title = title;
    }

}


