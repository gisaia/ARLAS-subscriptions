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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Response;
import io.arlas.client.ApiClient;
import io.arlas.client.ApiException;
import io.arlas.client.Pair;
import io.arlas.client.model.CollectionReferenceParameters;
import io.arlas.subscriptions.model.IndexedUserSubscription;
import io.arlas.subscriptions.model.UserSubscription;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.geojson.LngLatAlt;
import org.geojson.Polygon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.*;

public class DataSetTool {
    static Logger LOGGER = LoggerFactory.getLogger(DataSetTool.class);

    public static String COLLECTION_GEODATA_NAME = "geodata";
    public static String COLLECTION_SUBSCRIPTIONS_NAME = "subscriptions";

    public final static String DATASET_INDEX_NAME = "dataset";
    public final static String DATASET_TYPE_NAME = "mytype";
    public final static String DATASET_ID_PATH = "id";
    public final static String DATASET_GEOMETRY_PATH = "geo_params.geometry";
    public final static String DATASET_CENTROID_PATH = "geo_params.centroid";
    public final static String DATASET_TIMESTAMP_PATH = "params.startdate";
    public static final String[] jobs = {"Actor", "Announcers", "Archeologists", "Architect", "Brain Scientist", "Chemist", "Coach", "Coder", "Cost Estimator", "Dancer", "Drafter"};
    public static final String[] cities = {"Paris", "London", "New York", "Tokyo", "Toulouse", "Marseille", "Lyon", "Bordeaux", "Lille", "Albi", "Calais"};
    public static final String[] countries = {"Afghanistan",
            "Albania",
            "Algeria",
            "Andorra",
            "Angola",
            "Antigua",
            "Barbuda",
            "Argentina",
            "Armenia",
            "Aruba",
            "Australia",
            "Austria"
    };

    public final static String SUBSCRIPTIONS_INDEX_NAME = "subs";
    public final static String SUBSCRIPTIONS_TYPE_NAME = "sub_type";
    public final static String SUBSCRIPTIONS_ID_PATH = "id";
    public final static String SUBSCRIPTIONS_GEOMETRY_PATH = "geometry";
    public final static String SUBSCRIPTIONS_CENTROID_PATH = "centroid";
    public final static String SUBSCRIPTIONS_TIMESTAMP_PATH = "created_at";

    public static ApiClient apiClient;
    public static AdminClient adminClient;
    public static Client client;

    public static ApiClient getApiClient() {
        return apiClient;
    }

