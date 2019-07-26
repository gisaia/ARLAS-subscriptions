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

package io.arlas.subscriptions;

import io.restassured.RestAssured;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Optional;

public abstract class AbstractTestContext {
    static Logger LOGGER = LoggerFactory.getLogger(AbstractTestContext.class);

    protected static String arlasSubManagerPath;

    public AbstractTestContext() {
    }

    static {
        String arlasSubManagerHost = Optional.ofNullable(System.getenv("ARLAS_SUB_MANAGER_HOST")).orElse("localhost");
        int arlasSubManagerPort = Integer.valueOf(Optional.ofNullable(System.getenv("ARLAS_SUB_MANAGER_PORT")).orElse("9998"));
        RestAssured.baseURI = "http://" + arlasSubManagerHost;
        RestAssured.port = arlasSubManagerPort;
        RestAssured.basePath = "";
        LOGGER.info(arlasSubManagerHost + ":" + arlasSubManagerPort);
        String arlasSubManagerPrefix = Optional.ofNullable(System.getenv("ARLAS_SUB_MANAGER_PREFIX")).orElse("/arlas-subscriptions-manager");
        String arlasSubManagerAppPath = Optional.ofNullable(System.getenv("ARLAS_SUB_MANAGER_APP_PATH")).orElse("/");
        if (arlasSubManagerAppPath.endsWith("/"))
            arlasSubManagerAppPath = arlasSubManagerAppPath.substring(0, arlasSubManagerAppPath.length() - 1);
        arlasSubManagerPath = arlasSubManagerPath + arlasSubManagerPrefix;
        if (arlasSubManagerAppPath.endsWith("//"))
            arlasSubManagerPath = arlasSubManagerPath.substring(0, arlasSubManagerPath.length() - 1);
        if (!arlasSubManagerAppPath.endsWith("/"))
            arlasSubManagerPath = arlasSubManagerPath + "/";

    }
}
