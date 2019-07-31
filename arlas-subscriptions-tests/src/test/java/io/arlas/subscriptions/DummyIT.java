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
import io.arlas.client.Pair;
import io.arlas.client.model.Hits;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;


public class DummyIT extends AbstractTestWithData {

    final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testArlasCollection() throws Exception {

        //SEARCH REQUEST
        Call searchCall = DataSetTool.getApiClient().buildCall("/explore/"+DataSetTool.COLLECTION_NAME+"/_search", "GET", new ArrayList<Pair>(),
                new ArrayList<>(), null, new HashMap<>(), new HashMap<>(), new String[0], null);
        Response searchResponse = searchCall.execute();
        String body = searchResponse.body().string();
        Hits hits = objectMapper.readValue(body, Hits.class);

        Assert.assertThat("Search response is 200",
                searchResponse.code(),
                Matchers.equalTo(200));
        Assert.assertThat("Search response returns 10 hits",
                hits.getNbhits(),
                Matchers.equalTo(10l));
        Assert.assertThat("Search response has 595 hits",
                hits.getTotalnb(),
                Matchers.equalTo(595l));
    }
}