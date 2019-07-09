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

package io.arlas.subscriptions.kafka;

import io.arlas.subscriptions.app.ArlasSubscriptionsConfiguration;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.Collections;
import java.util.Properties;
import java.util.UUID;

public class SubscriptionEventKafkaConsumer extends KafkaConsumer<String, String> {

    private SubscriptionEventKafkaConsumer(Properties properties) {
        super(properties);
    }

    public static SubscriptionEventKafkaConsumer build(ArlasSubscriptionsConfiguration configuration) {
        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, configuration.kafkaConfiguration.bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, configuration.kafkaConfiguration.consumerGroupId);
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, UUID.randomUUID().toString());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, configuration.kafkaConfiguration.batchSize);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        SubscriptionEventKafkaConsumer consumer = new SubscriptionEventKafkaConsumer(props);
        consumer.subscribe(Collections.singletonList(configuration.kafkaConfiguration.subscriptionEventsTopic));

        return consumer;
    }
}
