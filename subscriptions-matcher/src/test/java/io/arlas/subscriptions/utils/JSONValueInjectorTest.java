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

package io.arlas.subscriptions.utils;

import io.arlas.subscriptions.exception.ArlasSubscriptionsException;
import io.arlas.subscriptions.model.SubscriptionEvent;
import org.geojson.LngLatAlt;
import org.geojson.Polygon;
import org.junit.Test;
import org.locationtech.jts.io.ParseException;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.arlas.subscriptions.utils.JSONValueInjector.VAR_START;
import static io.arlas.subscriptions.utils.JSONValueInjector.VAR_END;

public class JSONValueInjectorTest {

    private final static String filter = "f=subscription.trigger.geometry:intersects:"+VAR_START+"object.geometry"+VAR_END
            +"&f=subscription.trigger.event:eq:"+VAR_START+"event"+VAR_END
            +"&f=subscription.trigger.job:eq:"+VAR_START+"object.job"+VAR_END
            +"&f=active:eq:true&f=deleted:eq:false&f=expires_at:gt:now&f=starts_at:lte:now&sort=id";

    @Test
    public void injectTest() {
        try {
            List<LngLatAlt> coords = new ArrayList<>();
            coords.add(new LngLatAlt(-11, -29));
            coords.add(new LngLatAlt(-9, -29));
            coords.add(new LngLatAlt(-9, -31));
            coords.add(new LngLatAlt(-11, -31));
            coords.add(new LngLatAlt(-11, -29));
            SubscriptionEvent event = new SubscriptionEvent();
            Map<String,Object> object = new HashMap<>();
            object.put("job", "Brain Scientist");
            object.put("id","ID__10__30DI");
            object.put("geometry", new Polygon(coords));
            event.put("object", object);
            event.put("event","UPDATE");

            String expected = "f=subscription.trigger.geometry:intersects:POLYGON ((-11 -29, -9 -29, -9 -31, -11 -31, -11 -29))"
                    +"&f=subscription.trigger.event:eq:UPDATE"
                    +"&f=subscription.trigger.job:eq:Brain Scientist"
                    +"&f=active:eq:true&f=deleted:eq:false&f=expires_at:gt:now&f=starts_at:lte:now&sort=id";

            assertThat(JSONValueInjector.inject(filter, event), equalTo(expected));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (ArlasSubscriptionsException e) {
            e.printStackTrace();
        }
    }
}
