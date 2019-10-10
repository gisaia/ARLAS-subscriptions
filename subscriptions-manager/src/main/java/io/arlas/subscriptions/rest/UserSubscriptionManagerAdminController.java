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

import com.codahale.metrics.annotation.Timed;
import io.arlas.subscriptions.exception.ArlasSubscriptionsException;
import io.arlas.subscriptions.exception.NotFoundException;
import io.arlas.subscriptions.logger.ArlasLogger;
import io.arlas.subscriptions.logger.ArlasLoggerFactory;
import io.arlas.subscriptions.model.SubscriptionListResource;
import io.arlas.subscriptions.model.UserSubscription;
import io.arlas.subscriptions.model.UserSubscriptionWithLinks;
import io.arlas.subscriptions.model.response.Error;
import io.arlas.subscriptions.service.UserSubscriptionHALService;
import io.arlas.subscriptions.service.UserSubscriptionManagerService;
import io.arlas.subscriptions.utils.ResponseFormatter;
import io.swagger.annotations.*;
import org.apache.commons.lang3.tuple.Pair;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.Optional;

import static io.arlas.subscriptions.app.ArlasSubscriptionsManager.MANAGER;

@Path("/admin/subscriptions")
@Api(value = "/admin/subscriptions", tags = {"admin"})
public class UserSubscriptionManagerAdminController extends UserSubscriptionManagerAbstractController {
    public final ArlasLogger logger = ArlasLoggerFactory.getLogger(UserSubscriptionManagerAbstractController.class, MANAGER);

    public UserSubscriptionManagerAdminController(
            UserSubscriptionManagerService subscriptionManagerService,
            UserSubscriptionHALService halService,
            String identityHeader,
            String identityAdmin) {
        super(subscriptionManagerService, halService, identityHeader, identityAdmin);
    }

