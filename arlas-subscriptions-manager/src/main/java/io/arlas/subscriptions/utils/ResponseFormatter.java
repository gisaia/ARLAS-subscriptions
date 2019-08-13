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

package io.arlas.subscriptions.utils;

import io.arlas.subscriptions.model.response.Success;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;

public class ResponseFormatter {

    public static Response getSuccessResponse(String message) {
        return Response.ok(new Success(Response.Status.OK.getStatusCode(), message))
                .type(MediaType.APPLICATION_JSON).build();
    }

    public static Response getResultResponse(Object object) {
        return Response.ok(object).type(MediaType.APPLICATION_JSON).build();
    }

    public static Response getAcceptedResponse(Object object) {
        return Response.accepted(object).type(MediaType.APPLICATION_JSON).build();
    }

    public static Response getCreatedResponse(URI location, Object object) {
        return Response.created(location).type(MediaType.APPLICATION_JSON).entity(object).build();
    }

    public static Response getFileResponse(Object object, String fileName) {
        return Response.ok(object).type(MediaType.APPLICATION_JSON)
                .header("Content-Disposition", "attachment; filename=" + fileName).build();
    }
}
