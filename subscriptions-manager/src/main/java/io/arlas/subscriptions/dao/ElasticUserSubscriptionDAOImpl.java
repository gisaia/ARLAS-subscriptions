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
import io.arlas.subscriptions.app.ArlasSubscriptionManagerConfiguration;
import io.arlas.subscriptions.app.TriggerConfiguration;
import io.arlas.subscriptions.db.elastic.ElasticDBManaged;
import io.arlas.subscriptions.exception.ArlasSubscriptionsException;
import io.arlas.subscriptions.model.IndexedUserSubscription;
import io.arlas.subscriptions.model.UserSubscription;
import io.arlas.subscriptions.model.elastic.ElasticDBConnection;
import io.arlas.subscriptions.utils.JsonSchemaValidator;
import org.apache.commons.lang3.tuple.Pair;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.exists.types.TypesExistsResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.script.Script;
import org.everit.json.schema.ValidationException;

import java.util.List;
import java.util.Optional;

public class ElasticUserSubscriptionDAOImpl implements UserSubscriptionDAO  {

    private Client client;
    private String arlasSubscriptionIndex;
    private String arlasSubscriptionType;
    private TriggerConfiguration triggerConfiguration;
    private JsonSchemaValidator jsonSchemaValidator;

    private static ObjectMapper mapper = new ObjectMapper();

    public ElasticUserSubscriptionDAOImpl(ElasticDBConnection elasticDBConnection, TriggerConfiguration triggerConfiguration, ElasticDBManaged elasticDBManaged,
                                          JsonSchemaValidator jsonSchemaValidator) throws ArlasSubscriptionsException {
            this.client=elasticDBManaged.esClient;
            this.arlasSubscriptionIndex = elasticDBConnection.elasticsubindex;
            this.arlasSubscriptionType = elasticDBConnection.elasticsubtype;
            this.triggerConfiguration=triggerConfiguration;
            this.initSubscriptionIndex();
            this.jsonSchemaValidator=jsonSchemaValidator;
    }

    @Override
    public Pair<Integer, List<UserSubscription>> getAllUserSubscriptions(String user, Long before, Boolean active, Boolean expired, boolean deleted, Integer page, Integer size) throws ArlasSubscriptionsException {
        return null;
    }

    @Override
    public UserSubscription postUserSubscription(UserSubscription userSubscription, boolean createdByAdmin) throws ArlasSubscriptionsException {
        IndexResponse response = null;
        try {
            this.jsonSchemaValidator.validJsonObjet(userSubscription.subscription.trigger);
            IndexedUserSubscription indexedUserSubscription = new IndexedUserSubscription(userSubscription,this.triggerConfiguration.triggerGeometryKey,this.triggerConfiguration.triggerCentroidKey);
            response = client.prepareIndex(arlasSubscriptionIndex, arlasSubscriptionType, userSubscription.getId())
                    .setSource(mapper.writeValueAsString(indexedUserSubscription), XContentType.JSON).get();
        } catch (ValidationException e) {
            throw new ArlasSubscriptionsException("Error in validation of trigger json schema: " + e.getErrorMessage());
        } catch (JsonProcessingException e) {
            throw new ArlasSubscriptionsException("Error in writing subscription json: " + e.getMessage());
        } catch (ElasticsearchException e) {
            throw new ArlasSubscriptionsException("Elasticsearch not available: " + e.getMessage());
        }
        if (response.status().getStatus() != RestStatus.OK.getStatus()
                && response.status().getStatus() != RestStatus.CREATED.getStatus()) {
            throw new ArlasSubscriptionsException("Unable to index subscription. Response: " + response.toString());
        }
        return userSubscription;
    }

    @Override
    public void putUserSubscription(UserSubscription updUserSubscription) throws ArlasSubscriptionsException {
        postUserSubscription(updUserSubscription, updUserSubscription.getCreated_by_admin());
    }

    @Override
    public void deleteUserSubscription(String ref) throws ArlasSubscriptionsException {
    }

    @Override
    public Optional<UserSubscription> getUserSubscription(String user, String id, boolean deleted) {
        return Optional.empty();
    }

    @Override
    public void setUserSubscriptionDeletedFlag(UserSubscription userSubscription, boolean isDeleted) throws ArlasSubscriptionsException {
        UpdateRequest updateRequest = new UpdateRequest(arlasSubscriptionIndex, arlasSubscriptionType, userSubscription.getId())
                .script(new Script("ctx._source.deleted = \"" + isDeleted + "\""));
        UpdateResponse response = null;
        try {
            response = client.update(updateRequest).get();
        } catch (Exception e) {
            throw new ArlasSubscriptionsException("Cannot update subscription index: " + e.getMessage());
        }

        if (response.status().getStatus() != RestStatus.OK.getStatus()
                && response.status().getStatus() != RestStatus.ACCEPTED.getStatus()) {
            throw new ArlasSubscriptionsException("Unable to update subscription. Response: " + response.status().toString());
        }
    }

    public void initSubscriptionIndex() throws ArlasSubscriptionsException {
        try {
            IndicesExistsResponse indicesExistsResponse = client.admin().indices().prepareExists().setIndices(arlasSubscriptionIndex).get();
            if (indicesExistsResponse.isExists()) {
                TypesExistsResponse typesExistsResponse = client.admin().indices().prepareTypesExists(arlasSubscriptionIndex).setTypes(arlasSubscriptionType).get();
                if (! typesExistsResponse.isExists()) {
                    throw new ArlasSubscriptionsException("Type " + arlasSubscriptionType  + " does not exist in " + arlasSubscriptionIndex + " index , create it to run ARLAS-Subscription");
                }
            } else {
                throw new ArlasSubscriptionsException(arlasSubscriptionIndex  + " elasticsearch index does not exist, create it to run ARLAS-Subscription");
            }
        } catch (IndexNotFoundException e) {
            throw new ArlasSubscriptionsException(arlasSubscriptionIndex  + " elasticsearch index does not exist, create it to run ARLAS-Subscription");
        }
    }
}
