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
import io.arlas.server.client.model.Hit;
import io.arlas.subscriptions.app.ArlasSubscriptionsMatcherConfiguration;
import io.arlas.subscriptions.app.KafkaConfiguration;
import io.arlas.subscriptions.kafka.SubscriptionEventKafkaConsumer;
import io.arlas.subscriptions.logger.ArlasLogger;
import io.arlas.subscriptions.logger.ArlasLoggerFactory;
import io.arlas.subscriptions.model.SubscriptionEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.locationtech.jts.io.ParseException;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.arlas.subscriptions.app.ArlasSubscriptionsMatcher.MATCHER;

public class KafkaConsumerRunner implements Runnable {
    private final ArlasLogger logger = ArlasLoggerFactory.getLogger(KafkaConsumerRunner.class, MATCHER);

    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final KafkaConfiguration kafkaConfiguration;
    private final SubscriptionsService subscriptionsService;
    private final ProductService productService;
    private KafkaConsumer consumer;

    KafkaConsumerRunner(ArlasSubscriptionsMatcherConfiguration configuration) {
        this.kafkaConfiguration = configuration.kafkaConfiguration;
        this.subscriptionsService = new SubscriptionsService(configuration);
        this.productService = new ProductService(configuration);
    }

    @Override
    public void run() {
        try {
            logger.info("Starting consumer of topic '" + kafkaConfiguration.subscriptionEventsTopic + "'");
            consumer = SubscriptionEventKafkaConsumer.build(kafkaConfiguration);

            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(kafkaConfiguration.consumerPollTimeout));
                for (ConsumerRecord<String, String> record : records) {

                    try {
                        final SubscriptionEvent event = objectMapper.readValue(record.value(), SubscriptionEvent.class);
                        logger.debug("Received subscription event:" + event.toString());

                        List<Hit> hits = subscriptionsService.searchMatchingSubscriptions(event);
                        logger.debug("Subscription matcher result=" + hits.toString());

                        productService.processMatchingProducts(event, hits);

                    } catch (IOException|ParseException e) {
                        logger.warn("Could not parse record " + record.value(), e);
                    }
                }
                consumer.commitSync();
            }

        } catch (WakeupException e) {
            // Ignore exception if closing
            if (!closed.get()) throw e;
        } finally {
            logger.error("Closing consumer of topic '" + kafkaConfiguration.subscriptionEventsTopic + "'");
            consumer.close();
            System.exit(1);
        }
    }

    // Shutdown hook which can be called from a separate thread
    public void stop() {
        closed.set(true);
        consumer.wakeup();
    }
}
