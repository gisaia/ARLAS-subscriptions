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

package io.arlas.subscriptions.db.mongo;


import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import io.arlas.subscriptions.configuration.mongo.MongoDBConfiguration;
import io.arlas.subscriptions.configuration.mongo.Seed;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.util.List;
import java.util.stream.Collectors;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class MongoDBFactoryConnection {
    private MongoDBConfiguration mongoDBConfiguration;

    public MongoDBFactoryConnection(final MongoDBConfiguration mongoDBConfiguration) {
        this.mongoDBConfiguration = mongoDBConfiguration;
    }

    public MongoClient getClient() {
        // Create a CodecRegistry containing the PojoCodecProvider instance.
        CodecProvider pojoCodecProvider = PojoCodecProvider.builder().register("io.arlas.subscriptions.model").build();
        CodecRegistry defaultCodecRegistry = MongoClientSettings.getDefaultCodecRegistry();
        CodecRegistry pojoCodecRegistry = fromRegistries(defaultCodecRegistry, fromProviders(pojoCodecProvider));

        final MongoClient client = MongoClients.create(
                MongoClientSettings.builder()
                        .codecRegistry(pojoCodecRegistry)
                        .applyToClusterSettings(builder -> builder.hosts(getServers())).build()
        );
        return client;
    }


    private List<ServerAddress> getServers() {
        final List<Seed> seeds = mongoDBConfiguration.getSeeds();
        return seeds.stream()
                .map(seed -> {
                    final ServerAddress serverAddress = new ServerAddress(seed.getHost(), seed.getPort());
                    return serverAddress;
                })
                .collect(Collectors.toList());
    }
}
