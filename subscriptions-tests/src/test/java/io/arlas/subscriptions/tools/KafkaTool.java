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

package io.arlas.subscriptions.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.arlas.subscriptions.model.NotificationOrder;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class KafkaTool implements Runnable {
    static Logger LOGGER = LoggerFactory.getLogger(KafkaTool.class);
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private KafkaConsumer<String, String> consumer;
    private KafkaProducer<String, String> producer;
    private BlockingQueue<NotificationOrder> messages = new ArrayBlockingQueue<>(500);
    private String subscriptionEventsTopic;

    public KafkaTool() {
    }

    public void init() {
        String kafkaBrokers = Optional.ofNullable(System.getenv("KAFKA_BROKERS")).orElse("kafka:9092");
        String notificationOrdersTopic = Optional.ofNullable(System.getenv("KAFKA_TOPIC_NOTIFICATION_ORDERS")).orElse("notification_orders");
        subscriptionEventsTopic = Optional.ofNullable(System.getenv("KAFKA_TOPIC_SUBSCRIPTION_EVENTS")).orElse("subscription_events");

        final Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBrokers);
        consumerProps.put(ConsumerConfig.CLIENT_ID_CONFIG, UUID.randomUUID().toString());
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "subscriptionsMatcherITGroup");
        consumerProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 10);
        consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);

        consumer = new KafkaConsumer(consumerProps);
        consumer.subscribe(Collections.singletonList(notificationOrdersTopic));

        Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBrokers);
        producerProps.put(ProducerConfig.CLIENT_ID_CONFIG, UUID.randomUUID().toString());
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        producer = new KafkaProducer(producerProps);
    }

    @Override
    public void run() {
        try {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, String> record : records) {
                    try {
                        final NotificationOrder notif = objectMapper.readValue(record.value(), NotificationOrder.class);
                        messages.add(notif);
                    } catch (IOException e) {
                        LOGGER.error("Error while consuming from Kafka:", e);
                    }
                }
            }
        } catch (WakeupException e) {
            if (!closed.get()) throw e;
        } finally {
            consumer.close();
        }
    }

    public void produce(String event) {
        try {
            //synchronous call
            producer.send(new ProducerRecord<>(subscriptionEventsTopic, event)).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public NotificationOrder consume(int timeout, TimeUnit seconds) throws InterruptedException {
        return messages.poll(timeout,seconds);
    }

    // Shutdown hook which can be called from a separate thread
    public void stop() {
        closed.set(true);
        consumer.wakeup();
        producer.flush();
    }
}
