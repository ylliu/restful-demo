package com.ylliu.restful.rest.service;

import com.ylliu.restful.dao.ConfigurationDB;
import com.ylliu.restful.rest.domain.Configuration;
import com.ylliu.restful.rest.domain.Configurations;
import com.ylliu.restful.rest.domain.common.Message;
import com.ylliu.restful.rest.domain.common.Status;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.List;

/**
 * This REST resource has common path "/configurations" and
 * represents configurations collection resources
 * as well as individual collection resources.
 * <p>
 * Default MIME type for this resource is "application/XML"
 */
@Path("/configurations")
@Produces("application/xml")
public class ConfigurationResource {
    /**
     * Initialize the application with these two default configurations
     * */
    static {
        ConfigurationDB.createConfiguration("Some Content", Status.ACTIVE);
        ConfigurationDB.createConfiguration("Some More Content", Status.INACTIVE);
    }

    /**
     * Use uriInfo to get current context path and to build HATEOAS links
     */
    @Context
    UriInfo uriInfo;

    /**
     * Get configurations collection resource mapped at path "HTTP GET /configurations"
     */
    @GET
    public Configurations getConfigurations() {

        List<com.ylliu.restful.rest.domain.Configuration> list = ConfigurationDB.getAllConfigurations();

        Configurations configurations = new Configurations();
        configurations.setConfigurations(list);
        configurations.setSize(list.size());

        //Set link for primary collection
        Link link = Link.fromUri(uriInfo.getPath()).rel("uri").build();
        configurations.setLink(link);

        //Set links in configuration items
        for (com.ylliu.restful.rest.domain.Configuration c : list) {
            Link lnk = Link.fromUri(uriInfo.getPath() + "/" + c.getId()).rel("self").build();
            c.setLink(lnk);
        }
        return configurations;
    }

    /**
     * Get individual configuration resource mapped at path "HTTP GET /configurations/{id}"
     */
    @GET
    @Path("/{id}")
    public Response getConfigurationById(@PathParam("id") Integer id) {
        com.ylliu.restful.rest.domain.Configuration config = ConfigurationDB.getConfiguration(id);

        if (config == null) {
            return Response.status(javax.ws.rs.core.Response.Status.NOT_FOUND)
                    .build();
        }

        if (config != null) {
            UriBuilder builder = UriBuilder.fromResource(ConfigurationResource.class)
                    .path(ConfigurationResource.class, "getConfigurationById");
            Link link = Link.fromUri(builder.build(id))
                    .rel("self")
                    .build();
            config.setLink(link);
        }

        return Response.status(javax.ws.rs.core.Response.Status.OK)
                .entity(config)
                .build();
    }

    /**
     * Create NEW configuration resource in configurations collection resource
     */
    @POST
    @Consumes("application/xml")
    public Response createConfiguration(com.ylliu.restful.rest.domain.Configuration config) {
        if (config.getContent() == null) {
            return Response.status(javax.ws.rs.core.Response.Status.BAD_REQUEST)
                    .entity(new Message("Config content not found"))
                    .build();
        }

        Integer id = ConfigurationDB.createConfiguration(config.getContent(), config.getStatus());
        Link lnk = Link.fromUri(uriInfo.getPath() + "/" + id).rel("self")
                .build();
        return Response.status(javax.ws.rs.core.Response.Status.CREATED)
                .location(lnk.getUri())
                .build();
    }

    /**
     * Modify EXISTING configuration resource by it’s "id" at path "/configurations/{id}"
     */
    @PUT
    @Path("/{id}")
    @Consumes("application/xml")
    public Response updateConfiguration(@PathParam("id") Integer id, com.ylliu.restful.rest.domain.Configuration config) {

        com.ylliu.restful.rest.domain.Configuration origConfig = ConfigurationDB.getConfiguration(id);
        if (origConfig == null) {
            return Response.status(javax.ws.rs.core.Response.Status.NOT_FOUND)
                    .build();
        }

        if (config.getContent() == null) {
            return Response.status(javax.ws.rs.core.Response.Status.BAD_REQUEST)
                    .entity(new Message("Config content not found"))
                    .build();
        }

        ConfigurationDB.updateConfiguration(id, config);
        return Response.status(javax.ws.rs.core.Response.Status.OK)
                .entity(new Message("Config Updated Successfully"))
                .build();
    }

    /**
     * Delete configuration resource by it’s "id" at path "/configurations/{id}"
     */
    @DELETE
    @Path("/{id}")
    public Response deleteConfiguration(@PathParam("id") Integer id) {

        Configuration origConfig = ConfigurationDB.getConfiguration(id);
        if (origConfig == null) {
            return Response.status(javax.ws.rs.core.Response.Status.NOT_FOUND).build();
        }

        ConfigurationDB.removeConfiguration(id);
        return Response.status(javax.ws.rs.core.Response.Status.OK).build();
    }
}