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

package io.arlas.subscriptions.rest;

import io.arlas.subscriptions.service.UserSubscriptionHALService;
import io.arlas.subscriptions.service.UserSubscriptionManagerService;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import jakarta.ws.rs.core.MediaType;

@OpenAPIDefinition(
        info = @Info(
                title = "ARLAS Subscriptions Manager API",
                description = "Manage ARLAS subscriptions on ARLAS collections' events.",
                license = @License(name = "Apache 2.0", url = "https://www.apache.org/licenses/LICENSE-2.0.html"),
                contact = @Contact(email = "contact@gisaia.com", name = "Gisaia", url = "http://www.gisaia.com/"),
                version = "24.0.5"),
        servers = {
                @Server(url = "/arlas-subscriptions-manager", description = "default server")
        }
)

public abstract class UserSubscriptionManagerAbstractController {

    public static final String UTF8JSON = MediaType.APPLICATION_JSON + ";charset=utf-8";
    protected UserSubscriptionManagerService subscriptionManagerService;
    protected UserSubscriptionHALService halService;
    protected String identityHeader;
    protected String identityAdmin;

    public UserSubscriptionManagerAbstractController(
            UserSubscriptionManagerService subscriptionManagerService,
            UserSubscriptionHALService halService,
            String identityHeader,
            String identityAdmin) {
        this.subscriptionManagerService = subscriptionManagerService;
        this.halService = halService;
        this.identityHeader = identityHeader;
        this.identityAdmin = identityAdmin;
    }

}
