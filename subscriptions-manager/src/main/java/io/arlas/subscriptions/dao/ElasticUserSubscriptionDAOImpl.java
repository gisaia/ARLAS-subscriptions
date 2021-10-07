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
import io.arlas.server.core.exceptions.ArlasException;
import io.arlas.server.core.impl.elastic.utils.ElasticClient;
import io.arlas.subscriptions.configuration.TriggerConfiguration;
import io.arlas.subscriptions.configuration.elastic.BulkConfiguration;
import io.arlas.subscriptions.configuration.elastic.ElasticDBConfiguration;
import io.arlas.subscriptions.db.elastic.ElasticDBManaged;
import io.arlas.subscriptions.exception.ArlasSubscriptionsException;
import io.arlas.subscriptions.logger.ArlasLogger;
import io.arlas.subscriptions.logger.ArlasLoggerFactory;
import io.arlas.subscriptions.model.IndexedUserSubscription;
import io.arlas.subscriptions.model.UserSubscription;
import io.arlas.subscriptions.utils.JsonSchemaValidator;
import org.apache.commons.lang3.tuple.Pair;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.script.Script;
import org.everit.json.schema.ValidationException;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static io.arlas.subscriptions.app.ArlasSubscriptionsManager.MANAGER;

public class ElasticUserSubscriptionDAOImpl implements UserSubscriptionDAO  {
    public final ArlasLogger logger = ArlasLoggerFactory.getLogger(ElasticUserSubscriptionDAOImpl.class, MANAGER);

    private ElasticClient client;
    private String arlasSubscriptionIndex;
    private TriggerConfiguration triggerConfiguration;
    private BulkConfiguration bulkConfiguration;
    private JsonSchemaValidator jsonSchemaValidator;
    private final DecimalFormat df2 = new DecimalFormat(" #,##0.00 %");

    private BulkProcessor bulkProcessor;

    private static ObjectMapper mapper = new ObjectMapper();

    public ElasticUserSubscriptionDAOImpl(ElasticDBConfiguration elasticDBConfiguration, TriggerConfiguration triggerConfiguration, ElasticDBManaged elasticDBManaged,
                                          JsonSchemaValidator jsonSchemaValidator) throws ArlasSubscriptionsException {
            this.client = elasticDBManaged.esClient;
            this.arlasSubscriptionIndex = elasticDBConfiguration.elasticsubindex;
            this.triggerConfiguration=triggerConfiguration;
            this.bulkConfiguration = elasticDBConfiguration.bulkConfiguration;
            this.initSubscriptionIndex();
            this.jsonSchemaValidator=jsonSchemaValidator;
    }

    @Override
    public Pair<Integer, List<UserSubscription>> getAllUserSubscriptions(String user, Long before, Long after, Boolean active, Boolean started, Boolean expired, boolean deleted, Boolean createdByAdmin, Integer page,
                                                                         Integer size) {
        return null;
    }

