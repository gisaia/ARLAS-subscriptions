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

package io.arlas.subscriptions.exception;

import javax.ws.rs.core.Response;

public class UnauthorizedException extends ArlasSubscriptionsException {

    private static final long serialVersionUID = 1L;

    public UnauthorizedException() {
        super();
        status  = Response.Status.UNAUTHORIZED;
    }

    public UnauthorizedException(String message) {
        super(message);
        status  = Response.Status.UNAUTHORIZED;
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
        status  = Response.Status.UNAUTHORIZED;
    }

}

