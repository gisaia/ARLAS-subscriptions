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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import io.arlas.subscriptions.exception.ArlasSubscriptionsException;
import org.geojson.GeoJsonObject;
import org.geojson.LngLatAlt;
import org.geojson.Polygon;
import org.hibernate.validator.constraints.NotEmpty;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class IndexedUserSubscription extends UserSubscription {
    private static ObjectMapper mapper = new ObjectMapper();
    private static ObjectReader reader = mapper.readerFor(GeoJsonObject.class);


    public static final String GEOMETRY_KEY = "geometry";
    public static final String CENTROID_KEY = "centroid";


    @NotEmpty
    @JsonProperty(required = true)
    public GeoJsonObject geometry;

    @NotEmpty
    @JsonProperty(required = true)
    public String centroid;

    public IndexedUserSubscription() {

    }

    public IndexedUserSubscription(UserSubscription userSubscription) throws ArlasSubscriptionsException {
        this.setId(userSubscription.getId());
        this.setCreated_at(userSubscription.getCreated_at());
        this.setModified_at(userSubscription.getModified_at());
        this.setCreated_by_admin(userSubscription.getCreated_by_admin());
        this.setDeleted(userSubscription.getDeleted());
        this.created_by = userSubscription.created_by;
        this.active = userSubscription.active;
        this.expires_at = userSubscription.expires_at;
        this.title = userSubscription.title;
        this.subscription = userSubscription.subscription;
        this.userMetadatas = userSubscription.userMetadatas;

        Object geometryValue = userSubscription.subscription.trigger.get(GEOMETRY_KEY);
        if (geometryValue != null) {
            //Standard GeoJSON object
            try {
                this.geometry = reader.readValue(mapper.writer().writeValueAsString(geometryValue));
            } catch (Exception e) {
                throw new ArlasSubscriptionsException("Invalid geosjon format in geometry trigger filed.");
            }
        } else {
            List<LngLatAlt> coords = new ArrayList<>();
            coords.add(new LngLatAlt(-180, 90));
            coords.add(new LngLatAlt(180, 90));
            coords.add(new LngLatAlt(180, -90));
            coords.add(new LngLatAlt(-180, -90));
            coords.add(new LngLatAlt(-180, 90));
            Polygon fullWorld = new Polygon(coords);
            this.geometry = fullWorld;
        }

        Object centroidValue = userSubscription.subscription.trigger.get(CENTROID_KEY);
        if (centroidValue != null) {
            try {
                this.centroid = (String) userSubscription.subscription.trigger.get(CENTROID_KEY);
            } catch (Exception e) {
                throw new ArlasSubscriptionsException("Invalid centroid format in centroid trigger filed.");
            }
        } else {
            //TODO calculate from geometry if present
            this.centroid = "0,0";
        }


    }
}






