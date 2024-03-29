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

package io.arlas.subscriptions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Response;
import io.arlas.server.client.model.Hits;
import io.arlas.commons.exceptions.ArlasException;
import io.arlas.subscriptions.exception.ArlasSubscriptionsException;
import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


public class DummyIT extends AbstractTestContext {

    final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeClass
    public static void beforeClass() {
        try {
            DataSetTool.loadDataSet(true);
            DataSetTool.loadSubscriptions(true, true);
        } catch (IOException | ArlasSubscriptionsException | ArlasException e) {
            LOGGER.error("Could not load data in ES", e);
        }
    }

    @AfterClass
    public static void afterClass() {
        DataSetTool.clearDataSet();
        DataSetTool.clearSubscriptions(true);
    }

    @Test
    public void testArlasCollection() throws Exception {

        //GEODATA SEARCH REQUEST
        Call searchCall = DataSetTool.getApiClient().buildCall("/explore/"+DataSetTool.COLLECTION_GEODATA_NAME +"/_search", "GET", new ArrayList<>(),
                new ArrayList<>(), null, new HashMap<>(), new HashMap<>(), new String[0], null);
        Response searchResponse = searchCall.execute();
        Hits hits = objectMapper.readValue(searchResponse.body().string(), Hits.class);

        Assert.assertThat("Search response is 200",
                searchResponse.code(),
                Matchers.equalTo(200));
        Assert.assertThat("Search response returns 10 hits",
                hits.getNbhits(),
                Matchers.equalTo(10L));
        Assert.assertThat("Search response has 595 hits",
                hits.getTotalnb(),
                Matchers.equalTo(595L));

        //SUBSCRIPTIONS SEARCH REQUEST
        Call subscriptionsCall = DataSetTool.getApiClient().buildCall("/explore/"+DataSetTool.COLLECTION_SUBSCRIPTIONS_NAME +"/_search", "GET", new ArrayList<>(),
                new ArrayList<>(), null, new HashMap<>(), new HashMap<>(), new String[0], null);
        Response subscriptionsResponse = subscriptionsCall.execute();
        Hits subscriptionsHits = objectMapper.readValue(subscriptionsResponse.body().string(), Hits.class);

        Assert.assertThat("Search response is 200",
                subscriptionsResponse.code(),
                Matchers.equalTo(200));
        Assert.assertThat("Subscriptions search returns 1 hits",
                subscriptionsHits.getNbhits(),
                Matchers.equalTo(1L));
        Assert.assertThat("Subscriptions search has 1 hits",
                subscriptionsHits.getTotalnb(),
                Matchers.equalTo(1L));
    }
}