    @Timed
    @Path("/")
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(
            value = "List all available subscriptions",
            produces = UTF8JSON,
            notes = "Return the list of all registered subscriptions from the latest created to the earliest.",
            consumes = UTF8JSON
    )
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation", response = SubscriptionListResource.class),
            @ApiResponse(code = 401, message = "Unauthorized.", response = Error.class),
            @ApiResponse(code = 503, message = "Arlas Subscriptions Manager Error.", response = Error.class)})

    public Response getAll(@Context UriInfo uriInfo,
                           @Context HttpHeaders headers,
            // --------------------------------------------------------
            // ----------------------- FORM -----------------------
            // --------------------------------------------------------
            @ApiParam(name = "before", value = "Retrieve subscriptions created before given timestamp.",
                    allowMultiple = false,
                    required = false)
            @QueryParam(value = "before") Long before,
           @ApiParam(name = "after", value = "Retrieve subscriptions created after given timestamp.",
                   allowMultiple = false,
                   required = false)
               @QueryParam(value = "after") Long after,
           @ApiParam(name = "active", value = "Filter subscriptions whether they are active or not (returns all if missing, 'active' if 'true', 'not active' if 'false').",
                    allowMultiple = false,
                    required = false)
            @QueryParam(value = "active") Boolean active,
           @ApiParam(name = "started", value = "Filter subscriptions whether they are started or not (returns all if missing, 'started' if 'true', 'not started' if 'false').",
                   allowMultiple = false,
                   required = false)
               @QueryParam(value = "started") Boolean started,
           @ApiParam(name = "expired", value = "Filter subscriptions whether they are expired or not (returns all if missing, 'expired' if 'true', 'not expired' if 'false').",
                    allowMultiple = false,
                    required = false)
            @QueryParam(value = "expired") Boolean expired,
           @ApiParam(name = "created-by", value = "Filter subscriptions by creator's identifier",
                   allowMultiple = false,
                   required = false)
               @QueryParam(value = "created-by") String createdBy,
           @ApiParam(name = "deleted", value = "Filter subscriptions whether they are deleted or not.",
                   allowMultiple = false,
                   required = false,
                    defaultValue = "true" )
               @QueryParam(value = "deleted") Boolean deleted,
           @ApiParam(name = "created-by-admin", value = "Filter subscriptions whether they have been created by admin or not (returns all if missing, 'created_by_admin' if 'true', 'not created_by_admin' if 'false').",
                   allowMultiple = false,
                   required = false)
               @QueryParam(value = "created-by-admin") Boolean createdByAdmin,
           @ApiParam(name = "pretty", value = "Pretty print",
                   allowMultiple = false,
                   defaultValue = "false",
                   required = false)
               @QueryParam(value = "pretty") Boolean pretty,
           @ApiParam(name = "page", value = "Page ID",
                   defaultValue = "1",
                   allowableValues = "range[1, infinity]",
                   type = "integer",
                   required = false)
               @DefaultValue("1")
               @QueryParam(value = "page") Integer page,
            @ApiParam(name = "size", value = "Page Size",
                    defaultValue = "10",
                    allowableValues = "range[1, infinity]",
                    type = "integer",
                    required = false)
            @DefaultValue("10")
            @QueryParam(value = "size") Integer size
    ) throws ArlasSubscriptionsException {

        logger.debug(String.format("Admin requests all subscriptions (before %d, after %d, active %s, started %b, expired %s, deleted %b, created-by-admin %b, page %d, size %d)",
                before, after, active, started, expired, deleted, createdByAdmin, page, size));
        Pair<Integer, List<UserSubscription>> subscriptionList = subscriptionManagerService.getAllUserSubscriptions(
                createdBy,
                before,
                after,
                active,
                started,
                expired,
                Optional.ofNullable(deleted).orElse(Boolean.TRUE),
                createdByAdmin,
                page,
                size);
        SubscriptionListResource subscriptionListResource = halService.subscriptionListToResource(subscriptionList, uriInfo, page, size);
        return ResponseFormatter.getResultResponse(subscriptionListResource);
    }

    @Timed
    @Path("{id}")
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(
            value = "Find subscription by ID",
            produces = UTF8JSON,
            notes = "Return a single subscription. " +
                    "Only creator can access their subscriptions.",
            consumes = UTF8JSON,
            response = UserSubscriptionWithLinks.class
    )
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation", response = UserSubscriptionWithLinks.class),
            @ApiResponse(code = 401, message = "Unauthorized.", response = Error.class),
            @ApiResponse(code = 404, message = "Subscription not found.", response = Error.class),
            @ApiResponse(code = 503, message = "Arlas Subscriptions Manager Error.", response = Error.class)})

    public Response get(@Context UriInfo uriInfo,
                        @Context HttpHeaders headers,
            // --------------------------------------------------------
            // ----------------------- FORM -----------------------
            // --------------------------------------------------------
            @ApiParam(
                    name = "id",
                    value = "ID of subscription to return",
                    allowMultiple = false,
                    required = true)
            @PathParam(value = "id") String id,
            @ApiParam(name = "deleted", value = "Filter subscriptions whether they are deleted or not.",
                    allowMultiple = false,
                    required = false,
                    defaultValue = "true" )
                @QueryParam(value = "deleted") Boolean deleted,
            @ApiParam(name = "pretty", value = "Pretty print",
                    allowMultiple = false,
                    defaultValue = "false",
                    required = false)
            @QueryParam(value = "pretty") Boolean pretty
    ) throws ArlasSubscriptionsException {

        logger.debug(String.format("Admin requests subscription %s (deleted %b)", id, Optional.ofNullable(deleted).orElse(Boolean.TRUE)));
        UserSubscription userSubscription = subscriptionManagerService.getUserSubscription(id, Optional.empty(), Optional.ofNullable(deleted).orElse(Boolean.TRUE))
                .orElseThrow(() -> new NotFoundException("Subscription with id " + id + " not found"));

        return ResponseFormatter.getResultResponse(halService.subscriptionWithLinks(userSubscription, uriInfo));
    }

    @Timed
    @Path("{id}")
    @DELETE
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(
            value = "Delete a subscription",
            produces = UTF8JSON,
            notes = "Mark a subscription as deleted.",
            consumes = UTF8JSON,
            response = UserSubscriptionWithLinks.class
    )
    @ApiResponses(value = {@ApiResponse(code = 202, message = "Subscription has been deleted.", response = UserSubscriptionWithLinks.class),
            @ApiResponse(code = 401, message = "Unauthorized.", response = Error.class),
            @ApiResponse(code = 404, message = "Subscription not found.", response = Error.class),
            @ApiResponse(code = 503, message = "Arlas Subscriptions Manager Error.", response = Error.class)})

    public Response delete(@Context HttpHeaders headers,
           // --------------------------------------------------------
           // ----------------------- FORM -----------------------
           // --------------------------------------------------------
            @ApiParam(
                    name = "id",
                    value = "Subscription ID to delete",
                    allowMultiple = false,
                    required = true)
            @PathParam(value = "id") String id,
            @ApiParam(name = "pretty", value = "Pretty print",
                    allowMultiple = false,
                    defaultValue = "false",
                    required = false)
            @QueryParam(value = "pretty") Boolean pretty
    ) throws ArlasSubscriptionsException {

        logger.debug(String.format("Admin requests deletion of subscription %s", id));
        UserSubscription userSubscription = subscriptionManagerService.getUserSubscription(id, Optional.empty(), true)
                .orElseThrow(() -> new NotFoundException("Subscription with id " + id + " not found"));
        subscriptionManagerService.deleteUserSubscription(userSubscription);

        return ResponseFormatter.getAcceptedResponse(new UserSubscriptionWithLinks(userSubscription));
    }

    @Path("/")
    @POST
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(
            value = "Register a new subscription",
            produces = UTF8JSON,
            notes = "Register a subscription for further notification.",
            consumes = UTF8JSON,
            response = UserSubscriptionWithLinks.class
    )
    @ApiResponses(value = {@ApiResponse(code = 201, message = "Subscription has been registered", response = UserSubscriptionWithLinks.class),
            @ApiResponse(code = 400, message = "JSON parameter malformed.", response = Error.class),
            @ApiResponse(code = 401, message = "Unauthorized.", response = Error.class),
            @ApiResponse(code = 503, message = "Arlas Subscriptions Manager Error.", response = Error.class)})
    public Response post(@Context UriInfo uriInfo,
            @Context HttpHeaders headers,
            @ApiParam(name = "subscription",
                    value = "Subscription description",
                    required = true)
            @NotNull @Valid UserSubscription subscription,

            // --------------------------------------------------------
            // ----------------------- FORM -----------------------
            // --------------------------------------------------------
            @ApiParam(name = "pretty", value = "Pretty print",
                    allowMultiple = false,
                    defaultValue = "false",
                    required = false)
            @QueryParam(value = "pretty") Boolean pretty

    ) throws ArlasSubscriptionsException {

        logger.debug(String.format("Admin creates a new subscription for user %s", subscription.created_by));

        return ResponseFormatter.getCreatedResponse(
                uriInfo.getRequestUriBuilder().build(),
                halService.subscriptionWithLinks(
                        subscriptionManagerService.postUserSubscription(
                                subscription,
                                true),
                        uriInfo));
    }

    @Path("{id}")
    @PUT
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(
            value = "Update an existing subscription",
            produces = UTF8JSON,
            notes = "Update an existing subscription. ",
            consumes = UTF8JSON,
            response = UserSubscriptionWithLinks.class
    )
    @ApiResponses(value = {@ApiResponse(code = 201, message = "Successful operation", response = UserSubscriptionWithLinks.class),
            @ApiResponse(code = 400, message = "JSON parameter malformed.", response = Error.class),
            @ApiResponse(code = 401, message = "Unauthorized.", response = Error.class),
            @ApiResponse(code = 404, message = "Not Found Error.", response = Error.class),
            @ApiResponse(code = 503, message = "Arlas Subscriptions Manager Error.", response = Error.class)})
    public Response put(@Context UriInfo uriInfo,
                        @Context HttpHeaders headers,
                        // --------------------------------------------------------
                        // ----------------------- FORM -----------------------
                        // --------------------------------------------------------
                        @ApiParam(
                                name = "id",
                                value = "ID of subscription to return",
                                allowMultiple = false,
                                required = true)
                        @PathParam(value = "id") String id,
                        @ApiParam(name = "subscription",
                                value = "Subscription description",
                                required = true)
                        @NotNull @Valid UserSubscription updUserSubscription,
                        @ApiParam(name = "pretty", value = "Pretty print",
                                allowMultiple = false,
                                defaultValue = "false",
                                required = false)
                        @QueryParam(value = "pretty") Boolean pretty

    ) throws ArlasSubscriptionsException {

        logger.debug(String.format("Admin requests update of subscription %s", id));
        UserSubscription oldUserSubscription = subscriptionManagerService.getUserSubscription(id, Optional.empty(), false)
                .orElseThrow(() -> new NotFoundException("Subscription with id " + id + " not found"));

        return ResponseFormatter.getCreatedResponse(uriInfo.getRequestUriBuilder().build(),
                halService.subscriptionWithLinks(subscriptionManagerService.putUserSubscription(oldUserSubscription, updUserSubscription, Optional.empty()), uriInfo));
    }
}
