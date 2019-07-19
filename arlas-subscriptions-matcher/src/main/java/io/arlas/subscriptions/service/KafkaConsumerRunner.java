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

package io.arlas.subscriptions.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.arlas.client.model.Hit;
import io.arlas.subscriptions.app.ArlasSubscriptionsConfiguration;
import io.arlas.subscriptions.kafka.SubscriptionEventKafkaConsumer;
import io.arlas.subscriptions.model.SubscriptionEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class KafkaConsumerRunner implements Runnable {
    private Logger LOGGER = LoggerFactory.getLogger(KafkaConsumerRunner.class);

    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ArlasSubscriptionsConfiguration configuration;
    private final SubscriptionsService subscriptionsService;
    private final ProductService productService;
    private KafkaConsumer consumer;

    KafkaConsumerRunner(ArlasSubscriptionsConfiguration configuration) {
        this.configuration = configuration;
        this.subscriptionsService = new SubscriptionsService(configuration);
        this.productService = new ProductService(configuration);
    }

    @Override
    public void run() {
        try {
            LOGGER.info("Starting consumer of topic '" + configuration.kafkaConfiguration.subscriptionEventsTopic + "'");
            consumer = SubscriptionEventKafkaConsumer.build(configuration);

            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(configuration.kafkaConfiguration.consumerPollTimeout));
                for (ConsumerRecord<String, String> record : records) {

                    try {
                        final SubscriptionEvent event = objectMapper.readValue(record.value(), SubscriptionEvent.class);
                        LOGGER.debug("Received subscription event:" + event.toString());

                        List<Hit> hits = subscriptionsService.searchMatchingSubscriptions(event);
                        productService.processMatchingProducts(event, hits);

                    } catch (IOException e) {
                        LOGGER.warn("Could not parse record " + record.value());
                    }
                }
                consumer.commitSync();
            }

        } catch (WakeupException e) {
            // Ignore exception if closing
            if (!closed.get()) throw e;
        } finally {
            LOGGER.info("Closing consumer of topic '" + configuration.kafkaConfiguration.subscriptionEventsTopic + "'");
            consumer.close();
        }
    }

    // Shutdown hook which can be called from a separate thread
    public void stop() {
        closed.set(true);
        consumer.wakeup();
    }
}