    static {
        try {
            // ARLAS Client
            String arlasHost = Optional.ofNullable(System.getenv("ARLAS_HOST")).orElse("localhost");
            int arlasPort = Integer.valueOf(Optional.ofNullable(System.getenv("ARLAS_PORT")).orElse("9999"));
            String arlasPrefix = Optional.ofNullable(System.getenv("ARLAS_PREFIX")).orElse("/arlas");
            apiClient = new ApiClient().setBasePath("http://"+arlasHost+":"+arlasPort+arlasPrefix);

            //ES Client
            Settings settings = null;
            String elasticHost = Optional.ofNullable(System.getenv("ARLAS_ELASTIC_HOST")).orElse("localhost");
            int elasticPort = Integer.valueOf(Optional.ofNullable(System.getenv("ARLAS_ELASTIC_PORT")).orElse("9300"));
            if ("localhost".equals(elasticHost)) {
                settings = Settings.EMPTY;
            } else {
                settings = Settings.builder().put("cluster.name", "elasticsearch").build();
            }
            client = new PreBuiltTransportClient(settings)
                    .addTransportAddress(new TransportAddress(InetAddress.getByName(elasticHost), elasticPort));
            adminClient = client.admin();

            LOGGER.info("Elasticsearch : " + elasticHost + ":" + elasticPort);
            LOGGER.info("ARLAS-server : " + arlasHost + ":" + arlasPort);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public static void main(String[] args) throws IOException {
        DataSetTool.loadDataSet();
        DataSetTool.loadSubscriptions();
    }

    public static void loadDataSet() throws IOException {
        //Create a single index with all data
        createIndex(DATASET_INDEX_NAME, DATASET_TYPE_NAME, "dataset.mapping.json");
        LOGGER.info("Index " + DATASET_INDEX_NAME + " created in Elasticsearch");
        fillIndex(DATASET_INDEX_NAME, -170, 170, -80, 80);
        LOGGER.info("Index " + DATASET_INDEX_NAME + " populated in Elasticsearch");


        //Create collection in ARLAS-server
        CollectionReferenceParameters collection = new CollectionReferenceParameters();
        collection.setIndexName(DataSetTool.DATASET_INDEX_NAME);
        collection.setTypeName(DataSetTool.DATASET_TYPE_NAME);
        collection.setIdPath(DataSetTool.DATASET_ID_PATH);
        collection.setGeometryPath(DataSetTool.DATASET_GEOMETRY_PATH);
        collection.setCentroidPath(DataSetTool.DATASET_CENTROID_PATH);
        collection.setTimestampPath(DataSetTool.DATASET_TIMESTAMP_PATH);
        Call collectionPut = null;
        try {
            collectionPut = apiClient.buildCall("/collections/"+ COLLECTION_GEODATA_NAME, "PUT", new ArrayList<Pair>(),
                    new ArrayList<>(), collection, new HashMap<>(), new HashMap<>(), new String[0], null);
            Response collectionPutResponse = collectionPut.execute();
            if(collectionPutResponse.code() == 200) {
                LOGGER.debug("Collection " + COLLECTION_GEODATA_NAME + " created in ARLAS-server : " + collectionPutResponse.message());
            } else {
                LOGGER.debug("Collection " + COLLECTION_GEODATA_NAME + " NOT created to ARLAS-server [" + collectionPutResponse.code() + "] : " +
                        collectionPutResponse.message());
            }
        } catch (ApiException e) {
            LOGGER.error("Unable to create collection in ARLAS-server",e);
        }
    }

    public static void loadSubscriptions() throws IOException {
        //Create subscription index with one existing subscription
        createIndex(SUBSCRIPTIONS_INDEX_NAME, SUBSCRIPTIONS_TYPE_NAME, "arlas.subtest.mapping.json");
        LOGGER.info("Index " + SUBSCRIPTIONS_INDEX_NAME + " created in Elasticsearch");

        IndexedUserSubscription subscription = new IndexedUserSubscription();
        subscription.active = true;
        subscription.centroid =  "0,0";
        List<LngLatAlt> coords = new ArrayList<>();
        coords.add(new LngLatAlt(-50, 50));
        coords.add(new LngLatAlt(50, 50));
        coords.add(new LngLatAlt(50, -50));
        coords.add(new LngLatAlt(-50, -50));
        coords.add(new LngLatAlt(-50, 50));
        subscription.geometry = new Polygon(coords);
        subscription.created_by = "gisaia";
        subscription.expires_at = 2145913200l;
        subscription.title = "Test Subscription";
        subscription.setCreated_at(1564578988l);
        subscription.setCreated_by_admin(false);
        subscription.setDeleted(false);
        subscription.setId("1234");
        subscription.setModified_at(-1l);
        subscription.subscription = new UserSubscription.Subscription();
        subscription.subscription.callback = "http://myservice.com/mycallback";
        subscription.subscription.trigger = new HashMap<>();
        //TODO change mapping type to geo_shape when use geoJSON instead of WKT for trigger.geometry
        subscription.subscription.trigger.put("geometry", "POLYGON((-50 50,50 50,50 -50,-50 -50,-50 50))" /*new Polygon(coords)*/);
        subscription.subscription.trigger.put("job", Arrays.asList(jobs).subList(0,5));
        subscription.subscription.trigger.put("event", Arrays.asList("UPDATE"));
        subscription.subscription.hits = new UserSubscription.Hits();
        subscription.subscription.hits.filter = "f=params.city:eq:Toulouse";
        subscription.subscription.hits.projection = "exclude=params.country";
        subscription.userMetadatas = new HashMap<>();
        subscription.userMetadatas.put("correlationId","my-correlation-id");

        ObjectMapper mapper = new ObjectMapper();
        client.prepareIndex(SUBSCRIPTIONS_INDEX_NAME, SUBSCRIPTIONS_TYPE_NAME, "SUB_" + subscription.getId())
                .setSource(mapper.writer().writeValueAsString(subscription), XContentType.JSON)
                .get();
        LOGGER.info("Index " + SUBSCRIPTIONS_INDEX_NAME + " populated in Elasticsearch");


        //Create collection in ARLAS-server
        CollectionReferenceParameters collection = new CollectionReferenceParameters();
        collection.setIndexName(DataSetTool.SUBSCRIPTIONS_INDEX_NAME);
        collection.setTypeName(DataSetTool.SUBSCRIPTIONS_TYPE_NAME);
        collection.setIdPath(DataSetTool.SUBSCRIPTIONS_ID_PATH);
        collection.setGeometryPath(DataSetTool.SUBSCRIPTIONS_GEOMETRY_PATH);
        collection.setCentroidPath(DataSetTool.SUBSCRIPTIONS_CENTROID_PATH);
        collection.setTimestampPath(DataSetTool.SUBSCRIPTIONS_TIMESTAMP_PATH);
        Call collectionPut = null;
        try {
            collectionPut = apiClient.buildCall("/collections/"+ COLLECTION_SUBSCRIPTIONS_NAME, "PUT", new ArrayList<>(),
                    new ArrayList<>(), collection, new HashMap<>(), new HashMap<>(), new String[0], null);
            Response collectionPutResponse = collectionPut.execute();
            if(collectionPutResponse.code() == 200) {
                LOGGER.debug("Collection " + COLLECTION_SUBSCRIPTIONS_NAME + " created in ARLAS-server : " + collectionPutResponse.message());
            } else {
                LOGGER.debug("Collection " + COLLECTION_SUBSCRIPTIONS_NAME + " NOT created to ARLAS-server [" + collectionPutResponse.code() + "] : " +
                        collectionPutResponse.message());
            }
        } catch (ApiException e) {
            LOGGER.error("Unable to create collection in ARLAS-server",e);
        }
    }

    private static void createIndex(String indexName, String typeName, String mappingFileName) throws IOException {
        String mapping = getString(DataSetTool.class.getClassLoader().getResourceAsStream(mappingFileName));
        try {
            adminClient.indices().prepareDelete(indexName).get();
        } catch (Exception e) {
        }
        adminClient.indices().prepareCreate(indexName).addMapping(typeName, mapping, XContentType.JSON).get();
    }

    private static void fillIndex(String indexName, int lonMin, int lonMax, int latMin, int latMax) throws JsonProcessingException {
        Data data;
        ObjectMapper mapper = new ObjectMapper();

        for (int i = lonMin; i <= lonMax; i += 10) {
            for (int j = latMin; j <= latMax; j += 10) {
                data = new Data();
                data.id = String.valueOf("ID_" + i + "_" + j + "DI").replace("-", "_");
                data.fullname = "My name is " + data.id;
                data.params.age = Math.abs(i * j);
                data.params.startdate = 1l * (i + 1000) * (j + 1000);
                if (data.params.startdate >= 1013600) {
                    data.params.weight = (i + 10) * (j + 10);
                }
                data.params.stopdate = 1l * (i + 1000) * (j + 1000) + 100;
                data.geo_params.centroid = j + "," + i;
                data.params.job = jobs[((Math.abs(i) + Math.abs(j)) / 10) % (jobs.length - 1)];
                data.params.country = countries[((Math.abs(i) + Math.abs(j)) / 10) % (countries.length - 1)];
                data.params.city = cities[((Math.abs(i) + Math.abs(j)) / 10) % (cities.length - 1)];
                List<LngLatAlt> coords = new ArrayList<>();
                coords.add(new LngLatAlt(i - 1, j + 1));
                coords.add(new LngLatAlt(i + 1, j + 1));
                coords.add(new LngLatAlt(i + 1, j - 1));
                coords.add(new LngLatAlt(i - 1, j - 1));
                coords.add(new LngLatAlt(i - 1, j + 1));
                data.geo_params.geometry = new Polygon(coords);

                client.prepareIndex(indexName, DATASET_TYPE_NAME, "ES_ID_TEST" + data.id)
                        .setSource(mapper.writer().writeValueAsString(data), XContentType.JSON)
                        .get();
            }
        }
    }

    public static void clearDataSet() {
        adminClient.indices().prepareDelete(DATASET_INDEX_NAME).get();
    }

    public static void clearSubscriptions() {
        adminClient.indices().prepareDelete(SUBSCRIPTIONS_INDEX_NAME).get();
    }

    public static void close() {
        client.close();
    }

    public static String getString(InputStream is) throws IOException {

        String text = null;
        try (Scanner scanner = new Scanner(is)) {
            text = scanner.useDelimiter("\\A").next();
        }

        return text;
    }
}
