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

import co.elastic.clients.elasticsearch._helpers.bulk.BulkIngester;
import co.elastic.clients.elasticsearch._helpers.bulk.BulkListener;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.Result;
import co.elastic.clients.elasticsearch._types.Script;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.UpdateResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import io.arlas.commons.exceptions.ArlasException;
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
import org.everit.json.schema.ValidationException;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static io.arlas.subscriptions.app.ArlasSubscriptionsManager.MANAGER;

public class ElasticUserSubscriptionDAOImpl implements UserSubscriptionDAO  {
    public final ArlasLogger logger = ArlasLoggerFactory.getLogger(ElasticUserSubscriptionDAOImpl.class, MANAGER);

    private final ElasticClient client;
    private final String arlasSubscriptionIndex;
    private final TriggerConfiguration triggerConfiguration;
    private final BulkConfiguration bulkConfiguration;
    private final JsonSchemaValidator jsonSchemaValidator;
    private final DecimalFormat df2 = new DecimalFormat(" #,##0.00 %");

    private BulkIngester<String> bulkProcessor;

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
            response = client.index(arlasSubscriptionIndex, userSubscription.getId(), indexedUserSubscription);
        } catch (ValidationException e) {
            throw new ArlasSubscriptionsException("Error in validation of trigger json schema: " + e.getErrorMessage());
        } catch (ElasticsearchException e) {
            throw new ArlasSubscriptionsException("Elasticsearch not available: " + e.getMessage());
        } catch (ArlasException e) {
            throw new ArlasSubscriptionsException("Error while indexing:" + e.getMessage());
        }
        if (response.result() != Result.Created && response.result() !=  Result.Updated) {
            throw new ArlasSubscriptionsException("Unable to index subscription. Response: " + response.result());
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
        UpdateResponse<UserSubscription> response = null;
        try {
            response = client.getClient().update(b -> b
                    .index(arlasSubscriptionIndex)
                    .id(userSubscription.getId())
                    .script(new Script.Builder().inline(s -> s
                                    .lang("painless")
                                    .source("ctx._source.deleted = " + isDeleted)
                                    .params(Collections.emptyMap()))
                            .build()
                    ), UserSubscription.class);
        } catch (Exception e) {
            throw new ArlasSubscriptionsException("Cannot update subscription index: " + e.getMessage());
        }

        if (response.result() != Result.Updated) {
            throw new ArlasSubscriptionsException("Unable to update subscription delete flag. Response: " + response.result());
        }
    }

    public void initSubscriptionIndex() throws ArlasSubscriptionsException {
        try {
            if (!client.indexExists(arlasSubscriptionIndex)) {
                throw new ArlasSubscriptionsException(arlasSubscriptionIndex  + " elasticsearch index does not exist, create it to run ARLAS-Subscription");
            }
        } catch (ArlasException e) {
            throw new ArlasSubscriptionsException("Error: " + e.getMessage());
        }
    }

    public void initBulkProcessor(final long total) throws ArlasSubscriptionsException {
        this.initSubscriptionIndex();

        BulkListener<String> listener = new BulkListener<>() {
            long count = 0;
            long err = 0;

            @Override
            public void beforeBulk(long executionId, BulkRequest request, List<String> contexts) {
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request, List<String> contexts, BulkResponse response) {
                for (int i = 0; i < contexts.size(); i++) {
                    BulkResponseItem item = response.items().get(i);
                    if (item.error() != null) {
                        err++;
                        logger.warn("Failed to index " + contexts.get(i) + " - " + item.error().reason());
                    } else {
                        count++;
                    }
                }
                logger.info(String.format("Nb of doc synchronised (error): %s (%s) / %s [%s]", count, err, total, df2.format((double) count/total)));
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request, List<String> contexts, Throwable failure) {
                // The request could not be sent
                logger.info(String.format("Nb of doc synchronised (error): %s (%s) / %s [%s]", count, err, total, df2.format((double) count/total)));
                logger.warn("-> Failure in synchronization: " + failure.getMessage());
            }
        };

        bulkProcessor = BulkIngester.of(b -> b
                .client(client.getClient())
                .maxOperations(bulkConfiguration.bulkActions * 1024 * 1024)
                .maxConcurrentRequests(bulkConfiguration.concurrentRequests)
                .maxSize(bulkConfiguration.bulkSize)
                .flushInterval(bulkConfiguration.flushInterval, TimeUnit.MILLISECONDS)
                .listener(listener)
        );
    }

    public void addToBulkProcessor(UserSubscription userSubscription) {
        try {
            IndexedUserSubscription indexedUserSubscription = new IndexedUserSubscription(userSubscription, this.triggerConfiguration.triggerGeometryKey, this.triggerConfiguration.triggerCentroidKey);
            bulkProcessor.add(op -> op
                            .index(idx -> idx
                                    .index(arlasSubscriptionIndex)
                                    .id(userSubscription.getId())
                                    .document(indexedUserSubscription)
                            ),
                    indexedUserSubscription.getId()
            );
        } catch (ArlasSubscriptionsException e) {
            logger.warn("Impossible to convert UserSubscription to IndexedUserSubscription:" + e.getMessage());
        } catch (RuntimeException e) {
            logger.warn("Unexpected error:" + e.getMessage());
        }
    }

    public void finaliseBulkProcessor() {
        try {
            long j = 0;
            while (j <= 600_000L) {
                try {
                    if (bulkProcessor.pendingOperations() > 0) {
                        j += 1000L;
                        logger.info("There are " + bulkProcessor.pendingOperations() + " pending operations. Sleeping 1000L");
                        Thread.sleep(1000L);
                    } else {
                        logger.info("There are 0 pending operations. Breaking.");
                        break;
                    }
                } catch (InterruptedException ignored) { }
            }

            logger.info("Close bulk");
            bulkProcessor.close();
            logger.info("Refresh indices");
            client.getClient().indices().refresh();
        } catch (IOException e) {
            logger.warn("Failure in finalisation: " + e.getMessage());
        }
    }
}
