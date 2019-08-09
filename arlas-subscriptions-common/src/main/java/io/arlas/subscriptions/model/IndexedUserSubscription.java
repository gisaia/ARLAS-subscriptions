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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.google.common.primitives.Doubles;
import io.arlas.subscriptions.exception.ArlasSubscriptionsException;
import org.geojson.GeoJsonObject;
import org.geojson.LngLatAlt;
import org.geojson.Point;

import org.geojson.Polygon;
import org.hibernate.validator.constraints.NotEmpty;
import org.locationtech.jts.algorithm.Centroid;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.geojson.GeoJsonReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class IndexedUserSubscription extends UserSubscription {
    private static ObjectMapper mapper = new ObjectMapper();
    private static ObjectReader reader = mapper.readerFor(GeoJsonObject.class);
    private static ObjectReader pointReader = mapper.readerFor(Point.class);
    private final GeoJsonReader geoJsonReader = new GeoJsonReader();

    @NotEmpty
    @JsonProperty(required = true)
    public GeoJsonObject geometry;

    @NotEmpty
    @JsonProperty(required = true)
    public List<Double> centroid;

    public IndexedUserSubscription() {

    }

    public IndexedUserSubscription(UserSubscription userSubscription,String geometryKey, String centroidKey) throws ArlasSubscriptionsException, ParseException, JsonProcessingException {
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

        Object geometryValue = userSubscription.subscription.trigger.get(geometryKey);
        if (geometryValue != null) {
            //Standard GeoJSON object
            try {
                this.geometry = reader.readValue(mapper.writer().writeValueAsString(geometryValue));
            } catch (Exception e) {
                throw new ArlasSubscriptionsException("Invalid geosjon format in geometry trigger filed.",e);
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

        Object centroidValue = userSubscription.subscription.trigger.get(centroidKey);
        if (centroidValue != null) {
            try {
                this.centroid = (pointReader.readValue(mapper.writer().writeValueAsString(centroidValue)));
            } catch (Exception e) {
                throw new ArlasSubscriptionsException("Invalid geojson point format in centroid trigger filed.",e);
            }
        } else {
            // Calculate centroid from geometry properties
            Geometry geom = geoJsonReader.read(mapper.writeValueAsString(this.geometry));
            Centroid centFromGeom = new Centroid(geom);
            this.centroid = Doubles.asList(centFromGeom.getCentroid().x ,centFromGeom.getCentroid().y);
        }
    }
}






