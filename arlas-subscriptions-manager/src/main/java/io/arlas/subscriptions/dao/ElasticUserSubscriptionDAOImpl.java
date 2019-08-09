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

package io.arlas.subscriptions.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import io.arlas.subscriptions.app.ArlasSubscriptionManagerConfiguration;
import io.arlas.subscriptions.db.elastic.ElasticDBManaged;
import io.arlas.subscriptions.exception.ArlasSubscriptionsException;
import io.arlas.subscriptions.exception.InternalServerErrorException;
import io.arlas.subscriptions.model.IndexedUserSubscription;
import io.arlas.subscriptions.model.UserSubscription;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.rest.RestStatus;
import org.geojson.GeoJsonObject;
import org.locationtech.jts.io.ParseException;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import org.apache.logging.log4j.core.util.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.UUID;

public class ElasticUserSubscriptionDAOImpl implements UserSubscriptionDAO  {

    private Client client = null;
    private String arlasSubscriptionIndex = null;
    private ArlasSubscriptionManagerConfiguration configuration = null;

    private static final String ARLAS_SUB_MAPPING_FILE_NAME = "arlas.sub.mapping.json";
    private static final String ARLAS_SUB_INDEX_MAPPING_NAME = "subscription";
    private static ObjectMapper mapper = new ObjectMapper();

    public ElasticUserSubscriptionDAOImpl(ArlasSubscriptionManagerConfiguration configuration, ElasticDBManaged elasticDBManaged) throws ArlasSubscriptionsException {
            this.client=elasticDBManaged.esClient;
            this.arlasSubscriptionIndex = configuration.elasticDBConnection.elasticsubindex;
            this.configuration=configuration;
            this.initSubscriptionIndex();
    }


    @Override
    public List<UserSubscription> getAllUserSubscriptions() throws ArlasSubscriptionsException {
        return null;
    }

    @Override
    public UserSubscription postUserSubscription(UserSubscription userSubscription) throws ArlasSubscriptionsException, IOException, ParseException {
        IndexedUserSubscription indexedUserSubscription = new IndexedUserSubscription(userSubscription,this.configuration.triggerGeometryKey,this.configuration.triggerCentroidKey);
        IndexResponse response = null;
        try {
            response = client.prepareIndex(arlasSubscriptionIndex, ARLAS_SUB_INDEX_MAPPING_NAME)
                    .setSource(mapper.writeValueAsString(indexedUserSubscription), XContentType.JSON).get();
        } catch (JsonProcessingException e) {
            new InternalServerErrorException("Can not put userSubscription.", e);
        }
        if (response.status().getStatus() != RestStatus.OK.getStatus()
                && response.status().getStatus() != RestStatus.CREATED.getStatus()) {
            throw new InternalServerErrorException("Unable to index collection : " + response.status().toString());
        }
        return userSubscription;

    }

    public void initSubscriptionIndex() {
        try {
            client.admin().indices().prepareGetIndex().setIndices(arlasSubscriptionIndex).get();
            this.putUserSubscriptionExtendedMapping(client, arlasSubscriptionIndex, ARLAS_SUB_INDEX_MAPPING_NAME, this.getClass()
                    .getClassLoader()
                    . getResourceAsStream(ARLAS_SUB_MAPPING_FILE_NAME));
        } catch (IndexNotFoundException e) {
            this.createUserSubscriptionIndex(client, arlasSubscriptionIndex, ARLAS_SUB_INDEX_MAPPING_NAME, ARLAS_SUB_MAPPING_FILE_NAME);
        }
    }

    private static CreateIndexResponse createUserSubscriptionIndex(Client client, String arlasIndexName, String arlasMappingName, String arlasMappingFileName)  {
        CreateIndexResponse createIndexResponse = null;
        try {
            String arlasMapping = IOUtils.toString(new InputStreamReader(ElasticUserSubscriptionDAOImpl.class.getClassLoader().getResourceAsStream(arlasMappingFileName)));
            createIndexResponse = client.admin().indices().prepareCreate(arlasIndexName).addMapping(arlasMappingName, arlasMapping, XContentType.JSON).get();
        } catch (IOException e) {
            new InternalServerErrorException("Can not initialize elasticsearch index for subscriptions.", e);
        }
        return createIndexResponse;
    }

    private static AcknowledgedResponse putUserSubscriptionExtendedMapping(Client client, String arlasIndexName, String arlasMappingName, InputStream in) {
        AcknowledgedResponse putMappingResponse = null;
        try {
            String arlasMapping = IOUtils.toString(new InputStreamReader(in));
            putMappingResponse = client.admin().indices().preparePutMapping(arlasIndexName).setType(arlasMappingName).setSource(arlasMapping, XContentType.JSON).get();
        } catch (IOException e) {
            new InternalServerErrorException("Cannot update " + arlasIndexName + " mapping");
        }
        return putMappingResponse;
    }
}
