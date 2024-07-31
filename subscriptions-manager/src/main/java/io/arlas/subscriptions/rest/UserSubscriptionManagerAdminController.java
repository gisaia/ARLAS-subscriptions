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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Optional;

import static io.arlas.subscriptions.app.ArlasSubscriptionsManager.MANAGER;

@Path("/admin/subscriptions")
@Tag(name="admin", description = "Optional endpoints to manage all subscriptions as an administrator of the service.")
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
    @Operation(
            summary = "List all available subscriptions",
            description = "Return the list of all registered subscriptions from the latest created to the earliest."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(schema = @Schema(implementation = SubscriptionListResource.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "503", description = "Arlas Subscriptions Manager Error.",
                    content = @Content(schema = @Schema(implementation = Error.class)))})

    public Response getAll(@Context UriInfo uriInfo,

                           // --------------------------------------------------------
                           // ----------------------- FORM -----------------------
                           // --------------------------------------------------------
                           @Parameter(name = "before",
                                   description = "Retrieve subscriptions created before given timestamp.")
                           @QueryParam(value = "before") Long before,

                           @Parameter(name = "after",
                                   description = "Retrieve subscriptions created after given timestamp.")
                           @QueryParam(value = "after") Long after,

                           @Parameter(name = "active",
                                   description = "Filter subscriptions whether they are active or not (returns all if missing, 'active' if 'true', 'not active' if 'false').")
                           @QueryParam(value = "active") Boolean active,

                           @Parameter(name = "started",
                                   description = "Filter subscriptions whether they are started or not (returns all if missing, 'started' if 'true', 'not started' if 'false').")
                           @QueryParam(value = "started") Boolean started,

                           @Parameter(name = "expired",
                                   description = "Filter subscriptions whether they are expired or not (returns all if missing, 'expired' if 'true', 'not expired' if 'false').")
                           @QueryParam(value = "expired") Boolean expired,

                           @Parameter(name = "created-by",
                                   description = "Filter subscriptions by creator's identifier")
                           @QueryParam(value = "created-by") String createdBy,

                           @Parameter(name = "deleted",
                                   description = "Filter subscriptions whether they are deleted or not.",
                                   schema = @Schema(defaultValue = "true"))
                           @QueryParam(value = "deleted") Boolean deleted,

                           @Parameter(name = "created-by-admin",
                                   description = "Filter subscriptions whether they have been created by admin or not (returns all if missing, 'created_by_admin' if 'true', 'not created_by_admin' if 'false').")
                           @QueryParam(value = "created-by-admin") Boolean createdByAdmin,

                           @Parameter(name = "pretty",
                                   description = "Pretty print",
                                   schema = @Schema(defaultValue = "false"))
                           @QueryParam(value = "pretty") Boolean pretty,

                           @Parameter(name = "page",
                                   description = "Page ID",
                                   schema = @Schema(type="integer", minimum = "1", defaultValue = "1"))
                           @DefaultValue("1")
                           @QueryParam(value = "page") Integer page,

                           @Parameter(name = "size",
                                   description = "Page Size",
                                   schema = @Schema(type="integer", minimum = "1", defaultValue = "10"))
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
    @Operation(
            summary = "Find subscription by ID",
            description = "Return a single subscription. Only creator can access their subscriptions."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(schema = @Schema(implementation = UserSubscriptionWithLinks.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "Subscription not found.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "503", description = "Arlas Subscriptions Manager Error.",
                    content = @Content(schema = @Schema(implementation = Error.class)))})

    public Response get(@Context UriInfo uriInfo,

                        // --------------------------------------------------------
                        // ----------------------- FORM -----------------------
                        // --------------------------------------------------------
                        @Parameter(name = "id",
                                description = "ID of subscription to return",
                                required = true)
                        @PathParam(value = "id") String id,

                        @Parameter(name = "deleted",
                                description = "Filter subscriptions whether they are deleted or not.",
                                schema = @Schema(defaultValue = "true") )
                        @QueryParam(value = "deleted") Boolean deleted,

                        @Parameter(name = "pretty",
                                description = "Pretty print",
                                schema = @Schema(defaultValue = "false"))
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
    @Operation(
            summary = "Delete a subscription",
            description = "Mark a subscription as deleted."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Subscription has been deleted.",
                    content = @Content(schema = @Schema(implementation = UserSubscriptionWithLinks.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "Subscription not found.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "503", description = "Arlas Subscriptions Manager Error.",
                    content = @Content(schema = @Schema(implementation = Error.class)))})

    public Response delete(
            // --------------------------------------------------------
            // ----------------------- FORM -----------------------
            // --------------------------------------------------------
            @Parameter(name = "id",
                    description = "Subscription ID to delete",
                    required = true)
            @PathParam(value = "id") String id,

            @Parameter(name = "pretty", description = "Pretty print",
                    schema = @Schema(defaultValue = "false"))
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
    @Operation(
            summary = "Register a new subscription",
            description = "Register a subscription for further notification."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Subscription has been registered",
                    content = @Content(schema = @Schema(implementation = UserSubscriptionWithLinks.class))),
            @ApiResponse(responseCode = "400", description = "JSON parameter malformed.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "503", description = "Arlas Subscriptions Manager Error.",
                    content = @Content(schema = @Schema(implementation = Error.class)))})

    public Response post(@Context UriInfo uriInfo,

                         // --------------------------------------------------------
                         // ----------------------- FORM -----------------------
                         // --------------------------------------------------------
                         @Parameter(name = "subscription",
                                 description = "Subscription description",
                                 required = true)
                         @NotNull @Valid UserSubscription subscription,

                         @Parameter(name = "pretty", description = "Pretty print",
                                 schema = @Schema(defaultValue = "false"))
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
    @Operation(
            summary = "Update an existing subscription",
            description = "Update an existing subscription. "
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successful operation",
                    content = @Content(schema = @Schema(implementation = UserSubscriptionWithLinks.class))),
            @ApiResponse(responseCode = "400", description = "JSON parameter malformed.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "Not Found Error.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "503", description = "Arlas Subscriptions Manager Error.",
                    content = @Content(schema = @Schema(implementation = Error.class)))})

    public Response put(@Context UriInfo uriInfo,

                        // --------------------------------------------------------
                        // ----------------------- FORM -----------------------
                        // --------------------------------------------------------
                        @Parameter(name = "id",
                                description = "ID of subscription to return",
                                required = true)
                        @PathParam(value = "id") String id,

                        @Parameter(name = "subscription",
                                description = "Subscription description",
                                required = true)
                        @NotNull @Valid UserSubscription updUserSubscription,

                        @Parameter(name = "pretty", description = "Pretty print",
                                schema = @Schema(defaultValue = "false"))
                        @QueryParam(value = "pretty") Boolean pretty

    ) throws ArlasSubscriptionsException {

        logger.debug(String.format("Admin requests update of subscription %s", id));
        UserSubscription oldUserSubscription = subscriptionManagerService.getUserSubscription(id, Optional.empty(), false)
                .orElseThrow(() -> new NotFoundException("Subscription with id " + id + " not found"));

        return ResponseFormatter.getCreatedResponse(uriInfo.getRequestUriBuilder().build(),
                halService.subscriptionWithLinks(subscriptionManagerService.putUserSubscription(oldUserSubscription, updUserSubscription), uriInfo));
    }
}