    @Override
    public UserSubscription postUserSubscription(UserSubscription userSubscription, boolean createdByAdmin) throws ArlasSubscriptionsException {
        IndexResponse response = null;
        try {
            this.jsonSchemaValidator.validJsonObjet(userSubscription.subscription.trigger);
            IndexedUserSubscription indexedUserSubscription = new IndexedUserSubscription(userSubscription,this.triggerConfiguration.triggerGeometryKey,this.triggerConfiguration.triggerCentroidKey);
            response = client.index(arlasSubscriptionIndex, userSubscription.getId(), mapper.writeValueAsString(indexedUserSubscription));
        } catch (ValidationException e) {
            throw new ArlasSubscriptionsException("Error in validation of trigger json schema: " + e.getErrorMessage());
        } catch (JsonProcessingException e) {
            throw new ArlasSubscriptionsException("Error in writing subscription json: " + e.getMessage());
        } catch (ElasticsearchException e) {
            throw new ArlasSubscriptionsException("Elasticsearch not available: " + e.getMessage());
        } catch (ArlasException e) {
            throw new ArlasSubscriptionsException("Error while indexing:" + e.getMessage());
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
    public Optional<UserSubscription> getSubscription(String id, Optional<String> user, boolean deleted) {
        return Optional.empty();
    }

    @Override
    public void setUserSubscriptionDeletedFlag(UserSubscription userSubscription, boolean isDeleted) throws ArlasSubscriptionsException {
        UpdateRequest updateRequest = new UpdateRequest(arlasSubscriptionIndex, userSubscription.getId())
                .script(new Script("ctx._source.deleted = \"" + isDeleted + "\""));
        UpdateResponse response = null;
        try {
            response = client.getClient().update(updateRequest, RequestOptions.DEFAULT);
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
            if (!client.indexExists(arlasSubscriptionIndex)) {
                throw new ArlasSubscriptionsException(arlasSubscriptionIndex  + " elasticsearch index does not exist, create it to run ARLAS-Subscription");
            }
        } catch (IndexNotFoundException e) {
            throw new ArlasSubscriptionsException(arlasSubscriptionIndex  + " elasticsearch index does not exist, create it to run ARLAS-Subscription");
        } catch (ArlasException e) {
            throw new ArlasSubscriptionsException("Error: " + e.getMessage());
        }
    }

    public void initBulkProcessor(final long total) throws ArlasSubscriptionsException {
        this.initSubscriptionIndex();

        BulkProcessor.Listener listener = new BulkProcessor.Listener() {
            long count = 0;

            @Override
            public void beforeBulk(long executionId,
                                   BulkRequest request) {
                count += request.numberOfActions();
            }

            @Override
            public void afterBulk(long executionId,
                                  BulkRequest request,
                                  BulkResponse response) {
                logger.info("Nb of doc synchronised: " + count + "/" + total + " (" + df2.format((double) count/total) + ")");
                if (response.hasFailures()) {
                    logger.warn("-> But some failure in synchronization: " + response.buildFailureMessage());
                }
            }

            @Override
            public void afterBulk(long executionId,
                                  BulkRequest request,
                                  Throwable failure) {
                logger.info("Nb of doc synchronised: " + count + "/" + total + " (" + df2.format((double) count/total) + ")");
                logger.warn("-> Failure in synchronization: " + failure.getMessage());
            }
        };

        bulkProcessor = BulkProcessor.builder((request, bulkListener) ->
                        client.getClient().bulkAsync(request, RequestOptions.DEFAULT, bulkListener), listener)
                .setConcurrentRequests(bulkConfiguration.concurrentRequests)
                .setBulkActions(bulkConfiguration.bulkActions)
                .setBulkSize(new ByteSizeValue(bulkConfiguration.bulkSize, ByteSizeUnit.MB))
                .setFlushInterval(TimeValue.timeValueMillis(bulkConfiguration.flushInterval))
                .setBackoffPolicy(
                        BackoffPolicy.exponentialBackoff(TimeValue.timeValueMillis(bulkConfiguration.backoffDelay), bulkConfiguration.backoffRetries))
                .build();
    }

    public void addToBulkProcessor(UserSubscription userSubscription) {
        try {
            IndexedUserSubscription indexedUserSubscription = new IndexedUserSubscription(userSubscription, this.triggerConfiguration.triggerGeometryKey, this.triggerConfiguration.triggerCentroidKey);
            bulkProcessor.add(new IndexRequest(arlasSubscriptionIndex).id(userSubscription.getId())
                    .source(mapper.writeValueAsString(indexedUserSubscription), XContentType.JSON));
        } catch (ArlasSubscriptionsException e) {
            logger.warn("Impossible to convert UserSubscription to IndexedUserSubscription:" + e.getMessage());
        } catch (JsonProcessingException e) {
            logger.warn("Impossible to convert IndexedUserSubscription to JSON:" + e.getMessage());
        } catch (RuntimeException e) {
            logger.warn("Unexpected error:" + e.getMessage());
        }
    }

    public void finaliseBulkProcessor() {
        try {
            bulkProcessor.awaitClose(10, TimeUnit.MINUTES);
            client.getClient().indices().refresh(new RefreshRequest(), RequestOptions.DEFAULT);
        } catch (InterruptedException | IOException e) {
            logger.warn("Failure in finalisation: " + e.getMessage());
        }
    }
}
