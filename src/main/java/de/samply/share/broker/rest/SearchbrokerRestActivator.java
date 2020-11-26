package de.samply.share.broker.rest;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import org.eclipse.microprofile.openapi.annotations.servers.Server;
import org.eclipse.microprofile.openapi.annotations.servers.ServerVariable;

@ApplicationPath("rest")
@Server(
    description = "Develop",
    url = "{protocol}://{host}:{port}/{route}/",
    variables = {
        @ServerVariable(
            name = "protocol",
            description = "http/https",
            enumeration = {
                "http",
                "https"
            },
            defaultValue = "http"),
        @ServerVariable(
            name = "host",
            description = "localhost",
            enumeration = {
                "localhost",
                "samplelocator.bbmri.de",
                "samplelocator.test.bbmri.de"
            },
            defaultValue = "localhost"),
        @ServerVariable(
            name = "port",
            description = "port",
            enumeration = {"8080", "8081", "8085", "80", ""},
            defaultValue = "8080"),
        @ServerVariable(
            name = "route",
            description = "route",
            enumeration = {
                "",
                "broker",
                "searchbroker"
            },
            defaultValue = "broker"),
    })
public class SearchbrokerRestActivator extends Application {

}
