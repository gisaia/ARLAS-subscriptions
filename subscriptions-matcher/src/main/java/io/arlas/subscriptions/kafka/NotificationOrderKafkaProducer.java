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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cyclops.control.Try;
import io.arlas.subscriptions.app.ArlasSubscriptionsConfiguration;
import io.arlas.subscriptions.logger.ArlasLogger;
import io.arlas.subscriptions.logger.ArlasLoggerFactory;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.UUID;

import static io.arlas.subscriptions.app.ArlasSubscriptionsMatcher.MATCHER;

public class NotificationOrderKafkaProducer extends KafkaProducer<String, String> {

    private final ArlasLogger logger = ArlasLoggerFactory.getLogger(NotificationOrderKafkaProducer.class, MATCHER);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String topic;

    public NotificationOrderKafkaProducer(Properties properties, String topic) {
        super(properties);
        this.topic = topic;
    }

    public Try<Void, Exception> send(Object object) {

        logger.debug("Sending to Kafka topic '" + topic + "'");
        return Try.runWithCatch(() -> {

            this.send(new ProducerRecord<>(topic, objectMapper.writeValueAsString(object)),
                    (metadata, exception) -> {
                        if (metadata == null) {
                            throw new RuntimeException(exception);
                        }
                    });

        }, JsonProcessingException.class);
    }

    public static NotificationOrderKafkaProducer build(ArlasSubscriptionsConfiguration configuration) {
        Properties kafkaProperties = new Properties();
        kafkaProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, configuration.kafkaConfiguration.bootstrapServers);
        kafkaProperties.put(ProducerConfig.CLIENT_ID_CONFIG, UUID.randomUUID().toString());
        kafkaProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        kafkaProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        return new NotificationOrderKafkaProducer(kafkaProperties,
                configuration.kafkaConfiguration.notificationOrdersTopic);
    }
}
