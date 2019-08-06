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

package io.arlas.subscriptions.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.arlas.subscriptions.AbstractTestWithData;
import io.arlas.subscriptions.DataSetTool;
import io.arlas.subscriptions.model.NotificationOrder;
import io.arlas.subscriptions.model.SubscriptionEvent;
import io.arlas.subscriptions.model.SubscriptionEventMetadata;
import io.arlas.subscriptions.tools.KafkaTool;
import org.geojson.LngLatAlt;
import org.geojson.Polygon;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class SubscriptionsMatcherIT extends AbstractTestWithData {
    static Logger LOGGER = LoggerFactory.getLogger(SubscriptionsMatcherIT.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final KafkaTool kafkaTool = new KafkaTool();

    @BeforeClass
    public static void beforeClass() {
        kafkaTool.init();
        new Thread(kafkaTool).start();
        try {
            DataSetTool.loadDataSet();
            DataSetTool.loadSubscriptions();
        } catch (IOException e) {
            LOGGER.error("Could not load data in ES", e);
        }
    }

    @AfterClass
    public static void afterClass() {
        kafkaTool.stop();
    }

    @Test
    public void testMatchingSubscriptionEventWithExistingProductIndex() throws Exception {
        List<LngLatAlt> coords = new ArrayList<>();
        coords.add(new LngLatAlt(-11, -29));
        coords.add(new LngLatAlt(-9, -29));
        coords.add(new LngLatAlt(-9, -31));
        coords.add(new LngLatAlt(-11, -31));
        coords.add(new LngLatAlt(-11, -29));
        SubscriptionEventMetadata md = new SubscriptionEventMetadata();
        md.geometry = new Polygon(coords);
        md.id = "ID__10__30DI";
        SubscriptionEvent event = new SubscriptionEvent();
        event.md = md;
        event.collection = "Brain Scientist";
        event.operation = "UPDATE";

        kafkaTool.produce(event);
        Thread.sleep(10000);

        // check that all the messages were received
        NotificationOrder received = kafkaTool.consume(5, TimeUnit.SECONDS);
        int nbNotifReceived = 0;
        while(received != null) {
            nbNotifReceived++;
            checkNotificationOrder(received);
            received = kafkaTool.consume(10, TimeUnit.SECONDS);
        }

        assertThat(nbNotifReceived,is(1));
    }

    public void checkNotificationOrder(NotificationOrder notifOrder) {
        assertThat(notifOrder.md.id, is("ID__10__30DI"));
        assertThat(notifOrder.operation, is("UPDATE"));
        assertNotNull(notifOrder.collection, is("Brain Scientist"));
    }
}
